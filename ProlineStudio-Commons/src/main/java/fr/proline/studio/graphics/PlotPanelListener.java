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
}
