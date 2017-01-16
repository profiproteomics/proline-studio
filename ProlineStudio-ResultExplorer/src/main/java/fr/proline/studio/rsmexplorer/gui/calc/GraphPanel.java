package fr.proline.studio.rsmexplorer.gui.calc;

import fr.proline.studio.graphics.SelectionGestureLasso;
import fr.proline.studio.graphics.SelectionGestureSquare;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.python.data.TableInfo;
import fr.proline.studio.rsmexplorer.gui.calc.functions.AbstractFunction;
import fr.proline.studio.rsmexplorer.gui.calc.graph.AbstractConnectedGraphObject;
import fr.proline.studio.rsmexplorer.gui.calc.graph.AbstractGraphObject;
import fr.proline.studio.rsmexplorer.gui.calc.graph.DataGraphNode;
import fr.proline.studio.rsmexplorer.gui.calc.graph.FunctionGraphNode;
import fr.proline.studio.rsmexplorer.gui.calc.graph.GraphConnector;
import fr.proline.studio.rsmexplorer.gui.calc.graph.GraphGroup;
import fr.proline.studio.rsmexplorer.gui.calc.graph.GraphLink;
import fr.proline.studio.rsmexplorer.gui.calc.graph.GraphNode;
import fr.proline.studio.rsmexplorer.gui.calc.graph.GraphicGraphNode;
import fr.proline.studio.rsmexplorer.gui.calc.graphics.AbstractGraphic;
import fr.proline.studio.rsmexplorer.gui.calc.macros.AbstractMacro;
import fr.proline.studio.utils.IconManager;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Panel to display the Graph for the Data analyzer
 *
 * @author JM235353
 */
public class GraphPanel extends JPanel implements MouseListener, MouseMotionListener, ActionListener {

    private static final int DEFAULT_PANEL_WIDTH = 400;
    private static final int DEFAULT_PANEL_HEIGHT = 400;

    private final LinkedList<GraphNode> m_graphNodeArray = new LinkedList<>();

    private final DataAnalyzerPanel m_dataAnalyzerPanel;

    private int m_mouseDragX;
    private int m_mouseDragY;

    private final ArrayList<AbstractConnectedGraphObject> m_selectedObjectsArray = new ArrayList();
    private AbstractConnectedGraphObject m_overObject = null;
    private GraphConnector m_selectedConnector = null;
    private GraphLink m_selectedLink = null;
    private final SelectionGestureSquare m_selectionGesture = new SelectionGestureSquare();
    private boolean m_actionOnRelease = false;

    private boolean m_deselectionIfNoMoveNoPopup = false;
    private boolean m_hasMovedOrPopup = false;
    
    private int m_curMoveCursor = Cursor.DEFAULT_CURSOR;

    private final Dimension m_preferredSize = new Dimension(DEFAULT_PANEL_WIDTH, DEFAULT_PANEL_HEIGHT); // minimum size at the beginning

    private Timer m_timer;

    public GraphPanel(DataAnalyzerPanel dataAnalyzerPanel) {

        setLayout(null);

        addMouseListener(this);
        addMouseMotionListener(this);
        setTransferHandler(new DataTreeTransferHandler(this));

        m_timer = new Timer(20, this);
        m_timer.setInitialDelay(200);
        m_timer.start();

        m_dataAnalyzerPanel = dataAnalyzerPanel;

    }

    public void displayBelow(GraphNode node, boolean newTab, String name, SplittedPanelContainer.PanelLayout layout) {
        m_dataAnalyzerPanel.displayBelow(node, newTab, name, layout);
    }

    @Override
    public Dimension getPreferredSize() {
        return m_preferredSize;
    }

    public LinkedList<GraphNode> getNodes() {
        return m_graphNodeArray;
    }

    public Point getNextGraphNodePosition(GraphNode graphNode) {
        int posX = 0;
        int posY = 120;
        if (!m_graphNodeArray.isEmpty()) {

            GraphNode lastGraphNode = m_graphNodeArray.getLast();
            posX = lastGraphNode.getCenterX();
            posY = lastGraphNode.getCenterY() + GraphNode.HEIGHT * 2;
            int height = getHeight();
            if (height > 60) {
                // could be ==0 when the panel has just been created
                if (posY > height - GraphNode.HEIGHT) {
                    posY = height - GraphNode.HEIGHT;
                }
            }

            if ((lastGraphNode instanceof DataGraphNode) && (graphNode instanceof FunctionGraphNode)) {
                posX += GraphNode.WIDTH * 2.2;
                posY = lastGraphNode.getCenterY();

            }

        }

        return new Point(posX, posY);
    }

    public void addGraphNodes(ArrayList<GraphNode> graphNodes) {
        for (GraphNode node : graphNodes) {
            addGraphNode(node);
        }
    }

    public void addGraphNode(GraphNode graphNode) {
        Point position = getNextGraphNodePosition(graphNode);

        addGraphNode(graphNode, position.x, position.y);
    }

    public void addGraphNode(GraphNode graphNode, int x, int y) {
        graphNode.setCenter(x, y);
        m_graphNodeArray.add(graphNode);

        // increase panel size if needed
        int newPanelWidth = Math.max(x + 140, m_preferredSize.width);
        int newPanelHeight = Math.max(y + 140, m_preferredSize.height);
        if ((newPanelWidth > m_preferredSize.width) || (newPanelHeight > m_preferredSize.height)) {
            m_preferredSize.width = newPanelWidth;
            m_preferredSize.height = newPanelHeight;
            revalidate();
        }

        repaint();
    }

    public void addMacroToGraph(AbstractMacro macro, int x, int y) {
        addMacroToGraph(macro, new Point(x, y));
    }

    public void addMacroToGraph(AbstractMacro macro, Point position) {

        GraphGroup group = new GraphGroup(macro.getName());

        ArrayList<DataTree.DataNode> nodes = macro.getNodes();

        HashMap<DataTree.DataNode, GraphNode> graphNodeMap = new HashMap<>();

        boolean firstNode = true;

        for (DataTree.DataNode node : nodes) {
            DataTree.DataNode.DataNodeType type = node.getType();
            int levelX = macro.getLevelX(node);
            int levelY = macro.getLevelY(node);

            GraphNode graphNode = null;
            switch (type) {
                case VIEW_DATA: {
                    DataTree.ViewDataNode dataNode = ((DataTree.ViewDataNode) node);
                    TableInfo tableInfo = dataNode.getTableInfo();
                    graphNode = new DataGraphNode(tableInfo, this);
                    break;
                }
                case FUNCTION: {
                    DataTree.FunctionNode functionNode = (DataTree.FunctionNode) node;
                    AbstractFunction function = functionNode.getFunction().cloneFunction(this);
                    graphNode = new FunctionGraphNode(function, this);
                    break;
                }
                case GRAPHIC: {
                    DataTree.GraphicNode graphicNode = (DataTree.GraphicNode) node;
                    AbstractGraphic graphic = graphicNode.getGraphic().cloneGraphic(this);
                    graphNode = new GraphicGraphNode(this, graphic);
                    break;
                }
            }
            if ((firstNode) && (position == null)) {
                position = new Point();
                firstNode = false;
                position = getNextGraphNodePosition(graphNode);
                if (position.x < GraphNode.WIDTH * 1.4) {
                    position.x = (int) (GraphNode.WIDTH * 1.4);
                }
                if (position.y < GraphNode.HEIGHT * 1.4) {
                    position.y = (int) (GraphNode.HEIGHT * 1.4);
                }
            }
            graphNodeMap.put(node, graphNode);
            addGraphNode(graphNode, position.x + (int) (levelX * GraphNode.WIDTH * 2.2), position.y + levelY * GraphNode.HEIGHT * 2);

            group.addObject(graphNode);
        }

        for (DataTree.DataNode node : nodes) {

            GraphNode nodeOut = graphNodeMap.get(node);
            ArrayList<DataTree.DataNode> links = macro.getLinks(node);
            if (links != null) {
                for (DataTree.DataNode linkedNode : links) {
                    GraphNode nodeIn = graphNodeMap.get(linkedNode);
                    nodeOut.connectTo(nodeIn);
                }
            }
        }

    }

    public void bringToFront(GraphNode graphObject) {
        m_graphNodeArray.remove(graphObject);
        m_graphNodeArray.add(graphObject);

    }

    public void removeGraphNode(GraphNode graphObject) {
        m_graphNodeArray.remove(graphObject);

        // decrease panel size if needed
        recalculatePanelDimension();

        repaint();
    }

    public void removeAllGraphNodes() {
        while (!m_graphNodeArray.isEmpty()) {
            m_graphNodeArray.removeLast();
        }
        recalculatePanelDimension();
    }

    private void recalculatePanelDimension() {

        if (m_graphNodeArray.isEmpty()) {
            m_preferredSize.width = DEFAULT_PANEL_WIDTH;
            m_preferredSize.height = DEFAULT_PANEL_HEIGHT;
            revalidate();
        } else {
            int maxX = 0;
            int maxY = 0;
            for (GraphNode node : m_graphNodeArray) {
                int x = node.getX();
                int y = node.getY();
                if (x > maxX) {
                    maxX = x;
                }
                if (y > maxY) {
                    maxY = y;
                }
            }

            int newPanelWidth = maxX + 140;
            int newPanelHeight = maxY + 140;

            if ((newPanelWidth != m_preferredSize.width) || (newPanelHeight != m_preferredSize.height)) {
                m_preferredSize.width = newPanelWidth;
                m_preferredSize.height = newPanelHeight;
                revalidate();
            }
        }

    }

    @Override
    public void paint(Graphics g) {

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(Color.white);
        g.fillRect(0, 0, getWidth(), getHeight());

        for (AbstractConnectedGraphObject graphNode : m_graphNodeArray) {
            GraphGroup group = graphNode.getGroup();
            if ((group != null) && (!m_paintedGroups.contains(group))) {
                m_paintedGroups.add(group);
                group.draw(g);
            }
            graphNode.draw(g);
        }
        m_paintedGroups.clear();

        if (m_selectedConnector != null) {
            int x = m_selectedConnector.getXConnection();
            int y = m_selectedConnector.getYConnection();
            g.setColor(Color.black);
            g.drawLine(x, y, m_mouseDragX, m_mouseDragY);
        }

        // paint buttons of graph nodes
        paintComponents(g);

        m_selectionGesture.paint(g);

    }
    private final HashSet<GraphGroup> m_paintedGroups = new HashSet<>();

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    private boolean m_mousePressed = false;
    
    @Override
    public void mousePressed(MouseEvent e) {

        int x = e.getX();
        int y = e.getY();

        boolean noOverObject = true;

        m_mousePressed = true;
        
        Iterator<GraphNode> it = m_graphNodeArray.descendingIterator();
        while (it.hasNext()) {
            GraphNode graphNode = it.next();

            AbstractGraphObject overObject = (AbstractGraphObject) graphNode.inside(x, y);
            if (overObject != null) {
                noOverObject = false;
                AbstractConnectedGraphObject.TypeGraphObject type = overObject.getType();
                switch (type) {

                    case GRAPH_NODE: {
                        
                        int modifiers = e.getModifiers();
                        
                        m_deselectionIfNoMoveNoPopup = false;
                        
                        if (!overObject.isSelected()) {
                            if (((modifiers & KeyEvent.CTRL_MASK) == 0) && ((modifiers & KeyEvent.SHIFT_MASK) == 0)) {
                                clearGraphNodeSelection();
                            }
                            m_selectedObjectsArray.add((AbstractConnectedGraphObject) overObject);
                            overObject.setSelected(true);
                        } else {
                            m_deselectionIfNoMoveNoPopup = true;
                            m_hasMovedOrPopup = false;
                        }
                        
                        
                        
                        
                        if (!m_selectedObjectsArray.contains(overObject)) {
                            m_selectedObjectsArray.add((AbstractConnectedGraphObject) overObject);
                            overObject.setSelected(true);
                        }


                        m_overObject = (AbstractConnectedGraphObject) overObject;

                        bringToFront((GraphNode) overObject);
                        m_mouseDragX = x;
                        m_mouseDragY = y;
                        if (SwingUtilities.isLeftMouseButton(e) || SwingUtilities.isMiddleMouseButton(e)) {
                            // starting moving action
                            setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                        }
                        repaint();
                        break;
                    }
                    case CONNECTOR: {
                        clearGraphNodeSelection();
                        m_selectedConnector = (GraphConnector) overObject;
                        m_mouseDragX = x;
                        m_mouseDragY = y;
                        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                        repaint();
                        break;
                    }
                    case LINK: {
                        clearGraphNodeSelection();
                        m_selectedLink = (GraphLink) overObject;
                        m_selectedLink.setSelected(true);
                        m_mouseDragX = x;
                        m_mouseDragY = y;
                        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                        repaint();
                        break;
                    }
                    case GRAPH_NODE_ACTION: {
                        m_overObject = graphNode;
                        m_actionOnRelease = true;
                        break;
                    }
                    default: {
                        clearGraphNodeSelection();
                        break;
                    }
                }
                break;
            }
        }

        if (noOverObject) {
            clearGraphNodeSelection();
            m_selectionGesture.startSelection(x, y);
        }
    }

    private void clearGraphNodeSelection() {
        int nbObjectSelected = m_selectedObjectsArray.size();
        for (int i = 0; i < nbObjectSelected; i++) {
            m_selectedObjectsArray.get(i).setSelected(false);
        }
        m_selectedObjectsArray.clear();
    }

    @Override
    public void mouseReleased(MouseEvent e) {

        boolean mousePressed = m_mousePressed;
        m_mousePressed = false;   
        
        if (e.isPopupTrigger()) {

            // JPM.HACK java bug, right click -> popup, right click again -> popup but mousePressed has not been called beforehand
            if (!mousePressed) {
                mousePressed(e);
            }
  
            m_hasMovedOrPopup = true;
            
            if (!m_selectedObjectsArray.isEmpty()) {
                if (m_selectedObjectsArray.size() == 1) {
                    JPopupMenu popup = m_selectedObjectsArray.get(0).createPopup(this);
                    if (popup != null) {
                        popup.show((JComponent) e.getSource(), e.getX(), e.getY());
                    }
                } else {
                    // popup for multiple object with delete menu
                    JPopupMenu popup = createNodePopup();
                    popup.show((JComponent) e.getSource(), e.getX(), e.getY());
                }
                m_overObject = null;
            } else if (m_selectedConnector != null) {
                JPopupMenu popup = m_selectedConnector.createPopup(this);
                if (popup != null) {
                    popup.show((JComponent) e.getSource(), e.getX(), e.getY());
                }
                m_selectedConnector = null;
            } else if (m_selectedLink != null) {
                JPopupMenu popup = m_selectedLink.createPopup(this);
                if (popup != null) {
                    popup.show((JComponent) e.getSource(), e.getX(), e.getY());
                }
                m_selectedLink = null;
            } else if (m_selectedObjectsArray.isEmpty() && m_selectedConnector == null && m_selectedLink == null) {
                JPopupMenu popup = createPanelPopup();
                popup.show((JComponent) e.getSource(), e.getX(), e.getY());
            }

        } else {

            if (m_selectedConnector != null) {
                int x = e.getX();
                int y = e.getY();
                AbstractGraphObject overObject = null;
                Iterator<GraphNode> it = m_graphNodeArray.descendingIterator();
                while (it.hasNext()) {
                    AbstractConnectedGraphObject graphNode = it.next();

                    overObject = graphNode.inside(x, y);
                    if (overObject != null) {
                        if (overObject.getType() == AbstractConnectedGraphObject.TypeGraphObject.CONNECTOR) {
                            GraphConnector connector = (GraphConnector) overObject;
                            if (m_selectedConnector.canBeLinked(connector)) {
                                m_selectedConnector.addConnection(connector);
                                connector.addConnection(m_selectedConnector);
                            }
                        }
                        break;
                    }
                }

                m_selectedConnector = null;
                setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                repaint();
            } else if (m_selectedLink != null) {
                m_selectedLink.setSelected(false);
                m_selectedLink = null;
                setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                repaint();
            } else if (m_selectionGesture.isSelecting()) {
                m_selectionGesture.stopSelection(e.getX(), e.getY());
                int action = m_selectionGesture.getAction();
                if (action == SelectionGestureLasso.ACTION_SURROUND) {

                    Path2D.Double selectionShape = m_selectionGesture.getSelectionPath();

                    int modifier = e.getModifiers();
                    boolean isCtrlOrShiftDown = ((modifier & (InputEvent.SHIFT_MASK | InputEvent.CTRL_MASK)) != 0);
                    if (!isCtrlOrShiftDown) {
                        clearGraphNodeSelection();
                    }

                    for (GraphNode node : m_graphNodeArray) {
                        if (node.isSelected()) {
                            continue;
                        }

                        int x1 = node.getX();
                        int y1 = node.getY();
                        int x2 = node.getXEnd();
                        int y2 = node.getYEnd();

                        if (selectionShape.contains(x1, y1, x2 - x1, y2 - y1)) {
                            m_selectedObjectsArray.add(node);
                            node.setSelected(true);
                        }
                    }

                } else if ((m_overObject == null) && (!m_selectedObjectsArray.isEmpty())) {

                    clearGraphNodeSelection();
                    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

                }
                repaint();
            } else if ((m_overObject != null) && (m_actionOnRelease)) {
                ((GraphNode) m_overObject).doAction(e.getX(), e.getY());
                repaint();
                ((GraphNode) m_overObject).hideAction();
            } else if (m_overObject != null) {
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                
                if ((m_deselectionIfNoMoveNoPopup) && (!m_hasMovedOrPopup)) {
                    clearGraphNodeSelection();
                    m_selectedObjectsArray.add((AbstractConnectedGraphObject) m_overObject);
                    m_overObject.setSelected(true);
                }

            } else {
                clearGraphNodeSelection();
            }
        }

        m_overObject = null;
        m_actionOnRelease = false;
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {

        if (e.isPopupTrigger()) {
            return;
        }
        if (m_actionOnRelease) {

            // check if mouse is dragged outside the action zone
            //boolean noOverObject = true;
            boolean noOverActionObject = true;
            Iterator<GraphNode> it = m_graphNodeArray.descendingIterator();
            while (it.hasNext()) {
                GraphNode graphNode = it.next();

                int x = e.getX();
                int y = e.getY();
                AbstractGraphObject overObject = (AbstractGraphObject) graphNode.inside(x, y);
                if (overObject != null) {
                    //noOverObject = false;
                    AbstractConnectedGraphObject.TypeGraphObject type = overObject.getType();
                    if (type == AbstractConnectedGraphObject.TypeGraphObject.GRAPH_NODE_ACTION) {
                        noOverActionObject = false;
                    }
                }
            }
            if (noOverActionObject) {
                /// mouse has been dragged outside the action zone
                m_overObject = null;
                m_actionOnRelease = false;
                setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                repaint();
            }

            return;
        }

        int x = e.getX();
        int y = e.getY();

        if (m_overObject != null) {

            m_hasMovedOrPopup = true;
            
            int nbObjectSelected = m_selectedObjectsArray.size();
            
            
            // correct the mouse drag, so the object can not go to negative values
            int deltaX = x - m_mouseDragX;
            int deltaY = y - m_mouseDragY;
            for (int i = 0; i < nbObjectSelected; i++) {
                deltaX = m_selectedObjectsArray.get(i).correctMoveX(deltaX);
                deltaY = m_selectedObjectsArray.get(i).correctMoveY(deltaY);
            }
            
            // move objects
            for (int i = 0; i < nbObjectSelected; i++) {
                m_selectedObjectsArray.get(i).move(deltaX, deltaY);
            }
            m_mouseDragX = x;
            m_mouseDragY = y;
            recalculatePanelDimension();
            repaint();
        } else if (m_selectedConnector != null) {
            m_mouseDragX = x;
            m_mouseDragY = y;
            repaint();
        } else if (m_selectionGesture.isSelecting()) {
            m_selectionGesture.continueSelection(x, y);
            repaint();
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {

        boolean needRepaint = false;

        int x = e.getX();
        int y = e.getY();
        int newCursor = Cursor.DEFAULT_CURSOR;
        Iterator<GraphNode> it = m_graphNodeArray.descendingIterator();
        while (it.hasNext()) {
            GraphNode graphNode = it.next();
            needRepaint |= graphNode.hideAction();

            if (graphNode.inside(x, y) != null) {
                newCursor = Cursor.HAND_CURSOR;
            }
        }
        if (newCursor != m_curMoveCursor) {
            m_curMoveCursor = newCursor;
            setCursor(Cursor.getPredefinedCursor(newCursor));
        }

        if (needRepaint) {
            repaint();
        }
    }

    public void deleteAllSelected() {
        int nbObjectSelected = m_selectedObjectsArray.size();
        for (int i = 0; i < nbObjectSelected; i++) {
            m_selectedObjectsArray.get(i).deleteAction();
        }
        m_selectedObjectsArray.clear();

    }

    public Point getViewportProperPoint() {
        int x = 0;
        int y = 0;

        if (!m_graphNodeArray.isEmpty()) {
            x = m_graphNodeArray.get(0).getXEnd();
            y = m_graphNodeArray.get(0).getYEnd();
        }

        for (int i = 0; i < m_graphNodeArray.size(); i++) {
            if (m_graphNodeArray.get(i).getXEnd() > x) {
                x = m_graphNodeArray.get(i).getXEnd();
            } else if (m_graphNodeArray.get(i).getYEnd() > y) {
                y = m_graphNodeArray.get(i).getYEnd();
            }
        }

        return new Point(x, y);
    }

    private JPopupMenu createNodePopup() {
        JPopupMenu popup = new JPopupMenu();
        popup.add(new DeleteAction());
        return popup;
    }

    private JPopupMenu createPanelPopup() {
        JPopupMenu popup = new JPopupMenu();
        popup.add(new SelectAllAction());
        popup.add(new ClearAllAction());

        return popup;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        repaint();
    }

    public class DeleteAction extends AbstractAction {

        public DeleteAction() {
            super("Delete");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            deleteAllSelected();
        }
    }

    public class ClearAllAction extends AbstractAction {

        public ClearAllAction() {
            super("Clear All", IconManager.getIcon(IconManager.IconType.CLEAR_ALL));
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            int reply = JOptionPane.showConfirmDialog(null, "Are you sure you want to clear the graph?", "Warning", JOptionPane.YES_NO_OPTION);
            if (reply == JOptionPane.YES_OPTION) {
                while (m_graphNodeArray.size() > 0) {
                    m_graphNodeArray.get(m_graphNodeArray.size() - 1).deleteAction();
                }
            }
        }

    }

    public class SelectAllAction extends AbstractAction {

        public SelectAllAction() {
            super("Select All", IconManager.getIcon(IconManager.IconType.SELECT_ALL));
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            for (int i = 0; i < m_graphNodeArray.size(); i++) {
                m_selectedObjectsArray.add(m_graphNodeArray.get(i));
            }
            for (int i = 0; i < m_selectedObjectsArray.size(); i++) {
                m_selectedObjectsArray.get(i).setSelected(true);
            }
        }

    }

}
