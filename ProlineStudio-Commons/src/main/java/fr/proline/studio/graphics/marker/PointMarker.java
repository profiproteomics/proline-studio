package fr.proline.studio.graphics.marker;

import fr.proline.studio.graphics.BasePlotPanel;
import fr.proline.studio.graphics.marker.coordinates.AbstractCoordinates;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 * marker with a point shape on a plot (or nothing)
 * @author MB243701
 */
public class PointMarker extends AbstractMarker{
    
    private AbstractCoordinates m_coordinates;
    private Color m_markerColor = null;
    
    private static final int OVAL_WIDTH = 6;

        public PointMarker(BasePlotPanel plotPanel,  AbstractCoordinates coordinates) {
        super(plotPanel);
        m_coordinates = coordinates;
    }
    
    public PointMarker(BasePlotPanel plotPanel,  AbstractCoordinates coordinates, Color color) {
        super(plotPanel);
        m_coordinates = coordinates;
        m_markerColor = color;
    }
    
    public void setCoordinates(AbstractCoordinates coordinates) {
        m_coordinates = coordinates;
    }
    
    public AbstractCoordinates getCoordinates() {
        return m_coordinates;
    }
    
    @Override
    public void paint(Graphics2D g) {
        if (m_markerColor == null) {
            return;
        }
        int pixelX = m_coordinates.getPixelX(m_plotPanel);
        int pixelY = m_coordinates.getPixelY(m_plotPanel);
        g.setColor(m_markerColor);
        g.fillOval(pixelX-(OVAL_WIDTH/2), pixelY-(OVAL_WIDTH/2), OVAL_WIDTH, OVAL_WIDTH);
    }
    
}
