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
import fr.proline.studio.dock.dragdrop.OverArea;
import fr.proline.studio.dock.gui.InfoLabel;
import fr.proline.studio.dock.gui.DraggingOverlayPanel;
import fr.proline.studio.dock.gui.MemoryPanel;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class DockContainerRoot extends DockContainer implements DockMultiInterface, DockReplaceInterface {

    private final HashMap<DockContainerMulti, DockPosition> m_containerMap = new HashMap<>();


    private final DraggingOverlayPanel m_draggingOverlayPanel;

    private JPanel m_mainPanel;
    private InfoLabel m_infoLabel;
    private MemoryPanel m_memoryPanel;


    public DockContainerRoot() {

        m_component = new JPanel(new BorderLayout());

        m_draggingOverlayPanel = new DraggingOverlayPanel(this);

        initComponents();
    }

    private void initComponents() {

        m_mainPanel = new JPanel(new BorderLayout());

        m_mainPanel.add(m_component, BorderLayout.CENTER);

        JPanel southPanel = new JPanel(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(2, 2, 2, 2);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        c.weighty = 0;

        m_memoryPanel = new MemoryPanel();
        southPanel.add(m_memoryPanel, c);

        //c.gridx++;
        //southPanel.add(Box.createHorizontalStrut(6), c);

        c.gridx++;
        c.weightx = 1;
        m_infoLabel = new InfoLabel();
        southPanel.add(m_infoLabel, c);






        m_mainPanel.add(southPanel, BorderLayout.SOUTH);


    }

    public JPanel getMainPanel() {
        return m_mainPanel;
    }

    public InfoLabel getInfoLabel() {
        return m_infoLabel;
    }

    public MemoryPanel getMemoryPanel() {
        return m_memoryPanel;
    }

    public void getTopPanels(HashSet<AbstractTopPanel> set) {

        for (DockContainerMulti c : m_containerMap.keySet()) {
            c.getTopPanels(set);
        }

    }

    @Override
    public DockContainer search(String windowKey) {

        for (DockContainerMulti c : m_containerMap.keySet()) {
            DockContainer containerSearched = c.search(windowKey);
            if (containerSearched != null) {
                return containerSearched;

            }
        }
        return null;
    }

    @Override
    public DockContainer searchZoneArea(String zoneArea) {

        for (DockContainerMulti c : m_containerMap.keySet()) {
            DockContainer containerSearched = c.searchZoneArea(zoneArea);
            if (containerSearched != null) {
                return containerSearched;

            }
        }
        return null;
    }

    @Override
    public void findAllDockComponents(ArrayList<DockComponent> components) {
        for (DockContainerMulti c : m_containerMap.keySet()) {
            c.findAllDockComponents(components);
        }

    }


    public void add(DockContainer container, DockPosition position) throws DockException {

        if (! (container instanceof DockContainerMulti)) {
            throw new DockException("Must add DockContainerMulti to DockContainerRoot");
        }

        m_containerMap.put((DockContainerMulti) container, position); // can add only DockContainerMulti
        ((JPanel) m_component).add(container.getComponent(), position.getBorderLayout());
        container.setParent(this);
    }


    public OverArea getOverArea(Point screenPoint) {

        for (DockContainerMulti container : m_containerMap.keySet()) {

            OverArea overArea = container.getOverArea(screenPoint);
            if (overArea != null) {
                return overArea;
            }

        }

        return null;
    }

    public void startDragging() {

        if (firstDrag) {
            firstDrag = false;
            JRootPane rootPane = m_component.getRootPane();
            rootPane.setGlassPane(m_draggingOverlayPanel);
        }

        m_draggingOverlayPanel.setVisible(true);

        m_component.repaint();
    }
    private boolean firstDrag = true;

    public void stopDragging() {

        m_draggingOverlayPanel.setVisible(false);

        m_component.repaint();
    }

    @Override
    public void replace(DockContainerMulti previous, DockContainerMulti next) {


        DockPosition position = m_containerMap.remove(previous);

        JPanel panel = (JPanel) m_component;

        panel.remove(previous.getComponent());

        panel.add(next.getComponent(), position.getBorderLayout());

        m_containerMap.put(next, position);

        next.setParent(this);

        panel.revalidate();
        panel.repaint();
    }


    public  void check() {
        for (DockContainer c : m_containerMap.keySet()) {
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
