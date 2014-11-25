package fr.proline.studio.rsmexplorer.gui;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

import javax.swing.JToolBar;

import org.apache.batik.svggen.SVGGraphics2DIOException;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.PieSectionLabelGenerator;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.general.DefaultPieDataset;
import org.slf4j.LoggerFactory;


import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import fr.proline.studio.export.ExportButton;
import fr.proline.studio.export.ImageExporterInterface;
import fr.proline.studio.gui.HourglassPanel;

import org.slf4j.Logger;

/**
 * Panel used to display a pie chart for MSDiag
 *
 * @author AW
 */
public class MSDiag_PieChart extends HourglassPanel implements  ImageExporterInterface {
    
    public static final String SERIES_NAME = "Pie chart";
    
    protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");
    private static final long serialVersionUID = 1L;

    private DefaultPieDataset m_dataSet;
    private JFreeChart m_chart;
    private File m_pngFile;
 
    private javax.swing.JPanel m_pieChartPanel;
 
     
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
    public MSDiag_PieChart() {
        
        m_dataSet = new DefaultPieDataset();
        m_chart = ChartFactory.createPieChart(
	            "Chart Title",  // chart title
	            m_dataSet ,             // data
	            true,               // include legend
	            true,
	            false
	        );
         
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
       
        m_pieChartPanel = cp;
        
            //
        JToolBar toolbar = initToolbar();
        
        add(toolbar, BorderLayout.WEST);
        add(m_pieChartPanel, BorderLayout.CENTER);
        
        
    }
    
    private JToolBar initToolbar() {
        
        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.setFloatable(false);
   
        ExportButton exportImageButton = new ExportButton("pieChart", (ImageExporterInterface) this);
        toolbar.add(exportImageButton);
       
        
        return toolbar;
        
    }
    
    public void setData(MSDiagOutput_AW msdo) {
        
        constructPieChart(msdo);
     }
    
    public void writeToPNG(String fileName) {
        m_pngFile = new File(fileName);
        try {
            ChartUtilities.saveChartAsPNG(m_pngFile, m_chart, m_pieChartPanel.getWidth(), m_pieChartPanel.getHeight());
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
        m_chart.draw(my_svg_generator, new Rectangle2D.Double(0, 0, m_pieChartPanel.getWidth(), m_pieChartPanel.getHeight()), null);
        
        
        try {
            my_svg_generator.stream(fileName);
        } catch (SVGGraphics2DIOException e) {
            LoggerFactory.getLogger("ProlineStudio.ResultExplorer").error("writeToSVG_batik", e);
        }
        
    }
   
    
    private void constructPieChart(MSDiagOutput_AW msdo) {

        // clear all data
        m_dataSet.clear();
    	
        if (msdo == null) {
            return;
        }
        
        
        // Set title
        String title = msdo.description;
        m_chart.setTitle(title);

        m_chart.getPlot().setBackgroundPaint(Color.white);
        PieSectionLabelGenerator gen = new StandardPieSectionLabelGenerator(
                "{0}: {1} ({2})", new DecimalFormat("0"), new DecimalFormat("0%"));
        ((PiePlot) m_chart.getPlot()).setLabelGenerator(gen);
            
        
        if(msdo.matrix.length == 1) { // then there is some data
	    	if(msdo.matrix[0].length == 2) { // then both data are present
		        m_dataSet.setValue(msdo.column_names[0], (Double) Math.abs((double) msdo.matrix[0][0]));
			    m_dataSet.setValue(msdo.column_names[1], (Double) Math.abs((double) msdo.matrix[0][1]));
			    ((PiePlot) m_chart.getPlot()).setSectionPaint(msdo.column_names[0], Color.red);
		        ((PiePlot) m_chart.getPlot()).setSectionPaint(msdo.column_names[1], Color.blue);
		    }
	    }
	    
    }

}
