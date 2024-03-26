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

import fr.profi.ms.algo.IsotopePatternEstimator;
import fr.profi.ms.model.TheoreticalIsotopePattern;
import fr.profi.mzdb.algo.DotProductPatternScorer;
import fr.profi.mzdb.algo.LegacyIsotopicPatternScorer;
import fr.profi.mzdb.model.SpectrumData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;

import java.util.*;
import java.util.stream.Collectors;

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
        Tuple2<Object, TheoreticalIsotopePattern>[] putativePatterns = LegacyIsotopicPatternScorer.calcIsotopicPatternHypotheses(spectrum, mz, fittedPpmTol);
        for (Tuple2<Object, TheoreticalIsotopePattern> t : putativePatterns) {
            Pattern p = new Pattern(t._2.monoMz(), t._2.charge());
            if (!map.containsKey(p)) map.put(p, new ArrayList<>());
            map.get(p).add((Double)t._1);
        }
        //tracePredictions("Proline", mz, ppmTol, putativePatterns);
        
        putativePatterns = _calcIsotopicPatternHypotheses(spectrum, mz, fittedPpmTol, new ChiSquareScorer());
        for (Tuple2<Object, TheoreticalIsotopePattern> t : putativePatterns) {
            Pattern p = new Pattern(t._2.monoMz(), t._2.charge());
            if (!map.containsKey(p)) { 
                map.put(p, new ArrayList<>());
                map.get(p).add(0.0);
            }
            map.get(p).add((Double)t._1);
        }
        
        putativePatterns = _calcIsotopicPatternHypotheses(spectrum, mz, fittedPpmTol, new DotProductScorer());
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

    double tol = 2.0* ppmTol*mz/1e6;
    Set<Integer> putativeCharges = new HashSet<>();
    double[] mzList = spectrum.getMzList();
    int nearestPeakIdx = SpectrumUtils.getNearestPeakIndex(mzList, mz);

    for (int k = nearestPeakIdx-1; k >=0; k--) {
      if ((mzList[nearestPeakIdx] - mzList[k]) < (tol + IsotopePatternEstimator.avgIsoMassDiff())) {
        int putativeCharge = Math.round((float)(1.0 / (mzList[nearestPeakIdx] - mzList[k])));
        if (Math.abs((mzList[nearestPeakIdx] - IsotopePatternEstimator.avgIsoMassDiff()/putativeCharge) - mzList[k]) < tol) {
          putativeCharges.add(putativeCharge);
        }
      } else {
        break;
      }
    }

    for (int k = nearestPeakIdx+1 ; k <spectrum.getPeaksCount(); k++) {
      if ((mzList[k] - mzList[nearestPeakIdx]) < (tol + IsotopePatternEstimator.avgIsoMassDiff())) {
        int putativeCharge = Math.round((float)(1.0 / (mzList[k] - mzList[nearestPeakIdx])));
        if (Math.abs((mzList[nearestPeakIdx] + IsotopePatternEstimator.avgIsoMassDiff()/putativeCharge) - mzList[k]) < tol) {
          putativeCharges.add(putativeCharge);
        }
      } else {
        break;
      }
    }
    List<Integer> charges = putativeCharges.stream().filter(z -> (z > 0) && (z <= MAX_CHARGE_STATE) ).sorted().collect(Collectors.toList());
    //logger.info("Putative Charges : "+charges.toString());


    double fittedPpmTol = ppmTol;
    Tuple2<Object, TheoreticalIsotopePattern>[] putativePatterns = DotProductPatternScorer.calcIsotopicPatternHypotheses(spectrum, mz, fittedPpmTol);
    Tuple2<Object, TheoreticalIsotopePattern> bestPatternHypothese = DotProductPatternScorer.selectBestPatternHypothese(putativePatterns, 0.1);

        double targetMz = mz;
        while (Math.abs(1e6*(targetMz - bestPatternHypothese._2.monoMz())/targetMz) > ppmTol) {
          targetMz = bestPatternHypothese._2.monoMz();
          putativePatterns = DotProductPatternScorer.calcIsotopicPatternHypothesesFromCharge(spectrum, targetMz, bestPatternHypothese._2.charge(), fittedPpmTol);
          bestPatternHypothese = DotProductPatternScorer.selectBestPatternHypothese(putativePatterns, 0.1);
        }


//    if (!charges.contains(bestPatternHypothese._2.charge())) {
//      logger.info(" !!!!!  -> The predicted charge is not in the expected range  !!!! ");
//    }

    return bestPatternHypothese;
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
    
     private static Tuple2<Object, TheoreticalIsotopePattern>[] _calcIsotopicPatternHypotheses(SpectrumData currentSpectrum, double mz, double ppmTol, Scorer scorer) {
            
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
        Collections.sort(result, (o1, o2) -> {
           int sign = (int)Math.signum(o1._1 - o2._1);
           return (sign == 0) ? o2._2.charge() - o1._2.charge() : sign;
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
