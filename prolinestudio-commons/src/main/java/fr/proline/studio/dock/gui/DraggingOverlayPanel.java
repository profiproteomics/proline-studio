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

package fr.proline.studio.dock.gui;

import fr.proline.studio.dock.container.*;
import fr.proline.studio.dock.dragdrop.DockTransferable;
import fr.proline.studio.dock.dragdrop.DockingImportTransfertHandler;
import fr.proline.studio.dock.dragdrop.OverArea;

import java.awt.*;

public class DraggingOverlayPanel extends javax.swing.JPanel
{

    private final DockContainerRoot m_dockContainerRoot;

    private static final AlphaComposite ALPHA_COMPOSITE = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);

    private static final Color OVER_COLOR = new Color(51, 153, 255);

    private OverArea m_overArea = null;


    public DraggingOverlayPanel(DockContainerRoot dockContainerRoot) {
        m_dockContainerRoot = dockContainerRoot;

        setOpaque(false);

        DockingImportTransfertHandler transferHandler = new DockingImportTransfertHandler(this);
        setTransferHandler(transferHandler);



    }

    @Override
    public void paintComponent(Graphics g) {


        Graphics2D g2D = (Graphics2D) g;

        if (m_overArea != null) {

            g2D.setColor(OVER_COLOR);


            Composite previousComposite = g2D.getComposite();

            g2D.setComposite(ALPHA_COMPOSITE);
            g2D.fill(m_overArea.getZone(this));
            g2D.setComposite(previousComposite);

        }

    }

    public OverArea getOverArea(Point pointOnScreen) {

        m_overArea = m_dockContainerRoot.getOverArea(pointOnScreen);

        repaint();

        return m_overArea;
    }

    public void resetOverArea() {
        m_overArea = null;
    }




    public void importData(DockTransferable dataSource) {

        if (m_overArea == null) {
            return;
        }

        // Source
        DockContainerTab dockContainerTab = dataSource.getDockContainerTab();
        DockComponent dockComponent =  dataSource.getDockComponent();

        dockContainerTab.remove(dockComponent);

        DockContainerTab destination = m_overArea.getDockContainerTab();
        DockPosition position = m_overArea.getPosition();

        try {
            destination.add(dockComponent, position);
        } catch (DockException e) {
            // can not happen
        }

        dockContainerTab.removeIfEmpty();

    }


}
