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
import fr.proline.studio.graphics.PlotPanel;
import fr.proline.studio.graphics.PlotVennDiagram;
import fr.proline.studio.parameter.SettingsButton;
import fr.proline.studio.sampledata.Sample;
import fr.proline.studio.sampledata.SampleTableModel;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import org.openide.util.Exceptions;

/**
 *
 * @author MB243701
 */
public class VennPlotDemo extends JFrame {

   private PlotPanel plotPanel;

   public VennPlotDemo() {
      super("LinearPlot demo");
      plotPanel = new PlotPanel(false);
      final BasePlotPanel basePlot = plotPanel.getBasePlotPanel();
      basePlot.setPlotTitle("graph title");
      basePlot.setDrawCursor(true);
      
//      Object[] a = { 1.0, Double.NaN, Double.NaN, Double.NaN };
//      Object[] b = { Double.NaN, 1.0, Double.NaN, Double.NaN };
//      Object[] c = { Double.NaN, Double.NaN, 1.0, Double.NaN };
//      Object[] d = { Double.NaN, Double.NaN, Double.NaN, 1.0 };
//      
//      Object[] ab = { 1.0, 1.0 , Double.NaN, Double.NaN };
//      Object[] ac = { 1.0, Double.NaN, 1.0, Double.NaN };
//      Object[] ad = { 1.0, Double.NaN, Double.NaN, 1.0 };
//      
//      Object[] bc = { Double.NaN, 1.0, 1.0, Double.NaN };
//      Object[] abc = { 1.0, 1.0, 1.0, Double.NaN };
//      
//      List<Object[]> values = new ArrayList<>();
//      values.add( a );
//      values.add( a );
//      values.add( a );
//      values.add( a );
//
//      values.add( b );
//      values.add( b );
//      values.add( b );
//
//      values.add( c );
//      values.add( c );
//      values.add( c );
//
//      values.add( d );      
//      
//      values.add( ab );
//      values.add( ab );
//      values.add( ab );
//
//      
//      values.add( ac );
//      values.add( ac );
//
//      values.add( abc );
      
//      values.add( bc );


//      Object[] a = { "AAA", "", ""};
//      Object[] b = { null, "xxx", ""};
//      Object[] c = { null, "", "trtr"};
//      Object[] ab = { "AAA", "ggg", ""};
//      Object[] ac = { "AAA", "", "CCC"};
//      
//      List<Object[]> values = new ArrayList<>();
//      values.add(a);
//      values.add(a);
//      values.add(a);
//
//      values.add(b);
//      values.add(b);
//      values.add(b);
//      
//      values.add(ab);
//      values.add(ab);
//      
//      values.add(ac);
//      values.add(c);

      Object[] a = { Integer.valueOf(1), null};
      Object[] b = { null, Integer.valueOf(1)};
      Object[] ab = { Integer.valueOf(1), Integer.valueOf(1)};
      
      List<Object[]> values = new ArrayList<>();

      values.add(ab);
      values.add(ab);

      values.add(a);
      values.add(a);
      values.add(a);

      values.add(b);
      values.add(b);
      values.add(b);
      
      
      String[] col = { "A", "B" };
      SampleTableModel model = new SampleTableModel(values, col);
      Sample sample = new Sample(model);      
      int[] cols = { 0, 1 };
      PlotVennDiagram venn = new PlotVennDiagram(basePlot, sample, null, cols);

      basePlot.setPlot(venn);
      basePlot.setXAxisBounds(0.0,5000.0);
      
      basePlot.repaint();
      getContentPane().setLayout(new BorderLayout());
      getContentPane().add(plotPanel, BorderLayout.CENTER);
      
      
      SettingsButton settingsButton = new SettingsButton(null, basePlot);
       JToolBar toolBar = new JToolBar();
       toolBar.add(settingsButton);
      getContentPane().add(toolBar, BorderLayout.NORTH);
      
      setSize(450, 350);
      setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

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
               VennPlotDemo plot = new VennPlotDemo();
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
