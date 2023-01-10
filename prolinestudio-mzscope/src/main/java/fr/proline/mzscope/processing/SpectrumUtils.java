/* 
 * Copyright (C) 2019
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

import com.google.common.primitives.Doubles;
import com.google.common.primitives.Floats;
import fr.profi.ms.model.TheoreticalIsotopePattern;
import fr.profi.mzdb.algo.signal.filtering.SavitzkyGolaySmoother;
import fr.profi.mzdb.algo.signal.filtering.SavitzkyGolaySmoothingConfig;
import fr.profi.mzdb.model.DataMode;
import fr.profi.mzdb.model.Peakel;
import fr.profi.mzdb.model.SpectrumData;
import fr.profi.mzdb.model.SpectrumHeader;
import fr.profi.mzdb.peakeldb.PeakelDbHelper;
import fr.proline.mzscope.model.Spectrum;
import fr.proline.mzscope.ui.SpectrumPanel;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;

import java.util.*;
import java.util.stream.IntStream;

/**
 *
 * @author MB243701
 */
public class SpectrumUtils {

    private static final Logger logger = LoggerFactory.getLogger(SpectrumUtils.class);

    public static final double MIN_CORRELATION_SCORE = 0.5;

    static class Peak {

        final double mass;
        final float intensity;
        final int index;
        boolean used = false;

        public Peak(Peak peak) {
            this(peak.mass, peak.intensity, peak.index);
        }

        public Peak(double mass, float intensity, int index) {
            this.mass = mass;
            this.intensity = intensity;
            this.index = index;
        }
    }

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
                    && ((Math.abs(peakels[k].getElutionTime() - referencePeakel.getElutionTime()) / referencePeakel.calcDuration()) < 0.25)
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
                    && (peakels[k].getElutionTime() >= referencePeakel.getFirstElutionTime())
                    && (peakels[k].getElutionTime() <= referencePeakel.getLastElutionTime())) {
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
      
      // not the most efficient solution to get smoothed intensities ... 
      int nbrPoints = Math.min(p1.peaksCount() / 4, 9);
      SavitzkyGolaySmoother smoother = new SavitzkyGolaySmoother(new SavitzkyGolaySmoothingConfig(nbrPoints, 2, 1));
      Peakel sp1 = (nbrPoints > 0) ? smoother.smoothPeakel(p1) : p1;
      nbrPoints = Math.min(p2.peaksCount() / 4, 9);
      smoother = new SavitzkyGolaySmoother(new SavitzkyGolaySmoothingConfig(nbrPoints, 2, 1));
      Peakel sp2 = (nbrPoints > 0) ? smoother.smoothPeakel(p2) : p2;            
       
      return correlation(sp1.getElutionTimes(), sp1.getIntensityValues(), sp2.getElutionTimes(), sp2.getIntensityValues());
    }

    public static double correlationOMP(Peakel p1, Peakel p2, boolean smooth) {
        if (smooth) {
            int nbrPoints = Math.min(p1.peaksCount() / 4, 9);
            SavitzkyGolaySmoother smoother = new SavitzkyGolaySmoother(new SavitzkyGolaySmoothingConfig(nbrPoints, 2, 1));
            Peakel sp1 = smoother.smoothPeakel(p1);
            nbrPoints = Math.min(p2.peaksCount() / 4, 9);
            smoother = new SavitzkyGolaySmoother(new SavitzkyGolaySmoothingConfig(nbrPoints, 2, 1));
            Peakel sp2 = smoother.smoothPeakel(p2);
            return PeakelDbHelper.computeCorrelation(sp1, sp2);
        } else {
            return PeakelDbHelper.computeCorrelation(p1, p2);
        }
    }

    public static double correlation(float[] x1, float[] y1, float[] x2, float[] y2) {
        Pair<double[], double[]> values = zipValues(x1, y1, x2, y2);
        PearsonsCorrelation pearson = new PearsonsCorrelation();
        double corr = pearson.correlation(values.getLeft(), values.getRight());
        return corr;
    }

    public static double correlation(double[] x1, double[] y1, double[] x2, double[] y2) {
        Pair<double[], double[]> values = zipValues(x1, y1, x2, y2);
        PearsonsCorrelation pearson = new PearsonsCorrelation();
        double corr = pearson.correlation(values.getLeft(), values.getRight());
        return corr;
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


    public static Spectrum buildSpectrum(double[] mzList, float[] intensityList, double fwhm, DataMode mode) {

        List<Float> xAxisData = new ArrayList<>(mzList.length);
        List<Float> yAxisData = new ArrayList<>(mzList.length);
        double[] leftSigma = new double[mzList.length];
        double[] rightSigma = new double[mzList.length];

        for (int count = 0; count < mzList.length; count++) {
            if (fwhm > 0 && !mode.equals(DataMode.PROFILE)) {
                leftSigma[count] = 2.0 * fwhm / 2.35482;
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
            if (fwhm > 0 && !mode.equals(DataMode.PROFILE)) {
                rightSigma[count] = 2.0 * fwhm / 2.35482;
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
        Spectrum.ScanType scanType = (mode.equals(DataMode.CENTROID)) ?  Spectrum.ScanType.CENTROID : Spectrum.ScanType.PROFILE;
        Spectrum spectrum = new Spectrum(0, 0.0f, Doubles.toArray(xAxisData), Floats.toArray(yAxisData), 1, scanType);
        spectrum.setPrecursorMz(null);
        spectrum.setPrecursorCharge(null);

        return spectrum;
    }

    private static double getGaussianPoint(double mz, double peakMz, double intensity, double sigma) {
        return intensity * Math.exp(-((mz - peakMz) * (mz - peakMz)) / (2.0 * sigma * sigma));
    }

    public static Spectrum deisotopeCentroidSpectrum(Spectrum spectrum) {
        double tolPpm = 20.0;
        double[] masses = spectrum.getMasses();
        float[] intensities = spectrum.getIntensities();
        final SpectrumData spectrumData = spectrum.getSpectrumData();
        List<Peak> peaks = new ArrayList<>(spectrumData.getPeaksCount());
        List<Peak> result = new ArrayList<>(spectrumData.getPeaksCount());
        Map<Integer, Peak> peaksByIndex = new HashMap<>();
        for (int k = 0; k < masses.length; k++) {
            Peak p = new Peak(masses[k], intensities[k], k);
            peaks.add(p);
            peaksByIndex.put(p.index, p);
        }

        peaks.sort((o1, o2) -> Float.compare(o2.intensity, o1.intensity));

        for (int k = 0; k < peaks.size(); k++) {
            Peak p = peaks.get(k);
            if (!p.used) {
                Tuple2<Object, TheoreticalIsotopePattern> prediction = IsotopicPatternUtils.predictIsotopicPattern(spectrumData, p.mass, tolPpm);
                if ( (1e6*(prediction._2.monoMz() - p.mass)/p.mass) <= tolPpm ) {
                    float intensity = 0;
                    int charge = prediction._2.charge();
                    for (Tuple2 t : prediction._2.mzAbundancePairs()) {
                        Double mz = (Double) t._1;
                        Float ab = (Float) t._2;
                        int peakIdx = SpectrumUtils.getPeakIndex(spectrumData.getMzList(), mz, tolPpm);
                        if ((peakIdx != -1) && (spectrumData.getIntensityList()[peakIdx] <= p.intensity)) {
                            intensity+= spectrumData.getIntensityList()[peakIdx];
                            peaksByIndex.get(peakIdx).used = true;
                        } else {
                            break;
                        }
                    }
                    if ( (charge == 1) || (intensity == p.intensity) ) {
                        Peak newPeak = new Peak(p.mass, intensity, p.index);
                        result.add(newPeak);
                    } else {
                        Peak newPeak = new Peak(p.mass*charge - (charge-1)*1.00728, intensity, p.index);
                        logger.info("Move peak ({},{}) to ({},{})", p.mass, p.intensity, newPeak.mass, newPeak.intensity);
                        result.add(newPeak);
                    }
                } else {
                    p.used = true;
                    result.add(new Peak(p));
                }
            }
        }

        result.sort(Comparator.comparingDouble(o -> o.mass));
        masses = new double[result.size()];
        intensities = new float[result.size()];
        int k = 0;
        for (Peak p : result) {
            masses[k] = p.mass;
            intensities[k++] = p.intensity;
        }
        Spectrum newSpectrum = new Spectrum(-1, spectrum.getRetentionTime() , masses, intensities, spectrum.getMsLevel(), Spectrum.ScanType.CENTROID);

        return newSpectrum;
    }
}
