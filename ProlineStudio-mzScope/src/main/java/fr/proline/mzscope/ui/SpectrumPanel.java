/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.mzscope.ui;

import fr.profi.ms.model.TheoreticalIsotopePattern;
//import fr.profi.mzdb.algo.IsotopicPatternScorer;
import fr.proline.mzscope.model.IsotopePattern;
import fr.proline.mzscope.model.MzScopePreferences;
import fr.proline.mzscope.model.Scan;
import fr.proline.mzscope.ui.event.ScanHeaderListener;
import fr.proline.mzscope.util.MzScopeConstants;
import fr.proline.mzscope.util.ScanUtils;
import fr.proline.studio.export.ExportButton;
import fr.proline.studio.graphics.PlotAbstract;
import fr.proline.studio.graphics.PlotLinear;
import fr.proline.studio.graphics.BasePlotPanel;
import fr.proline.studio.graphics.PlotPanel;
import fr.proline.studio.graphics.PlotPanelListener;
import fr.proline.studio.graphics.PlotStick;
import fr.proline.studio.graphics.marker.IntervalMarker;
import fr.proline.studio.graphics.marker.LineMarker;
import fr.proline.studio.graphics.marker.PointMarker;
import fr.proline.studio.utils.CyclicColorPalette;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;

/**
 * contains the scan panel
 *
 * @author MB243701
 */
public class SpectrumPanel extends JPanel implements ScanHeaderListener, PlotPanelListener {

   private static final Logger logger = LoggerFactory.getLogger(SpectrumPanel.class);
   
   private AbstractRawFilePanel rawFilePanel;

   protected BasePlotPanel spectrumPlotPanel;
   protected JToolBar spectrumToolbar;
   private HeaderSpectrumPanel headerSpectrumPanel;
   protected PlotAbstract scanPlot;
   protected Scan currentScan;
   protected LineMarker positionMarker;
   private boolean keepMsLevel = true;
    
   protected int xicModeDisplay = MzScopeConstants.MODE_DISPLAY_XIC_REPLACE;

   public SpectrumPanel(AbstractRawFilePanel rawFilePanel) {
      super();
      this.rawFilePanel = rawFilePanel;
   }

   public void initChart() {
      // Create Scan Charts
      PlotPanel plotPanel = new PlotPanel();
      spectrumPlotPanel = plotPanel.getBasePlotPanel();
      spectrumPlotPanel.addListener(this);
      spectrumPlotPanel.setDrawCursor(true);

      positionMarker = new LineMarker(spectrumPlotPanel, 0.0, LineMarker.ORIENTATION_VERTICAL, Color.BLUE, false);

      List<Integer> emptyListScanIndex = new ArrayList<>();
      emptyListScanIndex.add(0);
      boolean multiRawFile = rawFilePanel instanceof MultiRawFilePanel;
      headerSpectrumPanel = new HeaderSpectrumPanel(null, emptyListScanIndex, !multiRawFile);
      headerSpectrumPanel.addScanHeaderListener(this);
      spectrumPlotPanel.repaint();

      this.removeAll();
      this.add(headerSpectrumPanel, BorderLayout.NORTH);
      this.add(plotPanel, BorderLayout.CENTER);
      this.add(getSpectrumToolbar(), BorderLayout.WEST);
   }

   private JToolBar getSpectrumToolbar() {
      spectrumToolbar = new JToolBar(JToolBar.VERTICAL);
      spectrumToolbar.setFloatable(false);
      ExportButton exportImageButton = new ExportButton("Graphic", spectrumPlotPanel);
      spectrumToolbar.add(exportImageButton);

      JButton displayIPBtn = new JButton("IP");
      displayIPBtn.setToolTipText("Display Isotopic Patterns");
      displayIPBtn.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            displayIsotopicPatterns();
         }
      });
      spectrumToolbar.add(displayIPBtn);

      return spectrumToolbar;
   }

   private void displayIsotopicPatterns() {
      float ppmTol = MzScopePreferences.getInstance().getMzPPMTolerance();

//      SortedMap putativePatterns = IsotopicPatternScorer.calclIsotopicPatternHypotheses(currentScan.getScanData(), positionMarker.getValue(), ppmTol);
//      
//      logger.info("scanId=" + currentScan.getIndex() + ", mz = " + positionMarker.getValue() + ", ppm = " + ppmTol);
//      scala.collection.Iterator it = putativePatterns.keySet().iterator();
//      while (it.hasNext()) {
//         Object key = it.next();
//         TheoreticalIsotopePattern pattern = (TheoreticalIsotopePattern) putativePatterns.get(key).get();
//         logger.info("Pattern : " + key + " " + pattern.charge() + " mz = " + pattern.monoMz());
//      }      

//      logger.info("Local estimation : ");
      TreeMap<Double, TheoreticalIsotopePattern> putativePatterns2 = IsotopePattern.getOrderedIPHypothesis(currentScan.getScanData(), positionMarker.getValue());
      logger.info("scanId=" + currentScan.getIndex() + ", mz = " + positionMarker.getValue() + ", ppm = " + ppmTol);
      Iterator itj = putativePatterns2.keySet().iterator();
      while (itj.hasNext()) {
         Object key = itj.next();
         TheoreticalIsotopePattern pattern = (TheoreticalIsotopePattern) putativePatterns2.get(key);
         logger.info("Pattern : " + key + " " + pattern.charge() + " mz = " + pattern.monoMz());
      }      

      TheoreticalIsotopePattern pattern = (TheoreticalIsotopePattern) putativePatterns2.get(putativePatterns2.firstKey());
      int refIdx = 0;
      int idx = ScanUtils.getNearestPeakIndex(currentScan.getScanData().getMzList(), positionMarker.getValue());
      for (Tuple2 t : pattern.mzAbundancePairs()) {
         if (1e6 * (Math.abs(currentScan.getScanData().getMzList()[idx] - (double) t._1) / currentScan.getScanData().getMzList()[idx]) < ppmTol) {
            break;
         }
         refIdx++;
      }

      if (refIdx < pattern.isotopeCount()) {
         float abundance = currentScan.getScanData().getIntensityList()[idx];
         float normAbundance = (Float) pattern.mzAbundancePairs()[refIdx]._2;
         for (Tuple2 t : pattern.mzAbundancePairs()) {
            Double mz = (Double) t._1;
            Float ab = (Float) t._2;
            scanPlot.addMarker(new PointMarker(spectrumPlotPanel, mz, ab * abundance / normAbundance, CyclicColorPalette.getColor(0)));
            int peakIdx = ScanUtils.getPeakIndex(currentScan.getScanData().getMzList(), mz, ppmTol);
            if ((peakIdx != -1) && (currentScan.getScanData().getIntensityList()[peakIdx] < 2.0 * ab * abundance / normAbundance)) {
               logger.info("Peak found mz= "+mz+" expected= "+(ab * abundance / normAbundance)+" observed= "+currentScan.getScanData().getIntensityList()[peakIdx]);
               scanPlot.addMarker(new PointMarker(spectrumPlotPanel, currentScan.getScanData().getMzList()[peakIdx], currentScan.getScanData().getIntensityList()[peakIdx], CyclicColorPalette.getColor(5)));
            }
         }
//         pattern = IsotopePatternEstimator.getTheoreticalPattern(pattern.monoMz(), pattern.charge());
//         for (Tuple2 t : pattern.mzAbundancePairs()) {
//            Double mz = (Double) t._1;
//            Float ab = (Float) t._2;
//            scanPlot.addMarker(new PointMarker(spectrumPlotPanel, mz, ab * abundance / normAbundance, CyclicColorPalette.getColor(3)));
//         }
      }
      spectrumPlotPanel.repaintUpdateDoubleBuffer();
   }

        
   @Override
   public void plotPanelMouseClicked(MouseEvent e, double xValue, double yValue) {
      if (e.getClickCount() == 2) {
         if ((e.getModifiers() & KeyEvent.ALT_MASK) == 0 && xicModeDisplay != MzScopeConstants.MODE_DISPLAY_XIC_OVERLAY) {
            scanPlot.clearMarkers();
            scanPlot.addMarker(positionMarker);
         }
         positionMarker.setValue(xValue);
         positionMarker.setVisible(true);
         double domain = xValue;
         float ppmTol = MzScopePreferences.getInstance().getMzPPMTolerance();
         double maxMz = domain + domain * ppmTol / 1e6;
         double minMz = domain - domain * ppmTol / 1e6;
         scanPlot.addMarker(new IntervalMarker(spectrumPlotPanel, Color.orange, Color.RED, minMz, maxMz));
         rawFilePanel.scanMouseClicked(e, minMz, maxMz, xicModeDisplay);
      } else if (SwingUtilities.isLeftMouseButton(e)) {
         positionMarker.setValue(xValue);
         positionMarker.setVisible(true);
      }
   }

   @Override
   public void updateScanIndex(Integer scanIndex) {
      rawFilePanel.displayScan(scanIndex);
   }

   @Override
   public void updateRetentionTime(float retentionTime) {
      int scanIdx = rawFilePanel.getCurrentRawfile().getScanId(retentionTime);
      rawFilePanel.displayScan(scanIdx);
   }

   @Override
   public void keepMsLevel(boolean keep) {
      this.keepMsLevel = keep;
      updateScanIndexList();
   }

   public void addMarkerRange(double minMz, double maxMz) {
      scanPlot.addMarker(new IntervalMarker(spectrumPlotPanel, Color.orange, Color.RED, minMz, maxMz));
   }

   public void displayScan(Scan scan) {

      double xMin = 0.0, xMax = 0.0, yMin = 0.0, yMax = 0.0;

      if (currentScan != null) {
         xMin = spectrumPlotPanel.getXAxis().getMinValue();
         xMax = spectrumPlotPanel.getXAxis().getMaxValue();
         yMin = spectrumPlotPanel.getYAxis().getMinValue();
         yMax = spectrumPlotPanel.getYAxis().getMaxValue();
      }

      if (scan != null) {
         Color plotColor = rawFilePanel.getPlotColor(rawFilePanel.getCurrentRawfile());
         ScanModel scanModel = new ScanModel(scan);
         scanModel.setColor(plotColor);
         if (scan.getDataType() == Scan.ScanType.CENTROID) { // mslevel2
            //stick plot
            scanPlot = new PlotStick(spectrumPlotPanel, scanModel, scanModel, ScanModel.COLTYPE_SCAN_MASS, ScanModel.COLTYPE_SCAN_INTENSITIES);
            ((PlotStick) scanPlot).setStrokeFixed(true);
            ((PlotStick) scanPlot).setPlotInformation(scanModel.getPlotInformation());
            ((PlotStick) scanPlot).setIsPaintMarker(true);
         } else {
            scanPlot = new PlotLinear(spectrumPlotPanel, scanModel, scanModel, ScanModel.COLTYPE_SCAN_MASS, ScanModel.COLTYPE_SCAN_INTENSITIES);
            ((PlotLinear) scanPlot).setStrokeFixed(true);
            ((PlotLinear) scanPlot).setPlotInformation(scanModel.getPlotInformation());
            ((PlotLinear) scanPlot).setIsPaintMarker(true);
         }

//         for (int k = 0; k < scan.getPeaksMz().length; k++) {
//            scanPlot.addMarker(new PointMarker(spectrumPlotPanel, scan.getPeaksMz()[k], scan.getPeaksIntensities()[k], Color.orange));
//         }
         spectrumPlotPanel.setPlot(scanPlot);
         spectrumPlotPanel.lockMinXValue();
         spectrumPlotPanel.lockMinYValue();

         if ((currentScan != null) && (currentScan.getMsLevel() == scan.getMsLevel())) {
            spectrumPlotPanel.getXAxis().setRange(xMin, xMax);
            spectrumPlotPanel.getYAxis().setRange(yMin, yMax);
         }
         if ((currentScan != null) && (currentScan.getMsLevel() != scan.getMsLevel())) {
            positionMarker.setVisible(false);
         }
         scanPlot.addMarker(positionMarker);
         spectrumPlotPanel.repaint();
         headerSpectrumPanel.setMzdbFileName(rawFilePanel.getCurrentRawfile().getName());
         currentScan = scan;
         updateScanIndexList();
         headerSpectrumPanel.setScan(currentScan);
      }
   }

   private void updateScanIndexList() {
      List<Integer> listScanIndex = new ArrayList();
      if (keepMsLevel) {
         listScanIndex.add(rawFilePanel.getCurrentRawfile().getPreviousScanId(currentScan.getIndex(), currentScan.getMsLevel()));
      } else {
         listScanIndex.add(currentScan.getIndex() - 1);
      }
      listScanIndex.add(currentScan.getIndex());
      if (keepMsLevel) {
         listScanIndex.add(rawFilePanel.getCurrentRawfile().getNextScanId(currentScan.getIndex(), currentScan.getMsLevel()));
      } else {
         listScanIndex.add(currentScan.getIndex() + 1);
      }
      headerSpectrumPanel.setScanIndexList(listScanIndex);
   }

   @Override
   public void updateXicDisplayMode(int mode) {
      xicModeDisplay = mode;
   }

   public int getXicModeDisplay() {
      return this.xicModeDisplay;
   }

}
