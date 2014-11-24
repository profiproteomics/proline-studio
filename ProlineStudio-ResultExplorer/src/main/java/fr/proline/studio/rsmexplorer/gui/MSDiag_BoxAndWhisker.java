package fr.proline.studio.rsmexplorer.gui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;

import org.apache.batik.svggen.SVGGraphics2DIOException;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.CombinedDomainCategoryPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.BoxAndWhiskerRenderer;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.chart.renderer.category.StackedBarRenderer3D;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.statistics.BoxAndWhiskerItem;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;
import org.slf4j.LoggerFactory;

//import org.freehep.graphicsio.emf.EMFGraphics2D;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.studio.export.ExportButton;
import fr.proline.studio.export.ImageExporterInterface;
import fr.proline.studio.gui.HourglassPanel;
import fr.proline.studio.pattern.AbstractDataBox;

import javax.swing.JButton;

import org.openide.windows.WindowManager;
import org.slf4j.Logger;

	/**
	 * Panel used to display MSDiag Chromatogram
	 *
	 * @author AW
	 */

public class MSDiag_BoxAndWhisker  extends HourglassPanel implements  ImageExporterInterface {
	    
	    public static final String SERIES_NAME = "Chromatogram";
	    
	    protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");
	    private static final long serialVersionUID = 1L;
	    private AbstractDataBox m_dataBox;
	    private static boolean redrawInProgress = false; // to ensure no circular loop in changeEven when triggered by zooming the graph... 
	    private double m_spectrumMinX = 0;
	    private double m_spectrumMaxX = 0;
	    private double m_spectrumMinY = 0;
	    private double m_spectrumMaxY = 0;
	    private DefaultBoxAndWhiskerCategoryDataset m_dataSet;
	    private JFreeChart m_chart;
	    private File m_pngFile;
	    private DPeptideMatch m_previousPeptideMatch = null;
	    private boolean m_previousFragmentationSet = false;
	    private javax.swing.JPanel m_chromatogragmPanel;
	    private JButton m_generateMatchButton;
	    
	    // menuItem ShowSpectrumTitle is created while creating the panel, to avoid having a multitude of menuItem in the popupMenu
	    private JMenuItem m_showSpectrumTitle;
	    private String m_spectrumTitle;

		
	    
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
	    public MSDiag_BoxAndWhisker() {
	        
	    	m_dataSet = new DefaultBoxAndWhiskerCategoryDataset();
	    	
	    	
	    	final NumberAxis rangeAxis = new NumberAxis("Masses"); // TODO: find a way to change this value in setData...
	    	 rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits()); // create integer ticks unit. (whatever data is)
	         final BoxAndWhiskerRenderer renderer = new BoxAndWhiskerRenderer(); //BarRenderer();
	       
	         renderer.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator()); // info bulles on bars
	         renderer.setSeriesFillPaint(0, new Color(128,60,60));
	         renderer.setSeriesOutlinePaint(0, new Color(128,60,60));
	        
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
	        
	       
	        final CategoryPlot subplot = new CategoryPlot(m_dataSet, null, rangeAxis, renderer);
	        subplot.setDomainGridlinesVisible(true);

	        final CategoryAxis domainAxis = new CategoryAxis("Category");
	        
	        domainAxis.setCategoryLabelPositions(
	            CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 4.0)
	        );
	        final CombinedDomainCategoryPlot plot = new CombinedDomainCategoryPlot(domainAxis);
	       ;
	        plot.add(subplot, 1);
	        

	        
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
	            @Override
	            public void restoreAutoBounds(){
	            	
	            	XYPlot plot=(XYPlot)getChart().getPlot();
	                double domainStart = plot.getDomainAxis().getDefaultAutoRange().getLowerBound();
	            	double domainEnd =  plot.getDomainAxis().getDefaultAutoRange().getUpperBound();
	            	double rangeStart = plot.getRangeAxis().getDefaultAutoRange().getLowerBound();
	            	double rangeEnd =  plot.getRangeAxis().getDefaultAutoRange().getUpperBound();
	                plot.getDomainAxis().setAutoRange(false);
	                plot.getDomainAxis().setRange(domainStart,domainEnd);
	                plot.getRangeAxis().setRange(rangeStart,rangeEnd);
	                
	            }
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

	        ExportButton exportImageButton = new ExportButton("pieChart", (ImageExporterInterface) this);
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
	        writeToSVG_batik(file);
	    }
	    
	    public void writeToSVG_batik(String fileName) {
	        DOMImplementation mySVGDOM = org.apache.batik.dom.GenericDOMImplementation.getDOMImplementation();
	        Document document = mySVGDOM.createDocument(null, "svg", null);
	        org.apache.batik.svggen.SVGGraphics2D my_svg_generator = new org.apache.batik.svggen.SVGGraphics2D(document);
	        // draw a rectangle of the size that determines the scale of the axis graduations.
	        m_chart.draw(my_svg_generator, new Rectangle2D.Double(0, 0, m_chromatogragmPanel.getWidth(), m_chromatogragmPanel.getHeight()), null);
	        
	        
	        try {
	            my_svg_generator.stream(fileName);
	        } catch (SVGGraphics2DIOException e) {
	            LoggerFactory.getLogger("ProlineStudio.ResultExplorer").error("writeToSVG_batik", e);
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

	        m_chart.getPlot().setBackgroundPaint(Color.white);

	        // 	set axes labels
	       
	        //	m_chart.getCategoryPlot().getRangeAxis().setLabel(msdo.x_axis_description); // does not work
	        
	       
	        m_chart.getCategoryPlot().getDomainAxis().setLabel(msdo.y_axis_description);
	        
	        
	        //String serieTable[] = new String[msdo.matrix[0].length];
	        int nbSeries = msdo.matrix[0].length;
	        int nbCategories = msdo.matrix.length - 1; // -1 because of 1st column is series names

	        ArrayList<Double> listOutliers = new ArrayList();
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
	        		double median= (double) (msdo.matrix[serie][4]);
	        		double q1= 0; //(double) (msdo.matrix[serie][2]);
	        		double q3= 0; //(double) (msdo.matrix[serie][2]);
	        		double minRegularValue= (double) (msdo.matrix[serie][1]);
	        		double maxRegularValue= (double) (msdo.matrix[serie][2]);
	        		double minOutlier = minRegularValue; // not used
	        		double maxOutlier = maxRegularValue; // not used
	        		listOutliers = new ArrayList(0); // not used
	               
					m_dataSet.add(new BoxAndWhiskerItem(mean,median,q1,q3,minRegularValue,maxRegularValue,minOutlier,maxOutlier,listOutliers),serieString,catString);
	        
	        }

	    }
	
}
