package fr.proline.studio.graphics.cursor;

import fr.proline.studio.graphics.BasePlotPanel;
import fr.proline.studio.graphics.XAxis;
import fr.proline.studio.graphics.YAxis;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;

/**
 *
 * @author JM235353
 */
public class VerticalCursor extends AbstractCursor {

    private double m_value;

    private int m_positionX;
    
    private final static int INSIDE_TOLERANCE = 2;
    
    public VerticalCursor(BasePlotPanel plotPanel, double value) {
        super(plotPanel);
        m_value = value;

    }

    @Override
    public void paint(Graphics2D g) {
        
        Stroke prevStroke = g.getStroke();
        g.setStroke(LINE2_STROKE);
        
        XAxis xAxis = m_plotPanel.getXAxis();
        YAxis yAxis = m_plotPanel.getYAxis();
        
        m_positionX = xAxis.valueToPixel(m_value);
        int y1 = yAxis.valueToPixel(yAxis.getMinValue());
        int y2 = yAxis.valueToPixel(yAxis.getMaxValue());

        g.setColor(CURSOR_COLOR);
        g.drawLine(m_positionX, y1, m_positionX, y2);
        
        
        // paint on xAxis
        xAxis.paintCursor(g, m_value);
        
        // restore stroke
        g.setStroke(prevStroke);
    }

    @Override
    public boolean inside(int x, int y) {
        return ((x>=m_positionX-INSIDE_TOLERANCE) && (x<=m_positionX+INSIDE_TOLERANCE));
    }

    @Override
    public void move(int deltaX, int deltaY) {
        m_positionX += deltaX;
        XAxis xAxis = m_plotPanel.getXAxis();
        
        m_value = xAxis.pixelToValue(m_positionX);
        
        // avoid going outside of the X Axis at the left
        double min = xAxis.getMinValue();
        if (m_value<min) {
            m_value = min;
            m_positionX = xAxis.valueToPixel(min);
        }
        
        // avoid going outside of the X Axis at the right
        double max = xAxis.getMaxValue();
        if (m_value>max) {
            m_value = max;
            m_positionX = xAxis.valueToPixel(max);
        }
        
    }

    @Override
    public boolean isMoveable() {
        return true;
    }
}
