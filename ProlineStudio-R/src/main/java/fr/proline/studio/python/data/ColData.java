package fr.proline.studio.python.data;

import java.util.ArrayList;

/**
 *
 * @author JM235353
 */
public class ColData extends Col {
    
    private final ArrayList<Double> m_data;
    private String m_name;
    
    public ColData(ArrayList<Double> data, String name) {
        m_data = data;
        m_name = name;
    }

    @Override
    public Object getValueAt(int row) {
        return m_data.get(row);
    }

    @Override
    public int getRowCount() {
        return m_data.size();
    }
    
    public String getColumnName() {
        return m_name;
    }
}
