package fr.proline.studio.rsmexplorer.gui.calc.graph;

import static fr.proline.studio.rsmexplorer.gui.calc.graph.AbstractGraphObject.STROKE_SELECTED;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.util.LinkedList;

/**
 *
 * @author JM235353
 */
public class GraphConnector extends AbstractGraphObject {

    private static final int WIDTH = 10;
    private static final int HEIGHT = 10;
    private static final int LINE_LENGTH = 3;
    
    private int m_x = 0;
    private int m_y = 0;
    
    private final boolean m_out;
    
    private final GraphNode m_graphNode;
    
    private final LinkedList<GraphConnector> m_connections = new LinkedList(); 
    
    public GraphConnector(GraphNode graphNode, boolean out) {
        super(TypeGraphObject.CONNECTOR);
        m_out = out;
        m_graphNode = graphNode;
    }
    
    public boolean canBeLinked(GraphConnector connector) {
        return (m_out ^ connector.m_out) && (m_graphNode != connector.m_graphNode);
    }
    
    public void addConnection(GraphConnector connector) {
        if ((!m_out) && (!m_connections.isEmpty())) {
            // for in connector, only one connection is accepted
            GraphConnector previousConnector = m_connections.getFirst();
            previousConnector.removeConnection(this);
            m_connections.clear();
        }
        m_connections.add(connector);
    }
    
    public void removeConnection(GraphConnector connector) {
        m_connections.remove(connector);
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
        
        // draw connection for out
        if (m_out) {
            for (GraphConnector connector : m_connections) {
                int x1 = getXConnection();
                int y1 = getYConnection();
                
                int x2 = connector.getXConnection();
                int y2 = connector.getYConnection();

                g.drawLine(x1, y1, x2, y2);
            }
        }
        
        
        
        
        
        
    }

    @Override
    public AbstractGraphObject inside(int x, int y) {
        // triangle is taken as a square
        final int DELTA = 3;
        if ((x+DELTA>=m_x) && (y+DELTA>=m_y) && (x-DELTA<=m_x+WIDTH) && (y-DELTA<=m_y+HEIGHT)) {
            return this;
        }
        return null;
    }

    @Override
    public void move(int dx, int dy) {
        m_x += dx;
        m_y += dy;
    }
    
}
