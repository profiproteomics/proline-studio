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
import fr.proline.studio.dock.gui.InfoLabel;
import fr.proline.studio.dock.gui.DraggingOverlayPanel;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.HashSet;

public class DockContainerRoot extends DockContainer implements DockMultiInterface, DockReplaceInterface {

    private final HashMap<DockContainerMulti, DockPosition> m_containerMap = new HashMap<>();


    private final DraggingOverlayPanel m_draggingOverlayPanel;
    private JComponent m_oldGlassPanel = null;

    private JPanel m_mainPanel;
    private InfoLabel m_infoLabel;


    public DockContainerRoot() {

        m_component = new JPanel(new BorderLayout());

        m_draggingOverlayPanel = new DraggingOverlayPanel(this);

        initComponents();
    }

    private void initComponents() {

        m_mainPanel = new JPanel(new BorderLayout());

        m_mainPanel.add(m_component, BorderLayout.CENTER);

        m_infoLabel = new InfoLabel();
        m_mainPanel.add(m_infoLabel, BorderLayout.SOUTH);


    }

    public JPanel getMainPanel() {
        return m_mainPanel;
    }

    public InfoLabel getInfoLabel() {
        return m_infoLabel;
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
            //Point containerPoint = SwingUtilities.convertPoint(getComponent(), screenPoint, container.getComponent());
            OverArea overArea = container.getOverArea(screenPoint);
            if (overArea != null) {
                return overArea;
            }

        }

        return null;
    }

    public void startDragging() {


        JRootPane rootPane = m_component.getRootPane();
        m_oldGlassPanel = (JComponent) rootPane.getGlassPane();

        rootPane.setGlassPane(m_draggingOverlayPanel);
        m_draggingOverlayPanel.setVisible(true);

        rootPane.revalidate();
        rootPane.repaint();
    }

    public void stopDragging() {

        JRootPane rootPane = m_component.getRootPane();

        rootPane.setGlassPane(m_oldGlassPanel);
        m_oldGlassPanel = null;

        rootPane.revalidate();
        rootPane.repaint();
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
