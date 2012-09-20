/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.gui.model;

import fr.proline.core.orm.msi.ProteinMatch;
import fr.proline.core.orm.msi.ProteinSet;
import fr.proline.studio.dam.ORMDataManager;



/**
 *
 * @author JM235353
 */
public class ProteinGroupTableModel extends AbstractProteinTableModel {


        private ProteinSet[] proteinSets = null;


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
                case COLTYPE_PROTEIN_NAME:
                    
                    // Retrieve typical Protein Match
                    ProteinMatch proteinMatch = (ProteinMatch) ORMDataManager.instance().get(ProteinSet.class, proteinSet.getId(), "ProteinMatch");

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
