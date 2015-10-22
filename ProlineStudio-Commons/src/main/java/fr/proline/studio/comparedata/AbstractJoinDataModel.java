package fr.proline.studio.comparedata;

import fr.proline.studio.table.GlobalTableModelInterface;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import javax.swing.table.AbstractTableModel;

/**
 * Abstract Model to do a join/diff between two tables by joining two columns which correspond to a key.
 * @author JM235353
 */
public abstract class AbstractJoinDataModel extends AbstractTableModel implements GlobalTableModelInterface  {
    
    
    
    protected GlobalTableModelInterface m_data1;
    protected GlobalTableModelInterface m_data2;
    
    protected String m_name;

    protected int m_selectedKey1 = -1;
    protected int m_selectedKey2 = -1;
    
        
    protected ArrayList m_allKeys;
    protected HashMap<Object, Integer> m_keyToRow1;
    protected HashMap<Object, Integer> m_keyToRow2;
    
    protected abstract void setColumns();
    
    public void setData(GlobalTableModelInterface data1, GlobalTableModelInterface data2) {
        m_data1 = data1;
        m_data2 = data2;

        selectKeys();

        if (joinPossible()) {
            join();
        }
    }
    
    public void setData(GlobalTableModelInterface data1, GlobalTableModelInterface data2, Integer key1, Integer key2) {
        m_data1 = data1;
        m_data2 = data2;
        m_selectedKey1 = key1;
        m_selectedKey2 = key2;

        if (joinPossible()) {
            join();
        }
    }
    

    
    protected void join() {
        if (!joinPossible()) {
            return;
        }

        // find all different keys
        joinKeys();
        
        setColumns();

    }

    protected void selectKeys() {
        int[] keys1 = m_data1.getKeysColumn();
        int[] keys2 = m_data2.getKeysColumn();

        // try to find a corresponding key with the same name and type
        if ((keys1 != null) && (keys2 != null)) {
            for (int i = 0; i < keys1.length; i++) {
                int col1 = keys1[i];
                Class c1 = m_data1.getDataColumnClass(col1);
                String id1 = m_data1.getDataColumnIdentifier(col1);
                for (int j = 0; j < keys2.length; j++) {
                    int col2 = keys2[i];
                    Class c2 = m_data2.getDataColumnClass(col2);
                    String id2 = m_data2.getDataColumnIdentifier(col2);
                    if (!c1.equals(c2)) {
                        continue;
                    }
                    if (id1.compareTo(id2) == 0) {
                        // perfect match, we have found the keys
                        m_selectedKey1 = col1;
                        m_selectedKey2 = col2;
                        return;
                    }
                    if (m_selectedKey1 == -1) {
                        // match with a different key name
                        m_selectedKey1 = col1;
                        m_selectedKey2 = col2;
                    }
                }
            }
        }

    }
    
    protected void joinKeys() {
        
        // find all different keys
        m_allKeys = new ArrayList();
        HashSet keysFound = new HashSet();
        m_keyToRow1 = new HashMap<>();
        m_keyToRow2 = new HashMap<>();
        
        int nb = m_data1.getRowCount();
        for (int i=0;i<nb;i++) {
            Object key = m_data1.getDataValueAt(i, m_selectedKey1);
            m_keyToRow1.put(key, i);
            if (!keysFound.contains(key)) {
                keysFound.add(key);
            }
            m_allKeys.add(key);
        }
        nb = m_data2.getRowCount();
        for (int i=0;i<nb;i++) {
            Object key = m_data2.getDataValueAt(i, m_selectedKey2);
            m_keyToRow2.put(key, i);
            if (!keysFound.contains(key)) {
                keysFound.add(key);
                m_allKeys.add(key);
            }
        }
    }
    
    public int getSelectedKey1() {
        return m_selectedKey1;
    }
    
    public int getSelectedKey2() {
        return m_selectedKey2;
    }
    
    public boolean checkKeys(int key1, int key2) {
        Class c1 = m_data1.getDataColumnClass(key1);
        Class c2 = m_data2.getDataColumnClass(key2);
        return (c1.equals(c2));
    } 
    
    public void setKeys(int key1, int key2) {
        m_selectedKey1 = key1;
        m_selectedKey2 = key2;
    }
    
    
    public boolean joinPossible() {
        return (m_selectedKey1 != -1);
    }
    
    @Override
    public int getRowCount() {
        if (!joinPossible()) {
            return 0;
        }

        return m_allKeys.size();
    }
    
    @Override
    public int[] getKeysColumn() {
        if (!joinPossible()) {
            return null;
        }

        final int[] keysColumn = {0};
        return keysColumn;
    }
    
    @Override
    public void setName(String name) {
        m_name = name;
    }

    @Override
    public String getName() {
        return m_name;
    }
    
    @Override
    public boolean isLoaded() {
        return m_data1.isLoaded() && m_data2.isLoaded();
    }

    @Override
    public int getLoadingPercentage() {
        return (m_data1.getLoadingPercentage() + m_data2.getLoadingPercentage())/2;
    }
    
    @Override
    public long row2UniqueId(int rowIndex) {
        return rowIndex;
    }
    
    
    @Override
    public int uniqueId2Row(long id) {
        return (int) id;
    }
    
    
    @Override
    public ArrayList<ExtraDataType> getExtraDataTypes() {
        ArrayList<ExtraDataType> list = null;
        
        ArrayList<ExtraDataType> list1 = m_data1.getExtraDataTypes();
        if (list1 != null) {
            list = new ArrayList<>(list1);
        }
        
        ArrayList<ExtraDataType> list2 = m_data1.getExtraDataTypes();
        if (list2 != null) {
            if (list == null) {
                list = new ArrayList<>(list2);
            } else {
                list.addAll(list2);
            }
        }

        return list;
        
    }

    @Override
    public Object getValue(Class c) {
        if (!joinPossible()) {
            return null;
        }

        Object o = m_data1.getValue(c);
        if (o != null) {
            return o;
        }
        
        o = m_data2.getValue(c);
        if (o != null) {
            return o;
        }
        
        return null;
    }

    @Override
    public Object getValue(Class c, int rowIndex) {
        if (!joinPossible()) {
            return null;
        }

        Object key = m_allKeys.get(rowIndex);
        
        Integer row = m_keyToRow1.get(key);
        if (row != null) {
            return m_data1.getValue(c, rowIndex);
        }
        row = m_keyToRow2.get(key);
        if (row != null) {
            return m_data2.getValue(c, rowIndex);
        }
        
        return null;
    }
    
    @Override
    public void addSingleValue(Object v) {
        // should not be called
    }

    @Override
    public Object getSingleValue(Class c) {
        return null; // should not be called
    }

}
