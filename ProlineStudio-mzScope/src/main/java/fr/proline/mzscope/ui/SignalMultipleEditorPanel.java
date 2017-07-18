package fr.proline.mzscope.ui;

import fr.proline.mzscope.model.Signal;
import fr.proline.mzscope.utils.SpectrumUtils;
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
import java.util.Arrays;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
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
      m_plotPanel = new PlotPanel();
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
            m_plotPanel.getBasePlotPanel().repaintUpdateDoubleBuffer();
         }
      });
      toolbar.add(button);
      
      
      add(m_plotPanel, BorderLayout.CENTER);
      add(toolbar, BorderLayout.NORTH);
      setPreferredSize(new Dimension(300,500));
   }
   
}

