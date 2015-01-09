package fr.proline.studio.graphics;

import java.util.ArrayList;
import java.util.HashSet;

/**
 *
 * @author JM235353
 */
public enum PlotType {

    HISTOGRAM_PLOT("Histogram", "on", null, "as"),
    SCATTER_PLOT("Scatter Plot", "X Axis:", "Y Axis:", null);
    
    public static PlotType[] ALL_PLOTS = { HISTOGRAM_PLOT, SCATTER_PLOT };
    
    private PlotType(/*int index,*/ String name, String xLabel, String yLabel, String zLabel) {
        //m_index = index;
        m_name = name;
        m_xLabel = xLabel;
        m_yLabel = yLabel;
        m_zLabel = zLabel;
    }
    
    //private final int m_index;
    private final String m_name;
    private final String m_xLabel;
    private final String m_yLabel;
    private final String m_zLabel;
    
    public boolean needsX() {
        return m_xLabel != null;
    }
    public boolean needsY() {
        return m_yLabel != null;
    }
    public boolean needsZ() {
        return m_zLabel != null;
    }
    
    public String getXLabel() {
        return m_xLabel;
    }
    
    public String getYLabel() {
        return m_yLabel;
    }
    
    public String getZLabel() {
        return m_zLabel;
    }
    
    @Override
    public String toString() {
        return m_name;
    }
    
    public HashSet<Class> getAcceptedXValues() {
        
        HashSet<Class> acceptedValues = new HashSet(3);
        
        switch (this) {
            case HISTOGRAM_PLOT: 
                acceptedValues.add(Double.class);
                acceptedValues.add(Float.class);
                acceptedValues.add(Integer.class);
                break;
            case SCATTER_PLOT: 
                acceptedValues.add(Double.class);
                acceptedValues.add(Float.class);
                acceptedValues.add(Integer.class);
                break;
        }
        return acceptedValues;
    }
    
    public HashSet<Class> getAcceptedValuesAsParam() {
        
        HashSet<Class> acceptedValues = new HashSet(2);
        
        switch (this) {
            case HISTOGRAM_PLOT: 
                break;
            case SCATTER_PLOT: 
                acceptedValues.add(Double.class);
                acceptedValues.add(Float.class);
                acceptedValues.add(Integer.class);
                break;
        }
        return acceptedValues;
    }
    
    public ArrayList<String> getZValues() {
        switch (this) {
            case HISTOGRAM_PLOT: { 
                ArrayList<String> zParams = new ArrayList<>();
                zParams.add(PlotHistogram.HISTOGRAM_PERCENTAGE);
                zParams.add(PlotHistogram.HISTOGRAM_COUNT);
                return zParams;
            }
            case SCATTER_PLOT:
            default:
                return null;
        }
    }
    
    
}
