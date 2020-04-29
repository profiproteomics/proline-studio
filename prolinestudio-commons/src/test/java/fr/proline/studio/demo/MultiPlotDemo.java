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

import fr.proline.studio.graphics.BasePlotPanel;
import fr.proline.studio.graphics.PlotLinear;
import fr.proline.studio.graphics.PlotPanel;
import fr.proline.studio.graphics.PlotScatter;
import fr.proline.studio.graphics.marker.IntervalMarker;
import fr.proline.studio.graphics.marker.LineMarker;
import fr.proline.studio.graphics.marker.PointMarker;
import fr.proline.studio.graphics.marker.XDeltaMarker;
import fr.proline.studio.graphics.marker.coordinates.DataCoordinates;
import fr.proline.studio.sampledata.Sample;
import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import org.openide.util.Exceptions;

/**
 *
 * @author Karine XUE
 */
public class MultiPlotDemo extends JFrame {
     private PlotPanel plotPanel;
     
     
     public MultiPlotDemo() {
      super("MultiPlot demo");
      plotPanel = new PlotPanel(false);
      final BasePlotPanel basePlot = plotPanel.getBasePlotPanel();
      basePlot.setPlotTitle("MultiPlot title");
      
      Sample sample = new Sample(3000);
      PlotLinear linear = new PlotLinear(basePlot, sample, null, 5, 0);
//      linear.setStrokeFixed(true);
//      linear.setAntiAliasing(true);
//      linear.addMarker(new LineMarker(basePlot, 1000, LineMarker.ORIENTATION_VERTICAL));
//      linear.addMarker(new LineMarker(basePlot, 1250, Color.BLUE));
//      linear.addMarker(new IntervalMarker(basePlot, Color.orange, Color.RED, 1200, 1300));
//      linear.addMarker(new XDeltaMarker(basePlot, 1400, 1500, 3));
//      linear.addMarker(new PointMarker(basePlot, new DataCoordinates(1600, 2.5), Color.ORANGE));

      basePlot.setPlot(linear);
      //basePlot.setMargins(new Insets(10, 0, 10, 0));
      //basePlot.getYAxis().setRange(0.0, 2.0);
      PlotScatter scatter = new PlotScatter(basePlot, sample, null, 5, 0);
      basePlot.addPlot(scatter, true);
      basePlot.setDrawCursor(true);
      basePlot.repaint();
      getContentPane().setLayout(new BorderLayout());
      getContentPane().add(plotPanel, BorderLayout.CENTER);
      setSize(450, 350);
      setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
//      addWindowListener(new WindowAdapter(){
//                public void windowClosing(WindowEvent e){
//                    System.out.println("Mean time per frame= "+basePlot.fps.getMeanTimePerFrame()+" ms");
//                }
//            });
   }

    /**
     * Initialization method that will be called after the applet is loaded into
     * the browser.
     */
    public void init() {
        // TODO start asynchronous download of heavy resources
    }

     /**
    * @param args the command line arguments
    */
   public static void main(String[] args) {

      try {
         UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
         SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
               MultiPlotDemo plot = new MultiPlotDemo();
               plot.setVisible(true);
            }
         });
      } catch (ClassNotFoundException ex) {
         Exceptions.printStackTrace(ex);
      } catch (InstantiationException ex) {
         Exceptions.printStackTrace(ex);
      } catch (IllegalAccessException ex) {
         Exceptions.printStackTrace(ex);
      } catch (UnsupportedLookAndFeelException ex) {
         Exceptions.printStackTrace(ex);
      }
      
   }
}
