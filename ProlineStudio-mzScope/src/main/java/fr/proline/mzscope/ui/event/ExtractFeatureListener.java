package fr.proline.mzscope.ui.event;

import java.util.EventListener;

/**
 *
 * @author MB243701
 */
public interface ExtractFeatureListener extends EventListener {
    /**
     * set the features extraction and the peakels detection
     * @param extractFeatures
     * @param detectPeakels 
     */
    public void extractFeatureListener(boolean extractFeatures, boolean detectPeakels);
}
