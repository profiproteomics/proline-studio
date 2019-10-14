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
package fr.proline.studio.graphics.measurement;

/**
 * Base Class for all measurements
 * @author JM235353
 */
public abstract class AbstractMeasurement {
    
    public enum MeasurementType {
        X_AXIS_POPUP, // Measurement through X Axis Popup
        Y_AXIS_POPUP, // Measurement through Y Axis Popup
        PLOT_POPUP    // Measurement through Plot Zone Popup
    }
    
    public abstract String getName();
    public abstract MeasurementType getMeasurementType();
    public abstract void applyMeasurement(int x, int y);
    public abstract boolean canApply();
}
