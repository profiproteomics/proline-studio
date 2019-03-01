package fr.proline.studio.graphics.marker.coordinates;

import fr.proline.studio.graphics.BasePlotPanel;

/**
 * Base class for coordinates used to position objects like markers in a graphic
 * 
 * @author JM235353
 */
public abstract class AbstractCoordinates {
    
    public abstract int getPixelX(BasePlotPanel plotPanel);
    public abstract int getPixelY(BasePlotPanel plotPanel);
    public abstract void setPixelPosition(BasePlotPanel plotPanel, int pixelX, int pixelY);
}
