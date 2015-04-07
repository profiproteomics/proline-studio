package fr.proline.mzscope.ui;

import com.google.common.base.Strings;
import fr.profi.mzdb.model.Feature;
import fr.proline.mzscope.model.Chromatogram;
import fr.proline.mzscope.model.ExtractionParams;
import fr.proline.mzscope.model.IRawFile;
import fr.proline.mzscope.model.MzScopePreferences;
import fr.proline.mzscope.ui.dialog.ExtractionParamsDialog;
import fr.proline.mzscope.ui.event.DisplayFeatureListener;
import fr.proline.mzscope.ui.event.ExtractFeatureListener;
import fr.proline.mzscope.ui.event.ExtractionListener;
import fr.proline.mzscope.util.MzScopeConstants;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
public class MzScopePanel extends JPanel implements  DisplayFeatureListener , ExtractionListener{

    private final static Logger logger = LoggerFactory.getLogger("ProlineStudio.mzScope.MzScopePanel");

    private Frame parentFrame = null;
    private JSplitPane mainRightSplitPane = null;
    private JTabbedPane viewersTabPane = null;
    private JTabbedPane featuresTabPane = null;

    private IRawFilePlot selectedRawFilePanel;

    private XICExtractionPanel extractionPanel = null;

    private EventListenerList listenerList = new EventListenerList();

    private Map<IRawFile, FeaturesPanel> mapFeaturePanelRawFile;
    private Map<IRawFile, List<AbstractRawFilePanel>> mapRawFilePanelRawFile;


    public MzScopePanel(Frame parentFrame) {
        this.parentFrame = parentFrame;
        initComponents();
    }

    private void initComponents() {
        mapFeaturePanelRawFile = new HashMap<>();
        mapRawFilePanelRawFile = new HashMap<>();
        setLayout(new BorderLayout());
        this.add(getExtractionPanel(), BorderLayout.NORTH);
        this.add(getMainRightComponent(), BorderLayout.CENTER);
    }


    private XICExtractionPanel getExtractionPanel(){
        if (extractionPanel == null){
            extractionPanel = new XICExtractionPanel();
            extractionPanel.addExtractionListener(this);
        }
        return extractionPanel;
    }

    private JSplitPane getMainRightComponent() {
        if (this.mainRightSplitPane == null) {
            this.mainRightSplitPane = new JSplitPane();
            this.mainRightSplitPane.setDividerLocation(320);
            this.mainRightSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);

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
        this.selectedRawFilePanel = (IRawFilePlot) viewersTabPane.getSelectedComponent();
    }

    
    public Chromatogram getCurrentChromatogram(){
        return selectedRawFilePanel.getCurrentChromatogram();
    }

    public void openRaw(File file) {
        IRawFile rawfile = RawFileManager.getInstance().getFile(file.getName());
        if (rawfile == null){
            rawfile = RawFileManager.getInstance().addRawFile(file);
        }
        boolean fileAlreadyOpen = false;
        List<AbstractRawFilePanel> list = mapRawFilePanelRawFile.get(rawfile);
        if (list != null){
            for (AbstractRawFilePanel p : list) {
                if (p instanceof SingleRawFilePanel) {
                    fileAlreadyOpen = true;
                    break;
                }
            }
        }
        if (!fileAlreadyOpen) {
            displayRawAction(rawfile);
        }
    }
    
    public void openRaw(List<File> files) {
        List<IRawFile> rawfiles = new ArrayList();
        for (File f : files) {
            IRawFile rawfile = RawFileManager.getInstance().addRawFile(f);
            rawfiles.add(rawfile);
        }
        displayRawAction(rawfiles);
    }
    
    private boolean isFileAlreadyOpened(File file){
        IRawFile rawfile = RawFileManager.getInstance().getFile(file.getName());
        if (rawfile == null){
            rawfile = RawFileManager.getInstance().addRawFile(file);
        }
        boolean fileAlreadyOpen = false;
        List<AbstractRawFilePanel> list = mapRawFilePanelRawFile.get(rawfile);
        if (list != null){
            for (AbstractRawFilePanel p : list) {
                if (p instanceof SingleRawFilePanel) {
                    fileAlreadyOpen = true;
                    break;
                }
            }
        }
        return fileAlreadyOpen;
    }
    
    public void openRawAndExtract(File file, double moz, double elutionTime, double firstScanTime, double lastScanTime) {
        IRawFile rawfile = RawFileManager.getInstance().getFile(file.getName());
        if (rawfile == null){
            rawfile = RawFileManager.getInstance().addRawFile(file);
        }
        boolean fileAlreadyOpen = false;
        List<AbstractRawFilePanel> list = mapRawFilePanelRawFile.get(rawfile);
        if (list != null){
            for (AbstractRawFilePanel p : list) {
                if (p instanceof SingleRawFilePanel) {
                    fileAlreadyOpen = true;
                    break;
                }
            }
        }
        
        if(!fileAlreadyOpen){
            displayRawAction(rawfile);
        }
        float ppm = MzScopePreferences.getInstance().getMzPPMTolerance();
        double minMz = moz;
        double maxMz = minMz + minMz * ppm / 1e6;
        minMz -= minMz * ppm / 1e6;
        list = mapRawFilePanelRawFile.get(rawfile);
        for (AbstractRawFilePanel p : list) {
            if (p instanceof SingleRawFilePanel) {
                p.extractChromatogramWithFeature(minMz, maxMz, elutionTime, firstScanTime, lastScanTime);
                break;
            }
        }
    }


    private void addRawTab(String s, Component c) {
        addTab(viewersTabPane, s, c);
        viewersTabPane.setSelectedComponent(c);
    }

    private void addFeatureTab(String s, Component c) {
        addTab(featuresTabPane, s, c);
        featuresTabPane.setSelectedComponent(c);
    }

    private void addTab(final JTabbedPane tabPane, String s, final Component c) {
        tabPane.add(s, c);
        int i = tabPane.getTabCount() - 1;
        ButtonTabComponent buttonTabComp = new ButtonTabComponent(s);
        buttonTabComp.addCloseTabListener(new ButtonTabComponent.CloseTabListener() {
            @Override
            public void closeTab(ButtonTabComponent tabComponent) {
                int index = tabPane.indexOfTabComponent(tabComponent);
                if (index != -1) {
                    tabPane.remove(index);
                    if (c instanceof FeaturesPanel) {
                        FeaturesPanel p = (FeaturesPanel) c;
                        for (Map.Entry<IRawFile, FeaturesPanel> entrySet : mapFeaturePanelRawFile.entrySet()) {
                            IRawFile rf = entrySet.getKey();
                            FeaturesPanel fp = entrySet.getValue();
                            if (fp.equals(p)) {
                                mapFeaturePanelRawFile.remove(rf);
                                break;
                            }
                        }
                    } else if (c instanceof SingleRawFilePanel) {
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
        tabPane.setTabComponentAt(i, buttonTabComp);
    }

    public void displayRawAction(IRawFile rawfile) {
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
            plotPanel = new SingleRawFilePanel(rawfile);
            //viewersTabPane.add(rawfile.getName(), plotPanel);
            addRawTab(rawfile.getName(), plotPanel);
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
        //features panel: check if already exists
        FeaturesPanel panel = mapFeaturePanelRawFile.get(rawfile);
        if (panel != null) {
            featuresTabPane.setSelectedComponent(panel);
        } else {
            FeaturesPanel featuresPanel = new FeaturesPanel(rawfile);
            featuresPanel.addDisplayFeatureListener(this);
            addFeatureTab(rawfile.getName(), featuresPanel);
            mapFeaturePanelRawFile.put(rawfile, featuresPanel);
        }
    }

    public void displayRawAction(List<IRawFile> rawfiles) {
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
                // trim to 30 char max
//              name = builder.substring(0, Math.min(builder.length()-1, 30))+"...";
                builder.deleteCharAt(builder.length() - 1);
                name = builder.toString();
            } else {
                name = Integer.toString(rawfiles.size()) + " files";
            }
        } else {
            name = rawfiles.get(0).getName();
        }
        AbstractRawFilePanel plotPanel = new MultiRawFilePanel(rawfiles);
        //viewersTabPane.add(name, plotPanel);
        addRawTab(name, plotPanel);
        for (IRawFile rawFile : rawfiles) {
            if (mapRawFilePanelRawFile.get(rawFile) == null) {
                List<AbstractRawFilePanel> list = new ArrayList();
                list.add(plotPanel);
                mapRawFilePanelRawFile.put(rawFile, list);
            } else {
                List<AbstractRawFilePanel> list = mapRawFilePanelRawFile.get(rawFile);
                list.add(plotPanel);
                mapRawFilePanelRawFile.put(rawFile, list);
            }
        }
    }

    public void extractFeaturesMI() {
        extractFeaturesMI(selectedRawFilePanel.getCurrentRawfile());
    }

    public void extractFeaturesMI(IRawFile rawFile) {
        ExtractionParamsDialog dialog = new ExtractionParamsDialog(this.parentFrame, true);
        dialog.setExtractionParamsTitle("Extract Features Parameters");
        dialog.setLocationRelativeTo(this);
        dialog.showExtractionParamsDialog();
        if (dialog.getExtractionParams() != null) {
            extractFeatures(rawFile, IRawFile.ExtractionType.EXTRACT_MS2_FEATURES, dialog.getExtractionParams());
        }
    }

    public void extractFeatures(final IRawFile rawFile, final IRawFile.ExtractionType type, final ExtractionParams params) {
        if ((selectedRawFilePanel != null) && (viewersTabPane.getSelectedIndex() >= 0)) {
            FeaturesPanel fp = mapFeaturePanelRawFile.get(rawFile);
            if (fp == null){
                fp = new FeaturesPanel(rawFile);
                fp.addDisplayFeatureListener(this);
                addFeatureTab(rawFile.getName(), fp);
                mapFeaturePanelRawFile.put(rawFile, fp);
            }
            final FeaturesPanel featurePanel = fp;
            //           final IRawFile rawFile = selectedRawFilePanel.getCurrentRawfile();
//            extractFeaturesMI.setEnabled(false);
//            detectPeakelsMI.setEnabled(false);
            fireExtractFeature(false, false);
            final long start = System.currentTimeMillis();
            SwingWorker worker = new SwingWorker<List<Feature>, Void>() {
                @Override
                protected List<Feature> doInBackground() throws Exception {
                    return rawFile.extractFeatures(type, params);
                }

                @Override
                protected void done() {
                    try {
                        List<Feature> features = get();
                        logger.info("{} features/peakels extracted in {}", features.size(), (System.currentTimeMillis() - start) / 1000.0);
                        featurePanel.setFeatures(features);
                        featuresTabPane.setSelectedComponent(featurePanel);
//                        extractFeaturesMI.setEnabled(true);
//                        detectPeakelsMI.setEnabled(true);
                        fireExtractFeature(true, true);
                    } catch (Exception e) {
                        logger.error("Error while reading chromatogram");
                    }
                }
            };
            worker.execute();
            logger.debug("Feature extraction running ... ");
        }
    }


    public void detectPeakelsMI() {
        detectPeakelsMI(selectedRawFilePanel.getCurrentRawfile());
    }
    
    public void detectPeakels(File file) {
        detectPeakelsMI(RawFileManager.getInstance().getFile(file.getName()));
    }
    
    public void detectPeakels(List<File> fileList) {
        List<IRawFile> listRawFile = new ArrayList();
        for (File file : fileList) {
            IRawFile rawFile = RawFileManager.getInstance().getFile(file.getName());
            listRawFile.add(rawFile);
        }
        detectPeakelsMI(listRawFile);
    }

        
    public void detectPeakelsMI(List<IRawFile> rawfiles) {
        ExtractionParamsDialog dialog = new ExtractionParamsDialog(this.parentFrame, true);
        dialog.setExtractionParamsTitle("Detect Peakels Parameters");
        dialog.setLocationRelativeTo(this);
        dialog.showExtractionParamsDialog();
        if (dialog.getExtractionParams() != null) {
            ExtractionParams extractionParams = dialog.getExtractionParams();
            for (IRawFile rawFile : rawfiles) {
                extractFeatures(rawFile, IRawFile.ExtractionType.DETECT_PEAKELS, extractionParams);
            }
        }
    }    
    
    public void detectPeakelsMI(IRawFile rawfile) {
        ExtractionParamsDialog dialog = new ExtractionParamsDialog(this.parentFrame, true);
        dialog.setExtractionParamsTitle("Detect Peakels Parameters");
        dialog.setLocationRelativeTo(this);
        dialog.showExtractionParamsDialog();
        if (dialog.getExtractionParams() != null) {
            extractFeatures(rawfile, IRawFile.ExtractionType.DETECT_PEAKELS, dialog.getExtractionParams());
        }
    }

    public void addExtractFeatureListener(ExtractFeatureListener listener) {
        listenerList.add(ExtractFeatureListener.class, listener);
    }

    public void removeMyEventListener(ExtractFeatureListener listener) {
        listenerList.remove(ExtractFeatureListener.class, listener);
    }

    void fireExtractFeature(boolean extractFeature, boolean detectPeakel) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = 0; i < listeners.length; i = i + 2) {
            if (listeners[i] == ExtractFeatureListener.class) {
                ((ExtractFeatureListener) listeners[i + 1]).extractFeatureListener(extractFeature, detectPeakel);
            }
        }
    }

    public boolean closeAllRaw() {
        String[] options = {"Yes", "No"};
        int reply = JOptionPane.showOptionDialog(parentFrame, "All files will be closed, do you want to continue?", "Close all files", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, "Yes");
        if (reply == JOptionPane.YES_OPTION) {
            viewersTabPane.removeAll();
            featuresTabPane.removeAll();
            //rawFilePanel.removeAllFiles();
            mapFeaturePanelRawFile = new HashMap<>();
            mapRawFilePanelRawFile = new HashMap<>();
            return true;
        }
        return false;
    }


    
    public void closeRawFile(IRawFile rawfile) {
        //feature panel
        FeaturesPanel featurePanel = mapFeaturePanelRawFile.get(rawfile);
        if (featurePanel != null) {
            featuresTabPane.remove(featurePanel);
            mapFeaturePanelRawFile.remove(rawfile);
        }
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
    public void displayFeatureInRawFile(Feature f, IRawFile rawFile) {
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
    public void displayFeatureInCurrentRawFile(Feature f) {
        AbstractRawFilePanel panel = (AbstractRawFilePanel) viewersTabPane.getSelectedComponent();
        if (panel != null) {
            panel.displayFeature(f);
        }
    }
    
    @Override
    public void extractChromatogramMass(double minMz, double maxMz) {
        AbstractRawFilePanel panel = (AbstractRawFilePanel) viewersTabPane.getSelectedComponent();
        if (panel != null) {
            int extractionMode = panel.getXicModeDisplay();
            switch (extractionMode) {
                case MzScopeConstants.MODE_DISPLAY_XIC_REPLACE: {
                    panel.extractChromatogram(minMz, maxMz);
                    break;
                }
                case MzScopeConstants.MODE_DISPLAY_XIC_OVERLAY: {
                    panel.addChromatogram(minMz, maxMz);
                    break;
                }
            }
        }
    }
    

}
