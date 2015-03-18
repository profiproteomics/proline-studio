package fr.proline.mzscope.ui.event;

import java.util.EventListener;

/**
 * extraciton param events (from the XICExtractionPanel)
 *
 * @author MB243701
 */
public interface ExtractionListener extends EventListener {

    /**
     * extract the chromatogram for the range defined by minMz - maxMz
     * extractionMode corresponds to replace - overlay or sum mode defined in MzScopeConstants
     * @param minMz
     * @param MaxMz 
     * @param extractionMode 
     */
    public void extractChromatogram(double minMz, double MaxMz, int extractionMode);
 
    public void updateXicModeDisplay(int extractionMode);

}
