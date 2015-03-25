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
import fr.proline.mzscope.util.KeyEventDispatcherDecorator;
import fr.proline.mzscope.util.MzScopeConstants;
import fr.proline.studio.utils.CyclicColorPalette;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.KeyEventDispatcher;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.SwingWorker;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYPointerAnnotation;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleEdge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * base for raw file panel. The panel could be a SingleRawFilePanel or MultiRawFilePanel
 * composed by 2 main components: ChromatogramPanel and SpectrumPanel (if scan is displayed)
 * @author CB205360
 */
public abstract class AbstractRawFilePanel extends JPanel implements IRawFilePlot, KeyEventDispatcher{

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
    
    protected ChartPanel chromatogramPanel;
    protected JToolBar chromatogramToolbar;
    protected Chromatogram currentChromatogram;
    protected Scan currentScan;
    protected Float currentScanTime = null;
    
    
    protected List<Marker> listMsMsMarkers;
    protected JButton displayMS2btn;
    
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
        chromatogramContainerPanel.add(initChromatogramToolbar(), BorderLayout.WEST);
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

        // Create Scan Charts
        spectrumContainerPanel.initChart();
    }
    
    private JToolBar initChromatogramToolbar() {
        chromatogramToolbar = new JToolBar();
        chromatogramToolbar.setOrientation(JToolBar.VERTICAL);
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
    
    protected void chromatogramMouseClicked(ChartMouseEvent event) {
        XYPlot xyplot = chromatogramPanel.getChart().getXYPlot();
        double d = xyplot.getDomainAxis().java2DToValue(event.getTrigger().getX(), chromatogramPanel.getScreenDataArea(), xyplot.getDomainAxisEdge());
        int scanIdx = getCurrentRawfile().getScanId(d * 60.0);
        displayScan(scanIdx);
    }
    
   
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
    
    public void updateXicModeDisplay(int mode){
        if (displayScan){
            spectrumContainerPanel.updateXicModeDisplay(mode);
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
                    for (Float time : listMsMsTime) {
                        Marker marker = new ValueMarker(time / 60.0);
                        marker.setPaint(CyclicColorPalette.getColor(8));
                        listMsMsMarkers.add(marker);
                        chromatogramPanel.getChart().getXYPlot().addDomainMarker(marker);
                    }
                } catch (InterruptedException | ExecutionException e) {
                    logger.error("Error while reading chromatogram");
                }
            }
        };
        worker.execute();
    }

    public void hideMSMSEvents() {
        for (Marker marker : listMsMsMarkers) {
            chromatogramPanel.getChart().getXYPlot().removeDomainMarker(marker);
        }
        listMsMsMarkers = new ArrayList();
    }
    
    
    @Override
    public Color displayChromatogram(Chromatogram chromato) {
        setMsMsEventButtonEnabled(true);
        this.currentChromatogram = chromato;
        XYSeries series = new XYSeries(chromato.rawFile.getName());
        for (int k = 0; k < chromato.intensities.length; k++) {
            series.add(chromato.time[k], chromato.intensities[k]);
        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series);
        StringBuilder builder = new StringBuilder("Mass range: ");
        builder.append(xFormatter.format(chromato.minMz)).append("-").append(xFormatter.format(chromato.maxMz));
        chromatogramPanel.getChart().setTitle(new TextTitle(builder.toString(), titleFont));

        XYPlot xyplot = chromatogramPanel.getChart().getXYPlot();
        xyplot.clearDomainMarkers();
        xyplot.setDataset(dataset);
        xyplot.getRangeAxis().setUpperMargin(0.3);
        XYItemRenderer renderer = xyplot.getRenderer();
        Color plotColor = CyclicColorPalette.getColor(1);
        renderer.setSeriesPaint(0, plotColor);
        return plotColor;
    }

    @Override
    public Color addChromatogram(Chromatogram chromato) {
        XYSeries series = new XYSeries(chromato.rawFile.getName()+"-"+chromato.minMz);
        for (int k = 0; k < chromato.intensities.length; k++) {
            series.add(chromato.time[k], chromato.intensities[k]);
        }
        XYPlot xyplot = chromatogramPanel.getChart().getXYPlot();
        ((XYSeriesCollection) xyplot.getDataset()).addSeries(series);
        Color plotColor = CyclicColorPalette.getColor(xyplot.getDataset().getSeriesCount());
        xyplot.getRenderer().setSeriesPaint(xyplot.getDataset().getSeriesCount() - 1, plotColor);
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
                    Marker marker = new IntervalMarker(f.getBasePeakel().getFirstElutionTime() / 60.0, f.getBasePeakel().getLastElutionTime() / 60.0, Color.ORANGE, new BasicStroke(1), Color.RED, new BasicStroke(1), 0.3f);
                    chromatogramPanel.getChart().getXYPlot().addDomainMarker(marker);
                    marker = new ValueMarker(f.getElutionTime() / 60.0);
                    chromatogramPanel.getChart().getXYPlot().addDomainMarker(marker);
                } catch (InterruptedException | ExecutionException e) {
                    logger.error("Error while reading chromatogram");
                }
            }
        };
        worker.execute();
    }

    @Override
    public void displayScan(int index) {
        if (displayScan){
            spectrumContainerPanel.displayScan(index);
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
                    XYPlot xyplot = chromatogramPanel.getChart().getXYPlot();
                    xyplot.clearDomainMarkers();
                    Marker marker = new IntervalMarker(firstScanTime / 60.0, lastScanTime / 60.0, Color.ORANGE, new BasicStroke(1), Color.RED, new BasicStroke(1), 0.3f);
                    xyplot.addDomainMarker(marker);
                    marker = new ValueMarker(elutionTime / 60.0);
                    xyplot.addDomainMarker(marker);
                } catch (InterruptedException | ExecutionException e) {
                    logger.error("Error while extraction chromatogram", e);
                }
            }
        };
        worker.execute();
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
