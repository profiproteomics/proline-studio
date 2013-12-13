package fr.proline.studio.utils;

import org.openide.nodes.Sheet;

/**
 * A class must implements this interface to provide properties
 * @author JM235353
 */
public interface PropertiesProviderInterface {
    
    public Sheet createSheet();
    
    public void loadDataForProperties(Runnable callback);
    
    
}
