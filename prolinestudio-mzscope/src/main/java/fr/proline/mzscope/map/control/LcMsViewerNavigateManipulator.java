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

import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;

import javax.swing.ImageIcon;
import javax.swing.JToggleButton;

import fr.proline.mzscope.map.LcMsViewport;
import fr.proline.mzscope.map.ui.LcMsViewerUI;
import fr.proline.studio.utils.IconManager;

/**
 * @author JeT
 *
 */
public class LcMsViewerNavigateManipulator extends AbstractLcMsViewerManipulator {

    private int clickX = -1;
    private int clickY = -1;
    private int clickWidth = -1;
    private int clickHeight = -1;
    private LcMsViewport clickViewport = null;
    private JToggleButton button = null;
    private ImageIcon navigateIcon = IconManager.getIcon(IconManager.IconType.NAVIGATE);
    private static final Cursor handCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
    private static final Cursor moveCursor = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);

    /**
     * @param controller
     */
    public LcMsViewerNavigateManipulator(LcMsViewerController controller) {
	super(controller);
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.profi.mzscope.map.control.LcMsViewerManipulator#getUIButton()
     */
    @Override
    public JToggleButton getUIButton() {
	if (this.button == null) {
	    this.button = new JToggleButton();
	    this.button.setIcon(this.navigateIcon);
	}
	return this.button;

    }

    /*
     * (non-Javadoc)
     *
     * @see fr.profi.mzscope.map.control.LcMsViewerManipulator#paintComponent(java.awt.Graphics2D)
     */
    @Override
    public void paintComponent(Graphics2D g) {
	if (this.isInteractive()) {
	    this.getDrawingPanel().setCursor(moveCursor);
	} else {
	    this.getDrawingPanel().setCursor(handCursor);
	}

    }

    /*
     * (non-Javadoc)
     *
     * @see java.awt.event.MouseWheelListener#mouseWheelMoved(java.awt.event.MouseWheelEvent)
     */
    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
	double wheel = e.getPreciseWheelRotation();
	double scale = 1. + (wheel / 5.);
	Point2D centerValues = this.getController().getUI().getDisplayViewport().pixel2value(e.getX(),
		e.getY(), this.getController().getUI().getCanvas().getWidth(),
		this.getController().getUI().getCanvas().getHeight());
	this.getController().getUI().setDisplayViewport(this.getController().getUI().getDisplayViewport()
		.centerScale(centerValues.getX(), centerValues.getY(), scale, scale));
	this.getController().changeViewport(this.getController().getUI().getDisplayViewport());

    }

    /*
     * (non-Javadoc)
     *
     * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseClicked(MouseEvent e) {

    }

    /*
     * (non-Javadoc)
     *
     * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
     */
    @Override
    public void mousePressed(MouseEvent e) {
	this.clickViewport = new LcMsViewport(this.getController().getUI().getDisplayViewport());
	this.clickX = e.getX();
	this.clickY = e.getY();
	this.clickWidth = this.getController().getUI().getCanvas().getWidth();
	this.clickHeight = this.getController().getUI().getCanvas().getHeight();
	this.setInteractive(true);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseReleased(MouseEvent e) {
	if (this.clickViewport != null) {
	    this.clickViewport = null;
	    this.setInteractive(false);
	    this.getController().changeViewport(this.getController().getUI().getDisplayViewport());
	}
    }

    /*
     * (non-Javadoc)
     *
     * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseEntered(MouseEvent e) {
	// TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     *
     * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseExited(MouseEvent e) {
	// TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     *
     * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseDragged(MouseEvent e) {
	if (this.clickViewport != null) {
	    int x = e.getX();
	    int y = e.getY();
	    double tRt = ((double) (this.clickX - x) / this.clickWidth)
		    * (this.clickViewport.maxRt - this.clickViewport.minRt);
	    double tMz = ((double) (this.clickY - y) / this.clickHeight)
		    * (this.clickViewport.maxMz - this.clickViewport.minMz);

	    this.getController().getUI().setDisplayViewport(this.clickViewport.translate(tMz, tRt));
	}

    }

    /*
     * (non-Javadoc)
     *
     * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseMoved(MouseEvent e) {
	this.getController().getUI().updateMouseInformationUI(e.getX(), e.getY());
    }

}
