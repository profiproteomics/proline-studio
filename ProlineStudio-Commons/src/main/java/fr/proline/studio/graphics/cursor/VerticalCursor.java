package fr.proline.studio.graphics.cursor;

import fr.proline.studio.graphics.BasePlotPanel;
import fr.proline.studio.graphics.XAxis;
import fr.proline.studio.graphics.YAxis;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 *
 * @author JM235353
 */
public class VerticalCursor extends AbstractCursor {

    private int m_positionX;
 
    public VerticalCursor(BasePlotPanel plotPanel, double value) {
        super(plotPanel);
        m_value = value;

    }

    public void setValue(double value) {
        
        m_value = value;
        XAxis xAxis = m_plotPanel.getXAxis();
        m_positionX = xAxis.valueToPixel(m_value);
    }
    
    @Override
    public void paint(Graphics2D g) {
        
        Stroke prevStroke = g.getStroke();
        g.setStroke(m_selected ? AbstractCursor.LINE2_STROKE : m_stroke);
        
        XAxis xAxis = m_plotPanel.getXAxis();
        YAxis yAxis = m_plotPanel.getYAxis();
        
        m_positionX = xAxis.valueToPixel(m_value);
        int y1 = yAxis.valueToPixel(yAxis.getMinValue());
        int y2 = yAxis.valueToPixel(yAxis.getMaxValue());

        g.setColor(m_color);
        g.drawLine(m_positionX, y1, m_positionX, y2);
        
        
        // paint on xAxis
        xAxis.paintCursor(g, this, m_selected);
        
        // restore stroke
        g.setStroke(prevStroke);
    }

    @Override
    public boolean inside(int x, int y) {
        if (!m_selectable) {
            return false;
        }
        return ((x>=m_positionX-INSIDE_TOLERANCE) && (x<=m_positionX+INSIDE_TOLERANCE));
    }

    @Override
    public void move(int deltaX, int deltaY) {
        m_positionX += deltaX;
        XAxis xAxis = m_plotPanel.getXAxis();
        
        m_value = xAxis.pixelToValue(m_positionX);
        
        // avoid going outside of the X Axis at the left
        double min = xAxis.getMinValue();
        if ((m_minValue != null) && (m_minValue>min)) {
            min = m_minValue;
        }
        if (m_value<min) {
            m_value = min;
            m_positionX = xAxis.valueToPixel(min);
        }
        
        // avoid going outside of the X Axis at the right
        double max = xAxis.getMaxValue();
        if ((m_maxValue != null) && (m_maxValue<max)) {
            max = m_maxValue;
        }
        if (m_value>max) {
            m_value = max;
            m_positionX = xAxis.valueToPixel(max);
        }
        
        if (m_actionListenerList !=null) {
            ActionEvent e = new ActionEvent(this, m_actionEventId++, null);
            for (ActionListener a : m_actionListenerList) {
                a.actionPerformed(e);
            }
            
        }
        
    }
    

    @Override
    public boolean isMoveable() {
        return true;
    }

    @Override
    public void snapToData() {
        if (!m_snapToData) {
            return;
        }
        
        
        m_value = m_plotPanel.getNearestXData(m_value);
        XAxis xAxis = m_plotPanel.getXAxis();
        m_positionX = xAxis.valueToPixel(m_value);
    }
}
