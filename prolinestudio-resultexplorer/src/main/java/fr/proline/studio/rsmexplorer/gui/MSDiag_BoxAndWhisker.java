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
package fr.proline.studio.rsmexplorer.gui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import javax.swing.JToolBar;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.CombinedDomainCategoryPlot;
import org.jfree.chart.renderer.category.BoxAndWhiskerRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.statistics.BoxAndWhiskerItem;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;
import org.slf4j.LoggerFactory;


import fr.proline.studio.export.ExportButton;
import fr.proline.studio.gui.HourglassPanel;

import org.slf4j.Logger;

/**
 * Panel used to display MSDiag Chromatogram
 *
 * @author AW
 */
public class MSDiag_BoxAndWhisker extends HourglassPanel {

    public static final String SERIES_NAME = "Chromatogram";

    protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");
    private static final long serialVersionUID = 1L;
    private final DefaultBoxAndWhiskerCategoryDataset m_dataSet;
    private final JFreeChart m_chart;
    private javax.swing.JPanel m_chromatogragmPanel;

    private final CategoryPlot m_subplot; // the plot that holds the range values data


    /**
     * Creates new form MSDiag_PieChart
     */
    public MSDiag_BoxAndWhisker() {

        m_dataSet = new DefaultBoxAndWhiskerCategoryDataset();

        NumberAxis rangeAxis = new NumberAxis(""); // TODO: find a way to change this value in setData...
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits()); // create integer ticks unit. (whatever data is)
        final BoxAndWhiskerRenderer renderer = new BoxAndWhiskerRenderer(); //BarRenderer();

        renderer.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator()); // info bulles on bars
        renderer.setSeriesFillPaint(0, new Color(128, 60, 60));
        renderer.setSeriesOutlinePaint(0, new Color(128, 60, 60));

        renderer.setFillBox(false);
        renderer.setSeriesOutlinePaint(0, Color.blue);
        renderer.setSeriesOutlineStroke(0, new BasicStroke(2f), true);
        renderer.setBaseOutlinePaint(Color.blue, true);
        renderer.setItemMargin(0.20);
        renderer.setMaximumBarWidth(1.0);
        renderer.setBaseLegendShape(new Rectangle2D.Double(-4.0, -4.0, 8.0, 8.0));

        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        renderer.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator()); // info bulles on bars

        renderer.setSeriesPaint(0, Color.red);
        renderer.setSeriesPaint(0, Color.blue);
        renderer.setSeriesPaint(0, Color.green);

        m_subplot = new CategoryPlot(m_dataSet, null, rangeAxis, renderer);
        m_subplot.setDomainGridlinesVisible(true);

        final CategoryAxis domainAxis = new CategoryAxis("");

        domainAxis.setCategoryLabelPositions(
                CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 4.0)
        );
        final CombinedDomainCategoryPlot plot = new CombinedDomainCategoryPlot(domainAxis);

        plot.add(m_subplot, 1);

        m_chart = new JFreeChart(
                "Combined Chart title",
                new Font("SansSerif", Font.BOLD, 12),
                plot,
                true
        );

        m_chart.setBackgroundPaint(Color.white);
        TextTitle textTitle = m_chart.getTitle();
        textTitle.setFont(textTitle.getFont().deriveFont(Font.PLAIN, 10.0f));

        initComponents();

    }

    private void initComponents() {
        setLayout(new BorderLayout());
        ChartPanel cp = new ChartPanel(m_chart, true) {

        };

        cp.setMinimumDrawWidth(0);
        cp.setMinimumDrawHeight(0);
        cp.setMaximumDrawWidth(Integer.MAX_VALUE); // make the legend to have a fixed size and not strecht it 
        cp.setMaximumDrawHeight(Integer.MAX_VALUE); // when the windows becomes bigger.
        m_chart.getPlot().setBackgroundPaint(Color.white);

        m_chromatogragmPanel = cp;

        //
        JToolBar toolbar = initToolbar();

        add(toolbar, BorderLayout.WEST);
        add(m_chromatogragmPanel, BorderLayout.CENTER);

    }

    private JToolBar initToolbar() {

        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.setFloatable(false);
	        //m_picWrapper = new ExportPictureWrapper();
        //m_picWrapper.setFile(m_svgFile);

        ExportButton exportImageButton = new ExportButton("pieChart", m_chromatogragmPanel);
        toolbar.add(exportImageButton);

        return toolbar;

    }

    public void setData(MSDiagOutput_AW msdo) {

        constructChromatogram(msdo);

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

        m_chart.getPlot().setBackgroundPaint(Color.white);

	        // 	set axes labels
        //	m_chart.getCategoryPlot().getRangeAxis().setLabel(msdo.y_axis_description); // does not work
        m_subplot.getRangeAxis().setLabel(msdo.y_axis_description);
	        //((CategoryPlot) m_chart.getPlot()).getRangeAxis().setLabel(msdo.y_axis_description);// does not work !!!

        m_chart.getCategoryPlot().getDomainAxis().setLabel(msdo.x_axis_description);

        //String serieTable[] = new String[msdo.matrix[0].length];
        int nbSeries = msdo.matrix[0].length;

        Comparable<String> serieString;
        Comparable<String> catString;
        for (int serie = 1; serie < nbSeries; serie++) { // lines of data table
            // Charge","Lowest Mass","Highest Mass","Average Mass","Median Mass
            // series n°; minregularvalue,maxregvalue,mean,median
            //BoxAndWhiskerItem item = new BoxAndWhiskerItem(mean,median,q1,q3,minregularValue,maxRegularValue,minOutlier,maxOutlier,listOutliers);

            serieString = msdo.column_names[serie];
            catString = msdo.matrix[serie][0].toString();
	        		// column_names\":[\"Charge\",\"Lowest Mass\",\"Highest Mass\",\"Average Mass\",\"Median Mass\"]
            //double charge = (double) (msdo.matrix[serie][0]);
            double mean = (double) (msdo.matrix[serie][3]);
            double median = (double) (msdo.matrix[serie][4]);
            double q1 = 0; //(double) (msdo.matrix[serie][2]);
            double q3 = 0; //(double) (msdo.matrix[serie][2]);
            double minRegularValue = (double) (msdo.matrix[serie][1]);
            double maxRegularValue = (double) (msdo.matrix[serie][2]);
            double minOutlier = minRegularValue; // not used
            double maxOutlier = maxRegularValue; // not used
            ArrayList<Double> listOutliers = new ArrayList(0); // not used

            m_dataSet.add(new BoxAndWhiskerItem(mean, median, q1, q3, minRegularValue, maxRegularValue, minOutlier, maxOutlier, listOutliers), serieString, catString);

        }

    }

}
