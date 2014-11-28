package fr.proline.studio.graphics;

import fr.proline.studio.utils.CyclicColorPalette;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import javax.swing.UIManager;

/**
 * Panel to display data with an X and Y Axis
 * @author JM235353
 */
public class PlotPanel extends JPanel implements MouseListener, MouseMotionListener {
    
    private static final Color PANEL_BACKGROUND_COLOR = UIManager.getColor ("Panel.background");
    
    private XAxis m_xAxis = null;
    private YAxis m_yAxis = null;
    
    private PlotAbstract m_plot = null;
    
    private final ZoomGesture m_zoomGesture = new ZoomGesture();
    
    
    public final static int GAP_FIGURES_Y = 50;
    public final static int GAP_FIGURES_X = 30;
    public final static int GAP_END_AXIS = 10;
    public final static int GAP_AXIS_TITLE = 0; //JPM.TODO
    
    private BufferedImage m_doubleBuffer = null;
    private boolean m_useDoubleBuffering = false;
    private boolean m_updateDoubleBuffer = false;
    
    public PlotPanel() {
        addMouseListener(this);
        addMouseMotionListener(this);
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
            m_xAxis.setSize(GAP_FIGURES_Y, ((int)bounds.getHeight())-GAP_FIGURES_X /*-GAP_TOP_AXIS*/, bounds.width-GAP_FIGURES_Y-GAP_END_AXIS, GAP_FIGURES_X + GAP_AXIS_TITLE);
            plotArea.x = m_xAxis.m_x+1;
            plotArea.width = m_xAxis.m_width-1;
            m_xAxis.paint(g2d);
        }
        
        if (m_yAxis != null) {
            m_yAxis.setSize(0, GAP_END_AXIS, GAP_FIGURES_Y+GAP_AXIS_TITLE /*+GAP_TOP_AXIS*/, ((int)bounds.getHeight())-GAP_FIGURES_X-GAP_END_AXIS);
            plotArea.y = m_yAxis.m_y+1;
            plotArea.height = m_yAxis.m_height-1;
            m_yAxis.paint(g2d);
        }
        
        if (m_plot != null) {
            
            if (m_useDoubleBuffering) {
                if ((m_doubleBuffer == null) || (m_doubleBuffer.getWidth()!=plotArea.width) || (m_doubleBuffer.getHeight()!=plotArea.height) || (m_updateDoubleBuffer)) {
                    m_doubleBuffer = new BufferedImage(plotArea.width, plotArea.height, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D graphicBufferG2d = (Graphics2D) m_doubleBuffer.getGraphics();
                    graphicBufferG2d.setColor(Color.white);
                    graphicBufferG2d.fillRect(0, 0, plotArea.width, plotArea.height);
                    graphicBufferG2d.translate(-plotArea.x, -plotArea.y);
                    m_plot.paint(graphicBufferG2d);
                    m_updateDoubleBuffer = false;
                }
                g2d.drawImage(m_doubleBuffer, plotArea.x, plotArea.y, null);
                
            } else {

                long startPlotTime = System.currentTimeMillis();
                g.setColor(Color.white);
                g.fillRect(plotArea.x, plotArea.y, plotArea.width, plotArea.height);
                m_plot.paint(g2d);
                long stopPlotTime = System.currentTimeMillis();
                if (stopPlotTime - startPlotTime > 50) {
                    // display is too slow , we use an image 
                    m_useDoubleBuffering = true;
                }
            }
        }
        
        m_zoomGesture.paint(g2d);
        
    }
  
    
    public void setPlot(PlotAbstract plot) {
        m_plot = plot;

        updateAxis(plot);

    }

    public void updateAxis(PlotAbstract plot) {
        
        if (plot.needsXAxis()) {
            if (m_xAxis == null) {
                m_xAxis = new XAxis();
            }
            m_xAxis.setRange(plot.getXMin(), plot.getXMax());
        }

        if (plot.needsYAxis()) {
            if (m_yAxis == null) {
                m_yAxis = new YAxis();
            }
            m_yAxis.setRange(plot.getYMin(), plot.getYMax());
        }
        
        m_updateDoubleBuffer = true;
        m_useDoubleBuffering = plot.needsDoubleBuffering();
    }
    
    public XAxis getXAxis() {
        return m_xAxis;
    }
    
    public YAxis getYAxis() {
        return m_yAxis;
    }

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {
        m_zoomGesture.startZooming(e.getX(), e.getY());
        repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        m_zoomGesture.stopZooming(e.getX(), e.getY());
        
        int action = m_zoomGesture.getAction();
        if (action == ZoomGesture.ACTION_ZOOM) {
            m_xAxis.setRange(m_xAxis.pixelToValue(m_zoomGesture.getStartX()), m_xAxis.pixelToValue(m_zoomGesture.getEndX()));
            m_yAxis.setRange(m_yAxis.pixelToValue(m_zoomGesture.getEndY()), m_yAxis.pixelToValue(m_zoomGesture.getStartY()));
            m_updateDoubleBuffer = true;
        } else if (action == ZoomGesture.ACTION_UNZOOM) {
            updateAxis(m_plot);
            m_updateDoubleBuffer = true;
        }
        repaint();
    }

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    @Override
    public void mouseDragged(MouseEvent e) {
        m_zoomGesture.moveZooming(e.getX(), e.getY());

        repaint();
    }

    @Override
    public void mouseMoved(MouseEvent e) {}
    
}
