package fr.proline.studio.rsmexplorer.gui.xic;

import fr.proline.core.orm.msi.dto.DMasterQuantProteinSet;
import fr.proline.core.orm.msi.dto.DQuantProteinSet;
import fr.proline.core.orm.uds.dto.DQuantitationChannel;
import fr.proline.studio.filter.Filter;
import fr.proline.studio.graphics.PlotInformation;
import fr.proline.studio.graphics.PlotType;
import fr.proline.studio.table.GlobalTableModelInterface;
import fr.proline.studio.table.LazyData;
import fr.proline.studio.table.LazyTable;
import fr.proline.studio.table.LazyTableModel;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.table.TableCellRenderer;

/**
 * data for one masterQuantProtein : for the different conditions (quant channels), abundances values
 * @author MB243701
 */
public class ProteinQuantTableModel extends LazyTableModel implements  GlobalTableModelInterface {
    
    
    public static final int COLTYPE_QC_ID = 0;
    public static final int COLTYPE_QC_NAME = 1;
    public static final int COLTYPE_ABUNDANCE = 2;
    public static final int COLTYPE_RAW_ABUNDANCE = 3;
    public static final int COLTYPE_PSM = 4;
    private static final String[] m_columnNames = {"Id", "Quant. Channel", "Abundance", "Raw Abundance", "Pep. match count"};
    private static final String[] m_columnNames_SC = {"Id", "Quant. Channel", "Abundance", "Specific SC", "Basic SC"};
    
    private DMasterQuantProteinSet m_quantProtein = null;
    private DQuantitationChannel[] m_quantChannels = null;

    private boolean m_isFiltering = false;
    private boolean m_filteringAsked = false;
    
    private String m_modelName;

    private boolean  m_isXICMode = true;
    
    
    public ProteinQuantTableModel(LazyTable table) {
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

        return m_quantChannels.length ;
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
                return qc.getId() ;
            }
            case COLTYPE_QC_NAME: {
                return qc.getResultFileName() ;
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
                return  quantProteinSet.getPeptideMatchesCount();
            }
        }
        return null; // should never happen
    }

    public void setData(DQuantitationChannel[] quantChannels, DMasterQuantProteinSet proteinSet, boolean isXICMode) {
        this.m_quantChannels  =quantChannels ;
        this.m_quantProtein = proteinSet ;
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
        return m_isXICMode? m_columnNames[columnIndex] : m_columnNames_SC[columnIndex];
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
        int[] keys = { COLTYPE_QC_ID };
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
    public int getBestXAxisColIndex(PlotType plotType) {
        switch (plotType) {
            case LINEAR_PLOT:
                return COLTYPE_QC_NAME;
        }
        return -1;
    }

    @Override
    public int getBestYAxisColIndex(PlotType plotType) {
        return COLTYPE_RAW_ABUNDANCE;
    }

    @Override
    public String getExportRowCell(int row, int col) {
        return null;
    }

    @Override
    public String getExportColumnName(int col) {
        return getColumnName(col);
    }

    @Override
    public TableCellRenderer getRenderer(int col) {
        return null;
    }
    
}

