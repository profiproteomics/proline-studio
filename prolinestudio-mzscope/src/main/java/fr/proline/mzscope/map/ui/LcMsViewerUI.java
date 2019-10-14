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
package fr.proline.mzscope.map.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import fr.profi.mzdb.util.concurrent.Callback;
import fr.proline.mzscope.map.LcMsMapChunk;
import fr.proline.mzscope.map.LcMsViewer;
import fr.proline.mzscope.map.LcMsViewerEvent;
import fr.proline.mzscope.map.LcMsViewerListener;
import fr.proline.mzscope.map.LcMsViewport;
import fr.proline.mzscope.map.color.IntensityFirePainter;
import fr.proline.mzscope.map.color.IntensityPainter;
import fr.proline.mzscope.map.control.AbstractLcMsViewerManipulator;
import fr.proline.mzscope.map.control.LcMsViewerController;
import fr.proline.mzscope.map.control.LcMsViewerExtractorManipulator;
import fr.proline.mzscope.map.control.LcMsViewerManipulator;
import fr.proline.mzscope.map.control.LcMsViewerNavigateManipulator;
import fr.proline.mzscope.map.control.LcMsViewerSelectionManipulator;
import fr.proline.mzscope.math.Function1D;
import fr.proline.mzscope.math.Function1DComposite;
import fr.proline.mzscope.math.Function1DLinear;
import fr.proline.mzscope.math.Function1DLog;
import fr.proline.studio.utils.IconManager;

/**
 * @author JeT
 *
 */
public class LcMsViewerUI implements LcMsViewerListener, ComponentListener, ChangeListener, KeyListener,
	Callback<LcMsMapChunk> {
    private static final Insets INSETS = new Insets(2, 2, 2, 2);
    private static final int PADDING = 2;
    private static final int MAX_LUMINOSITY_FACTOR = +100;
    private static final NumberFormat viewportFormatter = new DecimalFormat("##0.####");
    private static final double MIN_MZ_ZOOM = 1;
    private static final double MIN_RT_ZOOM = 1;

    private LcMsViewer viewer = null;
    private LcMsViewerController controller = null;
    private LcMsMapChunk currentChunk = null;
    private JPanel root = null;
    private JToolBar toolbar = null;
    private JTextField minRtTextField = null;
    private JTextField maxRtTextField = null;
    private JTextField minMzTextField = null;
    private JTextField maxMzTextField = null;
    private List<LcMsViewerManipulator> manipulators = new ArrayList<LcMsViewerManipulator>();
    private IntensityPainter painter = new IntensityFirePainter();

    private JSlider luminosityFactorSlider = null;
    private JLabel updateLabel = null;
    private JLabel mouseInformationLabel = null;
    private LcMsViewport displayViewport = null;
    private JLcMsMapPanel canvas = null;
    private Function1D selectedFunction = null;

    private Action reinitViewportAction = null;
    private Action updateImageAction = null;
    private ImageIcon loadingIcon = IconManager.getIcon(IconManager.IconType.PROGRESS);
    private ImageIcon fullscreenIcon = IconManager.getIcon(IconManager.IconType.FULL_SCREEN);
    private ImageIcon updateIcon = IconManager.getIcon(IconManager.IconType.UPDATE);

    /**
     * Default constructor
     *
     * @param viewer
     */
    public LcMsViewerUI() {
	super();
	this.controller = new LcMsViewerController();
	this.controller.setUI(this);
	// Toolkit.getDefaultToolkit().setDynamicLayout(false);
	this.manipulators.add(new LcMsViewerNavigateManipulator(this.controller));
	this.manipulators.add(new LcMsViewerSelectionManipulator(this.controller));
	this.manipulators.add(new LcMsViewerExtractorManipulator(this.controller));
    }

    /**
     * @param viewer
     */
    public LcMsViewerUI(LcMsViewer viewer) {
	this();
	this.setViewer(viewer);
    }

    /**
     * @return the viewer
     */
    public LcMsViewer getViewer() {
	return this.viewer;
    }

    /**
     * @return the controller
     */
    public LcMsViewerController getController() {
	return this.controller;
    }

    /**
     * @param viewer
     *            the viewer to set
     */
    public void setViewer(LcMsViewer viewer) {
	if (this.viewer != null) {
	    this.viewer.removeLcMsViewerListener(this);
	}
	this.viewer = viewer;
	if (this.viewer != null) {
	    this.controller.setViewer(viewer);
	    this.viewer.addLcMsViewerListener(this);
	    // this.setDisplayViewport(this.viewer.getMapViewport());
	    // this.updateUI();
	}
    }

    /**
     * Change all User Interface elements with viewer content
     */
    private void updateUI() {
	if (this.getViewer() == null) {
	    return;
	}
	this.updateViewportUI();
	this.updateProgressUI();
	if (this.getViewer().getMap() == null) {
	    return;
	}
	// request viewer to compute a new image. When image is ready viewer will call onCompletion callback
	this.getViewer().requestNewImage(this.getCanvas().getWidth(), this.getCanvas().getHeight(),
		this.getLuminosityFunction(), new LcMsViewport(this.getDisplayViewport()), this,
		this.painter);
    }

    /**
     * @return the painter
     */
    public IntensityPainter getPainter() {
	return this.painter;
    }

    /**
     * @return the currentChunk
     */
    public LcMsMapChunk getChunk() {
	return this.currentChunk;
    }

    /**
     * @param currentChunk
     *            the currentChunk to set
     */
    public void setChunk(LcMsMapChunk currentChunk) {
	this.currentChunk = currentChunk;
	this.getCanvas().setChunk(currentChunk);
    }

    /**
     * @param result
     */
    private void displayChunk() {
	// display viewport limits
	this.updateViewportUI();
	// repaint canvas
	this.getCanvas().repaint();
    }

    /**
     * @return the displayViewport
     */
    public LcMsViewport getDisplayViewport() {
	return this.displayViewport;
    }

    /**
     * @param translate
     */
    public void setDisplayViewport(LcMsViewport viewport) {
	this.displayViewport = this.clampViewport(viewport);
	this.getCanvas().setViewport(this.displayViewport);
	this.displayChunk();
    }

    /**
     * Return a clamped Viewport depending on min/max zoom and max viewport
     *
     * @param viewport
     * @return
     */
    public LcMsViewport clampViewport(LcMsViewport viewport) {
	LcMsViewport clampedViewport = new LcMsViewport(viewport);
	clampedViewport.minMz = Math.max(clampedViewport.minMz, this.getViewer().getMapViewport().minMz);
	clampedViewport.minRt = Math.max(clampedViewport.minRt, this.getViewer().getMapViewport().minRt);
	clampedViewport.maxMz = Math.min(clampedViewport.maxMz, this.getViewer().getMapViewport().maxMz);
	clampedViewport.maxRt = Math.min(clampedViewport.maxRt, this.getViewer().getMapViewport().maxRt);
	if ((clampedViewport.maxMz - clampedViewport.minMz) < MIN_MZ_ZOOM) {
	    clampedViewport.minMz = ((clampedViewport.maxMz + clampedViewport.minMz) / 2.)
		    - (MIN_MZ_ZOOM / 2);
	    clampedViewport.maxMz = ((clampedViewport.maxMz + clampedViewport.minMz) / 2.)
		    + (MIN_MZ_ZOOM / 2);
	}
	if ((clampedViewport.maxRt - clampedViewport.minRt) < MIN_RT_ZOOM) {
	    clampedViewport.minRt = ((clampedViewport.maxRt + clampedViewport.minRt) / 2.)
		    - (MIN_RT_ZOOM / 2);
	    clampedViewport.maxRt = ((clampedViewport.maxRt + clampedViewport.minRt) / 2.)
		    + (MIN_RT_ZOOM / 2);
	}
	return clampedViewport;
    }

    /**
     * update values associated with mouse informations
     */
    public void updateMouseInformationUI(final int x, final int y) {
	if (this.getDisplayViewport() == null) {
	    return;
	}
	if (this.getCanvas() == null) {
	    return;
	}
	Point2D cursorValues = this.getDisplayViewport().pixel2value(x, y, this.getCanvas().getWidth(),
		this.getCanvas().getHeight());
	final double rt = cursorValues.getY();
	final double mz = cursorValues.getX();

	SwingUtilities.invokeLater(new Runnable() {

	    @Override
	    public void run() {

		LcMsViewerUI.this.getMouseInformationLabel()
			.setText("pixel: " + x + "x" + y + " on " + LcMsViewerUI.this.getCanvas().getWidth()
				+ "x" + LcMsViewerUI.this.getCanvas().getHeight() + "\n" + "rt: " + rt
				+ " m/z:" + mz);
	    }
	});
    }

    /**
     * update the animated 'computation in progress' icon
     */
    private void updateProgressUI() {
	SwingUtilities.invokeLater(new Runnable() {

	    @Override
	    public void run() {
		LcMsViewerUI.this.getUpdateLabel()
			.setVisible(LcMsViewerUI.this.getViewer().getRunningTaskCount() != 0);
	    }
	});
    }

    /**
     * update min & max values for Rt (Retention Time) and Mz (mass)
     */
    private void updateViewportUI() {
	if (this.getViewer() == null) {
	    return;
	}
	final LcMsViewport viewport = this.getDisplayViewport();
	SwingUtilities.invokeLater(new Runnable() {

	    @Override
	    public void run() {
		if (viewport == null) {
		    LcMsViewerUI.this.getMinMzTextField().setText("-");
		    LcMsViewerUI.this.getMaxMzTextField().setText("-");
		    LcMsViewerUI.this.getMinRtTextField().setText("-");
		    LcMsViewerUI.this.getMaxRtTextField().setText("-");
		    return;
		}
		LcMsViewerUI.this.getMinMzTextField().setText(viewportFormatter.format(viewport.minMz));
		LcMsViewerUI.this.getMaxMzTextField().setText(viewportFormatter.format(viewport.maxMz));
		LcMsViewerUI.this.getMinRtTextField().setText(viewportFormatter.format(viewport.minRt));
		LcMsViewerUI.this.getMaxRtTextField().setText(viewportFormatter.format(viewport.maxRt));
	    }
	});
    }

    /**
     * Get Graphics Interface
     *
     * @return
     */
    public JComponent getGui() {
	return this.getRoot();
    }

    /**
     * root panel
     */
    private JPanel getRoot() {
	if (this.root == null) {
	    this.root = new JPanel(new BorderLayout());
	    this.root.add(this.getViewPanel(), BorderLayout.CENTER);
	    this.root.add(this.getToolPanel(), BorderLayout.EAST);
	    this.root.add(this.getInfoPanel(), BorderLayout.SOUTH);
	    this.root.addComponentListener(this);

	}
	return this.root;
    }

    private Component getViewPanel() {
	JPanel panel = new JPanel(new GridBagLayout());
	panel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
	panel.add(this.getUpdateLabel(), Util.dc(0, 0, 1, 1, 0f, 0f));
	panel.add(this.getMinRtTextField(), Util.dc(1, 0, 1, 1, 0f, 0f));
	panel.add(this.getToolbar(), Util.dc(2, 0, 1, 1, 1f, 0f));
	panel.add(this.getMaxRtTextField(), Util.dc(3, 0, 1, 1, 0f, 0f));
	panel.add(this.getMinMzTextField(), Util.dc(0, 1, 1, 1, 0f, 0f));
	panel.add(Box.createVerticalGlue(), Util.dc(0, 2, 1, 1, 0f, 1f));
	panel.add(this.getMaxMzTextField(), Util.dc(0, 3, 1, 1, 0f, 0f));
	panel.add(this.getCanvas(), Util.dc(1, 1, 3, 3, 1f, 1f));
	return panel;
    }

    private Component getToolPanel() {
	JPanel panel = new JPanel(new GridBagLayout());
	panel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
	panel.add(this.getLuminosityFactorSlider(), Util.dc(0, 0, 1, 1, 0f, 1f));
	final JComboBox<Function1D> logScaleComboBox = new JComboBox<Function1D>();
	this.selectedFunction = new Function1DLinear(1, 0);
	logScaleComboBox.addItem(this.selectedFunction);
	logScaleComboBox.addItem(new Function1DComposite(new Function1DLog(10.), new Function1DLinear(1, 1)));
	logScaleComboBox.addItem(new Function1DComposite(new Function1DLog(2), new Function1DLinear(1, 1)));
	panel.add(logScaleComboBox, Util.dc(0, 1, 1, 1, 0f, 0f));
	logScaleComboBox.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		LcMsViewerUI.this.selectedFunction = (Function1D) logScaleComboBox.getSelectedItem();
		LcMsViewerUI.this.getController().updateImageColor(LcMsViewerUI.this.getDisplayViewport());
	    }
	});
	return panel;
    }

    private Component getInfoPanel() {
	JPanel panel = new JPanel(new GridBagLayout());
	panel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
	panel.add(this.getMouseInformationLabel(), Util.dc(0, 0, 1, 1, 1f, 0f));
	return panel;
    }

    /**
     * lazy getter of LuminosityFactor
     *
     * @return the existing JSlider or create/store a new one
     */
    public JSlider getLuminosityFactorSlider() {
	if (this.luminosityFactorSlider == null) {
	    this.luminosityFactorSlider = new JSlider();
	    this.luminosityFactorSlider.setOrientation(SwingConstants.VERTICAL);
	    this.luminosityFactorSlider.setMinimum(-MAX_LUMINOSITY_FACTOR);
	    this.luminosityFactorSlider.setMaximum(MAX_LUMINOSITY_FACTOR);
	    this.luminosityFactorSlider.setValue(0);
	    this.luminosityFactorSlider.setMajorTickSpacing(100);
	    this.luminosityFactorSlider.setMinorTickSpacing(10);
	    this.luminosityFactorSlider.setPaintTicks(true);
	    this.luminosityFactorSlider.addChangeListener(new ChangeListener() {

		@Override
		public void stateChanged(ChangeEvent e) {
		    LcMsViewerUI.this.getController()
			    .updateImageColor(LcMsViewerUI.this.getDisplayViewport());
		}
	    });

	}
	return this.luminosityFactorSlider;
    }

    public double getLuminosityFactor() {
	return 1.0 + Math.tan((1.57 * this.getLuminosityFactorSlider().getValue()) / MAX_LUMINOSITY_FACTOR);
    }

    public Function1D getSelectedFunction() {
	return this.selectedFunction;
    }

    public Function1D getLuminosityFunction() {
	return new Function1DComposite(new Function1DLinear(this.getLuminosityFactor(), 0),
		this.getSelectedFunction());
    }

    /**
     * lazy getter of Toolbar
     *
     * @return the existing JToolBar or create/store a new one
     */
    private JToolBar getToolbar() {
	if (this.toolbar == null) {
	    this.toolbar = new JToolBar();
	    this.toolbar.setFloatable(false);
	    this.toolbar.setRollover(true);
	    this.toolbar.setOrientation(SwingConstants.HORIZONTAL);
	    JButton initButton = new JButton();
	    initButton.setAction(this.getInitViewportAction());
	    initButton.setIcon(this.fullscreenIcon);
	    this.toolbar.add(initButton);
	    JButton updateButton = new JButton();
	    updateButton.setAction(this.getUpdateViewportAction());
	    updateButton.setIcon(this.updateIcon);
	    this.toolbar.add(updateButton);

	    ButtonGroup group = new ButtonGroup();
	    for (final LcMsViewerManipulator manipulator : this.manipulators) {
		group.add(manipulator.getUIButton());
		this.toolbar.add(manipulator.getUIButton());
		((AbstractLcMsViewerManipulator) manipulator).setDrawingPanel(this.getCanvas());
		manipulator.getUIButton().addChangeListener(new ChangeListener() {

		    @Override
		    public void stateChanged(ChangeEvent e) {
			if (manipulator.getUIButton().isSelected()) {
			    LcMsViewerUI.this.getController().setManipulator(manipulator);
			    LcMsViewerUI.this.getCanvas().repaint();
			}
		    }
		});
	    }
	    this.manipulators.get(0).getUIButton().setSelected(true);

	}
	return this.toolbar;
    }

    /**
     * get the current selected manipulator
     *
     * @return
     */
    public LcMsViewerManipulator getCurrentManipulator() {
	for (final LcMsViewerManipulator manipulator : this.manipulators) {
	    if (manipulator.getUIButton().isSelected()) {
		return manipulator;
	    }
	}
	return null;

    }

    /**
     * MinRtLabel
     */
    private JTextField getMinRtTextField() {
	if (this.minRtTextField == null) {
	    this.minRtTextField = new JTextField("0000.0000");
	    this.minRtTextField.setPreferredSize(new Dimension(80, 25));
	    this.minRtTextField.addKeyListener(this);
	}
	return this.minRtTextField;
    }

    /**
     * lazy getter of MaxRtLabel
     *
     * @return the existing JLAbel or create and store a new one
     */
    private JTextField getMaxRtTextField() {
	if (this.maxRtTextField == null) {
	    this.maxRtTextField = new JTextField("0000.0000");
	    this.maxRtTextField.setPreferredSize(new Dimension(80, 25));
	    this.maxRtTextField.addKeyListener(this);
	}
	return this.maxRtTextField;
    }

    /**
     * lazy getter of minMzLabel
     *
     * @return the existing JLabel or create and store a new one
     */
    private JTextField getMinMzTextField() {
	if (this.minMzTextField == null) {
	    this.minMzTextField = new JTextField("0000.0000");
	    this.minMzTextField.setPreferredSize(new Dimension(80, 25));
	    this.minMzTextField.addKeyListener(this);
	}
	return this.minMzTextField;
    }

    /**
     * lazy getter of MaxMzLabel
     *
     * @return the existing JLabel or create and store a new one
     */
    private JTextField getMaxMzTextField() {
	if (this.maxMzTextField == null) {
	    this.maxMzTextField = new JTextField("0000.0000");
	    this.maxMzTextField.setPreferredSize(new Dimension(80, 25));
	    this.maxMzTextField.addKeyListener(this);
	}
	return this.maxMzTextField;
    }

    /**
     * lazy getter of Canvas
     *
     * @return the existing JLabel or create and store a new one
     */
    public JLcMsMapPanel getCanvas() {
	if (this.canvas == null) {
	    this.canvas = new JLcMsMapPanel(this);
	    this.canvas.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
	    this.canvas.addMouseListener(this.controller);
	    this.canvas.addMouseMotionListener(this.controller);
	    this.canvas.addMouseWheelListener(this.controller);
	}
	return this.canvas;
    }

    /**
     * lazy getter of UpdateIcon
     *
     * @return the existing JLabel or create and store a new one
     */
    private JLabel getUpdateLabel() {
	if (this.updateLabel == null) {
	    this.updateLabel = new JLabel(this.loadingIcon);
	    this.updateLabel.setVisible(false);

	}
	return this.updateLabel;
    }

    /**
     * lazy getter of Mouse information label
     *
     * @return the existing JLabel or create and store a new one
     */
    private JLabel getMouseInformationLabel() {
	if (this.mouseInformationLabel == null) {
	    this.mouseInformationLabel = new JLabel("no mouse information");
	}
	return this.mouseInformationLabel;
    }

    public static class Util {
	/**
	 * create a new default constraint
	 *
	 * @param gridx
	 * @param gridy
	 * @param gridwidth
	 * @param gridheight
	 * @param weightx
	 * @param weighty
	 * @return
	 */
	private static GridBagConstraints dc(int gridx, int gridy, int gridwidth, int gridheight,
		float weightx, float weighty) {
	    return new GridBagConstraints(gridx, gridy, gridwidth, gridheight, weightx, weighty,
		    GridBagConstraints.CENTER, GridBagConstraints.BOTH, INSETS, PADDING, PADDING);
	}

    }

    /*
     * (non-Javadoc)
     *
     * @see fr.profi.mzscope.LcMsViewerListener#onLcMsViewerEvent(fr.profi.mzscope.LcMsViewerEvent)
     */
    @Override
    public void onLcMsViewerEvent(LcMsViewerEvent e) {
	switch (e.getType()) {
	case MAP_CHANGED:
	    this.viewerMapChanged();
	    break;
	case VIEWPORT_CHANGED:
	    this.viewerViewportChanged();
	    break;
	case IMAGE_PROCESS_ADDED:
	    // updateProgressUI not called. sometimes jobs have not been started at this point
	    LcMsViewerUI.this.getUpdateLabel().setVisible(true);
	    break;
	default:
	    break;
	}
	throw new UnsupportedOperationException("event not handled");

    }

    /**
     *
     */
    private void viewerViewportChanged() {
	throw new UnsupportedOperationException("viewport changed. Update view");
    }

    /**
     *
     */
    private void viewerMapChanged() {
	throw new UnsupportedOperationException("map changed. Update view");
    }

    /*
     * (non-Javadoc)
     *
     * @see java.awt.event.ComponentListener#componentResized(java.awt.event.ComponentEvent)
     */
    @Override
    public void componentResized(ComponentEvent e) {
	if (e.getComponent() == this.getRoot()) {
	    this.updateUI();
	}
    }

    /*
     * (non-Javadoc)
     *
     * @see java.awt.event.ComponentListener#componentMoved(java.awt.event.ComponentEvent)
     */
    @Override
    public void componentMoved(ComponentEvent e) {
	// nothing to do
    }

    /*
     * (non-Javadoc)
     *
     * @see java.awt.event.ComponentListener#componentShown(java.awt.event.ComponentEvent)
     */
    @Override
    public void componentShown(ComponentEvent e) {
	// nothing to do
    }

    /*
     * (non-Javadoc)
     *
     * @see java.awt.event.ComponentListener#componentHidden(java.awt.event.ComponentEvent)
     */
    @Override
    public void componentHidden(ComponentEvent e) {
	// nothing to do
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
     */
    @Override
    public void stateChanged(ChangeEvent e) {
    }

    /*
     * (non-Javadoc)
     *
     * @see fr.profi.mzdb.util.future.FutureCallback#onCompletion(java.lang.Object)
     */
    /**
     * @return the currentChunk
     */
    public LcMsMapChunk getCurrentChunk() {
	return this.currentChunk;
    }

    @Override
    public void onCompletion(LcMsMapChunk chunk) {
	if (chunk != null) {
	    this.updateProgressUI();
	    if ((this.getCurrentChunk() != null) && (this.getCurrentChunk().getCreationTime()
		    .getTime() > chunk.getCreationTime().getTime())) {
		// if received chunk is older than the current one, just forget it...
		return;
	    }
	    this.setChunk(chunk);
	    this.displayChunk();
	}
    }

    /*
     * (non-Javadoc)
     *
     * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
     */
    @Override
    public void keyTyped(KeyEvent e) {
	// TODO Auto-generated method stub
    }

    /*
     * (non-Javadoc)
     *
     * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
     */
    @Override
    public void keyPressed(KeyEvent e) {
	if ((e.getSource() == this.getMinMzTextField()) || (e.getSource() == this.getMaxMzTextField())
		|| (e.getSource() == this.getMinRtTextField())
		|| (e.getSource() == this.getMaxRtTextField())) {
	    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
		System.out.println("update view");
		double minMz, maxMz, minRt, maxRt;
		try {
		    minMz = this.parseDouble(this.getMinMzTextField().getText());
		} catch (NumberFormatException e1) {
		    minMz = this.getDisplayViewport().minMz;
		    this.logError(e1.getMessage());
		}
		try {
		    maxMz = this.parseDouble(this.getMaxMzTextField().getText());
		} catch (NumberFormatException e1) {
		    maxMz = this.getDisplayViewport().maxMz;
		    this.logError(e1.getMessage());
		}
		try {
		    minRt = this.parseDouble(this.getMinRtTextField().getText());
		} catch (NumberFormatException e1) {
		    minRt = this.getDisplayViewport().minRt;
		    this.logError(e1.getMessage());
		}
		try {
		    maxRt = this.parseDouble(this.getMaxRtTextField().getText());
		} catch (NumberFormatException e1) {
		    maxRt = this.getDisplayViewport().maxRt;
		    this.logError(e1.getMessage());
		}
		this.getController().changeViewport(minMz, maxMz, minRt, maxRt);
	    }
	}
    }

    /**
     * @param message
     */
    private void logError(String message) {
	System.err.println(message);

    }

    private double parseDouble(final String str) throws NumberFormatException {
	try {
	    NumberFormat format = NumberFormat.getInstance(Locale.FRANCE);
	    return format.parse(str).doubleValue();
	} catch (ParseException e) {
	    e.printStackTrace();
	}
	try {
	    NumberFormat format = NumberFormat.getInstance(Locale.ENGLISH);
	    return format.parse(str).doubleValue();
	} catch (ParseException e) {
	    e.printStackTrace();
	}
	throw new NumberFormatException("String '" + str + "' is not a valid number");
    }

    /*
     * (non-Javadoc)
     *
     * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
     */
    @Override
    public void keyReleased(KeyEvent e) {
	// TODO Auto-generated method stub

    }

    /**
     * @return
     */
    @SuppressWarnings("serial")
    private Action getInitViewportAction() {
	if (this.reinitViewportAction == null) {
	    this.reinitViewportAction = new AbstractAction() {

		@Override
		public void actionPerformed(ActionEvent e) {
		    LcMsViewerUI.this.getController().reinitViewport();

		}
	    };
	}
	return this.reinitViewportAction;
    }

    /**
     * @return
     */
    @SuppressWarnings("serial")
    private Action getUpdateViewportAction() {
	if (this.updateImageAction == null) {
	    this.updateImageAction = new AbstractAction() {

		@Override
		public void actionPerformed(ActionEvent e) {
		    LcMsViewerUI.this.getController().updateViewport();

		}
	    };
	}
	return this.updateImageAction;
    }

}
