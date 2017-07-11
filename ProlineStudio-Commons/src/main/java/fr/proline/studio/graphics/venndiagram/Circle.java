package fr.proline.studio.graphics.venndiagram;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;

/**
 *
 * @author JM235353
 */
public class Circle {
    
    private double m_x = 0;
    private double m_y = 0;
    private double m_radius;
    
    private boolean m_positionSet = false;
    
    public Circle(double size) {
        m_radius = Math.sqrt(size / Math.PI);
    }
    
    public Circle(double x, double y, double radius) {
        m_x = x;
        m_y = y;
        m_radius = radius;
        m_positionSet = true;
    }
    
    public double getX() {
        return m_x;
    }
    
    public double getY() {
        return m_y;
    }
    
    public double getRadius() {
        return m_radius;
    }
    
    public boolean isPositionSet() {
        return m_positionSet;
    }
    
    public void setPosition(double x, double y) {
        m_x = x;
        m_y = y;
        m_positionSet = true;
    }
    
    public void scale(double x, double y, double r) {
        m_x = x;
        m_y = y;
        m_radius = r;
    }
    
    public double intersectionArea(Circle c) {
        
        double r1 = m_radius;
        double r2 = c.getRadius();
        double distance = distance(m_x, m_y, c.getX(), c.getY());
        
        // check if there is at least two intersections
        if ((distance >= (r1 + r2)) ) {
            return 0;
        }
        
        if (distance <= Math.abs(r1 - r2)) {
            double r = r1>r2 ? r2 : r1;
            return Math.PI*r*r;  // small circle is in big circle
        }
        
        return intersectionArea(distance, r1, r2);
    }
    
    public static double intersectionArea(double distance, double radius1, double radius2) {

        if (distance >= (radius1 + radius2)) {
            return 0;
        }
        
        if (distance <= Math.abs(radius1 - radius2)) {
            double r = radius1>radius2 ? radius2 : radius1;
            return r*r*Math.PI;
        }
        
        double part1 = radius1 * radius1 * Math.acos((distance * distance + radius1 * radius1 - radius2 * radius2) / (2 * distance * radius1));
        double part2 = radius2 * radius2 * Math.acos((distance * distance + radius2 * radius2 - radius1 * radius1) / (2 * distance * radius2));
        double part3 = 0.5 * Math.sqrt((-distance + radius1 + radius2) * (distance + radius1 - radius2) * (distance - radius1 + radius2) * (distance + radius1 + radius2));

        return part1 + part2 - part3;
    }
    
    public static void intersection(Circle c1, Circle c2, ArrayList<Point2D.Double> result) {
        
        double x1 = c1.getX();
        double y1 = c1.getY();
        double x2 = c2.getX();
        double y2 = c2.getY();
        
        double r1 = c1.getRadius();
        double r2 = c2.getRadius();
        
        double distance = distance(x1, y1, x2, y2);

        // check if there is at least two intersections
        if ((distance >= (r1 + r2)) || (distance <= Math.abs(r1 - r2))) {
            return;
        }
        
        double a = (r1 * r1 - r2 * r2 + distance * distance) / (2 * distance);
        double h = Math.sqrt(r1 * r1 - a * a);
        double x0 = x1 + a * (x2 - x1) / distance;
        double y0 = y1 + a * (y2 - y1) / distance;
        double rx = (y1 - y2) * (h / distance);
        double ry = (x1 - x2) * (h / distance);

        Point2D.Double p1 = new Point2D.Double(x0 + rx, y0 - ry);
        Point2D.Double p2 = new Point2D.Double(x0 - rx, y0 + ry);

        result.add(p1);
        result.add(p2);

    }
    
    public static double distance(double x1, double y1, double x2, double y2) {
        return Math.sqrt((x1-x2)*(x1-x2)+(y1-y2)*(y1-y2));
    }
    
    
}
