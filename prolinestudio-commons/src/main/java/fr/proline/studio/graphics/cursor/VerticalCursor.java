/* 
 * Copyright (C) 2019 VD225637
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1 
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License 
 * along with this program; If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */
package fr.proline.studio.graphics.cursor;

import fr.proline.studio.graphics.BasePlotPanel;
import fr.proline.studio.graphics.XAxis;
import fr.proline.studio.graphics.YAxis;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Horizontal cursor : it displays the X axis value
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
    public void snapToData(boolean isCtrlOrShiftDown) {
        if (!m_snapToData) {
            return;
        }
        
        
        m_value = m_plotPanel.getNearestXData(m_value);
        XAxis xAxis = m_plotPanel.getXAxis();
        m_positionX = xAxis.valueToPixel(m_value);
    }
}
