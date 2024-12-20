/* 
 * Copyright (C) 2019
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
package fr.proline.studio.graphics;

import fr.proline.studio.graphics.Axis.EnumXInterface;
import fr.proline.studio.graphics.Axis.EnumYInterface;
import fr.proline.studio.graphics.core.PlotToolbarListenerInterface;
import fr.proline.studio.graphics.cursor.AbstractCursor;
import fr.proline.studio.graphics.measurement.AbstractMeasurement;
import fr.proline.studio.parameter.ParameterList;
import fr.proline.studio.parameter.SettingsInterface;
import fr.proline.studio.utils.StringUtils;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.event.EventListenerList;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Panel to display data with X and Y Axis
 *
 * @author JM235353
 */
public class BasePlotPanel extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener, EnumXInterface, EnumYInterface, SettingsInterface, MoveableInterface {



    public enum MOUSE_MODE {
        NORMAL_MODE,
        SELECTION_MODE
    }

    private static final Logger m_logger = LoggerFactory.getLogger(BasePlotPanel.class);

    private static final Color PANEL_BACKGROUND_COLOR = UIManager.getColor("Panel.background");

    private XAxis m_xAxis = null;
    private YAxis m_yAxis = null;
    private YAxis m_yAxisRight = null;

    private double[] m_xAxisBounds = {Double.NaN, Double.NaN};
    private double[] m_yAxisBounds = {Double.NaN, Double.NaN};
    private double[] m_yAxisRightBounds = {Double.NaN, Double.NaN};

    private final ZoomGesture m_zoomGesture = new ZoomGesture();
    private final SelectionGestureLasso m_selectionGesture = new SelectionGestureLasso();
    /**
     * manage zoom/unzoom gesture
     */
    private final PanAxisGesture m_panAxisGesture = new PanAxisGesture();
    /**
     * manage movable object
     */
    private final MoveGesture m_moveGesture = new MoveGesture();

    public final static int GAP_FIGURES_Y = 30;
    public final static int GAP_FIGURES_X = 24;
    public final static int GAP_END_AXIS = 10;
    public final static int GAP_AXIS_TITLE = 20;
    public final static int GAP_AXIS_LINE = 5;
    
    private BufferedImage m_doubleBuffer = null;
    private boolean m_useDoubleBuffering = false;
    private boolean m_updateDoubleBuffer = false; 
    
    private BasePlotPanelViewAll m_viewAllPanel = new BasePlotPanelViewAll(this);

    private boolean m_plotHorizontalGrid = true;
    private boolean m_plotVerticalGrid = true;
    private boolean m_dataLocked = false;
    private boolean m_drawCursor = false;

    private List<PlotPanelListener> m_listeners = new ArrayList<>();

    private PlotToolbarListenerInterface m_plotToolbarListener = null;

    private Rectangle m_plotArea = new Rectangle();
    private Rectangle m_plotAreaViewAllMap = new Rectangle();

    // cursor, show coord.
    private String m_coordX = "";
    private String m_coordY = "";
    private int m_posx;
    private int m_posy;
    /* font coord */
    private final static Font coordFont = new Font("dialog", Font.BOLD, 9);
    private final static Color coordColor = Color.GRAY;

    private final static NumberFormat nfE = NumberFormat.getNumberInstance();
    private final static DecimalFormat formatE = (DecimalFormat) nfE;
    private final static NumberFormat nf = NumberFormat.getNumberInstance();
    private final static DecimalFormat format = (DecimalFormat) nf;

    // title
    private String m_plotTitle = null;
    /* title coord */
    private final static Font TITLE_FONT = new Font("dialog", Font.BOLD, 11);
    private final static Color TITLE_COLOR = Color.DARK_GRAY;

//    public final FPSUtility fps = new FPSUtility(20);
    //events
    private EventListenerList listenerList = new EventListenerList();
    /**
     * if Axis is Enum type, after calculate the min max, we add a space before
     * & after, this ajustment is need only one time before the first paint,
     * each time if the select Axis change item, we should ajust it.
     */
    private boolean m_isEnumAxisUpdated = false;

    private MOUSE_MODE m_mouseMode = MOUSE_MODE.NORMAL_MODE;

    public BasePlotPanel() {
        formatE.applyPattern("0.#####E0");
        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
        ToolTipManager.sharedInstance().registerComponent(this);

    }

    public void setMouseMode(MOUSE_MODE mouseMode) {
        m_mouseMode = mouseMode;
    }

    public void setSelectionType(boolean selectionSquare) {
        m_selectionGesture.setSelectionIsSquare(selectionSquare);
    }

    public void setPlotTitle(String title) {
        m_plotTitle = title;
    }

    public String getPlotTitle() {
        return m_plotTitle;
    }

    public void setPlotToolbarListener(PlotToolbarListenerInterface plotToolbarListener) {
        m_plotToolbarListener = plotToolbarListener;
    }

    public void enableButton(PlotToolbarListenerInterface.BUTTONS button, boolean v) {
        if (m_plotToolbarListener != null) {
            m_plotToolbarListener.enable(button, v);
        }
    }

    public Rectangle getPlotArea() {
        return m_plotArea;
    }
    
    /**
     * used to add 0.5 space before first & after last enum label
     */
    private void updateEnumAxis() {

        XAxis xAxis = this.getXAxis();
        if (!xAxis.hasPlots()) {
            return;
        }

        YAxis yAxis = this.getYAxis();
        double xMin = xAxis.getMinValue();
        double xMax = xAxis.getMaxValue();
        double yMin = yAxis.getMinValue();
        double yMax = yAxis.getMaxValue();
        double newXMin = xMin;
        double newXMax = xMax;
        double newYMin = yMin;
        double newYMax = yMax;

        boolean isModified = false;
        if (xAxis.isEnum()) {
            newXMin = xMin - 0.5;
            newXMax = xMax + 0.5;
            isModified = true;
            xAxis.setRange(newXMin, newXMax);
        }
        if (yAxis.isEnum()) {
            newYMin = yMin - 0.5;
            newYMax = yMax + 0.5;
            isModified = true;
            yAxis.setRange(newYMin, newYMax);
        }
        if (isModified) {
            fireUpdateAxisRange(xMin, xMax, newXMin, newXMax, yMin, yMax, newYMin, newYMax);

        }
        
        YAxis yAxisRight = getYAxisRight();
        if (yAxisRight.isEnum()) {
            double y2Min = yAxisRight.getMinValue() - 0.5;
            double y2Max = yAxisRight.getMaxValue() + 0.5;
            yAxisRight.setRange(y2Min, y2Max);
        }
        
        m_isEnumAxisUpdated = true;
    }

    @Override
    public void paint(Graphics g) {
        
        Graphics2D g2d = (Graphics2D) g; 
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        if (!m_isEnumAxisUpdated) {
            updateEnumAxis();
        }

        boolean displayYAxisAtRight = ((m_yAxisRight != null) && (m_yAxisRight.hasPlots()) && (m_yAxisRight.displayAxis()));
        
        // To let space for Axis Title when it is displayed
        int gapForAxisXTtile = getXAxis().displayTitle() ? GAP_AXIS_TITLE : 0;
        int gapForAxisYTitle = getYAxis().displayTitle() ? GAP_AXIS_TITLE : 0;

        int xAxisX = GAP_FIGURES_Y + gapForAxisYTitle + GAP_AXIS_LINE;
        int yAxisWidth = GAP_FIGURES_Y + gapForAxisYTitle + GAP_AXIS_LINE;
        int yAxisRightWidth = (displayYAxisAtRight) ? yAxisWidth : 0;
        // height and width of the panel
        
        int width = getWidth() - yAxisRightWidth;
        int height = getHeight();
        int xAxisWidth = width - (xAxisX);

        // initialize plotArea
        m_plotArea.x = 0;
        m_plotArea.y = 0;

        m_plotArea.width = width;
        m_plotArea.height = height;
        g.setColor(PANEL_BACKGROUND_COLOR);
        g.fillRect(0, 0, width + yAxisRightWidth, height);
        //1 optionnal title at the begin
        int titleY = 0;
        if (this.m_plotTitle != null) {
            int wt = StringUtils.lenghtOfString(m_plotTitle, getFontMetrics(TITLE_FONT));
            int titleX = (width - wt) / 2;
            int ascent = getFontMetrics(TITLE_FONT).getAscent();
            int descent = getFontMetrics(TITLE_FONT).getDescent();
            titleY = ascent;
            g.setFont(TITLE_FONT);
            g.setColor(TITLE_COLOR);
            g.drawString(m_plotTitle, titleX, titleY);
            titleY += descent;
        }
        m_plotArea.y = titleY;

        g.setColor(Color.darkGray);

        int figuresXHeight = GAP_FIGURES_X;

        // checj max zoom reached
        m_xAxis.checkMaxZoomOutDone();
        m_yAxis.checkMaxZoomOutDone();


        //2 draw axis X
        boolean hasPlot = ((m_xAxis != null) && (m_xAxis.hasPlots()));
        if (hasPlot) {
            if ((m_xAxis != null) && (m_xAxis.displayAxis())) {
                int xAxisY = height - figuresXHeight - gapForAxisXTtile;
                int xAxisHeight = figuresXHeight + gapForAxisXTtile + GAP_AXIS_LINE;
                // set default size
                m_xAxis.setSize(xAxisX, xAxisY, xAxisWidth, xAxisHeight);

                // prepare paint
                m_xAxis.preparePaint(g2d);

                // set correct size
                figuresXHeight = m_xAxis.getMiniMumAxisHeight(figuresXHeight);
                xAxisY = height - figuresXHeight - gapForAxisXTtile;
                xAxisHeight = figuresXHeight + gapForAxisXTtile + GAP_AXIS_LINE;
                m_xAxis.setSize(xAxisX, xAxisY, xAxisWidth, xAxisHeight);


                //prepare plotArea begin point & width
                m_plotArea.x = m_xAxis.getX() + 1;
                m_plotArea.width = m_xAxis.getWidth();
                m_xAxis.paint(g2d);

            }
            //3.1 draw main axis Y
            int yAxisX = 0;
            int yAxisY = GAP_END_AXIS + titleY;

            int yAxisHeight = height - figuresXHeight - gapForAxisXTtile - GAP_END_AXIS - titleY;
            if (m_yAxis != null) {

                m_yAxis.setSize(yAxisX, yAxisY, yAxisWidth, yAxisHeight);
                
                if (m_yAxis.displayAxis()) {
                    //prepare plotArea begin point & height
                    m_plotArea.y = m_yAxis.getY() + 1;
                    m_plotArea.height = m_yAxis.getHeight();
                    m_yAxis.paint(g2d);
                }
            }
            //3.2 draw second axis Y & paint a rectangle to indicate the plot color on 2nd Axis Y
            if (displayYAxisAtRight) {
                //m_logger.debug("->seconde Axis Y");                    
                m_yAxisRight.setSize(m_plotArea.width + yAxisRightWidth, yAxisY, yAxisRightWidth, yAxisHeight);
                m_yAxisRight.paint(g2d);
            }
 
            //4 paint plot with grid
            if (m_plotArea.width >= 0 && m_plotArea.height >= 0) {
                //for test m_useDoubleBuffering = true;
                if (m_useDoubleBuffering) {
                    boolean createDoubleBuffer = ((m_doubleBuffer == null) || (m_doubleBuffer.getWidth() != m_plotArea.width) || (m_doubleBuffer.getHeight() != m_plotArea.height));
                    if (createDoubleBuffer) {
                        m_doubleBuffer = new BufferedImage(m_plotArea.width, m_plotArea.height, BufferedImage.TYPE_INT_ARGB);
                    }
                    if (createDoubleBuffer || m_updateDoubleBuffer) {
                        //from BufferedImage, get Graphics2D, initialize it's color,
                        Graphics2D graphicBufferG2d = (Graphics2D) m_doubleBuffer.getGraphics();
                        graphicBufferG2d.translate(-m_plotArea.x, -m_plotArea.y);
                        paintPlotWithGrid(graphicBufferG2d);
                        
                        m_updateDoubleBuffer = false;
                    }
                    g2d.drawImage(m_doubleBuffer, m_plotArea.x, m_plotArea.y, null);
                } else {
                    paintPlotWithGrid(g2d);
                    
                }
                // paint cursor and markers
                paintOverPlot(g2d);
                
                // paint view all 
                m_viewAllPanel.paintBufferedViewAllMap(g2d);
            }

            // set clipping area for zooming and selecting
            Shape previousClipping = g2d.getClip();
            if ((m_xAxis != null) && (m_yAxis != null) && (m_xAxis.displayAxis()) && (m_yAxis.displayAxis())) {
                int clipX = m_xAxis.valueToPixel(m_xAxis.getMinValue());
                int clipWidth = m_xAxis.valueToPixel(m_xAxis.getMaxValue()) - clipX;
                int clipY = m_yAxis.valueToPixel(m_yAxis.getMaxValue());
                int clipHeight = m_yAxis.valueToPixel(m_yAxis.getMinValue()) - clipY;
                g2d.setClip(clipX, clipY, clipWidth, clipHeight);
            }

            m_zoomGesture.paint(g2d);
            m_selectionGesture.paint(g2d);

            if (m_drawCursor) {
                // show coord
                paintCoord(g2d);
            }

            g2d.setClip(previousClipping);

        }

    }

    /**
     * paint each Plot with grid
     *
     * @param g2d
     */
    private void paintPlotWithGrid(Graphics2D g2d) {
        // m_logger.debug("___->paintPlotpaint plot...useDoubleBuffer=" + m_useDoubleBuffering);
        long startPlotTime = System.currentTimeMillis();
        //paint background
        g2d.setColor(Color.white);
        g2d.fillRect(m_plotArea.x, m_plotArea.y, m_plotArea.width, m_plotArea.height);
        //paint vertical grid
        if ((m_plotVerticalGrid) && (m_xAxis != null) && (m_xAxis.displayAxis())) {
            m_xAxis.paintGrid(g2d, m_plotArea.x, m_plotArea.width, m_plotArea.y, m_plotArea.height);
        }
        //paint horizontalGrid
        if ((m_plotHorizontalGrid) && (m_yAxis != null) && (m_yAxis.displayAxis())) {
            m_yAxis.paintGrid(g2d, m_plotArea.x, m_plotArea.width, m_plotArea.y, m_plotArea.height);
        }

        for (PlotBaseAbstract plot : getYAxis().getPlots()) {
            plot.paint(g2d, m_xAxis, m_yAxis);
        }
        for (PlotBaseAbstract plot : getYAxisRight().getPlots()) {
            plot.paint(g2d, m_xAxis, getYAxisRight());
        }

        long stopPlotTime = System.currentTimeMillis();
        if (stopPlotTime - startPlotTime > 50) {
            // display is too slow , we use with buffered image
            m_useDoubleBuffering = true;
        }

    }
    
    /**
     * paint things like cursors and markers which are over the paint
     *
     * @param g2d
     */
    private void paintOverPlot(Graphics2D g2d) {


        for (PlotBaseAbstract plot : getYAxis().getPlots()) {
            plot.paintOver(g2d);    // used by Parallel Coordinates
            plot.paintMarkers(g2d); // markers
            plot.paintCursors(g2d); // cursors
        }
        for (PlotBaseAbstract plot : getYAxisRight().getPlots()) {
            plot.paintOver(g2d); // not used on second y axis for the moment
        }

    }
    
    /**
     * Paint coordinate x, y value
     *
     * @param g
     */
    private void paintCoord(Graphics2D g) {
        int lx = StringUtils.lenghtOfString(m_coordX, getFontMetrics(coordFont));
        int ly = StringUtils.lenghtOfString(m_coordY, getFontMetrics(coordFont));
        int maxl = Math.max(lx, ly);
        g.setColor(coordColor);
        g.setFont(coordFont);
        int px = m_posx + 10;
        int py = m_posy;
        // on the edges
        if (px + maxl > this.getWidth()) {
            px = m_posx - maxl - 10;
        }
        if (py - 10 < 0) {
            py = 10;
        } else if (py + 20 > this.getHeight()) {
            py = this.getHeight() - 20;
        }
        g.drawString(m_coordX, px, py);
        g.drawString(m_coordY, px, (py + 15));
    }

    @Override
    public ArrayList<ParameterList> getParameters() {
        if (m_yAxis == null) {
            return null;
        }

        for (PlotBaseAbstract plot : m_yAxis.getPlots()) {
            ArrayList<ParameterList> parameters = plot.getParameters();
            if (parameters != null) {
                return parameters;
            }
        }
        return null;
    }

    @Override
    public void parametersChanged() {

        ArrayList<PlotBaseAbstract> plots = m_xAxis.getPlots();
        if (plots != null) {
            for (int i = 0; i < plots.size(); i++) {
                plots.get(i).parametersChanged();
            }
        }
        m_updateDoubleBuffer = true;
        repaint();
    }

    @Override
    public boolean parametersCanceled() {

        boolean repaintNeeded = false;

        ArrayList<PlotBaseAbstract> plots = m_xAxis.getPlots();
        if (plots != null) {
            for (int i = 0; i < plots.size(); i++) {
                repaintNeeded |= plots.get(i).parametersCanceled();
            }
        }
        if (repaintNeeded) {
            m_updateDoubleBuffer = true;
            repaint();
        }

        return repaintNeeded;
    }

    public void updatePlots(int[] cols, String parameterZ) {
        for (PlotBaseAbstract plot : getXAxis().getPlots()) {
            plot.update(cols, parameterZ);
        }
    }

    public void forceUpdateDoubleBuffer() {
        m_updateDoubleBuffer = true;
    }

    public void lockData(boolean lock) {
        if (lock == m_dataLocked) {
            return;
        }
        m_dataLocked = lock;

        m_updateDoubleBuffer = true;
        repaint();

    }

    public boolean isLocked() {
        return m_dataLocked;
    }

    public void lockMinXValue() {
        double[] resMinMax = getMinMaxPlots(getXAxis());
        m_xAxis.lockMinValue(resMinMax[0]);

    }

    public void lockMinYValue() {
        double[] resMinMax = getMinMaxPlots(getYAxis());
        m_yAxis.lockMinValue(resMinMax[0]);

    }

    public void setDrawCursor(boolean drawCursor) {
        m_drawCursor = drawCursor;
    }

    public void displayGrid(boolean v) {
        m_plotVerticalGrid = v;
        m_plotHorizontalGrid = v;
        m_updateDoubleBuffer = true;
        repaint();
    }

    public boolean displayGrid() {
        return (m_plotHorizontalGrid || m_plotVerticalGrid);
    }
    
    public void displayViewAllMap(boolean b) {
        m_viewAllPanel.setDisplay(b);
    }
    
    public boolean isViewAllMapDisplayed() {
        return m_viewAllPanel.isDisplayed();
    }


    /**
     * set the first plot with Axis
     *
     * @param plot
     */
    public void setPlot(PlotBaseAbstract plot) {
        getXAxis().setPlot(plot);
        getYAxis().setPlot(plot);
        
        m_viewAllPanel.setDisplay(false);
        if (m_plotToolbarListener != null) {
            m_plotToolbarListener.stateModified(PlotToolbarListenerInterface.BUTTONS.VIEW_ALL_MAP);
        }

        updateAxis(plot);
    }

    public void clearPlots() {
        getXAxis().clearPlots();
        getYAxis().clearPlots();
        getYAxisRight().clearPlots();
    }

    public void clearPlotsWithRepaint() {
        boolean actualLock = m_dataLocked;
        lockData(true); //if not true, repaint don't work
        clearPlots();
        repaint();
        lockData(actualLock);
    }

    public boolean hasPlots() {
        return getXAxis().hasPlots();
    }

    public ArrayList<PlotBaseAbstract> getPlots() {
        return getXAxis().getPlots();
    }

    public void addPlot(PlotXYAbstract plot) {
        addPlot(plot, true);
    }
    public void addPlot(PlotXYAbstract plot, boolean leftYAxis) {
        getXAxis().addPlot(plot);
        if (leftYAxis) {
            getYAxis().addPlot(plot);
        } else {
            getYAxisRight().addPlot(plot);
        }
        updateAxis(plot);
    }

    public void setSecondAxisPlotInfo(String title, Color color) {
        getYAxisRight().setTitle(title);
        getYAxisRight().setColorOnTitle(color);
    }

    public ArrayList<Long> getSelection() {
        if (getXAxis().hasPlots()) {
            return getXAxis().getPlots().get(0).getSelectedIds();
        } else {
            return null;
        }
    }

    public void setSelection(ArrayList<Long> selection) {
        if (getXAxis().hasPlots()) {
            getXAxis().getPlots().get(0).setSelectedIds(selection);
            m_updateDoubleBuffer = true;
            repaint();
        }
    }

    /**
     * browse each plot to find the Min Max X, Y, useful for XAxis YAxis
     *
     * @return doube[4]= [minX, maxX, minY, maxY]
     */
    public double[] getMinMaxPlots(Axis axis) {
        
        boolean isXAxis = (axis instanceof XAxis);
        
        ArrayList<PlotBaseAbstract> plotList = axis.getPlots();
        int nb = (plotList != null) ? plotList.size() : 0;
        if (nb > 0) {

            if (isXAxis) {
                double minX = plotList.get(0).getXMin();
                double maxX = plotList.get(0).getXMax();

                for (int k = 1; k < nb; k++) {
                    double plotXMin = plotList.get(k).getXMin();
                    double plotXMax = plotList.get(k).getXMax();
                    if (plotXMin != 0 || plotXMax != 0) {
                        if (minX == 0 && maxX == 0) {
                            minX = plotXMin;
                            maxX = plotXMax;
                        }
                        minX = Math.min(minX, plotXMin);
                        maxX = Math.max(maxX, plotXMax);
                    }
                }
                m_resMinMax[0] = minX;
                m_resMinMax[1] = maxX;
            } else {
                double minY = plotList.get(0).getYMin();
                double maxY = plotList.get(0).getYMax();

                for (int k = 1; k < nb; k++) {
                    double plotYMin = plotList.get(k).getYMin();
                    double plotYMax = plotList.get(k).getYMax();
                    if (plotYMin != 0 || plotYMax != 0) {
                        if (minY == 0 && maxY == 0) {
                            minY = plotYMin;
                            maxY = plotYMax;
                        }
                        minY = Math.min(minY, plotYMin);
                        maxY = Math.max(maxY, plotYMax);
                    }
                }
                m_resMinMax[0] = minY;
                m_resMinMax[1] = maxY;
            }

        }
        return m_resMinMax;
    }
    private final double[] m_resMinMax = new double[2];

    /**
     * from plot list, get the bounds of Axis X and Y,
     *
     * @param plot
     */
    public void updateAxis(PlotBaseAbstract plot) {
        updateAxis(plot.getDoubleBufferingPolicy());
    }

    private void updateAxis(boolean useDoubleBuffering) {
        updateAxis();
        m_useDoubleBuffering = useDoubleBuffering;
    }

    public void updateAxis() {
        m_isEnumAxisUpdated = false;
        
        XAxis xAxis = getXAxis();
        double[] resMinMax = getMinMaxPlots(xAxis);
        xAxis.setSelected(false);
        xAxis.setRange((Double.isNaN(m_xAxisBounds[0])) ? resMinMax[0] : m_xAxisBounds[0], (Double.isNaN(m_xAxisBounds[1])) ? resMinMax[1] : m_xAxisBounds[1]);

        YAxis yAxis = getYAxis();
        resMinMax = getMinMaxPlots(yAxis);
        yAxis.setSelected(false);
        yAxis.setRange((Double.isNaN(m_yAxisBounds[0])) ? resMinMax[0] : m_yAxisBounds[0], (Double.isNaN(m_yAxisBounds[1])) ? resMinMax[1] : m_yAxisBounds[1]);

        YAxis rightYAxis = getYAxisRight();
        if (rightYAxis.hasPlots()) {
            resMinMax = getMinMaxPlots(rightYAxis);
            rightYAxis.setSelected(false);
            rightYAxis.setRange((Double.isNaN(m_yAxisRightBounds[0])) ? resMinMax[0] : m_yAxisRightBounds[0], (Double.isNaN(m_yAxisRightBounds[1])) ? resMinMax[1] : m_yAxisRightBounds[1]);
        }

        m_updateDoubleBuffer = true;
        m_viewAllPanel.updateDoubleBuffer();
        m_viewAllPanel.setSelected(false, false);
    }

    public void setXAxisTitle(String title) {
        if (m_xAxis == null) {
            return;
        }
        m_xAxis.setTitle(title);
    }

    public void setXAxisBounds(double min, double max) {
        m_xAxisBounds = new double[]{min, max};
        if (getXAxis().hasPlots()) {
            updateAxis(getXAxis().getPlots().get(0));
        }
    }

    public double[] getXAxisBounds() {
        return new double[]{m_xAxisBounds[0], m_xAxisBounds[1]};
    }

    public double[] getYAxisBounds() {
        return new double[]{m_yAxisBounds[0], m_yAxisBounds[1]};
    }
    
    public double[] getYAxisRightBounds() {
        return new double[]{m_yAxisRightBounds[0], m_yAxisRightBounds[1]};
    }

    public void setYAxisBounds(double min, double max) {
        m_yAxisBounds = new double[]{min, max};
        if (getXAxis().hasPlots()) {
            updateAxis(getXAxis().getPlots().get(0));
        }
    }
    
    public void setYAxisRightBounds(double min, double max) {
        m_yAxisRightBounds = new double[]{min, max};
        if (getXAxis().hasPlots()) {
            updateAxis(getXAxis().getPlots().get(0));
        }
    }

    public void setYAxisTitle(String title) {
        getYAxis().setTitle(title);
    }
    
    
    public void setYAxisRightTitle(String title) {
        getYAxisRight().setTitle(title);
    }

    public XAxis getXAxis() {
        if (m_xAxis == null) {
            m_xAxis = new XAxis(this);
        }
        return m_xAxis;
    }

    public YAxis getYAxis() {
        if (m_yAxis == null) {
            m_yAxis = new YAxis(this);
        }
        return m_yAxis;
    }

    public YAxis getYAxisRight() {
        if (m_yAxisRight == null) {
            m_yAxisRight = new YAxis(this);
            m_yAxisRight.setSecondAxis();
        }
        return m_yAxisRight;
    }

    @Override
    public void mouseClicked(MouseEvent e) {

        if (e.getClickCount() == 2) {
            for (PlotBaseAbstract plot : getXAxis().getPlots()) {
                plot.doubleClicked(e.getX(), e.getY());
            }
        }
        if (m_xAxis != null && m_yAxis != null) {

            double xValue = m_xAxis.pixelToValue(e.getX());
            double yValue = m_yAxis.pixelToValue(e.getY());

            fireMouseClicked(e, xValue, yValue);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (m_drawCursor) {
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
        if (!getXAxis().hasPlots()) {
            return;
        }

        int x = e.getX();
        int y = e.getY();
        if (insidePlotArea(x, y)) {
            // Mouse Pressed in the plot area

            // deselect axis
            if (m_xAxis != null) {
                m_xAxis.setSelected(false);
            }
            if (m_yAxis != null) {
                m_yAxis.setSelected(false);
            }
            if (m_yAxisRight != null) {
                m_yAxisRight.setSelected(false);
            }

            if (SwingUtilities.isLeftMouseButton(e)) {

                // Action according to mode
                if (m_mouseMode == MOUSE_MODE.SELECTION_MODE) {
                    m_selectionGesture.startSelection(x, y);
                } else {
                    // check if we are over a moveable object
                    MoveableInterface movable = null;
                    PlotBaseAbstract plotWithMovable = null;
                    
                    movable = m_viewAllPanel.getOverMovable(x, y);
                    if (movable != null) {
                        movable.setSelected(true, false);
                    }

                    if (movable == null) {
                        m_viewAllPanel.setSelected(false, false);
                        
                        for (PlotBaseAbstract plot : getXAxis().getPlots()) {
                            movable = plot.getOverMovable(x, y);
                            if (movable != null) {
                                plotWithMovable = plot;
                                break;
                            }
                        }
                    }

                    if (movable == null) {
                        // we move the panel itself !
                        movable = this;
                    }

                    if (movable != null) {
                        m_moveGesture.startMoving(x, y, movable);
                        if (movable instanceof AbstractCursor) {
                            plotWithMovable.selectCursor((AbstractCursor) movable);
                        }
                        setCursor(new Cursor(Cursor.MOVE_CURSOR));
                    }
                }

            } else if (SwingUtilities.isRightMouseButton(e)) {
                m_zoomGesture.startZooming(x, y, !m_viewAllPanel.insideXY(x, y));
            }
            repaint();
        } else if (!SwingUtilities.isRightMouseButton(e) && m_xAxis != null && m_xAxis.inside(x, y)) {
            m_panAxisGesture.startPanning(x, y, PanAxisGesture.X_AXIS_PAN);
        } else if (!SwingUtilities.isRightMouseButton(e) && m_yAxis != null && m_yAxis.inside(x, y)) {
            m_panAxisGesture.startPanning(x, y, PanAxisGesture.Y_AXIS_PAN);
        } else if (!SwingUtilities.isRightMouseButton(e) && m_yAxisRight != null && m_yAxisRight.inside(x, y)) {
            m_panAxisGesture.startPanning(x, y, PanAxisGesture.Y_AT_RIGHT_AXIS_PAN);
        }

    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (m_drawCursor) {
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
        boolean mustRepaint = false;

        int x = e.getX();
        int y = e.getY();

        if (e.isPopupTrigger() && !m_panAxisGesture.isPanning() && !m_zoomGesture.isZooming()) {
            if (m_xAxis != null && m_yAxis != null && m_xAxis.inside(x, y)) {
                m_xAxis.setSelected(true);
                m_yAxis.setSelected(false);
                m_yAxisRight.setSelected(false);
                JPopupMenu popup = createAxisPopup(m_xAxis, null, x, y);
                popup.show((JComponent) e.getSource(), x, y);
                mustRepaint = true;
            } else if (m_xAxis != null && m_yAxis != null && m_yAxis.inside(x, y)) {
                m_xAxis.setSelected(false);
                m_yAxis.setSelected(true);
                m_yAxisRight.setSelected(false);
                JPopupMenu popup = createAxisPopup(m_yAxis, getYAxisRight().hasPlots() ? getYAxisRight() : null, x, y);
                popup.show((JComponent) e.getSource(), x, y);
                mustRepaint = true;
            } //trigger on m_secondYAxis is not allowed
        } else if (!m_zoomGesture.isZooming() && !m_selectionGesture.isSelecting() && !m_moveGesture.isMoving() && (m_panAxisGesture.getAction() != PanAxisGesture.ACTION_PAN)) {

            if (m_xAxis != null && m_xAxis.inside(x, y)) {//zoom on the X Axis
                mustRepaint |= m_xAxis.setSelected(!m_xAxis.isSelected());
                mustRepaint |= m_yAxis.setSelected(false);
            } else if (m_yAxis != null && m_yAxis.inside(x, y)) {//zoom on the Y Axis
                mustRepaint |= m_xAxis.setSelected(false);
                mustRepaint |= m_yAxis.setSelected(!m_yAxis.isSelected());
            }else if (m_yAxisRight != null && m_yAxisRight.inside(x, y)) {//zoom on the Y Axis
                mustRepaint |= m_xAxis.setSelected(false);
                mustRepaint |= m_yAxis.setSelected(false);
                mustRepaint |= m_yAxisRight.setSelected(!m_yAxisRight.isSelected());
            }
            if (mustRepaint) {
                repaint();
            }
        }

        if (m_moveGesture.isMoving()) {

            int modifier = e.getModifiersEx();
            boolean isCtrlOrShiftDown = ((modifier & (InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK)) != 0);

            m_moveGesture.stopMoving(x, y, isCtrlOrShiftDown);
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            m_updateDoubleBuffer = true;
            mustRepaint = true;
        } else if (m_selectionGesture.isSelecting()) {
            m_selectionGesture.stopSelection(x, y);

            int modifier = e.getModifiersEx();
            boolean isCtrlOrShiftDown = ((modifier & (InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK)) != 0);

            int action = m_selectionGesture.getAction();
            if (action == SelectionGestureLasso.ACTION_CLICK) {
                Point p = m_selectionGesture.getClickPoint();
                double valueX = m_xAxis.pixelToValue(p.x);
                double valueY = m_yAxis.pixelToValue(p.y);

                if (getXAxis().hasPlots()) {
                    getXAxis().getPlots().get(0).select(valueX, valueY, isCtrlOrShiftDown);
                }
                m_updateDoubleBuffer = true;
                m_viewAllPanel.updateDoubleBuffer();
                mustRepaint = true;
            } else if (action == SelectionGestureLasso.ACTION_SURROUND) {

                Path2D.Double path = new Path2D.Double();

                Polygon p = m_selectionGesture.getSelectionPolygon();
                double valueX = m_xAxis.pixelToValue(p.xpoints[0]);
                double valueY = m_yAxis.pixelToValue(p.ypoints[0]);
                path.moveTo(valueX, valueY);
                for (int i = 1; i < p.npoints; i++) {
                    valueX = m_xAxis.pixelToValue(p.xpoints[i]);
                    valueY = m_yAxis.pixelToValue(p.ypoints[i]);
                    path.lineTo(valueX, valueY);
                }
                path.closePath();

                double xMin = m_xAxis.pixelToValue(m_selectionGesture.getMinX());
                double xMax = m_xAxis.pixelToValue(m_selectionGesture.getMaxX());
                double yMin = m_yAxis.pixelToValue(m_selectionGesture.getMaxY()); // inversion of min max is normal between pixel and Y Axis
                double yMax = m_yAxis.pixelToValue(m_selectionGesture.getMinY());

                if (getXAxis().hasPlots()) {
                    getXAxis().getPlots().get(0).select(path, xMin, xMax, yMin, yMax, isCtrlOrShiftDown);
                }
                m_updateDoubleBuffer = true; 
                m_viewAllPanel.updateDoubleBuffer();
                mustRepaint = true;
            }

        } else if (m_zoomGesture.isZooming()) {
            m_zoomGesture.stopZooming(e.getX(), e.getY());

            int action = m_zoomGesture.getAction();
            if (action == ZoomGesture.ACTION_ZOOM) {
                
                double oldMinX = m_xAxis.getMinValue();
                double oldMaxX = m_xAxis.getMaxValue();
                double oldMinY = m_yAxis.getMinValue();
                double oldMaxY = m_yAxis.getMaxValue();
                boolean isBasePlot = m_zoomGesture.isBasePlot();

                if (!isBasePlot) {
                    // first unzoom if the zoom is on the viewAllMap
                    double[] resMinMax = getMinMaxPlots(m_xAxis);
                    m_xAxis.setRange(resMinMax[0], resMinMax[1]);
                    
                    resMinMax = getMinMaxPlots(m_yAxis);
                    m_yAxis.setRange(resMinMax[0], resMinMax[1]);
                    
                    if (m_yAxisRight.hasPlots()) {
                        resMinMax = getMinMaxPlots(m_yAxisRight);
                        m_yAxisRight.setRange(resMinMax[0], resMinMax[1]);
                    }
                }

                int startX = isBasePlot ? m_zoomGesture.getStartX() : m_viewAllPanel.convertToBasePlotX(m_zoomGesture.getStartX());
                int endX = isBasePlot ? m_zoomGesture.getEndX() : m_viewAllPanel.convertToBasePlotX(m_zoomGesture.getEndX());
                int startY = isBasePlot ? m_zoomGesture.getStartY() : m_viewAllPanel.convertToBasePlotY(m_zoomGesture.getStartY());
                int endY = isBasePlot ? m_zoomGesture.getEndY() : m_viewAllPanel.convertToBasePlotY(m_zoomGesture.getEndY());
                m_xAxis.setRange(m_xAxis.pixelToValue(startX), m_xAxis.pixelToValue(endX), true);
                m_yAxis.setRange(m_yAxis.pixelToValue(endY), m_yAxis.pixelToValue(startY), true);
                m_yAxisRight.setRange(m_yAxisRight.pixelToValue(endY), m_yAxisRight.pixelToValue(startY), true);
                m_updateDoubleBuffer = true;
                fireUpdateAxisRange(oldMinX, oldMaxX, m_xAxis.getMinValue(), m_xAxis.getMaxValue(), oldMinY, oldMaxY, m_yAxis.getMinValue(), m_yAxis.getMaxValue());

            } else if (action == ZoomGesture.ACTION_UNZOOM) {
                if (getXAxis().hasPlots()) {
                    double oldMinX = m_xAxis.getMinValue();
                    double oldMaxX = m_xAxis.getMaxValue();
                    double oldMinY = m_yAxis.getMinValue();
                    double oldMaxY = m_yAxis.getMaxValue();
                    updateAxis(getXAxis().getPlots().get(0));
                    fireUpdateAxisRange(oldMinX, oldMaxX, m_xAxis.getMinValue(), m_xAxis.getMaxValue(), oldMinY, oldMaxY, m_yAxis.getMinValue(), m_yAxis.getMaxValue());
                    // after a view all, axis have not been zoomed by the user
                    getXAxis().setRangeModifiedByUser(false);
                    getYAxis().setRangeModifiedByUser(false);
                    if (m_yAxisRight != null) {
                        m_yAxisRight.setRangeModifiedByUser(false);
                    }
                }
                m_updateDoubleBuffer = true;
            } else if (e.isPopupTrigger()) { // action == ZoomGesture.ACTION_NONE
                // WART, in fact we are not zooming, so if it is the right mouse button,
                // the user wants the popup menu
                if (getXAxis().hasPlots()) {
                    PlotBaseAbstract plot = getXAxis().getPlots().get(0);
                    double xValue = m_xAxis.pixelToValue(e.getX());
                    double yValue = m_yAxis.pixelToValue(e.getY());

                    JPopupMenu popup = plot.getPopupMenu(xValue, yValue);
                    if (popup != null) {
                        popup.show((JComponent) e.getSource(), e.getX(), e.getY());
                    }
                }
            }

            mustRepaint = true;

        } else if (m_panAxisGesture.isPanning()) {
            m_panAxisGesture.stopPanning(e.getX(), e.getY());
        }

        if (mustRepaint) {
            repaint();
        }
    }

    public void repaintUpdateDoubleBuffer() {
        m_updateDoubleBuffer = true;
        repaint();
    }

    public void zoomIn() {
        zoom(true, true, true, getYAxisRight().hasPlots(), null, null);
    }

    public void zoomOut() {
        zoom(false, true, true, getYAxisRight().hasPlots(), null, null);
    }

    /**
     * 
     * @param zoomIn Direction of the zoom
     * @param zoomX zoom along X
     * @param zoomY zoom along Y
     * @param zoomYRight zoom along second Y Axis at right
     * @param fixedCoordinateX zoom around a fixed X coordinate
     * @param fixedCoordinateY zoom around a fixed Y coordinate
     */
    private void zoom(boolean zoomIn, boolean zoomX, boolean zoomY, boolean zoomYRight, Integer fixedCoordinateX, Integer fixedCoordinateY) {
        if (!getXAxis().hasPlots()) {
            return;
        }

        if (!zoomIn && zoomX && m_xAxis.isMaxZoomOutReached()) {
            return;
        }

        if (!zoomIn && zoomY && m_yAxis.isMaxZoomOutReached()) {
            return;
        }


        int zoomDirection = (zoomIn) ? -1 : 1;
        
        
        
        double fixedX = 0;
        double fixedY = 0;
        double fixedYRight = 0;
        if (fixedCoordinateX != null) {
            if (zoomX) {
                fixedX = m_xAxis.pixelToValue(fixedCoordinateX);
            }
        }
        if (fixedCoordinateY != null) {
            if (zoomY) {
                fixedY = m_yAxis.pixelToValue(fixedCoordinateY);
            }
            if (zoomYRight) {
                fixedYRight = m_yAxisRight.pixelToValue(fixedCoordinateY);
            }
        }

        
        double oldMinX = m_xAxis.getMinValue();
        double oldMaxX = m_xAxis.getMaxValue();
        if (zoomX) {
            double newXmin;
            double newXmax;
            boolean doZoom = true;
            if (m_xAxis.isLog()) {
                double factorMin;
                double factorMax;
                if (zoomDirection == 1) {
                    factorMax = 5;
                    factorMin = 1d/5;
                } else {
                    factorMin = 5;
                    factorMax = 1d/5;
                }
                newXmin = oldMinX * factorMin;
                newXmax = oldMaxX * factorMax;
                doZoom = (newXmax/newXmin)>=2; // avoid to zoom too much, in log it can lead to infinite loop
                
                
            } else {
                double factor = 0.20 * zoomDirection;
                
                double xValue = (oldMinX + oldMaxX) / 2;
                newXmin = oldMinX + (oldMinX - xValue) * factor;
                newXmax = oldMaxX - (xValue - oldMaxX) * factor;
            }
           
            if (doZoom) {
                m_xAxis.setRange(newXmin, newXmax, true);
                if (fixedCoordinateX != null) {
                    if (m_xAxis.isLog()) {
                        int coordinateNowX = m_xAxis.valueToPixel(fixedX);
                        double mult = m_xAxis.deltaPixelToLogMultValue(fixedCoordinateX - coordinateNowX);
                        m_xAxis.setRange(m_xAxis.getMinValue() / mult, m_xAxis.getMaxValue() / mult, true);
                    } else {
                        double valueTranslated = m_xAxis.pixelToValue(fixedCoordinateX);
                        m_xAxis.setRange(newXmin + (fixedX - valueTranslated), newXmax + (fixedX - valueTranslated), true);
                    }
                }
            }
        }
        
        double oldMinY = m_yAxis.getMinValue();
        double oldMaxY = m_yAxis.getMaxValue();
        if (zoomY) {
            double newYmin;
            double newYmax;
            boolean doZoom = true;
            if (m_yAxis.isLog()) {
                double factorMin;
                double factorMax;
                if (zoomDirection == 1) {
                    factorMax = 5;
                    factorMin = 1d/5;
                } else {
                    factorMin = 5;
                    factorMax = 1d/5;
                }
                newYmin = oldMinY * factorMin;
                newYmax = oldMaxY * factorMax;
                doZoom = (newYmax/newYmin)>=2; // avoid to zoom too much, in log it can lead to infinite loop
                
            } else {
                double factor = 0.20 * zoomDirection;
                
                double yValue = (oldMinY + oldMaxY) / 2;
                newYmin = oldMinY + (oldMinY - yValue) * factor;
                newYmax = oldMaxY - (yValue - oldMaxY) * factor;
            }
            if (doZoom) {
                m_yAxis.setRange(newYmin, newYmax, true);
                if (fixedCoordinateY != null) {
                    if (m_yAxis.isLog()) {
                        int coordinateNowY = m_yAxis.valueToPixel(fixedY);
                        double mult = m_yAxis.deltaPixelToLogMultValue(coordinateNowY - fixedCoordinateY);
                        m_yAxis.setRange(m_yAxis.getMinValue() / mult, m_yAxis.getMaxValue() / mult, true);
                    } else {
                        double valueTranslated = m_yAxis.pixelToValue(fixedCoordinateY);
                        m_yAxis.setRange(newYmin + (fixedY - valueTranslated), newYmax + (fixedY - valueTranslated), true);
                    }
                }
            }
            
        }

        double oldMinYRight = m_yAxisRight.getMinValue();
        double oldMaxYRight = m_yAxisRight.getMaxValue();
        if (zoomYRight) {
            double newYmin;
            double newYmax;
            boolean doZoom = true;
            if (m_yAxisRight.isLog()) {
                double factorMin;
                double factorMax;
                if (zoomDirection == 1) {
                    factorMax = 5;
                    factorMin = 1d/5;
                } else {
                    factorMin = 5;
                    factorMax = 1d/5;
                }
                newYmin = oldMinYRight * factorMin;
                newYmax = oldMaxYRight * factorMax;
                doZoom = (newYmax/newYmin)>=2; // avoid to zoom too much, in log it can lead to infinite loop
                
                
            } else {
                double factor = 0.20 * zoomDirection;
                
                double yValue = (oldMinYRight + oldMaxYRight) / 2;
                newYmin = oldMinYRight + (oldMinYRight - yValue) * factor;
                newYmax = oldMaxYRight - (yValue - oldMaxYRight) * factor;
            }
            
            if (doZoom) {
                m_yAxisRight.setRange(newYmin, newYmax, true);

                if (fixedCoordinateY != null) {
                    if (m_yAxis.isLog()) {
                        int coordinateNowY = m_yAxisRight.valueToPixel(fixedYRight);
                        double mult = m_yAxisRight.deltaPixelToLogMultValue(coordinateNowY - fixedCoordinateY);
                         m_yAxisRight.setRange(m_yAxisRight.getMinValue() / mult, m_yAxisRight.getMaxValue() / mult, true);
                    } else {
                        double valueTranslated = m_yAxisRight.pixelToValue(fixedCoordinateY);
                        m_yAxisRight.setRange(newYmin + (fixedYRight - valueTranslated), newYmax + (fixedYRight - valueTranslated), true);
                    }
                }
            }
        }

        
        repaintUpdateDoubleBuffer();

        fireUpdateAxisRange(oldMinX, oldMaxX, m_xAxis.getMinValue(), m_xAxis.getMaxValue(), oldMinY, oldMaxY, m_yAxis.getMinValue(), m_yAxis.getMaxValue());

    }

    public void viewAll() {
        for (PlotBaseAbstract plot : getXAxis().getPlots()) {
            updateAxis(plot.getDoubleBufferingPolicy());
        }
        
        // after a view all, axis have not been zoomed by the user
        getXAxis().setRangeModifiedByUser(false);
        getYAxis().setRangeModifiedByUser(false);
        if (m_yAxisRight != null) {
            m_yAxisRight.setRangeModifiedByUser(false);
        }
        
        repaint();
    }

    public void addListener(PlotPanelListener listener) {
        m_listeners.add(listener);
    }

    private void fireMouseClicked(MouseEvent e, double xValue, double yValue) {
        // Notify 
        for (PlotPanelListener l : m_listeners) {
            l.plotPanelMouseClicked(e, xValue, yValue);
        }
    }

    private void fireUpdateAxisRange(double oldMinX, double oldMaxX, double newMinX, double newMaxX, double oldMinY, double oldMaxY, double newMinY, double newMaxY) {
        // Notify 
        double[] oldX = new double[2];
        oldX[0] = oldMinX;
        oldX[1] = oldMaxX;
        double[] newX = new double[2];
        newX[0] = newMinX;
        newX[1] = newMaxX;
        double[] oldY = new double[2];
        oldY[0] = oldMinY;
        oldY[1] = oldMaxY;
        double[] newY = new double[2];
        newY[0] = newMinY;
        newY[1] = newMaxY;
        for (PlotPanelListener l : m_listeners) {
            l.updateAxisRange(oldX, newX, oldY, newY);
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
        if (m_drawCursor) {
            m_coordX = "";
            m_coordY = "";
            repaint();
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (m_drawCursor) {
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }

        if (m_moveGesture.isMoving()) {
            m_moveGesture.move(e.getX(), e.getY());
            repaint();
        } else if (m_selectionGesture.isSelecting()) {
            m_selectionGesture.continueSelection(e.getX(), e.getY());
            repaint();
        } else if (m_zoomGesture.isZooming()) {
            m_zoomGesture.moveZooming(e.getX(), e.getY());
            repaint();
        } else if ((m_xAxis != null) && (m_yAxis != null) && (m_panAxisGesture.isPanning())) {
            double oldMinX = m_xAxis.getMinValue();
            double oldMaxX = m_xAxis.getMaxValue();
            double oldMinY = m_yAxis.getMinValue();
            double oldMaxY = m_yAxis.getMaxValue();
            if (m_panAxisGesture.getPanningAxis() == PanAxisGesture.X_AXIS_PAN) {
                if (m_xAxis.isLog()) {
                    double mult = m_xAxis.deltaPixelToLogMultValue(e.getX() - m_panAxisGesture.getPreviousX());
                    m_xAxis.setRange(m_xAxis.getMinValue() / mult, m_xAxis.getMaxValue() / mult, true);
                } else {
                    double delta = m_xAxis.deltaPixelToDeltaValue(m_panAxisGesture.getPreviousX() - e.getX());
                    m_xAxis.setRange(m_xAxis.getMinValue() + delta, m_xAxis.getMaxValue() + delta, true);
                }
                fireUpdateAxisRange(oldMinX, oldMaxX, m_xAxis.getMinValue(), m_xAxis.getMaxValue(), oldMinY, oldMaxY, m_yAxis.getMinValue(), m_yAxis.getMaxValue());
            } else if (m_panAxisGesture.getPanningAxis() == PanAxisGesture.Y_AT_RIGHT_AXIS_PAN) {
                 if (m_yAxisRight.isLog()) {
                    double mult = m_yAxisRight.deltaPixelToLogMultValue(m_panAxisGesture.getPreviousY()-e.getY());
                    m_yAxisRight.setRange(m_yAxisRight.getMinValue()/mult, m_yAxisRight.getMaxValue()/mult, true);
                } else {
                    double delta = m_yAxisRight.deltaPixelToDeltaValue(m_panAxisGesture.getPreviousY()-e.getY());
                    m_yAxisRight.setRange(m_yAxisRight.getMinValue() + delta, m_yAxisRight.getMaxValue() + delta, true);       
                }
            } else {
                if (m_yAxis.isLog()) {
                    double mult = m_yAxis.deltaPixelToLogMultValue(m_panAxisGesture.getPreviousY() - e.getY());
                    m_yAxis.setRange(m_yAxis.getMinValue() / mult, m_yAxis.getMaxValue() / mult, true);
                } else {
                    double delta = m_yAxis.deltaPixelToDeltaValue(m_panAxisGesture.getPreviousY() - e.getY());
                    m_yAxis.setRange(m_yAxis.getMinValue() + delta, m_yAxis.getMaxValue() + delta, true);
                }
                fireUpdateAxisRange(oldMinX, oldMaxX, m_xAxis.getMinValue(), m_xAxis.getMaxValue(), oldMinY, oldMaxY, m_yAxis.getMinValue(), m_yAxis.getMaxValue());
            }
            m_panAxisGesture.movePan(e.getX(), e.getY());
            m_updateDoubleBuffer = true;
            repaint();
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (!getXAxis().hasPlots() || e == null) {
            m_coordX = "";
            m_coordY = "";
            if (m_drawCursor) {
                repaint();
            }
            return;
        }

        if ((m_xAxis == null) || (m_yAxis == null)) {
            return;
        }

        int mouseX = e.getX();
        int mouseY = e.getY();

        double xValue = m_xAxis.pixelToValue(mouseX);
        
        boolean isInGridArea = (mouseX <= m_plotArea.width + m_plotArea.x && mouseX >= m_plotArea.x && mouseY >= m_plotArea.y && mouseY <= m_plotArea.height + m_plotArea.y);
        if (!isInGridArea) {
            m_coordX = "";
            m_coordY = "";
            if (m_xAxis.inside(mouseX, mouseY)) {
                setCursor(new Cursor(Cursor.W_RESIZE_CURSOR));
            } else if (m_yAxis.inside(mouseX, mouseY)) { //JPM.TODO.RIGHT?
                setCursor(new Cursor(Cursor.N_RESIZE_CURSOR));
            } else {
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        } else {
            
            double yValue = m_yAxis.pixelToValue(mouseY);
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            if (m_drawCursor) {
                setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
                if ((xValue > -0.1 && xValue < 0.1) || xValue >= 10000 || xValue <= -10000) {
                    m_coordX = formatE.format(xValue);
                } else {
                    m_coordX = format.format(xValue);
                }
                if ((yValue > -0.1 && yValue < 0.1) || yValue >= 10000 || yValue <= -10000) {
                    m_coordY = formatE.format(yValue);
                } else {
                    m_coordY = format.format(yValue);
                }
                m_posx = mouseX;
                m_posy = mouseY;
            }
        }

        int nbPlotTooltip = 0;
        boolean repaintNeeded = false;
        //tooltips on yAxis 
        for (PlotBaseAbstract plot : getYAxis().getPlots()) {

            double yValue = m_yAxis.pixelToValue(mouseY);
            
            boolean isPlotSelected = isInGridArea && plot.isMouseOnPlot(xValue, yValue);
            //by mouseMoved, if isPlotSelected, then plot paint a marker effet
            repaintNeeded |= plot.setIsPaintMarker(isPlotSelected);

            String toolTipForPlot = isInGridArea ? plot.getToolTipText(xValue, yValue) : null;

            if (toolTipForPlot != null && !toolTipForPlot.isEmpty()) {
                if (nbPlotTooltip == 0) {
                    m_sbTooltip.append("<html>");
                }

                if (nbPlotTooltip < 3) {
                    m_sbTooltip.append(toolTipForPlot);
                    m_sbTooltip.append("<br/>");
                }

                nbPlotTooltip++;
            }

        }
        for (PlotBaseAbstract plot : getYAxisRight().getPlots()) {

            double yValue = getYAxisRight().pixelToValue(mouseY);
            
            boolean isPlotSelected = isInGridArea && plot.isMouseOnPlot(xValue, yValue);
            //by mouseMoved, if isPlotSelected, then plot paint a marker effet
            repaintNeeded |= plot.setIsPaintMarker(isPlotSelected);

            String toolTipForPlot = isInGridArea ? plot.getToolTipText(xValue, yValue) : null;

            if (toolTipForPlot != null && !toolTipForPlot.isEmpty()) {
                if (nbPlotTooltip == 0) {
                    m_sbTooltip.append("<html>");
                }

                if (nbPlotTooltip < 3) {
                    m_sbTooltip.append(toolTipForPlot);
                    m_sbTooltip.append("<br/>");
                }

                nbPlotTooltip++;
            }

        }
        if (nbPlotTooltip >= 3) {
            m_sbTooltip.append("[...]");
        }

        if (nbPlotTooltip > 0) {
            m_sbTooltip.append("</html>");
            setToolTipText(m_sbTooltip.toString());
            m_sbTooltip.setLength(0);
        } else {
            setToolTipText(null);
        }

        if (repaintNeeded) {
            repaintUpdateDoubleBuffer();
        } else if (m_drawCursor) {
            repaint();
        }

    }
    private final StringBuilder m_sbTooltip = new StringBuilder();

    private JPopupMenu createAxisPopup(final Axis axis, final YAxis secondAxis, int x, int y) {

        boolean isXAxis = axis.equals(m_xAxis);

        JPopupMenu popup = new JPopupMenu();
        popup.add(new LogAction(axis, isXAxis, secondAxis));
        popup.add(new TitleAction(axis, (isXAxis) ? null : m_yAxisRight));
        popup.add(new GridAction(axis.equals(m_xAxis)));
        popup.add(new RangeAction(axis, axis.equals(m_xAxis)));


        ArrayList<AbstractMeasurement> measurements = new ArrayList<>();
        for (PlotBaseAbstract plot : getXAxis().getPlots()) {
            plot.getMeasurements(measurements, isXAxis ? AbstractMeasurement.MeasurementType.X_AXIS_POPUP : AbstractMeasurement.MeasurementType.Y_AXIS_POPUP);
        }
        if (measurements.size() > 0) {
            popup.addSeparator();
            for (AbstractMeasurement measurement : measurements) {
                popup.add(new MeasurementAction(measurement, x, y));
            }
        }

        popup.addPopupMenuListener(new PopupMenuListener() {

            @Override
            public void popupMenuCanceled(PopupMenuEvent popupMenuEvent) {
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent popupMenuEvent) {
                axis.setSelected(false);
                repaint();
            }

            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent popupMenuEvent) {
            }
        });

        return popup;
    }

    @Override
    public String getEnumValueX(int index, boolean fromData) {
        if (!getXAxis().hasPlots()) {
            return null;
        }
        return getXAxis().getPlots().get(0).getEnumValueX(index, fromData);
    }

    @Override
    public String getEnumValueY(int index, boolean fromData, Axis axis) {
        if (!axis.hasPlots()) {
            return null;
        }
        return axis.getPlots().get(0).getEnumValueY(index, fromData, axis);
    }

    public double getNearestXData(double x) {
        if (!getXAxis().hasPlots()) {
            return x;
        }

        // look only to the first plot for the moment
        return getXAxis().getPlots().get(0).getNearestXData(x);
    }

    public double getNearestYData(double y) {
        if (!getXAxis().hasPlots()) {
            return y;
        }

        // look only to the first plot for the moment
        return getXAxis().getPlots().get(0).getNearestYData(y);
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (!getXAxis().hasPlots() || e == null) {
            return;
        }
        if (!getXAxis().getPlots().get(0).isMouseWheelSupported()) {
            return;
        }
        
        Integer fixedCoordinateX = null;
        Integer fixedCoordinateY = null;
        boolean zoomX = false;
        boolean zoomY = false;
        boolean zoomYRight  = false;
        
        int modifier = e.getModifiersEx();
        boolean isCtrlOrShiftDown = ((modifier & (InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK)) != 0);
            
        if (insidePlotArea(e.getX(), e.getY())) {
            zoomX = true;
            zoomY = true;
            zoomYRight = getYAxisRight().hasPlots();

            if (isCtrlOrShiftDown) {
                // zoom around the mouse
                fixedCoordinateX = e.getX();
                fixedCoordinateY = e.getY();
            }
        } else if ((m_xAxis != null) && m_xAxis.inside(e.getX(), e.getY())) {
            //mouse wheel move on Axis X
            zoomX = true;
            if (isCtrlOrShiftDown) {
                // zoom around the mouse
                fixedCoordinateX = e.getX();
            }
        } else if ((m_yAxis != null) && m_yAxis.inside(e.getX(), e.getY())) {//mouse wheel move on Axis Y
            zoomY = true;
            if (isCtrlOrShiftDown) {
                // zoom around the mouse
                fixedCoordinateY = e.getY();
            }
        } else if ((m_yAxisRight != null) && m_yAxisRight.inside(e.getX(), e.getY())) {//mouse wheel move on second Axis Y
            zoomYRight = true;
            if (isCtrlOrShiftDown) {
                // zoom around the mouse
                fixedCoordinateY = e.getY();
            }
        }
        
        zoom(e.getWheelRotation() < 0, zoomX, zoomY, zoomYRight, fixedCoordinateX, fixedCoordinateY);
        
    }

    public void setAxisXSpecificities(boolean isIntegerY, boolean isEnum, boolean isPixel) {
        XAxis axis = getXAxis();
        axis.setSpecificities(isIntegerY, isEnum, isPixel);
    }

    public void setAxisYSpecificities(boolean isIntegerY, boolean isEnum, boolean isPixel, PlotBaseAbstract plot) {
        YAxis yAxis = getYAxis();
        if (yAxis.hasPlot(plot)) {
            yAxis.setSpecificities(isIntegerY, isEnum, isPixel);
            return;
        } 
        
        yAxis = getYAxisRight();
        if (yAxis.hasPlot(plot)) { // should always be the case
            yAxis.setSpecificities(isIntegerY, isEnum, isPixel);
            return;
        }
    }

    public class MeasurementAction extends AbstractAction {

        private final AbstractMeasurement m_measurement;
        private int m_x;
        private int m_y;

        public MeasurementAction(AbstractMeasurement measurement, int x, int y) {
            super(measurement.getName());
            m_measurement = measurement;
            m_x = x;
            m_y = y;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            m_measurement.applyMeasurement(m_x, m_y);
            repaint();
        }

        @Override
        public boolean isEnabled() {
            return m_measurement.canApply();
        }
    }

    public class LogAction extends AbstractAction {

        private final Axis m_axis;
        private final boolean m_isXAxis;
        private final Axis m_secondAxis;

        public LogAction(Axis axis, boolean isXAxis, Axis secondAxis) {
            super(axis.isLog() ? "Linear Axis" : "Log10 Axis");
            m_axis = axis;
            m_isXAxis = isXAxis;
            m_secondAxis = secondAxis;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            m_axis.setLog(!m_axis.isLog());
            if (m_secondAxis != null) {
                m_secondAxis.setLog(m_axis.isLog());
            }
            m_updateDoubleBuffer = true;
            repaint();
        }

        @Override
        public boolean isEnabled() {
            return (m_axis.canBeInLog() && (m_isXAxis ? getXAxis().getPlots().get(0).canLogXAxis() : getXAxis().getPlots().get(0).canLogYAxis()));
        }
    }

    public class TitleAction extends AbstractAction {

        Axis m_axis1;
        Axis m_axis2;

        public TitleAction(Axis axis1, Axis axis2) {
            super(axis1.displayTitle() ? "Hide Title" : "Display Title");
            m_axis1 = axis1;
            m_axis2 = axis2;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            boolean displayTitle = !m_axis1.displayTitle();
            m_axis1.setDisplayTitle(displayTitle);
            if (m_axis2 != null) {
               m_axis2.setDisplayTitle(displayTitle); 
            }
            
            m_updateDoubleBuffer = true;
            repaint();
        }

    }
    
    public class GridAction extends AbstractAction {

        private final boolean m_xAxis;

        public GridAction(boolean xAxis) {
            super(xAxis ? (m_plotVerticalGrid ? "Remove Vertical Grid" : "Add Vertical Grid") : (m_plotHorizontalGrid ? "Remove Horizontal Grid" : "Add Horizontal Grid"));
            m_xAxis = xAxis;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (m_xAxis) {
                m_plotVerticalGrid = !m_plotVerticalGrid;
            } else { // y Axis
                m_plotHorizontalGrid = !m_plotHorizontalGrid;
            }
            if (m_plotToolbarListener != null) {
                m_plotToolbarListener.stateModified(PlotToolbarListenerInterface.BUTTONS.GRID);
            }
            m_updateDoubleBuffer = true;
            repaint();
        }

    }
    

    public class RangeAction extends AbstractAction {

        private final Axis m_axis;
        private final boolean m_isXAxis;

        public RangeAction(Axis axis, boolean isXAxis) {
            super("Set Visible Range");
            m_axis = axis;
            m_isXAxis = isXAxis;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Axis.AxisRangePanel panel = m_axis.getRangePanel();
            panel.setVisible(true);
            Point p = MouseInfo.getPointerInfo().getLocation();

            // TODO : pas sur que cette position soit à l'interieur du graphique. Le plus simple est de choisir en 
            //fonction de l'axe : en X : min (0, mouse.y - 20), en Y: min (mouse.x + 5, width)
            SwingUtilities.convertPointFromScreen(p, m_axis.getPlotPanel());
            if (m_isXAxis) {
                p.y = m_axis.getY() - panel.getHeight();
            }
            //ensure panel is inside the plot panel
            if ((p.x + panel.getWidth()) > getWidth()) {
                p.x -= (p.x + panel.getWidth()) - getWidth();
            }
            if (p.x < 0) {
                p.x = 0;
            }
            if ((p.y + panel.getHeight()) > getHeight()) {
                p.y -= (p.y + panel.getHeight()) - getHeight();
            }
            if (p.y < 0) {
                p.y = 0;
            }
            panel.setLocation(p.x, p.y);
        }

    }

    /*public class MapAction extends AbstractAction {

        private final BasePlotPanelViewAll m_viewAll;

        public MapAction(BasePlotPanelViewAll viewAll) {
            super(!viewAll.isDisplayed() ? "Display Map" : "Hide Map");
            m_viewAll = viewAll;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            m_viewAll.setDisplay(!m_viewAll.isDisplayed());
            
            if (m_plotToolbarListener != null) {
                m_plotToolbarListener.stateModified(PlotToolbarListenerInterface.BUTTONS.GRID);
            }
        }

    }*/

    public class FPSUtility {

        private double[] frames;
        private int index = 0;
        private long startTime;

        private FPSUtility(int size) {
            frames = new double[size];
            Arrays.fill(frames, -1.0);
        }

        private void startFrame() {
            startTime = System.currentTimeMillis();
        }

        private void stopFrame() {
            frames[index++] = System.currentTimeMillis() - startTime;
            index = index % frames.length;
        }

        public double getMeanTimePerFrame() {
            int count = 0;
            double sum = 0.0;
            for (double d : frames) {
                if (d > 0) {
                    count++;
                    sum += d;
                }
            }
            return sum / (double) count;
        }
    }

    private boolean insidePlotArea(int x, int y) {
        return this.insidePlotArea(x, y, m_xAxis, m_yAxis);
    }

    private boolean insidePlotArea(int x, int y, XAxis xAxis, YAxis yAxis) {
        int x1 = xAxis.valueToPixel(xAxis.getMinValue());
        int x2 = xAxis.valueToPixel(xAxis.getMaxValue());
        int y1 = yAxis.valueToPixel(yAxis.getMaxValue());
        int y2 = yAxis.valueToPixel(yAxis.getMinValue());
        return (x >= x1) && (x <= x2) && (y >= y1) && (y <= y2);
    }

    @Override
    public boolean isMoveable() {
        return true;
    }

    @Override
    public void snapToData(boolean isCtrlOrShiftDown) {
        // do nothing
    }

    @Override
    public void setSelected(boolean s, boolean isCtrlOrShiftDown) {
        // do nothing
    }

    @Override
    public boolean insideXY(int x, int y) {
        return insidePlotArea(x, y);
    }

    @Override
    public void moveDXY(int deltaX, int deltaY) {

        double oldMinX = m_xAxis.getMinValue();
        double oldMaxX = m_xAxis.getMaxValue();
        double oldMinY = m_yAxis.getMinValue();
        double oldMaxY = m_yAxis.getMaxValue();

        if (m_xAxis.isLog()) {
            double mult = m_xAxis.deltaPixelToLogMultValue(deltaX);
            m_xAxis.setRange(m_xAxis.getMinValue() / mult, m_xAxis.getMaxValue() / mult, true);
        } else {
            double delta = m_xAxis.deltaPixelToDeltaValue(deltaX);
            m_xAxis.setRange(m_xAxis.getMinValue() - delta, m_xAxis.getMaxValue() - delta, true);
        }

        if (m_yAxis.isLog()) {
            double mult = m_yAxis.deltaPixelToLogMultValue(-deltaY);
            m_yAxis.setRange(m_yAxis.getMinValue() / mult, m_yAxis.getMaxValue() / mult, true);
        } else {
            double delta = m_yAxis.deltaPixelToDeltaValue(-deltaY);
            m_yAxis.setRange(m_yAxis.getMinValue() + delta, m_yAxis.getMaxValue() + delta, true);
        }
        
        if ((m_yAxisRight!=null) && (m_yAxisRight.hasPlots())) {
            if (m_yAxisRight.isLog()) {
                double mult = m_yAxisRight.deltaPixelToLogMultValue(-deltaY);
                m_yAxisRight.setRange(m_yAxisRight.getMinValue() / mult, m_yAxisRight.getMaxValue() / mult, true);
            } else {
                double delta = m_yAxisRight.deltaPixelToDeltaValue(-deltaY);
                m_yAxisRight.setRange(m_yAxisRight.getMinValue() + delta, m_yAxisRight.getMaxValue() + delta, true);
            }
        }


        fireUpdateAxisRange(oldMinX, oldMaxX, m_xAxis.getMinValue(), m_xAxis.getMaxValue(), oldMinY, oldMaxY, m_yAxis.getMinValue(), m_yAxis.getMaxValue());

        m_updateDoubleBuffer = true;
        repaint();

    }
}
