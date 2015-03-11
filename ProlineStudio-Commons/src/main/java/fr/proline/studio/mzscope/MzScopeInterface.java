package fr.proline.studio.mzscope;

import java.util.List;


/**
 * gives information needed to display mzscope
 * @author MB243701
 */
public interface MzScopeInterface {
    /**
     * returns the mzdb filenames to display
     * @return 
     */
    public List<String> getMzdbFileName();
}
