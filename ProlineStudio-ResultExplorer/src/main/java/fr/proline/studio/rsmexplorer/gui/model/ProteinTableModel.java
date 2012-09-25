/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.gui.model;

import fr.proline.core.orm.msi.ProteinMatch;
import fr.proline.core.orm.msi.ProteinSet;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author JM235353
 */
public class ProteinTableModel extends AbstractTableModel {

    public static final int COLTYPE_PROTEIN_NAME = 0;
    public static final int COLTYPE_SAMESET_SUBSET = 1;
    public static final int COLTYPE_PROTEIN_SCORE = 2;
    private static final String[] columnNames = {"Protein", "Same Set", "Score"};
    private ProteinMatch[] sameSetMatches = null;
    private ProteinMatch[] subSetMatches = null;

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
            case COLTYPE_SAMESET_SUBSET:
                return Boolean.class;
            case COLTYPE_PROTEIN_SCORE:
                return Float.class;
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
        // Retrieve Protein Group
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
                return new Float(proteinMatch.getScore());
        }
        return null; // should never happen
    }

    public void setData(ProteinMatch[] sameSetMatches, ProteinMatch[] subSetMatches) {
        this.sameSetMatches = sameSetMatches;
        this.subSetMatches = subSetMatches;
        fireTableDataChanged();
    }
}
