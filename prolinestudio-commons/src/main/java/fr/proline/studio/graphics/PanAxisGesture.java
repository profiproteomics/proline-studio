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

/**
 * Used to manage zoom/unzoom gesture
 *
 * @author JM235353
 */
public class PanAxisGesture {

    public static final int X_AXIS_PAN = 0;
    public static final int Y_AXIS_PAN = 1;
    public static final int Y_AT_RIGHT_AXIS_PAN = 2;
    private static final int MIN_PANNING_DELTA = 10;

    public static final int ACTION_NONE = 0;
    public static final int ACTION_PAN = 1;

    private boolean m_isPanning = false;
    private int m_x1, m_x2, m_y1, m_y2;
    private int m_panningAxis;
    private int m_action = ACTION_NONE;

    public PanAxisGesture() {

    }

    public int getPanningAxis() {
        return m_panningAxis;
    }

    public boolean isPanning() {
        return m_isPanning;
    }

    public void startPanning(int x, int y, int panningAxis) {
        m_x1 = x;
        m_y1 = y;
        m_x2 = x;
        m_y2 = y;
        m_isPanning = true;
        m_panningAxis = panningAxis;
        m_action = ACTION_NONE;
    }

    public void movePan(int x, int y) {
        
        
        boolean yPanning = (m_panningAxis == Y_AXIS_PAN) || (m_panningAxis == Y_AT_RIGHT_AXIS_PAN);
                
        m_x2 = x;
        if (yPanning) {
            m_y2 = y;
        }

        if (((Math.abs(m_x2 - m_x1) <= MIN_PANNING_DELTA) && m_panningAxis == X_AXIS_PAN)
                || ((Math.abs(m_y2 - m_y1) <= MIN_PANNING_DELTA) && yPanning)) {
            m_action = ACTION_NONE;
        } else {
            m_action = ACTION_PAN;
        }
    }

    public void stopPanning(int x, int y) {
        m_isPanning = false;
        m_action = ACTION_NONE;
    }

    public int getPreviousX() {
        return m_x2;
    }

    public int getPreviousY() {
        return m_y2;
    }


    public int getAction() {
        return m_action;
    }
}
