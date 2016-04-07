package fr.proline.studio.rsmexplorer.gui.calc;

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
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

/**
 * Panel to display the Graph for the Data analyzer
 * @author JM235353
 */
public class GraphPanel extends JPanel implements MouseListener, MouseMotionListener {
    
    private static final int DEFAULT_PANEL_WIDTH = 400;
    private static final int DEFAULT_PANEL_HEIGHT = 400;
    
    private final LinkedList<GraphNode> m_graphNodeArray = new LinkedList<>();
    
    private final DataAnalyzerPanel m_dataAnalyzerPanel;
    
    private int m_mouseDragX;
    private int m_mouseDragY;
    private AbstractGraphObject m_selectedObject = null;
    private GraphConnector m_selectedConnector = null;
    private GraphLink m_selectedLink = null;
    
    private int m_curMoveCursor = Cursor.DEFAULT_CURSOR;

    private final Dimension m_preferredSize = new Dimension(DEFAULT_PANEL_WIDTH, DEFAULT_PANEL_HEIGHT); // minimum size at the beginning
    
    public GraphPanel(DataAnalyzerPanel dataAnalyzerPanel) {
        addMouseListener(this);
        addMouseMotionListener(this);
        setTransferHandler(new DataTreeTransferHandler(this));
        
        m_dataAnalyzerPanel = dataAnalyzerPanel;
    }
    
    public void displayBelow(GraphNode node, boolean newTab, String name) {
        m_dataAnalyzerPanel.displayBelow(node, newTab, name);
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
                if (position.x<GraphNode.WIDTH*1.4) {
                    position.x = (int) (GraphNode.WIDTH*1.4);
                }
                if (position.y<GraphNode.HEIGHT*1.4) {
                    position.y = (int) (GraphNode.HEIGHT*1.4);
                }
            }
            graphNodeMap.put(node, graphNode);
            addGraphNode(graphNode, position.x+(int) (levelX*GraphNode.WIDTH* 2.2), position.y+levelY*GraphNode.HEIGHT*2);
            
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
    
    public void removeGraphNode(AbstractConnectedGraphObject graphObject) {
        m_graphNodeArray.remove(graphObject);
        
        
        // decrease panel size if needed
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

            if ((newPanelWidth < m_preferredSize.width) || (newPanelHeight < m_preferredSize.height)) {
                m_preferredSize.width = newPanelWidth;
                m_preferredSize.height = newPanelHeight;
                revalidate();
            }
        }
        
        repaint();
    }
    
    @Override
    public void paint(Graphics g) {

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g.setColor(Color.white);
        g.fillRect(0, 0, getWidth(), getHeight());
        
        for (AbstractConnectedGraphObject graphNode : m_graphNodeArray) {
            GraphGroup group = graphNode.getGroup();
            if ((group!=null) && (!m_paintedGroups.contains(group))) {
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

    }
    private final HashSet<GraphGroup> m_paintedGroups = new HashSet<>();

    @Override
    public void mouseClicked(MouseEvent e) {
        
    }

    @Override
    public void mousePressed(MouseEvent e) {
        

        int x = e.getX();
        int y = e.getY();

        Iterator<GraphNode> it = m_graphNodeArray.descendingIterator();
        while (it.hasNext()) {
            AbstractConnectedGraphObject graphNode = it.next();
            
            AbstractGraphObject overObject = graphNode.inside(x, y);
            if (overObject != null) {
                AbstractConnectedGraphObject.TypeGraphObject type = overObject.getType();
                switch (type) {

                    case GRAPH_NODE: {
                        m_selectedObject = overObject;
                        m_selectedObject.setSelected(true);
                        bringToFront((GraphNode)overObject);
                        m_mouseDragX = x;
                        m_mouseDragY = y;
                        if (e.getButton() == MouseEvent.BUTTON1) {
                            // starting moving action
                            setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                        }
                        repaint();
                        break;
                    }
                    case CONNECTOR: {
                        m_selectedConnector = (GraphConnector) overObject;
                        m_mouseDragX = x;
                        m_mouseDragY = y;
                        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                        repaint();
                        break;
                    }
                    case LINK: {
                        m_selectedLink = (GraphLink) overObject;
                        m_selectedLink.setSelected(true);
                        m_mouseDragX = x;
                        m_mouseDragY = y;
                        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                        repaint();
                        break;
                    }
                }
                break;
            }

        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        
        if (e.isPopupTrigger()) {

            if (m_selectedObject != null) {
                JPopupMenu popup = m_selectedObject.createPopup(this);
                if (popup !=null) {
                    popup.show((JComponent) e.getSource(), e.getX(), e.getY());
                }
                 m_selectedObject = null;
            } else if (m_selectedConnector != null) {
                JPopupMenu popup = m_selectedConnector.createPopup(this);
                if (popup !=null) {
                    popup.show((JComponent) e.getSource(), e.getX(), e.getY());
                }
                m_selectedConnector = null;
            } else if (m_selectedLink != null) {
                JPopupMenu popup = m_selectedLink.createPopup(this);
                if (popup !=null) {
                    popup.show((JComponent) e.getSource(), e.getX(), e.getY());
                }
                m_selectedLink = null;
            }
            
        } else {

            if (m_selectedObject != null) {
                m_selectedObject.setSelected(false);
                m_selectedObject = null;
                setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                repaint();
            } else if (m_selectedConnector != null) {
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
            }
        }
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
        
        if (m_selectedObject != null) {
            int x = e.getX();
            int y = e.getY();
            m_selectedObject.move(x-m_mouseDragX, y-m_mouseDragY);
            m_mouseDragX = x;
            m_mouseDragY = y;
            repaint();
        } else if (m_selectedConnector != null) {
            m_mouseDragX = e.getX();
            m_mouseDragY = e.getY();
            repaint();
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        
        int x = e.getX();
        int y = e.getY();
        int newCursor = Cursor.DEFAULT_CURSOR;
        Iterator<GraphNode> it = m_graphNodeArray.descendingIterator();
        while (it.hasNext()) {
            AbstractConnectedGraphObject graphNode = it.next();
            if (graphNode.inside(x, y) != null) {
                newCursor = Cursor.HAND_CURSOR;
            }
        }
        if (newCursor != m_curMoveCursor) {
            m_curMoveCursor = newCursor;
            setCursor(Cursor.getPredefinedCursor(newCursor));
        }
    }
    
}
