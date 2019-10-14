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

import fr.profi.mzdb.algo.signal.detection.SmartPeakelFinder;
import fr.profi.mzdb.algo.signal.filtering.BaselineRemover;
import fr.profi.mzdb.algo.signal.filtering.ISignalSmoother;
import fr.profi.mzdb.algo.signal.filtering.PartialSavitzkyGolaySmoother;
import fr.profi.mzdb.algo.signal.filtering.SavitzkyGolaySmoother;
import fr.profi.mzdb.algo.signal.filtering.SavitzkyGolaySmoothingConfig;
import fr.profi.mzdb.util.math.DerivativeAnalysis;
import fr.proline.mzscope.model.Signal;
import fr.proline.studio.graphics.BasePlotPanel;
import fr.proline.studio.graphics.PlotLinear;
import fr.proline.studio.graphics.PlotPanel;
import fr.proline.studio.graphics.marker.LineMarker;
import fr.proline.studio.graphics.marker.PointMarker;
import fr.proline.studio.graphics.marker.coordinates.DataCoordinates;
import fr.proline.studio.utils.CyclicColorPalette;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;

/**
 *
 * @author CB205360
 */
public class SignalEditorPanel extends JPanel {
   
   final private static Logger logger = LoggerFactory.getLogger(SignalEditorPanel.class);

   private final PlotPanel m_plotPanel;
   private final PlotLinear m_linear;
   private final Signal m_signal;
   private Map<Signal, PlotLinear> m_smoothedSignals;
   private final JButton minmaxBtn;
   private final JButton baseLineBtn;
    
   public SignalEditorPanel(Signal signal) {
      m_signal = signal;
      m_plotPanel = new PlotPanel();
      m_smoothedSignals = new HashMap<>();
      
      BasePlotPanel basePlot = m_plotPanel.getBasePlotPanel();
      basePlot.setPlotTitle("2d signal");
      basePlot.setDrawCursor(true);
      SignalWrapper wrappedSignal = new SignalWrapper(m_signal, "original signal", CyclicColorPalette.getColor(1));
      m_linear = new PlotLinear(basePlot, wrappedSignal, null, 0, 1);
      m_linear.setPlotInformation(wrappedSignal.getPlotInformation());
      m_linear.setStrokeFixed(true);
      m_linear.setAntiAliasing(true);
      basePlot.setPlot(m_linear);
      setLayout(new BorderLayout());
      JToolBar toolbar = new JToolBar();
      
      JButton smoothBtn = new JButton("Smooth");
      smoothBtn.addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent e) {
            
            List<Tuple2> input = toScalaArrayTuple();
 
            PartialSavitzkyGolaySmoother psgSmoother = new PartialSavitzkyGolaySmoother(new SavitzkyGolaySmoothingConfig(5, 4, 1));
            smooth(input, psgSmoother, "Partial SG");
            //int nbPoints = (input.size() <= 20) ? 5 : (input.size() < 50) ? 7 : 11;
            int nbPoints = Math.min(input.size()/4, 9);
            logger.info("display smoothed signal, SG nb smoothing points = "+nbPoints);
            SavitzkyGolaySmoother sgSmoother = new SavitzkyGolaySmoother(new SavitzkyGolaySmoothingConfig(nbPoints, 2, 1));
            smooth(input, sgSmoother, "SG");
         }
      });
      toolbar.add(smoothBtn);
      
      minmaxBtn = new JButton("Min/Max");
      minmaxBtn.setEnabled(false);
      minmaxBtn.addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent e) {
            logger.info("detect significant min max");
            detectSignificantMinMax();
         }
      });
      
      toolbar.add(minmaxBtn);
      
      baseLineBtn = new JButton("Baseline");
      baseLineBtn.addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent e) {
            
            List<Tuple2> input = toScalaArrayTuple();
            Tuple2[] rtIntPairs = input.toArray(new Tuple2[input.size()]);
            BaselineRemover baselineRemover = new BaselineRemover(1, 3);
            double threshold = baselineRemover.calcNoiseThreshold(rtIntPairs);
            logger.info("detected baseline at threshold "+threshold);
            LineMarker positionMarker = new LineMarker(m_plotPanel.getBasePlotPanel(), threshold, LineMarker.ORIENTATION_HORIZONTAL, Color.BLUE, true);
            m_linear.addMarker(positionMarker);
            SmartPeakelFinder peakelFinder = new SmartPeakelFinder(5, 3, (float)0.66, false, 10, false, false, true);
            Tuple2[] indices = peakelFinder.findPeakelsIndices(rtIntPairs);
         }
      });
      
      toolbar.add(baseLineBtn);
      
      add(m_plotPanel, BorderLayout.CENTER);
      add(toolbar, BorderLayout.NORTH);
      setPreferredSize(new Dimension(300,500));
   }
   
   private void addSmoothedSignal(Signal s, String title) {
      
      minmaxBtn.setEnabled(true);
      BasePlotPanel basePlot = m_plotPanel.getBasePlotPanel();
      SignalWrapper wrappedSignal = new SignalWrapper(s, "smoothed signal : "+title, CyclicColorPalette.getColor((m_smoothedSignals.size()+1)*2));
      PlotLinear linear = new PlotLinear(basePlot, wrappedSignal, null, 0, 1);
      linear.setPlotInformation(wrappedSignal.getPlotInformation());
      linear.setStrokeFixed(true);
      linear.setAntiAliasing(true);
      basePlot.addPlot(linear);
      basePlot.repaintUpdateDoubleBuffer();
      m_smoothedSignals.put(s, linear);
   }
   
   private void detectSignificantMinMax() {
      for (Map.Entry<Signal, PlotLinear> e : m_smoothedSignals.entrySet()) {
         Signal s = e.getKey();
         DerivativeAnalysis.ILocalDerivativeChange[] mm = DerivativeAnalysis.findSignificantMiniMaxi(s.getYSeries(),3,0.75f);
         for (int k = 0; k < mm.length; k++) {
            PlotLinear plot = e.getValue();
            plot.addMarker(new PointMarker(m_plotPanel.getBasePlotPanel(), new DataCoordinates(s.getXSeries()[mm[k].index()], s.getYSeries()[mm[k].index()]), plot.getPlotInformation().getPlotColor()));
         }
      }
      m_plotPanel.getBasePlotPanel().repaintUpdateDoubleBuffer();
   }
   
   private void smooth(List<Tuple2> rtIntPairs, ISignalSmoother smoother, String title) {
      logger.info("signal length before smoothing = "+rtIntPairs.size());
      
      Tuple2[] result = smoother.smoothTimeIntensityPairs(rtIntPairs.toArray(new Tuple2[rtIntPairs.size()]));
      logger.info("signal length after smoothing = "+result.length);
      double[] x = new double[result.length];
      double[] y = new double[result.length];
      for (int k = 0; k < result.length; k++) {
         x[k] = (Double)result[k]._1;
         y[k] = (Double)result[k]._2;
      }
      Signal s = new Signal(x,y);
      addSmoothedSignal(s, title);
   }

   private List<Tuple2> toScalaArrayTuple() {
      List<Tuple2> rtIntPairs = new ArrayList<Tuple2>();
      for (int k = 0; k < m_signal.getXSeries().length; k++) {
         // peakel detection did not include peaks with intensity = 0, this seems also mandatory since PartialSG smoother
         // get strange behavior with 0 intensities
         if (m_signal.getYSeries()[k] > 0.0)
            rtIntPairs.add(new Tuple2(m_signal.getXSeries()[k], m_signal.getYSeries()[k]));
      }
      return rtIntPairs;
   }
}

