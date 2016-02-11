package fr.proline.studio.python.data;

import fr.proline.studio.python.util.Conversion;
import fr.proline.studio.table.CompoundTableModel;
import fr.proline.studio.table.GlobalTableModelInterface;
import fr.proline.studio.table.LazyData;
import java.util.ArrayList;
import org.python.core.Py;

/**
 *
 * @author JM235353
 */
public class ColRef extends Col {
    
    private final int m_modelCol;
    private final GlobalTableModelInterface m_tableModel;
    
    public ColRef(Table table, int col, CompoundTableModel model) {
        super(table);
        m_modelCol = col;
        m_tableModel = model.getLastNonFilterModel();
    }
    
    public ColRef(Table table, int col, GlobalTableModelInterface model) {
        super(table);
        m_modelCol = col;
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
        return new ColData(m_table, resultArray, getColumnName());
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

        
    @Override
    public String getColumnName() {
        if ((m_columnName == null) || (m_columnName.isEmpty())) {
           return m_tableModel.getColumnName(m_modelCol); 
        }
        return m_columnName;
    }
    
    @Override
    public String getExportColumnName() {
        if ((m_columnName == null) || (m_columnName.isEmpty())) {
           return m_tableModel.getExportColumnName(m_modelCol); 
        }
        return m_columnName;
    }


}
