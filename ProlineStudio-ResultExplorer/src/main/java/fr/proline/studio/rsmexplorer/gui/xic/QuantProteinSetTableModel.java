package fr.proline.studio.rsmexplorer.gui.xic;

import fr.proline.core.orm.msi.dto.DMasterQuantProteinSet;
import fr.proline.core.orm.msi.dto.DProteinMatch;
import fr.proline.core.orm.msi.dto.DProteinSet;
import fr.proline.core.orm.msi.dto.DQuantProteinSet;
import fr.proline.core.orm.uds.dto.DQuantitationChannel;
import fr.proline.studio.comparedata.CompareDataInterface;
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
public class QuantProteinSetTableModel extends LazyTableModel implements ExportTableSelectionInterface, CompareDataInterface {

    public static final int COLTYPE_PROTEIN_SET_ID = 0;
    public static final int COLTYPE_PROTEIN_SET_NAME = 1;
    private static final String[] m_columnNames = {"Id", "Protein Set"};
    
    public static final int COLTYPE_SELECTION_LEVEL = 0;
    public static final int COLTYPE_ABUNDANCE = 1;
    public static final int COLTYPE_RAW_ABUNDANCE = 2;
    public static final int COLTYPE_PSM = 3;
    
    private static final String[] m_columnNamesQC = {"Sel. level", "Abundance", "Raw abundance", "Pep. match count"};
    private static final String[] m_toolTipQC = {"Selection level", "Abundance", "Raw abundance", "Peptides match count"};
    
    private DMasterQuantProteinSet[] m_proteinSets = null;
    private DQuantitationChannel[] m_quantChannels = null;
    private int m_quantChannelNumber;
    
    private ArrayList<Integer> m_filteredIds = null;
    private boolean m_isFiltering = false;
    private boolean m_filteringAsked = false;
    
    private String m_modelName;
    
    
    public QuantProteinSetTableModel(LazyTable table) {
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
        if (col<=COLTYPE_PROTEIN_SET_NAME) {
            return m_columnNames[col];
        } else {
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
        }
    }
    
        @Override
    public String getToolTipForHeader(int col) {
        if (col<=COLTYPE_PROTEIN_SET_NAME) {
            return m_columnNames[col];
        } else {
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
        }
    }

    @Override
    public Class getColumnClass(int col) {
        if (col == COLTYPE_PROTEIN_SET_ID) {
            return Long.class;
        }
        return LazyData.class;
    }
    
    @Override
    public int getSubTaskId(int col) {
        /*switch (col) {
            /*case COLTYPE_PROTEIN_SET_NAME:
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
        if (m_proteinSets == null) {
            return 0;
        }
        if ((!m_isFiltering) && (m_filteredIds != null)) {
            return m_filteredIds.size();
        }
        return m_proteinSets.length;
    }

    @Override
    public Object getValueAt(int row, int col) {
        
        int rowFiltered = row;
        if ((!m_isFiltering) && (m_filteredIds != null)) {
            rowFiltered = m_filteredIds.get(row).intValue();
        }
        
        // Retrieve Protein Set
        DMasterQuantProteinSet proteinSet = m_proteinSets[rowFiltered];
        //long rsmId = proteinSet.getResultSummaryId();

        switch (col) {
            case COLTYPE_PROTEIN_SET_ID: {
                return proteinSet.getId();
            }
            case COLTYPE_PROTEIN_SET_NAME: {

                LazyData lazyData = getLazyData(row,col);
                
                // Retrieve typical Protein Match
                DProteinMatch proteinMatch = null;
                if (proteinSet.getProteinSet()!= null) {
                    proteinMatch =proteinSet.getProteinSet().getTypicalProteinMatch();
                    if (proteinMatch == null) {
                        lazyData.setData("");
                    }else {
                        lazyData.setData(proteinMatch.getAccession());
                    }
                }else{
                    lazyData.setData(null);
                    givePriorityTo(m_taskId, row, col);
                }
                return lazyData;
            }
            default: {
                // Quant Channel columns 
                LazyData lazyData = getLazyData(row,col);
                
                // retrieve quantProteinSet for the quantChannelId
                Map<Long, DQuantProteinSet> quantProteinSetByQchIds = proteinSet.getQuantProteinSetByQchIds() ;
                if (quantProteinSetByQchIds == null) {
                    lazyData.setData("");
                }else{
                    int nbQc = (col - m_columnNames.length) / m_columnNamesQC.length ;
                    int id = col - m_columnNames.length -  (nbQc *m_columnNamesQC.length );
                    DQuantProteinSet quantProteinSet = quantProteinSetByQchIds.get(m_quantChannels[nbQc].getId()) ;
                    if (quantProteinSet == null) {
                        lazyData.setData("");
                    } else {
                        switch (id ) {
                            case COLTYPE_SELECTION_LEVEL : lazyData.setData(quantProteinSet.getSelectionLevel());
                                     break;
                            case COLTYPE_ABUNDANCE : lazyData.setData(quantProteinSet.getAbundance());
                                     break;
                            case COLTYPE_RAW_ABUNDANCE : lazyData.setData(quantProteinSet.getRawAbundance());
                                     break;
                            case COLTYPE_PSM : lazyData.setData(quantProteinSet.getPeptideMatchesCount());
                                     break;
                        }
                    }
                }
                return lazyData;
            }
        }
        //return null; // should never happen
    }


    
    public DProteinSet getProteinSet(int row) {
        int rowFiltered = row;
        if ((!m_isFiltering) && (m_filteredIds != null)) {
            rowFiltered = m_filteredIds.get(row).intValue();
        }
        
        // Retrieve Protein Set
        DMasterQuantProteinSet quantProteinSet = m_proteinSets[rowFiltered];
        return quantProteinSet.getProteinSet();
    }
     
    
    public void setData(Long taskId, DQuantitationChannel[] quantChannels, DMasterQuantProteinSet[] proteinSets ) {
        this.m_quantChannels = quantChannels ;
        this.m_quantChannelNumber = quantChannels.length;
        this.m_proteinSets = proteinSets;
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

    public DMasterQuantProteinSet getQuantProteinSet(int i) {
        
        if (m_filteredIds != null) {
            i = m_filteredIds.get(i).intValue();
        }
        
        return m_proteinSets[i];
    }
    
    
    public int findRow(long proteinSetId) {
        
        if (m_filteredIds != null) {
            int nb = m_filteredIds.size();
            for (int i = 0; i < nb; i++) {
                if (proteinSetId == m_proteinSets[m_filteredIds.get(i)].getProteinSetId()) {
                    return i;
                }
            }
            return -1;
        }
        
        int nb = m_proteinSets.length;
        for (int i=0;i<nb;i++) {
            if (proteinSetId == m_proteinSets[i].getProteinSetId()) {
                return i;
            }
        }
        return -1;
        
    }
    
    
    public void sortAccordingToModel(ArrayList<Long> proteinSetIds) {
        
        if (m_proteinSets == null) {
            // data not loaded 
            return;
        }
        
        HashSet<Long> proteinSetIdMap = new HashSet<>(proteinSetIds.size());
        proteinSetIdMap.addAll(proteinSetIds);
        
        int nb = getRowCount();
        int iCur = 0;
        for (int iView=0;iView<nb;iView++) {
            int iModel = m_table.convertRowIndexToModel(iView);
            // Retrieve Protein Set
            DMasterQuantProteinSet ps = getQuantProteinSet(iModel);
            if (  proteinSetIdMap.contains(ps.getId())  ) {
                proteinSetIds.set(iCur++,ps.getId());
            }
        }
        
        // need to refilter
        if (m_filteredIds != null) { // NEEDED ????
            filter();
        }
    }



    @Override
    public void filter() {
        
        if (m_proteinSets == null) {
            // filtering not possible for the moment
            m_filteringAsked = true;
            return;
        }
        
        m_isFiltering = true;
        try {

            int nbData = m_proteinSets.length;
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
            case COLTYPE_PROTEIN_SET_NAME: {
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
            m_filters[COLTYPE_PROTEIN_SET_ID] = null;
            m_filters[COLTYPE_PROTEIN_SET_NAME] = new StringFilter(getColumnName(COLTYPE_PROTEIN_SET_NAME));
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
        if ((m_proteinSets == null) || (m_proteinSets.length == 0)) {
            return null;
        }
        
        return m_proteinSets[0].getQuantResultSummaryId();
    }
    
    /**
     * by default the abundance and selectionLevel are hidden
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

            // Retrieve Protein Set
            DMasterQuantProteinSet proteinSet = m_proteinSets[rowFiltered];

            selectedObjects.add(proteinSet.getProteinSet().getId());
        }
        return selectedObjects;
    }

    @Override
    public String getDataColumnIdentifier(int col) {
        if (col <= COLTYPE_PROTEIN_SET_NAME) {
            return m_columnNames[col];
        } else {
            int nbQc = (col - m_columnNames.length) / m_columnNamesQC.length;
            int id = col - m_columnNames.length - (nbQc * m_columnNamesQC.length);

            StringBuilder sb = new StringBuilder();
            sb.append(m_columnNamesQC[id]);
            sb.append(' ');
            sb.append(m_quantChannels[nbQc].getResultFileName());
            
            return sb.toString();
        }
    }

    @Override
    public Class getDataColumnClass(int col) {

        switch (col) {
            case COLTYPE_PROTEIN_SET_ID: {
                return Long.class;
            }
            case COLTYPE_PROTEIN_SET_NAME: {
                return String.class;
            }
            default: {
                int nbQc = (col - m_columnNames.length) / m_columnNamesQC.length;
                int id = col - m_columnNames.length - (nbQc * m_columnNamesQC.length);
                switch (id) {
                    case COLTYPE_SELECTION_LEVEL:
                        return Integer.class;
                    case COLTYPE_ABUNDANCE:
                        return Float.class;
                    case COLTYPE_RAW_ABUNDANCE:
                        return Float.class;
                    case COLTYPE_PSM:
                        return Integer.class;
                        
                }

            }
        }
        return null; // should never happen

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
        int[] keys = { COLTYPE_PROTEIN_SET_NAME };
        return keys;
    }

    @Override
    public void setName(String name) {
        m_modelName = name;
    }

    @Override
    public String getName() {
        return m_modelName;
    }
}