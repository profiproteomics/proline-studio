package fr.proline.studio.rsmexplorer.gui.xic;

import fr.proline.core.orm.lcms.Feature;
import fr.proline.studio.comparedata.CompareDataInterface;
import fr.proline.studio.dam.tasks.xic.DatabaseLoadLcMSTask;
import fr.proline.studio.export.ExportColumnTextInterface;
import fr.proline.studio.export.ExportRowTextInterface;
import fr.proline.studio.filter.DoubleFilter;
import fr.proline.studio.filter.Filter;
import fr.proline.studio.filter.IntegerFilter;
import fr.proline.studio.graphics.BestGraphicsInterface;
import fr.proline.studio.graphics.PlotInformation;
import fr.proline.studio.graphics.PlotType;
import fr.proline.studio.table.ExportTableSelectionInterface;
import fr.proline.studio.table.LazyData;
import fr.proline.studio.table.LazyTable;
import fr.proline.studio.table.LazyTableModel;
import fr.proline.studio.utils.CyclicColorPalette;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 *
 * @author JM235353
 */
public class FeatureTableModel extends LazyTableModel implements ExportTableSelectionInterface, ExportColumnTextInterface, CompareDataInterface, BestGraphicsInterface,  ExportRowTextInterface {

    public static final int COLTYPE_FEATURE_ID = 0;
    public static final int COLTYPE_FEATURE_MAP_NAME = 1;
    public static final int COLTYPE_FEATURE_MOZ = 2;
    public static final int COLTYPE_FEATURE_CHARGE = 3;
    public static final int COLTYPE_FEATURE_ELUTION_TIME = 4;
    public static final int COLTYPE_FEATURE_APEX_INTENSITY = 5;
    public static final int COLTYPE_FEATURE_INTENSITY = 6;
    public static final int COLTYPE_FEATURE_DURATION = 7;
    public static final int COLTYPE_FEATURE_QUALITY_SCORE = 8;
    public static final int COLTYPE_FEATURE_IS_OVERLAPPING = 9;
    
    
    
    private static final String[] m_columnNames = {"Id", "Map", "m/z", "Charge", "Elution Time (min)", "Apex Intensity", "Intensity", "Duration (sec)", "Quality Score", "Is Overlapping"};
    private static final String[] m_toolTipColumns = {"Feature Id","Map name", "Mass to Charge Ratio", "Charge", "Elution Time (min)", "Apex Intensity", "Intensity", "Duration (sec)", "Quality Score", "Is Overlapping"};
    private static final String[] m_columnNamesForExport = {"Id", "Map", "m/z", "Charge", "Elution Time (sec)", "Apex Intensity", "Intensity", "Duration (sec)", "Quality Score", "Is Overlapping"};
    
    
    private List<Feature> m_features = null;
    private QuantChannelInfo m_quantChannelInfo = null;

    private ArrayList<Integer> m_filteredIds = null;
    private boolean m_isFiltering = false;
    private boolean m_filteringAsked = false;
    
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
        if ((!m_isFiltering) && (m_filteredIds != null)) {
            return m_filteredIds.size();
        }
        return m_features.size();
    }

    
    @Override
    public Object getValueAt(int row, int col) {

        int rowFiltered = row;
        if ((!m_isFiltering) && (m_filteredIds != null)) {
            rowFiltered = m_filteredIds.get(row).intValue();
        }
        // Retrieve Feature
        Feature feature = m_features.get(rowFiltered);

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
        }
        return null; // should never happen
    }

    public void setData(Long taskId, List<Feature> features, QuantChannelInfo quantChannelInfo) {
        this.m_features = features;
        this.m_quantChannelInfo = quantChannelInfo;
        m_filteredIds = null;
        m_isFiltering = false;

        m_taskId = taskId;

        if (m_filteringAsked) {
            m_filteringAsked = false;
            filter();
        } else {
            fireTableDataChanged();
        }

    }

    public void dataUpdated() {

        // no need to do an updateMinMax : scores are known at once
        if (m_filteredIds != null) {
            filter();
        } else {
            fireTableDataChanged();
        }
    }

    public Feature getFeature(int i) {

        if (m_filteredIds != null) {
            i = m_filteredIds.get(i).intValue();
        }

        return m_features.get(i);
    }
    
    public Color getPlotColor(int i) {

        if (m_filteredIds != null) {
            i = m_filteredIds.get(i).intValue();
        }

        return m_quantChannelInfo.getMapColor(m_features.get(i).getMap().getId());
    }
    
    public String getPlotTitle(int i) {

        if (m_filteredIds != null) {
            i = m_filteredIds.get(i).intValue();
        }

        return m_quantChannelInfo.getMapTitle(m_features.get(i).getMap().getId());
    }

    public int findRow(long featureId) {

        if (m_filteredIds != null) {
            int nb = m_filteredIds.size();
            for (int i = 0; i < nb; i++) {
                if (featureId == m_features.get(m_filteredIds.get(i)).getId()) {
                    return i;
                }
            }
            return -1;
        }

        int nb = m_features.size();
        for (int i = 0; i < nb; i++) {
            if (featureId == m_features.get(i).getId()) {
                return i;
            }
        }
        return -1;

    }

    public void sortAccordingToModel(ArrayList<Long> featureIds) {

        if (m_features == null) {
            // data not loaded 
            return;
        }

        HashSet<Long> featureIdMap = new HashSet<>(featureIds.size());
        featureIdMap.addAll(featureIds);

        int nb = getRowCount();
        int iCur = 0;
        for (int iView = 0; iView < nb; iView++) {
            int iModel = m_table.convertRowIndexToModel(iView);
            // Retrieve Feature
            Feature f = getFeature(iModel);
            if (featureIdMap.contains(f.getId())) {
                featureIds.set(iCur++, f.getId());
            }
        }

        // need to refilter
        if (m_filteredIds != null) { // NEEDED ????
            filter();
        }
    }

    @Override
    public void filter() {

        if (m_features == null) {
            // filtering not possible for the moment
            m_filteringAsked = true;
            return;
        }

        m_isFiltering = true;
        try {

            int nbData = m_features.size();
            if (m_filteredIds == null) {
                m_filteredIds = new ArrayList<>(nbData);
            } else {
                m_filteredIds.clear();
            }

            for (int i = 0; i < nbData; i++) {
                if (!filter(i)) {
                    continue;
                }
                m_filteredIds.add(Integer.valueOf(i));
            }

        } finally {
            m_isFiltering = false;
        }
        fireTableDataChanged();
    }

    @Override
    public boolean filter(int row, int col) {
        Filter filter = getColumnFilter(col);
        if ((filter == null) || (!filter.isUsed())) {
            return true;
        }

        Object data = ((LazyData) getValueAt(row, col)).getData();
        if (data == null) {
            return true; // should not happen
        }

        switch (col) {
            case COLTYPE_FEATURE_MOZ:
            case COLTYPE_FEATURE_ELUTION_TIME:
            case COLTYPE_FEATURE_APEX_INTENSITY:
            case COLTYPE_FEATURE_INTENSITY:
            case COLTYPE_FEATURE_DURATION:
            case COLTYPE_FEATURE_QUALITY_SCORE:
            {
                return ((DoubleFilter) filter).filter((Double) data);
            }
            case COLTYPE_FEATURE_CHARGE:
            {
                return ((IntegerFilter) filter).filter((Integer) data);
            }
        }

        return true; // should never happen
    }

    @Override
    public void initFilters() {
        if (m_filters == null) {
            int nbCol = getColumnCount();
            m_filters = new Filter[nbCol];
            m_filters[COLTYPE_FEATURE_ID] = null;
            m_filters[COLTYPE_FEATURE_MOZ] = new DoubleFilter(getColumnName(COLTYPE_FEATURE_MOZ));
            m_filters[COLTYPE_FEATURE_CHARGE] = new IntegerFilter(getColumnName(COLTYPE_FEATURE_CHARGE));
            m_filters[COLTYPE_FEATURE_ELUTION_TIME] = new DoubleFilter(getColumnName(COLTYPE_FEATURE_ELUTION_TIME));
            m_filters[COLTYPE_FEATURE_APEX_INTENSITY] = new DoubleFilter(getColumnName(COLTYPE_FEATURE_ELUTION_TIME));
            m_filters[COLTYPE_FEATURE_INTENSITY] = new DoubleFilter(getColumnName(COLTYPE_FEATURE_ELUTION_TIME));
            m_filters[COLTYPE_FEATURE_DURATION] = new DoubleFilter(getColumnName(COLTYPE_FEATURE_ELUTION_TIME));
            m_filters[COLTYPE_FEATURE_QUALITY_SCORE] = new DoubleFilter(getColumnName(COLTYPE_FEATURE_ELUTION_TIME));
        }
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
    public HashSet exportSelection(int[] rows) {

        int nbRows = rows.length;
        HashSet selectedObjects = new HashSet();
        for (int i = 0; i < nbRows; i++) {

            int row = rows[i];
            int rowFiltered = row;
            if ((!m_isFiltering) && (m_filteredIds != null)) {
                rowFiltered = m_filteredIds.get(row).intValue();
            }

            // Retrieve Feature
            Feature feature = m_features.get(rowFiltered);

            selectedObjects.add(feature.getId());
        }
        return selectedObjects;
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
                return String.class;
            case COLTYPE_FEATURE_MOZ:
                return Double.class;
            case COLTYPE_FEATURE_ELUTION_TIME:
            case COLTYPE_FEATURE_APEX_INTENSITY:
            case COLTYPE_FEATURE_INTENSITY:
            case COLTYPE_FEATURE_DURATION:
                return Float.class;
            case COLTYPE_FEATURE_IS_OVERLAPPING:
                return Boolean.class;
            case COLTYPE_FEATURE_CHARGE:
                return Integer.class;
        }
        return null; // should not happen
    }

    @Override
    public Object getDataValueAt(int rowIndex, int columnIndex) {
        int rowFiltered = rowIndex;
        if ((!m_isFiltering) && (m_filteredIds != null)) {
            rowFiltered = m_filteredIds.get(rowIndex).intValue();
        }
        // Retrieve Feature
        Feature feature = m_features.get(rowFiltered);

        switch (columnIndex) {
            case COLTYPE_FEATURE_ID: {
                return feature.getId();
            }
            case COLTYPE_FEATURE_MAP_NAME: {
                return feature.getMap().getName();
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
    
    
    public String getTootlTipValue(int row, int col) {
        if (m_features == null || row <0) {
            return "";
        }
        int rowFiltered = row;
        if ((!m_isFiltering) && (m_filteredIds != null)) {
            rowFiltered = m_filteredIds.get(row).intValue();
        }
        // Retrieve Feature
        Feature feature = m_features.get(rowFiltered);
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
        int rowFiltered = row;
        if ((!m_isFiltering) && (m_filteredIds != null)) {
            rowFiltered = m_filteredIds.get(row).intValue();
        }
        // Retrieve Feature
        Feature feature = m_features.get(rowFiltered);

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
        }
        return ""; // should never happen
    }
    
}
