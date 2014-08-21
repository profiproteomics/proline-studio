package fr.proline.studio.rsmexplorer.tree;

import fr.proline.studio.rsmexplorer.tree.quantitation.QuantitationProjectNode;
import fr.proline.studio.rsmexplorer.tree.identification.IdProjectIdentificationNode;
import fr.proline.studio.rsmexplorer.tree.identification.IdAllImportedNode;
import fr.proline.studio.dam.data.AbstractData;



/**
 * Class used to create children of a node asynchronously
 * @author JM235353
 */
public class ChildFactory  {

    public static AbstractNode createNode(AbstractData key) {
        
        // result Node
        AbstractNode result = null;
        
                // Creation of the correct Node type
        AbstractData.DataTypes type = key.getDataType();
        switch (type) {
            case MAIN:
                result = new TreeParentNode(key);
                break;
            case PROJECT_IDENTIFICATION:
                result = new IdProjectIdentificationNode(key);
                break;
            case PROJECT_QUANTITATION:
                result = new QuantitationProjectNode(key);
                break;
            case DATA_SET:
                result = new DataSetNode(key);
                break;
            case ALL_IMPORTED:
                result = new IdAllImportedNode(key);
                break;
        }
        
        if (key.hasChildren()) {
            result.add(new HourGlassNode(null));
        }
        
        return result;
    }
    
    
    
    
    
 
    
}
