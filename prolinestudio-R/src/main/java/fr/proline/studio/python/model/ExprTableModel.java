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
package fr.proline.studio.python.model;

import fr.proline.studio.extendedtablemodel.ExtraDataType;
import fr.proline.studio.export.ExportFontData;
import fr.proline.studio.filter.BooleanFilter;
import fr.proline.studio.filter.DoubleFilter;
import fr.proline.studio.filter.Filter;
import fr.proline.studio.graphics.PlotInformation;
import fr.proline.studio.graphics.PlotType;
import fr.proline.studio.extendedtablemodel.ChildModelInterface;
import fr.proline.studio.table.DecoratedTableModel;
import fr.proline.studio.extendedtablemodel.GlobalTableModelInterface;
import fr.proline.studio.python.data.Col;
import fr.proline.studio.table.LazyData;
import fr.proline.studio.table.TableDefaultRendererManager;
import fr.proline.studio.table.renderer.DefaultRightAlignRenderer;
import fr.proline.studio.table.renderer.DoubleRenderer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableCellRenderer;


/**
 *
 * Table Model which is a result of an expressions (with modified columns or new columns added)
 * 
 * @author JM235353
 */
public class ExprTableModel extends DecoratedTableModel implements ChildModelInterface {

    private GlobalTableModelInterface m_parentModel;
    
    public static DoubleRenderer DOUBLE_RENDERER = new DoubleRenderer(new DefaultRightAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class)), 4, true, true);

    // for a column Index gives back a Map of values according to a class
    private HashMap<Integer, HashMap<Class, Object>> m_colValueMap = null;
    
    private HashMap<Integer, Col> m_modifiedColumns = null;

    private final ArrayList<Col> m_extraColumns = new ArrayList();
    private final ArrayList<TableCellRenderer> m_extraColumnRenderers = new ArrayList();
    
    
    private int m_bestXAxisColIndex = -1;
    private int m_bestYAxisColIndex = -1;
    private PlotType m_bestPlotType = null;
    

    

    public ExprTableModel(GlobalTableModelInterface parentModel) {
        m_parentModel = parentModel;
    }
    public ExprTableModel(Col column, TableCellRenderer colRenderer, GlobalTableModelInterface parentModel) {
        this(parentModel);
        addExtraColumn(column, colRenderer);
    }
    
    public final void addExtraColumn(Col column, TableCellRenderer colRenderer) {
        m_extraColumns.add(column);
        m_extraColumnRenderers.add(colRenderer);
    }
    
    public final void addExtraColumnInfo(int colIndex, Object colExtraInfo) {
        if (colExtraInfo == null) {
            return;
        }
        
        if (m_colValueMap == null) {
            m_colValueMap = new HashMap<>();
        }

        Class c = colExtraInfo.getClass();
        
        HashMap<Class, Object> map = m_colValueMap.get(colIndex);
        if (map == null) {
            map = new HashMap<>();
            m_colValueMap.put(colIndex, map);
        }
        map.put(c, colExtraInfo);

    }


    
    public final void modifyColumnValues(HashMap<Integer, Col> modifiedColumns, HashMap<Integer, Object> modifiedColumnsExtraInfo) {
        m_modifiedColumns = modifiedColumns;
        
        if (modifiedColumnsExtraInfo != null) {

            if (m_colValueMap == null) {
                m_colValueMap = new HashMap<>();
            }
        
            Iterator<Integer> it = modifiedColumnsExtraInfo.keySet().iterator();
            while (it.hasNext()) {
                Integer colIndex = it.next();
                Object value = modifiedColumnsExtraInfo.get(colIndex);
                Class c = value.getClass();
                
                HashMap<Class, Object> map = m_colValueMap.get(colIndex);
                if (map == null) {
                    map = new HashMap<>();
                    m_colValueMap.put(colIndex, map);
                }
                map.put(c, value);
            }
        }

    }
    
    @Override
    public int getRowCount() {
        return m_parentModel.getRowCount();
    }

    @Override
    public int getColumnCount() {
        return m_parentModel.getColumnCount()+ m_extraColumns.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
   
        int parentCount = m_parentModel.getColumnCount();
        if (columnIndex>=parentCount) {
            return m_extraColumns.get(columnIndex-parentCount).getValueAt(rowIndex);
        }
        
        if (m_modifiedColumns!=null) {
            Col c = m_modifiedColumns.get(columnIndex);
            if (c != null) {
                return c.getValueAt(rowIndex);
            }
        }
        
        return m_parentModel.getValueAt(rowIndex, columnIndex);

    }
    
    @Override
    public Class getColumnClass(int columnIndex) {
        if (columnIndex>=m_parentModel.getColumnCount()) {
            return m_extraColumns.get(columnIndex-m_parentModel.getColumnCount()).getColumnClass();
        }
        
        if ((m_modifiedColumns != null) && (m_modifiedColumns.containsKey(columnIndex))) {
            return Double.class;
        }
        
        return m_parentModel.getColumnClass(columnIndex);
    }

    @Override
    public String getColumnName(int columnIndex) {
        int parentCount = m_parentModel.getColumnCount();
        if (columnIndex>=parentCount) {
            return m_extraColumns.get(columnIndex-parentCount).getColumnName();
        }
         
        if (m_modifiedColumns!=null) {
            Col c = m_modifiedColumns.get(columnIndex);
            if (c != null) {
                return c.getColumnName();
            }
        }
         
        return m_parentModel.getColumnName(columnIndex);
    }
    
    @Override
    public Long getTaskId() {
        return m_parentModel.getTaskId();
    }

    @Override
    public LazyData getLazyData(int row, int col) {
        if (col>=m_parentModel.getColumnCount()) {
            return null; // no lazy data
        }
        
        if (m_modifiedColumns != null) {
            Col c = m_modifiedColumns.get(col);
            if (c != null) {
                return null; // no lazy data
            }
        }
        
        return m_parentModel.getLazyData(row, col);
    }

    @Override
    public void givePriorityTo(Long taskId, int row, int col) {
        if (col>=m_parentModel.getColumnCount()) {
            return; // nothing to do
        }
        m_parentModel.givePriorityTo(taskId, row, col);
    }

    @Override
    public void sortingChanged(int col) {
        if (col>=m_parentModel.getColumnCount()) {
            return; // nothing to do
        }
        m_parentModel.sortingChanged(col);
    }

    @Override
    public int getSubTaskId(int col) {
        if (col>=m_parentModel.getColumnCount()) {
            return -1;
        }
        return m_parentModel.getSubTaskId(col);
    }

    @Override
    public String getToolTipForHeader(int col) {
        if (col>=m_parentModel.getColumnCount()) {
            return getColumnName(col);
        }
        
        if (m_modifiedColumns != null) {
            Col c = m_modifiedColumns.get(col);
            if (c != null) {
                return c.getColumnName();
            }
        }
        
        return m_parentModel.getToolTipForHeader(col);
    }

    @Override
    public String getTootlTipValue(int row, int col) {
        if (col>=m_parentModel.getColumnCount()) {
            return null;
        }
        
        if (m_modifiedColumns != null) {
            Col c = m_modifiedColumns.get(col);
            if (c != null) {
                return c.getTooltip();
            }
        }
        
        return m_parentModel.getTootlTipValue(row, col);
    }

    @Override
    public String getDataColumnIdentifier(int columnIndex) {
        if (columnIndex >= m_parentModel.getColumnCount()) {
            return getColumnName(columnIndex);
        }
        
        if (m_modifiedColumns != null) {
            Col c = m_modifiedColumns.get(columnIndex);
            if (c != null) {
                return c.getColumnName();
            }
        }
        
        return m_parentModel.getDataColumnIdentifier(columnIndex);
    }

    @Override
    public Class getDataColumnClass(int columnIndex) {
        if (columnIndex >= m_parentModel.getColumnCount()) {
            return m_extraColumns.get(columnIndex-m_parentModel.getColumnCount()).getColumnClass();
        }
        
        if ((m_modifiedColumns != null) && (m_modifiedColumns.containsKey(columnIndex))) {
            return Double.class;
        }
        
        return m_parentModel.getDataColumnClass(columnIndex);
    }

    @Override
    public Object getDataValueAt(int rowIndex, int columnIndex) {
        
        int parentCount = m_parentModel.getColumnCount();
        if (columnIndex>=parentCount) {
            return m_extraColumns.get(columnIndex-parentCount).getValueAt(rowIndex);
        }
        
        if (m_modifiedColumns != null) {
            Col c = m_modifiedColumns.get(columnIndex);
            if (c != null) {
                return c.getValueAt(rowIndex);
            }
        }
        
        return m_parentModel.getDataValueAt(rowIndex, columnIndex);
    }

    @Override
    public int[] getKeysColumn() {
        return m_parentModel.getKeysColumn();
    }

    @Override
    public int getInfoColumn() {
        return m_parentModel.getInfoColumn();
    }

    @Override
    public void setName(String name) {
        m_parentModel.setName(name);
    }

    @Override
    public String getName() {
        return m_parentModel.getName();
    }

    @Override
    public Map<String, Object> getExternalData() {
        return m_parentModel.getExternalData();
    }

    @Override
    public PlotInformation getPlotInformation() {
        return m_parentModel.getPlotInformation();
    }

    @Override
    public void addFilters(LinkedHashMap<Integer, Filter> filtersMap) {
        if (filtersMap.isEmpty()) {
            m_parentModel.addFilters(filtersMap);
        }

        int parentCount = m_parentModel.getColumnCount();
        for (int i = 0; i < m_extraColumns.size(); i++) {
            Integer key = i + parentCount;

            if (!filtersMap.containsKey(key)) {
                Class c = m_extraColumns.get(i).getColumnClass();
                if (c.equals(Double.class)) {
                    filtersMap.put(key, new DoubleFilter(m_extraColumns.get(i).getColumnName(), null, parentCount+i));
                } else if (c.equals(Boolean.class)) {
                    filtersMap.put(key, new BooleanFilter(m_extraColumns.get(i).getColumnName(), null, parentCount+i));
                }
            }
        }

    }

    @Override
    public boolean isLoaded() {
        return m_parentModel.isLoaded();
    }

    @Override
    public int getLoadingPercentage() {
        return m_parentModel.getLoadingPercentage();
    }

    @Override
    public PlotType getBestPlotType() {
        if (m_bestPlotType != null) {
            return m_bestPlotType;
        }
        return m_parentModel.getBestPlotType();
    }

    @Override
    public int[] getBestColIndex(PlotType plotType) {
        if (m_bestPlotType != null) {
            int[] cols = new int[2];
            cols[0] = m_bestXAxisColIndex;
            cols[1] = m_bestYAxisColIndex;
            return cols;
        }
        return m_parentModel.getBestColIndex(plotType);
    }


    public void setBestPlot(int bestXAxisColIndex, int bestYAxisColIndex, PlotType bestPlotType) {
        m_bestXAxisColIndex = bestXAxisColIndex;
        m_bestYAxisColIndex = bestYAxisColIndex;
        m_bestPlotType = bestPlotType;
    }

    
    @Override
    public String getExportRowCell(int row, int col) {
        if (col >= m_parentModel.getColumnCount()) {
            Object o = getValueAt(row, col);
            if (o == null) {
                return "";
            }
            return o.toString();
        }
        
        if (m_modifiedColumns != null) {
            Col c = m_modifiedColumns.get(col);
            if (c != null) {
                Object d = c.getValueAt(row);
                if (d == null) {
                    return "";
                } else {
                    return d.toString();
                }
            }
        }
        
        return m_parentModel.getExportRowCell(row, col);
    }
    
    @Override
    public ArrayList<ExportFontData> getExportFonts(int row, int col) {
        if (col >= m_parentModel.getColumnCount()) {
            return null;
        }

        if (m_modifiedColumns != null) {
            Col c = m_modifiedColumns.get(col);
            if (c != null) {
                return null;
            }
        }

        return m_parentModel.getExportFonts(row, col);
    }

    @Override
    public String getExportColumnName(int col) {
        if (col >= m_parentModel.getColumnCount()) {
            return getColumnName(col);
        }
        
        if (m_modifiedColumns != null) {
            Col c = m_modifiedColumns.get(col);
            if (c != null) {
                return getColumnName(col);
            }
        }
        
        return m_parentModel.getExportColumnName(col);   
    }

    @Override
    public void setParentModel(GlobalTableModelInterface parentModel) {
        if (m_parentModel != null) {
            m_parentModel.removeTableModelListener(this);
        }
        m_parentModel = parentModel;
        m_parentModel.addTableModelListener(this);
    }

    @Override
    public GlobalTableModelInterface getParentModel() {
        return m_parentModel;
    }

    @Override
    public void tableChanged(TableModelEvent e) {
        // JPM.TODO : redo calculation
        fireTableDataChanged();
    }

    @Override
    public TableCellRenderer getRenderer(int row, int col) {

        int parentCount = m_parentModel.getColumnCount();
        if (col >= parentCount) {
            return m_extraColumnRenderers.get(col - parentCount);
        }
        
        if (m_modifiedColumns != null) {
            Col c = m_modifiedColumns.get(col);
            if (c != null) {
                return DOUBLE_RENDERER;
            }
        }
        
        return m_parentModel.getRenderer(row, col);   
    }

    @Override
    public GlobalTableModelInterface getFrozzenModel() {
        return this;
    }

    @Override
    public ArrayList<ExtraDataType> getExtraDataTypes() {
        return m_parentModel.getExtraDataTypes();
    }

    @Override
    public Object getValue(Class c) {
        return m_parentModel.getValue(c);
    }

    @Override
    public Object getRowValue(Class c, int row) {
        return m_parentModel.getRowValue(c, row);
    }
    
    @Override
    public Object getColValue(Class c, int col) {
        
        if (m_colValueMap != null) {
           HashMap<Class, Object> map = m_colValueMap.get(col);
           if (map != null) {
               Object value = map.get(c);
               if (value != null) {
                   return value;
               }
           }
        }
        
        int parentCount = m_parentModel.getColumnCount();
        if (col < parentCount) {
            return m_parentModel.getColValue(c, col);
        }
        
        return null;

    }
 
}
