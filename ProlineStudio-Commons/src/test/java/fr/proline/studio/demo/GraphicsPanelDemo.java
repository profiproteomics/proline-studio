/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.demo;

import fr.proline.studio.graphics.BaseGraphicsPanel;
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
public class GraphicsPanelDemo extends JFrame { 

   private BaseGraphicsPanel graphicsPanel;
   
   public GraphicsPanelDemo() {
      super("Graphics Panel demo");
      graphicsPanel = new BaseGraphicsPanel(true);
      Sample sample = new Sample(3000);
      graphicsPanel.setData(sample, null);
      getContentPane().setLayout(new BorderLayout());
      getContentPane().add(graphicsPanel, BorderLayout.CENTER);
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
            GraphicsPanelDemo plot = new GraphicsPanelDemo();
            plot.setVisible(true);
         }
      });
   }
   
}
