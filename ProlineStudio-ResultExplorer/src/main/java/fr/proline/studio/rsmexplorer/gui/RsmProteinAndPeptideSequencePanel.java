package fr.proline.studio.rsmexplorer.gui;


import fr.proline.core.orm.msi.Peptide;
import fr.proline.core.orm.msi.PeptideInstance;
import fr.proline.core.orm.msi.SequenceMatchPK;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.msi.dto.DProteinMatch;
import fr.proline.core.orm.ps.PeptidePtm;
import fr.proline.studio.export.ExportButton;
import fr.proline.studio.gui.HourglassPanel;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import fr.proline.studio.utils.GlobalValues;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import javax.swing.*;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;


/**
 * Panel to display the sequence of a Protein and the sequences of its Peptides found
 * @author JM235353
 */
public class RsmProteinAndPeptideSequencePanel extends HourglassPanel implements DataBoxPanelInterface {

    private AbstractDataBox m_dataBox;
    
    private JEditorPane m_editorPane;
    private JScrollPane m_sequenceScrollPane;
     
    private JPanel m_sequencePanel;

    public RsmProteinAndPeptideSequencePanel() {

        setLayout(new BorderLayout());
        
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

        
        Color selectionColor = UIManager.getColor ("Table.selectionBackground");

        styleSheet.addRule("p.body {color:black; font: bold 10px monospace; margin: 4px; }"); // JPM.HACK : this rule was for the body, but there is a Java bug, and it was later used for all html display
        styleSheet.addRule("span.peptidesel {background-color:#"+Integer.toHexString(selectionColor.getRGB() & 0xffffff)+"; color:white;}");
        styleSheet.addRule("span.onepeptide {background-color: #DDDDDD;}");
        styleSheet.addRule("span.multipeptides {background-color: #C0C0C0;}");
        styleSheet.addRule("span.modif_nter_cter {background-color: "+GlobalValues.HTML_COLOR_VIOLET+"}");
        styleSheet.addRule("span.modif {background-color: "+GlobalValues.HTML_COLOR_ORANGE+"}");
        styleSheet.addRule("span.nter_cter {background-color: "+GlobalValues.HTML_COLOR_GREEN+"}");
        

        Document doc = kit.createDefaultDocument();
        m_editorPane.setDocument(doc);   
        
        sequencePanel.add(m_sequenceScrollPane, BorderLayout.CENTER);
        
        return sequencePanel;
    }

    public final JToolBar initToolbar() {
            
        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.setFloatable(false);

        ExportButton exportImageButton = new ExportButton("Protein Sequence", m_sequencePanel);

        
        toolbar.add(exportImageButton);

        return toolbar;

    }
    

    private final int HIGHLIGHT_NONE                       = 0x00;
    private final int HIGHLIGHT_PEPTIDE_SELECTED           = 0x01;
    private final int HIGHLIGHT_PEPTIDE_NOT_SELECTED       = 0x02;
    private final int HIGHLIGHT_MULTI_PEPTIDE_NOT_SELECTED = 0x04;
    private final int HIGHLIGHT_NTER_OR_CTER_MODIFICATION  = 0x08;
    private final int HIGHLIGHT_OTHER_MODIFICATION         = 0x10;
    

    public void setData(DProteinMatch pm, PeptideInstance selectedPeptide, PeptideInstance[] peptideInstances) {
        
        if ((pm == null) || (pm.getBioSequence() == null)) {
            m_editorPane.setText("Protein Sequence not avaible in database");

            return;
        }
        
        
        String sequence = null;
        fr.proline.core.orm.msi.BioSequence bioSequenceMSI = pm.getBioSequence();
        if (pm.getBioSequence() != null) {
            sequence = pm.getBioSequence().getSequence();
        }
        
        
        int sequenceLength = sequence.length();
        
        int[] highlights = new int[sequenceLength];
        for (int i=0;i<sequenceLength;i++) {
            highlights[i] = HIGHLIGHT_NONE;
        }
        
        if (peptideInstances != null) {

            // highlight for non selected peptides
            int nbPeptides = peptideInstances.length;
            for (int i = 0; i < nbPeptides; i++) {

                if (peptideInstances[i].equals(selectedPeptide)) {
                    continue;
                }

                Peptide p = ((DPeptideMatch)peptideInstances[i].getTransientData().getBestPeptideMatch()).getPeptide();
                hightlight(p, false, highlights);
            }
        }
        
        if (selectedPeptide != null) {
            // highlight for selected peptide (must be done last to override modifications
            // of overlaping non selected peptides
            hightlight(((DPeptideMatch)selectedPeptide.getTransientData().getBestPeptideMatch()).getPeptide(), true, highlights);
        }

       
        m_editorPane.setText(constructDisplayedSequence(sequence, highlights));
        
        m_editorPane.setCaretPosition(0);


    }
    private void hightlight(Peptide p, boolean selectedPeptide, int[] highlights) {
                   
            Peptide.TransientData peptideData = p.getTransientData();
            SequenceMatchPK smpk = peptideData.getSequenceMatch().getId();
            
            
            int start = smpk.getStart();
            int stop = smpk.getStop();
            
            if (selectedPeptide) {
                for (int j=start;j<=stop;j++) {
                    highlights[j-1] = HIGHLIGHT_PEPTIDE_SELECTED;
                }
            } else {
                for (int j=start;j<=stop;j++) {
                    if ((highlights[j-1] & HIGHLIGHT_PEPTIDE_NOT_SELECTED) == HIGHLIGHT_PEPTIDE_NOT_SELECTED) {
                        highlights[j-1] |= HIGHLIGHT_MULTI_PEPTIDE_NOT_SELECTED;
                    } else {
                        highlights[j-1] |= HIGHLIGHT_PEPTIDE_NOT_SELECTED;
                    }
                }
            }
            
            HashMap<Integer,PeptidePtm> ptmMap = peptideData.getPeptidePtmMap();
            if (ptmMap != null) {
                Collection<PeptidePtm> peptidePtms = ptmMap.values();
                Iterator<PeptidePtm> it = peptidePtms.iterator();
                while (it.hasNext()) {
                    PeptidePtm ptm = it.next();
                    int pos = ptm.getSeqPosition();
                    if (pos == 0){
                        // Nter
                        highlights[start-1] |= HIGHLIGHT_NTER_OR_CTER_MODIFICATION;
                    } else if (pos==-1) {
                        // Cter
                        highlights[stop-1] |= HIGHLIGHT_NTER_OR_CTER_MODIFICATION;
                    } else {
                        highlights[start-1+pos-1] |= HIGHLIGHT_OTHER_MODIFICATION; 
                    }
                }
            }
    }
    
    private String constructDisplayedSequence(String sequence, int[] highlights) {



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
                } else  if ((state & HIGHLIGHT_NTER_OR_CTER_MODIFICATION) == HIGHLIGHT_NTER_OR_CTER_MODIFICATION) {
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
        
        sb.append("</p></body></html>");
        
        return sb.toString();
    }


    @Override
    public void setDataBox(AbstractDataBox dataBox) {
        m_dataBox = dataBox;
    }
    
    @Override
    public ActionListener getRemoveAction(SplittedPanelContainer splittedPanel) {
        return m_dataBox.getRemoveAction(splittedPanel);
    }

    @Override
    public ActionListener getAddAction(SplittedPanelContainer splittedPanel) {
        return m_dataBox.getAddAction(splittedPanel);
    }


    

}
