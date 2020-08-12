package fr.proline.studio.dock.gui;

import fr.proline.studio.dock.container.*;
import fr.proline.studio.dock.dragdrop.DockTransferable;
import fr.proline.studio.dock.dragdrop.DockingImportTransfertHandler;
import fr.proline.studio.dock.dragdrop.OverArea;

import java.awt.*;

public class DraggingOverlayPanel extends javax.swing.JPanel
{

    final DockContainerRoot m_dockContainerRoot;

    final static AlphaComposite ALPHA_COMPOSITE = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);

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

            g2D.setColor(Color.white);


            Composite originalComposite = g2D.getComposite();

            g2D.setComposite(ALPHA_COMPOSITE);
            g2D.fill(m_overArea.getZone(this));
            g2D.setComposite(originalComposite);

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
