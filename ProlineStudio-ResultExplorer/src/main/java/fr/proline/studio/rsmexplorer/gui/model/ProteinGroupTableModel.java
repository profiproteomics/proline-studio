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
public class ProteinGroupTableModel extends AbstractTableModel {

        public static final int COLTYPE_PROTEIN_GROUPS_NAME     = 0;
        public static final int COLTYPE_PROTEIN_SCORE           = 1;
        public static final int COLTYPE_PROTEINS_COUNT          = 2;
        public static final int COLTYPE_PEPTIDES_COUNT          = 3;
        public static final int COLTYPE_SPECTRAL_COUNT          = 4;
        public static final int COLTYPE_SPECIFIC_SPECTRAL_COUNT = 5;

        private static final String[] columnNames = { "Protein Group", "Score", "Proteins", "Peptides", "Spectral Count", "Specific Spectral Count" };
        
        private ProteinSet[] proteinSets = null;


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
                    return String.class;
                case COLTYPE_PROTEIN_SCORE:
                    return Float.class;
                case COLTYPE_PROTEINS_COUNT:
                    return String.class;
                case COLTYPE_PEPTIDES_COUNT:
                    return Integer.class;
                case COLTYPE_SPECTRAL_COUNT:
                    return Integer.class;
                case COLTYPE_SPECIFIC_SPECTRAL_COUNT:
                    return Integer.class;
            }
            return null;
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
                    
                    // Retrieve typical Protein Match
                    ProteinMatch proteinMatch = proteinSet.getTransientProteinSetData().getTypicalProteinMatch();

                    if (proteinMatch != null) {
                        return proteinMatch.getAccession();
                    } else {
                        return "";
                    }
                }
                case COLTYPE_PROTEIN_SCORE:
                    return new Float(proteinSet.getScore()); //JPM.TODO get rid of the Float creation each time
                case COLTYPE_PROTEINS_COUNT: {
                    int sameSetCount = proteinSet.getTransientProteinSetData().getSameSetCount();
                    int subSetCount = proteinSet.getTransientProteinSetData().getSubSetCount();
                    sb.setLength(0);
                    sb.append((int) (sameSetCount+subSetCount));
                    sb.append(" (");
                    sb.append(sameSetCount);
                    sb.append(',');
                    sb.append(subSetCount);
                    sb.append(')');
                    return sb.toString();
                }
                case COLTYPE_PEPTIDES_COUNT: {
                    ProteinMatch proteinMatch = proteinSet.getTransientProteinSetData().getTypicalProteinMatch();
                    return new Integer(proteinMatch.getPeptideCount());
                }
                case COLTYPE_SPECTRAL_COUNT:
                    return new Integer(proteinSet.getTransientProteinSetData().getSpectralCount());
                case COLTYPE_SPECIFIC_SPECTRAL_COUNT:
                    return new Integer(proteinSet.getTransientProteinSetData().getSpecificSpectralCount());
            }
            return null; // should never happen
        }
        private static StringBuilder sb = new StringBuilder(20);


        public void setData(ProteinSet[] proteinSets) {
            this.proteinSets = proteinSets;
            fireTableDataChanged();
            
        }
        
        public ProteinSet getProteinSet(int i) {
            return proteinSets[i];
        }
}
