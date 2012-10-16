/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.rsmexplorer.gui.model;


import fr.proline.core.orm.msi.Peptide;
import fr.proline.core.orm.msi.PeptideInstance;
import fr.proline.core.orm.msi.PeptideMatch;
import fr.proline.core.orm.msi.SequenceMatch;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author JM235353
 */
public class PeptideTableModel extends AbstractTableModel {
    
    public static final int COLTYPE_PEPTIDE_NAME  = 0;
    public static final int COLTYPE_PEPTIDE_SCORE = 1;

    private static final String[] columnNames = { "Peptide", "Score" };
    
    private PeptideInstance[] peptideInstances = null;


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
            case COLTYPE_PEPTIDE_NAME:
                return String.class;
            case COLTYPE_PEPTIDE_SCORE:
                return Float.class;
        }
        return null;
    }

    @Override
    public int getRowCount() {
        if (peptideInstances == null) {
            return 0;
        }
        return peptideInstances.length;
    }



        @Override
        public Object getValueAt(int row, int col) {
            // Retrieve Protein Group
            PeptideInstance peptideInstance = peptideInstances[row];
    
            switch (col) {
                case COLTYPE_PEPTIDE_NAME: {
                    // Retrieve typical Peptide Match
                    PeptideMatch peptideMatch = peptideInstance.getTransientBestPeptideMatch();
                    if (peptideMatch == null) {
                        
                    }
                    Peptide peptide = peptideMatch.getTransientPeptide();
                    if (peptide == null) {
                        return ""; // should never happen
                        
                    }
                    
                    SequenceMatch sequenceMatch = peptide.getTransientSequenceMatch();
                    
                    if (sequenceMatch != null) {
                        
                        // Add Before and After residue to the peptide
                        
                        StringBuilder display = new StringBuilder();
                        display.append("<HTML>");
                        String residueBefore = sequenceMatch.getResidueBefore();
                        if (residueBefore != null) {
                            display.append("<span style='color:#fd9b1d;'>");
                            display.append(residueBefore.toUpperCase());
                            display.append("</span>");
                        }
                        display.append(peptide.getSequence());
                        String residueAfter = sequenceMatch.getResidueAfter();
                        if (residueAfter != null) {
                            display.append("<span style='color:#fd9b1d;'>");
                            display.append(residueAfter.toUpperCase());
                            display.append("</span>");
                        }
                        display.append("</HTML>");
                        
                        return display.toString();
                    }
                    
                    return peptide.getSequence();

                    
                }    
                case COLTYPE_PEPTIDE_SCORE: {
                    // Retrieve typical Peptide Match
                    PeptideMatch peptideMatch = peptideInstance.getTransientBestPeptideMatch();
                    if (peptideMatch == null) {
                     return new Float(0); // should never happen   
                    }
                    return new Float(peptideMatch.getScore()); //JPM.TODO get rid of the Float creation each time
                }
            }
            return null; // should never happen
        }


        public void setData(PeptideInstance[] peptideInstances) {
            this.peptideInstances = peptideInstances;
            fireTableDataChanged();
            
        }
    
        public PeptideInstance[] getPeptideInstances() {
            return peptideInstances;
        }
        

    
}
