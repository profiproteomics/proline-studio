package fr.proline.studio.graphics;

import java.awt.Color;
import java.util.HashMap;

/**
 * plot information: color, title,...
 * @author MB243701
 */
public class PlotInformation {
    
    private Color plotColor ;
    
    private String plotTitle;
    
    // draw the points for a linear plot
    private boolean isDrawPoints;
    
    // draw gap
    private boolean isDrawGap;
    
    // key value information, to be displayed in the tooltip
    private HashMap<String, String> plotInfo;

    public PlotInformation() {
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
    
}

