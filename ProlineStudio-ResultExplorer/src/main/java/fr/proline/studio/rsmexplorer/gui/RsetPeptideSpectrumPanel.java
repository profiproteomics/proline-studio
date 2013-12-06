package fr.proline.studio.rsmexplorer.gui;




import fr.proline.core.orm.msi.Peptide;
import fr.proline.core.orm.msi.Spectrum;
import fr.proline.core.orm.msi.dto.DMsQuery;
import fr.proline.core.orm.msi.dto.DPeptideMatch;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.DefaultXYDataset;
import org.slf4j.LoggerFactory;

import fr.proline.studio.export.ExportButton;
import fr.proline.studio.gui.HourglassPanel;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;

import java.awt.Graphics2D;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;

import javax.swing.JToolBar;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.event.PlotChangeEvent;
import org.jfree.chart.event.PlotChangeListener;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.renderer.xy.AbstractXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.data.xy.XYDataset;

/**
 * Panel used to display a Spectrum of a PeptideMatch
 * @author JM235353
 */
public class RsetPeptideSpectrumPanel extends HourglassPanel implements DataBoxPanelInterface {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private AbstractDataBox m_dataBox;
    
	private static boolean redrawInProgress = false ; // to ensure no circular loop in changeEven when triggered by zooming the graph... 
	private double spectrumMinX = 0;
    private double spectrumMaxX = 0;
    private double spectrumMinY = 0;
    private double spectrumMaxY = 0;
	    
	    
    private DefaultXYDataset m_dataSet;
    private JFreeChart m_chart;
    
    private DPeptideMatch m_previousPeptideMatch = null;
    
    private javax.swing.JPanel m_spectrumPanel;

    
    private RsetPeptideSpectrumAnnotations spectrumAnnotations= null;
    /**
     * Creates new form RsetPeptideSpectrumPanel
     */
    public RsetPeptideSpectrumPanel() {
        
        m_dataSet = new DefaultXYDataset();
        m_chart = ChartFactory.createXYLineChart("", "m/z", "intensity", m_dataSet, PlotOrientation.VERTICAL, true, true, false);
        
        m_chart.removeLegend();
        m_chart.setBackgroundPaint(Color.white);
        TextTitle textTitle = m_chart.getTitle();
        textTitle.setFont(textTitle.getFont().deriveFont(Font.PLAIN, 10.0f));

        XYPlot plot = (XYPlot) m_chart.getPlot();
        plot.getRangeAxis().setUpperMargin(0.2);
       
        plot.setBackgroundPaint(Color.white);

        XYStickRenderer renderer = new XYStickRenderer();
        renderer.setBaseStroke(new BasicStroke(1.0f));
        
        plot.setRenderer(renderer);
        
        initComponents();

        
    }

   
    private void initComponents() {
        setLayout(new BorderLayout());
        ChartPanel cp = new ChartPanel(m_chart, true);
        
        
        cp.setMinimumDrawWidth(0);
        cp.setMinimumDrawHeight(0);
		cp.setMaximumDrawWidth(Integer.MAX_VALUE);
		cp.setMaximumDrawHeight(Integer.MAX_VALUE);
		m_spectrumPanel = cp;
        
		JToolBar toolbar = initToolbar();

        add(toolbar, BorderLayout.WEST);
        add(m_spectrumPanel, BorderLayout.CENTER);

    
        
    }
   
    public final JToolBar initToolbar() {
        
        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.setFloatable(false);
        ExportButton exportImageButton = new ExportButton("Spectre", m_spectrumPanel);
        toolbar.add(exportImageButton);
        return toolbar;

    }
    

    
   public void setData(DPeptideMatch peptideMatch) {

       if (peptideMatch == m_previousPeptideMatch) {
           return;
       }
       m_previousPeptideMatch = peptideMatch;
       
        constructSpectrumChart(peptideMatch);
        spectrumAnnotations = new RsetPeptideSpectrumAnnotations(m_dataBox, m_dataSet, m_chart, peptideMatch);
        spectrumAnnotations.addAnnotations();
 
    }
    
    private void constructSpectrumChart(DPeptideMatch pm) {

        final String SERIES_NAME = "spectrumData";
        if (pm == null) {
            m_dataSet.removeSeries(SERIES_NAME);
    		if(spectrumAnnotations!=null) {
    			spectrumAnnotations.removeAnnotations();
    		}
    		  
            return ;
        }

        DMsQuery msQuery = pm.isMsQuerySet() ? pm.getMsQuery() : null;
        if (msQuery == null) {
            m_dataSet.removeSeries(SERIES_NAME);
            if(spectrumAnnotations!=null) {
    			spectrumAnnotations.removeAnnotations();
    		}
    		
    		  
            return;
        }
        
        Spectrum spectrum = msQuery.isSpectrumSet() ? msQuery.getSpectrum() : null;
        if (spectrum == null) {
            m_dataSet.removeSeries(SERIES_NAME);
            if(spectrumAnnotations!=null) {
    			spectrumAnnotations.removeAnnotations();
    		}
    		  
            return;
        }
        
        Peptide p = pm.getPeptide();
        if (p== null) {
            m_dataSet.removeSeries(SERIES_NAME);
            if(spectrumAnnotations!=null) {
    			spectrumAnnotations.removeAnnotations();
    		}
    		  
            return;
        }


        byte[] intensityByteArray = spectrum.getIntensityList(); 
        byte[] massByteArray = spectrum.getMozList(); 

        
        if ((intensityByteArray == null) || (massByteArray == null)) {
            // should not happen
            m_dataSet.removeSeries(SERIES_NAME);
            return;
        }
        
     
        
        
        ByteBuffer intensityByteBuffer = ByteBuffer.wrap(intensityByteArray).order(ByteOrder.LITTLE_ENDIAN);
        FloatBuffer intensityFloatBuffer = intensityByteBuffer.asFloatBuffer();
        double[] intensityDoubleArray = new double[intensityFloatBuffer.remaining()];
        for (int i=0;i<intensityDoubleArray.length;i++) {
            intensityDoubleArray[i] = (double) intensityFloatBuffer.get();
        }
        
        ByteBuffer massByteBuffer = ByteBuffer.wrap(massByteArray).order(ByteOrder.LITTLE_ENDIAN);
        DoubleBuffer massDoubleBuffer = massByteBuffer.asDoubleBuffer();
        double[] massDoubleArray = new double[massDoubleBuffer.remaining()]; 
        for (int i=0;i<massDoubleArray.length;i++) {
            massDoubleArray[i] = massDoubleBuffer.get();
        }
        

        int size = intensityDoubleArray.length;
        if (size != massDoubleArray.length) {
            LoggerFactory.getLogger("ProlineStudio.ResultExplorer").error("Intensity and Mass List have different size");
            return;
        }

        double[][] data = new double[2][size];
        for (int i = 0; i < size; i++) {
            data[0][i] = massDoubleArray[i];
            data[1][i] = intensityDoubleArray[i];
        }
        m_dataSet.addSeries(SERIES_NAME, data);


        // Set title
        String title = "Query " + pm.getMsQuery().getInitialId() + " - " + pm.getPeptide().getSequence();
        m_chart.setTitle(title);

        // reset X/Y zooming
        ((ChartPanel) m_spectrumPanel).restoreAutoBounds();
        ((ChartPanel) m_spectrumPanel).setBackground(Color.white);
 
        
        /// ---- start of plot listenener (for zoom changes for instance)
    	final XYPlot plot = m_chart.getXYPlot();
		//p.setRenderer(renderer);

    		
		spectrumMinX = plot.getDomainAxis().getLowerBound();
		spectrumMaxX =  plot.getDomainAxis().getUpperBound();
		spectrumMinY = plot.getRangeAxis().getLowerBound();
		spectrumMaxY =  plot.getRangeAxis().getUpperBound();
		
		plot.addChangeListener(new PlotChangeListener() {
			
			@Override
			public void plotChanged(PlotChangeEvent arg0) {
				
				//Plot CHANGED (due to zoom for instance)
				 
				double newMinX = plot.getDomainAxis().getLowerBound();
				double newMaxX = plot.getDomainAxis().getUpperBound();
				double newMinY = plot.getRangeAxis().getLowerBound();
				double newMaxY = plot.getRangeAxis().getUpperBound();
				
				// only if zoom change do the following:
				
				if(!redrawInProgress) {
					if(newMinX != spectrumMinX ||
					   newMaxX != spectrumMaxX ||
					   newMinY != spectrumMinY ||
					   newMaxY != spectrumMaxY  ) {
						
						redrawInProgress = true;
						
						spectrumMinX = newMinX ;
						spectrumMaxX = newMaxX ;
						spectrumMinY = newMinY ;
						spectrumMaxY = newMaxY;
						spectrumAnnotations.removeAnnotations();
						spectrumAnnotations.addAnnotations();
						
						redrawInProgress = false;
					}
				}
				
			}
		});
		
        /// ----
    }
    
    
    
    @Override
    public void setDataBox(AbstractDataBox dataBox) {
        m_dataBox = dataBox;
    }
    
    @Override
    public ActionListener getRemoveAction(SplittedPanelContainer splittedPanel) {
        return m_dataBox.getRemoveAction(splittedPanel);
    }

    @Override
    public ActionListener getAddAction(SplittedPanelContainer splittedPanel) {
        return m_dataBox.getAddAction(splittedPanel);
    }

    
    public static class XYStickRenderer extends AbstractXYItemRenderer {

        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
        public void drawItem(Graphics2D g2, XYItemRendererState state, Rectangle2D dataArea,
                PlotRenderingInfo info, XYPlot plot, ValueAxis domainAxis, ValueAxis rangeAxis,
                XYDataset dataset, int series, int item, CrosshairState crosshairState, int pass) {

            double x = dataset.getXValue(series, item);
            double y = dataset.getYValue(series, item);
            if (!Double.isNaN(y)) {
                org.jfree.ui.RectangleEdge xAxisLocation = plot.getDomainAxisEdge();
                org.jfree.ui.RectangleEdge yAxisLocation = plot.getRangeAxisEdge();
                double transX = domainAxis.valueToJava2D(x, dataArea, xAxisLocation);
                double transOX = domainAxis.valueToJava2D(0, dataArea, xAxisLocation);
                double transY = rangeAxis.valueToJava2D(y, dataArea, yAxisLocation);
                double transOY = rangeAxis.valueToJava2D(0, dataArea, yAxisLocation);
                g2.setPaint(getItemPaint(series, item));
                setSeriesPaint(0, Color.black); // AW: change peak bars color to black (was red by default)
                // g2.setStroke(getBaseStroke()); // AW: original
                g2.setStroke(new BasicStroke(0.5f)); // AW: A thinner stroke than the original
                PlotOrientation orientation = plot.getOrientation();
                if (orientation == PlotOrientation.VERTICAL) {
                    g2.drawLine((int) transX, (int) transOY, (int) transX, (int) transY);
                } else if (orientation == PlotOrientation.HORIZONTAL) {
                    g2.drawLine((int) transOY, (int) transX, (int) transY, (int) transX);
                }
                int domainAxisIndex = plot.getDomainAxisIndex(domainAxis);
                int rangeAxisIndex = plot.getRangeAxisIndex(rangeAxis);
                updateCrosshairValues(crosshairState, x, y, domainAxisIndex, rangeAxisIndex, transX, transY,
                        orientation);

            }
        }
    }
    
    public static byte[] floatsToBytes( float[] floats) {

    // Convert float to a byte buffer
    ByteBuffer byteBuf = ByteBuffer.allocate(4 * floats.length).order(ByteOrder.LITTLE_ENDIAN);
    for (int i=0;i<floats.length;i++) {
        byteBuf.putFloat(floats[i]);
    }
    // Convert byte buffer into a byte array
    return byteBuf.array();
  }
    

  	
}
	
	

