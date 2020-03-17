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
import fr.profi.ms.model.TheoreticalIsotopePattern;
import fr.profi.mzdb.MzDbReader;
import fr.profi.mzdb.algo.IsotopicPatternScorer;
import fr.profi.mzdb.model.Feature;
import fr.profi.mzdb.model.Peakel;
import fr.profi.mzdb.model.PeakelCursor;
import fr.profi.mzdb.model.SpectrumData;
import static fr.proline.mzscope.processing.SpectrumUtils.MIN_CORRELATION_SCORE;
import java.io.StreamCorruptedException;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;
import scala.collection.JavaConverters;

/**
 *
 * @author CB205360
 */
public class PeakelsHelper {

    private static final Logger logger = LoggerFactory.getLogger(PeakelsHelper.class);
    private static int HALF_MZ_WINDOW = 5;
    private static float RT_TOLERANCE = 40.0f;

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
            rTree = rTree.add(p, Geometries.point(p.getMz(), p.getApexElutionTime()));
        }
        logger.info("built RTree contains {} peakels", rTree.size());
    }

    public List<Peakel> findCoelutigPeakels(double minMz, double maxMz, float minRt, float maxRt) {
        List<Peakel> coelutingPeakels = new ArrayList<>(1000);
        Iterator<Entry<Peakel, Point>> peakelIterator = rTree.search(Geometries.rectangle(minMz, minRt, maxMz, maxRt)).toBlocking().toIterable().iterator();
        while (peakelIterator.hasNext()) {
            coelutingPeakels.add(peakelIterator.next().value());
        }
        return coelutingPeakels;
    }

    public List<Peakel> findCoelutigPeakels(double minMz, double maxMz, float minRt, float maxRt, Map<Integer, Peakel> assigned) {
        List<Peakel> coelutingPeakels = new ArrayList<>(1000);
        Iterator<Entry<Peakel, Point>> peakelIterator = rTree.search(Geometries.rectangle(minMz, minRt, maxMz, maxRt)).toBlocking().toIterable().iterator();
        while (peakelIterator.hasNext()) {
            Peakel p = peakelIterator.next().value();
            if (!assigned.containsKey(p.getId()) )
                coelutingPeakels.add(p);
        }
        return coelutingPeakels;
    }

    public List<Feature> deisotopePeakels(MzDbReader reader, float mzTolPPM) throws StreamCorruptedException, SQLiteException {

        long start = System.currentTimeMillis();

        List<Feature> result = new ArrayList<>();
        Map<Integer, Peakel> assigned = new HashMap<>();

        peakels.sort(new Comparator<Peakel>() {
            @Override
            public int compare(Peakel o1, Peakel o2) {
                return Double.compare(o2.getApexIntensity(), o1.getApexIntensity());
            }
        });

        // for debug purposes
        double REFMZ = 724.71024;
        double REFRT = 133.866;
        

        for (int k = 0; k < peakels.size(); k++) {
            if ((k % 10000) == 0) {
                logger.info("processing peakel " + k);
            }
            if (!assigned.containsKey(peakels.get(k).getId())) {
                
              if ((Math.abs(peakels.get(k).getMz() - REFMZ) < REFMZ*mzTolPPM/1e6) && (Math.abs(REFRT*60.0 - peakels.get(k).getApexElutionTime()) < 60)) {
                logger.debug("this one");
              }

                SpectrumData data = buildSpectrumDataFromPeakels(k, assigned);

//                Tuple2<Object, TheoreticalIsotopePattern>[] putativePatterns = IsotopicPatternScorer.calcIsotopicPatternHypotheses(data, peakels.get(k).getMz(), mzTolPPM);
                Tuple2<Object, TheoreticalIsotopePattern>[] putativePatterns = IsotopicPatternUtils.calcIsotopicPatternHypotheses(data, peakels.get(k).getMz(), mzTolPPM);
                TheoreticalIsotopePattern bestPattern = putativePatterns[0]._2;
                List<Peakel> l = new ArrayList<>(bestPattern.isotopeCount() + 1);
                int gapRank = 0;

                int indexOfMatching = 0;
                for (Tuple2 t : bestPattern.mzAbundancePairs()) {
                    if (1e6 * (Math.abs(peakels.get(k).getMz() - (double) t._1) / peakels.get(k).getMz()) < mzTolPPM) {
                        break;
                    }
                    indexOfMatching++;
                }

                double scaling = peakels.get(k).getApexIntensity() / ((Float)bestPattern.mzAbundancePairs()[indexOfMatching]._2);

                for (Tuple2 t : bestPattern.mzAbundancePairs()) {
                    double patternMz = (double) t._1;
                    float patternNormAbundance = (float)t._2;
                    List<Peakel> isotopes = findCoelutigPeakels(patternMz - (patternMz * mzTolPPM / 1e6), patternMz + (patternMz * mzTolPPM / 1e6), peakels.get(k).getFirstElutionTime(), peakels.get(k).getLastElutionTime(), assigned);
                    Peakel bestMatching = null;
                    double maxCorr = Double.MIN_VALUE;
                    for (Peakel p : isotopes) {
                        double corr = SpectrumUtils.correlation(peakels.get(k), p);
                        if (corr > MIN_CORRELATION_SCORE && (corr > maxCorr) &&
                            (p.getApexIntensity() < 2.5 * patternNormAbundance * scaling)) {
                            maxCorr = corr;
                            bestMatching = p;
                        }
                    }

                    if (bestMatching != null) {
                        if (!assigned.containsKey(bestMatching.getId())) {
                            gapRank = 0;
                            assigned.put(bestMatching.getId(), bestMatching);
                            l.add(bestMatching);
                            if ((Math.abs(bestMatching.getMz() - REFMZ) < REFMZ * mzTolPPM / 1e6) && (Math.abs(REFRT * 60.0 - bestMatching.getApexElutionTime()) < 60)) {
                                logger.debug("this one but assigned as isotope");
                            }
                        } else {
                            logger.debug("best isotope match found but already assigned");
                        }
                    } else {
                        gapRank++;
                    }
                    if (gapRank >= 3)
                        break;
                }
                if (l.isEmpty()) {
                    logger.warn("Strange situation: peakel {}, {} not found from pattern at mono {}, {}+ (gap: {})", peakels.get(k).getMz(), peakels.get(k).getApexElutionTime()/60.0, bestPattern.monoMz(), bestPattern.charge(), gapRank);
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

//                Tuple2<Object, TheoreticalIsotopePattern>[] putativePatterns = IsotopicPatternScorer.calcIsotopicPatternHypotheses(data, peakels.get(k).getMz(), mzTolPPM);
                Tuple2<Object, TheoreticalIsotopePattern>[] putativePatterns = IsotopicPatternUtils.calcIsotopicPatternHypotheses(data, peakels.get(k).getMz(), mzTolPPM);
                TheoreticalIsotopePattern bestPattern = putativePatterns[0]._2;
                List<Peakel> l = new ArrayList<>(bestPattern.isotopeCount() + 1);

                for (Tuple2 t : bestPattern.mzAbundancePairs()) {
                    double mz = (double) t._1;
                    List<Peakel> isotopes = findCoelutigPeakels(mz - (mz * mzTolPPM / 1e6), mz + (mz * mzTolPPM / 1e6), peakels.get(k).getFirstElutionTime(), peakels.get(k).getLastElutionTime());
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
                    logger.warn("Strange situation : peakel not found within isotopic pattern .... " + peakels.get(k).getMz());
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

    private SpectrumData buildSpectrumDataFromPeakels(int k, Map<Integer, Peakel> assigned) {
//        List<Peakel> coelutingPeakels = findCoelutigPeakels(peakels.get(k).getApexMz() - HALF_MZ_WINDOW,
//                peakels.get(k).getApexMz() + HALF_MZ_WINDOW,
//                peakels.get(k).getApexElutionTime() - RT_TOLERANCE,
//                peakels.get(k).getApexElutionTime() + RT_TOLERANCE);

        final Peakel peakel = peakels.get(k);
        List<Peakel> coelutingPeakels = findCoelutigPeakels(peakel.getApexMz() - HALF_MZ_WINDOW,
                peakel.getApexMz() + HALF_MZ_WINDOW,
                peakel.getFirstElutionTime(),
                peakel.getLastElutionTime(),
                assigned);

        return buildSpectrumDataFromPeakels(peakel, coelutingPeakels);
    }

    public SpectrumData buildSpectrumDataFromPeakels(Peakel peakel, List<Peakel> coelutingPeakels) {
        Tuple2<double[], float[]> values = slicePeakels(coelutingPeakels, peakel);
        SpectrumData data = new SpectrumData(values._1, values._2);
        return data;
    }

    private Tuple2<double[], float[]> slicePeakels(List<Peakel> coelutingPeakels, Peakel matchingPeakel) {

        Long matchingSpectrumId = matchingPeakel.getApexSpectrumId();
        final List<Double> mzList = new ArrayList<>(coelutingPeakels.size());
        final List<Float> intensityList = new ArrayList<>(coelutingPeakels.size());

        coelutingPeakels.sort(new Comparator<Peakel>() {
            @Override
            public int compare(Peakel o1, Peakel o2) {
                return Double.compare(o1.getApexMz(), o2.getApexMz());
            }
        });

//        coelutingPeakels.stream().forEach(peakel -> {
//
//            PeakelCursor peakelCursor = peakel.getNewCursor();
//            boolean foundPeak = false;
//
//            // TODO: optimize this search (start from the apex or implement binary search)
//            while (peakelCursor.next() && foundPeak == false) {
//                if (peakelCursor.getSpectrumId() == matchingSpectrumId) {
//                    mzList.add(peakelCursor.getMz());
//                    intensityList.add(peakelCursor.getIntensity());
//                    foundPeak = true;
//                }
//            }
//        }
//        );
//

        final int SPAN = 3;
        coelutingPeakels.stream().forEach(peakel -> {

            PeakelCursor peakelCursor = peakel.getNewCursor();
            boolean foundPeak = false;
            float sum = 0;
            int count = 0;
            while (peakelCursor.next() &&  foundPeak == false) {
                if (peakelCursor.getSpectrumId() == matchingSpectrumId) {
                    mzList.add(peakelCursor.getMz());
                    foundPeak = true;
                }
            }
            if (foundPeak) {
                int index = peakelCursor.cursorIndex();
                int minBound = Math.max(0, index - SPAN);
                int maxBound = Math.min(index + SPAN, peakel.getPeaksCount() - 1);
                for (int i = minBound; i <= maxBound; i++) {
                    sum += peakel.intensityValues()[i];
                    count++;
                }
                intensityList.add(sum / count);
            }
        });



        int idx = 0;
        double[] mz = new double[mzList.size()];
        for (Double d : mzList) {
            mz[idx++] = d;
        }
        idx = 0;
        float[] intensities = new float[intensityList.size()];
        for (Float f : intensityList) {
            intensities[idx++] = f;
        }

        return new Tuple2(mz, intensities);
    }

}
