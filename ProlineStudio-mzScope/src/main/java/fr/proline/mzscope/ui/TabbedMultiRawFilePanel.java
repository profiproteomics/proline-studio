package fr.proline.mzscope.ui;

import fr.proline.mzscope.model.Chromatogram;
import fr.proline.mzscope.model.IFeature;
import fr.proline.mzscope.model.IRawFile;
import fr.proline.mzscope.model.Ms1ExtractionRequest;
import fr.proline.mzscope.model.MzScopeCallback;
import fr.proline.mzscope.model.Spectrum;
import fr.proline.mzscope.utils.ButtonTabComponent;
import fr.proline.mzscope.utils.MzScopeConstants.DisplayMode;
import fr.proline.studio.utils.CyclicColorPalette;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author CB205360
 */
public class TabbedMultiRawFilePanel extends JPanel implements IRawFilePanel {

    final private static Logger logger = LoggerFactory.getLogger(TabbedMultiRawFilePanel.class);

    private JSplitPane splitPane;
    private JTabbedPane chromatogramContainerPanel;
    private ChromatogramPanel currentChromatogramPanel;
    protected SpectrumPanel spectrumContainerPanel;
    protected Spectrum currentScan;

    private final List<IRawFile> rawfiles;
    private final Map<IRawFile, Chromatogram> mapChromatogramForRawFile;
    
    private final Map<IRawFile, IRawFileLoading> mapRawFileLoading;
    
    
    public TabbedMultiRawFilePanel(List<IRawFile> rawfiles) {
        super();
        this.rawfiles = rawfiles;
        mapChromatogramForRawFile = new HashMap();
        mapRawFileLoading = new HashMap();
        for (IRawFile rawFile : rawfiles) {
            mapChromatogramForRawFile.put(rawFile, null);
        }
        initComponents();
        spectrumContainerPanel.initChart();
        displayTIC();
    }

    private void initComponents() {
        this.setLayout(new BorderLayout());
        this.add(getSplitPane(), BorderLayout.CENTER);
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

    private JTabbedPane getChromatogramContainerPanel() {
        if (this.chromatogramContainerPanel == null) {
            chromatogramContainerPanel = new JTabbedPane();
            chromatogramContainerPanel.setName("chromatogramContainerPanel");
            for (IRawFile rawFile : rawfiles) {
                ButtonTabComponent buttonTabComp = new ButtonTabComponent(rawFile.getName());
                IRawFileLoading rawFileLoading = (boolean waitingState) -> {
                    buttonTabComp.setWaitingState(waitingState);
                };
                mapRawFileLoading.put(rawFile, rawFileLoading);
                buttonTabComp.addCloseTabListener(new ButtonTabComponent.CloseTabListener() {

                    @Override
                    public void closeTab(ButtonTabComponent buttonTabComponent) {
                        int index = chromatogramContainerPanel.indexOfTabComponent(buttonTabComponent);
                        if (index != -1) {
                            chromatogramContainerPanel.remove(index);
                        }
                    }
                    
                });
                chromatogramContainerPanel.addTab(rawFile.getName(), getChromatogramPanel(rawFile));
                int i = chromatogramContainerPanel.getTabCount() - 1;
                chromatogramContainerPanel.setTabComponentAt(i, buttonTabComp);
            }
        }
        chromatogramContainerPanel.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                currentChromatogramPanel = (ChromatogramPanel) chromatogramContainerPanel.getComponentAt(chromatogramContainerPanel.getSelectedIndex());
            }
        });
        if (chromatogramContainerPanel.getTabCount() > 0) {
            chromatogramContainerPanel.setSelectedIndex(0);
        }
        return chromatogramContainerPanel;
    }

    private ChromatogramPanel getChromatogramPanel(final IRawFile rawFile) {
        ChromatogramPanel chromatogramPanel = new ChromatogramPanel();
        chromatogramPanel.setName("chromatogramPanel");
        chromatogramPanel.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                currentChromatogramPanel = (ChromatogramPanel) evt.getSource();
                displayScan(rawFile.getSpectrumId((Float) evt.getNewValue()));
            }
        });
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

    @Override
    public IRawFile getCurrentRawfile() {
        Chromatogram c = getCurrentChromatogram();
        if (c != null) {
            for (Map.Entry<IRawFile, Chromatogram> entrySet : mapChromatogramForRawFile.entrySet()) {
                if (entrySet.getValue().equals(c)) {
                    return entrySet.getKey();
                }
            }
        }
        return null;
    }

    @Override
    public void extractAndDisplayChromatogram(final Ms1ExtractionRequest params, DisplayMode mode, MzScopeCallback callback) {
        // in this implementation displayMode is ignored : always REPLACE since we will extract one Chromatogram per RawFile
        SwingWorker worker = new SwingWorker<Integer, Chromatogram>() {
            int count = 0;

            @Override
            protected Integer doInBackground() throws Exception {

                for (IRawFile rawFile : rawfiles) {
                    Chromatogram c = rawFile.getXIC(params);
                    count++;
                    publish(c);
                }
                return count;
            }

            @Override
            protected void process(List<Chromatogram> chunks) {
                Chromatogram c = chunks.get(chunks.size() - 1);
                if (count == 1) {
                    logger.info("display first chromato");
                    displayChromatogram(c, DisplayMode.REPLACE);
                } else {
                    logger.info("add additionnal chromato");
                    displayChromatogram(c, DisplayMode.OVERLAY);
                }
            }

            @Override
            protected void done() {
                try {
                    logger.info("{} TIC chromatogram extracted", get());
                } catch (InterruptedException | ExecutionException e) {
                    logger.error("Error while reading chromatogram");
                }
            }
        };

        worker.execute();
    }

    @Override
    public Color displayChromatogram(Chromatogram chromato, DisplayMode mode) {
        String rawFileName = chromato.rawFilename;
        int nbTab = chromatogramContainerPanel.getTabCount();
        Color c = null;
        for (int t = 0; t < nbTab; t++) {
            String tabTitle = chromatogramContainerPanel.getTitleAt(t);
            if (tabTitle.equals(rawFileName)) {  // see how to better get the tabComponent linked to a rawFile
                ChromatogramPanel chromatoPanel = (ChromatogramPanel) chromatogramContainerPanel.getComponentAt(t);
                c = chromatoPanel.displayChromatogram(chromato, mode);
                mapChromatogramForRawFile.put(getCurrentRawfile(), chromato);
            }
        }
        return c;
    }

    // override display feature to display all xic
    @Override
    public void displayFeature(final IFeature f) {
        // TODO : how to handle this simple display of a unique feature ? In this case multiple Features must be displayed, on by RawFile
    }

    @Override
    public void displayScan(long index) {
        if ((currentScan == null) || (index != currentScan.getIndex()) && getCurrentRawfile() != null) {
            currentScan = getCurrentRawfile().getSpectrum((int) index);
            if (currentScan != null) {
                spectrumContainerPanel.displayScan(currentScan);
            }
        }
    }

    private IRawFile getRawFile(String fileName) {
        for (IRawFile rawFile : rawfiles) {
            if (rawFile.getName().equals(fileName)) {
                return rawFile;
            }
        }
        return null;
    }

    @Override
    public Color getPlotColor(String rawFilename) {
        return CyclicColorPalette.getColor(1);
    }

    @Override
    public Chromatogram getCurrentChromatogram() {
        return (currentChromatogramPanel == null) ? null : currentChromatogramPanel.getCurrentChromatogram();
    }

    private void displayTIC() {

        SwingWorker worker = new SwingWorker<Integer, Chromatogram>() {
            int count = 0;

            @Override
            protected Integer doInBackground() throws Exception {
                    
                for (IRawFile rawFile : rawfiles) {
                    mapRawFileLoading.get(rawFile).setWaitingState(true);
                    Chromatogram c = rawFile.getTIC();
                    count++;
                    publish(c);
                }
                return count;
            }

            @Override
            protected void process(List<Chromatogram> chunks) {
                for (int k = 0; k < chunks.size(); k++) {
                    logger.info("display  chromato");
                    displayChromatogram(chunks.get(k), DisplayMode.REPLACE);
                }
            }

            @Override
            protected void done() {
                try {
                    logger.info("{} TIC chromatogram extracted", get());
                } catch (InterruptedException | ExecutionException e) {
                    logger.error("Error while reading chromatogram");
                }finally{
                    for (IRawFile rawFile : rawfiles) {
                        mapRawFileLoading.get(rawFile).setWaitingState(false);
                    }
                }
            }
        };

        worker.execute();
    }

}
