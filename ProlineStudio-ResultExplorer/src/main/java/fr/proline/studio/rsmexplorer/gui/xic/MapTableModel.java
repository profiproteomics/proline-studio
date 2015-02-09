package fr.proline.studio.rsmexplorer.gui.xic;

import fr.proline.core.orm.lcms.Map;
import fr.proline.studio.export.ExportColumnTextInterface;
import fr.proline.studio.filter.Filter;
import fr.proline.studio.filter.StringFilter;
import fr.proline.studio.table.ExportTableSelectionInterface;
import fr.proline.studio.table.LazyData;
import fr.proline.studio.table.LazyTable;
import fr.proline.studio.table.LazyTableModel;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 *
 * @author JM235353
 */
public class MapTableModel extends LazyTableModel implements ExportTableSelectionInterface, ExportColumnTextInterface {

    public static final int COLTYPE_MAP_ID = 0;
    public static final int COLTYPE_MAP_NAME = 1;
    public static final int COLTYPE_MAP_DESCRIPTION = 2;
    public static final int COLTYPE_MAP_CREATION_DATE = 3;
    
    private static final String[] m_columnNames = {"Id", "Name", "Description", "Creation Date"};
    private static final String[] m_toolTipColumns = {"Map Id", "Name", "Description", "Creation Date"};

    private List<Map> m_maps = null;

    private boolean m_isFiltering = false;
    private boolean m_filteringAsked = false;

    public MapTableModel(LazyTable table) {
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
        if (col == COLTYPE_MAP_ID) {
            return Long.class;
        }
        return LazyData.class;
    }

    @Override
    public int getSubTaskId(int col) {
        return -1;
    }

    @Override
    public int getRowCount() {
        if (m_maps == null) {
            return 0;
        }
        if ((!m_isFiltering) && (m_filteredIds != null)) {
            return m_filteredIds.size();
        }
        return m_maps.size();
    }

    
    
    @Override
    public Object getValueAt(int row, int col) {

        int rowFiltered = row;
        if ((!m_isFiltering) && (m_filteredIds != null)) {
            rowFiltered = m_filteredIds.get(row).intValue();
        }
        // Retrieve map
        Map amap = m_maps.get(rowFiltered);

        switch (col) {
            case COLTYPE_MAP_ID: {
                return amap.getId();
            }
            case COLTYPE_MAP_NAME: {
                LazyData lazyData = getLazyData(row, col);
                lazyData.setData(amap.getName());
                return lazyData;
            }
            case COLTYPE_MAP_DESCRIPTION: {
                LazyData lazyData = getLazyData(row, col);
                lazyData.setData(amap.getDescription());
                return lazyData;
            }
            case COLTYPE_MAP_CREATION_DATE: {
                LazyData lazyData = getLazyData(row, col);
                lazyData.setData(amap.getCreationTimestamp());
                return lazyData;
            }
        }
        return null; // should never happen
    }

    public void setData(Long taskId, List<Map> maps) {
        this.m_maps = maps;
        m_filteredIds = null;
        m_isFiltering = false;

        m_taskId = taskId;

        if (m_restrainIds != null) {
            m_restrainIds = null;
            m_filteringAsked = true;
        }
        
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

    public Map getMap(int i) {
        if (m_filteredIds != null) {
            i = m_filteredIds.get(i).intValue();
        }
        return m_maps.get(i);
    }

    public int findRow(long mapId) {
        if (m_filteredIds != null) {
            int nb = m_filteredIds.size();
            for (int i = 0; i < nb; i++) {
                if (mapId == m_maps.get(m_filteredIds.get(i)).getId()) {
                    return i;
                }
            }
            return -1;
        }

        int nb = m_maps.size();
        for (int i = 0; i < nb; i++) {
            if (mapId == m_maps.get(i).getId()) {
                return i;
            }
        }
        return -1;

    }

    public void sortAccordingToModel(ArrayList<Long> mapIds) {

        if (m_maps == null) {
            // data not loaded 
            return;
        }

        HashSet<Long> mapIdMap = new HashSet<>(mapIds.size());
        mapIdMap.addAll(mapIds);

        int nb = getRowCount();
        int iCur = 0;
        for (int iView = 0; iView < nb; iView++) {
            int iModel = m_table.convertRowIndexToModel(iView);
            // Retrieve Map
            Map m = getMap(iModel);
            if (mapIdMap.contains(m.getId())) {
                mapIds.set(iCur++, m.getId());
            }
        }

        // need to refilter
        if (m_filteredIds != null) { // NEEDED ????
            filter();
        }
    }

    @Override
    public void filter() {

        if (m_maps == null) {
            // filtering not possible for the moment
            m_filteringAsked = true;
            return;
        }

        m_isFiltering = true;
        try {

            int nbData = m_maps.size();
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
            case COLTYPE_MAP_NAME: {
                return ((StringFilter) filter).filter((String) data);
            }
            case COLTYPE_MAP_DESCRIPTION: {
                return ((StringFilter) filter).filter((String) data);
            }
        }

        return true; // should never happen
    }

    @Override
    public void initFilters() {
        if (m_filters == null) {
            int nbCol = getColumnCount();
            m_filters = new Filter[nbCol];
            m_filters[COLTYPE_MAP_ID] = null;
            m_filters[COLTYPE_MAP_NAME] = new StringFilter(getColumnName(COLTYPE_MAP_NAME));
            m_filters[COLTYPE_MAP_DESCRIPTION] = new StringFilter(getColumnName(COLTYPE_MAP_DESCRIPTION));
            m_filters[COLTYPE_MAP_CREATION_DATE] = null;
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

            // Retrieve Map
            Map map = m_maps.get(rowFiltered);

            selectedObjects.add(map.getId());
        }
        return selectedObjects;
    }
}
