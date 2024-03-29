/* 
 * Copyright (C) 2019
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1 
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License 
 * along with this program; If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */
package fr.proline.studio.rsmexplorer.gui.calc.graph;

import fr.proline.studio.rsmexplorer.gui.calc.GraphPanel;
import static fr.proline.studio.rsmexplorer.gui.calc.graph.AbstractConnectedGraphObject.STROKE_SELECTED;
import fr.proline.studio.extendedtablemodel.GlobalTableModelInterface;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.geom.GeneralPath;
import javax.swing.AbstractAction;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

/**
 * Used only to display a link between two connectors
 * In reality, collectors are directly linked
 * @author JM235353
 */
public class GraphLink extends AbstractConnectedGraphObject {
    
    private GeneralPath m_path = null;
    private GraphConnector m_connector = null;
    
    
    private int m_x1;
    private int m_x2;
    private int m_y1;
    private int m_y2;
    
    protected GraphPanel m_graphPanel = null;
    
    public GraphLink(GraphConnector connector, GraphPanel panel) {
        super(TypeGraphObject.LINK);
        m_connector = connector;
        m_graphPanel = panel;
    }
    
    @Override
    public boolean isConnected(boolean recursive) {
        if (m_connector == null) {
            return false;
        }
        return m_connector.isConnected(recursive);
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
        if (m_connector == null) {
            return false;
        }
        return m_connector.calculationDone();
    }

    
    
    @Override
    public String getFullName() {
        return null;
    }

    @Override
    public String getDataName() {
        return null;
    }

    @Override
    public String getTypeName() {
        return null;
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
        
        Stroke previousStroke = g2.getStroke();
        BasicStroke stroke = m_selected ? STROKE_SELECTED : STROKE_NOT_SELECTED;
        g2.setStroke(stroke);
        g2.draw(m_path);
        g2.setStroke(previousStroke);
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
        popup.add(new DeleteAction());
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


    @Override
    public GlobalTableModelInterface getGlobalTableModelInterface(int index) {
        return null;
    }

    @Override
    public String getPreviousDataName() {
        return null;
    }
 
    @Override
    public void deleteAction() {
        m_connector.deleteInLink();
        m_graphPanel.repaint();
    }

    @Override
    public String getTooltip(int x, int y) {
        return null;
    }
    
    public class DeleteAction  extends AbstractAction {
        

        public DeleteAction() {
            super("Delete");
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            deleteAction();
        }
    }
}
