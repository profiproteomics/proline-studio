package fr.proline.mzscope.utils;

import com.almworks.sqlite4java.SQLiteException;
import fr.profi.ms.model.TheoreticalIsotopePattern;
import fr.profi.mzdb.algo.IsotopicPatternScorer;
import fr.profi.mzdb.model.Feature;
import fr.profi.mzdb.model.Peakel;
import fr.profi.mzdb.model.SpectrumData;
import fr.profi.mzdb.model.SpectrumHeader;
import fr.profi.mzdb.model.SpectrumSlice;
import fr.proline.mzscope.model.IFeature;
import fr.proline.mzscope.mzdb.MzdbFeatureWrapper;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;
import scala.collection.JavaConverters;

/**
 *
 * @author MB243701
 */
public class SpectrumUtils {

    private static final Logger logger = LoggerFactory.getLogger(SpectrumUtils.class);
    
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
                    if (sc1.getPrecursorMz() <= sc2.getPrecursorMz()){
                        return -1;
                    }else {
                        return 1;
                    }
                }
                
            });
            ms2SpectrumHeaders = list.toArray(new SpectrumHeader[nbSc]);

        }
        return ms2SpectrumHeaders;
    }
    
    public static int getNearestPeakIndex( double[] peaksMz, double value) {
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
                    && (peakels[k].getApexElutionTime() > referencePeakel.getFirstElutionTime()) 
                    && (peakels[k].getApexElutionTime() < referencePeakel.getLastElutionTime()) ) {
                double corr = correlation(referencePeakel, peakels[k]);
                //logger.debug("correlation "+referencePeakel.getMz()+ " with "+peakels[k].getMz()+" = "+corr);
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
    public static double correlation(Peakel p1, Peakel p2) {
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
    
}
