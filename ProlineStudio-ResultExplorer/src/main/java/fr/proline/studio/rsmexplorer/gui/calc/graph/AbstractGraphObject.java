package fr.proline.studio.rsmexplorer.gui.calc.graph;

import fr.proline.studio.rsmexplorer.gui.calc.GraphPanel;
import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Graphics;
import javax.swing.JPopupMenu;

/**
 *
 * @author JM235353
 */
public abstract class AbstractGraphObject {
            
    protected static final BasicStroke STROKE_SELECTED = new BasicStroke(4);
    protected static final BasicStroke STROKE_NOT_SELECTED = new BasicStroke(2);
    
    protected static Font m_font = null;
    protected static Font m_fontBold = null;
    
    protected static int m_hgtBold;
    protected static int m_hgtPlain;
    protected static int m_ascentBold;
    
    protected boolean m_selected = false;
    protected boolean m_hightlighted = false;
    
    protected TypeGraphObject m_type;
    
    public enum TypeGraphObject {
        GRAPH_NODE,
        CONNECTOR,
        LINK,
        GRAPH_NODE_ACTION,
        GROUP
    };
    
    public AbstractGraphObject(TypeGraphObject type) {
        m_type = type;
    }
   
    public TypeGraphObject getType() {
        return m_type;
    }

    
    public abstract void draw(Graphics g);
    
    public abstract AbstractGraphObject inside(int x, int y);
    public abstract void move(int dx, int dy);
    public int correctMoveX(int dx) {
        return dx;
    }
    public int correctMoveY(int dy) {
        return dy;
    }
    
    public abstract void delete();

    public void setSelected(boolean s) {
        m_selected = s;
    }
    
    public boolean isSelected() {
        return m_selected;
    }
    
    public boolean setHighlighted(boolean h) {
        if (h ^ m_hightlighted) {
            m_hightlighted = h;
            return true;
        }
        return false;
    }
    
    public boolean isHighlighted() {
        return m_hightlighted;
    }
    
    
    
    public abstract JPopupMenu createPopup(final GraphPanel panel);

}
