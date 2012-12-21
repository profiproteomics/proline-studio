package fr.proline.studio.rsmexplorer.node;

import fr.proline.studio.dam.data.AbstractData;



/**
 * Class used to create children of a note asynchronously
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
            case RESULT_SET:
                result = new RSMResultSetNode(key);
                break;
            case RESULT_SUMMARY:
                result = new RSMResultSummaryNode(key);
                break;
            case DATA_SET:
                result = new RSMDataSetNode(key);
                break;
            case IDENTIFICATION:
                result = new RSMIdentificationNode(key);
                break;
            case IDENTIFICATION_FRACTION:
                result = new RSMIdentificationFractionNode(key);
                break;
        }
        
        if (key.hasChildren()) {
            result.add(new RSMHourGlassNode(null));
        }
        
        return result;
    }
    
    
    
    
    
 
    
}
