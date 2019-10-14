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

/**
 * Coordinates expressed in pixel
 * 
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


