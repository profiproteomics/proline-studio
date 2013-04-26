package fr.proline.studio.rsmexplorer.node;

import fr.proline.studio.dam.data.AbstractData;



/**
 * Class used to create children of a node asynchronously
 * @author JM235353
 */
public class RSMChildFactory  {

    public static RSMNode createNode(AbstractData key) {
        
        // result Node
        RSMNode result = null;
        
                // Creation of the correct Node type
        AbstractData.DataTypes type = key.getDataType();
        switch (type) {
            case MAIN:
                result = new RSMTreeParentNode(key);
                break;
            case PROJECT:
                result = new RSMProjectNode(key);
                break;
            case DATA_SET:
                result = new RSMDataSetNode(key);
                break;
            case ALL_IMPORTED:
                result = new RSMAllImportedNode(key);
                break;
        }
        
        if (key.hasChildren()) {
            result.add(new RSMHourGlassNode(null));
        }
        
        return result;
    }
    
    
    
    
    
 
    
}
