/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.extendedtablemodel;

import fr.proline.studio.export.ExportFontData;
import fr.proline.studio.filter.Filter;
import fr.proline.studio.graphics.PlotInformation;
import fr.proline.studio.graphics.PlotType;
import fr.proline.studio.table.LazyData;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author CB205360
 */
public class ExpansionTableModel implements GlobalTableModelInterface {

    private GlobalTableModelInterface m_slaveModel = null;
    private GlobalTableModelInterface m_masterModel = null;

    public ExpansionTableModel(GlobalTableModelInterface model1, GlobalTableModelInterface model2) {
        m_masterModel = model1;
        m_slaveModel = model2;
    }


    @Override
    public GlobalTableModelInterface getFrozzenModel() {
        return this;
    }

    @Override
    public Long getTaskId() {
        return m_masterModel.getTaskId();
    }

    @Override
    public LazyData getLazyData(int row, int col) {
        if (col >= m_masterModel.getColumnCount()) {
            return m_slaveModel.getLazyData(row, col - m_masterModel.getColumnCount());
        }
        return m_masterModel.getLazyData(row, col);
    }

    @Override
    public void givePriorityTo(Long taskId, int row, int col) {
        if (col >= m_masterModel.getColumnCount()) {
            m_slaveModel.givePriorityTo(taskId, row, col - m_masterModel.getColumnCount());
        }
        m_masterModel.givePriorityTo(taskId, row, col);
    }

    @Override
    public void sortingChanged(int col) {
        if (col >= m_masterModel.getColumnCount()) {
            m_slaveModel.sortingChanged(col - m_masterModel.getColumnCount());
        }
        m_masterModel.sortingChanged(col);
    }

    @Override
    public int getSubTaskId(int col) {
        if (col >= m_masterModel.getColumnCount()) {
            return m_slaveModel.getSubTaskId(col - m_masterModel.getColumnCount());
        }
        return m_masterModel.getSubTaskId(col);
    }

    @Override
    public String getToolTipForHeader(int col) {
        if (col >= m_masterModel.getColumnCount()) {
            return m_slaveModel.getToolTipForHeader(col - m_masterModel.getColumnCount());
        }
        return m_masterModel.getToolTipForHeader(col);
    }

    @Override
    public String getTootlTipValue(int row, int col) {
        if (col >= m_masterModel.getColumnCount()) {
            return m_slaveModel.getTootlTipValue(row, col - m_masterModel.getColumnCount());
        }
        return m_masterModel.getTootlTipValue(row, col);
    }

    @Override
    public TableCellRenderer getRenderer(int row, int col) {
        if (col >= m_masterModel.getColumnCount()) {
            return m_slaveModel.getRenderer(row, col - m_masterModel.getColumnCount());
        }
        return m_masterModel.getRenderer(row, col);
    }

    @Override
    public int getRowCount() {
        return m_masterModel.getRowCount();
    }

    @Override
    public int getColumnCount() {
        return m_masterModel.getColumnCount() + m_slaveModel.getColumnCount();
    }

    @Override
    public String getDataColumnIdentifier(int columnIndex) {
        if (columnIndex >= m_masterModel.getColumnCount()) {
            return m_slaveModel.getDataColumnIdentifier(columnIndex - m_masterModel.getColumnCount());
        }
        return m_masterModel.getDataColumnIdentifier(columnIndex);
    }

    @Override
    public Class getDataColumnClass(int columnIndex) {
        if (columnIndex >= m_masterModel.getColumnCount()) {
            return m_slaveModel.getDataColumnClass(columnIndex - m_masterModel.getColumnCount());
        }
        return m_masterModel.getDataColumnClass(columnIndex);
    }

    @Override
    public Object getDataValueAt(int rowIndex, int columnIndex) {
        if (columnIndex >= m_masterModel.getColumnCount()) {
            return m_slaveModel.getDataValueAt(rowIndex, columnIndex - m_masterModel.getColumnCount());
        }
        return m_masterModel.getDataValueAt(rowIndex, columnIndex);
    }

    @Override
    public int[] getKeysColumn() {
        return m_masterModel.getKeysColumn();
    }

    @Override
    public int getInfoColumn() {
        return m_masterModel.getInfoColumn();
    }

    @Override
    public void setName(String name) {
        m_masterModel.setName(name);
    }

    @Override
    public String getName() {
        return m_masterModel.getName();
    }

    @Override
    public Map<String, Object> getExternalData() {
        return m_masterModel.getExternalData();
    }

    @Override
    public PlotInformation getPlotInformation() {
        return m_masterModel.getPlotInformation();
    }

    @Override
    public long row2UniqueId(int rowIndex) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int uniqueId2Row(long id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ArrayList<ExtraDataType> getExtraDataTypes() {
        return m_masterModel.getExtraDataTypes();
    }

    @Override
    public Object getValue(Class c) {
        return m_masterModel.getValue(c);
    }

    @Override
    public Object getRowValue(Class c, int row) {
        return m_masterModel.getRowValue(c, row);
    }

    @Override
    public Object getColValue(Class c, int col) {
        if (col >= m_masterModel.getColumnCount()) {
            return m_slaveModel.getColValue(c, col - m_masterModel.getColumnCount());
        }
        return m_masterModel.getColValue(c, col);
    }

    @Override
    public void addSingleValue(Object v) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object getSingleValue(Class c) {
        return m_masterModel.getSingleValue(c);
    }

    @Override
    public String getColumnName(int columnIndex) {
        if (columnIndex >= m_masterModel.getColumnCount()) {
            return m_slaveModel.getColumnName(columnIndex - m_masterModel.getColumnCount());
        }
        return m_masterModel.getColumnName(columnIndex);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex >= m_masterModel.getColumnCount()) {
            return m_slaveModel.getColumnClass(columnIndex - m_masterModel.getColumnCount());
        }
        return m_masterModel.getColumnClass(columnIndex);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (columnIndex >= m_masterModel.getColumnCount()) {
            return m_slaveModel.isCellEditable(rowIndex, columnIndex - m_masterModel.getColumnCount());
        }
        return m_masterModel.isCellEditable(rowIndex, columnIndex);
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (columnIndex >= m_masterModel.getColumnCount()) {
            return m_slaveModel.getValueAt(rowIndex, columnIndex - m_masterModel.getColumnCount());
        }
        return m_masterModel.getValueAt(rowIndex, columnIndex);
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (columnIndex >= m_masterModel.getColumnCount()) {
            m_slaveModel.setValueAt(aValue, rowIndex, columnIndex - m_masterModel.getColumnCount());
        }
        m_masterModel.setValueAt(aValue, rowIndex, columnIndex);
    }

    @Override
    public void addTableModelListener(TableModelListener l) {
        m_masterModel.addTableModelListener(l);
        m_slaveModel.addTableModelListener(l);
    }

    @Override
    public void removeTableModelListener(TableModelListener l) {
        m_masterModel.removeTableModelListener(l);
        m_slaveModel.removeTableModelListener(l);
    }

    @Override
    public void addFilters(LinkedHashMap<Integer, Filter> filtersMap) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isLoaded() {
        return m_masterModel.isLoaded();
    }

    @Override
    public int getLoadingPercentage() {
        return m_masterModel.getLoadingPercentage();
    }

    @Override
    public PlotType getBestPlotType() {
        return m_masterModel.getBestPlotType();
    }

    @Override
    public int[] getBestColIndex(PlotType plotType) {
        return m_masterModel.getBestColIndex(plotType);
    }

    @Override
    public String getExportRowCell(int row, int col) {
        if (col >= m_masterModel.getColumnCount()) {
            return m_slaveModel.getExportRowCell(row, col - m_masterModel.getColumnCount());
        }
        return m_masterModel.getExportRowCell(row, col);
    }

    @Override
    public ArrayList<ExportFontData> getExportFonts(int row, int col) {
        if (col >= m_masterModel.getColumnCount()) {
            return m_slaveModel.getExportFonts(row, col - m_masterModel.getColumnCount());
        }
        return m_masterModel.getExportFonts(row, col);
    }

    @Override
    public String getExportColumnName(int col) {
        if (col >= m_masterModel.getColumnCount()) {
            return m_slaveModel.getExportColumnName(col - m_masterModel.getColumnCount());
        }
        return m_masterModel.getExportColumnName(col);
    }

}
