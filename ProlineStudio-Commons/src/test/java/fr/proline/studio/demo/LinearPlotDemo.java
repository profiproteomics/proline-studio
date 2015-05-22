/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.demo;

import fr.proline.studio.graphics.PlotLinear;
import fr.proline.studio.graphics.BasePlotPanel;
import fr.proline.studio.graphics.PlotPanel;
import fr.proline.studio.graphics.marker.IntervalMarker;
import fr.proline.studio.graphics.marker.LineMarker;
import fr.proline.studio.graphics.marker.PointMarker;
import fr.proline.studio.graphics.marker.XDeltaMarker;
import fr.proline.studio.sampledata.Sample;
import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

/**
 *
 * @author MB243701
 */
public class LinearPlotDemo extends JFrame {
    private PlotPanel plotPanel;
    
    public LinearPlotDemo() {
      super("LinearPlot demo");
      plotPanel = new PlotPanel();
      BasePlotPanel basePlot = plotPanel.getBasePlotPanel();
      basePlot.setPlotTitle("graph title");
      basePlot.setDrawCursor(true);
      Sample sample = new Sample(3000);
      PlotLinear linear = new PlotLinear(basePlot, sample, null, 5, 0);
      linear.setStrokeFixed(true);
      linear.setAntiAliasing(true);
      linear.addMarker(new LineMarker(basePlot, 1000, LineMarker.ORIENTATION_VERTICAL));
      linear.addMarker(new LineMarker(basePlot, 1250, Color.BLUE));
      linear.addMarker(new IntervalMarker(basePlot, Color.orange, Color.RED, 1200, 1300));
      linear.addMarker(new XDeltaMarker(basePlot, 1400, 1500, 3));
      linear.addMarker(new PointMarker(basePlot, 1600, 2.5, Color.ORANGE));
      
      basePlot.setPlot(linear);
      basePlot.getYAxis().setRange(0.0, 2.0);
      basePlot.repaint();
      getContentPane().setLayout(new BorderLayout());
      getContentPane().add(plotPanel, BorderLayout.CENTER);
      setSize(450,350);
      setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
   }
    
    /**
    * @param args the command line arguments
    */
   public static void main(String[] args) {
      SwingUtilities.invokeLater(new Runnable() {

         @Override
         public void run() {
            LinearPlotDemo plot = new LinearPlotDemo();
            plot.setVisible(true);
         }
      });
   }
}
