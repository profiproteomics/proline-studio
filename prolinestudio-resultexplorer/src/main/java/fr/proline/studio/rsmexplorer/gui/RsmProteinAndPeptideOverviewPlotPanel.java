/*
 * @cea 
 * http://www.profiproteomics.fr
 * Create Date: 25 sept. 2019 
 */
package fr.proline.studio.rsmexplorer.gui;

import fr.proline.core.orm.msi.SequenceMatchPK;
import fr.proline.core.orm.msi.dto.DInfoPTM;
import fr.proline.core.orm.msi.dto.DPeptideInstance;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.msi.dto.DPeptidePTM;
import fr.proline.studio.dam.tasks.data.ptm.PTMPeptideInstance;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import fr.proline.studio.rsmexplorer.gui.ptm.ViewSetting;
import fr.proline.studio.rsmexplorer.gui.renderer.PeptideRenderer;
import fr.proline.studio.utils.DataFormat;
import fr.proline.studio.utils.GlobalValues;
import java.awt.BasicStroke;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import javax.swing.JPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import javax.swing.BorderFactory;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

/**
 *
 * @author Karine XUE
 */
public class RsmProteinAndPeptideOverviewPlotPanel extends JPanel {

    private static final Logger m_logger = LoggerFactory.getLogger(RsmProteinAndPeptideOverviewPlotPanel.class);

    protected DataBoxPanelInterface m_superCtrl;
    /**
     * HashMap position on protein, List DPeptideInstance, useful for tootips &
     * find peptide from mouse click position
     */
    protected HashMap<Integer, ArrayList<DPeptideInstance>> m_postionPeptideMap;
    /**
     * used to draw PTM mark above the peptide, alse used to getTooltips for
     * these mark
     */
    protected HashMap<Integer, ArrayList<DPeptidePTM>> m_peptidePTMMap;
    protected int m_proteinLengh;
    protected DPeptideInstance[] m_peptideInstances;
    protected DPeptideInstance m_selectedPeptideInstance;
    protected double m_aaWidth;//width of an amino acid
    protected double m_aaWidthOriginal;
    protected int m_startPositionProtein = 0;
    protected final int m_x0 = 10;
    protected final int m_y0 = 10;
    protected final int m_height = 25;
    protected final int m_ptm_y0 = 15;
    protected final int m_ptm_height = m_height / 2;
    protected final int m_protein_y0 = m_ptm_y0 + m_ptm_height;
    protected final int m_protein_height = m_height - m_ptm_height;
    protected final BasicStroke STROKE_PROTEINE_LINE = new BasicStroke(3, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND);
    protected final BasicStroke STROKE_NORMAL = new BasicStroke(2, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND);
    protected final String TITLE = "Protein Sequence Coverage";
    protected final Color SELECTED_COLOR = Color.black;//blue
    protected final Color PEPTIDE_COLOR = new Color(0, 200, 0, 100);//green with transparence
    protected int m_oldWidth;

    public RsmProteinAndPeptideOverviewPlotPanel(DataBoxPanelInterface superCtrl) {
        super();
        this.m_superCtrl = superCtrl;
        this.setLayout(new BorderLayout());
        initComponent();
    }

    private void initComponent() {
        this.setBorder(BorderFactory.createTitledBorder(TITLE));
        m_oldWidth = this.getWidth();
        ProteinMouseAdapter mouseAdapter = new ProteinMouseAdapter();
        this.addMouseListener(mouseAdapter);
        this.addMouseMotionListener(mouseAdapter);
        this.addMouseWheelListener(mouseAdapter);
    }

    public void setSelectedPeptide(DPeptideInstance selectedPeptideInstance) {
        m_selectedPeptideInstance = selectedPeptideInstance;
    }

    protected int getPosOnProtein(int x) {
        return m_startPositionProtein + (int) ((x - m_x0) / m_aaWidth);
    }

    private enum ZoomMouseAction {
        ZOOM_IN,
        ZOOM_OUT,
        UN_ZOOM,
        MOVE_HORIZONTAL
    }

    public void setEmpty(String proteinName) {
        this.setBorder(BorderFactory.createTitledBorder(proteinName + " " + TITLE));
        this.removeAll();
    }

    public void setData(String proteinName, String sequence, DPeptideInstance selectedPeptide, DPeptideInstance[] peptideInstances) {
        m_peptideInstances = peptideInstances;
        String titleComment = "";
        if (sequence == null) {
            m_proteinLengh = 0;
            titleComment = " (calculated <= protelin length)";
        } else {
            m_proteinLengh = sequence.length();
        }
        m_selectedPeptideInstance = selectedPeptide;
        m_startPositionProtein = 0;
        createAADataMap(peptideInstances);
        //m_proteinLengh may be changed after calculating, so m_aaWidthOriginal is calcul after above create
        m_aaWidthOriginal = ((double) (this.getWidth() - 20) / m_proteinLengh);
        m_aaWidth = m_aaWidthOriginal;
        String title = proteinName + " " + TITLE + " " + m_proteinLengh + " amino acid" + titleComment;
        ((TitledBorder) this.getBorder()).setTitle(title);
        this.removeAll();

        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        this.setSize(this.getWidth(), 90);
        g.setColor(Color.white);
        g.fillRect(0, 0, this.getWidth(), this.getHeight());
        if (this.getWidth() != m_oldWidth) {
            if (m_proteinLengh != 0 && m_aaWidth == m_aaWidthOriginal) {
                m_aaWidthOriginal = ((double) (this.getWidth() - 20) / m_proteinLengh);
                m_aaWidth = m_aaWidthOriginal;
                m_oldWidth = this.getWidth();
            }
        }
        Graphics2D g2 = (Graphics2D) g;
        if (m_peptideInstances != null && m_peptideInstances.length != 0) {
            paintPTM(g2);
            paintPeptideListOnSequence(g2);
            paintSelectedPeptide(g2);
        }
    }

    protected void paintPTM(Graphics2D g) {
        if (m_peptidePTMMap.isEmpty()) {
            return;
        }
        int height = m_ptm_height;

        int x0, y0;
        Color color;
        for (Integer i : m_peptidePTMMap.keySet()) {
            x0 = (int) (m_x0 + m_aaWidth * (i - m_startPositionProtein));
            y0 = m_ptm_y0;
            DPeptidePTM ptm = m_peptidePTMMap.get(i).get(0);//only paint the color for first element
            color = ViewSetting.getColor(ptm.getIdPtmSpecificity());
            g.setColor(color);
            g.fillRect(x0, y0, (int) ((m_aaWidth) + 1), m_ptm_height);//width minimum = 1
        }
    }

    protected void paintPeptideListOnSequence(Graphics2D g) {
        if (m_peptideInstances == null || m_peptideInstances.length == 0) {
            return;
        }

        int startPos = (int) (this.m_startPositionProtein * m_aaWidth);
        int endPos = (int) ((m_proteinLengh + 1) * m_aaWidth);
        g.setColor(Color.LIGHT_GRAY);
        g.setStroke(STROKE_PROTEINE_LINE);
        g.drawLine(m_x0 - startPos, m_protein_y0 + m_protein_height / 2, m_x0 - startPos + endPos, m_protein_y0 + m_protein_height / 2); //draw protein length in middle
        g.drawLine(m_x0 - startPos, m_protein_y0, m_x0 - startPos, m_protein_y0 + m_protein_height);//mark protein begin
        g.drawLine(m_x0 - startPos + endPos, m_protein_y0, m_x0 - startPos + endPos, m_protein_y0 + m_protein_height);//mark protein end

        g.setStroke(STROKE_NORMAL);
        g.setColor(PEPTIDE_COLOR);
        for (DPeptideInstance pep : m_peptideInstances) {
            paintPeptide(g, pep);
        }

    }

    protected void paintSelectedPeptide(Graphics2D g) {
        g.setColor(SELECTED_COLOR);
        if (m_selectedPeptideInstance != null) {
            int start = m_selectedPeptideInstance.getBestPeptideMatch().getSequenceMatch().getId().getStart();
            int stop = m_selectedPeptideInstance.getBestPeptideMatch().getSequenceMatch().getId().getStop();
            int length = stop - start + 1;
            drawPeptide(g, start, length);
        }
    }

    protected void drawPeptide(Graphics2D g, int start, int length) {
        int width = (int) ((float) (length) * m_aaWidth);
        int x0 = (int) (m_x0 + (m_aaWidth * (start - m_startPositionProtein)));
        g.drawRoundRect(x0, m_protein_y0, width, m_protein_height, m_protein_height, m_protein_height);
    }

    protected void paintPeptide(Graphics2D g, DPeptideInstance pep) {

        int start = pep.getBestPeptideMatch().getSequenceMatch().getId().getStart();
        int stop = pep.getBestPeptideMatch().getSequenceMatch().getId().getStop();
        int length = stop - start + 1;

        int width = (int) ((float) (length) * m_aaWidth);
        int x0 = (int) (m_x0 + (m_aaWidth * (start - m_startPositionProtein)));
        g.fillRoundRect(x0, m_protein_y0, width, m_protein_height, m_protein_height, m_protein_height);
    }

    /**
     *
     * @param x
     * @param y
     * @return null, will refresh to show nothing
     */
    private String getTooltips(int x, int y) {
        if (m_peptidePTMMap == null) {
            return null;
        }
        int yRangPTM = m_ptm_y0;
        int yRangA = m_protein_y0;
        int yRangZ = yRangA + m_protein_height;
        List<DPeptidePTM> ptms;
        int positionOnProtein = getPosOnProtein(x);

        if (y > yRangPTM && y < yRangPTM + m_ptm_height) {
            ptms = this.m_peptidePTMMap.get(positionOnProtein);
            if (ptms == null) {
                return null;
            } else {
                String s = "";
                for (DPeptidePTM pep : ptms) {
                    DInfoPTM ptmTypeInfo = DInfoPTM.getInfoPTMMap().get(pep.getIdPtmSpecificity());
                    int position = (int) pep.getSeqPosition();//position convert to int
                    s += ptmTypeInfo.toReadablePtmString(position);//@todo position on pep ou position on prot?
                }
                return s;
            }
        } else if (y > yRangA && y < yRangZ) {
            List<DPeptideInstance> peptides;
            List<PTMPeptideInstance> ptmPeptides;
            peptides = this.m_postionPeptideMap.get(positionOnProtein);
            String s = null;
            if (peptides != null) {
                s = (s == null) ? "" : s;
                for (DPeptideInstance pep : peptides) {
                    Long id = pep.getId();
                    String sequence = PeptideRenderer.constructPeptideDisplay(pep.getBestPeptideMatch().getPeptide())
                            .replaceAll(GlobalValues.HTML_TAG_BEGIN, "")
                            .replaceAll(GlobalValues.HTML_TAG_END, "");
                    String score = DataFormat.format(pep.getBestPeptideMatch().getScore(), 2);
                    SequenceMatchPK pepSequencePK = pep.getBestPeptideMatch().getSequenceMatch().getId();

                    String tips = String.format("%d -%s- %d, score: %s", pepSequencePK.getStart(), sequence, pepSequencePK.getStop(), score);
                    s = s + tips + "<br>";
                }
                return GlobalValues.HTML_TAG_BEGIN + "<body>" + s + "</body>" + GlobalValues.HTML_TAG_END;
            }
            return "position: " + positionOnProtein;
        }
        return null;
    }

    protected void createAADataMap(DPeptideInstance[] peptideInstances) {
        //long beginTime = System.currentTimeMillis();
        m_postionPeptideMap = new HashMap();
        m_peptidePTMMap = new HashMap();
        ArrayList<DPeptideInstance> pepList;
        ArrayList<DPeptideMatch> pepMatchList;

        for (DPeptideInstance pep : peptideInstances) {
            int start = 0, stop = 0;
            try {
                //create peptide on protein sequence couvrage
                DPeptideMatch bestPeptideMatch = pep.getBestPeptideMatch();
                if (bestPeptideMatch != null) {
                    start = bestPeptideMatch.getSequenceMatch().getId().getStart();
                    stop = bestPeptideMatch.getSequenceMatch().getId().getStop();
                    if (stop > m_proteinLengh) {
                        m_proteinLengh = stop;
                    }
                    for (int i = start; i <= stop; i++) {
                        pepList = m_postionPeptideMap.get(i);
                        pepList = (pepList == null ? new ArrayList() : pepList);
                        pepList.add(pep);
                        m_postionPeptideMap.put(i, pepList);
                    }
                }

                //create ptm
                Collection<DPeptidePTM> allPtm = bestPeptideMatch.getPeptide().getTransientData().getDPeptidePtmMap().values();
                for (DPeptidePTM ptm : allPtm) {
                    int position = (int) ptm.getSeqPosition() + start - 1;//position convert to int

                    ArrayList<DPeptidePTM> ptmList = m_peptidePTMMap.get(position);
                    if (ptmList == null) {
                        ptmList = new ArrayList();
                        ptmList.add(ptm);
                        m_peptidePTMMap.put(position, ptmList);
                    } else {
                        boolean isExist = false;
                        long ptmId = ptm.getIdPtmSpecificity();
                        for (DPeptidePTM pepPtm : ptmList) {
                            if (ptmId == pepPtm.getIdPtmSpecificity()) {
                                isExist = true;
                            }
                        }
                        if (isExist == false) {
                            ptmList.add(ptm);
                        }
                    }
                }
                //need to know ptm caractor, type , position
            } catch (NullPointerException nullE) {
                //has not peptide information, skip                
            }
        }
        //m_logger.debug("createAADataMap execution time: {} ms", (System.currentTimeMillis() - beginTime));
    }

    protected void actionMouseClicked(MouseEvent e) {
        return;
    }

    private class ProteinMouseAdapter extends MouseAdapter {

        int currentX, currentY, startX, startY;
        ZoomMouseAction _action = null;

        ProteinMouseAdapter() {
            super();
        }

        public void mouseClicked(MouseEvent e) {//for selected on the protein, _action will be transfer to superCtrl
            requestFocusInWindow();
            actionMouseClicked(e);
        }

        @Override
        public void mouseMoved(MouseEvent e) {//for tooltips
            int x = e.getX();
            int y = e.getY();
            String tips = getTooltips(x, y);
            setToolTipText(tips);//null will turn off tooltip
        }

        public void mousePressed(MouseEvent event) {
            _action = null;
            Point point = event.getPoint();
            startX = point.x;
            startY = point.y;
        }

        public void mouseReleased(MouseEvent event) {
            Point p = event.getPoint();
            currentX = p.x;
            currentY = p.y;
            if (_action == null) {
                return;
            }
            switch (_action) {
                case ZOOM_IN: //will not be presente
                    //m_logger.debug("release, ZOOM_IN");
                    break;
                case ZOOM_OUT:
                    //m_logger.debug("release, zoom out");
                    double scale = 1 + 0.2 * (Math.max((currentX - startX) / m_height, 1));
                    this.zoom(currentX, scale);
                    break;
                case UN_ZOOM:
                    //m_logger.debug("release, un zoom");
                    m_startPositionProtein = 0;
                    m_aaWidth = m_aaWidthOriginal;
                    repaint();
                    break;
                case MOVE_HORIZONTAL:
                    //m_logger.debug("release, MOVE_HORIZONTAL");
                    move();
                    break;
                default:
                    throw new AssertionError(_action.name());
            }

            startX = currentX;
            startY = currentY;
        }

        public void mouseDragged(MouseEvent event) {
            Point p = event.getPoint();
            currentX = p.x;
            currentY = p.y;
            if (SwingUtilities.isRightMouseButton(event)) {
                if (currentX - startX < 0) {
                    _action = ZoomMouseAction.UN_ZOOM;
                    //m_logger.debug("Un Zoom");
                } else {
                    _action = ZoomMouseAction.ZOOM_OUT;
                    //m_logger.debug(" Zoom out");
                }

            } else if (SwingUtilities.isLeftMouseButton(event) && (currentX - startX != 0)) {
                _action = ZoomMouseAction.MOVE_HORIZONTAL;
            }
            if (_action == ZoomMouseAction.MOVE_HORIZONTAL) {//move droit, gauch
                move();
            }
        }

        public void mouseWheelMoved(MouseWheelEvent event) {
            Point p = event.getPoint();
            currentX = p.x;
            int nbWheel = event.getWheelRotation();
            double scale = 1.2;
            if (nbWheel < 0) { // "Mouse wheel moved UP " zoom+   
                _action = ZoomMouseAction.ZOOM_OUT;
                zoom(currentX, scale);
            } else {
                _action = ZoomMouseAction.ZOOM_IN;
                zoom(currentX, scale);
            }
        }

        private void zoom(int x, double scale) {
            int startNb = (int) (x / m_aaWidth);;
            if (_action == ZoomMouseAction.ZOOM_OUT) { // "Mouse wheel moved UP " zoom+
                m_aaWidth = m_aaWidth * scale;
            } else if (_action == ZoomMouseAction.ZOOM_IN) {
                m_aaWidth = m_aaWidth / scale;
                if (m_aaWidth < m_aaWidthOriginal) {
                    m_aaWidth = m_aaWidthOriginal;
                    m_startPositionProtein = 0;
                }
            }
            int currentNB = (int) (currentX / m_aaWidth);
            m_startPositionProtein = m_startPositionProtein + startNb - currentNB;
            if (m_startPositionProtein < 0) {
                m_startPositionProtein = 0;
            }
            // m_logger.debug("ZZZZZZZ Zoom scale={},_action={},aaWidth={},distance2Start={}, start={}", scale, _action, m_aaWidth, nbAA, m_startPositionProtein);
            repaint();
        }

        private void move() {
            int distance = currentX - startX;
            //m_logger.debug("MMMMMMMM Move currentX = {} startX = {}", currentX, startX);
            int startPosition = getPosOnProtein(startX);
            int currentPosition = getPosOnProtein(currentX);
            distance = currentPosition - startPosition;
            //m_logger.debug("MMMMMMMM Move horizontal move distance = {} positionOld = {},positionCurrent = {}, ", distance, startPosition, currentPosition);
            m_startPositionProtein = Math.min(m_startPositionProtein - distance, m_proteinLengh - (int) ((getWidth() - 20) / m_aaWidth));
            m_startPositionProtein = Math.max(m_startPositionProtein, 0);
            startX = currentX;
            startY = currentY;
            repaint();
        }
    }
}
