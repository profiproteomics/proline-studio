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
package fr.proline.studio.rsmexplorer.gui.xic;

import fr.proline.core.orm.msi.dto.DMasterQuantProteinSet;
import fr.proline.core.orm.msi.dto.DQuantProteinSet;
import fr.proline.core.orm.uds.dto.DDatasetType;
import fr.proline.core.orm.uds.dto.DQuantitationChannel;
import fr.proline.studio.extendedtablemodel.ExtraDataType;
import fr.proline.studio.export.ExportModelUtilities;
import fr.proline.studio.export.ExportFontData;
import fr.proline.studio.filter.Filter;
import fr.proline.studio.graphics.PlotInformation;
import fr.proline.studio.graphics.PlotType;
import fr.proline.studio.rsmexplorer.gui.renderer.FloatRenderer;
import fr.proline.studio.extendedtablemodel.GlobalTableModelInterface;
import fr.proline.studio.table.LazyData;
import fr.proline.studio.table.LazyTable;
import fr.proline.studio.table.LazyTableModel;
import fr.proline.studio.table.TableDefaultRendererManager;
import fr.proline.studio.table.renderer.BigFloatOrDoubleRenderer;
import fr.proline.studio.table.renderer.DefaultAlignRenderer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.table.TableCellRenderer;

/**
 * data for one masterQuantProtein : for the different conditions (quant
 * channels), abundances values
 *
 * @author MB243701
 */
//VDS Should we add peptides_count column... See where it is really used 
public class ProteinQuantTableModel extends LazyTableModel implements GlobalTableModelInterface {

    public static final int COLTYPE_QC_ID = 0;
    public static final int COLTYPE_QC_NAME = 1;
    public static final int COLTYPE_ABUNDANCE = 2;
    public static final int COLTYPE_RAW_ABUNDANCE = 3;
    public static final int COLTYPE_PSM = 4;
    private static final String[] m_columnNames = {"Id", "Quant. Channel", "Abundance", "Raw Abundance", "Pep. match count"};
    private static final String[] m_columnNames_SC = {"Id", "Quant. Channel", "Abundance", "Specific SC", "Basic SC"};

    private DMasterQuantProteinSet m_quantProtein = null;
    private DQuantitationChannel[] m_quantChannels = null;

    private String m_modelName;

    private DDatasetType.QuantitationMethodInfo m_quantMethodInfo;

    public ProteinQuantTableModel(LazyTable table) {
        super(table);
        m_quantMethodInfo = DDatasetType.QuantitationMethodInfo.FEATURES_EXTRACTION;
    }

    private boolean isSpectralCountQuant(){
        return m_quantMethodInfo.equals(DDatasetType.QuantitationMethodInfo.SPECTRAL_COUNTING);
    }

    @Override
    public int getColumnCount() {
        return m_columnNames.length;
    }

    @Override
    public String getColumnName(int col) {
        return isSpectralCountQuant() ? m_columnNames_SC[col] :  m_columnNames[col];
    }

    @Override
    public int getSubTaskId(int col) {
        return -1;
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
    public int getRowCount() {
        if (m_quantChannels == null) {
            return 0;
        }

        return m_quantChannels.length;
    }

    @Override
    public Object getValueAt(int row, int col) {

        // Retrieve QuantChannel
        DQuantitationChannel qc = m_quantChannels[row];
        // retrieve quantProteinSet for the quantChannelId
        Map<Long, DQuantProteinSet> quantProteinSetByQchIds = m_quantProtein.getQuantProteinSetByQchIds();
        DQuantProteinSet quantProteinSet = quantProteinSetByQchIds.get(qc.getId());
        switch (col) {
            case COLTYPE_QC_ID: {
                return qc.getId();
            }
            case COLTYPE_QC_NAME: {
                return qc.getName();
            }
            case COLTYPE_ABUNDANCE: {
                if (quantProteinSet == null || quantProteinSet.getAbundance() == null) {
                    return null;
                }
                return quantProteinSet.getAbundance().isNaN() ? null : quantProteinSet.getAbundance();
            }
            case COLTYPE_RAW_ABUNDANCE: {
                if (quantProteinSet == null || quantProteinSet.getRawAbundance() == null) {
                    return null;
                }
                return quantProteinSet.getRawAbundance().isNaN() ? null : quantProteinSet.getRawAbundance();
            }
            case COLTYPE_PSM: {
                if (quantProteinSet == null || quantProteinSet.getPeptideMatchesCount() == null) {
                    return null;
                }
                return quantProteinSet.getPeptideMatchesCount();
            }
        }
        return null; // should never happen
    }

    public void setData(DQuantitationChannel[] quantChannels, DMasterQuantProteinSet proteinSet, DDatasetType.QuantitationMethodInfo quantitationMethodInfo) {
        this.m_quantChannels = quantChannels;
        this.m_quantProtein = proteinSet;
        this.m_quantMethodInfo = quantitationMethodInfo;

        fireTableDataChanged();

    }

    public void dataUpdated() {

        // no need to do an updateMinMax : scores are known at once
        fireTableDataChanged();
    }

    @Override
    public void addFilters(LinkedHashMap<Integer, Filter> filtersMap) {

    }

    @Override
    public boolean isLoaded() {
        return m_table.isLoaded();
    }

    @Override
    public int getLoadingPercentage() {
        return m_table.getLoadingPercentage();
    }

    @Override
    public String getDataColumnIdentifier(int columnIndex) {
        return isSpectralCountQuant() ? m_columnNames_SC[columnIndex] :  m_columnNames[columnIndex];
    }

    @Override
    public Class getDataColumnClass(int columnIndex) {
        switch (columnIndex) {
            case COLTYPE_QC_ID: {
                return Long.class;
            }
            case COLTYPE_QC_NAME: {
                return String.class;
            }
            case COLTYPE_ABUNDANCE:
            case COLTYPE_RAW_ABUNDANCE: {
                return Float.class;
            }
            case COLTYPE_PSM: {
                return Integer.class;
            }
        }
        return null;
    }

    @Override
    public Object getDataValueAt(int rowIndex, int columnIndex) {
        Object data = getValueAt(rowIndex, columnIndex);
        if (data instanceof LazyData) {
            data = ((LazyData) data).getData();
        }
        return data;
    }

    @Override
    public int[] getKeysColumn() {
        int[] keys = {COLTYPE_QC_ID};
        return keys;
    }

    @Override
    public int getInfoColumn() {
        return COLTYPE_QC_NAME;
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
        PlotInformation plotInformation = new PlotInformation();
        if (m_quantProtein.getProteinSet() != null && m_quantProtein.getProteinSet().getTypicalProteinMatch() != null) {
            plotInformation.setPlotTitle(m_quantProtein.getProteinSet().getTypicalProteinMatch().getAccession());
        }
        plotInformation.setDrawPoints(true);
        plotInformation.setDrawGap(true);
        return plotInformation;
    }

    @Override
    public PlotType getBestPlotType() {
        return PlotType.LINEAR_PLOT;
    }


    @Override
    public int[] getBestColIndex(PlotType plotType) {
 
        switch (plotType) {
            case LINEAR_PLOT: {
                int[] cols = new int[2];
                cols[0] = COLTYPE_QC_NAME;
                cols[1] = COLTYPE_RAW_ABUNDANCE;
                return cols;
            }
        }
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

    @Override
    public TableCellRenderer getRenderer(int row, int col) {
        TableCellRenderer renderer = null;
        switch (col) {

            case COLTYPE_ABUNDANCE:
                if(isSpectralCountQuant()){
                    renderer = new FloatRenderer(new DefaultAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class), JLabel.RIGHT), 2);
                }else{
                    renderer = new BigFloatOrDoubleRenderer(new DefaultAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class), JLabel.RIGHT), 0);
                }
                break;

            case COLTYPE_RAW_ABUNDANCE:
                if (isSpectralCountQuant()) {
                    renderer = new FloatRenderer(new DefaultAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class), JLabel.RIGHT), 0);
                } else {
                    renderer = new BigFloatOrDoubleRenderer(new DefaultAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class), JLabel.RIGHT), 0);
                }
                break;

        }
        return renderer;
    }

    @Override
    public GlobalTableModelInterface getFrozzenModel() {
        return this;
    }

    @Override
    public ArrayList<ExtraDataType> getExtraDataTypes() {
        ArrayList<ExtraDataType> list = new ArrayList<>();
        list.add(new ExtraDataType(DQuantitationChannel.class, true));
        list.add(new ExtraDataType(DMasterQuantProteinSet.class, false));
        registerSingleValuesAsExtraTypes(list);
        return list;
    }

    @Override
    public Object getValue(Class c) {
        if (c.equals(DMasterQuantProteinSet.class)) {
            return m_quantProtein;
        }
        return getSingleValue(c);
    }

    @Override
    public Object getRowValue(Class c, int row) {
        if (c.equals(DQuantitationChannel.class)) {
            return m_quantChannels[row];
        }
        return null;
    }

    @Override
    public Object getColValue(Class c, int col) {
        return null;
    }

}
