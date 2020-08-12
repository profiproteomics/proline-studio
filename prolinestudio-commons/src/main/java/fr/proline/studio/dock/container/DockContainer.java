package fr.proline.studio.dock.container;


import javax.swing.*;

public abstract class DockContainer {

    protected JComponent m_component = null;
    private DockContainer m_parent;


    protected String m_zoneArea = null;


    private int m_id = ID_COUNT++;
    private int m_lastParentId = -1;
    private static int ID_COUNT = 0;

    public DockContainer() {
        m_parent = null;
    }

    public abstract DockContainer search(String windowKey);
    public void toFront() {
        // nothing to do
    }
    public void toFront(DockContainer child) {
        // nothing to do
    }



    public void setParent(DockContainer parent) {
        m_parent = parent;
        if (parent != null) {
            m_lastParentId = parent.getId();
        }
    }

    public int getId() {
        return m_id;
    }

    public int getLastParentId() {
        return m_lastParentId;
    }


    public void setZoneArea(String zoneArea) {
        m_zoneArea = zoneArea;
    }

    public String getZoneArea() {
        if (m_zoneArea != null) {
            return m_zoneArea;
        }
        if (m_parent != null) {
            return m_parent.getZoneArea();
        }
        return null;
    }


    public abstract void check();


    public DockContainer getParent() {
        return m_parent;
    }

    public void minimize(DockContainer container) {
        if (m_parent != null) {
            m_parent.minimize(container);
        }
    }

    public DockContainerRoot getRoot() {
        DockContainer root = this;
        while (root.getParent() != null) {
            root  = root.getParent();
        }
        return (DockContainerRoot) root;
    }


    public JComponent getComponent() {
        return m_component;
    }

    public String getTitle() {
        return "";
    }

}
