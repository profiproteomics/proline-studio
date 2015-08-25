package fr.proline.studio.python.data;

import fr.proline.studio.filter.DoubleFilter;
import fr.proline.studio.filter.Filter;
import fr.proline.studio.graphics.PlotInformation;
import fr.proline.studio.graphics.PlotType;
import fr.proline.studio.table.ChildModelInterface;
import fr.proline.studio.table.DecoratedTableModel;
import fr.proline.studio.table.GlobalTableModelInterface;
import fr.proline.studio.table.LazyData;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableCellRenderer;


/**
 *
 * @author JM235353
 */
public class ExprTableModel extends DecoratedTableModel implements ChildModelInterface {

    private final Col m_column;
    private GlobalTableModelInterface m_parentModel;
    private TableCellRenderer m_colRenderer = null;
    
    public ExprTableModel(Col column, TableCellRenderer colRenderer, GlobalTableModelInterface parentModel) {
        m_column = column;
        m_colRenderer = colRenderer;
        m_parentModel = parentModel;
    }
    
    @Override
    public int getRowCount() {
        return m_parentModel.getRowCount();
    }

    @Override
    public int getColumnCount() {
        return m_parentModel.getColumnCount()+1;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (columnIndex>=m_parentModel.getColumnCount()) {
            return m_column.getValueAt(rowIndex);
        }
        return m_parentModel.getValueAt(rowIndex, columnIndex);
    }
    
    @Override
    public Class getColumnClass(int columnIndex) {
        if (columnIndex>=m_parentModel.getColumnCount()) {
            return Double.class;
        }
        return m_parentModel.getColumnClass(columnIndex);
    }

    @Override
    public String getColumnName(int columnIndex) {
         if (columnIndex>=m_parentModel.getColumnCount()) {
            return m_column.getColumnName();
        }
        return m_parentModel.getColumnName(columnIndex);
    }
    
    @Override
    public Long getTaskId() {
        return m_parentModel.getTaskId();
    }

    @Override
    public LazyData getLazyData(int row, int col) {
        if (col>=m_parentModel.getColumnCount()) {
            return null; // no lazy data
        }
        return m_parentModel.getLazyData(row, col);
    }

    @Override
    public void givePriorityTo(Long taskId, int row, int col) {
        if (col>=m_parentModel.getColumnCount()) {
            return; // nothing to do
        }
        m_parentModel.givePriorityTo(taskId, row, col);
    }

    @Override
    public void sortingChanged(int col) {
        if (col>=m_parentModel.getColumnCount()) {
            return; // nothing to do
        }
        m_parentModel.sortingChanged(col);
    }

    @Override
    public int getSubTaskId(int col) {
        if (col>=m_parentModel.getColumnCount()) {
            return -1;
        }
        return m_parentModel.getSubTaskId(col);
    }

    @Override
    public String getToolTipForHeader(int col) {
        if (col>=m_parentModel.getColumnCount()) {
            return getColumnName(col);
        }
        return m_parentModel.getToolTipForHeader(col);
    }

    @Override
    public String getTootlTipValue(int row, int col) {
        if (col>=m_parentModel.getColumnCount()) {
            return null;
        }
        return m_parentModel.getTootlTipValue(row, col);
    }

    @Override
    public String getDataColumnIdentifier(int columnIndex) {
        if (columnIndex >= m_parentModel.getColumnCount()) {
            return getColumnName(columnIndex);
        }
        return m_parentModel.getDataColumnIdentifier(columnIndex);
    }

    @Override
    public Class getDataColumnClass(int columnIndex) {
        if (columnIndex >= m_parentModel.getColumnCount()) {
            return Double.class;
        }
        return m_parentModel.getDataColumnClass(columnIndex);
    }

    @Override
    public Object getDataValueAt(int rowIndex, int columnIndex) {
        if (columnIndex>=m_parentModel.getColumnCount()) {
            return m_column.getValueAt(rowIndex);
        }
        return m_parentModel.getDataValueAt(rowIndex, columnIndex);
    }

    @Override
    public int[] getKeysColumn() {
        return m_parentModel.getKeysColumn();
    }

    @Override
    public int getInfoColumn() {
        return m_parentModel.getInfoColumn();
    }

    @Override
    public void setName(String name) {
        m_parentModel.setName(name);
    }

    @Override
    public String getName() {
        return m_parentModel.getName();
    }

    @Override
    public Map<String, Object> getExternalData() {
        return m_parentModel.getExternalData();
    }

    @Override
    public PlotInformation getPlotInformation() {
        return m_parentModel.getPlotInformation();
    }

    @Override
    public void addFilters(LinkedHashMap<Integer, Filter> filtersMap) {
        if (filtersMap.isEmpty()) {
            m_parentModel.addFilters(filtersMap);
        }
        
        Integer key = m_parentModel.getColumnCount();
        if (!filtersMap.containsKey(key)) {
            filtersMap.put(m_parentModel.getColumnCount(), new DoubleFilter(m_column.getColumnName(),null));
        }
    }

    @Override
    public boolean isLoaded() {
        return m_parentModel.isLoaded();
    }

    @Override
    public int getLoadingPercentage() {
        return m_parentModel.getLoadingPercentage();
    }

    @Override
    public PlotType getBestPlotType() {
        return m_parentModel.getBestPlotType();
    }

    @Override
    public int getBestXAxisColIndex(PlotType plotType) {
        return m_parentModel.getBestXAxisColIndex(plotType);
    }

    @Override
    public int getBestYAxisColIndex(PlotType plotType) {
        return m_parentModel.getBestYAxisColIndex(plotType);
    }

    @Override
    public String getExportRowCell(int row, int col) {
        if (col >= m_parentModel.getColumnCount()) {
            Object o = getValueAt(row, col);
            if (o == null) {
                return "";
            }
            return ((Double) o).toString();
        }
        return m_parentModel.getExportRowCell(row, col);
    }

    @Override
    public String getExportColumnName(int col) {
        if (col >= m_parentModel.getColumnCount()) {
            return getColumnName(col);
        }
        return m_parentModel.getExportColumnName(col);   
    }

    @Override
    public void setParentModel(GlobalTableModelInterface parentModel) {
        if (m_parentModel != null) {
            m_parentModel.removeTableModelListener(this);
        }
        m_parentModel = parentModel;
        m_parentModel.addTableModelListener(this);
    }

    @Override
    public GlobalTableModelInterface getParentModel() {
        return m_parentModel;
    }

    @Override
    public void tableChanged(TableModelEvent e) {
        // JPM.TODO : redo calculation
        fireTableDataChanged();
    }

    @Override
    public TableCellRenderer getRenderer(int col) {
        if (col >= m_parentModel.getColumnCount()) {
            return m_colRenderer;
        }
        return m_parentModel.getRenderer(col);   
    }
 
}
