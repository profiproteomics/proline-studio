package fr.proline.studio.comparedata;

import java.util.ArrayList;

/**
 *
 * @author JM235353
 */
public interface ExtraDataInterface {
    
    public ArrayList<ExtraDataType> getExtraDataTypes();
    
    public Object getValue(Class c);
    
    public Object getRowValue(Class c, int row);
    
    public Object getColValue(Class c, int col);
    
    public void addSingleValue(Object v);
    
    public Object getSingleValue(Class c);
    
    
    
}
