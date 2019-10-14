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