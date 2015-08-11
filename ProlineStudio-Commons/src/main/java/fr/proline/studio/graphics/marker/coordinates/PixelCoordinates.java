package fr.proline.studio.graphics.marker.coordinates;

import fr.proline.studio.graphics.BasePlotPanel;

/**
 * Coordinates expressed in pixel
 * @author JM235353
 */
public class PixelCoordinates extends AbstractCoordinates {
    private int m_pixelX;
    private int m_pixelY;
    
    public PixelCoordinates(int pixelX, int pixelY) {
        m_pixelX = pixelX;
        m_pixelY = pixelY;
    }
    
    @Override
    public int getPixelX(BasePlotPanel plotPanel) {
        return m_pixelX;
    }
    
    @Override
    public int getPixelY(BasePlotPanel plotPanel) {
        return m_pixelY;
    }
    
    @Override
    public void setPixelPosition(BasePlotPanel plotPanel, int pixelX, int pixelY) {
        m_pixelX = pixelX;
        m_pixelY = pixelY;
    }
}


