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
 * Panel used to display MSDiag box chart
 *
 * @author AW
 */
public class MSDiag_BoxChart extends HourglassPanel {

    public static final String SERIES_NAME = "BoxChart";

    protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");
    private static final long serialVersionUID = 1L;
    private final DefaultBoxAndWhiskerCategoryDataset m_dataSet;
    private final JFreeChart m_chart;
    private javax.swing.JPanel m_boxChartPanel;

    private final CategoryPlot m_subplot; // the plot that holds the range values data

    private final RsetMSDiagPanel m_msdiagPanel;


    /**
     * Creates new form MSDiag_PieChart
     */
    public MSDiag_BoxChart(RsetMSDiagPanel rsetMSDiagPanel) {

        m_msdiagPanel = rsetMSDiagPanel;
        m_dataSet = new DefaultBoxAndWhiskerCategoryDataset();

        NumberAxis rangeAxis = new NumberAxis(""); // TODO: find a way to change this value in setData...
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits()); // create integer ticks unit. (whatever data is)
        final BoxAndWhiskerRenderer renderer = new BoxAndWhiskerRenderer(); //BarRenderer();

        renderer.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator()); // info bulles on bars
        //renderer.setSeriesFillPaint(0, new Color(128,60,60));
        //renderer.setSeriesOutlinePaint(0, new Color(128,60,60));

        renderer.setFillBox(true);
	         //renderer.setSeriesOutlinePaint(0, Color.blue);
        //renderer.setSeriesOutlineStroke(0, new BasicStroke(2f), true);
        //renderer.setSeriesOutlinePaint(1, Color.orange);
        //renderer.setSeriesOutlinePaint(2, Color.red);
        renderer.setSeriesPaint(0, new Color(220, 220, 220/*254,60,60*/));
        renderer.setSeriesPaint(1, new Color(176, 190, 255));
        renderer.setSeriesPaint(2, new Color(132, 153, 255));
        renderer.setSeriesPaint(3, new Color(91, 120, 255));
        renderer.setSeriesPaint(4, new Color(64, 98, 255));
        renderer.setSeriesPaint(5, new Color(42, 71, 255));
        renderer.setSeriesPaint(6, new Color(11, 191, 220));
        renderer.setSeriesPaint(7, new Color(126, 94, 214));

        //renderer.setBaseOutlinePaint(Color.blue, true);
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

        m_boxChartPanel = cp;

        //
        JToolBar toolbar = initToolbar();

        add(toolbar, BorderLayout.WEST);
        add(m_boxChartPanel, BorderLayout.CENTER);

    }

    private JToolBar initToolbar() {

        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.setFloatable(false);
	        //m_picWrapper = new ExportPictureWrapper();
        //m_picWrapper.setFile(m_svgFile);
        FlipButton flipModeButton = new FlipButton("flip button text", m_msdiagPanel);
        toolbar.add(flipModeButton);
        ExportButton exportImageButton = new ExportButton("pieChart", m_boxChartPanel);
        toolbar.add(exportImageButton);

        return toolbar;

    }

    public void setData(MSDiagOutput_AW msdo) {

        constructBoxChart(msdo);

    }


    private void constructBoxChart(MSDiagOutput_AW msdo) {

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
        int nbSeries = msdo.matrix.length;

        ArrayList<Double> listOutliers;
        Comparable<String> serieString;
        Comparable<String> catString;
        for (int serie = 0; serie < nbSeries; serie++) { // lines of data table
            // Charge","Lowest Mass","Highest Mass","Average Mass","Median Mass
            // series n°; minregularvalue,maxregvalue,mean,median
            //BoxAndWhiskerItem item = new BoxAndWhiskerItem(mean,median,q1,q3,minregularValue,maxRegularValue,minOutlier,maxOutlier,listOutliers);

            serieString = msdo.matrix[serie][0].toString();// msdo.column_names[serie];
            catString = msdo.matrix[serie][1].toString();
	        		// column_names\":[\"Charge\",\"Lowest Mass\",\"Highest Mass\",\"Average Mass\",\"Median Mass\"]
            //double charge = (double) (msdo.matrix[serie][0]);
            double mean = (double) (msdo.matrix[serie][4]);
            double median = (double) (msdo.matrix[serie][5]);
            double minRegularValue = (double) (/*Double.valueOf*/(/*(String)*/msdo.matrix[serie][2])); // if it's a string
            double maxRegularValue = (double) (msdo.matrix[serie][3]);
            Number q1 = minRegularValue;//0; //(double) (msdo.matrix[serie][2]);
            Number q3 = maxRegularValue;// null; //(double) (msdo.matrix[serie][2]);

            Number minOutlier = minRegularValue; // not used
            Number maxOutlier = maxRegularValue; // not used
            listOutliers = null; //new ArrayList(0); // not used

            m_dataSet.add(new BoxAndWhiskerItem(mean, median, q1, q3, minRegularValue, maxRegularValue, minOutlier, maxOutlier, listOutliers), catString, serieString);

        }

    }

}
