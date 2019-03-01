package fr.proline.studio.extendedtablemodel;

import java.util.ArrayList;

/**
 * Interface to be used when we want to add extra info to a table model.
 * 
 * @author JM235353
 */
public interface ExtraDataForTableModelInterface {
    
    // Used for the model to give extra Data Types as out parameters for databoxes.
    public ArrayList<ExtraDataType> getExtraDataTypes();
    
    // Get a value corresponding to the class.
    // This value can not be changed and is a constant of the underlying model.
    // (JPM.TODO : this method could be removed, and be merged with getSingleValue(Class c);
    public Object getValue(Class c);
    
    // get a value of the defined class, corresponding to the row
    public Object getRowValue(Class c, int row);
    
    // get a value of the defined class, corresponding to the col
    public Object getColValue(Class c, int col);
    
    // Add/Retrieve a value to a table model. 
    // It is possible to add multiple values if they are instances of different classes
    public void addSingleValue(Object v);
    public Object getSingleValue(Class c);
    
    
    
}
