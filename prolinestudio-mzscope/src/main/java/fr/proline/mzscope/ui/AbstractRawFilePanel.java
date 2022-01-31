/* 
 * Copyright (C) 2019
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
package fr.proline.mzscope.ui;

import fr.proline.mzscope.model.*;
import fr.proline.mzscope.utils.MzScopeCallback;
import fr.proline.mzscope.ui.model.MzScopePreferences;
import fr.proline.mzscope.utils.Display;
import fr.proline.mzscope.utils.KeyEventDispatcherDecorator;
import fr.proline.studio.export.ExportButton;
import fr.proline.studio.graphics.ExtendableButtonPanel;
import fr.proline.studio.graphics.marker.IntervalMarker;
import fr.proline.studio.utils.IconManager;
import fr.proline.studio.utils.StringUtils;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import javax.swing.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * base for raw file panel. The panel could be a SingleRawFilePanel or MultiRawFilePanel composed by 2 main components: ChromatogramPanel and
 * SpectrumPanel (if scan is displayed)
 *
 * @author CB205360
 */
public abstract class AbstractRawFilePanel extends JPanel implements IRawFileViewer, KeyEventDispatcher {

    final private static Logger logger = LoggerFactory.getLogger("ProlineStudio.mzScope.AbstractRawFilePanel");

    private boolean displayScan = true;
    private Display.Mode xicDisplayMode = Display.Mode.REPLACE;

    private JSplitPane splitPane;
    private JPanel mainPanel;
    private JPanel chromatogramContainerPanel;

    protected ChromatogramPanel chromatogramPanel;
    protected SpectrumPanel spectrumContainerPanel;
    protected JToolBar chromatogramToolbar;
    protected Spectrum currentScan;
    private JToggleButton showMS2EventsButton;
    private JToggleButton overlayBtn;
    private JLayeredPane m_layeredPane;

    protected IRawFileLoading rawFileLoading;

    public AbstractRawFilePanel() {
        super();
        init();
    }

    /**
     * If displayScan is true, display the chromatogram and the scan, otherwise display only the chromatogram
     *
     * @param displayScan
     */
    public void setDisplayScan(boolean displayScan) {
        this.displayScan = displayScan;
    }

    public void setRawFileLoading(IRawFileLoading rawFileLoading) {
        this.rawFileLoading = rawFileLoading;
    }

    private void init() {
        initComponents();
        spectrumContainerPanel.initComponents();
        KeyEventDispatcherDecorator.addKeyEventListener(this);
    }

    private void initComponents() {
        this.setLayout(new BorderLayout());

        m_layeredPane = new JLayeredPane();
        add(m_layeredPane, BorderLayout.CENTER);

        JPanel mainPanel = getMainPanel();
        m_layeredPane.add(mainPanel, JLayeredPane.DEFAULT_LAYER);
        m_layeredPane.addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent e) {
                final Component c = e.getComponent();
                mainPanel.setBounds(0, 0, c.getWidth(), c.getHeight());
                m_layeredPane.revalidate();
                m_layeredPane.repaint();
            }
        });
    }

    private JPanel getMainPanel() {
        if (mainPanel == null) {
            mainPanel = new JPanel();
            mainPanel.setName("mainPanel");
            mainPanel.setLayout(new BorderLayout());
            if (displayScan) {
                mainPanel.add(getSplitPane(), BorderLayout.CENTER);
            } else {
                mainPanel.add(getChromatogramContainerPanel(), BorderLayout.CENTER);
            }
        }
        return mainPanel;
    }

    private JSplitPane getSplitPane() {
        if (this.splitPane == null) {
            splitPane = new JSplitPane();
            splitPane.setDividerLocation(160);
            splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
            splitPane.setResizeWeight(0.5);
            splitPane.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            splitPane.setDoubleBuffered(true);
            splitPane.setOneTouchExpandable(true);
            splitPane.setTopComponent(getChromatogramContainerPanel());
            splitPane.setBottomComponent(getSpectrumContainerPanel());
        }
        return splitPane;
    }

    private JPanel getChromatogramContainerPanel() {
        if (this.chromatogramContainerPanel == null) {
            chromatogramContainerPanel = new JPanel();
            chromatogramContainerPanel.setName("chromatogramContainerPanel");
            chromatogramContainerPanel.setLayout(new BorderLayout());
            chromatogramContainerPanel.add(getChromatogramPanel(), BorderLayout.CENTER);
            chromatogramContainerPanel.add(getChromatogramToolbar(), BorderLayout.NORTH);
        }
        return chromatogramContainerPanel;
    }

    private ChromatogramPanel getChromatogramPanel() {
        if (this.chromatogramPanel == null) {
            chromatogramPanel = new ChromatogramPanel();
            chromatogramPanel.setName("chromatogramPanel");
            chromatogramPanel.addPropertyChangeListener(new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    displayScan(getCurrentRawfile().getSpectrumId((Float) evt.getNewValue()));
                }
            });
        }
        return chromatogramPanel;

    }

    protected JPanel getSpectrumContainerPanel() {
        if (this.spectrumContainerPanel == null) {
            spectrumContainerPanel = new SpectrumPanel(this);
            spectrumContainerPanel.setName("spectrumContainerPanel");
            spectrumContainerPanel.setLayout(new BorderLayout());
        }
        return spectrumContainerPanel;
    }

    private JToolBar getChromatogramToolbar() {
        chromatogramToolbar = new JToolBar(JToolBar.HORIZONTAL);
        chromatogramToolbar.setFloatable(false);
        ExportButton exportImageButton = new ExportButton("Graphic", chromatogramPanel.getChromatogramPlotPanel());
        chromatogramToolbar.add(exportImageButton);

        JButton ticBtn = new JButton(IconManager.getIcon(IconManager.IconType.TIC));
        ticBtn.setToolTipText("Display TIC Chromatogram");
        ticBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                displayTIC(-1);
            }
        });

        JButton displayTICbtn = new JButton(IconManager.getIcon(IconManager.IconType.TIC));
        displayTICbtn.setToolTipText("Display TIC Chromatogram");
        displayTICbtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                displayTIC(-1);
            }
        });

        JButton displayTICMS1btn = new JButton(IconManager.getIcon(IconManager.IconType.TIC_MS1));
        displayTICMS1btn.setToolTipText("Display MS1 TIC Chromatogram");
        displayTICMS1btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                displayTIC(1);
            }
        });

        ExtendableButtonPanel extendableButtonPanel = new ExtendableButtonPanel(ticBtn);
        m_layeredPane.add(extendableButtonPanel, JLayeredPane.PALETTE_LAYER);
        extendableButtonPanel.registerButton(displayTICbtn);
        extendableButtonPanel.registerButton(displayTICMS1btn);


        chromatogramToolbar.add(ticBtn);

        JButton displayBPIbtn = new JButton(IconManager.getIcon(IconManager.IconType.BPC));
        displayBPIbtn.setToolTipText("Display Base Peak Chromatogram");
        displayBPIbtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                displayBPI();
            }
        });
        chromatogramToolbar.add(displayBPIbtn);
        chromatogramToolbar.add(getShowMS2Button());
        setMsMsEventButtonEnabled(false);

        chromatogramToolbar.add(getOverlayBtn());
        chromatogramToolbar.setFloatable(false);
        chromatogramToolbar.setRollover(true);

        return chromatogramToolbar;
    }

    protected AbstractButton getShowMS2Button() {
        if (showMS2EventsButton == null) {
        showMS2EventsButton = new JToggleButton(IconManager.getIcon(IconManager.IconType.MS2), false);
        showMS2EventsButton.setToolTipText("Show or hide MS2 Events");
        showMS2EventsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                displayMsMsEvents(showMS2EventsButton.isSelected());
            }
        });
        }
        return showMS2EventsButton;
    }
    
    protected JToggleButton getOverlayBtn() {
        if (overlayBtn == null) {
            overlayBtn = new JToggleButton();
            overlayBtn.setIcon(IconManager.getIcon(IconManager.IconType.OVERLAY));
            overlayBtn.setSelected(false);
            overlayBtn.setName("cbXicOverlay");
            overlayBtn.setToolTipText("Overlay extracted chromatograms. This can also be done by using the Alt key");
            overlayBtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    xicDisplayMode = ((AbstractButton) e.getSource()).isSelected() ? Display.Mode.OVERLAY : Display.Mode.REPLACE;
                }
            });
        }
        return overlayBtn;
    }

    protected void setMsMsEventButtonEnabled(boolean b) {
        this.showMS2EventsButton.setEnabled(b);
        if (!b) {
            hideMSMSEvents();
        }
    }

    public Display.Mode getChromatogramDisplayMode() {
        return xicDisplayMode;
    }

    private void displayMsMsEvents(boolean showMsMsEvents) {
        if (showMsMsEvents) {
            showMSMSEvents();
        } else {
            hideMSMSEvents();
        }
    }

    public void showMSMSEvents() {
        if (chromatogramPanel.getCurrentChromatogram() == null) {
            return;
        }
        final double minMz = chromatogramPanel.getCurrentChromatogram().getMinMz();
        final double maxMz = chromatogramPanel.getCurrentChromatogram().getMaxMz();

        SwingWorker worker = new SwingWorker<List<Float>, Void>() {
            @Override
            protected List<Float> doInBackground() throws Exception {
                return getCurrentRawfile().getMsMsEvent(minMz, maxMz);
            }

            @Override
            protected void done() {
                try {
                    chromatogramPanel.showMSMSEvents(get());
                } catch (InterruptedException | ExecutionException e) {
                    logger.error("Error while reading chromatogram");
                }
            }
        };
        worker.execute();
    }

    public void hideMSMSEvents() {
        chromatogramPanel.hideMSMSEvents();
    }

    @Override
    public Color displayChromatogram(IChromatogram chromato, Display display) {
        setMsMsEventButtonEnabled(display.getMode() == Display.Mode.REPLACE);
        Color plotColor = chromatogramPanel.displayChromatogram(chromato, display);
        displayMsMsEvents(showMS2EventsButton.isSelected());
        return plotColor;
    }

    /**
     * Default implementation: display only the chromatogram corresponding this the current raw file.
     */
    @Override
    public void displayChromatograms(Map<IRawFile, IChromatogram> chromatogramByRawFile, Display display) {
        displayChromatogram(chromatogramByRawFile.get(getCurrentRawfile()), display);
    }

    @Override
    public void extractAndDisplayChromatogram(MsnExtractionRequest params, final Display display, final MzScopeCallback callback) {
        if (rawFileLoading != null) {
            rawFileLoading.setWaitingState(true);
        }
        SwingWorker worker = new AbstractMs1ExtractionWorker(getCurrentRawfile(), params) {
            @Override
            protected void done() {
                try {
                    IChromatogram c = get();
                    if (c != null) {
                        displayChromatogram(c, display);
                        setMsMsEventButtonEnabled(true);
                        if (callback != null) {
                            callback.callback(true);
                        }
                    } else {
                        String msg = StringUtils.formatString(params.toString(), 60);
                        JOptionPane.showMessageDialog(null, "The following extraction request did not produce any data: \n"+msg, "IChromatogram Extraction failure", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (InterruptedException | ExecutionException e) {
                    logger.error("Error while extraction chromatogram", e);
                    if (callback != null) {
                        callback.callback(false);
                    }
                } finally {
                    if (rawFileLoading != null) {
                        rawFileLoading.setWaitingState(false);
                    }
                }
            }
        };
        worker.execute();
    }

    @Override
    public void displayPeakel(final IPeakel f) {
        double ppm = MzScopePreferences.getInstance().getMzPPMTolerance();
        final MsnExtractionRequest.Builder builder = MsnExtractionRequest.builder().setMzTolPPM((float) ppm);

        if (f.getMsLevel() == 1) {
            builder.setMz(f.getMz());
        } else {
            builder.setMz(f.getParentMz());
            ppm = MzScopePreferences.getInstance().getFragmentMzPPMTolerance();
            builder.setFragmentMzTolPPM((float) ppm);
            builder.setFragmentMz(f.getMz());
        }

        // TODO : made this configurable un feature panel : extract around peakel rt or full time range
        //builder.setElutionTimeLowerBound(f.getBasePeakel().getFirstElutionTime()-5*60).setElutionTimeUpperBound(f.getBasePeakel().getLastElutionTime()+5*60);
        if (rawFileLoading != null) {
            rawFileLoading.setWaitingState(true);
        }
        SwingWorker worker = new SwingWorker<IChromatogram, Void>() {
            @Override
            protected IChromatogram doInBackground() throws Exception {
                return getCurrentRawfile().getXIC(builder.build());
            }

            @Override
            protected void done() {
                try {
                    displayChromatogram(get(), new Display(getChromatogramDisplayMode()));
                    chromatogramPanel.displayFeature(f, new Display(Collections.singletonList(
                        new IntervalMarker(null, Color.ORANGE, Color.RED, f.getFirstElutionTime() / 60.0, f.getLastElutionTime() / 60.0))));
                    displayScan(getCurrentRawfile().getSpectrumId(f.getElutionTime()));
                    if (rawFileLoading != null) {
                        rawFileLoading.setWaitingState(false);
                    }
                } catch (InterruptedException ex) {
                    logger.error("Error while extraction feature chromatogram", ex);
                } catch (ExecutionException ex) {
                    logger.error("Error while extraction feature chromatogram", ex);
                }
            }
        };
        worker.execute();
    }

    @Override
    public void displayScan(long index) {
        if ((currentScan == null) || (index != currentScan.getIndex())) {
            if (rawFileLoading != null) {
                rawFileLoading.setWaitingState(true);
            }
            currentScan = getCurrentRawfile().getSpectrum((int) index);
            if (currentScan != null) {
                chromatogramPanel.setCurrentScanTime(currentScan.getRetentionTime());
                if (displayScan) {
                    spectrumContainerPanel.displayScan(currentScan);
                    for (IChromatogram chromato : chromatogramPanel.getChromatograms()) {
                        if ((chromato.getMinMz() != -1) && (chromato.getMaxMz() != -1) && (currentScan.getMsLevel() == 1)) {
                            spectrumContainerPanel.addMarkerRange(chromato.getMinMz(), chromato.getMaxMz());
                        }
                    }

                }
            }
            if (rawFileLoading != null) {
                rawFileLoading.setWaitingState(false);
            }
        }
    }

    @Override
    public void setReferenceSpectrum(Spectrum spectrum) {
        spectrumContainerPanel.displayReferenceSpectrum(spectrum);
    }

    @Override
    public IChromatogram getCurrentChromatogram() {
        return chromatogramPanel.getCurrentChromatogram();
    }

    @Override
    public Iterable<IChromatogram> getAllChromatograms() {
        return chromatogramPanel.getChromatograms();
    }
    
    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        if (e.isConsumed() || e.getID() != KeyEvent.KEY_PRESSED) {
            return false;
        }
        if (currentScan == null) {
            e.consume();
            return true;
        }
        if ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0) {
            if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                displayScan(getCurrentRawfile().getPreviousSpectrumId(currentScan.getIndex(), currentScan.getMsLevel()));
                e.consume();
                return true;
            } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                displayScan(getCurrentRawfile().getNextSpectrumId(currentScan.getIndex(), currentScan.getMsLevel()));
                e.consume();
                return true;
            }

        } else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            displayScan(spectrumContainerPanel.getPreviousScanIndex(currentScan.getIndex()));
            e.consume();
            return true;
        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            displayScan(spectrumContainerPanel.getNextScanIndex(currentScan.getIndex()));
            e.consume();
            return true;
        }
        return false;
    }

    protected abstract void displayTIC(int msLevel);

    protected abstract void displayBPI();

}
