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
package fr.proline.studio.graphics;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.BufferedImage;

/**
 *
 * Display of a over panel on a plot that display a mini unzoom plot and
 * allow to zoom on it.
 * 
 * @author Jean-Philippe
 */
public class BasePlotPanelViewAll implements MoveableInterface {
    
    private BasePlotPanel m_basePlotPanel;
    
    private Rectangle m_previousBasePlotArea = new Rectangle();
    private Rectangle m_plotAreaViewAllMap = null;
    
    private BufferedImage m_viewAllDoubleBuffer = null;
    private boolean m_updateViewAllDoubleBuffer = false;
    
    private boolean m_selected = false;
    private boolean m_isDisplayed = false;
    
    private Handle[] m_handles = new Handle[4];
            
    private static final BasicStroke STROKE_1 = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
    
    private static final float dash[] = {2.0f, 2.0f};
    private static final BasicStroke STROKE_1_DASHED = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f);

    private static final int MINIMAL_SIZE = 20;
    

    
    public BasePlotPanelViewAll(BasePlotPanel basePlotPanel) {
        m_basePlotPanel = basePlotPanel;
        
        m_handles[0] = new Handle(Handle.HandleType.LEFT_TOP, this);
        m_handles[1] = new Handle(Handle.HandleType.RIGHT_TOP, this);
        m_handles[2] = new Handle(Handle.HandleType.RIGHT_BOTTOM, this);
        m_handles[3] = new Handle(Handle.HandleType.LEFT_BOTTOM, this);
    }

    public void setDisplay(boolean b) {
        m_isDisplayed = b;
        m_basePlotPanel.repaint();
    }
    
    public boolean isDisplayed() {
        return m_isDisplayed;
    }
    
    public void updateDoubleBuffer() {
        m_updateViewAllDoubleBuffer = true;
    }
    
    public int convertToBasePlotX(int x) {
        if (x>m_plotAreaViewAllMap.x+m_plotAreaViewAllMap.width) {
            x = m_plotAreaViewAllMap.x+m_plotAreaViewAllMap.width;
        }
        if (x<m_plotAreaViewAllMap.x) {
            x = m_plotAreaViewAllMap.x;
        }
        double percentage = ((double)(x-m_plotAreaViewAllMap.x))/m_plotAreaViewAllMap.width;
        
        Rectangle plotArea = m_basePlotPanel.getPlotArea();
        
        return (int) Math.round(plotArea.x+percentage*plotArea.width);
    }
    

    public int convertToBasePlotY(int y) {
        
        if (y>m_plotAreaViewAllMap.y+m_plotAreaViewAllMap.height) {
            y = m_plotAreaViewAllMap.y+m_plotAreaViewAllMap.height;
        }
        if (y<m_plotAreaViewAllMap.y) {
            y = m_plotAreaViewAllMap.y;
        }
        
        double percentage = ((double)(y-m_plotAreaViewAllMap.y))/m_plotAreaViewAllMap.height;
        
        Rectangle plotArea = m_basePlotPanel.getPlotArea();
        
        return (int) Math.round(plotArea.y+percentage*plotArea.height);
    }
    
    
    public void moveHandle(int deltaX, int deltaY, boolean left, boolean top) {
        Rectangle plotArea = m_basePlotPanel.getPlotArea();
        
        if (left) {
            if ((deltaX > 0) && (m_plotAreaViewAllMap.width - deltaX < MINIMAL_SIZE)) {
                deltaX = m_plotAreaViewAllMap.width - MINIMAL_SIZE;
            } else if ((deltaX < 0) && (m_plotAreaViewAllMap.x + deltaX < plotArea.x)) {
                deltaX = plotArea.x - m_plotAreaViewAllMap.x;
            }
            m_plotAreaViewAllMap.x += deltaX;
            m_plotAreaViewAllMap.width -= deltaX;
        } else {
            if ((deltaX < 0) && (m_plotAreaViewAllMap.width + deltaX < MINIMAL_SIZE)) {
                deltaX = MINIMAL_SIZE - m_plotAreaViewAllMap.width;
            } else if ((deltaX > 0) && (m_plotAreaViewAllMap.x + m_plotAreaViewAllMap.width + deltaX > plotArea.x + plotArea.width - 1)) {
                deltaX = plotArea.x + plotArea.width - 1 - m_plotAreaViewAllMap.x - m_plotAreaViewAllMap.width;
            }
            m_plotAreaViewAllMap.width += deltaX;
        }
        
        if (top) {
            if ((deltaY > 0) && (m_plotAreaViewAllMap.height - deltaY < MINIMAL_SIZE)) {
                deltaY = m_plotAreaViewAllMap.height - MINIMAL_SIZE;
            } else if ((deltaY < 0) && (m_plotAreaViewAllMap.y + deltaY < plotArea.y)) {
                deltaY = plotArea.y - m_plotAreaViewAllMap.y;
            }
            m_plotAreaViewAllMap.y += deltaY;
            m_plotAreaViewAllMap.height -= deltaY;
        } else {
            if ((deltaY < 0) && (m_plotAreaViewAllMap.height + deltaY < MINIMAL_SIZE)) {
                deltaY = MINIMAL_SIZE - m_plotAreaViewAllMap.height;
            } else if ((deltaY > 0) && (m_plotAreaViewAllMap.y + m_plotAreaViewAllMap.height + deltaY > plotArea.y + plotArea.height - 1)) {
                deltaY = plotArea.y + plotArea.height - 1 - m_plotAreaViewAllMap.y - m_plotAreaViewAllMap.height;
            }
            m_plotAreaViewAllMap.height += deltaY;
        }
    }
    
    /*
    public void moveLeftTop(int deltaX, int deltaY) {

        Rectangle plotArea = m_basePlotPanel.getPlotArea();
        
        if ((deltaX>0) && (m_plotAreaViewAllMap.width-deltaX<MINIMAL_SIZE)) {
            deltaX = m_plotAreaViewAllMap.width-MINIMAL_SIZE;
        } else if ((deltaX<0) && (m_plotAreaViewAllMap.x+deltaX<plotArea.x)) {
            deltaX = plotArea.x - m_plotAreaViewAllMap.x;
        }
        if ((deltaY>0) && (m_plotAreaViewAllMap.height-deltaY<MINIMAL_SIZE)) {
            deltaY = m_plotAreaViewAllMap.height-MINIMAL_SIZE;
        } else if ((deltaY<0) && (m_plotAreaViewAllMap.y+deltaY<plotArea.y)) {
            deltaY = plotArea.y - m_plotAreaViewAllMap.y;
        }
        
        
        m_plotAreaViewAllMap.x += deltaX;
        m_plotAreaViewAllMap.width -= deltaX;
        
        m_plotAreaViewAllMap.y += deltaY;
        m_plotAreaViewAllMap.height -= deltaY;
        
    }
    
    public void moveRightBottom(int deltaX, int deltaY) {

        Rectangle plotArea = m_basePlotPanel.getPlotArea();
        
        if ((deltaX<0) && (m_plotAreaViewAllMap.width+deltaX<MINIMAL_SIZE)) {
            deltaX = MINIMAL_SIZE-m_plotAreaViewAllMap.width;
        } else if ((deltaX>0) && (m_plotAreaViewAllMap.x + m_plotAreaViewAllMap.width+deltaX > plotArea.x+plotArea.width-1)) {
            deltaX = plotArea.x+plotArea.width-1-m_plotAreaViewAllMap.x - m_plotAreaViewAllMap.width;
        }  
        
        
        if ((deltaY<0) && (m_plotAreaViewAllMap.height+deltaY<MINIMAL_SIZE)) {
            deltaY = MINIMAL_SIZE-m_plotAreaViewAllMap.height;
        } else if ((deltaY>0) && (m_plotAreaViewAllMap.y + m_plotAreaViewAllMap.height+deltaY>plotArea.y+plotArea.height-1)) {
            deltaY = plotArea.y+plotArea.height-1-m_plotAreaViewAllMap.y - m_plotAreaViewAllMap.height;
        } 
        m_plotAreaViewAllMap.width += deltaX;
        m_plotAreaViewAllMap.height += deltaY;
        
    }*/
    
    public MoveableInterface getOverMovable(int x, int y) {
        
        if (!m_isDisplayed) {
            return null;
        }
        
        for (int i = 0; i < m_handles.length; i++) {
            if (m_handles[i].insideXY(x, y)) {
                return m_handles[i];
            }
        }
        if (insideXY(x, y)) {
            return this;
        }

        return null;
    }
    
   /**
     * paint each Plot with grid and cursors
     *
     * @param g2d
     */
    public void paintBufferedViewAllMap(Graphics2D g2d) {
        
        if (!m_isDisplayed) {
            return;
        }
        
        XAxis xAxis = m_basePlotPanel.getXAxis();
        YAxis yAxis = m_basePlotPanel.getYAxis();
        YAxis rightYAxis = m_basePlotPanel.getYAxisRight();

        // store previous values of Axis
        AxisDataStore.storeValues(xAxis, yAxis, rightYAxis);
        
        // prepare the paint zone rectangle
        preparePaintZone();

        // look for current viewArea
        double y2 = yAxis.getMinValue();
        double y1 = yAxis.getMaxValue();
        double x1 = xAxis.getMinValue();
        double x2 = xAxis.getMaxValue();
        
        // set axis values to view all
        double[] resMinMax = m_basePlotPanel.getMinMaxPlots(xAxis);
        xAxis.setRange(resMinMax[0], resMinMax[1]);
        xAxis.m_x = m_plotAreaViewAllMap.x;
        xAxis.m_y = m_plotAreaViewAllMap.y+m_plotAreaViewAllMap.height;
        xAxis.m_width = m_plotAreaViewAllMap.width;
        xAxis.m_height = 1;

        resMinMax = m_basePlotPanel.getMinMaxPlots(yAxis);
        yAxis.setRange(resMinMax[0], resMinMax[1]);
        yAxis.m_x = m_plotAreaViewAllMap.x;
        yAxis.m_y = m_plotAreaViewAllMap.y;
        yAxis.m_width = 1;
        yAxis.m_height = m_plotAreaViewAllMap.height;
        
        if (rightYAxis.hasPlots()) {
            resMinMax = m_basePlotPanel.getMinMaxPlots(rightYAxis);
            rightYAxis.setRange(resMinMax[0], resMinMax[1]);
            rightYAxis.m_x = m_plotAreaViewAllMap.x + m_plotAreaViewAllMap.width;
            rightYAxis.m_y = m_plotAreaViewAllMap.y;
            rightYAxis.m_width = 1;
            rightYAxis.m_height = m_plotAreaViewAllMap.height;
        }
        
        
        // create double buffer image if needed
        boolean createDoubleBuffer = ((m_viewAllDoubleBuffer == null) || (m_viewAllDoubleBuffer.getWidth() != m_plotAreaViewAllMap.width) || (m_viewAllDoubleBuffer.getHeight() != m_plotAreaViewAllMap.height));
        if (createDoubleBuffer) {
            m_viewAllDoubleBuffer = new BufferedImage(m_plotAreaViewAllMap.width, m_plotAreaViewAllMap.height, BufferedImage.TYPE_INT_ARGB);
        }
        if (createDoubleBuffer || m_updateViewAllDoubleBuffer) {

            Graphics2D graphicBufferG2d = (Graphics2D) m_viewAllDoubleBuffer.getGraphics();
            graphicBufferG2d.translate(-m_plotAreaViewAllMap.x, -m_plotAreaViewAllMap.y);
            paintViewAllMap(graphicBufferG2d, xAxis, yAxis, rightYAxis);
            m_updateViewAllDoubleBuffer = false;
        }
        
        g2d.drawImage(m_viewAllDoubleBuffer, m_plotAreaViewAllMap.x, m_plotAreaViewAllMap.y, null);

        paintViewAllMapOver(g2d, xAxis, yAxis, rightYAxis, x1, y1, x2, y2);

        // restore previous values of Axis
        AxisDataStore.restoreValues(xAxis, yAxis, rightYAxis);

    }
    
    private void preparePaintZone() {
        Rectangle plotArea = m_basePlotPanel.getPlotArea();

        // zone to paint
        if (m_plotAreaViewAllMap == null) {
            m_plotAreaViewAllMap = new Rectangle();
            m_plotAreaViewAllMap.width = (int) Math.round(plotArea.getWidth() * 0.2);
            m_plotAreaViewAllMap.height = (int) Math.round(plotArea.getHeight() * 0.2);
            m_plotAreaViewAllMap.x = plotArea.x + plotArea.width - 5 - m_plotAreaViewAllMap.width;
            m_plotAreaViewAllMap.y = plotArea.y + plotArea.height - 5 - m_plotAreaViewAllMap.height;

            m_previousBasePlotArea.x = plotArea.x;
            m_previousBasePlotArea.y = plotArea.y;
            m_previousBasePlotArea.width = plotArea.width;
            m_previousBasePlotArea.height = plotArea.height;
        } else if ((m_previousBasePlotArea.x != plotArea.x) || (m_previousBasePlotArea.y != plotArea.y) || (m_previousBasePlotArea.width != plotArea.width) || (m_previousBasePlotArea.height != plotArea.height)) {
            // if the base plot has changed, we must resize de plotViewAll

            double percentage = ((double) m_plotAreaViewAllMap.x - m_previousBasePlotArea.x) / m_previousBasePlotArea.width;
            int x1 = (int) Math.round(plotArea.x + plotArea.width * percentage);
            percentage = ((double) m_plotAreaViewAllMap.x + m_plotAreaViewAllMap.width - m_previousBasePlotArea.x) / m_previousBasePlotArea.width;
            int x2 = (int) Math.round(plotArea.x + plotArea.width * percentage);

            percentage = ((double) m_plotAreaViewAllMap.y - m_previousBasePlotArea.y) / m_previousBasePlotArea.height;
            int y1 = (int) Math.round(plotArea.y + plotArea.height * percentage);
            percentage = ((double) m_plotAreaViewAllMap.y + m_plotAreaViewAllMap.height - m_previousBasePlotArea.y) / m_previousBasePlotArea.height;
            int y2 = (int) Math.round(plotArea.y + plotArea.height * percentage);

            m_plotAreaViewAllMap.x = x1;
            m_plotAreaViewAllMap.y = y1;
            m_plotAreaViewAllMap.width = x2 - x1;
            m_plotAreaViewAllMap.height = y2 - y1;

            m_previousBasePlotArea.x = plotArea.x;
            m_previousBasePlotArea.y = plotArea.y;
            m_previousBasePlotArea.width = plotArea.width;
            m_previousBasePlotArea.height = plotArea.height;
        }
    }

    private void paintViewAllMap(Graphics2D g2d, XAxis xAxis, YAxis yAxis, YAxis rightYAxis) {

        if (!m_isDisplayed) {
            return;
        }

        // paint background
        g2d.setColor(Color.white);
        g2d.fillRect(m_plotAreaViewAllMap.x, m_plotAreaViewAllMap.y, m_plotAreaViewAllMap.width, m_plotAreaViewAllMap.height);
        

        for (PlotBaseAbstract plot : yAxis.getPlots()) {
            plot.paint(g2d, xAxis, yAxis);
        }
        
        if (rightYAxis != null) {
            for (PlotBaseAbstract plot : rightYAxis.getPlots()) {
                plot.paint(g2d, xAxis, rightYAxis);
            }
        }
    }
    
    private void paintViewAllMapOver(Graphics2D g2d, XAxis xAxis, YAxis yAxis, YAxis rightYAxis, double x1, double y1, double x2, double y2) {
        
        if (!m_isDisplayed) {
            return;
        }
        
        Shape previousClipping = g2d.getClip();
        g2d.setClip(m_plotAreaViewAllMap);


        // paint zoom area
        g2d.setColor(Color.red);
        g2d.setStroke(STROKE_1_DASHED);
        int zoomX = xAxis.valueToPixel(x1);
        int zoomY = yAxis.valueToPixel(y1);
        int zoomWidth = xAxis.valueToPixel(x2) - zoomX;
        int zoomHeight = yAxis.valueToPixel(y2) - zoomY;
        g2d.drawRect(zoomX, zoomY, zoomWidth, zoomHeight);
        
        
        // paint frame
        g2d.setColor(Color.black);
        g2d.setStroke(STROKE_1);
        int delta = 0; 
        g2d.drawRect(m_plotAreaViewAllMap.x+delta, m_plotAreaViewAllMap.y+delta, m_plotAreaViewAllMap.width-1-delta*2, m_plotAreaViewAllMap.height-1-delta*2);

        g2d.setClip(previousClipping);
        
        if (m_selected) {
            for (int i=0;i<m_handles.length;i++) {
                m_handles[i].paint(g2d, m_plotAreaViewAllMap);
            }
        }
    }

    @Override
    public boolean insideXY(int x, int y) {
        if (!m_isDisplayed) {
            return false;
        }
        for (int i = 0; i < m_handles.length; i++) {
            if (m_handles[i].insideXY(x, y)) {
                return true;
            }
        }
        
        return m_plotAreaViewAllMap.contains(x, y);
    }

    @Override
    public void moveDXY(int deltaX, int deltaY) {
        m_plotAreaViewAllMap.x += deltaX;
        m_plotAreaViewAllMap.y += deltaY;
        
        Rectangle plotArea = m_basePlotPanel.getPlotArea();
        
        if (m_plotAreaViewAllMap.x<plotArea.x) {
            m_plotAreaViewAllMap.x = plotArea.x;
        } else if (m_plotAreaViewAllMap.x+m_plotAreaViewAllMap.width>plotArea.x+plotArea.width-1) {
            m_plotAreaViewAllMap.x = plotArea.x+plotArea.width-+m_plotAreaViewAllMap.width-1;
        }
        
        if (m_plotAreaViewAllMap.y<plotArea.y) {
            m_plotAreaViewAllMap.y = plotArea.y;
        } else if (m_plotAreaViewAllMap.y+m_plotAreaViewAllMap.height>plotArea.y+plotArea.height-1) {
            m_plotAreaViewAllMap.y = plotArea.y+plotArea.height-+m_plotAreaViewAllMap.height-1;
        }
        
        m_basePlotPanel.repaint();
    }

    @Override
    public boolean isMoveable() {
        return true;
    }

    @Override
    public void snapToData(boolean isCtrlOrShiftDown) {
    }

    @Override
    public void setSelected(boolean s, boolean isCtrlOrShiftDown) {
        m_selected = s;
        
    }
    
    public static class Handle implements MoveableInterface {

        public enum HandleType {
            LEFT_TOP,
            RIGHT_TOP,
            RIGHT_BOTTOM,
            LEFT_BOTTOM
        }
        
        private static final int SIZE = 7;
        
        private HandleType m_type;
        private Rectangle m_handleRectangle = new Rectangle();
        private BasePlotPanelViewAll m_viewAll;
        
        public Handle(HandleType type, BasePlotPanelViewAll viewAll) {
            m_type = type;
            m_handleRectangle.width = SIZE;
            m_handleRectangle.height = SIZE;
            m_viewAll = viewAll;
        }
        
        public void paint(Graphics2D g2d, Rectangle r) {
            switch (m_type) {
                case LEFT_TOP:
                    m_handleRectangle.x = r.x-SIZE/2;
                    m_handleRectangle.y = r.y-SIZE/2;
                    break;
                case RIGHT_TOP:
                    m_handleRectangle.x = r.x+r.width-1-SIZE/2;
                    m_handleRectangle.y = r.y-SIZE/2;
                    break;
                case RIGHT_BOTTOM:
                    m_handleRectangle.x = r.x+r.width-1-SIZE/2;
                    m_handleRectangle.y = r.y+r.height-1-SIZE/2;
                    break;
                case LEFT_BOTTOM:
                    m_handleRectangle.x = r.x-SIZE/2;
                    m_handleRectangle.y = r.y+r.height-1-SIZE/2;
                    break;
            }
            
            g2d.setColor(Color.white);
            g2d.fillRect(m_handleRectangle.x, m_handleRectangle.y, m_handleRectangle.width, m_handleRectangle.height);
            
            g2d.setColor(Color.black);
            g2d.drawRect(m_handleRectangle.x, m_handleRectangle.y, m_handleRectangle.width, m_handleRectangle.height);
        }
        
        @Override
        public boolean insideXY(int x, int y) {
            return m_handleRectangle.contains(x, y);
        }

        @Override
        public void moveDXY(int deltaX, int deltaY) {
            switch (m_type) {
                case LEFT_TOP:
                    m_viewAll.moveHandle(deltaX, deltaY, true, true);
                    break;
                case RIGHT_TOP:
                    m_viewAll.moveHandle(deltaX, deltaY, false, true);
                    break;
                case RIGHT_BOTTOM:
                    m_viewAll.moveHandle(deltaX, deltaY, false, false);
                    break;
                case LEFT_BOTTOM:
                    m_viewAll.moveHandle(deltaX, deltaY, true, false);
                    break;
            }
        }

        @Override
        public boolean isMoveable() {
            return true;
        }

        @Override
        public void snapToData(boolean isCtrlOrShiftDown) {
        }

        @Override
        public void setSelected(boolean s, boolean isCtrlOrShiftDown) {
            m_viewAll.setSelected(true, false);
        }
        
    }
}
