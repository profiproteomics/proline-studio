package fr.proline.studio.rsmexplorer.gui.xic;

import fr.proline.core.orm.lcms.Feature;
import fr.proline.core.orm.lcms.Peakel;
import fr.proline.studio.comparedata.CompareDataInterface;
import fr.proline.studio.dam.tasks.xic.DatabaseLoadLcMSTask;
import fr.proline.studio.export.ExportColumnTextInterface;
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
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 *
 * @author JM235353
 */
public class PeakelTableModel extends LazyTableModel implements ExportTableSelectionInterface, ExportColumnTextInterface, CompareDataInterface , BestGraphicsInterface {

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

    private ArrayList<Integer> m_filteredIds = null;
    private boolean m_isFiltering = false;
    private boolean m_filteringAsked = false;
    
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
        if ((!m_isFiltering) && (m_filteredIds != null)) {
            return m_filteredIds.size();
        }
        return m_peakels.size();
    }

    
    @Override
    public Object getValueAt(int row, int col) {

        int rowFiltered = row;
        if ((!m_isFiltering) && (m_filteredIds != null)) {
            rowFiltered = m_filteredIds.get(row).intValue();
        }
        // Retrieve Peakel
        Peakel peakel = m_peakels.get(rowFiltered);

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

        if (m_filteredIds != null) {
            i = m_filteredIds.get(i).intValue();
        }

        return m_peakels.get(i);
    }

    public int findRow(long peakelId) {

        if (m_filteredIds != null) {
            int nb = m_filteredIds.size();
            for (int i = 0; i < nb; i++) {
                if (peakelId == m_peakels.get(m_filteredIds.get(i)).getId()) {
                    return i;
                }
            }
            return -1;
        }

        int nb = m_peakels.size();
        for (int i = 0; i < nb; i++) {
            if (peakelId == m_peakels.get(i).getId()) {
                return i;
            }
        }
        return -1;

    }

    public void sortAccordingToModel(ArrayList<Long> peakelIds) {

        if (m_peakels == null) {
            // data not loaded 
            return;
        }

        HashSet<Long> peakelIdMap = new HashSet<>(peakelIds.size());
        peakelIdMap.addAll(peakelIds);

        int nb = getRowCount();
        int iCur = 0;
        for (int iView = 0; iView < nb; iView++) {
            int iModel = m_table.convertRowIndexToModel(iView);
            // Retrieve Peakel
            Peakel p = getPeakel(iModel);
            if (peakelIdMap.contains(p.getId())) {
                peakelIds.set(iCur++, p.getId());
            }
        }

        // need to refilter
        if (m_filteredIds != null) { // NEEDED ????
            filter();
        }
    }

    @Override
    public void filter() {

        if (m_peakels == null) {
            // filtering not possible for the moment
            m_filteringAsked = true;
            return;
        }

        m_isFiltering = true;
        try {

            int nbData = m_peakels.size();
            if (m_filteredIds == null) {
                m_filteredIds = new ArrayList<>(nbData);
            } else {
                m_filteredIds.clear();
            }

            for (int i = 0; i < nbData; i++) {
                
                Integer iInteger = i;
                
                if ((m_restrainIds!=null) && (!m_restrainIds.isEmpty()) && (!m_restrainIds.contains(iInteger))) {
                    continue;
                }
                
                if (!filter(i)) {
                    continue;
                }
                m_filteredIds.add(iInteger);
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
            case COLTYPE_PEAKEL_MOZ:
            case COLTYPE_PEAKEL_ELUTION_TIME:
            case COLTYPE_PEAKEL_APEX_INTENSITY:
            case COLTYPE_PEAKEL_AREA:
            case COLTYPE_PEAKEL_DURATION:
            case COLTYPE_PEAKEL_FWHM:
            {
                return ((DoubleFilter) filter).filter((Double) data);
            }
            case COLTYPE_PEAKEL_FEATURE_COUNT:
            case COLTYPE_PEAKEL_PEAK_COUNT:
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
            m_filters[COLTYPE_PEAKEL_ID] = null;
            m_filters[COLTYPE_PEAKEL_MOZ] = new DoubleFilter(getColumnName(COLTYPE_PEAKEL_MOZ));
            m_filters[COLTYPE_PEAKEL_ELUTION_TIME] = new DoubleFilter(getColumnName(COLTYPE_PEAKEL_ELUTION_TIME));
            m_filters[COLTYPE_PEAKEL_APEX_INTENSITY] = new DoubleFilter(getColumnName(COLTYPE_PEAKEL_APEX_INTENSITY));
            m_filters[COLTYPE_PEAKEL_AREA] = new DoubleFilter(getColumnName(COLTYPE_PEAKEL_AREA));
            m_filters[COLTYPE_PEAKEL_DURATION] = new DoubleFilter(getColumnName(COLTYPE_PEAKEL_DURATION));
            m_filters[COLTYPE_PEAKEL_FWHM] = new DoubleFilter(getColumnName(COLTYPE_PEAKEL_FWHM));
            m_filters[COLTYPE_PEAKEL_FEATURE_COUNT] = new IntegerFilter(getColumnName(COLTYPE_PEAKEL_FEATURE_COUNT));
            m_filters[COLTYPE_PEAKEL_PEAK_COUNT] = new IntegerFilter(getColumnName(COLTYPE_PEAKEL_PEAK_COUNT));
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

            // Retrieve Peakel
            Peakel peakel = m_peakels.get(rowFiltered);

            selectedObjects.add(peakel.getId());
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
        int rowFiltered = rowIndex;
        if ((!m_isFiltering) && (m_filteredIds != null)) {
            rowFiltered = m_filteredIds.get(rowIndex).intValue();
        }
        // Retrieve Peakel
        Peakel peakel = m_peakels.get(rowFiltered);

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
    public int getBestXAxisColIndex(PlotType plotType) {
        switch (plotType) {
            case HISTOGRAM_PLOT:
                return COLTYPE_PEAKEL_APEX_INTENSITY;
            case SCATTER_PLOT:
                return COLTYPE_PEAKEL_ELUTION_TIME;
        }
        return -1;
    }

    @Override
    public int getBestYAxisColIndex(PlotType plotType) {
        return COLTYPE_PEAKEL_APEX_INTENSITY;
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
}
