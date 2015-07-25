package fr.proline.mzscope.model;

import fr.profi.ms.algo.IsotopePatternEstimator;
import fr.profi.ms.model.TheoreticalIsotopePattern;
import fr.profi.mzdb.model.ScanData;
import fr.proline.mzscope.utils.ScanUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import scala.collection.immutable.SortedMap;

/**
 *
 * @author CB205360
 */
public class IsotopePattern {

   public static List<Pair<Double, TheoreticalIsotopePattern>> getOrderedIPHypothesis(ScanData currentScan, double mz) {

      List<Pair<Double, TheoreticalIsotopePattern>> result = new ArrayList<>();
      for (int charge = 1; charge <= 5; charge++) {
         Pair<Double, TheoreticalIsotopePattern> p = getIPHypothese(currentScan, mz, charge);
         for (int j = 1; j <= p.getRight().theoreticalMaxPeakelIndex()+1; j++) {
            double alternativeMoz = mz - j * IsotopePatternEstimator.avgIsoMassDiff() / charge;
            Pair<Double, TheoreticalIsotopePattern> alternativeP = getIPHypothese(currentScan, alternativeMoz, charge);
            result.add(alternativeP);
         }
         result.add(p);
      }
      Collections.sort(result, new Comparator<Pair<Double, TheoreticalIsotopePattern>>() {
         @Override
         public int compare(Pair<Double, TheoreticalIsotopePattern> o1, Pair<Double, TheoreticalIsotopePattern> o2) {
            int sign = (int)Math.signum(o1.getLeft() - o2.getLeft());
            return (sign == 0) ? o2.getRight().charge() - o1.getRight().charge() : sign;
         }
      
      });
      return result;
   }

   private static Pair<Double, TheoreticalIsotopePattern> getIPHypothese(ScanData currentScan, double mz, int charge) {
      float ppmTol = MzScopePreferences.getInstance().getMzPPMTolerance();
      System.out.println("Hypotheses = " + mz + ", " + charge + "+");
      double score = 0.0;
      TheoreticalIsotopePattern pattern = IsotopePatternEstimator.getTheoreticalPattern(mz, charge);
      double normalisationRatio = -1.0;
      Double ipMoz = mz;
      int matchingPeaksCount = 0;
      for (int rank = 0; rank < pattern.mzAbundancePairs().length; rank++) {
         ipMoz = (rank == 0) ? ipMoz : ipMoz + IsotopePatternEstimator.avgIsoMassDiff()/charge;
         int nearestPeakIdx =  ScanUtils.getNearestPeakIndex(currentScan.getMzList(), ipMoz);
         if (normalisationRatio < 0) {
            normalisationRatio = currentScan.getIntensityList()[nearestPeakIdx]/(Float) pattern.mzAbundancePairs()[0]._2;
         }
         double ipAbundance = ((Float) pattern.mzAbundancePairs()[rank]._2) * normalisationRatio;
         double penality = Math.min(100.0, 0.0001 * Math.pow(10, rank*2));
         double abundance = ((1e6 * Math.abs(currentScan.getMzList()[nearestPeakIdx] - ipMoz) / ipMoz) < ppmTol) ? currentScan.getIntensityList()[nearestPeakIdx] : ipAbundance/100.0;
         double d = ((ipAbundance - abundance) / Math.min(abundance, ipAbundance)) * 1.0/penality;
         System.out.println("Exp Peak = " + ipMoz + ", exp Abun = " + ipAbundance + " nearest mz = "+ currentScan.getMzList()[nearestPeakIdx]+ ", obs Abun = "+ abundance + ", d = " + d);
         score += d * d;
         if ((1e6 * Math.abs(currentScan.getMzList()[nearestPeakIdx] - ipMoz) / ipMoz) < ppmTol) {
            matchingPeaksCount++;
            ipMoz = currentScan.getMzList()[nearestPeakIdx];
         }
      }
      score = Math.log10(score) - matchingPeaksCount;
      System.out.println("Score = " + score);

      return new ImmutablePair(score, pattern);
   }

   private static Pair<Double, TheoreticalIsotopePattern> getIPHypothese(ScanData currentScan, double mz, int charge, int matchingPeakIdx, int matchingIsotopeIdx) {
      float ppmTol = MzScopePreferences.getInstance().getMzPPMTolerance();
      System.out.println("Hypotheses = " + mz + ", " + charge + "+");
      double score = 0.0;
      TheoreticalIsotopePattern pattern = IsotopePatternEstimator.getTheoreticalPattern(mz, charge);
      double normalisationRatio = (matchingPeakIdx != -1) ? currentScan.getIntensityList()[matchingPeakIdx]/(Float) pattern.mzAbundancePairs()[matchingIsotopeIdx]._2 : -1.0;
      Double ipMoz = pattern.monoMz();
      int matchingPeaksCount = 0;
      for (int rank = 0; rank < pattern.mzAbundancePairs().length; rank++) {
         ipMoz = (rank == 0) ? ipMoz : ipMoz + IsotopePatternEstimator.avgIsoMassDiff()/charge;
         int nearestPeakIdx =  ScanUtils.getNearestPeakIndex(currentScan.getMzList(), ipMoz);
         if (normalisationRatio < 0) {
            normalisationRatio = currentScan.getIntensityList()[nearestPeakIdx]/(Float) pattern.mzAbundancePairs()[0]._2;
         }
         double ipAbundance = ((Float) pattern.mzAbundancePairs()[rank]._2) * normalisationRatio;
         double penality = Math.min(100.0, 0.0001 * Math.pow(10, rank*2));
         double abundance = ((1e6 * Math.abs(currentScan.getMzList()[nearestPeakIdx] - ipMoz) / ipMoz) < ppmTol) ? currentScan.getIntensityList()[nearestPeakIdx] : penality;
         double d = ((ipAbundance - abundance) / Math.min(abundance, ipAbundance)) * 1.0/penality;
         System.out.println("Exp Peak = " + ipMoz + ", exp Abun = " + ipAbundance + " nearest mz = "+ currentScan.getMzList()[nearestPeakIdx]+ ", obs Abun = "+ abundance + ", d = " + d);
         score += d * d;
         if ((1e6 * Math.abs(currentScan.getMzList()[nearestPeakIdx] - ipMoz) / ipMoz) < ppmTol) {
            matchingPeaksCount++;
            ipMoz = currentScan.getMzList()[nearestPeakIdx];
         }
      }
      score = Math.log10(score) - matchingPeaksCount;
      System.out.println("Score = " + score);

      return new ImmutablePair(score, pattern);
   }

   public static void compare(TreeMap<Double, TheoreticalIsotopePattern> putativePatterns, SortedMap map) {
      for (Map.Entry entry : putativePatterns.entrySet()) {
         if (!map.contains(entry.getKey()))
            System.out.println("Key "+entry.getKey()+" not found in Scala map");
         else 
            System.out.println("IP hypothesis similar");
      }
   }

}
