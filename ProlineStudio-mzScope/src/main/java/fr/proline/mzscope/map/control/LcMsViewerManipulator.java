/**
 *
 */
package fr.proline.mzscope.map.control;

import java.awt.Graphics2D;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;

import javax.swing.JPanel;
import javax.swing.JToggleButton;

/**
 * @author JeT
 *
 */
public interface LcMsViewerManipulator extends MouseMotionListener, MouseListener, MouseWheelListener {

    /**
     * @return the controller
     */
    LcMsViewerController getController();

    /**
     * return true if the manipulator is in 'interactive' state : Oftenly used when the user drag the mouse
     *
     * @return
     */
    boolean isInteractive();

    /**
     * Get the panel associated with this manipulator
     *
     * @return
     */
    JPanel getDrawingPanel();

    /**
     * Paint manipulator
     *
     * @param g
     */
    void paintComponent(Graphics2D g);

    JToggleButton getUIButton();

}