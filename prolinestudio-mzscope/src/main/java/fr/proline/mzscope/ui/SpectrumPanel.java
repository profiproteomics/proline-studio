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
import fr.profi.mzdb.algo.signal.filtering.ISignalSmoother;
import fr.profi.mzdb.algo.signal.filtering.PartialSavitzkyGolaySmoother;
import fr.profi.mzdb.algo.signal.filtering.SavitzkyGolaySmoother;
import fr.profi.mzdb.algo.signal.filtering.SavitzkyGolaySmoothingConfig;
import fr.profi.mzdb.model.SpectrumData;
import fr.profi.mzdb.util.math.DerivativeAnalysis;
import fr.proline.mzscope.model.MsnExtractionRequest;
import fr.proline.mzscope.model.Signal;
import fr.proline.mzscope.model.Spectrum;
import fr.proline.mzscope.processing.IsotopicPatternUtils;
import fr.proline.mzscope.processing.SpectrumUtils;
import fr.proline.mzscope.ui.dialog.SmoothingParamDialog;
import fr.proline.mzscope.ui.event.ScanHeaderListener;
import fr.proline.mzscope.ui.model.MzScopePreferences;
import fr.proline.mzscope.utils.Display;
import fr.proline.studio.WindowManager;
import fr.proline.studio.export.ExportButton;
import fr.proline.studio.graphics.PlotPanelListener;
import fr.proline.studio.graphics.marker.IntervalMarker;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.utils.IconManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
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
   private boolean autoZoom = false;

   private ScansSpinnerModel spinnerModel;
   
   private JButton m_editSignalBtn;
   private JButton m_showCentroidBtn;
   private JToggleButton m_freezeSpectrumBtn;

   private static final DecimalFormat df = new DecimalFormat("#.###");

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
    super(rawFilePanel);
   }

   protected JToolBar getSpectrumToolbar() {
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
           displayIsotopicPrediction(positionMarker.getValue());
         }
      });
      
      spectrumToolbar.add(displayIPBtn);

      m_freezeSpectrumBtn = new JToggleButton(IconManager.getIcon(IconManager.IconType.PIN));
      m_freezeSpectrumBtn.addActionListener(new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
              JToggleButton tBtn = (JToggleButton)e.getSource();
            if (tBtn.isSelected()) {
              setReferenceSpectrum(currentScan, 1.0f);
            } else {
               clearReferenceSpectrumData();
            }
          }
      });
      
      spectrumToolbar.add(m_freezeSpectrumBtn);
      
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
      
      m_showCentroidBtn = new JButton();
      m_showCentroidBtn.setEnabled(false);
      m_showCentroidBtn.setIcon(IconManager.getIcon(IconManager.IconType.CENTROID_SPECTRA));
      m_showCentroidBtn.setToolTipText("Compute and show centroid peaks");
      m_showCentroidBtn.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            showCentroid();
         }
      });        
      spectrumToolbar.add(m_showCentroidBtn);

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

      spinnerModel = new ScansSpinnerModel(); 
      
      headerSpectrumPanel = new ScanHeaderPanel(null, spinnerModel);
      headerSpectrumPanel.addScanHeaderListener(this);
      spectrumToolbar.add(headerSpectrumPanel);

     spectrumToolbar.addSeparator();
     JButton testBtn = new JButton("MS2");
     testBtn.setToolTipText("process MS2 Spectrum");
     testBtn.addActionListener(new ActionListener() {
       @Override
       public void actionPerformed(ActionEvent e) {
         processMS2Spectrum();
       }
     });
     spectrumToolbar.add(testBtn);

     return spectrumToolbar;
   }

  private void processMS2Spectrum() {
    if (currentScan.getDataType() == Spectrum.ScanType.CENTROID) {
      double tolPpm = 20.0;
      double[] masses = currentScan.getMasses();
      float[] intensities = currentScan.getIntensities();
      final SpectrumData spectrumData = currentScan.getSpectrumData();
      List<Peak> peaks = new ArrayList<>(spectrumData.getPeaksCount());
      List<Peak> result = new ArrayList<>(spectrumData.getPeaksCount());
      Map<Integer, Peak> peaksByIndex = new HashMap<>();
      for (int k = 0; k < masses.length; k++) {
        Peak p = new Peak(masses[k], intensities[k], k);
        peaks.add(p);
        peaksByIndex.put(p.index, p);
      }

      peaks.sort((o1, o2) -> Float.compare(o2.intensity, o1.intensity));

      for (int k = 0; k < peaks.size(); k++) {
        Peak p = peaks.get(k);
        if (!p.used) {
          Tuple2<Object, TheoreticalIsotopePattern> prediction = IsotopicPatternUtils.predictIsotopicPattern(spectrumData, p.mass, tolPpm);
          if ( (1e6*(prediction._2.monoMz() - p.mass)/p.mass) <= tolPpm ) {
            float intensity = 0;
            int charge = prediction._2.charge();
            for (Tuple2 t : prediction._2.mzAbundancePairs()) {
              Double mz = (Double) t._1;
              Float ab = (Float) t._2;
              int peakIdx = SpectrumUtils.getPeakIndex(spectrumData.getMzList(), mz, tolPpm);
              if ((peakIdx != -1) && (spectrumData.getIntensityList()[peakIdx] <= p.intensity)) {
                  intensity+= spectrumData.getIntensityList()[peakIdx];
                  peaksByIndex.get(peakIdx).used = true;
              } else {
                break;
              }
            }
            if ( (charge == 1) || (intensity == p.intensity) ) {
              Peak newPeak = new Peak(p.mass, intensity, p.index);
              result.add(newPeak);
            } else {
              Peak newPeak = new Peak(p.mass*charge - (charge-1)*1.00728, intensity, p.index);
              logger.info("Move peak ({},{}) to ({},{})", p.mass, p.intensity, newPeak.mass, newPeak.intensity);
              result.add(newPeak);
            }
          } else {
            p.used = true;
            result.add(new Peak(p));
          }
        }
      }

      result.sort(Comparator.comparingDouble(o -> o.mass));
      masses = new double[result.size()];
      intensities = new float[result.size()];
      int k = 0;
      for (Peak p : result) {
        masses[k] = p.mass;
        intensities[k++] = p.intensity;
      }
      Spectrum newSpectrum = new Spectrum(-1, currentScan.getRetentionTime() , masses, intensities, currentScan.getMsLevel(), Spectrum.ScanType.CENTROID);
      setReferenceSpectrum(newSpectrum, -1.0f);
    }
  }

  class Peak {

    final double mass;
    final float intensity;
    final int index;
    boolean used = false;

    public Peak(Peak peak) {
      this(peak.mass, peak.intensity, peak.index);
    }

    public Peak(double mass, float intensity, int index) {
      this.mass = mass;
      this.intensity = intensity;
      this.index = index;
    }
  }

  protected void updateToolbar() {
       m_editSignalBtn.setEnabled(true);
       m_showCentroidBtn.setEnabled(true);
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
    public void plotPanelMouseClicked(MouseEvent e, double xValue, double yValue) {
        if (e.getClickCount() == 2) {
            if ((e.getModifiersEx() & OVERLAY_KEY) == 0 && rawFilePanel.getChromatogramDisplayMode() != Display.Mode.OVERLAY) {
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
                if ((e.getModifiersEx() & OVERLAY_KEY) != 0) {
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
      super.displayScan(scan);

      if (scan != null && scan.getMasses().length > 0) {
         headerSpectrumPanel.setMzdbFileName(rawFilePanel.getCurrentRawfile().getName());
         spinnerModel.setValue(currentScan.getIndex());
         headerSpectrumPanel.setScan(currentScan);
      } else if (scan != null) {
        spinnerModel.setValue(currentScan.getIndex());
        headerSpectrumPanel.setScan(currentScan);
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


}
