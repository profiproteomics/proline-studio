/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.gui.model;

import fr.proline.core.orm.msi.ProteinMatch;



/**
 *
 * @author JM235353
 */
public class ProteinTableModel extends AbstractProteinTableModel {


        private ProteinMatch[] proteinMatches = null;

        @Override
        public int getRowCount() {
            if (proteinMatches == null) {
                return 0;
            }
            return proteinMatches.length;
        }


        @Override
        public Object getValueAt(int row, int col) {
            // Retrieve Protein Group
            ProteinMatch proteinMatch = proteinMatches[row];
    
            switch (col) {
                case COLTYPE_PROTEIN_NAME:
                    return proteinMatch.getAccession();
                case COLTYPE_PROTEIN_SCORE:
                    return proteinMatch.getScore();
            }
            return null; // should never happen
        }


        public void setData(ProteinMatch[] proteinMatches) {
            this.proteinMatches = proteinMatches;
            fireTableDataChanged();
            
        }
}
