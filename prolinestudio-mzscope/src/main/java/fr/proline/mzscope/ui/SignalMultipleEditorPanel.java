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

import fr.profi.mzdb.algo.signal.filtering.SavitzkyGolaySmoother;
import fr.profi.mzdb.algo.signal.filtering.SavitzkyGolaySmoothingConfig;
import fr.proline.mzscope.model.Signal;
import fr.proline.mzscope.processing.DotProductScorer;
import fr.proline.mzscope.processing.SpectrumUtils;
import fr.proline.studio.graphics.BasePlotPanel;
import fr.proline.studio.graphics.PlotLinear;
import fr.proline.studio.graphics.PlotPanel;
import fr.proline.studio.graphics.marker.LabelMarker;
import fr.proline.studio.graphics.marker.coordinates.PixelCoordinates;
import fr.proline.studio.utils.CyclicColorPalette;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;

/**
 *
 * @author CB205360
 */
public class SignalMultipleEditorPanel extends JPanel {
   
   final private static Logger logger = LoggerFactory.getLogger(SignalMultipleEditorPanel.class);
   final private static DecimalFormat FORMATTER = new DecimalFormat("#.0000");
   private final PlotPanel m_plotPanel;
   private final List<PlotLinear> m_plots;
   private final List<Signal> m_signals;
    
   public SignalMultipleEditorPanel(List<Signal> signals) {
      m_signals = signals;
      m_plotPanel = new PlotPanel(false);
      m_plots = new ArrayList<>();
      BasePlotPanel basePlot = m_plotPanel.getBasePlotPanel();
      basePlot.setPlotTitle("2d signal");
      basePlot.setDrawCursor(true);
      for (Signal s : m_signals) {
        SignalWrapper wrappedSignal = new SignalWrapper(s, "original signal", CyclicColorPalette.getColor(1));
        PlotLinear linear = new PlotLinear(basePlot, wrappedSignal, null, 0, 1);
        linear.setPlotInformation(wrappedSignal.getPlotInformation());
        linear.setStrokeFixed(true);
        linear.setAntiAliasing(true);
        basePlot.addPlot(linear, true); 
        m_plots.add(linear);
      }
      
      setLayout(new BorderLayout());
      JToolBar toolbar = new JToolBar();
      
      JButton button = new JButton("Normalize");
      button.addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent e) {
            
         }
      });
      toolbar.add(button);
      button = new JButton("Correlation");
      button.addActionListener(new ActionListener() {

         @Override
         public void actionPerformed(ActionEvent e) {
           
           List<Tuple2> input0 = m_signals.get(0).toScalaArrayTuple(false);
           int nbrPoints =  Math.min(input0.size()/4, 9);
           SavitzkyGolaySmoother smoother = new SavitzkyGolaySmoother(new SavitzkyGolaySmoothingConfig(nbrPoints, 2, 1));
            
           Tuple2[] result0 = smoother.smoothTimeIntensityPairs(input0.toArray(new Tuple2[input0.size()]));
           double[] y0 = new double[result0.length];
           double[] x0 = new double[result0.length];
           for (int k = 0; k < result0.length; k++) {
             x0[k] = (Double)result0[k]._1;
             y0[k] = (Double)result0[k]._2;
           }
            Signal s = new Signal(x0,y0);
          addSmoothedSignal(s, "S0 smoothed");
      
           List<Tuple2> input1 = m_signals.get(1).toScalaArrayTuple(false);
           nbrPoints =  Math.min(input1.size()/4, 9);
           smoother = new SavitzkyGolaySmoother(new SavitzkyGolaySmoothingConfig(nbrPoints, 2, 1));
           
           Tuple2[] result1 = smoother.smoothTimeIntensityPairs(input1.toArray(new Tuple2[input1.size()]));
           double[] y1 = new double[result1.length];
           double[] x1 = new double[result1.length];
           for (int k = 0; k < result1.length; k++) {
             x1[k] = (Double)result1[k]._1;
             y1[k] = (Double)result1[k]._2;
           }
            s = new Signal(x1,y1);
           addSmoothedSignal(s, "S1 smoothed");
           
           
            double smoothedValue = SpectrumUtils.correlation(x0, y0, x1,  y1);
            double value = SpectrumUtils.correlation(m_signals.get(0).getXSeries(), m_signals.get(0).getYSeries(), m_signals.get(1).getXSeries(),  m_signals.get(1).getYSeries());
            m_plots.get(0).addMarker(new LabelMarker(m_plotPanel.getBasePlotPanel(), new PixelCoordinates(10, 10), MessageFormat.format("Pearson Corr = {0} ({1} smoothed)",value, smoothedValue)));
            
            Pair<double[], double[]> values = SpectrumUtils.zipValues(x0, y0, x1,  y1);
            double smoothedCosineCorr = DotProductScorer.dotProduct(values.getLeft(), values.getRight());

            values = SpectrumUtils.zipValues(m_signals.get(0).getXSeries(), m_signals.get(0).getYSeries(), m_signals.get(1).getXSeries(),  m_signals.get(1).getYSeries());
            double cosineCorr = DotProductScorer.dotProduct(values.getLeft(), values.getRight());
            
            m_plots.get(0).addMarker(new LabelMarker(m_plotPanel.getBasePlotPanel(), new PixelCoordinates(10, 60), MessageFormat.format("Cosine Corr = {0} ({1} smoothed)", cosineCorr, smoothedCosineCorr)));
            
            m_plotPanel.getBasePlotPanel().repaintUpdateDoubleBuffer();
            
         }
      });
      toolbar.add(button);
      
      
      add(m_plotPanel, BorderLayout.CENTER);
      add(toolbar, BorderLayout.NORTH);
      setPreferredSize(new Dimension(300,500));
   }
   
   private void addSmoothedSignal(Signal s, String title) {
      
      BasePlotPanel basePlot = m_plotPanel.getBasePlotPanel();
      SignalWrapper wrappedSignal = new SignalWrapper(s, "smoothed signal : "+title, CyclicColorPalette.getColor((basePlot.getPlots().size()+1)*2));
      PlotLinear linear = new PlotLinear(basePlot, wrappedSignal, null, 0, 1);
      linear.setPlotInformation(wrappedSignal.getPlotInformation());
      linear.setStrokeFixed(true);
      linear.setAntiAliasing(true);
      basePlot.addPlot(linear, true);
      basePlot.repaintUpdateDoubleBuffer();
   }

}

