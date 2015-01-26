package fr.proline.studio.graphics;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.text.DecimalFormat;

/**
 * Base class for axis
 * @author JM235353
 */
public abstract class Axis {

    protected String m_title = null;
    protected Font m_titleFont = null;
    protected FontMetrics m_titleFontMetrics;
    protected Font m_valuesFont = null;
    protected FontMetrics m_valuesFontMetrics = null;
    
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
    protected double m_tickSpacing;
    
    protected DecimalFormat m_df;
    protected DecimalFormat m_dfPlot;
    protected int m_digits = -1;
    
    protected boolean m_log = false;
    
    protected boolean m_selected = false;
    
    protected boolean m_isInteger = false;
    protected boolean m_isEnum = false;
    
    protected PlotPanel m_plotPanel;
    
    public Axis(PlotPanel p) {
        m_plotPanel = p;
    }
    
    public void setSpecificities(boolean isInteger, boolean isEnum) {
        m_isInteger = isInteger;
        m_isEnum = isEnum;
    }

    public boolean isEnum() {
        return m_isEnum;
    }

    
    public void setLog(boolean log) {
        m_log = log;
        m_df = null; // reinit for display
    }
    
   
    
    public boolean isLog() {
        return m_log;
    }
    
    public boolean canBeInLog() {
        return (m_minValue>=10e-9) && (!m_isEnum);
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

    public void setTitle(String title) {
        m_title = title;
    }
    
    public String getTitle() {
        return m_title;
    }
    
    public boolean setSelected(boolean v) {
        boolean changed = v ^ m_selected;
        m_selected = v;
        return changed;
    }
    
    public boolean isSelected() {
        return m_selected;
    }
    
    public abstract void paint(Graphics2D g);

    public abstract int valueToPixel(double v);

    public abstract double pixelToValue(int pixel);
    
    public double getMinValue() {
        return m_minValue;
    }

    public double getMaxValue() {
        return m_maxValue;
    }

    public double getMinTick() {
        if (m_log) {
            return Math.pow(10, m_minTick);
        }
        return m_minTick;
    }

    public double getMaxTick() {
         if (m_log) {
            return Math.pow(10, m_maxTick);
        }
        return m_maxTick;
    }

    public boolean inside(int x, int y) {
        return (x>=m_x) && (x<m_x+m_width) && (y>m_y) && (y<m_y+m_height);
    }
    
    protected DecimalFormat selectDecimalFormat(int digits) {
        String pattern;
        if (m_log) {
            pattern = ("0.0E0");
        } else if (digits <= 0) {
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
    
    public DecimalFormat getExternalDecimalFormat() {
        return m_dfPlot;
    } 
    
    
    public interface EnumXInterface {
        public String getEnumValueX(int index);
    }
    
    public interface EnumYInterface {
        public String getEnumValueY(int index);
    }
}
