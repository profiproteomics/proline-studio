package fr.proline.studio.rsmexplorer.gui.model;

import fr.proline.core.orm.msi.ProteinMatch;
import fr.proline.core.orm.msi.ProteinSet;
import java.util.HashMap;
import javax.swing.table.AbstractTableModel;

/**
 * This model contains a first column for same set or subset
 * Each other column correspond to a Protein Set
 * @author JM235353
 */
public class ProteinSetCmpTableModel extends AbstractTableModel {

    private ProteinSet[] proteinSetArray = null;
    private int maxSameSetCount;
    private int maxSubSetCount;
    private HashMap<String, ProteinStatus> proteinMatchStatusMap;

    // One column for sameSet/subSet, others colum are for a ProteinSet
    public static final int COLTYPE_SAMESET_SUBSET         = 0;
    
    
    public static final int STATUS_OK = 0;
    public static final int STATUS_SAME_OR_SUB_SET  = 1;
    public static final int STATUS_NOT_ALWAYS_PRESENT  = 2;
    
    public void setData(ProteinMatch proteinMatch) {
        
        proteinSetArray = (proteinMatch == null) ? null : proteinMatch.getTransientData().getProteinSetArray();
        
        if (proteinSetArray == null) {
            fireTableStructureChanged();
            return;
        }
        
        maxSameSetCount = 0;
        maxSubSetCount = 0;
        
        int nbProteinSets = proteinSetArray.length;
        for (int i=0;i<nbProteinSets;i++) {
            ProteinSet pset = proteinSetArray[i];
            int sameSetCount = pset.getTransientData().getSameSet().length;
            if (sameSetCount>maxSameSetCount) {
                maxSameSetCount = sameSetCount;
            }
            int subSetCount  = pset.getTransientData().getSubSet().length;
            if (subSetCount>maxSubSetCount) {
                maxSubSetCount = subSetCount;
            }
        
            
        }
        
        if (proteinMatchStatusMap == null) {
            proteinMatchStatusMap = new HashMap<String, ProteinStatus>();
        } else {
            proteinMatchStatusMap.clear();
        }
        int nbCol = getColumnCount();
        int nbRow = getRowCount();
        for (int columnIndex = 1; columnIndex < nbCol; columnIndex++) {
            for (int rowIndex = 0; rowIndex < nbRow; rowIndex++) {
                ProteinMatch pmCur = (ProteinMatch) getValueAt(rowIndex, columnIndex);
                if (pmCur == null) {
                    continue;
                }
                String proteinName = pmCur.getAccession();
                ProteinStatus status = proteinMatchStatusMap.get(proteinName);
                if (status == null) {
                    status = new ProteinStatus();
                    proteinMatchStatusMap.put(proteinName, status);
                }
                if (rowIndex < maxSameSetCount) {
                    status.inSameSet();
                } else {
                    status.inSubSet();
                }
            }
        }

        fireTableStructureChanged();
    }
    
    public int getStatus(ProteinMatch pm) {
        ProteinStatus status = proteinMatchStatusMap.get(pm.getAccession());
        if (status == null) {
            return STATUS_NOT_ALWAYS_PRESENT; // should not happen
        }
        return status.getStatus(getColumnCount()-1);
    }
    
    @Override
    public int getRowCount() {
        if (proteinSetArray == null) {
            return 0;
        }
        return maxSameSetCount+maxSubSetCount;
    }

    @Override
    public int getColumnCount() {
        if ((proteinSetArray == null) || (proteinSetArray.length == 0)) {
            return 0;
        }
        return proteinSetArray.length+1;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        
        // Same Set of Sub Set
        if (columnIndex == COLTYPE_SAMESET_SUBSET) {
            // Same Set or Sub Set column
            
            if (rowIndex == 0) {
                // first row : is same set
                return Boolean.TRUE;
            } else if (rowIndex == maxSameSetCount) {
                // first sub set row
                return Boolean.FALSE;
            }
            return null;
        }
        
        // other columns correspond to a ProteinSet
        ProteinSet proteinSet = proteinSetArray[columnIndex-1];
        if (rowIndex<maxSameSetCount) {
            // Same set
            ProteinMatch[] sameSet = proteinSet.getTransientData().getSameSet();
            if (rowIndex<sameSet.length) {
                return sameSet[rowIndex];
            }
            return null;
        } else {
            // Sub set
            ProteinMatch[] subSet = proteinSet.getTransientData().getSubSet();
            int subSetIndex = rowIndex-maxSameSetCount;
            if (subSetIndex<subSet.length) {
                return subSet[subSetIndex];
            }
            return null;
        }
        
    }
    
    @Override
    public Class getColumnClass(int col) {
        if (col == COLTYPE_SAMESET_SUBSET) {
            return Boolean.class;
      
        }
        return ProteinMatch.class;
    }
    
    @Override
    public String getColumnName(int col) {
        if (col == COLTYPE_SAMESET_SUBSET) {
            return "";
        }
        return proteinSetArray[col-1].getResultSummary().getDescription();
    }
    
    private class ProteinStatus {
        private int sameSetCount = 0;
        private int subSetCount = 0;
        

        
        public ProteinStatus() {
        }
        
        public void inSameSet() {
            sameSetCount++;
        }
        public void inSubSet() {
            subSetCount++;
        }
        
        public int getStatus(int nbProteinSet) {
            if ((sameSetCount+subSetCount)<nbProteinSet) {
                return STATUS_NOT_ALWAYS_PRESENT;
            }
            if ((sameSetCount == 0) || (subSetCount == 0)) {
                return STATUS_OK;
            }
            return STATUS_SAME_OR_SUB_SET;
        }
        
    }
    
}
