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
package fr.proline.studio.rsmexplorer.gui.ptm;

import fr.proline.studio.dam.tasks.data.ptm.PTMCluster;
import fr.proline.studio.dam.tasks.data.ptm.PTMPeptideInstance;
import fr.proline.studio.dam.tasks.data.ptm.PTMSite;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.rsmexplorer.gui.ptm.mark.ProteinSequence;
import fr.proline.studio.rsmexplorer.gui.ptm.mark.PTMMarkCtrl;
import fr.proline.studio.rsmexplorer.gui.ptm.pep.PeptideAreaCtrl;
import fr.proline.studio.utils.CyclicColorPalette;
import fr.proline.studio.utils.GlobalValues;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author VD225637
 */
public class PTMPeptidesGraphicView extends JPanel {

    private static Logger m_logger = LoggerFactory.getLogger("ProlineStudio.rsmexplorer.ptm");
    protected AbstractDataBox m_dataBox;

    private PTMPeptidesGraphicPanel m_internalPanel;
    private PTMPeptidesGraphicModel m_dataModel;
    private PTMGraphicCtrlPanel m_superCtrl;

    public void setSuperCtrl(PTMGraphicCtrlPanel superCtrl) {
        this.m_superCtrl = superCtrl;
    }

    public void setScrollLocation(int ajustedLocation) {
        this.m_internalPanel.setScrollLocation(ajustedLocation);
        repaint();
    }

    public PTMPeptidesGraphicView(boolean isClusterData) {
        super();
        m_superCtrl = null;
        m_internalPanel = new PTMPeptidesGraphicPanel(isClusterData);
        m_dataModel = new PTMPeptidesGraphicModel();
        m_internalPanel.setModel(m_dataModel);
        initComponents();
    }

    /**
     * ToolBar at left, PainArea at Center
     */
    private void initComponents() {
        setLayout(new BorderLayout());

        m_internalPanel.setSize(WIDTH - 30, HEIGHT);
        this.add(m_internalPanel, BorderLayout.CENTER);

    }

    /**
     * useful between table-graphic ptm site panel
     *
     * @param i
     */
    public void setSelectedRow(int i) {
        this.m_internalPanel.setSelectedPeptideIndex(i);
    }

    public void setSelectedPTMPeptide(PTMPeptideInstance pep) {
        int row = this.m_dataModel.getPeptideIndex(pep);
        this.m_internalPanel.setSelectedPeptideIndex(row);
        repaint();
    }

    /**
     * used to set next Data Box
     */
    public PTMPeptideInstance getSelectedPTMPeptideInstance() {
        int selectedRowIndex = this.m_internalPanel.getSelectedPeptideIndex();
        if (m_dataModel.getRowCount() > 0 && selectedRowIndex < m_dataModel.getRowCount()) {
            return this.m_dataModel.getPeptideAt(selectedRowIndex);
        }
        return null;
    }

    public PTMSite getMainPTMSite() {
        return m_dataModel.getMainPTMSite();
    }

    public void setData(List<PTMPeptideInstance> peptidesInstances) {
        m_dataModel.setData(peptidesInstances, m_dataBox.getProjectId());
        if (peptidesInstances == null) {
            this.m_internalPanel.clean();
        } else {
            this.m_internalPanel.updateData();
            m_dataBox.propagateDataChanged(PTMPeptideInstance.class);
        }
        setScrollLocation(this.m_dataModel.getLowerStartInProtSeq());
        this.repaint();
    }

    public void setDataBox(AbstractDataBox dataBox) {
        this.m_dataBox = dataBox;
    }

    public String getSequence() {
        return this.m_dataModel.getProteinSequence();
    }

    private class PTMPeptidesGraphicPanel extends JPanel {

        private static final int INITIAL_WIDTH = 1200;
        private static final int AJUSTE_GAP = 3;

        private PTMPeptidesGraphicModel m_dataModel = null;

        private JScrollPane m_scrollPane;
        private final TitlePane m_titlePane;
        private final PeptidePane m_peptidesPane;
        private final PeptideNumberPane m_peptidesNumberPane;

        private int m_sequenceLength;
        private boolean m_isDataNull;//when precedent databox change order or filter, we can have non selected row, in this case, nothing to show
        PeptideAreaCtrl m_peptideAreaCtrl;

        public PTMPeptidesGraphicPanel(boolean isClusterData) {
            super();
            m_peptideAreaCtrl = new PeptideAreaCtrl();
            PTMMarkCtrl ctrlMark = new PTMMarkCtrl();
            ProteinSequence ctrlSequence = new ProteinSequence();

            m_titlePane = new TitlePane(ctrlMark, ctrlSequence);
            m_peptidesPane = new PeptidePane();
            if (isClusterData) {
                m_peptidesNumberPane = new ColoredClusterMarkPane();
            } else {
                m_peptidesNumberPane = new PeptideNumberPane();
            }
            m_sequenceLength = 0;
            initComponents();
        }

        private void initComponents() {
            setDoubleBuffered(false);

            m_titlePane.setMarkBeginPoint(ViewSetting.BORDER_GAP, ViewSetting.BORDER_GAP);
            m_titlePane.setSequenceBeginPoint(ViewSetting.BORDER_GAP, ViewSetting.HEIGHT_MARK);
            m_peptidesPane.setBeginPoint(ViewSetting.BORDER_GAP, ViewSetting.BORDER_GAP);
            m_peptidesNumberPane.setBeginPoint(ViewSetting.BORDER_GAP, ViewSetting.BORDER_GAP);
            //first setPreferredSize to guarantee the height
            m_titlePane.setPreferredSize(new Dimension(INITIAL_WIDTH, ViewSetting.HEIGHT_MARK + ViewSetting.HEIGHT_SEQUENCE));
            m_titlePane.setBackground(Color.WHITE);
            m_peptidesPane.setBackground(Color.WHITE);
            m_titlePane.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, CyclicColorPalette.GRAY_GRID));
            m_peptidesPane.setBorder(BorderFactory.createMatteBorder(1, 1, 0, 0, CyclicColorPalette.GRAY_GRID));
            m_scrollPane = new JScrollPane(m_peptidesPane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            m_scrollPane.setColumnHeaderView(m_titlePane);
            m_scrollPane.setRowHeaderView(m_peptidesNumberPane);
            m_scrollPane.setBackground(Color.white);
            this.setLayout(new BorderLayout());
            this.add(m_scrollPane, BorderLayout.CENTER);
        }

        public void setModel(PTMPeptidesGraphicModel dataModel) {
            m_dataModel = dataModel;
        }

        /**
         *
         * @param ajustedLocation: position on protein
         */
        private void setScrollLocation(int ajustedLocation) {
            JScrollBar bar = this.m_scrollPane.getHorizontalScrollBar();
            int max = bar.getMaximum();
            int x = (int) ((float) max / m_sequenceLength * (ajustedLocation - AJUSTE_GAP));
            bar.setValue(x);
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

        private void updateData() {
            m_titlePane.updateData();
            m_peptidesPane.updateData();
            m_isDataNull = false;
            m_sequenceLength = m_dataModel.getProteinSequence().length();
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
                m_ctrlSequence.setData(m_dataModel.getProteinSequence());
                if (m_dataModel.getMainPTMSite() != null) {
                    m_ctrlSequence.setPTMSequencePosition(m_dataModel.getMainPTMSite().getPositionOnProtein());
                } else {
//                //Take first PTMSite (??) or all ??? VDS FIXME
//                if(m_dataModel.getPeptideAt(0)!=null && m_dataModel.getPeptideAt(0).getSites().size()>0)
                    m_ctrlSequence.setPTMSequencePosition(-1);
                }
                m_ctrlMark.setData(m_dataModel.getAllPtmMarks());
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
                PeptideAreaMouseAdapter mouseAdapter = new PeptideAreaMouseAdapter();
                this.addMouseListener(mouseAdapter);
                this.addMouseMotionListener(mouseAdapter);
                this.addKeyListener(new PeptideAreaKeyAdapter());
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
                m_peptideAreaCtrl.setPTMPepInstanceData(m_dataModel.getPTMPeptideInstance());
                m_peptideAreaCtrl.setSelectedIndex(0);
            }

            public void setBeginPoint(int x, int y) {
                m_peptideAreaCtrl.setBeginPoint(x, y);
            }

            @Override
            public Dimension getPreferredSize() {
                int width = (int) ((m_sequenceLength + AJUSTE_GAP) * ViewSetting.WIDTH_AA);
                int height = m_dataModel.getRowCount() * (ViewSetting.HEIGHT_AA * 2 - ViewSetting.HEIGHT_AA / 2);
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
        }

        private class PeptideNumberPane extends JPanel {

            int m_x0, m_y0;

            private PeptideNumberPane() {
                super();
                this.setBackground(Color.WHITE);
            }

            public void setBeginPoint(int x, int y) {
                m_x0 = x;
                m_y0 = y;
            }

            @Override
            public Dimension getPreferredSize() {
                int width = ViewSetting.WIDTH_AA + 2 * ViewSetting.BORDER_GAP;
                int height = m_dataModel.getRowCount() * (ViewSetting.HEIGHT_AA * 2 - ViewSetting.HEIGHT_AA / 2);
                if (height == 0) {
                    height = 5 * ViewSetting.HEIGHT_AA;
                }
                return new Dimension(width, height);

            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (!m_isDataNull) {
                    Graphics2D g2 = (Graphics2D) g;
                    FontMetrics f = g2.getFontMetrics(ViewSetting.FONT_NUMBER);
                    int ascend = f.getAscent();
                    int y0 = m_y0 + ascend;
                    int x0;
                    g2.setColor(Color.black);
                    g2.setFont(ViewSetting.FONT_NUMBER);
//                    g2.setColor(Color.WHITE);
//                    g2.setFont(ViewSetting.FONT_PTM);
                    String number;
                    int stringWidth;
                    int rowCount = m_dataModel.getRowCount();
                    for (int i = 1; i < rowCount + 1; i++) {
                        number = String.valueOf(i);
                        stringWidth = f.stringWidth(number);
                        x0 = m_x0 + ViewSetting.WIDTH_AA - stringWidth;
                        g2.drawString("" + i, x0, y0);
                        y0 += (int) ViewSetting.HEIGHT_AA * 1.5;
                    }

                }
            }

        }

        /**
         * call by mouse event adapter ou key event adapter
         *
         * @param selectedIndex
         * @param oldSelected
         */
        private void selectedAction(int selectedIndex, int oldSelected) {

            if (selectedIndex >= 0 && selectedIndex < m_dataModel.getRowCount()) {
                PTMPeptideInstance pep = m_dataModel.getPeptideAt(selectedIndex);
                PTMPeptideInstance oldPep = m_dataModel.getPeptideAt(oldSelected);
                if (pep != null) {
                    if (oldPep != null) {
                        if (Math.abs(oldPep.getStartPosition() - pep.getStartPosition()) > 3) {
                            setScrollLocation(pep.getStartPosition());
                        }
                    } else {
                        setScrollLocation(pep.getStartPosition());
                    }
                }
                m_peptideAreaCtrl.setSelectedIndex(selectedIndex);
                if (selectedIndex != oldSelected && (selectedIndex != -1) && (m_dataBox != null)) {
                    if (m_superCtrl != null) {
                        m_superCtrl.onMessage(PTMGraphicCtrlPanel.Source.PEPTIDE_AREA, PTMGraphicCtrlPanel.Message.SELECTED);
                    }
                    m_dataBox.propagateDataChanged(PTMPeptideInstance.class);
                    repaint();
                }
            }
        }

        private class ColoredClusterMarkPane extends PeptideNumberPane {

            private Map<PTMCluster, Color> PTM_CLUSTER_COLORS = new HashMap<>();
            private Map<PTMCluster, String> PTM_CLUSTER_ID = new HashMap<>();
            int m_x0, m_y0;

            private ColoredClusterMarkPane() {
                super();
                this.setBackground(Color.WHITE);
                ScrollPaneRowHeaderMouseAdapter coloredTooltips = new ScrollPaneRowHeaderMouseAdapter();
                this.addMouseMotionListener(coloredTooltips);
                this.addMouseListener(coloredTooltips);
            }

            class ScrollPaneRowHeaderMouseAdapter extends MouseAdapter {

                @Override
                public void mouseMoved(MouseEvent e) {
                    int x = e.getX();
                    int y = e.getY();
                    String tips = null;
                    int index = (int) ((y - m_y0) / (ViewSetting.HEIGHT_AA * 1.5));
                    if (index >= 0 && index < m_dataModel.getRowCount()) {
                        PTMPeptideInstance pep = m_dataModel.getPeptideAt(index);
                        if (pep != null) {
                            List<PTMCluster> clusters = pep.getClusters();
                            int size = clusters.size();
                            tips = GlobalValues.HTML_TAG_BEGIN + "<body>";
                            for (int j = 0; j < size; j++) {
                                PTMCluster cluster = clusters.get(j);
                                String htmlColor = CyclicColorPalette.getHTMLColoredBlock(getColor(cluster));
                                tips += htmlColor + getColorId(cluster);
                            }
                            tips += "</body>" + GlobalValues.HTML_TAG_END;

                        }
                    }
                    setToolTipText(tips);
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
                    int num = ('A') + PTM_CLUSTER_COLORS.size() - 1;
                    String s = String.valueOf((char) num);
                    PTM_CLUSTER_ID.put(cluster, s);// give it a color id, A,B, C...

                }
                return c;
            }

            public String getColorId(PTMCluster cluster) {
                return PTM_CLUSTER_ID.get(cluster);

            }

            public void setBeginPoint(int x, int y) {
                m_x0 = x;
                m_y0 = y;
            }

            @Override
            public Dimension getPreferredSize() {
                int width = ViewSetting.WIDTH_AA + 2 * ViewSetting.BORDER_GAP;
                int height = m_dataModel.getRowCount() * (ViewSetting.HEIGHT_AA * 2 - ViewSetting.HEIGHT_AA / 2);
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
                    int rowCount = m_dataModel.getRowCount();
                    for (int i = 1; i < rowCount + 1; i++) {
                        PTMPeptideInstance item = m_dataModel.getPeptideAt(i - 1);
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
                            colorId = getColorId(clusters.get(0));//only the first
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

}
