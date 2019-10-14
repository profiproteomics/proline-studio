/* 
 * Copyright (C) 2019 VD225637
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

import fr.proline.core.orm.lcms.Feature;
import fr.proline.core.orm.lcms.Peakel;
import fr.proline.core.orm.lcms.dto.DFeature;
import fr.proline.studio.extendedtablemodel.ExtraDataType;
import fr.proline.studio.dam.tasks.xic.DatabaseLoadLcMSTask;
import fr.proline.studio.export.ExportModelUtilities;
import fr.proline.studio.export.ExportFontData;
import fr.proline.studio.filter.DoubleFilter;
import fr.proline.studio.filter.Filter;
import fr.proline.studio.filter.IntegerFilter;
import fr.proline.studio.graphics.PlotInformation;
import fr.proline.studio.graphics.PlotType;
import fr.proline.studio.table.renderer.DefaultLeftAlignRenderer;
import fr.proline.studio.table.renderer.DefaultRightAlignRenderer;
import fr.proline.studio.table.renderer.DoubleRenderer;
import fr.proline.studio.rsmexplorer.gui.renderer.FloatRenderer;
import fr.proline.studio.extendedtablemodel.CompoundTableModel;
import fr.proline.studio.extendedtablemodel.GlobalTableModelInterface;
import fr.proline.studio.table.LazyData;
import fr.proline.studio.table.LazyTable;
import fr.proline.studio.table.LazyTableModel;
import fr.proline.studio.table.TableDefaultRendererManager;
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
public class PeakelTableModel extends LazyTableModel implements GlobalTableModelInterface {

    public static final int COLTYPE_PEAKEL_ID = 0;
    public static final int COLTYPE_PEAKEL_MOZ = 1;
    public static final int COLTYPE_PEAKEL_ELUTION_TIME = 2;
    public static final int COLTYPE_PEAKEL_APEX_INTENSITY = 3;
    public static final int COLTYPE_PEAKEL_AREA = 4;
    public static final int COLTYPE_PEAKEL_DURATION = 5;
    public static final int COLTYPE_PEAKEL_FWHM = 6;
    public static final int COLTYPE_PEAKEL_IS_OVERLAPPING = 7;
    public static final int COLTYPE_PEAKEL_FEATURE_COUNT = 8;
    public static final int COLTYPE_PEAKEL_PEAK_COUNT = 9;
    
    
    private static final String[] m_columnNames = {"Id", "m/z", "Elution Time", "Apex Intensity", "Area", "Duration (sec)", "FWHM (sec)", "Is Overlapping", "#Features", "#Peaks"};
    private static final String[] m_toolTipColumns = {"Peakel Id", "Mass to Charge Ratio", "Elution Time", "Apex Intensity", "Area", "Duration (sec)", "FWHM (sec)", "Is Overlapping", "#Features", "#Peaks"};

    private Feature m_feature = null;
    private List<Peakel> m_peakels = null;
    private Color m_color = null;
    private String m_title = null;

    
    private String m_modelName;

    public PeakelTableModel(LazyTable table) {
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
    public String getToolTipForHeader(int col) {
        return m_toolTipColumns[col];
    }

    @Override
    public String getTootlTipValue(int row, int col) {
        return null;
    }
    
    @Override
    public Class getColumnClass(int col) {
        if (col == COLTYPE_PEAKEL_ID) {
            return Long.class;
        }
        return LazyData.class;
    }

    @Override
    public int getSubTaskId(int col) {
        return DatabaseLoadLcMSTask.SUB_TASK_PEAKEL;
    }

    @Override
    public int getRowCount() {
        if (m_peakels == null) {
            return 0;
        }

        return m_peakels.size();
    }

    
    @Override
    public Object getValueAt(int row, int col) {

        // Retrieve Peakel
        Peakel peakel = m_peakels.get(row);

        switch (col) {
            case COLTYPE_PEAKEL_ID: {
                return peakel.getId();
            }
            case COLTYPE_PEAKEL_MOZ: {
                LazyData lazyData = getLazyData(row, col);
                if (peakel.getFeatureCount() == null) {
                    lazyData.setData(null);
                    givePriorityTo(m_taskId, row, col);
                }else {
                    lazyData.setData(peakel.getMoz());
                }
                return lazyData;

            }
            case COLTYPE_PEAKEL_ELUTION_TIME: {
                LazyData lazyData = getLazyData(row, col);
                if (peakel.getFeatureCount() == null) {
                    lazyData.setData(null);
                    givePriorityTo(m_taskId, row, col);
                }else {
                    lazyData.setData(peakel.getElutionTime());
                }
                return lazyData;

            }
            case COLTYPE_PEAKEL_APEX_INTENSITY: {
                LazyData lazyData = getLazyData(row, col);
                if (peakel.getFeatureCount() == null) {
                    lazyData.setData(null);
                    givePriorityTo(m_taskId, row, col);
                }else {
                    lazyData.setData(peakel.getApexIntensity());
                }
                return lazyData;

            }
            case COLTYPE_PEAKEL_AREA: {
                LazyData lazyData = getLazyData(row, col);
                if (peakel.getFeatureCount() == null) {
                    lazyData.setData(null);
                    givePriorityTo(m_taskId, row, col);
                }else {
                    lazyData.setData(peakel.getArea());
                }
                return lazyData;

            }
            case COLTYPE_PEAKEL_DURATION: {
                LazyData lazyData = getLazyData(row, col);
                if (peakel.getFeatureCount() == null) {
                    lazyData.setData(null);
                    givePriorityTo(m_taskId, row, col);
                }else {
                    lazyData.setData(peakel.getDuration());
                }
                return lazyData;

            }
            case COLTYPE_PEAKEL_FWHM: {
                LazyData lazyData = getLazyData(row, col);
                if (peakel.getFeatureCount() == null) {
                    lazyData.setData(null);
                    givePriorityTo(m_taskId, row, col);
                }else {
                    lazyData.setData(peakel.getFwhm());
                }
                return lazyData;

            }
            case COLTYPE_PEAKEL_IS_OVERLAPPING: {
                LazyData lazyData = getLazyData(row, col);
                if (peakel.getFeatureCount() == null) {
                    lazyData.setData(null);
                    givePriorityTo(m_taskId, row, col);
                }else {
                    lazyData.setData(peakel.getIsOverlapping()?"Yes":"No");
                }
                return lazyData;

            }
            case COLTYPE_PEAKEL_FEATURE_COUNT: {
                LazyData lazyData = getLazyData(row, col);
                if (peakel.getFeatureCount() == null) {
                    lazyData.setData(null);
                    givePriorityTo(m_taskId, row, col);
                }else {
                    lazyData.setData(peakel.getFeatureCount());
                }
                return lazyData;

            }
            case COLTYPE_PEAKEL_PEAK_COUNT: {
                LazyData lazyData = getLazyData(row, col);
                if (peakel.getFeatureCount() == null) {
                    lazyData.setData(null);
                    givePriorityTo(m_taskId, row, col);
                }else {
                    lazyData.setData(peakel.getPeakCount());
                }
                return lazyData;

            }
        }
        return null; // should never happen
    }

    public void setData(Long taskId, Feature feature, List<Peakel> peakels, Color color, String title) {
        this.m_peakels = peakels;
        this.m_feature = feature;
        this.m_color = color;
        this.m_title = title;

        m_taskId = taskId;

        fireTableDataChanged();

    }

    public void dataUpdated() {

        // no need to do an updateMinMax : scores are known at once
        fireTableDataChanged();

    }
    
    public Feature getFeature() {
        return this.m_feature;
    }
    
    public Color getColor() {
        return this.m_color;
    }
    
    public String getTitle() {
        return this.m_title;
    }

    public Peakel getPeakel(int i) {

        return m_peakels.get(i);
    }

    public int findRow(long peakelId) {


        int nb = m_peakels.size();
        for (int i = 0; i < nb; i++) {
            if (peakelId == m_peakels.get(i).getId()) {
                return i;
            }
        }
        return -1;

    }

    public void sortAccordingToModel(ArrayList<Long> peakelIds, CompoundTableModel compoundTableModel) {

        if (m_peakels == null) {
            // data not loaded 
            return;
        }

        HashSet<Long> peakelIdMap = new HashSet<>(peakelIds.size());
        peakelIdMap.addAll(peakelIds);

        int nb = m_table.getRowCount();
        int iCur = 0;
        for (int iView = 0; iView < nb; iView++) {
            int iModel = m_table.convertRowIndexToModel(iView);
            if (compoundTableModel != null) {
                iModel = compoundTableModel.convertCompoundRowToBaseModelRow(iModel);
            }
            // Retrieve Peakel
            Peakel p = getPeakel(iModel);
            if (peakelIdMap.contains(p.getId())) {
                peakelIds.set(iCur++, p.getId());
            }
        }

    }


    @Override
    public void addFilters(LinkedHashMap<Integer, Filter> filtersMap) {

        filtersMap.put(COLTYPE_PEAKEL_MOZ, new DoubleFilter(getColumnName(COLTYPE_PEAKEL_MOZ), null, COLTYPE_PEAKEL_MOZ));
        filtersMap.put(COLTYPE_PEAKEL_ELUTION_TIME, new DoubleFilter(getColumnName(COLTYPE_PEAKEL_ELUTION_TIME), null, COLTYPE_PEAKEL_ELUTION_TIME));
        filtersMap.put(COLTYPE_PEAKEL_APEX_INTENSITY, new DoubleFilter(getColumnName(COLTYPE_PEAKEL_APEX_INTENSITY), null, COLTYPE_PEAKEL_APEX_INTENSITY));
        filtersMap.put(COLTYPE_PEAKEL_AREA, new DoubleFilter(getColumnName(COLTYPE_PEAKEL_AREA), null, COLTYPE_PEAKEL_AREA));
        filtersMap.put(COLTYPE_PEAKEL_DURATION, new DoubleFilter(getColumnName(COLTYPE_PEAKEL_DURATION), null, COLTYPE_PEAKEL_DURATION));
        filtersMap.put(COLTYPE_PEAKEL_FWHM, new DoubleFilter(getColumnName(COLTYPE_PEAKEL_FWHM), null, COLTYPE_PEAKEL_FWHM));
        filtersMap.put(COLTYPE_PEAKEL_FEATURE_COUNT, new IntegerFilter(getColumnName(COLTYPE_PEAKEL_FEATURE_COUNT), null, COLTYPE_PEAKEL_FEATURE_COUNT));
        filtersMap.put(COLTYPE_PEAKEL_PEAK_COUNT, new IntegerFilter(getColumnName(COLTYPE_PEAKEL_PEAK_COUNT), null, COLTYPE_PEAKEL_PEAK_COUNT));
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
            case COLTYPE_PEAKEL_ID:
                return Long.class;
            case COLTYPE_PEAKEL_MOZ:
                return Double.class;
            case COLTYPE_PEAKEL_ELUTION_TIME:
            case COLTYPE_PEAKEL_APEX_INTENSITY:
            case COLTYPE_PEAKEL_AREA:
            case COLTYPE_PEAKEL_DURATION:
            case COLTYPE_PEAKEL_FWHM:
                return Float.class;
            case COLTYPE_PEAKEL_IS_OVERLAPPING:
                return Boolean.class;
            case COLTYPE_PEAKEL_FEATURE_COUNT:
            case COLTYPE_PEAKEL_PEAK_COUNT:
                return Integer.class;
        }
        return null; // should not happen
    }

    @Override
    public Object getDataValueAt(int rowIndex, int columnIndex) {

        // Retrieve Peakel
        Peakel peakel = m_peakels.get(rowIndex);

        switch (columnIndex) {
            case COLTYPE_PEAKEL_ID: {
                return peakel.getId();
            }
            case COLTYPE_PEAKEL_MOZ: {
                return peakel.getMoz();

            }
            case COLTYPE_PEAKEL_ELUTION_TIME: {
                return peakel.getElutionTime();

            }
            case COLTYPE_PEAKEL_APEX_INTENSITY: {
                return peakel.getApexIntensity();

            }
            case COLTYPE_PEAKEL_AREA: {
                return peakel.getArea();

            }
            case COLTYPE_PEAKEL_DURATION: {
                return peakel.getDuration();

            }
            case COLTYPE_PEAKEL_FWHM: {
                return peakel.getFwhm();

            }
            case COLTYPE_PEAKEL_IS_OVERLAPPING: {
                return peakel.getIsOverlapping();

            }
            case COLTYPE_PEAKEL_FEATURE_COUNT: {
                return peakel.getFeatureCount();

            }
            case COLTYPE_PEAKEL_PEAK_COUNT: {
                return peakel.getPeakCount();

            }
        }
        return null; // should never happen
    }

    @Override
    public int[] getKeysColumn() {
        int[] keys = { COLTYPE_PEAKEL_ID };
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
                cols[0] = COLTYPE_PEAKEL_APEX_INTENSITY;
                cols[1] = COLTYPE_PEAKEL_APEX_INTENSITY;
                return cols;
            }
            case SCATTER_PLOT: {
                int[] cols = new int[2];
                cols[0] = COLTYPE_PEAKEL_ELUTION_TIME;
                cols[1] = COLTYPE_PEAKEL_APEX_INTENSITY;
                return cols;
            }
        }
        return null;
    }
    
    
    @Override
    public int getInfoColumn() {
        return COLTYPE_PEAKEL_ID;
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
    public String getExportRowCell(int row, int col) {
        return ExportModelUtilities.getExportRowCell(this, row, col);
    }

    @Override
    public ArrayList<ExportFontData> getExportFonts(int row, int col) {
        return null;
    }
    
    @Override
    public TableCellRenderer getRenderer(int row, int col) {

        if (m_rendererMap.containsKey(col)) {
            return m_rendererMap.get(col);
        }

        TableCellRenderer renderer = null;

        switch (col) {
            case COLTYPE_PEAKEL_MOZ: {
                renderer = new DoubleRenderer( new DefaultRightAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class)) );
                break;
            }
            case COLTYPE_PEAKEL_ELUTION_TIME:
            case COLTYPE_PEAKEL_APEX_INTENSITY:
            case COLTYPE_PEAKEL_AREA:
            case COLTYPE_PEAKEL_DURATION:
            case COLTYPE_PEAKEL_FWHM: {
                renderer = new FloatRenderer( new DefaultRightAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class)) );
                break;
            }
            case COLTYPE_PEAKEL_IS_OVERLAPPING: {
                renderer = new DefaultLeftAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class));
                break;
            }
            case COLTYPE_PEAKEL_FEATURE_COUNT:
            case COLTYPE_PEAKEL_PEAK_COUNT: {
                renderer = new DefaultRightAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(Integer.class));
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
        list.add(new ExtraDataType(Peakel.class, true));
        list.add(new ExtraDataType(Feature.class, false));
        registerSingleValuesAsExtraTypes(list);
        return list;
    }

    @Override
    public Object getValue(Class c) {
        if (c.equals(Feature.class)) {
            return m_feature;
        }
        return getSingleValue(c);
    }

    @Override
    public Object getRowValue(Class c, int row) {
        if (c.equals(Peakel.class)) {
            return m_peakels.get(row);
        }
        return null;
    }
    
    @Override
    public Object getColValue(Class c, int col) {
        return null;
    }
}
