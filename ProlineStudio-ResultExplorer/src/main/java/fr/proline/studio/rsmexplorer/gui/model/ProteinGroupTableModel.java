/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.gui.model;

import fr.proline.core.om.model.msi.ProteinMatch;
import fr.proline.core.om.model.msi.ProteinSet;
import scala.Option;

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
                    Option<ProteinMatch> optionProteinMatch = proteinSet.typicalProteinMatch();
                    ProteinMatch proteinMatch = null;
                    if ((optionProteinMatch!=null) && (optionProteinMatch.isDefined())) {
                        proteinMatch = optionProteinMatch.get();
                    }
                    if (proteinMatch == null) {
                        Option<ProteinMatch[]> optionProteinMatches =  proteinSet.proteinMatches();
                        ProteinMatch[] proteinMatches = null;
                        if ((optionProteinMatches!=null) && (optionProteinMatches.isDefined())) {
                            proteinMatches = optionProteinMatches.get();
                        }
                        
                        float bestScore = -1;
                        for (ProteinMatch proteinCur : proteinMatches) {
                            float scoreCur = proteinCur.score();
                            if (scoreCur>bestScore) {
                                bestScore = scoreCur;
                                proteinMatch = proteinCur;
                            }
                        }
                        
                        if (proteinMatch != null) {
                            proteinSet.typicalProteinMatch_$eq(Option.apply(proteinMatch));
                            proteinSet.typicalProteinMatchId_$eq(proteinMatch.id());
                        }
                        
                    }
                    if (proteinMatch != null) {
                        return proteinMatch.accession();
                    } else {
                        return "";
                    }
                case COLTYPE_PROTEIN_SCORE:
                    return new Float(proteinSet.score()); //JPM.TODO get rid of the Float creation each time
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
