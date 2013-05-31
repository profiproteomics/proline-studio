package fr.proline.studio.rsmexplorer.gui.model;

import fr.proline.core.orm.msi.BioSequence;
import fr.proline.core.orm.msi.ProteinMatch;
import fr.proline.studio.utils.DataFormat;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author JM235353
 */
public class ProteinTableModel extends AbstractTableModel {

    public static final int COLTYPE_PROTEIN_NAME           = 0;
    public static final int COLTYPE_SAMESET_SUBSET         = 1;
    public static final int COLTYPE_PROTEIN_SCORE          = 2;
    public static final int COLTYPE_PROTEIN_PEPTIDES_COUNT = 3;
    public static final int COLTYPE_PROTEIN_MASS           = 4;
    private static final String[] columnNames = {"Protein", "Same Set", "Score", "Peptides", "Mass"};
    private ProteinMatch[] sameSetMatches = null;
    private ProteinMatch[] subSetMatches = null;
    private Integer rsmId = null;

    public ProteinMatch getProteinMatch(int row) {

        int sameSetNb = sameSetMatches.length;
        if (row < sameSetNb) {
            return sameSetMatches[row];
        }
        return subSetMatches[row - sameSetNb];
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
            case COLTYPE_PROTEIN_NAME:
                return String.class;
            case COLTYPE_PROTEIN_PEPTIDES_COUNT:
                return Integer.class;
            case COLTYPE_PROTEIN_MASS:
            case COLTYPE_PROTEIN_SCORE:
                return Float.class;
            case COLTYPE_SAMESET_SUBSET:
                return Boolean.class;
        }
        return null;
    }

    @Override
    public int getRowCount() {
        if (sameSetMatches == null) {
            return 0;
        }
        return sameSetMatches.length + subSetMatches.length;
    }

    @Override
    public Object getValueAt(int row, int col) {
        // Retrieve Protein Match
        ProteinMatch proteinMatch = getProteinMatch(row);

        switch (col) {
            case COLTYPE_PROTEIN_NAME:
                return proteinMatch.getAccession();
            case COLTYPE_SAMESET_SUBSET:
                if (row<sameSetMatches.length) {
                    return Boolean.TRUE;
                } else {
                    return Boolean.FALSE;
                }
            case COLTYPE_PROTEIN_SCORE:
                Float score = Float.valueOf(proteinMatch.getTransientData().getPeptideSet(rsmId).getScore() );
                return score;
            case COLTYPE_PROTEIN_PEPTIDES_COUNT:
                return proteinMatch.getTransientData().getPeptideSet(rsmId).getPeptideCount();
            case COLTYPE_PROTEIN_MASS:
                fr.proline.core.orm.msi.BioSequence bioSequenceMSI = proteinMatch.getTransientData().getBioSequenceMSI();
                if (bioSequenceMSI != null) {
                    return new Float(bioSequenceMSI.getMass());
                }
                fr.proline.core.orm.pdi.BioSequence bioSequencePDI = proteinMatch.getTransientData().getBioSequencePDI();
                if (bioSequencePDI != null) {
                    return new Float(bioSequencePDI.getMass());
                }
                return null;
        }
        return null; // should never happen
    }
    

    public void setData(Integer rsmId, ProteinMatch[] sameSetMatches, ProteinMatch[] subSetMatches) {
        this.rsmId = rsmId;
        this.sameSetMatches = sameSetMatches;
        this.subSetMatches = subSetMatches;
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
    
}
