package fr.proline.mzscope.mzdb;

import com.almworks.sqlite4java.SQLiteException;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Floats;
import fr.profi.ms.model.TheoreticalIsotopePattern;
import fr.profi.mzdb.FeatureDetectorConfig;
import fr.profi.mzdb.MzDbFeatureDetector;
import fr.profi.mzdb.MzDbFeatureExtractor;
import fr.profi.mzdb.MzDbReader;
import fr.profi.mzdb.SmartPeakelFinderConfig;
import fr.profi.mzdb.algo.IsotopicPatternScorer;
import fr.profi.mzdb.algo.feature.extraction.FeatureExtractorConfig;
import fr.profi.mzdb.io.reader.provider.RunSliceDataProvider;
import fr.profi.mzdb.io.writer.MsSpectrumTSVWriter;
import fr.profi.mzdb.io.writer.mgf.MgfWriter;
import fr.profi.mzdb.model.AcquisitionMode;
import fr.profi.mzdb.model.Feature;
import fr.profi.mzdb.model.Peak;
import fr.profi.mzdb.model.Peakel;
import fr.profi.mzdb.model.PutativeFeature;
import fr.profi.mzdb.model.RunSlice;
import fr.profi.mzdb.model.SpectrumData;
import fr.profi.mzdb.model.SpectrumHeader;
import fr.profi.mzdb.model.SpectrumSlice;
import fr.proline.mzscope.model.Chromatogram;
import fr.proline.mzscope.model.FeaturesExtractionRequest;
import fr.proline.mzscope.model.IExportParameters;
import fr.proline.mzscope.model.IExportParameters.ExportType;
import fr.proline.mzscope.model.IFeature;
import fr.proline.mzscope.model.Spectrum;
import fr.proline.mzscope.model.IRawFile;
import fr.proline.mzscope.model.MsnExtractionRequest;
import fr.proline.mzscope.ui.MgfExportParameters;
import fr.proline.mzscope.ui.ScanHeaderExportParameters;
import fr.proline.mzscope.ui.ScanHeaderType;
import fr.proline.mzscope.utils.SpectrumUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StreamCorruptedException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Option;
import scala.Tuple2;
import scala.collection.JavaConverters;

/**
 *
 * @author CB205360
 */
public class MzdbRawFile implements IRawFile {

    private static final Logger logger = LoggerFactory.getLogger(MzdbRawFile.class);
    final private static DecimalFormat massFormatter = new DecimalFormat("0.####");
    final private static DecimalFormat timeFormatter = new DecimalFormat("0.00");

    private final File mzDbFile;
    private MzDbReader reader;
    private SpectrumHeader[] ms2SpectrumHeaders = null;
    private double[] ms2SpectrumHeaderByMz = null;
    
    private boolean isDIAFile;

    public MzdbRawFile(File file) {
        mzDbFile = file;
        init();
    }

    public MzDbReader getMzDbReader() {
        return reader;
    }

    private void init() {
        try {
            reader = new MzDbReader(mzDbFile, true);
            long start = System.currentTimeMillis();
            isDIAFile= checkDIAFile();
            logger.debug("MzdbRawFile "+getName()+(isDIAFile?" is ": " is not ")+" a DIA File in "+(System.currentTimeMillis() - start)+" ms");
        } catch (ClassNotFoundException | FileNotFoundException | SQLiteException e) {
            logger.error("cannot read file " + mzDbFile.getAbsolutePath(), e);
        }
    }

    private void buildMs2SpectrumHeaderIndexes(SpectrumHeader[] spectrums) {
        ms2SpectrumHeaders = SpectrumUtils.sortMs2SpectrumHeaders(spectrums);
        ms2SpectrumHeaderByMz = new double[ms2SpectrumHeaders.length];
        int i = 0;
        for (SpectrumHeader spectrum : ms2SpectrumHeaders) {
            ms2SpectrumHeaderByMz[i] = spectrum.getPrecursorMz();
            i++;
        }
    }

    @Override
    public String getName() {
        return mzDbFile.getName();
    }

    @Override
    public File getFile() {
        return mzDbFile;
    }

    @Override
    public String toString() {
        return mzDbFile.getName();
    }

    @Override
    public Chromatogram getTIC() {
        Chromatogram chromatogram = null;
        try {
            SpectrumHeader[] headers = reader.getSpectrumHeaders();
            double[] xAxisData = new double[headers.length];
            double[] yAxisData = new double[headers.length];
            for (int i = 0; i < headers.length; i++) {
                xAxisData[i] = (headers[i].getElutionTime() / 60.0);
                yAxisData[i] = ((double) headers[i].getTIC());
            }

            chromatogram = new Chromatogram(getName());
            chromatogram.time = xAxisData;
            chromatogram.intensities = yAxisData;
            return chromatogram;
        } catch (SQLiteException ex) {
            logger.error("Cannot generate TIC chromatogram", ex);
        }
        return chromatogram;
    }

    @Override
    public Chromatogram getBPI() {
        Chromatogram chromatogram = null;
        try {
            SpectrumHeader[] headers = reader.getSpectrumHeaders();
            double[] xAxisData = new double[headers.length];
            double[] yAxisData = new double[headers.length];
            for (int i = 0; i < headers.length; i++) {
                xAxisData[i] = (headers[i].getElutionTime() / 60.0);
                yAxisData[i] = ((double) headers[i].getBasePeakIntensity());
            }

            chromatogram = new Chromatogram(getName());
            chromatogram.time = xAxisData;
            chromatogram.intensities = yAxisData;
            return chromatogram;
        } catch (SQLiteException ex) {
            logger.error("Cannot generate BPI chromatogram", ex);
        }
        return chromatogram;
    }

    /**
     *
     * @param params
     * @return
     */
    @Override
    public Chromatogram getXIC(MsnExtractionRequest params) {
        long start = System.currentTimeMillis();
        Chromatogram chromatogram = null;
        logger.info("Extract XIC with params : " + params.toString());

        try {
            Peak[] peaks;
            if (params.getMsLevel() > 1){
                peaks = reader.getMsnXIC(params.getMz(), params.getFragmentMz(), params.getFragmentMzTolPPM()*params.getFragmentMz()/1e6, params.getElutionTimeLowerBound(), params.getElutionTimeUpperBound(), params.getMethod());
            } else {
                peaks = reader.getMsXicInMzRtRanges(params.getMinMz(), params.getMaxMz(), params.getElutionTimeLowerBound(), params.getElutionTimeUpperBound(), params.getMethod());
            }
            chromatogram = createChromatoFromPeaks(peaks, params.getMsLevel() );
            chromatogram.minMz = (params.getMsLevel() == 1) ? params.getMinMz() : params.getFragmentMinMz();
            chromatogram.maxMz = (params.getMsLevel() == 1) ? params.getMaxMz() : params.getFragmentMaxMz();
            StringBuilder builder = new StringBuilder("Mass range: ");
            builder.append(massFormatter.format(chromatogram.minMz)).append("-").append(massFormatter.format(chromatogram.maxMz));
            if (params.getMsLevel() > 1) {
                builder.append(" (").append(massFormatter.format(params.getMz()));
            }
            chromatogram.title = builder.toString();
            logger.info("mzdb chromatogram extracted in {} ms", (System.currentTimeMillis() - start));

        } catch (SQLiteException | StreamCorruptedException e) {
            logger.error("Error during chromatogram extraction", e);
        }
        return chromatogram;
    }

    private Chromatogram createChromatoFromPeaks(Peak[] peaks, int msLevel) {
        Chromatogram chromatogram = null;
        List<Double> xAxisData = new ArrayList<>(peaks.length);
        List<Double> yAxisData = new ArrayList<>(peaks.length);
        try {
            int previousSpectrumId = (int) peaks[0].getLcContext().getSpectrumId();
            for (Peak peak : peaks) {
                int spectrumId = (int) peak.getLcContext().getSpectrumId();
                if ((msLevel == 1) && (previousSpectrumId != getPreviousSpectrumId(spectrumId, msLevel))) {
                    // there is a gap between peaks, add 0 values after the previous peak and before this one
                    xAxisData.add(reader.getSpectrumHeaderById().get((long) getNextSpectrumId(previousSpectrumId, msLevel)).getElutionTime() / 60.0);
                    yAxisData.add(0.0);
                    xAxisData.add(reader.getSpectrumHeaderById().get((long) getPreviousSpectrumId(spectrumId, msLevel)).getElutionTime() / 60.0);
                    yAxisData.add(0.0);
                }
                double rt = peak.getLcContext().getElutionTime() / 60.0;
                xAxisData.add(rt);
                yAxisData.add((double) peak.getIntensity());
                previousSpectrumId = (int) peak.getLcContext().getSpectrumId();
            }
        } catch (SQLiteException sle) {
            logger.error("Error while reading mzdb file", sle);
        }
        chromatogram = new Chromatogram(getName());
        chromatogram.time = Doubles.toArray(xAxisData);
        chromatogram.intensities = Doubles.toArray(yAxisData);

        return chromatogram;
    }

    @Override
    public List<IFeature> extractFeatures(FeaturesExtractionRequest params) {
        switch (params.getExtractionMethod()) {
            case EXTRACT_MS2_FEATURES:
                return extractFeaturesFromMs2(params.getMzTolPPM());
            case DETECT_FEATURES:
                return detectFeatures(params.getMzTolPPM(), params.getMinMz(), params.getMaxMz());
            case DETECT_PEAKELS:
                return detectPeakels(params);
        }
        return null;
    }

    private List<IFeature> detectFeatures(float mzTolPPM, double minMz, double maxMz) {
        List<IFeature> result = new ArrayList<>();
        FeatureDetectorConfig detectorConfig = new FeatureDetectorConfig(1, mzTolPPM, 5, new SmartPeakelFinderConfig(5, 3, 0.75f, false, 10, false, true));
        MzDbFeatureDetector detector = new MzDbFeatureDetector(reader, detectorConfig);
        try {
            Iterator<RunSlice> runSlices;
            if (minMz == 0 && maxMz == 0) {
                runSlices = getMzDbReader().getLcMsRunSliceIterator();
            } else {
                runSlices = getMzDbReader().getLcMsRunSliceIterator(minMz, maxMz);
            }
            Peakel[] peakels = detector.detectPeakels(runSlices,Option.empty());

             Iterator<RunSlice> tmpRunSlices = (minMz == 0 && maxMz == 0) ? getMzDbReader().getLcMsRunSliceIterator() : getMzDbReader().getLcMsRunSliceIterator(minMz, maxMz);
             logSliceBounds(tmpRunSlices);
             
            Arrays.sort(peakels, new Comparator<Peakel>() {
                @Override
                public int compare(Peakel p1, Peakel p2) {
                    return Double.compare(p2.getApexIntensity(), p1.getApexIntensity());
                }
            });
            logger.info("Peakels detected : " + peakels.length);
            result = deisotopePeakels(peakels, mzTolPPM);
        } catch (SQLiteException | StreamCorruptedException ex) {
            logger.error("Error while getting LcMs RunSlice Iterator: " + ex);
        }
        return result;

    }

   private void logSliceBounds(Iterator<RunSlice> tmpRunSlices) {
      double min = Double.MAX_VALUE;
      double max = Double.MIN_VALUE;
      for (; tmpRunSlices.hasNext();) {
         RunSlice rs = tmpRunSlices.next();
         min = Math.min(min, rs.getHeader().getBeginMz());
         max = Math.max(max, rs.getHeader().getEndMz());
      }
      logger.info("Real bounds : " + min + " - " + max);
   }
    
    private List<IFeature> deisotopePeakels(Peakel[] peakels, float mzTolPPM) throws StreamCorruptedException, SQLiteException {
        List<IFeature> result = new ArrayList<>();
        boolean[] assigned = new boolean[peakels.length];
        Arrays.fill(assigned, false);
        Pair<Double, Integer> peakelIndexesByMz[] = new Pair[peakels.length];
        for (int k = 0; k < peakels.length; k++) {
            peakelIndexesByMz[k] = new ImmutablePair<>(peakels[k].getMz(), k);
        }

        Arrays.sort(peakelIndexesByMz, new Comparator<Pair<Double, Integer>>() {
            @Override
            public int compare(Pair<Double, Integer> p1, Pair<Double, Integer> p2) {
                return Double.compare(p1.getLeft(), p2.getLeft());
            }
        });

        for (int k = 0; k < peakels.length; k++) {
            if ((k % 10000) == 0) {
                logger.info("processing peakel " + k);
            }
            if (!assigned[k]) {
                SpectrumSlice[] slices = reader.getMsSpectrumSlices(peakels[k].getApexMz() - 5.0, peakels[k].getApexMz() + 5.0, peakels[k].getApexElutionTime() - 0.1f, peakels[k].getApexElutionTime() + 0.1f);
                int i = 0;
                while ((i < slices.length) && (slices[i].getHeader().getSpectrumId() != peakels[k].getApexSpectrumId())) {
                    i++;
                }
                SpectrumData data = slices[i].getData();

                Tuple2<Object, TheoreticalIsotopePattern>[] putativePatterns = IsotopicPatternScorer.calcIsotopicPatternHypotheses(data, peakels[k].getMz(), mzTolPPM);
                //TreeMap<Double, TheoreticalIsotopePattern> putativePatterns = IsotopePattern.getOrderedIPHypothesis(data, peakels[k].getMz());
                TheoreticalIsotopePattern bestPattern = putativePatterns[0]._2;
                List<Peakel> l = new ArrayList<>(bestPattern.isotopeCount() + 1);
                
                for (Tuple2 t : bestPattern.mzAbundancePairs()) {
                    int idx = findPeakIndexByCorrelation(peakels, peakelIndexesByMz, (double) t._1, peakels[k], mzTolPPM);
                    if (idx != -1) {
                        assigned[idx] = true;
                        l.add(peakels[idx]);
                    } //else if ( ((double)t._1 > min) && ((double)t._1 < max)) {
//                     logger.info("Isotope at "+(double)t._1+" not found");
//                  }
                }
                if (l.isEmpty()) {
                    logger.warn("Strange situation : peakel not found within isotopic pattern .... " + peakels[k].getMz());
                    l.add(peakels[k]);
                }
               logger.info("Creates feature with "+l.size()+" peakels at mz="+l.get(0).getMz()+ " from peakel "+peakels[k].getMz()+ " at "+peakels[k].getApexElutionTime()/60.0);                
                Feature feature = new Feature(l.get(0).getMz(), bestPattern.charge(), JavaConverters.asScalaBufferConverter(l).asScala(), true);
                result.add(new MzdbFeatureWrapper(feature, this, 1));
            }
        }

        logger.info("Features detected : " + result.size());
        return result;
    }

    private List<IFeature> detectPeakels(FeaturesExtractionRequest params) {

        List<IFeature> result = new ArrayList<>();
        logger.info("Extract peakels with params : " + params.toString());

        try {
            Iterator<RunSlice> runSlices;
            if (params.getMinMz() <= 0 && params.getMaxMz() <= 0) {
                if(params.isMsnExtraction()){
                    runSlices = getMzDbReader().getLcMsnRunSliceIterator(params.getMinMz(), params.getMaxMz());
                }else{
                    runSlices = getMzDbReader().getLcMsRunSliceIterator();
                }
            } else {
                if (params.isMsnExtraction()){
                    if (params.getFragmentMinMz() <= 0 && params.getFragmentMaxMz() <= 0) {
                        runSlices = getMzDbReader().getLcMsnRunSliceIterator(params.getMinMz(), params.getMaxMz());
                    } else {
                        runSlices = getMzDbReader().getLcMsnRunSliceIterator(params.getMinMz(), params.getMaxMz(), params.getFragmentMinMz(), params.getFragmentMaxMz());
                    }
                }else{
                    runSlices = getMzDbReader().getLcMsRunSliceIterator(params.getMinMz(), params.getMaxMz());
                }
            }
            
            
            FeatureDetectorConfig detectorConfig = null;
            if (params.isMsnExtraction()) {
                detectorConfig = new FeatureDetectorConfig(2, params.getFragmentMzTolPPM(), 5, new SmartPeakelFinderConfig(5, 3, 0.75f, false, 10, false, params.isRemoveBaseline()));
            } else {
                detectorConfig = new FeatureDetectorConfig(1, params.getMzTolPPM(), 5, new SmartPeakelFinderConfig(5, 3, 0.75f, false, 10, false, params.isRemoveBaseline()));                
            }
            
            MzDbFeatureDetector detector = new MzDbFeatureDetector(reader, detectorConfig);
            
            Peakel[] peakels = detector.detectPeakels(runSlices, Option.empty());
            double min = (params.getMsLevel() == 1) ? params.getMinMz() : params.getFragmentMinMz();
            double max = (params.getMsLevel() == 1) ? params.getMaxMz() : params.getFragmentMaxMz();
            
            for (Peakel peakel : peakels) {
                //creates a fake Feature associated to this peakel in order to always display Features
                IFeature feature = (params.getMsLevel() == 1) ? new MzdbPeakelWrapper(peakel, this) : new MzdbPeakelWrapper(peakel, this, getMzDbReader().getSpectrumHeader(peakel.getApexSpectrumId()).getPrecursorMz());
                if (min <= 0 && max <= 0) {
                    result.add(feature);
                } else {
                    //check that the feature is in the mass range
                    if (feature.getMz() >= min && feature.getMz() <= max) {
                        result.add(feature);
                    }
                }
            }
        } catch (SQLiteException | StreamCorruptedException ex) {
            logger.error("Error while getting LcMs RunSlice Iterator: " + ex);
        }
        return result;
    }

    private List<IFeature> extractFeaturesFromMs2(float tolPPM) {
        List<IFeature> result = null;
        try {
            logger.info("retrieve spectrum headers...");
            SpectrumHeader[] ms2SpectrumHeaders = reader.getMs2SpectrumHeaders();

            List<PutativeFeature> pfs = new ArrayList<PutativeFeature>();
            logger.info("building putative features list from MS2 spectrum events...");
            for (SpectrumHeader spectrumH : ms2SpectrumHeaders) {
                pfs.add(new PutativeFeature(
                        PutativeFeature.generateNewId(),
                        spectrumH.getPrecursorMz(),
                        spectrumH.getPrecursorCharge(),
                        spectrumH.getId(),
                        2
                ));

            }
            List<Feature> mzdbResult = extractFeatures(pfs, tolPPM);
            for(Feature f : mzdbResult) {
                result.add(new MzdbFeatureWrapper(f, this, 1));
            }
        } catch (SQLiteException ex) {
            logger.error("error while extracting features", ex);
        }
        return result;
    }

    private List<Feature> extractFeatures(List<PutativeFeature> pfs, float tolPPM) {
        List<Feature> result = new ArrayList();
        try {
            // Instantiates a Run Slice Data provider
            RunSliceDataProvider rsdProv = new RunSliceDataProvider(reader.getLcMsRunSliceIterator());
            FeatureExtractorConfig extractorConfig = new FeatureExtractorConfig(tolPPM, 5, 1, 3, 1200.0f, 0.05f, Option.empty(), 90, Option.empty());
            MzDbFeatureExtractor extractor = new MzDbFeatureExtractor(reader, 5, 5, extractorConfig);
            // Extract features
            // force the result in a java List to avoid UnsupportedOperationException on scala.collection...WrappedSequence
            List<Feature> tmpresult = scala.collection.JavaConversions.seqAsJavaList(extractor.extractFeatures(rsdProv, scala.collection.JavaConversions.asScalaBuffer(pfs), tolPPM));
            tmpresult.stream().forEach((f) -> {
                result.add(f);
            });
        } catch (SQLiteException | StreamCorruptedException ex) {
            logger.error("error while extracting features", ex);
        }
        return result;
    }

    @Override
    public Spectrum getSpectrum(int spectrumIndex) {
        Spectrum spectrum = null;
        try {
            fr.profi.mzdb.model.Spectrum rawSpectrum = reader.getSpectrum((long) spectrumIndex);
            SpectrumData data = rawSpectrum.getData();
            Spectrum.ScanType scanType = (rawSpectrum.getHeader().getMsLevel() == 2) ?  Spectrum.ScanType.CENTROID : Spectrum.ScanType.PROFILE;
            final double[] mzList = data.getMzList();
            final double[] leftSigma = new double[mzList.length];
            final double[] rightSigma = new double[mzList.length];
            final float[] intensityList = data.getIntensityList();
            List<Float> xAxisData = new ArrayList<Float>(mzList.length);
            List<Float> yAxisData = new ArrayList<Float>(mzList.length);
            for (int count = 0; count < mzList.length; count++) {
                if ((data.getLeftHwhmList() != null) && data.getLeftHwhmList()[count] > 0) {
                    leftSigma[count] = 2.0 * data.getLeftHwhmList()[count] / 2.35482;
                    double x = mzList[count] - 4.0 * leftSigma[count];
                    //search for the first left value less than x
                    if (!xAxisData.isEmpty()) {
                        int k = xAxisData.size() - 1;
                        while (k >= 0 && xAxisData.get(k) >= x) {
                            k--;
                        }
                        k++;
                        for (; k < xAxisData.size(); k++) {
                            x = xAxisData.get(k);
                            double y = getGaussianPoint(x, mzList[count], intensityList[count], leftSigma[count]);
                            yAxisData.set(k, yAxisData.get(k) + (float) y);
                        }
                    }
                    for (; x < mzList[count]; x += leftSigma[count] / 2.0) {
                        double y = getGaussianPoint(x, mzList[count], intensityList[count], leftSigma[count]);
                        xAxisData.add((float) x);
                        yAxisData.add((float) y);
                    }
                } else {
                    scanType = Spectrum.ScanType.CENTROID;
                }
                xAxisData.add((float) mzList[count]);
                yAxisData.add(intensityList[count]);
                if (data.getRightHwhmList() != null && data.getRightHwhmList()[count] > 0) {
                    rightSigma[count] = 2.0 * data.getRightHwhmList()[count] / 2.35482;
                    double x = mzList[count] + rightSigma[count] / 2.0;
                    if (!xAxisData.isEmpty()) {
                        int k = xAxisData.size() - 1;
                        while (k >= 0 && xAxisData.get(k) > x) {
                            k--;
                        }
                        k++;
                        for (; k < xAxisData.size(); k++) {
                            x = xAxisData.get(k);
                            double y = getGaussianPoint(x, mzList[count], intensityList[count], rightSigma[count]);
                            yAxisData.set(k, yAxisData.get(k) + (float) y);
                        }
                    }
                    for (; x < mzList[count] + 4.0 * rightSigma[count]; x += rightSigma[count] / 2.0) {
                        double y = getGaussianPoint(x, mzList[count], intensityList[count], rightSigma[count]);
                        xAxisData.add((float) x);
                        yAxisData.add((float) y);
                    }
                }  else {
                    scanType = Spectrum.ScanType.CENTROID;
                }

            }
            spectrum = new Spectrum(spectrumIndex, rawSpectrum.getHeader().getElutionTime(), Doubles.toArray(xAxisData), Floats.toArray(yAxisData), rawSpectrum.getHeader().getMsLevel(), scanType);
            StringBuilder builder = new StringBuilder(getName());

            if (spectrum.getMsLevel() == 2) {
                builder.append(massFormatter.format(rawSpectrum.getHeader().getPrecursorMz())).append(" (");
                builder.append(rawSpectrum.getHeader().getPrecursorCharge()).append("+) - ");
                spectrum.setPrecursorMz(rawSpectrum.getHeader().getPrecursorMz());
                spectrum.setPrecursorCharge(rawSpectrum.getHeader().getPrecursorCharge());
            } else {
                spectrum.setPrecursorMz(null);
                spectrum.setPrecursorCharge(null);
            }
            builder.append(", sc=").append(spectrumIndex).append(", rt=").append(timeFormatter.format(rawSpectrum.getHeader().getElutionTime() / 60.0));
            builder.append(", ms").append(spectrum.getMsLevel());
            //spectrum.setTitle(builder.toString());
            spectrum.setTitle("");
            spectrum.setSpectrumData(data);
            //logger.debug("mzdb Spectrum length {} rebuilded in Spectrum length {} ", mzList.length, xAxisData.size());
        } catch (SQLiteException | StreamCorruptedException ex) {
            logger.error("enable to retrieve Spectrum data", ex);
        }catch(Exception e){
            logger.error("Error while retrieving Spectrum data", e);
        }
        return spectrum;
    }

    @Override
    public int getSpectrumId(double retentionTime) {
        try {
            return (int) reader.getSpectrumHeaderForTime((float) retentionTime, 1).getSpectrumId();
        } catch (Exception ex) {
            logger.error("enable to retrieve Spectrum Id", ex);
        }
        return 0;
    }

    @Override
    public int getNextSpectrumId(int spectrumIndex, int msLevel) {
        return getNextSiblingSpectrumId(spectrumIndex, msLevel, 1);
    }

    @Override
    public int getPreviousSpectrumId(int spectrumIndex, int msLevel) {
        return getNextSiblingSpectrumId(spectrumIndex, msLevel, -1);
    }

    private int getNextSiblingSpectrumId(int spectrumIndex, int msLevel, int way) {
        try {
            SpectrumHeader header = reader.getSpectrumHeaderById().get((long) spectrumIndex);
            int maxSpectrum = reader.getSpectraCount();
            long k = Math.max(1, Math.min(maxSpectrum, header.getSpectrumId() + way));
            for (; (k > 0) && (k < maxSpectrum); k += way) {
                if (reader.getSpectrumHeaderById().get(k).getMsLevel() == msLevel) {
                    break;
                }
            }
            return (int) k;
        } catch (SQLiteException e) {
            logger.error("Error while reading spectrumsCount", e);
        }
        return 0;
    }

    private static double getGaussianPoint(double mz, double peakMz, double intensity, double sigma) {
        return intensity * Math.exp(-((mz - peakMz) * (mz - peakMz)) / (2.0 * sigma * sigma));
    }

    public static void main(String[] args) {
        for (double m = 500; m < 510; m += 0.1) {
            System.out.println("" + m + "\t" + getGaussianPoint(m, 502, 100, 1));
        }
    }

    @Override
    public List<Float> getMsMsEvent(double minMz, double maxMz) {
        Long startTime = System.currentTimeMillis();
        List<Float> listMsMsEventTime = new ArrayList();
        if (ms2SpectrumHeaders == null) {
            try {
                logger.debug("retrieve Ms2 SpectrumHeader");
                buildMs2SpectrumHeaderIndexes(reader.getMs2SpectrumHeaders());
            } catch (SQLiteException ex) {
                logger.error("Exception while retrieving SpectrumHeader " + ex);
            }
        }
        if (ms2SpectrumHeaders != null) {
            int minId = ~Arrays.binarySearch(ms2SpectrumHeaderByMz, minMz);
            int maxId = ~Arrays.binarySearch(ms2SpectrumHeaderByMz, maxMz);
            if (minId != -1 && maxId != -1) {
                for (int i = minId; i <= maxId; i++) {
                    SpectrumHeader spectrumHeader = ms2SpectrumHeaders[i];
                    double mz = spectrumHeader.getPrecursorMz();
                    if (mz >= minMz && mz <= maxMz) {
                        listMsMsEventTime.add(spectrumHeader.getElutionTime());
                    }
                }
            }
        }
        logger.debug("retrieve MS/MS events finished in " + (System.currentTimeMillis() - startTime) + "+ ms");
        return listMsMsEventTime;
    }

    private int findPeakIndex(Peakel[] peakels, Pair<Double, Integer>[] peakelIndexesByMz, double moz, Peakel referencePeakel, float mzTolPPM) {
        double min = Double.MAX_VALUE;
        int resultIdx = -1;
        Comparator<Pair<Double, Integer>> c = new Comparator<Pair<Double, Integer>>() {

            @Override
            public int compare(Pair<Double, Integer> o1, Pair<Double, Integer> o2) {
                return Double.compare(o1.getLeft(), o2.getLeft());
            }
        };
        int lowerIdx = Arrays.binarySearch(peakelIndexesByMz, new ImmutablePair<Double, Integer>(moz - (moz * mzTolPPM / 1e6), 0), c);
        lowerIdx = (lowerIdx < 0) ? Math.max(0, ~lowerIdx - 1) : Math.max(0, lowerIdx - 1);
        int upperIdx = Arrays.binarySearch(peakelIndexesByMz, new ImmutablePair<Double, Integer>(moz + (moz * mzTolPPM / 1e6), 0), c);
        upperIdx = (upperIdx < 0) ? Math.min(peakelIndexesByMz.length - 1, ~upperIdx) : Math.min(peakelIndexesByMz.length - 1, upperIdx + 1);

        for (int i = lowerIdx; i <= upperIdx; i++) {
            int k = peakelIndexesByMz[i].getRight();
            if ((1e6 * Math.abs(peakels[k].getMz() - moz) / moz < mzTolPPM)
                    && ((Math.abs(peakels[k].getApexElutionTime() - referencePeakel.getApexElutionTime()) / referencePeakel.calcDuration()) < 0.25)
                    && (Math.abs(peakels[k].getMz() - moz) < min)) {
                min = Math.abs(peakels[k].getMz() - moz);
                resultIdx = k;
            }
        }
        return resultIdx;
    }

    private int findPeakIndexByCorrelation(Peakel[] peakels, Pair<Double, Integer>[] peakelIndexesByMz, double moz, Peakel referencePeakel, float mzTolPPM) {
        double maxCorr = Double.MIN_VALUE;
        int resultIdx = -1;
        Comparator<Pair<Double, Integer>> c = new Comparator<Pair<Double, Integer>>() {
    @Override
            public int compare(Pair<Double, Integer> o1, Pair<Double, Integer> o2) {
                return Double.compare(o1.getLeft(), o2.getLeft());
            }
        };
        
        int lowerIdx = Arrays.binarySearch(peakelIndexesByMz, new ImmutablePair<Double, Integer>(moz - (moz * mzTolPPM / 1e6), 0), c);
        lowerIdx = (lowerIdx < 0) ? Math.max(0, ~lowerIdx - 1) : Math.max(0, lowerIdx - 1);
        int upperIdx = Arrays.binarySearch(peakelIndexesByMz, new ImmutablePair<Double, Integer>(moz + (moz * mzTolPPM / 1e6), 0), c);
        upperIdx = (upperIdx < 0) ? Math.min(peakelIndexesByMz.length - 1, ~upperIdx) : Math.min(peakelIndexesByMz.length - 1, upperIdx + 1);

        for (int i = lowerIdx; i <= upperIdx; i++) {
            int k = peakelIndexesByMz[i].getRight();
            if ((1e6 * Math.abs(peakels[k].getMz() - moz) / moz < mzTolPPM) 
                    && (peakels[k].getApexElutionTime() > referencePeakel.getFirstElutionTime()) 
                    && (peakels[k].getApexElutionTime() < referencePeakel.getLastElutionTime()) ) {
                double corr = correlation(referencePeakel, peakels[k]);
                logger.debug("correlation "+referencePeakel.getMz()+ " with "+peakels[k].getMz()+" = "+corr);
                if ( corr > 0.6 && (corr > maxCorr)) {
                maxCorr = corr;
                resultIdx = k;
                }
            }
        }
        return resultIdx;
    }

    /*
    * Returns the absolute value of the correlation between 2 peakels
    */
    private double correlation(Peakel p1, Peakel p2) {
        int p1Offset = 0;
        int p2Offset = 0;

        // not clean : some RT values can be missing in elutiontime array when intensity = 0
        if (p1.getFirstElutionTime() < p2.getFirstElutionTime()) {
            // search p2.firstElutionTime index in p1
            int idx = Arrays.binarySearch(p1.getElutionTimes(), p2.getFirstElutionTime());
            p2Offset = idx < 0 ? ~idx : idx;
        } else {
            // search p1.firstElutionTime in p2
            int idx = Arrays.binarySearch(p2.getElutionTimes(), p1.getFirstElutionTime());
            p1Offset = idx < 0 ? ~idx : idx;
        }

        float[] p1Values = p1.getIntensityValues();
        float[] p2Values = p2.getIntensityValues();
        
        int length = Math.max(p1Values.length+p1Offset, p2Values.length+p2Offset);
        
        if (Math.abs(p1.getMz() - 651.36385)<1e-5 && Math.abs(p2.getMz() - 650.6159987649247) < 1e-5) {
            logger.debug("Stop here");            
        }
        double[] y = new double[length];
        Arrays.fill(y, 0.0);
        double[] y1 = new double[length];
        Arrays.fill(y1, 0.0);
        
        for (int k = 0; k < length; k++) {
            if (k >= p1Offset && k < p1Values.length) {
                y[k] = p1Values[k-p1Offset];
            }
            if (k >= p2Offset && k < p2Values.length) {
                y1[k] = p2Values[k-p2Offset];
            }
        }
        
        PearsonsCorrelation pearson = new PearsonsCorrelation();
        double corr = pearson.correlation(y, y1);
        
        return Math.abs(corr);
    }

    
    @Override
    public int getSpectrumCount() {
        try {
            return reader.getSpectraCount();
        } catch (SQLiteException sle) {
            logger.error("Error while reading mzdb file", sle);
        }
        return 0;
    }

    @Override
    public boolean exportRawFile(String outputFileName, IExportParameters exportParams) {
        
            long start = System.currentTimeMillis();
            ExportType exportType = exportParams.getExportType();
            switch (exportType){
                case MGF :{
                    try {
                        MgfExportParameters mgfExportParam = (MgfExportParameters)exportParams;
                        logger.debug("MGF writer start for " + this.getName() + ": mgfFilePath=" + outputFileName 
                                + ", precursorMzComputation=" + mgfExportParam.getPrecComp().getParamName() +
                                ", mzTol=" + mgfExportParam.getMzTolPPM() 
                                + ", intensityCutoff=" + mgfExportParam.getIntensityCutoff() 
                                + ", exportProlineTitle=" + mgfExportParam.isExportProlineTitle());
                        MgfWriter writer = new MgfWriter(this.getFile().getAbsolutePath());
                        writer.write(outputFileName, mgfExportParam.getPrecComp(), mgfExportParam.getIntensityCutoff(), mgfExportParam.isExportProlineTitle());
                        logger.debug(" mgf created in " + (System.currentTimeMillis() - start) + " ms");
                    } catch (SQLiteException | ClassNotFoundException ex) {
                        logger.error("SQLiteException or ClassNotFoundException while exporting mgf file", ex);
                        return false;
                    } catch (FileNotFoundException ex) {
                        logger.error("FileNotFoundException while exporting mgf file: ", ex);
                        return false;
                    }catch (IOException ex) {
                        logger.error("IOException while exporting mgf file: ", ex);
                        return false;
                    }
                    break;
                }
                case SCAN_HEADER:{
                    ScanHeaderExportParameters scanHeaderExportParam = (ScanHeaderExportParameters)exportParams;
                    ScanHeaderType type = scanHeaderExportParam.getScanHeadertype();
                    File outFile = new File(outputFileName);
                    SpectrumHeader[] spectrumHeaders;
                    switch (type){
                        case MS1:{
                            try{
                                spectrumHeaders = reader.getMs1SpectrumHeaders();
                            }catch(SQLiteException ex){
                                logger.error("SQLiteException while exporting spectrum Header file", ex);
                                return false;
                            }
                            break;
                        }
                        case MS2:{
                            try{
                                spectrumHeaders = reader.getMs2SpectrumHeaders();
                            }catch(SQLiteException ex){
                                logger.error("SQLiteException while exporting scpectrum Header file", ex);
                                return false;
                            }
                            break;
                        }
                        default:{
                            spectrumHeaders = new SpectrumHeader[0];
                            // should not happen;
                        }
                    }
                    // runId is set to -1
                    MsSpectrumTSVWriter.writeRun(spectrumHeaders, -1, outFile);
                    logger.debug(" scan header file created in " + (System.currentTimeMillis() - start) + " ms");
                    break;
                }
                default:{
                    //should not happen
                    break;
                }
            }
            
        
        return true;
    }
    
    private boolean checkDIAFile(){
        try{
            AcquisitionMode acqMode = this.reader.getAcquisitionMode();
            if (acqMode != null && acqMode.equals(AcquisitionMode.SWATH)){
                return true;
            }
        } catch (SQLiteException ex) {
            logger.error("Check DIA: SQLiteException while reading acquisition mode", ex);
        }
        return false;
    }
    
    @Override
    public boolean isDIAFile(){
        return this.isDIAFile;
    }

}
