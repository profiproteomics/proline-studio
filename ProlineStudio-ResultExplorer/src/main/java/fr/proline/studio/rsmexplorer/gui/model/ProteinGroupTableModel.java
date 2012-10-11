/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.gui.model;

import fr.proline.core.orm.msi.ProteinMatch;
import fr.proline.core.orm.msi.ProteinSet;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.studio.dam.tasks.DatabaseProteinSetsTask;
import fr.proline.studio.utils.LazyData;
import fr.proline.studio.utils.LazyTable;
import fr.proline.studio.utils.LazyTableModel;
import java.util.ArrayList;
import java.util.HashSet;

/**
 *
 * @author JM235353
 */
public class ProteinGroupTableModel extends LazyTableModel {

    public static final int COLTYPE_PROTEIN_GROUPS_NAME = 0;
    public static final int COLTYPE_PROTEIN_SCORE = 1;
    public static final int COLTYPE_PROTEINS_COUNT = 2;
    public static final int COLTYPE_PEPTIDES_COUNT = 3;
    public static final int COLTYPE_SPECTRAL_COUNT = 4;
    public static final int COLTYPE_SPECIFIC_SPECTRAL_COUNT = 5;
    private static final String[] columnNames = {"Protein Group", "Score", "Proteins", "Peptides", "Spectral Count", "Specific Spectral Count"};
    private ProteinSet[] proteinSets = null;
    

    
    
    public ProteinGroupTableModel(LazyTable table) {
        super(table);
    }
    
    public Long getTaskId() {
        return taskId;
    }
    
    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }

    @Override
    public Class getColumnClass(int col) {
        switch (col) {
            case COLTYPE_PROTEIN_GROUPS_NAME:
                return LazyData.class;
            case COLTYPE_PROTEIN_SCORE:
                return Float.class;
            case COLTYPE_PROTEINS_COUNT:
                return LazyData.class;
            case COLTYPE_PEPTIDES_COUNT:
                return LazyData.class;
            case COLTYPE_SPECTRAL_COUNT:
                return LazyData.class;
            case COLTYPE_SPECIFIC_SPECTRAL_COUNT:
                return LazyData.class;
        }
        return null;
    }
    
    @Override
    public int getSubTaskId(int col) {
        switch (col) {
            case COLTYPE_PROTEIN_GROUPS_NAME:
                return DatabaseProteinSetsTask.SUB_TASK_TYPICAL_PROTEIN;
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
        if (proteinSets == null) {
            return 0;
        }
        return proteinSets.length;
    }

    @Override
    public Object getValueAt(int row, int col) {
        // Retrieve Protein Group
        ProteinSet proteinSet = proteinSets[row];

        switch (col) {
            case COLTYPE_PROTEIN_GROUPS_NAME: {
                
                
                
                LazyData lazyData = getLazyData(row,col);
                
                // Retrieve typical Protein Match
                ProteinMatch proteinMatch = proteinSet.getTransientProteinSetData().getTypicalProteinMatch();

                if (proteinMatch == null) {
                    lazyData.setData(null);
                    givePriorityTo(taskId, row, col);
                } else {
                    lazyData.setData(proteinMatch.getAccession());
                }
                return lazyData;
            }
            case COLTYPE_PROTEIN_SCORE:
                return new Float(proteinSet.getScore()); //JPM.TODO get rid of the Float creation each time
            case COLTYPE_PROTEINS_COUNT: {
                
                
                
                LazyData lazyData = getLazyData(row,col);
                
                Integer sameSetCount = proteinSet.getTransientProteinSetData().getSameSetCount();
                Integer subSetCount = proteinSet.getTransientProteinSetData().getSubSetCount();
                
                if ((sameSetCount == null) || (subSetCount == null)) {
                    // data is not already fetched
                    lazyData.setData(null);
                    givePriorityTo(taskId, row, col);
                } else {
                    sb.setLength(0);
                    sb.append((int) (sameSetCount + subSetCount));
                    sb.append(" (");
                    sb.append(sameSetCount);
                    sb.append(',');
                    sb.append(subSetCount);
                    sb.append(')');

                    lazyData.setData(sb.toString());
                }
                
                return lazyData;
            }
            case COLTYPE_PEPTIDES_COUNT: {
                
                LazyData lazyData = getLazyData(row,col);
                
                // Retrieve typical Protein Match
                ProteinMatch proteinMatch = proteinSet.getTransientProteinSetData().getTypicalProteinMatch();

                if (proteinMatch == null) {
                    lazyData.setData(null);
                    
                    givePriorityTo(taskId, row, col);
                } else {
                    lazyData.setData(new Integer(proteinMatch.getPeptideCount()));
                }
                return lazyData;
   
            }
            case COLTYPE_SPECTRAL_COUNT: {
                LazyData lazyData = getLazyData(row,col);
                Integer spectralCount = proteinSet.getTransientProteinSetData().getSpectralCount();
                lazyData.setData(spectralCount);
                if (spectralCount == null) {
                    givePriorityTo(taskId, row, col);
                }
                return lazyData;
            }
            case COLTYPE_SPECIFIC_SPECTRAL_COUNT: {
                LazyData lazyData = getLazyData(row,col);
                Integer specificSpectralCount = proteinSet.getTransientProteinSetData().getSpecificSpectralCount();
                lazyData.setData(specificSpectralCount);
                if (specificSpectralCount == null) {
                    givePriorityTo(taskId, row, col);
                }
                return lazyData;
            }
        }
        return null; // should never happen
    }
    private static StringBuilder sb = new StringBuilder(20);


    

     
    
    public void setData(Long taskId, ProteinSet[] proteinSets) {
        this.proteinSets = proteinSets;
        this.taskId = taskId;
        fireTableDataChanged();
    }
    
    public void dataUpdated() {
        
        fireTableDataChanged();
    }

    public ProteinSet getProteinSet(int i) {
        return proteinSets[i];
    }
    
    public ResultSummary getResultSummary() {
        if ((proteinSets == null) || (proteinSets.length == 0)) {
            return null;
        }
        
        return proteinSets[0].getResultSummary();
    }

    public int findRow(Integer proteinSetId) {
        int nb = proteinSets.length;
        for (int i=0;i<nb;i++) {
            if (proteinSetId.intValue() == proteinSets[i].getId().intValue()) {
                return i;
            }
        }
        return -1;
        
    }
    
    
    public void sortAccordingToModel(ArrayList<Integer> proteinSetIds) {
        HashSet<Integer> proteinSetIdMap = new HashSet<Integer>(proteinSetIds.size());
        proteinSetIdMap.addAll(proteinSetIds);
        
        int nb = proteinSets.length;
        int iCur = 0;
        for (int iView=0;iView<nb;iView++) {
            int iModel = table.convertRowIndexToModel(iView);
            ProteinSet ps = proteinSets[iModel];
            if (  proteinSetIdMap.contains(ps.getId())  ) {
                proteinSetIds.set(iCur++,ps.getId());
            }
        }
        
    }


    

}
