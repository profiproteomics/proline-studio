package fr.proline.mzscope.ui.model;

import fr.profi.mzdb.model.Feature;
import fr.proline.mzscope.model.IFeature;
import fr.proline.mzscope.utils.NumberFormatter;
import fr.proline.studio.comparedata.ExtraDataType;
import fr.proline.studio.export.ExportModelUtilities;
import fr.proline.studio.export.ExportFontData;
import fr.proline.studio.filter.DoubleFilter;
import fr.proline.studio.filter.Filter;
import fr.proline.studio.filter.IntegerFilter;
import fr.proline.studio.filter.StringFilter;
import fr.proline.studio.graphics.PlotInformation;
import fr.proline.studio.graphics.PlotType;
import fr.proline.studio.table.DecoratedTableModel;
import fr.proline.studio.table.GlobalTableModelInterface;
import fr.proline.studio.table.LazyData;
import fr.proline.studio.table.TableDefaultRendererManager;
import fr.proline.studio.table.renderer.BigFloatOrDoubleRenderer;
import fr.proline.studio.table.renderer.DefaultLeftAlignRenderer;
import fr.proline.studio.table.renderer.DefaultRightAlignRenderer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JLabel;
import javax.swing.table.TableCellRenderer;
import org.jdesktop.swingx.renderer.DefaultTableRenderer;

/**
 * table model for m_features / peaks
 * @author CB205360
 */
public class FeaturesTableModel extends DecoratedTableModel implements GlobalTableModelInterface {
    
    public static final int COLTYPE_FEATURE_MZCOL = 0;
    public static final int COLTYPE_FEATURE_ET_COL = 1;
    public static final int COLTYPE_FEATURE_DURATION_COL = 2;
    public static final int COLTYPE_FEATURE_APEX_INT_COL = 3;
    public static final int COLTYPE_FEATURE_AREA_COL = 4;
    public static final int COLTYPE_FEATURE_SCAN_COUNT_COL = 5;
    public static final int COLTYPE_FEATURE_CHARGE_COL = 6;
    public static final int COLTYPE_FEATURE_PEAKELS_COUNT_COL = 7;
    public static final int COLTYPE_FEATURE_RAWFILE = 8;
    
    
   private static final String[] m_columnNames = {"m/z", "Elution","Duration", "Apex Int.", "Area", "MS Count", "Charge", "Peakels", "Raw File"};
   private static final String[] m_columnTooltips = {"m/z", "Elution Time in min","Duration in min", "Apex Intensity", "Area", "MS Count", "Charge state", "Isotope peakels count", "Raw File Name"};
    
   private final HashMap<Integer, TableCellRenderer> m_rendererMap = new HashMap();

    private List<IFeature> m_features = new ArrayList<>();
    
    private String m_modelName;

    public void setFeatures(List<IFeature> features) {
        m_features = features;
        fireTableDataChanged();
    }
    
    public void addFeatures(List<IFeature> features) {
        m_features.addAll(features);
        fireTableDataChanged();
    }
    

   @Override
    public int getRowCount() {
        return m_features.size();
    }

   @Override
    public int getColumnCount() {
        return m_columnNames.length;
    }

   @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        switch (columnIndex){
            case COLTYPE_FEATURE_MZCOL:
                return m_features.get(rowIndex).getMz();
            case COLTYPE_FEATURE_ET_COL:
                return m_features.get(rowIndex).getElutionTime()/60.0;
            case COLTYPE_FEATURE_DURATION_COL:
                return m_features.get(rowIndex).getDuration()/60.0;
            case COLTYPE_FEATURE_APEX_INT_COL:
                return m_features.get(rowIndex).getApexIntensity();
            case COLTYPE_FEATURE_AREA_COL:
                return m_features.get(rowIndex).getArea();
            case COLTYPE_FEATURE_SCAN_COUNT_COL:
                return m_features.get(rowIndex).getScanCount();
            case COLTYPE_FEATURE_CHARGE_COL:
               return m_features.get(rowIndex).getCharge();
            case COLTYPE_FEATURE_PEAKELS_COUNT_COL:
               return m_features.get(rowIndex).getPeakelsCount();
            case COLTYPE_FEATURE_RAWFILE: 
                return m_features.get(rowIndex).getRawFile().getName();
        }
        return null; // should not happen
    }

    @Override
    public String getColumnName(int column) {
        return m_columnNames[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex){
            case COLTYPE_FEATURE_MZCOL:
                return Double.class;
            case COLTYPE_FEATURE_ET_COL:
            case COLTYPE_FEATURE_DURATION_COL:
            case COLTYPE_FEATURE_APEX_INT_COL:
            case COLTYPE_FEATURE_AREA_COL:
                return Float.class;
            case COLTYPE_FEATURE_SCAN_COUNT_COL:
            case COLTYPE_FEATURE_CHARGE_COL:
            case COLTYPE_FEATURE_PEAKELS_COUNT_COL:
                return Integer.class;
            case COLTYPE_FEATURE_RAWFILE: 
                return String.class;
        }
        return null; // should not happen
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
    public Long getTaskId() {
        return -1l; // not used
    }

    @Override
    public LazyData getLazyData(int row, int col) {
        return null; // not used
    }

    @Override
    public void givePriorityTo(Long taskId, int row, int col) {
        // not used
    }

    @Override
    public void sortingChanged(int col) {
        // not used
    }

    @Override
    public int getSubTaskId(int col) {
        return -1; // not used
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
        int[] keys = { COLTYPE_FEATURE_MZCOL, COLTYPE_FEATURE_ET_COL, };
        return keys;
    }

    @Override
    public int getInfoColumn() {
        return COLTYPE_FEATURE_MZCOL;
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
        filtersMap.put(COLTYPE_FEATURE_MZCOL, new DoubleFilter(getColumnName(COLTYPE_FEATURE_MZCOL), null, COLTYPE_FEATURE_MZCOL));
        filtersMap.put(COLTYPE_FEATURE_ET_COL, new DoubleFilter(getColumnName(COLTYPE_FEATURE_ET_COL), null, COLTYPE_FEATURE_ET_COL));
        filtersMap.put(COLTYPE_FEATURE_DURATION_COL, new DoubleFilter(getColumnName(COLTYPE_FEATURE_DURATION_COL), null, COLTYPE_FEATURE_DURATION_COL));
        filtersMap.put(COLTYPE_FEATURE_APEX_INT_COL, new DoubleFilter(getColumnName(COLTYPE_FEATURE_APEX_INT_COL), null, COLTYPE_FEATURE_APEX_INT_COL));
        filtersMap.put(COLTYPE_FEATURE_AREA_COL, new DoubleFilter(getColumnName(COLTYPE_FEATURE_AREA_COL), null, COLTYPE_FEATURE_AREA_COL));
        filtersMap.put(COLTYPE_FEATURE_SCAN_COUNT_COL, new IntegerFilter(getColumnName(COLTYPE_FEATURE_SCAN_COUNT_COL), null, COLTYPE_FEATURE_SCAN_COUNT_COL));
        filtersMap.put(COLTYPE_FEATURE_PEAKELS_COUNT_COL, new IntegerFilter(getColumnName(COLTYPE_FEATURE_PEAKELS_COUNT_COL), null, COLTYPE_FEATURE_PEAKELS_COUNT_COL));
        filtersMap.put(COLTYPE_FEATURE_CHARGE_COL, new IntegerFilter(getColumnName(COLTYPE_FEATURE_CHARGE_COL), null, COLTYPE_FEATURE_CHARGE_COL));
        filtersMap.put(COLTYPE_FEATURE_RAWFILE, new StringFilter(getColumnName(COLTYPE_FEATURE_RAWFILE), null, COLTYPE_FEATURE_RAWFILE));
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
        return ExportModelUtilities.getExportRowCell(this, row, col);
    }
    
    @Override
    public ArrayList<ExportFontData> getExportFonts(int row, int col) {
        return null;
    }

    @Override
    public String getExportColumnName(int col) {
        return getColumnName(col);
    }

    @Override
    public TableCellRenderer getRenderer(int row, int col) {
        if (m_rendererMap.containsKey(col)) {
            return m_rendererMap.get(col);
        }

        TableCellRenderer renderer = null;
        
        switch (col) {
            case COLTYPE_FEATURE_MZCOL:
                renderer = new DefaultTableRenderer(new NumberFormatter("#.00000"), JLabel.RIGHT);
                break;
            case COLTYPE_FEATURE_ET_COL:
            case COLTYPE_FEATURE_DURATION_COL:
                renderer = new DefaultTableRenderer(new NumberFormatter("#0.00"), JLabel.RIGHT);
                break;
            case COLTYPE_FEATURE_APEX_INT_COL:
            case COLTYPE_FEATURE_AREA_COL:
                renderer = new BigFloatOrDoubleRenderer( new DefaultRightAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class)), 0 );
                break;
            case COLTYPE_FEATURE_RAWFILE: {
                renderer = new DefaultLeftAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class)) ;
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
    public ArrayList<ExtraDataType> getExtraDataTypes() {
        ArrayList<ExtraDataType> list = new ArrayList<>();
        list.add(new ExtraDataType(Feature.class, true));
        registerSingleValuesAsExtraTypes(list);
        return list;
    }

    @Override
    public Object getValue(Class c) {
        return getSingleValue(c);
    }

    @Override
    public Object getRowValue(Class c, int row) {
        if (c.equals(Feature.class)) {
            return m_features.get(row);
        }
        return null;
    }
    
    @Override
    public Object getColValue(Class c, int col) {
        return null;
    }
    
    
}
