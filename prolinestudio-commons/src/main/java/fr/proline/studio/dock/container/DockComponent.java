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


public class DockComponent extends DockContainer {

    final private String m_windowKey;
    private String m_title;
    final private Icon m_icon;

    private DockComponentListener m_componentListener = null;
    private DockMaximizeInterface m_maximizeInterface = null;


    private int m_properties = 0;

    public static final int PROP_NONE = 0;
    public static final int PROP_CLOSE = 0b01;
    public static final int PROP_MINIMIZE = 0b10;


    public DockComponent(AbstractTopPanel topPanel, int properties) {
        this(topPanel.getTopPanelIdentifierKey(), topPanel.getTitle(), (topPanel.getIcon() != null) ? new ImageIcon(topPanel.getIcon()) : null, topPanel, properties);
        topPanel.addTopPanelListener(this);
    }

    public DockComponent(String windowKey, String title, Icon icon, JComponent component, int properties) {
        m_windowKey = windowKey;
        m_title = title;
        m_icon = icon;
        m_component = component;
        m_properties = properties;
    }

    public void setDockComponentListener(DockComponentListener listener) {
        m_componentListener = listener;
    }

    public void removeDockComponentListener() {
        m_componentListener = null;
    }

    public void getTopPanels(HashSet<AbstractTopPanel> set) {

        if (m_component instanceof AbstractTopPanel) {
            set.add((AbstractTopPanel) m_component);
        }

    }

    // TopPanelListener
    @Override
    public void propertyChanged(String property) {
        if(property.equals(TopPanelListener.TITLE_PROPERTY) && (m_component instanceof AbstractTopPanel) ){
            setTitle(((AbstractTopPanel) m_component).getTitle());
        }
    }

    @Override
    public DockContainer search(String windowKey) {

        if (m_windowKey == null) {
            return null;
        }

        if (m_windowKey.equals(windowKey)) {
            return this;
        }

        return null;
    }

    @Override
    public DockContainer searchZoneArea(String zoneArea) {

        if ((m_zoneArea!=null) && (m_zoneArea.equals(zoneArea))) {
            return this;
        }

        return null;
    }

    @Override
    public  void findAllDockComponents(ArrayList<DockComponent> components) {
        components.add(this);
    }

    @Override
    public void toFront() {
        getParent().toFront(this);
    }

    public void maximize() {
        if (m_maximizeInterface != null) {
            m_maximizeInterface.maximize();
        }
    }

    public void setMaximizeInterface(DockMaximizeInterface maximizeInterface) {
        m_maximizeInterface = maximizeInterface;
    }


    public void addProperties(int properties) {
        m_properties |= properties;
    }

    public boolean canClose() {
        return (PROP_CLOSE & m_properties) != 0;
    }

    public boolean canMinimize() {
        return (PROP_MINIMIZE & m_properties) != 0;
    }

    @Override
    public String getTitle() {
        return m_title;
    }

    public void setTitle(String title) {
        m_title = title;
        if (m_componentListener != null) {
            m_componentListener.titleChanged();
        }
    }

    public Icon getIcon()  {
        return null;
    }


    public  void check() {

    }

}
