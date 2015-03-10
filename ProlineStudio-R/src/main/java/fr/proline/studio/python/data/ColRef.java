package fr.proline.studio.python.data;

import fr.proline.studio.table.CompoundTableModel;
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
    public Object getValueAt(int row) {
        return m_tableModel.getValueAt(row, m_modelCol);
    }

    @Override
    public int getRowCount() {
        return m_tableModel.getRowCount();
    }

}
