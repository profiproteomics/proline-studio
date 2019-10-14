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

import java.awt.Color;
import java.util.HashMap;

/**
 * a data class which contains the information for the plot: color, title,...
 * @author MB243701
 */
public class PlotInformation {
    
    private Color plotColor ;
    
    private String plotTitle;
    
    private boolean m_isSelected;
    
    // draw the points for a linear plot
    private boolean isDrawPoints;
    
    // draw gap
    private boolean isDrawGap;
    
    // key value information, to be displayed in the tooltip
    private HashMap<String, String> plotInfo;
    
    private boolean _hasDataSpec;

    public PlotInformation() {
        _hasDataSpec = false;
    }

    public boolean isHasDataSpec() {
        return _hasDataSpec;
    }

    public void setHasDataSpec(boolean hasDataSpec) {
        this._hasDataSpec = hasDataSpec;
    }

    public Color getPlotColor() {
        return plotColor;
    }

    public void setPlotColor(Color plotColor) {
        this.plotColor = plotColor;
    }

    public String getPlotTitle() {
        return plotTitle;
    }

    public void setPlotTitle(String plotTitle) {
        this.plotTitle = plotTitle;
    }

    public HashMap<String, String> getPlotInfo() {
        return plotInfo;
    }

    public void setPlotInfo(HashMap<String, String> plotInfo) {
        this.plotInfo = plotInfo;
    }
    
    /**
     * if draw a pint at the PlotLinear point
     * @return 
     */
    public boolean isDrawPoints() {
        return this.isDrawPoints;
    }
    /**
     * if draw a pint at the PlotLinear point
     * @return 
     */
    public void setDrawPoints(boolean drawPoints) {
        this.isDrawPoints = drawPoints ;
    }
    
    public boolean isDrawGap() {
        return this.isDrawGap;
    }
    
    public void setDrawGap(boolean drawGap) {
        this.isDrawGap = drawGap ;
    }
    
    public boolean isSelected(){
        return this.m_isSelected;
    }
    public void setSelected(boolean option){
        this.m_isSelected = option;
    }
}

