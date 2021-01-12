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

import fr.profi.ms.algo.IsotopePatternEstimator;
import fr.profi.ms.model.TheoreticalIsotopePattern;
import fr.profi.mzdb.algo.IsotopicPatternScorer;
import fr.profi.mzdb.model.SpectrumData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;

/**
 *
 * @author CB205360
 */
public class IsotopicPatternUtils {

    private static final Logger logger = LoggerFactory.getLogger(IsotopicPatternUtils.class);

      private static final int MAX_CHARGE_STATE = 8;

    static class Pattern {
        
        public double mz;
        public int charge;

        public Pattern(double mz, int charge) {
            this.mz = mz;
            this.charge = charge;
        }

        
        @Override
        public int hashCode() {
            int hash = MAX_CHARGE_STATE;
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
        //tracePredictions("Proline", mz, ppmTol, putativePatterns);
        
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
        tracePredictions("Comparison ([Proline, ChiSquare, DotProduct]", mz, ppmTol, map);
        return map;
    }
    
    public static Tuple2<Object, TheoreticalIsotopePattern> predictIsotopicPattern(SpectrumData spectrum, double mz, double ppmTol) {
//        long start = System.currentTimeMillis();
        double fittedPpmTol = ppmTol;
        
        int nearestPeakIdx = SpectrumUtils.getNearestPeakIndex(spectrum.getMzList(), mz);
        if (SpectrumUtils.isInRange(spectrum.getMzList()[nearestPeakIdx], mz, ppmTol) && (spectrum.getLeftHwhmList() != null)) {
            if (spectrum.getLeftHwhmList()[nearestPeakIdx] > 0.0f) {
                fittedPpmTol = (float) (1e6 * spectrum.getLeftHwhmList()[nearestPeakIdx] / spectrum.getMzList()[nearestPeakIdx]);
            }
        }

//        if (fittedPpmTol > ppmTol) {
//          logger.debug("tol set to {} instead of {}", fittedPpmTol, ppmTol);
//        }
        //Tuple2<Object, TheoreticalIsotopePattern>[] putativePatterns = IsotopicPatternScorer.calcIsotopicPatternHypotheses(spectrum, mz, fittedPpmTol);
        Tuple2<Object, TheoreticalIsotopePattern>[] putativePatterns = calcIsotopicPatternHypotheses(spectrum, mz, fittedPpmTol, new WeightedDotProductScorer());
        // AAAARRRRRRGGGG : CBy rule : if two DotProduct predictions are close, prefer higher charge state or lower mono MZ
        selectBestHypothesis(putativePatterns, 0.1);

//        Tuple2<Object, TheoreticalIsotopePattern>[] putativePatterns = calcIsotopicPatternHypotheses(spectrum, mz, fittedPpmTol, new ProlineLikeScorer());
//        logger.info("Prediction took {} ms", (System.currentTimeMillis() - start));
        return putativePatterns[0];
    }

  private static void selectBestHypothesis(Tuple2<Object, TheoreticalIsotopePattern>[] putativePatterns, double deltaScore) {
    double refScore = (Double)putativePatterns[0]._1;
    
    List<Tuple2<Object, TheoreticalIsotopePattern>> list = Arrays.stream(putativePatterns).filter(t -> Math.abs((Double)t._1 - refScore) <= deltaScore ).collect(Collectors.toList());

    if (list.isEmpty())
      return;

    int maxCharge = list.stream().max(Comparator.comparing(t -> t._2.charge())).get()._2.charge();
    Optional<Tuple2<Object, TheoreticalIsotopePattern>> betterPattern = list.stream().filter(t -> t._2.charge() == maxCharge).collect(Collectors.minBy(Comparator.comparing(t->(Double)t._1)));

    if (betterPattern.isPresent()) {
      // a simpler solution is to return a List from the prediction then insert bestPattern at the head of the list and
      // remove if from it's previous position.
      Tuple2<Object, TheoreticalIsotopePattern> bestPattern = betterPattern.get();
      if (bestPattern != putativePatterns[0]) {
        Tuple2<Object, TheoreticalIsotopePattern> tmp = putativePatterns[0];
        // search for bestPattern index k
        int k = 1;
        for (; k < putativePatterns.length; k++) {
          Tuple2<Object, TheoreticalIsotopePattern> p = putativePatterns[k];
          if (p == bestPattern) break;
        }
//                logger.debug("A better prediction is available at position {}, swap them", k);
        putativePatterns[0] = putativePatterns[k];
        putativePatterns[k] = tmp;
      }
    }

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
    
     private static Tuple2<Object, TheoreticalIsotopePattern>[] calcIsotopicPatternHypotheses(SpectrumData currentSpectrum, double mz, double ppmTol, Scorer scorer) {
            
      List<Tuple2<Double, TheoreticalIsotopePattern>> result = new ArrayList<>();
      for (int charge = 1; charge <= MAX_CHARGE_STATE; charge++) {
         Tuple2<Double, TheoreticalIsotopePattern> p = scorer.score(currentSpectrum, mz, 0, charge, ppmTol);
         
         int j = 1;
         double backwardMz = mz - j*IsotopePatternEstimator.avgIsoMassDiff()/charge;
         int nearestPeakIdx = SpectrumUtils.getNearestPeakIndex(currentSpectrum.getMzList(), backwardMz);
         boolean existBackward = ((1e6 * Math.abs(currentSpectrum.getMzList()[nearestPeakIdx] - backwardMz) / backwardMz) < ppmTol);
         
         while (existBackward && j <= p._2.theoreticalMaxPeakelIndex()+1) {
            Tuple2<Double, TheoreticalIsotopePattern> alternativeP = scorer.score(currentSpectrum, mz, j, charge, ppmTol);
            result.add(alternativeP);
            j++;
            backwardMz = mz - j*IsotopePatternEstimator.avgIsoMassDiff()/charge;
            nearestPeakIdx = SpectrumUtils.getNearestPeakIndex(currentSpectrum.getMzList(), backwardMz);
            existBackward = ((1e6 * Math.abs(currentSpectrum.getMzList()[nearestPeakIdx] - backwardMz) / backwardMz) < ppmTol);
         }
         result.add(p);
      }
      if (!result.isEmpty()) {
        Collections.sort(result, new Comparator<Tuple2<Double, TheoreticalIsotopePattern>>() {
           @Override
           public int compare(Tuple2<Double, TheoreticalIsotopePattern> o1, Tuple2<Double, TheoreticalIsotopePattern> o2) {
              int sign = (int)Math.signum(o1._1 - o2._1);
              return (sign == 0) ? o2._2.charge() - o1._2.charge() : sign;
           }

        });
      }
      Tuple2<Object, TheoreticalIsotopePattern>[] patterns = result.toArray(new Tuple2[result.size()]);
      //tracePredictions(scorer.getClass().getSimpleName(), mz, ppmTol, patterns);
      return patterns;
   }

}


class ProlineLikeScorer implements Scorer {

  public Tuple2<Double, TheoreticalIsotopePattern> score(SpectrumData currentSpectrum, double intialMz, int shift, int charge, double ppmTol) {
    double score = 0.0;
    double mz = intialMz - shift * IsotopePatternEstimator.avgIsoMassDiff() / charge;
    TheoreticalIsotopePattern pattern = IsotopePatternEstimator.getTheoreticalPattern(mz, charge);
    double normalisationRatio = -1.0;
    Double ipMoz = mz;
    int matchingPeaksCount = 0;
    for (int rank = 0; rank < pattern.mzAbundancePairs().length; rank++) {
      ipMoz = (rank == 0) ? ipMoz : ipMoz + IsotopePatternEstimator.avgIsoMassDiff() / charge;
      int nearestPeakIdx = SpectrumUtils.getNearestPeakIndex(currentSpectrum.getMzList(), ipMoz);
      if (normalisationRatio < 0) {
        normalisationRatio = currentSpectrum.getIntensityList()[nearestPeakIdx] / (Float) pattern.mzAbundancePairs()[0]._2;
      }
      double ipAbundance = ((Float) pattern.mzAbundancePairs()[rank]._2) * normalisationRatio;
      double penality = Math.min(100.0, 0.0001 * Math.pow(10, rank * 2));
      double abundance = ((1e6 * Math.abs(currentSpectrum.getMzList()[nearestPeakIdx] - ipMoz) / ipMoz) < ppmTol) ? currentSpectrum.getIntensityList()[nearestPeakIdx] : ipAbundance / 100.0;
      double d = ((ipAbundance - abundance) / Math.min(abundance, ipAbundance)) * 1.0 / penality;
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
