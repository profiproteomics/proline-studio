package fr.proline.studio.rsmexplorer.gui.calc.graph;

import fr.proline.studio.rsmexplorer.gui.calc.GraphPanel;
import static fr.proline.studio.rsmexplorer.gui.calc.graph.AbstractConnectedGraphObject.STROKE_SELECTED;
import fr.proline.studio.extendedtablemodel.GlobalTableModelInterface;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.util.LinkedList;
import javax.swing.JPopupMenu;

/**
 * In or Out Connector of a Graph Node 
 * @author JM235353
 */
public class GraphConnector extends AbstractConnectedGraphObject {

    private static final int WIDTH = 10;
    public static final int HEIGHT = 10;
    private static final int LINE_LENGTH = 3;
    
    private int m_x = 0;
    private int m_y = 0;
    
    private final boolean m_out;
    
    // when there are several in or out connectors, the index correspond to the number of the connector.
    private int m_index = 0;
    
    private final GraphNode m_graphNode;
    private GraphLink m_link = null;
    
    protected GraphPanel m_graphPanel = null;
    
    private final LinkedList<GraphConnector> m_connections = new LinkedList(); 
    
    public GraphConnector(GraphNode graphNode, boolean out, int index, GraphPanel panel) {
        super(TypeGraphObject.CONNECTOR);
        m_out = out;
        m_graphNode = graphNode;
        m_graphPanel = panel;
        m_index = index;
    }
    
    public boolean isOutConnector() {
        return m_out;
    }
    
    public int getIndex() {
        return m_index;
    }
    
    public void getOutLinkedGraphNodes(LinkedList<GraphNode> outLinkedGraphNodes) {
        if (!m_out) {
            return;
        }

        for (GraphConnector inConnector : m_connections) {
            outLinkedGraphNodes.add(inConnector.getGraphNode());
        }

    }
    
    @Override
    public String getFullName() {
        return m_graphNode.getFullName(m_index);
    }

    @Override
    public String getDataName() {
        GraphConnector connector = getLinkedSourceGraphConnector();
        if (connector == null) {
            return null;
        }
        GraphNode node = connector.getGraphNode();
        if (node == null) {
            return null;
        }
        return node.getDataName();
    }

    @Override
    public String getTypeName() {
        return null;
    }
    
    
    @Override
    public boolean isConnected(boolean recursive) {
        if (m_connections.isEmpty()) {
            return false;
        } else if (!recursive) {
            return true;
        }
        GraphNode node = getLinkedSourceGraphConnector().getGraphNode();
        return (node.isConnected(recursive));
    }
    
    @Override
    public boolean canSetSettings() {
        return false;
    }
    
    @Override
    public boolean settingsDone() {
        return true;
    }
    @Override
    public boolean calculationDone() {
        if (!isConnected(true)) {
            return false;
        }
        GraphNode node = getLinkedSourceGraphConnector().getGraphNode();
        return (node.calculationDone());
    }

    
    public GraphNode getGraphNode() {
        return m_graphNode;
    }
    
    public GraphConnector getLinkedSourceGraphConnector() {
        if ((m_out) || (m_connections.isEmpty())) {
            return null;
        }
        GraphConnector connector = m_connections.getFirst();
        return connector;
    }
    
    public boolean canBeLinked(GraphConnector connector) {
        return (m_out ^ connector.m_out) && (m_graphNode != connector.m_graphNode);
    }
    
    public void addConnection(GraphConnector connector) {
        if ((!m_out) && (!m_connections.isEmpty())) {
            // for in connector, only one connection is accepted
            //GraphConnector previousConnector = m_connections.getFirst();
            //previousConnector.removeConnection(this);
            deleteInLink();
        }
        m_connections.add(connector);
    }
    
    public void removeConnection(GraphConnector connector) {
        m_connections.remove(connector);
        if (m_connections.isEmpty()) {
            m_link = null; 
        }
    }
    
    public int getXConnection() {
        if (m_out) {
            return m_x+WIDTH+LINE_LENGTH;
        } else {
            return m_x-LINE_LENGTH;
        }
    }
    
    public int getYConnection() {
        return m_y + HEIGHT / 2;
    }

    public void setPosition(int xLeft, int yMiddle) {
        m_x = xLeft+LINE_LENGTH;
        m_y = yMiddle-HEIGHT/2;
    }
    public void setRightPosition(int xRight, int yMiddle) {
        m_x = xRight-LINE_LENGTH-WIDTH;
        m_y = yMiddle-HEIGHT/2;
    }
    
    @Override
    public void draw(Graphics g) {
        
        g.setColor(Color.black);
        
        Graphics2D g2 = (Graphics2D) g;
        Stroke previousStroke = g2.getStroke();
        BasicStroke stroke = m_selected ? STROKE_SELECTED : STROKE_NOT_SELECTED;
        g2.setStroke(stroke);
        
        // triangle
        g2.drawLine(m_x, m_y, m_x+WIDTH, m_y+HEIGHT/2);
        g2.drawLine(m_x, m_y+HEIGHT, m_x+WIDTH, m_y+HEIGHT/2);
        g2.drawLine(m_x, m_y, m_x, m_y+HEIGHT);
        
        // previous line
        g2.drawLine(m_x-LINE_LENGTH,m_y+HEIGHT/2,m_x,m_y+HEIGHT/2);
        
        // after line
        g2.drawLine(m_x+WIDTH+LINE_LENGTH, m_y+HEIGHT/2, m_x+WIDTH, m_y+HEIGHT/2);
        
        g2.setStroke(previousStroke);
        
        // draw connection for in
        if ((!m_out) && (!m_connections.isEmpty())) {
            GraphConnector connector = m_connections.getFirst();
            if (m_link == null) {
                m_link = new GraphLink(this, m_graphPanel);
            }
            m_link.setLink(getXConnection(), getYConnection(), connector.getXConnection(), connector.getYConnection());

            m_link.draw(g);
        }
        
 
    }


     
     
     
    
    @Override
    public AbstractGraphObject inside(int x, int y) {
        // triangle is taken as a square
        final int DELTA = 3;
        if ((x+DELTA>=m_x) && (y+DELTA>=m_y) && (x-DELTA<=m_x+WIDTH) && (y-DELTA<=m_y+HEIGHT)) {
            return this;
        }
        
        if (m_link != null) {
            return (m_link.inside(x,y));
        }
        
        return null;
    }

    @Override
    public void move(int dx, int dy) {
        m_x += dx;
        m_y += dy;
    }
    
    @Override
    public void delete() {

        for (GraphConnector connector : m_connections) {
            connector.removeConnection(this);
        }
        m_connections.clear();
        m_link = null;
    }
    
    public void deleteInLink() {
        // when called, we have an in-connecter
        for (GraphConnector connector : m_connections) {
            connector.removeConnection(this);
        }
        m_connections.clear();
        m_link = null;

        if (!m_out) {
            
            m_graphNode.updateNumberOfInConnections();
            m_graphNode.propagateSourceChanged();
        }
    }
    
    public void propagateSourceChanged() {
        if (m_out) {
            for (GraphConnector connector : m_connections) {
                connector.propagateSourceChanged();
            }
        } else {
            m_graphNode.propagateSourceChanged();
        }
    }
    
    @Override
    public JPopupMenu createPopup(final GraphPanel panel) {
        return null;
    }

    public GlobalTableModelInterface getGlobalTableModelInterface() {
        return getGlobalTableModelInterface(m_index);
    }
    
    @Override
    public GlobalTableModelInterface getGlobalTableModelInterface(int index) {
        return m_graphNode.getGlobalTableModelInterface(index);
    }


    @Override
    public String getPreviousDataName() {
        return null;
    }
    
    @Override
    public void deleteAction() {
        // never deleted directly by an action
    }
    
    public String getTooltip(int x, int y) {
        if (m_graphNode == null) {
            return null;
        }
        if (m_out) {
            return m_graphNode.getOutTooltip(m_index);
        }
        return null;
    }
    
}
