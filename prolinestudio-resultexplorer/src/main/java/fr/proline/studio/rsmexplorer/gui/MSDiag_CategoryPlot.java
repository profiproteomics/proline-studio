/* 
 * Copyright (C) 2019
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
package fr.proline.studio.rsmexplorer.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.IOException;

import javax.swing.JToolBar;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.DefaultCategoryDataset;
import org.slf4j.LoggerFactory;


import fr.proline.studio.export.ExportButton;
import fr.proline.studio.gui.HourglassPanel;
import org.jfree.graphics2d.svg.SVGGraphics2D;
import org.jfree.graphics2d.svg.SVGUtils;

import org.slf4j.Logger;

/**
 * Panel used to display MSDiag Chromatogram
 *
 * @author AW
 */
public class MSDiag_CategoryPlot extends HourglassPanel {

    public static final String SERIES_NAME = "Chromatogram";

    protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");
    private static final long serialVersionUID = 1L;
    private final DefaultCategoryDataset m_dataSet;
    private JFreeChart m_chart;
    private File m_pngFile;
    private javax.swing.JPanel m_chromatogragmPanel;

    private final RsetMSDiagPanel m_msdiagPanel;


    /**
     * Creates new form MSDiag_PieChart
     */
    public MSDiag_CategoryPlot(RsetMSDiagPanel rsetMSDiagPanel) {

        m_msdiagPanel = rsetMSDiagPanel;
        m_dataSet = new DefaultCategoryDataset();

        NumberAxis numberaxis = new NumberAxis("X");
        numberaxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
	        // NumberAxis numberaxis1 = new NumberAxis("Y");   

        m_chart = ChartFactory.createStackedBarChart(
                "", // chart title
                "", // domain axis label
                "", // range axis label
                m_dataSet, // data
                PlotOrientation.VERTICAL, // orientation
                true, // include legend
                true, // tooltips?
                false // URLs?
        );

        m_chart.setBackgroundPaint(Color.white);
        CategoryPlot plot = m_chart.getCategoryPlot();

        m_chart = new JFreeChart(
                "Combined Chart title",
                new Font("SansSerif", Font.BOLD, 12),
                plot,
                true
        );

        StackedBarRenderer renderer = (StackedBarRenderer) plot.getRenderer();
        renderer.setShadowVisible(true);
        //renderer.setDrawBarOutline();
        renderer.setSeriesPaint(0, new Color(220, 220, 220/*254,60,60*/));
        renderer.setSeriesPaint(1, new Color(176, 190, 255));
        renderer.setSeriesPaint(2, new Color(132, 153, 255));
        renderer.setSeriesPaint(3, new Color(91, 120, 255));
        renderer.setSeriesPaint(4, new Color(64, 98, 255));
        renderer.setSeriesPaint(5, new Color(32, 71, 255));
        renderer.setSeriesPaint(6, new Color(1, 191, 220));
        renderer.setSeriesPaint(7, new Color(126, 84, 214));

        renderer.setDrawBarOutline(false);
	       // plot.setDomainGridlinePaint(Color.white);
        //plot.setDomainGridlinesVisible(true);
        //plot.setRangeGridlinePaint(Color.white);

        TextTitle textTitle = m_chart.getTitle();
        textTitle.setFont(textTitle.getFont().deriveFont(Font.PLAIN, 10.0f));
        m_chart.getCategoryPlot().getDomainAxis().setTickLabelFont(new Font("SansSerif", Font.BOLD, 10));
        m_chart.getCategoryPlot().getRangeAxis().setTickLabelFont(new Font("SansSerif", Font.BOLD, 10));
        initComponents();

    }

    private void initComponents() {
        setLayout(new BorderLayout());
        ChartPanel cp = new ChartPanel(m_chart, true);

        cp.setMinimumDrawWidth(0);
        cp.setMinimumDrawHeight(0);
        cp.setMaximumDrawWidth(Integer.MAX_VALUE); // make the legend to have a fixed size and not strecht it 
        cp.setMaximumDrawHeight(Integer.MAX_VALUE); // when the windows becomes bigger.
        m_chart.getPlot().setBackgroundPaint(Color.white);

        m_chromatogragmPanel = cp;

        JToolBar toolbar = initToolbar();

        add(toolbar, BorderLayout.WEST);
        add(m_chromatogragmPanel, BorderLayout.CENTER);

    }

    private JToolBar initToolbar() {

        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.setFloatable(false);
        FlipButton flipModeButton = new FlipButton("flip button text", m_msdiagPanel);
        toolbar.add(flipModeButton);
        ExportButton exportImageButton = new ExportButton("chart", m_chromatogragmPanel);
        toolbar.add(exportImageButton);

        return toolbar;

    }

    public void setData(MSDiagOutput_AW msdo) {

        constructChromatogram(msdo);

    }

    public void writeToPNG(String fileName) {
        m_pngFile = new File(fileName);
        try {
            ChartUtils.saveChartAsPNG(m_pngFile, m_chart, m_chromatogragmPanel.getWidth(), m_chromatogragmPanel.getHeight());
        } catch (IOException e) {
            LoggerFactory.getLogger("ProlineStudio.ResultExplorer").error("writeToPNG", e);
        }
    }

    public void writeToSVG(String file) {
        SVGGraphics2D g2 = new SVGGraphics2D(m_chromatogragmPanel.getWidth(), m_chromatogragmPanel.getHeight());
        m_chromatogragmPanel.paint(g2);

        try {
            SVGUtils.writeToSVG(new File(file), g2.getSVGElement());
        } catch (Exception ex) {
        }
    }

    private void constructChromatogram(MSDiagOutput_AW msdo) {

        // clear all data
        m_dataSet.clear();

        if (msdo == null) {
            return;
        }

        // Set title
        String title = msdo.description;
        m_chart.setTitle(title);

	        // set axes labels
        //m_subplot.getRangeAxis().setLabel(msdo.y_axis_description);
        // m_chart.getCategoryPlot().getDomainAxis().setLabel(msdo.x_axis_description);
        // m_chart.getCategoryPlot().getRangeAxis().setLabel(msdo.y_axis_description);
        m_chart.getPlot().setBackgroundPaint(Color.white);
        m_chart.getCategoryPlot().getDomainAxis().setLabel(msdo.x_axis_description);
        m_chart.getCategoryPlot().getDomainAxis().setLabelFont(new Font("SansSerif", Font.PLAIN, 12));
        m_chart.getCategoryPlot().getRangeAxis().setLabel(msdo.y_axis_description);
        m_chart.getCategoryPlot().getRangeAxis().setLabelFont(new Font("SansSerif", Font.PLAIN, 12));
        int nbSeries = msdo.matrix[0].length;
        int nbCategories = msdo.matrix.length; // -1 because of 1st column is series names
        for (int serie = 1; serie < nbSeries; serie++) {
            for (int cat = 0; cat < nbCategories; cat++) { // columns of data table also
                //m_dataSet.addValue((double) msdo.matrix[cat][serie], msdo.column_names[serie],  msdo.matrix[cat][0].toString());

                Comparable rowKey = msdo.matrix[cat][0].toString();

                //(Double) Double.parseDouble( Integer.toString(serie)); //msdo.matrix[cat][0];
                double y = (double) msdo.matrix[cat][serie];
                String seriesName = msdo.column_names[serie];

                if (y > 0) { // do not add a zero value otherwise it adds an element on the graph with 0 "thickness"

                    m_dataSet.addValue(y, seriesName, rowKey /*columnKey*/);//(x, y, seriesName); 
                }
            }

        }

    }

}
