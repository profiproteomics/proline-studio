/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.mzscope.processing;

import fr.profi.ms.algo.IsotopePatternEstimator;
import fr.profi.ms.model.TheoreticalIsotopePattern;
import fr.profi.mzdb.model.SpectrumData;
import org.apache.commons.math3.stat.inference.TestUtils;
import scala.Tuple2;

/**
 *
 * @author CB205360
 */
public class ChiSquareScorer implements Scorer {
    
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
