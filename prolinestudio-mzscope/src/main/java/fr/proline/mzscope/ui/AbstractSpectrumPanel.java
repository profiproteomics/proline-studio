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

import fr.profi.ms.model.TheoreticalIsotopePattern;
import fr.proline.mzscope.model.Spectrum;
import fr.proline.mzscope.processing.IsotopicPatternUtils;
import fr.proline.mzscope.processing.SpectrumUtils;
import fr.proline.mzscope.ui.dialog.IsotopicPredictionParamDialog;
import fr.proline.mzscope.ui.model.MzScopePreferences;
import fr.proline.mzscope.ui.model.ScanTableModel;
import fr.proline.studio.WindowManager;
import fr.proline.studio.graphics.*;
import fr.proline.studio.graphics.marker.*;
import fr.proline.studio.graphics.marker.coordinates.DataCoordinates;
import fr.proline.studio.graphics.measurement.IntegralMeasurement;
import fr.proline.studio.graphics.measurement.WidthMeasurement;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.utils.CyclicColorPalette;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static fr.proline.studio.graphics.marker.LabelMarker.ORIENTATION_XY_MIDDLE;

/**
 * contains the scan panel
 *
 * @author MB243701
 */
public abstract class AbstractSpectrumPanel extends JPanel implements PlotPanelListener, PropertyChangeListener {

  private class ReferenceSpectrum {
      public final Spectrum spectrum;
      public final Float scaleFactor;

      protected ReferenceSpectrum(Spectrum spectrum, Float scaleFactor) {
        this.spectrum = spectrum;
        this.scaleFactor = scaleFactor;
      }
   }

   private static final Logger logger = LoggerFactory.getLogger(AbstractSpectrumPanel.class);
   private static final DecimalFormat SCORE_FORMATTER = new DecimalFormat("#.###");
   protected final IRawFileViewer rawFileViewer;
  protected BasePlotPanel spectrumPlotPanel;

  protected JSplitPane splitPane;

  protected PlotXYAbstract scanPlot;
  protected LineMarker positionMarker;

  protected Spectrum currentScan;
  protected ReferenceSpectrum referenceSpectrum;
  protected boolean autoZoom = false;
  protected List<AbstractMarker> ipMarkers = new ArrayList();


   public AbstractSpectrumPanel(IRawFileViewer rawFileViewer) {
      super();
      this.rawFileViewer = rawFileViewer;
      this.rawFileViewer.addPropertyChangeListener(IRawFileViewer.LAST_EXTRACTION_REQUEST, this);
   }

   public void initComponents() {
     setLayout(new BorderLayout());
      // Create Scan Charts
      PlotPanel plotPanel = new PlotPanel();
      spectrumPlotPanel = plotPanel.getBasePlotPanel();
      spectrumPlotPanel.setDrawCursor(true);
      spectrumPlotPanel.addListener(this);
      positionMarker = new LineMarker(spectrumPlotPanel, 0.0, LineMarker.ORIENTATION_VERTICAL, Color.BLUE, false);
      
      spectrumPlotPanel.repaint();

      splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
      splitPane.setLeftComponent(plotPanel);
      splitPane.setDividerLocation(1.0);
      splitPane.setDividerSize(0);

      this.add(splitPane, BorderLayout.CENTER);
      this.add(getSpectrumToolbar(), BorderLayout.NORTH);

   }

  protected abstract JToolBar getSpectrumToolbar();

  public void displayIsotopicPrediction(double mozToPredict) {

    Spectrum spectrum = currentScan;

    if (referenceSpectrum != null) {
      IsotopicPredictionParamDialog dialog = new IsotopicPredictionParamDialog(WindowManager.getDefault().getMainWindow());
      dialog.pack();
      dialog.setVisible(true);
      if (dialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
        spectrum = (dialog.getSpectrum() == IsotopicPredictionParamDialog.CURRENT_SPECTRUM) ? currentScan : referenceSpectrum.spectrum;
      }
    }

    ipMarkers.stream().forEach((m) -> {
      scanPlot.removeMarker(m);
    });
    ipMarkers = new ArrayList();
    float ppmTol = MzScopePreferences.getInstance().getMzPPMTolerance();

    Tuple2<Object, TheoreticalIsotopePattern> prediction = IsotopicPatternUtils.predictIsotopicPattern(spectrum.getSpectrumData(), mozToPredict, ppmTol);
    displayIsotopes(spectrum, mozToPredict, prediction, ppmTol, 5);
    
    spectrumPlotPanel.repaintUpdateDoubleBuffer();
  }

  private void displayIsotopes(Spectrum spectrum, double mozToPredict, Tuple2<Object, TheoreticalIsotopePattern> scoredPattern, float ppmTol, int colorIndex) {
    // search for the index of the user selected mz value
    int referenceMzIdx = 0;
    TheoreticalIsotopePattern pattern = scoredPattern._2;
    int idx = SpectrumUtils.getNearestPeakIndex(spectrum.getSpectrumData().getMzList(), mozToPredict);
    for (Tuple2 t : pattern.mzAbundancePairs()) {
      if (1e6 * (Math.abs(spectrum.getSpectrumData().getMzList()[idx] - (double) t._1) / spectrum.getSpectrumData().getMzList()[idx]) < ppmTol) {
        break;
      }
      referenceMzIdx++;
    }

    if (referenceMzIdx < pattern.isotopeCount()) {
      float abundance = spectrum.getSpectrumData().getIntensityList()[idx];
      float normAbundance = (Float) pattern.mzAbundancePairs()[referenceMzIdx]._2;
      for (Tuple2 t : pattern.mzAbundancePairs()) {
        Double mz = (Double) t._1;
        Float ab = (Float) t._2;
        PointMarker m = new PointMarker(spectrumPlotPanel, new DataCoordinates(mz, ab * abundance / normAbundance), CyclicColorPalette.getColor(0));
        ipMarkers.add(m);
        scanPlot.addMarker(m);
        int peakIdx = SpectrumUtils.getPeakIndex(spectrum.getSpectrumData().getMzList(), mz, ppmTol);
        if ((peakIdx != -1)) {
          logger.info("Peak found mz= " + mz + " expected= " + (ab * abundance / normAbundance) + " observed= " + spectrum.getSpectrumData().getIntensityList()[peakIdx]);
          PointMarker pm = new PointMarker(spectrumPlotPanel, new DataCoordinates(spectrum.getSpectrumData().getMzList()[peakIdx], spectrum.getSpectrumData().getIntensityList()[peakIdx]), CyclicColorPalette.getColor(colorIndex));
          ipMarkers.add(pm);
          scanPlot.addMarker(pm);
        }
      }
      Double mz = 0.1 + ((Double) pattern.mzAbundancePairs()[0]._1 + (Double) pattern.mzAbundancePairs()[1]._1) / 2.0;
      Float ab = (Float) pattern.mzAbundancePairs()[0]._2 * 0.75f;
      StringBuilder labelTxt = new StringBuilder("charge ");
      labelTxt.append(pattern.charge()).append("+").append("(").append(SCORE_FORMATTER.format(scoredPattern._1)).append(")");
      LabelMarker label = new LabelMarker(spectrumPlotPanel, new DataCoordinates(mz, ab * abundance / normAbundance), labelTxt.toString(), ORIENTATION_XY_MIDDLE, ORIENTATION_XY_MIDDLE, CyclicColorPalette.getColor(0));
      ipMarkers.add(label);
      scanPlot.addMarker(label);
    }
  }

   public void addMarkerRange(double minMz, double maxMz) {
      scanPlot.addMarker(new IntervalMarker(spectrumPlotPanel, Color.orange, Color.RED, minMz, maxMz));
   }
   
   public void displayScan(Spectrum scan) {

      double xMin = 0.0, xMax = 0.0, yMin = 0.0, yMax = 0.0;

      if ((currentScan != null) && (currentScan.getMasses().length > 0)){
         xMin = spectrumPlotPanel.getXAxis().getMinValue();
         xMax = spectrumPlotPanel.getXAxis().getMaxValue();
         yMin = spectrumPlotPanel.getYAxis().getMinValue();
         yMax = spectrumPlotPanel.getYAxis().getMaxValue();
      }

      if (scan != null && scan.getMasses().length > 0) {    
        
//       logger.info("display scan id = {}, masses length = {} ", scan.getIndex(), scan.getMasses().length);
         Color plotColor = rawFileViewer.getPlotColor(rawFileViewer.getCurrentRawfile() == null ? null : rawFileViewer.getCurrentRawfile().getName());
         ScanTableModel scanModel = new ScanTableModel(scan);
         scanModel.setColor(plotColor);
         scanPlot = buildPlot(scan, plotColor, 1.0f);
         spectrumPlotPanel.setPlot(scanPlot);
         spectrumPlotPanel.lockMinXValue();
         spectrumPlotPanel.lockMinYValue();

         if ((currentScan != null) && (currentScan.getMasses().length > 0) && (currentScan.getMsLevel() == scan.getMsLevel())) {
            if (!autoZoom) { 
                spectrumPlotPanel.getXAxis().setRange(xMin, xMax);
                spectrumPlotPanel.getYAxis().setRange(yMin, yMax);
            } else {
                spectrumPlotPanel.getXAxis().setRange(xMin, xMax);                
            }
         }
         
         if ((currentScan != null) && (currentScan.getMsLevel() != scan.getMsLevel())) {
            positionMarker.setVisible(false);
         }
         
         scanPlot.addMarker(positionMarker);
         _displayReferenceSpectrum();
         spectrumPlotPanel.repaint();
         currentScan = scan;
      } else if (scan != null) {
        logger.info("display scan id = {},contains no data ", scan.getIndex());
         currentScan = scan;
         spectrumPlotPanel.clearPlotsWithRepaint();
      } else {
        spectrumPlotPanel.clearPlotsWithRepaint();
      }
   }

    protected void _displayReferenceSpectrum() {
        if (referenceSpectrum != null) {
            
            double xMin = 0.0, xMax = 0.0, yMin = 0.0, yMax = 0.0;
            
            if (currentScan != null) {
               xMin = spectrumPlotPanel.getXAxis().getMinValue();
               xMax = spectrumPlotPanel.getXAxis().getMaxValue();
               yMin = spectrumPlotPanel.getYAxis().getMinValue();
               yMax = spectrumPlotPanel.getYAxis().getMaxValue();
            }

            PlotXYAbstract plot = buildPlot(referenceSpectrum.spectrum, CyclicColorPalette.getColor(5), referenceSpectrum.scaleFactor);
            spectrumPlotPanel.addPlot(plot, true);
            spectrumPlotPanel.lockMinYValue();

          if (currentScan != null) {
                double[] minMaxY = spectrumPlotPanel.getMinMaxPlots(spectrumPlotPanel.getYAxis());
                spectrumPlotPanel.getXAxis().setRange(xMin, xMax);
                spectrumPlotPanel.getYAxis().setRange(Math.min(yMin, minMaxY[0]), yMax);
            } 
            
            spectrumPlotPanel.repaint();
        }
    }

    
    protected PlotXYAbstract buildPlot(Spectrum scan, Color plotColor, float scaleFactor) {
        ScanTableModel scanModel = new ScanTableModel(scan, scaleFactor);
        PlotXYAbstract plot = null;
        scanModel.setColor(plotColor);
        if (scan.getDataType() == Spectrum.ScanType.CENTROID) {
            //stick plot
            plot = new PlotStick(spectrumPlotPanel, scanModel, null, ScanTableModel.COLTYPE_SCAN_MASS, ScanTableModel.COLTYPE_SCAN_INTENSITIES);
            ((PlotStick) plot).setStrokeFixed(true);
            ((PlotStick) plot).setPlotInformation(scanModel.getPlotInformation());
        } else {
            plot = new PlotLinear(spectrumPlotPanel, scanModel, null, ScanTableModel.COLTYPE_SCAN_MASS, ScanTableModel.COLTYPE_SCAN_INTENSITIES);
            plot.addMeasurement(new IntegralMeasurement(plot));
            plot.addMeasurement(new WidthMeasurement(plot));
            ((PlotLinear) plot).setStrokeFixed(true);
            ((PlotLinear) plot).setPlotInformation(scanModel.getPlotInformation());
        }
        plot.setIsPaintMarker(true);
        return plot;
    }
   
    public void clearReferenceSpectrumData() {
        if (referenceSpectrum != null) {
            referenceSpectrum = null;
            displayScan(currentScan);
        }
    }

    public void setReferenceSpectrum(Spectrum spectrum, Float scaleFactor) {
      referenceSpectrum = new ReferenceSpectrum(spectrum, scaleFactor);
      displayScan(currentScan);
    }


  @Override
  public void updateAxisRange(double[] oldX, double[] newX,  double[] oldY,  double[] newY) {

  }
}
