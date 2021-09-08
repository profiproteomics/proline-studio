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

    public OverArea(DockContainerTab container, DockPosition dockPosition) {
        m_container = container;
        m_position = dockPosition;

        JComponent c = container.getComponent();

        int position = dockPosition.getPosition();

        if (position>=0) {
            // We are over a Tabbed Label
            Component tabComponent = ((JTabbedPane) c).getTabComponentAt(position);
            Point  p = tabComponent.getLocationOnScreen();
            final int MARGIN = 5;
            m_zone = new Rectangle(p.x-MARGIN, p.y-MARGIN, tabComponent.getWidth()+MARGIN*2, tabComponent.getHeight()+MARGIN*2);
        } else {

            Point p = c.getLocationOnScreen();


            m_zone = new Rectangle(p.x, p.y, c.getWidth(), c.getHeight());


            switch (position) {
                case DockPosition.NORTH:
                    m_zone.height /= 2;
                    break;
                case DockPosition.SOUTH:
                    m_zone.height /= 2;
                    m_zone.y += m_zone.height;
                    break;
                case DockPosition.WEST:
                    m_zone.width /= 2;
                    break;
                case DockPosition.EAST:
                    m_zone.width /= 2;
                    m_zone.x += m_zone.width;
                    break;
            }
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
