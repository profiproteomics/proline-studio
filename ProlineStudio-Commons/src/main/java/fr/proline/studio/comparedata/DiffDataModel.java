package fr.proline.studio.comparedata;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author JM235353
 */
public class DiffDataModel extends AbstractJoinDataModel {

    private ArrayList<Integer> m_allColumns1 = new ArrayList<>();
    private ArrayList<Integer> m_allColumns2 = new ArrayList<>();

    
    @Override
    protected void setColumns() {
        
        // construct a map of the columns in data2
        HashMap<String, Integer> mapColumn2 = new HashMap<>();
        int nbColumn = m_data2.getColumnCount();
        for (int i = 0; i < nbColumn; i++) {
            if (i == m_selectedKey2) {
                continue;
            }
            mapColumn2.put(m_data2.getDataColumnIdentifier(i), i);
        }
        
        // find corresponding columns in data1 and data2
        nbColumn = m_data1.getColumnCount();
        for (int i = 0; i < nbColumn; i++) {
            if (i == m_selectedKey1) {
                continue;
            }
            String columnIdentifier = m_data1.getDataColumnIdentifier(i);
            if (!mapColumn2.containsKey(columnIdentifier)) {
                continue;
            }
            
            int col2 = mapColumn2.get(columnIdentifier);
            
            // check classes
            Class c1 = m_data1.getDataColumnClass(i);
            Class c2 = m_data2.getDataColumnClass(col2);
            if (!c1.equals(c2)) {
                continue;
            }
            
            m_allColumns1.add(i);
            m_allColumns2.add(col2);
        }

    }



    @Override
    public int getColumnCount() {
        return 1+m_allColumns1.size();
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
        return m_data1.getDataColumnIdentifier(m_allColumns1.get(columnIndex));
  
    }

    @Override
    public Class getDataColumnClass(int columnIndex) {
        if (!joinPossible()) {
            return null;
        }
        
        Class c = null;
        if (columnIndex == 0) {
            c = m_data1.getDataColumnClass(m_selectedKey1);
        } else {
            columnIndex--;
            if (columnIndex < m_allColumns1.size()) {
                c = m_data1.getDataColumnClass(m_allColumns1.get(columnIndex));
            } else {
                columnIndex -= m_allColumns1.size();
                c = m_data2.getDataColumnClass(m_allColumns2.get(columnIndex));
            }
        }
        
        if ((c.equals(Double.class)) || (c.equals(Float.class)) || (c.equals(Long.class)) || (c.equals(Integer.class))) {
            return c;
        }
        return String.class;
        
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
        
        Object value1 = null;
        Integer row = m_keyToRow1.get(key);
        if (row != null) {
            value1 = m_data1.getDataValueAt(row, m_allColumns1.get(columnIndex));
        }
        Object value2 = null;
        row = m_keyToRow2.get(key);
        if (row != null) {
            value2 = m_data2.getDataValueAt(row, m_allColumns2.get(columnIndex));
        }
        
        if ((value1 == null) || (value2 == null)) {
            return null;
        }
        
        if (value1 instanceof Double) {
            return new Double(((Double) value1)-((Double) value2));
        }
        if (value1 instanceof Float) {
            return new Float(((Float) value1)-((Float) value2));
        }
        if (value1 instanceof Long) {
            return new Long(((Long) value1)-((Long) value2));
        }
        if (value1 instanceof Integer) {
            return new Integer(((Integer) value1)-((Integer) value2));
        }

        String value1String = value1.toString();
        String value2String = value2.toString();
        if (value1String.compareTo(value2String) == 0) {
            return "";
        } else {
            return "<>";
        }
        
        
    }

    @Override
    public int getInfoColumn() {
        return 0;
    }

    
}
