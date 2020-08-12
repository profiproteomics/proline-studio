package fr.proline.studio.dock.container;

import fr.proline.studio.dock.dragdrop.OverArea;
import fr.proline.studio.dock.gui.DraggingOverlayPanel;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

public class DockContainerRoot extends DockContainer implements DockMultiInterface, DockReplaceInterface {

    private final HashMap<DockContainerMulti, DockPosition> m_containerMap = new HashMap<>();


    private final DraggingOverlayPanel m_draggingOverlayPanel;
    private JComponent m_oldGlassPanel = null;

    private JPanel m_mainPanel;


    public DockContainerRoot() {

        m_component = new JPanel(new BorderLayout());

        m_draggingOverlayPanel = new DraggingOverlayPanel(this);

        initComponents();
    }

    private void initComponents() {

        m_mainPanel = new JPanel(new BorderLayout());

        m_mainPanel.add(m_component, BorderLayout.CENTER);

    }

    public JPanel getMainPanel() {
        return m_mainPanel;
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

    /*public void minimize(DockContainer container) {
        m_dockMinimizePanel.add(container);
    }*/




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
