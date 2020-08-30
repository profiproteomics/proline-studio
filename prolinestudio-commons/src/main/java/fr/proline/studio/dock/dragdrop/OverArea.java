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

package fr.proline.studio.dock.dragdrop;

import fr.proline.studio.dock.container.DockContainerTab;
import fr.proline.studio.dock.container.DockPosition;

import javax.swing.*;
import java.awt.*;

public class OverArea {

    private DockContainerTab m_container;
    private DockPosition m_position;
    private Rectangle m_zone;

    public OverArea(DockContainerTab container, DockPosition position) {
        m_container = container;
        m_position = position;

        JComponent c = container.getComponent();
        Point p = c.getLocationOnScreen();


        m_zone = new Rectangle(p.x, p.y, c.getWidth(), c.getHeight());

        switch (position) {
            case NORTH:
                m_zone.height /= 2;
                break;
            case SOUTH:
                m_zone.height /= 2;
                m_zone.y += m_zone.height;
                break;
            case WEST:
                m_zone.width /= 2;
                break;
            case EAST:
                m_zone.width /= 2;
                m_zone.x += m_zone.width;
                break;
        }


    }

    public DockContainerTab getDockContainerTab() {
        return m_container;
    }
    public DockPosition getPosition() {
        return m_position;
    }
    public Rectangle getZone(JComponent c) {

        Point p = new Point(m_zone.x, m_zone.y);
        SwingUtilities.convertPointFromScreen(p, c);
        Rectangle localZone = new Rectangle(p.x, p.y, m_zone.width, m_zone.height);

        return localZone;

    }

}
