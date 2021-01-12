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
import fr.profi.mzdb.model.SpectrumData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;

/**
 *
 * @author CB205360
 */
public class WeightedDotProductScorer implements Scorer {
    
    private static final Logger logger = LoggerFactory.getLogger(WeightedDotProductScorer.class);

    private static final int MAX_SCORED_ISOTOPES = 8;

  public Tuple2<Double, TheoreticalIsotopePattern> score(SpectrumData currentSpectrum, double intialMz, int shift, int charge, double initialPpmTol) {
        double score = 0.0;
        double ppmTol = initialPpmTol;
        double mz = intialMz - shift * IsotopePatternEstimator.avgIsoMassDiff() / charge;
        TheoreticalIsotopePattern pattern = IsotopePatternEstimator.getTheoreticalPattern(mz, charge);
        
        double scale = currentSpectrum.getIntensityList()[SpectrumUtils.getNearestPeakIndex(currentSpectrum.getMzList(), intialMz)] / (Float)pattern.mzAbundancePairs()[shift]._2;
         
        Double ipMoz = mz;
        double[] observed = new double[pattern.mzAbundancePairs().length];
        double[] expected = new double[pattern.mzAbundancePairs().length];
        
        for (int rank = 0; rank < pattern.mzAbundancePairs().length; rank++) {
            ipMoz = (rank == 0) ? ipMoz : ipMoz + IsotopePatternEstimator.avgIsoMassDiff() / charge;
            int nearestPeakIdx = SpectrumUtils.getNearestPeakIndex(currentSpectrum.getMzList(), ipMoz);
               if (((1e6 * Math.abs(currentSpectrum.getMzList()[nearestPeakIdx] - ipMoz) / ipMoz) < ppmTol)) {
                   observed[rank] = currentSpectrum.getIntensityList()[nearestPeakIdx];
                   // Try: reassign ipMoz with the observed value !
                   //ipMoz = currentSpectrum.getMzList()[nearestPeakIdx];
               } else {
                   observed[rank] = -(Float)pattern.mzAbundancePairs()[rank]._2 * scale;
               }
            // Try: increase ppmTol for higher isotope rank
            //double ppmIncr = Math.max(1, ppmTol/MAX_SCORED_ISOTOPES);
            //ppmTol = Math.min( 2*initialPpmTol , ppmTol + ppmIncr);
            expected[rank] = (Float) pattern.mzAbundancePairs()[rank]._2;
        }

        score = dotProduct(observed, expected);
        score = (1.0 - score);
        //score = 1.0 - score;
        return new Tuple2<Double, TheoreticalIsotopePattern>(score, pattern);
    }
       
    public static double dotProduct(double[] observed, double[] expected) {
      
      
        double sumObserved = 0.0;
        double sumExpected = 0.0;
        double dotProduct = 0.0;
        
        double sumWeight = 0.0;
        double[] weight = {0.25, 0.25, 0.25, 0.08, 0.06, 0.05, 0.04, 0.02};
        
        for (int k = 0; k < Math.min(observed.length, MAX_SCORED_ISOTOPES); k++) {
          sumWeight += weight[k];
        }
        
        for (int k = 0; k < Math.min(observed.length, MAX_SCORED_ISOTOPES); k++) {
            weight[k] = weight[k]/sumWeight;
            dotProduct += observed[k]*expected[k]*weight[k];
            sumExpected += expected[k]*expected[k]*weight[k];
            sumObserved += observed[k]*observed[k]*weight[k];
        }
        
        return ((sumExpected == 0) || (sumObserved == 0)) ? 0.0 : dotProduct/(Math.sqrt(sumExpected)*Math.sqrt(sumObserved));
    }
}
