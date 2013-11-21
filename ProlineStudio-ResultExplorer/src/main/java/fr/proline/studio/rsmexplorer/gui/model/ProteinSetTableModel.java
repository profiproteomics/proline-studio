package fr.proline.studio.rsmexplorer.gui.model;

import fr.proline.core.orm.msi.dto.DProteinMatch;
import fr.proline.core.orm.msi.dto.DProteinSet;
import fr.proline.studio.dam.tasks.DatabaseProteinSetsTask;
import fr.proline.studio.filter.*;
import fr.proline.studio.utils.*;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Table Model for Protein Sets
 * @author JM235353
 */
public class ProteinSetTableModel extends LazyTableModel {

    public static final int COLTYPE_PROTEIN_SETS_NAME = 0;
    public static final int COLTYPE_PROTEIN_SETS_DESCRIPTION = 1;
    public static final int COLTYPE_PROTEIN_SCORE = 2;
    public static final int COLTYPE_PROTEINS_COUNT = 3;
    public static final int COLTYPE_PEPTIDES_COUNT = 4;
    public static final int COLTYPE_SPECTRAL_COUNT = 5;
    public static final int COLTYPE_SPECIFIC_SPECTRAL_COUNT = 6;
    private static final String[] m_columnNames = {"Protein Set", "Description", "Score", "Proteins", "Peptides", "Peptide Match Count", "Specific Peptide Match Count"};
    
    private DProteinSet[] m_proteinSets = null;
    
    private ArrayList<Integer> m_filteredIds = null;
    private boolean m_isFiltering = false;
    private boolean m_filteringAsked = false;
    

    
    
    public ProteinSetTableModel(LazyTable table) {
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
    public Class getColumnClass(int col) {
        /*if (col == COLTYPE_PROTEIN_SCORE) {
            return Float.class;
        }*/
        return LazyData.class;
    }
    
    @Override
    public int getSubTaskId(int col) {
        switch (col) {
            case COLTYPE_PROTEIN_SETS_NAME:
            case COLTYPE_PROTEIN_SETS_DESCRIPTION:
                return DatabaseProteinSetsTask.SUB_TASK_TYPICAL_PROTEIN;
            case COLTYPE_PROTEIN_SCORE:
            case COLTYPE_PROTEINS_COUNT:
                return DatabaseProteinSetsTask.SUB_TASK_SAMESET_SUBSET_COUNT;
            case COLTYPE_PEPTIDES_COUNT:
                return DatabaseProteinSetsTask.SUB_TASK_TYPICAL_PROTEIN;
            case COLTYPE_SPECTRAL_COUNT:
                return DatabaseProteinSetsTask.SUB_TASK_SPECTRAL_COUNT;
            case COLTYPE_SPECIFIC_SPECTRAL_COUNT:
                return DatabaseProteinSetsTask.SUB_TASK_SPECIFIC_SPECTRAL_COUNT;
        }
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
        long rsmId = proteinSet.getResultSummaryId();

        switch (col) {
            case COLTYPE_PROTEIN_SETS_NAME: {

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
            case COLTYPE_PROTEIN_SETS_DESCRIPTION: {
                LazyData lazyData = getLazyData(row, col);

                // Retrieve typical Protein Match
                DProteinMatch proteinMatch = proteinSet.getTypicalProteinMatch();

                if (proteinMatch == null) {
                    lazyData.setData(null);
                    givePriorityTo(m_taskId, row, col);
                } else {
                    lazyData.setData(proteinMatch.getDescription());
                }
                return lazyData;
            }
            case COLTYPE_PROTEIN_SCORE: {
                /*Float score = Float.valueOf(proteinSet.getPeptideOverSet().getScore());
                return score;*/
                LazyData lazyData = getLazyData(row,col);
                
                // Retrieve typical Protein Match
                DProteinMatch proteinMatch = proteinSet.getTypicalProteinMatch();
                
                if (proteinMatch == null) {
                    lazyData.setData(null);
                    
                    givePriorityTo(m_taskId, row, col);
                } else {
                    lazyData.setData( Float.valueOf(proteinMatch.getPeptideSet(rsmId).getScore()) );
                }
                return lazyData;
            }
                
            case COLTYPE_PROTEINS_COUNT: {
                
                
                
                LazyData lazyData = getLazyData(row,col);
                
                Integer sameSetCount = proteinSet.getSameSetCount();
                Integer subSetCount = proteinSet.getSubSetCount();
                
                if ((sameSetCount == null) || (subSetCount == null)) {
                    // data is not already fetched
                    lazyData.setData(null);
                    givePriorityTo(m_taskId, row, col);
                } else {
                    

                    lazyData.setData(new ProteinCount(sameSetCount, subSetCount));
                }
                
                return lazyData;
            }
            case COLTYPE_PEPTIDES_COUNT: {
                
                LazyData lazyData = getLazyData(row,col);
                
                // Retrieve typical Protein Match
                DProteinMatch proteinMatch = proteinSet.getTypicalProteinMatch();
                
                if (proteinMatch == null) {
                    lazyData.setData(null);
                    
                    givePriorityTo(m_taskId, row, col);
                } else {
                    lazyData.setData( proteinMatch.getPeptideSet(rsmId).getPeptideCount() );
                }
                return lazyData;
   
            }
            case COLTYPE_SPECTRAL_COUNT: {
                LazyData lazyData = getLazyData(row,col);
                Integer spectralCount = proteinSet.getSpectralCount();
                lazyData.setData(spectralCount);
                if (spectralCount == null) {
                    givePriorityTo(m_taskId, row, col);
                }
                return lazyData;
            }
            case COLTYPE_SPECIFIC_SPECTRAL_COUNT: {
                LazyData lazyData = getLazyData(row,col);
                Integer specificSpectralCount = proteinSet.getSpecificSpectralCount();
                lazyData.setData(specificSpectralCount);
                if (specificSpectralCount == null) {
                    givePriorityTo(m_taskId, row, col);
                }
                return lazyData;
            }
        }
        return null; // should never happen
    }
    private static StringBuilder sb = new StringBuilder(20);


    

     
    
    public void setData(Long taskId, DProteinSet[] proteinSets) {
        m_proteinSets = proteinSets;
        m_taskId = taskId;
        
        updateMinMax();
        
        if (m_filteringAsked) {
            m_filteringAsked = false;
            filter();
        } else {
            fireTableDataChanged();
        }
        
        
    }
    
    private void updateMinMax() {
        RelativePainterHighlighter.NumberRelativizer relativizer = m_table.getRelativizer();
        if (relativizer == null) {
            return;
        }

        double maxScore = 0;
        int size = getRowCount();
        for (int i = 0; i < size; i++) {
            
            // Retrieve Protein Set
            DProteinSet proteinSet = getProteinSet(i);

            DProteinMatch proteinMatch = proteinSet.getTypicalProteinMatch();
            if (proteinMatch != null) {
                long rsmId = proteinSet.getResultSummaryId();
                double score = proteinMatch.getPeptideSet(rsmId).getScore();
                if (score > maxScore) {
                    maxScore = score;
                }
            }
        }
        relativizer.setMax(maxScore);

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
            case COLTYPE_PROTEIN_SETS_NAME: {
                return ((StringFilter) filter).filter((String)data);
            }
            case COLTYPE_PROTEIN_SETS_DESCRIPTION: {
                return ((StringFilter) filter).filter((String)data);
            }
            case COLTYPE_PROTEIN_SCORE: {
                return ((DoubleFilter) filter).filter((Float)data);
            }
            case COLTYPE_PROTEINS_COUNT:
            case COLTYPE_PEPTIDES_COUNT:
            case COLTYPE_SPECTRAL_COUNT:
            case COLTYPE_SPECIFIC_SPECTRAL_COUNT: {
                return ((IntegerFilter) filter).filter((Integer)data);
            }
    
        }
        
        return true; // should never happen
    }


    @Override
    public void initFilters() {
        if (m_filters == null) {
            int nbCol = getColumnCount();
            m_filters = new Filter[nbCol];
            m_filters[COLTYPE_PROTEIN_SETS_NAME] = new StringFilter(getColumnName(COLTYPE_PROTEIN_SETS_NAME));
            m_filters[COLTYPE_PROTEIN_SETS_DESCRIPTION] = new StringFilter(getColumnName(COLTYPE_PROTEIN_SETS_DESCRIPTION));
            m_filters[COLTYPE_PROTEIN_SCORE] = new DoubleFilter(getColumnName(COLTYPE_PROTEIN_SCORE));
            m_filters[COLTYPE_PROTEINS_COUNT] = null;
            m_filters[COLTYPE_PEPTIDES_COUNT] = new IntegerFilter(getColumnName(COLTYPE_PEPTIDES_COUNT));
            m_filters[COLTYPE_SPECTRAL_COUNT] = new IntegerFilter(getColumnName(COLTYPE_SPECTRAL_COUNT));
            m_filters[COLTYPE_SPECIFIC_SPECTRAL_COUNT] = new IntegerFilter(getColumnName(COLTYPE_SPECIFIC_SPECTRAL_COUNT));
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

    public class ProteinCount implements Comparable {

        private int m_sameSetCount;
        private int m_subSetCount;
        
        public ProteinCount(int sameSetCount, int subSetCount) {
            m_sameSetCount = sameSetCount;
            m_subSetCount = subSetCount;
        }
        
        @Override
        public String toString() {
            sb.setLength(0);
            sb.append((int) (m_sameSetCount + m_subSetCount));
            sb.append(" (");
            sb.append(m_sameSetCount);
            sb.append(',');
            sb.append(m_subSetCount);
            sb.append(')');
            return sb.toString();
        }
        
        @Override
        public int compareTo(Object o) {
            int sameSet =  m_sameSetCount-((ProteinCount) o).m_sameSetCount;
            if (sameSet != 0) {
                return sameSet;
            }
            return m_subSetCount-((ProteinCount) o).m_subSetCount;
        }
        
    }

}
