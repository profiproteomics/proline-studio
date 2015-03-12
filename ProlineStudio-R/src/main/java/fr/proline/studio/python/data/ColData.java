package fr.proline.studio.python.data;

import fr.proline.studio.python.util.Conversion;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import org.python.core.Py;

/**
 *
 * @author JM235353
 */
public class ColData extends Col {
    
    private final ArrayList<Double> m_data;
    
    public ColData(ArrayList<Double> data, String name) {
        m_data = data;
        m_columnName = name;
    }

    ColData() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object getValueAt(int row) {
        return m_data.get(row);
    }
    
    @Override
    public void setValuetAt(int row, Object o) {
        Number n = Conversion.convertToJavaNumber(o);
        if (n == null) {
            m_data.set(row, null);
        } else {
            m_data.set(row, Double.valueOf(n.doubleValue()));
        }
        
        
    }

    @Override
    public Col mutable() {
        return this;
    }
    
    @Override
    public int getRowCount() {
        return m_data.size();
    }
    

    
}
