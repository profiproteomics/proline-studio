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
