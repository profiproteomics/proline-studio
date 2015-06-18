package fr.proline.studio.demo;

import fr.proline.studio.graphics.BasePlotPanel;
import fr.proline.studio.graphics.PlotPanel;
import fr.proline.studio.graphics.PlotScatter;
import fr.proline.studio.sampledata.Sample;
import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

/**
 *
 * @author CB205360
 */
public class ScatterPlotDemo extends JFrame { 

   private PlotPanel plotPanel;
   
   public ScatterPlotDemo() {
      super("ScatterPlot demo");
      plotPanel = new PlotPanel();
      Sample sample = new Sample(3000);
      BasePlotPanel basePlot = plotPanel.getBasePlotPanel();
      PlotScatter scatter = new PlotScatter(basePlot, sample, null, 0, 3);
      basePlot.setPlot(scatter);
      basePlot.setDrawCursor(true);
      basePlot.getXAxis().setRange(-4, 4);
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
            ScatterPlotDemo plot = new ScatterPlotDemo();
            plot.setVisible(true);
         }
      });
   }
   
}
