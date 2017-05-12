
package fr.proline.studio.rsmexplorer.gui.model;

import fr.proline.studio.comparedata.ExtraDataType;
import fr.proline.studio.dam.data.ClearProjectData;
import fr.proline.studio.export.ExportFontData;
import fr.proline.studio.filter.Filter;
import fr.proline.studio.graphics.PlotInformation;
import fr.proline.studio.graphics.PlotType;
import fr.proline.studio.rsmexplorer.gui.renderer.TimestampRenderer;
import fr.proline.studio.table.DecoratedTableModel;
import fr.proline.studio.table.GlobalTableModelInterface;
import fr.proline.studio.table.LazyData;
import fr.proline.studio.table.TableDefaultRendererManager;
import fr.proline.studio.table.renderer.DefaultLeftAlignRenderer;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author MB243701
 */
public class DataClearProjectTableModel extends DecoratedTableModel implements GlobalTableModelInterface{
    public static final int COLTYPE_DATA_IS_SELECTED = 0;
    public static final int COLTYPE_DATA_ID = 1;
    public static final int COLTYPE_DATA_TYPE = 2;
    public static final int COLTYPE_DATA_RS_NAME = 3;
    public static final int COLTYPE_DATA_PEAKLIST_PATH = 4;
    public static final int COLTYPE_DATA_MSI_SEARCH_FILENAME = 5;
    public static final int COLTYPE_DATA_MSI_SEARCH_DIRECTORY = 6;
    public static final int COLTYPE_DATA_MSI_SEARCH_DATE = 7;
    
    
    private static final String[] m_columnNames = {"Selected", "Id", "Type", "Search Result Name",  "Peaklist Path", "MSISearch File Name", "MSISearch File Directory", "SearchDate"};
    private static final String[] m_toolTipColumns = {"Should be selected to be deleted", "Id of Search Result/Identification Summary", "Type: Search Result or Identification Summary and decoy or quantitation data", "Search Result Name", "Peaklist Path", "MSISearch File Name", "MSISearch File Directory","MSISearch Date"};
     

    private List<ClearProjectData> m_dataToClear;
    
    private final HashMap<Integer, TableCellRenderer> m_rendererMap = new HashMap();
    private String m_modelName;

    public DataClearProjectTableModel() {
        super();
    }
    
     
    @Override
    public int getRowCount() {
        if (m_dataToClear == null) {
            return 0;
        }

        return m_dataToClear.size();
    }

    @Override
    public int getColumnCount() {
        return m_columnNames.length;
    }

    @Override
    public Object getValueAt(int row, int col) {
        ClearProjectData data = m_dataToClear.get(row);
        switch (col) {
            case COLTYPE_DATA_IS_SELECTED: {
                return data.isSelected();
            }
            case COLTYPE_DATA_ID: {
                return data.getId();
            }
            case COLTYPE_DATA_TYPE:{
                return data.getType();
            }
            case COLTYPE_DATA_RS_NAME:{
                return data.getRsName();
            }
            case COLTYPE_DATA_PEAKLIST_PATH:{
                return data.getPeaklistPath();
            }
            case COLTYPE_DATA_MSI_SEARCH_FILENAME:{
                return data.getMsiSearchFileName();
            }
            case COLTYPE_DATA_MSI_SEARCH_DIRECTORY:{
                return data.getMsiSearchDirectory();
            }
            case COLTYPE_DATA_MSI_SEARCH_DATE:{
                return data.getMsiSearchDate();
            }
        }
        return null; // should never happen
    }

    @Override
    public String getToolTipForHeader(int col) {
        return m_toolTipColumns[col];
    }

    @Override
    public String getTootlTipValue(int row, int col) {
        return "";
    }

    @Override
    public TableCellRenderer getRenderer(int row, int col) {
        if (m_rendererMap.containsKey(col)) {
            return m_rendererMap.get(col);
        }

        TableCellRenderer renderer = null;

        switch (col){

            case COLTYPE_DATA_IS_SELECTED: {
                break;
            }
            case COLTYPE_DATA_TYPE:
            case COLTYPE_DATA_RS_NAME: 
            case COLTYPE_DATA_PEAKLIST_PATH:
            case COLTYPE_DATA_MSI_SEARCH_FILENAME:
            case COLTYPE_DATA_MSI_SEARCH_DIRECTORY:{
                renderer = new DefaultLeftAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class));
                break;
            }
            case COLTYPE_DATA_MSI_SEARCH_DATE: {
                renderer = new TimestampRenderer( TableDefaultRendererManager.getDefaultRenderer(String.class));
                break;
            }
        }
        
        m_rendererMap.put(col, renderer);
        return renderer;
    }

    @Override
    public GlobalTableModelInterface getFrozzenModel() {
        return this;
    }

    @Override
    public Long getTaskId() {
        return (long)-1;
    }

    @Override
    public LazyData getLazyData(int row, int col) {
        return null;
    }

    @Override
    public void givePriorityTo(Long taskId, int row, int col) {
        //
    }

    @Override
    public void sortingChanged(int col) {
        //
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
    public Class getColumnClass(int col) {
        switch (col){
            case COLTYPE_DATA_IS_SELECTED:
                return Boolean.class;
            case COLTYPE_DATA_ID:
                return Long.class;
            case COLTYPE_DATA_RS_NAME:
            case COLTYPE_DATA_PEAKLIST_PATH:
            case COLTYPE_DATA_TYPE:
            case COLTYPE_DATA_MSI_SEARCH_FILENAME:
            case COLTYPE_DATA_MSI_SEARCH_DIRECTORY:
                return String.class;
            case COLTYPE_DATA_MSI_SEARCH_DATE:
                return Timestamp.class;
        }
        return null; // should not happen
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
        int[] keys = { COLTYPE_DATA_ID};
        return keys;
    }

    @Override
    public int getInfoColumn() {
        return COLTYPE_DATA_RS_NAME;
    }

    @Override
    public void setName(String name) {
        m_modelName = name;
    }

    @Override
    public String getName() {
        return m_modelName;
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
        return getSingleValue(c);
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
        return null;
    }
    
    @Override
    public ArrayList<ExportFontData> getExportFonts(int row, int col) {
        return null;
    }

    @Override
    public String getExportColumnName(int col) {
         return m_columnNames[col];
    }
    
    @Override
    public String getColumnName(int col) {
        return m_columnNames[col];
    }
    
    public void setData(List<ClearProjectData> data){
        this.m_dataToClear = data;
        fireTableDataChanged();
    }
    
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (!m_dataToClear.get(rowIndex).isEditable()){
            return false;
        }
        switch (columnIndex){
            case COLTYPE_DATA_IS_SELECTED:
                return true;
            case COLTYPE_DATA_ID:
            case COLTYPE_DATA_RS_NAME:
            case COLTYPE_DATA_PEAKLIST_PATH:
            case COLTYPE_DATA_TYPE:
            case COLTYPE_DATA_MSI_SEARCH_FILENAME:
            case COLTYPE_DATA_MSI_SEARCH_DIRECTORY:
            case COLTYPE_DATA_MSI_SEARCH_DATE:
                return false;
        }
        return false; // should not happen
        
    }
    
    
    public List<ClearProjectData> getSelectedData(){
        if (m_dataToClear != null){
            return m_dataToClear.stream().filter(d -> d.isSelected()).collect(Collectors.toList());
        }
        return new ArrayList();
    }
    
    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (m_dataToClear != null && columnIndex == COLTYPE_DATA_IS_SELECTED){
            m_dataToClear.get(rowIndex).setSelected((Boolean)aValue); 
        }
        fireTableCellUpdated(rowIndex, columnIndex);
    }
}
