/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.gui.xic;

import fr.proline.core.orm.msi.dto.DMasterQuantPeptideIon;
import fr.proline.core.orm.msi.dto.DQuantPeptideIon;
import fr.proline.core.orm.uds.dto.DQuantitationChannel;
import fr.proline.studio.export.ExportFontData;
import fr.proline.studio.export.ExportModelUtilities;
import fr.proline.studio.extendedtablemodel.ExtraDataType;
import fr.proline.studio.extendedtablemodel.GlobalTableModelInterface;
import fr.proline.studio.filter.Filter;
import fr.proline.studio.filter.IntegerFilter;
import fr.proline.studio.graphics.PlotInformation;
import fr.proline.studio.graphics.PlotType;
import fr.proline.studio.table.DecoratedTableModel;
import fr.proline.studio.table.LazyData;
import fr.proline.studio.utils.CyclicColorPalette;
import fr.proline.studio.filter.StringDiffFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author Jean-Philippe
 */
public class QuantAggregatePeptideIonTableModel extends DecoratedTableModel implements GlobalTableModelInterface {

    public static final int COLTYPE_MASTER_QUANT_PEPTIDE_ION_ID = 0;
    public static final int COLTYPE_QUANTI_DATASET_NAME = 1;
    public static final int LAST_STATIC_COLUMN = COLTYPE_QUANTI_DATASET_NAME;
    
    private static final String[] m_columnNames = {"MasterQuantPeptideIon Id", "Quanti."};
    private static final String[] m_columnNamesForFilter = m_columnNames;
    private static final String[] m_toolTipColumns = {"Master Quant. Peption Ion Id of aggregated Quanti.", "Quantification"};

    
    private DMasterQuantPeptideIon m_aggregatedMasterQuantPeptideIon = null;
    private List<DMasterQuantPeptideIon> m_masterQuantPeptideIonList = null;
    private QuantChannelInfo m_quantChannelInfo = null;
    private HashMap<Long, HashSet<Long>> m_aggregatedToChildrenQuantChannelsId;
    private HashMap<Long, DQuantitationChannel> m_quantitationChannelsMap = null;
    
    public QuantAggregatePeptideIonTableModel() {

    }
    
    public void setData(DMasterQuantPeptideIon aggregatedMasterQuantPeptideIon, List<DMasterQuantPeptideIon> masterQuantPeptideIonList, QuantChannelInfo quantChannelInfo, HashMap<Long, HashSet<Long>> aggregatedToChildrenQuantChannelsId, HashMap<Long, DQuantitationChannel> quantitationChannelsMap) {
       m_aggregatedMasterQuantPeptideIon = aggregatedMasterQuantPeptideIon;
       m_masterQuantPeptideIonList = masterQuantPeptideIonList;
       m_quantChannelInfo = quantChannelInfo;
       m_aggregatedToChildrenQuantChannelsId = aggregatedToChildrenQuantChannelsId;
       m_quantitationChannelsMap = quantitationChannelsMap;
       
       fireTableStructureChanged(); //JPM.TODO
       
        /*boolean structureChanged = true;

        if (m_quantChannels != null && m_quantChannels.length == quantChannels.length) {
            for (int i = 0; i < m_quantChannels.length; i++) {
                structureChanged = !(m_quantChannels[i].equals(quantChannels[i]));
            }
        }
        m_quantPeptideIons = peptideIons;
        m_quantChannels = quantChannels;
        m_quantChannelNumber = quantChannels.length;

        if (structureChanged) {
            fireTableStructureChanged();
        }

        m_taskId = taskId;

        fireTableDataChanged();*/

    }
    
    @Override
    public int getRowCount() {
        if (m_masterQuantPeptideIonList == null) {
            return 0;
        }
        return m_masterQuantPeptideIonList.size();
    }

    @Override
    public int getColumnCount() {
        if (m_masterQuantPeptideIonList == null) {
            return m_columnNames.length;
        }
        return m_columnNames.length+m_aggregatedMasterQuantPeptideIon.getQuantPeptideIonByQchIds().size();
    }

    @Override
    public String getColumnName(int col) {
        if (col<=LAST_STATIC_COLUMN) {
            return m_columnNames[col];
        }
        
        if (m_masterQuantPeptideIonList == null) {
            return "";
        }
        
        col -= LAST_STATIC_COLUMN+1;
        
        StringBuilder sb = new StringBuilder();
        String rsmHtmlColor = CyclicColorPalette.getHTMLColor(col);
        sb.append("<html><font color='").append(rsmHtmlColor).append("'>&#x25A0;&nbsp;</font>");
        sb.append(m_quantChannelInfo.getQuantChannels()[col].getName());
        sb.append("</html>");
        
        return sb.toString();
    }
    
    @Override
    public Class getColumnClass(int col) {
        switch (col) {
            case COLTYPE_MASTER_QUANT_PEPTIDE_ION_ID:
                return Long.class;
            case COLTYPE_QUANTI_DATASET_NAME:
                return String.class;
            default:
                return Float.class;
        }
        
    }
    
    @Override
    public Object getValueAt(int row, int col) {
        
        DMasterQuantPeptideIon masterQuantPeptideIon = m_masterQuantPeptideIonList.get(row);
        
        switch (col) {
            case COLTYPE_MASTER_QUANT_PEPTIDE_ION_ID: {
                return masterQuantPeptideIon.getId();
            }
            case COLTYPE_QUANTI_DATASET_NAME: {
                DQuantitationChannel aggregateQC = m_quantChannelInfo.getQuantChannels()[0];
                HashSet<Long> childrenQC = m_aggregatedToChildrenQuantChannelsId.get(aggregateQC.getId());
                Map<Long, DQuantPeptideIon> quantPeptideIonMap = masterQuantPeptideIon.getQuantPeptideIonByQchIds();
                for (Long qcId : childrenQC) {
                    if (quantPeptideIonMap.get(qcId) != null) {
                        return m_quantitationChannelsMap.get(qcId).getMasterQuantitationChannel().getName();
                    }
                }
                
                
                return ""; // should never happen
            }
            default: {
                col -= LAST_STATIC_COLUMN+1;
                DQuantitationChannel aggregateQC = m_quantChannelInfo.getQuantChannels()[col];
                HashSet<Long> childrenQC = m_aggregatedToChildrenQuantChannelsId.get(aggregateQC.getId());

                DQuantPeptideIon peptideIon = null;
                Map<Long, DQuantPeptideIon> quantPeptideIonMap = masterQuantPeptideIon.getQuantPeptideIonByQchIds();
                for (Long qcId : childrenQC) {
                    peptideIon = quantPeptideIonMap.get(qcId);
                    if (peptideIon != null) {
                        break;
                    }
                }

                if (peptideIon != null) {
                    return peptideIon.getAbundance();
                }

                return new Float(0);
            }
        }
        
      
    }

    @Override
    public String getToolTipForHeader(int col) {
        if (col<=LAST_STATIC_COLUMN) {
            return m_toolTipColumns[col];
        }
        return null;
    }

    @Override
    public String getTootlTipValue(int row, int col) {
        return null;
    }

    @Override
    public TableCellRenderer getRenderer(int row, int col) {
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
        return null;
    }

    @Override
    public void givePriorityTo(Long taskId, int row, int col) {
        // nothing to do
    }

    @Override
    public void sortingChanged(int col) {
        // nothing to do
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
        return null;
    }

    @Override
    public int getInfoColumn() {
        return -1;
    }

    @Override
    public void setName(String name) {
        // nothing to do
    }

    @Override
    public String getName() {
        return null;
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
    public Object getRowValue(Class c, int row) {
        return null;
    }

    @Override
    public Object getColValue(Class c, int col) {
        return null;
    }

    @Override
    public void addFilters(LinkedHashMap<Integer, Filter> filtersMap) {
        filtersMap.put(COLTYPE_QUANTI_DATASET_NAME, new StringDiffFilter(getExportColumnName(COLTYPE_QUANTI_DATASET_NAME), null, COLTYPE_QUANTI_DATASET_NAME));

        int nbCol = getColumnCount();
        for (int i = LAST_STATIC_COLUMN + 1; i < nbCol; i++) {
            filtersMap.put(i, new IntegerFilter(getColumnName(i), null, i));

        }
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
    

    public List<Integer> getDefaultColumnsToHide() {

        List<Integer> listIds = new ArrayList();
        listIds.add(COLTYPE_MASTER_QUANT_PEPTIDE_ION_ID);
        
        return listIds;
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
        if (col<=LAST_STATIC_COLUMN) {
            return m_columnNames[col];
        }
        
        if (m_masterQuantPeptideIonList == null) {
            return "";
        }
        
        col -= LAST_STATIC_COLUMN+1;

        return m_quantChannelInfo.getQuantChannels()[col].getName();
    }
    
}
