package fr.proline.mzscope.util;

import fr.profi.mzdb.model.ScanHeader;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author MB243701
 */
public class ScanUtils {

    /**
     * sort scanHeader by mz
     *
     * @param scans
     * @return
     */
    public static ScanHeader[] sortScanHeader(ScanHeader[] scans) {
        ScanHeader[] ms2ScanHeaders = null;
        if (scans != null) {
            int nbSc = scans.length;
            ms2ScanHeaders = new ScanHeader[nbSc];
            List<ScanHeader> list = Arrays.asList(scans);
            // sort by precursorMz
            Collections.sort(list, new Comparator<ScanHeader>() {

                @Override
                public int compare(ScanHeader sc1, ScanHeader sc2) {
                    if (sc1.getPrecursorMz() <= sc2.getPrecursorMz()){
                        return -1;
                    }else {
                        return 1;
                    }
                }
                
            });
            ms2ScanHeaders = list.toArray(new ScanHeader[nbSc]);

        }
        return ms2ScanHeaders;
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
}
