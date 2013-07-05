package fr.proline.studio.rsmexplorer.gui.model;

import fr.proline.core.orm.msi.BioSequence;
import fr.proline.core.orm.msi.ProteinMatch;
import fr.proline.studio.utils.DataFormat;
import javax.swing.table.AbstractTableModel;

/**
 * Table Model for Proteins
 * @author JM235353
 */
public class ProteinTableModel extends AbstractTableModel {

    public static final int COLTYPE_PROTEIN_NAME           = 0;
    public static final int COLTYPE_SAMESET_SUBSET         = 1;
    public static final int COLTYPE_PROTEIN_SCORE          = 2;
    public static final int COLTYPE_PROTEIN_PEPTIDES_COUNT = 3;
    public static final int COLTYPE_PROTEIN_MASS           = 4;
    private static final String[] m_columnNames = {"Protein", "Same Set", "Score", "Peptides", "Mass"};
    private ProteinMatch[] m_sameSetMatches = null;
    private ProteinMatch[] m_subSetMatches = null;
    private Long rsmId = null;

    public ProteinMatch getProteinMatch(int row) {

        int sameSetNb = m_sameSetMatches.length;
        if (row < sameSetNb) {
            return m_sameSetMatches[row];
        }
        return m_subSetMatches[row - sameSetNb];
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
        if (m_sameSetMatches == null) {
            return 0;
        }
        return m_sameSetMatches.length + m_subSetMatches.length;
    }

    @Override
    public Object getValueAt(int row, int col) {
        // Retrieve Protein Match
        ProteinMatch proteinMatch = getProteinMatch(row);

        switch (col) {
            case COLTYPE_PROTEIN_NAME:
                return proteinMatch.getAccession();
            case COLTYPE_SAMESET_SUBSET:
                if (row<m_sameSetMatches.length) {
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
    

    public void setData(long rsmId, ProteinMatch[] sameSetMatches, ProteinMatch[] subSetMatches) {
        this.rsmId = rsmId;
        this.m_sameSetMatches = sameSetMatches;
        this.m_subSetMatches = subSetMatches;
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
