package fr.proline.mzscope.ui;

import fr.proline.mzscope.model.Ms1ExtractionRequest;


/**
 * extraction param events (from the XICExtractionPanel)
 *
 * @author MB243701
 */
public interface IExtractionExecutor  {

    public void extractChromatogramMass(Ms1ExtractionRequest params); 

}
