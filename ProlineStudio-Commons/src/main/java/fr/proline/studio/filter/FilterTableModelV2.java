package fr.proline.studio.filter;

import fr.proline.studio.graphics.PlotInformation;
import fr.proline.studio.graphics.PlotType;
import fr.proline.studio.progress.ProgressInterface;
import fr.proline.studio.table.DecoratedTableModel;
import fr.proline.studio.table.DecoratedTableModelInterface;
import fr.proline.studio.table.GlobalTableModelInterface;
import fr.proline.studio.table.LazyData;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

/**
 * Model use to filter data and/or restrain rows of another model
 * @author JM235353
 */
public class FilterTableModelV2 extends DecoratedTableModel implements FilterTableModelInterfaceV2, TableModelListener {

    protected LinkedHashMap<Integer, Filter> m_filters = null;
    protected HashSet<Integer> m_restrainIds = null;
    protected ArrayList<Integer> m_filteredIds = null;
    
    private GlobalTableModelInterface m_tableModelSource = null;
    
    private boolean m_isFiltering = false;
    //private boolean m_filteringAsked = false;
    
    public FilterTableModelV2(GlobalTableModelInterface tableModelSource) {
        setTableModelSource(tableModelSource);
    }
    
    @Override
    public void setTableModelSource(GlobalTableModelInterface tableModelSource) {
        if (m_tableModelSource != null) {
            m_tableModelSource.removeTableModelListener(this);
        }
        m_tableModelSource = tableModelSource;
        m_tableModelSource.addTableModelListener(this);
        initFilters();
    }
    
    @Override
    public GlobalTableModelInterface getTableModelSource() {
        return m_tableModelSource;
    }
    
    @Override
    public LinkedHashMap<Integer, Filter> getFilters() {
        return m_filters;
    }

    @Override
    public Filter getColumnFilter(int col) {

        return m_filters.get(col);
        
    }

    @Override
    public boolean filter(int row) {
        int nbCol = getColumnCount();
        for (int i=0;i<nbCol;i++) {
            if (!filter(row, i)) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public int convertRowToOriginalModel(int row) {
        if (m_filteredIds == null) {
            return row;
        }
        return m_filteredIds.get(row);
    }
    
    @Override
    public int convertOriginalModelToRow(int row) {
        // JPM?.TODO : to be changed
        if (m_filteredIds == null) {
            return row;
        }
        int nb = m_filteredIds.size();
        for (int i=0;i<nb;i++) {
            if (row == m_filteredIds.get(i)) {
                return i;
            }
        }
        return -1;
    }
    
    @Override
    public void restrain(HashSet<Integer> restrainRowSet) {
        m_restrainIds = restrainRowSet;
        filter();
    }
    
    @Override
    public HashSet<Integer> getRestrainRowSet() {
        return m_restrainIds;
    }
    
    @Override
    public boolean hasRestrain() {
        return (m_restrainIds != null) && (!m_restrainIds.isEmpty());
    }

    @Override
    public String getToolTipForHeader(int col) {
        return ((DecoratedTableModelInterface) m_tableModelSource).getToolTipForHeader(col);
    }
    
    @Override
    public String getTootlTipValue(int row, int col) {
        int rowFiltered = row;
        if ((!m_isFiltering) && (m_filteredIds != null)) {
            rowFiltered = m_filteredIds.get(row).intValue();
        }
        return m_tableModelSource.getTootlTipValue(rowFiltered, col);
    }


    @Override
    public int getRowCount() {
        if ((!m_isFiltering) && (m_filteredIds != null)) {
            return m_filteredIds.size();
        }
        return m_tableModelSource.getRowCount();
    }

    @Override
    public int getColumnCount() {
        return m_tableModelSource.getColumnCount();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
         
        int rowFiltered = rowIndex;
        if ((!m_isFiltering) && (m_filteredIds != null)) {
            rowFiltered = m_filteredIds.get(rowIndex).intValue();
        }
        return m_tableModelSource.getValueAt(rowFiltered, columnIndex);
    }
    @Override
    public Class getColumnClass(int columnIndex) {
        return m_tableModelSource.getColumnClass(columnIndex);
    }

    @Override
    public void initFilters() {
        if (m_filters == null) {
            m_filters = new LinkedHashMap<>();
        }
        m_tableModelSource.addFilters(m_filters);
    }

    @Override
    public void filter() {
        try {
        int nbData = ((AbstractTableModel) m_tableModelSource).getRowCount();

        
        m_isFiltering = true;
        try {

            if (m_filteredIds == null) {
                m_filteredIds = new ArrayList<>(nbData);
            } else {
                m_filteredIds.clear();
            }

            for (int i = 0; i < nbData; i++) {
                
                Integer iInteger = i;
                
                if ((m_restrainIds!=null) && (!m_restrainIds.isEmpty()) && (!m_restrainIds.contains(iInteger))) {
                    continue;
                }
                
                if (!filter(i)) {
                    continue;
                }
                m_filteredIds.add(iInteger);
            }

        } finally {
            m_isFiltering = false;
        }
        fireTableDataChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean filter(int row, int col) {
        Filter filter = getColumnFilter(col);
        if ((filter == null) || (!filter.isUsed())) {
            return true;
        }
        
        Object data = getValueAt(row, col);
        data = filter.convertValue(data);

        if (data == null) {
            return true; // should not happen
        }
        
        
        
        switch (filter.getFilterType()) {
            case FILTER_STRING: {
                return ((StringFilter) filter).filter((String)data);
            }
            case FILTER_INTEGER: {
                return ((IntegerFilter) filter).filter((Integer)data);
            }
            case FILTER_DOUBLE: {
                return ((DoubleFilter) filter).filter(((Number)data).doubleValue());
            }
            case FILTER_STRING_DIFF: {
                return ((StringDiffFilter) filter).filter((String)data);
            }
            case FILTER_VALUE: {
                return ((ValueFilter) filter).filter((Integer)data);
            }
    
        }
        
        return true; // should never happen
    }

    @Override
    public boolean isLoaded() {
        return ((ProgressInterface) m_tableModelSource).isLoaded();
    }

    @Override
    public int getLoadingPercentage() {
        return ((ProgressInterface) m_tableModelSource).getLoadingPercentage();
    }

    @Override
    public Long getTaskId() {
        return m_tableModelSource.getTaskId();
    }

    @Override
    public LazyData getLazyData(int row, int col) {
        return m_tableModelSource.getLazyData(row, col);
    }

    @Override
    public void givePriorityTo(Long taskId, int row, int col) {
        m_tableModelSource.givePriorityTo(taskId, row, col);
    }

    @Override
    public void sortingChanged(int col) {
        m_tableModelSource.sortingChanged(col);
    }

    @Override
    public int getSubTaskId(int col) {
        return m_tableModelSource.getSubTaskId(col);
    }

    @Override
    public String getDataColumnIdentifier(int columnIndex) {
        return m_tableModelSource.getDataColumnIdentifier(columnIndex);
    }

    @Override
    public Class getDataColumnClass(int columnIndex) {
        return m_tableModelSource.getDataColumnClass(columnIndex);
    }

    @Override
    public Object getDataValueAt(int rowIndex, int columnIndex) {
        
        int rowFiltered = rowIndex;
        if ((!m_isFiltering) && (m_filteredIds != null)) {
            rowFiltered = m_filteredIds.get(rowIndex).intValue();
        }
        return m_tableModelSource.getDataValueAt(rowFiltered, columnIndex);

    }

    @Override
    public int[] getKeysColumn() {
        return m_tableModelSource.getKeysColumn();
    }

    @Override
    public int getInfoColumn() {
        return m_tableModelSource.getInfoColumn();
    }

    @Override
    public void setName(String name) {
        m_tableModelSource.setName(name);
    }

    @Override
    public String getName() {
        return m_tableModelSource.getName();
    }

    @Override
    public Map<String, Object> getExternalData() {
        return m_tableModelSource.getExternalData();
    }

    @Override
    public PlotInformation getPlotInformation() {
        return m_tableModelSource.getPlotInformation();
    }

    @Override
    public void addFilters(LinkedHashMap<Integer, Filter> filtersMap) {
        m_tableModelSource.addFilters(filtersMap);
    }

    @Override
    public PlotType getBestPlotType() {
        return m_tableModelSource.getBestPlotType();
    }

    @Override
    public int getBestXAxisColIndex(PlotType plotType) {
        return m_tableModelSource.getBestXAxisColIndex(plotType);
    }

    @Override
    public int getBestYAxisColIndex(PlotType plotType) {
        return m_tableModelSource.getBestYAxisColIndex(plotType);
    }

    @Override
    public String getExportRowCell(int row, int col) {
                 
        int rowFiltered = row;
        if ((!m_isFiltering) && (m_filteredIds != null)) {
            rowFiltered = m_filteredIds.get(row).intValue();
        }
        return m_tableModelSource.getExportRowCell(rowFiltered, col);
    }

    @Override
    public String getExportColumnName(int col) {
        return m_tableModelSource.getExportColumnName(col);
    }

    @Override
    public void tableChanged(TableModelEvent e) {
        filter();
    }
    
    
    @Override
    public String getColumnName(int column) {
        return m_tableModelSource.getColumnName(column);
    }

    @Override
    public TableCellRenderer getRenderer(int col) {
        return m_tableModelSource.getRenderer(col);
    }

    @Override
    public long row2UniqueId(int rowIndex) {
        rowIndex = convertRowToOriginalModel(rowIndex);
        return m_tableModelSource.row2UniqueId(rowIndex);
    }
    
    @Override
    public int uniqueId2Row(long id) {
        int rowSource =  m_tableModelSource.uniqueId2Row(id);
        return convertOriginalModelToRow(rowSource);
    }
}
