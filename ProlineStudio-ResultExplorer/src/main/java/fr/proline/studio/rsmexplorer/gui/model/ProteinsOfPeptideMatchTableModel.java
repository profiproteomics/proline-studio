package fr.proline.studio.rsmexplorer.gui.model;


import fr.proline.core.orm.msi.ProteinMatch;
import fr.proline.studio.utils.DataFormat;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author JM235353
 */
public class ProteinsOfPeptideMatchTableModel extends AbstractTableModel {

    public static final int COLTYPE_PROTEIN_NAME           = 0;
    public static final int COLTYPE_PROTEIN_SCORE          = 1;
    public static final int COLTYPE_PROTEIN_PEPTIDES_COUNT = 2;
    public static final int COLTYPE_PROTEIN_MASS           = 3;
    private static final String[] columnNames = {"Protein", "Score", "Peptides", "Mass"};
    private ProteinMatch[] proteinMatchArray = null;

    public ProteinMatch getProteinMatch(int row) {

        return proteinMatchArray[row];
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
            case COLTYPE_PROTEIN_SCORE:
                return Float.class;
            case COLTYPE_PROTEIN_PEPTIDES_COUNT:
                return Integer.class;
            case COLTYPE_PROTEIN_MASS:
                return Double.class;
        }
        return null;
    }

    @Override
    public int getRowCount() {
        if (proteinMatchArray == null) {
            return 0;
        }
        return proteinMatchArray.length;
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

    public void setData(ProteinMatch[] proteinMatchArray) {
        this.proteinMatchArray = proteinMatchArray;
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
