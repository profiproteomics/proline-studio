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

import java.awt.event.MouseEvent;
import java.util.EventListener;

/**
 *
 * @author MB243701
 */
public interface PlotPanelListener extends EventListener {
    
    /***
     * mouse clicked event on the plot panel 
     * @param e
     * @param xValue
     * @param yValue
     */
    public void plotPanelMouseClicked(MouseEvent e, double xValue, double yValue);
    
    /**
     * update on the X and Y axis, coming from the zooming gesture or pan gesture
     * 
     * @param oldX contains oldMinX and oldMaxX
     * @param newX contains newMinX and newMaxX
     * @param oldY contains oldMinY and oldMaxY
     * @param newY contains newMinY and newMaxY
     */
    public void updateAxisRange(double[] oldX, double[] newX,  double[] oldY,  double[] newY);
        
}
