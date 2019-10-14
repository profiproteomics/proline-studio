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

import static fr.proline.studio.graphics.AbstractSelectionGesture.ACTION_CLICK;
import static fr.proline.studio.graphics.AbstractSelectionGesture.ACTION_NONE;
import static fr.proline.studio.graphics.AbstractSelectionGesture.ACTION_SURROUND;
import fr.proline.studio.utils.CyclicColorPalette;
import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.Path2D;

/**
 * Rectangle Selection zone
 * 
 * @author JM235353
 */
public class SelectionGestureSquare extends AbstractSelectionGesture {

    private static final float DASH1[] = {10.0f};
    private static final BasicStroke DASHED_STROKE = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, DASH1, 0.0f);
    
    private final Rectangle m_selectionRectangle = new Rectangle();
    
    private int m_x1;
    private int m_x2;
    private int m_y1;
    private int m_y2;
    
    public SelectionGestureSquare() {
    }

    @Override
    public void startSelection(int x, int y) {
        m_selectionRectangle.x = x;
        m_selectionRectangle.y = y;
        m_x1 = x;
        m_y1 = y;
        m_x2 = x;
        m_y2 = y;

        m_isSelecting = true;
        m_action = ACTION_NONE;
    }
    
    @Override
    public void continueSelection(int x, int y) {
        m_x2 = x;
        m_y2 = y;
        
        m_minX = Math.min(m_x1, m_x2);
        m_maxX = Math.max(m_x1, m_x2);
        m_minY = Math.min(m_y1, m_y2);
        m_maxY = Math.max(m_y1, m_y2);

        m_selectionRectangle.x = m_minX;
        m_selectionRectangle.y = m_minY;
        m_selectionRectangle.width = m_maxX - m_minX;
        m_selectionRectangle.height = m_maxY - m_minY;
    }
    
    @Override
    public void stopSelection(int x, int y) {
        
        continueSelection(x, y);
        m_isSelecting = false;

        if ((Math.abs(m_y2-m_y1)<MIN_SURROUND_DELTA) && (Math.abs(m_x2-m_x1)<MIN_SURROUND_DELTA)) {
            m_action = ACTION_CLICK;
        } else {
            m_action = ACTION_SURROUND;
        }


    }

    @Override
    public Point getClickPoint() {
        return new Point((m_x1+m_x2)/2, (m_y1+m_y2)/2);
    }
    
    @Override
    public void paint(Graphics g) {
        if (!m_isSelecting) {
            return;
        }

        if ((Math.abs(m_y2-m_y1)<MIN_SURROUND_DELTA) && (Math.abs(m_x2-m_x1)<MIN_SURROUND_DELTA)) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g;
        
        g2.setColor(CyclicColorPalette.BLUE_SELECTION_ZONE);
        Stroke sOld = g2.getStroke();
        g2.setStroke(DASHED_STROKE);

        ((Graphics2D)g).draw(m_selectionRectangle);
        
         g2.setStroke(sOld);
    }

    @Override
    public Path2D.Double getSelectionPath() {
        return new Path2D.Double(m_selectionRectangle);
    }

}
