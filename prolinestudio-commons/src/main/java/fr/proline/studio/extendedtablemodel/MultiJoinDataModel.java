package fr.proline.studio.extendedtablemodel;

import fr.proline.studio.export.ExportFontData;
import fr.proline.studio.extendedtablemodel.JoinDataModel.JoinTAG;
import fr.proline.studio.filter.Filter;
import fr.proline.studio.graphics.PlotDataSpec;
import fr.proline.studio.graphics.PlotInformation;
import fr.proline.studio.graphics.PlotType;
import fr.proline.studio.table.LazyData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author JM235353
 */
public class MultiJoinDataModel extends AbstractTableModel implements GlobalTableModelInterface  {

    protected String m_name;
    
    private ArrayList<GlobalTableModelInterface> m_data = null;
    private ArrayList<Integer> m_tableXKey1 = null;
    private Double m_tolerance1 = null;
    private ArrayList<Integer> m_tableXKey2 = null;
    private Double m_tolerance2 = null;
    private Boolean m_showSourceColumn;
 
    
    private JoinDataModel m_modelFinal = null;

    
    private int m_lastSourceOrKeyIndex = -1;
    private int m_numberOfSourcesOrKeys = -1;
    ArrayList<JoinTAG> m_keyOrSourcetags = new ArrayList<>();
    
    public void setData(ArrayList<GlobalTableModelInterface> data, ArrayList<Integer> tableXKey1, Double tolerance1, ArrayList<Integer>  tableXKey2, Double tolerance2, Boolean showSourceColumn) {
        m_tableXKey1 = tableXKey1;
        m_data = data;
        m_tableXKey1 = tableXKey1;
        m_tolerance1 = tolerance1; 
        m_tableXKey2 = tableXKey2;
        m_tolerance2 = tolerance2; 
        m_showSourceColumn = showSourceColumn;

        int nbTables = m_data.size();
        
        m_modelFinal = new JoinDataModel();
        m_modelFinal.setName("");
        if (tableXKey1 == null) {
            m_modelFinal.setData(data.get(0), data.get(1));
        } else {
            m_modelFinal.setData(data.get(0), data.get(1), tableXKey1.get(0), tableXKey1.get(1), m_tolerance1, tableXKey2.get(0), tableXKey2.get(1), m_tolerance2, m_showSourceColumn);
        }

        // create a tree of JoinDataModel to join all the tables : a JoinDataModel contains a JoinDataModel and a Data Model
        for (int i=2;i<nbTables;i++) {
            JoinDataModel model2 = new JoinDataModel();
            model2.setName("");
            if (tableXKey1 == null) {
                model2.setData(m_modelFinal, data.get(i));
            } else {
                model2.setData(m_modelFinal, data.get(i), 0, tableXKey1.get(i), m_tolerance1, tableXKey2.get(i) != -1 ? 1 : -1, tableXKey2.get(i), m_tolerance2, m_showSourceColumn);
            }
            m_modelFinal = model2;
        }
        

        // if there are more than two tables joined, we need
        // to retrieve all keys and source columns created from all the JoinDataModel
        // in the tree of JoinDataModels
        int nbColumns = getColumnCount();
        m_keyOrSourcetags.clear();
        m_lastSourceOrKeyIndex = -1;
        for (int i=0;i<nbColumns;i++) {
            JoinTAG tag = m_modelFinal.getKeyIndex(i, 0);
            if (tag != null) {
                m_lastSourceOrKeyIndex = i;
                tag.setColumnIndex(i);
                m_keyOrSourcetags.add(tag);
            }
        }
        
        // sort the columns which contain a key or a source column
        Collections.sort(m_keyOrSourcetags);
                
        m_numberOfSourcesOrKeys = m_keyOrSourcetags.size();
    }
    

    public static int[] selectKeys(ArrayList<GlobalTableModelInterface> data) {
        
        int nbTables = data.size();
        
        int[] selectedKeys = new int[nbTables];
        
        for (int i=0;i<nbTables;i++) {
            selectedKeys[i] = -1;
        }
        
        ArrayList<int[]> keysXArray = new ArrayList();
        for (int i=0;i<nbTables;i++) {
            int[] keys = data.get(i).getKeysColumn();
            if (keys == null) {
                return selectedKeys;
            }
            keysXArray.add(keys);
        }
        
        // find the best solution for the first two tables
        
        // try to find a corresponding key with the same name and type
        int[] keys1 = data.get(0).getKeysColumn();
        int[] keys2 = data.get(1).getKeysColumn();

        int selectedTableKey0 = -1;
        int selectedTableKey1 = -1;
        
        end:
        for (int i = 0; i < keys1.length; i++) {
            int col0 = keys1[i];
            Class c0 = data.get(0).getDataColumnClass(col0);
            String id0 = data.get(0).getDataColumnIdentifier(col0);
            for (int j = 0; j < keys2.length; j++) {
                int col1 = keys2[j];
                Class c1 = data.get(1).getDataColumnClass(col1);
                String id1 = data.get(1).getDataColumnIdentifier(col1);
                if (!c0.equals(c1)) {
                    continue;
                }
                if (id0.compareTo(id1) == 0) {
                    // perfect match, we have found the keys
                    selectedTableKey0 = col0;
                    selectedTableKey1 = col1;
                    break end;
                }
                if (selectedTableKey0 == -1) {
                    // match with a different key name
                    selectedTableKey0 = col0;
                    selectedTableKey1 = col1;
                }
            }
        }
        
        if (selectedTableKey0 == -1) {
            return selectedKeys;
        }
        
        selectedKeys[0] = selectedTableKey0;
        selectedKeys[1] = selectedTableKey1;

        
        // find the corresponding solution for the next tables
        
        int col0 = selectedKeys[0];
        Class c0 = data.get(0).getDataColumnClass(col0);
        String id0 = data.get(0).getDataColumnIdentifier(col0);
        for (int i = 2; i < nbTables; i++) {

            int[] keys = data.get(i).getKeysColumn();
            
            int selectedTableKey = -1;
            for (int j = 0; j < keys2.length; j++) {
                int col = keys2[j];
                Class c = data.get(1).getDataColumnClass(col);
                String id = data.get(1).getDataColumnIdentifier(col);
                if (!c0.equals(c)) {
                    continue;
                }
                if (id0.compareTo(id) == 0) {
                    // perfect match, we have found the keys
                    selectedTableKey = col;
                    break;
                }
                if (selectedTableKey == -1) {
                    // match with a different key name
                    selectedTableKey = col;
                }
            }
            selectedKeys[i] = selectedTableKey;
        }

        
        return selectedKeys;
    }

    

    public int getSelectedKey1() {
        return m_modelFinal.getSelectedKey1();
    }

    private int convertColIndex(int col) {
        
        if (col<m_numberOfSourcesOrKeys) {
            return m_keyOrSourcetags.get(col).getColumnIndex();
        }
        return col+(1+m_lastSourceOrKeyIndex-m_numberOfSourcesOrKeys);


    }
    
    @Override
    public int getRowCount() {
        return m_modelFinal.getRowCount();
    }

    @Override
    public int getColumnCount() {

        return m_modelFinal.getColumnCount()-(1+m_lastSourceOrKeyIndex-m_numberOfSourcesOrKeys);


    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return m_modelFinal.getValueAt(rowIndex, convertColIndex(columnIndex));
    }
    
    @Override
    public Class getColumnClass(int columnIndex) {
        return m_modelFinal.getColumnClass(convertColIndex(columnIndex));
    }

    @Override
    public String getColumnName(int column) {
        return m_modelFinal.getColumnName(convertColIndex(column));
    }

    @Override
    public GlobalTableModelInterface getFrozzenModel() {
        return this;
    }

    @Override
    public Long getTaskId() {
        return -1L;
    }

    @Override
    public LazyData getLazyData(int row, int col) {
        return m_modelFinal.getLazyData(row, convertColIndex(col));
    }

    @Override
    public void givePriorityTo(Long taskId, int row, int col) {
    }

    @Override
    public void sortingChanged(int col) {
    }

    @Override
    public int getSubTaskId(int col) {
         return -1; // not used
    }

    @Override
    public String getToolTipForHeader(int col) {
        return getColumnName(convertColIndex(col));
    }

    @Override
    public String getTootlTipValue(int row, int col) {
        return null; // not used
    }

    @Override
    public TableCellRenderer getRenderer(int row, int col) {
        return m_modelFinal.getRenderer(row, convertColIndex(col));
    }

    @Override
    public String getDataColumnIdentifier(int columnIndex) {
        return m_modelFinal.getDataColumnIdentifier(convertColIndex(columnIndex));
    }

    @Override
    public Class getDataColumnClass(int columnIndex) {
        return m_modelFinal.getDataColumnClass(convertColIndex(columnIndex));
    }

    @Override
    public Object getDataValueAt(int rowIndex, int columnIndex) {
        return m_modelFinal.getDataValueAt(rowIndex, convertColIndex(columnIndex));
    }

    @Override
    public int[] getKeysColumn() {
        final int[] keysColumn = {0};
        return keysColumn;
    }

    @Override
    public int getInfoColumn() {
         return 0;
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
    public Map<String, Object> getExternalData() {
        return m_modelFinal.getExternalData();
    }

    @Override
    public PlotInformation getPlotInformation() {
        return m_modelFinal.getPlotInformation();
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
        return m_modelFinal.getExtraDataTypes();
    }

    @Override
    public Object getValue(Class c) {

        return m_modelFinal.getValue(c);  
    }

    @Override
    public Object getRowValue(Class c, int row) {

        return m_modelFinal.getRowValue(c, row);
    }

    @Override
    public Object getColValue(Class c, int col) {
        return m_modelFinal.getColValue(c, convertColIndex(col));
    }

    @Override
    public void addSingleValue(Object v) {
        m_modelFinal.addSingleValue(v);
    }

    @Override
    public Object getSingleValue(Class c) {
       return m_modelFinal.getSingleValue(c);
    }

    @Override
    public void addFilters(LinkedHashMap<Integer, Filter> filtersMap) {
        m_modelFinal.addFilters(filtersMap);
    }

    @Override
    public boolean isLoaded() {
        return m_modelFinal.isLoaded();
    }

    @Override
    public int getLoadingPercentage() {
        return m_modelFinal.getLoadingPercentage();
    }

    @Override
    public PlotType getBestPlotType() {
        return m_modelFinal.getBestPlotType();
    }

    @Override
    public int[] getBestColIndex(PlotType plotType) {
        return m_modelFinal.getBestColIndex(plotType);
    }

    @Override
    public String getExportRowCell(int row, int col) {
        return m_modelFinal.getExportRowCell(row, convertColIndex(col));
    }

    @Override
    public ArrayList<ExportFontData> getExportFonts(int row, int col) {
        return m_modelFinal.getExportFonts(row, convertColIndex(col));
    }

    @Override
    public String getExportColumnName(int col) {
        return m_modelFinal.getExportColumnName(convertColIndex(col));
    }

    @Override
    public PlotDataSpec getDataSpecAt(int i) {
        return null;
    }
    
}
