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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Karine XUE
 */
public class PanelPtmDraw extends JPanel implements MouseListener{

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
    JScrollPane _horizonScrollPane;

    PanelPtmDraw(PtmMarkCtrl ctrlMark, ProteinSequenceCtrl ctrlSequence, PeptideAreaCtrl ctrlPeptideArea) {
        _titlePane = new TitlePane(ctrlMark, ctrlSequence);
        _peptidePane = new PeptidePane(ctrlPeptideArea);
        _rowCount = 0;
        _sequenceLength = 0;
        initComponents();
    }

    private void initComponents() {
        //first setPreferredSize to guarantee the height
        _titlePane.setPreferredSize(new Dimension(INITIAL_WIDTH, ViewSetting.HEIGHT_MARK + ViewSetting.HEIGHT_SEQUENCE));
        _titlePane.setBackground(Color.WHITE);

        _peptidePane.setBackground(Color.WHITE);
        _peptidePane.setPreferredSize(new Dimension(INITIAL_WIDTH, ViewSetting.HEIGHT_MARK * 5));
        JScrollPane verticalScrollPane = new JScrollPane(_peptidePane, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        verticalScrollPane.getViewport().setPreferredSize(new Dimension(INITIAL_WIDTH, ViewSetting.HEIGHT_MARK * 3));

        ContentPane pane = new ContentPane();
        //BoxLayout take the MaximumSize
        pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
        pane.add(_titlePane);
        pane.add(verticalScrollPane);
        pane.setPreferredSize(new Dimension(INITIAL_WIDTH, INITIAL_HEIGHT));

        _horizonScrollPane = new JScrollPane(pane, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        _horizonScrollPane.getViewport().setPreferredSize(new Dimension(0, 250));
        this.setLayout(new BorderLayout());
        this.add(_horizonScrollPane, BorderLayout.CENTER);
    }

    private void InitComponentPost() {
        JScrollBar hsb = this._horizonScrollPane.getHorizontalScrollBar();
        //next 2 lines work together to put the scroll button at the end
        hsb.setValue(hsb.getMaximum());
        hsb.setValue((this._sequenceLength + AJUSTE_GAP) * ViewSetting.WIDTH_AA);
    }

    public void setIsDataLoaded(boolean isDataLoaded) {
        this._isDataLoaded = isDataLoaded;
    }

    public void setAjustedLocation(int ajustedLocation) {
        this._ajustedLocation = ajustedLocation;
        if (ajustedLocation >= AJUSTE_GAP) {
            ajustedLocation -= AJUSTE_GAP;
        }
        InitComponentPost();
    }

    void setRowCount(int rowCount) {
        this._rowCount = rowCount;
    }

    public void setSequenceLength(int sequenceLength) {
        this._sequenceLength = sequenceLength;
        this._titlePane.setMaximumSize(new Dimension((_sequenceLength + AJUSTE_GAP) * ViewSetting.WIDTH_AA, ViewSetting.HEIGHT_MARK + ViewSetting.HEIGHT_SEQUENCE + 30));
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void mousePressed(MouseEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void mouseExited(MouseEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


    /**
     * class used as content panel in the horizontal scroll panel,
     * getPreferredSize return the useful scroll size.
     */
    private class ContentPane extends JPanel {

        @Override
        public Dimension getPreferredSize() {
             int width = (_sequenceLength + AJUSTE_GAP-_ajustedLocation) * ViewSetting.WIDTH_AA;
            //int width = (_sequenceLength + AJUSTE_GAP) * ViewSetting.WIDTH_AA;
            if (width == 0) {
                width = INITIAL_WIDTH;
            }
            int height = _rowCount * (ViewSetting.WIDTH_AA * 2 - ViewSetting.WIDTH_AA / 2);
            if (height == 0) {
                height = 5 * ViewSetting.WIDTH_AA;
            }
            //System.out.println("ContentPane getPreferredSize(" + width + "," + height + ")");
            return new Dimension(width, height); //must be the all dimension as in paintComponent

        }
    }

    private class TitlePane extends JPanel {

        private PtmMarkCtrl _ctrlMark;
        private ProteinSequenceCtrl _ctrlSequence;

        private TitlePane(PtmMarkCtrl ctrlMark, ProteinSequenceCtrl ctrlSequence) {
            super();

            _ctrlMark = ctrlMark;
            _ctrlSequence = ctrlSequence;
            _ctrlMark.setBeginPoint(ViewSetting.BORDER_GAP + ViewSetting.WIDTH_AA, ViewSetting.BORDER_GAP);
            _ctrlSequence.setBeginPoint(ViewSetting.BORDER_GAP + ViewSetting.WIDTH_AA, ViewSetting.HEIGHT_MARK);

        }

        /**
         * fixer the height
         *
         * @return
         */
        @Override
        public Dimension getMinimumSize() {
            return new Dimension(this.getWidth(), ViewSetting.HEIGHT_MARK + ViewSetting.HEIGHT_SEQUENCE);
        }

        @Override
        public Dimension getMaximumSize() {
            //logger.debug("getMaximumSize title Pane Maxi size: (" + this.getWidth() + "," + this.getHeight() + ")");
            return new Dimension(Integer.MAX_VALUE, ViewSetting.HEIGHT_MARK + ViewSetting.HEIGHT_SEQUENCE + 30);
        }

        @Override
        public Dimension getPreferredSize() {
            //logger.debug("title Pane preferred size: (" + this.getWidth() + "," + this.getHeight() + ")");
            //logger.debug("getPreferredSize horizontal value: " + _horizonScrollPane.getHorizontalScrollBar().getValue());
            return new Dimension(this.getWidth(), ViewSetting.HEIGHT_MARK + ViewSetting.HEIGHT_SEQUENCE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (_isDataLoaded) {
                Graphics2D g2 = (Graphics2D) g;
                _ctrlMark.paint(g2, _ajustedLocation, this.getWidth());
                _ctrlSequence.paint(g2, _ajustedLocation, this.getWidth());
            }
        }
    }

    private class PeptidePane extends JPanel {

        private PeptideAreaCtrl _ctrlPeptideArea;

        private PeptidePane(PeptideAreaCtrl ctrlPeptideArea) {
            super();
            _ctrlPeptideArea = ctrlPeptideArea;
            _ctrlPeptideArea.setBeginPoint(ViewSetting.BORDER_GAP + ViewSetting.WIDTH_AA, ViewSetting.BORDER_GAP);
        }

        @Override
        public Dimension getPreferredSize() {
            int width = (_sequenceLength + AJUSTE_GAP-_ajustedLocation) * ViewSetting.WIDTH_AA;
            //int width = (_sequenceLength + AJUSTE_GAP) * ViewSetting.WIDTH_AA;
            int height = _rowCount * (ViewSetting.WIDTH_AA * 2 - ViewSetting.WIDTH_AA / 2);
            if (height == 0) {
                height = 5 * ViewSetting.WIDTH_AA;
            }
            return new Dimension(width, height); //must be the all paint dimension in order to be visible in scroll panel

        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (_isDataLoaded) {
                Graphics2D g2 = (Graphics2D) g;
                _ctrlPeptideArea.paint(g2, _ajustedLocation, this.getWidth());
            }
        }
    }

}
