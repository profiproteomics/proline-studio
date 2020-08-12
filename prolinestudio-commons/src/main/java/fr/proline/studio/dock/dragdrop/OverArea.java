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
