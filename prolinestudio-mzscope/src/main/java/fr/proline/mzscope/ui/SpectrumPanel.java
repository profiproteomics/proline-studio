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
import fr.profi.mzdb.algo.signal.filtering.ISignalSmoother;
import fr.profi.mzdb.algo.signal.filtering.PartialSavitzkyGolaySmoother;
import fr.profi.mzdb.algo.signal.filtering.SavitzkyGolaySmoother;
import fr.profi.mzdb.algo.signal.filtering.SavitzkyGolaySmoothingConfig;
import fr.profi.mzdb.util.math.DerivativeAnalysis;
import fr.proline.mzscope.model.MsnExtractionRequest;
import fr.proline.mzscope.ui.model.MzScopePreferences;
import fr.proline.mzscope.model.Spectrum;
import fr.proline.mzscope.model.Signal;
import fr.proline.mzscope.ui.event.ScanHeaderListener;
import fr.proline.mzscope.processing.IsotopicPatternUtils;
import fr.proline.mzscope.processing.SpectrumUtils;
import fr.proline.mzscope.ui.dialog.SmoothingParamDialog;
import fr.proline.mzscope.utils.Display;
import fr.proline.studio.WindowManager;
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
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.utils.CyclicColorPalette;
import fr.proline.studio.utils.IconManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.AbstractSpinnerModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
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
   protected Signal currentScanCentroided;
   
   private boolean keepSameMsLevel = true;
   private boolean autoZoom = false;
   private List<AbstractMarker> ipMarkers = new ArrayList();
   private ScansSpinnerModel spinnerModel;
   
   private JButton m_editSignalBtn;
   private JButton m_showCentroidMarkBtn;
   private JButton m_clearMarkersBtn;
   private JButton m_viewCentroidSignal;

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
      PlotPanel plotPanel = new PlotPanel(false);
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
               _displayReferenceSpectrum(currentScan);
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
      
      m_showCentroidMarkBtn = new JButton();
      m_showCentroidMarkBtn.setEnabled(false);
      m_showCentroidMarkBtn.setIcon(IconManager.getIcon(IconManager.IconType.CENTROID_SPECTRA));
      m_showCentroidMarkBtn.setToolTipText("Compute and show centroid peaks");
      m_showCentroidMarkBtn.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            showCentroid();
         }
      });        
      spectrumToolbar.add(m_showCentroidMarkBtn);
      
      m_viewCentroidSignal = new JButton();
      m_viewCentroidSignal.setIcon(IconManager.getIcon(IconManager.IconType.EXPORT_CENTROID));
      m_viewCentroidSignal.setEnabled(false);
      m_viewCentroidSignal.setToolTipText("View computed centroid signal");
      m_viewCentroidSignal.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            viewCentroidSignal();
         }
      });
      
      spectrumToolbar.add(m_viewCentroidSignal);
      

      m_editSignalBtn = new JButton();
      m_editSignalBtn.setEnabled(false);
      m_editSignalBtn.setIcon(IconManager.getIcon(IconManager.IconType.SIGNAL));
      m_editSignalBtn.setToolTipText("Spectrum signal processing dialog");
      m_editSignalBtn.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            editSignal();
         }
      });
      spectrumToolbar.add(m_editSignalBtn);
      
      spectrumToolbar.addSeparator();
      
      m_clearMarkersBtn = new JButton();      
      m_clearMarkersBtn.setIcon(IconManager.getIcon(IconManager.IconType.CLEAR_ALL));
      m_clearMarkersBtn.setToolTipText("Clear markers from signal");
      m_clearMarkersBtn.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            scanPlot.clearMarkers();            
         }
      });
      
      spectrumToolbar.add(m_clearMarkersBtn);
      
      
      spinnerModel = new ScansSpinnerModel(); 
      
      headerSpectrumPanel = new ScanHeaderPanel(null, spinnerModel);
      headerSpectrumPanel.addScanHeaderListener(this);
      spectrumToolbar.add(headerSpectrumPanel);

      return spectrumToolbar;
   }

    
   protected void updateToolbar() {
       m_editSignalBtn.setEnabled(true);
       m_showCentroidMarkBtn.setEnabled(true);
    }
   
    private void showCentroid() {
        
        Signal signal = getSignal();
        scanPlot.clearMarkers(); //clean previous markers
        currentScanCentroided = null;
        m_viewCentroidSignal.setEnabled(false);
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
            
            //Call specified smoother using specieifed nbr points.
            List<Tuple2> input = signal.toScalaArrayTuple(false);
            Tuple2[] param = input.toArray(new Tuple2[input.size()]);
            long step1a = System.currentTimeMillis();  //VDS time calc
            Tuple2[] result = smoother.smoothTimeIntensityPairs(param);
            long step1 = System.currentTimeMillis();  //VDS time calc
            int resultLenght = result.length;
            logger.debug("Smooting: signal length after smoothing = "+resultLenght+" vs before "+input.size()+". TIME: "+(step1-start)+" which "+(step1a-start)+" for arrays");
            double[] x = new double[resultLenght];
            double[] y = new double[resultLenght];
            for (int k = 0; k < resultLenght; k++) {
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
            Spectrum newSpectrum = new Spectrum(-1, currentScan.getRetentionTime() , Arrays.copyOfRange(newSpMasses, 0, realLenght), Arrays.copyOfRange(newSpIntensities, 0, realLenght), currentScan.getMsLevel(), Spectrum.ScanType.PROFILE);
            
//            double[] centroidSignalX = new double[mm.length];
//            double[] centroidSignalY = new double[mm.length];
//            int realLenght = 0;
//            Color markerColor = rawFilePanel.getPlotColor(rawFilePanel.getCurrentRawfile().getName());
//            for (int k = 0; k < mm.length; k++) {
//               if(mm[k].isMaximum()){                   
//                   double massIdx = newSignal.getXSeries()[mm[k].index()];
//                   centroidSignalX[realLenght] = massIdx;
//                   centroidSignalY[realLenght] = signal.getYSeries()[mm[k].index()];
//                   scanPlot.addMarker(new PointMarker(spectrumPlotPanel, new DataCoordinates(massIdx, newSignal.getYSeries()[mm[k].index()]), markerColor));
//                   realLenght++;
//               }
//            }
          
            currentScanCentroided = new Signal(Arrays.copyOfRange(centroidSignalX, 0, realLenght),Arrays.copyOfRange(centroidSignalY,0,realLenght));
            currentScanCentroided.setSignalType(Signal.CENTROID);
            long step4 = System.currentTimeMillis();      
            logger.debug("Create CentroidSignal values + display markers.Nbr real points = "+realLenght+". TIME: "+(step4-step3)+ "TOTAL == "+(step4-start));
            referenceSpectrum = newSpectrum;
            _displayReferenceSpectrum(newSpectrum);
            m_viewCentroidSignal.setEnabled(true);
        }
    }
    
    private void viewCentroidSignal(){
        if(currentScanCentroided==null){
            JOptionPane.showMessageDialog(this, "No centroid signal to show. Run compute centroid peaks first.", "View Centroid Signal Error", JOptionPane.WARNING_MESSAGE);                                       
            return;
        }
        
        List<Signal> signals = new ArrayList<>();
        signals.add(currentScanCentroided);
        JDialog dialog = new JDialog((JFrame)this.getTopLevelAncestor(), "Computed Centroid Signal Editor", true);
        dialog.setContentPane(SignalViewerBuilder.buildEditor(signals, false));
        dialog.setModalityType(Dialog.ModalityType.MODELESS);
        dialog.pack();
        dialog.setVisible(true);
        
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
            if ((e.getModifiers() & OVERLAY_KEY) == 0 && rawFilePanel.getChromatogramDisplayMode() != Display.Mode.OVERLAY) {
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
                    rawFilePanel.extractAndDisplayChromatogram(builder.build(), new Display(rawFilePanel.getChromatogramDisplayMode()), null);
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

      if ((currentScan != null) && (currentScan.getMasses().length > 0)){
         xMin = spectrumPlotPanel.getXAxis().getMinValue();
         xMax = spectrumPlotPanel.getXAxis().getMaxValue();
         yMin = spectrumPlotPanel.getYAxis().getMinValue();
         yMax = spectrumPlotPanel.getYAxis().getMaxValue();
      }

      if (scan != null && scan.getMasses().length > 0) {    
        
         logger.info("display scan id = {}, masses length = {} ", scan.getIndex(), scan.getMasses().length);
         currentScanCentroided = null;
         m_viewCentroidSignal.setEnabled(false);
         Color plotColor = rawFilePanel.getPlotColor(rawFilePanel.getCurrentRawfile().getName());
         ScanTableModel scanModel = new ScanTableModel(scan);
         scanModel.setColor(plotColor);
         scanPlot = buildPlot(scan, plotColor);
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
         _displayReferenceSpectrum(referenceSpectrum);
         spectrumPlotPanel.repaint();
         headerSpectrumPanel.setMzdbFileName(rawFilePanel.getCurrentRawfile().getName());
         currentScan = scan;
         spinnerModel.setValue(currentScan.getIndex());
         headerSpectrumPanel.setScan(currentScan);
      } else if (scan != null) {
        logger.info("display scan id = {},contains no data ", scan.getIndex());
         currentScan = scan;
         spinnerModel.setValue(currentScan.getIndex());
         headerSpectrumPanel.setScan(currentScan);
         spectrumPlotPanel.clearPlotsWithRepaint();
      } else {
        spectrumPlotPanel.clearPlotsWithRepaint();
      }
   }

    private void _displayReferenceSpectrum(Spectrum spectrum) {
        if (spectrum != null) {
            
            double xMin = 0.0, xMax = 0.0, yMin = 0.0, yMax = 0.0;
            
            if (currentScan != null) {
               xMin = spectrumPlotPanel.getXAxis().getMinValue();
               xMax = spectrumPlotPanel.getXAxis().getMaxValue();
               yMin = spectrumPlotPanel.getYAxis().getMinValue();
               yMax = spectrumPlotPanel.getYAxis().getMaxValue();
            }

            PlotXYAbstract plot = buildPlot(spectrum, CyclicColorPalette.getColor(5));                        
            spectrumPlotPanel.addPlot(plot, true);
            
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

    public void displayReferenceSpectrum(Spectrum spectrum) {
      referenceSpectrum = spectrum;
      displayScan(currentScan);
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
