package fr.proline.studio.rsmexplorer.gui.renderer;

import fr.proline.core.orm.msi.Peptide;
import fr.proline.core.orm.msi.SequenceMatch;
import fr.proline.core.orm.ps.PeptidePtm;
import fr.proline.studio.export.ExportTextInterface;
import fr.proline.studio.utils.GlobalValues;
import java.awt.Component;
import java.util.HashMap;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Renderer for a Peptide in a Table Cell
 * @author JM235353
 */
public class PeptideRenderer extends DefaultTableCellRenderer implements ExportTextInterface {

    private String m_basicTextForExport = "";
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

        String displayString;
        
        if (value == null) {
            displayString = "";
        } else {
            Peptide p = (Peptide) value;

            displayString = constructPeptideDisplay(p);
        }
        
        return super.getTableCellRendererComponent(table, displayString, isSelected, hasFocus, row, column);
    }

    private String constructPeptideDisplay(Peptide peptide) {

        SequenceMatch sequenceMatch = peptide.getTransientData().getSequenceMatch();

        if (sequenceMatch != null) {

            HashMap<Integer, PeptidePtm> ptmMap = peptide.getTransientData().getPeptidePtmMap();
            if (ptmMap != null) {
                m_displaySB.append("<HTML>");
            }

            // Add Before residue of the peptide
            Character residueBefore = sequenceMatch.getResidueBefore();
            if (residueBefore != null) {
                m_displaySB.append(Character.toUpperCase(residueBefore));
                m_displaySB.append('-');
                
                m_exportSB.append(Character.toUpperCase(residueBefore));
                m_exportSB.append('-');
            }


            String sequence = peptide.getSequence();

            if (ptmMap == null) {
                m_displaySB.append(sequence);
                m_exportSB.append(sequence);
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
                            m_displaySB.append("<span style='color:").append(GlobalValues.HTML_COLOR_VIOLET).append("'>");
                        } else if (nTerOrCterModification) {
                            m_displaySB.append("<span style='color:").append(GlobalValues.HTML_COLOR_GREEN).append("'>");
                        } else if (aminoAcidModification) {
                            m_displaySB.append("<span style='color:").append(GlobalValues.HTML_COLOR_ORANGE).append("'>");
                        }
                        m_displaySB.append(sequence.charAt(i));
                        m_displaySB.append("</span>");
                    } else {
                        m_displaySB.append(sequence.charAt(i));
                    }
                    m_exportSB.append(sequence.charAt(i));

                }

            }

            // Add After residue of the peptide
            Character residueAfter = sequenceMatch.getResidueAfter();
            if (residueAfter != null) {
                m_displaySB.append('-');
                m_displaySB.append(Character.toUpperCase(residueAfter));
                m_exportSB.append('-');
                m_exportSB.append(Character.toUpperCase(residueAfter));
            }

            if (ptmMap != null) {
                m_displaySB.append("</HTML>");
            }

            m_basicTextForExport = m_exportSB.toString();
            m_exportSB.setLength(0);
            
            String res = m_displaySB.toString();
            m_displaySB.setLength(0);
            return res;
        }

        m_basicTextForExport = peptide.getSequence();
        
        return m_basicTextForExport;


    }
    private StringBuilder m_displaySB = new StringBuilder();
    private StringBuilder m_exportSB = new StringBuilder();

    @Override
    public String getExportText() {
        return m_basicTextForExport;
    }
}
