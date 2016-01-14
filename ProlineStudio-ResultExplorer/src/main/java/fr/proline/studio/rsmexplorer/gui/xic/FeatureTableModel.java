package fr.proline.studio.rsmexplorer.gui.xic;

import fr.proline.core.orm.lcms.dto.DFeature;
import fr.proline.core.orm.uds.dto.DQuantitationChannel;
import fr.proline.studio.comparedata.ExtraDataType;
import fr.proline.studio.dam.tasks.xic.DatabaseLoadLcMSTask;
import fr.proline.studio.filter.ConvertValueInterface;
import fr.proline.studio.filter.DoubleFilter;
import fr.proline.studio.filter.Filter;
import fr.proline.studio.filter.IntegerFilter;
import fr.proline.studio.graphics.PlotInformation;
import fr.proline.studio.graphics.PlotType;
import fr.proline.studio.mzscope.MzScopeInterface;
import fr.proline.studio.mzscope.MzdbInfo;
import fr.proline.studio.table.renderer.BigFloatRenderer;
import fr.proline.studio.table.renderer.DefaultLeftAlignRenderer;
import fr.proline.studio.table.renderer.DefaultRightAlignRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.DoubleRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.FontRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.TimeRenderer;
import fr.proline.studio.table.CompoundTableModel;
import fr.proline.studio.table.GlobalTableModelInterface;
import fr.proline.studio.table.LazyData;
import fr.proline.studio.table.LazyTable;
import fr.proline.studio.table.LazyTableModel;
import fr.proline.studio.table.TableDefaultRendererManager;
import fr.proline.studio.utils.CyclicColorPalette;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author JM235353
 */
public class FeatureTableModel extends LazyTableModel implements GlobalTableModelInterface, MzScopeInterface {

    public static final int COLTYPE_FEATURE_ID = 0;
    public static final int COLTYPE_FEATURE_MAP_NAME = 1;
    public static final int COLTYPE_FEATURE_QC = 2;
    public static final int COLTYPE_FEATURE_MOZ = 3;
    public static final int COLTYPE_FEATURE_CHARGE = 4;
    public static final int COLTYPE_FEATURE_ELUTION_TIME = 5;
    public static final int COLTYPE_FEATURE_APEX_INTENSITY = 6;
    public static final int COLTYPE_FEATURE_INTENSITY = 7;
    public static final int COLTYPE_FEATURE_DURATION = 8;
    public static final int COLTYPE_FEATURE_QUALITY_SCORE = 9;
    public static final int COLTYPE_FEATURE_IS_OVERLAPPING = 10;
    public static final int COLTYPE_FEATURE_PREDICTED_ELUTION_TIME = 11;
    public static final int COLTYPE_FEATURE_PEAKELS_COUNT = 12;
    
    public final static String SERIALIZED_PROP_PREDICTED_ELUTION_TIME = "predicted_elution_time";
    public final static String SERIALIZED_PROP_PEAKELS_COUNT = "peakels_count";
    
    
    private static final String[] m_columnNames = {"Id", "Map", "Quant. Channel", "m/z", "Charge", "Elution Time (min)", "Apex Intensity", "Intensity", "Duration (sec)", "Quality Score", "Is Overlapping", "<html>Predicted<br/> El. Time (min)</html>", "#Peakels"};
    private static final String[] m_toolTipColumns = {"Feature Id","Map name","Quantitation Channel ",  "Mass to Charge Ratio", "Charge", "Elution Time (min)", "Apex Intensity", "Intensity", "Duration (sec)", "Quality Score", "Is Overlapping", "Predicted Elution time in min", "Peakels count"};
    private static final String[] m_columnNamesForExport = {"Id", "Map", "Quantitation Channel", "m/z", "Charge", "Elution Time (sec)", "Apex Intensity", "Intensity", "Duration (sec)", "Quality Score", "Is Overlapping", "Predicted Elution Time (sec)", "Peakels Count"};
    
    
    private List<DFeature> m_features = null;
    private QuantChannelInfo m_quantChannelInfo = null;
    private List<Boolean> m_featureHasPeak = null;

    private String m_modelName;

    public FeatureTableModel(LazyTable table) {
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
        return m_columnNamesForExport[col];
        
    }

    @Override
    public String getToolTipForHeader(int col) {
        return m_toolTipColumns[col];
    }

    @Override
    public Class getColumnClass(int col) {
        if (col == COLTYPE_FEATURE_ID) {
            return Long.class;
        }
        return LazyData.class;
    }

    @Override
    public int getSubTaskId(int col) {
        return DatabaseLoadLcMSTask.SUB_TASK_FEATURE;
    }

    @Override
    public int getRowCount() {
        if (m_features == null) {
            return 0;
        }

        return m_features.size();
    }

    
    @Override
    public Object getValueAt(int row, int col) {

        // Retrieve Feature
        DFeature feature = m_features.get(row);
        
        switch (col) {
            case COLTYPE_FEATURE_ID: {
                return feature.getId();
            }
            case COLTYPE_FEATURE_MAP_NAME: {
                LazyData lazyData = getLazyData(row, col);
                if (feature.getCharge() == null) {
                    lazyData.setData(null);
                    givePriorityTo(m_taskId, row, col);
                }else {
                    lazyData.setData(feature.getMap().getName());
                }
                return lazyData;
            }
            case COLTYPE_FEATURE_QC: {
                LazyData lazyData = getLazyData(row, col);
                if (feature.getCharge() == null) {
                    lazyData.setData(null);
                    givePriorityTo(m_taskId, row, col);
                }else {
                    String mapTitle = m_quantChannelInfo.getMapTitle(feature.getMap().getId());
                    Color mapColor = m_quantChannelInfo.getMapColor(feature.getMap().getId());
                    String rsmHtmlColor = CyclicColorPalette.getHTMLColor(mapColor);
        
                    StringBuilder sb = new StringBuilder();
                    sb.append("<html>");
                    sb.append("<html><font color='").append(rsmHtmlColor).append("'>&#x25A0;&nbsp;</font>");
                    sb.append(mapTitle);
                    sb.append("</html>");
                    lazyData.setData(sb.toString());
                }
                return lazyData;
            }
            case COLTYPE_FEATURE_MOZ: {
                LazyData lazyData = getLazyData(row, col);
                if (feature.getCharge() == null) {
                    lazyData.setData(null);
                    givePriorityTo(m_taskId, row, col);
                }else {
                    lazyData.setData(feature.getMoz());
                }
                return lazyData;

            }
            case COLTYPE_FEATURE_CHARGE: {
                LazyData lazyData = getLazyData(row, col);
                if (feature.getCharge() == null) {
                    lazyData.setData(null);
                    givePriorityTo(m_taskId, row, col);
                }else {
                    lazyData.setData(feature.getCharge());
                }
                return lazyData;

            }
            case COLTYPE_FEATURE_ELUTION_TIME: {
                LazyData lazyData = getLazyData(row, col);
                if (feature.getCharge() == null) {
                    lazyData.setData(null);
                    givePriorityTo(m_taskId, row, col);
                }else {
                    lazyData.setData(feature.getElutionTime());
                }
                return lazyData;

            }
            case COLTYPE_FEATURE_APEX_INTENSITY: {
                LazyData lazyData = getLazyData(row, col);
                if (feature.getCharge() == null) {
                    lazyData.setData(null);
                    givePriorityTo(m_taskId, row, col);
                }else {
                    lazyData.setData(feature.getApexIntensity());
                }
                return lazyData;

            }
            case COLTYPE_FEATURE_INTENSITY: {
                LazyData lazyData = getLazyData(row, col);
                if (feature.getCharge() == null) {
                    lazyData.setData(null);
                    givePriorityTo(m_taskId, row, col);
                }else {
                    lazyData.setData(feature.getIntensity());
                }
                return lazyData;

            }
            case COLTYPE_FEATURE_DURATION: {
                LazyData lazyData = getLazyData(row, col);
                if (feature.getCharge() == null) {
                    lazyData.setData(null);
                    givePriorityTo(m_taskId, row, col);
                }else {
                    lazyData.setData(feature.getDuration());
                }
                return lazyData;

            }
            case COLTYPE_FEATURE_QUALITY_SCORE: {
                LazyData lazyData = getLazyData(row, col);
                if (feature.getCharge() == null) {
                    lazyData.setData(null);
                    givePriorityTo(m_taskId, row, col);
                }else {
                    lazyData.setData(feature.getQualityScore());
                }
                return lazyData;

            }
            case COLTYPE_FEATURE_IS_OVERLAPPING: {
                LazyData lazyData = getLazyData(row, col);
                if (feature.getCharge() == null) {
                    lazyData.setData(null);
                    givePriorityTo(m_taskId, row, col);
                }else {
                    lazyData.setData(feature.getIsOverlapping()?"Yes":"No");
                }
                return lazyData;

            }
            case COLTYPE_FEATURE_PREDICTED_ELUTION_TIME: {
                LazyData lazyData = getLazyData(row, col);
                Map<String, Object> prop = null;
                try {
                    prop = feature.getSerializedPropertiesAsMap();
                } catch (Exception ex) {
                    
                }
                if (prop != null) {
                    Object o = prop.get(SERIALIZED_PROP_PREDICTED_ELUTION_TIME);
                    if (o != null && o instanceof Double) {
                        Double predictedElutionTime = (Double)o;
                        lazyData.setData(new Float(predictedElutionTime));
                    }else{
                        lazyData.setData(new Float(feature.getPredictedElutionTime()));
                    }
                }else{
                    lazyData.setData(new Float(feature.getPredictedElutionTime()));
                }
                return lazyData;
            }
            case COLTYPE_FEATURE_PEAKELS_COUNT:{
                LazyData lazyData = getLazyData(row, col);
                Map<String, Object> prop = null;
                try {
                    prop = feature.getSerializedPropertiesAsMap();
                } catch (Exception ex) {
                    
                }
                if (prop != null) {
                    Object o = prop.get(SERIALIZED_PROP_PEAKELS_COUNT);
                    if (o != null && o instanceof Integer) {
                        Integer peakelsCount = (Integer)o;
                        lazyData.setData(peakelsCount);
                    }else{
                        lazyData.setData(0);
                    }
                }else{
                    lazyData.setData(0);
                }
                return lazyData;
            }
        }
        return null; // should never happen
    }

    public void setData(Long taskId, List<DFeature> features, QuantChannelInfo quantChannelInfo, List<Boolean> featureHasPeak) {
        this.m_features = features;
        this.m_quantChannelInfo = quantChannelInfo;
        this.m_featureHasPeak = featureHasPeak ;

        m_taskId = taskId;

        fireTableDataChanged();

    }

    public void dataUpdated() {

        // no need to do an updateMinMax : scores are known at once
        fireTableDataChanged();

    }

    public DFeature getFeature(int i) {

        return m_features.get(i);
    }
    
    public Color getPlotColor(int i) {

        return m_quantChannelInfo.getMapColor(m_features.get(i).getMap().getId());
    }
    
    public String getPlotTitle(int i) {

        return m_quantChannelInfo.getMapTitle(m_features.get(i).getMap().getId());
    }

    public int findRow(long featureId) {

        int nb = m_features.size();
        for (int i = 0; i < nb; i++) {
            if (featureId == m_features.get(i).getId()) {
                return i;
            }
        }
        return -1;

    }

    public void sortAccordingToModel(ArrayList<Long> featureIds, CompoundTableModel compoundTableModel) {

        if (m_features == null) {
            // data not loaded 
            return;
        }

        HashSet<Long> featureIdMap = new HashSet<>(featureIds.size());
        featureIdMap.addAll(featureIds);

        int nb = m_table.getRowCount();
        int iCur = 0;
        for (int iView = 0; iView < nb; iView++) {
            int iModel = m_table.convertRowIndexToModel(iView);
            if (compoundTableModel != null) {
                iModel = compoundTableModel.convertCompoundRowToBaseModelRow(iModel);
            }
            // Retrieve Feature
            DFeature f = getFeature(iModel);
            if (featureIdMap.contains(f.getId())) {
                featureIds.set(iCur++, f.getId());
            }
        }

    }


    
    @Override
    public void addFilters(LinkedHashMap<Integer, Filter> filtersMap) {
        
        filtersMap.put(COLTYPE_FEATURE_MOZ, new DoubleFilter(getColumnName(COLTYPE_FEATURE_MOZ), null, COLTYPE_FEATURE_MOZ));
        filtersMap.put(COLTYPE_FEATURE_CHARGE, new IntegerFilter(getColumnName(COLTYPE_FEATURE_CHARGE), null, COLTYPE_FEATURE_CHARGE));
        
        ConvertValueInterface minuteConverter = new ConvertValueInterface() {
            @Override
            public Object convertValue(Object o) {
                if (o == null) {
                    return null;
                }
                return ((Float) o) / 60d;
            }

        };
        filtersMap.put(COLTYPE_FEATURE_ELUTION_TIME, new DoubleFilter(getColumnName(COLTYPE_FEATURE_ELUTION_TIME), minuteConverter, COLTYPE_FEATURE_ELUTION_TIME));
        filtersMap.put(COLTYPE_FEATURE_APEX_INTENSITY, new DoubleFilter(getColumnName(COLTYPE_FEATURE_APEX_INTENSITY), null, COLTYPE_FEATURE_APEX_INTENSITY));
        filtersMap.put(COLTYPE_FEATURE_INTENSITY, new DoubleFilter(getColumnName(COLTYPE_FEATURE_INTENSITY), null, COLTYPE_FEATURE_INTENSITY));
        filtersMap.put(COLTYPE_FEATURE_DURATION, new DoubleFilter(getColumnName(COLTYPE_FEATURE_DURATION), null, COLTYPE_FEATURE_DURATION));
        filtersMap.put(COLTYPE_FEATURE_QUALITY_SCORE, new DoubleFilter(getColumnName(COLTYPE_FEATURE_QUALITY_SCORE), null, COLTYPE_FEATURE_QUALITY_SCORE));
        filtersMap.put(COLTYPE_FEATURE_PREDICTED_ELUTION_TIME, new DoubleFilter(getColumnName(COLTYPE_FEATURE_PREDICTED_ELUTION_TIME), minuteConverter, COLTYPE_FEATURE_PREDICTED_ELUTION_TIME));
        filtersMap.put(COLTYPE_FEATURE_PEAKELS_COUNT, new IntegerFilter(getColumnName(COLTYPE_FEATURE_PEAKELS_COUNT), null, COLTYPE_FEATURE_PEAKELS_COUNT));

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
            case COLTYPE_FEATURE_ID:
                return Long.class;
            case COLTYPE_FEATURE_MAP_NAME:
            case COLTYPE_FEATURE_QC:
                return String.class;
            case COLTYPE_FEATURE_MOZ:
                return Double.class;
            case COLTYPE_FEATURE_ELUTION_TIME:
            case COLTYPE_FEATURE_APEX_INTENSITY:
            case COLTYPE_FEATURE_INTENSITY:
            case COLTYPE_FEATURE_DURATION:
            case COLTYPE_FEATURE_PREDICTED_ELUTION_TIME:
                return Float.class;
            case COLTYPE_FEATURE_IS_OVERLAPPING:
                return Boolean.class;
            case COLTYPE_FEATURE_CHARGE:
            case COLTYPE_FEATURE_PEAKELS_COUNT:
                return Integer.class;
        }
        return null; // should not happen
    }

    @Override
    public Object getDataValueAt(int rowIndex, int columnIndex) {

        // Retrieve Feature
        DFeature feature = m_features.get(rowIndex);

        switch (columnIndex) {
            case COLTYPE_FEATURE_ID: {
                return feature.getId();
            }
            case COLTYPE_FEATURE_MAP_NAME: {
                return feature.getMap().getName();
            }
            case COLTYPE_FEATURE_QC: {
               return m_quantChannelInfo.getMapTitle(feature.getMap().getId());
            }
            case COLTYPE_FEATURE_MOZ: {
                return feature.getMoz();

            }
            case COLTYPE_FEATURE_CHARGE: {
                return feature.getCharge();

            }
            case COLTYPE_FEATURE_ELUTION_TIME: {
                return feature.getElutionTime();

            }
            case COLTYPE_FEATURE_APEX_INTENSITY: {
                return feature.getApexIntensity();

            }
            case COLTYPE_FEATURE_INTENSITY: {
                return feature.getIntensity();

            }
            case COLTYPE_FEATURE_DURATION: {
                return feature.getDuration();

            }
            case COLTYPE_FEATURE_QUALITY_SCORE: {
                return feature.getQualityScore();

            }
            case COLTYPE_FEATURE_IS_OVERLAPPING: {
                return feature.getIsOverlapping();

            }
            case COLTYPE_FEATURE_PREDICTED_ELUTION_TIME: {
                Map<String, Object> prop = null;
                try {
                    prop = feature.getSerializedPropertiesAsMap();
                } catch (Exception ex) {
                    
                }
                if (prop != null) {
                    Object o = prop.get(SERIALIZED_PROP_PREDICTED_ELUTION_TIME);
                    if (o != null && o instanceof Double) {
                        return (Double)o;
                    }
                }
                return new Float(feature.getPredictedElutionTime());
            }
            case COLTYPE_FEATURE_PEAKELS_COUNT: {
                Map<String, Object> prop = null;
                try {
                    prop = feature.getSerializedPropertiesAsMap();
                } catch (Exception ex) {
                    
                }
                if (prop != null) {
                    Object o = prop.get(SERIALIZED_PROP_PEAKELS_COUNT);
                    if (o != null && o instanceof Integer) {
                        return (Integer)o;
                    }
                }
                return null;
            }
        }
        return null; // should never happen
    }

    @Override
    public int[] getKeysColumn() {
        int[] keys = { COLTYPE_FEATURE_ID };
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
        return PlotType.SCATTER_PLOT;
    }

    @Override
    public int getBestXAxisColIndex(PlotType plotType) {
        switch (plotType) {
            case HISTOGRAM_PLOT:
                return COLTYPE_FEATURE_INTENSITY;
            case SCATTER_PLOT:
                return COLTYPE_FEATURE_ELUTION_TIME;
        }
        return -1;
    }

    @Override
    public int getBestYAxisColIndex(PlotType plotType) {
        return COLTYPE_FEATURE_INTENSITY;
    }

    @Override
    public int getInfoColumn() {
        return COLTYPE_FEATURE_ID;
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
    public String getTootlTipValue(int row, int col) {
        if (m_features == null || row <0) {
            return "";
        }

        // Retrieve Feature
        DFeature feature = m_features.get(row);
        String mapTitle = m_quantChannelInfo.getMapTitle(feature.getMap().getId());
        Color mapColor = m_quantChannelInfo.getMapColor(feature.getMap().getId());
        String rsmHtmlColor = CyclicColorPalette.getHTMLColor(mapColor);
        
        StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        sb.append("<html><font color='").append(rsmHtmlColor).append("'>&#x25A0;&nbsp;</font>");
        sb.append(mapTitle);
        sb.append("</html>");
        return sb.toString();
    }

    @Override
    public String getExportRowCell(int row, int col) {

        // Retrieve Feature
        DFeature feature = m_features.get(row);

        switch (col) {
            case COLTYPE_FEATURE_ID: {
                return ""+feature.getId();
            }
            case COLTYPE_FEATURE_MAP_NAME: {
               if (feature.getCharge() == null) {
                    return "";
                }else {
                    return feature.getMap().getName();
                }
            }
            case COLTYPE_FEATURE_QC: {
               if (feature.getCharge() == null) {
                    return "";
                }else {
                    return m_quantChannelInfo.getMapTitle(feature.getMap().getId());
                }
            }
            case COLTYPE_FEATURE_MOZ: {
                if (feature.getCharge() == null) {
                    return "";
                }else {
                    return ""+feature.getMoz();
                }

            }
            case COLTYPE_FEATURE_CHARGE: {
                if (feature.getCharge() == null) {
                    return "";
                }else {
                    return ""+feature.getCharge();
                }

            }
            case COLTYPE_FEATURE_ELUTION_TIME: {
                if (feature.getCharge() == null) {
                    return "";
                }else {
                    return ""+feature.getElutionTime();
                }

            }
            case COLTYPE_FEATURE_APEX_INTENSITY: {
                if (feature.getCharge() == null) {
                    return "";
                }else {
                    return ""+feature.getApexIntensity();
                }

            }
            case COLTYPE_FEATURE_INTENSITY: {
                if (feature.getCharge() == null) {
                    return "";
                }else {
                    return ""+feature.getIntensity();
                }

            }
            case COLTYPE_FEATURE_DURATION: {
               if (feature.getCharge() == null) {
                    return "";
                }else {
                    return ""+feature.getDuration();
                }

            }
            case COLTYPE_FEATURE_QUALITY_SCORE: {
                if (feature.getCharge() == null) {
                    return "";
                }else {
                    return ""+feature.getQualityScore();
                }

            }
            case COLTYPE_FEATURE_IS_OVERLAPPING: {
                if (feature.getCharge() == null) {
                    return "";
                }else {
                    return (feature.getIsOverlapping()?"Yes":"No");
                }

            }
            case COLTYPE_FEATURE_PREDICTED_ELUTION_TIME: {
                Map<String, Object> prop = null;
                try {
                    prop = feature.getSerializedPropertiesAsMap();
                } catch (Exception ex) {
                    
                }
                if (prop != null) {
                    Object o = prop.get(SERIALIZED_PROP_PREDICTED_ELUTION_TIME);
                    if (o != null && o instanceof Double) {
                        return ((Double)o).toString();
                    }
                }
                return ""+feature.getPredictedElutionTime();
            }
            case COLTYPE_FEATURE_PEAKELS_COUNT: {
                Map<String, Object> prop = null;
                try {
                    prop = feature.getSerializedPropertiesAsMap();
                } catch (Exception ex) {
                    
                }
                if (prop != null) {
                    Object o = prop.get(SERIALIZED_PROP_PEAKELS_COUNT);
                    if (o != null && o instanceof Integer) {
                        return ((Integer)o).toString();
                    }
                }
                return ""+feature.getPredictedElutionTime();
            }
        }
        return ""; // should never happen
    }
    
    public List<Integer> getDefaultColumnsToHide() {
        List<Integer> listIds = new ArrayList();
        listIds.add(COLTYPE_FEATURE_PEAKELS_COUNT);
        listIds.add(COLTYPE_FEATURE_IS_OVERLAPPING);
        listIds.add(COLTYPE_FEATURE_QUALITY_SCORE);
        return listIds; 
    }
    
    
    private Boolean hasFeaturePeaks(int row) {

        // Retrieve Feature
        return m_featureHasPeak.get(row);
    }
    
    public Boolean isInItalic(int row, int col) {
        //return !hasFeaturePeaks(row);
        // Retrieve Feature
        DFeature feature = m_features.get(row);
        if (feature.getId() == -1){
            // fake feature
            return true;
        }else{
            if (col == COLTYPE_FEATURE_PREDICTED_ELUTION_TIME){
                Map<String, Object> prop = null;
                try {
                    prop = feature.getSerializedPropertiesAsMap();
                } catch (Exception ex) {
                prop = null;     
                }
                if (prop != null) {
                    Object o = prop.get(SERIALIZED_PROP_PREDICTED_ELUTION_TIME);
                    if (o != null && o instanceof Double) {
                        return false;
                    }else {
                        return true;
                    }
                }else{
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public List<MzdbInfo> getMzdbInfo() {
        List<MzdbInfo> mzdbInfos = new ArrayList();
        List<String> fileNameList = new ArrayList();
        for (DFeature feature : m_features) {
            DQuantitationChannel qc = m_quantChannelInfo.getQuantChannelForMap(feature.getMap().getId());
            if (qc != null) {
                if (qc.getMzdbFileName() != null) {
                    String fn = qc.getMzdbFileName();
                    MzdbInfo mzdbInfo;
                    if (feature.getId() == -1){ // fake feature added to display predictedElutionTime
                        mzdbInfo = new MzdbInfo(fn, Double.valueOf(feature.getMoz()), Double.valueOf(feature.getPredictedElutionTime()), 
                            0.0, 0.0);
                    }else{
                        mzdbInfo = new MzdbInfo(fn, Double.valueOf(feature.getMoz()), Double.valueOf(feature.getElutionTime()), 
                            Double.valueOf(feature.getFirstScan().getTime()), Double.valueOf(feature.getLastScan().getTime()));
                    }
                    if (!fileNameList.contains(fn)) {
                        mzdbInfos.add(mzdbInfo);
                        fileNameList.add(fn);
                    }
                }
            }
        }
        
        return mzdbInfos;
    }

    @Override
    public TableCellRenderer getRenderer(int row, int col) {

        if (m_rendererMap.containsKey(col)) {
            return m_rendererMap.get(col);
        }

        TableCellRenderer renderer = null;
        
        switch (col) {
            case COLTYPE_FEATURE_MAP_NAME: {
                renderer = new FontRenderer( new DefaultLeftAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class)) );
                break;
            }
            case COLTYPE_FEATURE_QC:
            case COLTYPE_FEATURE_IS_OVERLAPPING: {
                renderer = new FontRenderer( new DefaultLeftAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class)));
                break;
            }
            case COLTYPE_FEATURE_MOZ: {
                renderer = new FontRenderer( new DoubleRenderer( new DefaultRightAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class)), 4 ));
                break;
            }
            case COLTYPE_FEATURE_APEX_INTENSITY:
            case COLTYPE_FEATURE_INTENSITY:
            case COLTYPE_FEATURE_DURATION: {
                renderer = new FontRenderer( new BigFloatRenderer( new DefaultRightAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class)), 0 ));
                break;
            }
            case COLTYPE_FEATURE_ELUTION_TIME:
            case COLTYPE_FEATURE_PREDICTED_ELUTION_TIME: {
                renderer = new FontRenderer( new TimeRenderer(new DefaultRightAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class))));
                break;
            }
            case COLTYPE_FEATURE_CHARGE:
            case COLTYPE_FEATURE_PEAKELS_COUNT: {
                renderer = new FontRenderer( new DefaultRightAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(Integer.class)));
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
        list.add(new ExtraDataType(DFeature.class, true));
        list.add(new ExtraDataType(QuantChannelInfo.class, false));
        registerSingleValuesAsExtraTypes(list);
        return list;
    }

    @Override
    public Object getValue(Class c) {
        if (c.equals(QuantChannelInfo.class)) {
            return m_quantChannelInfo;
        }
        return getSingleValue(c);
    }

    @Override
    public Object getValue(Class c, int row) {
        if (c.equals(DFeature.class)) {
            return m_features.get(row);
        }
        return null;
    }

}
