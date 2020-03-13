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

import fr.proline.studio.graphics.MonoGraphicsPanel;
import fr.proline.studio.sampledata.Sample;
import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import org.openide.util.Exceptions;

/**
 *
 * @author CB205360
 */
public class GraphicsPanelDemo extends JFrame {

   private MonoGraphicsPanel graphicsPanel;

   public GraphicsPanelDemo() {
      super("Graphics Panel demo");
      graphicsPanel = new MonoGraphicsPanel(true);
      Sample sample = new Sample(3000);
      graphicsPanel.setData(sample, null);
      getContentPane().setLayout(new BorderLayout());
      getContentPane().add(graphicsPanel, BorderLayout.CENTER);
      pack();
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
               GraphicsPanelDemo plot = new GraphicsPanelDemo();
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
