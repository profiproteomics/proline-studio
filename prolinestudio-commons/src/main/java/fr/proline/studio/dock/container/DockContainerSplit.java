package fr.proline.studio.dock.container;

import fr.proline.studio.dock.dragdrop.OverArea;

import javax.swing.*;
import java.awt.*;

public class DockContainerSplit extends DockContainerMulti implements DockReplaceInterface {

    private DockContainerMulti m_leftContainer;
    private DockContainerMulti m_rightContainer;

    private boolean m_canRemoveChildren = true;

    public DockContainerSplit() {

    }

    @Override
    public DockContainer search(String windowKey) {

        DockContainer containerSearched = m_leftContainer.search(windowKey);
        if (containerSearched != null) {
            return containerSearched;

        }

        containerSearched = m_rightContainer.search(windowKey);
        if (containerSearched != null) {
            return containerSearched;

        }

        return null;
    }


    public void setCanRemoveChildren(boolean v) {
        m_canRemoveChildren = v;
    }


    public void add(boolean horizontal, DockContainerMulti leftContainer, DockContainerMulti rightContainer) {

        m_leftContainer = leftContainer;
        m_rightContainer = rightContainer;

        JSplitPane splitPane = new JSplitPane(horizontal ? JSplitPane.HORIZONTAL_SPLIT : JSplitPane.VERTICAL_SPLIT, leftContainer.getComponent(), rightContainer.getComponent());
        splitPane.setDividerSize(5);
        m_component = splitPane;


        leftContainer.setParent(this);
        rightContainer.setParent(this);

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                ((JSplitPane) m_component).setDividerLocation(0.5d);
            }
        });
    }


    @Override
    public OverArea getOverArea(Point pointOnScreen) {

        OverArea overArea = m_leftContainer.getOverArea(pointOnScreen);
        if (overArea != null) {
            return overArea;
        }

        return m_rightContainer.getOverArea(pointOnScreen);

    }

    @Override
    public void replace(DockContainerMulti previous, DockContainerMulti next) {

        JSplitPane splitPane = (JSplitPane) m_component;

        final int oldLocation = splitPane.getDividerLocation();

        if (m_leftContainer == previous) {
            splitPane.remove(m_leftContainer.getComponent());
            splitPane.setLeftComponent(next.getComponent());

            m_leftContainer = next;
            next.setParent(this);

            splitPane.revalidate();
            splitPane.repaint();

        } else if (m_rightContainer == previous) {
            splitPane.remove(m_rightContainer.getComponent());
            splitPane.setRightComponent(next.getComponent());

            m_rightContainer = next;
            next.setParent(this);

            splitPane.revalidate();
            splitPane.repaint();

        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                splitPane.setDividerLocation(oldLocation);
            }
        });
    }

    @Override
    public void remove(DockContainer container) {

        if (!m_canRemoveChildren) {
            return;
        }

        JSplitPane splitPane = (JSplitPane) m_component;

        splitPane.remove(m_leftContainer.getComponent());
        splitPane.remove(m_rightContainer.getComponent());

        DockContainerMulti keptContainer = (m_leftContainer == container) ? m_rightContainer : m_leftContainer;

        m_leftContainer = null;
        m_rightContainer = null;

        ((DockReplaceInterface) getParent()).replace(this, keptContainer);



    }

    public boolean isEmpty() {
        return m_leftContainer.isEmpty() && m_rightContainer.isEmpty();
    }

    @Override
    public DockContainerTab searchTab(int idContainer) {
        DockContainerTab tab = m_leftContainer.searchTab(idContainer);
        if (tab != null) {
            return tab;
        }

        return m_rightContainer.searchTab(idContainer);
    }


    public  void check() {
        DockContainer c = m_leftContainer;
            if (c.getParent() != this) {
                System.err.println("Wrong Parent");
            }
            if (c.getComponent().getParent() != this.getComponent()) {
                System.err.println("Wrong Parent AWT");
            }

            c.check();

        c = m_rightContainer;
        if (c.getParent() != this) {
            System.err.println("Wrong Parent");
        }
        if (c.getComponent().getParent() != this.getComponent()) {
            System.err.println("Wrong Parent AWT");
        }

        c.check();
    }
}
