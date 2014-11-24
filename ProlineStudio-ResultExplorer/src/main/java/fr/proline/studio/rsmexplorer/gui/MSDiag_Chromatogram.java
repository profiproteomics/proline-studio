package fr.proline.studio.rsmexplorer.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;

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
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.chart.renderer.category.StackedBarRenderer3D;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.DefaultCategoryDataset;
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

public class MSDiag_Chromatogram  extends HourglassPanel implements  ImageExporterInterface {
	    
	    public static final String SERIES_NAME = "Chromatogram";
	    
	    protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");
	    private static final long serialVersionUID = 1L;
	    private AbstractDataBox m_dataBox;
	    private static boolean redrawInProgress = false; // to ensure no circular loop in changeEven when triggered by zooming the graph... 
	    private double m_spectrumMinX = 0;
	    private double m_spectrumMaxX = 0;
	    private double m_spectrumMinY = 0;
	    private double m_spectrumMaxY = 0;
	    private DefaultCategoryDataset m_dataSet;
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
	    public MSDiag_Chromatogram() {
	        
	    	m_dataSet = new DefaultCategoryDataset();
	    	//m_chart = ChartFactory.createXYLineChart("", "m/z", "intensity", m_dataSet, PlotOrientation.VERTICAL, true, true, false);

	    	
	    	final NumberAxis rangeAxis2 = new NumberAxis("Value");
	        rangeAxis2.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
	        final BarRenderer renderer = new StackedBarRenderer3D();
	        renderer.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator());
	        renderer.setDrawBarOutline(false);
	        renderer.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator()); // info bulles on bars
	        renderer.setShadowVisible(false);
	     // set up gradient paints for series...
//	        GradientPaint gp0 = new GradientPaint(
//	            0.0f, 0.0f, Color.red, 
//	            0.0f, 0.0f, new Color(0, 0, 64)
//	        );
//	        GradientPaint gp2 = new GradientPaint(
//	            0.0f, 0.0f, Color.blue, 
//	            0.0f, 0.0f, new Color(0, 64, 0)
//	        );
//	        GradientPaint gp1 = new GradientPaint(
//		            0.0f, 0.0f, Color.green, 
//		            0.0f, 0.0f, new Color(64, 0, 0)
//		        );
//	        GradientPaint gp4 = new GradientPaint(
//		            0.0f, 0.0f, Color.yellow, 
//		            0.0f, 0.0f, new Color(64, 0, 0)
//		        );
//	        GradientPaint gp3 = new GradientPaint(
//		            0.0f, 0.0f, Color.orange, 
//		            0.0f, 0.0f, new Color(64, 0, 0)
//		        );
//	        GradientPaint gp5 = new GradientPaint(
//		            0.0f, 0.0f, Color.pink, 
//		            0.0f, 0.0f, new Color(64, 0, 0)
//		        );
//	        GradientPaint gp6 = new GradientPaint(
//		            0.0f, 0.0f, Color.gray, 
//		            0.0f, 0.0f, new Color(64, 0, 0)
//		        );
//	        
	        renderer.setSeriesPaint(0, new Color(255,60,60));
	        renderer.setSeriesPaint(1, new Color(60,255,60));
	        renderer.setSeriesPaint(2, new Color(60,60,255));
	        renderer.setSeriesPaint(3, new Color(255,60,255));
	        renderer.setSeriesPaint(4, new Color(60,255,255));
	        renderer.setSeriesPaint(5, new Color(1500,20,50));
	        renderer.setSeriesPaint(6, new Color(20,150,50));

	        
	        final CategoryPlot subplot = new CategoryPlot(m_dataSet, null, rangeAxis2, renderer);
	        subplot.setDomainGridlinesVisible(true);

	        final CategoryAxis domainAxis = new CategoryAxis("Category");
	         //CategoryAxis domainAxis = plot.getDomainAxis();
	        domainAxis.setCategoryLabelPositions(
	            CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 4.0)
	        );
	        final CombinedDomainCategoryPlot plot = new CombinedDomainCategoryPlot(domainAxis);
	        //plot.add(subplot1, 2);
	        plot.add(subplot, 1);
	        
	        
	       
	        
	    	m_chart = new JFreeChart(
	                "Combined Chart title",
	                new Font("SansSerif", Font.BOLD, 12),
	                plot,
	                true
	            );
	    	
	        
	  //      m_chart.setNotify(false); // stops events handler
	        //m_chart.removeLegend();
	        m_chart.setBackgroundPaint(Color.white);
	        TextTitle textTitle = m_chart.getTitle();
	        textTitle.setFont(textTitle.getFont().deriveFont(Font.PLAIN, 10.0f));
//	        
	       //XYPlot plot = (XYPlot) m_chart.getPlot();
	       // plot.getRangeAxis().setUpperMargin(0.2);
	//
	 //       plot.setBackgroundPaint(Color.white);
//	        
//	        XYStickRenderer renderer = new XYStickRenderer();
//	        renderer.setBaseStroke(new BasicStroke(1.0f));
	        
	 //       plot.setRenderer(renderer);
	        
	        initComponents();
	        
	        
	    }
	    
	    private void initComponents() {
	        setLayout(new BorderLayout());
	        ChartPanel cp = new ChartPanel(m_chart, true) {
//	            @Override
//	            public void restoreAutoBounds(){
//	            	
//	            	XYPlot plot=(XYPlot)getChart().getPlot();
//	                double domainStart = plot.getDomainAxis().getDefaultAutoRange().getLowerBound();
//	            	double domainEnd =  plot.getDomainAxis().getDefaultAutoRange().getUpperBound();
//	            	double rangeStart = plot.getRangeAxis().getDefaultAutoRange().getLowerBound();
//	            	double rangeEnd =  plot.getRangeAxis().getDefaultAutoRange().getUpperBound();
//	                plot.getDomainAxis().setAutoRange(false);
//	                plot.getDomainAxis().setRange(domainStart,domainEnd);
//	                plot.getRangeAxis().setRange(rangeStart,rangeEnd);
//	                
//	            }
	        };
	        
	        
	        cp.setMinimumDrawWidth(0);
	        cp.setMinimumDrawHeight(0);
	        cp.setMaximumDrawWidth(Integer.MAX_VALUE); // make the legend to have a fixed size and not strecht it 
	        cp.setMaximumDrawHeight(Integer.MAX_VALUE); // when the windows becomes bigger.
	        m_chart.getPlot().setBackgroundPaint(Color.white);
	       
	        m_chromatogragmPanel = cp;
	        
	        // set temp initial data:
	       // m_dataSet.setValue("loading...", new Double(10));
	    	//m_dataSet.setValue("this should not stay for ever...", new Double(20));

	        // JFreePanel sub Menus
	        // creation of the menuItem Show Spectrum Title
	       // m_showSpectrumTitle = new JMenuItem("Show Spectrum Title");
//	        m_showSpectrumTitle.addActionListener(new ActionListener() {
	//
//	            @Override
//	            public void actionPerformed(ActionEvent e) {
//	                JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(), m_spectrumTitle, "Spectrum Title", 1);
//	            }
//	        });

	        // add to the popupMenu
	        //((ChartPanel) m_pieChartPanel).getPopupMenu().add(m_showSpectrumTitle);
	        
	        
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
	             
	        
	   //     m_chart.setNotify(false);
	        constructChromatogram(msdo);
	        //m_spectrumAnnotations = new RsetPeptideSpectrumAnnotations(m_dataBox, m_dataSet, m_chart, peptideMatch, peptideFragmentationData);
	        
	        // set default auto bounds in case there is no annotations (which sets new autobounds)
//	        m_chart.getXYPlot().getRangeAxis().setDefaultAutoRange(new Range(m_chart.getXYPlot().getRangeAxis().getLowerBound(),m_chart.getXYPlot().getRangeAxis().getUpperBound()));
//	        m_chart.getXYPlot().getDomainAxis().setDefaultAutoRange(new Range(m_chart.getXYPlot().getDomainAxis().getLowerBound(),m_chart.getXYPlot().getDomainAxis().getUpperBound()));
	       // m_spectrumAnnotations.addAnnotations();
	       // m_chart.setNotify(true);
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
	        //m_chart.draw(my_svg_generator, new Rectangle2D.Double(0, 0, 800,600), null);
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
	        m_dataSet.clear(); //removeSeries(SERIES_NAME);
	    	
	    	
	 
	        if (msdo == null) {
	            return;
	        }
	       
	        // Set title
	        String title = msdo.description;
	        m_chart.setTitle(title);
	        
	        // set axes labels
	        m_chart.getCategoryPlot().getDomainAxis().setLabel(msdo.x_axis_description);
	        m_chart.getCategoryPlot().getDomainAxis().setLabel(msdo.y_axis_description);
	        // set the spectrum title
	        //m_spectrumTitle = spectrum.getTitle();

	        
	        // reset X/Y zooming
	        // ((ChartPanel) m_spectrumPanel).restoreAutoBounds();
	        m_chart.getPlot().setBackgroundPaint(Color.white);

	        //String serieTable[] = new String[msdo.matrix[0].length];
	        int nbSeries = msdo.matrix[0].length;
	        int nbCategories = msdo.matrix.length - 1; // -1 because of 1st column is series names
//	        for (int i = 0; i < nbSeries; i++) {
//	          serieTable[i] = msdo.matrix[0][i].toString();
//	        }
	        
	       // type 1: mettre: column_titles (category) columns
	        // serie: mettre 1ere colonne de données. (scan group etc).
	        // column keys...
	        

	       // result.addValue(1.0, series1, type1);
	       // result.addValue(4.0, series1, type2);
	        
	      //  JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(), "nb series:" + nbSeries + "\nnbCat:"+ nbCategories, "DEBUG", 1);
	        for (int serie = 1; serie < nbSeries; serie++) {
	        	for (int cat = 0; cat < nbCategories; cat++) { // columns of data table also
	        		m_dataSet.addValue((double) msdo.matrix[cat][serie], msdo.column_names[serie],  msdo.matrix[cat][0].toString());
	        	}
	        
	        }
	       
	        
	        
	        

	        
	    }

	
}
