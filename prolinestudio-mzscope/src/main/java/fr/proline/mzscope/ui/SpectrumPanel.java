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

import fr.proline.mzscope.ui.model.ScanTableModel;
import fr.profi.ms.model.TheoreticalIsotopePattern;
import fr.proline.mzscope.model.MsnExtractionRequest;
import fr.proline.mzscope.ui.model.MzScopePreferences;
import fr.proline.mzscope.model.Spectrum;
import fr.proline.mzscope.ui.event.ScanHeaderListener;
import fr.proline.mzscope.processing.IsotopicPatternUtils;
import fr.proline.mzscope.processing.SpectrumUtils;
import fr.proline.mzscope.utils.Display;
import fr.proline.studio.export.ExportButton;
import fr.proline.studio.graphics.PlotXYAbstract;
import fr.proline.studio.graphics.PlotLinear;
import fr.proline.studio.graphics.BasePlotPanel;
import fr.proline.studio.graphics.PlotPanel;
import fr.proline.studio.graphics.PlotPanelListener;
import fr.proline.studio.graphics.PlotStick;
import fr.proline.studio.graphics.marker.AbstractMarker;
import fr.proline.studio.graphics.marker.IntervalMarker;
import fr.proline.studio.graphics.marker.LabelMarker;
import static fr.proline.studio.graphics.marker.LabelMarker.ORIENTATION_XY_MIDDLE;
import fr.proline.studio.graphics.marker.LineMarker;
import fr.proline.studio.graphics.marker.PointMarker;
import fr.proline.studio.graphics.marker.coordinates.DataCoordinates;
import fr.proline.studio.graphics.measurement.IntegralMeasurement;
import fr.proline.studio.graphics.measurement.WidthMeasurement;
import fr.proline.studio.utils.CyclicColorPalette;
import fr.proline.studio.utils.IconManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractSpinnerModel;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
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
   private static final int OVERLAY_KEY = KeyEvent.CTRL_MASK;
   
   private final IRawFileViewer rawFilePanel;
   private ScanHeaderPanel headerSpectrumPanel;
   protected BasePlotPanel spectrumPlotPanel;
   protected JToolBar spectrumToolbar;
   
   protected PlotXYAbstract scanPlot;
   protected LineMarker positionMarker;

   protected Spectrum currentScan;
   protected Spectrum referenceSpectrum;
   
   private boolean keepSameMsLevel = true;
   private boolean autoZoom = false;
   private List<AbstractMarker> ipMarkers = new ArrayList();
   private ScansSpinnerModel spinnerModel;

class ScansSpinnerModel extends AbstractSpinnerModel {

    @Override
    public Object getValue() {
        return (currentScan == null) ? 0 : currentScan.getIndex();
    }

    @Override
    public void setValue(Object value) {
        if (((Integer)value).intValue() != ((Integer)getValue()).intValue()) {
            // the supplied index is not the index of the currentScan : force display (the modification came from the spinner itself)
            // the display starts from the rawFilePanel to update markers in chromatogram display
            rawFilePanel.displayScan((Integer)value);
        }
        // allow GUI update by firing the event.
        fireStateChanged();
    }

    @Override
    public Object getNextValue() {
        return (currentScan == null) ? 0 : getNextScanIndex(currentScan.getIndex());
    }

    @Override
    public Object getPreviousValue() {
        return (currentScan == null) ? 0 :  getPreviousScanIndex(currentScan.getIndex());
    }
    
}


   public SpectrumPanel(IRawFileViewer rawFilePanel) {
      super();
      this.rawFilePanel = rawFilePanel;
   }

   public void initComponents() {
      // Create Scan Charts
      PlotPanel plotPanel = new PlotPanel();
      spectrumPlotPanel = plotPanel.getBasePlotPanel();
      spectrumPlotPanel.addListener(this);
      spectrumPlotPanel.setDrawCursor(true);

      positionMarker = new LineMarker(spectrumPlotPanel, 0.0, LineMarker.ORIENTATION_VERTICAL, Color.BLUE, false);
      
      spectrumPlotPanel.repaint();

      this.removeAll();
      this.add(plotPanel, BorderLayout.CENTER);
      this.add(getSpectrumToolbar(), BorderLayout.NORTH);
   }

   private JToolBar getSpectrumToolbar() {
      spectrumToolbar = new JToolBar(JToolBar.HORIZONTAL);
      spectrumToolbar.setFloatable(false);
      
      ExportButton exportImageButton = new ExportButton("Graphic", spectrumPlotPanel);
      spectrumToolbar.add(exportImageButton);

      JButton displayIPBtn = new JButton();
      displayIPBtn.setIcon(IconManager.getIcon(IconManager.IconType.ISOTOPES_PREDICTION));
      displayIPBtn.setToolTipText("Display Isotopic Patterns");
      displayIPBtn.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            displayIsotopicPatterns();
         }
      });
      
      spectrumToolbar.add(displayIPBtn);
      
      JToggleButton freezeSpectrumBtn = new JToggleButton(IconManager.getIcon(IconManager.IconType.PIN));
      
      freezeSpectrumBtn.addActionListener(new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
              JToggleButton tBtn = (JToggleButton)e.getSource();
            if (tBtn.isSelected()) {
               displayReferenceSpectrum(currentScan);
            } else {
               clearReferenceSpectrumData();
            }
          }
      });
      
      spectrumToolbar.add(freezeSpectrumBtn);
      
       JToggleButton autoZoomBtn = new JToggleButton();
       autoZoomBtn.setIcon(IconManager.getIcon(IconManager.IconType.AUTO_ZOOM));
       autoZoomBtn.addActionListener(new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
              autoZoom = ((JToggleButton)e.getSource()).isSelected();
          }
      });
      
      spectrumToolbar.add(autoZoomBtn);
      
      spectrumToolbar.addSeparator();
      
      spinnerModel = new ScansSpinnerModel(); 
      
      headerSpectrumPanel = new ScanHeaderPanel(null, spinnerModel);
      headerSpectrumPanel.addScanHeaderListener(this);
      spectrumToolbar.add(headerSpectrumPanel);

      return spectrumToolbar;
   }

    private void displayIsotopicPatterns() {
        ipMarkers.stream().forEach((m) -> {
            scanPlot.removeMarker(m);
        });
        ipMarkers = new ArrayList();
        float ppmTol = MzScopePreferences.getInstance().getMzPPMTolerance();

        //IsotopicPatternUtils.compareIsotopicPatternPredictions(currentScan.getSpectrumData(), positionMarker.getValue(), ppmTol);

        TheoreticalIsotopePattern pattern = IsotopicPatternUtils.predictIsotopicPattern(currentScan.getSpectrumData(), positionMarker.getValue(), ppmTol);
        
        // search for the index of the user selected mz value
        int referenceMzIdx = 0;
        int idx = SpectrumUtils.getNearestPeakIndex(currentScan.getSpectrumData().getMzList(), positionMarker.getValue());
        for (Tuple2 t : pattern.mzAbundancePairs()) {
            if (1e6 * (Math.abs(currentScan.getSpectrumData().getMzList()[idx] - (double) t._1) / currentScan.getSpectrumData().getMzList()[idx]) < ppmTol) {
                break;
            }
            referenceMzIdx++;
        }

        if (referenceMzIdx < pattern.isotopeCount()) {
            float abundance = currentScan.getSpectrumData().getIntensityList()[idx];
            float normAbundance = (Float) pattern.mzAbundancePairs()[referenceMzIdx]._2;
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
            Double mz = 0.1+((Double)pattern.mzAbundancePairs()[0]._1 + (Double)pattern.mzAbundancePairs()[1]._1)/2.0;
            Float ab = (Float)pattern.mzAbundancePairs()[0]._2 * 0.75f;
            LabelMarker label = new LabelMarker(spectrumPlotPanel, new DataCoordinates(mz, ab * abundance / normAbundance), "charge "+pattern.charge()+"+", ORIENTATION_XY_MIDDLE,ORIENTATION_XY_MIDDLE, CyclicColorPalette.getColor(0));
            ipMarkers.add(label);
            scanPlot.addMarker(label);

        }
        spectrumPlotPanel.repaintUpdateDoubleBuffer();
    }

        
    @Override
    public void plotPanelMouseClicked(MouseEvent e, double xValue, double yValue) {
        if (e.getClickCount() == 2) {
            if ((e.getModifiers() & OVERLAY_KEY) == 0 && rawFilePanel.getXicDisplayMode() != Display.Mode.OVERLAY) {
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
                if ((e.getModifiers() & OVERLAY_KEY) != 0) {
                    rawFilePanel.extractAndDisplayChromatogram(builder.build(), new Display(Display.Mode.OVERLAY), null);
                } else {
                    rawFilePanel.extractAndDisplayChromatogram(builder.build(), new Display(rawFilePanel.getXicDisplayMode()), null);
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
      this.keepSameMsLevel = keep;
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
         scanPlot = buildPlot(scan, rawFilePanel.getPlotColor(rawFilePanel.getCurrentRawfile().getName()));
         spectrumPlotPanel.setPlot(scanPlot);
         spectrumPlotPanel.lockMinXValue();
         spectrumPlotPanel.lockMinYValue();

         if ((currentScan != null) && (currentScan.getMsLevel() == scan.getMsLevel())) {
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
         displayReferenceSpectrum(referenceSpectrum);
         spectrumPlotPanel.repaint();
         headerSpectrumPanel.setMzdbFileName(rawFilePanel.getCurrentRawfile().getName());
         currentScan = scan;
         spinnerModel.setValue(currentScan.getIndex());
         headerSpectrumPanel.setScan(currentScan);
      }
   }

    public void displayReferenceSpectrum(Spectrum spectrum) {
        if (spectrum != null) {
            
            double xMin = 0.0, xMax = 0.0, yMin = 0.0, yMax = 0.0;
            
            if (currentScan != null) {
               xMin = spectrumPlotPanel.getXAxis().getMinValue();
               xMax = spectrumPlotPanel.getXAxis().getMaxValue();
               yMin = spectrumPlotPanel.getYAxis().getMinValue();
               yMax = spectrumPlotPanel.getYAxis().getMaxValue();
            }

            PlotXYAbstract plot = buildPlot(spectrum, CyclicColorPalette.getColor(5));                        
            spectrumPlotPanel.addPlot(plot);
            
            if (currentScan != null) {
                spectrumPlotPanel.getXAxis().setRange(xMin, xMax);
                spectrumPlotPanel.getYAxis().setRange(yMin, yMax);
            } 
            
            referenceSpectrum = spectrum;
            spectrumPlotPanel.repaint();
        }
    }

    
    private PlotXYAbstract buildPlot(Spectrum scan, Color plotColor) {
        ScanTableModel scanModel = new ScanTableModel(scan);
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
    
   public int getNextScanIndex(Integer spectrumIndex) {
      if (keepSameMsLevel) 
         return (rawFilePanel.getCurrentRawfile().getNextSpectrumId(spectrumIndex, currentScan.getMsLevel()));
      return Math.min(currentScan.getIndex() + 1, rawFilePanel.getCurrentRawfile().getSpectrumCount()-1);
   } 

   public int getPreviousScanIndex(Integer spectrumIndex) {
      if (keepSameMsLevel) 
         return (rawFilePanel.getCurrentRawfile().getPreviousSpectrumId(spectrumIndex, currentScan.getMsLevel()));
      return Math.max(1, currentScan.getIndex() - 1);
   } 

    @Override
    public void updateAxisRange(double[] oldX, double[] newX,  double[] oldY,  double[] newY) {
        
    }

}
