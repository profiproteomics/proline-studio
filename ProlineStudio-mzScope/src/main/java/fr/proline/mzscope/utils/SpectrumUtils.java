package fr.proline.mzscope.utils;

import fr.profi.mzdb.model.SpectrumHeader;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author MB243701
 */
public class SpectrumUtils {

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
}
