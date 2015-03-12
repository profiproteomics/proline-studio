package fr.proline.studio.python.data;

import fr.proline.studio.python.util.Conversion;
import fr.proline.studio.table.CompoundTableModel;
import fr.proline.studio.table.LazyData;
import java.util.ArrayList;
import org.python.core.Py;
import org.python.core.PyObject;

/**
 *
 * @author JM235353
 */
public class ColRef extends Col {
    
    private final int m_modelCol;
    private final CompoundTableModel m_tableModel;
    
    public ColRef(int col, CompoundTableModel model) {
        m_modelCol = col-1;
        m_tableModel = model;
    }
    
    @Override
    public Col mutable() {
        int nb = __len__();
        ArrayList<Double> resultArray = new ArrayList<>(nb);
        for (int i = 0; i < nb; i++) {
            Number v = Conversion.convertToJavaNumber(getValueAt(i));
            if (v ==  null) {
                 resultArray.add(null);
            } else {
                 resultArray.add(v.doubleValue());
            }
        }
        return new ColData(resultArray, getColumnName());
    }
    
    @Override
    public Object getValueAt(int row) {
        Object o =  m_tableModel.getValueAt(row, m_modelCol);
        if (o instanceof LazyData) {
            o = ((LazyData) o).getData();
        }
        return o;
    }

    @Override
    public int getRowCount() {
        return m_tableModel.getRowCount();
    }

    @Override
    public void setValuetAt(int row, Object o) {
        throw Py.TypeError("Tried to modify constant col");
    }

        
    public String getColumnName() {
        if ((m_columnName == null) || (m_columnName.isEmpty())) {
           return m_tableModel.getColumnName(m_modelCol); 
        }
        return m_columnName;
    }
    


}
