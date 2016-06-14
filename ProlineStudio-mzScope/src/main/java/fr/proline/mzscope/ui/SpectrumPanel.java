package fr.proline.mzscope.ui;

import fr.proline.mzscope.ui.model.ScanTableModel;
import fr.profi.ms.model.TheoreticalIsotopePattern;
import fr.profi.mzdb.algo.IsotopicPatternScorer;
import fr.proline.mzscope.model.MsnExtractionRequest;
import fr.proline.mzscope.model.MzScopePreferences;
import fr.proline.mzscope.model.Spectrum;
import fr.proline.mzscope.ui.event.ScanHeaderListener;
import fr.proline.mzscope.utils.MzScopeConstants.DisplayMode;
import fr.proline.mzscope.utils.SpectrumUtils;
import fr.proline.studio.export.ExportButton;
import fr.proline.studio.graphics.PlotAbstract;
import fr.proline.studio.graphics.PlotLinear;
import fr.proline.studio.graphics.BasePlotPanel;
import fr.proline.studio.graphics.PlotPanel;
import fr.proline.studio.graphics.PlotPanelListener;
import fr.proline.studio.graphics.PlotStick;
import fr.proline.studio.graphics.marker.AbstractMarker;
import fr.proline.studio.graphics.marker.IntervalMarker;
import fr.proline.studio.graphics.marker.LineMarker;
import fr.proline.studio.graphics.marker.PointMarker;
import fr.proline.studio.graphics.marker.coordinates.DataCoordinates;
import fr.proline.studio.utils.CyclicColorPalette;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
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
   
   private final IRawFileViewer rawFilePanel;

   protected BasePlotPanel spectrumPlotPanel;
   protected JToolBar spectrumToolbar;
   private ScanHeaderPanel headerSpectrumPanel;
   protected PlotAbstract scanPlot;
   protected Spectrum currentScan;
   protected LineMarker positionMarker;
   private boolean keepMsLevel = true;
   protected DisplayMode xicModeDisplay = DisplayMode.REPLACE;
   
   private List<AbstractMarker> ipMarkers = new ArrayList();

   public SpectrumPanel(IRawFileViewer rawFilePanel) {
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
      headerSpectrumPanel = new ScanHeaderPanel(null, emptyListScanIndex, !multiRawFile);
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
        ipMarkers.stream().forEach((m) -> {
            scanPlot.removeMarker(m);
        });
        ipMarkers = new ArrayList();
        float ppmTol = MzScopePreferences.getInstance().getMzPPMTolerance();

        int nearestPeakIdx = SpectrumUtils.getNearestPeakIndex(currentScan.getSpectrumData().getMzList(), positionMarker.getValue());
        if (SpectrumUtils.isInRange(currentScan.getSpectrumData().getMzList()[nearestPeakIdx], positionMarker.getValue(), ppmTol)) {
           if (currentScan.getSpectrumData().getLeftHwhmList()[nearestPeakIdx] > 0.0f)
               ppmTol = (float) (1e6 * currentScan.getSpectrumData().getLeftHwhmList()[nearestPeakIdx] / currentScan.getSpectrumData().getMzList()[nearestPeakIdx]);
        }
        Tuple2<Object, TheoreticalIsotopePattern>[] putativePatterns = IsotopicPatternScorer.calcIsotopicPatternHypotheses(currentScan.getSpectrumData(), positionMarker.getValue(), ppmTol);

        logger.info("scanId=" + currentScan.getIndex() + ", mz = " + positionMarker.getValue() + ", ppm = " + ppmTol);
        for (Tuple2<Object, TheoreticalIsotopePattern> t : putativePatterns) {
            Double score = ((Double) t._1);
            TheoreticalIsotopePattern pattern = t._2;
            logger.info("mzdbProcessing Pattern : " + score + " " + pattern.charge() + " mz = " + pattern.monoMz());
        }

//      logger.info("Local estimation : ");
//      List<Pair<Double, TheoreticalIsotopePattern>> putativePatterns2 = IsotopePattern.getOrderedIPHypothesis(currentScan.getScanData(), positionMarker.getValue());
//      logger.info("scanId=" + currentScan.getIndex() + ", mz = " + positionMarker.getValue() + ", ppm = " + ppmTol);
//      Iterator<Pair<Double, TheoreticalIsotopePattern>> itj = putativePatterns2.iterator();
//      while (itj.hasNext()) {
//         Pair<Double, TheoreticalIsotopePattern> pair = itj.next();
//         TheoreticalIsotopePattern pattern = pair.getRight();
//         logger.info("Pattern : " + pair.getLeft() + " " + pattern.charge() + " mz = " + pattern.monoMz());
//      }      
        TheoreticalIsotopePattern pattern = (TheoreticalIsotopePattern) putativePatterns[0]._2;
        int refIdx = 0;
        int idx = SpectrumUtils.getNearestPeakIndex(currentScan.getSpectrumData().getMzList(), positionMarker.getValue());
        for (Tuple2 t : pattern.mzAbundancePairs()) {
            if (1e6 * (Math.abs(currentScan.getSpectrumData().getMzList()[idx] - (double) t._1) / currentScan.getSpectrumData().getMzList()[idx]) < ppmTol) {
                break;
            }
            refIdx++;
        }

        if (refIdx < pattern.isotopeCount()) {
            float abundance = currentScan.getSpectrumData().getIntensityList()[idx];
            float normAbundance = (Float) pattern.mzAbundancePairs()[refIdx]._2;
            for (Tuple2 t : pattern.mzAbundancePairs()) {
                Double mz = (Double) t._1;
                Float ab = (Float) t._2;
                PointMarker m = new PointMarker(spectrumPlotPanel, new DataCoordinates(mz, ab * abundance / normAbundance), CyclicColorPalette.getColor(0));
                ipMarkers.add(m);
                scanPlot.addMarker(m);
                int peakIdx = SpectrumUtils.getPeakIndex(currentScan.getSpectrumData().getMzList(), mz, ppmTol);
                if ((peakIdx != -1) && (currentScan.getSpectrumData().getIntensityList()[peakIdx] < 2.0 * ab * abundance / normAbundance)) {
                    logger.info("Peak found mz= " + mz + " expected= " + (ab * abundance / normAbundance) + " observed= " + currentScan.getSpectrumData().getIntensityList()[peakIdx]);
                    PointMarker pm = new PointMarker(spectrumPlotPanel, new DataCoordinates(currentScan.getSpectrumData().getMzList()[peakIdx], currentScan.getSpectrumData().getIntensityList()[peakIdx]), CyclicColorPalette.getColor(5));
                    ipMarkers.add(pm);
                    scanPlot.addMarker(pm);
                }
            }
        }
        spectrumPlotPanel.repaintUpdateDoubleBuffer();
    }

        
    @Override
    public void plotPanelMouseClicked(MouseEvent e, double xValue, double yValue) {
        if (e.getClickCount() == 2) {
            if ((e.getModifiers() & KeyEvent.ALT_MASK) == 0 && xicModeDisplay != DisplayMode.OVERLAY) {
                scanPlot.clearMarkers();
                scanPlot.addMarker(positionMarker);
            }
                positionMarker.setValue(xValue);
                positionMarker.setVisible(true);
                double mz = xValue;
                float ppmTol = (currentScan.getMsLevel() == 1) ? MzScopePreferences.getInstance().getMzPPMTolerance() :  MzScopePreferences.getInstance().getFragmentMzPPMTolerance();  
                double maxMz = mz + mz * ppmTol / 1e6;
                double minMz = mz - mz * ppmTol / 1e6;
                scanPlot.addMarker(new IntervalMarker(spectrumPlotPanel, Color.orange, Color.RED, minMz, maxMz));
                MsnExtractionRequest.Builder builder = MsnExtractionRequest.builder();
                if (currentScan.getMsLevel() == 1) {
                    builder.setMinMz(minMz).setMaxMz(maxMz);
                } else {
                    builder.setMz(currentScan.getPrecursorMz()).setFragmentMz(mz).setFragmentMzTolPPM(ppmTol);
                }
                if ((e.getModifiers() & KeyEvent.ALT_MASK) != 0) {
                    rawFilePanel.extractAndDisplayChromatogram(builder.build(), DisplayMode.OVERLAY, null);
                } else {
                    rawFilePanel.extractAndDisplayChromatogram(builder.build(), xicModeDisplay, null);
                }

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
      int scanIdx = rawFilePanel.getCurrentRawfile().getSpectrumId(retentionTime);
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
   
   public void displayScan(Spectrum scan) {

      double xMin = 0.0, xMax = 0.0, yMin = 0.0, yMax = 0.0;

      if (currentScan != null) {
         xMin = spectrumPlotPanel.getXAxis().getMinValue();
         xMax = spectrumPlotPanel.getXAxis().getMaxValue();
         yMin = spectrumPlotPanel.getYAxis().getMinValue();
         yMax = spectrumPlotPanel.getYAxis().getMaxValue();
      }

      if (scan != null) {
         Color plotColor = rawFilePanel.getPlotColor(rawFilePanel.getCurrentRawfile().getName());
         ScanTableModel scanModel = new ScanTableModel(scan);
         scanModel.setColor(plotColor);
         if (scan.getDataType() == Spectrum.ScanType.CENTROID) { 
            //stick plot
            scanPlot = new PlotStick(spectrumPlotPanel, scanModel, null, ScanTableModel.COLTYPE_SCAN_MASS, ScanTableModel.COLTYPE_SCAN_INTENSITIES);
            ((PlotStick) scanPlot).setStrokeFixed(true);
            ((PlotStick) scanPlot).setPlotInformation(scanModel.getPlotInformation());
            ((PlotStick) scanPlot).setIsPaintMarker(true);
         } else {
            scanPlot = new PlotLinear(spectrumPlotPanel, scanModel, null, ScanTableModel.COLTYPE_SCAN_MASS, ScanTableModel.COLTYPE_SCAN_INTENSITIES);
            ((PlotLinear) scanPlot).setStrokeFixed(true);
            ((PlotLinear) scanPlot).setPlotInformation(scanModel.getPlotInformation());
            ((PlotLinear) scanPlot).setIsPaintMarker(true);
         }

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

   public int getNextScanIndex(Integer spectrumIndex) {
      if (keepMsLevel) 
         return (rawFilePanel.getCurrentRawfile().getNextSpectrumId(spectrumIndex, currentScan.getMsLevel()));
      return Math.min(currentScan.getIndex() + 1, rawFilePanel.getCurrentRawfile().getSpectrumCount()-1);
   } 

   public int getPreviousScanIndex(Integer spectrumIndex) {
      if (keepMsLevel) 
         return (rawFilePanel.getCurrentRawfile().getPreviousSpectrumId(spectrumIndex, currentScan.getMsLevel()));
      return Math.max(1, currentScan.getIndex() - 1);
   } 

   private void updateScanIndexList() {
      List<Integer> listScanIndex = new ArrayList(3);
      Integer currentIndex = currentScan.getIndex();
      listScanIndex.add(getPreviousScanIndex(currentIndex));
      listScanIndex.add(currentIndex);
      listScanIndex.add(getNextScanIndex(currentIndex));
      headerSpectrumPanel.setScanIndexList(listScanIndex);
   }

   @Override
   public void updateXicDisplayMode(DisplayMode mode) {
      xicModeDisplay = mode;
   }

   public DisplayMode getXicModeDisplay() {
      return this.xicModeDisplay;
   }

    @Override
    public void updateAxisRange(double[] oldX, double[] newX,  double[] oldY,  double[] newY) {
        
    }

}
