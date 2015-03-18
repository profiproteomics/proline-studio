/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.mzscope.ui;

import fr.proline.mzscope.util.KeyEventDispatcherDecorator;
import fr.profi.mzdb.model.Feature;
import fr.proline.mzscope.model.Chromatogram;
import fr.proline.mzscope.model.IRawFile;
import fr.proline.mzscope.model.MzScopePreferences;
import fr.proline.mzscope.model.Scan;
import fr.proline.mzscope.ui.event.ScanHeaderListener;
import fr.proline.studio.utils.CyclicColorPalette;
import fr.proline.mzscope.util.MzScopeConstants;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.KeyEventDispatcher;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.SwingWorker;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYPointerAnnotation;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.AbstractXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleEdge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author CB205360
 */
abstract public class AbstractRawFilePanel extends javax.swing.JPanel implements IRawFilePlot, KeyEventDispatcher, ScanHeaderListener {

    final private static Logger logger = LoggerFactory.getLogger("ProlineStudio.mzScope.AbstractRawFilePanel");
    final private static DecimalFormat xFormatter = new DecimalFormat("0.0000");
    final private static DecimalFormat yFormatter = new DecimalFormat("0.###E0");
    final private static Font tickLabelFont = new Font("SansSerif", java.awt.Font.PLAIN, 10);
    final private static Font titleFont = new Font("SansSerif", java.awt.Font.PLAIN, 12);

    protected ChartPanel chromatogramPanel;
    protected ChartPanel spectrumPanel;
    private HeaderSpectrumPanel headerSpectrumPanel;
    protected JToolBar toolbar;
    protected Chromatogram currentChromatogram;
    protected Scan currentScan;
    protected Float currentScanTime = null;

    private final XYItemRenderer stickRenderer = new XYItemStickRenderer();
    private final XYItemRenderer lineRenderer = new XYLineAndShapeRenderer(true, false);

    private boolean keepMsLevel = true;

    protected List<Marker> listMsMsMarkers;
    protected JButton displayMS2btn;
    
    protected int xicModeDisplay = MzScopeConstants.MODE_DISPLAY_XIC_REPLACE;

    /**
     * Creates new form IRawFilePlotPanel
     */
    public AbstractRawFilePanel() {
        init();
    }

    private void init() {
        listMsMsMarkers = new ArrayList();
        initComponents();
        initChartPanels();
        KeyEventDispatcherDecorator.addKeyEventListener(this);
    }

    private void initChartPanels() {
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
        chromatogramContainerPanel.add(initToolbar(), BorderLayout.WEST);
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
        dataset = new XYSeriesCollection();
        JFreeChart scanChart = ChartFactory.createXYLineChart("", null, null, dataset, PlotOrientation.VERTICAL, false, true, false);
        xyplot = scanChart.getXYPlot();
        XYItemRenderer renderer = xyplot.getRenderer();
        renderer.setSeriesPaint(0, CyclicColorPalette.getColor(1));
        xyplot.setDomainPannable(true);
        xyplot.setDomainCrosshairVisible(true);
        xyplot.setDomainCrosshairLockedOnData(false);
        xyplot.setRangeCrosshairVisible(false);
        xyplot.getDomainAxis().setTickLabelFont(tickLabelFont);
        xyplot.getRangeAxis().setTickLabelFont(tickLabelFont);
        xyplot.setBackgroundPaint(CyclicColorPalette.GRAY_BACKGROUND);
        xyplot.setRangeGridlinePaint(CyclicColorPalette.GRAY_GRID);
        spectrumPanel = new ChartPanel(scanChart);
        List<Integer> emptyListScanIndex = new ArrayList<>();
        emptyListScanIndex.add(0);
        headerSpectrumPanel = new HeaderSpectrumPanel(null, emptyListScanIndex);
        headerSpectrumPanel.addScanHeaderListener(this);
        spectrumPanel.setMouseWheelEnabled(true);

        spectrumPanel.addChartMouseListener(new ChartMouseListener() {

            @Override
            public void chartMouseMoved(ChartMouseEvent event) {
                int deviceX = event.getTrigger().getX();

                JFreeChart jfreechart = event.getChart();
                if ((jfreechart != null) && (currentScan != null)) {
                    XYPlot xyplot = event.getChart().getXYPlot();
                    double domain = xyplot.getDomainAxis().java2DToValue(deviceX, spectrumPanel.getScreenDataArea(), xyplot.getDomainAxisEdge());
                    double[] domainValues = ((currentScan.getPeaksMz() == null) ? currentScan.getMasses() : currentScan.getPeaksMz());
                    float[] rangeValues = ((currentScan.getPeaksIntensities() == null) ? currentScan.getIntensities() : currentScan.getPeaksIntensities());
                    int result = Arrays.binarySearch(domainValues, domain);
                    if (~result < domainValues.length) {
                        xyplot.clearAnnotations();
                        StringBuilder builder = new StringBuilder();
                        builder.append(xFormatter.format(domainValues[~result])).append(" - ");
                        builder.append(yFormatter.format(rangeValues[~result]));
                        double y = rangeValues[~result];
                        final Rectangle2D area = spectrumPanel.getChartRenderingInfo().getPlotInfo().getDataArea();
                        RectangleEdge rangeEdge = Plot.resolveRangeAxisLocation(xyplot.getRangeAxisLocation(), PlotOrientation.VERTICAL);
                        double pY = xyplot.getRangeAxis().valueToJava2D(y, area, rangeEdge);
                        double angle = (pY < 60.0) ? -5.0 * Math.PI / 4.0 : -Math.PI / 2.0;
                        xyplot.addAnnotation(new XYPointerAnnotation(builder.toString(), domainValues[~result], rangeValues[~result], angle));
                    }
                }
            }

            @Override
            public void chartMouseClicked(ChartMouseEvent event) {
                JFreeChart jfreechart = event.getChart();
                if ((jfreechart != null) && (currentScan != null)) {
                    scanMouseClicked(event);
                }
            }
        });

        spectrumContainerPanel.removeAll();
        spectrumContainerPanel.add(headerSpectrumPanel, BorderLayout.NORTH);
        spectrumContainerPanel.add(spectrumPanel, BorderLayout.CENTER);
    }

    private JToolBar initToolbar() {
        toolbar = new JToolBar();
        toolbar.setOrientation(JToolBar.VERTICAL);
        JButton displayTICbtn = new JButton("TIC");
        displayTICbtn.setToolTipText("Display TIC Chromatogram");
        displayTICbtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                displayTIC();
            }
        });
        toolbar.add(displayTICbtn);

        JButton displayBPIbtn = new JButton("BPI");
        displayBPIbtn.setToolTipText("Display BPI Chromatogram");
        displayBPIbtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                displayBPI();
            }
        });
        toolbar.add(displayBPIbtn);

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
        toolbar.add(displayMS2btn);
        setMsMsEventButtonEnabled(false);

        toolbar.setFloatable(false);
        toolbar.setRollover(true);
        return toolbar;
    }
    
    protected void setMsMsEventButtonEnabled(boolean b) {
        this.displayMS2btn.setEnabled(b);
        if (!b) {
            hideMSMSEvents();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
   // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
   private void initComponents() {

      jSplitPane1 = new javax.swing.JSplitPane();
      chromatogramContainerPanel = new javax.swing.JPanel();
      spectrumContainerPanel = new javax.swing.JPanel();

      setBackground(new java.awt.Color(240, 240, 40));
      setLayout(new java.awt.BorderLayout());

      jSplitPane1.setDividerLocation(160);
      jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
      jSplitPane1.setResizeWeight(0.5);
      jSplitPane1.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
      jSplitPane1.setDoubleBuffered(true);
      jSplitPane1.setOneTouchExpandable(true);

      chromatogramContainerPanel.setLayout(new java.awt.BorderLayout());
      jSplitPane1.setTopComponent(chromatogramContainerPanel);

      spectrumContainerPanel.setLayout(new java.awt.BorderLayout());
      jSplitPane1.setBottomComponent(spectrumContainerPanel);

      add(jSplitPane1, java.awt.BorderLayout.CENTER);
   }// </editor-fold>//GEN-END:initComponents

    protected void chromatogramMouseClicked(ChartMouseEvent event) {
        XYPlot xyplot = chromatogramPanel.getChart().getXYPlot();
        double d = xyplot.getDomainAxis().java2DToValue(event.getTrigger().getX(), chromatogramPanel.getScreenDataArea(), xyplot.getDomainAxisEdge());
        int scanIdx = getCurrentRawfile().getScanId(d * 60.0);
        displayScan(scanIdx);
    }

    protected void scanMouseClicked(ChartMouseEvent event) {
        if ((event.getTrigger().getClickCount() == 2)) {
            XYPlot xyplot = event.getChart().getXYPlot();
            double domain = xyplot.getDomainAxis().java2DToValue(event.getTrigger().getX(), spectrumPanel.getScreenDataArea(), xyplot.getDomainAxisEdge());
            float ppmTol = MzScopePreferences.getInstance().getMzPPMTolerance();
            double maxMz = domain + domain * ppmTol / 1e6;
            double minMz = domain - domain * ppmTol / 1e6;
            if ((event.getTrigger().getModifiers() & KeyEvent.ALT_MASK) != 0) {
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

    public abstract Color getPlotColor(IRawFile rawFile);

    @Override
    public void displayScan(int index) {
        if ((currentScan == null) || (index != currentScan.getIndex())) {
            currentScan = getCurrentRawfile().getScan(index);
            if (currentScan != null) {
                Color plotColor = getPlotColor(getCurrentRawfile());
                currentScanTime = currentScan.getRetentionTime();
                XYSeries series = new XYSeries(currentScan.getTitle());
                double[] masses = currentScan.getMasses();
                float[] intensities = currentScan.getIntensities();
                for (int k = 0; k < currentScan.getMasses().length; k++) {
                    series.add(masses[k], intensities[k]);
                }

                XYSeriesCollection dataset = new XYSeriesCollection();
                dataset.addSeries(series);
                spectrumPanel.getChart().setTitle(new TextTitle(currentScan.getTitle(), titleFont));
                headerSpectrumPanel.setMzdbFileName(getCurrentRawfile().getName());
                updateScanIndexList();
                headerSpectrumPanel.setScan(currentScan);
                XYPlot xyplot = spectrumPanel.getChart().getXYPlot();
                xyplot.setDataset(dataset);
                if (currentScan.getDataType() == Scan.ScanType.CENTROID) {
                    stickRenderer.setSeriesPaint(0, plotColor);
                    xyplot.setRenderer(stickRenderer);
                } else {
                    lineRenderer.setSeriesPaint(0, plotColor);
                    xyplot.setRenderer(lineRenderer);
                }
                xyplot.getRangeAxis().setUpperMargin(0.3);
                chromatogramPanel.getChart().getXYPlot().setDomainCrosshairValue(currentScan.getRetentionTime() / 60.0);
            }
        }
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
    public Chromatogram getCurrentChromatogram() {
        return currentChromatogram;
    }

    abstract void displayTIC();

    abstract void displayBPI();

   // Variables declaration - do not modify//GEN-BEGIN:variables
   private javax.swing.JPanel chromatogramContainerPanel;
   private javax.swing.JSplitPane jSplitPane1;
   private javax.swing.JPanel spectrumContainerPanel;
   // End of variables declaration//GEN-END:variables

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

    @Override
    public void updateScanIndex(Integer scanIndex) {
        displayScan(scanIndex);
    }

    @Override
    public void updateRetentionTime(float retentionTime) {
        int scanIdx = getCurrentRawfile().getScanId(retentionTime);
        displayScan(scanIdx);
    }

    @Override
    public void keepMsLevel(boolean keepMsLevel) {
        this.keepMsLevel = keepMsLevel;
        updateScanIndexList();
    }

    private void updateScanIndexList() {
        List<Integer> listScanIndex = new ArrayList();
        if (keepMsLevel) {
            listScanIndex.add(getCurrentRawfile().getPreviousScanId(currentScan.getIndex(), currentScan.getMsLevel()));
        } else {
            listScanIndex.add(currentScan.getIndex() - 1);
        }
        listScanIndex.add(currentScan.getIndex());
        if (keepMsLevel) {
            listScanIndex.add(getCurrentRawfile().getNextScanId(currentScan.getIndex(), currentScan.getMsLevel()));
        } else {
            listScanIndex.add(currentScan.getIndex() + 1);
        }
        headerSpectrumPanel.setScanIndexList(listScanIndex);
    }

    private class XYItemStickRenderer extends AbstractXYItemRenderer {

        public XYItemStickRenderer() {
        }

        @Override
        public void drawItem(Graphics2D g2, XYItemRendererState state, Rectangle2D dataArea, PlotRenderingInfo info, XYPlot plot, ValueAxis domainAxis, ValueAxis rangeAxis,
                XYDataset dataset, int series, int item, CrosshairState crosshairState, int pass) {
            if (!getItemVisible(series, item)) {
                return;
            }
            double x = dataset.getXValue(series, item);
            double y = dataset.getYValue(series, item);

            if (!java.lang.Double.isNaN(y)) {
                RectangleEdge xAxisLocation = plot.getDomainAxisEdge();
                RectangleEdge yAxisLocation = plot.getRangeAxisEdge();
                double transX = domainAxis.valueToJava2D(x, dataArea, xAxisLocation);
                double transY = rangeAxis.valueToJava2D(y, dataArea, yAxisLocation);
                double transY0 = rangeAxis.valueToJava2D(0.0, dataArea, yAxisLocation);
                g2.setStroke(DEFAULT_STROKE);
                g2.setPaint(getItemPaint(series, item));
                PlotOrientation orientation = plot.getOrientation();
                if (orientation == PlotOrientation.HORIZONTAL) {
                    g2.drawLine((int) transY, (int) transX, (int) transY0, (int) transX);
                } else if (orientation == PlotOrientation.VERTICAL) {
                    g2.drawLine((int) transX, (int) transY, (int) transX, (int) transY0);
                }
//            int domainAxisIndex = plot.getDomainAxisIndex(domainAxis);
//            int rangeAxisIndex = plot.getRangeAxisIndex(rangeAxis);
                //updateCrosshairValues(crosshairState, x, y, domainAxisIndex, rangeAxisIndex, transX, transY, orientation);
            }
        }
    }
    
    public void updateXicModeDisplay(int mode){
        xicModeDisplay = mode;
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
}
