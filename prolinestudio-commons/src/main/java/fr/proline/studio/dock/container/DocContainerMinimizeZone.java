package fr.proline.studio.dock.container;

import fr.proline.studio.dock.dragdrop.OverArea;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.util.HashMap;

public class DocContainerMinimizeZone extends DockContainerMulti implements DockReplaceInterface {

    private DockMinimizePanel m_minimizePanel;

    private DockContainerMulti m_dockContainerMulti;

    private int m_previousSplitLocation = -1;

    public DocContainerMinimizeZone() {

        m_minimizePanel = new DockMinimizePanel(this);

        JPanel containerPanel = new JPanel(new BorderLayout());

        containerPanel.add(m_minimizePanel, BorderLayout.WEST);

        m_component = containerPanel;

    }

    @Override
    public DockContainer search(String windowKey) {

        DockContainer containerSearched = m_dockContainerMulti.search(windowKey);
        if (containerSearched != null) {
            return containerSearched;
        }

        return m_minimizePanel.search(windowKey);
    }

    public void set(DockContainerMulti containerMulti) {
        m_dockContainerMulti = containerMulti;
        containerMulti.setParent(this);
        ((JPanel) m_component).add(containerMulti.getComponent(), BorderLayout.CENTER);
    }


    public void minimize(DockContainer container) {
        m_minimizePanel.add(container);

        if (m_dockContainerMulti.isEmpty()) {
            m_dockContainerMulti.getComponent().setVisible(false);



            if (getParent() instanceof DockContainerSplit) {
                m_previousSplitLocation = ((JSplitPane) ((DockContainerSplit)  getParent() ).getComponent()).getDividerLocation();
                ((JSplitPane) ((DockContainerSplit)  getParent() ).getComponent()).resetToPreferredSizes();
            }
        }

    }

    public void putBack(DockContainer container) {
        if (m_dockContainerMulti.isEmpty()) {
            m_dockContainerMulti.getComponent().setVisible(true);
        }

        DockContainerTab tab = searchTab(container.getLastParentId());
        if (tab == null) {
            tab = searchTab(-1);
        }

        tab.add((DockComponent) container);


        if (getParent() instanceof DockContainerSplit) {
            ((JSplitPane) ((DockContainerSplit)  getParent() ).getComponent()).setDividerLocation(m_previousSplitLocation);
        }

    }

    public void toFront(DockContainer child) {
        m_minimizePanel.remove(child);
    }

    @Override
    public void check() {

    }

    @Override
    public void remove(DockContainer container) {
        // nothing to do
    }

    public boolean isEmpty() {
        return false;
    }

    @Override
    public DockContainerTab searchTab(int idContainer) {
        return m_dockContainerMulti.searchTab(idContainer);
    }



    @Override
    public OverArea getOverArea(Point pointOnScreen) {

        if (! m_dockContainerMulti.getComponent().isVisible()) {
            return null;
        }
        return m_dockContainerMulti.getOverArea(pointOnScreen);
    }

    @Override
    public void replace(DockContainerMulti previous, DockContainerMulti next) {

        if (m_dockContainerMulti == previous) {
            m_component.remove(previous.getComponent());
            ((JPanel) m_component).add(next.getComponent(), BorderLayout.CENTER);

            m_dockContainerMulti = next;
            next.setParent(this);

            m_component.repaint();

        }

    }


    public class DockMinimizePanel extends JPanel {

        private DocContainerMinimizeZone m_containerMinimizeZone;
        private HashMap<DockContainer, MinimizedLabel> m_componentMap = new HashMap<>();

        public DockMinimizePanel(DocContainerMinimizeZone containerMinimizeZone) {

            m_containerMinimizeZone = containerMinimizeZone;

            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

            setBackground(Color.white);

            setVisible(false);
        }

        public DockContainer search(String windowKey) {
            for (DockContainer c : m_componentMap.keySet()) {
                DockContainer containerSearched = c.search(windowKey);
                if (containerSearched != null) {
                    return containerSearched;
                }
            }
            return null;

        }

        public void add(DockContainer component) {

            MinimizedLabel l = new MinimizedLabel(this, component);
            add(l);

            m_componentMap.put(component, l);

            if (!isVisible()) {
                setVisible(true);
            }

            revalidate();
            repaint();
        }

        public void remove(DockContainer component) {

            JComponent l = m_componentMap.remove(component);

            remove(l);

            m_containerMinimizeZone.putBack(component);

            revalidate();
            repaint();
        }


    }


    public class MinimizedLabel extends JPanel implements MouseListener {

        private static final int PAD = 2;

        private DockMinimizePanel m_parentPanel;
        private DockContainer m_component;


        private Font m_font = javax.swing.UIManager.getDefaults().getFont("Label.font");
        private Dimension m_preferredSize = null;

        public MinimizedLabel(DockMinimizePanel parentPanel, DockContainer component) {

            m_parentPanel = parentPanel;
            m_component = component;

            addMouseListener(this);
        }

        @Override
        public Dimension getPreferredSize() {
            if (m_preferredSize == null) {

                Icon icon = ((DockComponent)m_component).getIcon();
                String title = ((DockComponent)m_component).getTitle();

                FontMetrics fm = m_component.getComponent().getFontMetrics(m_font);
                int stringWidth = fm.stringWidth(title);

                int height = PAD*3+ ((icon !=null) ? icon.getIconWidth() : 0)+stringWidth;
                int width = Math.max(20, ((icon !=null) ? icon.getIconHeight(): 0) + PAD*2);



                m_preferredSize = new Dimension(width,height);
            }

            return m_preferredSize;
        }

        @Override
        public Dimension getMaximumSize() {
            return getPreferredSize();
        }

        public void paint(Graphics g) {

            Graphics2D g2d = (Graphics2D) g;

            //g2d.setColor(Color.blue);
            //g.fillRect(0,0,getWidth(), getHeight());

            Font oldFont = g2d.getFont();

            g2d.setFont(m_font);

            FontMetrics fm = g2d.getFontMetrics();

            AffineTransform previousTransform = g2d.getTransform();

            //g2d.rotate( Math.PI / 2 );
            //g2d.translate( 0, - getWidth() );
            g2d.rotate( - Math.PI / 2 );
            g2d.translate( - getHeight(), 0 );

            Icon icon = ((DockComponent)m_component).getIcon();
            if (icon != null) {
                icon.paintIcon(this, g2d, PAD, (getWidth()-icon.getIconHeight())/2  ); //JPM.TODO
            }

            String title = ((DockComponent)m_component).getTitle();

            if (title != null) {
                int textX = 2*PAD + ((icon!=null) ? icon.getIconWidth() : 0);
                int textY = PAD + fm.getAscent();

                g2d.setColor(Color.black);
                g2d.drawString(title, textX, textY);

            }

            g2d.setFont(oldFont);
            g2d.setTransform( previousTransform );
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            m_parentPanel.remove(m_component);
        }

        @Override
        public void mousePressed(MouseEvent e) {

        }

        @Override
        public void mouseReleased(MouseEvent e) {

        }

        @Override
        public void mouseEntered(MouseEvent e) {

        }

        @Override
        public void mouseExited(MouseEvent e) {

        }
    }
}
