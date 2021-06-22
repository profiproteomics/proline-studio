/* 
 * Copyright (C) 2019 VD225637
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1 
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License 
 * along with this program; If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */
package fr.proline.mzscope.processing;

import com.almworks.sqlite4java.SQLiteException;
import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Geometries;
import com.github.davidmoten.rtree.geometry.Point;
import fr.profi.chemistry.model.MolecularConstants;
import fr.profi.ms.algo.IsotopePatternEstimator;
import fr.profi.ms.model.TheoreticalIsotopePattern;
import fr.profi.mzdb.MzDbReader;
import fr.profi.mzdb.algo.PeakelsPatternPredictor;
import fr.profi.mzdb.model.Feature;
import fr.profi.mzdb.model.Peakel;
import fr.profi.mzdb.model.SpectrumData;
import static fr.proline.mzscope.processing.SpectrumUtils.MIN_CORRELATION_SCORE;
import java.io.StreamCorruptedException;
import java.util.*;

import fr.profi.mzdb.util.ms.MsUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;
import scala.Tuple4;
import scala.collection.JavaConverters;

/**
 *
 * @author CB205360
 */
public class PeakelsHelper {

    public static final String VALID_FEATURES = "valid features";
    public static final String DUBIOUS_FEATURES = "dubious features";

    private static final Logger logger = LoggerFactory.getLogger(PeakelsHelper.class);
    public static int HALF_MZ_WINDOW = 5;
    private static float RT_TOLERANCE = 40.0f;
    private static float INTENSITY_PERCENTILE = 0.99f;

    private List<Peakel> peakels;
    private RTree<Peakel, Point> rTree;

    public PeakelsHelper(Peakel[] peakels) {
        this(Arrays.asList(peakels));
    }

    public PeakelsHelper(List<Peakel> peakels) {
        logger.info("creates PeakelsHelper with {} peakels", peakels.size());
        this.peakels = peakels;
        this.rTree = RTree.create();
        for (Peakel p : peakels) {
            rTree = rTree.add(p, Geometries.point(p.getMz(), p.getElutionTime()));
        }
        logger.info("built RTree contains {} peakels", rTree.size());
    }

    public List<Peakel> findCoelutingPeakels(double minMz, double maxMz, float minRt, float maxRt) {
        List<Peakel> coelutingPeakels = new ArrayList<>(1000);
        Iterator<Entry<Peakel, Point>> peakelIterator = rTree.search(Geometries.rectangle(minMz, minRt, maxMz, maxRt)).toBlocking().toIterable().iterator();
        while (peakelIterator.hasNext()) {
            coelutingPeakels.add(peakelIterator.next().value());
        }
        return coelutingPeakels;
    }

    public List<Peakel> findCoelutingPeakels(double minMz, double maxMz, float minRt, float maxRt, Map<Integer, Peakel> assigned) {
        List<Peakel> coelutingPeakels = new ArrayList<>(1000);
        Iterator<Entry<Peakel, Point>> peakelIterator = rTree.search(Geometries.rectangle(minMz, minRt, maxMz, maxRt)).toBlocking().toIterable().iterator();
        while (peakelIterator.hasNext()) {
            Peakel p = peakelIterator.next().value();
            if (!assigned.containsKey(p.getId()) )
                coelutingPeakels.add(p);
        }
        return coelutingPeakels;
    }

    public Map<String, List<Feature>> deisotopePeakels(float mzTolPPM, float rtTolerance) throws StreamCorruptedException, SQLiteException {

        long start = System.currentTimeMillis();

        DescriptiveStatistics[] stats = new DescriptiveStatistics[] {new DescriptiveStatistics(), new DescriptiveStatistics(), new DescriptiveStatistics()};

        List<Feature> features = new ArrayList<>();
        List<Feature> monoPeakelFeatures = new ArrayList<>();
        Map<Integer, Peakel> assigned = new HashMap<>();

        peakels.sort((o1, o2) -> Double.compare(o2.getApexIntensity(), o1.getApexIntensity()));

        for (int k = 0; k < peakels.size(); k++) {
            if ((k % 10000) == 0) {
                logger.info("processing peakel " + k);
            }
            final Peakel peakel = peakels.get(k);
            
            if (!assigned.containsKey(peakel.getId())) {
                
                SpectrumData data = buildSpectrumFromPeakels(k, assigned, rtTolerance);
                TheoreticalIsotopePattern pattern = IsotopicPatternUtils.predictIsotopicPattern(data, peakel.getMz(), mzTolPPM)._2;
                List<Peakel> l = new ArrayList<>(pattern.isotopeCount() + 1);
                int gapRank = 0;

                int indexOfMatching = 0;
                for (Tuple2 t : pattern.mzAbundancePairs()) {
                    if (1e6 * (Math.abs(peakel.getMz() - (double) t._1) / peakel.getMz()) < mzTolPPM) {
                        break;
                    }
                    indexOfMatching++;
                }

                if ((Math.abs(peakel.getMz() - 762.70542) < 1e-4) && (Math.abs(peakel.getApexElutionTime() - 127.33*60.0) < 10)) {
                  logger.info("this one");
                }
                double scaling = peakel.getApexIntensity() / ((Float)pattern.mzAbundancePairs()[indexOfMatching]._2);
                int rank = 0;
                
                for (Tuple2 t : pattern.mzAbundancePairs()) {
                    double patternMz = (double) t._1;
                    float patternNormAbundance = (float)t._2;
                    float minRt = peakel.getFirstElutionTime();
                    float maxRt = peakel.getLastElutionTime();
                    
                    //List<Peakel> isotopes = findCoelutingPeakels(patternMz - (patternMz * mzTolPPM / 1e6), patternMz + (patternMz * mzTolPPM / 1e6), minRt, maxRt, assigned);
                    List<Peakel> isotopes = findCoelutingPeakels(patternMz - (patternMz * mzTolPPM / 1e6), patternMz + (patternMz * mzTolPPM / 1e6), minRt, maxRt);
                    Peakel bestMatching = null;
                    double maxCorr = Double.MIN_VALUE;
                    for (Peakel p : isotopes) {
                        double corr = SpectrumUtils.correlation(peakel, p);
                        if (corr > MIN_CORRELATION_SCORE && (corr > maxCorr) &&
                            (p.getApexIntensity() < 4 * patternNormAbundance * scaling)) {
                            maxCorr = corr;
                            bestMatching = p;
                        }
                    }

                    if (bestMatching != null) {
                        if (!assigned.containsKey(bestMatching.getId())) {
                            gapRank = 0;
                            assigned.put(bestMatching.getId(), bestMatching);
                            l.add(bestMatching);
                        } else {
                            //logger.info("best isotope at {} match found but is already assigned (predicted from isotope {})", rank, indexOfMatching);
                        }
                    } else {
                        gapRank++;
                    }
                    if (gapRank >= 3)
                        break;
                    rank++;
                }
                if (l.isEmpty()) {
                    logger.info("Strange situation: peakel {}, {} not found from pattern at mono {}, {}+ (indexOfMatching: {})", peakel.getMz(), peakel.getElutionTime()/60.0, pattern.monoMz(), pattern.charge(), indexOfMatching);
                    l.add(peakel);
                }
                Feature feature = new Feature(l.get(0).getMz(), pattern.charge(), JavaConverters.asScalaBufferConverter(l).asScala(), true);
                if (l.size() > 1) {
                    features.add(feature);
                    stats[0].addValue(feature.getBasePeakel().getApexIntensity());
                    stats[1].addValue(feature.calcDuration());
                    stats[2].addValue(feature.getMs1Count());
                } else {
                    monoPeakelFeatures.add(feature);
                }
            }
        }
        // filter mono peakel features
        double minIntensity = stats[0].getPercentile(10);
        double maxDuration = stats[1].getMax();
        double minMs1Count = stats[2].getPercentile(10);

        logger.info("Filter dubious features: Thresholds are minIntensity={}, maxDuration={}, minMs1Count{}", minIntensity, maxDuration, minMs1Count);

        Map<String, List<Feature>> map = new HashMap<>();
        map.put(VALID_FEATURES, features);
        map.put(DUBIOUS_FEATURES, new ArrayList<>());

        for(Feature f: monoPeakelFeatures) {


// Option 1
            if (f.getCharge() > 1) {
                map.get(VALID_FEATURES).add(f);
            } else {
                map.get(DUBIOUS_FEATURES).add(f);
            }

// Option 2
//            if ((f.getBasePeakel().getApexIntensity() >= minIntensity) && (f.calcDuration() <= maxDuration) && (f.getMs1Count() >= minMs1Count)) {
//                map.get(VALID_FEATURES).add(f);
//            } else {
//                //logger.info("Doubtful Feature  = {}; {}; {}; {}; {} ",f.getMz(), f.getElutionTime() / 60.0, f.getCharge(), f.getBasePeakel().getApexIntensity(),  f.getMs1Count());
//                map.get(DUBIOUS_FEATURES).add(f);
//            }
        }

        logger.info("Features detected : {} in {} ms", map.get(VALID_FEATURES).size(), (System.currentTimeMillis() - start));
        return map;
    }

    public List<Feature> deisotopePeakelsFromMzdb(MzDbReader reader, float mzTolPPM) throws StreamCorruptedException, SQLiteException {

        long start = System.currentTimeMillis();

        List<Feature> result = new ArrayList<>();
        Map<Integer, Peakel> assigned = new HashMap<>();

        peakels.sort(new Comparator<Peakel>() {
            @Override
            public int compare(Peakel o1, Peakel o2) {
                return Double.compare(o2.getApexIntensity(), o1.getApexIntensity());
            }
        });

        
        for (int k = 0; k < peakels.size(); k++) {
            if ((k % 10000) == 0) {
                logger.info("processing peakel " + k);
            }
            if (!assigned.containsKey(peakels.get(k).getId())) {
              
                SpectrumData data = reader.getSpectrumData(peakels.get(k).getApexSpectrumId());
                TheoreticalIsotopePattern bestPattern = IsotopicPatternUtils.predictIsotopicPattern(data, peakels.get(k).getMz(), mzTolPPM)._2;
                List<Peakel> l = new ArrayList<>(bestPattern.isotopeCount() + 1);

                for (Tuple2 t : bestPattern.mzAbundancePairs()) {
                    double mz = (double) t._1;
                    List<Peakel> isotopes = findCoelutingPeakels(mz - (mz * mzTolPPM / 1e6), mz + (mz * mzTolPPM / 1e6), peakels.get(k).getFirstElutionTime(), peakels.get(k).getLastElutionTime());
                    Peakel bestMatching = null;
                    double maxCorr = Double.MIN_VALUE;
                    for (Peakel p : isotopes) {
                        double corr = SpectrumUtils.correlation(peakels.get(k), p);
                        if (corr > MIN_CORRELATION_SCORE && (corr > maxCorr)) {
                            maxCorr = corr;
                            bestMatching = p;
                        }
                    }

                    if ((bestMatching != null) && !assigned.containsKey(bestMatching.getId())) {
                       assigned.put(bestMatching.getId(), bestMatching);
                       l.add(bestMatching);
                    }
                }
                if (l.isEmpty()) {
                    logger.trace("Strange situation : peakel not found within isotopic pattern .... " + peakels.get(k).getMz());
                    l.add(peakels.get(k));
                }
                //logger.info("Creates feature with "+l.size()+" peakels at mz="+l.get(0).getMz()+ " from peakel "+peakels[k].getMz()+ " at "+peakels[k].getApexElutionTime()/60.0);                
                Feature feature = new Feature(l.get(0).getMz(), bestPattern.charge(), JavaConverters.asScalaBufferConverter(l).asScala(), true);
                result.add(feature);
            }
        }
        logger.info("Features detected : {} in {} ms", result.size(), (System.currentTimeMillis() - start));
        return result;
    }

//    public static List<Feature> deisotopePeakelsFromMzdb(MzDbReader reader, Peakel[] peakels, float mzTolPPM) throws StreamCorruptedException, SQLiteException {
//
//        long start = System.currentTimeMillis();
//        List<Feature> result = new ArrayList<>();
//        boolean[] assigned = new boolean[peakels.length];
//        Arrays.fill(assigned, false);
//
//        Arrays.sort(peakels, new Comparator<Peakel>() {
//            @Override
//            public int compare(Peakel o1, Peakel o2) {
//                return Double.compare(o2.getApexIntensity(), o1.getApexIntensity());
//            }
//        });
//
//        Pair<Double, Integer> peakelIndexesByMz[] = new Pair[peakels.length];
//        for (int k = 0; k < peakels.length; k++) {
//            peakelIndexesByMz[k] = new ImmutablePair<>(peakels[k].getMz(), k);
//        }
//
//        Arrays.sort(peakelIndexesByMz, new Comparator<Pair<Double, Integer>>() {
//            @Override
//            public int compare(Pair<Double, Integer> p1, Pair<Double, Integer> p2) {
//                return Double.compare(p1.getLeft(), p2.getLeft());
//            }
//        });
//
//        for (int k = 0; k < peakels.length; k++) {
//            if ((k % 10000) == 0) {
//                logger.info("processing peakel " + k);
//            }
//            if (!assigned[k]) {
//
//                SpectrumData data = reader.getSpectrumData(peakels[k].getApexSpectrumId());
////                Tuple2<Object, TheoreticalIsotopePattern>[] putativePatterns = IsotopicPatternScorer.calcIsotopicPatternHypotheses(data, peakels[k].getMz(), mzTolPPM);
//                Tuple2<Object, TheoreticalIsotopePattern>[] putativePatterns = IsotopicPatternUtils.calcIsotopicPatternHypotheses(data, peakels[k].getMz(), mzTolPPM);
//                //TreeMap<Double, TheoreticalIsotopePattern> putativePatterns = IsotopePattern.getOrderedIPHypothesis(data, peakels[k].getMz());
//                TheoreticalIsotopePattern bestPattern = putativePatterns[0]._2;
//                List<Peakel> l = new ArrayList<>(bestPattern.isotopeCount() + 1);
//
//                for (Tuple2 t : bestPattern.mzAbundancePairs()) {
//                    int idx = SpectrumUtils.findCorrelatingPeakelIndex(peakels, peakelIndexesByMz, (double) t._1, peakels[k], mzTolPPM);
//                    if ((idx != -1) && (!assigned[idx])) {
//                        assigned[idx] = true;
//                        l.add(peakels[idx]);
//                    } //else if ( ((double)t._1 > min) && ((double)t._1 < max)) {
////                     logger.info("Isotope at "+(double)t._1+" not found");
////                  }
//                }
//                if (l.isEmpty()) {
//                    logger.warn("Strange situation : peakel not found within isotopic pattern .... " + peakels[k].getMz());
//                    l.add(peakels[k]);
//                }
//                //logger.info("Creates feature with "+l.size()+" peakels at mz="+l.get(0).getMz()+ " from peakel "+peakels[k].getMz()+ " at "+peakels[k].getApexElutionTime()/60.0);
//                Feature feature = new Feature(l.get(0).getMz(), bestPattern.charge(), JavaConverters.asScalaBufferConverter(l).asScala(), true);
//                result.add(feature);
//            }
//        }
//
//        logger.info("Features detected : {} in {} ms", result.size(), (System.currentTimeMillis() - start));
//        return result;
//    }

    private SpectrumData buildSpectrumFromPeakels(int k, Map<Integer, Peakel> assigned, float rtTolerance) {

        final Peakel peakel = peakels.get(k);

        List<Peakel> coelutingPeakels = findCoelutingPeakels(peakel.getApexMz() - HALF_MZ_WINDOW,
                peakel.getApexMz() + HALF_MZ_WINDOW,
                peakel.getElutionTime() - rtTolerance,
                peakel.getElutionTime() + rtTolerance);

        return buildSpectrumFromPeakels(coelutingPeakels, peakel);
    }

    public SpectrumData buildSpectrumFromPeakels(List<Peakel> coelutingPeakels, Peakel peakel) {

        // Proline core implementation

        SpectrumData data = PeakelsPatternPredictor.buildSpectrumFromPeakels(JavaConverters.asScalaBufferConverter(coelutingPeakels).asScala(), peakel);

        // local alternative implementation

//        Tuple4<double[], float[], float[], float[]> values = slicePeakels(coelutingPeakels, peakel.getApexSpectrumId());
//        SpectrumData data = new SpectrumData(values._1(), values._2(), values._3(), values._4());

        return data;
    }

    private Tuple4<double[], float[],  float[],  float[]> slicePeakels(List<Peakel> coelutingPeakels, Long matchingSpectrumId) {

        final List<Double> mzList = new ArrayList<>(coelutingPeakels.size());
        final List<Float> intensityList = new ArrayList<>(coelutingPeakels.size());
        final List<Float> lfwhmList = new ArrayList<>(coelutingPeakels.size());
        final List<Float> rfwhmList = new ArrayList<>(coelutingPeakels.size());

        coelutingPeakels.sort(new Comparator<Peakel>() {
            @Override
            public int compare(Peakel o1, Peakel o2) {
                return Double.compare(o1.getApexMz(), o2.getApexMz());
            }
        });

        final int SPAN = 1;
        coelutingPeakels.stream().forEach(peakel -> {


            int index = Arrays.binarySearch(peakel.getSpectrumIds(), matchingSpectrumId);
            Boolean foundPeak = (index >= 0) && (index < peakel.peaksCount()); 
            if (!foundPeak) {
                  index = ~index;
                  foundPeak = (index > 0 && index < peakel.peaksCount());
            }

            float sum = 0;
            int count = 0;
//            PeakelCursor peakelCursor = peakel.getNewCursor();
//            boolean foundPeak = false;
//            while (peakelCursor.next() &&  foundPeak == false) {
//                if (peakelCursor.getSpectrumId() == matchingSpectrumId) {
//                    mzList.add(peakelCursor.getMz());
//                    foundPeak = true;
//                }
//            }
            if (foundPeak) {
//                int index = peakelCursor.cursorIndex();
                
                int minBound = Math.max(0, index - SPAN);
                int maxBound = Math.min(index + SPAN, peakel.getPeaksCount() - 1);
                for (int i = minBound; i <= maxBound; i++) {
                    sum += peakel.intensityValues()[i];
                    count++;
                }
                mzList.add(peakel.mzValues()[index]);
                intensityList.add(sum / count);
                lfwhmList.add(peakel.getLeftHwhmMean());
                rfwhmList.add(peakel.getRightHwhmMean());
            }
        });


        double[] mz = new double[mzList.size()];
        float[] intensities = new float[intensityList.size()];
        float[] lfwhm = new float[lfwhmList.size()];
        float[] rfwhm = new float[rfwhmList.size()];
        for (int idx = 0; idx < mzList.size(); idx++) {
          mz[idx] = mzList.get(idx);
          intensities[idx] = intensityList.get(idx);
          lfwhm[idx] = lfwhmList.get(idx);
          rfwhm[idx] = rfwhmList.get(idx);
        }
        
        return new Tuple4(mz, intensities, lfwhm, rfwhm);
    }


    public List<Peakel>  findFeatureIsotopes(Peakel peakel, int charge, double mzTolPPM)  {

        double peakelMz = peakel.getMz();

        TheoreticalIsotopePattern pattern = IsotopePatternEstimator.getTheoreticalPattern(peakelMz, charge);
        float intensityScalingFactor = (peakel.getApexIntensity()/(Float)pattern.mzAbundancePairs()[0]._2);
        int gapRank = 0;

        List<Peakel> isotopes = new ArrayList<Peakel>(pattern.isotopeCount());

        for (Tuple2 t : pattern.mzAbundancePairs()) {
            double patternMz = (double) t._1;
            float patternNormAbundance = (float)t._2;
            float minRt = peakel.getFirstElutionTime();
            float maxRt = peakel.getLastElutionTime();

            List<Peakel> putativeIsotopes = findCoelutingPeakels(patternMz - (patternMz * mzTolPPM / 1e6), patternMz + (patternMz * mzTolPPM / 1e6), minRt, maxRt);
            Peakel bestMatching = null;
            double maxCorr = Double.MIN_VALUE;
            for (Peakel p : putativeIsotopes) {
                double corr = SpectrumUtils.correlation(peakel, p);
                if (corr > MIN_CORRELATION_SCORE && (corr > maxCorr) &&
                        (p.getApexIntensity() < 4 * patternNormAbundance * intensityScalingFactor)) {
                    maxCorr = corr;
                    bestMatching = p;
                }
            }

            if (bestMatching != null) {
                gapRank = 0;
                isotopes.add(bestMatching);
            } else {
                gapRank++;
            }
            if (gapRank >= 3)
                break;
        }

        return isotopes;
    }


}
