/* 
 * Copyright (C) 2019
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

import fr.proline.mzscope.model.IChromatogram;
import fr.proline.mzscope.model.IPeakel;
import fr.proline.mzscope.ui.event.AxisRangeChromatogramListener;
import fr.proline.mzscope.ui.model.ChromatogramTableModel;
import fr.proline.mzscope.utils.Display;
import fr.proline.studio.Exceptions;
import fr.proline.studio.graphics.BasePlotPanel;
import fr.proline.studio.graphics.PlotBaseAbstract;
import fr.proline.studio.graphics.PlotLinear;
import fr.proline.studio.graphics.PlotPanel;
import fr.proline.studio.graphics.PlotPanelListener;
import fr.proline.studio.graphics.marker.AbstractMarker;
import fr.proline.studio.graphics.marker.LineMarker;
import fr.proline.studio.graphics.measurement.IntegralMeasurement;
import fr.proline.studio.utils.CyclicColorPalette;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
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
   final public static String SCAN_TIME = "scan time";

   protected BasePlotPanel chromatogramPlotPanel;
   protected List<PlotLinear> chromatogramPlots;
   protected IChromatogram currentChromatogram;
   protected List<IChromatogram> listChromatogram;
   protected List<LineMarker> listMsMsMarkers;
   protected LineMarker currentScanMarker;
   protected Float currentScanTime = null;
   protected String lastDisplayIdentifier;
   
   private PropertyChangeSupport changeSupport;
   
   private List<AxisRangeChromatogramListener> m_listeners = new ArrayList<>();
   
   public ChromatogramPanel() {
      listMsMsMarkers = new ArrayList<>();
      listChromatogram = new ArrayList<>();
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
      chromatogramPlots = new ArrayList<>();
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
      listMsMsMarkers = new ArrayList<>();
   }

   public Color displayChromatogram(IChromatogram chromato, Display display) {
      if (display.getMode() == Display.Mode.REPLACE || ((display.getMode() == Display.Mode.SERIES) && !display.getIdentifier().equals(lastDisplayIdentifier))) {
         currentChromatogram = chromato;
         listChromatogram = new ArrayList<>();
         chromatogramPlotPanel.setPlotTitle(chromato.getTitle());
         chromatogramPlotPanel.clearPlots();
         PlotLinear chromatogramPlot = chromatogramPlots.isEmpty() ? null : chromatogramPlots.get(0);
         if (chromatogramPlot != null) {
            chromatogramPlot.clearMarkers();
         }
         chromatogramPlots = new ArrayList<>();
      } else if (!listChromatogram.isEmpty()) {
        chromatogramPlotPanel.setPlotTitle("<" + (listChromatogram.size() + 1) + " chromatograms>");
      }

      listChromatogram.add(chromato);
      double xMin = Double.NaN, xMax = Double.NaN;
      if (chromatogramPlotPanel.hasPlots()) {
          double[] bounds = chromatogramPlotPanel.getXAxisBounds();
          if (Double.isNaN(bounds[0]) || bounds[0] < chromatogramPlotPanel.getXAxis().getMinValue()) xMin = chromatogramPlotPanel.getXAxis().getMinValue();
          if (Double.isNaN(bounds[1]) || bounds[1] > chromatogramPlotPanel.getXAxis().getMaxValue()) xMax = chromatogramPlotPanel.getXAxis().getMaxValue();
      }
      
      // if a previous chromatogram was plotted, compute new bounds, otherwise set bounds to chromato start / end time
      if (chromatogramPlotPanel.hasPlots()) {
        double[] bounds = chromatogramPlotPanel.getXAxisBounds();
        chromatogramPlotPanel.setXAxisBounds(Math.min(chromato.getElutionStartTime(), bounds[0]) , Math.max(chromato.getElutionEndTime(), bounds[1]));
      } else {
        chromatogramPlotPanel.setXAxisBounds(chromato.getElutionStartTime(), chromato.getElutionEndTime());
      }

      Color plotColor = CyclicColorPalette.getColor(chromatogramPlots.size() + 1);
      ChromatogramTableModel chromatoModel = new ChromatogramTableModel(chromato);
      chromatoModel.setColor(plotColor);
      PlotLinear chromatogramPlot = new PlotLinear(chromatogramPlotPanel, chromatoModel, null, ChromatogramTableModel.COLTYPE_CHROMATOGRAM_XIC_TIME, ChromatogramTableModel.COLTYPE_CHROMATOGRAM_XIC_INTENSITIES);
      chromatogramPlot.setPlotInformation(chromatoModel.getPlotInformation());
      chromatogramPlot.setIsPaintMarker(true);
      chromatogramPlot.setStrokeFixed(true);
      chromatogramPlot.addMeasurement(new IntegralMeasurement(chromatogramPlot));
      //chromatogramPlot.addMeasurement(new WidthMeasurement(chromatogramPlot));
      chromatogramPlotPanel.addPlot(chromatogramPlot, true);
      chromatogramPlots.add(chromatogramPlot);
      
      // if a a zoom range was set, restore it
      if (!Double.isNaN(xMax) && !Double.isNaN(xMin)) {
         chromatogramPlotPanel.getXAxis().setRange(xMin, xMax);
      }

      if ((display.getMode() == Display.Mode.REPLACE) || ((display.getMode() == Display.Mode.SERIES) && !display.getIdentifier().equals(lastDisplayIdentifier))) {
         chromatogramPlots.get(0).addMarker(currentScanMarker);
         if (currentScanTime != null) {
            currentScanMarker.setValue(currentScanTime / 60.0);
         }
         // TODO debug this part in DIA mode => freeze the appl?
         chromatogramPlotPanel.lockMinXValue();
         chromatogramPlotPanel.lockMinYValue();
      }
      
      displayMarkers(chromatogramPlot, chromatogramPlotPanel, display);
      chromatogramPlotPanel.repaintUpdateDoubleBuffer();
      lastDisplayIdentifier = display.getIdentifier();
      return plotColor;
   }

    private void displayMarkers(PlotBaseAbstract plot, BasePlotPanel plotPanel, Display display) {
        if (display.getMarkers() != null) {
            for (AbstractMarker marker : display.getMarkers()) {
                try {
                    AbstractMarker clone = marker.clone(plotPanel);
                    plot.addMarker(clone);
                } catch (CloneNotSupportedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }

   public void displayFeature(final IPeakel f, Display display) {
      PlotLinear chromatogramPlot = chromatogramPlots.isEmpty() ? null : chromatogramPlots.get(0);
      if (chromatogramPlot != null) {
         chromatogramPlot.clearMarkers();
         displayMarkers(chromatogramPlot, chromatogramPlotPanel, display);
         currentScanMarker.setValue(f.getElutionTime() / 60.0);
         chromatogramPlot.addMarker(currentScanMarker);
      }
   }

   public IChromatogram getCurrentChromatogram() {
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

   Iterable<IChromatogram> getChromatograms() {
      return listChromatogram;
   }

   public BasePlotPanel getChromatogramPlotPanel() {
      return chromatogramPlotPanel;
   }

   void setCurrentChromatogram(IChromatogram chromatogram) {
      if (listChromatogram.contains(chromatogram)) {
         currentChromatogram = chromatogram;
      } else {
          logger.warn("current chromatogram supplied is not in the list of extracted chromatograms : "+chromatogram.toString());
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
        
        double newRangeX = zoomXLevel * (oldXMax - oldXMin) / 100.0;
        double x = oldXMin + (relativeXValue * (oldXMax - oldXMin) / 100.0);
        double newRangeY = zoomYLevel * (oldYMax - oldYMin) / 100.0;
        double y = oldYMin + (relativeYValue * (oldYMax - oldYMin) / 100.0);
        double newXMin = x - (newRangeX / 2.0);
        double newXMax = x + (newRangeX / 2.0);
        double newYMin = y - (newRangeY / 2.0);
        double newYMax = y + (newRangeY / 2.0);
        logger.debug("{} move from min={}, max={} to min={}, max={}", currentChromatogram.getRawFilename(), oldXMin, oldXMax, newXMin, newXMax);
        chromatogramPlotPanel.getXAxis().setRange(newXMin, newXMax);
        chromatogramPlotPanel.getYAxis().setRange(newYMin, newYMax);
        chromatogramPlotPanel.repaintUpdateDoubleBuffer();
    }

    
    public void updateAxisRange2(double zoomXRange, double relativeXPosition, double zoomYLevel, double relativeYValue){

        double oldYMin = chromatogramPlotPanel.getYAxis().getMinValue();
        double oldYMax = chromatogramPlotPanel.getYAxis().getMaxValue();
        
        double newRangeY = zoomYLevel * (oldYMax - oldYMin) / 100.0;
        double y = oldYMin + (relativeYValue * (oldYMax - oldYMin) / 100.0);

        double newXMin = (relativeXPosition * (currentChromatogram.getElutionEndTime() - currentChromatogram.getElutionStartTime()));
        double newXMax = newXMin + zoomXRange;
        double newYMin = y - (newRangeY / 2.0);
        double newYMax = y + (newRangeY / 2.0);
        //logger.debug("{} move to min={}, max={}", currentChromatogram.rawFilename, newXMin, newXMax);
        chromatogramPlotPanel.getXAxis().setRange(newXMin, newXMax);
        chromatogramPlotPanel.getYAxis().setRange(newYMin, newYMax);
        chromatogramPlotPanel.repaintUpdateDoubleBuffer();
    }
        
}
