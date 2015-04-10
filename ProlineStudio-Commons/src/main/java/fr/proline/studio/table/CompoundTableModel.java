package fr.proline.studio.table;

import fr.proline.studio.filter.Filter;
import fr.proline.studio.filter.FilterTableModelInterfaceV2;
import fr.proline.studio.filter.FilterTableModelV2;
import fr.proline.studio.graphics.PlotInformation;
import fr.proline.studio.graphics.PlotType;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author JM235353
 */
public class CompoundTableModel extends AbstractTableModel implements GlobalTableModelInterface, TableModelListener, FilterTableModelInterfaceV2 {
    
    private GlobalTableModelInterface m_baseModel = null;
    private GlobalTableModelInterface m_lastModel = null;
    private FilterTableModelV2 m_filterModel = null;

    public CompoundTableModel(GlobalTableModelInterface baseModel, boolean filterCapability) {
        m_baseModel = baseModel;
        m_lastModel = baseModel;
        
        if (m_baseModel != null) {
            ((GlobalTableModelInterface)m_baseModel).addTableModelListener(this);
        }
        
    }

    public void addModel(ChildModelInterface model) {
        
        if (m_filterModel != null) {
            m_filterModel.getTableModelSource().removeTableModelListener(m_filterModel);
            model.setParentModel(m_filterModel.getTableModelSource());
            model.addTableModelListener(m_filterModel);
            m_filterModel.setTableModelSource(model);
            filter();
        } else {
            ((GlobalTableModelInterface) m_lastModel).removeTableModelListener(this);
            model.setParentModel(m_lastModel);
            model.addTableModelListener(this);
            m_lastModel = model;
        }
  
        fireTableStructureChanged();
    }
    
    public GlobalTableModelInterface getBaseModel() {
        return m_baseModel;
    }
    
    public GlobalTableModelInterface getLastNonFilterModel() {
        if (m_filterModel == null) {
            return m_lastModel;
        } else {
            return m_filterModel.getTableModelSource();
        }
    }
    
    public int convertCompoundRowToBaseModelRow(int row) {
        if (m_filterModel == null) {
            return row;
        }
        return m_filterModel.convertRowToOriginalModel(row);
    }
    public int convertBaseModelRowToCompoundRow(int row) {
        if (m_filterModel == null) {
            return row;
        }
         return m_filterModel.convertOriginalModelToRow(row);
    }
    
    @Override
    public int getRowCount() {
        if (m_lastModel == null) {
            return 0;
        }
        return m_lastModel.getRowCount();
    }

    @Override
    public int getColumnCount() {
        if (m_lastModel == null) {
            return 0;
        }
        return m_lastModel.getColumnCount();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return m_lastModel.getValueAt(rowIndex, columnIndex);
    }

    @Override
    public String getColumnName(int column) {
        return m_lastModel.getColumnName(column);
    }
    
    @Override
    public Class getColumnClass(int columnIndex) {
        return m_lastModel.getColumnClass(columnIndex);
    }
    

    
    @Override
    public void initFilters() {
        if (m_filterModel == null) {
            m_filterModel = new FilterTableModelV2(m_lastModel);
            m_lastModel.removeTableModelListener(this);
            m_lastModel = m_filterModel;
            m_filterModel.addTableModelListener(this);
        }
    }

    @Override
    public LinkedHashMap<Integer, Filter> getFilters() {
        if (m_filterModel == null) {
            initFilters();
        }
        return m_filterModel.getFilters();
    }

    @Override
    public Filter getColumnFilter(int col) {
        if (m_filterModel == null) {
            return null;
        }
        return m_filterModel.getColumnFilter(col);
    }

    @Override
    public void filter() {
        if (m_filterModel == null) {
            return;
        }
        m_filterModel.filter();
    }

    @Override
    public boolean filter(int row, int col) {
        if (m_filterModel == null) {
            return false;
        }
        return m_filterModel.filter(row, col);
    }

    @Override
    public boolean filter(int row) {
        if (m_filterModel == null) {
            return false;
        }
        return m_filterModel.filter(row);
    }

    @Override
    public int convertRowToOriginalModel(int row) {
        if (m_filterModel == null) {
            return row;
        }
        return m_filterModel.convertRowToOriginalModel(row);
    }

    @Override
    public int convertOriginalModelToRow(int row) {
        if (m_filterModel == null) {
            return row;
        }
        return m_filterModel.convertOriginalModelToRow(row);
    }
    
    @Override
    public void restrain(HashSet<Integer> restrainRowSet) {
        initFilters();
        m_filterModel.restrain(restrainRowSet);
    }

    @Override
    public HashSet<Integer> getRestrainRowSet() {
        if (m_filterModel == null) {
            return null;
        }
        return m_filterModel.getRestrainRowSet();
    }

    @Override
    public boolean hasRestrain() {
        if (m_filterModel == null) {
            return false;
        }
        return m_filterModel.hasRestrain();
    }

    @Override
    public boolean isLoaded() {

        return m_lastModel.isLoaded();
    }

    @Override
    public int getLoadingPercentage() {
        return m_lastModel.getLoadingPercentage();
    }

    @Override
    public String getDataColumnIdentifier(int columnIndex) {
        return m_lastModel.getDataColumnIdentifier(columnIndex);
    }

    @Override
    public Class getDataColumnClass(int columnIndex) {
        return m_lastModel.getDataColumnClass(columnIndex);
    }

    @Override
    public Object getDataValueAt(int rowIndex, int columnIndex) {
        return m_lastModel.getDataValueAt(rowIndex, columnIndex);
    }

    @Override
    public int[] getKeysColumn() {
        return m_lastModel.getKeysColumn();
    }

    @Override
    public int getInfoColumn() {
        return m_lastModel.getInfoColumn();
    }

    @Override
    public void setName(String name) {
       m_lastModel.setName(name);
    }

    @Override
    public String getName() {
        return m_lastModel.getName();
    }

    @Override
    public Map<String, Object> getExternalData() {
        return m_lastModel.getExternalData();
    }

    @Override
    public PlotInformation getPlotInformation() {
        return m_lastModel.getPlotInformation();
    }

    @Override
    public void tableChanged(TableModelEvent e) {
        fireTableChanged(e);
    }

    @Override
    public int getSubTaskId(int col) {
        return m_lastModel.getSubTaskId(col);
    }

    @Override
    public Long getTaskId() {
        return m_lastModel.getTaskId();
    }

    @Override
    public LazyData getLazyData(int row, int col) {
        return m_lastModel.getLazyData(row, col);
    }

    @Override
    public void givePriorityTo(Long taskId, int row, int col) {
        m_lastModel.givePriorityTo(taskId, row, col);
    }

    @Override
    public void sortingChanged(int col) {
        m_lastModel.sortingChanged(col);
    }

    @Override
    public String getToolTipForHeader(int col) {
        return m_lastModel.getToolTipForHeader(col);
    }
 
    @Override
    public String getTootlTipValue(int row, int col) {
        return m_lastModel.getTootlTipValue(row, col);
    }
    

    @Override
    public void addFilters(LinkedHashMap<Integer, Filter> filtersMap) {
        m_lastModel.addFilters(filtersMap);
    }

    @Override
    public PlotType getBestPlotType() {
        return m_lastModel.getBestPlotType();
    }

    @Override
    public int getBestXAxisColIndex(PlotType plotType) {
        return m_lastModel.getBestXAxisColIndex(plotType);
    }

    @Override
    public int getBestYAxisColIndex(PlotType plotType) {
        return m_lastModel.getBestYAxisColIndex(plotType);
    }

    @Override
    public String getExportRowCell(int row, int col) {
        return m_lastModel.getExportRowCell(row, col);
    }

    @Override
    public String getExportColumnName(int col) {
        return m_lastModel.getExportColumnName(col);
    }

    @Override
    public void setTableModelSource(GlobalTableModelInterface tableModelSource) {
        // nothing to do
    }

    @Override
    public GlobalTableModelInterface getTableModelSource() {
        return null;
    }


}
