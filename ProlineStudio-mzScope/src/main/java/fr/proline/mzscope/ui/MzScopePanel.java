package fr.proline.mzscope.ui;

import fr.proline.mzscope.utils.ButtonTabComponent;
import com.google.common.base.Strings;
import fr.profi.mzdb.model.Feature;
import fr.proline.mzscope.model.BaseFeature;
import fr.proline.mzscope.model.Chromatogram;
import fr.proline.mzscope.model.FeaturesExtractionRequest;
import fr.proline.mzscope.model.IFeature;
import fr.proline.mzscope.model.IRawFile;
import fr.proline.mzscope.model.Ms1ExtractionRequest;
import fr.proline.mzscope.model.MzScopeCallback;
import fr.proline.mzscope.model.MzScopePreferences;
import fr.proline.mzscope.ui.dialog.ExtractionParamsDialog;
import fr.proline.mzscope.ui.event.ExtractionEvent;
import fr.proline.mzscope.ui.event.ExtractionStateListener;
import fr.proline.mzscope.utils.MzScopeConstants;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
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
public class MzScopePanel extends JPanel implements IFeatureViewer, IExtractionExecutor {

   private final static Logger logger = LoggerFactory.getLogger("ProlineStudio.mzScope.MzScopePanel");

   private Frame parentFrame = null;
   private JSplitPane mainRightSplitPane = null;
   private JTabbedPane viewersTabPane = null;
   private JTabbedPane featuresTabPane = null;

   private IRawFilePanel selectedRawFilePanel;
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
      this.add(getMainRightComponent(), BorderLayout.CENTER);
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
      tabPane.setTabComponentAt(i, buttonTabComp);
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
         //viewersTabPane.add(rawfile.getName(), plotPanel);
         addRawTab(rawfile.getName(), plotPanel);
         registerRawFilePanel(rawfile, plotPanel);
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
            builder.deleteCharAt(builder.length() - 1);
            name = builder.toString();
         } else {
            name = Integer.toString(rawfiles.size()) + " files";
         }
      } else {
         name = rawfiles.get(0).getName();
      }
      AbstractRawFilePanel plotPanel = new MultiRawFilePanel(rawfiles);
      addRawTab(name, plotPanel);
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
      ExtractionParamsDialog dialog = new ExtractionParamsDialog(this.parentFrame, true);
      dialog.setExtractionParamsTitle("Extract Features Parameters");
      dialog.setLocationRelativeTo(this);
      dialog.showExtractionParamsDialog();
      FeaturesExtractionRequest.Builder builder = dialog.getExtractionParams();
      if (builder != null) {
         builder.setExtractionMethod(FeaturesExtractionRequest.ExtractionMethod.EXTRACT_MS2_FEATURES);
         for (IRawFile rawFile : rawfiles) {
            extractFeatures(rawFile, builder.build());
         }
      }
   }

   private void extractFeatures(final IRawFile rawFile, final FeaturesExtractionRequest params) {
      if ((selectedRawFilePanel != null) && (viewersTabPane.getSelectedIndex() >= 0)) {
         final FeaturesPanel featurePanel = new FeaturesPanel(rawFile, this);
         addFeatureTab(rawFile.getName(), featurePanel);
         fireExtractionEvent(new ExtractionEvent(this, ExtractionEvent.EXTRACTION_STARTED));
         final long start = System.currentTimeMillis();
         SwingWorker worker = new SwingWorker<List<Feature>, Void>() {
            @Override
            protected List<Feature> doInBackground() throws Exception {
               return rawFile.extractFeatures(params);
            }

            @Override
            protected void done() {
               try {
                  List<Feature> features = get();
                  logger.info("{} features/peakels extracted in {}", features.size(), (System.currentTimeMillis() - start) / 1000.0);
                  featurePanel.setFeatures(features);
                  featuresTabPane.setSelectedComponent(featurePanel);
                  fireExtractionEvent(new ExtractionEvent(this, ExtractionEvent.EXTRACTION_DONE));
               } catch (Exception e) {
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
      ExtractionParamsDialog dialog = new ExtractionParamsDialog(this.parentFrame, true);
      dialog.setExtractionParamsTitle("Detect Peakels Parameters");
      dialog.setLocationRelativeTo(this);
      dialog.showExtractionParamsDialog();
      FeaturesExtractionRequest.Builder builder = dialog.getExtractionParams();
      if (builder != null) {
         builder.setExtractionMethod(FeaturesExtractionRequest.ExtractionMethod.DETECT_PEAKELS);
         for (IRawFile rawFile : rawfiles) {
            extractFeatures(rawFile, builder.build());
         }
      }
   }

   public void detectFeatures() {
      detectFeatures(Arrays.asList(selectedRawFilePanel.getCurrentRawfile()));
   }

   public void detectFeatures(List<IRawFile> rawfiles) {
      ExtractionParamsDialog dialog = new ExtractionParamsDialog(this.parentFrame, true);
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
      AbstractRawFilePanel panel = (AbstractRawFilePanel) viewersTabPane.getSelectedComponent();
      if (panel != null) {
         panel.displayFeature(f);
      }
   }

   @Override
   public void extractChromatogramMass(Ms1ExtractionRequest params) {
      AbstractRawFilePanel panel = (AbstractRawFilePanel) viewersTabPane.getSelectedComponent();
      if (panel != null) {
         panel.extractAndDisplayChromatogram(params, panel.getXicModeDisplay(), null);
      }
   }

   public void displayAllRawAction() {
      final List<IRawFile> rawfiles = RawFileManager.getInstance().getAllFiles();
      TabbedMultiRawFilePanel plotPanel = new TabbedMultiRawFilePanel(rawfiles);
      addRawTab("All", plotPanel);
   }

}
