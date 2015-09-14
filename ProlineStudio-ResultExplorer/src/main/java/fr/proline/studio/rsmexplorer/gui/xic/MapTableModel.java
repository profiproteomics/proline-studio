package fr.proline.studio.rsmexplorer.gui.xic;

import fr.proline.core.orm.lcms.Map;
import fr.proline.studio.filter.Filter;
import fr.proline.studio.filter.StringFilter;
import fr.proline.studio.graphics.PlotInformation;
import fr.proline.studio.graphics.PlotType;
import fr.proline.studio.table.CompoundTableModel;
import fr.proline.studio.table.GlobalTableModelInterface;
import fr.proline.studio.table.LazyData;
import fr.proline.studio.table.LazyTable;
import fr.proline.studio.table.LazyTableModel;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author JM235353
 */
public class MapTableModel extends LazyTableModel implements GlobalTableModelInterface {

    public static final int COLTYPE_MAP_ID = 0;
    public static final int COLTYPE_MAP_NAME = 1;
    public static final int COLTYPE_MAP_DESCRIPTION = 2;
    public static final int COLTYPE_MAP_CREATION_DATE = 3;
    
    private static final String[] m_columnNames = {"Id", "Name", "Description", "Creation Date"};
    private static final String[] m_toolTipColumns = {"Map Id", "Name", "Description", "Creation Date"};

    private List<Map> m_maps = null;

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
    public String getTootlTipValue(int row, int col) {
        return null;
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

        return m_maps.size();
    }

    
    
    @Override
    public Object getValueAt(int row, int col) {


        // Retrieve map
        Map amap = m_maps.get(row);

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

        m_taskId = taskId;

        fireTableDataChanged();


    }

    public void dataUpdated() {
        // no need to do an updateMinMax : scores are known at once
        fireTableDataChanged();

    }

    public Map getMap(int i) {

        return m_maps.get(i);
    }

    public int findRow(long mapId) {

        int nb = m_maps.size();
        for (int i = 0; i < nb; i++) {
            if (mapId == m_maps.get(i).getId()) {
                return i;
            }
        }
        return -1;

    }

    public void sortAccordingToModel(ArrayList<Long> mapIds, CompoundTableModel compoundTableModel) {

        if (m_maps == null) {
            // data not loaded 
            return;
        }

        HashSet<Long> mapIdMap = new HashSet<>(mapIds.size());
        mapIdMap.addAll(mapIds);

        int nb = m_table.getRowCount();
        int iCur = 0;
        for (int iView = 0; iView < nb; iView++) {
            int iModel = m_table.convertRowIndexToModel(iView);
            if (compoundTableModel != null) {
                iModel = compoundTableModel.convertCompoundRowToBaseModelRow(iModel);
            }
            // Retrieve Map
            Map m = getMap(iModel);
            if (mapIdMap.contains(m.getId())) {
                mapIds.set(iCur++, m.getId());
            }
        }

    }


    @Override
    public void addFilters(LinkedHashMap<Integer, Filter> filtersMap) {

        filtersMap.put(COLTYPE_MAP_NAME, new StringFilter(getColumnName(COLTYPE_MAP_NAME), null, COLTYPE_MAP_NAME));
        filtersMap.put(COLTYPE_MAP_DESCRIPTION, new StringFilter(getColumnName(COLTYPE_MAP_DESCRIPTION), null, COLTYPE_MAP_DESCRIPTION));

    }


    @Override
    public String getDataColumnIdentifier(int columnIndex) {
        return null; // not needed
    }

    @Override
    public Class getDataColumnClass(int columnIndex) {
        return null; // not needed
    }

    @Override
    public Object getDataValueAt(int rowIndex, int columnIndex) {
        return null; // not needed
    }

    @Override
    public int[] getKeysColumn() {
        return null; // not needed
    }

    @Override
    public int getInfoColumn() {
        return -1; // not needed
    }

    @Override
    public void setName(String name) {
        // not needed
    }

    @Override
    public String getName() {
        return null; // not needed
    }

    @Override
    public java.util.Map<String, Object> getExternalData() {
        return null; // not needed
    }

    @Override
    public PlotInformation getPlotInformation() {
        return null; // not needed
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
    public int getLoadingPercentage() {
        return m_table.getLoadingPercentage();
    }

    @Override
    public boolean isLoaded() {
        return m_table.isLoaded();
    }

    @Override
    public String getExportRowCell(int row, int col) {
        return null; // no specific export
    }

    @Override
    public TableCellRenderer getRenderer(int col) {
        return null;
    }

    @Override
    public GlobalTableModelInterface getFrozzenModel() {
        return this;
    }


}
