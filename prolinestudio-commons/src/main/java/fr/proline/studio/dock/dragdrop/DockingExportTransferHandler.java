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

package fr.proline.studio.dock.dragdrop;

import fr.proline.studio.dock.container.DockComponent;
import fr.proline.studio.dock.container.DockContainerRoot;
import fr.proline.studio.dock.container.DockContainerTab;

import javax.swing.*;
import java.awt.datatransfer.Transferable;

public class DockingExportTransferHandler extends TransferHandler {

    private DockContainerTab m_dockContainerTab = null;
    private DockComponent m_component = null;

    public DockingExportTransferHandler(DockContainerTab tabbed, DockComponent component) {
        m_dockContainerTab = tabbed;
        m_component = component;
    }

    @Override
    public int getSourceActions(JComponent c) {

        return MOVE;
    }

    @Override
    public Transferable createTransferable(JComponent c) {

        ((DockContainerRoot) m_dockContainerTab.getRoot()).startDragging();

        DockTransferable dockTransferable = new DockTransferable(m_dockContainerTab, m_component);

        return dockTransferable;
    }

    @Override
    public void exportDone(JComponent c, Transferable t, int action) {
       ((DockContainerRoot) m_dockContainerTab.getRoot()).stopDragging();
    }
}
