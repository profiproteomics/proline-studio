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

package fr.proline.studio.dock.container;

import fr.proline.studio.dock.AbstractTopPanel;
import fr.proline.studio.dock.dragdrop.OverArea;
import fr.proline.studio.dock.gui.TabbedPaneLabel;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;

public class DockContainerTab extends DockContainerMulti {

    private HashSet<DockContainer> m_dockContainerSet = new HashSet();

    public DockContainerTab() {
        final JTabbedPane tabbedPane = new JTabbedPane();
        m_component = tabbedPane;

    }

    public void toFront(DockContainer child) {
        JTabbedPane tabbedPane = ((JTabbedPane)m_component);

        int index = tabbedPane.indexOfComponent(child.getComponent());
        if (index != -1) {
            tabbedPane.setSelectedIndex(index);
        }
    }

    public void getTopPanels(HashSet<AbstractTopPanel> set) {
        for (DockContainer c : m_dockContainerSet) {
            c.getTopPanels(set);
        }
    }


    @Override
    public DockContainer search(String windowKey) {

        for (DockContainer c : m_dockContainerSet) {
            DockContainer containerSearched = c.search(windowKey);
            if (containerSearched != null) {
                return containerSearched;

            }
        }
        return null;
    }

    @Override
    public DockContainer searchZoneArea(String zoneArea) {

        if ((m_zoneArea!=null) && (m_zoneArea.equals(zoneArea))) {
            return this;
        }


        for (DockContainer c : m_dockContainerSet) {
            DockContainer containerSearched = c.searchZoneArea(zoneArea);
            if (containerSearched != null) {
                return containerSearched;

            }
        }
        return null;
    }

    @Override
    public void findAllDockComponents(ArrayList<DockComponent> components) {

        for (DockContainer c : m_dockContainerSet) {
            c.findAllDockComponents(components);
        }
    }




    public void add(DockContainer container, DockPosition position)  throws DockException {

        if (! (container instanceof DockComponent)) {
            throw new DockException("Must add DockComponent to DockContainerTab");
        }

        DockComponent component = (DockComponent) container;
        DockReplaceInterface parent = (DockReplaceInterface) getParent();



        switch(position) {
            case CENTER: {
                add(component);
                break;
            }
            case WEST: {
                DockContainerSplit dockContainerSplit = new DockContainerSplit();
                DockContainerTab containerTab = new DockContainerTab();
                containerTab.setZoneArea(getZoneArea());
                containerTab.add(component);
                dockContainerSplit.add(true, containerTab, this);
                parent.replace(this, dockContainerSplit);
                break;
            }
            case EAST: {
                DockContainerSplit dockContainerSplit = new DockContainerSplit();
                DockContainerTab containerTab = new DockContainerTab();
                containerTab.setZoneArea(getZoneArea());
                containerTab.add(component);
                this.getComponent().getParent().remove(this.getComponent());
                dockContainerSplit.add(true, this, containerTab);
                parent.replace(this, dockContainerSplit);
                break;
            }
            case NORTH: {
                DockContainerSplit dockContainerSplit = new DockContainerSplit();
                DockContainerTab containerTab = new DockContainerTab();
                containerTab.setZoneArea(getZoneArea());
                containerTab.add(component);
                dockContainerSplit.add(false, containerTab, this);
                parent.replace(this, dockContainerSplit);
                break;
            }
            case SOUTH: {
                DockContainerSplit dockContainerSplit = new DockContainerSplit();
                DockContainerTab containerTab = new DockContainerTab();
                containerTab.setZoneArea(getZoneArea());
                containerTab.add(component);
                this.getComponent().getParent().remove(this.getComponent());
                dockContainerSplit.add(false, this, containerTab);
                parent.replace(this, dockContainerSplit);
                break;
            }
        }





        getRoot().check();
    }

    public void add(DockComponent container) {

        JTabbedPane tabbedPane = ((JTabbedPane)m_component);

        tabbedPane.addTab(container.getTitle(), container.getComponent());

        int index = tabbedPane.indexOfComponent(container.getComponent());

        TabbedPaneLabel tpl =  new TabbedPaneLabel(this, container);
        tabbedPane.setTabComponentAt(index, tpl);
        tabbedPane.setSelectedIndex(index);

        m_dockContainerSet.add(container);
        container.setParent(this);

        if (container.getComponent() instanceof AbstractTopPanel) {
            AbstractTopPanel topPanel = (AbstractTopPanel) container.getComponent();
            topPanel.componentAdded();
        }

    }

    @Override
    public void remove(DockContainer container) {

        m_dockContainerSet.remove(container);

        JTabbedPane tabbedPane = ((JTabbedPane)m_component);

        int index = tabbedPane.indexOfComponent(container.getComponent());
        tabbedPane.remove(index);
        container.setParent(null);

        removeIfEmpty();


    }

    public boolean isEmpty() {
        return m_dockContainerSet.isEmpty();
    }

    @Override
    public DockContainerTab searchTab(int idContainer) {
        if (idContainer == -1) {
            return this;
        }
        if (idContainer == getId()) {
            return this;
        }
        return null;
    }

    public void removeIfEmpty() {
        if (! m_dockContainerSet.isEmpty() || !m_canRemoveChildren) {
            return;
        }
        ((DockContainerMulti) getParent()).remove(this);

    }

    @Override
    public OverArea getOverArea(Point pointOnScreen) {

        Point localPoint = new Point(pointOnScreen);
        SwingUtilities.convertPointFromScreen(localPoint, m_component);

        Rectangle bounds = m_component.getBounds();
        bounds.x = 0;
        bounds.y = 0;

        if (! bounds.contains(localPoint)) {
            return null;
        }

        if (localPoint.y < bounds.y + bounds.height / 3) {
            return new OverArea(this, DockPosition.NORTH);
        }

        if (localPoint.y > bounds.y + bounds.height * 2 / 3) {
            return new OverArea(this, DockPosition.SOUTH);
        }

        if (localPoint.x > bounds.x + bounds.width * 2 / 3) {
            return new OverArea(this, DockPosition.EAST);
        }

        if (localPoint.x < bounds.x + bounds.width / 3) {
            return new OverArea(this, DockPosition.WEST);
        }

        return new OverArea(this, DockPosition.CENTER);

    }

    public  void check() {
        for (DockContainer c : m_dockContainerSet) {
            if (c.getParent() != this) {
                System.err.println("Wrong Parent");
            }
            if (c.getComponent().getParent() != this.getComponent()) {
                System.err.println("Wrong Parent AWT");
            }

            c.check();
        }
    }


}
