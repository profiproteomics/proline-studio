package fr.proline.studio.graphics;

import fr.proline.studio.graphics.cursor.AbstractCursor;
import fr.proline.studio.graphics.panel.DoubleYAxisPlotPanel;
import fr.proline.studio.utils.CyclicColorPalette;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.text.DecimalFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * X Axis
 *
 * @author JM235353
 */
public class XAxis extends Axis {

    private static final Logger m_logger = LoggerFactory.getLogger(XAxis.class);
    private static final double FONT_ROTATE = Math.PI / 6;

    private int m_lastWidth;

    private AxisTicks m_ticks;

    public XAxis(BasePlotPanel p) {
        super(p);
    }

    @Override
    public void paint(Graphics2D g) {
        m_logger.debug(String.format("___________AxisX width=%d", m_width));
        if (m_selected) {
            int stringWidth = m_valuesFontMetrics.stringWidth("    ");
            g.setColor(Color.darkGray);
            g.fillRect(m_x - stringWidth, m_y, m_width + stringWidth * 2, m_height);
        }

        if (m_selected) {
            g.setColor(Color.white);
        } else {
            g.setColor(Color.black);
        }

        //display Axis line
        if (m_log) {
            paintLog(g, m_ticks);
        } else {
            paintLinear(g, m_ticks);
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
            int top = m_y + m_height - BasePlotPanel.GAP_AXIS_TITLE - BasePlotPanel.GAP_AXIS_LINE;
            int ascent = m_titleFontMetrics.getAscent();
            int descent = m_titleFontMetrics.getDescent();
            int baseline = top + ((bottom + 1 - top) / 2) - ((ascent + descent) / 2) + ascent;
            g.drawString(m_title, m_x + (m_width - titleWidth) / 2, baseline);
        }

    }

    @Override
    public void paintCursor(Graphics2D g, AbstractCursor cursor, boolean selected) {

        g.setFont(m_valuesFont);

        final int DELTA = 3;

        double x = cursor.getValue();

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
        if (m_isEnum) {
            label = m_plotPanel.getEnumValueX((int) Math.round(x), false); //JPM.WART
            if (label == null) {
                label = " ";
            }
        } else {
            // round x
            double xDisplay = (m_log) ? StrictMath.log10(x) : x;
            if (fractionalDigits > 0) {
                xDisplay = StrictMath.round(xDisplay * multForRounding) / multForRounding;
            }

            DecimalFormat df = selectDecimalFormat(fractionalDigits + 2, integerDigits);

            label = df.format(xDisplay);

            cursor.setFormat(integerDigits, fractionalDigits, df);
        }

        int stringWidth = m_valuesFontMetrics.stringWidth(label);

        int posX = valueToPixel(x);

        int height = m_valuesFontMetrics.getHeight();

        Stroke prevStroke = g.getStroke();
        g.setStroke(selected ? AbstractCursor.LINE2_STROKE : cursor.getStroke());

        g.setColor(Color.white);

        // check paint at right
        int rightLimitX = valueToPixel(m_maxValue);
        int flagPositionX = posX + stringWidth + DELTA * 2;
        if (flagPositionX < rightLimitX) {
            // Cursor X Flag painted at right
            g.fillRect(posX, m_y + 4 + BasePlotPanel.GAP_AXIS_LINE - DELTA, stringWidth + DELTA * 2, height + DELTA * 2);

            g.setColor(cursor.getColor());
            g.drawLine(posX, m_y, posX, m_y + 4 + BasePlotPanel.GAP_AXIS_LINE - DELTA);
            g.drawRect(posX, m_y + 4 + BasePlotPanel.GAP_AXIS_LINE - DELTA, stringWidth + DELTA * 2, height + DELTA * 2);

            g.drawString(label, posX + DELTA, m_y + height + 4 + BasePlotPanel.GAP_AXIS_LINE);
        } else {
            // Cursor X Flag painted at left
            g.fillRect(posX - (stringWidth + DELTA * 2), m_y + 4 + BasePlotPanel.GAP_AXIS_LINE - DELTA, stringWidth + DELTA * 2, height + DELTA * 2);

            g.setColor(cursor.getColor());
            g.drawLine(posX, m_y, posX, m_y + 4 + BasePlotPanel.GAP_AXIS_LINE - DELTA);
            g.drawRect(posX - (stringWidth + DELTA * 2), m_y + 4 + BasePlotPanel.GAP_AXIS_LINE - DELTA, stringWidth + DELTA * 2, height + DELTA * 2);

            g.drawString(label, posX - stringWidth - DELTA, m_y + height + 4 + BasePlotPanel.GAP_AXIS_LINE);
        }

        // restore stroke
        g.setStroke(prevStroke);

    }

    public String defaultFormat(double x) {

        int integerDigits = m_ticks.getIntegerDigits();
        int fractionalDigits = m_ticks.getFractionalDigits() + 2;

        double multForRounding = Math.pow(10, fractionalDigits);

        String label;
        if (m_isEnum) {
            label = m_plotPanel.getEnumValueX((int) Math.round(x), false); //JPM.WART
            if (label == null) {
                label = " ";
            }
        } else {
            // round x
            double xDisplay = (m_log) ? StrictMath.log10(x) : x;
            if (fractionalDigits > 0) {
                xDisplay = StrictMath.round(xDisplay * multForRounding) / multForRounding;
            }

            DecimalFormat df = selectDecimalFormat(fractionalDigits + 2, integerDigits);

            label = df.format(xDisplay);

        }

        return label;

    }

    /**
     * Main purpose of this function is to evaluate if we need to plot labels in
     * diagonal when there is not enough space
     *
     * @param g
     */
    public void preparePaint(Graphics2D g) {

        m_labelMaxWidth = 0;
        m_mustDrawDiagonalLabels = false;

        if (m_valuesFont == null) {
            m_valuesFont = g.getFont().deriveFont(Font.PLAIN, 10);
            m_valuesFontMetrics = g.getFontMetrics(m_valuesFont);
        }

        int maxTicks = m_width / 30;
        m_ticks = new AxisTicks(m_minValue, m_maxValue, maxTicks, m_log, m_isInteger, m_isEnum);
        m_logger.debug(String.format("new AxisTicks, min=%f, max=%f, maxTicks=%d, log=%b, integer=%b, Enum=%b",m_minValue, m_maxValue, maxTicks, m_log, m_isInteger, m_isEnum));
        m_minTick = m_ticks.getTickMin();
        m_maxTick = m_ticks.getTickMax();
        m_tickSpacing = m_ticks.getTickSpacing();

        if (m_log) {
            preparePaintLog(g, m_ticks);
        } else {
            preparePaintLinear(g, m_ticks);
        }

        if (m_mustDrawDiagonalLabels) {
            m_labelMinWidth = m_valuesFontMetrics.stringWidth("000");
            if (m_valuesDiagonalFont == null) {
                AffineTransform rotateText = new AffineTransform();
                rotateText.rotate(FONT_ROTATE);
                m_valuesDiagonalFont = m_valuesFont.deriveFont(rotateText);
            }

            m_minimumAxisHeight = 8 + m_valuesFontMetrics.getHeight() + (int) Math.round(StrictMath.ceil(StrictMath.sin(FONT_ROTATE) * m_labelMaxWidth));
        } else {
            m_labelMinWidth = m_valuesFontMetrics.stringWidth("0");
            m_minimumAxisHeight = 8 + m_valuesFontMetrics.getHeight();
        }
    }

    private void preparePaintLinear(Graphics2D g, AxisTicks ticks) {

        int fractionalDigits = ticks.getFractionalDigits();
        int integerDigits = ticks.getIntegerDigits();
        if ((fractionalDigits != m_fractionalDigits) || (integerDigits != m_integerDigits) || (m_df == null)) {
            m_df = selectDecimalFormat(fractionalDigits, integerDigits);
            m_dfPlot = selectDecimalFormat(fractionalDigits + 2, integerDigits);
            m_fractionalDigits = fractionalDigits;
            m_integerDigits = integerDigits;
        }

        int pixelStart = valueToPixel(m_minTick);
        int pixelStop = valueToPixel(m_maxTick);

        if (pixelStart >= pixelStop) { // avoid infinite loop 
            return;
        }

        double multForRounding = Math.pow(10, fractionalDigits);

        String maxLabel = "";
        double x = m_minTick;
        int pX = pixelStart;
        int previousEndX = -Integer.MAX_VALUE;
        while (true) {

            int stringWidth;
            String label;
            if (m_isEnum) {
                label = m_plotPanel.getEnumValueX((int) Math.round(x), false); //JPM.WART
                if (label == null || label.isEmpty()) {
                    label = " "; //JPM.WART
                }
                stringWidth = m_valuesFontMetrics.stringWidth(label);

            } else {
                // round x
                double xDisplay = x;
                if (fractionalDigits > 0) {
                    xDisplay = StrictMath.round(xDisplay * multForRounding) / multForRounding;
                }

                label = m_df.format(xDisplay);
                stringWidth = m_valuesFontMetrics.stringWidth(label);
            }

            if (stringWidth > m_labelMaxWidth) {
                m_labelMaxWidth = stringWidth;
                maxLabel = label;
            }

            int posX = pX - stringWidth / 2;
            if (posX > previousEndX + 2) { // check to avoid to overlap labels
                previousEndX = posX + stringWidth;
            } else {
                m_mustDrawDiagonalLabels = true;
                previousEndX = posX + m_valuesFontMetrics.stringWidth("000");
            }

            x += m_tickSpacing;
            pX = valueToPixel(x);
            if (pX > pixelStop) {
                break;
            }

        }

        if (m_mustDrawDiagonalLabels) {
            if (maxLabel.length() > 20) {
                maxLabel = maxLabel.substring(0, 19) + "..";
                m_labelMaxWidth = m_valuesFontMetrics.stringWidth(maxLabel);
            }
        }
    }

    private void preparePaintLog(Graphics2D g, AxisTicks ticks) {

        if (m_df == null) {
            m_df = selectLogDecimalFormat();
            m_dfPlot = selectLogDecimalFormat();
        }

        int pixelStart = valueToPixel(Math.pow(10, m_minTick));
        int pixelStop = valueToPixel(Math.pow(10, m_maxTick));

        if (pixelStart >= pixelStop) { // avoid infinite loop 
            return;
        }

        double x = m_minTick;
        int pX = pixelStart;
        int previousEndX = -Integer.MAX_VALUE;
        while (true) {

            // round x
            double xDisplay = x;

            String s = m_df.format(xDisplay);
            int stringWidth = m_valuesFontMetrics.stringWidth(s);

            if (stringWidth > m_labelMaxWidth) {
                m_labelMaxWidth = stringWidth;
            }

            int posX = pX - stringWidth / 2;
            if (posX > previousEndX + 2) { // check to avoid to overlap labels
                previousEndX = posX + stringWidth;
            } else {
                m_mustDrawDiagonalLabels = true;
                previousEndX = posX + m_valuesFontMetrics.stringWidth("000");
            }

            x += m_tickSpacing;
            pX = valueToPixel(Math.pow(10, x));
            if (pX > pixelStop) {
                break;
            }

        }

    }

    private void paintLinear(Graphics2D g, AxisTicks ticks) {

        int pixelStart = valueToPixel(m_minValue);
        int pixelStop = valueToPixel(m_maxValue);
        g.drawLine(pixelStart, m_y + BasePlotPanel.GAP_AXIS_LINE, pixelStop, m_y + BasePlotPanel.GAP_AXIS_LINE);

        if (pixelStart >= pixelStop) { // avoid infinite loop 
            return;
        }

        //pick the color to paint ticks on the line
        if (m_selected) {
            g.setColor(Color.white);
        } else {
            g.setColor(CyclicColorPalette.GRAY_TEXT_DARK);
        }

        //pick the font to paint ticks on the line
        g.setFont(m_mustDrawDiagonalLabels ? m_valuesDiagonalFont : m_valuesFont);

        int height = m_valuesFontMetrics.getHeight();

        int fractionalDigits = ticks.getFractionalDigits();
        double multForRounding = Math.pow(10, fractionalDigits);

        double x = m_minTick;
        if (valueToPixel(x) < pixelStart) {
            x += m_tickSpacing;
        }
        int pX = valueToPixel(x);
        int previousEndX = -Integer.MAX_VALUE;
        m_lastWidth = -1;

        while (true) {

            String label;
            if (m_isEnum) {
                label = m_plotPanel.getEnumValueX((int) Math.round(x), false); //JPM.WART
                if (label == null) {
                    label = " ";
                }
                if (m_mustDrawDiagonalLabels) {
                    if (label.length() > 20) {
                        label = label.substring(0, 19) + "..";
                    }
                }
            } else {
                // round x
                double xDisplay = x;
                if (fractionalDigits > 0) {
                    xDisplay = StrictMath.round(xDisplay * multForRounding) / multForRounding;
                }

                label = m_df.format(xDisplay);

            }

            int posX;
            if (m_mustDrawDiagonalLabels) {
                posX = pX;
            } else {
                int stringWidth = m_valuesFontMetrics.stringWidth(label);
                posX = pX - stringWidth / 2;
            }

            if (posX > previousEndX + 2) { // check to avoid to overlap labels

                g.drawLine(pX, m_y + BasePlotPanel.GAP_AXIS_LINE, pX, m_y + 4 + BasePlotPanel.GAP_AXIS_LINE);//draw tick
                g.drawString(label, posX, m_y + height + 4 + BasePlotPanel.GAP_AXIS_LINE);//draw label

                previousEndX = posX + m_labelMinWidth;
            }

            x += m_tickSpacing;
            pX = valueToPixel(x);
            if (pX > pixelStop) {
                break;
            }

        }
        g.setFont(m_valuesFont);

    }

    private void paintLog(Graphics2D g, AxisTicks ticks) {
        m_minTick = ticks.getTickMin();
        m_maxTick = ticks.getTickMax();
        m_tickSpacing = ticks.getTickSpacing();

        if (m_df == null) {
            m_df = selectLogDecimalFormat();
            m_dfPlot = selectLogDecimalFormat();
        }

        int pixelStart = valueToPixel(m_minValue);
        int pixelStop = valueToPixel(m_maxValue);
        g.drawLine(pixelStart, m_y + BasePlotPanel.GAP_AXIS_LINE, pixelStop, m_y + BasePlotPanel.GAP_AXIS_LINE);

        if (pixelStart >= pixelStop) { // avoid infinite loop 
            return;
        }

        g.setFont(m_mustDrawDiagonalLabels ? m_valuesDiagonalFont : m_valuesFont);

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

            g.drawLine(pX, m_y + BasePlotPanel.GAP_AXIS_LINE, pX, m_y + 4 + BasePlotPanel.GAP_AXIS_LINE);

            // round x
            double xDisplay = x;

            String s = m_df.format(xDisplay);
            int stringWidth = m_valuesFontMetrics.stringWidth(s);

            int posX = pX - stringWidth / 2;
            if (posX > previousEndX + 2) { // check to avoid to overlap labels
                g.drawString(s, posX, m_y + height + 4 + BasePlotPanel.GAP_AXIS_LINE);
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
            for (int i = 2; i <= 9; i++) {
                double xMinTick = Math.pow(10, x) * (((double) i) * 0.1d);
                int pMinTick = valueToPixel(xMinTick);
                g.drawLine(pMinTick, m_y + BasePlotPanel.GAP_AXIS_LINE, pMinTick, m_y + 4 + BasePlotPanel.GAP_AXIS_LINE);
            }

        }

    }

    public void paintGrid(Graphics2D g, int x, int width, int y, int height) {

        if (m_log) {
            paintGridLog(g, y, height);
        } else {
            paintGridLinear(g, x, width, y, height);
        }

    }

    public void paintGridLinear(Graphics2D g, int xPixel, int width, int yPixel, int height) {

        int pixelStart = valueToPixel(m_minTick);
        int pixelStop = valueToPixel(m_maxTick);
        m_logger.debug(String.format("___________AxisX m_minTick=%f, m_maxTick=%f, m_x=%d", m_minTick, m_maxTick, m_x));
         int stop =  m_x + (int) Math.round(((m_maxTick - m_minValue) / (m_maxValue - m_minValue)) * m_width);
        m_logger.debug(String.format("___________AxisX pixelStart=%d, pixelStop=%d", pixelStart, pixelStop));
        m_logger.debug(String.format("valueToPixel stop = %d, min=%f, max=%f,width=%d)", stop, m_minValue, m_maxValue, m_width));
        if (pixelStart >= pixelStop) { // avoid infinite loop 
            return;
        }

        //g.setColor(CyclicColorPalette.GRAY_GRID);
        g.setColor(Color.ORANGE);
        Stroke s = g.getStroke();
        g.setStroke(DASHED);

        double x = m_minTick;
        int pX = pixelStart;
        int previousEndX = -Integer.MAX_VALUE;
        while (true) {

            if (pX > previousEndX + 2) { // check to avoid to display grid for overlap labels
                if ((pX >= xPixel) && (pX <= xPixel + width)) {
                    g.drawLine(pX, yPixel, pX, yPixel + height - 1);
                }
                previousEndX = pX + m_lastWidth;
            }

            x += m_tickSpacing;
            pX = valueToPixel(x);
            if (pX > pixelStop) {
                break;
            }
        }
        g.setStroke(s);
    }

    public void paintGridLog(Graphics2D g, int y, int height) {
        int pixelStart = valueToPixel(Math.pow(10, m_minTick));
        int pixelStop = valueToPixel(Math.pow(10, m_maxTick));

        if (pixelStart >= pixelStop) { // avoid infinite loop 
            return;
        }

        Stroke s = g.getStroke();
        g.setStroke(DASHED);

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
            for (int i = 2; i <= 9; i++) {
                double xMinTick = Math.pow(10, x) * (((double) i) * 0.1d);
                int pMinTick = valueToPixel(xMinTick);
                g.drawLine(pMinTick, y, pMinTick, y + height - 1);
            }
        }
        g.setStroke(s);
    }

    @Override
    public int valueToPixel(double v) {
        if (m_isPixel) {
            return (int) Math.round(v);
        }
        if (m_log) {
            v = Math.log10(v);
            double min = Math.log10(m_minValue);
            double max = Math.log10(m_maxValue);
            return m_x + (int) Math.round(((v - min) / (max - min)) * m_width);
        } else {
            return m_x + (int) Math.round(((v - m_minValue) / (m_maxValue - m_minValue)) * m_width);
        }
    }

    @Override
    public double pixelToValue(int pixel) {
        if (m_isPixel) {
            return pixel;
        }
        if (m_log) {
            double min = Math.log10(m_minValue);
            double max = Math.log10(m_maxValue);
            double v = min + ((((double) pixel) - m_x) / ((double) m_width)) * (max - min);
            v = Math.pow(10, v);
            return v;
        } else {
            double v = m_minValue + ((((double) pixel) - m_x) / ((double) m_width)) * (m_maxValue - m_minValue);
            return v;
        }

    }

}
