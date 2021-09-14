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

package fr.proline.studio.dock.dragdrop;

import fr.proline.studio.dock.container.DockComponent;
import fr.proline.studio.dock.container.DockContainerTab;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

public class DockTransferable implements Transferable
{
    public static final DataFlavor FLAVOR = new DataFlavor(DockTransferable.class, "DockTransferable");

    private DockContainerTab m_dockContainerTab = null;
    private DockComponent m_component = null;

    public DockTransferable(DockContainerTab tabbed, DockComponent component) {
        m_dockContainerTab = tabbed;
        m_component = component;
    }


    @Override
    public DataFlavor[] getTransferDataFlavors()
    {
        return new DataFlavor[]{FLAVOR};
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return FLAVOR.equals(flavor);
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException
    {
        return this;
    }

    public DockContainerTab getDockContainerTab() {
        return m_dockContainerTab;
    }

    public DockComponent getDockComponent() {
        return m_component;
    }

}
