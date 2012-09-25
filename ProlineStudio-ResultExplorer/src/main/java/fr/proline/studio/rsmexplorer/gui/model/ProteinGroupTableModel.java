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

        public static final int COLTYPE_PROTEIN_GROUPS_NAME  = 0;
        public static final int COLTYPE_PROTEIN_SCORE = 1;

        private static final String[] columnNames = { "Protein Group", "Score" };
        
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
                case COLTYPE_PROTEIN_GROUPS_NAME:
                    
                    // Retrieve typical Protein Match
                    ProteinMatch proteinMatch = proteinSet.getTransientTypicalProteinMatch();

                    if (proteinMatch != null) {
                        return proteinMatch.getAccession();
                    } else {
                        return "";
                    }
                case COLTYPE_PROTEIN_SCORE:
                    return new Float(proteinSet.getScore()); //JPM.TODO get rid of the Float creation each time
            }
            return null; // should never happen
        }


        public void setData(ProteinSet[] proteinSets) {
            this.proteinSets = proteinSets;
            fireTableDataChanged();
            
        }
        
        public ProteinSet getProteinSet(int i) {
            return proteinSets[i];
        }
}
