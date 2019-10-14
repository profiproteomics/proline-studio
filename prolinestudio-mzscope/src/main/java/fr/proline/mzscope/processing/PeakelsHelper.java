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
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.geometry.Geometries;
import com.github.davidmoten.rtree.geometry.Point;
import fr.profi.ms.model.TheoreticalIsotopePattern;
import fr.profi.mzdb.MzDbReader;
import fr.profi.mzdb.model.Feature;
import fr.profi.mzdb.model.Peakel;
import fr.profi.mzdb.model.PeakelCursor;
import fr.profi.mzdb.model.SpectrumData;
import fr.proline.mzscope.model.IFeature;
import static fr.proline.mzscope.processing.SpectrumUtils.MIN_CORRELATION_SCORE;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
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

    private static PeakelsHelper fromMzScopeFeatures(List<IFeature> features) {
        List<Peakel> peakels = features.stream().flatMap(f -> Arrays.asList(f.getPeakels()).stream()).collect(Collectors.toList());
        return new PeakelsHelper(peakels);
    }

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

    public List<Feature> deisotopePeakels(MzDbReader reader, float mzTolPPM) throws StreamCorruptedException, SQLiteException {

        long start = System.currentTimeMillis();

        List<Feature> result = new ArrayList<>();
        Set<Peakel> assigned = new HashSet<>();

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
            if (!assigned.contains(peakels.get(k))) {

                SpectrumData data = buildSpectrumDataFromPeakels(k);

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

                    if ((bestMatching != null) && !assigned.contains(bestMatching)) {
                        assigned.add(bestMatching);
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

    public List<Feature> deisotopePeakelsFromMzdb(MzDbReader reader, float mzTolPPM) throws StreamCorruptedException, SQLiteException {

        long start = System.currentTimeMillis();

        List<Feature> result = new ArrayList<>();
        Set<Peakel> assigned = new HashSet<>();

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
            if (!assigned.contains(peakels.get(k))) {
                SpectrumData data = reader.getSpectrumData(peakels.get(k).getApexSpectrumId());
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

                    if ((bestMatching != null) && !assigned.contains(bestMatching)) {
                        assigned.add(bestMatching);
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

    public static List<Feature> deisotopePeakelsFromMzdb(MzDbReader reader, Peakel[] peakels, float mzTolPPM) throws StreamCorruptedException, SQLiteException {

        long start = System.currentTimeMillis();
        List<Feature> result = new ArrayList<>();
        boolean[] assigned = new boolean[peakels.length];
        Arrays.fill(assigned, false);

        Arrays.sort(peakels, new Comparator<Peakel>() {
            @Override
            public int compare(Peakel o1, Peakel o2) {
                return Double.compare(o2.getApexIntensity(), o1.getApexIntensity());
            }
        });

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

                SpectrumData data = reader.getSpectrumData(peakels[k].getApexSpectrumId());
//                Tuple2<Object, TheoreticalIsotopePattern>[] putativePatterns = IsotopicPatternScorer.calcIsotopicPatternHypotheses(data, peakels[k].getMz(), mzTolPPM);
                Tuple2<Object, TheoreticalIsotopePattern>[] putativePatterns = IsotopicPatternUtils.calcIsotopicPatternHypotheses(data, peakels[k].getMz(), mzTolPPM);
                //TreeMap<Double, TheoreticalIsotopePattern> putativePatterns = IsotopePattern.getOrderedIPHypothesis(data, peakels[k].getMz());
                TheoreticalIsotopePattern bestPattern = putativePatterns[0]._2;
                List<Peakel> l = new ArrayList<>(bestPattern.isotopeCount() + 1);

                for (Tuple2 t : bestPattern.mzAbundancePairs()) {
                    int idx = SpectrumUtils.findCorrelatingPeakelIndex(peakels, peakelIndexesByMz, (double) t._1, peakels[k], mzTolPPM);
                    if ((idx != -1) && (!assigned[idx])) {
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
                //logger.info("Creates feature with "+l.size()+" peakels at mz="+l.get(0).getMz()+ " from peakel "+peakels[k].getMz()+ " at "+peakels[k].getApexElutionTime()/60.0);                
                Feature feature = new Feature(l.get(0).getMz(), bestPattern.charge(), JavaConverters.asScalaBufferConverter(l).asScala(), true);
                result.add(feature);
            }
        }

        logger.info("Features detected : {} in {} ms", result.size(), (System.currentTimeMillis() - start));
        return result;
    }

    private SpectrumData buildSpectrumDataFromPeakels(int k) {
        List<Peakel> coelutingPeakels = findCoelutigPeakels(peakels.get(k).getApexMz() - HALF_MZ_WINDOW,
                peakels.get(k).getApexMz() + HALF_MZ_WINDOW,
                peakels.get(k).getApexElutionTime() - RT_TOLERANCE,
                peakels.get(k).getApexElutionTime() + RT_TOLERANCE);
        Tuple2<double[], float[]> values = slicePeakels(coelutingPeakels, peakels.get(k));
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

        coelutingPeakels.stream().forEach(peakel -> {

            PeakelCursor peakelCursor = peakel.getNewCursor();
            boolean foundPeak = false;

            // TODO: optimize this search (start from the apex or implement binary search)
            while (peakelCursor.next() && foundPeak == false) {
                if (peakelCursor.getSpectrumId() == matchingSpectrumId) {
                    mzList.add(peakelCursor.getMz());
                    intensityList.add(peakelCursor.getIntensity());
                    foundPeak = true;
                }
            }
        }
        );
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
