package fr.proline.studio.rsmexplorer.gui.xic;

import fr.proline.core.orm.msi.Peptide;
import fr.proline.core.orm.msi.dto.DMasterQuantPeptide;
import fr.proline.core.orm.msi.dto.DQuantPeptide;
import fr.proline.core.orm.uds.dto.DQuantitationChannel;
import fr.proline.studio.extendedtablemodel.ExtraDataType;
import fr.proline.studio.export.ExportModelUtilities;
import fr.proline.studio.export.ExportFontData;
import fr.proline.studio.filter.Filter;
import fr.proline.studio.graphics.PlotInformation;
import fr.proline.studio.graphics.PlotType;
import fr.proline.studio.rsmexplorer.gui.renderer.FloatRenderer;
import fr.proline.studio.extendedtablemodel.GlobalTableModelInterface;
import fr.proline.studio.graphics.PlotDataSpec;
import fr.proline.studio.table.LazyData;
import fr.proline.studio.table.LazyTable;
import fr.proline.studio.table.LazyTableModel;
import fr.proline.studio.table.TableDefaultRendererManager;
import fr.proline.studio.table.renderer.BigFloatOrDoubleRenderer;
import fr.proline.studio.table.renderer.DefaultRightAlignRenderer;
import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.table.TableCellRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * data for one masterQuantPeptide : for the different conditions (quant
 * channels), abundances values
 *
 * @author MB243701
 */
public class PeptideTableModel extends LazyTableModel implements GlobalTableModelInterface {

    public static final int COLTYPE_QC_ID = 0;
    public static final int COLTYPE_QC_NAME = 1;
    public static final int COLTYPE_ABUNDANCE = 2;
    public static final int COLTYPE_RAW_ABUNDANCE = 3;
    public static final int COLTYPE_PSM = 4;
    protected static final String[] m_columnNames = {"Id", "Quant. Channel", "Abundance", "Raw Abundance", "Pep. match count"};
    protected static final String[] m_columnNames_SC = {"Id", "Quant. Channel", "Abundance", "Specific SC", "Basic SC"};

    private DMasterQuantPeptide m_quantPeptide = null;
    protected DQuantitationChannel[] m_quantChannels = null;

    protected String m_modelName;

    protected boolean m_isXICMode = true;

    public PeptideTableModel(LazyTable table) {
        super(table);
    }

    @Override
    public int getColumnCount() {
        return m_columnNames.length;
    }

    @Override
    public String getColumnName(int col) {
        return m_isXICMode ? m_columnNames[col] : m_columnNames_SC[col];
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
        // retrieve quantPeptide for the quantChannelId
        Map<Long, DQuantPeptide> quantPeptideByQchIds = m_quantPeptide.getQuantPeptideByQchIds();
        DQuantPeptide quantPeptide = quantPeptideByQchIds.get(qc.getId());
        switch (col) {
            case COLTYPE_QC_ID: {
                return qc.getId();
            }
            case COLTYPE_QC_NAME: {
                return qc.getName();
            }
            case COLTYPE_ABUNDANCE: {
                if (quantPeptide == null || quantPeptide.getAbundance() == null) {
                    return null;
                }
                return quantPeptide.getAbundance().isNaN() ? null : quantPeptide.getAbundance();
            }
            case COLTYPE_RAW_ABUNDANCE: {
                if (quantPeptide == null || quantPeptide.getRawAbundance() == null) {
                    return null;
                }
                return quantPeptide.getRawAbundance().isNaN() ? null : quantPeptide.getRawAbundance();
            }
            case COLTYPE_PSM: {
                if (quantPeptide == null || quantPeptide.getPeptideMatchesCount() == null) {
                    return null;
                }
                return quantPeptide.getPeptideMatchesCount();
            }
        }
        return null; // should never happen
    }

    public void setData(DQuantitationChannel[] quantChannels, DMasterQuantPeptide peptide, boolean isXICMode) {
        this.m_quantChannels = quantChannels;
        this.m_quantPeptide = peptide;
        this.m_isXICMode = isXICMode;

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
        return m_isXICMode ? m_columnNames[columnIndex] : m_columnNames_SC[columnIndex];
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
            case COLTYPE_ABUNDANCE: {
                return Float.class;
            }
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
        if (m_quantPeptide.getPeptideInstance() != null && m_quantPeptide.getPeptideInstance().getBestPeptideMatch() != null) {
            Peptide peptide = m_quantPeptide.getPeptideInstance().getBestPeptideMatch().getPeptide();
            StringBuilder sb = new StringBuilder(peptide.getSequence());
            if (peptide.getTransientData().isPeptideReadablePtmStringLoaded() && peptide.getTransientData().getPeptideReadablePtmString() != null) {
                sb.append(" - ").append(peptide.getTransientData().getPeptideReadablePtmString().getReadablePtmString());
            }
            plotInformation.setPlotTitle(sb.toString());
        }

        if (m_quantPeptide.getSelectionLevel() < 2) {
            plotInformation.setPlotColor(Color.LIGHT_GRAY);
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

            case COLTYPE_RAW_ABUNDANCE: {
                if (m_isXICMode) {
                    renderer = new BigFloatOrDoubleRenderer(new DefaultRightAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class)), 0);
                } else {
                    renderer = new FloatRenderer(new DefaultRightAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class)), 0);
                }
            }

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
        list.add(new ExtraDataType(DMasterQuantPeptide.class, false));
        registerSingleValuesAsExtraTypes(list);
        return list;
    }

    @Override
    public Object getValue(Class c) {
        if (c.equals(DMasterQuantPeptide.class)) {
            return m_quantPeptide;
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

    //private static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer.peptideTableModel");

    @Override
    public PlotDataSpec getDataSpecAt(int row) {
        //m_logger.debug("########call getDataSpecAt");
        PlotDataSpec result = new PlotDataSpec();
        DQuantitationChannel qc = m_quantChannels[row];
        // retrieve quantPeptide for the quantChannelId
        Map<Long, DQuantPeptide> quantPeptideByQchIds = m_quantPeptide.getQuantPeptideByQchIds();
        DQuantPeptide quantPeptide = quantPeptideByQchIds.get(qc.getId());
        if (!(quantPeptide == null || quantPeptide.getRawAbundance() == null || quantPeptide.getPeptideMatchesCount() == null)) {
            int matcheCount = quantPeptide.getPeptideMatchesCount();
            //m_logger.debug(String.format("#########row %s, matchcount=%d", qc.getFullName(), matcheCount));
            if (matcheCount == 0) {
                result.setFill(PlotDataSpec.FILL.EMPTY);
            }
        }
        return result;
    }


}
