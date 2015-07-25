package fr.proline.mzscope.ui.event;

import fr.proline.mzscope.utils.MzScopeConstants;
import java.util.EventListener;

/**
 * scan header events (from the HeaderSpectrumPanel)
 *
 * @author MB243701
 */
public interface ScanHeaderListener extends EventListener {

    /**
     * update the scan index value
     *
     * @param scanIndex new scanIndex value
     */
    public void updateScanIndex(Integer scanIndex);

    /**
     * update the retention time value (in sec)
     *
     * @param retentionTime the new retention time value (in sec)
     */
    public void updateRetentionTime(float retentionTime);

    /**
     * keep the msLevel or not while changing the scanIndex value
     * @param keep 
     */
    public void keepMsLevel(boolean keep);
    
    
    /**
     * display xic as overlay or replace mode
     * @param mode 
     */
    public void updateXicDisplayMode(MzScopeConstants.DisplayMode mode);
    

}
