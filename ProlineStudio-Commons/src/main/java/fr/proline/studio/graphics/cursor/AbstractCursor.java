package fr.proline.studio.graphics.cursor;

import fr.proline.studio.graphics.BasePlotPanel;
import fr.proline.studio.graphics.MoveableInterface;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 *
 * @author JM235353
 */
public abstract class AbstractCursor implements MoveableInterface {

    public final static Color CURSOR_COLOR = new Color(160,82,45);
    public final static BasicStroke LINE1_STROKE = new BasicStroke(1.0f);
    public final static BasicStroke LINE2_STROKE = new BasicStroke(2.0f);
    
    protected BasePlotPanel m_plotPanel;

    protected boolean m_snapToData = false;
    protected boolean m_selected = false;
    
    public AbstractCursor(BasePlotPanel plotPanel) {
        m_plotPanel = plotPanel;
    }

    public boolean isSnapToData() {
        return m_snapToData;
    }
    
    public boolean toggleSnapToData() {
        return m_snapToData = !m_snapToData;
    }
    
    public void setSelected(boolean s) {
        m_selected = s;
    }
    
    public boolean isSelected() {
        return m_selected;
    }
    
    public abstract void snapToData();
    
    public abstract void paint(Graphics2D g);
}
