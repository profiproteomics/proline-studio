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
}
