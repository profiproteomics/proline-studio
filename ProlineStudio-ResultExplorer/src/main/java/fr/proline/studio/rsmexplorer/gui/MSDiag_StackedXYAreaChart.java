package fr.proline.studio.rsmexplorer.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.IOException;

import javax.swing.JToolBar;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;

import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StackedXYAreaRenderer2;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.CategoryTableXYDataset;
import org.slf4j.LoggerFactory;

import fr.proline.studio.export.ExportButton;
import fr.proline.studio.export.ImageExporterInterface;
import fr.proline.studio.gui.HourglassPanel;
import javax.swing.JPanel;
import org.jfree.graphics2d.svg.SVGGraphics2D;
import org.jfree.graphics2d.svg.SVGUtils;

import org.slf4j.Logger;

/**
 * Panel used to display MSDiag Chromatogram
 *
 * @author AW
 */
public class MSDiag_StackedXYAreaChart extends HourglassPanel implements ImageExporterInterface {

    public static final String SERIES_NAME = "Chromatogram";

    protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");
    private static final long serialVersionUID = 1L;
    private final CategoryTableXYDataset m_dataSet;
    private final JFreeChart m_chart;
    private ChartPanel m_chromatogragmPanel;

    private File m_pngFile;

    @Override // declared in ProlineStudioCommons ImageExporterInterface
    public void generateSvgImage(String file) {
        writeToSVG(file);
    }

    @Override // declared in ProlineStudioCommons ImageExporterInterface
    public void generatePngImage(String file) {
        writeToPNG(file);
    }

    @Override
    public String getSupportedFormats() {
        return "png,svg";
    }

    /**
     * Creates new form MSDiag_PieChart
     */
    public MSDiag_StackedXYAreaChart() {

        m_dataSet = new CategoryTableXYDataset();

        final NumberAxis rangeAxis = new NumberAxis("");
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
	       // StackedXYAreaRenderer2 renderer = new StackedXYAreaRenderer2();

//	        renderer.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator());
//	        renderer.setDrawBarOutline(false);
//	        renderer.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator()); // info bulles on bars
//	        renderer.setShadowVisible(false);
	        //m_subplot = new CategoryPlot(m_dataSet, null, rangeAxis, renderer);
        //m_subplot.setDomainGridlinesVisible(true);
	        //final CategoryAxis domainAxis = new CategoryAxis("");
	        //domainAxis.setCategoryLabelPositions(
        //    CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 2.0)
        //);
        //final CombinedDomainCategoryPlot plot = new CombinedDomainCategoryPlot(domainAxis);
	        //plot.add(m_subplot, 1);
//	    	m_chart = new JFreeChart(
//	                "Combined Chart title",
//	                new Font("SansSerif", Font.BOLD, 12),
//	                plot,
//	                true
//	            );
        m_chart = ChartFactory.createStackedXYAreaChart(
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
        XYPlot plot = (XYPlot) m_chart.getPlot();

        StackedXYAreaRenderer2 renderer = (StackedXYAreaRenderer2) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(254, 60, 60));
        renderer.setSeriesPaint(1, new Color(176, 190, 255));
        renderer.setSeriesPaint(2, new Color(132, 153, 255));
        renderer.setSeriesPaint(3, new Color(91, 120, 255));
        renderer.setSeriesPaint(4, new Color(64, 98, 255));
        renderer.setSeriesPaint(5, new Color(32, 71, 255));
        renderer.setSeriesPaint(6, new Color(1, 191, 220));
        renderer.setSeriesPaint(7, new Color(126, 84, 214));

	       // plot.setDomainGridlinePaint(Color.white);
        //plot.setDomainGridlinesVisible(true);
        //plot.setRangeGridlinePaint(Color.white);
        TextTitle textTitle = m_chart.getTitle();
        textTitle.setFont(textTitle.getFont().deriveFont(Font.PLAIN, 10.0f));

        initComponents();

    }

    private void initComponents() {
        setLayout(new BorderLayout());
        m_chromatogragmPanel = new ChartPanel(m_chart, true);

        m_chromatogragmPanel.setMinimumDrawWidth(0);
        m_chromatogragmPanel.setMinimumDrawHeight(0);
        m_chromatogragmPanel.setMaximumDrawWidth(Integer.MAX_VALUE); // make the legend to have a fixed size and not strecht it 
        m_chromatogragmPanel.setMaximumDrawHeight(Integer.MAX_VALUE); // when the windows becomes bigger.
        m_chart.getPlot().setBackgroundPaint(Color.white);

        JToolBar toolbar = initToolbar();

        add(toolbar, BorderLayout.WEST);
        add(m_chromatogragmPanel, BorderLayout.CENTER);

    }

    private JToolBar initToolbar() {

        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.setFloatable(false);

        ExportButton exportImageButton = new ExportButton("chart", (ImageExporterInterface) this);
        toolbar.add(exportImageButton);

        return toolbar;

    }

    public void setData(MSDiagOutput_AW msdo) {

        constructChromatogram(msdo);

    }

    public void writeToPNG(String fileName) {
        m_pngFile = new File(fileName);
        try {
            ChartUtilities.saveChartAsPNG(m_pngFile, m_chart, m_chromatogragmPanel.getWidth(), m_chromatogragmPanel.getHeight());
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

        int nbSeries = msdo.matrix[0].length;
        int nbCategories = msdo.matrix.length - 1; // -1 because of 1st column is series names
        for (int serie = 1; serie < nbSeries; serie++) {
            for (int cat = 0; cat < nbCategories; cat++) { // columns of data table also
                //m_dataSet.addValue((double) msdo.matrix[cat][serie], msdo.column_names[serie],  msdo.matrix[cat][0].toString());

                double x = (Double) Double.parseDouble(Integer.toString(serie)); //msdo.matrix[cat][0];
                double y = (Double) msdo.matrix[cat][serie];
                String seriesName = msdo.column_names[serie];
                if (y > 0) { // do not add a zero value otherwise it adds an element on the graph with 0 "thickness"
                    m_dataSet.add(x, y, seriesName);
                }
            }

        }

    }

}
