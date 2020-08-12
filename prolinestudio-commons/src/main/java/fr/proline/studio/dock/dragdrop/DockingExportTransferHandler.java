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
