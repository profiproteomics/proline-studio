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
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.*;

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
        setDoubleBuffered(false);
        initComponents();
    }

    private void initComponents() {
        //first setPreferredSize to guarantee the height
        _titlePane.setPreferredSize(new Dimension(INITIAL_WIDTH, ViewSetting.HEIGHT_MARK + ViewSetting.HEIGHT_SEQUENCE));
        _titlePane.setBackground(Color.WHITE);

        _peptidePane.setBackground(Color.WHITE);
        _peptidePane.setPreferredSize(new Dimension(INITIAL_WIDTH, ViewSetting.HEIGHT_MARK * 5));

//        JScrollPane verticalScrollPane = new JScrollPane(_peptidePane, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
//        verticalScrollPane.getViewport().setPreferredSize(new Dimension(INITIAL_WIDTH, ViewSetting.HEIGHT_MARK * 3));

//        ContentPane pane = new ContentPane();
        //BoxLayout take the MaximumSize
//        pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
//        pane.add(_titlePane);
//        pane.add(verticalScrollPane);
//        pane.setPreferredSize(new Dimension(INITIAL_WIDTH, INITIAL_HEIGHT));

        _horizonScrollPane = new JScrollPane(_peptidePane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        _horizonScrollPane.getViewport().setPreferredSize(new Dimension(INITIAL_WIDTH, INITIAL_HEIGHT));

        _horizonScrollPane.setColumnHeaderView(_titlePane);

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
            //TODO Cby rq =assigning the parameter value does not change anything ..
            ajustedLocation -= AJUSTE_GAP;
        }
//        InitComponentPost();
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
            return new Dimension((_sequenceLength + AJUSTE_GAP-_ajustedLocation) * ViewSetting.WIDTH_AA, ViewSetting.HEIGHT_MARK + ViewSetting.HEIGHT_SEQUENCE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            this.isPaintingOrigin();

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


/* Rule.java is used by ScrollDemo.java. */
class Rule extends JComponent {
    public static final int INCH = Toolkit.getDefaultToolkit().
        getScreenResolution();
    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;
    public static final int SIZE = 35;

    public int orientation;
    public boolean isMetric;
    private int increment;
    private int units;

    public Rule(int o, boolean m) {
        orientation = o;
        isMetric = m;
        setIncrementAndUnits();
    }

    public void setIsMetric(boolean isMetric) {
        this.isMetric = isMetric;
        setIncrementAndUnits();
        repaint();
    }

    private void setIncrementAndUnits() {
        if (isMetric) {
            units = (int)((double)INCH / (double)2.54); // dots per centimeter
            increment = units;
        } else {
            units = INCH;
            increment = units / 2;
        }
    }

    public boolean isMetric() {
        return this.isMetric;
    }

    public int getIncrement() {
        return increment;
    }

    public void setPreferredHeight(int ph) {
        setPreferredSize(new Dimension(SIZE, ph));
    }

    public void setPreferredWidth(int pw) {
        setPreferredSize(new Dimension(pw, SIZE));
    }

    protected void paintComponent(Graphics g) {
        Rectangle drawHere = g.getClipBounds();

        // Fill clipping area with dirty brown/orange.
        g.setColor(new Color(230, 163, 4));
        g.fillRect(drawHere.x, drawHere.y, drawHere.width, drawHere.height);

        // Do the ruler labels in a small font that's black.
        g.setFont(new Font("SansSerif", Font.PLAIN, 10));
        g.setColor(Color.black);

        // Some vars we need.
        int end = 0;
        int start = 0;
        int tickLength = 0;
        String text = null;

        // Use clipping bounds to calculate first and last tick locations.
        if (orientation == HORIZONTAL) {
            start = (drawHere.x / increment) * increment;
            end = (((drawHere.x + drawHere.width) / increment) + 1)
                * increment;
        } else {
            start = (drawHere.y / increment) * increment;
            end = (((drawHere.y + drawHere.height) / increment) + 1)
                * increment;
        }

        // Make a special case of 0 to display the number
        // within the rule and draw a units label.
        if (start == 0) {
            text = Integer.toString(0) + (isMetric ? " cm" : " in");
            tickLength = 10;
            if (orientation == HORIZONTAL) {
                g.drawLine(0, SIZE-1, 0, SIZE-tickLength-1);
                g.drawString(text, 2, 21);
            } else {
                g.drawLine(SIZE-1, 0, SIZE-tickLength-1, 0);
                g.drawString(text, 9, 10);
            }
            text = null;
            start = increment;
        }

        // ticks and labels
        for (int i = start; i < end; i += increment) {
            if (i % units == 0)  {
                tickLength = 10;
                text = Integer.toString(i/units);
            } else {
                tickLength = 7;
                text = null;
            }

            if (tickLength != 0) {
                if (orientation == HORIZONTAL) {
                    g.drawLine(i, SIZE-1, i, SIZE-tickLength-1);
                    if (text != null)
                        g.drawString(text, i-3, 21);
                } else {
                    g.drawLine(SIZE-1, i, SIZE-tickLength-1, i);
                    if (text != null)
                        g.drawString(text, 9, i+3);
                }
            }
        }
    }
}