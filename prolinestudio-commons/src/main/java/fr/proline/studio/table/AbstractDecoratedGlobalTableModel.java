/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.table;

import fr.proline.studio.export.ExportFontData;
import fr.proline.studio.export.ExportModelUtilities;
import fr.proline.studio.extendedtablemodel.ExtraDataType;
import fr.proline.studio.extendedtablemodel.GlobalTableModelInterface;
import fr.proline.studio.filter.Filter;
import fr.proline.studio.graphics.PlotInformation;
import fr.proline.studio.graphics.PlotType;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author KX257079
 */
public abstract class AbstractDecoratedGlobalTableModel<T> extends DecoratedTableModel implements GlobalTableModelInterface {

    protected String[] m_columnNames;
    protected String[] m_columnTooltips;
    protected List<T> m_entities = new ArrayList<>();

    @Override
    public abstract Object getValueAt(int rowIndex, int columnIndex);

    @Override
    public abstract TableCellRenderer getRenderer(int row, int col);

    public void setData(List<T> list) {
        this.m_entities = list;
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return m_entities.size();
    }

    @Override
    public int getColumnCount() {
        return m_columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return m_columnNames[column];
    }

    //**  DecoratedTableModelInterface **//
    @Override
    public String getToolTipForHeader(int col) {
        return m_columnTooltips[col];
    }

    //***************** Next is specific GlobalTableModelInterface******************//
    @Override
    public abstract Object getRowValue(Class c, int row);

    /**
     * return default keys columns (used for join)
     *
     * @return
     */
    @Override
    public abstract int[] getKeysColumn();

    @Override
    public abstract void addFilters(LinkedHashMap<Integer, Filter> filtersMap);

    ///***************** Next is generic GlobalTableModelInterface******************///
    @Override
    public String getTootlTipValue(int row, int col) {
        return null;
    }

    @Override
    public GlobalTableModelInterface getFrozzenModel() {
        return this;
    }

    @Override
    public Long getTaskId() {
        return -1L; // not used
    }

    @Override
    public LazyData getLazyData(int row, int col) {
        return null; // not used
    }

    @Override
    public void givePriorityTo(Long taskId, int row, int col) {
        // not used
    }

    @Override
    public void sortingChanged(int col) {
        // not used
    }

    @Override
    public int getSubTaskId(int col) {
        return -1; // not used
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
    public int getInfoColumn() {
        return -1;
    }

    String m_modelName;

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
        return getSingleValue(c);
    }

    @Override
    public Object getColValue(Class c, int col) {
        return null;
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
        return null;
    }

    @Override
    public int[] getBestColIndex(PlotType plotType) {
        return null;
    }

    @Override
    public String getExportRowCell(int row, int col) {
        return ExportModelUtilities.getExportRowCell(this, row, col);
    }

    @Override
    public ArrayList<ExportFontData> getExportFonts(int row, int col) {
        return null;
    }

    @Override
    public String getExportColumnName(int col) {
        return getColumnName(col);
    }

    //*********************** END of GlobalTableModelInterface *******************//
}
