package fr.proline.studio.rsmexplorer.gui.renderer;

import fr.proline.core.orm.msi.Peptide;
import fr.proline.core.orm.msi.SequenceMatch;
import fr.proline.core.orm.ps.PeptidePtm;
import fr.proline.studio.utils.GlobalValues;
import java.awt.Component;
import java.util.HashMap;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Renderer for a Peptide in a Table Cell
 * @author JM235353
 */
public class PeptideRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

        Peptide p = (Peptide) value;

        String displayString = constructPeptideDisplay(p);

        return super.getTableCellRendererComponent(table, displayString, isSelected, hasFocus, row, column);
    }

    private String constructPeptideDisplay(Peptide peptide) {

        SequenceMatch sequenceMatch = peptide.getTransientData().getSequenceMatch();

        if (sequenceMatch != null) {

            HashMap<Integer, PeptidePtm> ptmMap = peptide.getTransientData().getPeptidePtmMap();
            if (ptmMap != null) {
                displaySB.append("<HTML>");
            }

            // Add Before residue of the peptide
            String residueBefore = sequenceMatch.getResidueBefore();
            if (residueBefore != null) {
                displaySB.append(residueBefore.toUpperCase());
                displaySB.append('-');
            }


            String sequence = peptide.getSequence();

            if (ptmMap == null) {
                displaySB.append(sequence);
            } else {


                int nb = sequence.length();
                for (int i = 0; i < nb; i++) {

                    boolean nTerOrCterModification = false;
                    if (i == 0) {
                        PeptidePtm nterPtm = ptmMap.get(0);
                        if (nterPtm != null) {
                            nTerOrCterModification = true;
                        }
                    } else if (i == nb - 1) {
                        PeptidePtm cterPtm = ptmMap.get(-1);
                        if (cterPtm != null) {
                            nTerOrCterModification = true;
                        }
                    }

                    PeptidePtm ptm = ptmMap.get(i + 1);
                    boolean aminoAcidModification = (ptm != null);

                    if (nTerOrCterModification || aminoAcidModification) {
                        if (nTerOrCterModification && aminoAcidModification) {
                            displaySB.append("<span style='color:").append(GlobalValues.HTML_COLOR_VIOLET).append("'>");
                        } else if (nTerOrCterModification) {
                            displaySB.append("<span style='color:").append(GlobalValues.HTML_COLOR_GREEN).append("'>");
                        } else if (aminoAcidModification) {
                            displaySB.append("<span style='color:").append(GlobalValues.HTML_COLOR_ORANGE).append("'>");
                        }
                        displaySB.append(sequence.charAt(i));
                        displaySB.append("</span>");
                    } else {
                        displaySB.append(sequence.charAt(i));
                    }

                }

            }

            // Add After residue of the peptide
            String residueAfter = sequenceMatch.getResidueAfter();
            if (residueAfter != null) {
                displaySB.append('-');
                displaySB.append(residueAfter.toUpperCase());
            }

            if (ptmMap != null) {
                displaySB.append("</HTML>");
            }

            String res = displaySB.toString();
            displaySB.setLength(0);
            return res;
        }

        return peptide.getSequence();


    }
    private StringBuilder displaySB = new StringBuilder();
}
