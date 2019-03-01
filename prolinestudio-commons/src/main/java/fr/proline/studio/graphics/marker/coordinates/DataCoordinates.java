package fr.proline.studio.graphics.marker.coordinates;

import fr.proline.studio.graphics.BasePlotPanel;
import fr.proline.studio.graphics.XAxis;
import fr.proline.studio.graphics.YAxis;

/**
 * Coordinates expressed in data according to X and Y axis
 * 
 * @author JM235353
 */
public class DataCoordinates extends AbstractCoordinates {
    private double m_dataX;
    private double m_dataY;
    
    public DataCoordinates(double dataX, double dataY) {
        setDataCoordinates(dataX, dataY);
    }
    
    public final void setDataCoordinates(double dataX, double dataY) {
        m_dataX = dataX;
        m_dataY = dataY;
    }
    
    @Override
    public int getPixelX(BasePlotPanel plotPanel) {
        XAxis xAxis = plotPanel.getXAxis();

        return xAxis.valueToPixel(m_dataX);
    }
    
    @Override
    public int getPixelY(BasePlotPanel plotPanel) {
        YAxis yAxis = plotPanel.getYAxis();
        return yAxis.valueToPixel(m_dataY);
    }
    
    @Override
    public void setPixelPosition(BasePlotPanel plotPanel, int pixelX, int pixelY) {
        XAxis xAxis = plotPanel.getXAxis();
        YAxis yAxis = plotPanel.getYAxis();
        m_dataX = xAxis.pixelToValue(pixelX);
        m_dataY = yAxis.pixelToValue(pixelY);
    }
}
