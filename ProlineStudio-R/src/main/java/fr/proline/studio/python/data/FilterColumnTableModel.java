package fr.proline.studio.python.data;

import fr.proline.studio.comparedata.ExtraDataType;
import fr.proline.studio.export.ExportFontData;
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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author JM235353
 */
public class FilterColumnTableModel extends DecoratedTableModel implements ChildModelInterface {
    
    private GlobalTableModelInterface m_parentModel;
    
    private int[] m_columnsKept = null;
    private HashMap<Integer, Integer> m_reverseColumnsKept = new HashMap<>();
    
    public String m_name = null;
    
    
    private static DoubleRenderer DOUBLE_RENDERER = new DoubleRenderer(new DefaultRightAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class)), 4, true, true);
    
    private HashMap<Integer, Col> m_modifiedColumns = null;
    private HashMap<Integer, Object> m_modifiedColumnsExtraInfo = null;
    
    private final ArrayList<Col> m_extraColumns = new ArrayList();
    private final ArrayList<HashMap<Class, Object>> m_extraColumnInfos = new ArrayList();
    private final ArrayList<TableCellRenderer> m_extraColumnRenderers = new ArrayList();
    

    public FilterColumnTableModel(GlobalTableModelInterface parentModel, int[] columnsKept) {
        m_parentModel = parentModel;
        m_columnsKept = columnsKept;
        for (int i=0;i<m_columnsKept.length;i++) {
            m_reverseColumnsKept.put(m_columnsKept[i], i);
        }
    }

    
    @Override
    public int getRowCount() {
        return m_parentModel.getRowCount();
    }

    @Override
    public int getColumnCount() {
        return m_columnsKept.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {

        return m_parentModel.getValueAt(rowIndex, m_columnsKept[columnIndex]);

    }
    
    @Override
    public Class getColumnClass(int columnIndex) {

        return m_parentModel.getColumnClass(m_columnsKept[columnIndex]);
    }

    @Override
    public String getColumnName(int columnIndex) {

        return m_parentModel.getColumnName(m_columnsKept[columnIndex]);
    }
    
    @Override
    public Long getTaskId() {
        return m_parentModel.getTaskId();
    }

    @Override
    public LazyData getLazyData(int row, int col) {

        return m_parentModel.getLazyData(row, m_columnsKept[col]);
    }

    @Override
    public void givePriorityTo(Long taskId, int row, int col) {

        m_parentModel.givePriorityTo(taskId, row, m_parentModel.getColumnCount());
    }

    @Override
    public void sortingChanged(int col) {

        m_parentModel.sortingChanged(m_columnsKept[col]);
    }

    @Override
    public int getSubTaskId(int col) {

        return m_parentModel.getSubTaskId(m_columnsKept[col]);
    }

    @Override
    public String getToolTipForHeader(int col) {

        return m_parentModel.getToolTipForHeader(m_columnsKept[col]);
    }

    @Override
    public String getTootlTipValue(int row, int col) {

        return m_parentModel.getTootlTipValue(row, m_columnsKept[col]);
    }

    @Override
    public String getDataColumnIdentifier(int columnIndex) {

        return m_parentModel.getDataColumnIdentifier(m_columnsKept[columnIndex]);
    }

    @Override
    public Class getDataColumnClass(int columnIndex) {

        return m_parentModel.getDataColumnClass(m_columnsKept[columnIndex]);
    }

    @Override
    public Object getDataValueAt(int rowIndex, int columnIndex) {

        return m_parentModel.getDataValueAt(rowIndex, m_columnsKept[columnIndex]);
    }

    @Override
    public int[] getKeysColumn() {
        
        int size = 0;
        int[] parentKeysColumn = m_parentModel.getKeysColumn();
        if (parentKeysColumn != null) {
            for (int i = 0; i < parentKeysColumn.length; i++) {
                Integer inIndex = m_reverseColumnsKept.get(i);
                if (inIndex != null) {
                    size++;
                }
            }
        }
        
        int[] keys = null;
        if (size > 0) {
            keys = new int[size];
            size = 0;
            for (int i = 0; i < parentKeysColumn.length; i++) {
                Integer inIndex = m_reverseColumnsKept.get(i);
                if (inIndex != null) {
                    keys[size] = inIndex;
                    size++;
                }
            }
        }
        
        
        return keys;
    }

    @Override
    public int getInfoColumn() {
        
        int iParent = m_parentModel.getInfoColumn();
        Integer inIndex = m_reverseColumnsKept.get(iParent);
        if (inIndex == null) {
            return -1;
        }
        return inIndex;
    }

    @Override
    public void setName(String name) {
        m_name = name;
    }

    @Override
    public String getName() {
        if (m_name != null) {
            return m_name;
        }
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
            
            LinkedHashMap<Integer, Filter> parentFiltersMap = new LinkedHashMap<>();
            m_parentModel.addFilters(parentFiltersMap);
            
            Iterator<Integer> it = parentFiltersMap.keySet().iterator();
            while (it.hasNext()) {
                Integer i = it.next();
                Integer key = m_reverseColumnsKept.get(i);
                Filter filter = parentFiltersMap.get(i);
                filtersMap.put(key, filter);
                
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
    public int[] getBestColIndex(PlotType plotType) {
        int[] cols = m_parentModel.getBestColIndex(plotType);
        if (cols == null) {
            return null;
        }
        int nb = cols.length;
        int[] colsRes = new int[nb];
        for (int i=0;i<nb;i++) {
            Integer index = m_reverseColumnsKept.get(cols[i]);
            if (index == null) {
                colsRes[i] = -1;
            } else {
                colsRes[i] = index;
            }
        }
        return colsRes;
    }


    @Override
    public String getExportRowCell(int row, int col) {
        return m_parentModel.getExportRowCell(row, m_columnsKept[col]);
    }
    
    @Override
    public ArrayList<ExportFontData> getExportFonts(int row, int col) {
        return m_parentModel.getExportFonts(row, m_columnsKept[col]);
    }

    @Override
    public String getExportColumnName(int col) {
        return m_parentModel.getExportColumnName(m_columnsKept[col]);
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
        return m_parentModel.getRenderer(row, m_columnsKept[col]);
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

        return m_parentModel.getColValue(c, m_columnsKept[col]);
    }
 
}
