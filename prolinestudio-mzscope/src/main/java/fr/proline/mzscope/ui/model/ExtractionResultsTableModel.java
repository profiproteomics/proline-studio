package fr.proline.mzscope.ui.model;

import fr.proline.mzscope.model.Chromatogram;
import fr.proline.mzscope.model.ExtractionResult;
import fr.proline.mzscope.model.ExtractionResult.Status;
import fr.proline.mzscope.model.IRawFile;
import fr.proline.mzscope.ui.StatusRenderer;
import fr.proline.studio.extendedtablemodel.ExtraDataType;
import fr.proline.studio.export.ExportModelUtilities;
import fr.proline.studio.export.ExportFontData;
import fr.proline.studio.filter.Filter;
import fr.proline.studio.graphics.PlotInformation;
import fr.proline.studio.graphics.PlotType;
import fr.proline.studio.table.DecoratedTableModel;
import fr.proline.studio.extendedtablemodel.GlobalTableModelInterface;
import fr.proline.studio.table.LazyData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.table.TableCellRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author CB205360
 */
public class ExtractionResultsTableModel extends DecoratedTableModel implements GlobalTableModelInterface {

    final private static Logger logger = LoggerFactory.getLogger(ExtractionResultsTableModel.class);
    
    private final HashMap<Integer, TableCellRenderer> m_rendererMap = new HashMap();
    private List<IRawFile> m_rawFiles = new ArrayList<>();
    
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

    public void setRawFiles(List<IRawFile> rawFiles) {
        this.m_rawFiles = rawFiles;
    }
    
    
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
        return Column.values().length + m_rawFiles.size();
    } 

    @Override
    public String getColumnName(int column) {
        int valuesCount = Column.values().length;
        return (column < valuesCount) ? Column.values()[column].getName() : m_rawFiles.get(column - valuesCount).getName();
    }

    @Override
    public Class getColumnClass(int col) {
        if (col == Column.MZ.id) {
            return Long.class;
        } else if (col == Column.STATUS.id) {
            return Status.class;
        }
        return Double.class;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        int valuesCount = Column.values().length;
        if (columnIndex < valuesCount) {
        switch (Column.values()[columnIndex]) {
            case MZ:
                return m_extractionResults.get(rowIndex).getMz();
            case STATUS:
                return m_extractionResults.get(rowIndex).getStatus();
        }
        } else {
            IRawFile rawFile = m_rawFiles.get(columnIndex - valuesCount);
            Map<IRawFile, Chromatogram> map = m_extractionResults.get(rowIndex).getChromatograms();
            return ((map != null) && (map.containsKey(rawFile))) ? map.get(rawFile).getMaxIntensity() : null;
        }
        return null;
    }

    @Override
    public String getToolTipForHeader(int columnIndex) {
        int valuesCount = Column.values().length;
        return (columnIndex < valuesCount) ? Column.values()[columnIndex].getTooltip() : "Intensity";
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
        
        if (renderer != null) m_rendererMap.put(col, renderer);
        
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
        return getColumnName(columnIndex);
    }

    @Override
    public Class getDataColumnClass(int columnIndex) {
        if (columnIndex == Column.MZ.id) {
            return Double.class;
        } else if (columnIndex == Column.STATUS.id) {
            return Status.class;
        }
        return Double.class;
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
    public int[] getBestColIndex(PlotType plotType) {
        return null;
    }


    @Override
    public String getExportRowCell(int row, int col) {
        if (col == Column.STATUS.id) {
            Status st = m_extractionResults.get(row).getStatus();
            if (st.equals(Status.DONE)) {
                return "done";
            } else if (st.equals(Status.REQUESTED)) {
                return "requested";
            } else {
                return "none";
            }
        }
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
