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
package fr.proline.studio.extendedtablemodel;

import fr.proline.studio.export.ExportFontData;
import fr.proline.studio.filter.Filter;
import fr.proline.studio.filter.FilterTableModelInterface;
import fr.proline.studio.filter.FilterTableModel;
import fr.proline.studio.graphics.PlotDataSpec;
import fr.proline.studio.graphics.PlotInformation;
import fr.proline.studio.graphics.PlotType;
import fr.proline.studio.table.LazyData;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import org.jdesktop.swingx.JXTable;

/**
 *
 * @author JM235353
 */
public class CompoundTableModel extends AbstractTableModel implements GlobalTableModelInterface, TableModelListener, FilterTableModelInterface {
    
    private GlobalTableModelInterface m_baseModel = null;
    private GlobalTableModelInterface m_lastModel = null;
    private FilterTableModel m_filterModel = null;

    public CompoundTableModel(GlobalTableModelInterface baseModel, boolean filterCapability) {
        setBaseModel(baseModel);
    }
    
    public final void setBaseModel(GlobalTableModelInterface baseModel) {
        m_baseModel = baseModel;
        m_lastModel = baseModel;

        if (m_baseModel != null) {
            ((GlobalTableModelInterface) m_baseModel).addTableModelListener(this);
        }
    }

    public void addModel(ChildModelInterface model) {
        
        if (m_filterModel != null) {
            m_filterModel.getTableModelSource().removeTableModelListener(m_filterModel);
            model.setParentModel(m_filterModel.getTableModelSource());
            model.addTableModelListener(m_filterModel);
            m_filterModel.setTableModelSource(model);
            filter();
        } else {
            ((GlobalTableModelInterface) m_lastModel).removeTableModelListener(this);
            model.setParentModel(m_lastModel);
            model.addTableModelListener(this);
            m_lastModel = model;
        }
  
        fireTableStructureChanged();
    }
    
    public GlobalTableModelInterface getBaseModel() {
        return m_baseModel;
    }
    
    public GlobalTableModelInterface getLastNonFilterModel() {
        if (m_filterModel == null) {
            return m_lastModel;
        } else {
            return m_filterModel.getTableModelSource();
        }
    }
    
    public int convertCompoundRowToBaseModelRow(int row) {
        if (m_filterModel == null) {
            return row;
        }
        return m_filterModel.convertRowToOriginalModel(row);
    }
    public int convertBaseModelRowToCompoundRow(int row) {
        if (m_filterModel == null) {
            return row;
        }
         return m_filterModel.convertOriginalModelToRow(row);
    }
    
    @Override
    public int getRowCount() {
        if (m_lastModel == null) {
            return 0;
        }
        return m_lastModel.getRowCount();
    }

    @Override
    public int getColumnCount() {
        if (m_lastModel == null) {
            return 0;
        }
        return m_lastModel.getColumnCount();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return m_lastModel.getValueAt(rowIndex, columnIndex);
    }

    @Override
    public String getColumnName(int column) {
        return m_lastModel.getColumnName(column);
    }
    
    @Override
    public Class getColumnClass(int columnIndex) {
        return m_lastModel.getColumnClass(columnIndex);
    }
    

    
    @Override
    public void initFilters() {
        if (m_filterModel == null) {
            m_filterModel = new FilterTableModel(m_lastModel);
            m_lastModel.removeTableModelListener(this);
            m_lastModel = m_filterModel;
            m_filterModel.addTableModelListener(this);
        }
    }

    @Override
    public LinkedHashMap<Integer, Filter> getFilters() {
        if (m_filterModel == null) {
            initFilters();
        }
        return m_filterModel.getFilters();
    }

    @Override
    public Filter getColumnFilter(int col) {
        if (m_filterModel == null) {
            return null;
        }
        return m_filterModel.getColumnFilter(col);
    }

    @Override
    public boolean filter() {
        if (m_filterModel == null) {
            return false;
        }
        m_filterModel.filter();
        return true;
    }

    @Override
    public boolean filter(int row, int col) {
        if (m_filterModel == null) {
            return false;
        }
        return m_filterModel.filter(row, col);
    }

    @Override
    public boolean filter(int row) {
        if (m_filterModel == null) {
            return false;
        }
        return m_filterModel.filter(row);
    }

    @Override
    public int convertRowToOriginalModel(int row) {
        if (m_filterModel == null) {
            return row;
        }
        return m_filterModel.convertRowToOriginalModel(row);
    }

    @Override
    public int convertOriginalModelToRow(int row) {
        if (m_filterModel == null) {
            return row;
        }
        return m_filterModel.convertOriginalModelToRow(row);
    }
    
    @Override
    public void restrain(HashSet<Integer> restrainRowSet) {
        initFilters();
        m_filterModel.restrain(restrainRowSet);
    }

    @Override
    public HashSet<Integer> getRestrainRowSet() {
        if (m_filterModel == null) {
            return null;
        }
        return m_filterModel.getRestrainRowSet();
    }

    @Override
    public boolean hasRestrain() {
        if (m_filterModel == null) {
            return false;
        }
        return m_filterModel.hasRestrain();
    }
    
    @Override
    public int search(JXTable table, Filter f, boolean newSearch) {
        initFilters();
        return m_filterModel.search(table, f, newSearch);
    }

    @Override
    public boolean isLoaded() {
        if (m_lastModel == null) {
            return false;
        }
        
        return m_lastModel.isLoaded();
    }

    @Override
    public int getLoadingPercentage() {
        if (m_lastModel == null) {
            return 0;
        }
        return m_lastModel.getLoadingPercentage();
    }

    @Override
    public String getDataColumnIdentifier(int columnIndex) {
        return m_lastModel.getDataColumnIdentifier(columnIndex);
    }

    @Override
    public Class getDataColumnClass(int columnIndex) {
        return m_lastModel.getDataColumnClass(columnIndex);
    }

    @Override
    public Object getDataValueAt(int rowIndex, int columnIndex) {
        return m_lastModel.getDataValueAt(rowIndex, columnIndex);
    }

    @Override
    public int[] getKeysColumn() {
        return m_lastModel.getKeysColumn();
    }

    @Override
    public int getInfoColumn() {
        return m_lastModel.getInfoColumn();
    }

    @Override
    public void setName(String name) {
       m_lastModel.setName(name);
    }

    @Override
    public String getName() {
        return m_lastModel.getName();
    }

    @Override
    public Map<String, Object> getExternalData() {
        return m_lastModel.getExternalData();
    }

    @Override
    public PlotInformation getPlotInformation() {
        return m_lastModel.getPlotInformation();
    }

    @Override
    public void tableChanged(TableModelEvent e) {
        fireTableChanged(e);
    }

    @Override
    public int getSubTaskId(int col) {
        return m_lastModel.getSubTaskId(col);
    }

    @Override
    public Long getTaskId() {
        return m_lastModel.getTaskId();
    }

    @Override
    public LazyData getLazyData(int row, int col) {
        return m_lastModel.getLazyData(row, col);
    }

    @Override
    public void givePriorityTo(Long taskId, int row, int col) {
        m_lastModel.givePriorityTo(taskId, row, col);
    }

    @Override
    public void sortingChanged(int col) {
        m_lastModel.sortingChanged(col);
    }

    @Override
    public String getToolTipForHeader(int col) {
        return m_lastModel.getToolTipForHeader(col);
    }
 
    @Override
    public String getTootlTipValue(int row, int col) {
        return m_lastModel.getTootlTipValue(row, col);
    }
    

    @Override
    public void addFilters(LinkedHashMap<Integer, Filter> filtersMap) {
        m_lastModel.addFilters(filtersMap);
    }

    @Override
    public PlotType getBestPlotType() {
        return m_lastModel.getBestPlotType();
    }

    @Override
    public int[] getBestColIndex(PlotType plotType) {
        return m_lastModel.getBestColIndex(plotType);
    }


    @Override
    public String getExportRowCell(int row, int col) {
        return m_lastModel.getExportRowCell(row, col);
    }

    @Override
    public ArrayList<ExportFontData> getExportFonts(int row, int col) {
        return m_lastModel.getExportFonts(row, col);
    }
    
    @Override
    public String getExportColumnName(int col) {
        return m_lastModel.getExportColumnName(col);
    }

    @Override
    public void setTableModelSource(GlobalTableModelInterface tableModelSource) {
        // nothing to do
    }

    @Override
    public GlobalTableModelInterface getTableModelSource() {
        return null;
    }

    @Override
    public TableCellRenderer getRenderer(int row, int col) {
        return m_lastModel.getRenderer(row, col);
    }

    @Override
    public long row2UniqueId(int rowIndex) {
        return m_lastModel.row2UniqueId(rowIndex);
    }

    @Override
    public int uniqueId2Row(long id) {
        return m_lastModel.uniqueId2Row(id);
    }

    @Override
    public GlobalTableModelInterface getFrozzenModel() {
        CompoundTableModel copyModel = new CompoundTableModel(null, true);
        
        ArrayList<GlobalTableModelInterface> modelList = new ArrayList<>();
        GlobalTableModelInterface curModel = m_lastModel;
        FilterTableModel filterModel = null;
        while (curModel != null) {
            GlobalTableModelInterface frozzenModel = curModel.getFrozzenModel();
            modelList.add(0, frozzenModel);
            if (frozzenModel instanceof FilterTableModel) {
                filterModel = (FilterTableModel) frozzenModel;
            }
            if (curModel instanceof ChildModelInterface) {
                curModel = ((ChildModelInterface) curModel).getParentModel();
            } else {
                curModel = null;
            }
        }
        
        if (modelList.isEmpty()) {
            return copyModel;
        }
        
        for (int i=modelList.size()-1;i>=1;i--) {
            GlobalTableModelInterface prevModel = modelList.get(i-1);
            GlobalTableModelInterface model = modelList.get(i);
            if (model instanceof ChildModelInterface) {
                ((ChildModelInterface) model).setParentModel(prevModel);
                prevModel.addTableModelListener((TableModelListener)model);
            }
        }
        
        copyModel.m_baseModel = modelList.get(0);
        copyModel.m_filterModel = filterModel;
        if (filterModel != null) {
            filterModel.setTableModelSource(((ChildModelInterface) filterModel).getParentModel());
        }
        copyModel.m_lastModel = modelList.get(modelList.size()-1);

        copyModel.m_lastModel.addTableModelListener(this);
        
        return copyModel;
    }
    
    @Override
    public ArrayList<ExtraDataType> getExtraDataTypes() {
        return m_lastModel.getExtraDataTypes();
    }

    @Override
    public Object getValue(Class c) {
        return m_lastModel.getValue(c);
    }

    @Override
    public Object getRowValue(Class c, int rowIndex) {

        return m_lastModel.getRowValue(c, rowIndex);
    }
    
    @Override
    public Object getColValue(Class c, int col) {
        return m_lastModel.getColValue(c, col);
    }
    
    @Override
    public void addSingleValue(Object v) {
        m_lastModel.addSingleValue(v);
    }

    @Override
    public Object getSingleValue(Class c) {
        return m_lastModel.getSingleValue(c);
    }
    
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return m_lastModel.isCellEditable(rowIndex, columnIndex);
    }
    
    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        m_lastModel.setValueAt(aValue, rowIndex, columnIndex);
    }

    @Override
    public PlotDataSpec getDataSpecAt(int i) {
        return m_lastModel.getDataSpecAt(i);
    }

}
