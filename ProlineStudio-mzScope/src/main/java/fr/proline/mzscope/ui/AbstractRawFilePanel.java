/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.mzscope.ui;

import fr.profi.mzdb.model.Feature;
import fr.proline.mzscope.model.Chromatogram;
import fr.proline.mzscope.model.IRawFile;
import fr.proline.mzscope.model.MzScopePreferences;
import fr.proline.mzscope.model.Scan;
import fr.proline.mzscope.ui.event.ExtractionListener;
import fr.proline.mzscope.util.KeyEventDispatcherDecorator;
import fr.proline.mzscope.util.MzScopeConstants;
import fr.proline.studio.export.ExportButton;
import fr.proline.studio.graphics.PlotLinear;
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
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.SwingWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * base for raw file panel. The panel could be a SingleRawFilePanel or MultiRawFilePanel
 * composed by 2 main components: ChromatogramPanel and SpectrumPanel (if scan is displayed)
 * @author CB205360
 */
public abstract class AbstractRawFilePanel extends JPanel implements IRawFilePlot, KeyEventDispatcher, PlotPanelListener, ExtractionListener{

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
    
    protected PlotPanel chromatogramPlotPanel;
    protected JToolBar chromatogramToolbar;
    protected List<PlotLinear> chromatogramPlots;
    protected Chromatogram currentChromatogram;
    protected Scan currentScan;
    protected Float currentScanTime = null;
    
    
    protected List<LineMarker> listMsMsMarkers;
    protected LineMarker currentScanMarker; 
    protected JButton displayMS2btn;
    protected JButton extractBtn;
    
    protected XICExtractionPanel extractionPanel;
    protected JPopupMenu popupMenuExtract;
    
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
        chromatogramPlotPanel  = new PlotPanel();
        chromatogramPlotPanel.addListener(this);
        chromatogramPlotPanel.setDrawCursor(true);
        chromatogramPlotPanel.repaint();
        currentScanMarker = new LineMarker(chromatogramPlotPanel, 0.0, LineMarker.ORIENTATION_VERTICAL, Color.BLUE, false);
        chromatogramPlots = new ArrayList();
        chromatogramContainerPanel.add(chromatogramPlotPanel, BorderLayout.CENTER);
        chromatogramContainerPanel.add(getChromatogramToolbar(), BorderLayout.WEST);
        
        /*
        XYSeriesCollection dataset = new XYSeriesCollection();
        JFreeChart chromatogramChart = ChartFactory.createXYLineChart("", null, null, dataset, PlotOrientation.VERTICAL, false, true, false);
        chromatogramPanel = new ChartPanel(chromatogramChart);
        chromatogramPanel.addChartMouseListener(new ChartMouseListener() {

            @Override
            public void chartMouseMoved(ChartMouseEvent event) {
                int deviceX = event.getTrigger().getX();
                JFreeChart jfreechart = event.getChart();
                if ((jfreechart != null) && (currentChromatogram != null)) {
                    XYPlot xyplot = event.getChart().getXYPlot();
                    double domain = xyplot.getDomainAxis().java2DToValue(deviceX, chromatogramPanel.getScreenDataArea(), xyplot.getDomainAxisEdge());
                    int result = Arrays.binarySearch(currentChromatogram.time, domain);
                    if (~result < currentChromatogram.time.length) {
                        xyplot.clearAnnotations();
                        double x = currentChromatogram.time[~result];
                        double y = currentChromatogram.intensities[~result];
                        final Rectangle2D area = chromatogramPanel.getChartRenderingInfo().getPlotInfo().getDataArea();
                        RectangleEdge rangeEdge = Plot.resolveRangeAxisLocation(xyplot.getRangeAxisLocation(), PlotOrientation.VERTICAL);
                        double pY = xyplot.getRangeAxis().valueToJava2D(y, area, rangeEdge);
                        double angle = (pY < 60.0) ? -5.0 * Math.PI / 4.0 : -Math.PI / 2.0;
                        xyplot.addAnnotation(new XYPointerAnnotation(xFormatter.format(x), x, y, angle));
                    }
                }
            }

            @Override
            public void chartMouseClicked(ChartMouseEvent event) {
                JFreeChart jfreechart = event.getChart();
                if ((jfreechart != null) && (currentChromatogram != null)) {
                    chromatogramMouseClicked(event);
                }
            }
        });

        chromatogramContainerPanel.removeAll();
        chromatogramContainerPanel.add(getChromatogramToolbar(), BorderLayout.WEST);
        chromatogramContainerPanel.add(chromatogramPanel, BorderLayout.CENTER);
        chromatogramPanel.setMouseWheelEnabled(true);
        XYPlot xyplot = chromatogramPanel.getChart().getXYPlot();
        xyplot.getDomainAxis().setTickLabelFont(tickLabelFont);
        xyplot.getRangeAxis().setTickLabelFont(tickLabelFont);
        xyplot.setDomainCrosshairVisible(true);
        xyplot.setDomainCrosshairLockedOnData(false);
        xyplot.setRangeCrosshairVisible(false);
        xyplot.setDomainPannable(true);
        xyplot.setBackgroundPaint(CyclicColorPalette.GRAY_BACKGROUND);
        xyplot.setRangeGridlinePaint(CyclicColorPalette.GRAY_GRID);
*/
        
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
        
        // extraction 
        popupMenuExtract = new JPopupMenu();
        extractionPanel = new XICExtractionPanel();
        extractionPanel.addExtractionListener(this);
        popupMenuExtract.add(extractionPanel);
        extractBtn = new JButton("Extract");
        extractBtn.setToolTipText("mass range to extract with the specified tolerance");
        extractBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                popupMenuExtract.show(extractBtn, extractBtn.getWidth()/2, extractBtn.getHeight());
            }
        });
        chromatogramToolbar.add(extractBtn);

        // MS/MS events
        final JPopupMenu popupMenuMs2 = new JPopupMenu();
        JMenuItem showMsMs = new JMenuItem("Show MS/MS Events");
        ActionListener showMsMsEventAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                showMSMSEvents();
            }
        };
        showMsMs.addActionListener(showMsMsEventAction);
        popupMenuMs2.add(showMsMs);
        JMenuItem hideMsMs = new JMenuItem("Hide All MS/MS Events");
        ActionListener hideMsMsEventAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                hideMSMSEvents();
            }
        };
        hideMsMs.addActionListener(hideMsMsEventAction);
        popupMenuMs2.add(hideMsMs);
        displayMS2btn = new JButton("MS/MS");
        displayMS2btn.setToolTipText("Show/Hide MS/MS Events");
        displayMS2btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                popupMenuMs2.show(displayMS2btn, 0, displayMS2btn.getHeight());
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
       /* XYSeries series = new XYSeries(chromato.rawFile.getName());
        for (int k = 0; k < chromato.intensities.length; k++) {
            series.add(chromato.time[k], chromato.intensities[k]);
        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series);*/
        StringBuilder builder = new StringBuilder("Mass range: ");
        builder.append(xFormatter.format(chromato.minMz)).append("-").append(xFormatter.format(chromato.maxMz));
        //chromatogramPanel.getChart().setTitle(new TextTitle(builder.toString(), titleFont));
        chromatogramPlotPanel.setPlotTitle(builder.toString());
        chromatogramPlotPanel.clearPlots();

        //XYPlot xyplot = chromatogramPanel.getChart().getXYPlot();
        PlotLinear chromatogramPlot= chromatogramPlots.isEmpty()?null:chromatogramPlots.get(0);
        if (chromatogramPlot != null){
            chromatogramPlot.clearMarkers();
        }
        //xyplot.clearDomainMarkers();
        chromatogramPlots = new ArrayList();
        Color plotColor = CyclicColorPalette.getColor(1);
        ChromatogramXICModel chromatoModel = new ChromatogramXICModel(currentChromatogram);
        chromatoModel.setColor(plotColor);
        chromatogramPlot = new PlotLinear(chromatogramPlotPanel, chromatoModel, chromatoModel, ChromatogramXICModel.COLTYPE_CHROMATOGRAM_XIC_TIME, ChromatogramXICModel.COLTYPE_CHROMATOGRAM_XIC_INTENSITIES);
        chromatogramPlot.setPlotInformation(chromatoModel.getPlotInformation());
        chromatogramPlot.setIsPaintMarker(true);
        chromatogramPlot.setStrokeFixed(true);
        chromatogramPlot.addMarker(currentScanMarker);
        if (currentScan != null){
            currentScanMarker.setValue(currentScan.getRetentionTime()/60.0);
        }
        chromatogramPlotPanel.setPlot(chromatogramPlot);
        chromatogramPlotPanel.repaintUpdateDoubleBuffer();
        chromatogramPlots.add(chromatogramPlot);
        //xyplot.setDataset(dataset);
       // xyplot.getRangeAxis().setUpperMargin(0.3);
        //XYItemRenderer renderer = xyplot.getRenderer();
        //renderer.setSeriesPaint(0, plotColor);
        return plotColor;
    }

    @Override
    public Color addChromatogram(Chromatogram chromato) {
        double xMin = Double.NaN, xMax = Double.NaN;
        if (chromatogramPlotPanel.hasPlots()) {
           xMin = chromatogramPlotPanel.getXAxis().getMinValue();
           xMax = chromatogramPlotPanel.getXAxis().getMaxValue();           
        }
        Color plotColor = CyclicColorPalette.getColor(chromatogramPlots.size()+1);
        ChromatogramXICModel chromatoModel = new ChromatogramXICModel(chromato);
        chromatoModel.setColor(plotColor);
        PlotLinear chromatogramPlot = new PlotLinear(chromatogramPlotPanel, chromatoModel, chromatoModel, ChromatogramXICModel.COLTYPE_CHROMATOGRAM_XIC_TIME, ChromatogramXICModel.COLTYPE_CHROMATOGRAM_XIC_INTENSITIES);
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
    public void extractChromatogramMass(double minMz, double maxMz) {
        popupMenuExtract.setVisible(false);
        int extractionMode = getXicModeDisplay();
        switch (extractionMode) {
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
                    if ((currentChromatogram.minMz != -1) && (currentChromatogram.maxMz != -1) && (currentScan.getMsLevel() == 1)) {
                       spectrumContainerPanel.addMarkerRange(currentChromatogram.minMz, currentChromatogram.maxMz);
                    }
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
