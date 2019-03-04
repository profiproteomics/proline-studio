/*
 * @cea 
 * http://www.profiproteomics.fr
 * create date: 23 janv. 2019
 */
package fr.proline.studio.graphics;

import fr.proline.studio.utils.StringUtils;
import fr.proline.studio.graphics.cursor.AbstractCursor;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.ToolTipManager;
import javax.swing.SwingUtilities;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

/**
 * A Panel which has multiple layout, one layout has 1 Axis X/Y, and a groupe of
 * Plot
 *
 * @author Karine XUE
 */
public class DoubleYAxisPlotPanel extends BasePlotPanel {

    //private static final Logger m_logger = LoggerFactory.getLogger(DoubleYAxisPlotPanel.class);
    String m_secondYAxisTitle = "";
    private Color m_secondYAxisColor;

    public enum Layout {
        MAIN, SECOND
    };

    private HashMap<PlotBaseAbstract, Layout> m_plotAxisMap;
    private ArrayList<PlotBaseAbstract> m_mainPlots;
    private ArrayList<PlotBaseAbstract> m_secondPlots;

    private XAxis m_secondXAxis;
    private YAxis m_secondYAxis;

    /**
     * second XAxis min max
     */
    private double[] m_secondXBounds;
    /**
     * second Yaxis min max
     */
    private double[] m_secondYBounds;

    public DoubleYAxisPlotPanel() {
        super();
        m_plots = new ArrayList<PlotBaseAbstract>();
        m_mainPlots = new ArrayList<PlotBaseAbstract>();
        m_secondPlots = new ArrayList<PlotBaseAbstract>();
        m_plotAxisMap = new HashMap();
        m_secondXAxis = new XAxis(this);
        m_secondYAxis = new YAxis(this);
        m_secondYAxis.setLeftAxis();
        m_secondXBounds = new double[]{Double.NaN, Double.NaN};
        m_secondYBounds = new double[]{Double.NaN, Double.NaN};
        ToolTipManager.sharedInstance().registerComponent(this);
    }

    @Override
    public void addPlot(PlotXYAbstract plot) {
        this.addPlot(plot, Layout.MAIN);
    }

    public void addPlot(PlotXYAbstract plot, Layout l) {
        if (l == Layout.MAIN) {
            this.m_mainPlots.add(plot);
            this.m_plotAxisMap.put(plot, Layout.MAIN);

        } else if (l == Layout.SECOND) {
            this.m_secondPlots.add(plot);
            this.m_plotAxisMap.put(plot, Layout.SECOND);
        }
    }

    @Override
    public void clearPlots() {
        m_plots = new ArrayList();
        m_mainPlots = new ArrayList();
        m_secondPlots = new ArrayList();
        this.m_plotAxisMap = new HashMap();
    }

    @Override
    public void setPlot(PlotBaseAbstract plot) {
        clearPlots();
        this.m_mainPlots.add(plot);
        this.m_plotAxisMap.put(plot, Layout.MAIN);
    }

    public void setSecondAxisPlotInfo(String title, Color color) {
        m_secondYAxisTitle = title;
        m_secondYAxisColor = color;
    }

    
    @Override
    public void setAxisYSpecificities(boolean isIntegerY, boolean isEnum, boolean isPixel, PlotBaseAbstract plot) {
        if (m_mainPlots.contains(plot)) {
            this.m_yAxis.setSpecificities(isIntegerY, isEnum, isPixel);
        } else {
            this.m_secondYAxis.setSpecificities(isIntegerY, isEnum, isPixel);
        }
    }

    public void preparePaint() {
        updateAxis(m_mainPlots, m_xAxis, m_yAxis, m_xAxisBounds, m_yAxisBounds);
        updateAxis(m_secondPlots, m_secondXAxis, m_secondYAxis, m_secondXBounds, m_secondYBounds); //suppose only one plot use seconde Y Axis
        this.m_plots = new ArrayList();
        for (Object obj : this.m_plotAxisMap.keySet()) {
            this.m_plots.add((PlotBaseAbstract) obj);
        }
        m_secondYAxis.setTitle(m_secondYAxisTitle);
        repaint();
    }

    public void updatePlots(int[] cols, String parameterZ) {
        this.m_plotAxisMap = new HashMap();
        for (PlotBaseAbstract plot : m_mainPlots) {
            plot.update(cols, parameterZ);
            m_plotAxisMap.put(plot, Layout.MAIN);
        }
        for (PlotBaseAbstract plot : m_secondPlots) {
            plot.update(cols, parameterZ);
            m_plotAxisMap.put(plot, Layout.SECOND);
        }

    }

    @Override
    public void updateAxis(PlotBaseAbstract plot) {
        updateAxis(m_mainPlots, m_xAxis, m_yAxis, m_xAxisBounds, m_yAxisBounds);
        updateAxis(m_secondPlots, m_secondXAxis, m_secondYAxis, m_secondXBounds, m_secondYBounds); //suppose only one plot use seconde Y Axis
    }

    /**
     * from plot list, get the bounds of Axis X and Y,
     *
     * @param plot
     */
    public void updateAxis(ArrayList<PlotBaseAbstract> plotList,
            XAxis xAxis, YAxis yAxis, double[] xBounds, double[] yBounds) {
        if (xAxis == null || yAxis == null) {
            return;
        }
        this.m_isEnumAxisUpdated = false;
        double[] tab = getMinMaxPlots(plotList);//get Axis X, Y bounds, tab is doube[4]= [minX, maxX, minY, maxY]
        //xAxis.setLog(false);  // we do no longer change the log setting
        xAxis.setSelected(false);
        xAxis.setRange((Double.isNaN(xBounds[0])) ? tab[0] : xBounds[0], (Double.isNaN(xBounds[1])) ? tab[1] : xBounds[1]);

        //yAxis.setLog(false);  // we do no longer change the log setting
        yAxis.setSelected(false);
        yAxis.setRange((Double.isNaN(yBounds[0])) ? tab[2] : yBounds[0], (Double.isNaN(yBounds[1])) ? tab[3] : yBounds[1]);
        m_updateDoubleBuffer = true;
    }

    @Override
    protected void updateEnumAxis() {
        if (m_secondYAxis.isEnum()) {
            double y2Min = m_secondYAxis.getMinValue()-0.5;
            double y2Max = m_secondYAxis.getMaxValue()+0.5;
            m_secondYAxis.setRange(y2Min, y2Max);
        }
        super.updateEnumAxis();
    }

    @Override
    public String getEnumValueY(int index, boolean fromData, Axis axis) {
        if (axis == this.m_yAxis) {
            if ((m_mainPlots == null) || (m_mainPlots.isEmpty())) {
                return null;
            }
            return m_mainPlots.get(0).getEnumValueY(index, fromData);
        }
        if (axis == this.m_secondYAxis) {
            if ((m_secondPlots == null) || (m_secondPlots.isEmpty())) {
                return null;
            }
            return m_secondPlots.get(0).getEnumValueY(index, fromData);
        }
        return null;
    }
    @Override
    public void paint(Graphics g) {
        if (!m_isEnumAxisUpdated) {
            updateEnumAxis();
        }

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int xAxisX = GAP_FIGURES_Y + GAP_AXIS_TITLE + GAP_AXIS_LINE;
        int yAxisWidth = GAP_FIGURES_Y + GAP_AXIS_TITLE + GAP_AXIS_LINE;
        // height and width of the panel
        int width = getWidth() - yAxisWidth;
        int height = getHeight();
        int xAxisWidth = width - (xAxisX);

        // initialize plotArea
        m_plotArea.x = 0;
        m_plotArea.y = 0;

        m_plotArea.width = width;
        m_plotArea.height = height;
        g.setColor(PANEL_BACKGROUND_COLOR);
        g.fillRect(0, 0, width + yAxisWidth, height);
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

        //2 draw axis X
        //m_logger.debug("draw Axis");
        if (m_plots.size() != 0) {
            if ((m_xAxis != null) && (m_xAxis.displayAxis())) {
                int xAxisY = height - figuresXHeight - GAP_AXIS_TITLE/*-GAP_TOP_AXIS*/;
                int xAxisHeight = figuresXHeight + GAP_AXIS_TITLE + GAP_AXIS_LINE;
                // set default size
                m_xAxis.setSize(xAxisX, xAxisY, xAxisWidth, xAxisHeight);
                // prepare paint
                m_xAxis.preparePaint(g2d);
                // set correct size
                figuresXHeight = m_xAxis.getMiniMumAxisHeight(figuresXHeight);
                xAxisY = height - figuresXHeight - GAP_AXIS_TITLE;
                xAxisHeight = figuresXHeight + GAP_AXIS_TITLE + GAP_AXIS_LINE;
                m_xAxis.setSize(xAxisX, xAxisY, xAxisWidth, xAxisHeight);
                //then set size of the second Axis Y
                m_secondXAxis.setSize(xAxisX, xAxisY, xAxisWidth, xAxisHeight);

                //prepare plotArea begin point & width
                m_plotArea.x = m_xAxis.getX() + 1;
                m_plotArea.width = m_xAxis.getWidth();
                m_xAxis.paint(g2d);
                //m_logger.debug("___->paint main xAxis done");

            }
            //3.1 draw main axis Y
            int yAxisX = 0;
            int yAxisY = GAP_END_AXIS + titleY;

            int yAxisHeight = height - figuresXHeight - GAP_AXIS_TITLE - GAP_END_AXIS - titleY;
            if ((m_yAxis != null) && (m_yAxis.displayAxis())) {

                m_yAxis.setSize(yAxisX, yAxisY, yAxisWidth, yAxisHeight);
                // yAxis.setSize(0, GAP_END_AXIS + titleY, GAP_FIGURES_Y + GAP_AXIS_TITLE + GAP_AXIS_LINE/*+GAP_TOP_AXIS*/, height - figuresXHeight - GAP_AXIS_TITLE - GAP_END_AXIS - titleY);
                //prepare plotArea begin point & height
                m_plotArea.y = m_yAxis.getY() + 1;
                m_plotArea.height = m_yAxis.getHeight();
                m_yAxis.paint(g2d);
            }
            //3.2 draw second axis Y & paint a rectangle to indicate the plot color en 2nd Axis Y
            {
                if ((m_secondYAxis != null) && (m_secondYAxis.displayAxis())) {
                    //m_logger.debug("->seconde Axis Y");                    
                    m_secondYAxis.setSize(m_plotArea.width + yAxisWidth, yAxisY, yAxisWidth, yAxisHeight);
                    m_secondYAxis.paint(g2d);
                    int x = m_plotArea.x + m_plotArea.width + BasePlotPanel.GAP_FIGURES_Y;
                    int y = m_plotArea.y;
                    if (m_secondYAxisColor != null) {
                        g2d.setColor(m_secondYAxisColor);
                    }
                    g2d.fillRect(x, y, 10, 10);  //paint a rectangle to indicate the plot color en 2nd Axis Y
                    //m_logger.debug("<->seconde Axis Y");
                }
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
                        this.paintPlot(graphicBufferG2d);
                    }
                } else {
                    this.paintPlot(g2d);
                }
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

            if (this.m_drawCursor) {
                // show coord
                paintCoord(g2d);
            }

            g2d.setClip(previousClipping);

        }

    }

    protected void fireUpdateSecondAxisRange(double oldMinX, double oldMaxX, double newMinX, double newMaxX, double oldMinY, double oldMaxY, double newMinY, double newMaxY) {
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
//        for (PlotPanelListener l : m_listeners) {
//            l.updateAxisRange(oldX, newX, oldY, newY);
//        }
    }

    /**
     * paint each BasePlotPanel
     *
     * @param g2d
     * @param xAxis
     * @param yAxis
     */
    private void paintPlot(Graphics2D g2d) {
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
        Layout layout;
        for (PlotBaseAbstract plot : m_plots) {
            layout = m_plotAxisMap.get(plot);
            if (layout == Layout.MAIN) {
                plot.paint(g2d, m_xAxis, m_yAxis);
                //for each plot on main Axis, if it has over,, draw over, ,cursors
                plot.paintOver(g2d); //nerver used
                plot.paintMarkers(g2d);//markers
                plot.paintCursors(g2d);//cursors
            } else if (layout == Layout.SECOND) {
                plot.paint(g2d, m_xAxis, m_secondYAxis);
                plot.paintOver(g2d); //nerver used
            }

        }

        long stopPlotTime = System.currentTimeMillis();
        if (stopPlotTime - startPlotTime > 50) {
            // display is too slow , we use with buffered image
            m_useDoubleBuffering = true;
        }
        // m_logger.debug("__<->paintPlot");
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
            for (PlotBaseAbstract plot : m_plots) {
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
        XAxis xAxis = this.m_xAxis;
        YAxis yAxis = this.m_yAxis;
        if (m_drawCursor) {
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
        if (m_plots == null || m_plots.isEmpty()) {
            return;
        }
        int x = e.getX();
        int y = e.getY();
        if (m_plots.get(0).inside(x, y)) {
            // Mouse Pressed in the plot area

            // deselect axis
            if (xAxis != null) {
                xAxis.setSelected(false);
                m_secondXAxis.setSelected(false);
            }
            if (yAxis != null) {
                yAxis.setSelected(false);
                m_secondYAxis.setSelected(false);
            }

            if (SwingUtilities.isLeftMouseButton(e)) {
                // check if we are over a moveable object
                MoveableInterface movable = null;
                PlotBaseAbstract plotWithMovable = null;
                for (PlotBaseAbstract plot : m_plots) {
                    movable = plot.getOverMovable(x, y);
                    if (movable != null) {
                        plotWithMovable = plot;
                        break;
                    }
                }
                if (movable != null) {
                    m_moveGesture.startMoving(x, y, movable);
                    if (movable instanceof AbstractCursor) {
                        plotWithMovable.selectCursor((AbstractCursor) movable);
                    }
                    setCursor(new Cursor(Cursor.MOVE_CURSOR));
                } else {
                    m_selectionGesture.startSelection(x, y);
                }

            } else if (SwingUtilities.isRightMouseButton(e)) {
                m_zoomGesture.startZooming(x, y);
            }
            repaint();
        } else if (!SwingUtilities.isRightMouseButton(e) && m_xAxis != null && m_xAxis.inside(x, y)) {
            m_panAxisGesture.startPanning(x, y, PanAxisGesture.X_AXIS_PAN);
        } else if (!SwingUtilities.isRightMouseButton(e) && yAxis != null && yAxis.inside(x, y)) {
            m_panAxisGesture.startPanning(x, y, PanAxisGesture.Y_AXIS_PAN);
        } else if (!SwingUtilities.isRightMouseButton(e) && m_secondYAxis != null && m_secondYAxis.inside(x, y)) {
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
                m_secondYAxis.setSelected(false);
                JPopupMenu popup = createAxisPopup(m_xAxis, x, y);
                popup.show((JComponent) e.getSource(), x, y);
                mustRepaint = true;
            } else if (m_xAxis != null && m_yAxis != null && m_yAxis.inside(x, y)) {
                m_xAxis.setSelected(false);
                m_yAxis.setSelected(true);
                m_secondYAxis.setSelected(false);
                JPopupMenu popup = createAxisPopup(m_yAxis, x, y);
                popup.show((JComponent) e.getSource(), x, y);
                mustRepaint = true;
            }//trigger on m_secondYAxis is not allowed
            if (mustRepaint) {
                repaint();
            }
        } else if (!m_zoomGesture.isZooming() && !m_selectionGesture.isSelecting() && !m_moveGesture.isMoving() && (m_panAxisGesture.getAction() != PanAxisGesture.ACTION_PAN)) {
            //select axis action
            if (m_xAxis != null && m_xAxis.inside(x, y)) {//zoom on the X Axis
                mustRepaint |= m_xAxis.setSelected(!m_xAxis.isSelected());
                mustRepaint |= m_yAxis.setSelected(false);
                mustRepaint |= m_secondYAxis.setSelected(false);
            } else if (m_yAxis != null && m_yAxis.inside(x, y)) {//zoom on the Y Axis
                mustRepaint |= m_xAxis.setSelected(false);
                mustRepaint |= m_yAxis.setSelected(!m_yAxis.isSelected());
                mustRepaint |= m_secondYAxis.setSelected(false);
            } else if (m_secondYAxis != null && m_secondYAxis.inside(x, y)) {//zoom on the Y Axis
                mustRepaint |= m_xAxis.setSelected(false);
                mustRepaint |= m_yAxis.setSelected(false);
                mustRepaint |= m_secondYAxis.setSelected(!m_secondYAxis.isSelected());
            }

            if (mustRepaint) {
                repaint();
            }
        }

        if (m_moveGesture.isMoving()) {

            int modifier = e.getModifiers();
            boolean isCtrlOrShiftDown = ((modifier & (InputEvent.SHIFT_MASK | InputEvent.CTRL_MASK)) != 0);

            m_moveGesture.stopMoving(x, y, isCtrlOrShiftDown);
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            m_updateDoubleBuffer = true;
            mustRepaint = true;
        } else if (m_selectionGesture.isSelecting()) {//select work only at the 1st plot
            m_selectionGesture.stopSelection(x, y);

            int modifier = e.getModifiers();
            boolean isCtrlOrShiftDown = ((modifier & (InputEvent.SHIFT_MASK | InputEvent.CTRL_MASK)) != 0);

            int action = m_selectionGesture.getAction();
            if (action == SelectionGestureLasso.ACTION_CLICK) {//??@todo, for select 
                Point p = m_selectionGesture.getClickPoint();
                double valueX = m_xAxis.pixelToValue(p.x);
                double valueY = m_yAxis.pixelToValue(p.y);
                if (!m_plots.isEmpty()) {
                    m_plots.get(0).select(valueX, valueY, isCtrlOrShiftDown);
                }
                m_updateDoubleBuffer = true;
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

                if (!m_plots.isEmpty()) {
                    m_plots.get(0).select(path, xMin, xMax, yMin, yMax, isCtrlOrShiftDown);
                }
                m_updateDoubleBuffer = true;
                mustRepaint = true;
            }

        } else if (m_zoomGesture.isZooming()) {
            m_zoomGesture.stopZooming(e.getX(), e.getY());

            int action = m_zoomGesture.getAction();
            if (action == ZoomGesture.ACTION_ZOOM) {
                double oldMinX = m_xAxis.getMinValue();//*
                double oldMaxX = m_xAxis.getMaxValue();//*
                double oldMinY = m_yAxis.getMinValue();//*
                double oldMaxY = m_yAxis.getMaxValue();//*
                double oldMinY2 = m_secondYAxis.getMinValue();
                double oldMaxY2 = m_secondYAxis.getMaxValue();
                m_xAxis.setRange(m_xAxis.pixelToValue(m_zoomGesture.getStartX()), m_xAxis.pixelToValue(m_zoomGesture.getEndX()));
                m_yAxis.setRange(m_yAxis.pixelToValue(m_zoomGesture.getEndY()), m_yAxis.pixelToValue(m_zoomGesture.getStartY()));
                m_secondYAxis.setRange(m_secondYAxis.pixelToValue(m_zoomGesture.getEndY()), m_secondYAxis.pixelToValue(m_zoomGesture.getStartY()));
                //seconde yAxis setRange
                m_updateDoubleBuffer = true;
                // @todo next line to verify
                fireUpdateAxisRange(oldMinX, oldMaxX, m_xAxis.getMinValue(), m_xAxis.getMaxValue(), oldMinY, oldMaxY, m_yAxis.getMinValue(), m_yAxis.getMaxValue());
            } else if (action == ZoomGesture.ACTION_UNZOOM) {
                if (!m_plots.isEmpty()) {
                    double oldMinX = m_xAxis.getMinValue();
                    double oldMaxX = m_xAxis.getMaxValue();
                    double oldMinY = m_yAxis.getMinValue();
                    double oldMaxY = m_yAxis.getMaxValue();
                    double oldMinY2 = m_secondYAxis.getMinValue();
                    double oldMaxY2 = m_secondYAxis.getMaxValue();
                    //updateAxis(m_plots.get(0));
                    m_xAxis.setRange(m_xAxis.pixelToValue(m_zoomGesture.getStartX()), m_xAxis.pixelToValue(m_zoomGesture.getEndX()));
                    m_yAxis.setRange(m_yAxis.pixelToValue(m_zoomGesture.getEndY()), m_yAxis.pixelToValue(m_zoomGesture.getStartY()));
                    m_secondYAxis.setRange(m_secondYAxis.pixelToValue(m_zoomGesture.getEndY()), m_secondYAxis.pixelToValue(m_zoomGesture.getStartY()));
                    // @todo next line to verify
                    fireUpdateAxisRange(oldMinX, oldMaxX, m_xAxis.getMinValue(), m_xAxis.getMaxValue(), oldMinY, oldMaxY, m_yAxis.getMinValue(), m_yAxis.getMaxValue());
                }
                m_updateDoubleBuffer = true;
            } else if (e.isPopupTrigger()) { // action == ZoomGesture.ACTION_NONE
                // WART, in fact we are not zooming, so if it is the right mouse button,
                // the user wants the popup menu
                if (!m_plots.isEmpty()) {
                    PlotBaseAbstract plot = m_plots.get(0);
                    double xValue = m_xAxis.pixelToValue(e.getX());
                    double yValue;
//                    if (m_secondYAxis != null && m_secondYAxis.inside(x, y)) {
//                        yValue = this.m_secondYAxis.pixelToValue(x);
//                    } else {
                    yValue = m_yAxis.pixelToValue(e.getY());
//                    }
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
        } else if ((m_xAxis != null) && (m_yAxis != null) && (m_panAxisGesture.isPanning())) {//axis drag
            double oldMinX = m_xAxis.getMinValue();
            double oldMaxX = m_xAxis.getMaxValue();
            double oldMinY = m_yAxis.getMinValue();
            double oldMaxY = m_yAxis.getMaxValue();
            if (m_panAxisGesture.getPanningAxis() == PanAxisGesture.X_AXIS_PAN) {
                double delta = m_xAxis.pixelToValue(m_panAxisGesture.getPreviousX()) - m_xAxis.pixelToValue(e.getX());
                m_xAxis.setRange(m_xAxis.getMinValue() + delta, m_xAxis.getMaxValue() + delta);
                fireUpdateAxisRange(oldMinX, oldMaxX, m_xAxis.getMinValue(), m_xAxis.getMaxValue(), oldMinY, oldMaxY, m_yAxis.getMinValue(), m_yAxis.getMaxValue());
            } else if (m_panAxisGesture.getPanningAxis() == PanAxisGesture.Y_AT_RIGHT_AXIS_PAN) {
                double delta = m_secondYAxis.pixelToValue(m_panAxisGesture.getPreviousYAtRight()) - m_secondYAxis.pixelToValue(e.getY());
                m_secondYAxis.setRange(m_secondYAxis.getMinValue() + delta, m_secondYAxis.getMaxValue() + delta);
            } else {
                double delta = m_yAxis.pixelToValue(m_panAxisGesture.getPreviousY()) - m_yAxis.pixelToValue(e.getY());
                m_yAxis.setRange(m_yAxis.getMinValue() + delta, m_yAxis.getMaxValue() + delta);
                fireUpdateAxisRange(oldMinX, oldMaxX, m_xAxis.getMinValue(), m_xAxis.getMaxValue(), oldMinY, oldMaxY, m_yAxis.getMinValue(), m_yAxis.getMaxValue());
            }
            m_panAxisGesture.movePan(e.getX(), e.getY());
            m_updateDoubleBuffer = true;
            repaint();
        }
    }

//    @Override
//    public void mouseMoved(MouseEvent e) { //same
//      super.mouseMoved(e);
//    }
    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (m_plots == null || m_plots.isEmpty() || e == null) {
            return;
        }
        if (!m_plots.get(0).isMouseWheelSupported()) {
            return;
        }

        double oldMinX = m_xAxis.getMinValue();
        double oldMaxX = m_xAxis.getMaxValue();
        double oldMinY = m_yAxis.getMinValue();
        double oldMaxY = m_yAxis.getMaxValue();
        double oldMinY2 = m_secondYAxis.getMinValue();
        double oldMaxY2 = m_secondYAxis.getMaxValue();
        double factor = 0.20;
        double xValue = m_xAxis.pixelToValue(e.getX());
        double yValue = m_yAxis.pixelToValue(e.getY());
        double y2Value = m_secondYAxis.pixelToValue(e.getY());
        double newXmin = m_xAxis.getMinValue() + (m_xAxis.getMinValue() - xValue) * factor * e.getWheelRotation();
        double newXmax = m_xAxis.getMaxValue() - (xValue - m_xAxis.getMaxValue()) * factor * e.getWheelRotation();
        double newYmin = m_yAxis.getMinValue() + (m_yAxis.getMinValue() - yValue) * factor * e.getWheelRotation();
        double newYmax = m_yAxis.getMaxValue() - (yValue - m_yAxis.getMaxValue()) * factor * e.getWheelRotation();
        double newMinY2 = m_secondYAxis.getMinValue() + (m_secondYAxis.getMinValue() - yValue) * factor * e.getWheelRotation();
        double newMaxY2 = m_secondYAxis.getMaxValue() - (y2Value - m_secondYAxis.getMaxValue()) * factor * e.getWheelRotation();
        if (m_plots.get(0).inside(e.getX(), e.getY())) {//mouse wheel move on m_plotArea
            m_xAxis.setRange(newXmin, newXmax);
            m_yAxis.setRange(newYmin, newYmax);
            m_secondYAxis.setRange(newMinY2, newMaxY2);
            repaintUpdateDoubleBuffer();
        } else if ((m_xAxis != null) && m_xAxis.inside(e.getX(), e.getY())) {//mouse wheel move on Axis X
            m_xAxis.setRange(newXmin, newXmax);
            repaintUpdateDoubleBuffer();
        } else if ((m_yAxis != null) && m_yAxis.inside(e.getX(), e.getY())) {//mouse wheel move on Axis Y
            m_yAxis.setRange(newYmin, newYmax);
            repaintUpdateDoubleBuffer();
        } else if ((m_secondYAxis != null) && m_secondYAxis.inside(e.getX(), e.getY())) {//mouse wheel move on second Axis Y
            m_secondYAxis.setRange(newMinY2, newMaxY2);
            repaintUpdateDoubleBuffer();
        }
        fireUpdateAxisRange(oldMinX, oldMaxX, m_xAxis.getMinValue(), m_xAxis.getMaxValue(), oldMinY, oldMaxY, m_yAxis.getMinValue(), m_yAxis.getMaxValue());
    }

}
