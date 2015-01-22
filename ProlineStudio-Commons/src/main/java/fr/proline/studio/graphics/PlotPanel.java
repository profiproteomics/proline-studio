package fr.proline.studio.graphics;

import fr.proline.studio.parameter.ParameterList;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

/**
 * Panel to display data with an X and Y Axis
 * @author JM235353
 */
public class PlotPanel extends JPanel implements MouseListener, MouseMotionListener {
    
    private static final Color PANEL_BACKGROUND_COLOR = UIManager.getColor ("Panel.background");
    
    private XAxis m_xAxis = null;
    private YAxis m_yAxis = null;
    
    private java.util.List<PlotAbstract> m_plots = null;
    
    private final ZoomGesture m_zoomGesture = new ZoomGesture();
    private final SelectionGesture m_selectionGesture = new SelectionGesture();
    
    
    public final static int GAP_FIGURES_Y = 50;
    public final static int GAP_FIGURES_X = 24;
    public final static int GAP_END_AXIS = 10;
    public final static int GAP_AXIS_TITLE = 20;
    
    private BufferedImage m_doubleBuffer = null;
    private boolean m_useDoubleBuffering = false;
    private boolean m_updateDoubleBuffer = false;
    
    private boolean m_plotHorizontalGrid = true;
    private boolean m_plotVerticalGrid = true;

    private boolean m_dataLocked = false;
    
    private GridListener m_gridListener = null;
    
    public PlotPanel() {
        addMouseListener(this);
        addMouseMotionListener(this);
        ToolTipManager.sharedInstance().registerComponent(this);
        m_plots = new ArrayList();
    }
    
    @Override
    public void paint(Graphics g) {
        
        Graphics2D g2d = (Graphics2D)g;
         g2d.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        Rectangle bounds = getBounds();
        Rectangle plotArea = getBounds();
        
        g.setColor(PANEL_BACKGROUND_COLOR);
        g.fillRect(0, 0, bounds.width, bounds.height);
        
        g.setColor(Color.darkGray);
//        g.drawRect(0, 0, bounds.width-1, bounds.height-1);
        
        if (m_xAxis != null) {
            m_xAxis.setSize(GAP_FIGURES_Y+GAP_AXIS_TITLE, ((int)bounds.getHeight())-GAP_FIGURES_X -GAP_AXIS_TITLE /*-GAP_TOP_AXIS*/, bounds.width-GAP_FIGURES_Y-GAP_AXIS_TITLE-GAP_END_AXIS, GAP_FIGURES_X + GAP_AXIS_TITLE);
            plotArea.x = m_xAxis.m_x+1;
            plotArea.width = m_xAxis.m_width-1;
            m_xAxis.paint(g2d);

        }
        
        if (m_yAxis != null) {
            m_yAxis.setSize(0, GAP_END_AXIS, GAP_FIGURES_Y+GAP_AXIS_TITLE /*+GAP_TOP_AXIS*/, ((int)bounds.getHeight())-GAP_FIGURES_X-GAP_AXIS_TITLE-GAP_END_AXIS);
            plotArea.y = m_yAxis.m_y+1;
            plotArea.height = m_yAxis.m_height-1;
            m_yAxis.paint(g2d);
        }
        
                    

        
        
        if (m_plots != null) {
                if (m_useDoubleBuffering) {
                    boolean createDoubleBuffer = ((m_doubleBuffer == null) || (m_doubleBuffer.getWidth()!=plotArea.width) || (m_doubleBuffer.getHeight()!=plotArea.height));
                    if (createDoubleBuffer) {
                        m_doubleBuffer = new BufferedImage(plotArea.width, plotArea.height, BufferedImage.TYPE_INT_ARGB);
                    }
                    if (createDoubleBuffer || m_updateDoubleBuffer) {
                        
                        Graphics2D graphicBufferG2d = (Graphics2D) m_doubleBuffer.getGraphics();
                        graphicBufferG2d.setColor(Color.white);
                        graphicBufferG2d.fillRect(0, 0, plotArea.width, plotArea.height);
                        graphicBufferG2d.translate(-plotArea.x, -plotArea.y);
                        if ((m_plotVerticalGrid) && (m_xAxis != null)) {
                            m_xAxis.paintGrid(graphicBufferG2d, plotArea.y, plotArea.height);
                        }
                        
                        if ((m_plotHorizontalGrid) && (m_yAxis != null)) {
                            m_yAxis.paintGrid(graphicBufferG2d, plotArea.x, plotArea.width);
                        }
                        

                        for (PlotAbstract plot : m_plots) {
                            plot.paint(graphicBufferG2d);
                        }
                        m_updateDoubleBuffer = false;
                    }
                    g2d.drawImage(m_doubleBuffer, plotArea.x, plotArea.y, null);
                    
                } else {
                    
                    long startPlotTime = System.currentTimeMillis();
                    g.setColor(Color.white);
                    g.fillRect(plotArea.x, plotArea.y, plotArea.width, plotArea.height);
                    if ((m_plotVerticalGrid) && (m_xAxis != null)) {
                        m_xAxis.paintGrid(g2d, plotArea.y, plotArea.height);
                    }

                    if ((m_plotHorizontalGrid) && (m_yAxis != null)) {
                        m_yAxis.paintGrid(g2d, plotArea.x, plotArea.width);
                    }
                    

                    for (PlotAbstract plot : m_plots) {
                        plot.paint(g2d);
                    }
                    long stopPlotTime = System.currentTimeMillis();
                    if (stopPlotTime - startPlotTime > 50) {
                        // display is too slow , we use an image
                        m_useDoubleBuffering = true;
                    }
                }
        }
        
        // set clipping area for zooming and selecting
        if ((m_xAxis != null) && (m_yAxis != null)) {
            int clipX = m_xAxis.valueToPixel(m_xAxis.getMinTick());
            int clipWidth = m_xAxis.valueToPixel(m_xAxis.getMaxTick()) - clipX;
            int clipY = m_yAxis.valueToPixel(m_yAxis.getMaxTick());
            int clipHeight = m_yAxis.valueToPixel(m_yAxis.getMinTick()) - clipY;
            g2d.setClip(clipX, clipY, clipWidth, clipHeight);
        }
        
        m_zoomGesture.paint(g2d);
        m_selectionGesture.paint(g2d);
        
    }
    
    public ArrayList<ParameterList> getParameters() {
        if (m_plots == null) {
            return null;
        }
        
        for (PlotAbstract plot : m_plots) {
            return plot.getParameters();
        }
        return null;
    }
    
    public void parametersChanged() {
        if (m_plots!= null) {
            for (int i=0;i<m_plots.size();i++) {
                m_plots.get(i).parametersChanged();
            }
        }
        m_updateDoubleBuffer = true;
        repaint();
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
  
    public void displayGrid(boolean v) {
        m_plotVerticalGrid = v;
        m_plotHorizontalGrid = v;
        m_updateDoubleBuffer = true;
        repaint();
    }
    
    public boolean displayGrid() {
        return (m_plotHorizontalGrid || m_plotVerticalGrid);
    }
    
    public void setPlot(PlotAbstract plot) {
       m_plots = new ArrayList();
       m_plots.add(plot);

        updateAxis(plot);
    }
    
    public void clearPlots() {
        this.m_plots = new ArrayList();
    }
    
    public void addPlot(PlotAbstract plot) {
        m_plots.add(plot);

        updateAxis(plot);
    }
    
    public ArrayList<Integer> getSelection() {
        if (!m_plots.isEmpty()){
            return m_plots.get(0).getSelection();
        }else {
            return null;
        }
    }
    public void setSelection(ArrayList<Integer> selection) {
        if (!m_plots.isEmpty()){
            m_plots.get(0).setSelection(selection);
            m_updateDoubleBuffer = true;
            repaint();
       }
    }
    
    private double[] getMinMaxPlots() {
        double[] tab = new double[4];
        if(m_plots != null && m_plots.size() > 0) {
            double minX = m_plots.get(0).getXMin();
            double maxX = m_plots.get(0).getXMax();
            double minY = m_plots.get(0).getYMin();
            double maxY = m_plots.get(0).getYMax();
            for(int k=1; k<m_plots.size(); k++) {
                minX = Math.min(minX, m_plots.get(k).getXMin());
                maxX = Math.max(maxX, m_plots.get(k).getXMax());
                minY = Math.min(minY, m_plots.get(k).getYMin());
                maxY = Math.max(maxY, m_plots.get(k).getYMax());
            }
            tab[0] = minX; 
            tab[1] = maxX;
            tab[2] = minY; 
            tab[3] = maxY;
        }
        return tab;
    }

    public void updateAxis(PlotAbstract plot) {
        double[] tab = getMinMaxPlots();
        
        if (plot.needsXAxis()) {
            
            XAxis xAxis = getXAxis();
            xAxis.setLog(false);
            xAxis.setSelected(false);
            xAxis.setRange(tab[0] ,tab[1]);
            
            
        }

        if (plot.needsYAxis()) {
            YAxis yAxis = getYAxis();
            yAxis.setLog(false);
            yAxis.setSelected(false);
            yAxis.setRange(tab[2] ,tab[3]);
        }
        
        m_updateDoubleBuffer = true;
        m_useDoubleBuffering = plot.needsDoubleBuffering();
    }
    
    public void setXAxisTitle(String title) {
        if  (m_xAxis == null) {
            return;
        }
        m_xAxis.setTitle(title);
    }
    
    public void setYAxisTitle(String title) {
        if (m_yAxis == null) {
            return;
        }
        m_yAxis.setTitle(title);
    }
    
    public XAxis getXAxis() {
        if (m_xAxis == null) {
            m_xAxis = new XAxis();
        }
        return m_xAxis;
    }
    
    public YAxis getYAxis() {
        if (m_yAxis == null) {
            m_yAxis = new YAxis();
        }
        return m_yAxis;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {

        if (m_plots == null || m_plots.isEmpty()) {
            return;
        }
        
        int x = e.getX();
        int y = e.getY();
        if (m_plots.get(0).inside(x, y)) {
            // Mouse Pressed in the plot area
            
            // deselect axis
            m_xAxis.setSelected(false);
            m_yAxis.setSelected(false);
            
            if (SwingUtilities.isLeftMouseButton(e)) {
                m_selectionGesture.startSelection(x, y);
            } else if (SwingUtilities.isRightMouseButton(e)) {
                m_zoomGesture.startZooming(x, y);
            }
            repaint();
        }

    }

    @Override
    public void mouseReleased(MouseEvent e) {
        
        boolean mustRepaint = false;
        
        int x = e.getX();
        int y = e.getY();
        
        if (e.isPopupTrigger()) {
           
            if (m_xAxis.inside(x, y)) {
                m_xAxis.setSelected(true);
                m_yAxis.setSelected(false);
                JPopupMenu popup = createAxisPopup(m_xAxis);
                popup.show((JComponent) e.getSource(), x, y);
                mustRepaint = true;
            } else if (m_yAxis.inside(x, y)) {
                m_xAxis.setSelected(false);
                m_yAxis.setSelected(true);
                JPopupMenu popup = createAxisPopup(m_yAxis);
                popup.show((JComponent) e.getSource(), x, y);
                mustRepaint = true;
            }
        } else {
            
            if (m_xAxis.inside(x, y)) {
                mustRepaint |= m_xAxis.setSelected(!m_xAxis.isSelected());
                mustRepaint |= m_yAxis.setSelected(false);
            } else if (m_yAxis.inside(x, y)) {
                mustRepaint |= m_xAxis.setSelected(false);
                mustRepaint |= m_yAxis.setSelected(!m_yAxis.isSelected());
            }
            if (mustRepaint) {
                repaint();
            }
        }
        
        if (m_selectionGesture.isSelecting()) {
            m_selectionGesture.stopSelection(x, y);
            
            int modifier = e.getModifiers();
            boolean isCtrlOrShiftDown = ((modifier & (InputEvent.SHIFT_MASK | InputEvent.CTRL_MASK)) != 0);

            int action = m_selectionGesture.getAction();
            if (action == SelectionGesture.ACTION_CLICK) {
                Point p = m_selectionGesture.getClickPoint();
                double valueX = m_xAxis.pixelToValue(p.x);
                double valueY = m_yAxis.pixelToValue(p.y);

                if (!m_plots.isEmpty()){
                    m_plots.get(0).select(valueX, valueY, isCtrlOrShiftDown);
                }
                m_updateDoubleBuffer = true;
                mustRepaint = true;
            } else if (action == SelectionGesture.ACTION_SURROUND) {
                Path2D.Double path = new Path2D.Double();
                
                Polygon p =  m_selectionGesture.getSelectionPolygon();
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
                
                if (!m_plots.isEmpty()){
                    m_plots.get(0).select(path, xMin, xMax, yMin, yMax, isCtrlOrShiftDown);
                }
                m_updateDoubleBuffer = true;
                mustRepaint = true;
            }
            

            
        } else if (m_zoomGesture.isZooming()) {
            m_zoomGesture.stopZooming(e.getX(), e.getY());

            int action = m_zoomGesture.getAction();
            if (action == ZoomGesture.ACTION_ZOOM) {
                m_xAxis.setRange(m_xAxis.pixelToValue(m_zoomGesture.getStartX()), m_xAxis.pixelToValue(m_zoomGesture.getEndX()));
                m_yAxis.setRange(m_yAxis.pixelToValue(m_zoomGesture.getEndY()), m_yAxis.pixelToValue(m_zoomGesture.getStartY()));
                m_updateDoubleBuffer = true;
            } else if (action == ZoomGesture.ACTION_UNZOOM) {
                if (!m_plots.isEmpty()){
                    updateAxis(m_plots.get(0));
                }
                m_updateDoubleBuffer = true;
            }
            mustRepaint = true;
        }
        
        if (mustRepaint) {
            repaint();
        }
    }
    
    public void repaintUpdateDoubleBuffer() {
        m_updateDoubleBuffer = true;
        repaint();
    }

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    @Override
    public void mouseDragged(MouseEvent e) {
        if (m_selectionGesture.isSelecting()) {
            m_selectionGesture.continueSelection(e.getX(), e.getY());
            repaint();
        } else if (m_zoomGesture.isZooming()) {
            m_zoomGesture.moveZooming(e.getX(), e.getY());
            repaint();
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (m_plots == null || m_plots.isEmpty()) {
            return;
        }
        String s =  "<html>";
        boolean hasToolTip = false;
        for (PlotAbstract plot : m_plots) {
            plot.setIsPaintMarker(false);
            boolean isPlotSelected = plot.isMouseOnPlot(m_xAxis.pixelToValue(e.getX()), m_yAxis.pixelToValue(e.getY()));
            plot.setIsPaintMarker(isPlotSelected);
            String toolTipForPlot = plot.getToolTipText(m_xAxis.pixelToValue(e.getX()), m_yAxis.pixelToValue(e.getY()));
            if (toolTipForPlot != null && !toolTipForPlot.isEmpty()){
                s += toolTipForPlot;
                hasToolTip = true;
                s += "<br/>";
                
            }
            
        }
        s += "</html>";
        if (hasToolTip) {
            setToolTipText(s); 
        }
        repaintUpdateDoubleBuffer();
        
    }

    
    private JPopupMenu createAxisPopup(final Axis axis) {
        JPopupMenu popup = new JPopupMenu();
        popup.add(new LogAction(axis));
        popup.add(new GridAction(axis.equals(m_xAxis)));
        
        popup.addPopupMenuListener(new PopupMenuListener() {

            @Override
            public void popupMenuCanceled(PopupMenuEvent popupMenuEvent) {}

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent popupMenuEvent) {
                 axis.setSelected(false);
                 repaint();
            }

            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent popupMenuEvent) {}
        });
        
        return popup;
    }
    
    public void setGridListener(GridListener gridListener) {
        m_gridListener = gridListener;
    }
    
    
    public class LogAction extends AbstractAction {

        private final Axis m_axis;

        public LogAction(Axis axis) {
            super(axis.isLog() ? "Linear Axis" : "Log Axis");
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
            super(xAxis ? (m_plotVerticalGrid ? "Remove Vertical Grid" : "Add Vertical Grid") : (m_plotHorizontalGrid ? "Remove Horizontal Grid" : "Add Horizontal Grid")  );
            m_xAxis = xAxis;
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            if (m_xAxis) {
                m_plotVerticalGrid = !m_plotVerticalGrid;
            } else { // y Axis
                m_plotHorizontalGrid = !m_plotHorizontalGrid;
            }
            if (m_gridListener != null) {
                m_gridListener.gridChanged();
            }
            m_updateDoubleBuffer = true;
            repaint();
        }

    }
    
    public interface GridListener {
        public void gridChanged();
    }
    
    
}
