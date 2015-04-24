package fr.proline.studio.rsmexplorer.gui.calc.graph;

import fr.proline.studio.rsmexplorer.gui.calc.GraphPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.geom.GeneralPath;
import javax.swing.AbstractAction;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

/**
 *
 * @author JM235353
 */
public class GraphLink extends AbstractGraphObject {
    
    private GeneralPath m_path = null;
    private GraphConnector m_connector = null;
    
    
    private int m_x1;
    private int m_x2;
    private int m_y1;
    private int m_y2;
    
    public GraphLink(GraphConnector connector) {
        super(TypeGraphObject.LINK);
        m_connector = connector;
    }
    
    public void setLink(int x1, int y1, int x2, int y2) {

        
        if ((m_path!=null) && (x1 == m_x1) && (x2 == m_x2) && (y1 == m_y1) && (y2 == m_y2)) {
            return;
        }
        if (m_path == null) {
            m_path = new GeneralPath();
        } else {
            m_path.reset();
        }
        
        m_x1 = x1;
        m_y1 = y1;
        m_x2 = x2;
        m_y2 = y2;
        
        
        double xdelta = (x2 - x1) / 3;
        double ydelta = (y2 - y1) / 2;

        m_path.moveTo(x1, y1);
        m_path.lineTo(x1 + xdelta, y1);
        m_path.quadTo(x1 + xdelta * 1.5, y1, x1 + xdelta * 1.5, y1 + ydelta);
        m_path.quadTo(x1 + xdelta * 1.5, y2, x1 + xdelta * 2, y2);
        m_path.lineTo(x2, y2);
        
        
    }
    
    
    private static void getBezierPoint(Point.Double p, Point.Double p1, Point.Double p2, Point.Double p3, double t) {
        p.x = p1.getX() * (1 - t) * (1 - t) + p2.getX() * 2 * t * (1 - t) + p3.getX() * t * t;
        p.y = p1.getY() * (1 - t) * (1 - t) + p2.getY() * 2 * t * (1 - t) + p3.getY() * t * t; 
    }
    
    /**
     * return the squared distance between two points
     *
     * @param p1,p2 the two points
     * @return dist the distance
     */
    private static double squaredDistance(Point.Double p1, Point.Double p2) {
        double d2 = (p2.x - p1.x) * (p2.x - p1.x) + (p2.y - p1.y) * (p2.y - p1.y);
        return d2;
    }

    /**
     * Return the squared distance from a point to a segment
     *     
     * @param ps,pe the start/end of the segment
     * @param p the given point
     * @return the distance from the given point to the segment
     */
    private static double squaredDistanceToSegment(Point.Double ps, Point.Double pe, Point.Double p) {

        if (ps.x == pe.x && ps.y == pe.y) {
            return squaredDistance(ps, p);
        }

        double sx = pe.x - ps.x;
        double sy = pe.y - ps.y;

        double ux = p.x - ps.x;
        double uy = p.y - ps.y;

        double dp = sx * ux + sy * uy;
        if (dp < 0) {
            return squaredDistance(ps, p);
        }

        double sn2 = sx * sx + sy * sy;
        if (dp > sn2) {
            return squaredDistance(pe, p);
        }

        double ah2 = dp * dp / sn2;
        double un2 = ux * ux + uy * uy;
        return un2 - ah2;
    }

    @Override
    public void draw(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(Color.black);
        g2.draw(m_path);
    }

    @Override
    public AbstractGraphObject inside(int x, int y) {
        
        m_p.setLocation(x, y);
        
        double xdelta = (m_x2 - m_x1) / 3;
        double ydelta = (m_y2 - m_y1) / 2;

                
        // distance to first segment
        m_p1.setLocation(m_x1, m_y1);
        m_p2.setLocation(m_x1 + xdelta, m_y1);

        double distSquared = squaredDistanceToSegment(m_p1, m_p2, m_p);
        if (distSquared<DISTMAX_SQUARED) {
            return this;
        }
        
        // distance to second segment
        m_p1.setLocation(m_x1 + xdelta * 2, m_y2);
        m_p2.setLocation(m_x2, m_y2);

        distSquared = squaredDistanceToSegment(m_p1, m_p2, m_p);
        if (distSquared<DISTMAX_SQUARED) {
            return this;
        }
        
        // distance with first bezier curve
        m_p1.setLocation(m_x1 + xdelta, m_y1);
        m_p2.setLocation(m_x1 + xdelta * 1.5, m_y1);
        m_p3.setLocation(m_x1 + xdelta * 1.5, m_y1 + ydelta);
        double t = 0;
        double deltaStep = 100;
        deltaStep = Math.max(Math.abs(m_p1.getX()-m_p3.getX()), deltaStep);
        deltaStep = Math.max(Math.abs(m_p1.getY()-m_p3.getY()), deltaStep);

        double deltaT = 1d/deltaStep;
        while (t<1) {
            getBezierPoint(m_pBezier, m_p1, m_p2, m_p3, t);
            distSquared = squaredDistance(m_p, m_pBezier);
            if (distSquared < DISTMAX_SQUARED) {
                return this;
            }
            t+=deltaT;
        }
        
        // distance with second bezier curve
        m_p1.setLocation(m_x1 + xdelta * 1.5, m_y1 + ydelta);
        m_p2.setLocation(m_x1 + xdelta * 1.5, m_y2);
        m_p3.setLocation(m_x1 + xdelta * 2, m_y2);
        t = 0;
        deltaStep = 100;
        deltaStep = Math.max(Math.abs(m_p1.getX()-m_p3.getX()), deltaStep);
        deltaStep = Math.max(Math.abs(m_p1.getY()-m_p3.getY()), deltaStep);

        deltaT = 1d / deltaStep;
        while (t < 1) {
            getBezierPoint(m_pBezier, m_p1, m_p2, m_p3, t);
            distSquared = squaredDistance(m_p, m_pBezier);
            if (distSquared < DISTMAX_SQUARED) {
                return this;
            }
            t += deltaT;
        }

        /*m_path.moveTo();
        m_path.lineTo(m_x1 + xdelta, m_y1);
        m_path.quadTo(m_x1 + xdelta * 1.5, m_y1, m_x1 + xdelta * 1.5, m_y1 + ydelta);
        m_path.quadTo(m_x1 + xdelta * 1.5, m_y2, m_x1 + xdelta * 2, m_y2);
        m_path.lineTo(m_x2, m_y2);*/
        
        return null;
 
    }
    private static final int DISTMAX_SQUARED = 16;
    private static final Point.Double m_p1 = new Point.Double();
    private static final Point.Double m_p2 = new Point.Double();
    private static final Point.Double m_p3 = new Point.Double();
    private static final Point.Double m_p = new Point.Double();
    private static final Point.Double m_pBezier = new Point.Double();

    
    

    @Override
    public void move(int dx, int dy) {
        // nothing to do
    }

    @Override
    public void delete() {
        // nothing to do
    }


    @Override
    public JPopupMenu createPopup(final GraphPanel panel) {
        JPopupMenu popup = new JPopupMenu();
        popup.add(new DeleteAction(panel, this));
        popup.addPopupMenuListener(new PopupMenuListener() {

            @Override
            public void popupMenuCanceled(PopupMenuEvent popupMenuEvent) {
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent popupMenuEvent) {
                setSelected(false); 
                panel.repaint();
            }

            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent popupMenuEvent) {
            }
        });
        return popup;
    }
 
    public class DeleteAction  extends AbstractAction {
        
        private GraphLink m_graphLink = null;
        private GraphPanel m_graphPanel = null;
        
        public DeleteAction(GraphPanel panel, GraphLink graphLink) {
            super("Delete");
            m_graphPanel = panel;
            m_graphLink = graphLink;
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            
            m_graphLink.m_connector.deleteInLink();
            m_graphPanel.repaint();
        }
    }
}
