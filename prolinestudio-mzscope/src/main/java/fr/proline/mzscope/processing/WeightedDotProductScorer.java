/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
    
       public Tuple2<Double, TheoreticalIsotopePattern> score(SpectrumData currentSpectrum, double intialMz, int shift, int charge, double ppmTol) {
        double score = 0.0;
        double mz = intialMz - shift * IsotopePatternEstimator.avgIsoMassDiff() / charge;
        TheoreticalIsotopePattern pattern = IsotopePatternEstimator.getTheoreticalPattern(mz, charge);
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
                   observed[rank] = 0.0;
               }
            expected[rank] = (Float) pattern.mzAbundancePairs()[rank]._2;
        }

        score = dotProduct(observed, expected);
        //score = (1.0 - score)/observations;
        score = 1.0 - score;
        return new Tuple2<Double, TheoreticalIsotopePattern>(score, pattern);
    }
       
    public static double dotProduct(double[] observed, double[] expected) {
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
