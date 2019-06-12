package fr.proline.studio.rsmexplorer.gui.renderer;

import fr.proline.core.orm.msi.Peptide;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.msi.dto.DPeptidePTM;
import fr.proline.studio.utils.GlobalValues;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import fr.proline.studio.table.renderer.GrayableTableCellRenderer;

/**
 * Renderer for a Peptide in a Table Cell
 *
 * @author JM235353
 */
public class PeptideRenderer extends DefaultTableCellRenderer implements GrayableTableCellRenderer {

    private boolean m_grayed = false;

    public PeptideRenderer() {

    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

        String displayString;

        if ((value == null) || !(DPeptideMatch.class.isAssignableFrom(value.getClass()) || Peptide.class.isAssignableFrom(value.getClass()))) {
            displayString = "";
        } else if (DPeptideMatch.class.isAssignableFrom(value.getClass())) {
            displayString = constructPeptideDisplay(((DPeptideMatch) value).getPeptide());
        } else {
            displayString = constructPeptideDisplay((Peptide) value);
        }

        JLabel l = (JLabel) super.getTableCellRendererComponent(table, displayString, isSelected, hasFocus, row, column);
        if (m_grayed) {
            l.setFont(l.getFont().deriveFont(Font.ITALIC));
            l.setForeground(Color.lightGray);
        }

        return l;
    }
    public static final String HTML_BEGIN = "<HTML>";
    public static final String HTML_END = "</HTML>";
    public static final String SPAN_END = "</span>";
    public static String constructPeptideDisplay(Peptide peptide) {
        StringBuilder m_displaySB = new StringBuilder();
        StringBuilder m_exportSB = new StringBuilder();
        String textToExport;

        if ((peptide != null) && (peptide.getTransientData() != null)) {

            HashMap<Integer, DPeptidePTM> ptmMap = peptide.getTransientData().getDPeptidePtmMap();
            if (ptmMap != null) {
                m_displaySB.append(HTML_BEGIN);
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
                        DPeptidePTM nterPtm = ptmMap.get(0);
                        if (nterPtm != null) {
                            nTerOrCterModification = true;
                        }
                    } else if (i == nb - 1) {
                        DPeptidePTM cterPtm = ptmMap.get(-1);
                        if (cterPtm != null) {
                            nTerOrCterModification = true;
                        }
                    }

                    DPeptidePTM ptm = ptmMap.get(i + 1);
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

            if (ptmMap != null) {
                m_displaySB.append(HTML_END);
            }

            textToExport = m_exportSB.toString();
            m_exportSB.setLength(0);

            String res = m_displaySB.toString();
            m_displaySB.setLength(0);
            return res;
        }

        if (peptide == null) {
            textToExport = "";
        } else {
            textToExport = peptide.getSequence();
        }
        return textToExport;
    }

    @Override
    public void setGrayed(boolean v) {
        m_grayed = v;
    }

}
