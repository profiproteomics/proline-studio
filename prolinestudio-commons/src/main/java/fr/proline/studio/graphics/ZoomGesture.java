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

import java.awt.Color;
import java.awt.Graphics;

/**
 * Used to manage zoom/unzoom gesture
 * @author JM235353
 */
public class ZoomGesture {

    public static final int ACTION_NONE = 0;
    public static final int ACTION_ZOOM = 1;
    public static final int ACTION_UNZOOM = 2;

    private boolean m_isZooming = false;
    private int m_action = ACTION_NONE;

    /**
     * pixel value
     */
    private int m_x1, m_x2, m_y1, m_y2;
    /**
     * pixel value
     */
    private int m_xStart, m_xEnd, m_yStart, m_yEnd;

    private static final int MIN_ZOOMING_DELTA = 10;

    public ZoomGesture() {

    }

    public boolean isZooming() {
        return m_isZooming;
    }

    public void startZooming(int x, int y) {
        m_x1 = x;
        m_y1 = y;
        m_x2 = x;
        m_y2 = y;
        m_isZooming = true;
        m_action = ACTION_NONE;
    }

    public void moveZooming(int x, int y) {
        m_x2 = x;
        m_y2 = y;
    }

    public void stopZooming(int x, int y) {
        m_x2 = x;
        m_y2 = y;
        m_isZooming = false;

        if ((Math.abs(m_x2 - m_x1) <= MIN_ZOOMING_DELTA) || (Math.abs(m_y2 - m_y1) <= MIN_ZOOMING_DELTA)) {
            m_action = ACTION_NONE;
        } else if ((m_x2 > m_x1) && (m_y2 > m_y1)) {
            m_action = ACTION_ZOOM;
        } else {
            m_action = ACTION_UNZOOM;
        }
    }

    public void paint(Graphics g) {
        if (!m_isZooming) {
            return;
        }

        if ((m_x2 < m_x1) || (m_y2 < m_y1)) {
            return;
        }

        g.setXORMode(Color.white);
        g.setColor(Color.red);

        if (m_x1 < m_x2) {
            m_xStart = m_x1;
            m_xEnd = m_x2;
        } else {
            m_xStart = m_x2;
            m_xEnd = m_x1;
        }
        if (m_y1 < m_y2) {
            m_yStart = m_y1;
            m_yEnd = m_y2;
        } else {
            m_yStart = m_y2;
            m_yEnd = m_y1;
        }
        g.drawRect(m_xStart, m_yStart, m_xEnd - m_xStart, m_yEnd - m_yStart);
    }

    public int getStartX() {
        return m_xStart;
    }

    public int getEndX() {
        return m_xEnd;
    }

    public int getStartY() {
        return m_yStart;
    }

    public int getEndY() {
        return m_yEnd;
    }

    public int getAction() {
        return m_action;
    }

}
