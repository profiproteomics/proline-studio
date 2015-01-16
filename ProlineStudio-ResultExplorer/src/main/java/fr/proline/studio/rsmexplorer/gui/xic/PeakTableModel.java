package fr.proline.studio.rsmexplorer.gui.xic;

import fr.proline.core.orm.lcms.Feature;
import fr.proline.core.orm.lcms.Peakel;
import fr.proline.core.orm.lcms.Peakel.Peak;
import fr.proline.studio.comparedata.CompareDataInterface;
import fr.proline.studio.export.ExportColumnTextInterface;
import fr.proline.studio.filter.DoubleFilter;
import fr.proline.studio.filter.Filter;
import fr.proline.studio.graphics.BestGraphicsInterface;
import fr.proline.studio.graphics.PlotType;
import fr.proline.studio.table.ExportTableSelectionInterface;
import fr.proline.studio.table.LazyData;
import fr.proline.studio.table.LazyTable;
import fr.proline.studio.table.LazyTableModel;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 *
 * @author JM235353
 */
public class PeakTableModel extends LazyTableModel implements ExportTableSelectionInterface, ExportColumnTextInterface, CompareDataInterface , BestGraphicsInterface {

    public static final int COLTYPE_PEAK_MOZ = 0;
    public static final int COLTYPE_PEAK_ELUTION_TIME = 1;
    public static final int COLTYPE_PEAK_INTENSITY = 2;
    
    
    private static final String[] m_columnNames = {"m/z", "Elution Time", "Intensity"};
    private static final String[] m_toolTipColumns = {"Mass to Charge Ratio", "Elution Time", "Intensity"};

    private Feature m_feature = null;
    private Peakel m_peakel;
    private List<Peak> m_peaks = null;
    private Color m_color = null;
    private String m_title = null;

    private ArrayList<Integer> m_filteredIds = null;
    private boolean m_isFiltering = false;
    private boolean m_filteringAsked = false;
    
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
    public String getToolTipForHeader(int col) {
        return m_toolTipColumns[col];
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
        if ((!m_isFiltering) && (m_filteredIds != null)) {
            return m_filteredIds.size();
        }
        return m_peaks.size();
    }

    
    @Override
    public Object getValueAt(int row, int col) {

        int rowFiltered = row;
        if ((!m_isFiltering) && (m_filteredIds != null)) {
            rowFiltered = m_filteredIds.get(row).intValue();
        }
        // Retrieve Peak
        Peak peak = m_peaks.get(rowFiltered);

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

    public void setData(Long taskId, Feature feature, Peakel peakel, List<Peak> peaks, Color color, String title) {
        this.m_peaks = peaks;
        this.m_feature = feature;
        this.m_peakel = peakel;
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

    public Peak getPeak(int i) {

        if (m_filteredIds != null) {
            i = m_filteredIds.get(i).intValue();
        }

        return m_peaks.get(i);
    }

    public int findRow(float elutionTime) {

        if (m_filteredIds != null) {
            int nb = m_filteredIds.size();
            for (int i = 0; i < nb; i++) {
                if (elutionTime == m_peaks.get(m_filteredIds.get(i)).getElutionTime()) {
                    return i;
                }
            }
            return -1;
        }

        int nb = m_peaks.size();
        for (int i = 0; i < nb; i++) {
            if (elutionTime == m_peaks.get(i).getElutionTime()) {
                return i;
            }
        }
        return -1;

    }


    @Override
    public void filter() {

        if (m_peaks == null) {
            // filtering not possible for the moment
            m_filteringAsked = true;
            return;
        }

        m_isFiltering = true;
        try {

            int nbData = m_peaks.size();
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
            case COLTYPE_PEAK_MOZ:
            case COLTYPE_PEAK_ELUTION_TIME:
            case COLTYPE_PEAK_INTENSITY:
            {
                return ((DoubleFilter) filter).filter((Double) data);
            }
        }

        return true; // should never happen
    }

    @Override
    public void initFilters() {
        if (m_filters == null) {
            int nbCol = getColumnCount();
            m_filters = new Filter[nbCol];
            m_filters[COLTYPE_PEAK_MOZ] = new DoubleFilter(getColumnName(COLTYPE_PEAK_MOZ));
            m_filters[COLTYPE_PEAK_ELUTION_TIME] = new DoubleFilter(getColumnName(COLTYPE_PEAK_ELUTION_TIME));
            m_filters[COLTYPE_PEAK_INTENSITY] = new DoubleFilter(getColumnName(COLTYPE_PEAK_INTENSITY));
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

            // Retrieve Peak
            Peak peak = m_peaks.get(rowFiltered);

            selectedObjects.add(peak);
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
        int rowFiltered = rowIndex;
        if ((!m_isFiltering) && (m_filteredIds != null)) {
            rowFiltered = m_filteredIds.get(rowIndex).intValue();
        }
        // Retrieve Peak
        Peak peak = m_peaks.get(rowFiltered);

        switch (columnIndex) {
            case COLTYPE_PEAK_MOZ: {
                return peak.getMoz();

            }
            case COLTYPE_PEAK_ELUTION_TIME: {
                return peak.getElutionTime();

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
                scanValues.add(new Double(m_feature.getFirstScan().getTime()));
                scanText.add(" First Scan: ");
                scanColor.add(m_color);
            }
            if (m_feature.getLastScan() != null) {
                scanValues.add(new Double(m_feature.getLastScan().getTime()));
                scanText.add(" Last Scan: ");
                scanColor.add(m_color);
            }
        } else if (m_peakel != null) {
            if (m_peakel.getFirstScan() != null) {
                scanValues.add(new Double(m_peakel.getFirstScan().getTime()));
                scanText.add("First Scan: ");
                scanColor.add(m_color);
            }
            if (m_peakel.getLastScan() != null) {
                scanValues.add(new Double(m_peakel.getLastScan().getTime()));
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
    public Color getPlotColor() {
        return m_color;
    }
    
    @Override
    public String getPlotTitle() {
        return m_title;
    }

}
