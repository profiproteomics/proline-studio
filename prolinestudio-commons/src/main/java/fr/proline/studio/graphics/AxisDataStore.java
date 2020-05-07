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

/**
 * Used to save axis settings and restore them when we want to modify them
 * to display the viewAllMap
 * 
 * @author Jean-Philippe
 */
public class AxisDataStore {
   
    private int m_x;
    private int m_y;
    private int m_width;
    private int m_height;

    // value of data
    private double m_minValue;
    private double m_maxValue;
    private double m_lockedMinValue = Double.NaN;

    // Min tick and max tick
    private double m_minTick;
    private double m_maxTick;
    private double m_tickSpacing;
    
    private static AxisDataStore[] m_axisDataStore;
    
    private AxisDataStore() {
        
    }
    
    public static void storeValues(Axis x, Axis y, Axis y2) {
        if (m_axisDataStore == null) {
            m_axisDataStore = new AxisDataStore[3];
            m_axisDataStore[0] = new AxisDataStore();
            m_axisDataStore[1] = new AxisDataStore();
            m_axisDataStore[2] = new AxisDataStore();
        }
        m_axisDataStore[0].storeValues(x);
        m_axisDataStore[1].storeValues(y);
        m_axisDataStore[2].storeValues(y2);
    }
    
    public static void restoreValues(Axis x, Axis y, Axis y2) {
        m_axisDataStore[0].restoreValues(x);
        m_axisDataStore[1].restoreValues(y);
        m_axisDataStore[2].restoreValues(y2);
    }
    
    
    private void storeValues(Axis axis) {
    
        if (axis == null) {
            return;
        }
        
        m_x = axis.m_x;
        m_y = axis.m_y;
        m_width = axis.m_width;
        m_height = axis.m_height;

        // value of data
        m_minValue = axis.m_minValue;
        m_maxValue = axis.m_maxValue;
        m_lockedMinValue = axis.m_lockedMinValue;

        // Min tick and max tick
        m_minTick = axis.m_minTick;
        m_maxTick = axis.m_maxTick;
        m_tickSpacing = axis.m_tickSpacing;
    }
    
    private void restoreValues(Axis axis) {
    
        if (axis == null) {
            return;
        }
                
        axis.m_x = m_x;
        axis.m_y = m_y;
        axis.m_width = m_width;
        axis.m_height = m_height;

        // value of data
        axis.m_minValue = m_minValue;
        axis.m_maxValue = m_maxValue;
        axis.m_lockedMinValue = m_lockedMinValue;

        // Min tick and max tick
        axis.m_minTick = m_minTick;
        axis.m_maxTick = m_maxTick;
        axis.m_tickSpacing = m_tickSpacing;
    }
    
}
