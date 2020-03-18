/* 
 * Copyright (C) 2019 VD225637
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1 
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License 
 * along with this program; If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */
package fr.proline.studio.graphics;

import fr.proline.studio.utils.CyclicColorPalette;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.Path2D;

/**
 * Used to manage mouse selection gesture as a polygon
 * 
 * @author JM235353
 */
public class SelectionGestureLasso extends AbstractSelectionGesture {

    private final Polygon m_selectionPolygon = new Polygon();
    private final Rectangle m_selectionRectangle = new Rectangle();

    private boolean m_selectionIsSquare = true;
    
    public SelectionGestureLasso() {
    }
    
    public void setSelectionIsSquare(boolean selectionIsSquare) {
        m_selectionIsSquare = selectionIsSquare;
    }

    
    @Override
    public void startSelection(int x, int y) {
        
        if (m_selectionIsSquare) {
            m_selectionRectangle.x = x;
            m_selectionRectangle.y = y;
            m_selectionRectangle.height = 0;
            m_selectionRectangle.width = 0;
        } else {
            m_selectionPolygon.reset();
            m_selectionPolygon.addPoint(x, y);
        }
        
        m_isSelecting = true;
        m_action = ACTION_NONE;
    }
    
    @Override
    public void continueSelection(int x, int y) {

        if (m_selectionIsSquare) {
            m_selectionRectangle.height = y-m_selectionRectangle.y;
            m_selectionRectangle.width = x-m_selectionRectangle.x;
            
        } else {
            int lastIndex = m_selectionPolygon.npoints - 1;
            if ((m_selectionPolygon.xpoints[lastIndex] != x) || (m_selectionPolygon.ypoints[lastIndex] != y)) {
                m_selectionPolygon.addPoint(x, y);
            }
        }

    }
    
    @Override
    public void stopSelection(int x, int y) {
        
        continueSelection(x, y);
        m_isSelecting = false;

        if (m_selectionIsSquare) {
            m_minX = m_selectionRectangle.x;
            m_maxX = m_selectionRectangle.x+m_selectionRectangle.width;
            m_minY = m_selectionRectangle.y;
            m_maxY = m_selectionRectangle.y+m_selectionRectangle.height;
        } else {
            m_minX = m_selectionPolygon.xpoints[0];
            m_maxX = m_minX;
            m_minY = m_selectionPolygon.ypoints[0];
            m_maxY = m_minY;

            // set surrounding box of the polygon
            for (int i = 1; i < m_selectionPolygon.npoints; i++) {
                int xCur = m_selectionPolygon.xpoints[i];
                int yCur = m_selectionPolygon.ypoints[i];
                if (xCur < m_minX) {
                    m_minX = xCur;
                } else if (xCur > m_maxX) {
                    m_maxX = xCur;
                }
                if (yCur < m_minY) {
                    m_minY = yCur;
                } else if (yCur > m_maxY) {
                    m_maxY = yCur;
                }

            }
        }
        
        if (((m_maxY-m_minY)<MIN_SURROUND_DELTA) && ((m_maxX-m_minX)<MIN_SURROUND_DELTA)) {
            m_action = ACTION_CLICK;
        } else {
            m_action = ACTION_SURROUND;
        }

    }

    
    @Override
    public Point getClickPoint() {
        if (m_selectionIsSquare) {
            return new Point(m_selectionRectangle.x, m_selectionRectangle.y);
        } else {
            return new Point(m_selectionPolygon.xpoints[m_selectionPolygon.npoints-1], m_selectionPolygon.ypoints[m_selectionPolygon.npoints-1]);
        }
    }
    
    @Override
    public Path2D.Double getSelectionPath() {

        Path2D.Double path = new Path2D.Double();
        
        if (m_selectionIsSquare) {
            path.moveTo(m_selectionRectangle.x, m_selectionRectangle.y);
            path.lineTo(m_selectionRectangle.x+m_selectionRectangle.width, m_selectionRectangle.y);
            path.lineTo(m_selectionRectangle.x+m_selectionRectangle.width, m_selectionRectangle.y+m_selectionRectangle.height);
            path.lineTo(m_selectionRectangle.x, m_selectionRectangle.y+m_selectionRectangle.height);
        } else {
            path.moveTo(m_selectionPolygon.xpoints[0], m_selectionPolygon.xpoints[0]);
            for (int i = 1; i < m_selectionPolygon.npoints; i++) {
                path.lineTo(m_selectionPolygon.xpoints[i], m_selectionPolygon.ypoints[i]);
            }
        }
        path.closePath();
        return path;
    }
    
    public Polygon getSelectionPolygon() {
        if (m_selectionIsSquare) {
            m_selectionPolygon.reset();
            m_selectionPolygon.addPoint(m_selectionRectangle.x, m_selectionRectangle.y);
            m_selectionPolygon.addPoint(m_selectionRectangle.x+m_selectionRectangle.width, m_selectionRectangle.y);
            m_selectionPolygon.addPoint(m_selectionRectangle.x+m_selectionRectangle.width, m_selectionRectangle.y+m_selectionRectangle.height);
            m_selectionPolygon.addPoint(m_selectionRectangle.x, m_selectionRectangle.y+m_selectionRectangle.height);
            
        }
        
        return m_selectionPolygon;
        
    }
    
    @Override
    public void paint(Graphics g) {
        if (!m_isSelecting) {
            return;
        }

        g.setColor(CyclicColorPalette.BLUE_SELECTION_ZONE);
        
        if (m_selectionIsSquare) {
            g.fillRect(m_selectionRectangle.x, m_selectionRectangle.y, m_selectionRectangle.width, m_selectionRectangle.height);
        } else {
            if (m_selectionPolygon.npoints < 3) {
                return;
            }
            g.fillPolygon(m_selectionPolygon);
        }

        

    }
    
    @Override
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
