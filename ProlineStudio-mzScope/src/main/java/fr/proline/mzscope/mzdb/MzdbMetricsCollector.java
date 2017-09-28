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
import fr.profi.mzdb.db.model.params.ScanList;
import fr.profi.mzdb.db.model.params.ScanParamTree;
import fr.profi.mzdb.db.model.params.param.CVParam;
import fr.profi.mzdb.model.DataEncoding;
import fr.profi.mzdb.model.SpectrumHeader;
import fr.proline.mzscope.model.QCMetrics;
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

    public static Map<String, Object> getFileFormatData(MzDbReader reader) throws SQLiteException {
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

    public static QCMetrics getMSMetrics(MzdbRawFile rawFile) throws SQLiteException {
        // Respect insertion order of keys : LinkedHashMap used
        QCMetrics metrics = new QCMetrics(rawFile);
        MzDbReader reader = rawFile.getMzDbReader();
        
        metrics.addMetric("Cycles count", reader.getCyclesCount());

        int maxMsLevel = reader.getMaxMsLevel();
        DescriptiveStatistics[] TICStatistics = new DescriptiveStatistics[maxMsLevel];
        DescriptiveStatistics[] SumTICStatistics = new DescriptiveStatistics[maxMsLevel];
        DescriptiveStatistics[] RTStatistics = new DescriptiveStatistics[maxMsLevel];
        DescriptiveStatistics[] PeaksCountStatistics = new DescriptiveStatistics[maxMsLevel];
        DescriptiveStatistics[] InjectionTimeStatistics = new DescriptiveStatistics[maxMsLevel];

        metrics.addMetric("Max MS level", maxMsLevel);
        
        for (int msLevel = 1; msLevel <= maxMsLevel; msLevel++) {
            StringBuilder labelBuilder = new StringBuilder();
            labelBuilder.append("MS").append(msLevel).append(" Spectra count");
            metrics.addMetric(labelBuilder.toString(), reader.getSpectraCount(msLevel));
            int[] range = reader.getMzRange(msLevel);
            labelBuilder = new StringBuilder();
            labelBuilder.append("MS").append(msLevel).append(" min m/z");
            metrics.addMetric(labelBuilder.toString(), range[0]);
            labelBuilder = new StringBuilder();
            labelBuilder.append("MS").append(msLevel).append(" max m/z");
            metrics.addMetric(labelBuilder.toString(), range[1]);
            // prepare statistics collector for next step
            SumTICStatistics[msLevel - 1] = new DescriptiveStatistics();
            TICStatistics[msLevel - 1] = new DescriptiveStatistics();
            RTStatistics[msLevel - 1] = new DescriptiveStatistics();
            PeaksCountStatistics[msLevel - 1] = new DescriptiveStatistics();
            InjectionTimeStatistics[msLevel - 1] = new DescriptiveStatistics();
        }

        SpectrumHeader[] headers = reader.getSpectrumHeaders();
        SpectrumHeader.loadScanLists(headers, reader.getConnection());

        metrics.addMetric("RT start", (int) Math.round(headers[0].getTime() / 60.0));
        metrics.addMetric("RT end", (int) Math.round(headers[headers.length - 1].getTime() / 60.0));
        float duration = (headers[headers.length - 1].getTime() - headers[0].getTime()) / 60.0f;
        double ticSum = 0.0d;
        metrics.addMetric("RT duration", (int) Math.round(duration));
        // charge states will be sorted by their natural order : TreeMap used
        Map<Integer, Integer> chargeStates = new TreeMap<>();

        for (SpectrumHeader header : headers) {

            ScanList sl = header.getScanList();
            if (sl == null) {
                header.loadScanList(reader.getConnection());
            }
            if (sl != null) {
                List<ScanParamTree> scans = sl.getScans();
                if (scans != null && !scans.isEmpty()) {
                    ScanParamTree spt = scans.get(0);
                    for (CVParam cvParam : spt.getCVParams()) {
                        if (cvParam.getAccession().equals("MS:1000927")) {
                            InjectionTimeStatistics[header.getMsLevel() - 1].addValue(Double.valueOf(cvParam.getValue()));
                            break;
                        }
                    }
                }
            } 

            ticSum += header.getTIC();
            SumTICStatistics[header.getMsLevel() - 1].addValue(ticSum);
            TICStatistics[header.getMsLevel() - 1].addValue(header.getTIC());
            RTStatistics[header.getMsLevel() - 1].addValue(header.getTime() / 60.0);
            RTStatistics[0].addValue(header.getTime() / 60.0);
            PeaksCountStatistics[header.getMsLevel() - 1].addValue(header.getPeaksCount());
            if ((header.getMsLevel() == 2) && (header.getPrecursorCharge() > 0)) {
                if (!chargeStates.containsKey(header.getPrecursorCharge())) {
                    chargeStates.put(header.getPrecursorCharge(), 1);
                } else {
                    chargeStates.put(header.getPrecursorCharge(), chargeStates.get(header.getPrecursorCharge()) + 1);
                }
            }
        }

        metrics.addMetric("MS  Sum TIC ", SumTICStatistics);
        metrics.addMetric("MS TIC", TICStatistics);
        metrics.addMetric("RT events ", RTStatistics[0]);
        metrics.addMetric("RT MS events ", RTStatistics);
        metrics.addMetric("MS peaks count", PeaksCountStatistics);
        metrics.addMetric("MS injection time", InjectionTimeStatistics);


        for (Map.Entry<Integer, Integer> e : chargeStates.entrySet()) {
            String label = new StringBuilder().append("MS2 precursor ").append(e.getKey()).append("+").toString();
            metrics.addMetric(label, e.getValue());
        }
        return metrics;
    }

}
