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
