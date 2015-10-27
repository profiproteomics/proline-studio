package fr.proline.mzscope.ui;

import fr.proline.mzscope.model.Ms1ExtractionRequest;


/**
 * extraction param methods (from the XICExtractionPanel)
 *
 * @author MB243701
 */
public interface IExtractionExecutor  {

    public void extractChromatogramMass(Ms1ExtractionRequest params); 

}
