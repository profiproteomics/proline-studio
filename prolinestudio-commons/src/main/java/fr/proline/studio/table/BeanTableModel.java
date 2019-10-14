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
package fr.proline.studio.table;

import fr.proline.studio.export.ExportFontData;
import fr.proline.studio.extendedtablemodel.ExtraDataType;
import fr.proline.studio.filter.DoubleFilter;
import fr.proline.studio.filter.Filter;
import fr.proline.studio.filter.IntegerFilter;
import fr.proline.studio.filter.StringFilter;
import fr.proline.studio.graphics.PlotInformation;
import fr.proline.studio.graphics.PlotType;
import fr.proline.studio.extendedtablemodel.GlobalTableModelInterface;
import fr.proline.studio.graphics.PlotDataSpec;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.util.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author CB205360
 */
public class BeanTableModel<T> extends AbstractTableModel implements GlobalTableModelInterface {

    final private static Logger logger = LoggerFactory.getLogger(BeanTableModel.class);

    private String m_name;
    private PropertyDescriptor[] m_descriptors;
    private List<T> m_entities = new ArrayList<>();
    private int[] m_columnIndexConverter = null;

    private HashMap<String, Integer> m_columnIndexMap = new HashMap<>();
    private HashMap<String, String> m_columnNameMap = new HashMap<>();
    private HashMap<String, TableCellRenderer> m_columnRendererMap = new HashMap<>();
    private HashMap<Integer, Filter> m_columnFilterMap = new HashMap<>();

    public BeanTableModel(Class<T> type) {
        this(type, Collections.singletonList("class"));
    }
        
    public BeanTableModel(Class<T> type, List<String> exclusionList) {
        try {
            //typeParameterClass = type;
            m_name = type.getName() + " table";
            PropertyDescriptor[] propertyDescriptors = Introspector.getBeanInfo(type, Introspector.USE_ALL_BEANINFO).getPropertyDescriptors();
            //avoid 'class' property 
            List<PropertyDescriptor> descriptors = new ArrayList<>();
            for (PropertyDescriptor property : propertyDescriptors) {
                if (!exclusionList.contains(property.getName())) {
                    descriptors.add(property);
                }
            }
            m_descriptors = descriptors.toArray(new PropertyDescriptor[0]);
        } catch (IntrospectionException ex) {
            logger.info("cannot infer getters from " + type, ex);
        }
    }

    public void addProperties(String columnKey, Integer columnIndex) {
        addProperties(columnKey, columnIndex, null, null, null);
    }

    public void addProperties(String columnKey, Integer columnIndex, String columnName) {
        addProperties(columnKey, columnIndex, columnName, null, null);
    }

    public void addProperties(String columnKey, Integer columnIndex, String columnName, TableCellRenderer renderer, Filter f) {
        if (columnIndex != null) {
            m_columnIndexMap.put(columnKey, columnIndex);
        }
        if (columnName != null) {
            m_columnNameMap.put(columnKey, columnName);
        }
        if (renderer != null) {
            m_columnRendererMap.put(columnKey, renderer);
        }
        if (f != null) {
            m_columnFilterMap.put(columnIndex, f);
        }
    }

    public void firePropertiesChanged() {

        // Prepare the index of columns as specified 
        int nbColumns = getColumnCount();
        if (m_columnIndexConverter == null) {
            m_columnIndexConverter = new int[nbColumns];
        }
        for (int i = 0; i < nbColumns; i++) {
            m_columnIndexConverter[i] = -1;
        }

        for (int i = 0; i < nbColumns; i++) {
            String columnKey = getColumnKey(i);
            Integer indexForUser = m_columnIndexMap.get(columnKey);
            if (indexForUser != null) {
                m_columnIndexConverter[indexForUser] = i;
            }
        }

        int firstFreePotentialIndex = 0;
        for (int i = 0; i < nbColumns; i++) {
            String columnKey = getColumnKey(i);
            Integer indexForUser = m_columnIndexMap.get(columnKey);
            if (indexForUser == null) {
                for (int j = firstFreePotentialIndex; j < nbColumns; j++) {
                    if (m_columnIndexConverter[j] == -1) {
                        m_columnIndexConverter[j] = i;
                        firstFreePotentialIndex = j;
                        break;
                    }
                }
            }
        }

        fireTableDataChanged();
    }

    public void setData(List<T> data) {
        m_entities = data;

        fireTableDataChanged();
    }

    public List<T> getData() {
        return m_entities;
    }

    @Override
    public int getRowCount() {
        return m_entities.size();
    }

    @Override
    public int getColumnCount() {
        return m_descriptors.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {

        if (m_columnIndexConverter != null) {
            columnIndex = m_columnIndexConverter[columnIndex];
        }

        try {
            Object obj = m_descriptors[columnIndex].getReadMethod().invoke(m_entities.get(rowIndex));
            if ((obj != null) && obj.getClass().isArray()) {
                int arrayLength = Array.getLength(obj);
                int length = Math.min(arrayLength, 10);
                StringBuilder buffer = new StringBuilder("[");
                int k = 0;
                for (; k < length; k++) {
                    buffer.append(Array.get(obj, k).toString()).append(",");
                }
                if (k < arrayLength) {
                    buffer.append("...");
                } else {
                    buffer.deleteCharAt(buffer.length() - 1);
                }
                buffer.append("]");
                obj = buffer.toString();
            }
            return obj;
        } catch (Exception ex) {
            logger.info("cannot get read method for property " + m_descriptors[columnIndex].getName(), ex);
        }
        return null;
    }

    public String getColumnKey(int column) {
        return m_descriptors[column].getDisplayName();
    }

    @Override
    public String getColumnName(int columnIndex) {

        if (m_columnIndexConverter != null) {
            columnIndex = m_columnIndexConverter[columnIndex];
        }

        String columnKey = getColumnKey(columnIndex);
        String columnName = m_columnNameMap.get(columnKey);
        if (columnName != null) {
            return columnName;
        } else {
            return columnKey;
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {

        if (m_columnIndexConverter != null) {
            columnIndex = m_columnIndexConverter[columnIndex];
        }

        Class<?> cl = m_descriptors[columnIndex].getPropertyType();
        if (cl.isPrimitive()) {
            switch (cl.getSimpleName()) {
                case "double":
                    cl = Double.class;
                    break;
                case "int":
                    cl = Integer.class;
                    break;
                case "long":
                    cl = Long.class;
                    break;
                case "float":
                    cl = Float.class;
                    break;
                case "boolean":
                    cl = Boolean.class;
            }
        } else if (cl.isArray()) {
            cl = String.class;
        }
        return cl;
    }

    @Override
    public String getToolTipForHeader(int columnIndex) {
        if (m_columnIndexConverter != null) {
            columnIndex = m_columnIndexConverter[columnIndex];
        }

        return m_descriptors[columnIndex].getShortDescription();
    }

    @Override
    public String getTootlTipValue(int row, int col) {
        return null;
    }

    @Override
    public TableCellRenderer getRenderer(int rowIndex, int columnIndex) {
        
        if (m_columnIndexConverter != null) {
            columnIndex = m_columnIndexConverter[columnIndex];
        }
        
        String columnKey = getColumnKey(columnIndex);
        return m_columnRendererMap.get(columnKey);
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
    public int[] getKeysColumn() {
        return null;
    }

    @Override
    public int getInfoColumn() {
        return 0;
    }

    @Override
    public void setName(String name) {
        this.m_name = name;
    }

    @Override
    public String getName() {
        return m_name;
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
        for (int columnIndex = 0; columnIndex< m_descriptors.length; columnIndex++) {


            Filter f = m_columnFilterMap.get(columnIndex);
            if (f != null) {
                filtersMap.put(columnIndex, f);
                return;
            }

            int descriptorIndex = columnIndex;
            if (m_columnIndexConverter != null) {
                descriptorIndex = m_columnIndexConverter[columnIndex];
            }
            
            switch (m_descriptors[descriptorIndex].getPropertyType().getSimpleName()) {
                case "String":
                    filtersMap.put(columnIndex, new StringFilter(getColumnName(columnIndex), null, columnIndex));
                    break;
                case "Double":
                case "double":
                    filtersMap.put(columnIndex, new DoubleFilter(getColumnName(columnIndex), null, columnIndex));
                    break;
                case "Integer":
                case "int":
                    filtersMap.put(columnIndex, new IntegerFilter(getColumnName(columnIndex), null, columnIndex));
                    break;
                default:
                    System.out.println("no filter for type : " + m_descriptors[descriptorIndex].getPropertyType().getSimpleName());
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

    @Override
    public ArrayList<ExtraDataType> getExtraDataTypes() {
        return null;
    }

    @Override
    public PlotDataSpec getDataSpecAt(int i) {
        return null;
    }

}
