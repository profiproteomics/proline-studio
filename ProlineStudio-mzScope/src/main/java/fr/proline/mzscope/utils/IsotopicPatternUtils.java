/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.mzscope.utils;

import fr.profi.ms.algo.IsotopePatternEstimator;
import fr.profi.ms.model.TheoreticalIsotopePattern;
import fr.profi.mzdb.algo.IsotopicPatternScorer;
import fr.profi.mzdb.model.SpectrumData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import org.apache.commons.math3.stat.inference.TestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;

/**
 *
 * @author CB205360
 */
public class IsotopicPatternUtils {

    private static final Logger logger = LoggerFactory.getLogger(IsotopicPatternUtils.class);

    static class Pattern {
        
        public double mz;
        public int charge;

        public Pattern(double mz, int charge) {
            this.mz = mz;
            this.charge = charge;
        }

        
        @Override
        public int hashCode() {
            int hash = 5;
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Pattern other = (Pattern) obj;
            if (Double.doubleToLongBits(this.mz) != Double.doubleToLongBits(other.mz)) {
                return false;
            }
            if (this.charge != other.charge) {
                return false;
            }
            return true;
        }
        
        
    }
    public static  Map<Pattern, List<Double>> compareIsotopicPatternPredictions(SpectrumData spectrum, double mz, double ppmTol) {
        
        double fittedPpmTol = ppmTol;
        
        int nearestPeakIdx = SpectrumUtils.getNearestPeakIndex(spectrum.getMzList(), mz);
        if (SpectrumUtils.isInRange(spectrum.getMzList()[nearestPeakIdx], mz, ppmTol)) {
            if (spectrum.getLeftHwhmList()[nearestPeakIdx] > 0.0f) {
                fittedPpmTol = (float) (1e6 * spectrum.getLeftHwhmList()[nearestPeakIdx] / spectrum.getMzList()[nearestPeakIdx]);
            }
        }
        Map<Pattern, List<Double>> map = new HashMap<>();
        Tuple2<Object, TheoreticalIsotopePattern>[] putativePatterns = IsotopicPatternScorer.calcIsotopicPatternHypotheses(spectrum, mz, fittedPpmTol);
        for (Tuple2<Object, TheoreticalIsotopePattern> t : putativePatterns) {
            Pattern p = new Pattern(t._2.monoMz(), t._2.charge());
            if (!map.containsKey(p)) map.put(p, new ArrayList<>());
            map.get(p).add((Double)t._1);
        }
        
        putativePatterns = calcIsotopicPatternHypotheses(spectrum, mz, fittedPpmTol, new ChiSquareScorer());
        for (Tuple2<Object, TheoreticalIsotopePattern> t : putativePatterns) {
            Pattern p = new Pattern(t._2.monoMz(), t._2.charge());
            if (!map.containsKey(p)) { 
                map.put(p, new ArrayList<>());
                map.get(p).add(0.0);
            }
            map.get(p).add((Double)t._1);
        }
        
        putativePatterns = calcIsotopicPatternHypotheses(spectrum, mz, fittedPpmTol, new DotProductScorer());
        for (Tuple2<Object, TheoreticalIsotopePattern> t : putativePatterns) {
            Pattern p = new Pattern(t._2.monoMz(), t._2.charge());
            if (!map.containsKey(p)) { 
                map.put(p, new ArrayList<>());
                map.get(p).add(0.0);
            }
            map.get(p).add((Double)t._1);
        }
        tracePredictions("Comparison", mz, ppmTol, map);
        return map;
    }
    
    public static TheoreticalIsotopePattern predictIsotopicPattern(SpectrumData spectrum, double mz, double ppmTol) {
        
        double fittedPpmTol = ppmTol;
        
        int nearestPeakIdx = SpectrumUtils.getNearestPeakIndex(spectrum.getMzList(), mz);
        if (SpectrumUtils.isInRange(spectrum.getMzList()[nearestPeakIdx], mz, ppmTol)) {
            if (spectrum.getLeftHwhmList()[nearestPeakIdx] > 0.0f) {
                fittedPpmTol = (float) (1e6 * spectrum.getLeftHwhmList()[nearestPeakIdx] / spectrum.getMzList()[nearestPeakIdx]);
            }
        }
        
        //Tuple2<Object, TheoreticalIsotopePattern>[] putativePatterns = IsotopicPatternScorer.calcIsotopicPatternHypotheses(spectrum, mz, fittedPpmTol);
        Tuple2<Object, TheoreticalIsotopePattern>[] putativePatterns = calcIsotopicPatternHypotheses(spectrum, mz, fittedPpmTol, new DotProductScorer());
        
        TheoreticalIsotopePattern pattern = (TheoreticalIsotopePattern) putativePatterns[0]._2;
        return pattern;
    }
    
    private static void tracePredictions(String title, double mz, double ppmTol, Map<Pattern, List<Double>> putativePatterns) {
        logger.info(" ######### "+title);
        logger.info("Prediction for mz = " + mz + ", ppm = " + ppmTol);
        for (Map.Entry<Pattern, List<Double>> e : putativePatterns.entrySet()) {
            StringJoiner joiner = new StringJoiner(",", "[", "]");
            e.getValue().forEach(x -> joiner.add(String.format("%.3f", x)));
            Pattern pattern = e.getKey();
            logger.info("Mono at mz = "  + String.format("%.3f", pattern.mz) + ", "+pattern.charge+"+ = "+ joiner.toString()  );
        }
    }
    
     private static void tracePredictions(String title, double mz, double ppmTol, Tuple2<Object, TheoreticalIsotopePattern>[] putativePatterns) {
        logger.info(" ######### "+title);
        logger.info("Prediction for mz = " + mz + ", ppm = " + ppmTol);
        for (Tuple2<Object, TheoreticalIsotopePattern> t : putativePatterns) {
            Double score = ((Double) t._1);
            TheoreticalIsotopePattern pattern = t._2;
            logger.info("Pattern : " + score + ", " + pattern.charge() + "+, mz = " + pattern.monoMz());
        }
        
     }
    
     public static Tuple2<Object, TheoreticalIsotopePattern>[] calcIsotopicPatternHypotheses(SpectrumData currentSpectrum, double mz, double ppmTol) {
         return calcIsotopicPatternHypotheses(currentSpectrum, mz, ppmTol, new DotProductScorer());
     }
     
     public static Tuple2<Object, TheoreticalIsotopePattern>[] calcIsotopicPatternHypotheses(SpectrumData currentSpectrum, double mz, double ppmTol, Scorer scorer) {

      List<Tuple2<Double, TheoreticalIsotopePattern>> result = new ArrayList<>();
      for (int charge = 1; charge <= 5; charge++) {
         Tuple2<Double, TheoreticalIsotopePattern> p = scorer.score(currentSpectrum, mz, 0, charge, ppmTol);
         for (int j = 1; j <= p._2.theoreticalMaxPeakelIndex()+2; j++) {
            Tuple2<Double, TheoreticalIsotopePattern> alternativeP = scorer.score(currentSpectrum, mz, j, charge, ppmTol);
            result.add(alternativeP);
         }
         result.add(p);
      }
      
      Collections.sort(result, new Comparator<Tuple2<Double, TheoreticalIsotopePattern>>() {
         @Override
         public int compare(Tuple2<Double, TheoreticalIsotopePattern> o1, Tuple2<Double, TheoreticalIsotopePattern> o2) {
            int sign = (int)Math.signum(o1._1 - o2._1);
            return (sign == 0) ? o2._2.charge() - o1._2.charge() : sign;
         }
      
      });
      
      return result.toArray(new Tuple2[result.size()]);
   }
     
 
}

interface Scorer {
    public  Tuple2<Double, TheoreticalIsotopePattern> score(SpectrumData currentSpectrum, double intialMz, int shift, int charge, double ppmTol);
}

class DotProductScorer implements Scorer {
    
       public Tuple2<Double, TheoreticalIsotopePattern> score(SpectrumData currentSpectrum, double intialMz, int shift, int charge, double ppmTol) {
        double score = 0.0;
        double mz = intialMz - shift * IsotopePatternEstimator.avgIsoMassDiff() / charge;
        TheoreticalIsotopePattern pattern = IsotopePatternEstimator.getTheoreticalPattern(mz, charge);
        Double ipMoz = mz;
        double[] observed = new double[pattern.mzAbundancePairs().length];
        double[] expected = new double[pattern.mzAbundancePairs().length];
        int observations = 0;
        for (int rank = 0; rank < pattern.mzAbundancePairs().length; rank++) {
            ipMoz = (rank == 0) ? ipMoz : ipMoz + IsotopePatternEstimator.avgIsoMassDiff() / charge;
            int nearestPeakIdx = SpectrumUtils.getNearestPeakIndex(currentSpectrum.getMzList(), ipMoz);
               if (((1e6 * Math.abs(currentSpectrum.getMzList()[nearestPeakIdx] - ipMoz) / ipMoz) < ppmTol)) {
                   observed[rank] = currentSpectrum.getIntensityList()[nearestPeakIdx];
                   observations++;
               } else {
                   observed[rank] = 0.0;
               }
            expected[rank] = (Float) pattern.mzAbundancePairs()[rank]._2;
        }

        score = dotProduct(observed, expected);
        //score = (1.0 - score)/observations;
        score = 1.0 - score;
        return new Tuple2<Double, TheoreticalIsotopePattern>(score, pattern);
    }
       
    private double dotProduct(double[] observed, double[] expected) {
          double sumObserved = 0.0;
        double sumExpected = 0.0;
        double dotProduct = 0.0;
        
        for (int k = 0; k < observed.length; k++) {
            dotProduct += observed[k]*expected[k];
            sumExpected += expected[k]*expected[k];
            sumObserved += observed[k]*observed[k];
        }
        
        return dotProduct/(Math.sqrt(sumExpected)*Math.sqrt(sumObserved));
    }
}

class ChiSquareScorer implements Scorer {
    
       public  Tuple2<Double, TheoreticalIsotopePattern> score(SpectrumData currentSpectrum, double intialMz, int shift, int charge, double ppmTol) {
        double score = 0.0;
        double mz = intialMz - shift * IsotopePatternEstimator.avgIsoMassDiff() / charge;
        TheoreticalIsotopePattern pattern = IsotopePatternEstimator.getTheoreticalPattern(mz, charge);
        Double ipMoz = mz;
        long[] observed = new long[pattern.mzAbundancePairs().length];
        double[] expected = new double[pattern.mzAbundancePairs().length];

        for (int rank = 0; rank < pattern.mzAbundancePairs().length; rank++) {
            ipMoz = (rank == 0) ? ipMoz : ipMoz + IsotopePatternEstimator.avgIsoMassDiff() / charge;
            int nearestPeakIdx = SpectrumUtils.getNearestPeakIndex(currentSpectrum.getMzList(), ipMoz);
            observed[rank] = ((1e6 * Math.abs(currentSpectrum.getMzList()[nearestPeakIdx] - ipMoz) / ipMoz) < ppmTol) ? (long) currentSpectrum.getIntensityList()[nearestPeakIdx] : 0;
            expected[rank] = (Float) pattern.mzAbundancePairs()[rank]._2;
        }

        double ratio = observed[shift] / expected[shift];
        for (int k = 0; k < observed.length; k++) {
            observed[k] = (long) Math.ceil(observed[k] / ratio);
        }
        score = TestUtils.chiSquareTest(expected, observed);

        for (int k = expected.length - 2; k >= 2; k--) {
            long[] sObserved = new long[k + 1];
            System.arraycopy(observed, 0, sObserved, 0, k + 1);
            double[] sExpected = new double[k + 1];
            System.arraycopy(expected, 0, sExpected, 0, k + 1);
            double s = TestUtils.chiSquareTest(sExpected, sObserved);
            if (s > score) {
                score = s;
            }
        }

        return new Tuple2<Double, TheoreticalIsotopePattern>(1.0 - score, pattern);
    }

}

class ProlineScorer implements Scorer {
    
      public  Tuple2<Double, TheoreticalIsotopePattern> score(SpectrumData currentSpectrum, double intialMz, int shift,  int charge, double ppmTol) {
      double score = 0.0;
      double mz = intialMz - shift * IsotopePatternEstimator.avgIsoMassDiff() / charge;
      TheoreticalIsotopePattern pattern = IsotopePatternEstimator.getTheoreticalPattern(mz, charge);
      double normalisationRatio = -1.0;
      Double ipMoz = mz;
      int matchingPeaksCount = 0;
      for (int rank = 0; rank < pattern.mzAbundancePairs().length; rank++) {
         ipMoz = (rank == 0) ? ipMoz : ipMoz + IsotopePatternEstimator.avgIsoMassDiff()/charge;
         int nearestPeakIdx =  SpectrumUtils.getNearestPeakIndex(currentSpectrum.getMzList(), ipMoz);
         if (normalisationRatio < 0) {
            normalisationRatio = currentSpectrum.getIntensityList()[nearestPeakIdx]/(Float) pattern.mzAbundancePairs()[0]._2;
         }
         double ipAbundance = ((Float) pattern.mzAbundancePairs()[rank]._2) * normalisationRatio;
         double penality = Math.min(100.0, 0.0001 * Math.pow(10, rank*2));
         double abundance = ((1e6 * Math.abs(currentSpectrum.getMzList()[nearestPeakIdx] - ipMoz) / ipMoz) < ppmTol) ? currentSpectrum.getIntensityList()[nearestPeakIdx] : ipAbundance/100.0;
         double d = ((ipAbundance - abundance) / Math.min(abundance, ipAbundance)) * 1.0/penality;
         score += d * d;
         if ((1e6 * Math.abs(currentSpectrum.getMzList()[nearestPeakIdx] - ipMoz) / ipMoz) < ppmTol) {
            matchingPeaksCount++;
            ipMoz = currentSpectrum.getMzList()[nearestPeakIdx];
         }
      }
      score = Math.log10(score) - matchingPeaksCount;

      return new Tuple2<Double, TheoreticalIsotopePattern>(score, pattern);
   }
}