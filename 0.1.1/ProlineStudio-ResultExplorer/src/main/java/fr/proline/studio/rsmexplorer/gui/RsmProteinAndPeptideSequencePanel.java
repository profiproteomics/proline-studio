package fr.proline.studio.rsmexplorer.gui;

import fr.proline.core.orm.msi.*;
import fr.proline.core.orm.ps.PeptidePtm;
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
import javax.swing.UIManager;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;


/**
 *
 * @author JM235353
 */
public class RsmProteinAndPeptideSequencePanel extends HourglassPanel implements DataBoxPanelInterface {

    private AbstractDataBox m_dataBox;
    
     
    /**
     * Creates new form RsmProteinAndPeptideSequencePanel
     */
    public RsmProteinAndPeptideSequencePanel() {

        initComponents();
        
        editorPane.setEditable(false);
        editorPane.setContentType("text/html");

        
        HTMLEditorKit kit = new HTMLEditorKit();
        editorPane.setEditorKit(kit);
        
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
        editorPane.setDocument(doc);      
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        sequenceScrollPane = new javax.swing.JScrollPane();
        editorPane = new javax.swing.JEditorPane();

        setMaximumSize(new java.awt.Dimension(32767, 800));

        sequenceScrollPane.setMaximumSize(new java.awt.Dimension(0, 0));

        editorPane.setMaximumSize(new java.awt.Dimension(0, 0));
        sequenceScrollPane.setViewportView(editorPane);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(sequenceScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 138, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(sequenceScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 138, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JEditorPane editorPane;
    private javax.swing.JScrollPane sequenceScrollPane;
    // End of variables declaration//GEN-END:variables

    private final int HIGHLIGHT_NONE                       = 0x00;
    private final int HIGHLIGHT_PEPTIDE_SELECTED           = 0x01;
    private final int HIGHLIGHT_PEPTIDE_NOT_SELECTED       = 0x02;
    private final int HIGHLIGHT_MULTI_PEPTIDE_NOT_SELECTED = 0x04;
    private final int HIGHLIGHT_NTER_OR_CTER_MODIFICATION  = 0x08;
    private final int HIGHLIGHT_OTHER_MODIFICATION         = 0x10;
    

    public void setData(ProteinMatch pm, PeptideInstance selectedPeptide, PeptideInstance[] peptideInstances) {
        
        if ((pm == null) || ((pm.getTransientData().getBioSequenceMSI() == null) && (pm.getTransientData().getBioSequencePDI() == null))) {
            editorPane.setText("Protein Sequence not avaible in database");

            return;
        }
        
        
        String sequence = null;
        fr.proline.core.orm.msi.BioSequence bioSequenceMSI = pm.getTransientData().getBioSequenceMSI();
        if (pm.getTransientData().getBioSequenceMSI() != null) {
            sequence = pm.getTransientData().getBioSequenceMSI().getSequence();
        } else if (pm.getTransientData().getBioSequencePDI() != null) {
            sequence = pm.getTransientData().getBioSequencePDI().getSequence();
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

                Peptide p = peptideInstances[i].getTransientData().getBestPeptideMatch().getTransientData().getPeptide();
                hightlight(p, false, highlights);
            }
        }
        
        if (selectedPeptide != null) {
            // highlight for selected peptide (must be done last to override modifications
            // of overlaping non selected peptides
            hightlight(selectedPeptide.getTransientData().getBestPeptideMatch().getTransientData().getPeptide(), true, highlights);
        }

       
        editorPane.setText(constructDisplayedSequence(sequence, highlights));
        
        editorPane.setCaretPosition(0);


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

    @Override
    public void setLoading(int id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setLoaded(int id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    

}
