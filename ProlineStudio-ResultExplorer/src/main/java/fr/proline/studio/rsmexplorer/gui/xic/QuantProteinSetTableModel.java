package fr.proline.studio.rsmexplorer.gui.xic;

import fr.proline.core.orm.msi.dto.DProteinMatch;
import fr.proline.core.orm.msi.dto.DProteinSet;
import fr.proline.studio.filter.Filter;
import fr.proline.studio.filter.StringFilter;
import fr.proline.studio.utils.LazyData;
import fr.proline.studio.utils.LazyTable;
import fr.proline.studio.utils.LazyTableModel;
import java.util.ArrayList;
import java.util.HashSet;

/**
 *
 * @author JM235353
 */
public class QuantProteinSetTableModel extends LazyTableModel {

    public static final int COLTYPE_PROTEIN_SET_ID = 0;
    public static final int COLTYPE_PROTEIN_SET_NAME = 1;
    private static final String[] m_columnNames = {"Id", "Protein Set"};
    
    private DProteinSet[] m_proteinSets = null;
    private int m_quantChannelNumber;
    
    private ArrayList<Integer> m_filteredIds = null;
    private boolean m_isFiltering = false;
    private boolean m_filteringAsked = false;
    

    
    
    public QuantProteinSetTableModel(LazyTable table) {
        super(table);
    }
    
 
    
    @Override
    public int getColumnCount() {
        return m_columnNames.length+m_quantChannelNumber;
    }

    @Override
    public String getColumnName(int col) {
        if (col<=COLTYPE_PROTEIN_SET_NAME) {
            return m_columnNames[col];
        } else {
            return "qc"+(col-COLTYPE_PROTEIN_SET_NAME);
        }
    }
    
        @Override
    public String getToolTipForHeader(int col) {
        return getColumnName(col);
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
        DProteinSet proteinSet = m_proteinSets[rowFiltered];
        //long rsmId = proteinSet.getResultSummaryId();

        switch (col) {
            case COLTYPE_PROTEIN_SET_ID: {
                return proteinSet.getId();
            }
            case COLTYPE_PROTEIN_SET_NAME: {

                LazyData lazyData = getLazyData(row,col);
                
                // Retrieve typical Protein Match
                DProteinMatch proteinMatch = proteinSet.getTypicalProteinMatch();

                if (proteinMatch == null) {
                    lazyData.setData(null);
                    givePriorityTo(m_taskId, row, col);
                } else {
                    lazyData.setData(proteinMatch.getAccession());
                }
                return lazyData;
            }
            default:
                // Quant Channel columns JPM.TODO
        }
        return null; // should never happen
    }
    private static StringBuilder m_sb = new StringBuilder(20);


    

     
    
    public void setData(Long taskId, DProteinSet[] proteinSets) {
        m_proteinSets = proteinSets;
        
        m_quantChannelNumber = 0; // JPM.TODO
        
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

    public DProteinSet getProteinSet(int i) {
        
        if (m_filteredIds != null) {
            i = m_filteredIds.get(i).intValue();
        }
        
        return m_proteinSets[i];
    }
    
    public Long getResultSummaryId() {
        if ((m_proteinSets == null) || (m_proteinSets.length == 0)) {
            return null;
        }
        
        return m_proteinSets[0].getResultSummaryId();
    }

    public int findRow(long proteinSetId) {
        
        if (m_filteredIds != null) {
            int nb = m_filteredIds.size();
            for (int i = 0; i < nb; i++) {
                if (proteinSetId == m_proteinSets[m_filteredIds.get(i)].getId()) {
                    return i;
                }
            }
            return -1;
        }
        
        int nb = m_proteinSets.length;
        for (int i=0;i<nb;i++) {
            if (proteinSetId == m_proteinSets[i].getId()) {
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
            DProteinSet ps = getProteinSet(iModel);
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


}