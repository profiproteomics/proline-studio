package fr.proline.studio.rsmexplorer.gui.xic;

import fr.proline.core.orm.lcms.Feature;
import fr.proline.core.orm.lcms.MapTime;
import fr.proline.core.orm.lcms.Peakel;
import fr.proline.core.orm.lcms.Peak;
import fr.proline.studio.comparedata.CompareDataInterface;
import fr.proline.studio.comparedata.ExtraDataType;
import fr.proline.studio.filter.DoubleFilter;
import fr.proline.studio.filter.Filter;
import fr.proline.studio.graphics.PlotInformation;
import fr.proline.studio.graphics.PlotType;
import fr.proline.studio.table.renderer.DefaultRightAlignRenderer;
import fr.proline.studio.table.renderer.DoubleRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.FloatRenderer;
import fr.proline.studio.table.GlobalTableModelInterface;
import fr.proline.studio.table.LazyData;
import fr.proline.studio.table.LazyTable;
import fr.proline.studio.table.LazyTableModel;
import fr.proline.studio.table.TableDefaultRendererManager;
import fr.proline.studio.utils.DataFormat;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author JM235353
 */
public class PeakTableModel extends LazyTableModel implements GlobalTableModelInterface {

    public static final int COLTYPE_PEAK_MOZ = 0;
    public static final int COLTYPE_PEAK_ELUTION_TIME = 1;
    public static final int COLTYPE_PEAK_INTENSITY = 2;
    
    
    private static final String[] m_columnNames = {"m/z", "Elution Time (min)", "Intensity"};
    private static final String[] m_toolTipColumns = {"Mass to Charge Ratio", "Elution Time (min)", "Intensity"};

    private Feature m_feature = null;
    private Peakel m_peakel;
    private List<Peak> m_peaks = null;
    private Color m_color = null;
    private String m_title = null;
    private Integer m_isotopeIndex = null;


    
    private String m_modelName;

    public PeakTableModel(LazyTable table) {
        super(table);
    }

    @Override
    public int getColumnCount() {
       return m_columnNames.length;
    }

    @Override
    public String getColumnName(int col) {
        return m_columnNames[col];
    }
    
    @Override
    public String getExportColumnName(int col) {
        return m_columnNames[col];
        
    }
    
    @Override
    public String getExportRowCell(int row, int col) {
        return null; // no specific export
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
    public Class getColumnClass(int col) {
       return LazyData.class;
    }

    @Override
    public int getSubTaskId(int col) {
        return -1;
    }

    @Override
    public int getRowCount() {
        if (m_peaks == null) {
            return 0;
        }

        return m_peaks.size();
    }

    
    @Override
    public Object getValueAt(int row, int col) {

        // Retrieve Peak
        Peak peak = m_peaks.get(row);

        switch (col) {
           case COLTYPE_PEAK_MOZ: {
                LazyData lazyData = getLazyData(row, col);
                lazyData.setData(peak.getMoz());
                return lazyData;

            }
            case COLTYPE_PEAK_ELUTION_TIME: {
                LazyData lazyData = getLazyData(row, col);
                lazyData.setData(peak.getElutionTime());
                return lazyData;

            }
            case COLTYPE_PEAK_INTENSITY: {
                LazyData lazyData = getLazyData(row, col);
                lazyData.setData(peak.getIntensity());
                return lazyData;

            }
        }
        return null; // should never happen
    }

    public void setData(Long taskId, Feature feature, Peakel peakel, Integer isotopeIndex, List<Peak> peaks, Color color, String title) {
        m_peaks = peaks;
        m_feature = feature;
        m_peakel = peakel;
        m_isotopeIndex = isotopeIndex ;
        m_color = color;
        m_title = title;

        m_taskId = taskId;

        fireTableDataChanged();

    }

    public void dataUpdated() {

        // no need to do an updateMinMax : scores are known at once
        fireTableDataChanged();

    }

    public Peak getPeak(int i) {

        return m_peaks.get(i);
    }

    public int findRow(float elutionTime) {

        int nb = m_peaks.size();
        for (int i = 0; i < nb; i++) {
            if (elutionTime == m_peaks.get(i).getElutionTime()) {
                return i;
            }
        }
        return -1;

    }


    @Override
    public void addFilters(LinkedHashMap<Integer, Filter> filtersMap) {
        filtersMap.put(COLTYPE_PEAK_MOZ, new DoubleFilter(getColumnName(COLTYPE_PEAK_MOZ), null, COLTYPE_PEAK_MOZ));
        filtersMap.put(COLTYPE_PEAK_ELUTION_TIME, new DoubleFilter(getColumnName(COLTYPE_PEAK_ELUTION_TIME), null, COLTYPE_PEAK_ELUTION_TIME));
        filtersMap.put(COLTYPE_PEAK_INTENSITY, new DoubleFilter(getColumnName(COLTYPE_PEAK_INTENSITY), null, COLTYPE_PEAK_INTENSITY));
    }
    


    @Override
    public int getLoadingPercentage() {
        return m_table.getLoadingPercentage();
    }

    @Override
    public boolean isLoaded() {
        return m_table.isLoaded();
    }
    

    
    @Override
    public String getDataColumnIdentifier(int columnIndex) {
        return getColumnName(columnIndex);
    }

    @Override
    public Class getDataColumnClass(int columnIndex) {
        switch (columnIndex){
            case COLTYPE_PEAK_MOZ:
                return Double.class;
            case COLTYPE_PEAK_ELUTION_TIME:
            case COLTYPE_PEAK_INTENSITY:
                return Float.class;
        }
        return null; // should not happen
    }

    @Override
    public Object getDataValueAt(int rowIndex, int columnIndex) {

        // Retrieve Peak
        Peak peak = m_peaks.get(rowIndex);

        switch (columnIndex) {
            case COLTYPE_PEAK_MOZ: {
                return peak.getMoz();

            }
            case COLTYPE_PEAK_ELUTION_TIME: {
                return peak.getElutionTime()/60;

            }
            case COLTYPE_PEAK_INTENSITY: {
                return peak.getIntensity();

            }
        }
        return null; // should never happen
    }

    @Override
    public int[] getKeysColumn() {
        int[] keys = { COLTYPE_PEAK_ELUTION_TIME };
        return keys;
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
    public PlotType getBestPlotType() {
        return PlotType.LINEAR_PLOT;
    }

    @Override
    public int getBestXAxisColIndex(PlotType plotType) {
        switch (plotType) {
            case HISTOGRAM_PLOT:
                return COLTYPE_PEAK_INTENSITY;
            case SCATTER_PLOT:
                return COLTYPE_PEAK_ELUTION_TIME;
            case LINEAR_PLOT:
                return COLTYPE_PEAK_ELUTION_TIME;
        }
        return -1;
    }

    @Override
    public int getBestYAxisColIndex(PlotType plotType) {
        return COLTYPE_PEAK_INTENSITY;
    }

    @Override
    public int getInfoColumn() {
        return COLTYPE_PEAK_ELUTION_TIME;
    }
    
    @Override
    public Map<String, Object> getExternalData() {
        Map<String, Object> externalData = new HashMap<>();
        List<Double> scanValues = new ArrayList();
        List<String> scanText = new ArrayList();
        List<Color> scanColor = new ArrayList();
        if(m_feature != null) {
            if (m_feature.getFirstScan() != null) {
                scanValues.add(new Double(m_feature.getFirstScan().getTime()/60));
                scanText.add(" First Scan: ");
                scanColor.add(m_color);
            }
            if (m_feature.getLastScan() != null) {
                scanValues.add(new Double(m_feature.getLastScan().getTime()/60));
                scanText.add(" Last Scan: ");
                scanColor.add(m_color);
            }
        } else if (m_peakel != null) {
            if (m_peakel.getFirstScan() != null) {
                scanValues.add(new Double(m_peakel.getFirstScan().getTime()/60));
                scanText.add("First Scan: ");
                scanColor.add(m_color);
            }
            if (m_peakel.getLastScan() != null) {
                scanValues.add(new Double(m_peakel.getLastScan().getTime()/60));
                scanText.add("Last Scan: ");
                scanColor.add(m_color);
            }
        }
        externalData.put(CompareDataInterface.EXTERNAL_DATA_VERTICAL_MARKER_VALUE, scanValues);
        externalData.put(CompareDataInterface.EXTERNAL_DATA_VERTICAL_MARKER_TEXT, scanText);
        externalData.put(CompareDataInterface.EXTERNAL_DATA_VERTICAL_MARKER_COLOR, scanColor);
        return externalData;
    }
    
    @Override
    public PlotInformation getPlotInformation() {
        PlotInformation plotInformation = new PlotInformation();
        plotInformation.setPlotColor(m_color);
        plotInformation.setPlotTitle(m_title);
        plotInformation.setDrawPoints(false);
        plotInformation.setDrawGap(true);
        HashMap<String, String> plotInfo = new HashMap();
        plotInfo.put("Apex Int.", DataFormat.formatWithGroupingSep(m_peakel.getApexIntensity(), 0));
        plotInfo.put("Isotope index", Integer.toString(m_isotopeIndex));
        plotInformation.setPlotInfo(plotInfo);
        return plotInformation;
    }

    @Override
    public TableCellRenderer getRenderer(int row, int col) {

        if (m_rendererMap.containsKey(col)) {
            return m_rendererMap.get(col);
        }

        TableCellRenderer renderer = null;

        switch (col) {
            case COLTYPE_PEAK_MOZ: {
                renderer = new DoubleRenderer( new DefaultRightAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class)) );
                break;
            }
            case COLTYPE_PEAK_ELUTION_TIME:
            case COLTYPE_PEAK_INTENSITY: {
                renderer = new FloatRenderer( new DefaultRightAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class)) );
                break;
            }
        }
        
        m_rendererMap.put(col, renderer);
        return renderer;
    }
    private final HashMap<Integer, TableCellRenderer> m_rendererMap = new HashMap();

    @Override
    public GlobalTableModelInterface getFrozzenModel() {
        return this;
    }

    @Override
    public ArrayList<ExtraDataType> getExtraDataTypes() {
        ArrayList<ExtraDataType> list = new ArrayList<>();
        list.add(new ExtraDataType(Peak.class, true));
        registerSingleValuesAsExtraTypes(list);
        return list;
    }

    @Override
    public Object getValue(Class c) {
        return getSingleValue(c);
    }

    @Override
    public Object getRowValue(Class c, int row) {
        if (c.equals(Peak.class)) {
            return m_peaks.get(row);
        }
        return null;
    }
    
    @Override
    public Object getColValue(Class c, int col) {
        return null;
    }

}
