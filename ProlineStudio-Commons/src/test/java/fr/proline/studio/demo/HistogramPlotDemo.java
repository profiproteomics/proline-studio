/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.demo;

import fr.proline.studio.graphics.PlotHistogram;
import fr.proline.studio.graphics.BasePlotPanel;
import fr.proline.studio.sampledata.Sample;
import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

/**
 *
 * @author MB243701
 */
public class HistogramPlotDemo extends JFrame {
    private BasePlotPanel plotPanel;
   
   public HistogramPlotDemo() {
      super("HistogramPlot demo");
      plotPanel = new BasePlotPanel();
      Sample sample = new Sample(3000);
      PlotHistogram histo = new PlotHistogram(plotPanel, sample, null, 0, "test");
      plotPanel.setPlot(histo);
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
            HistogramPlotDemo plot = new HistogramPlotDemo();
            plot.setVisible(true);
         }
      });
   }
}
