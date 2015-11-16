package fr.proline.mzscope.ui;

import fr.proline.mzscope.utils.ButtonTabComponent;
import com.google.common.base.Strings;
import fr.profi.mzdb.io.writer.mgf.PrecursorMzComputation;
import fr.profi.mzdb.model.Feature;
import fr.proline.mzscope.map.LcMsMap;
import fr.proline.mzscope.map.LcMsViewer;
import fr.proline.mzscope.map.LcMsViewport;
import fr.proline.mzscope.map.ui.LcMsViewerUI;
import fr.proline.mzscope.model.BaseFeature;
import fr.proline.mzscope.model.Chromatogram;
import fr.proline.mzscope.model.FeaturesExtractionRequest;
import fr.proline.mzscope.model.IFeature;
import fr.proline.mzscope.model.IRawFile;
import fr.proline.mzscope.model.Ms1ExtractionRequest;
import fr.proline.mzscope.model.MzScopeCallback;
import fr.proline.mzscope.model.MzScopePreferences;
import fr.proline.mzscope.ui.dialog.ExportMGFDialog;
import fr.proline.mzscope.ui.dialog.ExtractionParamsDialog;
import fr.proline.mzscope.ui.event.ExtractionEvent;
import fr.proline.mzscope.ui.event.ExtractionStateListener;
import fr.proline.mzscope.utils.MzScopeConstants;
import fr.proline.mzscope.utils.MzScopeConstants.DisplayMode;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.gui.DefaultDialog.ProgressTask;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * main panel for mzscope
 *
 * @author MB243701
 */
public class MzScopePanel extends JPanel implements IFeatureViewer, IExtractionExecutor, IExtractionResults {

    private final static Logger logger = LoggerFactory.getLogger("ProlineStudio.mzScope.MzScopePanel");

    private Frame parentFrame = null;
    private JSplitPane mainRightSplitPane = null;
    private JSplitPane mainLeftSplitPane = null;
    private JTabbedPane viewersTabPane = null;
    private JTabbedPane featuresTabPane = null;
    private boolean m_extractResultPanel;
    private JSplitPane generalSplitPane = null;

    private IRawFilePanel selectedRawFilePanel;
    private XICExtractionPanel extractionPanel = null;
    private EventListenerList listenerList = new EventListenerList();
    private Map<IRawFile, List<AbstractRawFilePanel>> mapRawFilePanelRawFile;

    public MzScopePanel(Frame parentFrame, boolean extractResultPanel) {
        this.parentFrame = parentFrame;
        this.m_extractResultPanel = extractResultPanel;
        initComponents();
    }

    private void initComponents() {
        mapRawFilePanelRawFile = new HashMap<>();
        setLayout(new BorderLayout());
        if (m_extractResultPanel) {
            this.add(getGeneralSplitPane(), BorderLayout.CENTER);
        } else {
            this.add(getExtractionPanel(), BorderLayout.NORTH);
            this.add(getMainRightComponent(), BorderLayout.CENTER);
        }
    }

    private JSplitPane getGeneralSplitPane() {
        if (this.generalSplitPane == null) {
            this.generalSplitPane = new JSplitPane();
            this.generalSplitPane.setDividerLocation(120);
            this.generalSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
            this.generalSplitPane.setOneTouchExpandable(true);
            this.generalSplitPane.setRightComponent(getMainRightComponent());
            this.generalSplitPane.setLeftComponent(getMainLeftComponent());
        }

        return this.generalSplitPane;
    }

    private JSplitPane getMainLeftComponent() {
        if (this.mainLeftSplitPane == null) {
            this.mainLeftSplitPane = new JSplitPane();
            this.mainLeftSplitPane.setDividerLocation(250);
            this.mainLeftSplitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
            this.mainLeftSplitPane.setOneTouchExpandable(true);
            this.mainLeftSplitPane.setRightComponent(new ExtractionResultsPanel(this, ExtractionResultsPanel.TOOLBAR_ALIGN_VERTICAL));
            this.mainLeftSplitPane.setLeftComponent(getExtractionPanel());
        }

        return this.mainLeftSplitPane;
    }

    private XICExtractionPanel getExtractionPanel() {
        if (extractionPanel == null) {
            extractionPanel = new XICExtractionPanel(this);
        }
        return extractionPanel;
    }

    private JSplitPane getMainRightComponent() {
        if (this.mainRightSplitPane == null) {
            this.mainRightSplitPane = new JSplitPane();
            this.mainRightSplitPane.setDividerLocation(320);
            this.mainRightSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
            this.mainRightSplitPane.setOneTouchExpandable(true);
            this.mainRightSplitPane.setRightComponent(getFeaturesTabPane());
            this.mainRightSplitPane.setLeftComponent(getViewersTabPane());
        }

        return this.mainRightSplitPane;
    }

    private JTabbedPane getFeaturesTabPane() {
        if (this.featuresTabPane == null) {
            this.featuresTabPane = new JTabbedPane();
        }
        return this.featuresTabPane;
    }

    private JTabbedPane getViewersTabPane() {
        if (this.viewersTabPane == null) {
            viewersTabPane = new JTabbedPane();
            viewersTabPane.addChangeListener(
                    new ChangeListener() {
                        @Override
                        public void stateChanged(ChangeEvent evt) {
                            viewersTabPaneStateChanged(evt);
                        }
                    });
        }
        return this.viewersTabPane;
    }

    private void viewersTabPaneStateChanged(ChangeEvent evt) {
        this.selectedRawFilePanel = (IRawFilePanel) viewersTabPane.getSelectedComponent();
        if (selectedRawFilePanel != null && selectedRawFilePanel.getCurrentRawfile() != null){
            getExtractionPanel().setDIAEnabled(selectedRawFilePanel.getCurrentRawfile().isDIAFile());
        }
    }

    public Chromatogram getCurrentChromatogram() {
        return selectedRawFilePanel.getCurrentChromatogram();
    }

    public void openRaw(List<File> files) {
        List<IRawFile> rawfiles = new ArrayList();
        for (File f : files) {
            IRawFile rawfile = RawFileManager.getInstance().getFile(f.getName());
            if (rawfile == null) {
                rawfile = RawFileManager.getInstance().addRawFile(f);
            }
            List<AbstractRawFilePanel> list = mapRawFilePanelRawFile.get(rawfile);
            if (list != null) {
                boolean fileAlreadyOpen = false;
                for (AbstractRawFilePanel p : list) {
                    if (p instanceof SingleRawFilePanel) {
                        fileAlreadyOpen = true;
                        break;
                    }
                }
                if (!fileAlreadyOpen) {
                    rawfiles.add(rawfile);
                }
            } else {
                rawfiles.add(rawfile);
            }
        }
        displayRawAction(rawfiles);
    }

    public void openRawAndExtract(File file, final double moz, final double elutionTime, final double firstScanTime, final double lastScanTime) {
        IRawFile rawfile = RawFileManager.getInstance().getFile(file.getName());
        if (rawfile == null) {
            rawfile = RawFileManager.getInstance().addRawFile(file);
        }
        boolean fileAlreadyOpen = false;
        List<AbstractRawFilePanel> list = mapRawFilePanelRawFile.get(rawfile);
        if (list != null) {
            for (AbstractRawFilePanel p : list) {
                if (p instanceof SingleRawFilePanel) {
                    fileAlreadyOpen = true;
                    break;
                }
            }
        }

        if (!fileAlreadyOpen) {
            displayRawAction(rawfile, false);
        }
        Ms1ExtractionRequest params = Ms1ExtractionRequest.builder().setMzTolPPM(MzScopePreferences.getInstance().getMzPPMTolerance()).setMz(moz).build();
        list = mapRawFilePanelRawFile.get(rawfile);
        for (final AbstractRawFilePanel p : list) {
            if (p instanceof SingleRawFilePanel) {
                p.extractAndDisplayChromatogram(params, MzScopeConstants.DisplayMode.REPLACE, new MzScopeCallback() {
                    @Override
                    public void callback(boolean success) {
                        p.displayFeature(new BaseFeature(moz, (float) elutionTime, (float) firstScanTime, (float) lastScanTime));
                    }
                });
                // display elutionTime, firstScanTime, lastScanTime
                break;
            }
        }
    }

    private ButtonTabComponent addRawTab(String s, Component c) {
        ButtonTabComponent buttonTabComp = addTab(viewersTabPane, s, c, null);
        viewersTabPane.setSelectedComponent(c);
        return buttonTabComp;
    }

    private ButtonTabComponent addFeatureTab(String s, Component c, String tooltip) {
        ButtonTabComponent buttonTabComp = addTab(featuresTabPane, s, c, tooltip);
        featuresTabPane.setSelectedComponent(c);
        return buttonTabComp;
    }

    private ButtonTabComponent addTab(final JTabbedPane tabPane, String s, final Component c, String tooltip) {
        tabPane.add(s, c);
        int i = tabPane.getTabCount() - 1;
        ButtonTabComponent buttonTabComp = new ButtonTabComponent(s);
        buttonTabComp.addCloseTabListener(new ButtonTabComponent.CloseTabListener() {
            @Override
            public void closeTab(ButtonTabComponent tabComponent) {
                int index = tabPane.indexOfTabComponent(tabComponent);
                if (index != -1) {
                    tabPane.remove(index);
                    if (c instanceof SingleRawFilePanel) {
                        SingleRawFilePanel p = (SingleRawFilePanel) c;
                        for (Map.Entry<IRawFile, List<AbstractRawFilePanel>> entrySet : mapRawFilePanelRawFile.entrySet()) {
                            IRawFile rf = entrySet.getKey();
                            List<AbstractRawFilePanel> list = entrySet.getValue();
                            if (list.contains(p)) {
                                list.remove(p);
                            }
                        }
                    } else if (c instanceof MultiRawFilePanel) {
                        MultiRawFilePanel p = (MultiRawFilePanel) c;
                        for (Map.Entry<IRawFile, List<AbstractRawFilePanel>> entrySet : mapRawFilePanelRawFile.entrySet()) {
                            IRawFile rf = entrySet.getKey();
                            List<AbstractRawFilePanel> list = entrySet.getValue();
                            if (list.contains(p)) {
                                list.remove(p);
                            }
                        }
                    }
                }
            }
        });
        if (tooltip != null){
            buttonTabComp.setToolTipText(tooltip); // set tooltip disables the tab selection!
            // so we force the selection
            buttonTabComp.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseClicked(MouseEvent e) {
                    int index = tabPane.indexOfTabComponent((Component) e.getSource());
                    tabPane.setSelectedIndex(index);
                }

            });
        }
        tabPane.setTabComponentAt(i, buttonTabComp);
        return buttonTabComp;
    }

    public void displayRawAction(IRawFile rawfile, boolean displayDefaultChrom) {
        if (rawfile == null) {
            return;
        }
        // rawFilePanel: check if already exists
        boolean rawFilePanelExists = false;
        AbstractRawFilePanel plotPanel = null;
        List<AbstractRawFilePanel> listTabs = mapRawFilePanelRawFile.get(rawfile);
        if (listTabs != null && !listTabs.isEmpty()) {
            for (AbstractRawFilePanel panel : listTabs) {
                if (panel instanceof SingleRawFilePanel) { // should be unique!
                    viewersTabPane.setSelectedComponent(panel);
                    plotPanel = panel;
                    rawFilePanelExists = true;
                }
            }
        }
        if (!rawFilePanelExists) {
            
            plotPanel = new SingleRawFilePanel(rawfile, displayDefaultChrom);
            final ButtonTabComponent tabComp  = addRawTab(rawfile.getName(), plotPanel);
            IRawFileLoading rawFileLoading = new IRawFileLoading() {

                @Override
                public void setWaitingState(boolean waitingState) {
                    tabComp.setWaitingState(waitingState);
                }
            };
            plotPanel.setRawFileLoading(rawFileLoading);
            if (displayDefaultChrom){
                tabComp.setWaitingState(true);
            }
            //viewersTabPane.add(rawfile.getName(), plotPanel);
            
            registerRawFilePanel(rawfile, plotPanel);
        }
    }
    
    private String getName(List<IRawFile> rawfiles){
        String name = "";
        if (rawfiles.size() > 1) {
            String prefix = Strings.commonPrefix(rawfiles.get(0).getName(), rawfiles.get(1).getName());
            if (!prefix.isEmpty()) {
                StringBuilder builder = new StringBuilder();
                for (IRawFile rawfile : rawfiles) {
                    String shortName = rawfile.getName().substring(prefix.length() - 1);
                    shortName = shortName.substring(0, shortName.lastIndexOf('.'));
                    builder.append("..").append(shortName).append(',');
                }
                builder.deleteCharAt(builder.length() - 1);
                name = builder.toString();
            } else {
                name = Integer.toString(rawfiles.size()) + " files";
            }
        } else {
            name = rawfiles.get(0).getName();
        }
        return name;
    }
    

    public void displayRawAction(List<IRawFile> rawfiles) {
        String name = getName(rawfiles);
        
        AbstractRawFilePanel plotPanel = new MultiRawFilePanel(rawfiles);
        final ButtonTabComponent tabComp = addRawTab(name, plotPanel);
        IRawFileLoading rawFileLoading = new IRawFileLoading() {

            @Override
            public void setWaitingState(boolean waitingState) {
                tabComp.setWaitingState(waitingState);
            }
        };
        plotPanel.setRawFileLoading(rawFileLoading);
        tabComp.setWaitingState(true);
        for (IRawFile rawFile : rawfiles) {
            registerRawFilePanel(rawFile, plotPanel);
        }
    }

    private void registerRawFilePanel(IRawFile rawfile, AbstractRawFilePanel plotPanel) {
        if (mapRawFilePanelRawFile.get(rawfile) == null) {
            List<AbstractRawFilePanel> list = new ArrayList();
            list.add(plotPanel);
            mapRawFilePanelRawFile.put(rawfile, list);
        } else {
            List<AbstractRawFilePanel> list = mapRawFilePanelRawFile.get(rawfile);
            list.add(plotPanel);
            mapRawFilePanelRawFile.put(rawfile, list);
        }
    }

    public void extractFeatures() {
        if (selectedRawFilePanel != null) {
            extractFeatures(Arrays.asList(selectedRawFilePanel.getCurrentRawfile()));
        }
    }

    public void extractFeatures(List<IRawFile> rawfiles) {
        boolean isDIA = isDIAFiles(rawfiles);
        ExtractionParamsDialog dialog = new ExtractionParamsDialog(this.parentFrame, true, isDIA);
        dialog.setExtractionParamsTitle("Extract Features Parameters");
        dialog.setLocationRelativeTo(this);
        dialog.showExtractionParamsDialog();
        FeaturesExtractionRequest.Builder builder = dialog.getExtractionParams();
        if (builder != null) {
            builder.setExtractionMethod(FeaturesExtractionRequest.ExtractionMethod.EXTRACT_MS2_FEATURES);
            FeaturesExtractionRequest params = builder.build();
            extractFeatures(rawfiles, params);
        }
    }
    
    private void extractFeatures(List<IRawFile> rawfiles, FeaturesExtractionRequest params) {
        final FeaturesPanel featurePanel = new FeaturesPanel(this);
        final ButtonTabComponent tabComp = addFeatureTab(getName(rawfiles), featurePanel, params.getExtractionParamsString());
        tabComp.setWaitingState(true);
        fireExtractionEvent(new ExtractionEvent(this, ExtractionEvent.EXTRACTION_STARTED));
        final long start = System.currentTimeMillis();
        SwingWorker worker = new SwingWorker<Integer, List<Feature>>() {
            int count = 0;

            @Override
            protected Integer doInBackground() throws Exception {

                for (IRawFile rawFile : rawfiles) {
                    List<Feature> listF = rawFile.extractFeatures(params);
                    count++;
                    publish(listF);
                }
                return count;
            }

            @Override
            protected void process(List<List<Feature>> list) {
                List<Feature> listF = list.get(list.size() - 1);
                logger.info("{} features/peakels extracted in {}", listF.size(), (System.currentTimeMillis() - start) / 1000.0);
                Map<Integer, IRawFile> mapRawFileByFeatureId = new HashMap();
                listF.stream().forEach((f) -> {
                    mapRawFileByFeatureId.put(f.getId(), rawfiles.get(count-1));
                });
                if (count == 1) {
                    featurePanel.setFeatures(listF, mapRawFileByFeatureId, rawfiles.size() > 1);
                } else {
                    featurePanel.addFeatures(listF, mapRawFileByFeatureId);
                }
            }

            @Override
            protected void done() {
                logger.info("All features/peakels extracted in {}", (System.currentTimeMillis() - start) / 1000.0);
                featuresTabPane.setSelectedComponent(featurePanel);
                tabComp.setWaitingState(false);
                fireExtractionEvent(new ExtractionEvent(this, ExtractionEvent.EXTRACTION_DONE));
            }
        };

        worker.execute();
        logger.debug("Feature extraction running ... ");
    }

    private void extractFeatures(final IRawFile rawFile, final FeaturesExtractionRequest params) {
        if ((selectedRawFilePanel != null) && (viewersTabPane.getSelectedIndex() >= 0)) {
            final FeaturesPanel featurePanel = new FeaturesPanel(this);
            final ButtonTabComponent tabComp = addFeatureTab(rawFile.getName(), featurePanel, params.getExtractionParamsString());
            tabComp.setWaitingState(true);
            fireExtractionEvent(new ExtractionEvent(this, ExtractionEvent.EXTRACTION_STARTED));
            final long start = System.currentTimeMillis();
            SwingWorker worker;
            worker = new SwingWorker<List<Feature>, Void>() {
                @Override
                protected List<Feature> doInBackground() throws Exception {
                    return rawFile.extractFeatures(params);
                }

                @Override
                protected void done() {
                    try {
                        List<Feature> features = get();
                        logger.info("{} features/peakels extracted in {}", features.size(), (System.currentTimeMillis() - start) / 1000.0);
                        Map<Integer, IRawFile> mapRawFileByFeatureId = new HashMap();
                        features.stream().forEach((f) -> {
                            mapRawFileByFeatureId.put(f.getId(), rawFile);
                        });
                        featurePanel.setFeatures(features, mapRawFileByFeatureId, false);
                        featuresTabPane.setSelectedComponent(featurePanel);
                        tabComp.setWaitingState(false);
                        fireExtractionEvent(new ExtractionEvent(this, ExtractionEvent.EXTRACTION_DONE));
                    } catch (InterruptedException | ExecutionException e) {
                        logger.error("Error while reading chromatogram");
                    }
                }
            };
            worker.execute();
            logger.debug("Feature extraction running ... ");
        }
    }

    public void detectPeakels() {
        if (selectedRawFilePanel != null) {
            detectPeakels(Arrays.asList(selectedRawFilePanel.getCurrentRawfile()));
        }
    }

    public void detectPeakels(List<IRawFile> rawfiles) {
        boolean isDIA = isDIAFiles(rawfiles);
        ExtractionParamsDialog dialog = new ExtractionParamsDialog(this.parentFrame, true, isDIA);
        dialog.setExtractionParamsTitle("Detect Peakels Parameters");
        dialog.setLocationRelativeTo(this);
        dialog.showExtractionParamsDialog();
        FeaturesExtractionRequest.Builder builder = dialog.getExtractionParams();
        if (builder != null) {
            builder.setExtractionMethod(FeaturesExtractionRequest.ExtractionMethod.DETECT_PEAKELS);
            FeaturesExtractionRequest params = builder.build();
            extractFeatures(rawfiles, params);
        }
    }

    public void detectFeatures() {
        detectFeatures(Arrays.asList(selectedRawFilePanel.getCurrentRawfile()));
    }

    public void detectFeatures(List<IRawFile> rawfiles) {
        boolean isDIA = isDIAFiles(rawfiles);
        ExtractionParamsDialog dialog = new ExtractionParamsDialog(this.parentFrame, true, isDIA);
        dialog.setExtractionParamsTitle("Detect Features Parameters");
        dialog.setLocationRelativeTo(this);
        dialog.showExtractionParamsDialog();
        FeaturesExtractionRequest.Builder builder = dialog.getExtractionParams();
        if (builder != null) {
            builder.setExtractionMethod(FeaturesExtractionRequest.ExtractionMethod.DETECT_FEATURES);
            for (IRawFile rawFile : rawfiles) {
                extractFeatures(rawFile, builder.build());
            }
        }
    }

    public void addExtractionListener(ExtractionStateListener listener) {
        listenerList.add(ExtractionStateListener.class, listener);
    }

    public void removeExtractionListener(ExtractionStateListener listener) {
        listenerList.remove(ExtractionStateListener.class, listener);
    }

    void fireExtractionEvent(ExtractionEvent event) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = 0; i < listeners.length; i = i + 2) {
            if (listeners[i] == ExtractionStateListener.class) {
                ((ExtractionStateListener) listeners[i + 1]).extractionStateChanged(event);
            }
        }
    }

    public boolean closeAllRaw() {
        String[] options = {"Yes", "No"};
        int reply = JOptionPane.showOptionDialog(parentFrame, "All files will be closed, do you want to continue?", "Close all files", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, "Yes");
        if (reply == JOptionPane.YES_OPTION) {
            viewersTabPane.removeAll();
            featuresTabPane.removeAll();
            mapRawFilePanelRawFile = new HashMap<>();
            return true;
        }
        return false;
    }

    public void closeRawFile(IRawFile rawfile) {
       // TODO : if we still need to be able to close raw file, a list of features panel 
        // must be maintained and closed at this stage
//        //feature panel
//        FeaturesPanel featurePanel = mapFeaturePanelRawFile.get(rawfile);
//        if (featurePanel != null) {
//            featuresTabPane.remove(featurePanel);
//            mapFeaturePanelRawFile.remove(rawfile);
//        }
        // raw file panel
        List<AbstractRawFilePanel> tabPanels = mapRawFilePanelRawFile.get(rawfile);
        if (tabPanels != null) {
            for (AbstractRawFilePanel tabPanel : tabPanels) {
                viewersTabPane.remove(tabPanel);
                if (tabPanel instanceof MultiRawFilePanel) {
                    // search for other rawfiles
                    List<IRawFile> list = getRawFileListForRawFilePanel(tabPanel);
                    for (IRawFile rawFile : list) {
                        if (rawFile != rawfile) {
                            removeRawFilePanel(rawFile, tabPanel);
                        }
                    }
                }
            }
            mapRawFilePanelRawFile.remove(rawfile);
        }
    }

    private void removeRawFilePanel(IRawFile rawFile, AbstractRawFilePanel panel) {
        List<AbstractRawFilePanel> list = mapRawFilePanelRawFile.get(rawFile);
        if (list.contains(panel)) {
            list.remove(panel);
            mapRawFilePanelRawFile.put(rawFile, list);
        }
    }

    private List<IRawFile> getRawFileListForRawFilePanel(AbstractRawFilePanel panel) {
        List<IRawFile> list = new ArrayList();
        if (panel == null) {
            return list;
        }
        for (Map.Entry<IRawFile, List<AbstractRawFilePanel>> entrySet : mapRawFilePanelRawFile.entrySet()) {
            IRawFile key = entrySet.getKey();
            List<AbstractRawFilePanel> value = entrySet.getValue();
            if (value.contains(panel)) {
                list.add(key);
            }
        }
        return list;
    }

    @Override
    public void displayFeatureInRawFile(IFeature f, IRawFile rawFile) {
        List<AbstractRawFilePanel> list = mapRawFilePanelRawFile.get(rawFile);
        if (list != null) {
            for (AbstractRawFilePanel panel : list) {
                if (panel instanceof SingleRawFilePanel) {
                    panel.displayFeature(f);
                    viewersTabPane.setSelectedComponent(panel);
                }
            }
        }
    }

    @Override
    public void displayFeatureInCurrentRawFile(IFeature f) {
        IRawFilePanel panel = (IRawFilePanel) viewersTabPane.getSelectedComponent();
        if (panel != null) {
            panel.displayFeature(f);
        }
    }

    @Override
    public void extractChromatogramMass(Ms1ExtractionRequest params) {
        IRawFilePanel panel = (IRawFilePanel) viewersTabPane.getSelectedComponent();
        if (panel != null) {
            DisplayMode xicModeDisplay = MzScopeConstants.DisplayMode.REPLACE;
            if (panel instanceof AbstractRawFilePanel){
                xicModeDisplay = ((AbstractRawFilePanel)panel).getXicModeDisplay();
            }
            panel.extractAndDisplayChromatogram(params, xicModeDisplay, null);
        }
    }

    public void displayAllRawAction() {
        final List<IRawFile> rawfiles = RawFileManager.getInstance().getAllFiles();
        TabbedMultiRawFilePanel plotPanel = new TabbedMultiRawFilePanel(rawfiles);
        addRawTab("All", plotPanel);
    }
    
    
    public void displayLCMSMap(IRawFile rawFile){
        JDialog mapDialog = new JDialog(this.parentFrame);
        mapDialog.setTitle("LCMS Map Viewer");
	LcMsViewer viewer = new LcMsViewer(new LcMsMap(rawFile.getFile().getAbsolutePath()));
	LcMsViewerUI viewerUI = new LcMsViewerUI();
	mapDialog.add(viewerUI.getGui(), BorderLayout.CENTER);
        mapDialog.setLocationRelativeTo(this);
        Dimension screenSize = Toolkit.getDefaultToolkit ().getScreenSize(); 
        mapDialog.setBounds(0, 0,  screenSize.width,  screenSize.height); 
	mapDialog.setVisible(true);
	viewerUI.setViewer(viewer);
	try {
	    Thread.sleep(1000);
	} catch (InterruptedException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	viewerUI.getController().changeViewport(new LcMsViewport(390, 440, 1000, 1150));
    }
    
    public void exportAsMGF(IRawFile rawFile){
        ExportMGFDialog exportDialog = ExportMGFDialog.getDialog(parentFrame, rawFile.getName());
        exportDialog.setLocationRelativeTo(parentFrame);
        ProgressTask task = new DefaultDialog.ProgressTask() {

            @Override
            public int getMinValue() {
                return 0;
            }

            @Override
            public int getMaxValue() {
                return 100;
            }

            @Override
            protected Object doInBackground() throws Exception {
                exportAsMGF(rawFile, exportDialog.getMgfFileName(), exportDialog.getPrecComp(), exportDialog.getMzTolPPM(), exportDialog.getIntensityCutoff(), exportDialog.isExportProlineTitle());
                setProgress(100);
                return null;
            }
        };
        exportDialog.setTask(task);
        exportDialog.setVisible(true);
    }
    
    public boolean exportAsMGF(IRawFile rawFile, String mgfFileName, PrecursorMzComputation precComp, float mzTolPPM ,float intensityCutoff, boolean exportProlineTitle ){
        return rawFile.exportAsMGF(mgfFileName, precComp, mzTolPPM, intensityCutoff, exportProlineTitle);
    }

    @Override
    public void displayChromatogramAsSingleView(IRawFile rawfile, Chromatogram c) {
        displayRawAction(rawfile, false);
        List<AbstractRawFilePanel> list = mapRawFilePanelRawFile.get(rawfile);
        if (list != null) {
            for (AbstractRawFilePanel panel : list) {
                if (panel instanceof SingleRawFilePanel) {
                    panel.displayChromatogram(c, DisplayMode.REPLACE);
                    viewersTabPane.setSelectedComponent(panel);
                }
            }
        }
    }

    @Override
    public void displayChromatogramAsMultiView(Map<IRawFile, Chromatogram> chromatogramByRawFile) {
        TabbedMultiRawFilePanel panel = getTabbedMultiRawFilePanel();
        if (panel == null){
            displayAllRawAction();
            panel = getTabbedMultiRawFilePanel();
        }
        panel.displayChromatograms(chromatogramByRawFile);
    }
    
    private TabbedMultiRawFilePanel getTabbedMultiRawFilePanel(){
        int nbT = viewersTabPane.getTabCount();
        TabbedMultiRawFilePanel panel = null;
        for(int i=0; i<nbT; i++){
            if (viewersTabPane.getComponentAt(i) instanceof TabbedMultiRawFilePanel){
                panel = (TabbedMultiRawFilePanel)viewersTabPane.getComponentAt(i);
            }
        }
        return panel;
    }
    
    private boolean isDIAFiles(List<IRawFile> rawfiles){
        if (rawfiles.stream().anyMatch((rf) -> (rf.isDIAFile()))) {
            return true;
        }
        return false;
    }
    
}
