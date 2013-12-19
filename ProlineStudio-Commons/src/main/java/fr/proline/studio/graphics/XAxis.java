package fr.proline.studio.graphics;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.text.DecimalFormat;

/**
 * X Axis
 * @author JM235353
 */
public class XAxis extends Axis {

    private DecimalFormat m_df;
    
    public XAxis() {
    }

    
    @Override
    public void paint(Graphics2D g) {
        
        int maxTicks = m_width/30;
        
        AxisTicks ticks = new AxisTicks(m_minValue, m_maxValue, maxTicks);
        
        g.setColor(Color.black);
        
        m_minTick = ticks.getTickMin();
        m_maxTick = ticks.getTickMax();
        double tickSpacing = ticks.getTickSpacing();
        
        String pattern;
        int digits = ticks.getDigits();
        if (digits <= 0) {
            pattern = "#";  // number like "3"
        } else if (digits>3) { // 3 is 
            pattern = ("0.0E0"); // scientific notation for numbers with too much digits "0.0000532"
        } else {
            pattern = "#.";
            while (digits > 0) {
                pattern += "#";
                digits--;
            }
        }
        m_df = new DecimalFormat(pattern);
        
        
        int pixelStart = valueToPixel(m_minTick);
        int pixelStop = valueToPixel(m_maxTick);
        g.drawLine(pixelStart, m_y, pixelStop, m_y);
        
        FontMetrics metrics = g.getFontMetrics(g.getFont());
        int height = metrics.getHeight();

        
        double multForRounding = Math.pow(10,digits);
        
        double x = m_minTick;
        int pX = pixelStart;
        int previousEndX = -Integer.MAX_VALUE;
        while(true) {
            g.drawLine(pX, m_y, pX, m_y+4);
            
            // round x
            double xDisplay = x;
            if (digits>0) {
                xDisplay = StrictMath.round(xDisplay * multForRounding) / multForRounding;
            }
            
            String s = m_df.format(xDisplay);
            int stringWidth = metrics.stringWidth(s);
            
            int posX = pX-stringWidth/2;
            if (posX>previousEndX+2) { // check to avoid to overlap labels
                g.drawString(s, posX, m_y+height+4);
                previousEndX = posX+stringWidth;
            }
            
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
