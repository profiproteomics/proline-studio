package fr.proline.studio.graphics;

import fr.proline.studio.utils.CyclicColorPalette;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

/**
 * Y Axis
 * @author JM235353
 */
public class YAxis extends Axis {

    private int m_digits = -1;
    
    public YAxis() {
    }

    
    @Override
    public void paint(Graphics2D g) {
        
        int maxTicks = m_height/20;
        
        AxisTicks ticks = new AxisTicks(m_minValue, m_maxValue, maxTicks);
        
        g.setColor(Color.black);
        
        m_minTick = ticks.getTickMin();
        m_maxTick = ticks.getTickMax();
        m_tickSpacing = ticks.getTickSpacing();
        
        int digits = ticks.getDigits();
        if ((digits != m_digits) || (m_df == null)) {
            m_df = selectDecimalFormat(digits);
            m_digits = digits;
        }
        
        g.setColor(CyclicColorPalette.GRAY_TEXT_DARK);
        
        if (m_valuesFont == null) {
            m_valuesFont = g.getFont().deriveFont(Font.PLAIN, 10);
            m_valuesFontMetrics = g.getFontMetrics(m_valuesFont);
        }
        g.setFont(m_valuesFont);
        
        int halfAscent = m_valuesFontMetrics.getAscent()/2;
        
        int pixelStart = valueToPixel(m_minTick);
        int pixelStop = valueToPixel(m_maxTick);
        g.drawLine(PlotPanel.GAP_FIGURES_Y, pixelStart, PlotPanel.GAP_FIGURES_Y, pixelStop);
        
        if (pixelStart <= pixelStop) { // avoid infinite loop when histogram is flat
            return;
        }
        
        double multForRounding = Math.pow(10,digits);
        
        double y = m_minTick;
        int pY = pixelStart;

        while(true) {
            
            // round y
            double yDisplay = y;
            if (digits > 0) {
                yDisplay = StrictMath.round(yDisplay * multForRounding) / multForRounding;
            }
            
            String s = m_df.format(yDisplay);
            int stringWidth = m_valuesFontMetrics.stringWidth(s);
            
            g.drawString(s, PlotPanel.GAP_FIGURES_Y-stringWidth-6, pY+halfAscent);
            
            g.drawLine(PlotPanel.GAP_FIGURES_Y, pY, PlotPanel.GAP_FIGURES_Y-4, pY);
            
            y += m_tickSpacing;
            pY = valueToPixel(y);
            if (pY<pixelStop) {
                break;
            }
        }
        
        if (m_title != null) {
            if (m_titleFont == null) {
                AffineTransform affineTr = new AffineTransform();
                affineTr.rotate(-Math.PI / 2);
                m_titleFont = g.getFont().deriveFont(Font.BOLD, 11).deriveFont(affineTr);
                m_titleFontMetrics = g.getFontMetrics(m_titleFont);
            }
            Font prevFont = g.getFont();
            
            g.setFont(m_titleFont);
            g.setColor(Color.black);
            int titleWidth = m_titleFontMetrics.stringWidth(m_title);
            int bottom = m_x;
            int top = m_x + PlotPanel.GAP_AXIS_TITLE;
            int ascent = m_titleFontMetrics.getAscent();
            int descent = m_titleFontMetrics.getDescent();
            int baseline = top + ((bottom + 1 - top) / 2) - ((ascent + descent) / 2) + ascent;
            g.drawString(m_title, baseline , m_y+(m_height - titleWidth) / 2);
            
            // restore font (necessary due to affine transform
            g.setFont(prevFont);
        }
        
        
    }
    
    public void paintGrid(Graphics2D g, int x, int width) {

        int pixelStart = valueToPixel(m_minTick);
        int pixelStop = valueToPixel(m_maxTick);

        if (pixelStart <= pixelStop) { // avoid infinite loop 
            return;
        }

        g.setColor(CyclicColorPalette.GRAY_GRID);

        double y = m_minTick;
        int pY = pixelStart;
        while (true) {
            g.drawLine(x+1, pY, x+width, pY);
            
            y += m_tickSpacing;
            pY = valueToPixel(y);
            if (pY<pixelStop) {
                break;
            }
        }
    }
    
    @Override
    public int valueToPixel(double v) {
        return (m_y+m_height)-(int) Math.round(((v - m_minTick)/(m_maxTick-m_minTick)) * m_height);
    }

    @Override
    public double pixelToValue(int pixel) {
        return m_minTick+(((double)((m_y+m_height)-pixel))/((double)m_height))*(m_maxTick-m_minTick);
    }
   
    
}