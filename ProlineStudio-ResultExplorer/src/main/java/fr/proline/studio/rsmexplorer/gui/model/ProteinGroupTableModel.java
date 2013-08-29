package fr.proline.studio.rsmexplorer.gui.model;

import fr.proline.core.orm.msi.dto.DProteinMatch;
import fr.proline.core.orm.msi.dto.DProteinSet;
import fr.proline.studio.dam.tasks.DatabaseProteinSetsTask;
import fr.proline.studio.utils.*;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Table Model for Protein Sets
 * @author JM235353
 */
public class ProteinGroupTableModel extends LazyTableModel {

    public static final int COLTYPE_PROTEIN_GROUPS_NAME = 0;
    public static final int COLTYPE_PROTEIN_SCORE = 1;
    public static final int COLTYPE_PROTEINS_COUNT = 2;
    public static final int COLTYPE_PEPTIDES_COUNT = 3;
    public static final int COLTYPE_SPECTRAL_COUNT = 4;
    public static final int COLTYPE_SPECIFIC_SPECTRAL_COUNT = 5;
    private static final String[] m_columnNames = {"Protein Set", "Score", "Proteins", "Peptides", "Peptide Match Count", "Specific Peptide Match Count"};
    private DProteinSet[] m_proteinSets = null;
    

    
    
    public ProteinGroupTableModel(LazyTable table) {
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
            case COLTYPE_PROTEIN_GROUPS_NAME:
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
        return m_proteinSets.length;
    }

    @Override
    public Object getValueAt(int row, int col) {
        // Retrieve Protein Set
        DProteinSet proteinSet = m_proteinSets[row];
        long rsmId = proteinSet.getResultSummaryId();

        switch (col) {
            case COLTYPE_PROTEIN_GROUPS_NAME: {
                
                
                
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
        
        fireTableDataChanged();
    }
    
    private void updateMinMax() {
        RelativePainterHighlighter.NumberRelativizer relativizer = m_table.getRelativizer();
        if (relativizer == null) {
            return;
        }

        double maxScore = 0;
        int size = getRowCount();
        for (int i = 0; i < size; i++) {
            DProteinSet proteinSet = m_proteinSets[i];
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
        
        fireTableDataChanged();
    }

    public DProteinSet getProteinSet(int i) {
        return m_proteinSets[i];
    }
    
    public Long getResultSummaryId() {
        if ((m_proteinSets == null) || (m_proteinSets.length == 0)) {
            return null;
        }
        
        return m_proteinSets[0].getResultSummaryId();
    }

    public int findRow(long proteinSetId) {
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
        
        int nb = m_proteinSets.length;
        int iCur = 0;
        for (int iView=0;iView<nb;iView++) {
            int iModel = m_table.convertRowIndexToModel(iView);
            DProteinSet ps = m_proteinSets[iModel];
            if (  proteinSetIdMap.contains(ps.getId())  ) {
                proteinSetIds.set(iCur++,ps.getId());
            }
        }
        
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
            return m_sameSetCount-((ProteinCount) o).m_sameSetCount;
        }
        
    }

}
