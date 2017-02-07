package fr.proline.studio.graphics.cursor;

import java.awt.BasicStroke;
import java.awt.Color;

/**
 *
 * @author JM235353
 */
public class CursorInfo {

    private final double m_value;
    
    private BasicStroke m_stroke = null;
    private Color m_color = null;
    private Boolean m_selectable = null;
    
    public CursorInfo(double v) {
        m_value = v;
    }
    
    public void applyParametersToCursor(AbstractCursor cursor) {
        if (m_color != null) {
            cursor.setColor(m_color);
        }
        if (m_stroke != null) {
            cursor.setStroke(m_stroke);
        }
        if (m_selectable != null) {
            cursor.setSelectable(m_selectable);
        }
    }
    
    public void setStroke(BasicStroke stroke) {
        m_stroke = stroke;
    }
    
    public void setColor(Color color) {
        m_color = color;
    }
    
    public void setSelectable(Boolean selectable) {
        m_selectable = selectable;
    }
    
    public double getValue() {
        return m_value;
    }
}
