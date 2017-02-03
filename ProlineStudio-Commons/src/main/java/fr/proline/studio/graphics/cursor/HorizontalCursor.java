package fr.proline.studio.graphics.cursor;

import fr.proline.studio.graphics.BasePlotPanel;
import fr.proline.studio.graphics.XAxis;
import fr.proline.studio.graphics.YAxis;
import static fr.proline.studio.graphics.cursor.AbstractCursor.CURSOR_COLOR;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 *
 * @author JM235353
 */
public class HorizontalCursor extends AbstractCursor {

    private int m_positionY;

    public HorizontalCursor(BasePlotPanel plotPanel, double value) {
        super(plotPanel);
        m_value = value;

    }

    public void setValue(double value) {
        
        m_value = value;
        YAxis yAxis = m_plotPanel.getYAxis();
        m_positionY = yAxis.valueToPixel(m_value);
    }
    
    @Override
    public void paint(Graphics2D g) {
        
        Stroke prevStroke = g.getStroke();
        g.setStroke(m_selected ? AbstractCursor.LINE2_STROKE : AbstractCursor.LINE1_STROKE);
        
        XAxis xAxis = m_plotPanel.getXAxis();
        YAxis yAxis = m_plotPanel.getYAxis();
        
        m_positionY = yAxis.valueToPixel(m_value);
        int x1 = xAxis.valueToPixel(xAxis.getMinValue());
        int x2 = xAxis.valueToPixel(xAxis.getMaxValue());

        g.setColor(CURSOR_COLOR);
        g.drawLine(x1, m_positionY, x2, m_positionY);
        
        
        // paint on xAxis
        yAxis.paintCursor(g, this, m_selected);
        
        // restore stroke
        g.setStroke(prevStroke);
    }

    @Override
    public boolean inside(int x, int y) {
        if (!m_selectable) {
            return false;
        }
        return ((y>=m_positionY-INSIDE_TOLERANCE) && (y<=m_positionY+INSIDE_TOLERANCE));
    }

    @Override
    public void move(int deltaX, int deltaY) {
        m_positionY += deltaY;
        YAxis yAxis = m_plotPanel.getYAxis();
        
        m_value = yAxis.pixelToValue(m_positionY);
        
        // avoid going outside of the X Axis at the left
        double min = yAxis.getMinValue();
        if ((m_minValue != null) && (m_minValue>min)) {
            min = m_minValue;
        }
        if (m_value<min) {
            m_value = min;
            m_positionY = yAxis.valueToPixel(min);
        }
        
        // avoid going outside of the X Axis at the right
        double max = yAxis.getMaxValue();
        if ((m_maxValue != null) && (m_maxValue<max)) {
            max = m_maxValue;
        }
        if (m_value>max) {
            m_value = max;
            m_positionY = yAxis.valueToPixel(max);
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
        m_positionY = xAxis.valueToPixel(m_value);
    }
}
