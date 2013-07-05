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
