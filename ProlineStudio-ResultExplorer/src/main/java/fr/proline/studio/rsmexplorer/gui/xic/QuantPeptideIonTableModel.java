package fr.proline.studio.rsmexplorer.gui.xic;

import fr.proline.core.orm.msi.dto.DMasterQuantPeptideIon;
import fr.proline.core.orm.msi.dto.DPeptideInstance;
import fr.proline.core.orm.msi.dto.DQuantPeptideIon;
import fr.proline.core.orm.uds.dto.DQuantitationChannel;
import fr.proline.studio.filter.Filter;
import fr.proline.studio.filter.StringFilter;
import fr.proline.studio.table.ExportTableSelectionInterface;
import fr.proline.studio.utils.CyclicColorPalette;
import fr.proline.studio.table.LazyData;
import fr.proline.studio.table.LazyTable;
import fr.proline.studio.table.LazyTableModel;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 *
 * @author JM235353
 */
public class QuantPeptideIonTableModel extends LazyTableModel implements ExportTableSelectionInterface {

    public static final int COLTYPE_PEPTIDE_ION_ID = 0;
    public static final int COLTYPE_PEPTIDE_ION_NAME = 1;
    private static final String[] m_columnNames = {"Id", "Peptide Ion"};
    
    public static final int COLTYPE_SELECTION_LEVEL = 0;
    public static final int COLTYPE_ABUNDANCE = 1;
    public static final int COLTYPE_RAW_ABUNDANCE = 2;
    public static final int COLTYPE_PSM = 3;
    
    private static final String[] m_columnNamesQC = {"Sel. level", "Abundance", "Raw abundance", "Pep. match count"};
    private static final String[] m_toolTipQC = {"Selection level", "Abundance", "Raw abundance", "Peptides match count"};
    
    private DMasterQuantPeptideIon[] m_quantPeptideIons = null;
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
            return m_columnNames.length ;
        }else {
            return m_columnNames.length+m_quantChannelNumber*m_columnNamesQC.length;
        }
    }

    @Override
    public String getColumnName(int col) {
        if (col<=COLTYPE_PEPTIDE_ION_NAME) {
            return m_columnNames[col];
        } else if (m_quantChannels != null) {
            int nbQc = (col - m_columnNames.length) / m_columnNamesQC.length ;
            int id = col - m_columnNames.length -  (nbQc *m_columnNamesQC.length );
            
            StringBuilder sb = new StringBuilder();
            String rsmHtmlColor = CyclicColorPalette.getHTMLColor(nbQc);
            sb.append("<html><font color='").append(rsmHtmlColor).append("'>&#x25A0;&nbsp;</font>");
            sb.append(m_columnNamesQC[id]);
            sb.append("<br/>");
            sb.append(m_quantChannels[nbQc].getResultFileName());
            
            sb.append("</html>");
            return sb.toString();
        }else{
            return ""; // should not happen
        }
    }
    
        @Override
    public String getToolTipForHeader(int col) {
        if (col<=COLTYPE_PEPTIDE_ION_NAME) {
            return m_columnNames[col];
        } else if (m_quantChannels != null) {
            int nbQc = (col - m_columnNames.length) / m_columnNamesQC.length ;
            int id = col - m_columnNames.length -  (nbQc *m_columnNamesQC.length );
            
            StringBuilder sb = new StringBuilder();
            String rsmHtmlColor = CyclicColorPalette.getHTMLColor(nbQc);
            sb.append("<html><font color='").append(rsmHtmlColor).append("'>&#x25A0;&nbsp;</font>");
            sb.append(m_toolTipQC[id]);
            sb.append("<br/>");
            sb.append(m_quantChannels[nbQc].getResultFileName());
            
            sb.append("</html>");
            return sb.toString();
        }else{
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
        /*switch (col) {
            /*case COLTYPE_PEPTIDE_ION_NAME:
            case COLTYPE_PROTEIN_SET_DESCRIPTION:
                return DatabaseProteinSetsTask.SUB_TASK_TYPICAL_PROTEIN;
            case COLTYPE_PROTEIN_SCORE:
            case COLTYPE_PROTEINS_COUNT:
                return DatabaseProteinSetsTask.SUB_TASK_SAMESET_SUBSET_COUNT;
            case COLTYPE_UNIQUE_SEQUENCES_COUNT:
            case COLTYPE_PEPTIDES_COUNT:
                return DatabaseProteinSetsTask.SUB_TASK_TYPICAL_PROTEIN;
            case COLTYPE_SPECTRAL_COUNT:
                return DatabaseProteinSetsTask.SUB_TASK_SPECTRAL_COUNT;
            case COLTYPE_SPECIFIC_SPECTRAL_COUNT:
                return DatabaseProteinSetsTask.SUB_TASK_SPECIFIC_SPECTRAL_COUNT;
        }*/ //JPM.TODO
        return -1;
    }

    
    @Override
    public int getRowCount() {
        if (m_quantPeptideIons == null) {
            return 0;
        }
        if ((!m_isFiltering) && (m_filteredIds != null)) {
            return m_filteredIds.size();
        }
        return m_quantPeptideIons.length;
    }

    @Override
    public Object getValueAt(int row, int col) {
        
        int rowFiltered = row;
        if ((!m_isFiltering) && (m_filteredIds != null)) {
            rowFiltered = m_filteredIds.get(row).intValue();
        }
        
        // Retrieve Quant Peptide Ion
        DMasterQuantPeptideIon peptideIon = m_quantPeptideIons[rowFiltered];
        DPeptideInstance peptideInstance = peptideIon.getPeptideInstance();

        switch (col) {
            case COLTYPE_PEPTIDE_ION_ID: {
                return peptideIon.getId();
            }
            case COLTYPE_PEPTIDE_ION_NAME: {
                LazyData lazyData = getLazyData(row,col);
                lazyData.setData(peptideInstance.getBestPeptideMatch().getPeptide().getSequence());
                return lazyData;

            }
            default: {
                // Quant Channel columns 
                LazyData lazyData = getLazyData(row,col);
                
                // retrieve quantPeptideIon for the quantChannelId
                Map<Long, DQuantPeptideIon> quantPeptideIonByQchIds = peptideIon.getQuantPeptideIonByQchIds() ;
                if (quantPeptideIonByQchIds == null) {
                    lazyData.setData("");
                }else{
                    int nbQc = (col - m_columnNames.length) / m_columnNamesQC.length ;
                    int id = col - m_columnNames.length -  (nbQc *m_columnNamesQC.length );
                    DQuantPeptideIon quantPeptideIon = quantPeptideIonByQchIds.get(m_quantChannels[nbQc].getId()) ;
                    if (quantPeptideIon == null) {
                        lazyData.setData("");
                    } else {
                        switch (id ) {
                            case COLTYPE_SELECTION_LEVEL : lazyData.setData(quantPeptideIon.getSelectionLevel());
                                     break;
                            case COLTYPE_ABUNDANCE : lazyData.setData(quantPeptideIon.getAbundance());
                                     break;
                            case COLTYPE_RAW_ABUNDANCE : lazyData.setData(quantPeptideIon.getRawAbundance());
                                     break;
                            case COLTYPE_PSM : lazyData.setData(quantPeptideIon.getPeptideMatchesCount());
                                     break;
                        }
                    }
                }
                return lazyData;
            }
        }
        //return null; // should never happen
    }

    public void setData(Long taskId,  DQuantitationChannel[] quantChannels, DMasterQuantPeptideIon[] peptideIons ) {
        this.m_quantPeptideIons = peptideIons;
        this.m_quantChannels = quantChannels ;
        this.m_quantChannelNumber = quantChannels.length;
        fireTableStructureChanged();
        
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

    public DMasterQuantPeptideIon getPeptideIon(int i) {
        
        if (m_filteredIds != null) {
            i = m_filteredIds.get(i).intValue();
        }
        
        return m_quantPeptideIons[i];
    }
    
    
    public int findRow(long peptideIonId) {
        
        if (m_filteredIds != null) {
            int nb = m_filteredIds.size();
            for (int i = 0; i < nb; i++) {
                if (peptideIonId == m_quantPeptideIons[m_filteredIds.get(i)].getId()) {
                    return i;
                }
            }
            return -1;
        }
        
        int nb = m_quantPeptideIons.length;
        for (int i=0;i<nb;i++) {
            if (peptideIonId == m_quantPeptideIons[i].getId()) {
                return i;
            }
        }
        return -1;
        
    }
    
    
    public void sortAccordingToModel(ArrayList<Long> peptideIds) {
        
        if (m_quantPeptideIons == null){
            // data not loaded 
            return;
        }
        
        HashSet<Long> peptideIdMap = new HashSet<>(peptideIds.size());
        peptideIdMap.addAll(peptideIds);
        
        int nb = getRowCount();
        int iCur = 0;
        for (int iView=0;iView<nb;iView++) {
            int iModel = m_table.convertRowIndexToModel(iView);
            // Retrieve Peptide Ion
            DMasterQuantPeptideIon p = getPeptideIon(iModel);
            if (  peptideIdMap.contains(p.getId())  ) {
                peptideIds.set(iCur++,p.getId());
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

            int nbData = m_quantPeptideIons.length;
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
                return ((StringFilter) filter).filter((String)data);
            }
            /*case COLTYPE_PROTEIN_SET_DESCRIPTION: {
                return ((StringFilter) filter).filter((String)data);
            }
            case COLTYPE_PROTEIN_SCORE: {
                return ((DoubleFilter) filter).filter((Float)data);
            }
            case COLTYPE_PROTEINS_COUNT:
            case COLTYPE_PEPTIDES_COUNT:
            case COLTYPE_SPECTRAL_COUNT:
            case COLTYPE_UNIQUE_SEQUENCES_COUNT:
            case COLTYPE_SPECIFIC_SPECTRAL_COUNT: {
                return ((IntegerFilter) filter).filter((Integer)data);
            }*/ //JPM.TODO
    
        }
        
        return true; // should never happen
    }


    @Override
    public void initFilters() {
        if (m_filters == null) {
            int nbCol = getColumnCount();
            m_filters = new Filter[nbCol];
            m_filters[COLTYPE_PEPTIDE_ION_ID] = null;
            m_filters[COLTYPE_PEPTIDE_ION_NAME] = new StringFilter(getColumnName(COLTYPE_PEPTIDE_ION_NAME));
            /*m_filters[COLTYPE_PROTEIN_SET_DESCRIPTION] = new StringFilter(getColumnName(COLTYPE_PROTEIN_SET_DESCRIPTION));
            m_filters[COLTYPE_PROTEIN_SCORE] = new DoubleFilter(getColumnName(COLTYPE_PROTEIN_SCORE));
            m_filters[COLTYPE_PROTEINS_COUNT] = null;
            m_filters[COLTYPE_PEPTIDES_COUNT] = new IntegerFilter(getColumnName(COLTYPE_PEPTIDES_COUNT));
            m_filters[COLTYPE_SPECTRAL_COUNT] = new IntegerFilter(getColumnName(COLTYPE_SPECTRAL_COUNT));
            m_filters[COLTYPE_SPECIFIC_SPECTRAL_COUNT] = new IntegerFilter(getColumnName(COLTYPE_SPECIFIC_SPECTRAL_COUNT));
            m_filters[COLTYPE_UNIQUE_SEQUENCES_COUNT] = new IntegerFilter(getColumnName(COLTYPE_UNIQUE_SEQUENCES_COUNT));   */         
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
        return m_columnNames.length+index*m_columnNamesQC.length;
    }
    public int getColumStop(int index) {
        return m_columnNames.length+(1+index)*m_columnNamesQC.length-1;
    }
    
    public String getQCName(int i) {

        StringBuilder sb = new StringBuilder();

        String rsmHtmlColor = CyclicColorPalette.getHTMLColor(i);
        sb.append("<html><font color='").append(rsmHtmlColor).append("'>&#x25A0;&nbsp;</font>");
        sb.append(m_quantChannels[i].getResultFileName());
        sb.append("</html>");

        return sb.toString();
    }
    
    public String getByQCMColumnName(int index) {
        return m_columnNamesQC[index];
    }
    
    public int getQCNumber(int col) {
        return (col-m_columnNames.length) / m_columnNamesQC.length;
    }
    
    public int getTypeNumber(int col) {
        return (col-m_columnNames.length) % m_columnNamesQC.length;
    }
    
    
    public Long getResultSummaryId() {
        if ((m_quantPeptideIons == null) || (m_quantPeptideIons.length == 0)) {
            return null;
        }
        
        return m_quantPeptideIons[0].getResultSummary().getId();
    }
    
    
    /**
     * by default the rawAbundance and selectionLevel are hidden
     * return the list of columns ids of these columns
     * @return 
     */
    public List<Integer> getDefaultColumnsToHide() {
        List<Integer> listIds = new ArrayList();
        if (m_quantChannels != null) {
            for (int i=m_quantChannels.length-1; i>=0; i--) {
                listIds.add(m_columnNames.length+COLTYPE_ABUNDANCE+(i*m_columnNamesQC.length));
                listIds.add(m_columnNames.length+COLTYPE_SELECTION_LEVEL+(i*m_columnNamesQC.length));
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
            DMasterQuantPeptideIon peptideIon = m_quantPeptideIons[rowFiltered];

            selectedObjects.add(peptideIon.getId());
        }
        return selectedObjects;
    }
}