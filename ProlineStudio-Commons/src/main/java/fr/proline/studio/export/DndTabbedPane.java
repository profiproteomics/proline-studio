package fr.proline.studio.export;


import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

class DnDTabbedPane extends JTabbedPane {

    private static final int LINEWIDTH = 3;
    private static final String NAME = "test";
    private final GhostGlassPane m_glassPane = new GhostGlassPane();
    private final Rectangle2D m_lineRect = new Rectangle2D.Double();
    private static final Color LINE_COLOR = new Color(0, 100, 255);

    private final DnDTabbedPane m_tabbedPane;
    
    protected int m_dragTabIndex = -1;

    public DnDTabbedPane(int parameter) {
        super(parameter); // uses constructor to apply tab panes in BOTTOM mode (like in EXCEL tabs)
        
        m_tabbedPane = this;
        
        final DragSourceListener dsl = new DragSourceListener() {
            @Override
            public void dragEnter(DragSourceDragEvent e) {
                e.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
            }

            @Override
            public void dragExit(DragSourceEvent e) {
                e.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
                m_lineRect.setRect(0, 0, 0, 0);
                m_glassPane.setPoint(new Point(-1000, -1000));
                m_glassPane.repaint();
            }

            @Override
            public void dragOver(DragSourceDragEvent e) {
	        //e.getLocation()
                //This method returns a Point indicating the cursor location in screen coordinates at the moment
                Point tabPt = e.getLocation();
                SwingUtilities.convertPointFromScreen(tabPt, DnDTabbedPane.this);
                Point glassPt = e.getLocation();
                SwingUtilities.convertPointFromScreen(glassPt, m_glassPane);
                int targetIdx = getTargetTabIndex(glassPt);
                if (getTabAreaBound().contains(tabPt) && targetIdx >= 0
                        && targetIdx != m_dragTabIndex && targetIdx != m_dragTabIndex + 1) {
                    e.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
                } else {
                    e.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
                }
            }

            @Override
            public void dragDropEnd(DragSourceDropEvent e) {
                m_lineRect.setRect(0, 0, 0, 0);
                m_dragTabIndex = -1;
                if (hasGhost()) {
                    m_glassPane.setVisible(false);
                    m_glassPane.setImage(null);
                }
            }

            @Override
            public void dropActionChanged(DragSourceDragEvent e) {
            }
        };
        final Transferable t = new Transferable() {
            private final DataFlavor FLAVOR = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType, NAME);

            @Override
            public Object getTransferData(DataFlavor flavor) {
                return DnDTabbedPane.this;
            }

            @Override
            public DataFlavor[] getTransferDataFlavors() {
                DataFlavor[] f = new DataFlavor[1];
                f[0] = this.FLAVOR;
                return f;
            }

            @Override
            public boolean isDataFlavorSupported(DataFlavor flavor) {
                return flavor.getHumanPresentableName().equals(NAME);
            }
        };
        final DragGestureListener dgl = new DragGestureListener() {
            @Override
            public void dragGestureRecognized(DragGestureEvent e) {
                Point tabPt = e.getDragOrigin();
                m_dragTabIndex = indexAtLocation(tabPt.x, tabPt.y);
                if (m_dragTabIndex < 0) {
                    return;
                }
                initGlassPane(e.getComponent(), e.getDragOrigin());
                try {
                    e.startDrag(DragSource.DefaultMoveDrop, t, dsl);
                } catch (InvalidDnDOperationException idoe) {
                    //idoe.printStackTrace();
                }
            }
        };
        //dropTarget =
        new DropTarget(m_glassPane, DnDConstants.ACTION_COPY_OR_MOVE, new CDropTargetListener(), true);
        new DragSource().createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY_OR_MOVE, dgl);
    }

    class CDropTargetListener implements DropTargetListener {

        @Override
        public void dragEnter(DropTargetDragEvent e) {
            if (isDragAcceptable(e)) {
                e.acceptDrag(e.getDropAction());
            } else {
                e.rejectDrag();
            }
        }

        @Override
        public void dragExit(DropTargetEvent e) {
        }

        @Override
        public void dropActionChanged(DropTargetDragEvent e) {
        }

        @Override
        public void dragOver(final DropTargetDragEvent e) {
            if (getTabPlacement() == JTabbedPane.TOP || getTabPlacement() == JTabbedPane.BOTTOM) {
                initTargetLeftRightLine(getTargetTabIndex(e.getLocation()));
            } else {
                initTargetTopBottomLine(getTargetTabIndex(e.getLocation()));
            }
            repaint();
            if (hasGhost()) {
                m_glassPane.setPoint(e.getLocation());
                m_glassPane.repaint();
            }
        }

        @Override
        public void drop(DropTargetDropEvent e) {
            if (isDropAcceptable(e)) {
                convertTab(m_dragTabIndex, getTargetTabIndex(e.getLocation()));
                e.dropComplete(true);
            } else {
                e.dropComplete(false);
            }
            repaint();
        }

        public boolean isDragAcceptable(DropTargetDragEvent e) {
            Transferable t = e.getTransferable();
            if (t == null) {
                return false;
            }
            DataFlavor[] f = e.getCurrentDataFlavors();
            if (t.isDataFlavorSupported(f[0]) && m_dragTabIndex >= 0) {
                return true;
            }
            return false;
        }

        public boolean isDropAcceptable(DropTargetDropEvent e) {
            Transferable t = e.getTransferable();
            if (t == null) {
                return false;
            }
            DataFlavor[] f = t.getTransferDataFlavors();
            if (t.isDataFlavorSupported(f[0]) && m_dragTabIndex >= 0) {
                return true;
            }
            return false;
        }
    }

    private boolean hasGhost = true;

    public void setPaintGhost(boolean flag) {
        hasGhost = flag;
    }

    public boolean hasGhost() {
        return hasGhost;
    }

    private int getTargetTabIndex(Point glassPt) {
        Point tabPt = SwingUtilities.convertPoint(m_glassPane, glassPt, this);
        for (int i = 0; i < getTabCount(); i++) {
            Rectangle r = getBoundsAt(i);
            if (r.contains(tabPt)) {
                r.translate(r.width/2, 0);
                if (r.contains(tabPt)) {
                    return i+1;
                } else {
                    return i;
                }
            }
        }
        Rectangle r = getBoundsAt(getTabCount() - 1);
        r.translate(r.width, 0);
        return r.contains(tabPt) ? getTabCount() : -1;
    }

    private void convertTab(int prev, int next) {
        if (next < 0 || prev == next) {
            return;
        }
        Component cmp = getComponentAt(prev);
        CheckboxTabPanel tabComponent = ((CheckboxTabPanel)getTabComponentAt(prev));
        String str = tabComponent.getText();
        String idSheet = tabComponent.getSheetId();
        boolean isSelected = tabComponent.isSelected();
        if (next == getTabCount()) {
            remove(prev);
            addTab(null, cmp);
            CheckboxTabPanel closableTabPanel = new CheckboxTabPanel(this, str, idSheet);
            closableTabPanel.setSelected(isSelected);
            setTabComponentAt(getTabCount() - 1, closableTabPanel);
            setSelectedIndex(getTabCount() - 1);
        } else if (prev > next) {
            remove(prev);
            CheckboxTabPanel closableTabPanel = new CheckboxTabPanel(this, str, idSheet);
            closableTabPanel.setSelected(isSelected);
            insertTab(null, null, cmp, null, next);
            setTabComponentAt(next, closableTabPanel);
            setSelectedIndex(next);
        } else {
            remove(prev);
            insertTab(null, null, cmp, null, next - 1);
            CheckboxTabPanel closableTabPanel = new CheckboxTabPanel(this, str, idSheet);
            closableTabPanel.setSelected(isSelected);
            setTabComponentAt(next - 1, closableTabPanel);
            setSelectedIndex(next - 1);
        }
    }

    private void initTargetLeftRightLine(int next) {
        if (next < 0 || m_dragTabIndex == next || next - m_dragTabIndex == 1) {
            m_lineRect.setRect(0, 0, 0, 0);
        } else if (next == getTabCount()) {
            Rectangle rect = getBoundsAt(getTabCount() - 1);
            m_lineRect.setRect(rect.x + rect.width - LINEWIDTH / 2, rect.y, LINEWIDTH, rect.height);
        } else if (next == 0) {
            Rectangle rect = getBoundsAt(0);
            m_lineRect.setRect(-LINEWIDTH / 2, rect.y, LINEWIDTH, rect.height);
        } else {
            Rectangle rect = getBoundsAt(next - 1);
            m_lineRect.setRect(rect.x + rect.width - LINEWIDTH / 2, rect.y, LINEWIDTH, rect.height);
        }
    }

    private void initTargetTopBottomLine(int next) {
        if (next < 0 || m_dragTabIndex == next || next - m_dragTabIndex == 1) {
            m_lineRect.setRect(0, 0, 0, 0);
        } else if (next == getTabCount()) {
            Rectangle rect = getBoundsAt(getTabCount() - 1);
            m_lineRect.setRect(rect.x, rect.y + rect.height - LINEWIDTH / 2, rect.width, LINEWIDTH);
        } else if (next == 0) {
            Rectangle rect = getBoundsAt(0);
            m_lineRect.setRect(rect.x, -LINEWIDTH / 2, rect.width, LINEWIDTH);
        } else {
            Rectangle rect = getBoundsAt(next - 1);
            m_lineRect.setRect(rect.x, rect.y + rect.height - LINEWIDTH / 2, rect.width, LINEWIDTH);
        }
    }

    private void initGlassPane(Component c, Point tabPt) {

        getRootPane().setGlassPane(m_glassPane);
        if (hasGhost()) {
            Rectangle rect = getBoundsAt(m_dragTabIndex);
            BufferedImage image = new BufferedImage(c.getWidth(), c.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics g = image.getGraphics();
            c.paint(g);
            try {
                image = image.getSubimage(rect.x, rect.y, rect.width, rect.height);
            } catch (Exception e) {
            }
            m_glassPane.setImage(image);
        }
        Point glassPt = SwingUtilities.convertPoint(c, tabPt, m_glassPane);
        m_glassPane.setPoint(glassPt);
        m_glassPane.setVisible(true);
    }

    private Rectangle getTabAreaBound() {
        Rectangle lastTab = getUI().getTabBounds(this, getTabCount() - 1);
        return new Rectangle(0, 0, getWidth(), lastTab.y + lastTab.height);
    }

    /*@Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        
        if (m_dragTabIndex >= 0) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setPaint(LINE_COLOR);
            g2.fill(m_lineRect);
            
            
        }
    }*/
    
    private class GhostGlassPane extends JPanel {

        private final AlphaComposite composite;
        private Point location = new Point(0, 0);
        private BufferedImage draggingGhost = null;

        public GhostGlassPane() {
            setOpaque(false);
            composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
        }

        public void setImage(BufferedImage draggingGhost) {
            this.draggingGhost = draggingGhost;
        }

        public void setPoint(Point location) {
            this.location = location;
        }

        @Override
        public void paintComponent(Graphics g) {
            if (draggingGhost == null) {
                return;
            }
            Graphics2D g2 = (Graphics2D) g;
            g2.setComposite(composite);
            double xx = location.getX() - (draggingGhost.getWidth(this) / 2d);
            double yy = location.getY() - (draggingGhost.getHeight(this) / 2d);
            g2.drawImage(draggingGhost, (int) xx, (int) yy, null);

            if (m_dragTabIndex >= 0) {

                Rectangle2D r = new Rectangle2D.Double(m_lineRect.getX()+getX()-m_tabbedPane.getX(), m_lineRect.getY()+getY()-m_tabbedPane.getY(), m_lineRect.getWidth(), m_lineRect.getHeight());
                g2.setPaint(LINE_COLOR);
                g2.fill(r);

            }
        }
    }
}


