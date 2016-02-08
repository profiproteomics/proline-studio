package fr.proline.mzscope.ui.model;

import fr.proline.mzscope.model.ExtractionResult;
import fr.proline.mzscope.model.ExtractionResult.Status;
import fr.proline.mzscope.ui.StatusRenderer;
import fr.proline.studio.comparedata.ExtraDataType;
import fr.proline.studio.filter.Filter;
import fr.proline.studio.graphics.PlotInformation;
import fr.proline.studio.graphics.PlotType;
import fr.proline.studio.table.DecoratedTableModel;
import fr.proline.studio.table.GlobalTableModelInterface;
import fr.proline.studio.table.LazyData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.table.TableCellRenderer;


/**
 *
 * @author CB205360
 */
public class ExtractionResultsTableModel extends DecoratedTableModel implements GlobalTableModelInterface {

    private final HashMap<Integer, TableCellRenderer> m_rendererMap = new HashMap();



    public enum Column {

        MZ(0, "m/z", "m/z extration value"),
        STATUS(1, "status", "Extraction status : NONE, REQUESTED or DONE");

        private final String name;
        private final String tooltip;
        private final int id;

        private Column(int id, String name, String tooltip) {
            this.id = id;
            this.name = name;
            this.tooltip = tooltip;
        }

        public String getName() {
            return name;
        }

        public String getTooltip() {
            return tooltip;
        }

        public int getId() {
            return id;
        }

    }

    private List<ExtractionResult> m_extractionResults = new ArrayList<>(0);
    
    public ExtractionResult getExtractionResultAt(int rowId){
        if (rowId > -1 && rowId < m_extractionResults.size()){
            return m_extractionResults.get(rowId);
        }else {
            return null;
        }
    }

    public void setExtractions(List<ExtractionResult> extractionResults) {
        this.m_extractionResults = extractionResults;
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return m_extractionResults.size();
    }

    @Override
    public int getColumnCount() {
        return Column.values().length;
    }

    @Override
    public String getColumnName(int column) {
        return Column.values()[column].getName();
    }

    @Override
    public Class getColumnClass(int col) {
        if (col == Column.MZ.id) {
            return Long.class;
        } else if (col == Column.STATUS.id) {
            return Status.class;
        }
        return LazyData.class;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        switch (Column.values()[columnIndex]) {
            case MZ:
                return m_extractionResults.get(rowIndex).getMz();
            case STATUS:
                return m_extractionResults.get(rowIndex).getStatus();
        }
        return null;
    }

    @Override
    public String getToolTipForHeader(int columnIndex) {
        return Column.values()[columnIndex].getTooltip();
    }

    @Override
    public String getTootlTipValue(int row, int col) {
        return null;
    }

    @Override
    public TableCellRenderer getRenderer(int row, int col) {
        if (m_rendererMap.containsKey(col)) {
            return m_rendererMap.get(col);
        }

        TableCellRenderer renderer = null;

        if (col == Column.STATUS.id){
            renderer = new StatusRenderer();
        }
        
        m_rendererMap.put(col, renderer);
        return renderer;
    }

    @Override
    public GlobalTableModelInterface getFrozzenModel() {
        return this;
    }

    @Override
    public Long getTaskId() {
        return (long) -1;
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
        return Column.values()[columnIndex].getName();
    }

    @Override
    public Class getDataColumnClass(int columnIndex) {
        if (columnIndex == Column.MZ.id) {
            return Double.class;
        } else if (columnIndex == Column.STATUS.id) {
            return Status.class;
        }
        return null; // should never happen
    }

    @Override
    public Object getDataValueAt(int rowIndex, int columnIndex) {
        return getValueAt(rowIndex, columnIndex);
    }

    @Override
    public int[] getKeysColumn() {
        int[] keys = {Column.MZ.id};
        return keys;
    }

    @Override
    public int getInfoColumn() {
        return Column.MZ.id;
    }

    @Override
    public void setName(String name) {

    }

    @Override
    public String getName() {
        return "Extraction Results";
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
    public void addFilters(LinkedHashMap<Integer, Filter> filtersMap) {

    }

    @Override
    public boolean isLoaded() {
        return true;
    }

    @Override
    public int getLoadingPercentage() {
        return 1;
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
    public String getExportRowCell(int row, int col) {
        return null;
    }

    @Override
    public String getExportColumnName(int col) {
        return Column.values()[col].getName();
    }
    
    @Override
    public ArrayList<ExtraDataType> getExtraDataTypes() {
        ArrayList<ExtraDataType> list = new ArrayList<>();
        list.add(new ExtraDataType(ExtractionResult.class, true));
        registerSingleValuesAsExtraTypes(list);
        return list;
    }

    @Override
    public Object getValue(Class c) {
        return null;
    }

    @Override
    public Object getRowValue(Class c, int row) {
        if (c.equals(ExtractionResult.class)) {
            return m_extractionResults.get(row);
        }
        return null;
    }
    
    @Override
    public Object getColValue(Class c, int col) {
        return null;
    }


}
