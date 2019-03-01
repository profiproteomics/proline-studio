package fr.proline.studio.graphics.cursor;

import fr.proline.studio.graphics.BasePlotPanel;
import fr.proline.studio.graphics.MoveableInterface;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Base class for cursors.
 * A cursor is a movable graphical object displayed over a graphic.
 * It displays the X or Y axis value.
 * @author JM235353
 */
public abstract class AbstractCursor implements MoveableInterface {

    public final static Color DEFAULT_CURSOR_COLOR = new Color(160,82,45);
    public final static BasicStroke LINE1_STROKE = new BasicStroke(1.0f);
    public final static BasicStroke LINE2_STROKE = new BasicStroke(2.0f);
    
    protected final static int INSIDE_TOLERANCE = 2;
    
    protected BasePlotPanel m_plotPanel;

    protected double m_value;
    
    protected boolean m_snapToData = false;
    protected boolean m_selected = false;
    
    protected ArrayList<ActionListener> m_actionListenerList = null;
    protected int m_actionEventId = 0;
    
    protected Double m_minValue = null;
    protected Double m_maxValue = null;
    
    protected boolean m_selectable = true;
    
    
    protected int m_integerDigits = -1;
    protected int m_fractionalDigits = -1;
    protected DecimalFormat m_df = null;
    
    protected Color m_color = DEFAULT_CURSOR_COLOR;
    protected BasicStroke m_stroke = LINE1_STROKE;
    
    public AbstractCursor(BasePlotPanel plotPanel) {
        m_plotPanel = plotPanel;
    }

    public void setColor(Color c) {
        m_color = c;
    }
    
    public Color getColor() {
        return m_color;
    }
    
    public void setStroke(BasicStroke s) {
        m_stroke = s;
    }
    
    public BasicStroke getStroke() {
        return m_stroke;
    }
    
    public double getValue() {
        return m_value;
    }
    
    public String getFormattedValue() {
        if (m_df != null) {
            double multForRounding = Math.pow(10, m_fractionalDigits);
            double xDisplay = StrictMath.round(m_value * multForRounding) / multForRounding;
            return m_df.format(xDisplay);
        } else {
            return String.valueOf(m_value);
        }
    }
    
    public int getIntegerDigits() {
        return m_integerDigits;
    }
    
    public int getFractionalDigits() {
        return m_fractionalDigits;
    }
    
    public DecimalFormat getDecimalFormat() {
        return m_df;
    }
    
    public void setFormat(int integerDigits, int fractionalDigits, DecimalFormat df) {
        m_integerDigits = integerDigits;
        m_fractionalDigits = fractionalDigits;
        m_df = df;
    }
    
    public void setMinValue(Double minValue) {
        m_minValue = minValue;
    }
    
    public void setMaxValue(Double maxValue) {
        m_maxValue = maxValue;
    }
    
    public void setSelectable(boolean s) {
        m_selectable = s;
    }
    
    public void addActionListener(ActionListener actionListener) {
        if (m_actionListenerList == null) {
            m_actionListenerList = new ArrayList();
        }
        m_actionListenerList.add(actionListener);
    }
    
    public boolean isSnapToData() {
        return m_snapToData;
    }
    
    public boolean toggleSnapToData() {
        return m_snapToData = !m_snapToData;
    }
    
    @Override
    public void setSelected(boolean s, boolean isCtrlOrShiftDown) {
        m_selected = s;
    }
    
    public boolean isSelected() {
        return m_selected;
    }
 
    
    public abstract void paint(Graphics2D g);
}
