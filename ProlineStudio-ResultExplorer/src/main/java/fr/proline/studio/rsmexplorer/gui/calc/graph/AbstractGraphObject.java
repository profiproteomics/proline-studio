package fr.proline.studio.rsmexplorer.gui.calc.graph;

import fr.proline.studio.rsmexplorer.gui.calc.GraphPanel;
import java.awt.BasicStroke;
import java.awt.Graphics;
import javax.swing.JPopupMenu;

/**
 *
 * @author JM235353
 */
public abstract class AbstractGraphObject {
        
    protected static final BasicStroke STROKE_SELECTED = new BasicStroke(4);
    protected static final BasicStroke STROKE_NOT_SELECTED = new BasicStroke(2);
    
    protected boolean m_selected = false;
    
    protected TypeGraphObject m_type;
    
    public enum TypeGraphObject {
        GRAPH_NODE,
        CONNECTOR,
        LINK
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
    
    public abstract void delete();
    
    public void setSelected(boolean s) {
        m_selected = s;
    }
    
    public abstract JPopupMenu createPopup(final GraphPanel panel);
}
