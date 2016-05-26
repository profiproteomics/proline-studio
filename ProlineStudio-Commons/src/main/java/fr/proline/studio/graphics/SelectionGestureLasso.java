package fr.proline.studio.graphics;

import fr.proline.studio.utils.CyclicColorPalette;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.geom.Path2D;

/**
 * Used to manage selection gesture
 * @author JM235353
 */
public class SelectionGestureLasso extends AbstractSelectionGesture {

    private final Polygon m_selectionPolygon = new Polygon();



    
    public SelectionGestureLasso() {
    }


    
    public void startSelection(int x, int y) {
        m_selectionPolygon.reset();
        m_selectionPolygon.addPoint(x, y);

        m_isSelecting = true;
        m_action = ACTION_NONE;
    }
    
    public void continueSelection(int x, int y) {

        int lastIndex = m_selectionPolygon.npoints - 1;
        if ((m_selectionPolygon.xpoints[lastIndex] != x) || (m_selectionPolygon.ypoints[lastIndex] != y)) {
            m_selectionPolygon.addPoint(x, y);
        }

    }
    
    public void stopSelection(int x, int y) {
        
        continueSelection(x, y);
        m_isSelecting = false;

        m_minX = m_selectionPolygon.xpoints[0];
        m_maxX = m_minX;
        m_minY = m_selectionPolygon.ypoints[0];
        m_maxY = m_minY;

        // set surrounding box of the polygon
        for (int i=1;i<m_selectionPolygon.npoints;i++) {
            int xCur = m_selectionPolygon.xpoints[i];
            int yCur = m_selectionPolygon.ypoints[i];
            if (xCur<m_minX) {
                m_minX = xCur;
            } else if (xCur>m_maxX) {
                m_maxX = xCur;
            }
            if (yCur<m_minY) {
                m_minY = yCur;
            } else if (yCur>m_maxY) {
                m_maxY = yCur;
            }
            
        }
        
        if (((m_maxY-m_minY)<MIN_SURROUND_DELTA) && ((m_maxX-m_minX)<MIN_SURROUND_DELTA)) {
            m_action = ACTION_CLICK;
        } else {
            m_action = ACTION_SURROUND;
        }

    }

    
    public Point getClickPoint() {
        return new Point(m_selectionPolygon.xpoints[m_selectionPolygon.npoints-1], m_selectionPolygon.ypoints[m_selectionPolygon.npoints-1]);
    }
    
    public Path2D.Double getSelectionPath() {

        Path2D.Double path = new Path2D.Double();
        path.moveTo(m_selectionPolygon.xpoints[0], m_selectionPolygon.xpoints[0]);
        for (int i = 1; i < m_selectionPolygon.npoints; i++) {
            path.lineTo(m_selectionPolygon.xpoints[i], m_selectionPolygon.ypoints[i]);
        }
        path.closePath();
        return path;
    }
    
    public void paint(Graphics g) {
        if (!m_isSelecting) {
            return;
        }

        if (m_selectionPolygon.npoints<3 ) {
            return;
        }

        
        g.setColor(CyclicColorPalette.BLUE_SELECTION_ZONE);

        g.fillPolygon(m_selectionPolygon);
    }
    
    public int getAction() {
        return m_action;
    }
    
    public int getMinX() {
        return m_minX;
    }

    public int getMaxX() {
        return m_maxX;
    }

    public int getMinY() {
        return m_minY;
    }

    public int getMaxY() {
        return m_maxY;
    }
    
}
