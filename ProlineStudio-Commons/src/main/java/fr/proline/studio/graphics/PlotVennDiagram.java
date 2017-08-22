package fr.proline.studio.graphics;

import fr.proline.studio.comparedata.CompareDataInterface;
import fr.proline.studio.graphics.marker.LabelMarker;
import fr.proline.studio.graphics.marker.coordinates.PercentageCoordinates;
import fr.proline.studio.graphics.venndiagram.Circle;
import fr.proline.studio.graphics.venndiagram.IntersectArea;
import fr.proline.studio.graphics.venndiagram.Set;
import fr.proline.studio.graphics.venndiagram.SetList;
import fr.proline.studio.parameter.ParameterList;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.util.ArrayList;

/**
 *
 * @author JM235353
 */
public class PlotVennDiagram extends PlotMultiDataAbstract {
 
    private SetList m_setList = null;
    
    private static final BasicStroke STROKE_5 = new BasicStroke(5);
    
    private boolean firstPaint = true;
    
    public PlotVennDiagram(BasePlotPanel plotPanel, CompareDataInterface compareDataInterface, CrossSelectionInterface crossSelectionInterface, int[] cols) {
        super(plotPanel, PlotType.SCATTER_PLOT, compareDataInterface, crossSelectionInterface);
        
        update(cols, null); 
    }

    @Override
    public boolean needsXAxis() {
        return false;
    }

    @Override
    public boolean needsYAxis() {
        return false;
    }

    @Override
    public void parametersChanged() {
        
    }

    @Override
    public void paint(Graphics2D g) {

        if (m_setList == null) {
            return;
        }
        
        Graphics2D g2d = (Graphics2D) g;
        
        int width = m_plotPanel.getWidth();
        int height = m_plotPanel.getHeight();
        

        boolean scaled = m_setList.scale(width, height, 10);

        if (scaled || firstPaint) {
            m_setList.generateAreas();
            
            // must be updated because axis depend of the pixel height and width for venn diagram
            m_plotPanel.updateAxis(this);
            m_plotPanel.getXAxis().setSize(0, height, width, 0);
            m_plotPanel.getYAxis().setSize(0, 0, 0, height);
        }
        
        if (firstPaint) {
            
            firstPaint = false;

            
            
            int labelIndex = 0;
            for (IntersectArea intersectArea : m_setList.getGeneratedAreas()) {
                Set s = intersectArea.getOnlySet();
                if (s == null) {
                    continue;
                }
                labelIndex++;
                double percentageX = ((double) (width-100))/width;
                double percentageY = 30d/height;
                LabelMarker marker = new LabelMarker(m_plotPanel, new PercentageCoordinates(percentageX, percentageY*labelIndex), s.getName() , LabelMarker.ORIENTATION_XY_MIDDLE, LabelMarker.ORIENTATION_XY_MIDDLE, m_setList.getColor(intersectArea.getIntersectedMap()));
                addMarker(marker);

            }
        }

        for (IntersectArea intersectArea : m_setList.getGeneratedAreas()) {
            
            //if (colorIndex == areaToPaint) {
                g.setColor(m_setList.getColor(intersectArea.getIntersectedMap()));
                Area a = intersectArea.getArea();
                g2d.fill(a);
                
                /*g.setColor(Color.black);
                g.drawString(String.valueOf(areaToPaint), 10, 10);*/
            //}
        }
        /*areaToPaint++;
        if (areaToPaint == m_setList.getGeneratedAreas().size()) {
            areaToPaint = 0;
        }*/
        
        g.setColor(Color.white);
        Stroke previousStroke = g2d.getStroke();
        g2d.setStroke(STROKE_5);
        /*for (IntersectArea intersectArea : m_setList.getGeneratedAreas()) {

            Area a = intersectArea.getArea();
            g2d.draw(a);
        }*/
        
        for (Set set : m_setList.getList()) {
            Circle c = set.getCircle();

            int x = (int) Math.round(c.getX() - c.getRadius());
            int y = (int) Math.round(c.getY() - c.getRadius());
            int size = (int) Math.round(c.getRadius() * 2);
            g.drawOval(x, y, size, size);

            //g.drawString(set.getName(), x+size/2, y+size/2);
        }
     
        g2d.setStroke(previousStroke);
    }

    @Override
    public String getToolTipText(double x, double y) {
        return null;
    }

    @Override
    public void update() {
        
        final double EPSILON = 1e-15;
        
        // Sets Creation
        m_setList = new SetList();
        
        int nbCols = m_cols.length;
        int nbRows = m_compareDataInterface.getRowCount();
        for (int i=0;i<nbCols;i++) {
            int colId = m_cols[i];
            
            int nbValues = 0;
            for (int j=0;j<nbRows;j++) {
                Number value = (Number) m_compareDataInterface.getDataValueAt(j, colId);

                if (value != null) {
                    double d = value.doubleValue();
                    if ((! Double.isNaN(d)) && (Math.abs(d)>EPSILON)) {
                        nbValues++;
                    }    
                }
                
            }
            Set s = new Set(m_compareDataInterface.getDataColumnIdentifier(colId), nbValues);
            m_setList.addSet(s);
            
        }
        
        for (int i=0;i<nbCols;i++) {
            int colId1 = m_cols[i];
            for (int j=0;j<nbCols;j++) {
                int colId2 = m_cols[j];
                int nbValues = 0;
                for (int k = 0; k < nbRows; k++) {
                    Number value1 = (Number) m_compareDataInterface.getDataValueAt(k, colId1);

                    if (value1 != null) {
                        double d1 = value1.doubleValue();
                        if ((!Double.isNaN(d1)) && (Math.abs(d1) > EPSILON)) {
                            
                            Number value2 = (Number) m_compareDataInterface.getDataValueAt(k, colId2);

                            if (value2 != null) {
                                double d2 = value2.doubleValue();
                                if ((!Double.isNaN(d2)) && (Math.abs(d2) > EPSILON)) {
                                    nbValues++;
                                }
                            }

                        }
                    }
                    if (nbValues>0) {
                        m_setList.addIntersection(m_setList.getSet(i), m_setList.getSet(j), nbValues);
                    }
                }
            }
        }

        
        m_setList.approximateSolution();
        m_setList.optimizeSolution();

        firstPaint = true;
        
        m_plotPanel.repaint();
        
    }

    @Override
    public boolean select(double x, double y, boolean append) {
        return false;
    }

    @Override
    public boolean select(Path2D.Double path, double minX, double maxX, double minY, double maxY, boolean append) {
        return false;
    }

    @Override
    public ArrayList<ParameterList> getParameters() {
        return null;
    }

    @Override
    public boolean isMouseOnPlot(double x, double y) {
        return false;
    }

    @Override
    public boolean isMouseOnSelectedPlot(double x, double y) {
        return false;
    }

    @Override
    public String getEnumValueX(int index, boolean fromData) {
        return null;
    }

    @Override
    public String getEnumValueY(int index, boolean fromData) {
        return null;
    }

    @Override
    public double getNearestXData(double x) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double getNearestYData(double y) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double getXMin() {
        return 0;
    }

    @Override
    public double getXMax() {
        if (m_plotPanel == null) {
            return 0;
        }
        return m_plotPanel.getWidth();
    }

    @Override
    public double getYMin() {
        return 0;
    }

    @Override
    public double getYMax() {
        if (m_plotPanel == null) {
            return 0;
        }
        return m_plotPanel.getHeight();
    }

    @Override
    public ArrayList<Long> getSelectedIds() {
        return null;
    }

    @Override
    public void setSelectedIds(ArrayList<Long> selection) {
        
    }


    
}
