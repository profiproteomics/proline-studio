package fr.proline.studio.graphics;

import fr.proline.studio.graphics.Axis.EnumXInterface;
import fr.proline.studio.graphics.Axis.EnumYInterface;
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
import scala.Array;

/**
 * Panel to display data with an X and Y Axis
 *
 * @author JM235353
 */
public class BasePlotPanel extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener, EnumXInterface, EnumYInterface, SettingsInterface {

    private static final Logger m_logger = LoggerFactory.getLogger(BasePlotPanel.class);

    private static final Color PANEL_BACKGROUND_COLOR = UIManager.getColor("Panel.background");

    private XAxis m_xAxis = null;
    private YAxis m_yAxis = null;
    private double[] m_xAxisBounds = {Double.NaN, Double.NaN};
    private double[] m_yAxisBounds = {Double.NaN, Double.NaN};

    private ArrayList<PlotBaseAbstract> m_plots = null;

    private final ZoomGesture m_zoomGesture = new ZoomGesture();
    private final SelectionGestureLasso m_selectionGesture = new SelectionGestureLasso();
    private final PanAxisGesture m_panAxisGesture = new PanAxisGesture();
    private final MoveGesture m_moveGesture = new MoveGesture();

    public final static int GAP_FIGURES_Y = 30;
    public final static int GAP_FIGURES_X = 24;
    public final static int GAP_END_AXIS = 10;
    public final static int GAP_AXIS_TITLE = 20;
    public final static int GAP_AXIS_LINE = 5;

    private BufferedImage m_doubleBuffer = null;
    private boolean m_useDoubleBuffering = false;
    private boolean m_updateDoubleBuffer = false;

    private boolean m_plotHorizontalGrid = true;
    private boolean m_plotVerticalGrid = true;
    private boolean m_dataLocked = false;
    private boolean m_drawCursor = false;

    private PlotToolbarListener m_plotToolbarListener = null;
    private List<PlotPanelListener> m_listeners = new ArrayList<>();

    private final Rectangle m_plotArea = new Rectangle();

    // show coord.
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
    private final static Font titleFont = new Font("dialog", Font.BOLD, 11);
    private final static Color titleColor = Color.DARK_GRAY;

//    public final FPSUtility fps = new FPSUtility(20);
    //events
    private EventListenerList listenerList = new EventListenerList();

    public BasePlotPanel() {
        formatE.applyPattern("0.#####E0");
        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
        ToolTipManager.sharedInstance().registerComponent(this);
        m_plots = new ArrayList();
    }

    public void setPlotTitle(String title) {
        m_plotTitle = title;
    }

    public void enableButton(PlotToolbarListener.BUTTONS button, boolean v) {
        if (m_plotToolbarListener != null) {
            m_plotToolbarListener.enable(button, v);
        }
    }

    @Override
    public void paint(Graphics g) {
//        fps.startFrame();

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // height and width of the panel
        int width = getWidth();
        int height = getHeight();

        // initialize plotArea
        m_plotArea.x = 0;
        m_plotArea.y = 0;
        m_plotArea.width = width;
        m_plotArea.height = height;

        g.setColor(PANEL_BACKGROUND_COLOR);
        g.fillRect(0, 0, width, height);

        // title
        int titleY = 0;
        if (this.m_plotTitle != null) {
            int wt = StringUtils.lenghtOfString(m_plotTitle, getFontMetrics(titleFont));
            int titleX = (width - wt) / 2;
            int ascent = getFontMetrics(titleFont).getAscent();
            int descent = getFontMetrics(titleFont).getDescent();
            titleY = ascent;
            g.setFont(titleFont);
            g.setColor(titleColor);
            g.drawString(m_plotTitle, titleX, titleY);
            titleY += descent;
        }
        m_plotArea.y = titleY;

        g.setColor(Color.darkGray);

        int figuresXHeight = GAP_FIGURES_X;
        //double[] tab = getMinMaxPlots();

        if ((m_xAxis != null) && (m_xAxis.displayAxis())) {

            // set default size
            m_xAxis.setSize(GAP_FIGURES_Y + GAP_AXIS_TITLE + GAP_AXIS_LINE, height - figuresXHeight - GAP_AXIS_TITLE /*-GAP_TOP_AXIS*/, width - GAP_FIGURES_Y - GAP_AXIS_TITLE - GAP_END_AXIS, figuresXHeight + GAP_AXIS_TITLE + GAP_AXIS_LINE);

            // prepare paint
            m_xAxis.preparePaint(g2d);

            // set correct size
            figuresXHeight = m_xAxis.getMiniMumAxisHeight(figuresXHeight);
            m_xAxis.setSize(GAP_FIGURES_Y + GAP_AXIS_TITLE + GAP_AXIS_LINE, height - figuresXHeight - GAP_AXIS_TITLE /*-GAP_TOP_AXIS*/, width - GAP_FIGURES_Y - GAP_AXIS_TITLE - GAP_END_AXIS, figuresXHeight + GAP_AXIS_TITLE + GAP_AXIS_LINE);
            /*if ((m_xAxis.getMinValue() == tab[0] && m_xAxis.getMaxValue() == tab[1])) {
                double rightMargin = m_margins.right == 0 ? 0.0 : Math.abs(m_xAxis.pixelToValue(0) -  m_xAxis.pixelToValue(m_margins.right));
                double leftMargin =  m_margins.left == 0 ? 0.0 : Math.abs(m_xAxis.pixelToValue(0) -  m_xAxis.pixelToValue(m_margins.left));
                m_xAxis.setRange(tab[0] - leftMargin,tab[1] + rightMargin);
            }*/

            m_plotArea.x = m_xAxis.m_x + 1;
            m_plotArea.width = m_xAxis.m_width;
            m_xAxis.paint(g2d);

        }

        if ((m_yAxis != null) && (m_yAxis.displayAxis())) {

            m_yAxis.setSize(0, GAP_END_AXIS + titleY, GAP_FIGURES_Y + GAP_AXIS_TITLE + GAP_AXIS_LINE/*+GAP_TOP_AXIS*/, height - figuresXHeight - GAP_AXIS_TITLE - GAP_END_AXIS - titleY);
            /*if ((m_yAxis.getMinValue() == tab[2] && m_yAxis.getMaxValue() == tab[3])) {
                double topMargin = m_margins.top == 0 ? 0.0 : Math.abs(m_yAxis.pixelToValue(0) -  m_yAxis.pixelToValue(m_margins.top));
                double bottomMargin =  m_margins.bottom == 0 ? 0.0 : Math.abs(m_yAxis.pixelToValue(0) -  m_yAxis.pixelToValue(m_margins.bottom));
                m_yAxis.setRange(tab[2] - bottomMargin,tab[3] + topMargin);
            }*/
            m_plotArea.y = m_yAxis.m_y + 1;
            m_plotArea.height = m_yAxis.m_height;
            m_yAxis.paint(g2d);
        }

        if (m_plots != null) {
            if (m_plotArea.width >= 0 && m_plotArea.height >= 0) {
                if (m_useDoubleBuffering) {
                    boolean createDoubleBuffer = ((m_doubleBuffer == null) || (m_doubleBuffer.getWidth() != m_plotArea.width) || (m_doubleBuffer.getHeight() != m_plotArea.height));
                    if (createDoubleBuffer) {
                        m_doubleBuffer = new BufferedImage(m_plotArea.width, m_plotArea.height, BufferedImage.TYPE_INT_ARGB);
                    }
                    if (createDoubleBuffer || m_updateDoubleBuffer) {
                        Graphics2D graphicBufferG2d = (Graphics2D) m_doubleBuffer.getGraphics();
                        graphicBufferG2d.setColor(Color.white);
                        graphicBufferG2d.fillRect(0, 0, m_plotArea.width, m_plotArea.height);
                        graphicBufferG2d.translate(-m_plotArea.x, -m_plotArea.y);
                        if ((m_plotVerticalGrid) && (m_xAxis.displayAxis())) {
                            m_xAxis.paintGrid(graphicBufferG2d, m_plotArea.x, m_plotArea.width, m_plotArea.y, m_plotArea.height);
                        }

                        if ((m_plotHorizontalGrid) && (m_yAxis.displayAxis())) {
                            m_yAxis.paintGrid(graphicBufferG2d, m_plotArea.x, m_plotArea.width, m_plotArea.y, m_plotArea.height);
                        }

                        for (PlotBaseAbstract plot : m_plots) {
                            plot.paint(graphicBufferG2d);
                        }
                        m_updateDoubleBuffer = false;
                    }
                    g2d.drawImage(m_doubleBuffer, m_plotArea.x, m_plotArea.y, null);

                } else {

                    long startPlotTime = System.currentTimeMillis();
                    g.setColor(Color.white);
                    g.fillRect(m_plotArea.x, m_plotArea.y, m_plotArea.width, m_plotArea.height);
                    if ((m_plotVerticalGrid) && (m_xAxis != null) && (m_xAxis.displayAxis())) {
                        m_xAxis.paintGrid(g2d, m_plotArea.x, m_plotArea.width, m_plotArea.y, m_plotArea.height);
                    }

                    if ((m_plotHorizontalGrid) && (m_yAxis != null) && (m_yAxis.displayAxis())) {
                        m_yAxis.paintGrid(g2d, m_plotArea.x, m_plotArea.width, m_plotArea.y, m_plotArea.height);
                    }

                    for (PlotBaseAbstract plot : m_plots) {
                        plot.paint(g2d);
                    }
                    long stopPlotTime = System.currentTimeMillis();
                    if (stopPlotTime - startPlotTime > 50) {
                        // display is too slow , we use an image
                        m_useDoubleBuffering = true;
                    }
                }
            }

            for (PlotBaseAbstract plot : m_plots) {
                plot.paintOver(g2d);
            }
            for (PlotBaseAbstract plot : m_plots) {
                plot.paintMarkers(g2d);
            }
            for (PlotBaseAbstract plot : m_plots) {
                plot.paintCursors(g2d);
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

//        fps.stopFrame();
    }

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
        if (m_plots == null) {
            return null;
        }

        for (PlotBaseAbstract plot : m_plots) {
            return plot.getParameters();
        }
        return null;
    }

    @Override
    public void parametersChanged() {
        if (m_plots != null) {
            for (int i = 0; i < m_plots.size(); i++) {
                m_plots.get(i).parametersChanged();
            }
        }
        m_updateDoubleBuffer = true;
        repaint();
    }

    @Override
    public boolean parametersCanceled() {

        boolean repaintNeeded = false;
        if (m_plots != null) {
            for (int i = 0; i < m_plots.size(); i++) {
                repaintNeeded |= m_plots.get(i).parametersCanceled();
            }
        }
        if (repaintNeeded) {
            m_updateDoubleBuffer = true;
            repaint();
        }

        return repaintNeeded;
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
        double[] tab = getMinMaxPlots();
        m_xAxis.lockMinValue(tab[0]);

    }

    public void lockMinYValue() {
        double[] tab = getMinMaxPlots();
        m_yAxis.lockMinValue(tab[2]);

    }

    public void setDrawCursor(boolean drawCursor) {
        this.m_drawCursor = drawCursor;
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

    public void setPlot(PlotBaseAbstract plot) {
        m_plots = new ArrayList();
        m_plots.add(plot);

        updateAxis(plot);
    }

    public void clearPlots() {
        m_plots = new ArrayList();
    }

    public boolean hasPlots() {
        return m_plots.size() > 0;
    }

    public void addPlot(PlotXYAbstract plot) {
        m_plots.add(plot);

        updateAxis(plot);
    }

    public ArrayList<Long> getSelection() {
        if (!m_plots.isEmpty()) {
            return m_plots.get(0).getSelectedIds();
        } else {
            return null;
        }
    }

    public void setSelection(ArrayList<Long> selection) {
        if (!m_plots.isEmpty()) {
            m_plots.get(0).setSelectedIds(selection);
            m_updateDoubleBuffer = true;
            repaint();
        }
    }

    private double[] getMinMaxPlots() {
        double[] tab = new double[4];
        int nb = m_plots.size();
        if (m_plots != null && nb > 0) {
            double minX = m_plots.get(0).getXMin();
            double maxX = m_plots.get(0).getXMax();
            double minY = m_plots.get(0).getYMin();
            double maxY = m_plots.get(0).getYMax();
            for (int k = 1; k < nb; k++) {
                double plotXMin = m_plots.get(k).getXMin();
                double plotXMax = m_plots.get(k).getXMax();
                double plotYMin = m_plots.get(k).getYMin();
                double plotYMax = m_plots.get(k).getYMax();
                if (plotXMin != 0 && plotXMax != 0) {
                    if (minX == 0 && maxX == 0) {
                        minX = plotXMin;
                        maxX = plotXMax;
                    }
                    minX = Math.min(minX, plotXMin);
                    maxX = Math.max(maxX, plotXMax);
                }
                if (plotYMin != 0 && plotYMax != 0) {
                    if (minY == 0 && maxY == 0) {
                        minY = plotYMin;
                        maxY = plotYMax;
                    }
                    minY = Math.min(minY, plotYMin);
                    maxY = Math.max(maxY, plotYMax);
                }

            }
            tab[0] = minX;
            tab[1] = maxX;
            tab[2] = minY;
            tab[3] = maxY;
        }
        return tab;
    }

    public void updateAxis(PlotBaseAbstract plot) {
        double[] tab = getMinMaxPlots();

        XAxis xAxis = getXAxis();
        //xAxis.setLog(false);  // we do no longer change the log setting
        xAxis.setSelected(false);
        xAxis.setRange((Double.isNaN(m_xAxisBounds[0])) ? tab[0] : m_xAxisBounds[0], (Double.isNaN(m_xAxisBounds[1])) ? tab[1] : m_xAxisBounds[1]);

        YAxis yAxis = getYAxis();
        //yAxis.setLog(false);  // we do no longer change the log setting
        yAxis.setSelected(false);
        yAxis.setRange((Double.isNaN(m_yAxisBounds[0])) ? tab[2] : m_yAxisBounds[0], (Double.isNaN(m_yAxisBounds[1])) ? tab[3] : m_yAxisBounds[1]);

        m_updateDoubleBuffer = true;
        m_useDoubleBuffering = plot.getDoubleBufferingPolicy();
    }

    public void setXAxisTitle(String title) {
        if (m_xAxis == null) {
            return;
        }
        m_xAxis.setTitle(title);
    }

    public void setXAxisBounds(double min, double max) {
        m_xAxisBounds = new double[]{min, max};
        if (m_plots != null && !m_plots.isEmpty()) {
            updateAxis(m_plots.get(0));
        }
    }

    public double[] getXAxisBounds() {
        return new double[]{m_xAxisBounds[0], m_xAxisBounds[1]};
    }

    public double[] getYAxisBounds() {
        return new double[]{m_yAxisBounds[0], m_yAxisBounds[1]};
    }

    public void setYAxisBounds(double min, double max) {
        m_yAxisBounds = new double[]{min, max};
        if (m_plots != null && !m_plots.isEmpty()) {
            updateAxis(m_plots.get(0));
        }
    }

    public void setYAxisTitle(String title) {
        if (m_yAxis == null) {
            return;
        }
        m_yAxis.setTitle(title);
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
            if (m_xAxis != null) {
                m_xAxis.setSelected(false);
            }
            if (m_yAxis != null) {
                m_yAxis.setSelected(false);
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
        } else if (!SwingUtilities.isRightMouseButton(e) && m_yAxis != null && m_yAxis.inside(x, y)) {
            m_panAxisGesture.startPanning(x, y, PanAxisGesture.Y_AXIS_PAN);
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
                JPopupMenu popup = createAxisPopup(m_xAxis, x, y);
                popup.show((JComponent) e.getSource(), x, y);
                mustRepaint = true;
            } else if (m_xAxis != null && m_yAxis != null && m_yAxis.inside(x, y)) {
                m_xAxis.setSelected(false);
                m_yAxis.setSelected(true);
                JPopupMenu popup = createAxisPopup(m_yAxis, x, y);
                popup.show((JComponent) e.getSource(), x, y);
                mustRepaint = true;
            }
        } else if (!m_zoomGesture.isZooming() && !m_selectionGesture.isSelecting() && !m_moveGesture.isMoving() && (m_panAxisGesture.getAction() != PanAxisGesture.ACTION_PAN)) {

            if (m_xAxis != null && m_xAxis.inside(x, y)) {
                mustRepaint |= m_xAxis.setSelected(!m_xAxis.isSelected());
                mustRepaint |= m_yAxis.setSelected(false);
            } else if (m_yAxis != null && m_yAxis.inside(x, y)) {
                mustRepaint |= m_xAxis.setSelected(false);
                mustRepaint |= m_yAxis.setSelected(!m_yAxis.isSelected());
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
        } else if (m_selectionGesture.isSelecting()) {
            m_selectionGesture.stopSelection(x, y);

            int modifier = e.getModifiers();
            boolean isCtrlOrShiftDown = ((modifier & (InputEvent.SHIFT_MASK | InputEvent.CTRL_MASK)) != 0);

            int action = m_selectionGesture.getAction();
            if (action == SelectionGestureLasso.ACTION_CLICK) {
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
                double oldMinX = m_xAxis.getMinValue();
                double oldMaxX = m_xAxis.getMaxValue();
                double oldMinY = m_yAxis.getMinValue();
                double oldMaxY = m_yAxis.getMaxValue();
                m_xAxis.setRange(m_xAxis.pixelToValue(m_zoomGesture.getStartX()), m_xAxis.pixelToValue(m_zoomGesture.getEndX()));
                m_yAxis.setRange(m_yAxis.pixelToValue(m_zoomGesture.getEndY()), m_yAxis.pixelToValue(m_zoomGesture.getStartY()));
                m_updateDoubleBuffer = true;
                fireUpdateAxisRange(oldMinX, oldMaxX, m_xAxis.getMinValue(), m_xAxis.getMaxValue(), oldMinY, oldMaxY, m_yAxis.getMinValue(), m_yAxis.getMaxValue());
            } else if (action == ZoomGesture.ACTION_UNZOOM) {
                if (!m_plots.isEmpty()) {
                    double oldMinX = m_xAxis.getMinValue();
                    double oldMaxX = m_xAxis.getMaxValue();
                    double oldMinY = m_yAxis.getMinValue();
                    double oldMaxY = m_yAxis.getMaxValue();
                    updateAxis(m_plots.get(0));
                    fireUpdateAxisRange(oldMinX, oldMaxX, m_xAxis.getMinValue(), m_xAxis.getMaxValue(), oldMinY, oldMaxY, m_yAxis.getMinValue(), m_yAxis.getMaxValue());
                }
                m_updateDoubleBuffer = true;
            } else if (e.isPopupTrigger()) { // action == ZoomGesture.ACTION_NONE
                // WART, in fact we are not zooming, so if it is the right mouse button,
                // the user wants the popup menu
                if (!m_plots.isEmpty()) {
                    PlotBaseAbstract plot = m_plots.get(0);
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

    public void addListener(PlotPanelListener listener) {
        m_listeners.add(listener);
    }

    protected void fireMouseClicked(MouseEvent e, double xValue, double yValue) {
        // Notify 
        for (PlotPanelListener l : m_listeners) {
            l.plotPanelMouseClicked(e, xValue, yValue);
        }
    }

    protected void fireUpdateAxisRange(double oldMinX, double oldMaxX, double newMinX, double newMaxX, double oldMinY, double oldMaxY, double newMinY, double newMaxY) {
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
                double delta = m_xAxis.pixelToValue(m_panAxisGesture.getPreviousX()) - m_xAxis.pixelToValue(e.getX());
                m_xAxis.setRange(m_xAxis.getMinValue() + delta, m_xAxis.getMaxValue() + delta);
                fireUpdateAxisRange(oldMinX, oldMaxX, m_xAxis.getMinValue(), m_xAxis.getMaxValue(), oldMinY, oldMaxY, m_yAxis.getMinValue(), m_yAxis.getMaxValue());
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

    @Override
    public void mouseMoved(MouseEvent e) {
        if (m_plots == null || m_plots.isEmpty() || e == null) {
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

        double xValue = m_xAxis.pixelToValue(e.getX());
        double yValue = m_yAxis.pixelToValue(e.getY());
        boolean isInGridArea = (e.getX() <= m_plotArea.width + m_plotArea.x && e.getX() >= m_plotArea.x && e.getY() >= m_plotArea.y && e.getY() <= m_plotArea.height + m_plotArea.y);
        if (!isInGridArea) {
            m_coordX = "";
            m_coordY = "";
            if (m_xAxis.inside(e.getX(), e.getY())) {
                setCursor(new Cursor(Cursor.W_RESIZE_CURSOR));
            } else if (m_yAxis.inside(e.getX(), e.getY())) {
                setCursor(new Cursor(Cursor.N_RESIZE_CURSOR));
            } else {
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        } else {
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
                m_posx = e.getX();
                m_posy = e.getY();
            }
        }

        int nbPlotTooltip = 0;
        boolean repaintNeeded = false;
        for (PlotBaseAbstract plot : m_plots) {

            boolean isPlotSelected = plot.isMouseOnPlot(xValue, yValue);
            repaintNeeded |= plot.setIsPaintMarker(isPlotSelected);

            String toolTipForPlot = plot.getToolTipText(xValue, yValue);

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

    private JPopupMenu createAxisPopup(final Axis axis, int x, int y) {

        boolean isXAxis = axis.equals(m_xAxis);

        JPopupMenu popup = new JPopupMenu();
        popup.add(new LogAction(axis));
        popup.add(new GridAction(axis.equals(m_xAxis)));
        popup.add(new RangeAction(axis, axis.equals(m_xAxis)));

        ArrayList<AbstractMeasurement> measurements = new ArrayList<>();
        for (PlotBaseAbstract plot : m_plots) {
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

    public void setPlotToolbarListener(PlotToolbarListener plotToolbarListener) {
        m_plotToolbarListener = plotToolbarListener;
    }

    @Override
    public String getEnumValueX(int index, boolean fromData) {
        if ((m_plots == null) || (m_plots.isEmpty())) {
            return null;
        }
        return m_plots.get(0).getEnumValueX(index, fromData);
    }

    @Override
    public String getEnumValueY(int index, boolean fromData) {
        if ((m_plots == null) || (m_plots.isEmpty())) {
            return null;
        }
        return m_plots.get(0).getEnumValueY(index, fromData);
    }

    public double getNearestXData(double x) {
        if ((m_plots == null) || (m_plots.isEmpty())) {
            return x;
        }

        // look only to the first plot for the moment
        return m_plots.get(0).getNearestXData(x);
    }

    public double getNearestYData(double y) {
        if ((m_plots == null) || (m_plots.isEmpty())) {
            return y;
        }

        // look only to the first plot for the moment
        return m_plots.get(0).getNearestYData(y);
    }

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

        double factor = 0.20;
        double xValue = m_xAxis.pixelToValue(e.getX());
        double yValue = m_yAxis.pixelToValue(e.getY());
        double newXmin = m_xAxis.getMinValue() + (m_xAxis.getMinValue() - xValue) * factor * e.getWheelRotation();
        double newXmax = m_xAxis.getMaxValue() - (xValue - m_xAxis.getMaxValue()) * factor * e.getWheelRotation();
        double newYmin = m_yAxis.getMinValue() + (m_yAxis.getMinValue() - yValue) * factor * e.getWheelRotation();
        double newYmax = m_yAxis.getMaxValue() - (yValue - m_yAxis.getMaxValue()) * factor * e.getWheelRotation();

        if (m_plots.get(0).inside(e.getX(), e.getY())) {
            m_xAxis.setRange(newXmin, newXmax);
            m_yAxis.setRange(newYmin, newYmax);
            repaintUpdateDoubleBuffer();
        } else if ((m_xAxis != null) && m_xAxis.inside(e.getX(), e.getY())) {
            m_xAxis.setRange(newXmin, newXmax);
            repaintUpdateDoubleBuffer();
        } else if ((m_yAxis != null) && m_yAxis.inside(e.getX(), e.getY())) {
            m_yAxis.setRange(newYmin, newYmax);
            repaintUpdateDoubleBuffer();
        }
        fireUpdateAxisRange(oldMinX, oldMaxX, m_xAxis.getMinValue(), m_xAxis.getMaxValue(), oldMinY, oldMaxY, m_yAxis.getMinValue(), m_yAxis.getMaxValue());
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

        public LogAction(Axis axis) {
            super(axis.isLog() ? "Linear Axis" : "Log10 Axis");
            m_axis = axis;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            m_axis.setLog(!m_axis.isLog());
            m_updateDoubleBuffer = true;
            repaint();
        }

        @Override
        public boolean isEnabled() {
            return m_axis.canBeInLog();
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
                m_plotToolbarListener.stateModified(PlotToolbarListener.BUTTONS.GRID);
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
            SwingUtilities.convertPointFromScreen(p, m_axis.m_plotPanel);
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

    public interface PlotToolbarListener {

        public enum BUTTONS {
            GRID,
            EXPORT_SELECTION,
            IMPORT_SELECTION
        }

        public void stateModified(BUTTONS b);

        public void enable(BUTTONS b, boolean v);
    }

    public class FPSUtility {

        private double[] frames;
        private int index = 0;
        private long startTime;

        protected FPSUtility(int size) {
            frames = new double[size];
            Arrays.fill(frames, -1.0);
        }

        protected void startFrame() {
            startTime = System.currentTimeMillis();
        }

        protected void stopFrame() {
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
}
