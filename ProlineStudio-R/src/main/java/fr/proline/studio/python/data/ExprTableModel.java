package fr.proline.studio.python.data;

import fr.proline.studio.comparedata.ExtraDataType;
import fr.proline.studio.filter.DoubleFilter;
import fr.proline.studio.filter.Filter;
import fr.proline.studio.graphics.PlotInformation;
import fr.proline.studio.graphics.PlotType;
import fr.proline.studio.table.ChildModelInterface;
import fr.proline.studio.table.DecoratedTableModel;
import fr.proline.studio.table.GlobalTableModelInterface;
import fr.proline.studio.table.LazyData;
import fr.proline.studio.table.TableDefaultRendererManager;
import fr.proline.studio.table.renderer.DefaultRightAlignRenderer;
import fr.proline.studio.table.renderer.DoubleRenderer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableCellRenderer;


/**
 *
 * @author JM235353
 */
public class ExprTableModel extends DecoratedTableModel implements ChildModelInterface {

    private GlobalTableModelInterface m_parentModel;
    
    private static DoubleRenderer DOUBLE_RENDERER = new DoubleRenderer(new DefaultRightAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class)), 4, true, true);
    
    private HashMap<Integer, Col> m_modifiedColumns = null;
    private HashMap<Integer, Object> m_modifiedColumnsExtraInfo = null;
    
    private final ArrayList<Col> m_extraColumns = new ArrayList();
    private final ArrayList<HashMap<Class, Object>> m_extraColumnInfos = new ArrayList();
    private final ArrayList<TableCellRenderer> m_extraColumnRenderers = new ArrayList();
    

    public ExprTableModel(GlobalTableModelInterface parentModel) {
        m_parentModel = parentModel;
    }
    public ExprTableModel(Col column, TableCellRenderer colRenderer, GlobalTableModelInterface parentModel) {
        this(parentModel);
        addExtraColumn(column, colRenderer);
    }
    
    public final void addExtraColumn(Col column, TableCellRenderer colRenderer) {
        m_extraColumns.add(column);
        m_extraColumnInfos.add(null);
        m_extraColumnRenderers.add(colRenderer);
    }
    
    public final void addExtraColumnInfo(Object colExtraInfo) {
        if (colExtraInfo == null) {
            return;
        }
        
        
        int lastIndex = m_extraColumnInfos.size()-1;
        
        HashMap<Class, Object> map = m_extraColumnInfos.get(lastIndex);
        if (map == null) {
            map = new HashMap<>();
            m_extraColumnInfos.set(lastIndex, map);
        }
        
        map.put(colExtraInfo.getClass(), colExtraInfo);

    }


    
    public final void modifyColumnValues(HashMap<Integer, Col> modifiedColumns, HashMap<Integer, Object> modifiedColumnsExtraInfo) {
        m_modifiedColumns = modifiedColumns;
        m_modifiedColumnsExtraInfo = modifiedColumnsExtraInfo;
    }
    
    @Override
    public int getRowCount() {
        return m_parentModel.getRowCount();
    }

    @Override
    public int getColumnCount() {
        return m_parentModel.getColumnCount()+ m_extraColumns.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
   
        int parentCount = m_parentModel.getColumnCount();
        if (columnIndex>=parentCount) {
            return m_extraColumns.get(columnIndex-parentCount).getValueAt(rowIndex);
        }
        
        if (m_modifiedColumns!=null) {
            Col c = m_modifiedColumns.get(columnIndex);
            if (c != null) {
                return c.getValueAt(rowIndex);
            }
        }
        
        return m_parentModel.getValueAt(rowIndex, columnIndex);

    }
    
    @Override
    public Class getColumnClass(int columnIndex) {
        if (columnIndex>=m_parentModel.getColumnCount()) {
            return m_extraColumns.get(columnIndex-m_parentModel.getColumnCount()).getColumnClass();
        }
        
        if ((m_modifiedColumns != null) && (m_modifiedColumns.containsKey(columnIndex))) {
            return Double.class;
        }
        
        return m_parentModel.getColumnClass(columnIndex);
    }

    @Override
    public String getColumnName(int columnIndex) {
        int parentCount = m_parentModel.getColumnCount();
        if (columnIndex>=parentCount) {
            return m_extraColumns.get(columnIndex-parentCount).getColumnName();
        }
         
        if (m_modifiedColumns!=null) {
            Col c = m_modifiedColumns.get(columnIndex);
            if (c != null) {
                return c.getColumnName();
            }
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
        
        if (m_modifiedColumns != null) {
            Col c = m_modifiedColumns.get(col);
            if (c != null) {
                return null; // no lazy data
            }
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
        
        if (m_modifiedColumns != null) {
            Col c = m_modifiedColumns.get(col);
            if (c != null) {
                return c.getColumnName();
            }
        }
        
        return m_parentModel.getToolTipForHeader(col);
    }

    @Override
    public String getTootlTipValue(int row, int col) {
        if (col>=m_parentModel.getColumnCount()) {
            return null;
        }
        
        if (m_modifiedColumns != null) {
            Col c = m_modifiedColumns.get(col);
            if (c != null) {
                return c.getTooltip();
            }
        }
        
        return m_parentModel.getTootlTipValue(row, col);
    }

    @Override
    public String getDataColumnIdentifier(int columnIndex) {
        if (columnIndex >= m_parentModel.getColumnCount()) {
            return getColumnName(columnIndex);
        }
        
        if (m_modifiedColumns != null) {
            Col c = m_modifiedColumns.get(columnIndex);
            if (c != null) {
                return c.getColumnName();
            }
        }
        
        return m_parentModel.getDataColumnIdentifier(columnIndex);
    }

    @Override
    public Class getDataColumnClass(int columnIndex) {
        if (columnIndex >= m_parentModel.getColumnCount()) {
            return Double.class;
        }
        
        if ((m_modifiedColumns != null) && (m_modifiedColumns.containsKey(columnIndex))) {
            return Double.class;
        }
        
        return m_parentModel.getDataColumnClass(columnIndex);
    }

    @Override
    public Object getDataValueAt(int rowIndex, int columnIndex) {
        
        int parentCount = m_parentModel.getColumnCount();
        if (columnIndex>=parentCount) {
            return m_extraColumns.get(columnIndex-parentCount).getValueAt(rowIndex);
        }
        
        if (m_modifiedColumns != null) {
            Col c = m_modifiedColumns.get(columnIndex);
            if (c != null) {
                return c.getValueAt(rowIndex);
            }
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

        int parentCount = m_parentModel.getColumnCount();
        for (int i = 0; i < m_extraColumns.size(); i++) {
            Integer key = i + parentCount;

            if (!filtersMap.containsKey(key)) {
                filtersMap.put(key, new DoubleFilter(m_extraColumns.get(i).getColumnName(), null, getColumnCount() - 1));
            }
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
        
        if (m_modifiedColumns != null) {
            Col c = m_modifiedColumns.get(col);
            if (c != null) {
                Object d = c.getValueAt(row);
                if (d == null) {
                    return "";
                } else {
                    return d.toString();
                }
            }
        }
        
        return m_parentModel.getExportRowCell(row, col);
    }

    @Override
    public String getExportColumnName(int col) {
        if (col >= m_parentModel.getColumnCount()) {
            return getColumnName(col);
        }
        
        if (m_modifiedColumns != null) {
            Col c = m_modifiedColumns.get(col);
            if (c != null) {
                return getColumnName(col);
            }
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
    public TableCellRenderer getRenderer(int row, int col) {

        int parentCount = m_parentModel.getColumnCount();
        if (col >= parentCount) {
            return m_extraColumnRenderers.get(col - parentCount);
        }
        
        if (m_modifiedColumns != null) {
            Col c = m_modifiedColumns.get(col);
            if (c != null) {
                return DOUBLE_RENDERER;
            }
        }
        
        return m_parentModel.getRenderer(row, col);   
    }

    @Override
    public GlobalTableModelInterface getFrozzenModel() {
        return this;
    }

    @Override
    public ArrayList<ExtraDataType> getExtraDataTypes() {
        return m_parentModel.getExtraDataTypes();
    }

    @Override
    public Object getValue(Class c) {
        return m_parentModel.getValue(c);
    }

    @Override
    public Object getRowValue(Class c, int row) {
        return m_parentModel.getRowValue(c, row);
    }
    
    @Override
    public Object getColValue(Class c, int col) {
        
        
        if (m_modifiedColumnsExtraInfo!=null) {
            Object colExtraInfo = m_modifiedColumnsExtraInfo.get(col);
            if ((colExtraInfo != null) && (c.equals(colExtraInfo.getClass()))) {
                return colExtraInfo;
            }
        }
        
        
        int parentCount = m_parentModel.getColumnCount();
        if (col >= parentCount) {
            HashMap<Class, Object> colExtraInfoMap = m_extraColumnInfos.get(col - parentCount);
            if (colExtraInfoMap != null) {
                Object colExtraInfo = colExtraInfoMap.get(c);
                return colExtraInfo;
            }
            return null;
        }

        return m_parentModel.getColValue(c, col);
    }
 
}
