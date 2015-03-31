package fr.proline.mzscope.ui.event;

import java.util.EventListener;

/**
 * extraction param events (from the XICExtractionPanel)
 *
 * @author MB243701
 */
public interface ExtractionListener extends EventListener {

    /**
     * extract the chromatogram for the range defined by minMz - maxMz
     * @param minMz
     * @param MaxMz 
     */
    public void extractChromatogramMass(double minMz, double MaxMz);
 

}
