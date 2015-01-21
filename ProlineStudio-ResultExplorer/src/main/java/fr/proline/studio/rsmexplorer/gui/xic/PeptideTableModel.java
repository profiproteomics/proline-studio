/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.gui.xic;

import fr.proline.core.orm.msi.dto.DMasterQuantPeptide;
import fr.proline.core.orm.msi.dto.DQuantPeptide;
import fr.proline.core.orm.uds.dto.DQuantitationChannel;
import fr.proline.studio.comparedata.CompareDataInterface;
import fr.proline.studio.filter.Filter;
import fr.proline.studio.graphics.BestGraphicsInterface;
import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.graphics.PlotInformation;
import fr.proline.studio.graphics.PlotType;
import fr.proline.studio.table.LazyData;
import fr.proline.studio.table.LazyTable;
import fr.proline.studio.table.LazyTableModel;
import java.util.ArrayList;
import java.util.Map;

/**
 * data for one masterQuantPeptide : for the different conditions (quant channels), abundances values
 * @author MB243701
 */
public class PeptideTableModel extends LazyTableModel implements  CompareDataInterface, BestGraphicsInterface, CrossSelectionInterface {
    
    public static final int COLTYPE_QC_NAME = 0;
    public static final int COLTYPE_ABUNDANCE = 1;
    public static final int COLTYPE_RAW_ABUNDANCE = 2;
    private static final String[] m_columnNames = {"Quant. Channel", "Abundance", "Raw Abundance"};
    
    private DMasterQuantPeptide m_quantPeptide = null;
    private DQuantitationChannel[] m_quantChannels = null;
    
    private ArrayList<Integer> m_filteredIds = null;
    private boolean m_isFiltering = false;
    private boolean m_filteringAsked = false;
    
    private String m_modelName;

    public PeptideTableModel(LazyTable table) {
        super(table);
    }
    
    @Override
    public int getColumnCount() {
        return m_columnNames.length;
    }

    @Override
    public String getColumnName(int col) {
        return m_columnNames[col];
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
    public int getRowCount() {
        if (m_quantChannels == null) {
            return 0;
        }
        if ((!m_isFiltering) && (m_filteredIds != null)) {
            return m_filteredIds.size();
        }
        return m_quantChannels.length ;
    }


    @Override
    public Object getValueAt(int row, int col) {
        int rowFiltered = row;
        if ((!m_isFiltering) && (m_filteredIds != null)) {
            rowFiltered = m_filteredIds.get(row).intValue();
        }
        // Retrieve QuantChannel
        DQuantitationChannel qc = m_quantChannels[rowFiltered];
        // retrieve quantPeptide for the quantChannelId
        Map<Long, DQuantPeptide> quantPeptideByQchIds = m_quantPeptide.getQuantPeptideByQchIds();
        DQuantPeptide quantPeptide = quantPeptideByQchIds.get(qc.getId());
        switch (col) {
            case COLTYPE_QC_NAME: {
                return qc.getResultFileName() ;
            }
            case COLTYPE_ABUNDANCE: {
                if (quantPeptide == null || quantPeptide.getAbundance() == null) {
                    return "";
                }
                return quantPeptide.getAbundance().isNaN() ? "" : quantPeptide.getAbundance();
            }
            case COLTYPE_RAW_ABUNDANCE: {
                if (quantPeptide == null || quantPeptide.getRawAbundance() == null) {
                    return "";
                }
                return quantPeptide.getRawAbundance().isNaN() ? "" : quantPeptide.getRawAbundance();
            }
        }
        return null; // should never happen
    }

    public void setData(DQuantitationChannel[] quantChannels, DMasterQuantPeptide peptide) {
        this.m_quantChannels  =quantChannels ;
        this.m_quantPeptide = peptide ;
        if (m_filteringAsked) {
            m_filteringAsked = false;
            filter();
        } else {
            fireTableDataChanged();
        }
    }
    
    public void dataUpdated() {

        // no need to do an updateMinMax : scores are known at once
        if (m_filteredIds != null) {
            filter();
        } else {
            fireTableDataChanged();
        }
    }
    
    
    @Override
    public void initFilters() {
        if (m_filters == null) {
            int nbCol = getColumnCount();
            m_filters = new Filter[nbCol];
        }
    }

    @Override
    public void filter() {
        if (m_quantChannels == null) {
            // filtering not possible for the moment
            m_filteringAsked = true;
            return;
        }

        m_isFiltering = true;
        try {

            int nbData = m_quantChannels.length;
            if (m_filteredIds == null) {
                m_filteredIds = new ArrayList<>(nbData);
            } else {
                m_filteredIds.clear();
            }

            for (int i = 0; i < nbData; i++) {
                if (!filter(i)) {
                    continue;
                }
                m_filteredIds.add(Integer.valueOf(i));
            }

        } finally {
            m_isFiltering = false;
        }
        fireTableDataChanged();
    }

    @Override
    public boolean filter(int row, int col) {
        Filter filter = getColumnFilter(col);
        if ((filter == null) || (!filter.isUsed())) {
            return true;
        }

        Object data = ((LazyData) getValueAt(row, col)).getData();
        if (data == null) {
            return true; // should not happen
        }

        /*switch (col) {
            case COLTYPE_PEPTIDE_NAME: {
                return ((StringDiffFilter) filter).filter((String) data);
            }
            case COLTYPE_PEPTIDE_CLUSTER: {
                return ((StringFilter) filter).filter((String) data);
            }
        }*/

        return true; // should never happen
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
        return m_columnNames[columnIndex];
    }

    @Override
    public Class getDataColumnClass(int columnIndex) {
        switch (columnIndex) {
            case COLTYPE_QC_NAME: {
                return String.class;
            }
            case COLTYPE_ABUNDANCE: {
                return Float.class;
            }
            case COLTYPE_RAW_ABUNDANCE: {
                return Float.class;
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
        int[] keys = { COLTYPE_QC_NAME };
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
        plotInformation.setPlotTitle(m_quantPeptide.getPeptideInstance().getBestPeptideMatch().getPeptide().getSequence());
        plotInformation.setDrawPoints(true);
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
    public void select(ArrayList<Integer> rows) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ArrayList<Integer> getSelection() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
