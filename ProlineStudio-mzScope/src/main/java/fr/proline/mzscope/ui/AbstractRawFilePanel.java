package fr.proline.mzscope.ui;

import fr.profi.mzdb.model.Feature;
import fr.proline.mzscope.model.Chromatogram;
import fr.proline.mzscope.model.IRawFile;
import fr.proline.mzscope.model.MzScopePreferences;
import fr.proline.mzscope.model.Scan;
import fr.proline.mzscope.util.KeyEventDispatcherDecorator;
import fr.proline.mzscope.util.MzScopeConstants;
import fr.proline.studio.export.ExportButton;
import fr.proline.studio.graphics.PlotLinear;
import fr.proline.studio.graphics.BasePlotPanel;
import fr.proline.studio.graphics.PlotPanel;
import fr.proline.studio.graphics.PlotPanelListener;
import fr.proline.studio.graphics.marker.IntervalMarker;
import fr.proline.studio.graphics.marker.LineMarker;
import fr.proline.studio.utils.CyclicColorPalette;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.KeyEventDispatcher;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
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
 * base for raw file panel. The panel could be a SingleRawFilePanel or MultiRawFilePanel
 * composed by 2 main components: ChromatogramPanel and SpectrumPanel (if scan is displayed)
 * @author CB205360
 */
public abstract class AbstractRawFilePanel extends JPanel implements IRawFilePlot, KeyEventDispatcher, PlotPanelListener{

    final private static Logger logger = LoggerFactory.getLogger("ProlineStudio.mzScope.AbstractRawFilePanel");
    final private static DecimalFormat xFormatter = new DecimalFormat("0.0000");
    final private static DecimalFormat yFormatter = new DecimalFormat("0.###E0");
    final private static Font tickLabelFont = new Font("SansSerif", java.awt.Font.PLAIN, 10);
    final private static Font titleFont = new Font("SansSerif", java.awt.Font.PLAIN, 12);
    
    private boolean displayScan = true;
    
    private JSplitPane splitPane;
    private JPanel mainPanel;
    private JPanel chromatogramContainerPanel;
    private SpectrumPanel spectrumContainerPanel;
    
    protected BasePlotPanel chromatogramPlotPanel;
    protected JToolBar chromatogramToolbar;
    protected List<PlotLinear> chromatogramPlots;
    protected Chromatogram currentChromatogram;
    protected List<Chromatogram> listChromatogram;
    protected Scan currentScan;
    protected Float currentScanTime = null;
    
    
    protected List<LineMarker> listMsMsMarkers;
    protected LineMarker currentScanMarker; 
    protected JToggleButton displayMS2btn;
    
    
    public AbstractRawFilePanel() {
        super();
        init();
    }
    
    /**
     * If displayScan is true, display the chromatogram and the scan, otherwise display only the chromatogram
     * @param displayScan 
     */
    public void setDisplayScan(boolean displayScan){
        this.displayScan = displayScan;
    }
    
    private void init() {
        listMsMsMarkers = new ArrayList();
        listChromatogram = new ArrayList();
        initComponents();
        initChartPanels();
        KeyEventDispatcherDecorator.addKeyEventListener(this);
    }
    
    private void initComponents() {
        this.setLayout(new BorderLayout());
        this.add(getMainPanel(), BorderLayout.CENTER);
    }
    
    
    private JPanel getMainPanel(){
        if (mainPanel == null){
            mainPanel = new JPanel();
            mainPanel.setName("mainPanel");
            mainPanel.setLayout(new BorderLayout());
            if (displayScan){
                mainPanel.add(getSplitPane(), BorderLayout.CENTER);
            }else{
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
    
    private JPanel getChromatogramContainerPanel(){
        if (this.chromatogramContainerPanel == null){
           chromatogramContainerPanel =new JPanel();
           chromatogramContainerPanel.setName("chromatogramContainerPanel");
           chromatogramContainerPanel.setLayout(new BorderLayout());
        }
        return chromatogramContainerPanel;
    }
    
    private JPanel getSpectrumContainerPanel(){
        if (this.spectrumContainerPanel == null){
           spectrumContainerPanel =new SpectrumPanel(this);
           spectrumContainerPanel.setName("spectrumContainerPanel");
           spectrumContainerPanel.setLayout(new BorderLayout());
        }
        return spectrumContainerPanel;
    }
    
    private void initChartPanels(){
        // create ChromatogramPanelPlot
        PlotPanel plotPanel = new PlotPanel();
        chromatogramPlotPanel  = plotPanel.getBasePlotPanel();
        chromatogramPlotPanel.addListener(this);
        chromatogramPlotPanel.setDrawCursor(true);
        chromatogramPlotPanel.repaint();
        currentScanMarker = new LineMarker(chromatogramPlotPanel, 0.0, LineMarker.ORIENTATION_VERTICAL, Color.BLUE, false);
        chromatogramPlots = new ArrayList();
        chromatogramContainerPanel.add(plotPanel, BorderLayout.CENTER);
        chromatogramContainerPanel.add(getChromatogramToolbar(), BorderLayout.WEST);
             
        // Create Scan Charts
        spectrumContainerPanel.initChart();
    }
    
    private JToolBar getChromatogramToolbar(){
        chromatogramToolbar = new JToolBar(JToolBar.VERTICAL);
        chromatogramToolbar.setFloatable(false);
        ExportButton exportImageButton = new ExportButton("Graphic", chromatogramPlotPanel);
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

        JButton displayBPIbtn = new JButton("BPI");
        displayBPIbtn.setToolTipText("Display BPI Chromatogram");
        displayBPIbtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                displayBPI();
            }
        });
        chromatogramToolbar.add(displayBPIbtn);
        
        // MS/MS events
        displayMS2btn = new JToggleButton("MS2", false);
        displayMS2btn.setToolTipText("Show/Hide MS2 Events");
        displayMS2btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                displayMsMsEvents(displayMS2btn.isSelected());
            }
        });
        chromatogramToolbar.add(displayMS2btn);
        setMsMsEventButtonEnabled(false);
        
        chromatogramToolbar.setFloatable(false);
        chromatogramToolbar.setRollover(true);

        return chromatogramToolbar;
    }
    
    protected void setMsMsEventButtonEnabled(boolean b) {
        this.displayMS2btn.setEnabled(b);
        if (!b) {
            hideMSMSEvents();
        }
    }
    /*
    protected void chromatogramMouseClicked(ChartMouseEvent event) {
        XYPlot xyplot = chromatogramPanel.getChart().getXYPlot();
        double d = xyplot.getDomainAxis().java2DToValue(event.getTrigger().getX(), chromatogramPanel.getScreenDataArea(), xyplot.getDomainAxisEdge());
        int scanIdx = getCurrentRawfile().getScanId(d * 60.0);
        displayScan(scanIdx);
    }*/
    
   
    public void scanMouseClicked(MouseEvent e, double minMz, double maxMz, int xicModeDisplay){
        if ((e.getModifiers() & KeyEvent.ALT_MASK) != 0) {
            addChromatogram(minMz, maxMz);
        } else {
            switch (xicModeDisplay) {
                case MzScopeConstants.MODE_DISPLAY_XIC_REPLACE: {
                    extractChromatogram(minMz, maxMz);
                    break;
                }
                case MzScopeConstants.MODE_DISPLAY_XIC_OVERLAY: {
                    addChromatogram(minMz, maxMz);
                    break;
                }
            }
        }
    }
    
    public void addChromatogram(double minMz, double maxMz) {
        SwingWorker worker = new AbstractXICExtractionWorker(getCurrentRawfile(), minMz, maxMz) {
            @Override
            protected void done() {
                try {
                    addChromatogram(get());
                } catch (InterruptedException | ExecutionException e) {
                    logger.error("Error while extraction chromatogram", e);
                }
            }
        };
        worker.execute();
    }
    
   
    public int getXicModeDisplay(){
        if (displayScan){
            spectrumContainerPanel.getXicModeDisplay();
        }
        return MzScopeConstants.MODE_DISPLAY_XIC_REPLACE;
    }
    
    private void displayMsMsEvents(boolean showMsMsEvents){
        if (showMsMsEvents){
            showMSMSEvents();
        }else{
            hideMSMSEvents();
        }
    }
    
    public void showMSMSEvents() {
        //ValueAxis axis = chromatogramPanel.getChart().getXYPlot().getDomainAxis();
        //final double min = axis.getRange().getLowerBound();
        //final double max = axis.getRange().getUpperBound();
        if (currentChromatogram == null) {
            return;
        }
        final double minMz = currentChromatogram.minMz;
        final double maxMz = currentChromatogram.maxMz;

        SwingWorker worker = new SwingWorker<List<Float>, Void>() {
            @Override
            protected List<Float> doInBackground() throws Exception {
                return getCurrentRawfile().getMsMsEvent(minMz, maxMz);
            }

            @Override
            protected void done() {
                try {
                    List<Float> listMsMsTime = get();
                    PlotLinear chromatogramPlot= chromatogramPlots.isEmpty()?null:chromatogramPlots.get(0);
                    if (chromatogramPlot != null){
                        for (Float time : listMsMsTime) {
                            LineMarker marker = new LineMarker(chromatogramPlotPanel, time / 60.0, CyclicColorPalette.getColor(8));
                            listMsMsMarkers.add(marker);
                            chromatogramPlot.addMarker(marker);
                            chromatogramPlotPanel.repaintUpdateDoubleBuffer();
                            //chromatogramPanel.getChart().getXYPlot().addDomainMarker(marker);
                        }
                    }
                } catch (InterruptedException | ExecutionException e) {
                    logger.error("Error while reading chromatogram");
                }
            }
        };
        worker.execute();
    }

    public void hideMSMSEvents() {
        PlotLinear chromatogramPlot= chromatogramPlots.isEmpty()?null:chromatogramPlots.get(0);
        if (chromatogramPlot != null){
            for (LineMarker marker : listMsMsMarkers) {
                chromatogramPlot.removeMarker(marker);
            }
            chromatogramPlotPanel.repaintUpdateDoubleBuffer();
        }
        /*for (Marker marker : listMsMsMarkers) {
            chromatogramPanel.getChart().getXYPlot().removeDomainMarker(marker);
        }*/
        listMsMsMarkers = new ArrayList();
    }
    
    
    @Override
    public Color displayChromatogram(Chromatogram chromato) {
        setMsMsEventButtonEnabled(true);
        this.currentChromatogram = chromato;
        listChromatogram = new ArrayList();
        listChromatogram.add(currentChromatogram);
        StringBuilder builder = new StringBuilder("Mass range: ");
        builder.append(xFormatter.format(chromato.minMz)).append("-").append(xFormatter.format(chromato.maxMz));
        chromatogramPlotPanel.setPlotTitle(builder.toString());
        chromatogramPlotPanel.clearPlots();
        PlotLinear chromatogramPlot= chromatogramPlots.isEmpty()?null:chromatogramPlots.get(0);
        if (chromatogramPlot != null){
            chromatogramPlot.clearMarkers();
        }
        chromatogramPlots = new ArrayList();
        Color plotColor = CyclicColorPalette.getColor(1);
        ChromatogramXICModel chromatoModel = new ChromatogramXICModel(currentChromatogram);
        chromatoModel.setColor(plotColor);
        chromatogramPlot = new PlotLinear(chromatogramPlotPanel, chromatoModel, null, ChromatogramXICModel.COLTYPE_CHROMATOGRAM_XIC_TIME, ChromatogramXICModel.COLTYPE_CHROMATOGRAM_XIC_INTENSITIES);
        chromatogramPlot.setPlotInformation(chromatoModel.getPlotInformation());
        chromatogramPlot.setIsPaintMarker(true);
        chromatogramPlot.setStrokeFixed(true);
        chromatogramPlot.addMarker(currentScanMarker);
        if (currentScan != null){
            currentScanMarker.setValue(currentScan.getRetentionTime()/60.0);
        }
        chromatogramPlotPanel.setPlot(chromatogramPlot);
        chromatogramPlotPanel.lockMinXValue();
        chromatogramPlotPanel.lockMinYValue();
        chromatogramPlotPanel.repaintUpdateDoubleBuffer();
        chromatogramPlots.add(chromatogramPlot);
        displayMsMsEvents(displayMS2btn.isSelected());
        return plotColor;
    }

    @Override
    public Color addChromatogram(Chromatogram chromato) {
        listChromatogram.add(chromato);
        double xMin = Double.NaN, xMax = Double.NaN;
        if (chromatogramPlotPanel.hasPlots()) {
           xMin = chromatogramPlotPanel.getXAxis().getMinValue();
           xMax = chromatogramPlotPanel.getXAxis().getMaxValue();           
        }
        Color plotColor = CyclicColorPalette.getColor(chromatogramPlots.size()+1);
        ChromatogramXICModel chromatoModel = new ChromatogramXICModel(chromato);
        chromatoModel.setColor(plotColor);
        PlotLinear chromatogramPlot = new PlotLinear(chromatogramPlotPanel, chromatoModel, null, ChromatogramXICModel.COLTYPE_CHROMATOGRAM_XIC_TIME, ChromatogramXICModel.COLTYPE_CHROMATOGRAM_XIC_INTENSITIES);
        chromatogramPlot.setPlotInformation(chromatoModel.getPlotInformation());
        chromatogramPlot.setIsPaintMarker(true);
        chromatogramPlot.setStrokeFixed(true);
        chromatogramPlotPanel.addPlot(chromatogramPlot);
        if (!Double.isNaN(xMax) && !Double.isNaN(xMin)) {
           chromatogramPlotPanel.getXAxis().setRange(xMin, xMax);
        }
        /*if (currentScan != null && !chromatogramPlots.isEmpty()){
            chromatogramPlots.get(0).clearMarkers();
            chromatogramPlots.get(0).addMarker(new LineMarker(chromatogramPlotPanel, currentScan.getRetentionTime(), Color.BLUE));
        }*/
        chromatogramPlotPanel.repaintUpdateDoubleBuffer();
        chromatogramPlots.add(chromatogramPlot);
        /*
        XYSeries series = new XYSeries(chromato.rawFile.getName()+"-"+chromato.minMz);
        for (int k = 0; k < chromato.intensities.length; k++) {
            series.add(chromato.time[k], chromato.intensities[k]);
        }
        XYPlot xyplot = chromatogramPanel.getChart().getXYPlot();
        ((XYSeriesCollection) xyplot.getDataset()).addSeries(series);
        Color plotColor = CyclicColorPalette.getColor(xyplot.getDataset().getSeriesCount());
        xyplot.getRenderer().setSeriesPaint(xyplot.getDataset().getSeriesCount() - 1, plotColor);*/
        return plotColor;
    }

    
    @Override
    public void extractChromatogram(double minMz, double maxMz) {
        SwingWorker worker = new AbstractXICExtractionWorker(getCurrentRawfile(), minMz, maxMz) {
            @Override
            protected void done() {
                try {
                    displayChromatogram(get());
                    setMsMsEventButtonEnabled(true);
                } catch (InterruptedException | ExecutionException e) {
                    logger.error("Error while extraction chromatogram", e);
                }
            }
        };
        worker.execute();
    }

    
    @Override
    public void displayFeature(final Feature f) {
        double ppm = MzScopePreferences.getInstance().getMzPPMTolerance();
        final double maxMz = f.getMz() + f.getMz() * ppm / 1e6;
        final double minMz = f.getMz() - f.getMz() * ppm / 1e6;

        SwingWorker worker = new SwingWorker<Chromatogram, Void>() {
            @Override
            protected Chromatogram doInBackground() throws Exception {
                return getCurrentRawfile().getXIC(minMz, maxMz);
            }

            @Override
            protected void done() {
                try {
                    displayChromatogram(get());
                    displayScan(f.getBasePeakel().getApexScanId());
                    PlotLinear chromatogramPlot= chromatogramPlots.isEmpty()?null:chromatogramPlots.get(0);
                    if (chromatogramPlot != null){
                        chromatogramPlot.clearMarkers();
                        chromatogramPlot.addMarker(new IntervalMarker(chromatogramPlotPanel, Color.ORANGE, Color.RED, f.getBasePeakel().getFirstElutionTime() / 60.0, f.getBasePeakel().getLastElutionTime() / 60.0));
                        currentScanMarker.setValue(f.getElutionTime() / 60.0);
                        chromatogramPlot.addMarker(currentScanMarker);
                    }
                } catch (InterruptedException | ExecutionException e) {
                    logger.error("Error while reading chromatogram");
                }
            }
        };
        worker.execute();
    }

    @Override
    public void displayScan(int index) {
        if ((currentScan == null) || (index != currentScan.getIndex())) {
            currentScan = getCurrentRawfile().getScan(index);
            if (currentScan != null) {
                currentScanTime = currentScan.getRetentionTime();
                currentScanMarker.setValue(currentScanTime / 60.0);
                chromatogramPlotPanel.repaintUpdateDoubleBuffer();
                if (displayScan){
                    spectrumContainerPanel.displayScan(currentScan);
                    for (Chromatogram chromato : listChromatogram) {
                        if ((chromato.minMz != -1) && (chromato.maxMz != -1) && (currentScan.getMsLevel() == 1)) {
                            spectrumContainerPanel.addMarkerRange(chromato.minMz, chromato.maxMz);
                        }
                    }
                    /*if ((currentChromatogram.minMz != -1) && (currentChromatogram.maxMz != -1) && (currentScan.getMsLevel() == 1)) {
                       spectrumContainerPanel.addMarkerRange(currentChromatogram.minMz, currentChromatogram.maxMz);
                    }*/
                }
            }
        }
    }

    

    @Override
    public Chromatogram getCurrentChromatogram() {
        return currentChromatogram;
    }

    @Override
    public void extractChromatogramWithFeature(double minMz, double maxMz, final double elutionTime, final double firstScanTime, final double lastScanTime) {
        SwingWorker worker = new AbstractXICExtractionWorker(getCurrentRawfile(), minMz, maxMz) {
            @Override
            protected void done() {
                try {
                    displayChromatogram(get());
                    setMsMsEventButtonEnabled(true);
                    PlotLinear chromatogramPlot= chromatogramPlots.isEmpty()?null:chromatogramPlots.get(0);
                    if (chromatogramPlot != null){
                        chromatogramPlot.clearMarkers();
                        chromatogramPlot.addMarker(new IntervalMarker(chromatogramPlotPanel, Color.ORANGE, Color.RED, firstScanTime / 60.0, lastScanTime / 60.0));
                        currentScanMarker.setValue(elutionTime / 60.0);
                        chromatogramPlot.addMarker(currentScanMarker);
                    }
                    
                    /*XYPlot xyplot = chromatogramPanel.getChart().getXYPlot();
                    xyplot.clearDomainMarkers();
                    Marker marker = new IntervalMarker(firstScanTime / 60.0, lastScanTime / 60.0, Color.ORANGE, new BasicStroke(1), Color.RED, new BasicStroke(1), 0.3f);
                    xyplot.addDomainMarker(marker);
                    marker = new ValueMarker(elutionTime / 60.0);
                    xyplot.addDomainMarker(marker);*/
                } catch (InterruptedException | ExecutionException e) {
                    logger.error("Error while extraction chromatogram", e);
                }
            }
        };
        worker.execute();
    }
    
    @Override
    public void plotPanelMouseClicked(MouseEvent e, double xValue, double yValue) {
        //XYPlot xyplot = chromatogramPanel.getChart().getXYPlot();
        //double d = xyplot.getDomainAxis().java2DToValue(event.getTrigger().getX(), chromatogramPanel.getScreenDataArea(), xyplot.getDomainAxisEdge());
        double d = xValue;
        int scanIdx = getCurrentRawfile().getScanId(d * 60.0);
        displayScan(scanIdx);
        if (!chromatogramPlots.isEmpty()){
            currentScanMarker.setVisible(true);
            currentScanMarker.setValue(xValue);
        }
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
                displayScan(getCurrentRawfile().getPreviousScanId(currentScan.getIndex(), currentScan.getMsLevel()));
                e.consume();
                return true;
            } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                displayScan(getCurrentRawfile().getNextScanId(currentScan.getIndex(), currentScan.getMsLevel()));
                e.consume();
                return true;
            }

        } else {
            if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                displayScan(currentScan.getIndex() - 1);
                e.consume();
                return true;
            } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                displayScan(currentScan.getIndex() + 1);
                e.consume();
                return true;
            }
        }
        return false;
    }

    protected abstract void displayTIC();

    protected abstract void displayBPI();
    
    protected abstract Color getPlotColor(IRawFile rawFile);
    
    
}
