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
package fr.proline.mzscope.ui.model;

import fr.proline.studio.extendedtablemodel.ExtraDataType;
import fr.proline.studio.export.ExportFontData;
import fr.proline.studio.filter.DoubleFilter;
import fr.proline.studio.filter.Filter;
import fr.proline.studio.filter.IntegerFilter;
import fr.proline.studio.filter.StringFilter;
import fr.proline.studio.graphics.PlotInformation;
import fr.proline.studio.graphics.PlotType;
import fr.proline.studio.extendedtablemodel.GlobalTableModelInterface;
import fr.proline.studio.graphics.PlotDataSpec;
import fr.proline.studio.table.LazyData;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author CB205360
 */
public class MapTableModel extends AbstractTableModel implements GlobalTableModelInterface {

    final private static Logger logger = LoggerFactory.getLogger(MapTableModel.class);

    private String name;
    private List<List<Object>> valuesByRows = new ArrayList<>();
    private List<ColumnDescriptor> columns = new ArrayList<>();

    private static class ColumnDescriptor {

        String label;
        Class clazz;

        public ColumnDescriptor(String label, Class clazz) {
            this.label = label;
            this.clazz = clazz;
        }

    }

    public MapTableModel() {

    }

    public void setData(List<Map<String, Object>> maps, List<String> names) {
        columns = extractKeys(maps);
        valuesByRows = new ArrayList<>();
        for (int k = 0; k < maps.size(); k++) {
            Map<String, Object> map = maps.get(k);
            ArrayList<Object> rowValues = new ArrayList<>();
            rowValues.add(names.get(k));
            for (int c = 1; c < columns.size(); c++) {
                ColumnDescriptor col = columns.get(c);
                Object value = map.get(col.label);
                rowValues.add(value);
            }
            valuesByRows.add(rowValues);
        }

        fireTableStructureChanged();
    }

    private List<ColumnDescriptor> extractKeys(List<Map<String, Object>> maps) {
        List<ColumnDescriptor> keys = new ArrayList<>();
        keys.add(new ColumnDescriptor("Id", String.class));
        Set<String> dictionnary = new HashSet<String>();
        for (Map<String, Object> map : maps) {
            for (Map.Entry<String, Object> e : map.entrySet()) {
                if (!dictionnary.contains(e.getKey())) {
                    keys.add(new ColumnDescriptor(e.getKey(), getValueClass(e.getKey(), maps)));
                    dictionnary.add(e.getKey());
                }
            }
        }

        return keys;
    }

    private Class getValueClass(String key, List<Map<String, Object>> maps) {
        Object value = null;
        for (Map<String, Object> map : maps) {
            value = map.get(key);
            if (value != null) {
                break;
            }
        }
        return value.getClass();
    }

    @Override
    public int getRowCount() {
        return valuesByRows.size();
    }

    @Override
    public int getColumnCount() {
        return columns.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return valuesByRows.get(rowIndex).get(columnIndex);
    }

    @Override
    public String getColumnName(int columnIndex) {
        return columns.get(columnIndex).label;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return columns.get(columnIndex).clazz;
    }

    @Override
    public String getToolTipForHeader(int columnIndex) {
        return getColumnName(columnIndex);
    }

    @Override
    public String getTootlTipValue(int row, int col) {
        return null;
    }

    @Override
    public TableCellRenderer getRenderer(int row, int col) {
        return null;
    }

    @Override
    public Long getTaskId() {
        return -1L;
    }

    @Override
    public LazyData getLazyData(int row, int col) {
        return null;
    }

    @Override
    public void givePriorityTo(Long taskId, int row, int col) {
    }

    @Override
    public void sortingChanged(int col) {
    }

    @Override
    public int getSubTaskId(int col) {
        return -1;
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
    public PlotDataSpec getDataSpecAt(int i) {
        return null;
    }

    @Override
    public int[] getKeysColumn() {
        return null;
    }

    @Override
    public int getInfoColumn() {
        return 0;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
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
    public long row2UniqueId(int rowIndex) {
        return rowIndex;
    }

    @Override
    public int uniqueId2Row(long id) {
        return (int) id;
    }

    @Override
    public void addFilters(LinkedHashMap<Integer, Filter> filtersMap) {
        for (int k = 0; k < columns.size(); k++) {
            switch (columns.get(k).clazz.getSimpleName()) {
                case "String":
                    filtersMap.put(k, new StringFilter(getColumnName(k), null, k));
                    break;
                case "Double":
                case "double":
                    filtersMap.put(k, new DoubleFilter(getColumnName(k), null, k));
                    break;
                case "Integer":
                case "int":
                    filtersMap.put(k, new IntegerFilter(getColumnName(k), null, k));
                    break;
                default:
                    System.out.println("no filter for type : " + columns.get(k).clazz.getSimpleName());
            }
        }
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
        return PlotType.SCATTER_PLOT;
    }

    @Override
    public int[] getBestColIndex(PlotType plotType) {
        return null;
    }

    @Override
    public String getExportRowCell(int row, int col) {
        return null;
    }

    @Override
    public String getExportColumnName(int col) {
        return getColumnName(col);
    }

    @Override
    public GlobalTableModelInterface getFrozzenModel() {
        return this;
    }

    @Override
    public ArrayList<ExtraDataType> getExtraDataTypes() {
        return null;
    }

    @Override
    public Object getValue(Class c) {
        return null;
    }

    @Override
    public void addSingleValue(Object v) {

    }

    @Override
    public Object getSingleValue(Class c) {
        return null;
    }

    @Override
    public Object getRowValue(Class c, int row) {
        return null;
    }

    @Override
    public Object getColValue(Class c, int col) {
        return null;
    }

    @Override
    public ArrayList<ExportFontData> getExportFonts(int row, int col) {
        return null;
    }

}
