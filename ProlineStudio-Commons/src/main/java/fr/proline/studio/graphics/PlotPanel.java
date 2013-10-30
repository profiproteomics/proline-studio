package fr.proline.studio.graphics;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import javax.swing.JPanel;

/**
 *
 * @author JM235353
 */
public class PlotPanel extends JPanel {
    
    private XAxis m_xAxis = null;
    private YAxis m_yAxis = null;
    
    private PlotAbstract m_plot = null;
    
    public final static int GAP_FIGURES_Y = 30;
    public final static int GAP_FIGURES_X = 30;
    public final static int GAP_END_AXIS = 10;
    //public final static int GAP_TOP_AXIS = 3;
    
    
    public PlotPanel() {

    }
    
    @Override
    public void paint(Graphics g) {
        Rectangle bounds = getBounds();

        g.setColor(Color.white);
        g.fillRect(0, 0, bounds.width, bounds.height);
        
        if (m_xAxis != null) {
            m_xAxis.setSize(GAP_FIGURES_Y, ((int)bounds.getHeight())-GAP_FIGURES_X /*-GAP_TOP_AXIS*/, bounds.width-GAP_FIGURES_Y-GAP_END_AXIS, GAP_FIGURES_X /*+GAP_TOP_AXIS*/);
            m_xAxis.paint(g);
        }
        
        if (m_yAxis != null) {
            m_yAxis.setSize(0, GAP_END_AXIS, GAP_FIGURES_Y /*+GAP_TOP_AXIS*/, ((int)bounds.getHeight())-GAP_FIGURES_X-GAP_END_AXIS);
            m_yAxis.paint(g);
        }
        
        if (m_plot != null) {
            m_plot.paint(g);
        }
        
    }
    
    public void addPlot(PlotAbstract plot) {
        m_plot = plot;
        
        if (m_plot.needsXAxis()) {
            if (m_xAxis == null) {
                m_xAxis = new XAxis();
            }
            m_xAxis.setRange(m_plot.getXMin(), m_plot.getXMax());
        }

        if (m_plot.needsYAxis()) {
            if (m_yAxis == null) {
                m_yAxis = new YAxis();
            }
            m_yAxis.setRange(m_plot.getYMin(), m_plot.getYMax());
        }

    }
    
    public XAxis getXAxis() {
        return m_xAxis;
    }
    
    public YAxis getYAxis() {
        return m_yAxis;
    }
    
}
