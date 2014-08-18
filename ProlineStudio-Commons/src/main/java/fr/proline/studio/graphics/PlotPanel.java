package fr.proline.studio.graphics;

import java.awt.*;
import javax.swing.JPanel;

/**
 * Panel to display data with an X and Y Axis
 * @author JM235353
 */
public class PlotPanel extends JPanel {
    
    private XAxis m_xAxis = null;
    private YAxis m_yAxis = null;
    
    private PlotAbstract m_plot = null;
    
    public final static int GAP_FIGURES_Y = 50;
    public final static int GAP_FIGURES_X = 30;
    public final static int GAP_END_AXIS = 10;

    
    
    public PlotPanel() {

    }
    
    @Override
    public void paint(Graphics g) {
        
        Graphics2D g2d = (Graphics2D)g;
         g2d.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        Rectangle bounds = getBounds();

        g.setColor(Color.white);
        g.fillRect(0, 0, bounds.width, bounds.height);
        
        g.setColor(Color.darkGray);
        g.drawRect(0, 0, bounds.width-1, bounds.height-1);
        
        if (m_xAxis != null) {
            m_xAxis.setSize(GAP_FIGURES_Y, ((int)bounds.getHeight())-GAP_FIGURES_X /*-GAP_TOP_AXIS*/, bounds.width-GAP_FIGURES_Y-GAP_END_AXIS, GAP_FIGURES_X /*+GAP_TOP_AXIS*/);
            m_xAxis.paint(g2d);
        }
        
        if (m_yAxis != null) {
            m_yAxis.setSize(0, GAP_END_AXIS, GAP_FIGURES_Y /*+GAP_TOP_AXIS*/, ((int)bounds.getHeight())-GAP_FIGURES_X-GAP_END_AXIS);
            m_yAxis.paint(g2d);
        }
        
        if (m_plot != null) {
            m_plot.paint(g2d);
        }
        
    }
    
    public void addPlot(PlotAbstract plot) {
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
    }
    
    public XAxis getXAxis() {
        return m_xAxis;
    }
    
    public YAxis getYAxis() {
        return m_yAxis;
    }
    
}
