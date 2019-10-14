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
public class DotProductScorer implements Scorer {
    
    private static final Logger logger = LoggerFactory.getLogger(DotProductScorer.class);
    
    private static final int MAX_SCORED_ISOTOPES = 8; 
    
       public Tuple2<Double, TheoreticalIsotopePattern> score(SpectrumData currentSpectrum, double intialMz, int shift, int charge, double ppmTol) {
        double score = 0.0;        
        double mz = intialMz - shift * IsotopePatternEstimator.avgIsoMassDiff() / charge;
        TheoreticalIsotopePattern pattern = IsotopePatternEstimator.getTheoreticalPattern(mz, charge);
        
        double scale = currentSpectrum.getIntensityList()[SpectrumUtils.getNearestPeakIndex(currentSpectrum.getMzList(), intialMz)] / (Float)pattern.mzAbundancePairs()[shift]._2;

        Double ipMoz = mz;
        double[] observed = new double[pattern.mzAbundancePairs().length];
        double[] expected = new double[pattern.mzAbundancePairs().length];
        int observations = 0;
        //logger.info("mz {} charge pattern {}+, nb isotopes = {}", mz, charge, pattern.mzAbundancePairs().length);
        
        for (int rank = 0; rank < pattern.mzAbundancePairs().length; rank++) {
            ipMoz = (rank == 0) ? ipMoz : ipMoz + IsotopePatternEstimator.avgIsoMassDiff() / charge;
            int nearestPeakIdx = SpectrumUtils.getNearestPeakIndex(currentSpectrum.getMzList(), ipMoz);
               if (((1e6 * Math.abs(currentSpectrum.getMzList()[nearestPeakIdx] - ipMoz) / ipMoz) < ppmTol)) {
                   observed[rank] = currentSpectrum.getIntensityList()[nearestPeakIdx];
                   observations++;
               } else {
                   // test minus expected abundance to penalise signal absence
                   observed[rank] = -(Float)pattern.mzAbundancePairs()[rank]._2 * scale; // was 0.0
                }
            expected[rank] = (Float) pattern.mzAbundancePairs()[rank]._2;
        }

        score = dotProduct(observed, expected);
        score = 1.0 - score;
        return new Tuple2<Double, TheoreticalIsotopePattern>(score, pattern);
    }
       
    public static double dotProduct(double[] observed, double[] expected) {
        double sumObserved = 0.0;
        double sumExpected = 0.0;
        double dotProduct = 0.0;
                
        for (int k = 0; k < Math.min(observed.length, MAX_SCORED_ISOTOPES); k++) {
            dotProduct += observed[k]*expected[k];
            sumExpected += expected[k]*expected[k];
            sumObserved += observed[k]*observed[k];
        }
        
        return ((sumExpected == 0) || (sumObserved == 0)) ? 0.0 : dotProduct/(Math.sqrt(sumExpected)*Math.sqrt(sumObserved));
    }
}
