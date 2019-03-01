package fr.proline.studio.graphics;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Path2D;

/**
 *
 * @author JM235353
 */
public abstract class AbstractSelectionGesture  {
    public static final int ACTION_NONE = 0;
    public static final int ACTION_CLICK = 1;
    public static final int ACTION_SURROUND = 2;

    protected boolean m_isSelecting = false;
    protected int m_action = ACTION_NONE;

    protected static final int MIN_SURROUND_DELTA = 10;
    
    protected int m_minX;
    protected int m_maxX;
    protected int m_minY;
    protected int m_maxY;
    
    public AbstractSelectionGesture() {
    }

    public boolean isSelecting() {
        return m_isSelecting;
    }
    
    public abstract void startSelection(int x, int y);
    public abstract void continueSelection(int x, int y);
    
    public abstract void stopSelection(int x, int y);

    
    public abstract void paint(Graphics g);
    
    public int getAction() {
        return m_action;
    }

    
    public abstract Point getClickPoint();
    
    public abstract Path2D.Double getSelectionPath();
}
