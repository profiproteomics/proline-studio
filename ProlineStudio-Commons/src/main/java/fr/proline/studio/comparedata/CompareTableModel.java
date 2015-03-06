package fr.proline.studio.comparedata;



import fr.proline.studio.filter.Filter;
import fr.proline.studio.graphics.PlotInformation;
import fr.proline.studio.graphics.PlotType;
import fr.proline.studio.table.DecoratedTableModel;
import fr.proline.studio.table.GlobalTableModelInterface;
import fr.proline.studio.table.LazyData;
import java.util.LinkedHashMap;
import java.util.Map;



/**
 *
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

    
    /*@Override
    public void initFilters() {
        if (m_filters == null) {
            int nbCol = getColumnCount();
            m_filters = new Filter[nbCol];
            
            for (int i=0;i<nbCol;i++) {
                Class columnClass = getColumnClass(i);
                if (columnClass.equals(String.class)) {
                    m_filters[i] = new StringFilter(getColumnName(i), null);
                } else if (columnClass.equals(Integer.class)) {
                    m_filters[i] = new IntegerFilter(getColumnName(i), null);
                } else if (columnClass.equals(Double.class)) {
                    m_filters[i] = new DoubleFilter(getColumnName(i), null);
                } else if (columnClass.equals(Float.class)) {
                    m_filters[i] = new DoubleFilter(getColumnName(i), null);
                } else {
                    m_filters[i] = null;
                }
            }

        }
    }*/


 


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







    
}
