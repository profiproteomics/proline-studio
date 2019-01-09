/*
 * @cea 
 * http://www.profiproteomics.fr
 * create date: 4 déc. 2018
 */
package fr.proline.studio.rsmexplorer.gui.ptm;

import fr.proline.studio.rsmexplorer.gui.ptm.mark.PtmMarkCtrl;
import fr.proline.studio.rsmexplorer.gui.ptm.pep.PeptideAreaCtrl;
import fr.proline.studio.rsmexplorer.gui.ptm.mark.ProteinSequenceCtrl;
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
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Karine XUE
 */
public class PanelPtmDraw extends JPanel {

    private static Logger logger = LoggerFactory.getLogger("ProlineStudio.rsmexplorer.ptm");
    private static final int INITIAL_WIDTH = 1200;
    private static final int INITIAL_HEIGHT = 250;
    private static final int AJUSTE_GAP = 3;
    private boolean _isDataLoaded;
    private TitlePane _titlePane;
    private PeptidePane _peptidePane;
    int _ajustedLocation;
    private int _rowCount;
    private int _sequenceLength;
    private JScrollPane _scrollPane;
    private PanelPeptidesPTMSiteGraphic _ctrl;
    private boolean _isDataNull;//when precedent databox change order ou filter, we can have non selected row, in this case, nothing to show
    private final PeptideNumberPane _numberPane;

    protected PanelPtmDraw(PanelPeptidesPTMSiteGraphic ctrl, PtmMarkCtrl ctrlMark, ProteinSequenceCtrl ctrlSequence, PeptideAreaCtrl ctrlPeptideArea) {
        _ctrl = ctrl;
        _titlePane = new TitlePane(ctrlMark, ctrlSequence);
        _peptidePane = new PeptidePane(ctrlPeptideArea);
        _numberPane = new PeptideNumberPane();
        _rowCount = 0;
        _sequenceLength = 0;
        initComponents();

    }

    private void initComponents() {
        setDoubleBuffered(false);

        _titlePane.setMarkBeginPoint(ViewSetting.BORDER_GAP, ViewSetting.BORDER_GAP);
        _titlePane.setSequenceBeginPoint(ViewSetting.BORDER_GAP, ViewSetting.HEIGHT_MARK);
        _peptidePane.setBeginPoint(ViewSetting.BORDER_GAP, ViewSetting.BORDER_GAP);
        _numberPane.setBeginPoint(ViewSetting.BORDER_GAP, ViewSetting.BORDER_GAP);
        //first setPreferredSize to guarantee the height
        _titlePane.setPreferredSize(new Dimension(INITIAL_WIDTH, ViewSetting.HEIGHT_MARK + ViewSetting.HEIGHT_SEQUENCE));
        _titlePane.setBackground(Color.WHITE);
        _peptidePane.setBackground(Color.WHITE);

        _scrollPane = new JScrollPane(_peptidePane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        _scrollPane.setColumnHeaderView(_titlePane);
        _scrollPane.setRowHeaderView(_numberPane);
        _scrollPane.setBackground(Color.white);
        this.setLayout(new BorderLayout());
        this.add(_scrollPane, BorderLayout.CENTER);
    }

    public void setIsDataLoaded(boolean isDataLoaded) {
        if (isDataLoaded == true) {
            this._isDataNull = false;
        }
        this._isDataLoaded = isDataLoaded;
    }

    public void setAjustedLocation(int ajustedLocation) {
        this._ajustedLocation = ajustedLocation;
        if (ajustedLocation >= AJUSTE_GAP) {
            _ajustedLocation -= AJUSTE_GAP;
        }
    }

    void setRowCount(int rowCount) {
        this._rowCount = rowCount;
    }

    public void setSequenceLength(int sequenceLength) {
        this._sequenceLength = sequenceLength;
    }

    /**
     * useful for change scroll bar state
     *
     * @param g
     */
    @Override
    public void paint(Graphics g) {
        this._scrollPane.getViewport().revalidate();//
        super.paint(g);
    }

    protected int getSelectedPeptideIndex() {
        return this._peptidePane.m_ctrlPeptideArea.getSelectedIndex();
    }

    protected void clean() {
        this._isDataNull = true;
    }

    protected void setSelectedPeptideIndex(int i) {
        this._peptidePane.m_ctrlPeptideArea.setSelectedIndex(i);
    }

    private class TitlePane extends JPanel {

        private PtmMarkCtrl m_ctrlMark;
        private ProteinSequenceCtrl m_ctrlSequence;

        private TitlePane(PtmMarkCtrl ctrlMark, ProteinSequenceCtrl ctrlSequence) {
            super();
            m_ctrlMark = ctrlMark;
            m_ctrlSequence = ctrlSequence;
            this.addMouseMotionListener(new MouseMotionListener() {

                @Override
                public void mouseMoved(MouseEvent e) {//for tooltips
                    int x = e.getX();
                    int y = e.getY();
                    String tips = m_ctrlMark.getToolTipText(x, y, _ajustedLocation);
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
            return new Dimension((int) ((_sequenceLength + AJUSTE_GAP - _ajustedLocation) * ViewSetting.WIDTH_AA), ViewSetting.HEIGHT_MARK + ViewSetting.HEIGHT_SEQUENCE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            this.isPaintingOrigin();
            super.paintComponent(g);
            if (!_isDataNull) {
                ViewContext viewContext = new ViewContext();
                viewContext.setAjustedLocation(_ajustedLocation);
                if (_isDataLoaded) {
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
                    if (selectedIndex != oldSelected && (selectedIndex != -1) && (_ctrl != null)) {
                        repaint();
                        _ctrl.valueChanged(selectedIndex);//propagate
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
                        _ctrl.valueChanged(selectedIndex);
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
            int width = (int) ((_sequenceLength + AJUSTE_GAP - _ajustedLocation) * ViewSetting.WIDTH_AA);
            int height = _rowCount * (ViewSetting.HEIGHT_AA * 2 - ViewSetting.HEIGHT_AA / 2);
            if (height == 0) {
                height = 5 * ViewSetting.HEIGHT_AA;
            }
            //logger.debug("PanelPtmDraw PeptidePane getPreferredSize(" + width + "," + height + ")");
            return new Dimension(width, height); //must be the all paint dimension in order to be visible in scroll panel

        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (!_isDataNull) {
                ViewContext viewContext = new ViewContext();
                viewContext.setAjustedLocation(_ajustedLocation).setAreaWidth(this.getWidth());
                if (_isDataLoaded) {
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
            int height = _rowCount * (ViewSetting.HEIGHT_AA * 2 - ViewSetting.HEIGHT_AA / 2);
            if (height == 0) {
                height = 5 * ViewSetting.HEIGHT_AA;
            }
            return new Dimension(width, height);

        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (!_isDataNull) {
                if (_isDataLoaded) {
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
                    for (int i = 1; i < _rowCount + 1; i++) {
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
