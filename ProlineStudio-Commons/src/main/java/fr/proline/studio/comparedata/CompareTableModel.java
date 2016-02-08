package fr.proline.studio.comparedata;



import fr.proline.studio.filter.Filter;
import fr.proline.studio.graphics.PlotInformation;
import fr.proline.studio.graphics.PlotType;
import fr.proline.studio.table.DecoratedTableModel;
import fr.proline.studio.table.GlobalTableModelInterface;
import fr.proline.studio.table.LazyData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.table.TableCellRenderer;



/**
 * JPM.TODO : rename this class
 * @author JM235353
 */
public class CompareTableModel extends DecoratedTableModel implements GlobalTableModelInterface {

    private GlobalTableModelInterface m_dataInterface = null;

    private String m_name;
    
    public CompareTableModel(GlobalTableModelInterface dataInterface) {
        m_dataInterface = dataInterface;
        
    }
    
    public void setDataInterface(GlobalTableModelInterface dataInterface) {
        m_dataInterface = dataInterface;
    }
    
    public CompareDataInterface getDataInterface() {
        return m_dataInterface;
    }
    
    @Override
    public int getRowCount() {
        if (m_dataInterface == null) {
            return 0;
        }

        return m_dataInterface.getRowCount();
    }

    @Override
    public int getColumnCount() {
        if (m_dataInterface == null) {
            return 0;
        }
        return m_dataInterface.getColumnCount();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (m_dataInterface == null) {
            return null; 
        }

        return m_dataInterface.getDataValueAt(rowIndex, columnIndex);
    }
    
    @Override
    public String getColumnName(int column) {
        if (m_dataInterface == null) {
            return null;
        }
        return m_dataInterface.getDataColumnIdentifier(column);
    }
    
    @Override
    public Class getColumnClass(int columnIndex) {
        if (m_dataInterface == null) {
            return null;
        }
        return m_dataInterface.getDataColumnClass(columnIndex);
    }

    @Override
    public String getToolTipForHeader(int col) {
        return getColumnName(col);
    }
    
    @Override
    public String getTootlTipValue(int row, int col) {
        return null;
    }



 


    @Override
    public boolean isLoaded() {
        return true;
    }

    @Override
    public int getLoadingPercentage() {
        return 100;
    }

    @Override
    public Long getTaskId() {
        return m_dataInterface.getTaskId();
    }

    @Override
    public LazyData getLazyData(int row, int col) {
        return m_dataInterface.getLazyData(row, col);
    }

    @Override
    public void givePriorityTo(Long taskId, int row, int col) {
        m_dataInterface.givePriorityTo(taskId, row, col);
    }

    @Override
    public void sortingChanged(int col) {
        m_dataInterface.sortingChanged(col);
    }

    @Override
    public int getSubTaskId(int col) {
        return m_dataInterface.getSubTaskId(col);
    }

    @Override
    public String getDataColumnIdentifier(int columnIndex) {
        return m_dataInterface.getDataColumnIdentifier(columnIndex);
    }

    @Override
    public Class getDataColumnClass(int columnIndex) {
        return m_dataInterface.getDataColumnClass(columnIndex);
    }

    @Override
    public Object getDataValueAt(int rowIndex, int columnIndex) {
        return m_dataInterface.getDataValueAt(rowIndex, columnIndex);
    }

    @Override
    public int[] getKeysColumn() {
        return m_dataInterface.getKeysColumn();
    }

    @Override
    public int getInfoColumn() {
        return m_dataInterface.getInfoColumn();
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
        return m_dataInterface.getExternalData();
    }

    @Override
    public PlotInformation getPlotInformation() {
        return m_dataInterface.getPlotInformation();
    }

    @Override
    public void addFilters(LinkedHashMap<Integer, Filter> filtersMap) {
        m_dataInterface.addFilters(filtersMap);
    }

    @Override
    public PlotType getBestPlotType() {
        return m_dataInterface.getBestPlotType();
    }

    @Override
    public int getBestXAxisColIndex(PlotType plotType) {
        return m_dataInterface.getBestXAxisColIndex(plotType);
    }

    @Override
    public int getBestYAxisColIndex(PlotType plotType) {
        return m_dataInterface.getBestYAxisColIndex(plotType);
    }

    @Override
    public String getExportRowCell(int row, int col) {
        return null;
    }

    @Override
    public String getExportColumnName(int col) {
        return getDataColumnIdentifier(col);
    }

    @Override
    public TableCellRenderer getRenderer(int row, int col) {
        return m_dataInterface.getRenderer(row, col);
    }

    @Override
    public long row2UniqueId(int rowIndex) {
        return m_dataInterface.row2UniqueId(rowIndex);
    }


    @Override
    public int uniqueId2Row(long id) {
        return m_dataInterface.uniqueId2Row(id);
    }

    @Override
    public GlobalTableModelInterface getFrozzenModel() {
        return this;
    }

    @Override
    public ArrayList<ExtraDataType> getExtraDataTypes() {
        if (m_dataInterface == null) {
            return null;
        }
        return m_dataInterface.getExtraDataTypes();
    }

    @Override
    public Object getValue(Class c) {
        if (m_dataInterface == null) {
            return null;
        }
        return m_dataInterface.getValue(c);
    }

    @Override
    public Object getRowValue(Class c, int row) {
        if (m_dataInterface == null) {
            return null;
        }
        return m_dataInterface.getRowValue(c, row);
    }
    
    @Override
    public Object getColValue(Class c, int col) {
        if (m_dataInterface == null) {
            return null;
        }
        return m_dataInterface.getColValue(c, col);
    }

    @Override
    public void addSingleValue(Object v) {
        return; // should not be called
    }
    
    @Override
    public Object getSingleValue(Class c) {
        return null; // should not be called
    }



    
}
