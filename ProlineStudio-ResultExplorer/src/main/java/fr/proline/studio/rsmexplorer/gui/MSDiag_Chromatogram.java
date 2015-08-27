package fr.proline.studio.rsmexplorer.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;

import javax.swing.JToolBar;

import org.apache.batik.svggen.SVGGraphics2DIOException;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StackedXYBarRenderer;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.DefaultTableXYDataset;
import org.jfree.data.xy.XYSeries;
import org.slf4j.LoggerFactory;


import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import fr.proline.studio.export.ExportButton;
import fr.proline.studio.export.ImageExporterInterface;
import fr.proline.studio.gui.HourglassPanel;








import org.slf4j.Logger;

	/**
	 * Panel used to display MSDiag Chromatogram
	 * it requires data with 1st column: retention time or other numeric value (for x axis)
	 * data from 2nd column are the y values.
	 * series names are column titles.
	 * they are stacked as a XYStackedChart.
	 *
	 * @author AW
	 */

public class MSDiag_Chromatogram  extends HourglassPanel implements  ImageExporterInterface {
	    
	    public static final String SERIES_NAME = "Chromatogram";
	    
	    protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");
	    private static final long serialVersionUID = 1L;
	    private DefaultTableXYDataset m_dataSet;
	    private JFreeChart m_chart;
	    private File m_pngFile;
	    private javax.swing.JPanel m_chromatogramPanel;

		private RsetMSDiagPanel m_msdiagPanel;
	    
	   	    
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
	     * @param rsetMSDiagPanel 
	     */
	    public MSDiag_Chromatogram(RsetMSDiagPanel rsetMSDiagPanel) {
	        
	    	m_msdiagPanel = rsetMSDiagPanel;
	    	m_dataSet = new DefaultTableXYDataset();
	    	

	    	 NumberAxis numberaxis = new NumberAxis("X");   
	         numberaxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());   
	         NumberAxis numberaxis1 = new NumberAxis("Y");   
	         StackedXYBarRenderer renderer = new StackedXYBarRenderer (0);    // put space between bars in parameter.0,...
	         renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator());
	         renderer.setDrawBarOutline(false);  
	        renderer.setBarPainter(new StandardXYBarPainter());
	         
	        renderer.setSeriesPaint(0, new Color(254,60,60));
	        renderer.setSeriesPaint(1, new Color(176,190,255));
	        renderer.setSeriesPaint(2, new Color(132,153,255));
	        renderer.setSeriesPaint(3, new Color(91,120,255));
	        renderer.setSeriesPaint(4, new Color(64,98,255));
	        renderer.setSeriesPaint(5, new Color(32,71,255));
	        renderer.setSeriesPaint(6, new Color(1,191,220));
	        renderer.setSeriesPaint(7, new Color(126,84,214));

	     
	        XYPlot plot = new XYPlot(m_dataSet, numberaxis, numberaxis1, renderer);   
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
	        ChartPanel cp = new ChartPanel(m_chart, true);
	        
	        
	        cp.setMinimumDrawWidth(0);
	        cp.setMinimumDrawHeight(0);
	        cp.setMaximumDrawWidth(Integer.MAX_VALUE); // make the legend to have a fixed size and not strecht it 
	        cp.setMaximumDrawHeight(Integer.MAX_VALUE); // when the windows becomes bigger.
	        m_chart.getPlot().setBackgroundPaint(Color.white);
	       
	        m_chromatogramPanel = cp;
	        
	        JToolBar toolbar = initToolbar();
	        
	        add(toolbar, BorderLayout.WEST);
	        add(m_chromatogramPanel, BorderLayout.CENTER);
	        
	        
	    }
	    
	    private JToolBar initToolbar() {
	        
	        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
	        toolbar.setFloatable(false);
	        
	        FlipButton flipModeButton = new FlipButton("flip button text", m_msdiagPanel);
	        toolbar.add(flipModeButton);
	        
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
	            ChartUtilities.saveChartAsPNG(m_pngFile, m_chart, m_chromatogramPanel.getWidth(), m_chromatogramPanel.getHeight());
	        } catch (IOException e) {
	            LoggerFactory.getLogger("ProlineStudio.ResultExplorer").error("writeToPNG", e);
	        }
	    }
	    
	    public void writeToSVG(String file) {
	        writeToSVG_batik(file);
	    }
	    
	    public void writeToSVG_batik(String fileName) {
	        DOMImplementation mySVGDOM = org.apache.batik.dom.GenericDOMImplementation.getDOMImplementation();
	        Document document = mySVGDOM.createDocument(null, "svg", null);
	        org.apache.batik.svggen.SVGGraphics2D my_svg_generator = new org.apache.batik.svggen.SVGGraphics2D(document);
	        //m_chart.draw(my_svg_generator, new Rectangle2D.Double(0, 0, 800,600), null);
	        // draw a rectangle of the size that determines the scale of the axis graduations.
	        m_chart.draw(my_svg_generator, new Rectangle2D.Double(0, 0, m_chromatogramPanel.getWidth(), m_chromatogramPanel.getHeight()), null);
	        
	        
	        try {
	            my_svg_generator.stream(fileName);
	        } catch (SVGGraphics2DIOException e) {
	            LoggerFactory.getLogger("ProlineStudio.ResultExplorer").error("writeToSVG_batik", e);
	        }
	        
	    }
	   
	    
	    private void constructChromatogram(MSDiagOutput_AW msdo) {

	        // clear all data
	        m_dataSet.removeAllSeries();
	    	
	    	
	 
	        if (msdo == null) {
	            return;
	        }
	       
	        // Set title
	        String title = msdo.description;
	        m_chart.setTitle(title);
	        
	        // set axes labels
	        //m_subplot.getRangeAxis().setLabel(msdo.y_axis_description);
	        m_chart.getXYPlot().getDomainAxis().setLabel(msdo.x_axis_description);
	        m_chart.getXYPlot().getRangeAxis().setLabel(msdo.y_axis_description);
	        
	        
	        
	        m_chart.getPlot().setBackgroundPaint(Color.white);

	        int nbSeries = msdo.matrix[0].length;
	        int nbCategories = msdo.matrix.length - 1; // -1 because of 1st column is series names
	        double x;
	        double y;

	        
	        for (int serie = 1; serie < nbSeries; serie++) {
	        	XYSeries xyseries = new XYSeries(msdo.column_names[serie], true, false);
	        	for (int cat = 0; cat < nbCategories; cat++) { // columns of data table also
	        		y = (Double) msdo.matrix[cat][serie];
	        		if(y>0) { // do not add a zero value otherwise it adds an element on the graph with 0 "thickness"
	        			 x =   (Double) msdo.matrix[cat][0];
	        			 xyseries.add(x,y);
	        	 	}
	        	}
	        	m_dataSet.addSeries(xyseries);
	        }
	        
	    }

	
}
