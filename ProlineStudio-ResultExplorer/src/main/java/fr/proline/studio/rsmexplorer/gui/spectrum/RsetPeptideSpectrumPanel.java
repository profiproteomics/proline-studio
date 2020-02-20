package fr.proline.studio.rsmexplorer.gui.spectrum;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.event.PlotChangeEvent;
import org.jfree.chart.event.PlotChangeListener;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.AbstractXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.Range;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYDataset;
import org.slf4j.LoggerFactory;


import fr.proline.core.orm.msi.Peptide;
import fr.proline.core.orm.msi.dto.DSpectrum;
import fr.proline.core.orm.msi.dto.DMsQuery;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.uds.Project;
import fr.proline.studio.dam.DatabaseDataManager;
import fr.proline.studio.dpm.AccessJMSManagerThread;
import fr.proline.studio.dpm.task.jms.AbstractJMSCallback;
import fr.proline.studio.dpm.task.jms.GenerateSpectrumMatchTask;
import fr.proline.studio.export.ExportButton;
import fr.proline.studio.gui.DefaultDialog;
import fr.proline.studio.gui.HourglassPanel;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import fr.proline.studio.pattern.DataBoxRsetPeptideSpectrum;
import fr.proline.studio.rsmexplorer.gui.ProjectExplorerPanel;
import fr.proline.studio.rsmexplorer.gui.dialog.GenerateSpectrumMarchesDialog;
import fr.proline.studio.utils.IconManager;
import java.awt.Cursor;

import java.awt.event.ActionEvent;

import javax.swing.JButton;
import org.openide.windows.WindowManager;


import org.slf4j.Logger;


/**
 * Panel used to display a Spectrum of a PeptideMatch
 *
 * @author JM235353
 */
public class RsetPeptideSpectrumPanel extends HourglassPanel implements DataBoxPanelInterface {
    
    public static final String SERIES_NAME = "spectrumData";
    
    protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");
    private static final long serialVersionUID = 1L;
    private AbstractDataBox m_dataBox;
    private static boolean redrawInProgress = false; // to ensure no circular loop in changeEven when triggered by zooming the graph... 
    private double m_spectrumMinX = 0;
    private double m_spectrumMaxX = 0;
    private double m_spectrumMinY = 0;
    private double m_spectrumMaxY = 0;
    private final DefaultXYDataset m_dataSet;
    private final JFreeChart m_chart;

    private DPeptideMatch m_previousPeptideMatch = null;
    private boolean m_previousFragmentationSet = false;
    private javax.swing.JPanel m_spectrumPanel;
    private JButton m_generateMatchButton;
    
    // menuItem ShowSpectrumTitle is created while creating the panel, to avoid having a multitude of menuItem in the popupMenu
    private JMenuItem m_showSpectrumTitle;
    private String m_spectrumTitle;
    
    private RsetPeptideSpectrumAnnotations m_spectrumAnnotations = null;

   

    /**
     * Creates new form RsetPeptideSpectrumPanel
     */
    public RsetPeptideSpectrumPanel() {
        
        m_dataSet = new DefaultXYDataset();
        m_chart = ChartFactory.createXYLineChart("", "m/z", "intensity", m_dataSet, PlotOrientation.VERTICAL, true, true, false);
        m_chart.setNotify(false);
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
        m_spectrumPanel = cp;

        // JFreePanel sub Menus
        // creation of the menuItem Show Spectrum Title
        m_showSpectrumTitle = new JMenuItem("Show Spectrum Title");
        m_showSpectrumTitle.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(), m_spectrumTitle, "Spectrum Title", 1);
            }
        });

        
        // add to the popupMenu
        ((ChartPanel) m_spectrumPanel).getPopupMenu().add(m_showSpectrumTitle);
        
        
        //
        JToolBar toolbar = initToolbar();
        
        add(toolbar, BorderLayout.WEST);
        add(m_spectrumPanel, BorderLayout.CENTER);
        
        
    }
    
    private JToolBar initToolbar() {
        
        JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
        toolbar.setFloatable(false);
        //m_picWrapper = new ExportPictureWrapper();
        //m_picWrapper.setFile(m_svgFile);

        ExportButton exportImageButton = new ExportButton("Spectre", m_spectrumPanel);
        toolbar.add(exportImageButton);
        
        m_generateMatchButton = new JButton();
        m_generateMatchButton.setIcon(IconManager.getIcon(IconManager.IconType.FRAGMENTATION));
        m_generateMatchButton.setToolTipText("Generate & Store Spectrum Match");
        m_generateMatchButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                GenerateSpectrumMarchesDialog dialog = new GenerateSpectrumMarchesDialog(WindowManager.getDefault().getMainWindow());
                dialog.setVisible(true);
                if (dialog.getButtonClicked() == DefaultDialog.BUTTON_OK) {
                    Long frsId = dialog.getFragmentationRuleSetId();
                    Boolean forceGenerate = dialog.getDoForceGenerate();                    
                    generateSpectrumMatch(frsId, forceGenerate);
                }          
            }
        });        
        toolbar.add(m_generateMatchButton);
        
        return toolbar;
        
    }
    
    public void setData(DPeptideMatch peptideMatch, PeptideFragmentationData peptideFragmentationData) {
        
        if ( (peptideMatch == m_previousPeptideMatch) && ((peptideFragmentationData== null) ||  m_previousFragmentationSet) ) {
            return;
        }
        m_previousPeptideMatch = peptideMatch;
        m_previousFragmentationSet = (peptideFragmentationData!=null);
        
        
        // enable generateMatchButton when the user own the project
//        if (peptideFragmentationData == null) {
            // to execute this action, the user must be the owner of the project
        Project selectedProject = ProjectExplorerPanel.getProjectExplorerPanel().getSelectedProject();
        if (DatabaseDataManager.getDatabaseDataManager().ownProject(selectedProject)) {
            m_generateMatchButton.setEnabled(true);
        }
//        }else {
//             m_generateMatchButton.setEnabled(false);
//        }
//        
        
        m_chart.setNotify(false);
        constructSpectrumChart(peptideMatch);
        m_spectrumAnnotations = new RsetPeptideSpectrumAnnotations(m_dataBox, m_dataSet, m_chart, peptideMatch, peptideFragmentationData);
        
        // set default auto bounds in case there is no annotations (which sets new autobounds)
        m_chart.getXYPlot().getRangeAxis().setDefaultAutoRange(new Range(m_chart.getXYPlot().getRangeAxis().getLowerBound(),m_chart.getXYPlot().getRangeAxis().getUpperBound()));
        m_chart.getXYPlot().getDomainAxis().setDefaultAutoRange(new Range(m_chart.getXYPlot().getDomainAxis().getLowerBound(),m_chart.getXYPlot().getDomainAxis().getUpperBound()));
        m_spectrumAnnotations.addAnnotations();
        m_chart.setNotify(true);
    }


    
    private void generateSpectrumMatch(Long frsId, Boolean forceGenerateSM) {

        AbstractJMSCallback spectrumMatchCallback = new AbstractJMSCallback() {
            
            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }
            
            @Override
            public void run(boolean success) {
                if (success) {
                   ((DataBoxRsetPeptideSpectrum)m_dataBox).loadAnnotations(m_previousPeptideMatch);
                } else {
                    m_logger.error("Fail to generate spectrum matches for peptide_match.id=" + m_previousPeptideMatch.getId());
                }
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        };
        
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        GenerateSpectrumMatchTask task = new GenerateSpectrumMatchTask(spectrumMatchCallback, null, m_dataBox.getProjectId(), m_previousPeptideMatch.getResultSetId(), null, m_previousPeptideMatch.getId(), frsId, forceGenerateSM);
        AccessJMSManagerThread.getAccessJMSManagerThread().addTask(task);
        
    }
    
    private void constructSpectrumChart(DPeptideMatch pm) {

        // clear all data
        m_dataSet.removeSeries(SERIES_NAME);

 
        if (pm == null) {
            return;
        }
        
        Peptide p = pm.getPeptide();
        if (p == null) {
            return;
        }
        
        DMsQuery msQuery = pm.isMsQuerySet() ? pm.getMsQuery() : null;
        if (msQuery == null) {
            return;
        }
        
        DSpectrum spectrum = msQuery.isSpectrumFullySet() ? msQuery.getDSpectrum() : null;
        if (spectrum == null) {
            return;
        }


        
        byte[] intensityByteArray = spectrum.getIntensityList();
        byte[] massByteArray = spectrum.getMozList();
        
        
        if ((intensityByteArray == null) || (massByteArray == null)) {
            // should not happen
            return;
        }
        
        
        double precursorMass = spectrum.getPrecursorMoz()*spectrum.getPrecursorCharge(); // used for setting spectrum display range
		        
        
        ByteBuffer intensityByteBuffer = ByteBuffer.wrap(intensityByteArray).order(ByteOrder.LITTLE_ENDIAN);
        FloatBuffer intensityFloatBuffer = intensityByteBuffer.asFloatBuffer();
        double[] intensityDoubleArray = new double[intensityFloatBuffer.remaining()];
        for (int i = 0; i < intensityDoubleArray.length; i++) {
            intensityDoubleArray[i] = (double) intensityFloatBuffer.get();
        }
        
        ByteBuffer massByteBuffer = ByteBuffer.wrap(massByteArray).order(ByteOrder.LITTLE_ENDIAN);
        DoubleBuffer massDoubleBuffer = massByteBuffer.asDoubleBuffer();
        double[] massDoubleArray = new double[massDoubleBuffer.remaining()];
        for (int i = 0; i < massDoubleArray.length; i++) {
            massDoubleArray[i] = massDoubleBuffer.get();
        }
        
        
        int size = intensityDoubleArray.length;
        if (size != massDoubleArray.length) {
            LoggerFactory.getLogger("ProlineStudio.ResultExplorer").debug("Intensity and Mass List have different size");
            return;
        }
        
        double[][] data = new double[2][size];
        for (int i = 0; i < size; i++) {
            data[0][i] = massDoubleArray[i];
            data[1][i] = intensityDoubleArray[i];
        }
        m_dataSet.addSeries(SERIES_NAME, data);


        // Set title
        String title = "Query " + msQuery.getInitialId() + " - " + p.getSequence();
        m_chart.setTitle(title);

        // set the spectrum title
        m_spectrumTitle = spectrum.getTitle();

        
        // reset X/Y zooming
        // ((ChartPanel) m_spectrumPanel).restoreAutoBounds();
        ((ChartPanel) m_spectrumPanel).setBackground(Color.white);


        /// ---- start of plot listenener (for zoom changes for instance)
        final XYPlot plot = m_chart.getXYPlot();
        //p.setRenderer(renderer);

    	plot.getDomainAxis().setAutoRange(true);
		plot.getRangeAxis().setAutoRange(true);	
		m_spectrumMinX = 0; //plot.getDomainAxis().getLowerBound();
		m_spectrumMaxX = precursorMass;
		 //plot.getDomainAxis().getUpperBound();
		m_spectrumMinY = 0;//plot.getRangeAxis().getLowerBound();
		m_spectrumMaxY =  plot.getRangeAxis().getUpperBound();
		plot.getDomainAxis().setRange(new Range(0,m_spectrumMaxX), false, true); 
		plot.getRangeAxis().setRange(new Range(0,m_spectrumMaxY), false, true);
		plot.getDomainAxis().setDefaultAutoRange(new Range(0,m_spectrumMaxX)); // set new default zoom for x axis
		plot.getRangeAxis().setDefaultAutoRange(new Range(0,m_spectrumMaxY)); //           "              y axis		
        
        plot.addChangeListener(new PlotChangeListener() {
            
            @Override
            public void plotChanged(PlotChangeEvent arg0) {

                //Plot CHANGED (due to zoom for instance)

                double newMinX = plot.getDomainAxis().getLowerBound();
                double newMaxX = plot.getDomainAxis().getUpperBound();
                double newMinY = plot.getRangeAxis().getLowerBound();
                double newMaxY = plot.getRangeAxis().getUpperBound();

                // only if zoom change do the following:

                if (!redrawInProgress) {
                    if (newMinX != m_spectrumMinX
                            || newMaxX != m_spectrumMaxX
                            || newMinY != m_spectrumMinY
                            || newMaxY != m_spectrumMaxY) {
                        
                        redrawInProgress = true;
                        
                        m_spectrumMinX = newMinX;
                        m_spectrumMaxX = newMaxX;
                        m_spectrumMinY = newMinY;
                        m_spectrumMaxY = newMaxY;

                        plot.getDomainAxis().setLowerBound(newMinX);
                        plot.getDomainAxis().setUpperBound(newMaxX);
                        plot.getRangeAxis().setLowerBound(newMinY);
                        plot.getRangeAxis().setUpperBound(newMaxY);
                        m_spectrumAnnotations.addAnnotations();

                        //----
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
    public AbstractDataBox getDataBox() {
        return m_dataBox;
    }
    
    @Override
    public void addSingleValue(Object v) {
        // not used for the moment JPM.TODO ?
    }
    
    @Override
    public ActionListener getRemoveAction(SplittedPanelContainer splittedPanel) {
        return m_dataBox.getRemoveAction(splittedPanel);
    }
    
    @Override
    public ActionListener getAddAction(SplittedPanelContainer splittedPanel) {
        return m_dataBox.getAddAction(splittedPanel);
    }
    
    @Override
    public ActionListener getSaveAction(SplittedPanelContainer splittedPanel) {
        return m_dataBox.getSaveAction(splittedPanel);
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
    
    public static byte[] floatsToBytes(float[] floats) {

        // Convert float to a byte buffer
        ByteBuffer byteBuf = ByteBuffer.allocate(4 * floats.length).order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < floats.length; i++) {
            byteBuf.putFloat(floats[i]);
        }
        // Convert byte buffer into a byte array
        return byteBuf.array();
    }
}