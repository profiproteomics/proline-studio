package fr.proline.studio.graphics;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.text.DecimalFormat;

/**
 * Base class for axis
 * @author JM235353
 */
public abstract class Axis {

    protected int m_x;
    protected int m_y;
    protected int m_width;
    protected int m_height;
    
    // value of data
    protected double m_minValue; 
    protected double m_maxValue;

    // Min tick and max tick
    protected double m_minTick; 
    protected double m_maxTick;
    
    protected DecimalFormat m_df;
    protected int m_digits = -1;
    
    
    public Axis() {
    }

    public void setSize(int x, int y, int width, int height) {
        m_x = x;
        m_y = y;
        m_width = width;
        m_height = height;
    }

    public void setRange(double min, double max) {
        m_minValue = min;
        m_maxValue = max;
        m_minTick = min;
        m_maxTick = max;
    }

    public abstract void paint(Graphics2D g);

    public int valueToPixel(double v) {
        return (m_y+m_height)-(int) Math.round(((v - m_minTick)/(m_maxTick-m_minTick)) * m_height);
    }

    public double pixelToValue(int pixel) {
        return m_minTick+(((double)((m_y+m_height)-pixel))/((double)m_height))*(m_maxTick-m_minTick);
    }
    
    public double getMinValue() {
        return m_minValue;
    }

    public double getMaxValue() {
        return m_maxValue;
    }

    public double getMinTick() {
        return m_minTick;
    }

    public double getMaxTick() {
        return m_maxTick;
    }

    protected DecimalFormat selectDecimalFormat(int digits) {
        String pattern;
        if (digits <= 0) {
            pattern = "#";  // number like "3"
        } else if (digits > 3) { // 3 is 
            pattern = ("0.0E0"); // scientific notation for numbers with too much digits "0.0000532"
        } else {
            pattern = "#.";
            while (digits > 0) {
                pattern += "#";
                digits--;
            }
        }
        return new DecimalFormat(pattern);
    }
}
