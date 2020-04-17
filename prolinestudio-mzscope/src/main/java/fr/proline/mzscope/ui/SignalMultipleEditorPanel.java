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
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author CB205360
 */
public class SignalMultipleEditorPanel extends JPanel {
   
   final private static Logger logger = LoggerFactory.getLogger(SignalMultipleEditorPanel.class);

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
        basePlot.addPlot(linear); 
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
            double value = SpectrumUtils.correlation(m_signals.get(0).getXSeries(), m_signals.get(0).getYSeries(), m_signals.get(1).getXSeries(),  m_signals.get(1).getYSeries());
            m_plots.get(0).addMarker(new LabelMarker(m_plotPanel.getBasePlotPanel(), new PixelCoordinates(10, 10), "Pearson Corr = "+value));
            
            Pair<double[], double[]> values = SpectrumUtils.zipValues(m_signals.get(0).getXSeries(), m_signals.get(0).getYSeries(), m_signals.get(1).getXSeries(),  m_signals.get(1).getYSeries());
            double cosineCorr = DotProductScorer.dotProduct(values.getLeft(), values.getRight());
            m_plots.get(0).addMarker(new LabelMarker(m_plotPanel.getBasePlotPanel(), new PixelCoordinates(10, 60), "Cosine Corr = "+cosineCorr));
            
            m_plotPanel.getBasePlotPanel().repaintUpdateDoubleBuffer();
            
         }
      });
      toolbar.add(button);
      
      
      add(m_plotPanel, BorderLayout.CENTER);
      add(toolbar, BorderLayout.NORTH);
      setPreferredSize(new Dimension(300,500));
   }
   
}

