/* 
 * Copyright (C) 2019 VD225637
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
package fr.proline.studio.rsmexplorer.gui;

import fr.proline.core.orm.msi.SequenceMatchPK;
import fr.proline.core.orm.msi.dto.DPeptideInstance;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.msi.dto.DPeptidePTM;
import fr.proline.core.orm.msi.dto.DProteinMatch;
import fr.proline.studio.dam.DatabaseDataManager;
import fr.proline.studio.dam.tasks.DatabaseBioSequenceTask;
import fr.proline.studio.export.ExportButton;
import fr.proline.studio.gui.HourglassPanel;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import fr.proline.studio.utils.DataFormat;
import fr.proline.studio.utils.GlobalValues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.event.ActionListener;
import java.util.*;
import javax.swing.*;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

/**
 * Panel to display the sequence of a Protein and the sequences of its Peptides
 * found
 *
 * @author JM235353
 */
public class RsmProteinAndPeptideSequencePanel extends HourglassPanel implements DataBoxPanelInterface {

    private static final Logger m_logger = LoggerFactory.getLogger(RsmProteinAndPeptideSequencePanel.class);

    private AbstractDataBox m_dataBox;
    private JEditorPane m_editorPane;
    private JScrollPane m_sequenceScrollPane;
    private RsmProteinAndPeptideOverviewPlotPanel m_sequencePoltPane;

    private JPanel m_sequencePanel;

    public RsmProteinAndPeptideSequencePanel() {

        setLayout(new BorderLayout());
        m_sequencePoltPane = new RsmProteinAndPeptideOverviewPlotPanel(this);
        m_sequencePanel = createSequencePanel();

        add(m_sequencePanel, BorderLayout.CENTER);

        JToolBar toolbar = initToolbar();
        add(toolbar, BorderLayout.WEST);

    }

    private JPanel createSequencePanel() {

        JPanel sequencePanel = new JPanel(new BorderLayout());

        m_sequenceScrollPane = new javax.swing.JScrollPane();
        m_editorPane = new javax.swing.JEditorPane();
        m_sequenceScrollPane.setViewportView(m_editorPane);

        m_editorPane.setEditable(false);
        m_editorPane.setContentType("text/html");

        HTMLEditorKit kit = new HTMLEditorKit();
        m_editorPane.setEditorKit(kit);

        StyleSheet styleSheet = kit.getStyleSheet();

        Color selectionColor = UIManager.getColor("Table.selectionBackground");

        styleSheet.addRule("p.body {color:black; font: bold 10px monospace; margin: 4px; }"); // JPM.HACK : this rule was for the body, but there is a Java bug, and it was later used for all html display
        styleSheet.addRule("span.peptidesel {background-color:#" + Integer.toHexString(selectionColor.getRGB() & 0xffffff) + "; color:white;}");
        styleSheet.addRule("span.onepeptide {background-color: #DDDDDD;}");
        styleSheet.addRule("span.multipeptides {background-color: #C0C0C0;}");
        styleSheet.addRule("span.modif_nter_cter {background-color: " + GlobalValues.HTML_COLOR_VIOLET + "}");
        styleSheet.addRule("span.modif {background-color: " + GlobalValues.HTML_COLOR_ORANGE + "}");
        styleSheet.addRule("span.nter_cter {background-color: " + GlobalValues.HTML_COLOR_GREEN + "}");

        Document doc = kit.createDefaultDocument();
        m_editorPane.setDocument(doc);

        m_sequencePoltPane.setPreferredSize(new Dimension(this.getWidth(), 50));
        m_sequenceScrollPane.setPreferredSize(new Dimension(this.getWidth(), this.getHeight() - 50));

        sequencePanel.add(m_sequenceScrollPane, BorderLayout.CENTER);
        sequencePanel.add(m_sequencePoltPane, BorderLayout.SOUTH);
        return sequencePanel;
    }

    public final JToolBar initToolbar() {

        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.setFloatable(false);

        ExportButton exportImageButton = new ExportButton("Protein Sequence", m_sequencePanel);

        toolbar.add(exportImageButton);

        return toolbar;

    }

    private final int HIGHLIGHT_NONE = 0x00;
    private final int HIGHLIGHT_PEPTIDE_SELECTED = 0x01;
    private final int HIGHLIGHT_PEPTIDE_NOT_SELECTED = 0x02;
    private final int HIGHLIGHT_MULTI_PEPTIDE_NOT_SELECTED = 0x04;
    private final int HIGHLIGHT_NTER_OR_CTER_MODIFICATION = 0x08;
    private final int HIGHLIGHT_OTHER_MODIFICATION = 0x10;

    public void setData(Long projectId, DProteinMatch pm, DPeptideInstance selectedPeptide, DPeptideInstance[] peptideInstances) {

        if (pm == null) {
            m_editorPane.setText("");
            return;
        }

        if (peptideInstances == null) {
            m_editorPane.setText("");
            return;
        }

        if (!DatabaseDataManager.getDatabaseDataManager().isSeqDatabaseExists()) {
            // Seq Database does not exists : no data is available
            m_editorPane.setText("There is no Protein Sequence Database available. Please contact your IT Administrator to install it.");
            return;
        }
    
        if (!pm.isDBiosequenceSet() && projectId != null) {
            m_logger.info("BioSequence is absent from the protein match, trying to load it ...");
            DatabaseBioSequenceTask.fetchData(Collections.singletonList(pm), projectId);
        }

        if (pm.isDBiosequenceSet()) {
            String sequence = pm.getDBioSequence().getSequence();
            int sequenceLength = sequence.length();

            int[] highlights = new int[sequenceLength];
            for (int i = 0; i < sequenceLength; i++) {
                highlights[i] = HIGHLIGHT_NONE;
            }

            // highlight for non selected peptides
            int nbPeptides = peptideInstances.length;
            for (int i = 0; i < nbPeptides; i++) {

                if (peptideInstances[i].equals(selectedPeptide)) {
                    continue;
                }

                DPeptideMatch peptideMatch = ((DPeptideMatch) peptideInstances[i].getBestPeptideMatch());
                hightlight(peptideMatch, false, highlights);
            }

            if (selectedPeptide != null) {
                // highlight for selected peptide (must be done last to override modifications
                // of overlaping non selected peptides
                hightlight(((DPeptideMatch) selectedPeptide.getBestPeptideMatch()), true, highlights);
            }

            // calculate coverage
            int noCoverageNumber = 0;
            for (int i = 0; i < sequenceLength; i++) {
                if (highlights[i] == HIGHLIGHT_NONE) {
                    noCoverageNumber++;
                }
            }
            double coverage = (((double) (sequenceLength - noCoverageNumber)) / ((double) sequenceLength)) * 100.0d;
            String coverageFormatted = DataFormat.format(coverage, 2);

            m_editorPane.setText(constructDisplayedSequence(pm, sequence, highlights, coverageFormatted));
            m_editorPane.setCaretPosition(0);
            m_sequencePoltPane.setData(pm.getAccession(), sequence, selectedPeptide, peptideInstances);
        } else {
            m_editorPane.setText("Protein Sequence not available in database");
            m_sequencePoltPane.setData(pm.getAccession(), null, selectedPeptide, peptideInstances);
        }
        repaint();
    }

    private void hightlight(DPeptideMatch p, boolean selectedPeptide, int[] highlights) {

        SequenceMatchPK smpk = p.getSequenceMatch().getId();

        int start = smpk.getStart();
        int stop = smpk.getStop();

        if (selectedPeptide) {
            for (int j = start; j <= stop; j++) {
                highlights[j - 1] = HIGHLIGHT_PEPTIDE_SELECTED;
            }
        } else {
            for (int j = start; j <= stop; j++) {
                if ((highlights[j - 1] & HIGHLIGHT_PEPTIDE_NOT_SELECTED) == HIGHLIGHT_PEPTIDE_NOT_SELECTED) {
                    highlights[j - 1] |= HIGHLIGHT_MULTI_PEPTIDE_NOT_SELECTED;
                } else {
                    highlights[j - 1] |= HIGHLIGHT_PEPTIDE_NOT_SELECTED;
                }
            }
        }

        HashMap<Integer, DPeptidePTM> ptmMap = p.getPeptide().getTransientData().getDPeptidePtmMap();
        if (ptmMap != null) {
            Collection<DPeptidePTM> peptidePtms = ptmMap.values();
            Iterator<DPeptidePTM> it = peptidePtms.iterator();
            while (it.hasNext()) {
                DPeptidePTM ptm = it.next();
                int pos = (int) ptm.getSeqPosition();
                if (pos == 0) {
                    // Nter
                    highlights[start - 1] |= HIGHLIGHT_NTER_OR_CTER_MODIFICATION;
                } else if (pos == -1) {
                    // Cter
                    highlights[stop - 1] |= HIGHLIGHT_NTER_OR_CTER_MODIFICATION;
                } else {
                    highlights[start - 1 + pos - 1] |= HIGHLIGHT_OTHER_MODIFICATION;
                }
            }
        }
    }

    private String constructDisplayedSequence(DProteinMatch pm, String sequence, int[] highlights, String coverageFormatted) {

        StringBuilder sb = new StringBuilder();

        sb.append("<html><body><p class='body'>");

        int previousState = HIGHLIGHT_NONE;

        int nb = sequence.length();
        for (int i = 0; i < nb; i++) {
            char c = sequence.charAt(i);

            int state = highlights[i];

            if (state != previousState) {
                if (previousState != HIGHLIGHT_NONE) {
                    sb.append("</span>");
                }

                // add a blank space every ten characters
                if (i % 10 == 0) {
                    sb.append(' ');
                }

                if ((state & HIGHLIGHT_OTHER_MODIFICATION) == HIGHLIGHT_OTHER_MODIFICATION) {
                    if ((state & HIGHLIGHT_NTER_OR_CTER_MODIFICATION) == HIGHLIGHT_NTER_OR_CTER_MODIFICATION) {
                        // Modification and (nter or cter) in the same time
                        sb.append("<span class='modif_nter_cter'>");
                    } else {
                        sb.append("<span class='modif'>");
                    }
                } else if ((state & HIGHLIGHT_NTER_OR_CTER_MODIFICATION) == HIGHLIGHT_NTER_OR_CTER_MODIFICATION) {
                    sb.append("<span class='nter_cter'>");
                } else if ((state & HIGHLIGHT_PEPTIDE_SELECTED) == HIGHLIGHT_PEPTIDE_SELECTED) {
                    sb.append("<span class='peptidesel'>");
                } else if ((state & HIGHLIGHT_MULTI_PEPTIDE_NOT_SELECTED) == HIGHLIGHT_MULTI_PEPTIDE_NOT_SELECTED) {
                    sb.append("<span class='multipeptides'>");
                } else if ((state & HIGHLIGHT_PEPTIDE_NOT_SELECTED) == HIGHLIGHT_PEPTIDE_NOT_SELECTED) {
                    sb.append("<span class='onepeptide'>");
                }
            } else {
                // add a blank space every ten characters
                if (i % 10 == 0) {
                    sb.append(' ');
                }
            }

            sb.append(c);

            previousState = state;
        }

        // close last span if needed
        if (previousState != HIGHLIGHT_NONE) {
            sb.append("</span>");
        }

        sb.append("<br><br>");
        sb.append("<span class='nter_cter'>&nbsp;&nbsp;</span>&nbsp;N/C-ter PTM&nbsp;&nbsp;&nbsp;&nbsp;");
        sb.append("<span class='modif'>&nbsp;&nbsp;</span>&nbsp;AA PTM&nbsp;&nbsp;&nbsp;&nbsp;");
        sb.append("<span class='modif_nter_cter'>&nbsp;&nbsp;</span>&nbsp;N/C-ter and AA PTM");
        sb.append("<br><br>");
        sb.append("Coverage : ").append(coverageFormatted).append('%');
        sb.append("<br><br>");
        sb.append(pm.getAccession()).append(" : ").append(pm.getDescription());
        sb.append("</p></body></html>");

        return sb.toString();
    }

    @Override
    public void setDataBox(AbstractDataBox dataBox) {
        m_dataBox = dataBox;
    }

    @Override
    public AbstractDataBox getDataBox() {
        return m_dataBox;
    }

    @Override
    public void addSingleValue(Object v) {
        // should not be used
    }

    @Override
    public ActionListener getRemoveAction(SplittedPanelContainer splittedPanel) {
        return m_dataBox.getRemoveAction(splittedPanel);
    }

    @Override
    public ActionListener getAddAction(SplittedPanelContainer splittedPanel) {
        return m_dataBox.getAddAction(splittedPanel);
    }

    @Override
    public ActionListener getSaveAction(SplittedPanelContainer splittedPanel) {
        return m_dataBox.getSaveAction(splittedPanel);
    }

}
