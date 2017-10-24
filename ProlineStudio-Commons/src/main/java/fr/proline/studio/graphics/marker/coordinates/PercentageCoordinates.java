package fr.proline.studio.graphics.marker.coordinates;

import fr.proline.studio.graphics.BasePlotPanel;
import fr.proline.studio.graphics.XAxis;
import fr.proline.studio.graphics.YAxis;

/**
 * Coordinates expressed in percentage of the visible area
 * 
 * @author JM235353
 */
public class PercentageCoordinates extends AbstractCoordinates {
    
    private double m_percentageX;
    private double m_percentageY;
    
    public PercentageCoordinates(double percentageX, double percentageY) {
        m_percentageX = percentageX;
        m_percentageY = percentageY;
    }
    
    public int getPixelX(BasePlotPanel plotPanel) {
        XAxis xAxis = plotPanel.getXAxis();

        double xMax = xAxis.getMaxValue();
        double xMin = xAxis.getMinValue();
        
        return xAxis.valueToPixel(xMin+(xMax-xMin)*m_percentageX);
    }
    
    public int getPixelY(BasePlotPanel plotPanel) {
        YAxis yAxis = plotPanel.getYAxis();

        double yMax = yAxis.getMaxValue();
        double yMin = yAxis.getMinValue();

        return yAxis.valueToPixel(yMin + (yMax - yMin) * m_percentageY);
    }
    
    @Override
    public void setPixelPosition(BasePlotPanel plotPanel, int pixelX, int pixelY) {
        XAxis xAxis = plotPanel.getXAxis();
        double xMax = xAxis.getMaxValue();
        double xMin = xAxis.getMinValue();
        double dataX = xAxis.pixelToValue(pixelX);
        m_percentageX = (dataX-xMin)/(xMax-xMin);
        
        YAxis yAxis = plotPanel.getYAxis();
        double yMax = yAxis.getMaxValue();
        double yMin = yAxis.getMinValue();
        double dataY = yAxis.pixelToValue(pixelY);
        m_percentageY = (dataY-yMin)/(yMax-yMin);
 
    }
    
}
