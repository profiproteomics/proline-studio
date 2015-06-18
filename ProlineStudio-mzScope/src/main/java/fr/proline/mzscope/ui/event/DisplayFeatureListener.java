package fr.proline.mzscope.ui.event;

import fr.profi.mzdb.model.Feature;
import fr.proline.mzscope.model.IRawFile;
import java.util.EventListener;

/**
 * feature/peak selected in the FeatureTable and should be display 
 * @author MB243701
 */
public interface DisplayFeatureListener extends EventListener{
    /**
     * display Feature in the raw file corresponding to the rawFile
     * @param f 
     * @param rawFile 
     */
    public void displayFeatureInRawFile(Feature f, IRawFile rawFile);
    
    /**
     * display Feature in the current raw file
     * @param f 
     */
    public void displayFeatureInCurrentRawFile(Feature f);
}
