package fr.proline.studio.graphics;

import java.util.HashSet;

/**
 *
 * @author JM235353
 */
public enum PlotType {

    HISTOGRAM_PLOT("Histogram", "on", null),
    SCATTER_PLOT("Scatter Plot", "X Axis:", "Y Axis:");
    
    public static PlotType[] ALL_PLOTS = { HISTOGRAM_PLOT, SCATTER_PLOT };
    
    private PlotType(/*int index,*/ String name, String xLabel, String yLabel) {
        //m_index = index;
        m_name = name;
        m_xLabel = xLabel;
        m_yLabel = yLabel;
    }
    
    //private final int m_index;
    private final String m_name;
    private final String m_xLabel;
    private final String m_yLabel;
    
    public boolean needsX() {
        return m_xLabel != null;
    }
    public boolean needsY() {
        return m_yLabel != null;
    }
    
    public String getXLabel() {
        return m_xLabel;
    }
    
    public String getYLabel() {
        return m_yLabel;
    }
    
    @Override
    public String toString() {
        return m_name;
    }
    
    public HashSet<Class> getAcceptedXValues() {
        
        HashSet<Class> acceptedValues = new HashSet(2);
        
        switch (this) {
            case HISTOGRAM_PLOT: 
                acceptedValues.add(Double.class);
                acceptedValues.add(Float.class);
                acceptedValues.add(Integer.class);
            case SCATTER_PLOT: 
                acceptedValues.add(Double.class);
                acceptedValues.add(Float.class);
                acceptedValues.add(Integer.class);
        }
        return acceptedValues;
    }
    
    
}
