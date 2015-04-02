package fr.proline.studio.graphics.marker;

import fr.proline.studio.graphics.PlotPanel;
import java.awt.Graphics2D;

/**
 * Base class for all markers which can be plot in a PlotPanel
 * @author JM235353
 */
public abstract class AbstractMarker {
    
    protected PlotPanel m_plotPanel;
    protected boolean m_isVisible;
    
    public AbstractMarker(PlotPanel plotPanel) {
        m_plotPanel = plotPanel;
    }

   public boolean isVisible() {
      return m_isVisible;
   }

   public void setVisible(boolean m_isVisible) {
      this.m_isVisible = m_isVisible;
   }
    
    public abstract void paint(Graphics2D g);
}
