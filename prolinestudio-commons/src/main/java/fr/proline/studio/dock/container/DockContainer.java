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


import fr.proline.studio.dock.AbstractTopPanel;
import fr.proline.studio.dock.TopPanelListener;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashSet;

public abstract class DockContainer implements TopPanelListener {

    protected JComponent m_component = null; //correspond to graphical component associated to this DockContainer
    private DockContainer m_parent;


    protected String m_zoneArea = null;


    private int m_id = ID_COUNT++;
    private int m_lastParentId = -1;
    private static int ID_COUNT = 0;

    public DockContainer() {
        m_parent = null;
    }

    public abstract DockContainer search(String windowKey);
    public abstract DockContainer searchZoneArea(String zoneArea);

    public abstract void findAllDockComponents(ArrayList<DockComponent> components);

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

    public abstract void getTopPanels(HashSet<AbstractTopPanel> set);


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

    // TopPanelListener
    @Override
    public void propertyChanged(String property) {
        //Default  behaviour : Nothing to do
    }
}
