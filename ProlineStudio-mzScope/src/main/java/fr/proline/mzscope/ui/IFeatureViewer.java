package fr.proline.mzscope.ui;

import fr.proline.mzscope.model.IFeature;
import fr.proline.mzscope.model.IRawFile;

/**
 * feature/peak selected in the FeatureTable and should be display 
 * @author MB243701
 */
public interface IFeatureViewer {
    /**
     * display Feature in the raw file corresponding to the rawFile
     * @param f 
     * @param rawFile 
     */
    public void displayFeatureInRawFile(IFeature f, IRawFile rawFile);
    
    /**
     * display Feature in the current raw file
     * @param f 
     */
    public void displayFeatureInCurrentRawFile(IFeature f);
}
