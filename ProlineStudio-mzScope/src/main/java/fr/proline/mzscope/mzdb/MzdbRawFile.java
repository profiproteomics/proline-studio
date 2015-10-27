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
import fr.profi.mzdb.io.writer.mgf.MgfWriter;
import fr.profi.mzdb.io.writer.mgf.PrecursorMzComputation;
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
import fr.proline.mzscope.model.Spectrum;
import fr.proline.mzscope.model.IRawFile;
import fr.proline.mzscope.model.Ms1ExtractionRequest;
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

    @Override
    public Chromatogram getXIC(Ms1ExtractionRequest params) {
        long start = System.currentTimeMillis();
        Chromatogram chromatogram = null;
        try {
            Peak[] peaks = reader.getMsXicInMzRtRanges(params.getMinMz(), params.getMaxMz(), params.getElutionTimeLowerBound(), params.getElutionTimeUpperBound(), params.getMethod());
            chromatogram = createChromatoFromPeaks(peaks);
            chromatogram.minMz = params.getMinMz();
            chromatogram.maxMz = params.getMaxMz();
            StringBuilder builder = new StringBuilder("Mass range: ");
            builder.append(massFormatter.format(params.getMinMz())).append("-").append(massFormatter.format(params.getMaxMz()));
            chromatogram.title = builder.toString();
            logger.info("mzdb chromatogram extracted : {} - {} over time {} - {} in {} ms", massFormatter.format(params.getMinMz()),
                    massFormatter.format(params.getMaxMz()), params.getElutionTimeLowerBound(), params.getElutionTimeUpperBound(), (System.currentTimeMillis() - start));

        } catch (SQLiteException | StreamCorruptedException e) {
            logger.error("Error during chromatogram extraction", e);
        }
        return chromatogram;
    }

    private Chromatogram createChromatoFromPeaks(Peak[] peaks) {
        Chromatogram chromatogram = null;
        List<Double> xAxisData = new ArrayList<>(peaks.length);
        List<Double> yAxisData = new ArrayList<>(peaks.length);
        try {
            int previousSpectrumId = (int) peaks[0].getLcContext().getSpectrumId();
            for (Peak peak : peaks) {
                int spectrumId = (int) peak.getLcContext().getSpectrumId();
                if (previousSpectrumId != getPreviousSpectrumId(spectrumId, 1)) {
                    // there is a gap between peaks, add 0 values after the previous peak and before this one
                    xAxisData.add(reader.getSpectrumHeaderById().get((long) getNextSpectrumId(previousSpectrumId, 1)).getElutionTime() / 60.0);
                    yAxisData.add(0.0);
                    xAxisData.add(reader.getSpectrumHeaderById().get((long) getPreviousSpectrumId(spectrumId, 1)).getElutionTime() / 60.0);
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
    public List<Feature> extractFeatures(FeaturesExtractionRequest params) {
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

    private List<Feature> detectFeatures(float mzTolPPM, double minMz, double maxMz) {
        List<Feature> result = new ArrayList<>();
        FeatureDetectorConfig detectorConfig = new FeatureDetectorConfig(1, mzTolPPM, 5, new SmartPeakelFinderConfig(5, 3, false, 10, false, true));
        MzDbFeatureDetector detector = new MzDbFeatureDetector(reader, detectorConfig);
        try {
            Iterator<RunSlice> runSlices;
            if (minMz == 0 && maxMz == 0) {
                runSlices = getMzDbReader().getLcMsRunSliceIterator();
            } else {
                runSlices = getMzDbReader().getLcMsRunSliceIterator(minMz, maxMz);
            }
            Peakel[] peakels = detector.detectPeakels(runSlices);

            Iterator<RunSlice> tmpRunSlices = (minMz == 0 && maxMz == 0) ? getMzDbReader().getLcMsRunSliceIterator() : getMzDbReader().getLcMsRunSliceIterator(minMz, maxMz);
            double min = Double.MAX_VALUE;
            double max = Double.MIN_VALUE;
            for (; tmpRunSlices.hasNext();) {
                RunSlice rs = tmpRunSlices.next();
                min = Math.min(min, rs.getHeader().getBeginMz());
                max = Math.max(max, rs.getHeader().getEndMz());
            }
            logger.info("Real bounds : " + min + " - " + max);
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

        logger.info("Features detected : " + result.size());
        return result;

    }

    private List<Feature> deisotopePeakels(Peakel[] peakels, float mzTolPPM) throws StreamCorruptedException, SQLiteException {
        List<Feature> result = new ArrayList<>();
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
                Tuple2<Object, TheoreticalIsotopePattern>[] putativePatterns = IsotopicPatternScorer.calclIsotopicPatternHypotheses(data, peakels[k].getMz(), mzTolPPM);
                //TreeMap<Double, TheoreticalIsotopePattern> putativePatterns = IsotopePattern.getOrderedIPHypothesis(data, peakels[k].getMz());
                TheoreticalIsotopePattern bestPattern = putativePatterns[0]._2;
                List<Peakel> l = new ArrayList<>(bestPattern.isotopeCount() + 1);
                for (Tuple2 t : bestPattern.mzAbundancePairs()) {
                    int idx = findPeakIndex(peakels, peakelIndexesByMz, (double) t._1, peakels[k], mzTolPPM);
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
//               logger.info("Creates feature with "+l.size()+" peakels at mz="+l.get(0).getMz()+ " from peakel "+peakels[k].getMz()+ " at "+peakels[k].getApexElutionTime()/60.0);
                Feature feature = new Feature(l.get(0).getMz(), bestPattern.charge(), JavaConverters.asScalaBufferConverter(l).asScala(), true);
                result.add(feature);
            }
        }

        logger.info("Features detected : " + result.size());
        return result;
    }

    private List<Feature> deisotopePeakelsV2(Peakel[] peakels, float mzTolPPM) {
        MzDbFeatureDetector detector = new MzDbFeatureDetector(reader, new FeatureDetectorConfig(1, mzTolPPM, 3, new SmartPeakelFinderConfig(5, 3, false, 10, false, true)));
        Feature[] result = detector._deisotopePeakelsV2(peakels);
        return Arrays.asList(result);
    }

    private List<Feature> detectPeakels(FeaturesExtractionRequest params) {

        List<Feature> result = new ArrayList<>();
        int msLevel = 1;
        FeatureDetectorConfig detectorConfig = new FeatureDetectorConfig(msLevel, params.getMzTolPPM(), 5, new SmartPeakelFinderConfig(5, 3, false, 10, false, params.isRemoveBaseline()));
        // hack : change SmartPeakelFinder configuration that will be used by the UnsupervisedPeakelDetector
        logger.info("Extract peakels with params : " + params.toString());
        MzDbFeatureDetector detector = new MzDbFeatureDetector(reader, detectorConfig);
        try {
            Iterator<RunSlice> runSlices;
            if (params.getMinMz() == 0 && params.getMaxMz() == 0) {
                runSlices = getMzDbReader().getLcMsRunSliceIterator();
            } else {
                runSlices = getMzDbReader().getLcMsRunSliceIterator(params.getMinMz(), params.getMaxMz());
            }
            Peakel[] peakels = detector.detectPeakels(runSlices);
            for (Peakel peakel : peakels) {
                ArrayList<Peakel> l = new ArrayList<>();
                l.add(peakel);
                //creates a fake Feature associated to this peakel in order to always display Features
                Feature feature = new Feature(peakel.getMz(), 0, JavaConverters.asScalaBufferConverter(l).asScala(), false);
                if (params.getMinMz() == 0 && params.getMaxMz() == 0) {
                    result.add(feature);
                } else {
                    //check that the feature is in the mass range
                    if (feature.getMz() >= params.getMinMz() && feature.getMz() <= params.getMaxMz()) {
                        result.add(feature);
                    }
                }
            }
        } catch (SQLiteException | StreamCorruptedException ex) {
            logger.error("Error while getting LcMs RunSlice Iterator: " + ex);
        }

        return result;
    }

    private List<Feature> extractFeaturesFromMs2(float tolPPM) {
        List<Feature> result = null;
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
            result = extractFeatures(pfs, tolPPM);
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
            final double[] mzList = data.getMzList();
            final double[] leftSigma = new double[mzList.length];
            final double[] rightSigma = new double[mzList.length];
            final float[] intensityList = data.getIntensityList();
            List<Float> xAxisData = new ArrayList<Float>(mzList.length);
            List<Float> yAxisData = new ArrayList<Float>(mzList.length);
            for (int count = 0; count < mzList.length; count++) {
                if ((data.getLeftHwhmList() != null)) {
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
                }
                xAxisData.add((float) mzList[count]);
                yAxisData.add(intensityList[count]);
                if (data.getRightHwhmList() != null) {
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
                }

            }
            spectrum = new Spectrum(spectrumIndex, rawSpectrum.getHeader().getElutionTime(), Doubles.toArray(xAxisData), Floats.toArray(yAxisData), rawSpectrum.getHeader().getMsLevel());
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
    public boolean exportAsMGF(String mgfFileName, PrecursorMzComputation precComp, float mzTolPPM, float intensityCutoff, boolean exportProlineTitle) {
        try {
            long start = System.currentTimeMillis();
            logger.debug("MGF writer start for " + this.getName() + ": mgfFilePath=" + mgfFileName + ", precursorMzComputation=" + precComp.getUserParamName() + ", mzTol=" + mzTolPPM + ", intensityCutoff=" + intensityCutoff + ", exportProlineTitle=" + exportProlineTitle);
            MgfWriter writer = new MgfWriter(this.getFile().getAbsolutePath());
            writer.write(mgfFileName, precComp, mzTolPPM, intensityCutoff, exportProlineTitle);
            logger.debug(" mgf created in " + (System.currentTimeMillis() - start) + " ms");
        } catch (SQLiteException | ClassNotFoundException ex) {
            logger.error("SQLiteException or ClassNotFoundException while exporting mgf file", ex);
            return false;
        } catch (FileNotFoundException ex) {
            logger.error("FileNotFoundException while exporting mgf file: ", ex);
            return false;
        } catch (IOException ex) {
            logger.error("IOException while exporting mgf file:", ex);
            return false;
        }
        return true;
    }

}
