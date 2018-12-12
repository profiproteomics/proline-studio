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
    private boolean _isDataNull;

    protected PanelPtmDraw(PanelPeptidesPTMSiteGraphic ctrl, PtmMarkCtrl ctrlMark, ProteinSequenceCtrl ctrlSequence, PeptideAreaCtrl ctrlPeptideArea) {
        _ctrl = ctrl;
        _titlePane = new TitlePane(ctrlMark, ctrlSequence);
        _peptidePane = new PeptidePane(ctrlPeptideArea);
        _rowCount = 0;
        _sequenceLength = 0;
        _selectedRowIndex = 0;
        setDoubleBuffered(false);
        initComponents();

    }

    private void initComponents() {
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
            //TODO Cby rq =assigning the parameter value does not change anything ..
            ajustedLocation -= AJUSTE_GAP;
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

    private class TitlePane extends JPanel {

        private PtmMarkCtrl _ctrlMark;
        private ProteinSequenceCtrl _ctrlSequence;

        private TitlePane(PtmMarkCtrl ctrlMark, ProteinSequenceCtrl ctrlSequence) {
            super();
            _ctrlMark = ctrlMark;
            _ctrlSequence = ctrlSequence;
        }

        public void setMarkBeginPoint(int x, int y) {
            _ctrlMark.setBeginPoint(x, y);
        }

        public void setSequenceBeginPoint(int x, int y) {
            _ctrlSequence.setBeginPoint(x, y);
        }

//        /**
//         * fixer the height
//         *
//         * @return
//         */
//        @Override
//        public Dimension getMinimumSize() {
//            return new Dimension(this.getWidth(), ViewSetting.HEIGHT_MARK + ViewSetting.HEIGHT_SEQUENCE);
//        }
//        //MUST rewrite it when use BoxLayout
//        @Override
//        public Dimension getMaximumSize() {
//            //logger.debug("getMaximumSize title Pane Maxi size: (" + this.getWidth() + "," + this.getHeight() + ")");
//            return new Dimension(Integer.MAX_VALUE, ViewSetting.HEIGHT_MARK + ViewSetting.HEIGHT_SEQUENCE + 30);
//        }
//
//        @Override
//        public Dimension getPreferredSize() {
//            //logger.debug("title Pane preferred size: (" + this.getWidth() + "," + this.getHeight() + ")");
//            //logger.debug("getPreferredSize horizontal value: " + _scrollPane.getHorizontalScrollBar().getValue());
//            //return new Dimension((_sequenceLength + AJUSTE_GAP - _ajustedLocation) * ViewSetting.WIDTH_AA, ViewSetting.HEIGHT_MARK + ViewSetting.HEIGHT_SEQUENCE);
//            return new Dimension((_sequenceLength + AJUSTE_GAP - _ajustedLocation) * 14, ViewSetting.HEIGHT_MARK + ViewSetting.HEIGHT_SEQUENCE);
//        }
        @Override
        protected void paintComponent(Graphics g) {
            if (_isDataNull) {
                super.paintComponent(g);
            } else {
                FontMetrics fontM = g.getFontMetrics(ViewSetting.FONT_SEQUENCE);
                int[] widthSet = fontM.getWidths();

                int fontWidth = widthSet[20];
                fontWidth = 14;
//            for (int i = 0; i < widthSet.length ; i++) {
//                
//                logger.debug(" width Font i : " +i+":"+widthSet[i]);
//            }
                //logger.debug(" width Font : "+ fontWidth);
                this.isPaintingOrigin();
                this.setPreferredSize(new Dimension((_sequenceLength + AJUSTE_GAP - _ajustedLocation) * fontWidth, ViewSetting.HEIGHT_MARK + ViewSetting.HEIGHT_SEQUENCE));
                super.paintComponent(g);

                if (_isDataLoaded) {
                    Graphics2D g2 = (Graphics2D) g;
                    _ctrlMark.paint(g2, _ajustedLocation, fontWidth);
                    _ctrlSequence.paint(g2, _ajustedLocation, fontWidth);
                }
            }
        }

    }

    private class PeptidePane extends JPanel {

        private PeptideAreaCtrl _ctrlPeptideArea;

        private PeptidePane(PeptideAreaCtrl ctrlPeptideArea) {
            super();
            _ctrlPeptideArea = ctrlPeptideArea;
            this.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    //PtmSitePeptide selectedItem = _ctrlPeptideArea.getSelectedItem(e.getX(), e.getY());
                    int selectedIndex = _ctrlPeptideArea.getSelectedItemIndex(e.getX(), e.getY());
                    if (selectedIndex != -1) {
                        _selectedRowIndex = selectedIndex;
                    }
                    //logger.debug("PeptidePane mouseClicked(" + e.getX() + "," + e.getY() + ")");
                    if (_selectedRowIndex != -1) {
                        _ctrl.valueChanged();
                        //logger.debug(selectedItem.toString());
                    }
                }
            });
        }

        public void setBeginPoint(int x, int y) {
            _ctrlPeptideArea.setBeginPoint(x, y);
        }

        @Override
        public Dimension getPreferredSize() {
            //int width = (_sequenceLength + AJUSTE_GAP - _ajustedLocation) * ViewSetting.WIDTH_AA;
            int width = (_sequenceLength + AJUSTE_GAP - _ajustedLocation) * 14;
            //int width = (_sequenceLength + AJUSTE_GAP) * ViewSetting.WIDTH_AA;
            int height = _rowCount * (ViewSetting.HEIGHT_AA * 2 - ViewSetting.HEIGHT_AA / 2);
            if (height == 0) {
                height = 5 * ViewSetting.HEIGHT_AA;
            }
            //logger.debug("PanelPtmDraw PeptidePane getPreferredSize(" + width + "," + height + ")");
            return new Dimension(width, height); //must be the all paint dimension in order to be visible in scroll panel

        }

        //tested don't work
        //@Override
//        public Dimension getMaximumSize() {
//            logger.debug("2222222222PanelPtmDraw PeptidePane getMaximumSize");
//            return super.getMaximumSize();
//        }
        //tested don't work
        //@Override
//        public Dimension getMinimumSize() {
//            logger.debug("333333333PanelPtmDraw PeptidePane getMinimumSize");
//            return super.getMinimumSize();
//        }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (!_isDataNull) {
                FontMetrics fontM = g.getFontMetrics(ViewSetting.FONT_SEQUENCE);
                int fontWidth = fontM.getWidths()[20];
                fontWidth = 14;
                //set graphic begin point
                //logger.debug(" width AA : " + fontWidth);
                if (_isDataLoaded) {
                    Graphics2D g2 = (Graphics2D) g;

                    _ctrlPeptideArea.paint(g2, _ajustedLocation, this.getWidth(), fontWidth);
                }
            }
        }
    }

}
