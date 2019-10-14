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

import fr.proline.mzscope.model.*;
import fr.proline.mzscope.model.IChromatogram;
import fr.proline.mzscope.utils.MzScopeCallback;
import fr.proline.mzscope.ui.event.AxisRangeChromatogramListener;
import fr.proline.mzscope.utils.ButtonTabComponent;
import fr.proline.mzscope.utils.Display;
import fr.proline.studio.tabs.IWrappedPanel;
import fr.proline.studio.tabs.TabsPanel;
import fr.proline.studio.utils.CyclicColorPalette;
import fr.proline.studio.utils.IconManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author CB205360
 */
public class TabbedMultiRawFilePanel extends JPanel implements IRawFileViewer {

    final private static Logger logger = LoggerFactory.getLogger(TabbedMultiRawFilePanel.class);

    private JSplitPane splitPane;
    private TabsPanel chromatogramContainerPanel;
    private ChromatogramPanel currentChromatogramPanel;
    protected SpectrumPanel spectrumContainerPanel;
    protected Spectrum currentScan;
    private Display.Mode xicDisplayMode = Display.Mode.REPLACE;
    
    private JToolBar m_toolbarPanel;
    private JPanel m_multiRawFilePanel;
    private JButton m_buttonLayout;
    private JButton m_buttonZoom;
    private JToggleButton overlayBtn;
    private JToggleButton showMS2EventsButton;

    private final static String tooltipForceZoom = "Synchronize zoom on all plots";
    private final static String tooltipZoom = "Remove zoom synchronization on all plots";

    private final List<IRawFile> rawfiles;
    private final Map<IRawFile, IChromatogram> mapChromatogramForRawFile;
    
    private final Map<IRawFile, IRawFileLoading> mapRawFileLoading;
    private boolean isZoomSynchronized = true;
    // keep percentage of the zoom and the relative value zoomed
    private Double relativeXValue = Double.NaN;
    private Double zoomXLevel = Double.NaN;
    private Double relativeYValue = Double.NaN;
    private Double zoomYLevel = Double.NaN;
    
    
    public TabbedMultiRawFilePanel(List<IRawFile> rawfiles) {
        super();
        this.rawfiles = rawfiles;
        mapChromatogramForRawFile = new HashMap();
        mapRawFileLoading = new HashMap();
        for (IRawFile rawFile : rawfiles) {
            mapChromatogramForRawFile.put(rawFile, null);
        }
        initComponents();
        spectrumContainerPanel.initComponents();
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
            splitPane.setTopComponent(getMultiRawFilePanel());
            splitPane.setBottomComponent(getSpectrumContainerPanel());
        }
        return splitPane;
    }
    
    private JPanel getMultiRawFilePanel(){
        if (m_multiRawFilePanel == null){
            m_multiRawFilePanel = new JPanel();
            m_multiRawFilePanel.setLayout(new BorderLayout());
            m_multiRawFilePanel.add(getChromatogramContainerPanel(), BorderLayout.CENTER);
            m_multiRawFilePanel.add(getToolBar(), BorderLayout.NORTH);
        }
        return m_multiRawFilePanel;
    }
    
    private JToolBar getToolBar(){
        if (this.m_toolbarPanel == null){
            m_toolbarPanel = new JToolBar();
            m_toolbarPanel.setFloatable(false);
            m_toolbarPanel.setRollover(true);
        
            m_toolbarPanel.add(getButtonLayout());
            m_toolbarPanel.addSeparator();
            
            m_toolbarPanel.add(getButtonForceZoom());
            
            m_toolbarPanel.addSeparator();
            JButton displayTICbtn = new JButton("TIC");
            displayTICbtn.setToolTipText("Display TIC IChromatogram");
            displayTICbtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    displayTIC();
                }
            });
            m_toolbarPanel.add(displayTICbtn);
            
            JButton displayBPIbtn = new JButton("BPC");
            displayBPIbtn.setToolTipText("Display Base Peak IChromatogram");
            displayBPIbtn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    displayBPI();
                }
            });
            m_toolbarPanel.add(displayBPIbtn);
            m_toolbarPanel.add(getOverlayBtn());
            
        }
        return this.m_toolbarPanel;
    }
    
    private JButton getButtonLayout(){
        if(m_buttonLayout == null){
            m_buttonLayout = new JButton();
            m_buttonLayout.setIcon(IconManager.getIcon(IconManager.IconType.GRID));
            m_buttonLayout.setToolTipText("Change the presentation: tabs/grid");
            m_buttonLayout.addActionListener((ActionEvent e) -> {
                chromatogramContainerPanel.changeLayout();
            });
        }
        return m_buttonLayout;
    }
    
    private JButton getButtonForceZoom(){
        if(m_buttonZoom == null){
            m_buttonZoom = new JButton();
            m_buttonZoom.setIcon(IconManager.getIcon(IconManager.IconType.ZOOM_FIT));
            updateZoomValues();
            m_buttonZoom.addActionListener((ActionEvent e) -> {
                synchronizeZoom();
            });
        }
        return m_buttonZoom;
    }
    

    private TabsPanel getChromatogramContainerPanel() {
        if (this.chromatogramContainerPanel == null) {
            chromatogramContainerPanel = new TabsPanel();
            chromatogramContainerPanel.setName("chromatogramContainerPanel");
            long id = 0;
            for (IRawFile rawFile : rawfiles) {
                ButtonTabComponent buttonTabComp = new ButtonTabComponent(rawFile.getName());
                IRawFileLoading rawFileLoading = (boolean waitingState) -> {
                    buttonTabComp.setWaitingState(waitingState);
                };
                mapRawFileLoading.put(rawFile, rawFileLoading);
                buttonTabComp.addCloseTabListener(new ButtonTabComponent.CloseTabListener() {

                    @Override
                    public void closeTab(ButtonTabComponent buttonTabComponent) {
                        int index = chromatogramContainerPanel.indexOfTabHeaderComponent(buttonTabComponent);
                        if (index != -1) {
                            chromatogramContainerPanel.removeTabAt(index);
                            rawfiles.remove(index);
                        }
                    }
                    
                });
                IWrappedPanel wpanel = new WrappedChromatogramPanel(id++, rawFile.getName(), getChromatogramPanel(rawFile));
                wpanel.setTabHeaderComponent(buttonTabComp);
                chromatogramContainerPanel.addTab(wpanel);
            }
            
        }
        chromatogramContainerPanel.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (chromatogramContainerPanel.getSelectedIndex() != -1){
                    currentChromatogramPanel = (ChromatogramPanel) chromatogramContainerPanel.getComponentAt(chromatogramContainerPanel.getSelectedIndex());
                }
            }
        });
        if (chromatogramContainerPanel.getTabCount() > 0) {
            chromatogramContainerPanel.setSelectedIndex(0);
            currentChromatogramPanel = (ChromatogramPanel) chromatogramContainerPanel.getComponentAt(0);
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
        chromatogramPanel.addListener(new AxisRangeChromatogramListener() {
            @Override
            public void updateAxisRange(double[] oldX, double[] newX,  double[] oldY,  double[] newY) {
                synchronizeAxisRange(rawFile, oldX, newX, oldY, newY);
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
        IChromatogram c = getCurrentChromatogram();
        if (c != null) {
            for (Map.Entry<IRawFile, IChromatogram> entrySet : mapChromatogramForRawFile.entrySet()) {
                if (c.equals(entrySet.getValue())) {
                    return entrySet.getKey();
                }
            }
        }
        return null;
    }

    @Override
    public void extractAndDisplayChromatogram(final MsnExtractionRequest params, Display display, MzScopeCallback callback) {
        // in this implementation displayMode is ignored : always REPLACE since we will extract one IChromatogram per RawFile
        SwingWorker worker = new SwingWorker<Integer, IChromatogram>() {
            int count = 0;

            @Override
            protected Integer doInBackground() throws Exception {

                for (IRawFile rawFile : rawfiles) {
                    mapRawFileLoading.get(rawFile).setWaitingState(true);
                    IChromatogram c = rawFile.getXIC(params);
                    count++;
                    publish(c);
                }
                return count;
            }

            @Override
            protected void process(List<IChromatogram> chunks) {
                for (int k = 0; k < chunks.size(); k++) {
                    displayChromatogram(chunks.get(k), display);
                }
            }

            @Override
            protected void done() {
                try {
                    logger.info("{} TIC chromatogram extracted", get());
                } catch (InterruptedException | ExecutionException e) {
                    logger.error("Error while reading chromatogram");
                } finally {
                    rawfiles.stream().forEach((rawFile) -> {
                        mapRawFileLoading.get(rawFile).setWaitingState(false);
                    });
                }
            }
        };

        worker.execute();
    }

    @Override
    public Color displayChromatogram(IChromatogram chromato, Display display) {
        String rawFileName = chromato.getRawFilename();
        int nbTab = chromatogramContainerPanel.getTabCount();
        Color c = null;
        for (int t = 0; t < nbTab; t++) {
            String tabTitle = chromatogramContainerPanel.getTitleAt(t);
            if (tabTitle.equals(rawFileName)) {  // see how to better get the tabComponent linked to a rawFile
                ChromatogramPanel chromatoPanel = (ChromatogramPanel) chromatogramContainerPanel.getComponentAt(t);
                c = chromatoPanel.displayChromatogram(chromato, display);
                mapChromatogramForRawFile.put(getRawFile(rawFileName), chromato);
                if (isZoomSynchronized && (!zoomXLevel.isNaN() && !zoomYLevel.isNaN())) {
                    chromatoPanel.updateAxisRange(zoomXLevel, relativeXValue, zoomYLevel, relativeYValue);
                }
            }
        }
        return c;
    }

    // override display feature to display all xic
    @Override
    public void displayFeature(final IFeature f) {
        // TODO : how to handle this simple display of a unique feature ? In this case multiple Features must be displayed, one by RawFile
    }

    @Override
    public void displayScan(long index) {
        IRawFile selectedRawFile = getCurrentRawfile();
        if (selectedRawFile != null && ((currentScan == null) || (index != currentScan.getIndex()) )) {
            currentScan = selectedRawFile.getSpectrum((int) index);
            if (currentScan != null) {
                spectrumContainerPanel.displayScan(currentScan);
            }
            int idP = rawfiles.indexOf(selectedRawFile);
            if (idP != -1){
                chromatogramContainerPanel.setSelectedIndex(idP);
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
    public IChromatogram getCurrentChromatogram() {
        return (currentChromatogramPanel == null) ? null : currentChromatogramPanel.getCurrentChromatogram();
    }

    @Override
    public Iterable<IChromatogram> getAllChromatograms() {
        int nbTab = chromatogramContainerPanel.getTabCount();
        List<IChromatogram> list = new ArrayList<>();
        for (int t = 0; t < nbTab; t++) {
           ChromatogramPanel chromatoPanel = (ChromatogramPanel) chromatogramContainerPanel.getComponentAt(t);
           chromatoPanel.getChromatograms().forEach(c -> list.add(c));
        }
        
        return list;
    }
    
    private void displayTIC() {

        SwingWorker worker = new SwingWorker<Integer, IChromatogram>() {
            int count = 0;

            @Override
            protected Integer doInBackground() throws Exception {
                    
                for (IRawFile rawFile : rawfiles) {
                    mapRawFileLoading.get(rawFile).setWaitingState(true);
                    IChromatogram c = rawFile.getTIC();
                    count++;
                    publish(c);
                }
                return count;
            }

            @Override
            protected void process(List<IChromatogram> chunks) {
                for (int k = 0; k < chunks.size(); k++) {
                    logger.info("display  chromato");
                    displayChromatogram(chunks.get(k), new Display(Display.Mode.REPLACE));
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
    
    private void displayBPI() {

        SwingWorker worker = new SwingWorker<Integer, IChromatogram>() {
            int count = 0;

            @Override
            protected Integer doInBackground() throws Exception {
                    
                for (IRawFile rawFile : rawfiles) {
                    mapRawFileLoading.get(rawFile).setWaitingState(true);
                    IChromatogram c = rawFile.getBPI();
                    count++;
                    publish(c);
                }
                return count;
            }

            @Override
            protected void process(List<IChromatogram> chunks) {
                for (int k = 0; k < chunks.size(); k++) {
                    logger.info("display  chromato");
                    displayChromatogram(chunks.get(k), new Display(Display.Mode.REPLACE));
                }
            }

            @Override
            protected void done() {
                try {
                    logger.info("{} BPI chromatogram extracted", get());
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
    
    public void displayChromatograms(Map<IRawFile, IChromatogram> chromatogramByRawFile) {
        int nbTab = chromatogramContainerPanel.getTabCount();
        for (Map.Entry<IRawFile, IChromatogram> entrySet : chromatogramByRawFile.entrySet()) {
            IRawFile rawFile = entrySet.getKey();
            IChromatogram chromato = entrySet.getValue();
            for (int t = 0; t < nbTab; t++) {
                String tabTitle = chromatogramContainerPanel.getTitleAt(t);
                if (tabTitle.equals(rawFile.getName())) {  // see how to better get the tabComponent linked to a rawFile
                    ChromatogramPanel chromatoPanel = (ChromatogramPanel) chromatogramContainerPanel.getComponentAt(t);
                    chromatoPanel.displayChromatogram(chromato, new Display(Display.Mode.REPLACE));
                    mapChromatogramForRawFile.put(rawFile, chromato);
                }
            }
        }
    }
    
    private void synchronizeZoom(){
        isZoomSynchronized = !isZoomSynchronized;
        updateZoomValues();
    }
    
    private void updateZoomValues(){
        m_buttonZoom.setToolTipText(isZoomSynchronized?tooltipZoom : tooltipForceZoom);
        if(!isZoomSynchronized){
            relativeXValue = Double.NaN;
            zoomXLevel = Double.NaN;
            relativeYValue = Double.NaN;
            zoomYLevel = Double.NaN;
        }
    }
    
    
//    private  void synchronizeAxisRange(IRawFile rawFile, double[] oldX, double[] newX,  double[] oldY,  double[] newY){
//        if (isZoomSynchronized){
//            double oldMinX = oldX[0];
//            double oldMaxX = oldX[1];
//            double newMinX = newX[0];
//            double newMaxX = newX[1];
//            double oldMinY = oldY[0];
//            double oldMaxY = oldY[1];
//            double newMinY = newY[0];
//            double newMaxY = newY[1];
//            
//            logger.debug("moving from min={}, max={} to min={}, max={} ", oldMinX, oldMaxX, newMinX, newMaxX);
//            zoomXLevel = (double)((newMaxX - newMinX)*100.0/(oldMaxX - oldMinX));
//            relativeXValue = (double)(100.0 * ((newMinX+((newMaxX-newMinX) /2.0)) - oldMinX)/(oldMaxX - oldMinX));
//            zoomYLevel = (double)((newMaxY - newMinY)*100.0/(oldMaxY - oldMinY));
//            relativeYValue = (double)(100.0 * ((newMinY+((newMaxY-newMinY) /2.0)) - oldMinY)/(oldMaxY - oldMinY));
//            
//            logger.debug("leads to zoomLevel={}, relativeX={}", zoomXLevel, relativeXValue);
//
//            int nbTab = chromatogramContainerPanel.getTabCount();
//            for (int t = 0; t < nbTab; t++) {
//                String tabTitle = chromatogramContainerPanel.getTitleAt(t);
//                if (!tabTitle.equals(rawFile.getName())) {  // see how to better get the tabComponent linked to a rawFile
//                 ChromatogramPanel chromatoPanel = (ChromatogramPanel) chromatogramContainerPanel.getComponentAt(t);
//                 chromatoPanel.updateAxisRange(zoomXLevel, relativeXValue, zoomYLevel, relativeYValue);
//                } else {
//                    logger.debug("in source rawfile {} ", rawFile.getName());
//                    ChromatogramPanel chromatoPanel = (ChromatogramPanel) chromatogramContainerPanel.getComponentAt(t);
//                    chromatoPanel.computeAxisRange(zoomXLevel, relativeXValue, oldMinX, oldMaxX);
//                }
//            }
//        }
//    }
    
        private  void synchronizeAxisRange(IRawFile rawFile, double[] oldX, double[] newX,  double[] oldY,  double[] newY){
        if (isZoomSynchronized){
            double newMinX = newX[0];
            double newMaxX = newX[1];

            double oldMinY = oldY[0];
            double oldMaxY = oldY[1];
            double newMinY = newY[0];
            double newMaxY = newY[1];
            
            int nbTab = chromatogramContainerPanel.getTabCount();
            IChromatogram referenceChromato = null;
            for (int t = 0; t < nbTab; t++) {
                String tabTitle = chromatogramContainerPanel.getTitleAt(t);
                if (tabTitle.equals(rawFile.getName())) {  // see how to better get the tabComponent linked to a rawFile
                 ChromatogramPanel chromatoPanel = (ChromatogramPanel) chromatogramContainerPanel.getComponentAt(t);
                 referenceChromato = chromatoPanel.getCurrentChromatogram();
                } 
            }
            
//            logger.debug("moving to min={}, max={} ", newMinX, newMaxX);
            double zoomXRange = (double)(newMaxX - newMinX);
            double relativeXMinPosition = (double)(newMinX/(referenceChromato.getElutionEndTime() - referenceChromato.getElutionStartTime()));

            zoomYLevel = (double)((newMaxY - newMinY)*100.0/(oldMaxY - oldMinY));
            relativeYValue = (double)(100.0 * ((newMinY+((newMaxY-newMinY) /2.0)) - oldMinY)/(oldMaxY - oldMinY));
            
//            logger.debug("leads to zoomRange={}, relativeX={}", zoomXRange, relativeXMinPosition);

            for (int t = 0; t < nbTab; t++) {
                String tabTitle = chromatogramContainerPanel.getTitleAt(t);
                if (!tabTitle.equals(rawFile.getName())) {  // see how to better get the tabComponent linked to a rawFile
                 ChromatogramPanel chromatoPanel = (ChromatogramPanel) chromatogramContainerPanel.getComponentAt(t);
                 chromatoPanel.updateAxisRange2(zoomXRange, relativeXMinPosition, zoomYLevel, relativeYValue);
                } 
            }
        }
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
       
     private void displayMsMsEvents(boolean showMsMsEvents) {
        if (showMsMsEvents) {
            
//            final double minMz = chromatogramPanel.getCurrentChromatogram().minMz;
//            final double maxMz = chromatogramPanel.getCurrentChromatogram().maxMz;
//
//            SwingWorker worker = new SwingWorker<List<Float>, Void>() {
//                @Override
//                protected List<Float> doInBackground() throws Exception {
//                    return getCurrentRawfile().getMsMsEvent(minMz, maxMz);
//                }
//
//                @Override
//                protected void done() {
//                    try {
//                        chromatogramPanel.showMSMSEvents(get());
//                    } catch (InterruptedException | ExecutionException e) {
//                        logger.error("Error while reading chromatogram");
//                    }
//                }
//            };
//            worker.execute();
//        } else {
//            chromatogramPanel.hideMSMSEvents();
        }
    }

    @Override
    public Display.Mode getXicDisplayMode() {
        return xicDisplayMode;
    }

}

class WrappedChromatogramPanel implements IWrappedPanel{
    
    private ChromatogramPanel chromatogramPanel;
    private String title;
    private Long id;
    private ButtonTabComponent tabHeaderComponent;

    public WrappedChromatogramPanel(Long id, String title, ChromatogramPanel chromatogramPanel) {
        this.id = id;
        this.chromatogramPanel = chromatogramPanel;
        this.title= title;
    }
    
    

    @Override
    public String getTitle() {
        return this.title;
    }

    @Override
    public JPanel getComponent() {
        return this.chromatogramPanel;
    }

    @Override
    public Long getId() {
        return this.id;
    }
    
    @Override
    public void setTabHeaderComponent(Component c){
        if (c instanceof ButtonTabComponent)
            this.tabHeaderComponent = (ButtonTabComponent)c;
    }
    
    @Override
    public Component getTabHeaderComponent(){
        return tabHeaderComponent;
    }
    
}

