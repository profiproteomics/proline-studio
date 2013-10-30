package fr.proline.studio.graphics;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.text.DecimalFormat;

/**
 *
 * @author JM235353
 */
public class XAxis extends Axis {

    private DecimalFormat m_df;
    
    public XAxis() {
    }

    
    @Override
    public void paint(Graphics g) {
        
        int maxTicks = m_width/30;
        
        AxisTicks ticks = new AxisTicks(m_minValue, m_maxValue, maxTicks);
        
        g.setColor(Color.black);
        
        m_minTick = ticks.getTickMin();
        m_maxTick = ticks.getTickMax();
        double tickSpacing = ticks.getTickSpacing();
        
        int digits = ticks.getDigits();
        String pattern = "#.";
        while(digits>0) {
            pattern += "#";
            digits--;
        }
        m_df = new DecimalFormat(pattern);
        
        
        int pixelStart = valueToPixel(m_minTick);
        int pixelStop = valueToPixel(m_maxTick);
        g.drawLine(pixelStart, m_y /*+PlotPanel.GAP_TOP_AXIS*/, pixelStop, m_y /*+PlotPanel.GAP_TOP_AXIS*/);
        
        FontMetrics metrics = g.getFontMetrics(g.getFont());
        int height = metrics.getHeight();

        
        double x = m_minTick;
        int pX = pixelStart;
        while(true) {
            g.drawLine(pX, m_y/*+PlotPanel.GAP_TOP_AXIS*/, pX, m_y+4/*+PlotPanel.GAP_TOP_AXIS*/);
            
            String s = m_df.format(x);
            int stringWidth = metrics.stringWidth(s);
            g.drawString(s, pX-stringWidth/2, m_y+height+4);
            
            x += tickSpacing;
            pX = valueToPixel(x);
            if (pX>pixelStop) {
                break;
            }
        }
    }
    
    @Override
    public int valueToPixel(double v) {
        return m_x + (int) Math.round(((v-m_minTick)/(m_maxTick-m_minTick))*m_width);
    }
   
    
}
