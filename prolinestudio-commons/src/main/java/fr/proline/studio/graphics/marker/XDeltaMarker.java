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
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;


/**
 * Marker to display an horizontal segment between two points
 * @author JM235353
 */
public class XDeltaMarker extends AbstractMarker {

    
    private double m_x1;
    private double m_x2;
    private double m_y;
    
    public XDeltaMarker(BasePlotPanel plotPanel, double x1, double x2, double y) {
        super(plotPanel);
        
        m_x1 = x1;
        m_x2 = x2;
        m_y = y;
    }
    
    public void set(double x1, double x2) {
        m_x1 = x1;
        m_x2 = x2;
    }
    
    @Override
    public void paint(Graphics2D g) {

        XAxis xAxis = m_plotPanel.getXAxis();
        YAxis yAxis = m_plotPanel.getYAxis();
        
        int x1 = xAxis.valueToPixel(m_x1);
        int x2 = xAxis.valueToPixel(m_x2);
        if (x1>x2) {
            int tmp = x2;
            x2 = x1;
            x1 = tmp;
        }
        int y = yAxis.valueToPixel(m_y);
            

        g.setColor(Color.black);
        g.setStroke(new BasicStroke(1));
        g.drawLine(x1, y, x2, y);

        final int ARROW_SIZE = 3;
        g.drawLine(x1,y,x1+ARROW_SIZE, y-ARROW_SIZE);
        g.drawLine(x1,y,x1+ARROW_SIZE, y+ARROW_SIZE);
        g.drawLine(x2,y,x2-ARROW_SIZE, y-ARROW_SIZE);
        g.drawLine(x2,y,x2-ARROW_SIZE, y+ARROW_SIZE);


        
    }
    
}
