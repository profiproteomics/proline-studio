package fr.proline.studio.graphics;

import java.awt.Color;
import java.awt.Graphics;

/**
 *
 * @author JM235353
 */
public class YAxis extends Axis {

    
    public YAxis() {
    }

    
    @Override
    public void paint(Graphics g) {
        
        int maxTicks = m_height/20;
        
        AxisTicks ticks = new AxisTicks(m_minValue, m_maxValue, maxTicks);
        
        g.setColor(Color.black);
        
        m_minTick = ticks.getTickMin();
        m_maxTick = ticks.getTickMax();
        double tickSpacing = ticks.getTickSpacing();
        
        
        int pixelStart = valueToPixel(m_minTick);
        int pixelStop = valueToPixel(m_maxTick);
        g.drawLine(PlotPanel.GAP_FIGURES_Y, pixelStart, PlotPanel.GAP_FIGURES_Y, pixelStop);
        
        double y = m_minTick;
        int pY = pixelStart;
        while(true) {
            g.drawLine(PlotPanel.GAP_FIGURES_Y, pY, PlotPanel.GAP_FIGURES_Y-4, pY);
            
            y += tickSpacing;
            pY = valueToPixel(y);
            if (pY<pixelStop) {
                break;
            }
        }
    }
    
    @Override
    public int valueToPixel(double v) {
        return m_y+m_height-(int) Math.round(((v-m_minTick)/(m_maxTick-m_minTick))*m_height);
    }
   
    
}