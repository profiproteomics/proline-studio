package fr.proline.mzscope.ui;

import fr.proline.mzscope.model.Chromatogram;
import fr.proline.mzscope.model.IFeature;
import fr.proline.mzscope.model.MsnExtractionRequest;
import fr.proline.mzscope.utils.MzScopeCallback;
import fr.proline.mzscope.ui.model.MzScopePreferences;
import fr.proline.mzscope.model.Spectrum;
import fr.proline.mzscope.utils.KeyEventDispatcherDecorator;
import fr.proline.mzscope.utils.MzScopeConstants.DisplayMode;
import fr.proline.studio.export.ExportButton;
import fr.proline.studio.utils.IconManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.KeyEventDispatcher;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingWorker;
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
    private DisplayMode xicModeDisplay = DisplayMode.REPLACE;

    private JSplitPane splitPane;
    private JPanel mainPanel;
    private JPanel chromatogramContainerPanel;

    protected ChromatogramPanel chromatogramPanel;
    protected SpectrumPanel spectrumContainerPanel;
    protected JToolBar chromatogramToolbar;
    protected Spectrum currentScan;
    private JToggleButton showMS2EventsButton;
    private JToggleButton overlayBtn;

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
        this.add(getMainPanel(), BorderLayout.CENTER);
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

    private JPanel getSpectrumContainerPanel() {
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

        JButton displayTICbtn = new JButton("TIC");
        displayTICbtn.setToolTipText("Display TIC Chromatogram");
        displayTICbtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                displayTIC();
            }
        });
        chromatogramToolbar.add(displayTICbtn);

        JButton displayBPIbtn = new JButton("BPC");
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
        showMS2EventsButton = new JToggleButton("MS2", false);
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
                    xicModeDisplay = ((AbstractButton) e.getSource()).isSelected() ? DisplayMode.OVERLAY : DisplayMode.REPLACE;
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

    public DisplayMode getXicModeDisplay() {
        return xicModeDisplay;
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
        final double minMz = chromatogramPanel.getCurrentChromatogram().minMz;
        final double maxMz = chromatogramPanel.getCurrentChromatogram().maxMz;

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
    public Color displayChromatogram(Chromatogram chromato, DisplayMode mode) {
        if (mode == DisplayMode.REPLACE) {
            setMsMsEventButtonEnabled(true);
        }
        Color plotColor = chromatogramPanel.displayChromatogram(chromato, mode);
        displayMsMsEvents(showMS2EventsButton.isSelected());
        return plotColor;
    }

    @Override
    public void extractAndDisplayChromatogram(MsnExtractionRequest params, final DisplayMode mode, final MzScopeCallback callback) {
        if (rawFileLoading != null) {
            rawFileLoading.setWaitingState(true);
        }
        SwingWorker worker = new AbstractMs1ExtractionWorker(getCurrentRawfile(), params) {
            @Override
            protected void done() {
                try {
                    Chromatogram c = get();
                    if (c != null) {
                        displayChromatogram(c, mode);
                        setMsMsEventButtonEnabled(true);
                        if (callback != null) {
                            callback.callback(true);
                        }
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
    public void displayFeature(final IFeature f) {
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
        SwingWorker worker = new SwingWorker<Chromatogram, Void>() {
            @Override
            protected Chromatogram doInBackground() throws Exception {
                return getCurrentRawfile().getXIC(builder.build());
            }

            @Override
            protected void done() {
                try {
                    displayChromatogram(get(), getXicModeDisplay());
                    chromatogramPanel.displayFeature(f);
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
                    for (Chromatogram chromato : chromatogramPanel.getChromatograms()) {
                        if ((chromato.minMz != -1) && (chromato.maxMz != -1) && (currentScan.getMsLevel() == 1)) {
                            spectrumContainerPanel.addMarkerRange(chromato.minMz, chromato.maxMz);
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
    public Chromatogram getCurrentChromatogram() {
        return chromatogramPanel.getCurrentChromatogram();
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

    protected abstract void displayTIC();

    protected abstract void displayBPI();

}
