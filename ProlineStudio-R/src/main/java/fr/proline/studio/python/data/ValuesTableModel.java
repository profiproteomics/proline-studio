package fr.proline.studio.python.data;

import fr.proline.studio.comparedata.ExtraDataType;
import fr.proline.studio.filter.Filter;
import fr.proline.studio.graphics.PlotInformation;
import fr.proline.studio.graphics.PlotType;
import fr.proline.studio.table.DecoratedTableModel;
import fr.proline.studio.table.GlobalTableModelInterface;
import fr.proline.studio.table.LazyData;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author JM235353
 */
public class ValuesTableModel extends DecoratedTableModel implements GlobalTableModelInterface {

    public static final int COLTYPE_NAME = 0;
    public static final int COLTYPE_VALUE = 1;
    
    private static final String[] m_columnNames =  {"Name", "Value"};
    
    private String m_name = "";
    
    private ArrayList<String> m_valuesNameList = null;
    private ArrayList<String> m_valuesList = null;
    
    public ValuesTableModel(ArrayList<String> valuesNameList, ArrayList<String> valuesList) {
        m_valuesNameList = valuesNameList;
        m_valuesList = valuesList;
    }
    
    @Override
    public Class getColumnClass(int col) {
        return String.class;        
    }
    
    @Override
    public String getColumnName(int columnIndex) {
        return m_columnNames[columnIndex];
    }
    
    @Override
    public int getRowCount() {
        return m_valuesNameList.size();
    }

    @Override
    public int getColumnCount() {
        return m_columnNames.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case COLTYPE_NAME:
                return m_valuesNameList.get(rowIndex);
            case COLTYPE_VALUE:
                return m_valuesList.get(rowIndex);
        }
        return null;
    }

    @Override
    public String getToolTipForHeader(int col) {
        return null;
    }

    @Override
    public String getTootlTipValue(int row, int col) {
        return null;
    }

    @Override
    public TableCellRenderer getRenderer(int row, int col) {
        return null;
    }

    @Override
    public GlobalTableModelInterface getFrozzenModel() {
        return this;
    }

    @Override
    public Long getTaskId() {
        return -1l;
    }

    @Override
    public LazyData getLazyData(int row, int col) {
        return null;
    }

    @Override
    public void givePriorityTo(Long taskId, int row, int col) {
        
    }

    @Override
    public void sortingChanged(int col) {
        
    }

    @Override
    public int getSubTaskId(int col) {
        return -1;
    }

    @Override
    public String getDataColumnIdentifier(int columnIndex) {
        return getColumnName(columnIndex);
    }

    @Override
    public Class getDataColumnClass(int columnIndex) {
        return getColumnClass(columnIndex);
    }

    @Override
    public Object getDataValueAt(int rowIndex, int columnIndex) {
        return getValueAt(rowIndex, columnIndex);
    }

    @Override
    public int[] getKeysColumn() {
        return null;
    }

    @Override
    public int getInfoColumn() {
        return -1;
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
        return null;
    }

    @Override
    public PlotInformation getPlotInformation() {
        return null;
    }

    @Override
    public ArrayList<ExtraDataType> getExtraDataTypes() {
        return null;
    }

    @Override
    public Object getValue(Class c) {
        return null;
    }

    @Override
    public Object getRowValue(Class c, int row) {
        return null;
    }

    @Override
    public Object getColValue(Class c, int col) {
        return null;
    }

    @Override
    public void addFilters(LinkedHashMap<Integer, Filter> filtersMap) {
        
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
    public PlotType getBestPlotType() {
        return null;
    }

    @Override
    public int getBestXAxisColIndex(PlotType plotType) {
        return -1;
    }

    @Override
    public int getBestYAxisColIndex(PlotType plotType) {
        return -1;
    }

    @Override
    public String getExportRowCell(int row, int col) {
        return (String) getValueAt(row, col);
    }

    @Override
    public String getExportColumnName(int col) {
        return getColumnName(col);
    }
    
}
