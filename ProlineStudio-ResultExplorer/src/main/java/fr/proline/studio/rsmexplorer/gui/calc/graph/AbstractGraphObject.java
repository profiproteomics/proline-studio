package fr.proline.studio.rsmexplorer.gui.calc.graph;

import java.awt.BasicStroke;
import java.awt.Graphics;

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
        CONNECTOR
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
    
    public void setSelected(boolean s) {
        m_selected = s;
    }
    
}
