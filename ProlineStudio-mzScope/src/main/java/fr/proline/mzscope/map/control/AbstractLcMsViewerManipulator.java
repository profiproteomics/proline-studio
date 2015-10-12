/**
 *
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