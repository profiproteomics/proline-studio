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
package fr.proline.studio.markerbar;

import java.awt.Color;
import java.awt.Graphics;

/**
 * Default Renderer for a marker
 * @author JM235353
 */
public class DefaultMarkerRenderer implements MarkerRendererInterface {

    private Color m_color;
    
    public DefaultMarkerRenderer(Color c) {
        m_color = c;
    }
    
    
    @Override
    public void paint(AbstractBar.BarType barType, Graphics g, int x, int y, int width, int height) {
        g.setColor(m_color);
        
        if (barType == AbstractBar.BarType.OVERVIEW_BAR) {
            g.fillRect(x, y, width, height);
        } else {
            g.fillRect(x+2, y+3, width-4, height-4);
            
        }
    }
}
