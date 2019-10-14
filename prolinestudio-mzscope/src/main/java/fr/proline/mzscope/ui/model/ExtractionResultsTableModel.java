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

import fr.proline.mzscope.model.AnnotatedChromatogram;
import fr.proline.mzscope.model.IChromatogram;
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

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
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

    public enum EColumn {
        INTENSITY,
        SCANS_COUNT
    }

    public enum Column {

        MZ(0, "m/z", "m/z extration value"),
        STATUS(1, "status", "Extraction status : NONE, REQUESTED or DONE");

        private final String name;
        private final String tooltip;
        private final int id;

        Column(int id, String name, String tooltip) {
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

    private Pair<IRawFile, EColumn> getColumnContent(int columnIndex) {
        int eCount = EColumn.values().length;
        int cCount = Column.values().length;

        int rawFileIdx = (columnIndex - cCount) / eCount;
        int columnIdx = (columnIndex - cCount - rawFileIdx*eCount) % eCount;

        return new ImmutablePair(m_rawFiles.get(rawFileIdx), EColumn.values()[columnIdx]);
    }

    @Override
    public int getRowCount() {
        return m_extractionResults.size();
    }

    @Override
    public int getColumnCount() {
        return Column.values().length + m_rawFiles.size()*EColumn.values().length;
    } 

    @Override
    public String getColumnName(int column) {
        if (column < Column.values().length)
            return Column.values()[column].getName();
        Pair<IRawFile, EColumn> index = getColumnContent(column);
        return index.getLeft().getName()+"-"+index.getRight().name();
    }

    @Override
    public Class getColumnClass(int col) {
        if (col == Column.MZ.id) {
            return Double.class;
        } else if (col == Column.STATUS.id) {
            return Status.class;
        }
        Pair<IRawFile, EColumn> index = getColumnContent(col);
        if (index.getRight() == EColumn.INTENSITY)
            return Double.class;
        return Integer.class;
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
            Pair<IRawFile, EColumn> index = getColumnContent(columnIndex);
            AnnotatedChromatogram chromatogram = (AnnotatedChromatogram)m_extractionResults.get(rowIndex).getChromatogram(index.getLeft());
            if (chromatogram == null)
                return null;
            if (index.getRight() == EColumn.INTENSITY) {
                if (chromatogram.getAnnotation() != null)
                    return chromatogram.getAnnotation().getApexIntensity();

                return chromatogram.getMaxIntensity();
            } else {
                if (chromatogram.getAnnotation() != null)
                    return chromatogram.getAnnotation().getScanCount();
                else
                    return null;
            }
        }
        return null;
    }

    @Override
    public String getToolTipForHeader(int column) {
        if (column < Column.values().length)
            return Column.values()[column].getTooltip();
        Pair<IRawFile, EColumn> index = getColumnContent(column);
        return index.getRight().name()+" value from "+index.getLeft().getName();
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
        return getColumnClass(columnIndex);
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
