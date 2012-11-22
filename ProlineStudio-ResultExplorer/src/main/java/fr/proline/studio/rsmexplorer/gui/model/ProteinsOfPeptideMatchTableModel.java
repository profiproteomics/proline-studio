package fr.proline.studio.rsmexplorer.gui.model;

import fr.proline.core.orm.msi.BioSequence;
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
            case COLTYPE_PROTEIN_SCORE:
            case COLTYPE_PROTEIN_PEPTIDES_COUNT:
            case COLTYPE_PROTEIN_MASS:
                return String.class;
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
                return DataFormat.format(proteinMatch.getScore(),2);
            case COLTYPE_PROTEIN_PEPTIDES_COUNT:
                return DataFormat.format(proteinMatch.getPeptideCount());
            case COLTYPE_PROTEIN_MASS:
                BioSequence bioSequence = proteinMatch.getTransientData().getBioSequence();
                if (bioSequence == null) {
                    return "";
                }
                return DataFormat.format(bioSequence.getMass(), 2);
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
