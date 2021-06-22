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
package fr.proline.mzscope.ui;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import fr.proline.mzscope.model.*;
import fr.proline.mzscope.ui.peakels.DetectedFeaturesPanel;
import fr.proline.mzscope.ui.peakels.PeakelsPanel;
import fr.proline.mzscope.utils.ButtonTabComponent;
import com.google.common.base.Strings;
import fr.profi.mzdb.peakeldb.io.PeakelDbReader;
import fr.profi.mzdb.model.Peakel;
import fr.proline.mzscope.map.LcMsMap;
import fr.proline.mzscope.map.LcMsViewer;
import fr.proline.mzscope.map.LcMsViewport;
import fr.proline.mzscope.map.ui.LcMsViewerUI;
import fr.proline.mzscope.mzdb.MzdbPeakelWrapper;
import fr.proline.mzscope.utils.MzScopeCallback;
import fr.proline.mzscope.ui.model.MzScopePreferences;
import fr.proline.mzscope.ui.dialog.ExportRawFileDialog;
import fr.proline.mzscope.ui.dialog.ExtractionParamsDialog;
import fr.proline.mzscope.ui.event.ExtractionEvent;
import fr.proline.mzscope.ui.event.ExtractionStateListener;
import fr.proline.mzscope.utils.Display;
import fr.proline.studio.Exceptions;
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
import java.util.*;
import java.util.stream.Collectors;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
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
import static scala.collection.JavaConversions.asJavaIterable;

/**
 * main panel for mzscope
 *
 * @author MB243701
 */
public class MzScopePanel extends JPanel implements IMzScopeController {

    private final static Logger logger = LoggerFactory.getLogger("ProlineStudio.mzScope.MzScopePanel");

    private Frame parentFrame = null;
    private JSplitPane mainSplitPane = null;
    private JTabbedPane viewersTabPane = null;
    private JTabbedPane featuresTabPane = null;

    private IRawFileViewer selectedRawFilePanel;
    private XICExtractionPanel extractionPanel = null;
    private EventListenerList listenerList = new EventListenerList();
    private Map<IRawFile, List<AbstractRawFilePanel>> mapRawFilePanelRawFile;

    public MzScopePanel(Frame parentFrame) {
        this.parentFrame = parentFrame;
        initComponents();
    }

    private void initComponents() {
        mapRawFilePanelRawFile = new HashMap<>();
        setLayout(new BorderLayout());
        this.add(getExtractionPanel(), BorderLayout.NORTH);
        this.add(getMainComponent(), BorderLayout.CENTER);
    }

    private XICExtractionPanel getExtractionPanel() {
        if (extractionPanel == null) {
            extractionPanel = new XICExtractionPanel(this);
        }
        return extractionPanel;
    }

    private JSplitPane getMainComponent() {
        if (this.mainSplitPane == null) {
            this.mainSplitPane = new JSplitPane();
            this.mainSplitPane.setDividerLocation(320);
            this.mainSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
            this.mainSplitPane.setOneTouchExpandable(true);
            this.mainSplitPane.setRightComponent(getFeaturesTabPane());
            this.mainSplitPane.setLeftComponent(getViewersTabPane());
        }

        return this.mainSplitPane;
    }

    public JTabbedPane getFeaturesTabPane() {
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
        Component c = viewersTabPane.getSelectedComponent();
        if ( (c != null) &&  IRawFileViewer.class.isAssignableFrom(c.getClass())) {
            this.selectedRawFilePanel = (IRawFileViewer) c;
            if (selectedRawFilePanel != null && selectedRawFilePanel.getCurrentRawfile() != null) {
                getExtractionPanel().setDIAEnabled(selectedRawFilePanel.getCurrentRawfile().isDIAFile());
            }
        }
    }

    @Override
    public IRawFileViewer getCurrentRawFileViewer() {
        return selectedRawFilePanel;
    }


    @Override
    public IRawFileViewer getRawFileViewer(IRawFile rawfile, boolean setVisible) {
        AbstractRawFilePanel viewer = displayRaw(rawfile, false);
        if (setVisible) {
            viewersTabPane.setSelectedComponent(viewer);
        }
        return viewer;
    }

    public void openRaw(List<File> files, boolean display) {
        List<IRawFile> rawfiles = new ArrayList();
        for (File f : files) {
            IRawFile rawfile = RawFileManager.getInstance().getFile(f.getAbsolutePath());
            if (rawfile == null) {
                rawfile = RawFileManager.getInstance().addRawFile(f);
            }
            rawfiles.add(rawfile);
        }
        if (display) {
            displayRaw(rawfiles);
        }
    }

    public void openRawAndExtract(File file, final double moz, final double elutionTime, final double firstScanTime, final double lastScanTime) {
        IRawFile tmpRawFile = RawFileManager.getInstance().getFile(file.getAbsolutePath());
        if (tmpRawFile == null) {
            tmpRawFile = RawFileManager.getInstance().addRawFile(file);
        }
        final IRawFile rawfile = tmpRawFile;
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
            displayRaw(rawfile, false);
        }
        MsnExtractionRequest params = MsnExtractionRequest.builder().setMzTolPPM(MzScopePreferences.getInstance().getMzPPMTolerance()).setMz(moz).build();
        list = mapRawFilePanelRawFile.get(rawfile);
        for (final AbstractRawFilePanel p : list) {
            if (p instanceof SingleRawFilePanel) {
                p.extractAndDisplayChromatogram(params, new Display(Display.Mode.REPLACE), new MzScopeCallback() {
                    @Override
                    public void callback(boolean success) {
                        p.displayPeakel(new BasePeakel(moz, (float) elutionTime, (float) firstScanTime, (float) lastScanTime, rawfile, 1));
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

    public ButtonTabComponent addFeatureTab(String s, Component c, String tooltip) {
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
        if (tooltip != null) {
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

    public AbstractRawFilePanel displayRaw(IRawFile rawfile, boolean displayDefaultChrom) {
        if (rawfile == null) {
            return null;
        }
        // rawFilePanel: check if already exists
        boolean rawPanelExists = false;
        AbstractRawFilePanel rawPanel = null;
        List<AbstractRawFilePanel> listTabs = mapRawFilePanelRawFile.get(rawfile);
        if (listTabs != null && !listTabs.isEmpty()) {
            for (AbstractRawFilePanel panel : listTabs) {
                if (panel instanceof SingleRawFilePanel) { // should be unique!
                    viewersTabPane.setSelectedComponent(panel);
                    rawPanel = panel;
                    rawPanelExists = true;
                }
            }
        }
        if (!rawPanelExists) {

            rawPanel = new SingleRawFilePanel(rawfile, displayDefaultChrom);
            final ButtonTabComponent tabComp = addRawTab(rawfile.getName(), rawPanel);
            IRawFileLoading rawFileLoading = new IRawFileLoading() {

                @Override
                public void setWaitingState(boolean waitingState) {
                    tabComp.setWaitingState(waitingState);
                }
            };
            rawPanel.setRawFileLoading(rawFileLoading);
            if (displayDefaultChrom) {
                tabComp.setWaitingState(true);
            }
            //viewersTabPane.add(rawfile.getName(), plotPanel);

            registerRawFilePanel(rawfile, rawPanel);
        }
        return rawPanel;
    }

    private String getName(List<IRawFile> rawfiles) {
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

    public AbstractRawFilePanel displayRaw(List<IRawFile> rawfiles) {
        if (rawfiles.size() == 1) {
            return displayRaw(rawfiles.get(0), true);
        } 
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
        
        return plotPanel;
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
            startPeakelsExtractions(rawfiles, params);
        }
    }

    public void extractFeaturesFromMS2(List<IRawFile> rawfiles) {
        boolean isDIA = isDIAFiles(rawfiles);
        ExtractionParamsDialog dialog = new ExtractionParamsDialog(this.parentFrame, true, isDIA);
        dialog.setExtractionParamsTitle("Extract Features Parameters");
        dialog.setLocationRelativeTo(this);
        dialog.showExtractionParamsDialog();
        FeaturesExtractionRequest.Builder builder = dialog.getExtractionParams();
        if (builder != null) {
            builder.setExtractionMethod(FeaturesExtractionRequest.ExtractionMethod.EXTRACT_MS2_FEATURES);
            FeaturesExtractionRequest params = builder.build();
            startFeaturesExtractions(rawfiles, params);
        }
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
            startFeaturesExtractions(rawfiles, builder.build());
        }
    }

    private void startPeakelsExtractions(List<IRawFile> rawfiles, FeaturesExtractionRequest params) {
        final PeakelsPanel peakelsPanel = new PeakelsPanel(this);
        final ButtonTabComponent tabComp = addFeatureTab(getName(rawfiles), peakelsPanel, params.getExtractionParamsString());
        tabComp.setWaitingState(true);
        fireExtractionEvent(new ExtractionEvent(this, ExtractionEvent.EXTRACTION_STARTED));
        final long start = System.currentTimeMillis();
        SwingWorker worker = new SwingWorker<Integer, List<IPeakel>>() {
            int count = 0;

            @Override
            protected Integer doInBackground() throws Exception {

                for (IRawFile rawFile : rawfiles) {
                    List<IPeakel> listF = rawFile.extractPeakels(params);
                    count++;
                    publish(listF);
                }
                return count;
            }

            @Override
            protected void process(List<List<IPeakel>> list) {
                List<IPeakel> listF = list.stream().flatMap(l -> l.stream()).collect(Collectors.toList());
                logger.info("{} peakels extracted in {}", listF.size(), (System.currentTimeMillis() - start) / 1000.0);

                if (count == 1) {
                    peakelsPanel.setPeakels(listF, rawfiles.size() > 1);
                } else {
                    peakelsPanel.addPeakels(listF);
                }
            }

            @Override
            protected void done() {
                logger.info("All peakels extracted in {}", (System.currentTimeMillis() - start) / 1000.0);
                featuresTabPane.setSelectedComponent(peakelsPanel);
                tabComp.setWaitingState(false);
                fireExtractionEvent(new ExtractionEvent(this, ExtractionEvent.EXTRACTION_DONE));
            }
        };

        worker.execute();
        logger.debug("Peakels extraction running ... ");
    }



    private void startFeaturesExtractions(List<IRawFile> rawfiles, FeaturesExtractionRequest params) {
        final DetectedFeaturesPanel featurePanel = new DetectedFeaturesPanel(this);
        final ButtonTabComponent tabComp = addFeatureTab(getName(rawfiles), featurePanel, params.getExtractionParamsString());
        tabComp.setWaitingState(true);
        fireExtractionEvent(new ExtractionEvent(this, ExtractionEvent.EXTRACTION_STARTED));
        final long start = System.currentTimeMillis();
        SwingWorker worker = new SwingWorker<Integer, List<IFeature>>() {
            int count = 0;

            @Override
            protected Integer doInBackground() throws Exception {

                for (IRawFile rawFile : rawfiles) {
                    List<IFeature> listF = rawFile.extractFeatures(params);
                    count++;
                    publish(listF);
                }
                return count;
            }

            @Override
            protected void process(List<List<IFeature>> list) {
                List<IFeature> listF = list.stream().flatMap(l -> l.stream()).collect(Collectors.toList());
                logger.info("{} features extracted in {}", listF.size(), (System.currentTimeMillis() - start) / 1000.0);

                if (count == 1) {
                    logger.info("Set {} features ", listF.size());
                    featurePanel.setFeatures(listF, rawfiles.size() > 1);
                } else {
                    logger.info("Add {} features ", listF.size());
                    featurePanel.addFeatures(listF);
                }
            }

            @Override
            protected void done() {
                logger.info("All features extracted in {}", (System.currentTimeMillis() - start) / 1000.0);
                featuresTabPane.setSelectedComponent(featurePanel);
                tabComp.setWaitingState(false);
                fireExtractionEvent(new ExtractionEvent(this, ExtractionEvent.EXTRACTION_DONE));
            }
        };

        worker.execute();
        logger.debug("Features extraction running ... ");
    }

    public void loadPeakels(IRawFile rawfile, File file) {
        try {
            List<IPeakel> features = new ArrayList<>();
            SQLiteConnection connection = new SQLiteConnection(file);
            connection.openReadonly();
            Iterator<Peakel> peakelsIt = asJavaIterable(PeakelDbReader.loadAllPeakels(connection, 400000)).iterator();
            while(peakelsIt.hasNext()) {
                features.add(new MzdbPeakelWrapper(peakelsIt.next(), rawfile));
            }
            logger.info("loaded peakels : "+features.size());
            connection.dispose();
            final PeakelsPanel peakelsPanel = new PeakelsPanel(this);
            addFeatureTab(rawfile.getName(), peakelsPanel, "loaded from "+file.getAbsolutePath());
            peakelsPanel.setPeakels(features, true);
            
        } catch (SQLiteException ex) {
            logger.error("error while reading peakeldb file", ex);
            Exceptions.printStackTrace(ex);
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
            RawFileManager.getInstance().removeAllFiles();
            return true;
        }
        return false;
    }

    public void closeRawFile(IRawFile rawfile) {
        // TODO : if we still need to be able to close raw file, a list of features panel must be maintained and closed at this stage

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
        RawFileManager.getInstance().removeFile(rawfile);
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

    public TabbedMultiRawFilePanel displayAllRaw() {
        final List<IRawFile> rawfiles = RawFileManager.getInstance().getAllFiles();
        TabbedMultiRawFilePanel multiRawPanel = new TabbedMultiRawFilePanel(rawfiles);
        addRawTab("All", multiRawPanel);
        return multiRawPanel;
        
    }

    public PropertiesPanel displayProperties(List<IRawFile> rawFiles) {
        if ((rawFiles != null) && rawFiles.size() > 0) {
            PropertiesPanel propertiesPanel = new PropertiesPanel(rawFiles);
            String title = rawFiles.size() == 1 ? "Properties" : new StringBuilder().append("Properties ").append(rawFiles.get(0).getName()).toString();
            addTab(viewersTabPane, title, propertiesPanel, "Raw file properties panel");
            return propertiesPanel;
        }
        return null;
    }
    
    public QCMetricsPanel displayMetrics(List<QCMetrics> metrics) {
        if ((metrics != null) && metrics.size() > 0) {
            QCMetricsPanel metricsPanel = new QCMetricsPanel(metrics);
            addTab(viewersTabPane, "QC Metrics", metricsPanel, "QC metrics panel");
            return metricsPanel;
        }
        return null;
    }
    
    public LcMsViewer displayLCMSMap(IRawFile rawFile) {
        JDialog mapDialog = new JDialog(this.parentFrame);
        mapDialog.setTitle("LCMS Map Viewer");
        LcMsViewer viewer = new LcMsViewer(new LcMsMap(rawFile.getFile().getAbsolutePath()));
        LcMsViewerUI viewerUI = new LcMsViewerUI();
        mapDialog.add(viewerUI.getGui(), BorderLayout.CENTER);
        mapDialog.setLocationRelativeTo(this);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        mapDialog.setBounds(0, 0, screenSize.width, screenSize.height);
        mapDialog.setVisible(true);
        viewerUI.setViewer(viewer);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        viewerUI.getController().changeViewport(new LcMsViewport(390, 440, 1000, 1150));
        return viewer;
    }

    public void export(List<IRawFile> rawFiles) {
        ExportRawFileDialog exportDialog = ExportRawFileDialog.getDialog(parentFrame, rawFiles.size() == 1 ? rawFiles.get(0).getName() : "selected Files");
        exportDialog.setLocationRelativeTo(parentFrame);
        exportDialog.setSelectionMode(rawFiles.size() == 1 ? JFileChooser.FILES_ONLY : JFileChooser.DIRECTORIES_ONLY);
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
                if (rawFiles.size() == 1) {
                    exportRawFile(rawFiles.get(0), exportDialog.getOutputFileName(), exportDialog.getExportParams());
                    setProgress(100);
                } else {
                    for (int i = 0; i < rawFiles.size() ; i++) {
                        IRawFile rawFile = rawFiles.get(i);
                        String filename = rawFile.getName();
                        filename = filename.substring(0, filename.lastIndexOf('.'));
                        StringBuilder filenameBuilder = new StringBuilder(exportDialog.getOutputFileName()).append('/').append(filename);
                        filenameBuilder.append('.').append(exportDialog.getFileExtension());
                        setProgress(0);
                        exportRawFile(rawFile, filenameBuilder.toString(), exportDialog.getExportParams());
                        setProgress((int)((i+1)*(100.0/rawFiles.size())));
                        //hack to force progress bar update
                        firePropertyChange("progress", (int)(i*(100.0/rawFiles.size())), (int)((i+1)*(100.0/rawFiles.size())));
                    }
                    //just to be sure to close the progress bar
                    setProgress(100);
                }
                return null;
            }
        };
        exportDialog.setTask(task);
        exportDialog.setVisible(true);
    }

    private boolean exportRawFile(IRawFile rawFile, String exportFileName, IExportParameters exportParams) {
        return rawFile.exportRawFile(exportFileName, exportParams);
    }

    @Override
    public IRawFileViewer getTabbedMultiRawFileViewer() {
        int nbT = viewersTabPane.getTabCount();
        TabbedMultiRawFilePanel panel = null;
        for (int i = 0; i < nbT; i++) {
            if (viewersTabPane.getComponentAt(i) instanceof TabbedMultiRawFilePanel) {
                panel = (TabbedMultiRawFilePanel) viewersTabPane.getComponentAt(i);
            }
        }
        if (panel == null) {
            panel = displayAllRaw();
        }
        return panel;
    }

    private boolean isDIAFiles(List<IRawFile> rawfiles) {
        if (rawfiles.stream().anyMatch((rf) -> (rf.isDIAFile()))) {
            return true;
        }
        return false;
    }

  @Override
  public void displayFeatures(Map<String, List<IFeature>> features) {
    final DetectedFeaturesPanel featurePanel = new DetectedFeaturesPanel(this);
    addFeatureTab("Generated Features", featurePanel, "generated");
    featuresTabPane.setSelectedComponent(featurePanel);
    featurePanel.setFeatures(features, false);
  }

}
