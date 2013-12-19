package fr.proline.studio.graphics.marker;

import fr.proline.studio.graphics.PlotPanel;
import java.awt.Graphics2D;

/**
 * Base class for all markers which can be plot in a PlotPanel
 * @author JM235353
 */
public abstract class AbstractMarker {
    
    protected PlotPanel m_plotPanel;
    
    public AbstractMarker(PlotPanel plotPanel) {
        m_plotPanel = plotPanel;
    }
    
    public abstract void paint(Graphics2D g);
}
