package fr.proline.studio.comparedata;

import fr.proline.studio.filter.DoubleFilter;
import fr.proline.studio.filter.Filter;
import fr.proline.studio.filter.IntegerFilter;
import fr.proline.studio.filter.LongFilter;
import fr.proline.studio.filter.StringFilter;
import fr.proline.studio.graphics.PlotInformation;
import fr.proline.studio.graphics.PlotType;
import fr.proline.studio.table.GlobalTableModelInterface;
import fr.proline.studio.table.LazyData;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.table.TableCellRenderer;

/**
 * Model to perform a join between two models (joined thanks to key columns)
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
        
        return m_allColumns1.size()+m_allColumns2.size()+1;
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
    public String getColumnName(int column) {
        return getDataColumnIdentifier(column);
    }
    
    @Override
    public int getInfoColumn() {
        return 0;
    }

    @Override
    public Map<String, Object> getExternalData() {
        return null;
    }

    @Override
    public PlotInformation getPlotInformation() {
        return null;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (!joinPossible()) {
            return null;
        }

        Object key = m_allKeys.get(rowIndex);
        if (columnIndex == 0) {
            Integer row = m_keyToRow1.get(key);
            if (row == null) {
                return null;
            }
            return m_data1.getValueAt(row, m_selectedKey1);
        }
        columnIndex--;
        if (columnIndex < m_allColumns1.size()) {
            Integer row = m_keyToRow1.get(key);
            if (row == null) {
                return null;
            }
            return m_data1.getValueAt(row, m_allColumns1.get(columnIndex));
        }
        columnIndex -= m_allColumns1.size();
        if (columnIndex < m_allColumns2.size()) {
            Integer row = m_keyToRow2.get(key);
            if (row == null) {
                return null;
            }
            return m_data2.getValueAt(row, m_allColumns2.get(columnIndex));
        }
        return null;
    }

    @Override
    public Class getColumnClass(int columnIndex) {
        if (!joinPossible()) {
            return null;
        }

        if (columnIndex == 0) { 
            return m_data1.getColumnClass(m_selectedKey1);
        }
        columnIndex--;
        if (columnIndex < m_allColumns1.size()) {
            return m_data1.getColumnClass(m_allColumns1.get(columnIndex));
        }
        columnIndex -= m_allColumns1.size();
        if (columnIndex < m_allColumns2.size()) {
            return m_data2.getColumnClass(m_allColumns2.get(columnIndex));
        }
        return null;
    }
    
    @Override
    public Long getTaskId() {
        return -1l; // not used
    }

    @Override
    public LazyData getLazyData(int row, int col) {
        return null; // not used
    }

    @Override
    public void givePriorityTo(Long taskId, int row, int col) {
        // not used
    }

    @Override
    public void sortingChanged(int col) {
        return; // not used
    }

    @Override
    public int getSubTaskId(int col) {
        return -1; // not used
    }

    @Override
    public String getToolTipForHeader(int col) {
        return getColumnName(col);
    }

    @Override
    public String getTootlTipValue(int row, int col) {
        return null; // not used
    }

    @Override
    public void addFilters(LinkedHashMap<Integer, Filter> filtersMap) {
        if (!joinPossible()) {
            return;
        }
        
        LinkedHashMap<Integer, Filter> filtersMap1 = new LinkedHashMap<>();
        m_data1.addFilters(filtersMap1);
        
        LinkedHashMap<Integer, Filter> filtersMap2 = new LinkedHashMap<>();
        m_data2.addFilters(filtersMap2);
        
        
        int nbColumns = getColumnCount();
        for (int i=0;i<nbColumns;i++) {
            if (i==0) {
                Class c = getDataColumnClass(0);
                if (c.equals(Double.class)) {
                    filtersMap.put(i, new DoubleFilter(getColumnName(i), null, i));
                } else if (c.equals(Integer.class)) {
                    filtersMap.put(i, new IntegerFilter(getColumnName(i), null, i));
                } else if (c.equals(String.class)) {
                    filtersMap.put(i, new StringFilter(getColumnName(i), null, i));
                } else if (c.equals(Long.class)) {
                    filtersMap.put(i, new LongFilter(getColumnName(i), null, i));
                }
            } else {
                int colIndex = i-1;
                if (colIndex < m_allColumns1.size()) {
                    Filter f = filtersMap1.get(m_allColumns1.get(colIndex));
                    if (f != null) {
                        filtersMap.put(i, f);
                    }
                } else {
                    colIndex -= m_allColumns1.size();
                    if (colIndex < m_allColumns2.size()) {
                        Filter f = filtersMap2.get(m_allColumns2.get(colIndex));
                        if (f != null) {
                            filtersMap.put(i, f);
                        }
                    }
                }
            }
        }
    }



    @Override
    public PlotType getBestPlotType() {
         return null; //JPM.TODO
    }

    @Override
    public int getBestXAxisColIndex(PlotType plotType) {
         return -1; //JPM.TODO
    }

    @Override
    public int getBestYAxisColIndex(PlotType plotType) {
        return -1; //JPM.TODO
    }

    @Override
    public String getExportRowCell(int row, int col) {
        Object o = getValueAt(row, col);
        if (o == null) {
            return "";
        }
        return o.toString();
    }

    @Override
    public String getExportColumnName(int col) {
        return getColumnName(col);
    }

    @Override
    public TableCellRenderer getRenderer(int col) {
         if (!joinPossible()) {
            return null;
        }
        
         if (col == 0) {
             return m_data1.getRenderer(m_selectedKey1);
         }

        col--;
        if (col<m_allColumns1.size()) {

            return m_data1.getRenderer(m_allColumns1.get(col));
        }
        col-=m_allColumns1.size();
        if (col<m_allColumns2.size()) {
            return m_data2.getRenderer(m_allColumns2.get(col));
        }
        return null;
    }

    @Override
    public GlobalTableModelInterface getFrozzenModel() {
        return this;
    }
    
    
}
