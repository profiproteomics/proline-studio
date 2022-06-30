/* 
 * Copyright (C) 2019
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1 
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License 
 * along with this program; If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */
package fr.proline.studio.rsmexplorer.gui.renderer;

import fr.profi.util.CollectionUtils;
import fr.proline.core.orm.msi.Peptide;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.msi.dto.DPeptidePTM;
import fr.proline.studio.table.renderer.GrayableTableCellRenderer;
import fr.proline.studio.utils.GlobalValues;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.List;
import java.util.*;

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
            displayString = constructPeptideDisplay(((DPeptideMatch) value).getPeptide(), (DPeptideMatch)value);
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

    public static String constructPeptideDisplay(Peptide peptide) {
        return PeptideRenderer.constructPeptideDisplay(peptide,null);
    }

    public static String constructPeptideDisplay(Peptide peptide, DPeptideMatch match) {
        String textToExport;

        if ((peptide != null) && (peptide.getTransientData() != null)) {
            String sequence = peptide.getSequence();

            List<Integer> ambiguousIndex = new ArrayList<>();
            boolean hasAmbiguity = false;
            if(match!= null && match.hasAmbiguousSeq()){
                hasAmbiguity = true;
                String ambiguityInfo = (String) ((Map<String, Object>) match.getPropertiesAsMap().get("mascot_properties")).get("ambiguity_string");
                List<String> ambiguityChars = Arrays.asList(ambiguityInfo.split(","));
                CollectionUtils.createSlidingWindow(ambiguityChars, 3).forEach(substInfo ->  {
                    ambiguousIndex.add(Integer.parseInt(substInfo.get(0)) -1);
                });
            }

            StringBuilder m_displaySB = new StringBuilder();
            HashMap<Integer, DPeptidePTM> ptmMap = peptide.getTransientData().getDPeptidePtmMap();
            boolean hasPtms = ptmMap != null;

            if(!hasPtms && !hasAmbiguity ) {
                m_displaySB.append(sequence);
            } else {
                m_displaySB.append(GlobalValues.HTML_TAG_BEGIN);

                int nb = sequence.length();
                for (int i = 0; i < nb; i++) {

                    boolean isAmbiguous = false;
                    if(ambiguousIndex.contains(i)){
                        isAmbiguous = true;
                    }

                    boolean nTerOrCterModification = false;
                    DPeptidePTM ptm = null;
                    if(hasPtms) {
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

                        ptm = ptmMap.get(i + 1);
                    }

                    boolean aminoAcidModification = (ptm != null);

                    if (nTerOrCterModification || aminoAcidModification || isAmbiguous) {

                        if (nTerOrCterModification && aminoAcidModification) {
                            m_displaySB.append("<span style='color:").append(GlobalValues.HTML_COLOR_VIOLET).append("'>");

                        } else if (nTerOrCterModification) {
                            m_displaySB.append("<span style='color:").append(GlobalValues.HTML_COLOR_GREEN).append("'>");

                        } else if (aminoAcidModification) {
                            m_displaySB.append("<span style='color:").append(GlobalValues.HTML_COLOR_ORANGE).append("'>");

                        }

                        if(isAmbiguous)
                            m_displaySB.append("<b>");

                        m_displaySB.append(sequence.charAt(i));

                        if(isAmbiguous)
                            m_displaySB.append("</b>");

                        if(nTerOrCterModification || aminoAcidModification)
                            m_displaySB.append(GlobalValues.HTML_TAG_SPAN_END);

                    } else {
                        m_displaySB.append(sequence.charAt(i));
                    }
                }

                m_displaySB.append(GlobalValues.HTML_TAG_END);
            }

            textToExport = m_displaySB.toString();

        } else {

            if (peptide == null) {
                textToExport = "";
            } else {
                textToExport = peptide.getSequence();
            }
        }

        return textToExport;
    }

    @Override
    public void setGrayed(boolean v) {
        m_grayed = v;
    }

}
