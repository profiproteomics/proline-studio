package fr.proline.studio.rsmexplorer.gui.xic;

import fr.proline.core.orm.msi.MasterQuantPeptideIon;
import fr.proline.core.orm.msi.dto.DQuantPeptideIon;
import fr.proline.core.orm.uds.dto.DQuantitationChannel;
import fr.proline.studio.dam.tasks.xic.DatabaseLoadXicMasterQuantTask;
import fr.proline.studio.export.ExportColumnTextInterface;
import fr.proline.studio.export.ExportRowTextInterface;
import fr.proline.studio.filter.Filter;
import fr.proline.studio.filter.DoubleFilter;
import fr.proline.studio.filter.IntegerFilter;
import fr.proline.studio.filter.StringDiffFilter;
import fr.proline.studio.table.ExportTableSelectionInterface;
import fr.proline.studio.utils.CyclicColorPalette;
import fr.proline.studio.table.LazyData;
import fr.proline.studio.table.LazyTable;
import fr.proline.studio.table.LazyTableModel;
import fr.proline.studio.utils.StringUtils;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 *
 * @author JM235353
 */
public class QuantPeptideIonTableModel extends LazyTableModel implements ExportTableSelectionInterface, ExportColumnTextInterface,  ExportRowTextInterface {

    public static final int COLTYPE_PEPTIDE_ION_ID = 0;
    public static final int COLTYPE_PEPTIDE_ION_NAME = 1;
    public static final int COLTYPE_PEPTIDE_ION_CHARGE = 2;
    public static final int COLTYPE_PEPTIDE_ION_MOZ = 3;
    public static final int COLTYPE_PEPTIDE_ION_ELUTION_TIME = 4;
    private static final String[] m_columnNames = {"Id", "Peptide Sequence", "Charge", "m/z", "<html>Elution<br/>time (min)</html>"};
    private static final String[] m_columnNamesForFilter = {"Id", "Peptide Sequence", "Charge", "m/z", "Elution time"};
    private static final String[] m_toolTipColumns = {"MasterQuantPeptideIon Id", "Identified Peptide Sequence", "Charge", "Mass to Charge Ratio", "Elution time"};

    public static final int COLTYPE_SELECTION_LEVEL = 0;
    public static final int COLTYPE_ABUNDANCE = 1;
    public static final int COLTYPE_RAW_ABUNDANCE = 2;
    public static final int COLTYPE_PSM = 3;

    private static final String[] m_columnNamesQC = {"Sel. level", "Abundance", "Raw abundance", "Pep. match count"};
    private static final String[] m_toolTipQC = {"Selection level", "Abundance", "Raw abundance", "Peptides match count"};

    private List<MasterQuantPeptideIon> m_quantPeptideIons = null;
    private DQuantitationChannel[] m_quantChannels = null;
    private int m_quantChannelNumber;

    private ArrayList<Integer> m_filteredIds = null;
    private boolean m_isFiltering = false;
    private boolean m_filteringAsked = false;

    public QuantPeptideIonTableModel(LazyTable table) {
        super(table);
    }

    @Override
    public int getColumnCount() {
        if (m_quantChannels == null) {
            return m_columnNames.length;
        } else {
            return m_columnNames.length + m_quantChannelNumber * m_columnNamesQC.length;
        }
    }

    @Override
    public String getColumnName(int col) {
        if (col <= COLTYPE_PEPTIDE_ION_ELUTION_TIME) {
            return m_columnNames[col];
        } else if (m_quantChannels != null) {
            int nbQc = (col - m_columnNames.length) / m_columnNamesQC.length;
            int id = col - m_columnNames.length - (nbQc * m_columnNamesQC.length);

            StringBuilder sb = new StringBuilder();
            String rsmHtmlColor = CyclicColorPalette.getHTMLColor(nbQc);
            sb.append("<html><font color='").append(rsmHtmlColor).append("'>&#x25A0;&nbsp;</font>");
            sb.append(m_columnNamesQC[id]);
            sb.append("<br/>");
            sb.append(m_quantChannels[nbQc].getResultFileName());
            /*sb.append("<br/>");
            sb.append(m_quantChannels[nbQc].getRawFileName());*/

            sb.append("</html>");
            return sb.toString();
        } else {
            return ""; // should not happen
        }
    }
    
    @Override
    public String getExportColumnName(int col) {
        if (col <= COLTYPE_PEPTIDE_ION_ELUTION_TIME) {
            return m_columnNamesForFilter[col];
        } else if (m_quantChannels != null) {
            int nbQc = (col - m_columnNames.length) / m_columnNamesQC.length;
            int id = col - m_columnNames.length - (nbQc * m_columnNamesQC.length);
            
            StringBuilder sb = new StringBuilder();
            sb.append(m_columnNamesQC[id]);
            sb.append(" ");
            sb.append(m_quantChannels[nbQc].getResultFileName());
            
            return sb.toString();
        }else {
            return ""; // should not happen
        }
        
    }
    
    public String getColumnNameForFilter(int col) {
        if (col <= COLTYPE_PEPTIDE_ION_ELUTION_TIME) {
            return m_columnNamesForFilter[col];
        } else if (m_quantChannels != null) {
            int nbQc = (col - m_columnNames.length) / m_columnNamesQC.length;
            int id = col - m_columnNames.length - (nbQc * m_columnNamesQC.length);
            return m_columnNamesQC[id];
        } else {
            return ""; // should not happen
        }
    }

    @Override
    public String getToolTipForHeader(int col) {
        if (col <= COLTYPE_PEPTIDE_ION_ELUTION_TIME) {
            return m_toolTipColumns[col];
        } else if (m_quantChannels != null) {
            int nbQc = (col - m_columnNames.length) / m_columnNamesQC.length;
            int id = col - m_columnNames.length - (nbQc * m_columnNamesQC.length);
            String rawFilePath = StringUtils.truncate(m_quantChannels[nbQc].getRawFilePath(), 50);

            StringBuilder sb = new StringBuilder();
            String rsmHtmlColor = CyclicColorPalette.getHTMLColor(nbQc);
            sb.append("<html><font color='").append(rsmHtmlColor).append("'>&#x25A0;&nbsp;</font>");
            sb.append(m_toolTipQC[id]);
            sb.append("<br/>");
            sb.append(m_quantChannels[nbQc].getResultFileName());
            sb.append("<br/>");
            sb.append(rawFilePath);
            sb.append("</html>");
            return sb.toString();
        } else {
            return ""; // should not happen
        }
    }

    @Override
    public Class getColumnClass(int col) {
        if (col == COLTYPE_PEPTIDE_ION_ID) {
            return Long.class;
        }
        return LazyData.class;
    }

    @Override
    public int getSubTaskId(int col) {
        return DatabaseLoadXicMasterQuantTask.SUB_TASK_PEPTIDE_ION;
    }

    @Override
    public int getRowCount() {
        if (m_quantPeptideIons == null) {
            return 0;
        }
        if ((!m_isFiltering) && (m_filteredIds != null)) {
            return m_filteredIds.size();
        }
        return m_quantPeptideIons.size();
    }

    @Override
    public Object getValueAt(int row, int col) {

        int rowFiltered = row;
        if ((!m_isFiltering) && (m_filteredIds != null)) {
            rowFiltered = m_filteredIds.get(row).intValue();
        }

        // Retrieve Quant Peptide Ion
        MasterQuantPeptideIon peptideIon = m_quantPeptideIons.get(rowFiltered);

        switch (col) {
            case COLTYPE_PEPTIDE_ION_ID: {
                return peptideIon.getId();
            }
            case COLTYPE_PEPTIDE_ION_NAME: {
                LazyData lazyData = getLazyData(row, col);
                if (peptideIon.getResultSummary() == null) {
                    lazyData.setData(null);
                    givePriorityTo(m_taskId, row, col);
                } else {
                    if (peptideIon.getPeptideInstance() == null) {
                        lazyData.setData("");
                    }else{
                        lazyData.setData(peptideIon.getPeptideInstance().getPeptide().getSequence());
                    }
                }
                return lazyData;

            }
            case COLTYPE_PEPTIDE_ION_CHARGE: {
                LazyData lazyData = getLazyData(row, col);
                if (peptideIon.getResultSummary() == null) {
                    lazyData.setData(null);
                    givePriorityTo(m_taskId, row, col);
                } else {
                    lazyData.setData(peptideIon.getCharge());
                }
                return lazyData;

            }
            case COLTYPE_PEPTIDE_ION_MOZ: {
                LazyData lazyData = getLazyData(row, col);
                if (peptideIon.getResultSummary() == null) {
                    lazyData.setData(null);
                    givePriorityTo(m_taskId, row, col);
                } else {
                    lazyData.setData(peptideIon.getMoz());
                }
                return lazyData;

            }
            case COLTYPE_PEPTIDE_ION_ELUTION_TIME: {
                LazyData lazyData = getLazyData(row, col);
                if (peptideIon.getResultSummary() == null) {
                    lazyData.setData(null);
                    givePriorityTo(m_taskId, row, col);
                } else {
                    lazyData.setData(peptideIon.getElutionTime());
                }
                return lazyData;

            }
            default: {
                // Quant Channel columns 
                LazyData lazyData = getLazyData(row, col);
                if (peptideIon.getResultSummary() == null) {
                    lazyData.setData(null);
                    givePriorityTo(m_taskId, row, col);
                } else {

                    // retrieve quantPeptideIon for the quantChannelId
                    int nbQc = (col - m_columnNames.length) / m_columnNamesQC.length;
                    int id = col - m_columnNames.length - (nbQc * m_columnNamesQC.length);
                    Map<Long, DQuantPeptideIon> quantPeptideIonByQchIds = peptideIon.getQuantPeptideIonByQchIds();
                    if (quantPeptideIonByQchIds == null) {
                        switch (id) {
                                case COLTYPE_SELECTION_LEVEL:
                                    lazyData.setData(Integer.valueOf(0));
                                    break;
                                case COLTYPE_ABUNDANCE:
                                    lazyData.setData(Float.valueOf(0));
                                    break;
                                case COLTYPE_RAW_ABUNDANCE:
                                    lazyData.setData(Float.valueOf(0));
                                    break;
                                case COLTYPE_PSM:
                                    lazyData.setData(Integer.valueOf(0));
                                    break;
                            }
                    } else {
                        DQuantPeptideIon quantPeptideIon = quantPeptideIonByQchIds.get(m_quantChannels[nbQc].getId());
                        if (quantPeptideIon == null) {
                            switch (id) {
                                case COLTYPE_SELECTION_LEVEL:
                                    lazyData.setData(Integer.valueOf(0));
                                    break;
                                case COLTYPE_ABUNDANCE:
                                    lazyData.setData(Float.valueOf(0));
                                    break;
                                case COLTYPE_RAW_ABUNDANCE:
                                    lazyData.setData(Float.valueOf(0));
                                    break;
                                case COLTYPE_PSM:
                                    lazyData.setData(Integer.valueOf(0));
                                    break;
                            }
                        } else {
                            switch (id) {
                                case COLTYPE_SELECTION_LEVEL:
                                    lazyData.setData(quantPeptideIon.getSelectionLevel());
                                    break;
                                case COLTYPE_ABUNDANCE:
                                    lazyData.setData(quantPeptideIon.getAbundance().isNaN() ? Float.valueOf(0) : quantPeptideIon.getAbundance());
                                    break;
                                case COLTYPE_RAW_ABUNDANCE:
                                    lazyData.setData(quantPeptideIon.getRawAbundance().isNaN() ? Float.valueOf(0) : quantPeptideIon.getRawAbundance());
                                    break;
                                case COLTYPE_PSM:
                                    lazyData.setData(quantPeptideIon.getPeptideMatchesCount());
                                    break;
                            }
                        }
                    }
                }
                return lazyData;
            }
        }
        //return null; // should never happen
    }

    public void setData(Long taskId, DQuantitationChannel[] quantChannels, List<MasterQuantPeptideIon> peptideIons) {
        boolean structureChanged = true;
        if (this.m_quantChannels !=null && m_quantChannels.length == quantChannels.length ) {
            for (int i=0; i<m_quantChannels.length; i++) {
                structureChanged = !(m_quantChannels[i].equals(quantChannels[i]));
            }
        }
        this.m_quantPeptideIons = peptideIons;
        this.m_quantChannels = quantChannels;
        this.m_quantChannelNumber = quantChannels.length;
        m_filteredIds = null;
        m_isFiltering = false;
        
        if (structureChanged) {
            fireTableStructureChanged();
        }else{
            fireTableDataChanged();
        }

        m_taskId = taskId;

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

    public MasterQuantPeptideIon getPeptideIon(int i) {

        if (m_filteredIds != null) {
            i = m_filteredIds.get(i).intValue();
        }

        return m_quantPeptideIons.get(i);
    }

    public int findRow(long peptideId) {

        if (m_filteredIds != null) {
            int nb = m_filteredIds.size();
            for (int i = 0; i < nb; i++) {
                if (peptideId == m_quantPeptideIons.get(m_filteredIds.get(i)).getId()) {
                    return i;
                }
            }
            return -1;
        }

        int nb = m_quantPeptideIons.size();
        for (int i = 0; i < nb; i++) {
            if (peptideId == m_quantPeptideIons.get(i).getId()) {
                return i;
            }
        }
        return -1;

    }

    public void sortAccordingToModel(ArrayList<Long> peptideIds) {

        if (m_quantPeptideIons == null) {
            // data not loaded 
            return;
        }

        HashSet<Long> peptideIdMap = new HashSet<>(peptideIds.size());
        peptideIdMap.addAll(peptideIds);

        int nb = getRowCount();
        int iCur = 0;
        for (int iView = 0; iView < nb; iView++) {
            int iModel = m_table.convertRowIndexToModel(iView);
            // Retrieve Peptide Ion
            MasterQuantPeptideIon p = getPeptideIon(iModel);
            if (peptideIdMap.contains(p.getId())) {
                peptideIds.set(iCur++, p.getId());
            }
        }

        // need to refilter
        if (m_filteredIds != null) { // NEEDED ????
            filter();
        }
    }

    @Override
    public void filter() {

        if (m_quantPeptideIons == null) {
            // filtering not possible for the moment
            m_filteringAsked = true;
            return;
        }

        m_isFiltering = true;
        try {

            int nbData = m_quantPeptideIons.size();
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

        switch (col) {
            case COLTYPE_PEPTIDE_ION_NAME: {
                return ((StringDiffFilter) filter).filter((String) data);
            }
            case COLTYPE_PEPTIDE_ION_CHARGE: {
                return ((IntegerFilter) filter).filter((Integer) data);
            }
            case COLTYPE_PEPTIDE_ION_MOZ: {
                return ((DoubleFilter) filter).filter((Double) data);
            }
            case COLTYPE_PEPTIDE_ION_ELUTION_TIME: {
                return ((DoubleFilter) filter).filter((Float) data);
            }

        }

        return true; // should never happen
    }

    @Override
    public void initFilters() {
        if (m_filters == null) {
            int nbCol = getColumnCount();
            m_filters = new Filter[nbCol];
            m_filters[COLTYPE_PEPTIDE_ION_ID] = null;
            m_filters[COLTYPE_PEPTIDE_ION_NAME] = new StringDiffFilter(getColumnNameForFilter(COLTYPE_PEPTIDE_ION_NAME));
            m_filters[COLTYPE_PEPTIDE_ION_CHARGE] = new IntegerFilter(getColumnNameForFilter(COLTYPE_PEPTIDE_ION_CHARGE));
            m_filters[COLTYPE_PEPTIDE_ION_MOZ] = new DoubleFilter(getColumnNameForFilter(COLTYPE_PEPTIDE_ION_MOZ));
            m_filters[COLTYPE_PEPTIDE_ION_ELUTION_TIME] = new DoubleFilter(getColumnNameForFilter(COLTYPE_PEPTIDE_ION_ELUTION_TIME));
        }
    }

    @Override
    public int getLoadingPercentage() {
        return m_table.getLoadingPercentage();
    }

    @Override
    public boolean isLoaded() {
        return m_table.isLoaded();
    }

    public int getByQCCount() {
        return m_columnNamesQC.length;
    }

    public int getQCCount() {
        return m_quantChannels.length;
    }

    public int getColumStart(int index) {
        return m_columnNames.length + index * m_columnNamesQC.length;
    }

    public int getColumStop(int index) {
        return m_columnNames.length + (1 + index) * m_columnNamesQC.length - 1;
    }

    public String getQCName(int i) {

        StringBuilder sb = new StringBuilder();

        String rsmHtmlColor = CyclicColorPalette.getHTMLColor(i);
        sb.append("<html><font color='").append(rsmHtmlColor).append("'>&#x25A0;&nbsp;</font>");
        sb.append(m_quantChannels[i].getResultFileName());
        /*sb.append("<br/>");
        sb.append(m_quantChannels[i].getRawFileName());*/
        sb.append("</html>");

        return sb.toString();
    }

    public String getByQCMColumnName(int index) {
        return m_columnNamesQC[index];
    }

    public int getQCNumber(int col) {
        return (col - m_columnNames.length) / m_columnNamesQC.length;
    }

    public int getTypeNumber(int col) {
        return (col - m_columnNames.length) % m_columnNamesQC.length;
    }

    public Long getResultSummaryId() {
        if ((m_quantPeptideIons == null) || (m_quantPeptideIons.size() == 0)) {
            return null;
        }

        return m_quantPeptideIons.get(0).getResultSummary().getId();
    }

    /**
     * by default the rawAbundance and selectionLevel are hidden return the list
     * of columns ids of these columns
     *
     * @return
     */
    public List<Integer> getDefaultColumnsToHide() {
        List<Integer> listIds = new ArrayList();
        if (m_quantChannels != null) {
            for (int i = m_quantChannels.length - 1; i >= 0; i--) {
                listIds.add(m_columnNames.length + COLTYPE_RAW_ABUNDANCE + (i * m_columnNamesQC.length));
                listIds.add(m_columnNames.length + COLTYPE_SELECTION_LEVEL + (i * m_columnNamesQC.length));
            }
        }
        return listIds;
    }

    @Override
    public HashSet exportSelection(int[] rows) {

        int nbRows = rows.length;
        HashSet selectedObjects = new HashSet();
        for (int i = 0; i < nbRows; i++) {

            int row = rows[i];
            int rowFiltered = row;
            if ((!m_isFiltering) && (m_filteredIds != null)) {
                rowFiltered = m_filteredIds.get(row).intValue();
            }

            // Retrieve PeptideIon
            MasterQuantPeptideIon peptideIon = m_quantPeptideIons.get(rowFiltered);

            selectedObjects.add(peptideIon.getId());
        }
        return selectedObjects;
    }

    @Override
    public String getExportRowCell(int row, int col) {
        int rowFiltered = row;
        if ((!m_isFiltering) && (m_filteredIds != null)) {
            rowFiltered = m_filteredIds.get(row).intValue();
        }

        // Retrieve Quant Peptide Ion
        MasterQuantPeptideIon peptideIon = m_quantPeptideIons.get(rowFiltered);

        switch (col) {
            case COLTYPE_PEPTIDE_ION_ID: {
                return ""+peptideIon.getId();
            }
            case COLTYPE_PEPTIDE_ION_NAME: {
                if (peptideIon.getResultSummary() == null) {
                    return "";
                } else {
                    if (peptideIon.getPeptideInstance() == null) {
                        return "";
                    }else{
                        return peptideIon.getPeptideInstance().getPeptide().getSequence();
                    }
                }

            }
            case COLTYPE_PEPTIDE_ION_CHARGE: {
                if (peptideIon.getResultSummary() == null) {
                    return "";
                } else {
                    return ""+peptideIon.getCharge();
                }

            }
            case COLTYPE_PEPTIDE_ION_MOZ: {
                if (peptideIon.getResultSummary() == null) {
                    return "";
                } else {
                    return ""+peptideIon.getMoz();
                }

            }
            case COLTYPE_PEPTIDE_ION_ELUTION_TIME: {
                if (peptideIon.getResultSummary() == null) {
                    return "";
                } else {
                    return ""+peptideIon.getElutionTime();
                }

            }
            default: {
                // Quant Channel columns 
                if (peptideIon.getResultSummary() == null) {
                    return "";
                } else {

                    // retrieve quantPeptideIon for the quantChannelId
                    Map<Long, DQuantPeptideIon> quantPeptideIonByQchIds = peptideIon.getQuantPeptideIonByQchIds();
                    if (quantPeptideIonByQchIds == null) {
                        return "";
                    } else {
                        int nbQc = (col - m_columnNames.length) / m_columnNamesQC.length;
                        int id = col - m_columnNames.length - (nbQc * m_columnNamesQC.length);
                        DQuantPeptideIon quantPeptideIon = quantPeptideIonByQchIds.get(m_quantChannels[nbQc].getId());
                        if (quantPeptideIon == null) {
                            return "";
                        } else {
                            switch (id) {
                                case COLTYPE_SELECTION_LEVEL : return (quantPeptideIon.getSelectionLevel() == null?"":Integer.toString(quantPeptideIon.getSelectionLevel()));
                                case COLTYPE_ABUNDANCE : return ((quantPeptideIon.getAbundance() == null || quantPeptideIon.getAbundance().isNaN())? "":Float.toString(quantPeptideIon.getAbundance()));
                                case COLTYPE_RAW_ABUNDANCE : return ((quantPeptideIon.getRawAbundance() == null || quantPeptideIon.getRawAbundance().isNaN())? "":Float.toString(quantPeptideIon.getRawAbundance()));
                                case COLTYPE_PSM : return (quantPeptideIon.getPeptideMatchesCount()== null?"":Integer.toString(quantPeptideIon.getPeptideMatchesCount()));
                            }
                        }
                    }
                }
            }
        }
       return ""; // should never happen
    }
}
