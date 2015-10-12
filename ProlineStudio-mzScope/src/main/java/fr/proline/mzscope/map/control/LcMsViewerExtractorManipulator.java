/**
 *
 */
package fr.proline.mzscope.map.control;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dialog.ModalityType;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;

import fr.proline.mzscope.map.LcMsMapChunk;
import fr.proline.mzscope.map.LcMsMapMzSpectrum;
import fr.proline.mzscope.map.LcMsMapRtSpectrum;
import fr.proline.mzscope.map.LcMsViewport;
import fr.proline.mzscope.map.ui.LcMsMapSpectrumPanel;
import fr.proline.mzscope.map.ui.LcMsViewerUI;
import fr.proline.studio.utils.IconManager;

/**
 * @author JeT
 *
 */
public class LcMsViewerExtractorManipulator extends AbstractLcMsViewerManipulator {

    private int clickX = -1;
    private int clickY = -1;
    private int currentX = -1;
    private int currentY = -1;
    private int clickWidth = -1;
    private int clickHeight = -1;
    private LcMsViewport clickViewport = null;
    private JToggleButton button = null;
    private Color overlayColor = new Color(0.8f, 0.8f, 1.0f, 0.2f);
    private Stroke selectionStroke = null;
    private ImageIcon spectrumIcon = IconManager.getIcon(IconManager.IconType.SPECTRUM);

    /**
     * @param controller
     */
    public LcMsViewerExtractorManipulator(LcMsViewerController controller) {
	super(controller);
	this.selectionStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 5.0f,
		new float[] { 5.0f }, 0.0f);
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
	    this.button.setIcon(this.spectrumIcon);
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
	    g.setColor(this.overlayColor);
	    g.fillRect(this.clickX, this.clickY, this.currentX - this.clickX, this.currentY - this.clickY);
	    g.setColor(Color.white);
	    g.setStroke(this.selectionStroke);
	    g.drawRect(this.clickX, this.clickY, this.currentX - this.clickX, this.currentY - this.clickY);
	}

    }

    /*
     * (non-Javadoc)
     *
     * @see java.awt.event.MouseWheelListener#mouseWheelMoved(java.awt.event.MouseWheelEvent)
     */
    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
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
	    Point2D p1 = this.getController().pixel2values(this.clickX, this.clickY);
	    Point2D p2 = this.getController().pixel2values(e.getX(), e.getY());
	    LcMsViewport viewport = new LcMsViewport(Math.min(p1.getX(), p2.getX()),
		    Math.max(p1.getX(), p2.getX()), Math.min(p1.getY(), p2.getY()),
		    Math.max(p1.getY(), p2.getY()));
	    JDialog subFrame = new JDialog(SwingUtilities.getWindowAncestor(this.getUIButton()),
		    "Spectrum extraction");
	    subFrame.setModalityType(ModalityType.MODELESS);
	    subFrame.setAlwaysOnTop(true);
	    JTabbedPane tabbedPane = new JTabbedPane();
	    subFrame.getContentPane().add(tabbedPane);
	    tabbedPane
		    .addTab("rt",
			    new LcMsMapSpectrumPanel(new LcMsMapRtSpectrum(
				    new LcMsMapChunk(this.getController().getViewer().getMap(), viewport),
				    2.)));
	    tabbedPane
		    .addTab("mz",
			    new LcMsMapSpectrumPanel(new LcMsMapMzSpectrum(
				    new LcMsMapChunk(this.getController().getViewer().getMap(), viewport),
				    .5)));
	    subFrame.setSize(600, 400);

	    subFrame.setVisible(true);
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
	    this.currentX = e.getX();
	    this.currentY = e.getY();
	    this.getDrawingPanel().repaint();
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
