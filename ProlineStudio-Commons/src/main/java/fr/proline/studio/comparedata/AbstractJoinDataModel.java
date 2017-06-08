package fr.proline.studio.comparedata;

import fr.proline.studio.table.GlobalTableModelInterface;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
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

    private boolean m_key1FloatOrDouble = false;
    private boolean m_key2FloatOrDouble = false;
    protected int m_selectedTable1Key1 = -1;
    protected int m_selectedTable2Key1 = -1;
    protected int m_selectedTable1Key2 = -1;
    protected int m_selectedTable2Key2 = -1;
    protected double m_tolerance1 = 0;
    protected double m_tolerance2 = 0;
    protected boolean m_showSourceColumn;

    
    protected ArrayList<Integer> m_rowsInTable1 = new ArrayList<>();
    protected ArrayList<Integer> m_rowsInTable2 = new ArrayList<>();

    
    protected abstract void setColumns();
    
    public void setData(GlobalTableModelInterface data1, GlobalTableModelInterface data2) {
        m_data1 = data1;
        m_data2 = data2;
        
        m_showSourceColumn = false;

        selectKeys();

        if (joinPossible()) {
            join();
        }
    }
    
    public void setData(GlobalTableModelInterface data1, GlobalTableModelInterface data2, Integer table1Key1, Integer table2Key1 , Double tolerance1, Integer table1Key2, Integer table2Key2 , Double tolerance2, Boolean showSourceColumn) {
        m_data1 = data1;
        m_data2 = data2;
        m_selectedTable1Key1 = table1Key1;
        m_selectedTable2Key1 = table2Key1; 
        m_tolerance1 = tolerance1;
        m_selectedTable1Key2 = table1Key2;
        m_selectedTable2Key2 = table2Key2;
        m_tolerance2 = tolerance2;
        m_showSourceColumn = showSourceColumn;

        
        Class c = m_data1.getDataColumnClass(m_selectedTable1Key1);
        m_key1FloatOrDouble = (c.equals(Double.class) || c.equals(Float.class));
        
        if (m_selectedTable1Key2 != -1) {
            c = m_data1.getDataColumnClass(m_selectedTable1Key2);
            m_key2FloatOrDouble = (c.equals(Double.class) || c.equals(Float.class));
        }
        
        if (joinPossible()) {
            join();
        }
    }

    public GlobalTableModelInterface getFirstTableModel(){
        return m_data1;
    }
    
    public GlobalTableModelInterface getSecondTableModel(){
        return m_data2;
    }
    
    public Integer getRowInFirstModel(int row) {
        return m_rowsInTable1.get(row);
    }
    public Integer getRowInSecondModel(int row) {
        return m_rowsInTable2.get(row);
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
                        m_selectedTable1Key1 = col1;
                        m_selectedTable2Key1 = col2;
                        return;
                    }
                    if (m_selectedTable1Key1 == -1) {
                        // match with a different key name
                        m_selectedTable1Key1 = col1;
                        m_selectedTable2Key1 = col2;
                    }
                }
            }
        }
        
        m_selectedTable1Key2 = -1;
        m_selectedTable2Key2 = -1;

    }
    
    
    protected void join() {
        if (!joinPossible()) {
            return;
        }

        // Prepare join structures for One Key
        int nb = m_data1.getRowCount();
        ArrayList<Object> table1Key1 = new ArrayList<>(nb);
        ArrayList<Integer> rowsInTable1Key1 = new ArrayList<>(nb);
        for (int i = 0; i < nb; i++) {
            table1Key1.add(m_data1.getDataValueAt(i, m_selectedTable1Key1));
            rowsInTable1Key1.add(i);
        }
        
        nb = m_data2.getRowCount();
        ArrayList<Object> table2Key1 = new ArrayList<>(nb);
        ArrayList<Integer> rowsInTable2Key1 = new ArrayList<>(nb);
        for (int i = 0; i < nb; i++) {
            table2Key1.add(m_data2.getDataValueAt(i, m_selectedTable2Key1));
            rowsInTable2Key1.add(i);
        }
        
        JoinKeyStructure joinKeyStructure = (m_key1FloatOrDouble) ? joinKeysDouble(table1Key1, rowsInTable1Key1, table2Key1, rowsInTable2Key1, m_tolerance1) : joinKeysNew(table1Key1, rowsInTable1Key1, table2Key1, rowsInTable2Key1);
        
        if (m_selectedTable1Key2 != -1) {
            // second key
            ArrayList allKeys = joinKeyStructure.m_allKeys;
            nb = allKeys.size();
            for (int i=0;i<nb;i++) {
                Object key = allKeys.get(i);
                CorrespondingKeyValues correspondingKeysValues = joinKeyStructure.m_correspondingKeys.get(key);
                
                int nbSub = correspondingKeysValues.m_correspondingKeys1.size();
                ArrayList<Object> table1Key2 = new ArrayList<>(nb);
                ArrayList<Integer> rowsInTable1Key2 = new ArrayList<>(nb);
                for (int j = 0; j < nbSub; j++) {
                    Integer rowCur = correspondingKeysValues.m_correspondingKeys1.get(j);
                    rowsInTable1Key2.add(rowCur);
                    table1Key2.add(m_data1.getDataValueAt(rowCur, m_selectedTable1Key2));
                }
                
                nbSub = correspondingKeysValues.m_correspondingKeys2.size();
                ArrayList<Object> table2Key2 = new ArrayList<>(nb);
                ArrayList<Integer> rowsInTable2Key2 = new ArrayList<>(nb);
                for (int j = 0; j < nbSub; j++) {
                    Integer rowCur = correspondingKeysValues.m_correspondingKeys2.get(j);
                    rowsInTable2Key2.add(rowCur);
                    table2Key2.add(m_data2.getDataValueAt(rowCur, m_selectedTable2Key2));
                }
                
                
                JoinKeyStructure joinKeyStructureSub = (m_key2FloatOrDouble) ? joinKeysDouble(table1Key2, rowsInTable1Key2, table2Key2, rowsInTable2Key2, m_tolerance2) : joinKeysNew(table1Key2, rowsInTable1Key2, table2Key2, rowsInTable2Key2);

                // create combined key !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                ArrayList allKeysSub = joinKeyStructureSub.m_allKeys;
                nbSub = allKeysSub.size();
                for (int j=0;j<nbSub;j++) {
                    Object keySub = allKeysSub.get(j);
                    CorrespondingKeyValues correspondingKeysValuesSub = joinKeyStructureSub.m_correspondingKeys.get(keySub);
                    int size1 = correspondingKeysValuesSub.m_correspondingKeys1.size();
                    int size2 = correspondingKeysValuesSub.m_correspondingKeys2.size();
                    if (size1 == 0) {
                        for (int k=0;k<size2;k++) {
                                m_rowsInTable1.add(null);
                                m_rowsInTable2.add(correspondingKeysValuesSub.m_correspondingKeys2.get(k));
                        }
                    } else if (size2 == 0) {
                        for (int k = 0; k < size1; k++) {
                            m_rowsInTable1.add(correspondingKeysValuesSub.m_correspondingKeys1.get(k));
                            m_rowsInTable2.add(null);
                        }
                    } else {
                        for (int k = 0; k < size1; k++) {
                            for (int m = 0; m < size2; m++) {
                                m_rowsInTable1.add(correspondingKeysValuesSub.m_correspondingKeys1.get(k));
                                m_rowsInTable2.add(correspondingKeysValuesSub.m_correspondingKeys2.get(m));
                            }
                        }
                    }
                }
            }
        } else {
            ArrayList allKeys = joinKeyStructure.m_allKeys;
            int nbSub = allKeys.size();
            for (int j = 0; j < nbSub; j++) {
                Object keySub = allKeys.get(j);
                CorrespondingKeyValues correspondingKeyValues = joinKeyStructure.m_correspondingKeys.get(keySub);
                int size1 = correspondingKeyValues.m_correspondingKeys1.size();
                int size2 = correspondingKeyValues.m_correspondingKeys2.size();
                if (size1 == 0) {
                    for (int k = 0; k < size2; k++) {
                        m_rowsInTable1.add(null);
                        m_rowsInTable2.add(correspondingKeyValues.m_correspondingKeys2.get(k));
                    }
                } else if (size2 == 0) {
                    for (int k = 0; k < size1; k++) {
                        m_rowsInTable1.add(correspondingKeyValues.m_correspondingKeys1.get(k));
                        m_rowsInTable2.add(null);
                    }
                } else {
                    for (int k = 0; k < size1; k++) {
                        for (int m = 0; m < size2; m++) {
                            m_rowsInTable1.add(correspondingKeyValues.m_correspondingKeys1.get(k));
                            m_rowsInTable2.add(correspondingKeyValues.m_correspondingKeys2.get(m));
                        }
                    }
                }
            }
        }
        
        
        setColumns();

    }
    
    private JoinKeyStructure joinKeysNew(ArrayList<Object> tableKey1, ArrayList<Integer> rowsInTableKey1, ArrayList<Object> tableKey2, ArrayList<Integer> rowsInTableKey2) {

        JoinKeyStructure joinKeyStructure = new JoinKeyStructure();
        HashSet keysFound = joinKeyStructure.m_keysFound;
        HashMap<Object, CorrespondingKeyValues> correspondingKeys = joinKeyStructure.m_correspondingKeys;
        ArrayList allKeys = joinKeyStructure.m_allKeys;
        
        int nb = tableKey1.size();
        for (int i=0;i<nb;i++) {
            Object key = tableKey1.get(i);
            CorrespondingKeyValues values = correspondingKeys.get(key);
            if (values == null) {
                values = new CorrespondingKeyValues();
                correspondingKeys.put(key, values);
                keysFound.add(key);
                allKeys.add(key);
                
            }
            values.m_correspondingKeys1.add(rowsInTableKey1.get(i));
        }
        
        nb = tableKey2.size();
        for (int i=0;i<nb;i++) {
            Object key = tableKey2.get(i);
            CorrespondingKeyValues values = correspondingKeys.get(key);
            if (values == null) {
                values = new CorrespondingKeyValues();
                correspondingKeys.put(key, values);
                keysFound.add(key);
                allKeys.add(key);
            }
            values.m_correspondingKeys2.add(rowsInTableKey2.get(i));
        }

        return joinKeyStructure;
    }
    
    private JoinKeyStructure joinKeysDouble(ArrayList<Object> tableKey1, ArrayList<Integer> rowsInTableKey1, ArrayList<Object> tableKey2, ArrayList<Integer> rowsInTableKey2, double tolerance) {
        JoinKeyStructure joinKeyStructure = new JoinKeyStructure();
        HashSet keysFound = joinKeyStructure.m_keysFound;
        HashMap<Object, CorrespondingKeyValues> correspondingKeys = joinKeyStructure.m_correspondingKeys;
        ArrayList allKeys = joinKeyStructure.m_allKeys;

        int size1 = tableKey1.size();

        if (size1 > 0) {
            double[] values1 = new double[size1];
            for (int i = 0; i < size1; i++) {
                Number key = (Number) tableKey1.get(i);
                CorrespondingKeyValues values = correspondingKeys.get(key);
                if (values == null) {
                    values = new CorrespondingKeyValues();
                    correspondingKeys.put(key, values);
                    keysFound.add(key);
                    allKeys.add(key);

                }
                values.m_correspondingKeys1.add(rowsInTableKey1.get(i));
                values1[i] = key.doubleValue();
            }

            // create an index corresponding to sorted values
            ArrayIndexComparator comparator = new ArrayIndexComparator(values1);
            Integer[] indexes = comparator.createIndexArray();
            Arrays.sort(indexes, comparator);

            int size2 = tableKey2.size();
            for (int i = 0; i < size2; i++) {
                try {
                    Number key = (Number) tableKey2.get(i);
                    double value = key.doubleValue();
                    Integer nearestValueIndex = searchNearestValueIndex(value, indexes, values1);
                    double nearestValue = values1[nearestValueIndex];
                    double delta = nearestValue - value;
                    if (delta < 0) {
                        delta = -delta;
                    }

                    Number keyAssorted = key;
                    if (delta < tolerance) {
                        keyAssorted = (Number) tableKey1.get(nearestValueIndex);

                        CorrespondingKeyValues values = correspondingKeys.get(keyAssorted);
                        values.m_correspondingKeys2.add(rowsInTableKey2.get(i));

                    } else {
                        // no correspondance even with tolerance
                        CorrespondingKeyValues values = correspondingKeys.get(key);
                        if (values == null) {
                            values = new CorrespondingKeyValues();
                            correspondingKeys.put(key, values);
                            keysFound.add(key);
                            allKeys.add(key);
                        }
                        values.m_correspondingKeys2.add(rowsInTableKey2.get(i));
                    }
                } catch (Exception e) {
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    e.printStackTrace(pw);
                    String s = sw.toString();
                    System.out.println(s);
                }
            }
        } else {
            int size2 = tableKey2.size();
            for (int i = 0; i < size2; i++) {
                try {
                    Number key = (Number) tableKey2.get(i);

                    // no correspondance even with tolerance
                    CorrespondingKeyValues values = correspondingKeys.get(key);
                    if (values == null) {
                        values = new CorrespondingKeyValues();
                        correspondingKeys.put(key, values);
                        keysFound.add(key);
                        allKeys.add(key);
                    }
                    values.m_correspondingKeys2.add(rowsInTableKey2.get(i));

                } catch (Exception e) {
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    e.printStackTrace(pw);
                    String s = sw.toString();
                    System.out.println(s);
                }
            }
        }
        return joinKeyStructure;
    }

 
    
    private static Integer searchNearestValueIndex(double value, Integer[] indexes, double[] values) {
        int lo = 0;
        int hi = indexes.length - 1;

        Integer lastValueIndex = null;

        while (lo <= hi) {
            int mid = (lo + hi) / 2;
            lastValueIndex = indexes[mid];
            if (value < values[lastValueIndex]) {
                hi = mid - 1;
            } else if (value > values[lastValueIndex]) {
                lo = mid + 1;
            } else {
                return lastValueIndex;
            }
        }
        return lastValueIndex;
    }
    
    
    public int getSelectedKey1() {
        return m_selectedTable1Key1;
    }
    
    public int getSelectedKey2() {
        return m_selectedTable2Key1;
    }
    
    public boolean checkKeys(int key1, int key2) {
        if (key1 == -1 && key2 == -1) {
            return true;
        } else if (key1 == -1 || key2 == -1) {
            return false;
        }
        Class c1 = m_data1.getDataColumnClass(key1);
        Class c2 = m_data2.getDataColumnClass(key2);
        return (c1.equals(c2));
    } 
    
    public void setKeys(int key1, int key2) {
        m_selectedTable1Key1 = key1;
        m_selectedTable2Key1 = key2;
    }
    
    
    public boolean joinPossible() {
        return (m_selectedTable1Key1 != -1);
    }
    
    @Override
    public int getRowCount() {
        if (!joinPossible()) {
            return 0;
        }

        return m_rowsInTable1.size();
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
    public Object getRowValue(Class c, int rowIndex) {
        if (!joinPossible()) {
            return null;
        }

        Object value = null;
        
        Integer row1Index = m_rowsInTable1.get(rowIndex);
        if (row1Index != null) {
            value = m_data1.getRowValue(c, row1Index);
        }
        if (value != null) {
            return value;
        }
        
        Integer row2Index = m_rowsInTable2.get(rowIndex);
        if (row2Index != null) {
            value = m_data2.getRowValue(c, row2Index);
        }
        
        return value;
    }
    
    @Override
    public void addSingleValue(Object v) {
        // should not be called
    }

    @Override
    public Object getSingleValue(Class c) {
        return null; // should not be called
    }

    
    public class ArrayIndexComparator implements Comparator<Integer> {

        private final double[] m_doubleArray;

        public ArrayIndexComparator(double[] array) {
            m_doubleArray = array;
        }

        public Integer[] createIndexArray() {
            Integer[] indexes = new Integer[m_doubleArray.length];
            for (int i = 0; i < m_doubleArray.length; i++) {
                indexes[i] = i;
            }
            return indexes;
        }

        @Override
        public int compare(Integer index1, Integer index2) {
            // Autounbox from Integer to int to use as array indexes
            if (m_doubleArray[index1]< m_doubleArray[index2]) {
                return -1;
            } else if (m_doubleArray[index1]< m_doubleArray[index2]) {
                return 1;
            }
            return 0;
        }
    }
    
    public class CorrespondingKeyValues {
        public final ArrayList<Integer> m_correspondingKeys1;
        public final ArrayList<Integer> m_correspondingKeys2;
        
        public CorrespondingKeyValues() {
            m_correspondingKeys1 = new ArrayList();
            m_correspondingKeys2 = new ArrayList();
        }
    }
    
    public class JoinKeyStructure {
        public HashSet<Object> m_keysFound = new HashSet<>();
        public ArrayList<Object> m_allKeys = new ArrayList<>();
        public HashMap<Object, CorrespondingKeyValues> m_correspondingKeys = new HashMap();

    }
}
