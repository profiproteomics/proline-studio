/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.mzscope.ui;

import fr.proline.mzscope.model.Signal;
import fr.proline.mzscope.ui.model.ScanTableModel;
import fr.proline.studio.graphics.BasePlotPanel;
import fr.proline.studio.graphics.PlotLinear;
import fr.proline.studio.graphics.PlotPanel;
import fr.proline.studio.graphics.PlotStick;
import fr.proline.studio.graphics.PlotXYAbstract;
import fr.proline.studio.utils.CyclicColorPalette;
import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author VD225637
 */
public class SignalPanel extends JPanel {

    final private static Logger logger = LoggerFactory.getLogger(SignalPanel.class);

    protected final PlotPanel m_plotPanel;
    protected final PlotXYAbstract m_linear;
    protected final Signal m_signal;

    public SignalPanel(Signal signal) {
      m_signal = signal;
      m_plotPanel = new PlotPanel();
      
      BasePlotPanel basePlot = m_plotPanel.getBasePlotPanel();
      basePlot.setPlotTitle("2d signal");
      basePlot.setDrawCursor(true);
      SignalWrapper wrappedSignal = new SignalWrapper(m_signal, "original signal", CyclicColorPalette.getColor(1));
      if(signal.getSignalType() == Signal.PROFILE){
        m_linear = new PlotLinear(basePlot, wrappedSignal, null, 0, 1);
        ((PlotLinear)m_linear).setPlotInformation(wrappedSignal.getPlotInformation());
        ((PlotLinear)m_linear).setStrokeFixed(true);
        ((PlotLinear)m_linear).setAntiAliasing(true);
      } else {
        m_linear = new PlotStick(basePlot, wrappedSignal, null, ScanTableModel.COLTYPE_SCAN_MASS, ScanTableModel.COLTYPE_SCAN_INTENSITIES);
        ((PlotStick) m_linear).setStrokeFixed(true);
        ((PlotStick) m_linear).setPlotInformation(wrappedSignal.getPlotInformation());
      }
      basePlot.setPlot(m_linear);
      setLayout(new BorderLayout());
      JToolBar toolbar = createToolBar();      
      
      add(m_plotPanel, BorderLayout.CENTER);
      add(toolbar, BorderLayout.NORTH);
      setPreferredSize(new Dimension(300,500));
    }
    
    protected JToolBar createToolBar(){
         return new JToolBar();
    }
}
