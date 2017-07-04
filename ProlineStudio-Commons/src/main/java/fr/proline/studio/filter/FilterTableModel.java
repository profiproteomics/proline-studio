package fr.proline.studio.filter;

import fr.proline.studio.comparedata.ExtraDataType;
import fr.proline.studio.export.ExportFontData;
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
import org.jdesktop.swingx.JXTable;

/**
 * Model use to filter data and/or restrain rows of another model
 * @author JM235353
 */
public class FilterTableModel extends DecoratedTableModel implements FilterTableModelInterface, TableModelListener {

    protected LinkedHashMap<Integer, Filter> m_filters = null;
    protected HashSet<Integer> m_restrainIds = null;
    protected ArrayList<Integer> m_filteredIds = null;
    
    private ArrayList<Integer> m_searchIds = null;
    private int m_searchIndex = 0;
    
    protected GlobalTableModelInterface m_tableModelSource = null;
    
    private boolean m_isFiltering = false;

    private boolean m_valueBeingSet = false;
    
    public FilterTableModel(GlobalTableModelInterface tableModelSource) {
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
    
    protected void setFilters(LinkedHashMap<Integer, Filter> filters) {
        m_filters = filters;
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
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        int rowFiltered = rowIndex;
        if ((!m_isFiltering) && (m_filteredIds != null)) {
            rowFiltered = m_filteredIds.get(rowIndex).intValue();
        }
        return m_tableModelSource.isCellEditable(rowFiltered, columnIndex);
    }
    
    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        m_valueBeingSet = true;
        try {
            int rowFiltered = rowIndex;
            if ((!m_isFiltering) && (m_filteredIds != null)) {
                rowFiltered = m_filteredIds.get(rowIndex).intValue();
            }
            m_tableModelSource.setValueAt(aValue, rowFiltered, columnIndex);
        } finally {
            m_valueBeingSet = false;
        }
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
    public boolean filter() {
        
        // reinit search
        m_searchIds = null;
        
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

                    if ((m_restrainIds != null) && (!m_restrainIds.isEmpty()) && (!m_restrainIds.contains(iInteger))) {
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
            //e.printStackTrace();
        }
        
        return true;
    }

    @Override
    public boolean filter(int row, int col) {
        Filter filter = getColumnFilter(col);
        if ((filter == null) || (!filter.isUsed())) {
            return true;
        }
        
        int extraCol = filter.getExtraModelColumn();
        
        return filter(filter, row, col, extraCol);
    }
    private boolean filter(Filter filter, int row, int col1, int col2) {
        Object data1 = getValueAt(row, col1);
        data1 = filter.convertValue(data1);

        if (data1 == null) {
            return false;
        }
        
        Object data2 = null;
        if (col2 != -1) {
            data2 = getValueAt(row, col2);

        }

        return filter.filter(data1, data2);

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
        m_searchIds = null;
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
    public ArrayList<ExportFontData> getExportFonts(int row, int col) {
                 
        int rowFiltered = row;
        if ((!m_isFiltering) && (m_filteredIds != null)) {
            rowFiltered = m_filteredIds.get(row).intValue();
        }
        return m_tableModelSource.getExportFonts(rowFiltered, col);
    }

    @Override
    public String getExportColumnName(int col) {
        return m_tableModelSource.getExportColumnName(col);
    }

    @Override
    public void tableChanged(TableModelEvent e) {
        if (m_valueBeingSet) {
            return;
        }
        
        // cancel restrain and redo filter
        restrain(null);
    }
    
    
    @Override
    public String getColumnName(int column) {
        return m_tableModelSource.getColumnName(column);
    }

    @Override
    public TableCellRenderer getRenderer(int row, int col) {
        int rowFiltered = row;
        if ((!m_isFiltering) && (m_filteredIds != null)) {
            rowFiltered = m_filteredIds.get(row).intValue();
        }
        return m_tableModelSource.getRenderer(rowFiltered, col);
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

    @Override
    public GlobalTableModelInterface getFrozzenModel() {
        return new LockedFilterTableModel(m_restrainIds, m_filteredIds, m_tableModelSource);
    }
    
    @Override
    public int search(JXTable table, Filter filter, boolean newSearch) {
        
        if (m_searchIds == null) {
            m_searchIds = new ArrayList<>();
            newSearch = true;
        }
            
        if (newSearch) {
            m_searchIndex = 0;
            m_searchIds.clear();
            int nb = getRowCount();
            for (int row = 0; row < nb; row++) {
                
                int searchRow;
                if (table != null) {
                    // we search according to table
                    searchRow = table.convertRowIndexToModel(row);
                } else {
                    searchRow = row;
                }
                
                int col = filter.getModelColumn();
                boolean found = filter(filter, searchRow, col, -1);
                if (found) {
                    m_searchIds.add(searchRow);
                }
            }
        } else {
            m_searchIndex++;
            if (m_searchIndex>=m_searchIds.size()) {
                m_searchIndex = 0;
            }
        }
        
        if (m_searchIds.isEmpty()) {
            return -1;
        }
        return m_searchIds.get(m_searchIndex);

    }
    
    @Override
    public ArrayList<ExtraDataType> getExtraDataTypes() {
        return m_tableModelSource.getExtraDataTypes();
    }

    @Override
    public Object getValue(Class c) {
        return m_tableModelSource.getValue(c);
    }

    @Override
    public Object getRowValue(Class c, int rowIndex) {

        int rowFiltered = rowIndex;
        if ((!m_isFiltering) && (m_filteredIds != null)) {
            rowFiltered = m_filteredIds.get(rowIndex).intValue();
        }
        return m_tableModelSource.getRowValue(c, rowFiltered);
    }
    
    @Override
    public Object getColValue(Class c, int col) {
        return m_tableModelSource.getColValue(c, col);
    }
    
    @Override
    public void addSingleValue(Object v) {
        m_tableModelSource.addSingleValue(v);
    }

    @Override
    public Object getSingleValue(Class c) {
        return m_tableModelSource.getSingleValue(c);
    }
}
