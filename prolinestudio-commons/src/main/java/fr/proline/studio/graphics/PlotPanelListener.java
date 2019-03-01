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
    
    /**
     * update on the X and Y axis, coming from the zooming gesture or pan gesture
     * 
     * @param oldX contains oldMinX and oldMaxX
     * @param newX contains newMinX and newMaxX
     * @param oldY contains oldMinY and oldMaxY
     * @param newY contains newMinY and newMaxY
     */
    public void updateAxisRange(double[] oldX, double[] newX,  double[] oldY,  double[] newY);
        
}
