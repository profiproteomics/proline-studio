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
import fr.proline.studio.graphics.PlotInformation;
import fr.proline.studio.graphics.PlotLinear;
import fr.proline.studio.sampledata.Sample;
import java.awt.BorderLayout;
import java.awt.Color;
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
public class DoubleAxisPlotPanelTest extends JFrame {

    private BasePlotPanel graphicsPanel;

    public DoubleAxisPlotPanelTest() {
        super("Graphics Panel demo");
        graphicsPanel = new BasePlotPanel();

        graphicsPanel.setPlotTitle("Double Y Axis");

        int[] curveY1 = {5, 4, 3, 2, 3, 5, 9, 11, 10, 8, 7, 6, 3, 1, 2, 2, 2, 4, 5, 6, 7, 10, 11, 12, 10, 9, 8, 7, 6, 5};//red
        int[] curve1Y1 = {10, 8, 7, 6, 3, 1, 2, 3, 2, 3, 12, 10, 9, 8}; //red
        int[] curve2Y1 = {2, 2, 2, 4, 5, 6, 8, 7, 6, 5, 5, 4, 2, 2, 4, 5};//red

        int[] curveY2 = {100, 80, 70, 60, 30, 10, 20, 20, 20, 40, 50, 60, 70, 100, 110, 120, 100, 90, 80, 70, 60, 50, 50, 40, 30, 20, 30, 50, 90, 110};//blue
//         int[] curveY1 = {5, 4, 3, 2, 3};
//        int[] curveY2 = {10, 8, 7, 6, 3 };
        Sample sample1 = new Sample(curveY1, 120, 30);
        Sample sample11 = new Sample(curve1Y1, 120, 30);
        Sample sample12 = new Sample(curve2Y1, 120, 30);
        Sample sample2 = new Sample(curveY2, 120, 30);

        PlotLinear p1 = new PlotLinear(graphicsPanel, sample1, null, 0, 1);
        PlotLinear p11 = new PlotLinear(graphicsPanel, sample11, null, 0, 1);
        PlotLinear p12 = new PlotLinear(graphicsPanel, sample12, null, 0, 1);
        PlotInformation pInfo1 = new PlotInformation();
        pInfo1.setPlotColor(Color.red);
        pInfo1.setPlotTitle("red linear");
        p1.setPlotInformation(pInfo1);
        p11.setPlotInformation(pInfo1);
        p12.setPlotInformation(pInfo1);
        PlotLinear p2 = new PlotLinear(graphicsPanel, sample2, null, 0, 1);
        PlotInformation pInfo2 = new PlotInformation();
        pInfo2.setPlotColor(Color.orange);
        pInfo2.setPlotTitle("blue linear");
        p2.setPlotInformation(pInfo2);
        graphicsPanel.addPlot(p1, true);
        graphicsPanel.addPlot(p11, true);
        graphicsPanel.addPlot(p12, true);
        graphicsPanel.addPlot(p2, false);
        Color color = p2.getPlotInformation().getPlotColor();
        graphicsPanel.setSecondAxisPlotInfo("Protein " , color);

        
        graphicsPanel.setYAxisBounds(0, 20);
        graphicsPanel.setYAxisRightBounds(-10, 200);
        
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

      System.out.println("NaN value = " + Double.NaN);      
      System.out.println("MIN value = " + Double.MIN_VALUE);
      System.out.println("Math.max(NaN, MIN) = " + Math.max(Double.NaN, Double.MIN_VALUE));
      System.out.println("Math.max(MIN, 0) = " + Math.max(Double.MIN_VALUE, 0.0));
      System.out.println("MIN = NAN " + (Double.NaN == Double.MIN_VALUE));
      
      
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    DoubleAxisPlotPanelTest plot = new DoubleAxisPlotPanelTest();
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
