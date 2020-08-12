package fr.proline.studio.dock.container;

import java.awt.*;

public enum DockPosition {

    CENTER(BorderLayout.CENTER),
    WEST(BorderLayout.WEST),
    EAST(BorderLayout.EAST),
    NORTH(BorderLayout.NORTH),
    SOUTH(BorderLayout.SOUTH);

    private String m_borderLayout;

    DockPosition(String borderLayout) {
        m_borderLayout = borderLayout;
    }

    public String getBorderLayout() {
        return m_borderLayout;
    }
}
