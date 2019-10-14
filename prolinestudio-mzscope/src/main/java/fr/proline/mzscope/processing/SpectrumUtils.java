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

import fr.profi.mzdb.model.Peakel;
import fr.profi.mzdb.model.SpectrumHeader;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author MB243701
 */
public class SpectrumUtils {

    private static final Logger logger = LoggerFactory.getLogger(SpectrumUtils.class);

    public static final double MIN_CORRELATION_SCORE = 0.6;

    /**
     * sort spectrumHeader by mz
     *
     * @param spectrums
     * @return
     */
    public static SpectrumHeader[] sortMs2SpectrumHeaders(SpectrumHeader[] spectrums) {
        SpectrumHeader[] ms2SpectrumHeaders = null;
        if (spectrums != null) {
            int nbSc = spectrums.length;
            ms2SpectrumHeaders = new SpectrumHeader[nbSc];
            List<SpectrumHeader> list = Arrays.asList(spectrums);
            // sort by precursorMz
            Collections.sort(list, new Comparator<SpectrumHeader>() {

                @Override
                public int compare(SpectrumHeader sc1, SpectrumHeader sc2) {
                    if (sc1.getPrecursorMz() <= sc2.getPrecursorMz()) {
                        return -1;
                    } else {
                        return 1;
                    }
                }

            });
            ms2SpectrumHeaders = list.toArray(new SpectrumHeader[nbSc]);

        }
        return ms2SpectrumHeaders;
    }

    public static int getNearestPeakIndex(double[] peaksMz, double value) {
        int idx = Arrays.binarySearch(peaksMz, value);
        idx = (idx < 0) ? ~idx : idx;
        double min = Double.MAX_VALUE;
        for (int k = Math.max(0, idx - 1); k <= Math.min(peaksMz.length - 1, idx + 1); k++) {
            if (Math.abs(peaksMz[k] - value) < min) {
                min = Math.abs(peaksMz[k] - value);
                idx = k;
            }
        }
        return idx;
    }

    public static int getPeakIndex(double[] peaksMz, double value, double ppmTol) {
        int idx = Arrays.binarySearch(peaksMz, value);
        idx = (idx < 0) ? ~idx : idx;
        double min = Double.MAX_VALUE;
        int resultIdx = -1;
        for (int k = Math.max(0, idx - 1); k <= Math.min(peaksMz.length - 1, idx + 1); k++) {
            if (((1e6 * Math.abs(peaksMz[k] - value) / value) < ppmTol) && (Math.abs(peaksMz[k] - value) < min)) {
                min = Math.abs(peaksMz[k] - value);
                resultIdx = k;
            }
        }
        return resultIdx;
    }

    public static boolean isInRange(double m1, double m2, double ppmTol) {
        return ((1e6 * Math.abs(m1 - m2) / m2) < ppmTol);
    }

    public static int findPeakIndex(Peakel[] peakels, Pair<Double, Integer>[] peakelIndexesByMz, double moz, Peakel referencePeakel, float mzTolPPM) {
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

    public static int findCorrelatingPeakelIndex(Peakel[] peakels, Pair<Double, Integer>[] peakelIndexesByMz, double moz, Peakel referencePeakel, float mzTolPPM) {
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
                    && (peakels[k].getApexElutionTime() >= referencePeakel.getFirstElutionTime())
                    && (peakels[k].getApexElutionTime() <= referencePeakel.getLastElutionTime())) {
                double corr = correlation(referencePeakel, peakels[k]);
                //logger.debug("correlation "+referencePeakel.getMz()+ " with "+peakels[k].getMz()+" = "+corr);
                if (corr > MIN_CORRELATION_SCORE && (corr > maxCorr)) {
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
    public static double correlation(Peakel p1, Peakel p2) {
        return correlation(p1.getElutionTimes(), p1.getIntensityValues(), p2.getElutionTimes(), p2.getIntensityValues());
    }

    public static double correlation(float[] x1, float[] y1, float[] x2, float[] y2) {
        Pair<double[], double[]> values = zipValues(x1, y1, x2, y2);
        PearsonsCorrelation pearson = new PearsonsCorrelation();
        double corr = pearson.correlation(values.getLeft(), values.getRight());
        return Math.abs(corr);
    }

    public static double correlation(double[] x1, double[] y1, double[] x2, double[] y2) {
        Pair<double[], double[]> values = zipValues(x1, y1, x2, y2);
        PearsonsCorrelation pearson = new PearsonsCorrelation();
        double corr = pearson.correlation(values.getLeft(), values.getRight());
        return Math.abs(corr);
    }
       
    public static Pair<double[], double[]> zipValues(double[] x1, double[] y1, double[] x2, double[] y2) {
        int offset1 = 0;
        int offset2 = 0;

        double[] c = Arrays.copyOf(x1, x1.length + x2.length);
        System.arraycopy(x2, 0, c, x1.length, x2.length);
        double[] time = Arrays.stream(c).sorted().distinct().toArray();

        double[] a1 = new double[time.length];
        double[] a2 = new double[time.length];

        for (int k = 0; k < time.length; k++) {
            a1[k] = 0;
            a2[k] = 0;
            // for strange reasons, some time points are duplicated ??? TO verify in extracted chromatograms ! 
            while ((offset1 < x1.length) && (time[k] == x1[offset1])) {
                a1[k] = y1[offset1];
                offset1++;
            }
            while ((offset2 < x2.length) && (time[k] == x2[offset2])) {
                a2[k] = y2[offset2];
                offset2++;
            }
        }
        return new ImmutablePair(a1, a2);
    }

    public static Pair<double[], double[]> zipValues(float[] x1, float[] y1, float[] x2, float[] y2) {
        int offset1 = 0;
        int offset2 = 0;

        float[] c = Arrays.copyOf(x1, x1.length + x2.length);
        System.arraycopy(x2, 0, c, x1.length, x2.length);
        double[] time = IntStream.range(0, c.length).mapToDouble(i -> c[i]).sorted().distinct().toArray();

        double[] a1 = new double[time.length];
        double[] a2 = new double[time.length];

        for (int k = 0; k < time.length; k++) {
            a1[k] = 0.0;
            a2[k] = 0.0;
            // for strange reasons, some time points are duplicated ??? TO verify in extracted chromatograms ! 
            while ((offset1 < x1.length) && (time[k] == x1[offset1])) {
                a1[k] = y1[offset1];
                offset1++;
            }
            while ((offset2 < x2.length) && (time[k] == x2[offset2])) {
                a2[k] = y2[offset2];
                offset2++;
            }
        }
        return new ImmutablePair(a1, a2);
    }

}
