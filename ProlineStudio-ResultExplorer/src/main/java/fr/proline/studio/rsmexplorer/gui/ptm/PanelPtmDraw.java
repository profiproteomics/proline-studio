/*
 * @cea 
 * http://www.profiproteomics.fr
 * create date: 4 déc. 2018
 */
package fr.proline.studio.rsmexplorer.gui.ptm;

import fr.proline.studio.rsmexplorer.gui.ptm.mark.PtmMarkCtrl;
import fr.proline.studio.rsmexplorer.gui.ptm.pep.PeptideAreaCtrl;
import fr.proline.studio.rsmexplorer.gui.ptm.mark.ProteinSequenceCtrl;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;

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
    private int _selectedRowIndex;
    int _ajustedLocation;
    private int _rowCount;
    private int _sequenceLength;
    JScrollPane _scrollPane;
    private PanelPeptidesPTMSiteGraphic _ctrl;
    private boolean _isDataNull;//when precedent databox change order ou filter, we can have non selected row, in this case, nothing to show

    protected PanelPtmDraw(PanelPeptidesPTMSiteGraphic ctrl, PtmMarkCtrl ctrlMark, ProteinSequenceCtrl ctrlSequence, PeptideAreaCtrl ctrlPeptideArea) {
        _ctrl = ctrl;
        _titlePane = new TitlePane(ctrlMark, ctrlSequence);
        _peptidePane = new PeptidePane(ctrlPeptideArea);
        _rowCount = 0;
        _sequenceLength = 0;
        _selectedRowIndex = 0;
       
        initComponents();

    }

    private void initComponents() {
         setDoubleBuffered(false);
        _titlePane.setMarkBeginPoint(ViewSetting.BORDER_GAP, ViewSetting.BORDER_GAP);
        _titlePane.setSequenceBeginPoint(ViewSetting.BORDER_GAP, ViewSetting.HEIGHT_MARK);
        _peptidePane.setBeginPoint(ViewSetting.BORDER_GAP, ViewSetting.BORDER_GAP);

        //first setPreferredSize to guarantee the height
        _titlePane.setPreferredSize(new Dimension(INITIAL_WIDTH, ViewSetting.HEIGHT_MARK + ViewSetting.HEIGHT_SEQUENCE));
        _titlePane.setBackground(Color.WHITE);
        _peptidePane.setBackground(Color.WHITE);

        _scrollPane = new JScrollPane(_peptidePane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        _scrollPane.setColumnHeaderView(_titlePane);

        this.setLayout(new BorderLayout());
        this.add(_scrollPane, BorderLayout.CENTER);
    }

    public void setIsDataLoaded(boolean isDataLoaded) {
        if (isDataLoaded == true) {
            this._selectedRowIndex = 0;
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

    protected int getSelectedPeptideIndex() {
        return this._selectedRowIndex;
    }

    protected void clean() {
        this._isDataNull = true;
    }

    protected void setSelectedIndex(int i) {
        _selectedRowIndex = i;
    }

    private class TitlePane extends JPanel {

        private PtmMarkCtrl m_ctrlMark;
        private ProteinSequenceCtrl m_ctrlSequence;

        private TitlePane(PtmMarkCtrl ctrlMark, ProteinSequenceCtrl ctrlSequence) {
            super();
            m_ctrlMark = ctrlMark;
            m_ctrlSequence = ctrlSequence;
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
                    //PtmSitePeptide selectedItem = _ctrlPeptideArea.getSelectedItem(e.getX(), e.getY());
                    int selectedIndex = m_ctrlPeptideArea.getSelectedItemIndex(e.getX(), e.getY());
                    if (selectedIndex != -1) {
                        _selectedRowIndex = selectedIndex;
                    }
                    //logger.debug("PeptidePane mouseClicked(" + e.getX() + "," + e.getY() + ")");
                    if (_selectedRowIndex != -1) {
                        _ctrl.valueChanged(_selectedRowIndex);
                        //logger.debug(selectedItem.toString());
                    }
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

}
