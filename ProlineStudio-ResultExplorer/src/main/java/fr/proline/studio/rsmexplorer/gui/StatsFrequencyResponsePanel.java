package fr.proline.studio.rsmexplorer.gui;

import fr.proline.studio.graphics.PlotHistogram;
import fr.proline.studio.graphics.PlotPanel;
import fr.proline.studio.gui.HourglassPanel;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.DataBoxPanelInterface;
import fr.proline.studio.stats.ValuesForStatsAbstract;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import javax.swing.JPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;

//import hep.aida.bin.StaticBin1D;
//import hep.aida.ref.Histogram1D;

/**
 *
 * @author JM235353
 */
public class StatsFrequencyResponsePanel extends HourglassPanel implements DataBoxPanelInterface {

    
    private AbstractDataBox m_dataBox;
    
    //private HistogramDataset m_dataSet;
    //private JFreeChart m_chart;
    //private ChartPanel m_statPanel;
    
    private PlotPanel m_plotPanel;
    
    public StatsFrequencyResponsePanel() {
        

        setLayout(new GridBagLayout());
        
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        JPanel statPanel = createStatPanel();
        
        c.weightx = 1;
        c.weighty = 1;
        add(statPanel, c);

    }
 
    
    private JPanel createStatPanel() {
        
        m_plotPanel = new PlotPanel();
        return m_plotPanel;
    }
    
    /*private JPanel createStatPanel() {

        m_dataSet = new HistogramDataset();
        m_dataSet.setType(HistogramType.RELATIVE_FREQUENCY);

        m_chart = ChartFactory.createHistogram( "Histogram", "Delta", "%", 
        m_dataSet,  PlotOrientation.VERTICAL, true, true, false); //ChartFactory.createXYLineChart("", "m", "%", m_dataSet, PlotOrientation.VERTICAL, true, true, false);

        //m_chart.removeLegend();
        m_chart.setBackgroundPaint(Color.white);

        XYPlot plot = (XYPlot) m_chart.getPlot();
        //plot.getRangeAxis().setUpperMargin(0.2);
        plot.setBackgroundPaint(Color.white);

        
        RsetPeptideSpectrumPanel.XYStickRenderer renderer = new RsetPeptideSpectrumPanel.XYStickRenderer();
        renderer.setBaseStroke(new BasicStroke(1.0f));
        plot.setRenderer(renderer);

        XYBarRenderer xybarrenderer = (XYBarRenderer)plot.getRenderer();
    xybarrenderer.setShadowVisible(false);
    xybarrenderer.setBarPainter(new StandardXYBarPainter());
        
        m_statPanel = new ChartPanel(m_chart, true);

 
        return m_statPanel;
    }*/
    
    public void setData(ValuesForStatsAbstract values) {

       // constructSpectrumChart(values);

        PlotHistogram histogram = new PlotHistogram(m_plotPanel, values);
        
        m_plotPanel.addPlot(histogram);
        
    }
    
        
    private void constructSpectrumChart(ValuesForStatsAbstract values) {
/*
        final String SERIES_NAME = "VALUES";
        if (values == null) {
            
            m_dataSet.addSeries(SERIES_NAME, null, 0);
           
            return;
        }
*/
        
        //StaticBin1D stats = new StaticBin1D();
        
        /*
        double[] data = new double[values.size()];
        for (int i = 0; i < data.length; i++) {
            stats.add(values.getValue(i));
        }
        
        plotter.destroyRegions();
		plotter.createRegion();
		if (stats.min() < stats.max()) {
			frequencies = getFrequencies(values, stats.min(), stats.max(), stats.standardDeviation(),title);
			plotter.currentRegion().plot(frequencies);
		}
        
        
        */
        
        
        
        
        // calculate histogram
        
        // number of bins
       /* int size = values.size();
        if (size == 0) {
             m_dataSet.addSeries(SERIES_NAME, null, 0);
  
            return;
        }

        // min and max values
        double min = values.getValue(0);
        double max = values.getValue(0);
        for (int i=1;i<size;i++) {
            double v = values.getValue(i);
            if (v<min) {
                min = v;
            } else if (v>max) {
                max = v;
            }
        }
        
        // bins
        double std = values.standardDeviation();
        int bins = (int) Math.round((max-min)/(3.5*std*Math.pow(size, -1/3.0)));
        
        double[] data = new double[values.size()];
        for (int i=0;i<data.length;i++) {
            data[i] = values.getValue(i);
        }
        
        
        m_dataSet.addSeries(SERIES_NAME, data, bins, min, max);
        */
        
        /*double delta = max-min;
        double[] histogram = new double[bins];

        for (int i=0;i<size;i++) {
            double v = values.getValue(i);
            int index = (int) (((v-min)/delta)*(bins));
            if (index>=bins) {
                index = bins-1;
            }
            histogram[index]++;
        }
        for (int i=0;i<bins;i++) {
            histogram[i] = histogram[i]/size*100;
        }*/
        
        

        /*double[][] data = new double[2][bins];
        double binDelta = delta/bins;
        for (int i = 0; i < bins; i++) {
            data[0][i] = min+i*binDelta+binDelta/2;
            data[1][i] = histogram[i];
        }*/
        
       // m_dataSet.addSeries(SERIES_NAME, histogram, bins, min, max);


        // Set title
        //String title = "Query " + pm.getMsQuery().getInitialId() + " - " + pm.getPeptide().getSequence();
        //m_chart.setTitle(title);

        // reset X/Y zooming
             //  m_statPanel.restoreAutoBounds();
        //((ChartPanel) spectrumPanel).setBackground(Color.white);
/*
        XYPlot plot = (XYPlot) m_chart.getPlot();
        plot.getDomainAxis().setRange(min, max);
            
        m_chart.setBorderVisible(true);*/
        
        //plot.getRangeAxis().setRange(0, 15);
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


    
}
