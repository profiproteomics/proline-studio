package fr.proline.studio.graphics;

import java.awt.Graphics;
import java.awt.Graphics2D;

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
        return (int) Math.round((v - m_minTick) * m_width);
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

    
}
