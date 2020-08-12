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
