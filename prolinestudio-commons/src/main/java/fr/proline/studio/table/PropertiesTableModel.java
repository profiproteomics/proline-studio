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
package fr.proline.studio.table;

import fr.proline.studio.extendedtablemodel.GlobalTableModelInterface;
import fr.proline.studio.extendedtablemodel.ExtraDataType;
import fr.proline.studio.export.ExportFontData;
import fr.proline.studio.filter.ConvertValueInterface;
import fr.proline.studio.filter.Filter;
import fr.proline.studio.filter.StringDiffFilter;
import fr.proline.studio.graphics.PlotInformation;
import fr.proline.studio.graphics.PlotType;
import fr.proline.studio.table.DataGroup.GroupObject;
import fr.proline.studio.table.renderer.PropertiesRenderer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.table.TableCellRenderer;

/**
 * Model to display properties. This model is a hack: it allows to not follow the general policy
 * of renderers (one renderer for each data type or for each column)
 * @author JM235353
 */
public abstract class PropertiesTableModel extends DecoratedTableModel implements GlobalTableModelInterface {
    
    protected PropertiesRenderer m_propertiesOkRenderer;
    protected PropertiesRenderer m_propertiesNonOkRenderer;    
    protected ArrayList<String> m_datasetNameArray = null;
    protected ArrayList<DataGroup> m_dataGroupList = null;
    private HashMap<Integer, DataGroup> m_dataGroupMap = null;
    protected int m_rowCount = -1;
    
    protected String m_modelName; 
    
    protected boolean m_loaded = false;
    
    public PropertiesTableModel() {
        m_propertiesOkRenderer = new PropertiesRenderer(false);
        m_propertiesNonOkRenderer = new PropertiesRenderer(true);
    }
    
    public HashMap<Integer, DataGroup> getDataGroupMap() {
        return m_dataGroupMap;
    }
    
    @Override
    public String getColumnName(int col) {
        if (col <= DataGroup.COLTYPE_PROPERTY_NAME) {
            return DataGroup.COLUMN_NAMES[col];
        }
        return m_datasetNameArray.get(col - 2);
    }
    
        
    @Override
    public int getColumnCount() {
        if (m_datasetNameArray == null) {
            return 2;
        }
        return 2+m_datasetNameArray.size();
    }
    
    @Override
    public int getRowCount() {
        if (m_rowCount != -1) {
            return m_rowCount;
        }
        if (m_dataGroupList == null) {
            return 0;
        }

        // calculate row count
        m_rowCount = 0;
        m_dataGroupMap = new HashMap<>();
        for (DataGroup dataGroup : m_dataGroupList) {
            int start = m_rowCount;
            m_rowCount += dataGroup.getRowCount();
            for (int i = start; i < m_rowCount; i++) {
                m_dataGroupMap.put(i, dataGroup);
            }
        }

        return m_rowCount;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        DataGroup dataGroup = m_dataGroupMap.get(rowIndex);
        GroupObject v = (GroupObject) dataGroup.getValueAt(rowIndex, columnIndex);
        /*if (v == null) {
            v = "";
        }*/
        return v;
    }

    @Override
    public String getToolTipForHeader(int col) {
        return getColumnName(col);
    }

    @Override
    public String getTootlTipValue(int row, int col) {
        return null;
    }

    @Override
    public TableCellRenderer getRenderer(int row, int col) {

        GroupObject groupObject = (GroupObject) getValueAt(row, col);

        DataGroup dataGroup = groupObject.m_group;
        boolean isFirstRow = groupObject.m_coloredRow;
        if (isFirstRow) {
            return dataGroup.getRenderer(row);
        } else if (col == DataGroup.COLTYPE_GROUP_NAME) {
            return dataGroup.getRenderer(row);
        } else if (col == DataGroup.COLTYPE_PROPERTY_NAME) {
            int nbCol = getColumnCount();
            if (nbCol > DataGroup.COLTYPE_PROPERTY_NAME + 2) {
                String value = ((GroupObject) getValueAt(row, DataGroup.COLTYPE_PROPERTY_NAME + 1)).stringForFilter();
                for (int i = DataGroup.COLTYPE_PROPERTY_NAME + 2; i < nbCol; i++) {
                    String valueCur = ((GroupObject) getValueAt(row, i)).stringForFilter();
                    if ((value == null) && (valueCur == null)) {
                        continue;
                    }
                    if (((value != null) && (valueCur == null)) || ((value == null) && (valueCur != null)) || (value.compareTo(valueCur) != 0)) {
                        return m_propertiesNonOkRenderer;
                    }
                }
            }
        }

        return m_propertiesOkRenderer;

    }
    
    @Override
    public GlobalTableModelInterface getFrozzenModel() {
        return this;
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
        return; // not used
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
        return DataGroup.COLTYPE_GROUP_NAME;
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
    public Map<String, Object> getExternalData() {
        return null;
    }

    @Override
    public PlotInformation getPlotInformation() {
        return null;
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
    public Object getRowValue(Class c, int row) {
        return null;
    }
    
    @Override
    public Object getColValue(Class c, int col) {
        return null;
    }

    @Override
    public void addFilters(LinkedHashMap<Integer, Filter> filtersMap) {
        
        ConvertValueInterface convertValueInterface = new ConvertValueInterface() {
            @Override
            public Object convertValue(Object o) {
                return ((DataGroup.GroupObject) o).m_valueFiltering;
            }
            
        };
        
        filtersMap.put(DataGroup.COLTYPE_GROUP_NAME, new StringDiffFilter(getColumnName(DataGroup.COLTYPE_GROUP_NAME), convertValueInterface, DataGroup.COLTYPE_GROUP_NAME));
        filtersMap.put(DataGroup.COLTYPE_PROPERTY_NAME, new StringDiffFilter(getColumnName(DataGroup.COLTYPE_PROPERTY_NAME), convertValueInterface, DataGroup.COLTYPE_PROPERTY_NAME));
    }

    @Override
    public boolean isLoaded() {
        return m_loaded;
    }

    @Override
    public int getLoadingPercentage() {
        if (m_loaded) {
            return 100;
        } else {
            return 0;
        }
    }

    @Override
    public PlotType getBestPlotType() {
        return null;
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
    public ArrayList<ExportFontData> getExportFonts(int row, int col) {
        return null;
    }

    @Override
    public String getExportColumnName(int col) {
        return getColumnName(col);
    }
}
