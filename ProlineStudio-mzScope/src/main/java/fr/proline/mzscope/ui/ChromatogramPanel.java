/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.mzscope.ui;

import fr.proline.mzscope.model.Chromatogram;
import fr.proline.mzscope.model.IFeature;
import fr.proline.mzscope.model.MsnExtractionRequest;
import fr.proline.mzscope.model.MzScopePreferences;
import fr.proline.mzscope.ui.event.AxisRangeChromatogramListener;
import fr.proline.mzscope.ui.model.ChromatogramTableModel;
import fr.proline.mzscope.utils.MzScopeConstants;
import fr.proline.studio.graphics.BasePlotPanel;
import fr.proline.studio.graphics.PlotLinear;
import fr.proline.studio.graphics.PlotPanel;
import fr.proline.studio.graphics.PlotPanelListener;
import fr.proline.studio.graphics.marker.IntervalMarker;
import fr.proline.studio.graphics.marker.LineMarker;
import fr.proline.studio.utils.CyclicColorPalette;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author CB205360
 */
public class ChromatogramPanel extends JPanel implements PlotPanelListener {

   final private static Logger logger = LoggerFactory.getLogger(ChromatogramPanel.class);
   final private static DecimalFormat xFormatter = new DecimalFormat("0.0000");
   final public static String SCAN_TIME = "scan time";

   protected BasePlotPanel chromatogramPlotPanel;
   protected List<PlotLinear> chromatogramPlots;
   protected Chromatogram currentChromatogram;
   protected List<Chromatogram> listChromatogram;
   protected List<LineMarker> listMsMsMarkers;
   protected LineMarker currentScanMarker;
   protected Float currentScanTime = null;
   private PropertyChangeSupport changeSupport;
   
   private List<AxisRangeChromatogramListener> m_listeners = new ArrayList<>();
   
   public ChromatogramPanel() {
      listMsMsMarkers = new ArrayList();
      listChromatogram = new ArrayList();
      changeSupport = new PropertyChangeSupport(this);
      initComponents();
   }

   private void initComponents() {
      this.setLayout(new BorderLayout());
      PlotPanel plotPanel = new PlotPanel();
      chromatogramPlotPanel = plotPanel.getBasePlotPanel();
      chromatogramPlotPanel.addListener(this);
      chromatogramPlotPanel.setDrawCursor(true);
      chromatogramPlotPanel.repaint();
      currentScanMarker = new LineMarker(chromatogramPlotPanel, 0.0, LineMarker.ORIENTATION_VERTICAL, Color.BLUE, false);
      chromatogramPlots = new ArrayList();
      this.add(plotPanel, BorderLayout.CENTER);
   }
   
   public void addListener(AxisRangeChromatogramListener listener) {
        m_listeners.add(listener);
    }

   public void showMSMSEvents(List<Float> listMsMsTime) {
      PlotLinear chromatogramPlot = chromatogramPlots.isEmpty() ? null : chromatogramPlots.get(0);
      if (chromatogramPlot != null) {
         for (Float time : listMsMsTime) {
            LineMarker marker = new LineMarker(chromatogramPlotPanel, time / 60.0, CyclicColorPalette.getColor(8));
            listMsMsMarkers.add(marker);
            chromatogramPlot.addMarker(marker);
            chromatogramPlotPanel.repaintUpdateDoubleBuffer();
            //chromatogramPanel.getChart().getXYPlot().addDomainMarker(marker);
         }
      }
   }

   public void hideMSMSEvents() {
      PlotLinear chromatogramPlot = chromatogramPlots.isEmpty() ? null : chromatogramPlots.get(0);
      if (chromatogramPlot != null) {
         for (LineMarker marker : listMsMsMarkers) {
            chromatogramPlot.removeMarker(marker);
         }
         chromatogramPlotPanel.repaintUpdateDoubleBuffer();
      }
      listMsMsMarkers = new ArrayList();
   }

   public Color displayChromatogram(Chromatogram chromato, MzScopeConstants.DisplayMode mode) {
      if (mode == MzScopeConstants.DisplayMode.REPLACE) {
         currentChromatogram = chromato;
         listChromatogram = new ArrayList();
         listChromatogram.add(currentChromatogram);
         StringBuilder builder = new StringBuilder("Mass range: ");
         builder.append(xFormatter.format(chromato.minMz)).append("-").append(xFormatter.format(chromato.maxMz));
         chromatogramPlotPanel.setPlotTitle(builder.toString());
         chromatogramPlotPanel.clearPlots();
         PlotLinear chromatogramPlot = chromatogramPlots.isEmpty() ? null : chromatogramPlots.get(0);
         if (chromatogramPlot != null) {
            chromatogramPlot.clearMarkers();
         }
         chromatogramPlots = new ArrayList();
      }

      listChromatogram.add(chromato);
      double xMin = Double.NaN, xMax = Double.NaN;
      if (chromatogramPlotPanel.hasPlots()) {
         xMin = chromatogramPlotPanel.getXAxis().getMinValue();
         xMax = chromatogramPlotPanel.getXAxis().getMaxValue();
      }
      Color plotColor = CyclicColorPalette.getColor(chromatogramPlots.size() + 1);
      ChromatogramTableModel chromatoModel = new ChromatogramTableModel(chromato);
      chromatoModel.setColor(plotColor);
      PlotLinear chromatogramPlot = new PlotLinear(chromatogramPlotPanel, chromatoModel, null, ChromatogramTableModel.COLTYPE_CHROMATOGRAM_XIC_TIME, ChromatogramTableModel.COLTYPE_CHROMATOGRAM_XIC_INTENSITIES);
      chromatogramPlot.setPlotInformation(chromatoModel.getPlotInformation());
      chromatogramPlot.setIsPaintMarker(true);
      chromatogramPlot.setStrokeFixed(true);
      chromatogramPlotPanel.addPlot(chromatogramPlot);
      chromatogramPlots.add(chromatogramPlot);
      if (!Double.isNaN(xMax) && !Double.isNaN(xMin)) {
         chromatogramPlotPanel.getXAxis().setRange(xMin, xMax);
      }

      if (mode == MzScopeConstants.DisplayMode.REPLACE) {
         chromatogramPlots.get(0).addMarker(currentScanMarker);
         if (currentScanTime != null) {
            currentScanMarker.setValue(currentScanTime / 60.0);
         }
         // TODO debug this part in DIA mode => freeze the appl?
         chromatogramPlotPanel.lockMinXValue();
         chromatogramPlotPanel.lockMinYValue();
      }
     chromatogramPlotPanel.repaintUpdateDoubleBuffer();
      return plotColor;
   }

   public void displayFeature(final IFeature f) {
      double ppm = MzScopePreferences.getInstance().getMzPPMTolerance();
      final MsnExtractionRequest.Builder builder = MsnExtractionRequest.builder().setMzTolPPM((float) ppm);
      builder.setMaxMz(f.getMz() + f.getMz() * ppm / 1e6).setMinMz(f.getMz() - f.getMz() * ppm / 1e6);
      PlotLinear chromatogramPlot = chromatogramPlots.isEmpty() ? null : chromatogramPlots.get(0);
      if (chromatogramPlot != null) {
         chromatogramPlot.clearMarkers();
         chromatogramPlot.addMarker(new IntervalMarker(chromatogramPlotPanel, Color.ORANGE, Color.RED, f.getFirstElutionTime() / 60.0, f.getLastElutionTime() / 60.0));
         currentScanMarker.setValue(f.getElutionTime() / 60.0);
         chromatogramPlot.addMarker(currentScanMarker);
      }
   }

   public Chromatogram getCurrentChromatogram() {
      return currentChromatogram;
   }

   @Override
   public void plotPanelMouseClicked(MouseEvent e, double xValue, double yValue) {
      setCurrentScanTime((float) xValue * 60.0f, false);
   }

   public Float getCurrentScanTime() {
      return currentScanTime;
   }

   
   public void setCurrentScanTime(float scanTime) {
      setCurrentScanTime(scanTime, true);
   }
   
   private void setCurrentScanTime(float scanTime, boolean silently) {
      float oldValue = (currentScanTime == null) ? Float.NaN : currentScanTime.floatValue();
      this.currentScanTime = scanTime;
      if (!chromatogramPlots.isEmpty()) {
         currentScanMarker.setVisible(true);
         currentScanMarker.setValue(scanTime / 60.0f);
         chromatogramPlotPanel.repaintUpdateDoubleBuffer();
      }
      if (!silently) {
         changeSupport.firePropertyChange(SCAN_TIME, oldValue, scanTime);
      }
   }

   Iterable<Chromatogram> getChromatograms() {
      return listChromatogram;
   }

   public BasePlotPanel getChromatogramPlotPanel() {
      return chromatogramPlotPanel;
   }

   void setCurrentChromatogram(Chromatogram chromatogram) {
      if (listChromatogram.contains(chromatogram)) {
         currentChromatogram = chromatogram;
      }
   }

   @Override
   public void addPropertyChangeListener(PropertyChangeListener listener) {
      changeSupport.addPropertyChangeListener(listener);
   }

   @Override
   public void removePropertyChangeListener(PropertyChangeListener listener) {
      changeSupport.removePropertyChangeListener(listener);
   }

    @Override
    public void updateAxisRange(double[] oldX,  double[] newX,  double[] oldY, double[] newY) {
        fireUpdateAxisRange(oldX, newX, oldY, newY);
    }
    
    
    protected void fireUpdateAxisRange(double[] oldX,  double[] newX,  double[] oldY, double[] newY){
        // Notify 
        for (AxisRangeChromatogramListener l : m_listeners)
            l.updateAxisRange(oldX, newX, oldY, newY);
    }
    
    
    public void updateAxisRange(double zoomXLevel, double relativeXValue, double zoomYLevel, double relativeYValue){
        double oldXMin = chromatogramPlotPanel.getXAxis().getMinValue();
        double oldXMax = chromatogramPlotPanel.getXAxis().getMaxValue();
        double oldYMin = chromatogramPlotPanel.getYAxis().getMinValue();
        double oldYMax = chromatogramPlotPanel.getYAxis().getMaxValue();
        
        double newRangeX = (double)(zoomXLevel * (oldXMax - oldXMin) / 100);
        double x =   (double)(oldXMin + (relativeXValue * (oldXMax - oldXMin) / 100));
        double newRangeY = (double)(zoomYLevel * (oldYMax - oldYMin) / 100);
        double y =   (double)(oldYMin + (relativeYValue * (oldYMax - oldYMin) / 100));
        double newXMin = x - (double)(newRangeX / 2);
        double newXMax = x + (double)(newRangeX / 2);
        double newYMin = y - (double)(newRangeY / 2);
        double newYMax = y + (double)(newRangeY / 2);
        chromatogramPlotPanel.getXAxis().setRange(newXMin, newXMax);
        chromatogramPlotPanel.getYAxis().setRange(newYMin, newYMax);
        chromatogramPlotPanel.repaintUpdateDoubleBuffer();
    }
  
}
