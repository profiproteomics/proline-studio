package fr.proline.studio.rsmexplorer.gui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.text.DecimalFormat;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;

import org.apache.batik.svggen.SVGGraphics2DIOException;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.event.PlotChangeEvent;
import org.jfree.chart.event.PlotChangeListener;
import org.jfree.chart.labels.PieSectionLabelGenerator;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.AbstractXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.Range;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYDataset;
import org.slf4j.LoggerFactory;

//import org.freehep.graphicsio.emf.EMFGraphics2D;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import fr.proline.core.orm.msi.Peptide;
import fr.proline.core.orm.msi.Spectrum;
import fr.proline.core.orm.msi.dto.DMsQuery;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.uds.Project;
import fr.proline.studio.dam.DatabaseDataManager;
import fr.proline.studio.dpm.AccessServiceThread;
import fr.proline.studio.dpm.task.AbstractServiceCallback;
import fr.proline.studio.dpm.task.GenerateSpectrumMatchTask;
import fr.proline.studio.export.ExportButton;
import fr.proline.studio.export.ImageExporterInterface;
import fr.proline.studio.gui.HourglassPanel;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import fr.proline.studio.pattern.DataBoxRsetPeptideSpectrum;
import fr.proline.studio.rsmexplorer.gui.ProjectExplorerPanel;
import fr.proline.studio.utils.IconManager;

import java.awt.event.ActionEvent;

import javax.swing.JButton;

import org.openide.windows.WindowManager;
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
    private AbstractDataBox m_dataBox;
    private static boolean redrawInProgress = false; // to ensure no circular loop in changeEven when triggered by zooming the graph... 
    private double m_spectrumMinX = 0;
    private double m_spectrumMaxX = 0;
    private double m_spectrumMinY = 0;
    private double m_spectrumMaxY = 0;
    private DefaultPieDataset m_dataSet;
    private JFreeChart m_chart;
    private File m_pngFile;
    private DPeptideMatch m_previousPeptideMatch = null;
    private boolean m_previousFragmentationSet = false;
    private javax.swing.JPanel m_pieChartPanel;
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
    public MSDiag_PieChart() {
        
        m_dataSet = new DefaultPieDataset();
        //m_chart = ChartFactory.createXYLineChart("", "m/z", "intensity", m_dataSet, PlotOrientation.VERTICAL, true, true, false);
        m_chart = ChartFactory.createPieChart(
	            "Chart Title",  // chart title
	            m_dataSet ,             // data
	            true,               // include legend
	            true,
	            false
	        );
        
        
  //      m_chart.setNotify(false); // stops events handler
        //m_chart.removeLegend();
//        m_chart.setBackgroundPaint(Color.white);
//        TextTitle textTitle = m_chart.getTitle();
//        textTitle.setFont(textTitle.getFont().deriveFont(Font.PLAIN, 10.0f));
//        
//        XYPlot plot = (XYPlot) m_chart.getPlot();
//        plot.getRangeAxis().setUpperMargin(0.2);
//
 //       plot.setBackgroundPaint(Color.white);
//        
//        XYStickRenderer renderer = new XYStickRenderer();
//        renderer.setBaseStroke(new BasicStroke(1.0f));
        
 //       plot.setRenderer(renderer);
        
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
       
        m_pieChartPanel = cp;
        
        // set temp initial data:
       // m_dataSet.setValue("loading...", new Double(10));
    	//m_dataSet.setValue("this should not stay for ever...", new Double(20));

        // JFreePanel sub Menus
        // creation of the menuItem Show Spectrum Title
       // m_showSpectrumTitle = new JMenuItem("Show Spectrum Title");
//        m_showSpectrumTitle.addActionListener(new ActionListener() {
//
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(), m_spectrumTitle, "Spectrum Title", 1);
//            }
//        });

        // add to the popupMenu
        //((ChartPanel) m_pieChartPanel).getPopupMenu().add(m_showSpectrumTitle);
        
        
        //
        JToolBar toolbar = initToolbar();
        
        add(toolbar, BorderLayout.WEST);
        add(m_pieChartPanel, BorderLayout.CENTER);
        
        
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
        constructPieChart(msdo);
        //m_spectrumAnnotations = new RsetPeptideSpectrumAnnotations(m_dataBox, m_dataSet, m_chart, peptideMatch, peptideFragmentationData);
        
        // set default auto bounds in case there is no annotations (which sets new autobounds)
//        m_chart.getXYPlot().getRangeAxis().setDefaultAutoRange(new Range(m_chart.getXYPlot().getRangeAxis().getLowerBound(),m_chart.getXYPlot().getRangeAxis().getUpperBound()));
//        m_chart.getXYPlot().getDomainAxis().setDefaultAutoRange(new Range(m_chart.getXYPlot().getDomainAxis().getLowerBound(),m_chart.getXYPlot().getDomainAxis().getUpperBound()));
       // m_spectrumAnnotations.addAnnotations();
       // m_chart.setNotify(true);
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
        //m_dataSet.removeSeries(SERIES_NAME);
    	m_dataSet.clear();
    	
 
        if (msdo == null) {
            return;
        }
        
        
       // m_dataSet.addSeries(SERIES_NAME, data);


        // Set title
        String title = msdo.description;
        m_chart.setTitle(title);

        // set the spectrum title
        //m_spectrumTitle = spectrum.getTitle();

        
        // reset X/Y zooming
        // ((ChartPanel) m_spectrumPanel).restoreAutoBounds();
        m_chart.getPlot().setBackgroundPaint(Color.white);
        PieSectionLabelGenerator gen = new StandardPieSectionLabelGenerator(
                "{0}: {1} ({2})", new DecimalFormat("0"), new DecimalFormat("0%"));
        ((PiePlot) m_chart.getPlot()).setLabelGenerator(gen);
            
        /// ---- start of plot listenener (for zoom changes for instance)
       // final XYPlot plot = m_chart.getXYPlot();
        //p.setRenderer(renderer);

// may be used but commented for now:
//    	plot.getDomainAxis().setAutoRange(true);
//		plot.getRangeAxis().setAutoRange(true);	
//		m_spectrumMinX = 0; //plot.getDomainAxis().getLowerBound();
//		m_spectrumMaxX = precursorMass;
//		 //plot.getDomainAxis().getUpperBound();
//		m_spectrumMinY = 0;//plot.getRangeAxis().getLowerBound();
//		m_spectrumMaxY =  plot.getRangeAxis().getUpperBound();
//		plot.getDomainAxis().setRange(new Range(0,m_spectrumMaxX), false, true); 
//		plot.getRangeAxis().setRange(new Range(0,m_spectrumMaxY), false, true);
//		plot.getDomainAxis().setDefaultAutoRange(new Range(0,m_spectrumMaxX)); // set new default zoom for x axis
//		plot.getRangeAxis().setDefaultAutoRange(new Range(0,m_spectrumMaxY)); //           "              y axis		
        
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
