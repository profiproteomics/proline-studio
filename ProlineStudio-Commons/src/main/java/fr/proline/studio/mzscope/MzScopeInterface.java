package fr.proline.studio.mzscope;

import java.util.List;


/**
 * gives information needed to display mzscope
 * 
 * @author MB243701
 */
public interface MzScopeInterface {
    /**
     * returns the mzdb information to display
     * @return 
     */
    public List<MzdbInfo> getMzdbInfo();
}
