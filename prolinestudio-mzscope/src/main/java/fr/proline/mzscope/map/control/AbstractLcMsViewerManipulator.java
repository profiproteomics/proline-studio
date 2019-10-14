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

import javax.swing.JPanel;

/**
 * @author JeT
 *
 */
public abstract class AbstractLcMsViewerManipulator implements LcMsViewerManipulator {

    private LcMsViewerController controller = null;
    private boolean interactive = false;
    private JPanel drawingPanel = null;

    /**
     *
     */
    public AbstractLcMsViewerManipulator(LcMsViewerController controller) {
	super();
	this.setController(controller);
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.profi.mzscope.map.control.LcMsViewerManipulator#getController()
     */
    @Override
    public LcMsViewerController getController() {
	return this.controller;
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.profi.mzscope.map.control.LcMsViewerManipulator#getDrawingPanel()
     */
    @Override
    public JPanel getDrawingPanel() {
	return this.drawingPanel;
    }

    /**
     * @param drawingPanel
     *            the drawingPanel to set
     */
    public void setDrawingPanel(JPanel drawingPanel) {
	this.drawingPanel = drawingPanel;
    }

    /**
     * @param controller
     *            the controller to set
     */
    public final void setController(LcMsViewerController controller) {
	this.controller = controller;
    }

    /**
     * @return the interactive
     */
    @Override
    public boolean isInteractive() {
	return this.interactive;
    }

    /**
     * @param interactive
     *            the interactive to set
     */
    protected void setInteractive(boolean interactive) {
	this.interactive = interactive;
    }

}