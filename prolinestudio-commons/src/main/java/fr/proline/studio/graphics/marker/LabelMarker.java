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

import fr.proline.studio.graphics.marker.coordinates.AbstractCoordinates;
import fr.proline.studio.graphics.BasePlotPanel;
import fr.proline.studio.graphics.MoveableInterface;
import fr.proline.studio.graphics.marker.coordinates.PixelCoordinates;
import java.awt.*;

/**
 * Marker to display text at a specific position
 *
 * @author JM235353
 */
public class LabelMarker extends AbstractMarker implements MoveableInterface {

    public static final int ORIENTATION_X_LEFT = 0;
    public static final int ORIENTATION_X_RIGHT = 1;
    public static final int ORIENTATION_XY_MIDDLE = 2;
    public static final int ORIENTATION_Y_TOP = 3;
    public static final int ORIENTATION_Y_BOTTOM = 4;
    
    public static final Font TEXT_FONT = new Font("Arial", Font.PLAIN, 12);
    public static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 16);
    
    private Font m_font = TEXT_FONT;
    private FontMetrics m_metrics;
    
    private PointMarker m_anchorMarker;
    private AbstractCoordinates m_labelCoordinates = null;
    
    private String m_valueLabel = null;
    
    private final int m_orientationX;
    private final int m_orientationY;
    
    private static final int LINE_DELTA = 18;
    private final static int COLOR_WIDTH = 15;
    private final static int DELTA_COLOR = 4;
    private final static int DELTA = 3;


    private int m_heightBox;
    private int m_widthBox;
    
    // show a color point in the marker
    private Color m_referenceColor = null;
    
    // draw the line to anchor
    private boolean m_drawLineToAnchor = true;
    
    // draw frame around the text
    private boolean m_drawFrame  = true;

    private double m_zoomFactor = 1.0d;
    
    /**
     * Constructor to have a label without anchor
     * @param plotPanel
     * @param coordinates
     * @param valueLabel 
     */
    public LabelMarker(BasePlotPanel plotPanel, AbstractCoordinates coordinates, String valueLabel) {
        super(plotPanel);

        m_valueLabel = valueLabel;
        m_anchorMarker = new PointMarker(plotPanel, coordinates);
        m_orientationX = ORIENTATION_XY_MIDDLE;
        m_orientationY = ORIENTATION_XY_MIDDLE;
        m_drawLineToAnchor = false;
    }
    
    /**
     * Constructor to have a label with an anchor but without a reference color
     * @param plotPanel
     * @param coordinates
     * @param valueLabel
     * @param orientationX
     * @param orientationY 
     */
    public LabelMarker(BasePlotPanel plotPanel, AbstractCoordinates coordinates, String valueLabel, int orientationX, int orientationY) {
        super(plotPanel);

        m_valueLabel = valueLabel;
        m_anchorMarker = new PointMarker(plotPanel, coordinates);
        m_orientationX = orientationX;
        m_orientationY = orientationY;
        m_drawLineToAnchor = (m_orientationX != ORIENTATION_XY_MIDDLE);
    }

    /**
     * Constructor to have a label with a reference color to remember another graphical object
     * @param plotPanel
     * @param coordinates
     * @param valueLabel
     * @param orientationX
     * @param orientationY
     * @param referenceColor 
     */
    public LabelMarker(BasePlotPanel plotPanel, AbstractCoordinates coordinates, String valueLabel, int orientationX, int orientationY, Color referenceColor) {
        super(plotPanel);

        m_valueLabel = valueLabel;
        m_anchorMarker = new PointMarker(plotPanel, coordinates);
        m_orientationX = orientationX;
        m_orientationY = orientationY;
        m_referenceColor = referenceColor;
        m_drawLineToAnchor = (m_orientationX != ORIENTATION_XY_MIDDLE);
    }

    public void setCoordinates(AbstractCoordinates coordinates) {
        m_anchorMarker.setCoordinates(coordinates);
        m_firstPaint = true;
    }
    
    public AbstractCoordinates getCoordinates() {
        return m_anchorMarker.getCoordinates();
    }
    
    public void setLabel(String valueLabel) {
        m_valueLabel = valueLabel;
        m_firstPaint = true;
    }
    
    public void setZoomFactor(double zoomFactor) {
        m_zoomFactor = zoomFactor;
    }
    
    public void setFont(Font f) {
        m_font = f;
    }
    
    public void setDrawFrame(boolean v) {
        m_drawFrame = v;
    }
     
    private boolean hasReferenceColor() {
        return m_referenceColor != null;
    }
    
    public void setReferenceColor(Color referenceColor) {
        m_referenceColor = referenceColor;
    }
    
    private void prepareCoordinates() {

        int deltaX;
        switch (m_orientationX) {
            case ORIENTATION_X_LEFT:
                deltaX = -LINE_DELTA;
                break;
            case ORIENTATION_X_RIGHT:
                deltaX = LINE_DELTA;
                break;
            case ORIENTATION_XY_MIDDLE:
            default:
                deltaX = 0;
                break;
        }

        int deltaY;
        switch (m_orientationY) {
            case ORIENTATION_Y_TOP:
                deltaY = -LINE_DELTA;
                break;
            case ORIENTATION_Y_BOTTOM:
                deltaY = LINE_DELTA;
                break;
            case ORIENTATION_XY_MIDDLE:
            default:
                deltaY = 0;
                break;
        }

        m_labelCoordinates = new PixelCoordinates(deltaX, deltaY);

    }
    
    @Override
    public void paint(Graphics2D g) {

        AbstractCoordinates anchorCoordinates = m_anchorMarker.getCoordinates();
        int pixelX = (int) Math.round(anchorCoordinates.getPixelX(m_plotPanel) * m_zoomFactor);
        int pixelY = (int) Math.round(anchorCoordinates.getPixelY(m_plotPanel) * m_zoomFactor);
        
        
        
        // set font
        g.setFont(m_font);
        
        // calculate height and width if needed
        
        if (m_firstPaint) {
            
            prepareCoordinates();
            
            int deltaX = m_labelCoordinates.getPixelX(m_plotPanel);
            int deltaY = m_labelCoordinates.getPixelY(m_plotPanel);
            
            m_metrics = g.getFontMetrics();
            int stringWidth = m_metrics.stringWidth(m_valueLabel);
            int stringHeight = m_metrics.getHeight();
            stringWidth += DELTA * 2;
            if (hasReferenceColor()) {
                stringWidth += 2 * DELTA_COLOR + COLOR_WIDTH;
            }

            m_heightBox = stringHeight + DELTA * 2;
            m_widthBox = stringWidth;
            
            int xBox = 0;
            int yBox = 0;
            if (m_orientationX == ORIENTATION_X_RIGHT) {
                xBox = pixelX + deltaX;
                yBox = pixelY + deltaY - stringHeight / 2 - DELTA;
            } else if (m_orientationX == ORIENTATION_X_LEFT) {
                xBox = pixelX + deltaX - stringWidth ;
                yBox = pixelY + deltaY - stringHeight / 2 - DELTA;
            } else if (m_orientationX == ORIENTATION_XY_MIDDLE) {
                xBox = pixelX + deltaX - stringWidth / 2 ;
                yBox = pixelY + deltaY - stringHeight / 2 - DELTA;
            }
            
            // automatically fix values if the frame is outside the visible area
            Rectangle r = g.getClipBounds();
            if (xBox<r.getX()) {
                xBox = (int) Math.round(r.getX());
            } else if (xBox+m_widthBox>r.getWidth()) {
                xBox = (int) Math.round(r.getWidth()-m_widthBox-1);
            }
            
            
            if (yBox<r.getY()) {
                yBox = (int) Math.round(r.getY());
            } else if (yBox+m_heightBox>r.getHeight()) {
                yBox = (int) Math.round(r.getHeight()-m_heightBox-1);
            }
            
            
            m_labelCoordinates.setPixelPosition(m_plotPanel, xBox-pixelX, yBox-pixelY);
            
            m_firstPaint = false;
        }
        
        int xBox = pixelX+m_labelCoordinates.getPixelX(m_plotPanel);
        int yBox = pixelY+m_labelCoordinates.getPixelY(m_plotPanel);


        // draw label
        int delta;
        g.setColor(new Color(255, 255, 255, 196));
        g.fillRect(xBox, yBox, m_widthBox, m_heightBox);
        g.setColor(Color.black);
        if (m_drawFrame) {
            g.drawRect(xBox, yBox, m_widthBox, m_heightBox);
        }
        if (hasReferenceColor()) {
            g.setColor(m_referenceColor);
            g.fillRect(xBox + DELTA_COLOR, yBox + ((m_heightBox - COLOR_WIDTH) / 2), COLOR_WIDTH, COLOR_WIDTH);
            delta = 2 * DELTA_COLOR + COLOR_WIDTH;
            g.setColor(Color.black);
        } else {
            delta = DELTA;
        }
        int spacer = (m_heightBox - (m_metrics.getAscent() + m_metrics.getDescent()))/2;
        g.drawString(m_valueLabel, xBox + delta, yBox + m_metrics.getAscent() + spacer);
        
        // draw line from anchor to label
        if (m_drawLineToAnchor) {
            g.setColor(Color.black);
            if (pixelX < xBox) {
                g.drawLine(pixelX, pixelY, xBox, yBox + m_heightBox / 2);
            } else if (pixelX > xBox + m_widthBox) {
                g.drawLine(pixelX, pixelY, xBox + m_widthBox, yBox + m_heightBox / 2);
            } else if (pixelY < yBox) {
                g.drawLine(pixelX, pixelY, xBox + m_widthBox / 2, yBox);
            } else if (pixelY > yBox) {
                g.drawLine(pixelX, pixelY, xBox + m_widthBox / 2, yBox + m_heightBox);
            }
        }



    }
    private boolean m_firstPaint = true;

    @Override
    public boolean inside(int x, int y) {
        if (m_labelCoordinates == null) {
            return false;
        }
        AbstractCoordinates anchorCoordinates = m_anchorMarker.getCoordinates();
        int pixelX = anchorCoordinates.getPixelX(m_plotPanel);
        int pixelY = anchorCoordinates.getPixelY(m_plotPanel);
        int xBox = pixelX+m_labelCoordinates.getPixelX(m_plotPanel);
        int yBox = pixelY+m_labelCoordinates.getPixelY(m_plotPanel);
        return (x>=xBox) && (x<=xBox+m_widthBox) && (y>=yBox) && (y<=yBox+m_heightBox);
    }

    @Override
    public void move(int deltaX, int deltaY) {
        m_labelCoordinates.setPixelPosition(m_plotPanel, m_labelCoordinates.getPixelX(m_plotPanel)+deltaX, m_labelCoordinates.getPixelY(m_plotPanel)+deltaY);
    }

    @Override
    public boolean isMoveable() {
        return true;
    }

    @Override
    public void snapToData(boolean isCtrlOrShiftDown) {
        // no snap to data for markers
    }
    
    @Override
    public void setSelected(boolean s, boolean isCtrlOrShiftDown) {
        // no selection for markers
    }

    @Override
    public AbstractMarker clone(BasePlotPanel plotPanel) throws CloneNotSupportedException {
        LabelMarker clone = (LabelMarker)super.clone(plotPanel); 
        clone.m_anchorMarker = (PointMarker)m_anchorMarker.clone(plotPanel);
        return clone;
    }

    
}
