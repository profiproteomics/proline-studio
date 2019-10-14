/* 
 * Copyright (C) 2019 VD225637
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
package fr.proline.studio.graphics.marker;

import fr.proline.studio.graphics.BasePlotPanel;
import fr.proline.studio.graphics.PlotLinear;
import fr.proline.studio.graphics.XAxis;
import fr.proline.studio.graphics.YAxis;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.util.ArrayList;

/**
 * Display the area below a PlotLinear when it is positive and upper a PlotLinear when it is negative
 * @author JM235353
 */
public class WaveformAreaMarker extends AbstractMarker {
    
    private final Color m_fillColor;
    private ArrayList<PolygonData> m_polygonDataArray = new ArrayList<>();
    private PlotLinear m_plotLinear = null;
    
    public WaveformAreaMarker(BasePlotPanel plotPanel, PlotLinear plotLinear, Color fillColor, double x1, double x2) {
        super(plotPanel);
        
        m_plotLinear = plotLinear;
        m_fillColor = fillColor;
        prepareData(plotLinear, x1, x2);
    }
    
    public void setPlotLinear(PlotLinear plotLinear) {
        m_plotLinear = plotLinear;
    }
    
    public void set(double x1, double x2) {
        prepareData(m_plotLinear, x1, x2);
    }
    
    /**
     * Prepare the data used to plot polygons under or over the linear plot 
     * @param plotLinear
     * @param xStart
     * @param xEnd 
     */
    private void prepareData(PlotLinear plotLinear, double xStart, double xEnd) {
        
        if (plotLinear == null) {
            return;
        }
        
        m_polygonDataArray.clear();
        
        double[] dataX = plotLinear.getDataX();
        double[] dataY = plotLinear.getDataY();
        
        int nb = dataX.length;
        
        if (nb == 0) {
            // nothing to do 
            return;
        }
        
        double[] tmpShapeX = new double[nb+2];
        double[] tmpShapeY = new double[nb+2];

        // first point
        final int SHAPE_OVER = 0;
        final int SHAPE_UNDER = 1;
        final int SHAPE_UNDEFINED = 2;
        int shapeType = SHAPE_UNDEFINED;
        
        
        // Search the beginning of the shape (could use a faster way to do that)
        int indexShape  = 0;
        double x0 = dataX[0];
        double y0 = dataY[0];
        int indexCur = 1;
        for (int i=indexCur;i<nb;i++) {
            double x1 = dataX[i];
            double y1 = dataY[i];
            
            if ((x0<=xStart) && (x1>=xStart)) {
                // find the start
                double yStart = ((xStart - x0) / (x1 - x0)) * (y1 - y0) + y0;
                
                // first point is on the X Axis
                tmpShapeX[indexShape] = xStart;
                tmpShapeY[indexShape] = 0;
                indexShape++;
                
                // draw the vertical line if needed
                if (yStart != 0) {
                    
                   
                    tmpShapeX[indexShape] = xStart;
                    tmpShapeY[indexShape] = yStart;
                    indexShape++;
                    if (yStart>0) {
                        shapeType = SHAPE_OVER;
                    } else {
                        //yStart<0
                        shapeType = SHAPE_UNDER;
                    }
                } else {
                    shapeType = SHAPE_UNDEFINED;
                }
                indexCur = i;
                break;
            }
            
            x0 = x1;
            y0 = y1;
        }
        
        boolean finished = false;
        x0 = dataX[indexCur-1];
        y0 = dataY[indexCur-1];
        for (int i=indexCur;i<nb;i++) {
            double x1 = dataX[i];
            double y1 = dataY[i];
            if (x1>=xEnd) {
                // this is the last point
                y1 = ((xEnd - x0) / (x1 - x0)) * (y1 - y0) + y0;
                x1 = xEnd;
                finished = true;
            }
            
            if (y1>0) {
                if ((shapeType == SHAPE_OVER) || (shapeType == SHAPE_UNDEFINED)) {
                    tmpShapeX[indexShape] = x1;
                    tmpShapeY[indexShape] = y1;
                    indexShape++;
                } else {
                    // shapeType == SHAPE_UNDER : the curve was under before
                    double xOnXAxis = (-y0 / (y1 - y0)) * (x1 - x0) + x0;
                    tmpShapeX[indexShape] = xOnXAxis;
                    tmpShapeY[indexShape] = 0;
                    indexShape++;
                    // we have finished a SHAPE_UNDER

                    // close the path
                    tmpShapeX[indexShape] = tmpShapeX[0];
                    tmpShapeY[indexShape] = tmpShapeY[0];
                    m_polygonDataArray.add(new PolygonData(tmpShapeX, tmpShapeY,indexShape+1));
                    
                    // we start a SHAPE_OVER
                    tmpShapeX[0] = xOnXAxis;
                    tmpShapeY[0] = 0;
                    tmpShapeX[1] = x1;
                    tmpShapeY[1] = y1;
                    indexShape = 2;
                }
                shapeType = SHAPE_OVER;
                
                if (finished) {
                    // at the last point
                    tmpShapeX[indexShape] = x1;
                    tmpShapeY[indexShape] = 0;
                    indexShape++;
                    
                    // close the path
                    tmpShapeX[indexShape] = tmpShapeX[0];
                    tmpShapeY[indexShape] = tmpShapeY[0];
                    
                    m_polygonDataArray.add(new PolygonData(tmpShapeX, tmpShapeY,indexShape+1));
                    break;
                }
            } else if (y1<0) {
                if ((shapeType == SHAPE_UNDER) || (shapeType == SHAPE_UNDEFINED)) {
                    tmpShapeX[indexShape] = x1;
                    tmpShapeY[indexShape] = y1;
                    indexShape++;
                } else {
                    // shapeType == SHAPE_OVER : the curve was over before
                    double xOnXAxis = (-y0 / (y1 - y0)) * (x1 - x0) + x0;
                    tmpShapeX[indexShape] = xOnXAxis;
                    tmpShapeY[indexShape] = 0;
                    indexShape++;
                    
                    // close the path
                    tmpShapeX[indexShape] = tmpShapeX[0];
                    tmpShapeY[indexShape] = tmpShapeY[0];
                    
                    // we have finished a SHAPE_OVER
                    m_polygonDataArray.add(new PolygonData(tmpShapeX, tmpShapeY,indexShape+1));
                    
                    // we start a SHAPE_UNDER
                    tmpShapeX[0] = xOnXAxis;
                    tmpShapeY[0] = 0;
                    tmpShapeX[1] = x1;
                    tmpShapeY[1] = y1;
                    indexShape = 2;
                }
                shapeType = SHAPE_UNDER;
            } else if (y1 == 0) {
                if (shapeType != SHAPE_UNDEFINED) {
                    tmpShapeX[indexShape] = x1;
                    tmpShapeY[indexShape] = y1;
                    indexShape++;
                    
                    // we have finished a shape over or under
                    
                    // close the path
                    tmpShapeX[indexShape] = tmpShapeX[0];
                    tmpShapeY[indexShape] = tmpShapeY[0];
                    
                    m_polygonDataArray.add(new PolygonData(tmpShapeX, tmpShapeY,indexShape+1));
                    shapeType = SHAPE_UNDEFINED;
                } 
                tmpShapeX[0] = x1;
                tmpShapeY[0] = 0;
                indexShape = 1;
 
            }
            
            x0 = x1;
            y0 = y1;
        }
        
    }

    
    
    @Override
    public void paint(Graphics2D g) {
        XAxis xAxis = m_plotPanel.getXAxis();
        YAxis yAxis = m_plotPanel.getYAxis();
        
        
        Shape previousClipping = g.getClip();
        int clipX = xAxis.valueToPixel(xAxis.getMinValue());
        int clipWidth = xAxis.valueToPixel(xAxis.getMaxValue()) - clipX;
        int clipY = yAxis.valueToPixel(yAxis.getMaxValue());
        int clipHeight = yAxis.valueToPixel(yAxis.getMinValue()) - clipY;
        g.setClip(clipX, clipY, clipWidth, clipHeight);


        g.setColor(m_fillColor);
        
        for (PolygonData polygonData : m_polygonDataArray) {
            polygonData.prepareForPaint(xAxis, yAxis);

            g.fillPolygon(polygonData.getPolygon());
        }
        
        g.setClip(previousClipping);
        
    }
    
    
    public class PolygonData {
        private double[] m_x;
        private double[] m_y;
        
        private Polygon m_polygon;
        
        public PolygonData(double[] xSource, double[] ySource, int size) {
            m_x = new double[size];
            m_y = new double[size];
            System.arraycopy( xSource, 0, m_x, 0, size);
            System.arraycopy( ySource, 0, m_y, 0, size);
    
            m_polygon = new Polygon();
            m_polygon.npoints = size;
            m_polygon.xpoints = new int[size];;
            m_polygon.ypoints = new int[size];
        }
        
        public void prepareForPaint(XAxis xAxis, YAxis yAxis) {
            
            int size = m_x.length;
            for (int i=0;i<size;i++) {
                m_polygon.xpoints[i] = xAxis.valueToPixel(m_x[i]);
                m_polygon.ypoints[i] = yAxis.valueToPixel(m_y[i]);
            }
        }
        
        public Polygon getPolygon() {
            return m_polygon;
        }
    }
}
