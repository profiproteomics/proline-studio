/*
 * Copyright (C) 2019
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

package fr.proline.studio.dock.container;

import java.awt.*;

public class DockPosition {

    public static final int CENTER = -1;
    public static final int WEST   = -2;
    public static final int EAST   = -3;
    public static final int NORTH  = -4;
    public static final int SOUTH  = -5;
    // positive value correspond to an index of a sub component.

    private int m_position;

    public DockPosition(int position) {
        m_position = position;
    }


    public String getBorderLayout() {
        switch (m_position) {
            case DockPosition.CENTER:
                return BorderLayout.CENTER;
            case DockPosition.WEST:
                return BorderLayout.WEST;
            case DockPosition.EAST:
                return BorderLayout.EAST;
            case DockPosition.NORTH:
                return BorderLayout.NORTH;
            case DockPosition.SOUTH:
                return BorderLayout.SOUTH;
        }
        return null;
    }

    public int getPosition() {
        return m_position;
    }



}
