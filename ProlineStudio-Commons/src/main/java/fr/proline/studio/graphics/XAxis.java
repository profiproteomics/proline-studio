package fr.proline.studio.graphics;

import fr.proline.studio.utils.CyclicColorPalette;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

/**
 * X Axis
 *
 * @author JM235353
 */
public class XAxis extends Axis {

    public XAxis() {

    }

    @Override
    public void paint(Graphics2D g) {

        if (m_valuesFont == null) {
            m_valuesFont = g.getFont().deriveFont(Font.PLAIN, 10);
            m_valuesFontMetrics = g.getFontMetrics(m_valuesFont);
        }
        
        if (m_selected) {
            int stringWidth = m_valuesFontMetrics.stringWidth("    ");
            g.setColor(Color.darkGray);
            g.fillRect(m_x-stringWidth, m_y, m_width+stringWidth*2, m_height);
        }

        int maxTicks = m_width / 30;

        AxisTicks ticks = new AxisTicks(m_minValue, m_maxValue, maxTicks, m_log);

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

        // display title
        if (m_title != null) {
            if (m_titleFont == null) {
                m_titleFont = g.getFont().deriveFont(Font.BOLD, 11);
                m_titleFontMetrics = g.getFontMetrics(m_titleFont);
            }
            g.setFont(m_titleFont);
            if (m_selected) {
                g.setColor(Color.white);
            } else {
                g.setColor(Color.black);
            }
            int titleWidth = m_titleFontMetrics.stringWidth(m_title);
            int bottom = m_y + m_height;
            int top = m_y + m_height - PlotPanel.GAP_AXIS_TITLE;
            int ascent = m_titleFontMetrics.getAscent();
            int descent = m_titleFontMetrics.getDescent();
            int baseline = top + ((bottom + 1 - top) / 2) - ((ascent + descent) / 2) + ascent;
            g.drawString(m_title, (m_width - titleWidth) / 2, baseline);
        }

    }

    private void paintLinear(Graphics2D g, AxisTicks ticks) {
        m_minTick = ticks.getTickMin();
        m_maxTick = ticks.getTickMax();
        m_tickSpacing = ticks.getTickSpacing();

        int digits = ticks.getDigits();

        if ((digits != m_digits) || (m_df == null)) {
            m_df = selectDecimalFormat(digits);
            m_dfPlot = selectDecimalFormat(digits+2);
            m_digits = digits;
        }

        int pixelStart = valueToPixel(m_minTick);
        int pixelStop = valueToPixel(m_maxTick);
        g.drawLine(pixelStart, m_y, pixelStop, m_y);

        if (pixelStart >= pixelStop) { // avoid infinite loop 
            return;
        }

        if (m_selected) {
            g.setColor(Color.white);
        } else {
            g.setColor(CyclicColorPalette.GRAY_TEXT_DARK);
        }

        
        g.setFont(m_valuesFont);

        int height = m_valuesFontMetrics.getHeight();

        double multForRounding = Math.pow(10, digits);

        double x = m_minTick;
        int pX = pixelStart;
        int previousEndX = -Integer.MAX_VALUE;
        while (true) {
            g.drawLine(pX, m_y, pX, m_y + 4);

            // round x
            double xDisplay = x;
            if (digits > 0) {
                xDisplay = StrictMath.round(xDisplay * multForRounding) / multForRounding;
            }

            String s = m_df.format(xDisplay);
            int stringWidth = m_valuesFontMetrics.stringWidth(s);

            int posX = pX - stringWidth / 2;
            if (posX > previousEndX + 2) { // check to avoid to overlap labels
                g.drawString(s, posX, m_y + height + 4);
                previousEndX = posX + stringWidth;
            }

            x += m_tickSpacing;
            pX = valueToPixel(x);
            if (pX > pixelStop) {
                break;
            }

        }

    }

    private void paintLog(Graphics2D g, AxisTicks ticks) {
        m_minTick = ticks.getTickMin();
        m_maxTick = ticks.getTickMax();
        m_tickSpacing = ticks.getTickSpacing();

        if (m_df == null) {
            m_df = selectDecimalFormat(-1);
            m_dfPlot = selectDecimalFormat(-1);
        }

        int pixelStart = valueToPixel(Math.pow(10, m_minTick));
        int pixelStop = valueToPixel(Math.pow(10, m_maxTick));
        g.drawLine(pixelStart, m_y, pixelStop, m_y);

        if (pixelStart >= pixelStop) { // avoid infinite loop 
            return;
        }
 
        g.setFont(m_valuesFont);

        int height = m_valuesFontMetrics.getHeight();

        double x = m_minTick;
        int pX = pixelStart;
        int previousEndX = -Integer.MAX_VALUE;
        while (true) {
            
            if (m_selected) {
                g.setColor(Color.white);
            } else {
                g.setColor(CyclicColorPalette.GRAY_TEXT_DARK);
            }
            
            g.drawLine(pX, m_y, pX, m_y + 4);

            // round x
            double xDisplay = x;

            String s = m_df.format(xDisplay);
            int stringWidth = m_valuesFontMetrics.stringWidth(s);

            int posX = pX - stringWidth / 2;
            if (posX > previousEndX + 2) { // check to avoid to overlap labels
                g.drawString(s, posX, m_y + height + 4);
                previousEndX = posX + stringWidth;
            }

            x += m_tickSpacing;
            pX = valueToPixel(Math.pow(10, x));
            if (pX > pixelStop) {
                break;
            }
            
            if (m_selected) {
                g.setColor(Color.white);
            } else {
                g.setColor(CyclicColorPalette.GRAY_TEXT_LIGHT);
            }
            
            // display min ticks between two major ticks
            for (int i=2;i<=9;i++) {
                double xMinTick = Math.pow(10, x)*(((double)i)*0.1d);
                int pMinTick = valueToPixel(xMinTick);
                 g.drawLine(pMinTick, m_y, pMinTick, m_y + 4);                
            }

        }

    }

        public void paintGrid(Graphics2D g, int y, int height) {

        if (m_log) {
            paintGridLog(g, y, height);
        } else {
            paintGridLinear(g, y, height);
        }

    }
    
    public void paintGridLinear(Graphics2D g, int y, int height) {

        int pixelStart = valueToPixel(m_minTick);
        int pixelStop = valueToPixel(m_maxTick);

        if (pixelStart >= pixelStop) { // avoid infinite loop 
            return;
        }

        g.setColor(CyclicColorPalette.GRAY_GRID);

        double x = m_minTick;
        int pX = pixelStart;
        while (true) {
            g.drawLine(pX, y, pX, y + height - 1);

            x += m_tickSpacing;
            pX = valueToPixel(x);
            if (pX > pixelStop) {
                break;
            }
        }
    }
    
    public void paintGridLog(Graphics2D g, int y, int height) {

        int pixelStart = valueToPixel(Math.pow(10, m_minTick));
        int pixelStop = valueToPixel(Math.pow(10, m_maxTick));

        if (pixelStart >= pixelStop) { // avoid infinite loop 
            return;
        }

        

        double x = m_minTick;
        int pX = pixelStart;
        while (true) {
            g.setColor(CyclicColorPalette.GRAY_GRID);
            g.drawLine(pX, y, pX, y + height - 1);

            x += m_tickSpacing;
            pX = valueToPixel(Math.pow(10, x));
            if (pX > pixelStop) {
                break;
            }
            
            g.setColor(CyclicColorPalette.GRAY_GRID_LOG);
            
            // display min ticks between two major ticks
            for (int i=2;i<=9;i++) {
                double xMinTick = Math.pow(10, x)*(((double)i)*0.1d);
                int pMinTick = valueToPixel(xMinTick);
                g.drawLine(pMinTick, y, pMinTick, y + height - 1);
            }
        }
    }

    @Override
    public int valueToPixel(double v) {
        if (m_log) {
            v = Math.log10(v);
        }
        return m_x + (int) Math.round(((v - m_minTick) / (m_maxTick - m_minTick)) * m_width);
    }

    @Override
    public double pixelToValue(int pixel) {
        double v = m_minTick + ((((double) pixel) - m_x) / ((double) m_width)) * (m_maxTick - m_minTick);
        if (m_log) {
            v = Math.pow(10, v);
        }
        return v;
    }

}
