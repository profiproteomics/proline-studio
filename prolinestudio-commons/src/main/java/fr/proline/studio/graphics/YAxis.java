/* 
 * Copyright (C) 2019
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
package fr.proline.studio.graphics;

import fr.proline.studio.graphics.cursor.AbstractCursor;
import fr.proline.studio.utils.CyclicColorPalette;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.text.DecimalFormat;

/**
 * Y Axis
 *
 * @author JM235353
 */
public class YAxis extends Axis {

    private AxisTicks m_ticks;

    private int m_lastHeight;
    private boolean m_isLeftAxis;
    private int m_lineXStart;
    private Color m_rightRefColor;

    public YAxis(BasePlotPanel p) {
        super(p);
        this.m_isLeftAxis = true;

    }

    public void setSecondAxis() {
        this.m_isLeftAxis = false;

    }

    public void setColorOnTitle(Color color) {
        m_rightRefColor = color;
    }

    private void calculLineXStart() {
        if (!m_isLeftAxis) {
            m_lineXStart = m_x + BasePlotPanel.GAP_AXIS_LINE;
        } else {
            m_lineXStart = m_x + m_width - BasePlotPanel.GAP_AXIS_LINE;
        }

    }

    @Override
    public void setSize(int x, int y, int width, int height) {
        super.setSize(x, y, width, height);
        calculLineXStart();
    }

    @Override
    public void paint(Graphics2D g) {
        if (m_valuesFont == null) {
            m_valuesFont = g.getFont().deriveFont(Font.PLAIN, 10);
            m_valuesFontMetrics = g.getFontMetrics(m_valuesFont);
        }

        if (m_selected) {
            g.setColor(Color.darkGray);
            g.fillRect(m_x, m_y - m_valuesFontMetrics.getHeight() / 2, m_width, m_height + m_valuesFontMetrics.getHeight());
        }

        int maxTicks = m_height / 20;

        m_ticks = new AxisTicks(m_minValue, m_maxValue, maxTicks, m_log, LOG_MIN_VALUE, m_isInteger, m_isEnum);

        if (m_selected) {
            g.setColor(Color.white);
        } else {
            g.setColor(Color.black);
        }

        if (m_log) {
            paintLog(g, m_ticks);
        } else {
            paintLinear(g, m_ticks);
        }

        if ((m_title != null) && (m_displayTitle)) {
            drawTitle(g);
        }
    }

    private void drawTitle(Graphics2D g) {
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
            int width = BasePlotPanel.GAP_AXIS_TITLE;
            int ascent = m_titleFontMetrics.getAscent();
            int descent = m_titleFontMetrics.getDescent();

            int bottom = (m_isLeftAxis) ? m_x : m_lineXStart + BasePlotPanel.GAP_FIGURES_Y;
            int top = bottom + width;
            int baseline = top + ((bottom - top) / 2) - ((ascent + descent) / 2) + ascent;
            int sideLength = width / 2;
            if (!m_isLeftAxis && m_rightRefColor != null) {
                Color txtColor = g.getColor();
                g.setColor(m_rightRefColor);
                int bx = bottom + width / 3;
                int by = m_y + (m_height - titleWidth - sideLength) / 2;
                g.fillRect(bx, by, sideLength, sideLength);//draw a colored square
                g.setColor(txtColor);
            }
            g.drawString(m_title, baseline, m_y + (m_height + titleWidth + sideLength) / 2);
            // restore font (necessary due to affine transform
            g.setFont(prevFont);
        }
    }

    @Override
    public void paintCursor(Graphics2D g, AbstractCursor cursor, boolean selected) {

        g.setFont(m_valuesFont);

        final int DELTA = 3;

        double y = cursor.getValue();

        int integerDigits = cursor.getIntegerDigits();
        if (integerDigits == -1) {
            integerDigits = m_ticks.getIntegerDigits();
        }
        int fractionalDigits = cursor.getFractionalDigits();
        if (fractionalDigits == -1) {
            fractionalDigits = m_ticks.getFractionalDigits() + 2;
        }

        double multForRounding = Math.pow(10, fractionalDigits);

        String label;
        int stringWidth;

        int height;
        if (m_isEnum) {
            label = m_plotPanel.getEnumValueY((int) Math.round(y), false, this); //JPM.WART
            stringWidth = m_valuesFontMetrics.stringWidth(label);
            height = (int) Math.round(StrictMath.ceil(m_valuesFontMetrics.getLineMetrics(label, g).getHeight()));
        } else {
            // round y
            double yDisplay = y;
            if (fractionalDigits > 0) {
                yDisplay = StrictMath.round(yDisplay * multForRounding) / multForRounding;
            }

            DecimalFormat df = selectDecimalFormat(fractionalDigits + 2, integerDigits);

            cursor.setFormat(integerDigits, fractionalDigits, df);

            label = df.format(yDisplay);
            stringWidth = m_valuesFontMetrics.stringWidth(label);
            height = (int) Math.round(StrictMath.ceil(m_valuesFontMetrics.getLineMetrics(label, g).getHeight()));
        }

        Stroke prevStroke = g.getStroke();
        g.setStroke(selected ? AbstractCursor.LINE2_STROKE : cursor.getStroke());

        g.setFont(m_valuesFont);

        int halfAscent = m_valuesFontMetrics.getAscent() / 2;

        int pY = valueToPixel(y);

        int x1 = m_x + m_width - stringWidth - 6 - BasePlotPanel.GAP_AXIS_LINE - DELTA;
        int x2 = m_x + m_width - BasePlotPanel.GAP_AXIS_LINE;
        g.setColor(Color.white);
        g.fillRect(x1, pY - halfAscent - DELTA, x2 - x1, height + DELTA * 2);

        g.setColor(cursor.getColor());
        g.drawRect(x1, pY - halfAscent - DELTA, x2 - x1, height + DELTA * 2);

        g.drawString(label, m_lineXStart - stringWidth - 6, pY + halfAscent);
        g.drawLine(m_lineXStart, pY, m_lineXStart - BasePlotPanel.GAP_AXIS_LINE, pY);

        // restore stroke
        g.setStroke(prevStroke);

    }

    /**
     * paint linear axis with ticks
     *
     * @param g
     * @param ticks
     */
    private void paintLinear(Graphics2D g, AxisTicks ticks) {

        m_minTick = ticks.getTickMin();
        m_maxTick = ticks.getTickMax();
        m_tickSpacing = ticks.getTickSpacing();

        int fractionalDigits = ticks.getFractionalDigits();
        int integerDigits = ticks.getIntegerDigits();
        if ((fractionalDigits != m_fractionalDigits) || (integerDigits != m_integerDigits) || (m_df == null)) {
            m_df = selectDecimalFormat(fractionalDigits, integerDigits);
            m_dfPlot = selectDecimalFormat(fractionalDigits + 2, integerDigits);
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
        //draw the line
        int pixelStart = valueToPixel(m_minValue);
        int pixelStop = valueToPixel(m_maxValue);
        g.drawLine(m_lineXStart, pixelStart, m_lineXStart, pixelStop);

        if (pixelStart <= pixelStop) { // avoid infinite loop when histogram is flat
            return;
        }

        double multForRounding = Math.pow(10, fractionalDigits);

        m_lastHeight = -1;
        double y = m_minTick;
        if (valueToPixel(y) > pixelStart) {
            y += m_tickSpacing;
        }
        int pY = valueToPixel(y);
        int previousEndY = Integer.MAX_VALUE;

        while (true) {

            String label;
            int stringWidth;

            if (m_isEnum) {
                label = m_plotPanel.getEnumValueY((int) Math.round(y), false, this); //JPM.WART
                if (label == null) {
                    label = " ";
                }
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
            // draw ticks & it's label
            int tickX = ((m_isLeftAxis) ? m_lineXStart : m_lineXStart + 4);
            int tickStringX = ((m_isLeftAxis) ? m_lineXStart - stringWidth - 6 : m_lineXStart + 6);

            if (pY < previousEndY - m_lastHeight - 2) { // check to avoid to overlap labels
                g.drawString(label, tickStringX, pY + halfAscent);
                g.drawLine(tickX, pY, tickX - 4, pY);
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

        int pixelStart = valueToPixel(m_minValue);
        int pixelStop = valueToPixel(m_maxValue);
        g.drawLine(m_lineXStart, pixelStart, m_lineXStart, pixelStop);

        if (pixelStart <= pixelStop) { // avoid infinite loop when histogram is flat
            return;
        }

        m_lastHeight = -1;
        int previousEndY = Integer.MAX_VALUE;

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

            int height = (int) Math.round(StrictMath.ceil(m_valuesFontMetrics.getLineMetrics(s, g).getHeight()));
            if (height > m_lastHeight) {
                m_lastHeight = height;
            }

            // draw ticks & it's label
            int tickX = ((m_isLeftAxis) ? m_lineXStart : m_lineXStart + 4);
            int tickStringX = ((m_isLeftAxis) ? m_lineXStart - stringWidth - 6 : m_lineXStart + 6);
            int delta = pY-valueToPixel(Math.pow(10.0,y)); // used to check that tick is inside the visible area
            if ((pY < previousEndY - m_lastHeight - 2) && (delta == 0)) { // check to avoid to overlap labels
                g.drawString(s, tickStringX, pY + halfAscent);
                g.drawLine(tickX, pY, tickX - 4, pY);
                previousEndY = pY;
            }

            y += m_tickSpacing;
            pY = valueToPixel(Math.pow(10, y));



            if (m_selected) {
                g.setColor(Color.white);
            } else {
                g.setColor(CyclicColorPalette.GRAY_TEXT_LIGHT);
            }
            tickX = ((m_isLeftAxis) ? m_lineXStart : m_lineXStart + 3);
            // display min ticks between two major ticks
            for (int i = 2; i <= 9; i++) {
                double yMinTick = Math.pow(10, y) * (((double) i) * 0.1d);
                int pMinTick = valueToPixel(yMinTick);
                if ((pMinTick>=pixelStop) && (pMinTick<=pixelStart)) {
                    g.drawLine(tickX, pMinTick, tickX - 3, pMinTick);
                }
            }
            
            if (pY < pixelStop) {
                break;
            }

        }
    }

    public void paintGrid(Graphics2D g, int x, int width, int y, int height) {

        if (m_log) {
            paintGridLog(g, x, width);
        } else {
            paintGridLinear(g, x, width, y, height);
        }

    }

    public void paintGridLinear(Graphics2D g, int xPixel, int width, int yPixel, int height) {

        int pixelStart = valueToPixel(m_minTick);
        int pixelStop = valueToPixel(m_maxTick);

        if (pixelStart <= pixelStop) { // avoid infinite loop 
            return;
        }

        g.setColor(CyclicColorPalette.GRAY_GRID);
        Stroke s = g.getStroke();
        g.setStroke(DASHED);

        double y = m_minTick;
        int pY = pixelStart;
        int previousEndY = Integer.MAX_VALUE;
        while (true) {

            if (pY < previousEndY - m_lastHeight - 2) { // check to avoid to draw line for overlap labels
                if ((pY >= yPixel) && (pY <= yPixel + height)) {
                    g.drawLine(xPixel + 1, pY, xPixel + width, pY);
                }
                previousEndY = pY;
            }

            y += m_tickSpacing;
            pY = valueToPixel(y);
            if (pY < pixelStop) {
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
        g.setStroke(DASHED);

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
            for (int i = 2; i <= 9; i++) {
                double yMinTick = Math.pow(10, y) * (((double) i) * 0.1d);
                int pMinTick = valueToPixel(yMinTick);
                g.drawLine(x + 1, pMinTick, x + width, pMinTick);

            }
        }

        g.setStroke(s);
    }

    @Override
    public int valueToPixel(double v) {
        if (m_isPixel) {
            return (int) Math.round(m_maxValue-v);
        }
        if (m_log) {
            double logV = ((v <= LOG_MIN_VALUE) || Double.isNaN(v)) ? Math.log10(LOG_MIN_VALUE) : Math.log10(v);
            double min = Math.log10(m_minValue <= LOG_MIN_VALUE ? LOG_MIN_VALUE : m_minValue);
            double max = Math.log10(m_maxValue <= LOG_MIN_VALUE ? LOG_MIN_VALUE*10 : m_maxValue);
            return (m_y + m_height) - (int) Math.round(((logV - min) / (max - min)) * m_height);
        } else {
            return (m_y + m_height) - (int) Math.round(((v - m_minValue) / (m_maxValue - m_minValue)) * m_height);
        }
    }

    @Override
    public double pixelToValue(int pixel) {

        if (m_log) {
            double min = Math.log10(m_minValue <= LOG_MIN_VALUE ? LOG_MIN_VALUE : m_minValue);
            double max = Math.log10(m_maxValue <= LOG_MIN_VALUE ? LOG_MIN_VALUE*10 : m_maxValue);
            double v = min + (((double) ((m_y + m_height) - pixel)) / ((double) m_height)) * (max - min);
            v = Math.pow(10, v);
            return v;
        } else {
            double v = m_minValue + (((double) ((m_y + m_height) - pixel)) / ((double) m_height)) * (m_maxValue - m_minValue);
            return v;
        }

    }
    
    @Override
    public double deltaPixelToDeltaValue(int deltaPixel) {
        double value1 = pixelToValue(m_height/2);
        double value2 = pixelToValue(m_height/2+deltaPixel);
        return value2-value1;
    }
    
    public double deltaPixelToLogMultValue(int deltaPixel) {
        double min = Math.log10(m_minValue <= LOG_MIN_VALUE ? LOG_MIN_VALUE : m_minValue);
        double max = Math.log10(m_maxValue <= LOG_MIN_VALUE ? LOG_MIN_VALUE*10 : m_maxValue);
        double mult = Math.pow(10,(max-min)*(((double) deltaPixel)/((double) m_height))); 
        if (Math.pow(10,min)/mult<LOG_MIN_VALUE) {
            mult = Math.pow(10, min)/LOG_MIN_VALUE;
        }
        return mult;
    }

}
