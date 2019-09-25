/*
 * @cea 
 * http://www.profiproteomics.fr
 * Create Date: 27 mai 2019 
 */
package fr.proline.studio.rsmexplorer.gui.ptm;

import fr.proline.core.orm.msi.SequenceMatchPK;
import fr.proline.core.orm.msi.dto.DInfoPTM;
import fr.proline.core.orm.msi.dto.DPeptideInstance;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.msi.dto.DPeptidePTM;
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
import java.awt.event.MouseWheelEvent;
import javax.swing.BorderFactory;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

/**
 *
 * @author Karine XUE
 */
public class PeptideOnProteinOverviewPanel extends JPanel {

    private static final Logger m_logger = LoggerFactory.getLogger(PeptideOnProteinOverviewPanel.class);

    private PTMGraphicCtrlPanel m_superCtrl;
    /**
     * HashMap position on protein, List PTMPeptideInstance, useful for tootips
     * & find peptide from mouse click position
     */
    HashMap<Integer, ArrayList<PTMPeptideInstance>> m_postionPTMPeptideMap;
    HashMap<Integer, ArrayList<DPeptideInstance>> m_postionPeptideMap;
    /**
     * used to draw PTM mark above the peptide
     */
    HashMap<Integer, AminoAcidPtm> m_PTMMap;
    HashMap<Integer, ArrayList<String>> m_PTMTooltipsMap;

    private int m_proteinLengh;
    private List<PTMPeptideInstance> m_PTMPeptideInstances;
    private DPeptideInstance[] m_peptideInstances;
    private PTMPeptideInstance m_selectedPeptideInstance;
    private int m_selectedPosition;
    private double m_aaWidth;//width of an amino acid
    private double m_aaWidthOriginal;
    private int m_startPositionProtein = 0;
    private final int m_x0 = 10;
    private final int m_y0 = 10;
    private final int m_height = 25;
    private final int m_ptm_y0 = 15;
    private final int m_ptm_height = m_height / 2;
    private final int m_protein_y0 = m_ptm_y0 + m_ptm_height;
    private final int m_protein_height = m_height - m_ptm_height;
    private final BasicStroke STROKE_PROTEINE_LINE = new BasicStroke(3, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND);
    private final BasicStroke STROKE_NORMAL = new BasicStroke(2, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND);
    private final String TITLE = "Protein Sequence Coverage";
    private final Color SELECTED_COLOR = Color.black;//blue
    private final Color PEPTIDE_COLOR = new Color(0, 200, 0, 100);//green with transparence
    private final Color PTM_PEPTIDE_COLOR = Color.red;
    private int m_oldWidth;

    public PeptideOnProteinOverviewPanel(PTMGraphicCtrlPanel superCtrl) {
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

    public void setSelectedPeptide(PTMPeptideInstance selectedPTMPeptideInstance) {
        m_selectedPeptideInstance = selectedPTMPeptideInstance;
    }

    public int getSelectedProteinPosition() {
        return this.m_selectedPosition;
    }

    /**
     * set position at the begin of a peptide
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

    private int getPosOnProtein(int x) {
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

    public void setData(String proteinName, int proteinLength, PTMPeptideInstance selectedPeptide, List<PTMPeptideInstance> ptmPeptideInstances, DPeptideInstance[] peptideInstances) {
        m_PTMPeptideInstances = ptmPeptideInstances;
        m_peptideInstances = peptideInstances;
        m_proteinLengh = proteinLength;
        m_selectedPeptideInstance = selectedPeptide;
        m_startPositionProtein = 0;

        createPTMPeptideMap(ptmPeptideInstances);
        createAADataMap(peptideInstances);
        //m_proteinLengh may be changed after calculating, so m_aaWidthOriginal is calcul after above create
        m_aaWidthOriginal = ((double) (this.getWidth() - 20) / m_proteinLengh);
        m_aaWidth = m_aaWidthOriginal;
        String title = proteinName + " " + TITLE + " " + m_proteinLengh + " amino acid";
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
        if (m_PTMPeptideInstances != null && !m_PTMPeptideInstances.isEmpty()) {
            paintPTM(g2);
            paintPeptideListOnSequence(g2);
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
            x0 = (int) (m_x0 + m_aaWidth * (ptm.getPositionOnProtein() - m_startPositionProtein));
            y0 = m_ptm_y0;
            color = ViewSetting.getColor(ptm);
            g.setColor(color);
            g.fillRect(x0, y0, (int) ((m_aaWidth) + 1), m_ptm_height);//width minimum = 1
        }
    }

    private void paintPeptideListOnSequence(Graphics2D g) {
        if (m_PTMPeptideInstances.isEmpty()) {
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
        g.setColor(PTM_PEPTIDE_COLOR);
        for (PTMPeptideInstance pep : m_PTMPeptideInstances) {
            paintPTMPeptide(g, pep);
        }
        g.setColor(SELECTED_COLOR);
        if (m_selectedPeptideInstance != null) {
            paintPTMPeptide(g, m_selectedPeptideInstance);
        }
    }

    private void paintPTMPeptide(Graphics2D g, PTMPeptideInstance pep) {
        int start = pep.getStartPosition();
        int stop = pep.getStopPosition();
        int width = (int) ((float) (stop - start + 1) * m_aaWidth);
        int x0 = (int) (m_x0 + (m_aaWidth * (start - m_startPositionProtein)));
        g.drawRoundRect(x0, m_protein_y0, width, m_protein_height, m_protein_height, m_protein_height);
    }

    private void paintPeptide(Graphics2D g, DPeptideInstance pep) {

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
    private String getPTMTooltips(int x, int y) {
        if (m_PTMMap == null) {
            return null;
        }
        int yRangPTM = m_ptm_y0;
        int yRangA = m_protein_y0;
        int yRangZ = yRangA + m_protein_height;
        List<PTMPeptideInstance> peptides;
        AminoAcidPtm ptms;
        int positionOnProtein = getPosOnProtein(x);
        if (y > yRangPTM && y < yRangPTM + m_ptm_height) {
            ptms = this.m_PTMMap.get(positionOnProtein);
            if (ptms == null) {
                return null;
            } else {
                return ptms.getTooltips();
            }
        } else if (y > yRangA && y < yRangZ) {
            peptides = this.m_postionPTMPeptideMap.get(positionOnProtein);
            if (peptides != null) {
                String s = "";
                for (PTMPeptideInstance pep : peptides) {
                    String tips = getDecoratedSequence(pep);
                    s += tips + "\n";
                }
                return s;
            }
        }
        return "position: " + positionOnProtein;
    }

    private String getDecoratedSequence(PTMPeptideInstance pep) {
        String sequence = PeptideRenderer.constructPeptideDisplay(pep.getPeptideInstance().getPeptide())
                .replaceAll(GlobalValues.HTML_TAG_BEGIN, "")
                .replaceAll(GlobalValues.HTML_TAG_END, "");
        String score = DataFormat.format(pep.getBestPepMatch().getScore(), 2);
        String tips = String.format("%d -%s- %d, score: %s", pep.getStartPosition(), sequence, pep.getStopPosition(), score);
//        if (sequence.contains(GlobalValues.HTML_TAG_SPAN_END)) {
//            tips = GlobalValues.HTML_TAG_BEGIN + "<body>" + tips + "</body>" + GlobalValues.HTML_TAG_END;
//        }
        return tips;
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

        AminoAcidPtm ptms;
        int positionOnProtein = getPosOnProtein(x);
        if (y > yRangPTM && y < yRangPTM + m_ptm_height) {
            ptms = this.m_PTMMap.get(positionOnProtein);
            if (ptms == null) {
                return null;
            } else {
                return ptms.getTooltips();
            }
        } else if (y > yRangA && y < yRangZ) {
            List<DPeptideInstance> peptides;
            List<PTMPeptideInstance> ptmPeptides;
            peptides = this.m_postionPeptideMap.get(positionOnProtein);
            ptmPeptides = this.m_postionPTMPeptideMap.get(positionOnProtein);
            List<Long> ptmPepIdList = null;
            String s = null;
            if (ptmPeptides != null) {
                s = "";
                ptmPepIdList = new ArrayList();
                for (PTMPeptideInstance ptmPep : ptmPeptides) {
                    s = s + getDecoratedSequence(ptmPep) + "<br>";
                    ptmPepIdList.add(ptmPep.getPeptideInstance().getId());
                }
            }
            if (peptides != null) {
                s = (s == null) ? "" : s;
                for (DPeptideInstance pep : peptides) {
                    Long id = pep.getId();
                    if (ptmPepIdList != null) {
                        if (ptmPepIdList.contains(id)) {
                            continue;
                        }
                    }
                    String sequence = pep.getBestPeptideMatch().getPeptide().getSequence();//pep.getPeptide() is null, so we get sequence via BestPeptideMatch
                    String score = DataFormat.format(pep.getBestPeptideMatch().getScore(), 2);
                    SequenceMatchPK pepSequencePK = pep.getBestPeptideMatch().getSequenceMatch().getId();

                    String tips = String.format("%d -%s- %d, score: %s", pepSequencePK.getStart(), sequence, pepSequencePK.getStop(), score);
                    s = s + tips + "<br>";
                }
                return GlobalValues.HTML_TAG_BEGIN + "<body>" + s + "</body>" + GlobalValues.HTML_TAG_END;
            }
        }
        return "position: " + positionOnProtein;
    }

    private void createPTMPeptideMap(List<PTMPeptideInstance> peptideInstances) {
        m_postionPTMPeptideMap = new HashMap();
        m_PTMMap = new HashMap<>();
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

    private void createAADataMap(DPeptideInstance[] peptideInstances) {
        //long beginTime = System.currentTimeMillis();
        m_postionPeptideMap = new HashMap();
        //m_PTMMap = new HashMap();
        m_PTMTooltipsMap = new HashMap();
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
                    DInfoPTM ptmTypeInfo = DInfoPTM.getInfoPTMMap().get(ptm.getIdPtmSpecificity());
                    int position = (int) ptm.getSeqPosition() + start - 1;//position convert to int
                    String tooltips = ptmTypeInfo.toReadablePtmString(position);

                    ArrayList<String> tooltipsList = m_PTMTooltipsMap.get(position);
                    if (tooltipsList == null) {
                        tooltipsList = new ArrayList();
                        tooltipsList.add(tooltips);
                        m_PTMTooltipsMap.put(position, tooltipsList);
                    } else {
                        if (!tooltipsList.contains(tooltips)) {
                            tooltipsList.add(tooltips);
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

    private class ProteinMouseAdapter extends MouseAdapter {

        int currentX, currentY, startX, startY;
        ZoomMouseAction _action = null;

        ProteinMouseAdapter() {
            super();
        }

        public void mouseClicked(MouseEvent e) {//for selected on the protein, _action will be transfer to superCtrl
            requestFocusInWindow();
            if (SwingUtilities.isLeftMouseButton(e)) {
                List<PTMPeptideInstance> peptides;
                AminoAcidPtm ptms;
                int x = e.getX();
                int y = e.getY();
                if (y > m_ptm_y0 && y < m_ptm_y0 + m_height) {
                    int positionOnProtein = getPosOnProtein(x);
                    setSelectedOnProtein(positionOnProtein);
                    m_superCtrl.onMessage(PTMGraphicCtrlPanel.Source.SEQUENCE, PTMGraphicCtrlPanel.Message.SELECTED);
                }
            }
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
