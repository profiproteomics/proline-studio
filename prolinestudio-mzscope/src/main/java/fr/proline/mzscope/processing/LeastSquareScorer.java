package fr.proline.mzscope.processing;

import fr.profi.ms.algo.IsotopePatternEstimator;
import fr.profi.ms.model.TheoreticalIsotopePattern;
import fr.profi.mzdb.model.SpectrumData;
import scala.Tuple2;

import java.util.Arrays;

public class LeastSquareScorer implements Scorer {

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

      for (int rank = 0; rank < pattern.mzAbundancePairs().length ; rank++) {
        ipMoz = (rank == 0) ? ipMoz : ipMoz + IsotopePatternEstimator.avgIsoMassDiff() / charge;
        int nearestPeakIdx = SpectrumUtils.getNearestPeakIndex(currentSpectrum.getMzList(), ipMoz);
        if (((1e6 * Math.abs(currentSpectrum.getMzList()[nearestPeakIdx] - ipMoz) / ipMoz) < ppmTol)) {
          observed[rank] = currentSpectrum.getIntensityList()[nearestPeakIdx];
          observations++;
        } else {
          //  minus expected abundance to penalise signal absence
          observed[rank] = -(Float)pattern.mzAbundancePairs()[rank]._2 * scale;
        }
        expected[rank] = (Float) pattern.mzAbundancePairs()[rank]._2 ;
      }

      score = leastSquare(observed, expected);
      return new Tuple2<Double, TheoreticalIsotopePattern>(score, pattern);
    }

  private static double leastSquare(double[] observed, double[] expected) {

    double sumExpected = 0.0;
    double score = 0.0;

    double max = Arrays.stream(observed).max().getAsDouble();

//        for (int k = 0; k < Math.min(observed.length, MAX_SCORED_ISOTOPES); k++) {
    for (int k = 0; (k < observed.length) && (expected[k] > 0.1) ; k++) {
      score += (100*observed[k]/max - expected[k]) * (100*observed[k]/max - expected[k]);
      sumExpected += expected[k]*expected[k];
    }

    return (sumExpected == 0) ? 0.0 : score/sumExpected ;
  }


}
