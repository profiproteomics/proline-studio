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
