package fr.proline.studio.rsmexplorer.gui.ptm;

import fr.proline.studio.dam.tasks.data.ptm.PTMCluster;
import fr.proline.studio.dam.tasks.data.ptm.PTMPeptideInstance;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.rsmexplorer.gui.ptm.mark.PTMMarkCtrl;
import fr.proline.studio.rsmexplorer.gui.ptm.mark.ProteinSequence;
import fr.proline.studio.rsmexplorer.gui.ptm.pep.PeptideAreaCtrl;
import fr.proline.studio.utils.CyclicColorPalette;
import fr.proline.studio.utils.GlobalValues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class PTMPeptidesGraphicPanel extends JPanel {

    private final Logger m_logger = LoggerFactory.getLogger(PTMPeptidesGraphicPanel.class);
    private AbstractDataBox m_dataBox;
    private PTMGraphicCtrlPanel m_superCtrl;

    private static final int INITIAL_WIDTH = 1200;
    private static final int AJUSTE_GAP = 3;

    private PTMPeptidesGraphicModel m_ptmPepDataModel = null;

    private JScrollPane m_scrollPane;
    private final TitlePane m_titlePane;
    private final PeptidePane m_peptidesPane;
    private final ColoredClusterMarkPane m_clusterMarkPane;

    private int m_sequenceLength;
    private boolean m_isDataNull;//when precedent databox change order or filter, we can have non selected row, in this case, nothing to show
    PeptideAreaCtrl m_peptideAreaCtrl;

    public PTMPeptidesGraphicPanel( ) {
        super();
        m_peptideAreaCtrl = new PeptideAreaCtrl();
        PTMMarkCtrl ctrlMark = new PTMMarkCtrl();
        ProteinSequence ctrlSequence = new ProteinSequence();

        m_titlePane = new TitlePane(ctrlMark, ctrlSequence);
        m_peptidesPane = new PeptidePane();
        m_clusterMarkPane = new ColoredClusterMarkPane();

        m_sequenceLength = 0;
        initComponents();
    }

    public void setDataBox(AbstractDataBox dataBox) {
        this.m_dataBox = dataBox;
    }

    protected PTMPeptidesGraphicModel getPTMPeptidesModel() {
        return m_ptmPepDataModel;
    }

    private void initComponents() {
        setDoubleBuffered(false);

        m_titlePane.setMarkBeginPoint(ViewSetting.BORDER_GAP, ViewSetting.BORDER_GAP);
        m_titlePane.setSequenceBeginPoint(ViewSetting.BORDER_GAP, ViewSetting.HEIGHT_MARK);
        m_peptidesPane.setBeginPoint(ViewSetting.BORDER_GAP, ViewSetting.BORDER_GAP);
        m_clusterMarkPane.setBeginPoint(ViewSetting.BORDER_GAP, ViewSetting.BORDER_GAP);

        //first setPreferredSize to guarantee the height
        m_titlePane.setPreferredSize(new Dimension(INITIAL_WIDTH, ViewSetting.HEIGHT_MARK + ViewSetting.HEIGHT_SEQUENCE));
        m_titlePane.setBackground(Color.WHITE);
        m_titlePane.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, CyclicColorPalette.GRAY_GRID));
        m_peptidesPane.setBorder(BorderFactory.createMatteBorder(1, 1, 0, 0, CyclicColorPalette.GRAY_GRID));
        m_peptidesPane.setBackground(Color.WHITE);

        m_scrollPane = new JScrollPane(m_peptidesPane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        m_scrollPane.setColumnHeaderView(m_titlePane);
        m_scrollPane.setRowHeaderView(m_clusterMarkPane);
        m_scrollPane.setBackground(Color.white);
        this.setLayout(new BorderLayout());
        this.add(m_scrollPane, BorderLayout.CENTER);
    }

    public void setModel(PTMPeptidesGraphicModel dataModel) {
        m_ptmPepDataModel = dataModel;
    }

    /**
     * @param ajustedLocation: position on protein
     */
    protected void setHorizonScrollLocation(int ajustedLocation) {
        JScrollBar bar = this.m_scrollPane.getHorizontalScrollBar();
        int max = bar.getMaximum();
        int visibleAmount = bar.getVisibleAmount();
        int prevAdjLoc= ajustedLocation;
        ajustedLocation -= (int) Math.round((m_sequenceLength * ((double) (visibleAmount / 2)) / max));

        m_logger.debug("setHorizonScrollLocation. m_sequenceLength"+m_sequenceLength+" passed ajustedLocation "+prevAdjLoc+" max "+max+" visiblaAmout "+visibleAmount+" new ajustedLocation "+ajustedLocation);

        int x = (int) ((float) max / m_sequenceLength * (ajustedLocation - AJUSTE_GAP));
        m_logger.debug("setHorizonScrollLocation x "+x);
        bar.setValue(x);
        setViewPosition(ajustedLocation, getSelectedPeptideIndex());
    }

    private void setViewPosition(int ajustedLocation, int peptideIndex) {
        JViewport jvp = this.m_scrollPane.getViewport();
        Rectangle visibleRec = jvp.getVisibleRect();
        Point vp = jvp.getViewPosition();
        int x = (ajustedLocation) * ViewSetting.WIDTH_AA;
        int y = peptideIndex * (ViewSetting.HEIGHT_AA * 2 - ViewSetting.HEIGHT_AA / 2);
        Point p = new Point(x, y);
        if (Math.abs(vp.x - p.x) > visibleRec.width || Math.abs(vp.y - p.y) > visibleRec.height) {
            jvp.setViewPosition(p);
        }

    }

    /**
     * useful for change scroll bar state
     *
     * @param g
     */
    @Override
    public void paint(Graphics g) {
        this.m_scrollPane.getViewport().revalidate();//
        super.paint(g);
    }

    protected int getSelectedPeptideIndex() {
        return m_peptideAreaCtrl.getSelectedIndex();
    }

    protected void clean() {
        this.m_isDataNull = true;
    }

    protected void setSelectedPeptideIndex(int i) {
        m_peptideAreaCtrl.setSelectedIndex(i);
    }

    protected void updateData() {
        m_titlePane.updateData();
        m_peptidesPane.updateData();
        m_isDataNull = false;
        m_sequenceLength = m_ptmPepDataModel.getProteinSequence().length();
    }

    protected void setSuperCtrl(PTMGraphicCtrlPanel superCtrl) {
        m_superCtrl = superCtrl;
    }

    private class TitlePane extends JPanel {

        private PTMMarkCtrl m_ctrlMark;
        private ProteinSequence m_ctrlSequence;

        private TitlePane(PTMMarkCtrl ctrlMark, ProteinSequence ctrlSequence) {
            super();
            m_ctrlMark = ctrlMark;
            m_ctrlSequence = ctrlSequence;
            this.addMouseMotionListener(new MouseAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {//for tooltips
                    int x = e.getX();
                    int y = e.getY();
                    String tips = m_ctrlMark.getToolTipText(x, y);
                    setToolTipText(tips);//null will turn off ToolTip
                }
            });
        }

        public void updateData() {
            m_ctrlSequence.setData(m_ptmPepDataModel.getProteinSequence());
            m_ctrlMark.setData(m_ptmPepDataModel.getAllPtmMarks());
        }

        public void setMarkBeginPoint(int x, int y) {
            m_ctrlMark.setBeginPoint(x, y);
        }

        public void setSequenceBeginPoint(int x, int y) {
            m_ctrlSequence.setBeginPoint(x, y);
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension((int) ((m_sequenceLength + AJUSTE_GAP) * ViewSetting.WIDTH_AA), ViewSetting.HEIGHT_MARK + ViewSetting.HEIGHT_SEQUENCE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            this.isPaintingOrigin();
            super.paintComponent(g);
            if (!m_isDataNull) {
                ViewContext viewContext = new ViewContext();

                viewContext.setShowNCtermIndex(false);
                Graphics2D g2 = (Graphics2D) g;
                m_ctrlMark.paint(g2, viewContext);
                m_ctrlSequence.paint(g2, viewContext);
            }
        }

    }

    private class PeptidePane extends JPanel {

        private PeptidePane() {
            super();
            PeptidePane.PeptideAreaMouseAdapter mouseAdapter = new PeptidePane.PeptideAreaMouseAdapter();
            this.addMouseListener(mouseAdapter);
            this.addMouseMotionListener(mouseAdapter);
            this.addKeyListener(new PeptidePane.PeptideAreaKeyAdapter());
        }

        class PeptideAreaKeyAdapter extends KeyAdapter {

            public PeptideAreaKeyAdapter() {
                super();
            }

            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                int oldSelected = m_peptideAreaCtrl.getSelectedIndex();
                if (evt.getKeyCode() == KeyEvent.VK_UP) {
                    m_peptideAreaCtrl.setRelativeSelected(-1);
                } else if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
                    m_peptideAreaCtrl.setRelativeSelected(1);
                }
                int selectedIndex = m_peptideAreaCtrl.getSelectedIndex();
                selectedAction(selectedIndex, oldSelected);
            }
        }

        class PeptideAreaMouseAdapter extends MouseAdapter {

            @Override
            public void mouseClicked(MouseEvent e) {
                requestFocusInWindow();
                //PtmSitePeptide selectedItem = _ctrlPeptideArea.getSelectedItem(e.getX(), e.getY());
                int oldSelected = m_peptideAreaCtrl.getSelectedIndex();
                int selectedIndex = m_peptideAreaCtrl.getSelectedIndex(e.getX(), e.getY());
                selectedAction(selectedIndex, oldSelected);
            }

            @Override
            public void mouseMoved(MouseEvent e) {//for tooltips
                int x = e.getX();
                int y = e.getY();
                String tips = m_peptideAreaCtrl.getToolTipText(x, y);
                //if (tips != null) {
                setToolTipText(tips);//null will turn off tooltip

            }
        }

        public void updateData() {
            m_peptideAreaCtrl.setPTMPepInstanceData(m_ptmPepDataModel.getPTMPeptideInstance());
            m_peptideAreaCtrl.setSelectedIndex(0);
        }

        public void setBeginPoint(int x, int y) {
            m_peptideAreaCtrl.setBeginPoint(x, y);
        }

        @Override
        public Dimension getPreferredSize() {
            int width = (int) ((m_sequenceLength + AJUSTE_GAP) * ViewSetting.WIDTH_AA);
            int height = m_ptmPepDataModel.getRowCount() * (ViewSetting.HEIGHT_AA * 2 - ViewSetting.HEIGHT_AA / 2);
            if (height == 0) {
                height = 5 * ViewSetting.HEIGHT_AA;
            }
            //logger.debug("PanelPtmDraw PeptidePane getPreferredSize(" + width + "," + height + ")");
            return new Dimension(width, height); //must be the all paint dimension in order to be visible in scroll panel

        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (!m_isDataNull) {
                ViewContext viewContext = new ViewContext();
                //viewContext.setAjustedStartLocation(m_ajustedStartLocation).setAreaWidth(this.getWidth());
                viewContext.setAreaWidth(this.getWidth());
                Graphics2D g2 = (Graphics2D) g;
                m_peptideAreaCtrl.paint(g2, viewContext);
            }
        }
    }//End PeptidePane


    /**
     * call by mouse event adapter ou key event adapter
     *
     * @param selectedIndex
     * @param oldSelected
     */
    private void selectedAction(int selectedIndex, int oldSelected) {
        if (selectedIndex >= 0 && selectedIndex < m_ptmPepDataModel.getRowCount()) {
            PTMPeptideInstance pep = m_ptmPepDataModel.getPeptideAt(selectedIndex);
            PTMPeptideInstance oldPep = m_ptmPepDataModel.getPeptideAt(oldSelected);
            if (pep != null) {
                if (oldPep != null) {
                    if (Math.abs(oldPep.getStartPosition() - pep.getStartPosition()) > 3) {
                        setHorizonScrollLocation(pep.getStartPosition());
                    }
                } else {
                    setHorizonScrollLocation(pep.getStartPosition());
                }
            }
            m_peptideAreaCtrl.setSelectedIndex(selectedIndex);
            if (selectedIndex != oldSelected && (selectedIndex != -1) && (m_dataBox != null)) {
                if (m_superCtrl != null) {
                    m_superCtrl.onMessage(PTMGraphicCtrlPanel.Source.PEPTIDE_AREA, PTMGraphicCtrlPanel.Message.SELECTED);
                }
                m_dataBox.addDataChanged(PTMPeptideInstance.class, null);  //JPM.DATABOX : put null, because I don't know which subtype has been change : null means all. So it works as previously
                m_dataBox.propagateDataChanged();
                repaint();
            }
        }
    }

    private class ColoredClusterMarkPane extends JPanel {

        private Map<PTMCluster, Color> PTM_CLUSTER_COLORS = new HashMap<>();
        private Map<PTMCluster, String> PTM_CLUSTER_ID = new HashMap<>();
        int m_x0, m_y0;

        private ColoredClusterMarkPane() {
            super();
            this.setBackground(Color.WHITE);
            ColoredClusterMarkPane.ScrollPaneRowHeaderMouseAdapter coloredTooltips = new ColoredClusterMarkPane.ScrollPaneRowHeaderMouseAdapter();
            this.addMouseMotionListener(coloredTooltips);
            this.addMouseListener(coloredTooltips);
        }

        class ScrollPaneRowHeaderMouseAdapter extends MouseAdapter {

            @Override
            public void mouseMoved(MouseEvent e) {
                int x = e.getX();
                int y = e.getY();
                StringBuilder tips = new StringBuilder();
                int index = (int) ((y - m_y0) / (ViewSetting.HEIGHT_AA * 1.5));
                if (index >= 0 && index < m_ptmPepDataModel.getRowCount()) {
                    PTMPeptideInstance pep = m_ptmPepDataModel.getPeptideAt(index);
                    if (pep != null) {
                        java.util.List<PTMCluster> clusters = pep.getClusters();
                        int size = clusters.size();
                        tips.append(GlobalValues.HTML_TAG_BEGIN).append("<body>Clusters: ");
                        for (int j = 0; j < size; j++) {
                            PTMCluster cluster = clusters.get(j);
                            String htmlColor = CyclicColorPalette.getHTMLColoredBlock(getColor(cluster));
                            tips.append(htmlColor).append(PTM_CLUSTER_ID.get(cluster)).append(' ');
                        }
                        tips.append("</body>").append(GlobalValues.HTML_TAG_END);
                    }
                }
                setToolTipText(tips.toString());
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                int x = e.getX();
                int y = e.getY();
                int oldSelected = m_peptideAreaCtrl.getSelectedIndex();
                int selectedIndex = (int) ((y - m_y0) / (ViewSetting.HEIGHT_AA * 1.5));
                selectedAction(selectedIndex, oldSelected);
            }
        }

        public Color getColor(PTMCluster cluster) {
            Color c = PTM_CLUSTER_COLORS.get(cluster);
            if (c == null) {
                Color co = CyclicColorPalette.getColorBlue(PTM_CLUSTER_COLORS.size());
                int rgb = co.getRGB() | 0xFF000000;
                c = new Color(rgb & 0x00FFFFFF);
                PTM_CLUSTER_COLORS.put(cluster, c);
                //int num = ('A') + PTM_CLUSTER_COLORS.size() - 1;
                //String s = String.valueOf((char) num);
                String s = String.valueOf(cluster.getId());
                PTM_CLUSTER_ID.put(cluster, s);// give it a color id, A,B, C...

            }
            return c;
        }

        public void setBeginPoint(int x, int y) {
            m_x0 = x;
            m_y0 = y;
        }

        @Override
        public Dimension getPreferredSize() {
            int width = ViewSetting.WIDTH_AA + 2 * ViewSetting.BORDER_GAP;
            int height = m_ptmPepDataModel.getRowCount() * (ViewSetting.HEIGHT_AA * 2 - ViewSetting.HEIGHT_AA / 2);
            if (height == 0) {
                height = 5 * ViewSetting.HEIGHT_AA;
            }
            return new Dimension(width, height);

        }

        @Override
        protected void paintComponent(Graphics g) {
            //super.paintComponent(g);
            if (!m_isDataNull) {
                PTM_CLUSTER_COLORS = new HashMap<>();
                PTM_CLUSTER_ID = new HashMap<>();
                Graphics2D g2 = (Graphics2D) g;
                FontMetrics f = g2.getFontMetrics(ViewSetting.FONT_NUMBER);
                int ascend = f.getAscent();
                int y0 = m_y0;
                int x0;
                g2.setFont(ViewSetting.FONT_NUMBER);
                String colorId;
                int stringWidth;
                int rowCount = m_ptmPepDataModel.getRowCount();
                for (int i = 1; i < rowCount + 1; i++) {
                    PTMPeptideInstance item = m_ptmPepDataModel.getPeptideAt(i - 1);
                    List<PTMCluster> clusters = item.getClusters();
                    {
                        int size = clusters.size();
                        int xi0 = m_x0;
                        int colorWidth = ViewSetting.WIDTH_AA / size;
                        for (int j = 0; j < size; j++) {
                            int xi = xi0 + colorWidth * j;
                            xi0 = xi;
                            PTMCluster cluster = clusters.get(j);
                            g2.setColor(getColor(cluster));
                            g2.fillRect(xi0, y0, colorWidth, ViewSetting.WIDTH_AA);
                        }
                        colorId = PTM_CLUSTER_ID.get(clusters.get(0));//only the first
                    }

                    stringWidth = f.stringWidth(colorId);
                    x0 = m_x0 + (ViewSetting.WIDTH_AA - stringWidth) / 2;
                    g2.setColor(Color.WHITE);
                    g2.drawString(colorId, x0, y0 + ascend);
                    y0 += (int) ViewSetting.HEIGHT_AA * 1.5;
                }

            }
        }

    }

}
