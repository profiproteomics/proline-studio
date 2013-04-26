package fr.proline.studio.markerbar;

import java.awt.Color;
import java.awt.Graphics;

public class DefaultMarkerRenderer implements MarkerRendererInterface {

    private Color color;
    
    public DefaultMarkerRenderer(Color c) {
        color = c;
    }
    
    
    @Override
    public void paint(AbstractBar.BarType barType, Graphics g, int x, int y, int width, int height) {
        g.setColor(color);
        
        if (barType == AbstractBar.BarType.OVERVIEW_BAR) {
            g.fillRect(x, y, width, height);
        } else {
            /*int sideLength = Math.min(width, height)-2;
            int gapX = (width-sideLength)/2;
            int gapY = (height-sideLength)/2;
            g.fillRect(x+gapX, y+gapY+1, sideLength, sideLength);*/
            g.fillRect(x+2, y+3, width-4, height-4);
            
        }
    }
}
