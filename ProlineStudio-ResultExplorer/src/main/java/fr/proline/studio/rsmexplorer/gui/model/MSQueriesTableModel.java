/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.gui.model;

import fr.proline.core.orm.msi.MsQuery;
import fr.proline.studio.dam.tasks.DatabaseLoadMSQueriesTask;
import fr.proline.studio.filter.DoubleFilter;
import fr.proline.studio.filter.Filter;
import fr.proline.studio.filter.IntegerFilter;
import fr.proline.studio.filter.StringFilter;
import fr.proline.studio.graphics.PlotInformation;
import fr.proline.studio.graphics.PlotType;
import fr.proline.studio.rsmexplorer.gui.renderer.DefaultLeftAlignRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.DefaultRightAlignRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.DoubleRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.FloatRenderer;
import fr.proline.studio.table.GlobalTableModelInterface;
import fr.proline.studio.table.LazyData;
import fr.proline.studio.table.LazyTable;
import fr.proline.studio.table.LazyTableModel;
import fr.proline.studio.table.TableDefaultRendererManager;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.table.TableCellRenderer;

/**
 * table model for msqQueries of a ResultSet
 * @author MB243701
 */
public class MSQueriesTableModel extends LazyTableModel implements GlobalTableModelInterface{

    public static final int COLTYPE_MSQUERY_ID = 0;
    public static final int COLTYPE_MSQUERY_INITIAL_ID = 1;
    public static final int COLTYPE_MSQUERY_CHARGE = 2;
    public static final int COLTYPE_MSQUERY_MOZ = 3;
    public static final int COLTYPE_MSQUERY_NB_PEPTIDE_MATCH = 4;
    public static final int COLTYPE_MSQUERY_FIRST_SCAN = 5;
    public static final int COLTYPE_MSQUERY_LAST_SCAN = 6;
    public static final int COLTYPE_MSQUERY_FIRST_TIME = 7;
    public static final int COLTYPE_MSQUERY_LAST_TIME = 8;
    public static final int COLTYPE_MSQUERY_SPECTRUM_TITLE = 9;
    
    private static final String[] m_columnNames =  {"Id", "Initial Id", "Charge", "m/z", "#Peptide Matches", "First Scan", "Last Scan", "First Time", "Last Time", "Spectrum Title"};
    private static final String[] m_columnTooltips = {"MS Query Id", "Initial Id", "Charge", "m/z", "Peptide Matches count", "First Scan", "Last Scan", "First Time", "Last Time", "Spectrum Title"};
    
    private final HashMap<Integer, TableCellRenderer> m_rendererMap = new HashMap();
    
    private List<MsQuery> m_msqueries;
    private Map<Long, Integer> m_nbPeptideMatchesByMsQueryIdMap;
    
    private String m_modelName;
    
    public MSQueriesTableModel(LazyTable table) {
        super(table);
    }
    
    public MsQuery getMsQuery(int row) {
        return m_msqueries.get(row);
    }
    
    @Override
    public int getSubTaskId(int col) {
        return DatabaseLoadMSQueriesTask.SUB_TASK_MSQUERY;
    }

    @Override
    public int getRowCount() {
        if (m_msqueries == null) {
            return 0;
        }

        return m_msqueries.size();
    }

    @Override
    public int getColumnCount() {
        return m_columnNames.length ;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        // Retrieve MSQuery
        MsQuery msquery = m_msqueries.get(rowIndex);

        switch (columnIndex) {
            case COLTYPE_MSQUERY_ID: {
                return msquery.getId();
            }
            case COLTYPE_MSQUERY_INITIAL_ID: {
                LazyData lazyData = getLazyData(rowIndex,columnIndex);
                
                if (msquery.getMsiSearch()!= null) {
                    lazyData.setData(msquery.getInitialId());
                }else{
                    lazyData.setData(null);
                    givePriorityTo(m_taskId, rowIndex, columnIndex);
                }
                return lazyData;
            }
            case COLTYPE_MSQUERY_CHARGE: {
                LazyData lazyData = getLazyData(rowIndex,columnIndex);
                
                if (msquery.getMsiSearch()!= null) {
                    lazyData.setData(msquery.getCharge());
                }else{
                    lazyData.setData(null);
                    givePriorityTo(m_taskId, rowIndex, columnIndex);
                }
                return lazyData;
            }
            case COLTYPE_MSQUERY_MOZ: {
                LazyData lazyData = getLazyData(rowIndex,columnIndex);
                
                if (msquery.getMsiSearch()!= null) {
                    lazyData.setData(msquery.getMoz());
                }else{
                    lazyData.setData(null);
                    givePriorityTo(m_taskId, rowIndex, columnIndex);
                }
                return lazyData;
            }
            case COLTYPE_MSQUERY_NB_PEPTIDE_MATCH: {
                LazyData lazyData = getLazyData(rowIndex,columnIndex);
                
                if (msquery.getMsiSearch()!= null) {
                    lazyData.setData(m_nbPeptideMatchesByMsQueryIdMap.get(msquery.getId()));
                }else{
                    lazyData.setData(null);
                    givePriorityTo(m_taskId, rowIndex, columnIndex);
                }
                return lazyData;
            }
            case COLTYPE_MSQUERY_FIRST_SCAN: {
                LazyData lazyData = getLazyData(rowIndex,columnIndex);
                
                if (msquery.getMsiSearch()!= null) {
                    lazyData.setData(msquery.getSpectrum().getFirstScan() == null ? 0: msquery.getSpectrum().getFirstScan());
                }else{
                    lazyData.setData(null);
                    givePriorityTo(m_taskId, rowIndex, columnIndex);
                }
                return lazyData;
            }
            case COLTYPE_MSQUERY_LAST_SCAN: {
                LazyData lazyData = getLazyData(rowIndex,columnIndex);
                
                if (msquery.getMsiSearch()!= null) {
                    lazyData.setData(msquery.getSpectrum().getLastScan() == null ? 0 : msquery.getSpectrum().getLastScan());
                }else{
                    lazyData.setData(null);
                    givePriorityTo(m_taskId, rowIndex, columnIndex);
                }
                return lazyData;
            }
            case COLTYPE_MSQUERY_FIRST_TIME: {
                LazyData lazyData = getLazyData(rowIndex,columnIndex);
                
                if (msquery.getMsiSearch()!= null) {
                    lazyData.setData(msquery.getSpectrum().getFirstTime() == null ? Float.NaN : msquery.getSpectrum().getFirstTime());
                }else{
                    lazyData.setData(null);
                    givePriorityTo(m_taskId, rowIndex, columnIndex);
                }
                return lazyData;
            }
            case COLTYPE_MSQUERY_LAST_TIME: {
                LazyData lazyData = getLazyData(rowIndex,columnIndex);
                
                if (msquery.getMsiSearch()!= null) {
                    lazyData.setData(msquery.getSpectrum().getLastTime() == null ? Float.NaN : msquery.getSpectrum().getLastTime());
                }else{
                    lazyData.setData(null);
                    givePriorityTo(m_taskId, rowIndex, columnIndex);
                }
                return lazyData;
            }
            case COLTYPE_MSQUERY_SPECTRUM_TITLE: {
                LazyData lazyData = getLazyData(rowIndex,columnIndex);
                
                if (msquery.getMsiSearch()!= null) {
                    lazyData.setData(msquery.getSpectrum().getTitle());
                }else{
                    lazyData.setData(null);
                    givePriorityTo(m_taskId, rowIndex, columnIndex);
                }
                return lazyData;
            }
        }
        return null; // should never happen
    }

    @Override
    public String getToolTipForHeader(int col) {
         return m_columnTooltips[col];
    }

    @Override
    public String getTootlTipValue(int row, int col) {
        return null;
    }

    @Override
    public TableCellRenderer getRenderer(int col) {
        
        if (m_rendererMap.containsKey(col)) {
            return m_rendererMap.get(col);
        }

        TableCellRenderer renderer = null;

        switch (col) {
            case COLTYPE_MSQUERY_ID: {
                break;
            }
            case COLTYPE_MSQUERY_INITIAL_ID: {
                break;
            }
            case COLTYPE_MSQUERY_CHARGE:
            case COLTYPE_MSQUERY_NB_PEPTIDE_MATCH:
            case COLTYPE_MSQUERY_FIRST_SCAN:
            case COLTYPE_MSQUERY_LAST_SCAN:
            {
                renderer = new DefaultRightAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(Integer.class));
                break;
            }
            case COLTYPE_MSQUERY_MOZ:{
                renderer = new DoubleRenderer( new DefaultRightAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class)), 4 );
                break;
            }
            case COLTYPE_MSQUERY_FIRST_TIME:
            case COLTYPE_MSQUERY_LAST_TIME:    
            {
                renderer = new FloatRenderer( new DefaultRightAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class)), 4 );
                break;
            }
            case COLTYPE_MSQUERY_SPECTRUM_TITLE: {
                renderer = new DefaultLeftAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class));
                break;
            }
        }
        
        m_rendererMap.put(col, renderer);
        return renderer;
    }

    @Override
    public String getDataColumnIdentifier(int columnIndex) {
        return m_columnNames[columnIndex];
    }

    @Override
    public Class getDataColumnClass(int columnIndex) {
        switch (columnIndex) {
            case COLTYPE_MSQUERY_ID: 
            case COLTYPE_MSQUERY_INITIAL_ID:{
                return Long.class;
            }
            case COLTYPE_MSQUERY_CHARGE:
            case COLTYPE_MSQUERY_NB_PEPTIDE_MATCH :
            case COLTYPE_MSQUERY_FIRST_SCAN :
            case COLTYPE_MSQUERY_LAST_SCAN :
            {
                return Integer.class;
            }
            case COLTYPE_MSQUERY_MOZ : {
                return Double.class;
            }
            case COLTYPE_MSQUERY_FIRST_TIME : 
            case COLTYPE_MSQUERY_LAST_TIME :{
                return Float.class;
            }
            case COLTYPE_MSQUERY_SPECTRUM_TITLE :{
                return String.class;
            }
        }
        return null; // should never happen
    }

    @Override
    public Object getDataValueAt(int rowIndex, int columnIndex) {
        Object data = getValueAt(rowIndex, columnIndex);
        if (data instanceof LazyData) {
            data = ((LazyData) data).getData();
        }
        // Retrieve MSQuery
        MsQuery msq = m_msqueries.get(rowIndex);

        if(columnIndex == COLTYPE_MSQUERY_ID) {
            return msq.getId();
        }
        return data;
    }

    @Override
    public int[] getKeysColumn() {
        int[] keys = { COLTYPE_MSQUERY_ID};
        return keys;
    }

    @Override
    public int getInfoColumn() {
        return COLTYPE_MSQUERY_ID;
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
    public void addFilters(LinkedHashMap<Integer, Filter> filtersMap) {
        filtersMap.put(COLTYPE_MSQUERY_CHARGE, new IntegerFilter(getColumnName(COLTYPE_MSQUERY_CHARGE), null, COLTYPE_MSQUERY_CHARGE));
        filtersMap.put(COLTYPE_MSQUERY_MOZ, new DoubleFilter(getColumnName(COLTYPE_MSQUERY_MOZ), null, COLTYPE_MSQUERY_MOZ));
        filtersMap.put(COLTYPE_MSQUERY_NB_PEPTIDE_MATCH, new IntegerFilter(getColumnName(COLTYPE_MSQUERY_NB_PEPTIDE_MATCH), null, COLTYPE_MSQUERY_NB_PEPTIDE_MATCH));
        filtersMap.put(COLTYPE_MSQUERY_FIRST_SCAN, new IntegerFilter(getColumnName(COLTYPE_MSQUERY_FIRST_SCAN), null, COLTYPE_MSQUERY_FIRST_SCAN));
        filtersMap.put(COLTYPE_MSQUERY_LAST_SCAN, new IntegerFilter(getColumnName(COLTYPE_MSQUERY_LAST_SCAN), null, COLTYPE_MSQUERY_LAST_SCAN));
        filtersMap.put(COLTYPE_MSQUERY_FIRST_TIME, new DoubleFilter(getColumnName(COLTYPE_MSQUERY_FIRST_TIME), null, COLTYPE_MSQUERY_FIRST_TIME));
        filtersMap.put(COLTYPE_MSQUERY_LAST_TIME, new DoubleFilter(getColumnName(COLTYPE_MSQUERY_LAST_TIME), null, COLTYPE_MSQUERY_LAST_TIME));
        filtersMap.put(COLTYPE_MSQUERY_SPECTRUM_TITLE, new StringFilter(getColumnName(COLTYPE_MSQUERY_SPECTRUM_TITLE), null, COLTYPE_MSQUERY_SPECTRUM_TITLE));
    }

    @Override
    public boolean isLoaded() {
        return m_table.isLoaded();
    }

    @Override
    public int getLoadingPercentage() {
        return m_table.getLoadingPercentage();
    }

    @Override
    public PlotType getBestPlotType() {
        return null; //JPM.TODO
    }

    @Override
    public int getBestXAxisColIndex(PlotType plotType) {
        return -1; //JPM.TODO
    }

    @Override
    public int getBestYAxisColIndex(PlotType plotType) {
        return -1; //JPM.TODO
    }

    @Override
    public String getExportRowCell(int row, int col) {
        return null;
    }

    @Override
    public String getExportColumnName(int col) {
        return getColumnName(col);
    }
    
    
    public void dataUpdated() {
        fireTableDataChanged();
    }
    
    public void setData(Long taskId,  List<MsQuery> msQueries, Map<Long, Integer> nbPeptideMatchesByMsQueryIdMap) {
        this.m_msqueries = msQueries ;
        this.m_nbPeptideMatchesByMsQueryIdMap = nbPeptideMatchesByMsQueryIdMap;
        fireTableStructureChanged();
        
        m_taskId = taskId;

        fireTableDataChanged();
    }
    
   @Override
    public Class getColumnClass(int col) {
        if (col == COLTYPE_MSQUERY_ID) {
            return Long.class;
        }
        return LazyData.class;
    }
    
    @Override
    public String getColumnName(int col) {
        return m_columnNames[col];
    }
    
     public MsQuery getSelectedMsQuery(int row){
        return m_msqueries.get(row);
     }

    @Override
    public GlobalTableModelInterface getFrozzenModel() {
        return this;
    }
}
