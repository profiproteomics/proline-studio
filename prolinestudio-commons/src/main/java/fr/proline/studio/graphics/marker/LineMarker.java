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
package fr.proline.studio.graphics.marker;

import fr.proline.studio.graphics.BasePlotPanel;
import fr.proline.studio.graphics.XAxis;
import fr.proline.studio.graphics.YAxis;
import java.awt.*;

/**
 * Marker to display an horizontal or vertical line
 * @author JM235353
 */
public class LineMarker extends AbstractMarker {

    public static final int ORIENTATION_HORIZONTAL = 0;
    public static final int ORIENTATION_VERTICAL = 1;
    
    private double m_value;
    private int m_orientation;
    private Color m_markerColor;
    
    public LineMarker(BasePlotPanel plotPanel, double value, int orientation) {
       this(plotPanel, value, orientation, Color.black, true);
    }
    
    public LineMarker(BasePlotPanel plotPanel, double value, int orientation, Color markerColor, boolean visible) {
        super(plotPanel);
        m_value = value;
        m_orientation = orientation;
        m_markerColor = markerColor;
        setVisible(visible);
    }
    
    // create a lineMarker Vertical with the specified color
    public LineMarker(BasePlotPanel plotPanel, double value, Color markerColor) {
        this(plotPanel, value, ORIENTATION_VERTICAL, markerColor, true);
    }

   public double getValue() {
      return m_value;
   }

   public void setValue(double m_value) {
      this.m_value = m_value;
   }
    
    @Override
    public void paint(Graphics2D g) {

        if (!isVisible()) return;
        
        Stroke prevStroke = g.getStroke();
        g.setStroke(dashed);
        
        XAxis xAxis = m_plotPanel.getXAxis();
        YAxis yAxis = m_plotPanel.getYAxis();
        
        if (m_orientation == ORIENTATION_HORIZONTAL) {
            int y = yAxis.valueToPixel(m_value);
            int x1 = xAxis.valueToPixel(xAxis.getMinValue());
            int x2 = xAxis.valueToPixel(xAxis.getMaxValue());

            g.setColor(m_markerColor);
            g.drawLine(x1, y, x2, y);


        } else if (m_orientation == ORIENTATION_VERTICAL) {
            int x = xAxis.valueToPixel(m_value);
            int y1 = yAxis.valueToPixel(yAxis.getMinValue());
            int y2 = yAxis.valueToPixel(yAxis.getMaxValue());
            
            g.setColor(m_markerColor);
            g.drawLine(x, y1, x, y2);
        }
        
        // restore stroke
        g.setStroke(prevStroke);
        
    }
    private final static float dash1[] = {2.0f};
    private final static BasicStroke dashed = new BasicStroke(1.0f,  BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash1, 0.0f);
}
