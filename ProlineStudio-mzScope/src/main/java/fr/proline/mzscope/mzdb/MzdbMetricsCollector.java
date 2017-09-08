/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.mzscope.mzdb;

import com.almworks.sqlite4java.SQLiteException;
import fr.profi.mzdb.MzDbReader;
import fr.profi.mzdb.db.model.Run;
import fr.profi.mzdb.db.model.Sample;
import fr.profi.mzdb.db.model.params.ParamTree;
import fr.profi.mzdb.db.model.params.ScanList;
import fr.profi.mzdb.db.model.params.ScanParamTree;
import fr.profi.mzdb.db.model.params.param.CVParam;
import fr.profi.mzdb.model.DataEncoding;
import fr.profi.mzdb.model.SpectrumHeader;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author CB205360
 */
public class MzdbMetricsCollector {

    private static final Logger logger = LoggerFactory.getLogger(MzdbMetricsCollector.class);

    public static Map<String, Object> getFileFormaData(MzDbReader reader) throws SQLiteException {
        // Respect insertion order of keys : LinkedHashMap used
        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("Filename", reader.getFirstSourceFileName());

        List<Run> runs = reader.getRuns();
        if ((runs != null) && (!runs.isEmpty())) {
            metrics.put("Acquisition date", runs.get(0).getStartTimestamp());
        }
        metrics.put("Format", "mzdb");
        metrics.put("Format version", reader.getModelVersion());

        metrics.put("PWIZ version", reader.getPwizMzDbVersion());
        metrics.put("Source file", reader.getFirstSourceFileName());
        metrics.put("Acquisition mode", reader.getAcquisitionMode().name());

        List<Sample> samples = reader.getSamples();
        if ((samples != null) && !samples.isEmpty()) {
            metrics.put("Samples", samples.stream().map(Sample::getName).collect(Collectors.joining(",")));
        }

        SpectrumHeader firstHeader = reader.getMs1SpectrumHeaders()[0];
        DataEncoding encoding = reader.getSpectrumDataEncoding(firstHeader.getId());
        metrics.put("MS1 encoding", encoding.getMode().name());

        firstHeader = reader.getMs2SpectrumHeaders()[0];
        encoding = reader.getSpectrumDataEncoding(firstHeader.getId());
        metrics.put("MS2 encoding", encoding.getMode().name());

        return metrics;
    }

    public static Map<String, Object> getMSMetrics(MzDbReader reader) throws SQLiteException {
        // Respect insertion order of keys : LinkedHashMap used
        Map<String, Object> metrics = new LinkedHashMap<>();

        metrics.put("Cycles count", reader.getCyclesCount());

        int maxMsLevel = reader.getMaxMsLevel();
        DescriptiveStatistics[] TICStatistices = new DescriptiveStatistics[maxMsLevel];
        DescriptiveStatistics[] SumTICStatistices = new DescriptiveStatistics[maxMsLevel];
        DescriptiveStatistics[] RTStatistices = new DescriptiveStatistics[maxMsLevel];
        DescriptiveStatistics[] PeaksCountStatistices = new DescriptiveStatistics[maxMsLevel];
        DescriptiveStatistics[] InjectionTimeStatistices = new DescriptiveStatistics[maxMsLevel];

        metrics.put("Max MS level", maxMsLevel);
        for (int msLevel = 1; msLevel <= maxMsLevel; msLevel++) {
            StringBuilder labelBuilder = new StringBuilder();
            labelBuilder.append("MS").append(msLevel).append(" Spectra count");
            metrics.put(labelBuilder.toString(), reader.getSpectraCount(msLevel));
            int[] range = reader.getMzRange(msLevel);
            labelBuilder = new StringBuilder();
            labelBuilder.append("MS").append(msLevel).append(" min m/z");
            metrics.put(labelBuilder.toString(), range[0]);
            labelBuilder = new StringBuilder();
            labelBuilder.append("MS").append(msLevel).append(" max m/z");
            metrics.put(labelBuilder.toString(), range[1]);
            // prepare statistics collector for next step
            SumTICStatistices[msLevel - 1] = new DescriptiveStatistics();
            TICStatistices[msLevel - 1] = new DescriptiveStatistics();
            RTStatistices[msLevel - 1] = new DescriptiveStatistics();
            PeaksCountStatistices[msLevel - 1] = new DescriptiveStatistics();
            InjectionTimeStatistices[msLevel - 1] = new DescriptiveStatistics();
        }

        SpectrumHeader[] headers = reader.getSpectrumHeaders();
        SpectrumHeader.loadScanLists(headers, reader.getConnection());
        
        metrics.put("RT start", (int) Math.round(headers[0].getTime() / 60.0));
        metrics.put("RT end", (int) Math.round(headers[headers.length - 1].getTime() / 60.0));
        float duration = (headers[headers.length - 1].getTime() - headers[0].getTime()) / 60.0f;
        double ticSum = 0.0d;
        metrics.put("RT duration", (int) Math.round(duration));
        // charge states will be sorted by their natural order : TreeMap used
        Map<Integer, Integer> chargeStates = new TreeMap<>();

        for (SpectrumHeader header : headers) {

            ScanList sl = header.getScanList();
            if (sl != null) {
                List<ScanParamTree> scans = sl.getScans();
                if (scans != null && !scans.isEmpty()) {
                    ScanParamTree spt = scans.get(0);
                    for (CVParam cvParam : spt.getCVParams()) {
                        if (cvParam.getAccession().equals("MS:1000927")) {
                            InjectionTimeStatistices[header.getMsLevel() - 1].addValue(Double.valueOf(cvParam.getValue()));
                            break;
                        }
                    }
                }
            } 
            ParamTree pt = header.getParamTree(reader.getConnection());

            ticSum += header.getTIC();
            SumTICStatistices[header.getMsLevel() - 1].addValue(ticSum);
            TICStatistices[header.getMsLevel() - 1].addValue(header.getTIC());
            RTStatistices[header.getMsLevel() - 1].addValue(header.getTime() / 60.0);
            PeaksCountStatistices[header.getMsLevel() - 1].addValue(header.getPeaksCount());
            if ((header.getMsLevel() == 2) && (header.getPrecursorCharge() > 0)) {
                if (!chargeStates.containsKey(header.getPrecursorCharge())) {
                    chargeStates.put(header.getPrecursorCharge(), 1);
                } else {
                    chargeStates.put(header.getPrecursorCharge(), chargeStates.get(header.getPrecursorCharge()) + 1);
                }
            }
        }

        for (int msLevel = 1; msLevel <= maxMsLevel; msLevel++) {
            if (msLevel == 1) {
                // doeant make sens for msLevel > 1
                String label = new StringBuilder().append("MS").append(msLevel).append(" Sum TIC ").toString();
                metrics.put(label + "Q1", SumTICStatistices[msLevel - 1].getPercentile(25));
                metrics.put(label + "Q2", SumTICStatistices[msLevel - 1].getPercentile(50));
                metrics.put(label + "Q3", SumTICStatistices[msLevel - 1].getPercentile(75));
                metrics.put(label + "Q4", SumTICStatistices[msLevel - 1].getPercentile(100));

                label = new StringBuilder().append("MS").append(msLevel).append(" TIC ").toString();
                metrics.put(label + "log10(Q2/Q1)", Math.log10(TICStatistices[msLevel - 1].getPercentile(50) / TICStatistices[msLevel - 1].getPercentile(25)));
                metrics.put(label + "log10(Q3/Q2)", Math.log10(TICStatistices[msLevel - 1].getPercentile(75) / TICStatistices[msLevel - 1].getPercentile(50)));
                metrics.put(label + "log10(Q4/Q3)", Math.log10(TICStatistices[msLevel - 1].getPercentile(100) / TICStatistices[msLevel - 1].getPercentile(75)));
            }

            String label = new StringBuilder().append("RTnorm MS").append(msLevel).append(" events ").toString();
            metrics.put(label + "Q1", RTStatistices[msLevel - 1].getPercentile(25) / duration);
            metrics.put(label + "Q2", RTStatistices[msLevel - 1].getPercentile(50) / duration);
            metrics.put(label + "Q3", RTStatistices[msLevel - 1].getPercentile(75) / duration);
            metrics.put(label + "Q4", RTStatistices[msLevel - 1].getPercentile(100) / duration);

            label = new StringBuilder().append("MS").append(msLevel).append(" peaks density ").toString();
            metrics.put(label + "Q1", PeaksCountStatistices[msLevel - 1].getPercentile(25));
            metrics.put(label + "Q2", PeaksCountStatistices[msLevel - 1].getPercentile(50));
            metrics.put(label + "Q3", PeaksCountStatistices[msLevel - 1].getPercentile(75));
            metrics.put(label + "Q4", PeaksCountStatistices[msLevel - 1].getPercentile(100));
            
            label = new StringBuilder().append("MS").append(msLevel).append(" injection time ").toString();
            metrics.put(label + "Q1", InjectionTimeStatistices[msLevel - 1].getPercentile(25));
            metrics.put(label + "Q2", InjectionTimeStatistices[msLevel - 1].getPercentile(50));
            metrics.put(label + "Q3", InjectionTimeStatistices[msLevel - 1].getPercentile(75));
            metrics.put(label + "Q4", InjectionTimeStatistices[msLevel - 1].getPercentile(100));
        }

        for (Map.Entry<Integer, Integer> e : chargeStates.entrySet()) {
            String label = new StringBuilder().append("MS2 precursor ").append(e.getKey()).append("+").toString();
            metrics.put(label, e.getValue());
        }
        return metrics;
    }

}
