package fr.proline.studio.rsmexplorer.gui.xic;

import fr.proline.core.orm.lcms.MapAlignment;
import fr.proline.core.orm.lcms.MapTime;
import fr.proline.studio.comparedata.ExtraDataType;
import fr.proline.studio.export.ExportFontData;
import fr.proline.studio.filter.DoubleFilter;
import fr.proline.studio.filter.Filter;
import fr.proline.studio.graphics.PlotInformation;
import fr.proline.studio.graphics.PlotType;
import fr.proline.studio.table.renderer.DefaultRightAlignRenderer;
import fr.proline.studio.table.renderer.DoubleRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.TimeRenderer;
import fr.proline.studio.table.GlobalTableModelInterface;
import fr.proline.studio.table.LazyData;
import fr.proline.studio.table.LazyTable;
import fr.proline.studio.table.LazyTableModel;
import fr.proline.studio.table.TableDefaultRendererManager;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.table.TableCellRenderer;

/**
 * map alignment table model (table not displayed, only used for graphic)
 * @author MB243701
 */
public class MapTimeTableModel  extends LazyTableModel implements GlobalTableModelInterface {
    public static final int COLTYPE_TIME = 0;
    public static final int COLTYPE_DELTA_TIME = 1;
    
    private static final String[] m_columnNames = {"Time in Reference Map (min)", "Delta time vs. Reference Map (s)"};
    private static final String[] m_toolTipColumns = {"Time (min)", "Delta time (s)"};
    
    private MapAlignment m_mapAlignment;
    private List<MapTime> m_mapTimes = null;
    private Color m_color = null;
    private String m_title = null;
    
    private final HashMap<Integer, TableCellRenderer> m_rendererMap = new HashMap();
    private String m_modelName;

    public MapTimeTableModel(LazyTable table) {
        super(table);
    }
    
    
    @Override
    public int getSubTaskId(int col) {
        return -1;
    }

    @Override
    public int getRowCount() {
        return m_mapTimes == null ? 0 : m_mapTimes.size();
    }

    @Override
    public int getColumnCount() {
        return m_columnNames.length;
    }
    
    @Override
    public String getColumnName(int col) {
        return  m_columnNames[col];
    }


    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        // Retrieve MapTime
        MapTime mapTime = m_mapTimes.get(rowIndex);

        switch (columnIndex) {
           case COLTYPE_TIME: {
                LazyData lazyData = getLazyData(rowIndex, columnIndex);
                lazyData.setData(mapTime.getTime());
                return lazyData;

            }
            case COLTYPE_DELTA_TIME: {
                LazyData lazyData = getLazyData(rowIndex, columnIndex);
                lazyData.setData(mapTime.getDeltaTime());
                return lazyData;

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
        return null;
    }

    @Override
    public TableCellRenderer getRenderer(int row, int col) {
        if (m_rendererMap.containsKey(col)) {
            return m_rendererMap.get(col);
        }

        TableCellRenderer renderer = null;

        switch (col) {
            case COLTYPE_TIME:{
                renderer = new TimeRenderer(new DefaultRightAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class)));
                break;
            }
            case COLTYPE_DELTA_TIME :{
                renderer = new DoubleRenderer( new DefaultRightAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class)) );
                break;
            }
        }
        
        m_rendererMap.put(col, renderer);
        return renderer;
    }

    @Override
    public String getDataColumnIdentifier(int columnIndex) {
        return getColumnName(columnIndex);
    }

    @Override
    public Class getDataColumnClass(int columnIndex) {
        switch (columnIndex){
            case COLTYPE_TIME:
            case COLTYPE_DELTA_TIME:
                return Double.class;
        }
        return null; // should not happen
    }

    @Override
    public Object getDataValueAt(int rowIndex, int columnIndex) {
        // Retrieve MapTime
        MapTime mapTime = m_mapTimes.get(rowIndex);

        switch (columnIndex) {
            case COLTYPE_TIME: {
                return mapTime.getTime() / 60;

            }
            case COLTYPE_DELTA_TIME: {
                return mapTime.getDeltaTime();

            }
        }
        return null; // should never happen
    }

    @Override
    public int[] getKeysColumn() {
        int[] keys = { COLTYPE_TIME };
        return keys;
    }

    @Override
    public int getInfoColumn() {
        return COLTYPE_TIME;
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
        PlotInformation plotInformation = new PlotInformation();
        plotInformation.setPlotColor(m_color);
        plotInformation.setPlotTitle(m_title);
        plotInformation.setDrawPoints(false);
        plotInformation.setDrawGap(true);
        return plotInformation;
    }

    @Override
    public void addFilters(LinkedHashMap<Integer, Filter> filtersMap) {
        filtersMap.put(COLTYPE_TIME, new DoubleFilter(getColumnName(COLTYPE_TIME), null, COLTYPE_TIME));
        filtersMap.put(COLTYPE_DELTA_TIME, new DoubleFilter(getColumnName(COLTYPE_DELTA_TIME), null, COLTYPE_DELTA_TIME));
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
        return PlotType.LINEAR_PLOT;
    }

    @Override
    public int getBestXAxisColIndex(PlotType plotType) {
        return COLTYPE_TIME;
    }

    @Override
    public int getBestYAxisColIndex(PlotType plotType) {
        return COLTYPE_DELTA_TIME;
    }

    @Override
    public String getExportRowCell(int row, int col) {
        return null; // no specific export
    }
    
    @Override
    public ArrayList<ExportFontData> getExportFonts(int row, int col) {
        return null;
    }

    @Override
    public String getExportColumnName(int col) {
        return m_columnNames[col];
    }
    
    public void setData(Long taskId, MapAlignment mapAlignment,  List<MapTime> mapTimes, Color color, String title) {
        m_mapAlignment = mapAlignment;
        m_mapTimes = mapTimes;
        m_color = color;
        m_title = title;

        m_taskId = taskId;

        fireTableDataChanged();
    }

    public void dataUpdated() {
        // no need to do an updateMinMax : scores are known at once
        fireTableDataChanged();

    }

    @Override
    public GlobalTableModelInterface getFrozzenModel() {
        return this;
    }
    
 
    @Override
    public ArrayList<ExtraDataType> getExtraDataTypes() {
        ArrayList<ExtraDataType> list = new ArrayList<>();
        list.add(new ExtraDataType(MapTime.class, true));
        registerSingleValuesAsExtraTypes(list);
        return list;
    }

    @Override
    public Object getValue(Class c) {
        return getSingleValue(c);
    }

    @Override
    public Object getRowValue(Class c, int row) {
        if (c.equals(MapTime.class)) {
            return m_mapTimes.get(row);
        }
        return null;
    }
    
    @Override
    public Object getColValue(Class c, int col) {
        return null;
    }
    

}
