package fr.proline.studio.comparedata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * @author JM235353
 */
public class JoinDataModel extends AbstractJoinDataModel {

    private ArrayList<Integer> m_allColumns1;
    private ArrayList<Integer> m_allColumns2;

    
    public JoinDataModel() {
        
    }
    
    @Override
    protected void setColumns() {
        // construct column array
        m_allColumns1 = new ArrayList<>();
        int nbColumn = m_data1.getColumnCount();
        for (int i = 0; i < nbColumn; i++) {
            if (i == m_selectedKey1) {
                continue;
            }
            m_allColumns1.add(i);
        }
        m_allColumns2 = new ArrayList<>();
        nbColumn = m_data2.getColumnCount();
        for (int i = 0; i < nbColumn; i++) {
            if (i == m_selectedKey2) {
                continue;
            }
            m_allColumns2.add(i);
        }
    }
    


    @Override
    public int getColumnCount() {
        if (!joinPossible()) {
            return 0;
        }
        
        return m_allColumns1.size()+m_allColumns1.size()+1;
    }

    @Override
    public Object getDataValueAt(int rowIndex, int columnIndex) {
        if (!joinPossible()) {
            return null;
        }
        
        Object key = m_allKeys.get(rowIndex);
        if (columnIndex == 0) {
            return key;
        }
        columnIndex--;
        if (columnIndex<m_allColumns1.size()) {
            Integer row = m_keyToRow1.get(key);
            if (row == null) {
                return null;
            }
            return m_data1.getDataValueAt(row, m_allColumns1.get(columnIndex));
        }
        columnIndex-=m_allColumns1.size();
        if (columnIndex<m_allColumns2.size()) {
            Integer row = m_keyToRow2.get(key);
            if (row == null) {
                return null;
            }
            return m_data2.getDataValueAt(row, m_allColumns2.get(columnIndex));
        }
        return null;
    }

    @Override
    public String getDataColumnIdentifier(int columnIndex) {
        if (!joinPossible()) {
            return null;
        }
        
        if (columnIndex == 0) {
            String col1 = m_data1.getDataColumnIdentifier(m_selectedKey1);
            String col2 = m_data2.getDataColumnIdentifier(m_selectedKey2);
            if (col1.compareTo(col2) == 0) {
                return col1;
            } else {
                return col1+"/"+col2;
            }
        }
        columnIndex--;
        if (columnIndex < m_allColumns1.size()) {
            return m_data1.getDataColumnIdentifier(m_allColumns1.get(columnIndex));
        }
        columnIndex -= m_allColumns1.size();
        if (columnIndex < m_allColumns2.size()) {
            return m_data2.getDataColumnIdentifier(m_allColumns2.get(columnIndex));
        }
        return null;
    }

    @Override
    public Class getDataColumnClass(int columnIndex) {
        if (!joinPossible()) {
            return null;
        }
        
        if (columnIndex == 0) {
            return m_data1.getDataColumnClass(m_selectedKey1);
        }
        columnIndex--;
        if (columnIndex < m_allColumns1.size()) {
            return m_data1.getDataColumnClass(m_allColumns1.get(columnIndex));
        }
        columnIndex -= m_allColumns1.size();
        if (columnIndex < m_allColumns2.size()) {
            return m_data2.getDataColumnClass(m_allColumns2.get(columnIndex));
        }
        return null;
    }

    @Override
    public int getInfoColumn() {
        return 0;
    }
    
}
