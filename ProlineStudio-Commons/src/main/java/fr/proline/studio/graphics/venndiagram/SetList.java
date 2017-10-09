package fr.proline.studio.graphics.venndiagram;

import fr.proline.studio.utils.CyclicColorPalette;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import org.apache.commons.math3.analysis.MultivariateFunction;

import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.*;
import org.apache.commons.math3.optim.*;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;

/**
 *
 * @author JM235353
 */
public class SetList {
    
    private final ArrayList<Set> m_setArrayList = new ArrayList<>(16);
    private final HashMap<Set, Integer> m_setMap = new HashMap<>();
            
    private ArrayList<IntersectArea> m_areas = null;
    
    private int m_scaleX = -1;
    private int m_scaleY = -1;
    
    private HashMap<Integer, Color> m_colorMap = new HashMap<>();
    
        public static final Color[] DEFAULT_BASE_PALETTE = {
        new Color(252, 180, 46),
        new Color(105, 203, 212),
        new Color(236, 33, 123),
        new Color(18, 168, 157),
        new Color(158, 31, 98),
        new Color(91, 131, 192)
    };
    
    public SetList() {
        
    }
    
    public ArrayList<Set> getList() {
        return m_setArrayList;
    }
    
    
    public Color getColor(int colorIndex) {

        Color c = m_colorMap.get(colorIndex);
        if (c != null) {
            return c;
        }
        
        return CyclicColorPalette.getColor(colorIndex, DEFAULT_BASE_PALETTE);
        
    }
    
    /*public static void test() {
        SetList setList = new SetList();
        Set s1 = new Set("S1 : 1000", 1000);
        Set s2 = new Set("S1 : 800", 800);
        Set s3 = new Set("S1 : 700", 700);
        Set s4 = new Set("S1 : 600", 600);
        setList.addSet(s1);
        setList.addSet(s2);
        setList.addSet(s3);
        setList.addSet(s4);
        setList.addIntersection(s1, s2, 500);
        setList.addIntersection(s2, s3, 150);
        setList.addIntersection(s1, s3, 100);
        setList.addIntersection(s1, s4, 125);
        setList.addIntersection(s2, s4, 125);
        
        setList.approximateSolution();
        setList.optimizeSolution();
        
        setList.scale(600, 600);
        setList.generateAreas();
        

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                TmpVennDiagramFrame frame = new TmpVennDiagramFrame(setList);
                frame.setVisible(true);
            }
        });
        
        System.out.println();
    }*/
    
    public void addSet(Set s) {
        int index = m_setArrayList.size();
        //s.setId(index);
        m_setArrayList.add(s);
        m_setMap.put(s, index);
        
    }
    
    public Set getSet(int i) {
        return m_setArrayList.get(i);
    }
    
    public void addIntersection(Set s1, Set s2, double size) {
        SetIntersection intersection = new SetIntersection(s1, s2, size);
        s1.addIntersection(intersection);
        s2.addIntersection(intersection);
    }
    
    public void approximateSolution() {
        
        int nbPositionned = 0;
        
        int setNb = m_setArrayList.size();
        
        if (setNb == 0) {
            return;
        }
        
        Set[] setArray = m_setArrayList.toArray(new Set[setNb]);
        Arrays.sort(setArray, Collections.reverseOrder());
        
        // position of the first circle is (0,0)
        Set firstSet = setArray[0];
        firstSet.getCircle().setPosition(0, 0);
        nbPositionned++;
        
        if (setNb == 1) {
            return;
        }
        
        // position of the second circle
        // we look for a circle which intersects in the order
        Set secondSet = null;
        for (int i=1;i<setNb;i++) {
            Set s = setArray[i];
            if (firstSet.intersect(s)) {
                secondSet = s;
                break;
            }
        }
        if (secondSet != null) {
            // calculate position of the second circle
            // to reach a precision of 0.1% (0.001) for the intersection area
            double areaIntersection = firstSet.getIntersection(secondSet);
            double distance = Calculations.circleDistanceForIntersectionArea(0.001, areaIntersection, firstSet.getCircle().getRadius(), secondSet.getCircle().getRadius());
            secondSet.getCircle().setPosition(distance, 0);
        
        } else {
            secondSet = setArray[1];
            secondSet.getCircle().setPosition(firstSet.getCircle().getRadius()+secondSet.getCircle().getRadius(), 0);
        }
        nbPositionned++;
        
        // to choose the following sets, we sort again and again
        // sorting will be different because it take in account the already positioned circles

        Arrays.sort(setArray, Collections.reverseOrder());
        
        ArrayList<Point2D.Double> intersectionPoints = new ArrayList<>();
        
        while (nbPositionned<setNb) {
            Set setCur = setArray[0];
            Circle cCur = setCur.getCircle();
            
            
            Circle[] circles = new Circle[nbPositionned];
            for (int i=setNb-nbPositionned;i<setNb;i++) {
                Set set2 = setArray[i];
                double distance;
                
                Circle c2 = set2.getCircle();
                if (setCur.intersect(set2)) {
                    distance = Calculations.circleDistanceForIntersectionArea(0.001, setCur.getIntersection(set2), cCur.getRadius(), c2.getRadius());
                } else {
                    distance = cCur.getRadius()+c2.getRadius();
                }
                Circle c = new Circle(c2.getX(), c2.getY(), distance);
                circles[i-setNb+nbPositionned] = c;
            }
            
            int nbCircles = circles.length;
            for (int i=0;i<nbCircles;i++) {
                Circle c1 = circles[i];
                for (int j=i+1;j<nbCircles;j++) {
                    Circle c2 = circles[j];
                    Circle.intersection(c1, c2, intersectionPoints);
                }
            }
            
            if (intersectionPoints.isEmpty()) {
                // we put the circle at right of the other circles
                double positionXMax = Double.NEGATIVE_INFINITY;
                double positionY = 0;
                for (int i = setNb - nbPositionned; i < setNb; i++) {
                    Set set2 = setArray[i];
                    Circle c2 = set2.getCircle();
                    double positionX = c2.getX()+c2.getRadius();
                    if (positionX>positionXMax) {
                        positionXMax = positionX;
                        positionY = c2.getY();
                    }
                }
                cCur.setPosition(positionXMax+cCur.getRadius(), positionY);
                
                
            } else {
                generateBarycenterPoints(intersectionPoints);
                
                // look for best solution
                Set[] lossCalculationSetArray = new Set[nbPositionned+1];
                lossCalculationSetArray[0] = setCur;
                for (int i=setNb-nbPositionned;i<setNb;i++) {
                    lossCalculationSetArray[i-setNb+nbPositionned+1] = setArray[i];
                }
                
                double bestLoss = Double.POSITIVE_INFINITY;
                Point2D.Double bestPoint = null;
                for (Point2D.Double point : intersectionPoints) {
                    cCur.setPosition(point.x, point.y);
                    double loss = lossFunction(lossCalculationSetArray);
                    if (loss<bestLoss) {
                        bestLoss = loss;
                        bestPoint = point;
                    }
                }
                cCur.setPosition(bestPoint.x, bestPoint.y);
                
            }
            nbPositionned++;
            
            intersectionPoints.clear();
            Arrays.sort(setArray, Collections.reverseOrder());
        }

    }
    public void optimizeSolution() {
        
        int nb = m_setArrayList.size();
        if (nb<2) {
            return;
        }
        
        SimplexOptimizer optimizer = new SimplexOptimizer(1e-5, 1e-10); 

        EvaluateFunction evaluateFunction = new EvaluateFunction(m_setArrayList);
        ObjectiveFunction objectiveFunction = new ObjectiveFunction(evaluateFunction);
        
        int simplexDimension = m_setArrayList.size()*2; // for each set, there are two variables : x and y of the center
        AbstractSimplex simplex = new NelderMeadSimplex(simplexDimension); 
        
        optimizer.optimize(new MaxEval(1000), objectiveFunction, simplex, GoalType.MINIMIZE, new InitialGuess(evaluateFunction.getCurrentGuess()) );

        
    }
    
    
    public void generateAreas() {
        
        int nb = m_setArrayList.size();
        if (nb == 0) {
            return;
        }

        // Initialization
        // first List contains one IntersectArea corresponding to first Set
        // second List contains all other IntersectArea corresponding to all others Set
        ArrayList<IntersectArea> resultList = new ArrayList<>();
        ArrayList<IntersectArea> todoList = new ArrayList<>();
        
        resultList.add(new IntersectArea(m_setArrayList.get(0)));

        for (int i = 1; i < nb; i++) {
            todoList.add(new IntersectArea(m_setArrayList.get(i)));
        }
        
        m_areas = generateAreasImpl(resultList, todoList);
        
        Collections.sort(m_areas);

    }
    private ArrayList<IntersectArea> generateAreasImpl(ArrayList<IntersectArea> resultList, ArrayList<IntersectArea> todoList) {
        
        if (todoList.isEmpty()) {
            return resultList;
        }
        
        IntersectArea pivotArea = todoList.remove(todoList.size()-1);
        
        boolean intersectionFound = false;
        ArrayList<IntersectArea> resultListModified = new ArrayList<>();
        for (IntersectArea area : resultList) {
            
            ArrayList<IntersectArea> intersectionList = null;
            if (pivotArea.isPotentialIntersect(area)) {
                intersectionList = pivotArea.intersect(area);
            }

            if (intersectionList !=null) {
                intersectionFound = true;
                for (IntersectArea a : intersectionList) {
                    resultListModified.add(a);
                }
            } else {
                resultListModified.add(area);
            }
        }
        if (!intersectionFound) {
            resultListModified.add(pivotArea);
        }
        
        return generateAreasImpl(resultListModified, todoList);
    }

    public ArrayList<IntersectArea> getGeneratedAreas() {
        return m_areas;
    }
        
    public boolean scale(int width, int height, int margin) {

        width -= 2* margin;
        height -= 2* margin;
        
        if ((m_scaleX == width) && (m_scaleY == height)) {
            return false;
        }
    
        m_scaleX = width;
        m_scaleY = height;
        
        double minX = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        
        for (Set set : m_setArrayList) {
            Circle c = set.getCircle();
            double x = c.getX();
            double y = c.getY();
            double r = c.getRadius();
            if (x-r<minX) {
                minX = x-r;
            }
            if (x+r>maxX) {
                maxX = x+r;
            }
            if (y-r<minY) {
                minY = y-r;
            }
            if (y+r>maxY) {
                maxY = y+r;
            }
        }
        double oldWidth = maxX-minX;
        double oldHeight = maxY-minY;
        
        double scaleFactorX = width/oldWidth;
        double scaleFactorY = height/oldHeight;
        
        double scale = scaleFactorX>scaleFactorY ? scaleFactorY : scaleFactorX;
        
        for (Set set : m_setArrayList) {
            Circle c = set.getCircle();
            double x2 = (c.getX()-minX)*scale + margin;
            double y2 = -(((c.getY()-minY)*scale)-height) + margin; // in the same time reverse Y axis for display
            double r = c.getRadius()*scale;
            c.scale(x2, y2, r);
        }
        
        return true;
    }
    
    private double lossFunction(Set[] setArray) {
        
        double loss = 0;
        
        int n = setArray.length;
        for (int i = 0; i < n; i++) {
            Set s1 = setArray[i];
            Circle c1 = s1.getCircle();
            for (int j = i + 1; j < n; j++) {
                Set s2 = setArray[j];
                Circle c2 = s2.getCircle();
                double area = c1.intersectionArea(c2);
                double wantedArea = s1.getIntersection(s2);
                double delta = wantedArea-area;
                loss += delta*delta;
            }
        }
        
        return loss;
    }
    
    
    private void generateBarycenterPoints(ArrayList<Point2D.Double> srcPoints) {
        int n = srcPoints.size();
        if (n<=2) {
            return;
        }
        for (int i=0;i<n;i++) {
            Point2D.Double p1 = srcPoints.get(i);
            for (int j=i+1;j<n;j++) {
                Point2D.Double p2 = srcPoints.get(j);
                srcPoints.add(new Point2D.Double((p1.x+p2.x)/2, (p1.y+p2.y)/2));
            }
        }

        for (int i = 0; i < n; i++) {
            Point2D.Double p1 = srcPoints.get(i);
            for (int j = i + 1; j < n; j++) {
                Point2D.Double p2 = srcPoints.get(j);
                for (int k = j + 1; k < n; k++) {
                    Point2D.Double p3 = srcPoints.get(k);
                    srcPoints.add(new Point2D.Double((p1.x + p2.x + p3.x) / 3, (p1.y + p2.y + p3.y) / 3));
                }
            }
        }
        
        for (int i = 0; i < n; i++) {
            Point2D.Double p1 = srcPoints.get(i);
            for (int j = i + 1; j < n; j++) {
                Point2D.Double p2 = srcPoints.get(j);
                for (int k = j + 1; k < n; k++) {
                    Point2D.Double p3 = srcPoints.get(k);
                    for (int m = k + 1; m < n; m++) {
                        Point2D.Double p4 = srcPoints.get(m);
                        srcPoints.add(new Point2D.Double((p1.x + p2.x + p3.x + p4.x) / 3, (p1.y + p2.y + p3.y + p4.y) / 4));
                    }
                }
            }
        }
        
    }
    
    public class EvaluateFunction implements MultivariateFunction {

        private final Set[] m_lossCalculationSetArray;

        public EvaluateFunction(ArrayList<Set> setArrayList) {
            m_lossCalculationSetArray = setArrayList.toArray(new Set[setArrayList.size()]);
        }
        
        public double[] getCurrentGuess() {
            int nb = m_lossCalculationSetArray.length;
            double[] guess = new double[nb*2];
            for (int i = 0; i < nb; i++) {
                Set s = m_lossCalculationSetArray[i];
                guess[i*2] = s.getCircle().getX();
                guess[i*2+1] = s.getCircle().getY();
            }
            return guess;
        }

        @Override
        public double value(double[] doubles) {
            int nb = m_lossCalculationSetArray.length;
            for (int i = 0; i < nb; i++) {
                Set s = m_lossCalculationSetArray[i];
                s.m_circle.setPosition(doubles[i * 2], doubles[i * 2 + 1]);
            }

            return lossFunction(m_lossCalculationSetArray);
        }
    }

    
}
