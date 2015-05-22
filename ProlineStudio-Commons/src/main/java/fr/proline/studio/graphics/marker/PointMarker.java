package fr.proline.studio.graphics.marker;

import fr.proline.studio.graphics.BasePlotPanel;
import fr.proline.studio.graphics.XAxis;
import fr.proline.studio.graphics.YAxis;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 * marker with a point shape on a plot
 * @author MB243701
 */
public class PointMarker extends AbstractMarker{
    
    private double m_x;
    private double m_y;
    private Color m_markerColor;
    
    private static final int width = 6;

    public PointMarker(BasePlotPanel plotPanel, double x, double y, Color color) {
        super(plotPanel);
        this.m_x = x;
        this.m_y = y;
        this.m_markerColor = color;
    }
    
    
    @Override
    public void paint(Graphics2D g) {
        XAxis xAxis = m_plotPanel.getXAxis();
        YAxis yAxis = m_plotPanel.getYAxis(); 
        g.setColor(m_markerColor);
        g.fillOval(xAxis.valueToPixel(m_x)-(width/2), yAxis.valueToPixel(m_y)-(width/2), width, width);
    }
    
}
