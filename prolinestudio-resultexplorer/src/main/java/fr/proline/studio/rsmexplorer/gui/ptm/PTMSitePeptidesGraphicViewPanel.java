/*
 * @cea 
 * http://www.profiproteomics.fr
 * create date: 4 déc. 2018
 */
package fr.proline.studio.rsmexplorer.gui.ptm;

import fr.proline.studio.rsmexplorer.gui.ptm.mark.PTMMarkCtrl;
import fr.proline.studio.rsmexplorer.gui.ptm.pep.PeptideAreaCtrl;
import fr.proline.studio.rsmexplorer.gui.ptm.mark.ProteinSequenceCtrl;
import fr.proline.studio.utils.CyclicColorPalette;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Karine XUE
 */
public class PTMSitePeptidesGraphicViewPanel extends JPanel {

    private static Logger m_logger = LoggerFactory.getLogger("ProlineStudio.rsmexplorer.ptm");
    private static final int AJUSTE_GAP = 2;//one space before, one after
    private boolean m_isDataLoaded;
    private TitlePane m_titlePane;
    private PeptidePane m_peptidePane;
    int m_ajustedLocation;
    private int m_rowCount;
    private int m_sequenceLength;
    private JScrollPane m_scrollPane;
    private PTMSitePeptidesGraphicCtrlPanel m_ptmSitePanel;
    private boolean m_isDataNull;//when precedent databox change order ou filter, we can have non selected row, in this case, nothing to show
    private final PeptideNumberPane m_numberPane;

    protected PTMSitePeptidesGraphicViewPanel(PTMSitePeptidesGraphicCtrlPanel ctrl, PTMMarkCtrl ctrlMark, ProteinSequenceCtrl ctrlSequence, PeptideAreaCtrl ctrlPeptideArea) {
        m_ptmSitePanel = ctrl;
        m_titlePane = new TitlePane(ctrlMark, ctrlSequence);
        m_peptidePane = new PeptidePane(ctrlPeptideArea);
        m_numberPane = new PeptideNumberPane();
        m_rowCount = 0;
        m_sequenceLength = 0;
        initComponents();

    }

    private void initComponents() {
        setDoubleBuffered(false);

        m_titlePane.setMarkBeginPoint(ViewSetting.BORDER_GAP, ViewSetting.BORDER_GAP);
        m_titlePane.setSequenceBeginPoint(ViewSetting.BORDER_GAP, ViewSetting.HEIGHT_MARK);
        m_peptidePane.setBeginPoint(ViewSetting.BORDER_GAP, ViewSetting.BORDER_GAP);
        m_numberPane.setBeginPoint(ViewSetting.BORDER_GAP, ViewSetting.BORDER_GAP);
        //first setPreferredSize to guarantee the height
        m_titlePane.setPreferredSize(new Dimension(this.getWidth(), ViewSetting.HEIGHT_MARK + ViewSetting.HEIGHT_SEQUENCE));
        m_titlePane.setBackground(Color.WHITE);
        m_peptidePane.setBackground(Color.WHITE);
        m_titlePane.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, CyclicColorPalette.GRAY_GRID));
        m_peptidePane.setBorder(BorderFactory.createMatteBorder(1, 1, 0, 0, CyclicColorPalette.GRAY_GRID));
        m_scrollPane = new JScrollPane(m_peptidePane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        m_scrollPane.setColumnHeaderView(m_titlePane);
        m_scrollPane.setRowHeaderView(m_numberPane);
        m_scrollPane.setBackground(Color.white);
        this.setLayout(new BorderLayout());
        this.add(m_scrollPane, BorderLayout.CENTER);
    }

    public void setIsDataLoaded(boolean isDataLoaded) {
        if (isDataLoaded == true) {
            this.m_isDataNull = false;
        }
        this.m_isDataLoaded = isDataLoaded;
    }

//    public void setAjustedLocation(int ajustedLocation) {
//        this.m_ajustedLocation = ajustedLocation;
//        if (ajustedLocation >= AJUSTE_GAP) {
//            m_ajustedLocation -= AJUSTE_GAP;
//        }
//    }
    public void setScrollLocation(int ajustedLocation) {
        JScrollBar bar = this.m_scrollPane.getHorizontalScrollBar();
        int max = bar.getMaximum();
        float unit = (float) (max) / m_sequenceLength;
        m_logger.debug("SSSSSSSSSSSSSSSSSS setScrollLocation, getValue={}, unit={},max={} ", bar.getValue(), unit, max);
        int x = (int) (unit * (ajustedLocation + AJUSTE_GAP));//relative in the scroll bar
        m_logger.debug("SSSSSSSSSSSSSSSSSS setScrollLocation, adjusted locaiton ={}, value to set ={} , sequence Length ={}", ajustedLocation + AJUSTE_GAP, x, m_sequenceLength);
        bar.setValue(x);
        m_logger.debug("SSSSSSSSSSSSSSSSSS setScrollLocation, getValue 2={} ", bar.getValue());

    }

    void setRowCount(int rowCount) {
        this.m_rowCount = rowCount;
    }

    public void setSequenceLength(int sequenceLength) {
        this.m_sequenceLength = sequenceLength;
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
        return this.m_peptidePane.m_ctrlPeptideArea.getSelectedIndex();
    }

    protected void clean() {
        this.m_isDataNull = true;
    }

    protected void setSelectedPeptideIndex(int i) {
        this.m_peptidePane.m_ctrlPeptideArea.setSelectedIndex(i);
    }

    private class TitlePane extends JPanel {

        private PTMMarkCtrl m_ctrlMark;
        private final ProteinSequenceCtrl m_ctrlSequence;

        private TitlePane(PTMMarkCtrl ctrlMark, ProteinSequenceCtrl ctrlSequence) {
            super();
            m_ctrlMark = ctrlMark;
            m_ctrlSequence = ctrlSequence;
            this.addMouseMotionListener(new MouseMotionListener() {

                @Override
                public void mouseMoved(MouseEvent e) {//for tooltips
                    int x = e.getX();
                    int y = e.getY();
                    String tips = m_ctrlMark.getToolTipText(x, y, m_ajustedLocation);
                    setToolTipText(tips);//null will turn off ToolTip

                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    // TODO Auto-generated method stub

                }
            });
        }

        public void setMarkBeginPoint(int x, int y) {
            m_ctrlMark.setBeginPoint(x, y);
        }

        public void setSequenceBeginPoint(int x, int y) {
            m_ctrlSequence.setBeginPoint(x, y);
        }

        @Override
        public Dimension getPreferredSize() {
            //return new Dimension((int) ((m_sequenceLength + AJUSTE_GAP - m_ajustedLocation) * ViewSetting.WIDTH_AA), ViewSetting.HEIGHT_MARK + ViewSetting.HEIGHT_SEQUENCE);
            return new Dimension((int) ((m_sequenceLength + AJUSTE_GAP) * ViewSetting.WIDTH_AA), ViewSetting.HEIGHT_MARK + ViewSetting.HEIGHT_SEQUENCE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            this.isPaintingOrigin();
            super.paintComponent(g);
            if (!m_isDataNull) {
                ViewContext viewContext = new ViewContext();
                // viewContext.setAjustedStartLocation(m_ajustedLocation);
                if (m_isDataLoaded) {
                    Graphics2D g2 = (Graphics2D) g;
                    m_ctrlMark.paint(g2, viewContext);
                    m_ctrlSequence.paint(g2, viewContext);
                }
            }
        }

    }

    private class PeptidePane extends JPanel {

        private PeptideAreaCtrl m_ctrlPeptideArea;

        private PeptidePane(PeptideAreaCtrl ctrlPeptideArea) {
            super();
            m_ctrlPeptideArea = ctrlPeptideArea;
            this.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    requestFocusInWindow();
                    //PtmSitePeptide selectedItem = _ctrlPeptideArea.getSelectedItem(e.getX(), e.getY());
                    int oldSelected = m_ctrlPeptideArea.getSelectedIndex();
                    int selectedIndex = m_ctrlPeptideArea.getSelectedIndex(e.getX(), e.getY());
                    if (selectedIndex != oldSelected && (selectedIndex != -1) && (m_ptmSitePanel != null)) {
                        repaint();
                        m_ptmSitePanel.valueChanged();//propagate
                    }
                }
            });
            this.addMouseMotionListener(new MouseMotionListener() {

                @Override
                public void mouseMoved(MouseEvent e) {//for tooltips
                    int x = e.getX();
                    int y = e.getY();
                    String tips = m_ctrlPeptideArea.getToolTipText(x, y);
                    //if (tips != null) {
                    setToolTipText(tips);//null will turn off tooltip

                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    // TODO Auto-generated method stub

                }
            });
            this.addKeyListener(
                    new java.awt.event.KeyListener() {
                public void keyPressed(java.awt.event.KeyEvent evt) {
                    int oldSelected = m_ctrlPeptideArea.getSelectedIndex();
                    if (evt.getKeyCode() == KeyEvent.VK_UP) {
                        m_ctrlPeptideArea.setRelativeSelected(-1);
                    } else if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
                        m_ctrlPeptideArea.setRelativeSelected(1);
                    }
                    int selectedIndex = m_ctrlPeptideArea.getSelectedIndex();
                    if (oldSelected != selectedIndex) {
                        m_ptmSitePanel.valueChanged();
                        repaint();
                    }
                }

                @Override
                public void keyTyped(KeyEvent e) {
                    // TODO Auto-generated method stub
                }

                @Override
                public void keyReleased(KeyEvent e) {
                    // TODO Auto-generated method stub
                }
            });
        }

        public void setBeginPoint(int x, int y) {
            m_ctrlPeptideArea.setBeginPoint(x, y);
        }

        @Override
        public Dimension getPreferredSize() {
            // int width = (int) ((m_sequenceLength + AJUSTE_GAP - m_ajustedLocation) * ViewSetting.WIDTH_AA);
            int width = (int) ((m_sequenceLength + AJUSTE_GAP) * ViewSetting.WIDTH_AA);
            int height = m_rowCount * (ViewSetting.HEIGHT_AA * 2 - ViewSetting.HEIGHT_AA / 2);
            if (height == 0) {
                height = 5 * ViewSetting.HEIGHT_AA;
            }
            //logger.debug("PTMSitePeptidesGraphicViewPanel PeptidePane getPreferredSize(" + width + "," + height + ")");
            return new Dimension(width, height); //must be the all paint dimension in order to be visible in scroll panel

        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (!m_isDataNull) {
                ViewContext viewContext = new ViewContext();
                //viewContext.setAjustedStartLocation(m_ajustedLocation).setAreaWidth(this.getWidth());
                viewContext.setAreaWidth(this.getWidth());
                if (m_isDataLoaded) {
                    Graphics2D g2 = (Graphics2D) g;
                    m_ctrlPeptideArea.paint(g2, viewContext);
                }
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
            int height = m_rowCount * (ViewSetting.HEIGHT_AA * 2 - ViewSetting.HEIGHT_AA / 2);
            if (height == 0) {
                height = 5 * ViewSetting.HEIGHT_AA;
            }
            return new Dimension(width, height);

        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (!m_isDataNull) {
                if (m_isDataLoaded) {
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
                    for (int i = 1; i < m_rowCount + 1; i++) {
                        number = String.valueOf(i);
                        stringWidth = f.stringWidth(number);
                        x0 = m_x0 + ViewSetting.WIDTH_AA - stringWidth;
                        g2.drawString("" + i, x0, y0);
                        y0 += (int) ViewSetting.HEIGHT_AA * 1.5;
                    }
                }
            }
        }

    }

}
