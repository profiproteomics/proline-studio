package fr.proline.studio.rsmexplorer.gui.model;


import fr.proline.core.orm.msi.ProteinMatch;
import fr.proline.studio.utils.LazyTable;
import fr.proline.studio.utils.LazyTableModel;
import fr.proline.studio.dam.tasks.DatabaseProteinMatchesTask;
import fr.proline.studio.utils.LazyData;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Table Model for Peptide Matches
 * @author JM235353
 */
public class ProteinsOfPeptideMatchTableModel extends LazyTableModel {

    public static final int COLTYPE_PROTEIN_NAME           = 0;
    public static final int COLTYPE_PROTEIN_SCORE          = 1;
    public static final int COLTYPE_PROTEIN_PEPTIDES_COUNT = 2;
    public static final int COLTYPE_PROTEIN_MASS           = 3;
    private static final String[] m_columnNames = {"Protein", "Score", "Peptides", "Mass"};
    private ProteinMatch[] m_proteinMatchArray = null;

    public ProteinsOfPeptideMatchTableModel(LazyTable table) {
        super(table);
    }
    
    public ProteinMatch getProteinMatch(int row) {

        return m_proteinMatchArray[row];
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
        switch (col) {
            case COLTYPE_PROTEIN_NAME:
                return String.class;
            case COLTYPE_PROTEIN_SCORE:
                return Float.class;
            case COLTYPE_PROTEIN_PEPTIDES_COUNT:
                return Integer.class;
            case COLTYPE_PROTEIN_MASS:
                return LazyData.class;
        }
        return null;
    }

    @Override
    public int getRowCount() {
        if (m_proteinMatchArray == null) {
            return 0;
        }
        return m_proteinMatchArray.length;
    }

    @Override
    public Object getValueAt(int row, int col) {

        ProteinMatch proteinMatch = getProteinMatch(row);

        switch (col) {
            case COLTYPE_PROTEIN_NAME:
                return proteinMatch.getAccession();
            case COLTYPE_PROTEIN_SCORE:
                return proteinMatch.getScore();
            case COLTYPE_PROTEIN_PEPTIDES_COUNT:
                return proteinMatch.getPeptideCount();
            case COLTYPE_PROTEIN_MASS:
                LazyData lazyData = getLazyData(row,col);
                
                fr.proline.core.orm.msi.BioSequence bioSequenceMSI = proteinMatch.getTransientData().getBioSequenceMSI();
                if (bioSequenceMSI != null) {
                    lazyData.setData(new Float(bioSequenceMSI.getMass()));
                    return lazyData;
                }
                /*fr.proline.core.orm.pdi.BioSequence bioSequencePDI = proteinMatch.getTransientData().getBioSequencePDI();
                if (bioSequencePDI != null) {
                    lazyData.setData(new Float(bioSequencePDI.getMass()));
                    return lazyData;
                }*/
                
                boolean noBioSequenceFound = proteinMatch.getTransientData().getNoBioSequenceFound();
                if (noBioSequenceFound) {
                    lazyData.setData("");
                    return lazyData;
                }
                
                lazyData.setData(null);
                    
                givePriorityTo(m_taskId, row, col);
                
                return lazyData;
                
        }
        return null; // should never happen
    }

    public void setData(ProteinMatch[] proteinMatchArray) {
        m_proteinMatchArray = proteinMatchArray;
        fireTableDataChanged();
        

    }
    
    public int findRowToSelect(String searchedText) {
        int rowToBeSelected = 0;
        if ((searchedText != null) && (searchedText.length() > 0)) {

            String searchedTextUpper = searchedText.toUpperCase();

            for (int row = 0; row < getRowCount(); row++) {
                String proteinName = ((String) getValueAt(row, COLTYPE_PROTEIN_NAME)).toUpperCase();
                if (proteinName.indexOf(searchedTextUpper) != -1) {
                    rowToBeSelected = row;
                }
            }
        }

        return rowToBeSelected;
    }

    public int findRow(long proteinMatchId) {
        int nb = m_proteinMatchArray.length;
        for (int i = 0; i < nb; i++) {
            if (proteinMatchId == m_proteinMatchArray[i].getId()) {
                return i;
            }
        }
        return -1;

    }
    
    @Override
    public int getSubTaskId(int col) {
        switch (col) {
            case COLTYPE_PROTEIN_MASS:
                return DatabaseProteinMatchesTask.SUB_TASK_BIOSEQUENCE;
        }
        return -1;
    }
    
    public void dataUpdated() {
        // no need to do an updateMinMax : scores are known at once
        fireTableDataChanged();
    }

    
    public void sortAccordingToModel(ArrayList<Long> proteinMatchIds) {
        
        if (m_proteinMatchArray == null) {
            // data not loaded 
            return;
        }
        
        HashSet<Long> proteinMatchIdMap = new HashSet<>(proteinMatchIds.size());
        proteinMatchIdMap.addAll(proteinMatchIds);

        int nb = m_proteinMatchArray.length;
        int iCur = 0;
        for (int iView = 0; iView < nb; iView++) {
            int iModel = m_table.convertRowIndexToModel(iView);
            ProteinMatch pm = m_proteinMatchArray[iModel];
            if (proteinMatchIdMap.contains(pm.getId())) {
                proteinMatchIds.set(iCur++, pm.getId());
            }
        }

    }
    
}
