package fr.proline.studio.python.data;

import java.util.ArrayList;

/**
 * Python object corresponding to the column of type Boolean of a Table Model
 * 
 * @author JM235353
 */
public class ColBooleanData extends Col {
    
    private final ArrayList<Boolean> m_data;
    
    public ColBooleanData(Table table, ArrayList<Boolean> data, String name) {
        super(table);
        m_data = data;
        m_columnName = name;
    }

    private ColBooleanData() {
        super(null);
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object getValueAt(int row) {
        return m_data.get(row);
    }
    
    @Override
    public void setValuetAt(int row, Object o) {
        Boolean b = (Boolean) o;
        m_data.set(row, b);

    }

    @Override
    public Col mutable() {
        return this;
    }
    
    @Override
    public int getRowCount() {
        return m_data.size();
    }
    
    @Override
    public Class getColumnClass() {
        return Boolean.class;
    }
    



    
    
}
