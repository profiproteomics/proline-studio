package fr.proline.studio.graphics;

import fr.proline.studio.graphics.marker.AbstractMarker;
import java.awt.Graphics2D;
import java.util.ArrayList;

/**
 *
 * @author JM235353
 */
public abstract class PlotAbstract {
    
        
    protected PlotPanel m_plotPanel;
    
    private ArrayList<AbstractMarker> m_markersList = null;

    public abstract double getXMin();
    public abstract double getXMax();
    public abstract double getYMin();
    public abstract double getYMax();

    public abstract boolean needsXAxis();
    public abstract boolean needsYAxis();
    
    public abstract void paint(Graphics2D g);

    public PlotAbstract(PlotPanel plotPanel) {
        m_plotPanel = plotPanel;
    }
    
    public void addMarker(AbstractMarker m) {
        if (m_markersList == null) {
            m_markersList = new ArrayList<>();
        }
        m_markersList.add(m);
    }
    
    public void clearMarkers() {
        if (m_markersList == null) {
            return;
        }
        m_markersList.clear();
    }
    
    public void paintMarkers(Graphics2D g) {
        if (m_markersList == null) {
            return;
        }
        for (int i=0;i<m_markersList.size();i++) {
            m_markersList.get(i).paint(g);
        }
    }
}
