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

import fr.proline.studio.dock.gui.DraggingOverlayPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;


public class DockingImportTransfertHandler  extends TransferHandler
{
    private DraggingOverlayPanel m_overlayPanel;

    public DockingImportTransfertHandler(DraggingOverlayPanel overlayPanel) {
        m_overlayPanel = overlayPanel;
    }


    @Override
    public boolean canImport(TransferHandler.TransferSupport info)
    {
        Point point = info.getDropLocation().getDropPoint();
        Component source = info.getComponent();


        if (point == null) {
            return false;
        }

        SwingUtilities.convertPointToScreen(point, source);

        OverArea overArea = m_overlayPanel.getOverArea(point);

        if (overArea == null) {
            return false;
        }

        Transferable transferable = info.getTransferable();

        DockTransferable data = null;
        try {
            data = (DockTransferable) transferable.getTransferData(DockTransferable.FLAVOR);
        } catch (UnsupportedFlavorException | IOException ex2) {}

        if (data == null) {
            return false;
        }

        String zoneAreaSource = data.getDockComponent().getZoneArea();

        String zoneAreaDestination = overArea.getDockContainerTab().getZoneArea();
        if (zoneAreaDestination != null) {
            if ((zoneAreaSource == null) || (!zoneAreaSource.equals(zoneAreaDestination))) {
                m_overlayPanel.resetOverArea();
                return false;
            }
        }


        return info.isDataFlavorSupported(DockTransferable.FLAVOR);
    }

    @Override
    public boolean importData(TransferHandler.TransferSupport info) {

        Transferable transferable = info.getTransferable();

        DockTransferable data = null;

        try {
            data = (DockTransferable) transferable.getTransferData(DockTransferable.FLAVOR);
        } catch (UnsupportedFlavorException | IOException ex2) {}

        if (data == null) {
            return false;
        }

        m_overlayPanel.importData(data);

        return true;
    }

}
