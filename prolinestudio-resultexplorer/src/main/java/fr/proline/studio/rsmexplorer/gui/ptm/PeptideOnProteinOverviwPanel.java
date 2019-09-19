/*
 * @cea 
 * http://www.profiproteomics.fr
 * Create Date: 27 mai 2019 
 */
package fr.proline.studio.rsmexplorer.gui.ptm;

import fr.proline.studio.dam.tasks.data.ptm.PTMPeptideInstance;
import fr.proline.studio.dam.tasks.data.ptm.PTMSite;
import fr.proline.studio.rsmexplorer.gui.renderer.PeptideRenderer;
import fr.proline.studio.utils.DataFormat;
import fr.proline.studio.utils.GlobalValues;
import java.awt.BasicStroke;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.JPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import javax.swing.BorderFactory;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

/**
 *
 * @author Karine XUE
 */
public class PeptideOnProteinOverviwPanel extends JPanel {

    private static final Logger m_logger = LoggerFactory.getLogger(PeptideOnProteinOverviwPanel.class);

    private PTMGraphicCtrlPanel m_superCtrl;
    /**
     * HashMap position on protein, List PTMPeptideInstance, useful for tootips
     * & find peptide from position
     */
    HashMap<Integer, ArrayList<PTMPeptideInstance>> m_AAPeptideMap;
    HashMap<Integer, AminoAcidPtm> m_PTMMap;

    private String m_sequence;
    private List<PTMPeptideInstance> m_peptideInstances;
    private PTMPeptideInstance m_selectedPeptideInstance;
    private int m_selectedPosition;
    private float m_aaWidth;//width of an amino acid
    private int m_startPositionProtein = 0;
    private int m_x0 = 10, m_y0 = 10;
    private final int m_height = 30;
    private final int m_ptm_y0 = 15;
    private final int m_ptm_height = m_height / 3;
    private final int m_protein_y0 = m_ptm_y0 + m_ptm_height;
    private final int m_protein_height = m_ptm_height * 2;
    private final String TITLE = "Protein Sequence Coverage";
    private final Color SELECTED_COLOR = new Color(0, 0, 150, 100);//blue
    private final Color PEPTIDE_COLOR = new Color(0, 200, 0, 100);//green
    //private Color TRANSPARENT_COLOR = new Color(0, 0, 0, 0);//transparent

    public PeptideOnProteinOverviwPanel(PTMGraphicCtrlPanel superCtrl) {
        super();
        this.m_superCtrl = superCtrl;
        this.setLayout(new BorderLayout());
        initComponent();
    }

    private void initComponent() {
        this.setBorder(BorderFactory.createTitledBorder(TITLE));
        this.addMouseListener(new SelectedMouseAdapter());
        this.addMouseMotionListener(new ToolTipMouseListener());
    }

    public void setSelectedPeptide(PTMPeptideInstance selectedPTMPeptideInstance) {
        m_selectedPeptideInstance = selectedPTMPeptideInstance;
    }

    public int getSelectedProteinPosition() {
        return this.m_selectedPosition;
    }

    private class ToolTipMouseListener implements MouseMotionListener {

        ToolTipMouseListener() {
            super();
        }

        @Override
        public void mouseMoved(MouseEvent e) {//for tooltips
            int x = e.getX();
            int y = e.getY();
            String tips = getTooltips(x, y);
            setToolTipText(tips);//null will turn off tooltip
        }

        @Override
        public void mouseDragged(MouseEvent e
        ) {
            // TODO Auto-generated method stub

        }
    }

    /**
     * set position at the begin of a peptide
     *
     * @param position
     */
    private void setSelectedOnProtein(int position) {
        this.m_selectedPosition = position;
        List<PTMPeptideInstance> pepList = this.m_AAPeptideMap.get(position);
        if (pepList != null) {
            int begin = 0;
            for (PTMPeptideInstance pep : pepList) {
                begin = Math.max(pep.getStartPosition(), begin);
            }
            this.m_selectedPosition = begin;
        }
    }

    private int getPosition(int x) {
        return (int) ((x - m_x0) / m_aaWidth);
    }

    private class SelectedMouseAdapter extends MouseAdapter {

        SelectedMouseAdapter() {
            super();
        }

        public void mouseClicked(MouseEvent e) {
            requestFocusInWindow();
            if (SwingUtilities.isLeftMouseButton(e)) {
                List<PTMPeptideInstance> peptides;
                AminoAcidPtm ptms;
                int x = e.getX();
                int y = e.getY();
                if (y > m_ptm_y0 && y < m_ptm_y0 + m_height) {
                    int positionOnProtein = getPosition(x);
                    setSelectedOnProtein(positionOnProtein);
                    m_superCtrl.onMessage(PTMGraphicCtrlPanel.Source.SEQUENCE, PTMGraphicCtrlPanel.Message.SELECTED);
                }
            }
        }
    }

    private enum ZoomMouseAction {
        ZOOM_IN,
        ZOOM_OUT,
        UN_ZOOM,
        MOVE_HORIZONTAL
    }

    private class ZoomMouseAdapter extends MouseAdapter {

        int currentX, currentY, startX, startY;
        boolean dragging = false;
        ZoomMouseAction action=null;
        
        ZoomMouseAdapter() {
            super();
        }

        public void mousePressed(MouseEvent event) {
            Point point = event.getPoint();
            startX = point.x;
            startY = point.y;
            dragging = true;
        }

        public void mouseReleased(MouseEvent event) {

        }

        public void mouseDragged(MouseEvent event) {
            Point p = event.getPoint();
            currentX = p.x;
            currentY = p.y;
            if (action == ZoomMouseAction.MOVE_HORIZONTAL){
                int distance = currentX-startX;
                int position = m_startPositionProtein + getPosition(distance);
                position = Math.max(0, position);
                m_startPositionProtein = Math.min(position, m_sequence.length());
                repaint();
                //move droit, gauch
            }
        }

        public void mouseWheelMoved(MouseWheelEvent e) {

            int nb = e.getWheelRotation();
            if (nb < 0) {
                // "Mouse wheel moved UP " zoom+

            } else {
                // "Mouse wheel moved DOWN "zoom -

            }

        }
    }

    public void setEmpty(String proteinName) {
        this.setBorder(BorderFactory.createTitledBorder(proteinName + " " + TITLE));
        this.removeAll();
    }

    public void setData(String proteinName, String sequence, PTMPeptideInstance selectedPeptide, List<PTMPeptideInstance> peptideInstances) {

        boolean isNew = true;

        if (m_sequence != null && m_sequence.equals(sequence)) {
            if (m_peptideInstances.equals(peptideInstances)) {
                isNew = false;
            }
        }
        m_peptideInstances = peptideInstances;
        m_sequence = sequence;
        m_selectedPeptideInstance = selectedPeptide;
        m_aaWidth = ((float) (this.getWidth() - 20) / m_sequence.length());
        String width = "" + m_aaWidth;
        m_logger.debug("Context width is {},protein length is {},  aaWidth is {}", this.getWidth(), m_sequence.length(), width);
        int proteinLength = sequence.length();
        //m_logger.debug("length: {} Amino Acid", proteinLength);
        if (isNew) {//if is not new, don't need to updata map
            createPTMPeptideMap(peptideInstances);
        }

        String title = proteinName + " " + TITLE + " " + proteinLength + " amino acid";

        ((TitledBorder) getBorder()).setTitle(title);
        this.removeAll();

        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        this.setSize(this.getWidth(), 90);
        Graphics2D g2 = (Graphics2D) g;
        if (m_peptideInstances != null && !m_peptideInstances.isEmpty()) {
            paintPTM(g2);
            paintPeptideOnSequence(g2);
        }
    }

    /**
     * paint PTMSite in increasing order
     *
     * @param g
     */
    private void paintPTM(Graphics2D g) {
        if (m_PTMMap.isEmpty()) {
            return;
        }
        int height = m_ptm_height;
        Stream<PTMSite> ptmStream = m_PTMMap.values().stream().map(bloc -> bloc.getPtmList()).flatMap(Collection::stream);
        List<PTMSite> sortedPtm = ptmStream.sorted(Comparator.comparing(PTMSite::getPositionOnProtein)).collect(Collectors.toList());
        int x0, y0;
        Color color;
        for (PTMSite ptm : sortedPtm) {
            x0 = (int) (m_x0 + m_aaWidth * ptm.getPositionOnProtein());
            y0 = m_ptm_y0;
            color = ViewSetting.getColor(ptm);
            g.setColor(color);
            g.fillRect(x0, y0, (int) ((m_aaWidth) + 1), m_ptm_height);//width minimum = 1
        }
    }

    private void paintPeptideOnSequence(Graphics2D g) {
        if (m_peptideInstances.isEmpty()) {
            return;
        }
        int width = this.getWidth();

        float referenceLineWidth = (float) (0.1 * m_protein_height); //10% of height
        BasicStroke refStroke = new BasicStroke(referenceLineWidth, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND);
        g.setColor(Color.LIGHT_GRAY);
        g.setStroke(refStroke);
        g.drawLine(m_x0, m_protein_y0 + m_protein_height / 2, m_x0 + width, m_protein_y0 + m_protein_height / 2); //middle  height

        g.setColor(PEPTIDE_COLOR);
        for (PTMPeptideInstance pep : m_peptideInstances) {
            paintPeptide(g, pep);
        }
        g.setColor(SELECTED_COLOR);
        if (m_selectedPeptideInstance != null) {
            paintPeptide(g, m_selectedPeptideInstance);
        }
    }

    private void paintPeptide(Graphics2D g, PTMPeptideInstance pep) {
        int start = pep.getStartPosition();
        int stop = pep.getStopPosition();
        int width = (int) ((float) (stop - start + 1) * m_aaWidth);
        int x0 = (int) (m_x0 + (m_aaWidth * start));
        g.fillRoundRect(x0, m_protein_y0, width, m_protein_height, m_protein_height, m_protein_height);
    }

    /**
     *
     * @param x
     * @param y
     * @return null, will refresh to show nothing
     */
    private String getTooltips(int x, int y) {
        if (m_PTMMap == null) {
            return null;
        }
        int yRangPTM = m_ptm_y0;
        int yRangA = m_protein_y0;
        int yRangZ = yRangA + m_protein_height;
        List<PTMPeptideInstance> peptides;
        AminoAcidPtm ptms;
        int positionOnProtein = getPosition(x);
        if (y > yRangPTM && y < yRangPTM + m_ptm_height) {
            ptms = this.m_PTMMap.get(positionOnProtein);
            if (ptms == null) {
                return null;
            } else {
                return ptms.getTooltips();
            }
        } else if (y > yRangA && y < yRangZ) {
            peptides = this.m_AAPeptideMap.get(positionOnProtein);
            if (peptides != null) {
                String s = "";
                for (PTMPeptideInstance pep : peptides) {
                    String sequence = PeptideRenderer.constructPeptideDisplay(pep.getPeptideInstance().getPeptide())
                            .replaceAll(GlobalValues.HTML_TAG_BEGIN, "")
                            .replaceAll(GlobalValues.HTML_TAG_END, "");
                    String score = DataFormat.format(pep.getBestPepMatch().getScore(), 2);
                    String tips = String.format("%d -%s- %d, score: %s", pep.getStartPosition(), sequence, pep.getStopPosition(), score);
                    if (sequence.contains(GlobalValues.HTML_TAG_SPAN_END)) {
                        tips = GlobalValues.HTML_TAG_BEGIN + "<body>" + tips + "</body>" + GlobalValues.HTML_TAG_END;
                    }
                    s += tips;
                }
                return s;
            }
        }
        return "position: " + positionOnProtein;
    }

    private void createPTMPeptideMap(List<PTMPeptideInstance> peptideInstances) {
        m_AAPeptideMap = new HashMap();
        m_PTMMap = new HashMap<>();
        ArrayList<PTMPeptideInstance> pepList;
        int start = 0, stop = 0;
        for (PTMPeptideInstance pep : peptideInstances) {
            start = pep.getStartPosition();
            stop = pep.getStopPosition();
            for (int i = start; i <= stop; i++) {
                pepList = m_AAPeptideMap.get(i);
                pepList = (pepList == null ? new ArrayList() : pepList);
                pepList.add(pep);
                m_AAPeptideMap.put(i, pepList);
            }
            List<PTMSite> ptmList = pep.getSites();
            for (PTMSite ptm : ptmList) {
                int position = ptm.getPositionOnProtein();
                AminoAcidPtm ap = m_PTMMap.get(position);
                if (ap == null) {
                    m_PTMMap.put(position, new AminoAcidPtm(ptm));
                } else {
                    if (!ap.contains(ptm)) {
                        ap.addPtm(ptm);
                    }
                }
            }
        }
    }

    private class AminoAcidPtm {

        private ArrayList<PTMSite> _ptmList;

        AminoAcidPtm(PTMSite ptm) {
            _ptmList = new ArrayList();
            _ptmList.add(ptm);
        }

        void addPtm(PTMSite ptm) {
            _ptmList.add(ptm);
        }

        public ArrayList<PTMSite> getPtmList() {
            return _ptmList;
        }

        public boolean contains(PTMSite ptm) {
            return this._ptmList.contains(ptm);
        }

        public String getTooltips() {
            String s = "";
            for (PTMSite ptm : _ptmList) {
                s += ptm.toProteinReadablePtmString();
            }
            return s;
        }
    }

}
