/**
 *
 */
package fr.proline.mzscope.map.control;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;

import fr.proline.mzscope.map.LcMsViewer;
import fr.proline.mzscope.map.LcMsViewport;
import fr.proline.mzscope.map.ui.LcMsViewerUI;

/**
 * @author JeT Modify model 'LcMsViewer' reacting to [user] inputs
 */
public class LcMsViewerController implements MouseMotionListener, MouseListener, MouseWheelListener {

    private LcMsViewer viewer = null;
    private LcMsViewerUI ui = null;
    private LcMsViewerManipulator manipulator = null;

    /**
     * @param viewer
     * @param ui
     */
    public LcMsViewerController() {
	super();
	this.setManipulator(new LcMsViewerSelectionManipulator(this));
    }

    /**
     * @return the viewer
     */
    public LcMsViewer getViewer() {
	return this.viewer;
    }

    /**
     * @param viewer
     *            the viewer to set
     */
    public void setViewer(LcMsViewer viewer) {
	this.viewer = viewer;
    }

    /**
     * @return the ui
     */
    public LcMsViewerUI getUI() {
	return this.ui;
    }

    /**
     * @param ui
     *            the ui to set
     */
    public void setUI(LcMsViewerUI ui) {
	this.ui = ui;
    }

    /**
     * @return the manipulator
     */
    public LcMsViewerManipulator getManipulator() {
	return this.manipulator;
    }

    /**
     * @param manipulator
     *            the manipulator to set
     */
    public void setManipulator(LcMsViewerManipulator manipulator) {
	this.manipulator = manipulator;
    }

    public Point2D pixel2values(double x, double y) {
	return this.getUI().getDisplayViewport().pixel2value(x, y, this.getUI().getCanvas().getWidth(),
		this.getUI().getCanvas().getHeight());
    }

    public Point2D values2pixel(double mz, double rt) {
	return this.getUI().getDisplayViewport().value2pixel(mz, rt, this.getUI().getCanvas().getWidth(),
		this.getUI().getCanvas().getHeight());
    }

    /**
     * Request the UI to display given Viewport
     *
     * @param viewport
     */
    public void changeViewport(double minMz, double maxMz, double minRt, double maxRt) {
	this.changeViewport(new LcMsViewport(minMz, maxMz, minRt, maxRt));
    }

    /**
     * Request the UI to display given Viewport
     *
     * @param viewport
     */
    public void reinitViewport() {
	this.changeViewport(this.getViewer().getMapViewport());
    }

    /**
     *
     */
    public void updateViewport() {
	this.changeViewport(this.getUI().getDisplayViewport());
    }

    /**
     * Request Viewer to compute an image with the given viewport and ask UI to display this Viewport
     *
     * @param viewport
     */
    public void changeViewport(LcMsViewport viewport) {
	// request model a new map viewport
	this.getViewer().requestNewImage(this.getUI().getCanvas().getWidth(),
		this.getUI().getCanvas().getHeight(), this.getUI().getLuminosityFunction(), viewport,
		this.getUI(), this.getUI().getPainter());
	// change the UI to fit the requested viewport
	this.getUI().setDisplayViewport(viewport);
    }

    /**
     * Request Viewer to compute an image with the given viewport and ask UI to display this Viewport
     *
     * @param viewport
     */
    public void updateImageColor(LcMsViewport viewport) {
	// request model a new map viewport
	this.getViewer().requestUpdateImageColor(this.getUI().getCanvas().getWidth(),
		this.getUI().getCanvas().getHeight(), this.getUI().getLuminosityFunction(), viewport,
		this.getUI(), this.getUI().getPainter());
	// change the UI to fit the requested viewport
	this.getUI().setDisplayViewport(viewport);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.awt.event.MouseWheelListener#mouseWheelMoved(java.awt.event.MouseWheelEvent)
     */
    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
	if (this.manipulator != null) {
	    this.manipulator.mouseWheelMoved(e);
	}
    }

    /*
     * (non-Javadoc)
     *
     * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseClicked(MouseEvent e) {
	if (this.manipulator != null) {
	    this.manipulator.mouseClicked(e);
	}
    }

    /*
     * (non-Javadoc)
     *
     * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
     */
    @Override
    public void mousePressed(MouseEvent e) {
	if (this.manipulator != null) {
	    this.manipulator.mousePressed(e);
	}
    }

    /*
     * (non-Javadoc)
     *
     * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseReleased(MouseEvent e) {
	if (this.manipulator != null) {
	    this.manipulator.mouseReleased(e);
	}
    }

    /*
     * (non-Javadoc)
     *
     * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseEntered(MouseEvent e) {
	if (this.manipulator != null) {
	    this.manipulator.mouseEntered(e);
	}
    }

    /*
     * (non-Javadoc)
     *
     * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseExited(MouseEvent e) {
	if (this.manipulator != null) {
	    this.manipulator.mouseExited(e);
	}
    }

    /*
     * (non-Javadoc)
     *
     * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseDragged(MouseEvent e) {
	if (this.manipulator != null) {
	    this.manipulator.mouseDragged(e);
	}
    }

    /*
     * (non-Javadoc)
     *
     * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseMoved(MouseEvent e) {
	if (this.manipulator != null) {
	    this.manipulator.mouseMoved(e);
	}
    }

}
