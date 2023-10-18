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

import fr.profi.mzdb.algo.signal.filtering.ISignalSmoother;
import fr.profi.mzdb.algo.signal.filtering.PartialSavitzkyGolaySmoother;
import fr.profi.mzdb.algo.signal.filtering.SavitzkyGolaySmoother;
import fr.profi.mzdb.algo.signal.filtering.SavitzkyGolaySmoothingConfig;
import fr.profi.mzdb.util.math.DerivativeAnalysis;
import fr.proline.mzscope.model.*;
import fr.proline.mzscope.processing.SpectrumUtils;
import fr.proline.mzscope.ui.dialog.SmoothingParamDialog;
import fr.proline.mzscope.ui.event.ScanHeaderListener;
import fr.proline.mzscope.ui.model.MobilogramTableModel;
import fr.proline.mzscope.ui.model.MzScopePreferences;
import fr.proline.mzscope.ui.model.ScanTableModel;
import fr.proline.mzscope.utils.Display;
import fr.proline.studio.WindowManager;
import fr.proline.studio.export.ExportButton;
import fr.proline.studio.graphics.*;
import fr.proline.studio.graphics.marker.IntervalMarker;
import fr.proline.studio.graphics.measurement.IntegralMeasurement;
import fr.proline.studio.graphics.measurement.WidthMeasurement;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.utils.IconManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.util.List;
import java.util.*;

/**
 * contains the scan panel
 *
 * @author MB243701
 */
public class SpectrumPanel extends AbstractSpectrumPanel implements ScanHeaderListener {

   private static final Logger logger = LoggerFactory.getLogger(SpectrumPanel.class);
   private static final int OVERLAY_KEY = KeyEvent.CTRL_DOWN_MASK;
   
   private ScanHeaderPanel headerSpectrumPanel;
   protected JToolBar spectrumToolbar;
   private boolean keepSameMsLevel = true;
   private ScansSpinnerModel spinnerModel;
  private JToggleButton m_freezeSpectrumBtn;


  private JToggleButton viewMobilogramBtn;
  private final BasePlotPanel mobilogramPlotPanel;
  private Mobilogram mobilogram;

  private MobilitySpectrum mobilitySpectrum;

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
            rawFileViewer.displayScan((Integer)value);
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
    
     super(rawFilePanel);
     PlotPanel plotPanel = new PlotPanel();
     mobilogramPlotPanel = plotPanel.getBasePlotPanel();
     mobilogramPlotPanel.setDrawCursor(true);
     mobilogramPlotPanel.setVisible(false);
   }

   protected JToolBar getSpectrumToolbar() {
      spectrumToolbar = new JToolBar(JToolBar.HORIZONTAL);
      spectrumToolbar.setFloatable(false);
      
      ExportButton exportImageButton = new ExportButton("Graphic", spectrumPlotPanel);
      spectrumToolbar.add(exportImageButton);

      JButton displayIPBtn = new JButton();
      displayIPBtn.setIcon(IconManager.getIcon(IconManager.IconType.ISOTOPES_PREDICTION));
      displayIPBtn.setToolTipText("Display Isotopic Patterns");
      displayIPBtn.addActionListener(e -> displayIsotopicPrediction(positionMarker.getValue()));
      
      spectrumToolbar.add(displayIPBtn);

      m_freezeSpectrumBtn = new JToggleButton(IconManager.getIcon(IconManager.IconType.PIN));
      m_freezeSpectrumBtn.addActionListener(e -> {
        JToggleButton tBtn = (JToggleButton)e.getSource();
        if (tBtn.isSelected()) {
          setReferenceSpectrum(currentScan, 1.0f);
        } else {
           clearReferenceSpectrumData();
        }
      });
      
      spectrumToolbar.add(m_freezeSpectrumBtn);
      
       JToggleButton autoZoomBtn = new JToggleButton();
       autoZoomBtn.setIcon(IconManager.getIcon(IconManager.IconType.AUTO_ZOOM));
       autoZoomBtn.addActionListener(e -> autoZoom = ((JToggleButton)e.getSource()).isSelected());
      
      spectrumToolbar.add(autoZoomBtn);
      
      spectrumToolbar.addSeparator();

     JButton m_showCentroidBtn = new JButton();
      m_showCentroidBtn.setIcon(IconManager.getIcon(IconManager.IconType.CENTROID_SPECTRA));
      m_showCentroidBtn.setToolTipText("Compute and show centroid peaks");
      m_showCentroidBtn.addActionListener(e -> showCentroid());
      spectrumToolbar.add(m_showCentroidBtn);

     JButton m_editSignalBtn = new JButton();
      m_editSignalBtn.setIcon(IconManager.getIcon(IconManager.IconType.SIGNAL));
      m_editSignalBtn.setToolTipText("Spectrum signal processing dialog");
      m_editSignalBtn.addActionListener(e -> editSignal());
      spectrumToolbar.add(m_editSignalBtn);

     JButton testBtn = new JButton("iso");
     testBtn.setToolTipText("de-isotope centroid spectrum");
     testBtn.addActionListener(e -> processMS2Spectrum());
     spectrumToolbar.add(testBtn);

     viewMobilogramBtn = new JToggleButton("IM");
     viewMobilogramBtn.setToolTipText("View mobilogram");
     viewMobilogramBtn.setEnabled(false);
     viewMobilogramBtn.addActionListener(e -> toggleMobilogramPlot());
     spectrumToolbar.add(viewMobilogramBtn);

       spectrumToolbar.addSeparator();
       JButton m_forceCentroidBtn = new JButton();
       m_forceCentroidBtn.setIcon(IconManager.getIcon(IconManager.IconType.FITTED_2_CENTROID));
       m_forceCentroidBtn.setToolTipText("Force Fitted to Centroid");
       m_forceCentroidBtn.addActionListener(e -> {
           rawFileViewer.changeForceFittedToCentroid();
       });
       spectrumToolbar.add(m_forceCentroidBtn);

     spectrumToolbar.addSeparator();

      spinnerModel = new ScansSpinnerModel();
      headerSpectrumPanel = new ScanHeaderPanel(null, spinnerModel);
      headerSpectrumPanel.addScanHeaderListener(this);
      spectrumToolbar.add(headerSpectrumPanel);

     return spectrumToolbar;
   }

  private void toggleMobilogramPlot() {
    if (splitPane.getRightComponent() == null) {
      if (currentScan != null) {
        mobilogramPlotPanel.setPlot(buildMobilogramPlot());
      }
      mobilogramPlotPanel.setVisible(true);
      splitPane.setRightComponent(mobilogramPlotPanel);
      splitPane.setDividerSize(3);
      splitPane.setDividerLocation(0.80);
    } else {
      mobilogramPlotPanel.setVisible(false);
      splitPane.setRightComponent(null);
      splitPane.setDividerLocation(1.0);
      splitPane.setDividerSize(0);
    }
  }

  protected PlotXYAbstract buildMobilogramPlot() {
    Color plotColor = rawFileViewer.getPlotColor(rawFileViewer.getCurrentRawfile() == null ? null : rawFileViewer.getCurrentRawfile().getName());
    MobilogramTableModel model = new MobilogramTableModel(mobilogram);
    PlotXYAbstract plot = null;
    model.setColor(plotColor);
    plot = new PlotLinear(mobilogramPlotPanel, model, null, ScanTableModel.COLTYPE_SCAN_MASS, ScanTableModel.COLTYPE_SCAN_INTENSITIES);
    plot.addMeasurement(new IntegralMeasurement(plot));
    plot.addMeasurement(new WidthMeasurement(plot));
    ((PlotLinear) plot).setStrokeFixed(true);
    ((PlotLinear) plot).setPlotInformation(model.getPlotInformation());
    plot.setIsPaintMarker(true);
    return plot;
  }

  private void processMS2Spectrum() {
    if (currentScan.getDataType() == Spectrum.ScanType.CENTROID) {
      Spectrum newSpectrum = SpectrumUtils.deisotopeCentroidSpectrum(currentScan);
      setReferenceSpectrum(newSpectrum, -1.0f);
    }
  }

    private void showCentroid() {
        
        Signal signal = getSignal();
        scanPlot.clearMarkers(); //clean previous markers
        //Get user preference for smoothing algo
        SmoothingParamDialog dialog = new SmoothingParamDialog(WindowManager.getDefault().getMainWindow());
        dialog.pack();
        dialog.setVisible(true);

        if (dialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
            long start = System.currentTimeMillis(); //VDS time calc
            int nbrPoints  = dialog.getNbrPoint();
            String smoothMethod = dialog.getMethod();
            ISignalSmoother smoother;
            switch(smoothMethod){
                case SmoothingParamDialog.SG_SMOOTHER:
                    smoother = new SavitzkyGolaySmoother(new SavitzkyGolaySmoothingConfig(nbrPoints, 2, 1));
                    break;
                case SmoothingParamDialog.PARTIAL_SG_SMOOTHER:
                    smoother =  new PartialSavitzkyGolaySmoother(new SavitzkyGolaySmoothingConfig(nbrPoints, 4, 1));
                    break;
                case SmoothingParamDialog.BOTH_SMOOTHER:
                    JOptionPane.showMessageDialog(this, "Can't use all smoothing methods, Savitzky-Golay will be used.", "Smoothing Error", JOptionPane.WARNING_MESSAGE);                   
                    smoother = new SavitzkyGolaySmoother(new SavitzkyGolaySmoothingConfig(nbrPoints, 2, 1));
                    break;
                default:
                    smoother = new SavitzkyGolaySmoother(new SavitzkyGolaySmoothingConfig(nbrPoints, 2, 1));
                    break;                    
            }
            
            //Call specified smoother using specified nbr points.
            List<Tuple2> input = signal.toScalaArrayTuple(false);
            Tuple2[] param = input.toArray(new Tuple2[input.size()]);
            long step1a = System.currentTimeMillis();  //VDS time calc
            Tuple2[] result = smoother.smoothTimeIntensityPairs(param);
            long step1 = System.currentTimeMillis();  //VDS time calc
            int resultLength = result.length;
            logger.debug("Smoothing: signal length after smoothing = "+resultLength+" vs before "+input.size()+". TIME: "+(step1-start)+" which "+(step1a-start)+" for arrays");
            double[] x = new double[resultLength];
            double[] y = new double[resultLength];
            for (int k = 0; k < resultLength; k++) {
               x[k] = (Double)result[k]._1;
               y[k] = (Double)result[k]._2;
            }
            Signal newSignal = new Signal(x,y);
            long step2 = System.currentTimeMillis();      
            logger.debug("Created new Signal. TIME: "+(step2-step1));
            
            DerivativeAnalysis.ILocalDerivativeChange[] mm = DerivativeAnalysis.findMiniMaxi(newSignal.getYSeries());
            long step3 = System.currentTimeMillis();      
            logger.debug("DerivativeAnalysis Done, number min/max points = "+mm.length+". TIME: "+(step3-step2));
            
            //Create new Spectrum
            int realLenght = 0;
            double[] newSpMasses = new double[mm.length];
            float[] newSpIntensities = new float[mm.length];
            double[] centroidSignalX = new double[mm.length];
            double[] centroidSignalY = new double[mm.length];
            for (int k = 0; k < mm.length; k++) {
               if(mm[k].isMaximum()){
                  double massIdx = newSignal.getXSeries()[mm[k].index()];
                  double intensityIdx = signal.getYSeries()[mm[k].index()];
                  centroidSignalY[realLenght] = 
                  centroidSignalX[realLenght] = massIdx;
                  centroidSignalY[realLenght] = intensityIdx;                   
                  newSpMasses[realLenght] = massIdx;
                  newSpIntensities[realLenght] = (float) intensityIdx;                  
                  realLenght++;
               }
            }
            Spectrum newSpectrum = new Spectrum(-1, currentScan.getRetentionTime() , Arrays.copyOfRange(newSpMasses, 0, realLenght), Arrays.copyOfRange(newSpIntensities, 0, realLenght), currentScan.getMsLevel(), Spectrum.ScanType.CENTROID);
            long step4 = System.currentTimeMillis();
            logger.debug("Create CentroidSignal values + display markers.Nbr real points = "+realLenght+". TIME: "+(step4-step3)+ "TOTAL == "+(step4-start));
            setReferenceSpectrum(newSpectrum, 1.0f);
            m_freezeSpectrumBtn.setSelected(true);

        }
    }

    private void editSignal() {
      Signal signal = getSignal();
      List<Signal> signals = new ArrayList<>();
      signals.add(signal);
      JDialog dialog = new JDialog((JFrame)this.getTopLevelAncestor(), "Spectra editor", true);
      dialog.setContentPane(SignalViewerBuilder.buildEditor(signals));
      dialog.pack();
      dialog.setVisible(true);
   }

    private Signal getSignal() {
      double min = spectrumPlotPanel.getXAxis().getMinValue();
      double max = spectrumPlotPanel.getXAxis().getMaxValue();      
      int minIdx = SpectrumUtils.getNearestPeakIndex(currentScan.getMasses(), min);
      int maxIdx = Math.min(SpectrumUtils.getNearestPeakIndex(currentScan.getMasses(), max)+1, currentScan.getMasses().length);
      Signal currentSignal = new Signal(Arrays.copyOfRange(currentScan.getMasses(), minIdx, maxIdx), Arrays.copyOfRange(currentScan.getIntensities(), minIdx, maxIdx));      
      if(currentScan.getDataType().equals(Spectrum.ScanType.CENTROID))
          currentSignal.setSignalType(Signal.CENTROID);
      return currentSignal;
    }

        
    @Override
    public void plotPanelMouseClicked(MouseEvent e, double mz, double yValue) {
      if (e.getClickCount() == 2) {
        if ((e.getModifiersEx() & OVERLAY_KEY) == 0 && rawFileViewer.getChromatogramDisplayMode() != Display.Mode.OVERLAY) {
          scanPlot.clearMarkers();
          scanPlot.addMarker(positionMarker);
        }
        positionMarker.setValue(mz);
        positionMarker.setVisible(true);
        float ppmTol = (currentScan.getMsLevel() == 1) ? MzScopePreferences.getInstance().getMzPPMTolerance() : MzScopePreferences.getInstance().getFragmentMzPPMTolerance();
        double maxMz = mz + mz * ppmTol / 1e6;
        double minMz = mz - mz * ppmTol / 1e6;
        scanPlot.addMarker(new IntervalMarker(spectrumPlotPanel, Color.orange, Color.RED, minMz, maxMz));
        ExtractionRequest.Builder builder = ExtractionRequest.builder(this);
        if (currentScan.getMsLevel() == 1) {
          builder.setMz(mz).setMzTolPPM(ppmTol);
        } else {
          builder.setMz(currentScan.getPrecursorMz()).setFragmentMz(mz).setFragmentMzTolPPM(ppmTol);
        }

        // clone last request mobility query.
        // TODO move to ExtractionRequest.Builder.cloneMobility(ExtractionRequest r)
        final ExtractionRequest lastExtractionRequest = MzScopePreferences.getInstance().getLastExtractionRequest();
        if (lastExtractionRequest != null && !lastExtractionRequest.getMobilityRequestType().equals(ExtractionRequest.Type.NONE)) {
          if (lastExtractionRequest.getMobilityRequestType().equals(ExtractionRequest.Type.CENTERED)) {
            builder.setMobility(lastExtractionRequest.getMobility());
            builder.setMobilityTol(lastExtractionRequest.getMobilityTol());
          } else {
            builder.setMinMobility(lastExtractionRequest.getMinMobility());
            builder.setMaxMobility(lastExtractionRequest.getMaxMobility());
          }
        }

        final ExtractionRequest extractionRequest = builder.build();
        if ((e.getModifiersEx() & OVERLAY_KEY) != 0) {
          rawFileViewer.extractAndDisplay(extractionRequest, new Display(Display.Mode.OVERLAY), null);
        } else {
          rawFileViewer.extractAndDisplay(extractionRequest, new Display(rawFileViewer.getChromatogramDisplayMode()), null);
        }

        updateMzRange(extractionRequest.getMinMz(), extractionRequest.getMaxMz());

      } else if (SwingUtilities.isLeftMouseButton(e)) {
        positionMarker.setValue(mz);
        positionMarker.setVisible(true);
      }
    }


  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    if (evt.getPropertyName().equals(IRawFileViewer.LAST_EXTRACTION_REQUEST) ) {
      ExtractionRequest request = (ExtractionRequest)evt.getNewValue();
      if (request.getSource() != this) {

        logger.info("IRawFileViewer last extraction applied was " + request);

        if (mobilitySpectrum != null) {
          if (!request.getMobilityRequestType().equals(ExtractionRequest.Type.NONE)) {
            updateMobilityRange(request.getMinMobility(), request.getMaxMobility());
          } else if (rawFileViewer.getCurrentRawfile() != null) {
            final IonMobilityIndex mobilityIndex = rawFileViewer.getCurrentRawfile().getIonMobilityIndex();
            updateMobilityRange(mobilityIndex.getMinValue(), mobilityIndex.getMaxValue());
          }
        }

        // WARN : must be done after potential ion mobility scan update
        if (currentScan != null) {
          if (!request.getMzRequestType().equals(ExtractionRequest.Type.NONE)) {
            if (rawFileViewer.getChromatogramDisplayMode() != Display.Mode.OVERLAY) {
              scanPlot.clearMarkers();
              scanPlot.addMarker(positionMarker);
            }
            positionMarker.setValue(request.getMz());
            positionMarker.setVisible(true);
            scanPlot.addMarker(new IntervalMarker(spectrumPlotPanel, Color.orange, Color.RED, request.getMinMz(), request.getMaxMz()));
            spectrumPlotPanel.repaint();
          }
        }

        if ((mobilitySpectrum != null) && mobilogramPlotPanel.isVisible()) {
          if (request.getMzRequestType().equals(ExtractionRequest.Type.NONE)) {
            updateMzRange(Double.MIN_VALUE, Double.MAX_VALUE);
          } else {
            updateMzRange(request.getMinMz(), request.getMaxMz());
          }
          mobilogramPlotPanel.getPlots().get(0).clearMarkers();
          if (!request.getMobilityRequestType().equals(ExtractionRequest.Type.NONE)) {
            mobilogramPlotPanel.getPlots().get(0).addMarker(new IntervalMarker(mobilogramPlotPanel, Color.orange, Color.RED, request.getMinMobility(), request.getMaxMobility()));
          }
          mobilogramPlotPanel.repaint();
        }

      } else {
        logger.debug("Event ignored : "+ evt);
      }
    }
  }

   public void updateMobilityRange(double min, double max) {
      if ((mobilitySpectrum.getMsLevel() == 1)) {
        Spectrum spectrum = mobilitySpectrum.applyMobilityFilterbySum(min, max);
        _displayScan(spectrum);
      }
   }

  public void updateMzRange(double min, double max) {
    if (mobilogramPlotPanel.isVisible() && mobilogram != null) {
      mobilogram.setMzFilter(min, max);
      mobilogramPlotPanel.getPlots().get(0).update();
      mobilogramPlotPanel.repaint();
    }
  }


  private MobilitySpectrum asMobilitySpectrum(Spectrum scan) {
    if (MobilitySpectrum.class.isAssignableFrom(scan.getClass())) {
      return (MobilitySpectrum)scan;
    } else {
      return new MobilitySpectrum(scan, rawFileViewer.getCurrentRawfile().getIonMobilityIndex());
    }
  }

  @Override
   public void updateRetentionTime(float retentionTime) {
      int scanIdx = rawFileViewer.getCurrentRawfile().getSpectrumId(retentionTime);
      rawFileViewer.displayScan(scanIdx);
   }

   @Override
   public void keepMsLevel(boolean keep) {
      this.keepSameMsLevel = keep;
   }

   public void addMarkerRange(double minMz, double maxMz) {
      scanPlot.addMarker(new IntervalMarker(spectrumPlotPanel, Color.orange, Color.RED, minMz, maxMz));
   }

  public void displayScan(Spectrum scan) {
    if ((scan != null) && !MobilitySpectrum.class.isAssignableFrom(scan.getClass())) {

      Spectrum wScan = scan;
      if (scan.hasIonMobilitySeparation()) {
        mobilitySpectrum =  asMobilitySpectrum(scan);
        mobilogram = new Mobilogram(mobilitySpectrum);
        wScan = mobilitySpectrum.applyMobilityFilterbySum(Double.MIN_VALUE, Double.MAX_VALUE);
      } else {
        mobilitySpectrum = null;
        mobilogram = null;
      }

      _displayScan(wScan);
      headerSpectrumPanel.setMzdbFileName(rawFileViewer.getCurrentRawfile().getName());
      spinnerModel.setValue(currentScan.getIndex());
      headerSpectrumPanel.setScan(currentScan);
      viewMobilogramBtn.setEnabled(mobilitySpectrum != null);

      if (mobilogramPlotPanel.isVisible() && mobilogram != null) {
        mobilogramPlotPanel.setPlot(buildMobilogramPlot());
        mobilogramPlotPanel.repaint();
      }

    } else {
      _displayScan(scan);
      viewMobilogramBtn.setEnabled((scan != null) && scan.hasIonMobilitySeparation());
    }
   }


  private void _displayScan(Spectrum scan) {
      super.displayScan(scan);
  }

    public int getNextScanIndex(Integer spectrumIndex) {
      if (keepSameMsLevel) 
         return (rawFileViewer.getCurrentRawfile().getNextSpectrumId(spectrumIndex, currentScan.getMsLevel()));
      return Math.min(currentScan.getIndex() + 1, rawFileViewer.getCurrentRawfile().getSpectrumCount()-1);
   } 

   public int getPreviousScanIndex(Integer spectrumIndex) {
      if (keepSameMsLevel) 
         return (rawFileViewer.getCurrentRawfile().getPreviousSpectrumId(spectrumIndex, currentScan.getMsLevel()));
      return Math.max(1, currentScan.getIndex() - 1);
   } 


}
