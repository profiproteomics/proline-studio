package fr.proline.studio.filter;

import fr.proline.studio.extendedtablemodel.ExtraDataType;
import fr.proline.studio.export.ExportFontData;
import fr.proline.studio.graphics.PlotInformation;
import fr.proline.studio.graphics.PlotType;
import fr.proline.studio.progress.ProgressInterface;
import fr.proline.studio.extendedtablemodel.ChildModelInterface;
import fr.proline.studio.table.DecoratedTableModel;
import fr.proline.studio.table.DecoratedTableModelInterface;
import fr.proline.studio.extendedtablemodel.GlobalTableModelInterface;
import fr.proline.studio.table.LazyData;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellRenderer;

/**
 * Model use to filter data and/or restrain rows of another model
 * @author JM235353
 */
public class LockedFilterTableModel extends DecoratedTableModel implements GlobalTableModelInterface, TableModelListener, ChildModelInterface {

    protected HashSet<Integer> m_restrainIds = null;
    protected ArrayList<Integer> m_filteredIds = null;
    
    private GlobalTableModelInterface m_tableModelSource = null;

    
    public LockedFilterTableModel(HashSet<Integer> restrainIds,  ArrayList<Integer> filteredIds, GlobalTableModelInterface tableModelSource) {
        m_tableModelSource = tableModelSource;
        m_tableModelSource.addTableModelListener(this);
        m_restrainIds = restrainIds;
        m_filteredIds = filteredIds;
        
        if (restrainIds != null) {
            m_restrainIds = new HashSet(restrainIds);
        }
        if (filteredIds != null) {
            m_filteredIds = new ArrayList<>(filteredIds);
        }
        
        
    }


    public int convertRowToOriginalModel(int row) {
        if (m_filteredIds == null) {
            return row;
        }
        return m_filteredIds.get(row);
    }

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
    public String getToolTipForHeader(int col) {
        return ((DecoratedTableModelInterface) m_tableModelSource).getToolTipForHeader(col);
    }
    
    @Override
    public String getTootlTipValue(int row, int col) {
        int rowFiltered = row;
        if (m_filteredIds != null) {
            rowFiltered = m_filteredIds.get(row).intValue();
        }
        return m_tableModelSource.getTootlTipValue(rowFiltered, col);
    }


    @Override
    public int getRowCount() {
        if (m_filteredIds != null) {
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
        if (m_filteredIds != null) {
            rowFiltered = m_filteredIds.get(rowIndex).intValue();
        }
        return m_tableModelSource.getValueAt(rowFiltered, columnIndex);
    }
    @Override
    public Class getColumnClass(int columnIndex) {
        return m_tableModelSource.getColumnClass(columnIndex);
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
        if (m_filteredIds != null) {
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
    public int[] getBestColIndex(PlotType plotType) {
        return m_tableModelSource.getBestColIndex(plotType);
    }


    @Override
    public String getExportRowCell(int row, int col) {
                 
        int rowFiltered = row;
        if (m_filteredIds != null) {
            rowFiltered = m_filteredIds.get(row).intValue();
        }
        return m_tableModelSource.getExportRowCell(rowFiltered, col);
    }
    
    @Override
    public ArrayList<ExportFontData> getExportFonts(int row, int col) {
        int rowFiltered = row;
        if (m_filteredIds != null) {
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
    }
    
    
    @Override
    public String getColumnName(int column) {
        return m_tableModelSource.getColumnName(column);
    }

    @Override
    public TableCellRenderer getRenderer(int row, int col) {
        return m_tableModelSource.getRenderer(row, col);
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
        return this;
    }

    @Override
    public void setParentModel(GlobalTableModelInterface parentModel) {
        if (m_tableModelSource != null) {
            m_tableModelSource.removeTableModelListener(this);
        }
        m_tableModelSource = parentModel;
        m_tableModelSource.addTableModelListener(this);
    }

    @Override
    public GlobalTableModelInterface getParentModel() {
        return m_tableModelSource;
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
        if (m_filteredIds != null) {
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
