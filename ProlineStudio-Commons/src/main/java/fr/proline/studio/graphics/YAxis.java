package fr.proline.studio.graphics;

import fr.proline.studio.utils.CyclicColorPalette;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;

/**
 * Y Axis
 *
 * @author JM235353
 */
public class YAxis extends Axis {

    private int m_lastHeight;
    
    public YAxis(PlotPanel p) {
        super(p);
    }

    @Override
    public void paint(Graphics2D g) {

        if (m_valuesFont == null) {
            m_valuesFont = g.getFont().deriveFont(Font.PLAIN, 10);
            m_valuesFontMetrics = g.getFontMetrics(m_valuesFont);
        }
        
        if (m_selected) {
            g.setColor(Color.darkGray);
            g.fillRect(m_x, m_y-m_valuesFontMetrics.getHeight()/2, m_width, m_height+m_valuesFontMetrics.getHeight());
        }

        int maxTicks = m_height / 20;

        AxisTicks ticks = new AxisTicks(m_minValue, m_maxValue, maxTicks, m_log, m_isInteger, m_isEnum);

        if (m_selected) {
            g.setColor(Color.white);
        } else {
            g.setColor(Color.black);
        }

        if (m_log) {
            paintLog(g, ticks);
        } else {
            paintLinear(g, ticks);
        }
        
        if (m_title != null) {
            if (m_titleFont == null) {
                AffineTransform affineTr = new AffineTransform();
                affineTr.rotate(-Math.PI / 2);
                m_titleFont = g.getFont().deriveFont(Font.BOLD, 11);
                m_titleFontMetrics = g.getFontMetrics(m_titleFont);
                m_titleFont = m_titleFont.deriveFont(affineTr);
            }
            Font prevFont = g.getFont();

            g.setFont(m_titleFont);
            if (m_selected) {
                g.setColor(Color.white);
            } else {
                g.setColor(Color.black);
            }
            int titleWidth = m_titleFontMetrics.stringWidth(m_title);
            int bottom = m_x;
            int top = m_x + PlotPanel.GAP_AXIS_TITLE;
            int ascent = m_titleFontMetrics.getAscent();
            int descent = m_titleFontMetrics.getDescent();
            int baseline = top + ((bottom + 1 - top) / 2) - ((ascent + descent) / 2) + ascent;
            g.drawString(m_title, baseline, m_y + (m_height + titleWidth) / 2);

            // restore font (necessary due to affine transform
            g.setFont(prevFont);
        }
        
    }

    private void paintLinear(Graphics2D g, AxisTicks ticks) {

        m_minTick = ticks.getTickMin();
        m_maxTick = ticks.getTickMax();
        m_tickSpacing = ticks.getTickSpacing();

        int fractionalDigits = ticks.getFractionalDigits();
        int integerDigits = ticks.getIntegerDigits();
        if ((fractionalDigits != m_fractionalDigits) || (integerDigits != m_integerDigits) || (m_df == null)) {
            m_df = selectDecimalFormat(fractionalDigits, integerDigits);
            m_dfPlot = selectDecimalFormat(fractionalDigits+2, integerDigits);
            m_fractionalDigits = fractionalDigits;
            m_integerDigits = integerDigits;
        }

        if (m_selected) {
            g.setColor(Color.white);
        } else {
            g.setColor(CyclicColorPalette.GRAY_TEXT_DARK);
        }

        
        g.setFont(m_valuesFont);

        int halfAscent = m_valuesFontMetrics.getAscent() / 2;

        int pixelStart = valueToPixel(m_minTick);
        int pixelStop = valueToPixel(m_maxTick);
        g.drawLine(m_x+m_width-PlotPanel.GAP_AXIS_LINE, pixelStart, m_x+m_width-PlotPanel.GAP_AXIS_LINE, pixelStop);

        if (pixelStart <= pixelStop) { // avoid infinite loop when histogram is flat
            return;
        }

        double multForRounding = Math.pow(10, fractionalDigits);

        m_lastHeight = -1;
        double y = m_minTick;
        int pY = pixelStart;
        int previousEndY = Integer.MAX_VALUE;
        while (true) {



            String label;
            int stringWidth;
            
            
            
            if (m_isEnum) {
                label = m_plotPanel.getEnumValueY((int) Math.round(y), false); //JPM.WART
                stringWidth = m_valuesFontMetrics.stringWidth(label);
                int height = (int) Math.round(StrictMath.ceil(m_valuesFontMetrics.getLineMetrics(label, g).getHeight()));
                if (height > m_lastHeight) {
                    m_lastHeight = height;
                }
            } else {
                // round y
                double yDisplay = y;
                if (fractionalDigits > 0) {
                    yDisplay = StrictMath.round(yDisplay * multForRounding) / multForRounding;
                }
                label = m_df.format(yDisplay);
                stringWidth = m_valuesFontMetrics.stringWidth(label);
                int height = (int) Math.round(StrictMath.ceil(m_valuesFontMetrics.getLineMetrics(label, g).getHeight()));
                if (height > m_lastHeight) {
                    m_lastHeight = height;
                }
            }

            
            if (pY < previousEndY - m_lastHeight - 2) { // check to avoid to overlap labels
                g.drawString(label, m_x + m_width - stringWidth - 6-PlotPanel.GAP_AXIS_LINE, pY + halfAscent);
                g.drawLine(m_x + m_width-PlotPanel.GAP_AXIS_LINE, pY, m_x + m_width - 4-PlotPanel.GAP_AXIS_LINE, pY);
                previousEndY = pY;
            }
            
            
            y += m_tickSpacing;
            pY = valueToPixel(y);
            if (pY < pixelStop) {
                break;
            }
        }



    }

    private void paintLog(Graphics2D g, AxisTicks ticks) {

        m_minTick = ticks.getTickMin();
        m_maxTick = ticks.getTickMax();
        m_tickSpacing = ticks.getTickSpacing();

        if (m_df == null) {
            m_df = selectLogDecimalFormat();
            m_dfPlot = selectLogDecimalFormat();
        }

        if (m_selected) {
            g.setColor(Color.white);
        } else {
            g.setColor(CyclicColorPalette.GRAY_TEXT_DARK);
        }

        
        g.setFont(m_valuesFont);

        int halfAscent = m_valuesFontMetrics.getAscent() / 2;

        int pixelStart = valueToPixel(Math.pow(10, m_minTick));
        int pixelStop = valueToPixel(Math.pow(10, m_maxTick));
        g.drawLine(m_x+m_width-PlotPanel.GAP_AXIS_LINE, pixelStart, m_x+m_width-PlotPanel.GAP_AXIS_LINE, pixelStop);

        if (pixelStart <= pixelStop) { // avoid infinite loop when histogram is flat
            return;
        }

        double y = m_minTick;
        int pY = pixelStart;
        while (true) {

            // round y
            double yDisplay = y;


            
            String s = m_df.format(yDisplay);
            int stringWidth = m_valuesFontMetrics.stringWidth(s);

            if (m_selected) {
                g.setColor(Color.white);
            } else {
                g.setColor(CyclicColorPalette.GRAY_TEXT_DARK);
            }
            
            g.drawString(s, m_x+m_width - stringWidth - 6-PlotPanel.GAP_AXIS_LINE, pY + halfAscent);

            g.drawLine(m_x+m_width-PlotPanel.GAP_AXIS_LINE, pY, m_x+m_width-PlotPanel.GAP_AXIS_LINE - 4, pY);

            y += m_tickSpacing;
            pY = valueToPixel(Math.pow(10,y));
            

            
            if (pY < pixelStop) {
                break;
            }
            
            if (m_selected) {
                g.setColor(Color.white);
            } else {
                g.setColor(CyclicColorPalette.GRAY_TEXT_LIGHT);
            }
            
            // display min ticks between two major ticks
            for (int i=2;i<=9;i++) {
                double yMinTick = Math.pow(10, y)*(((double)i)*0.1d);
                int pMinTick = valueToPixel(yMinTick);
                g.drawLine(m_x+m_width-PlotPanel.GAP_AXIS_LINE, pMinTick, m_x+m_width - 3-PlotPanel.GAP_AXIS_LINE, pMinTick);
                
            }
            
        }



    }
    
    public void paintGrid(Graphics2D g, int x, int width) {

        if (m_log) {
            paintGridLog(g, x, width);
        } else {
            paintGridLinear(g, x, width);
        }

    }
    
    public void paintGridLinear(Graphics2D g, int x, int width) {
                
        int pixelStart = valueToPixel(m_minTick);
        int pixelStop = valueToPixel(m_maxTick);

        if (pixelStart <= pixelStop) { // avoid infinite loop 
            return;
        }

        g.setColor(CyclicColorPalette.GRAY_GRID);
        Stroke s = g.getStroke();
        g.setStroke(dashed);
        
        double y = m_minTick;
        int pY = pixelStart;
        int previousEndY = Integer.MAX_VALUE;
        while (true) {
            
            if (pY < previousEndY - m_lastHeight - 2) { // check to avoid to draw line for overlap labels
                g.drawLine(x+1, pY, x+width, pY);
                previousEndY = pY;
            }

            y += m_tickSpacing;
            pY = valueToPixel(y);
            if (pY<pixelStop) {
                break;
            }
            
        }
        g.setStroke(s);
    }
    
    public void paintGridLog(Graphics2D g, int x, int width) {
        int pixelStart = valueToPixel(Math.pow(10, m_minTick));
        int pixelStop = valueToPixel(Math.pow(10, m_maxTick));

        if (pixelStart <= pixelStop) { // avoid infinite loop 
            return;
        }
        
        Stroke s = g.getStroke();
        g.setStroke(dashed);
        
        

        double y = m_minTick;
        int pY = pixelStart;
        while (true) {
            g.setColor(CyclicColorPalette.GRAY_GRID);
            g.drawLine(x + 1, pY, x + width, pY);

            y += m_tickSpacing;
            pY = valueToPixel(Math.pow(10, y));
            if (pY < pixelStop) {
                break;
            }
            
            g.setColor(CyclicColorPalette.GRAY_GRID_LOG);
            
            // display min ticks between two major ticks
            for (int i=2;i<=9;i++) {
                double yMinTick = Math.pow(10, y)*(((double)i)*0.1d);
                int pMinTick = valueToPixel(yMinTick);
                g.drawLine(x + 1, pMinTick, x + width, pMinTick);
                
            }
        }
        
        g.setStroke(s);
    }
    
    @Override
    public int valueToPixel(double v) {
        if (m_log) {
            v = Math.log10(v);
        }
        return (m_y+m_height)-(int) Math.round(((v - m_minTick)/(m_maxTick-m_minTick)) * m_height);
    }

    @Override
    public double pixelToValue(int pixel) {
        double v = m_minTick+(((double)((m_y+m_height)-pixel))/((double)m_height))*(m_maxTick-m_minTick);
        if (m_log) {
            v = Math.pow(10,v);
        }
        return v;
    }
   
    
}