package fr.proline.studio.rsmexplorer.gui.calc;

import fr.proline.studio.rsmexplorer.gui.calc.graph.AbstractGraphObject;
import fr.proline.studio.rsmexplorer.gui.calc.graph.GraphConnector;
import fr.proline.studio.rsmexplorer.gui.calc.graph.GraphNode;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Iterator;
import java.util.LinkedList;
import javax.swing.JPanel;

/**
 *
 * @author JM235353
 */
public class GraphPanel extends JPanel implements MouseListener, MouseMotionListener {
    
    private final LinkedList<AbstractGraphObject> m_graphNodeArray = new LinkedList<>();
    
    private int m_mouseDragX;
    private int m_mouseDragY;
    private AbstractGraphObject m_selectedObject = null;
    private GraphConnector m_selectedConnector = null;
    
    private int m_curMoveCursor = Cursor.DEFAULT_CURSOR;
    
    public GraphPanel() {
        addMouseListener(this);
        addMouseMotionListener(this);
    }
    
    public void addGraphNode(GraphNode graphNode) {
       int width = getWidth();
       int height = getHeight();
       addGraphNode(graphNode, width/2, height/2);
    }
    public void addGraphNode(GraphNode graphNode, int x, int y) {
        graphNode.setCenter(x, y);
        m_graphNodeArray.add(graphNode);
        repaint();
    }
    
    public void bringToFront(AbstractGraphObject graphObject) {
        m_graphNodeArray.remove(graphObject);
        m_graphNodeArray.add(graphObject);
    }
    
    @Override
    public void paint(Graphics g) {

        g.setColor(Color.white);
        g.fillRect(0, 0, getWidth(), getHeight());
        
        for (AbstractGraphObject graphNode : m_graphNodeArray) {
            graphNode.draw(g);
        }

        if (m_selectedConnector != null) {
            int x = m_selectedConnector.getXConnection();
            int y = m_selectedConnector.getYConnection();
            g.setColor(Color.black);
            g.drawLine(x, y, m_mouseDragX, m_mouseDragY);
        }

    }

    @Override
    public void mouseClicked(MouseEvent e) {
        
    }

    @Override
    public void mousePressed(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();

        Iterator<AbstractGraphObject> it = m_graphNodeArray.descendingIterator();
        while (it.hasNext()) {
            AbstractGraphObject graphNode = it.next();
            
            AbstractGraphObject overObject = graphNode.inside(x, y);
            if (overObject != null) {
                AbstractGraphObject.TypeGraphObject type = overObject.getType();
                switch (type) {

                    case GRAPH_NODE: {
                        m_selectedObject = overObject;
                        m_selectedObject.setSelected(true);
                        bringToFront(overObject);
                        m_mouseDragX = x;
                        m_mouseDragY = y;
                        setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                        repaint();
                        break;
                    }
                    case CONNECTOR: {
                        m_selectedConnector = (GraphConnector) overObject;
                        bringToFront(graphNode);
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
        if (m_selectedObject != null) {
            m_selectedObject.setSelected(false);
            m_selectedObject = null;
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            repaint();
        } else if (m_selectedConnector != null) {
            int x = e.getX();
            int y = e.getY();
            AbstractGraphObject overObject = null;
            Iterator<AbstractGraphObject> it = m_graphNodeArray.descendingIterator();
            while (it.hasNext()) {
                AbstractGraphObject graphNode = it.next();

                overObject = graphNode.inside(x, y);
                if (overObject != null) {
                    if (overObject.getType() == AbstractGraphObject.TypeGraphObject.CONNECTOR) {
                        GraphConnector connector = (GraphConnector) overObject;
                        m_selectedConnector.addConnection(connector);
                        connector.addConnection(m_selectedConnector);
                    }
                    break;
                }
            }
            
            
            //JPM.TODO
            m_selectedConnector = null;
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            repaint();
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
        Iterator<AbstractGraphObject> it = m_graphNodeArray.descendingIterator();
        while (it.hasNext()) {
            AbstractGraphObject graphNode = it.next();
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
