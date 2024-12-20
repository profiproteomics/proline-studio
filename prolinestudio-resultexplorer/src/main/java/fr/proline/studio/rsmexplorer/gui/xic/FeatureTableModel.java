/* 
 * Copyright (C) 2019
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1 
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License 
 * along with this program; If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */
package fr.proline.studio.rsmexplorer.gui.xic;

import fr.proline.core.orm.lcms.dto.DFeature;
import fr.proline.core.orm.msi.dto.DMasterQuantPeptideIon;
import fr.proline.core.orm.uds.dto.DQuantitationChannel;
import fr.proline.studio.corewrapper.util.PeptideClassesUtils;
import fr.proline.studio.extendedtablemodel.ExtraDataType;
import fr.proline.studio.dam.tasks.xic.DatabaseLoadLcMSTask;
import fr.proline.studio.export.ExportFontData;
import fr.proline.studio.filter.ConvertValueInterface;
import fr.proline.studio.filter.DoubleFilter;
import fr.proline.studio.filter.Filter;
import fr.proline.studio.filter.IntegerFilter;
import fr.proline.studio.graphics.PlotInformation;
import fr.proline.studio.graphics.PlotType;
import fr.proline.studio.mzscope.MzScopeInterface;
import fr.proline.studio.mzscope.MzdbInfo;
import fr.proline.studio.table.renderer.BigFloatOrDoubleRenderer;
import fr.proline.studio.table.renderer.DefaultAlignRenderer;
import fr.proline.studio.table.renderer.DoubleRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.FontRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.TimeRenderer;
import fr.proline.studio.extendedtablemodel.CompoundTableModel;
import fr.proline.studio.extendedtablemodel.GlobalTableModelInterface;
import fr.proline.studio.table.LazyData;
import fr.proline.studio.table.LazyTable;
import fr.proline.studio.table.LazyTableModel;
import fr.proline.studio.table.TableDefaultRendererManager;
import fr.proline.studio.utils.CyclicColorPalette;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
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
    public static final int COLTYPE_FEATURE_CALIBRATED_MOZ = 4;
    public static final int COLTYPE_FEATURE_CHARGE = 5;
    public static final int COLTYPE_FEATURE_RETENTION_TIME = 6;
    public static final int COLTYPE_FEATURE_APEX_INTENSITY = 7;
    public static final int COLTYPE_FEATURE_INTENSITY = 8;
    public static final int COLTYPE_FEATURE_DURATION = 9;
    public static final int COLTYPE_FEATURE_QUALITY_SCORE = 10;
    public static final int COLTYPE_FEATURE_IS_OVERLAPPING = 11;
    public static final int COLTYPE_FEATURE_PREDICTED_RETENTION_TIME = 12;
    public static final int COLTYPE_FEATURE_PEAKELS_COUNT = 13;

    public final static String SERIALIZED_PROP_PREDICTED_ELUTION_TIME = "predicted_elution_time";
    public final static String SERIALIZED_PROP_PEAKELS_COUNT = "peakels_count";
    public final static String SERIALIZED_PROP_IS_RELIABLE = "is_reliable";

    protected static final Logger logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");
    
    private static final String[] m_columnNames = {"Id", "Map", "Quant. Channel", "m/z", "Calibrated MoZ", "Charge", "RT", "Apex Intensity", "Intensity", "Duration (sec)", "Quality Score", "Is Overlapping", "Predicted RT", "#Peakels"};
    private static final String[] m_toolTipColumns = {"Feature Id","Map name","Quantitation Channel ",  "Mass to Charge Ratio", "Calibrated Mass to Charge Ratio", "Charge", "Retention Time in minutes", "Apex Intensity", "Intensity", "Duration (sec)", "Quality Score", "Is Overlapping", "Predicted Retention time in min", "Peakels count"};
    private static final String[] m_columnNamesForExport = {"Id", "Map", "Quantitation Channel", "m/z", "Calibrated MoZ", "Charge", "Elution Time (sec)", "Apex Intensity", "Intensity", "Duration (sec)", "Quality Score", "Is Overlapping", "Predicted Retention Time (sec)", "Peakels Count"};

    private DMasterQuantPeptideIon m_quantPeptideIon = null;
    private List<DFeature> m_features = null;
    private QuantChannelInfo m_quantChannelInfo = null;
    private List<Boolean> m_featureHasPeak = null;
    private Map<Long, Double> calibratedMozByFeatureId = new HashMap<>();

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
        return DatabaseLoadLcMSTask.SUB_TASK_PEAKEL_FOR_FEATURE;
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
                    String title = m_quantChannelInfo.getQuantChannels(feature.getQuantChannelId()).getName();
                    Color color = m_quantChannelInfo.getQuantChannelColor(feature.getQuantChannelId());
                    String rsmHtmlColor = CyclicColorPalette.getHTMLColor(color);
        
                    StringBuilder sb = new StringBuilder();
                    sb.append("<html>");
                    sb.append("<html><font color='").append(rsmHtmlColor).append("'>&#x25A0;&nbsp;</font>");
                    sb.append(title);
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
            case COLTYPE_FEATURE_RETENTION_TIME: {
                LazyData lazyData = getLazyData(row, col);
                if (feature.getCharge() == null) {
                    lazyData.setData(null);
                    givePriorityTo(m_taskId, row, col);
                }else {
                    lazyData.setData(feature.getElutionTime());
                }
                return lazyData;

            }
            case COLTYPE_FEATURE_CALIBRATED_MOZ: {
                LazyData lazyData = getLazyData(row, col);
                if (feature.getCharge() == null) {
                    lazyData.setData(null);
                    givePriorityTo(m_taskId, row, col);
                }else {
                    if(calibratedMozByFeatureId.containsKey(feature.getId()) )
                        lazyData.setData(calibratedMozByFeatureId.get(feature.getId()));
                    else
                        lazyData.setData(getCalibratedMoZ(feature));
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
            case COLTYPE_FEATURE_PREDICTED_RETENTION_TIME: {
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
                        lazyData.setData(Float.valueOf(predictedElutionTime.floatValue()));
                    }else{
                        lazyData.setData( Float.valueOf((float)feature.getPredictedElutionTime()));
                    }
                }else{
                    lazyData.setData( Float.valueOf((float)feature.getPredictedElutionTime()));
                }
                return lazyData;
            }
            case COLTYPE_FEATURE_PEAKELS_COUNT:{
                LazyData lazyData = getLazyData(row, col);
                if (feature.getCharge() == null) {
                    lazyData.setData(null);
                    givePriorityTo(m_taskId, row, col);
                }else {
                    lazyData.setData(feature.getPeakelCount());
                }
                return lazyData;
            }
        }
        return null; // should never happen
    }

    private Double getCalibratedMoZ(DFeature feature){
        logger.debug("calculate delta moz at RT " + feature.getElutionTime() + " for map =" + feature.getMap().getName());
        Double calcMoz = Double.NaN;
        try {
            if(m_quantPeptideIon != null) {
                Double deltaMoz = PeptideClassesUtils.getDeltaMozFor(feature.getMoz(), feature.getCharge(), m_quantPeptideIon.getPeptideInstance().getPeptide()).doubleValue();
                calcMoz = feature.getMoz() + deltaMoz;
                calibratedMozByFeatureId.put(feature.getId(), calcMoz);
                logger.debug("...result= " + calcMoz);
            }
        } catch (Exception e) {
            logger.error("Error while retrieving time in map calibration: " + e);
        }
        //Displayed deltaMoz is inverted compare to store deltaMoz !
        return calcMoz;
    }


    public void setData(Long taskId, List<DFeature> features, QuantChannelInfo quantChannelInfo, List<Boolean> featureHasPeak, DMasterQuantPeptideIon pepIon) {
        m_features = features;
        m_quantPeptideIon = pepIon;
        calibratedMozByFeatureId.clear();
        m_quantChannelInfo = quantChannelInfo;
        m_featureHasPeak = featureHasPeak ;

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

        return m_quantChannelInfo.getQuantChannelColor(m_features.get(i).getQuantChannelId());
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
        filtersMap.put(COLTYPE_FEATURE_CALIBRATED_MOZ, new DoubleFilter(getColumnName(COLTYPE_FEATURE_CALIBRATED_MOZ), null, COLTYPE_FEATURE_CALIBRATED_MOZ));
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
        filtersMap.put(COLTYPE_FEATURE_RETENTION_TIME, new DoubleFilter(getColumnName(COLTYPE_FEATURE_RETENTION_TIME), minuteConverter, COLTYPE_FEATURE_RETENTION_TIME));
        filtersMap.put(COLTYPE_FEATURE_APEX_INTENSITY, new DoubleFilter(getColumnName(COLTYPE_FEATURE_APEX_INTENSITY), null, COLTYPE_FEATURE_APEX_INTENSITY));
        filtersMap.put(COLTYPE_FEATURE_INTENSITY, new DoubleFilter(getColumnName(COLTYPE_FEATURE_INTENSITY), null, COLTYPE_FEATURE_INTENSITY));
        filtersMap.put(COLTYPE_FEATURE_DURATION, new DoubleFilter(getColumnName(COLTYPE_FEATURE_DURATION), null, COLTYPE_FEATURE_DURATION));
        filtersMap.put(COLTYPE_FEATURE_QUALITY_SCORE, new DoubleFilter(getColumnName(COLTYPE_FEATURE_QUALITY_SCORE), null, COLTYPE_FEATURE_QUALITY_SCORE));
        filtersMap.put(COLTYPE_FEATURE_PREDICTED_RETENTION_TIME, new DoubleFilter(getColumnName(COLTYPE_FEATURE_PREDICTED_RETENTION_TIME), minuteConverter, COLTYPE_FEATURE_PREDICTED_RETENTION_TIME));
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
            case COLTYPE_FEATURE_CALIBRATED_MOZ:
                return Double.class;
            case COLTYPE_FEATURE_RETENTION_TIME:
            case COLTYPE_FEATURE_APEX_INTENSITY:
            case COLTYPE_FEATURE_INTENSITY:
            case COLTYPE_FEATURE_DURATION:
            case COLTYPE_FEATURE_PREDICTED_RETENTION_TIME:
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
            case COLTYPE_FEATURE_RETENTION_TIME: {
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
            case COLTYPE_FEATURE_PREDICTED_RETENTION_TIME: {
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
                return Float.valueOf((float) feature.getPredictedElutionTime());
            }
            case COLTYPE_FEATURE_PEAKELS_COUNT: {
                return feature.getPeakelCount();
            }
            case COLTYPE_FEATURE_CALIBRATED_MOZ:{
                if(calibratedMozByFeatureId.containsKey(feature.getId()))
                    return calibratedMozByFeatureId.get(feature.getId());
                else
                    return getCalibratedMoZ(feature);
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
    public int[] getBestColIndex(PlotType plotType) {
 
        switch (plotType) {
            case HISTOGRAM_PLOT: {
                int[] cols = new int[2];
                cols[0] = COLTYPE_FEATURE_INTENSITY;
                cols[1] = COLTYPE_FEATURE_INTENSITY;
                return cols;
            }
            case SCATTER_PLOT: {
                int[] cols = new int[2];
                cols[0] = COLTYPE_FEATURE_RETENTION_TIME;
                cols[1] = COLTYPE_FEATURE_INTENSITY;
                return cols;
            }
        }
        return null;
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
        String title = m_quantChannelInfo.getQuantChannels(feature.getQuantChannelId()).getName();
        Color color = m_quantChannelInfo.getQuantChannelColor(feature.getQuantChannelId());
        String rsmHtmlColor = CyclicColorPalette.getHTMLColor(color);
        
        StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        sb.append("<html><font color='").append(rsmHtmlColor).append("'>&#x25A0;&nbsp;</font>");
        sb.append(title);
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
            case COLTYPE_FEATURE_CALIBRATED_MOZ: {
                if (!calibratedMozByFeatureId.containsKey(feature.getId()) || calibratedMozByFeatureId.get(feature.getId()).isNaN())  {
                    return "";
                } else {
                    return ""+calibratedMozByFeatureId.get(feature.getId());
                }

            }
            case COLTYPE_FEATURE_CHARGE: {
                if (feature.getCharge() == null) {
                    return "";
                }else {
                    return ""+feature.getCharge();
                }

            }
            case COLTYPE_FEATURE_RETENTION_TIME: {
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
            case COLTYPE_FEATURE_PREDICTED_RETENTION_TIME: {
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
                return String.valueOf(feature.getPredictedElutionTime());
            }
            case COLTYPE_FEATURE_PEAKELS_COUNT: {
               if (feature.getCharge() == null) {
                    return "";
                } else {
                   return ""+feature.getPeakelCount();
               }
            }
        }
        return ""; // should never happen
    }
    
    @Override
    public ArrayList<ExportFontData> getExportFonts(int row, int col) {
        return null;
    }
    
    public List<Integer> getDefaultColumnsToHide() {
        List<Integer> listIds = new ArrayList();
        listIds.add(COLTYPE_FEATURE_PEAKELS_COUNT);
        listIds.add(COLTYPE_FEATURE_IS_OVERLAPPING);
        listIds.add(COLTYPE_FEATURE_QUALITY_SCORE);
        listIds.add(COLTYPE_FEATURE_CALIBRATED_MOZ);
        return listIds;
    }
    

    public Boolean isReliable(int row, int col) {
        //return !hasFeaturePeaks(row);
        // Retrieve Feature
        DFeature feature = m_features.get(row);
        if (feature.getId() == -1){
            // fake feature
            return false;
        } else {
                Map<String, Object> prop = null;
                try {
                    prop = feature.getSerializedPropertiesAsMap();
                } catch (Exception ex) {
                prop = null;     
                }
                if (prop != null) {
                    Object o = prop.get(SERIALIZED_PROP_IS_RELIABLE);
                    if (o != null && o instanceof Boolean) {
                        return (Boolean)o;
                    } else {
                        return true;
                    }
                } else {
                    return true;
                }
        }
    }
    
    public Boolean isInItalic(int row, int col) {
        //return !hasFeaturePeaks(row);
        // Retrieve Feature
        DFeature feature = m_features.get(row);
        if (feature.getId() == -1){
            // fake feature
            return true;
        }else{
            if (col == COLTYPE_FEATURE_PREDICTED_RETENTION_TIME){
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
            case COLTYPE_FEATURE_MAP_NAME:
            case COLTYPE_FEATURE_QC:
            case COLTYPE_FEATURE_IS_OVERLAPPING: {
                renderer = new FontRenderer( new DefaultAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class), JLabel.LEFT) );
                break;
            }
            case COLTYPE_FEATURE_CALIBRATED_MOZ:
            case COLTYPE_FEATURE_MOZ: {
                renderer = new FontRenderer( new DoubleRenderer( new DefaultAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class), JLabel.RIGHT), 4 ));
                break;
            }
            case COLTYPE_FEATURE_APEX_INTENSITY:
            case COLTYPE_FEATURE_INTENSITY:
            case COLTYPE_FEATURE_DURATION: {
                renderer = new FontRenderer( new BigFloatOrDoubleRenderer( new DefaultAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class), JLabel.RIGHT), 0 ));
                break;
            }
            case COLTYPE_FEATURE_RETENTION_TIME:
            case COLTYPE_FEATURE_PREDICTED_RETENTION_TIME: {
                renderer = new FontRenderer( new TimeRenderer(new DefaultAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class), JLabel.RIGHT)));
                break;
            }
            case COLTYPE_FEATURE_CHARGE:
            case COLTYPE_FEATURE_PEAKELS_COUNT: {
                renderer = new FontRenderer( new DefaultAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(Integer.class), JLabel.RIGHT));
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
    public Object getRowValue(Class c, int row) {
        if (c.equals(DFeature.class)) {
            return m_features.get(row);
        }
        return null;
    }
    
    @Override
    public Object getColValue(Class c, int col) {
        return null;
    }

}
