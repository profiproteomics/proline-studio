/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.demo;

import fr.proline.studio.graphics.PlotLinear;
import fr.proline.studio.graphics.PlotPanel;
import fr.proline.studio.sampledata.Sample;
import java.awt.BorderLayout;
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
      Sample sample = new Sample(3000);
      PlotLinear linear = new PlotLinear(plotPanel, sample, null, 5, 0);
      linear.setStrokeFixed(true);
      linear.setAntiAliasing(true);
      plotPanel.setPlot(linear);
      getContentPane().setLayout(new BorderLayout());
      getContentPane().add(plotPanel, BorderLayout.CENTER);
      pack();
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
