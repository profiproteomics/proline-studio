/*
 * @cea 
 * http://www.profiproteomics.fr
 * Create Date: 16 sept. 2019 
 */
package fr.proline.studio.rsmexplorer.gui.ptm;

import fr.proline.core.orm.msi.dto.DPeptideInstance;
import fr.proline.studio.dam.tasks.data.ptm.PTMPeptideInstance;
import fr.proline.studio.rsmexplorer.gui.RsmProteinAndPeptideOverviewPlotPanel;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

/**
 *
 * @author Karine XUE
 */
public class PeptideOnProteinOverviewPanel extends RsmProteinAndPeptideOverviewPlotPanel {

    private static final Logger m_logger = LoggerFactory.getLogger(PeptideOnProteinOverviewPanel.class);

    /**
     * HashMap position on protein, List PTMPeptideInstance, useful for paint a
     * cycle around the peptide to indicate this is the PTM cluster in
     * interesting
     */
    HashMap<Integer, ArrayList<PTMPeptideInstance>> m_postionPTMPeptideMap;
    private List<PTMPeptideInstance> m_PTMPeptideInstances;
    private PTMPeptideInstance m_selectedPTMPeptideInstance;
    private int m_selectedPosition;
    private final Color PTM_PEPTIDE_COLOR = Color.red;

    public PeptideOnProteinOverviewPanel(PTMGraphicCtrlPanel superCtrl) {
        super(superCtrl);
    }

    public void setSelectedPeptide(PTMPeptideInstance selectedPTMPeptideInstance) {
        m_selectedPTMPeptideInstance = selectedPTMPeptideInstance;
        repaint();
    }

    public int getSelectedProteinPosition() {
        return this.m_selectedPosition;
    }

    /**
     * set position at the begin of a peptide used to modify scroll bar position
     * in the PTMPeptidesGraphicView.java panel via superCtrl
     *
     * @param position
     */
    private void setSelectedOnProtein(int position) {
        this.m_selectedPosition = position;
        List<PTMPeptideInstance> pepList = this.m_postionPTMPeptideMap.get(position);
        if (pepList != null) {
            int begin = position;
            for (PTMPeptideInstance pep : pepList) {
                begin = Math.min(pep.getStartPosition(), begin);
            }
            this.m_selectedPosition = begin;
        }
        m_logger.debug("selected positon en protein is : {}", m_selectedPosition);
    }

    public void setData(String proteinName, String sequence, PTMPeptideInstance selectedPeptide, List<PTMPeptideInstance> ptmPeptideInstances, DPeptideInstance[] peptideInstances) {
        m_PTMPeptideInstances = ptmPeptideInstances;
        m_peptideInstances = peptideInstances;
        m_proteinLengh = sequence.length();
        String titleComment = "";
        if (sequence.startsWith("0")) {
            m_proteinLengh = Integer.valueOf(sequence.substring(1));
            titleComment = " (calculated <= protelin length)";
        }
        m_selectedPTMPeptideInstance = selectedPeptide;
        m_startPositionProtein = 0;
        m_peptidePTMMap = new HashMap();
        createPTMPeptideMap(ptmPeptideInstances);
        createAADataMap(peptideInstances);
        //m_proteinLengh may be changed after calculating, so m_aaWidthOriginal is calculated after above create
        m_aaWidthOriginal = ((double) (this.getWidth() - 20) / m_proteinLengh);
        m_aaWidth = m_aaWidthOriginal;
        String title = proteinName + " " + TITLE + " " + m_proteinLengh + " amino acid" + titleComment;
        ((TitledBorder) this.getBorder()).setTitle(title);
        this.removeAll();

        repaint();
    }
   
    @Override
    protected void paintSelectedPeptide(Graphics2D g) {
        g.setColor(PTM_PEPTIDE_COLOR);
        for (PTMPeptideInstance pep : m_PTMPeptideInstances) {//paint PTM clusters
            int start = pep.getStartPosition()+1;//strange, in PTMPeptideInstance, start is 1 position before 
            int stop = pep.getStopPosition();
            int length = stop-start+1;
            //m_logger.debug("sequence {}, position({}-{}, length={})",pep.getSequence(),start, stop,length);
            drawPeptide(g, start, length);
        }
        g.setColor(SELECTED_COLOR);
        if (m_selectedPTMPeptideInstance != null) {//paint selected PTM Cluster
            int start = m_selectedPTMPeptideInstance.getStartPosition()+1;
            int length = m_selectedPTMPeptideInstance.getStopPosition()-start+1;
            drawPeptide(g, start, length);
        }
    }
    
    private void createPTMPeptideMap(List<PTMPeptideInstance> peptideInstances) {
        m_postionPTMPeptideMap = new HashMap();
        ArrayList<PTMPeptideInstance> pepList;
        int start = 0, stop = 0;
        for (PTMPeptideInstance pep : peptideInstances) {
            start = pep.getStartPosition();
            stop = pep.getStopPosition();
            if (stop > m_proteinLengh) {
                m_proteinLengh = stop;
            }
            for (int i = start; i <= stop; i++) {
                pepList = m_postionPTMPeptideMap.get(i);
                pepList = (pepList == null ? new ArrayList() : pepList);
                pepList.add(pep);
                m_postionPTMPeptideMap.put(i, pepList);
            }
        }
    }

    @Override
    protected void actionMouseClicked(MouseEvent e) {
        requestFocusInWindow();
        if (SwingUtilities.isLeftMouseButton(e)) {
            List<PTMPeptideInstance> peptides;
            int x = e.getX();
            int y = e.getY();
            if (y > m_ptm_y0 && y < m_ptm_y0 + m_height) {
                int positionOnProtein = getPosOnProtein(x);
                setSelectedOnProtein(positionOnProtein);
                ((PTMGraphicCtrlPanel) m_superCtrl).onMessage(PTMGraphicCtrlPanel.Source.SEQUENCE, PTMGraphicCtrlPanel.Message.SELECTED);
            }
        }
    }
}
