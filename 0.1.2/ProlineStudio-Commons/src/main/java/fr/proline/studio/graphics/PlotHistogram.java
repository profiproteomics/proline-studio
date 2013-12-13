package fr.proline.studio.graphics;

import fr.proline.studio.graphics.marker.LabelMarker;
import fr.proline.studio.graphics.marker.LineMarker;
import fr.proline.studio.graphics.marker.TextMarker;
import fr.proline.studio.graphics.marker.XDeltaMarker;
import fr.proline.studio.stats.ValuesForStatsAbstract;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 *
 * @author JM235353
 */
public class PlotHistogram extends PlotAbstract {

    private double m_xMin;
    private double m_xMax;
    private double m_yMax;
    
    private double[] m_dataX;
    private double[] m_dataY;

    
    public PlotHistogram(PlotPanel plotPanel, ValuesForStatsAbstract values) {
        
        super(plotPanel);
       
        update(values);
        
    }
    
    public final void update(ValuesForStatsAbstract values) {
         
        clearMarkers();
        
        // number of bins
        int size = values.size();
        if (size == 0) {

            return;
        }

        // min and max values
        double min = values.getValue(0);
        double max = values.getValue(0);
        for (int i = 1; i < size; i++) {
            double v = values.getValue(i);
            if (v < min) {
                min = v;
            } else if (v > max) {
                max = v;
            }
        }
        m_xMin = min;
        m_xMax = max;
        
        // bins
        double std = values.standardDeviation();
        int bins = (int) Math.round((max-min)/(3.5*std*Math.pow(size, -1/3.0)));
        
        double[] data = new double[values.size()];
        for (int i=0;i<data.length;i++) {
            data[i] = values.getValue(i);
        }
        
        double delta = max-min;
        double[] histogram = new double[bins];
        for (int i = 0; i < size; i++) {
            double v = values.getValue(i);
            int index = (int) (((v - min) / delta) * (bins));
            if (index >= bins) {
                index = bins - 1;
            }
            histogram[index]++;
        }
        
        m_yMax = 0;
        for (int i = 0; i < bins; i++) {
            double y = histogram[i] / size * 100;
            histogram[i] = y;
            if (y > m_yMax) {
                m_yMax = y;
            }
        }
        double yStdevLabel = m_yMax*0.1;
        double yMeanLabel = m_yMax*1.1;
        m_yMax *= 1.2; // we let place at the top to be able to put information
        
        m_plotPanel.updateAxis(this);
        
        m_dataX = new double[bins + 1];
        m_dataY = new double[bins + 1];
        double binDelta = delta / bins;
        for (int i = 0; i < bins; i++) {
            m_dataX[i] = min + i * binDelta;
            m_dataY[i] = histogram[i];
        }
        m_dataX[bins] = m_dataX[bins - 1] + binDelta;
        m_dataY[bins] = m_dataY[bins - 1];


        // add Stdev value
        double mean = values.mean();
        addMarker(new XDeltaMarker(m_plotPanel, mean, mean+std, yStdevLabel));
        addMarker(new LineMarker(m_plotPanel, mean+std, LineMarker.ORIENTATION_VERTICAL));
        
        addMarker(new XDeltaMarker(m_plotPanel, mean, mean-std, yStdevLabel));
        addMarker(new LineMarker(m_plotPanel, mean-std, LineMarker.ORIENTATION_VERTICAL));
        
        addMarker(new LabelMarker(m_plotPanel, mean+std/2, yStdevLabel, "Stdev : "+std, LabelMarker.ORIENTATION_X_RIGHT, LabelMarker.ORIENTATION_Y_TOP));
        
        
        // add Mean value
        addMarker(new LineMarker(m_plotPanel, mean, LineMarker.ORIENTATION_VERTICAL));
        addMarker(new LabelMarker(m_plotPanel, mean, yMeanLabel, "Mean : "+mean, LabelMarker.ORIENTATION_X_RIGHT, LabelMarker.ORIENTATION_Y_BOTTOM));
        
        // add Title
        addMarker(new TextMarker(m_plotPanel, 0.05, 0.95, values.getValueType()+" Histogram"));
        
        m_plotPanel.repaint();
    }
    
    
    @Override
    public double getXMin() {
        return m_xMin;
    }

    @Override
    public double getXMax() {
        return m_xMax;
    }

    @Override
    public double getYMin() {
        return 0;
    }

    @Override
    public double getYMax() {
        return m_yMax;
    }

    @Override
    public void paint(Graphics2D g) {
        
        
        
        XAxis xAxis = m_plotPanel.getXAxis();
        YAxis yAxis = m_plotPanel.getYAxis(); 
        
        int y2 = yAxis.valueToPixel(0);
        int size = m_dataX.length;
        for (int i=0;i<size-1;i++) {
            int x1 = xAxis.valueToPixel( m_dataX[i]);
            int x2 = xAxis.valueToPixel( m_dataX[i+1]);
            int y1 = yAxis.valueToPixel( m_dataY[i]);
            
            
            
            g.setColor(COLOR_MARS_RED);
            g.fillRect(x1, y1 , x2-x1, y2-y1);
            
            g.setColor(Color.black);
            g.drawRect(x1, y1 , x2-x1, y2-y1);
            
        }
        
        paintMarkers(g);
    }
    private final static Color COLOR_MARS_RED = new Color(227,38,54);



    @Override
    public boolean needsXAxis() {
        return true;
    }

    @Override
    public boolean needsYAxis() {
        return true;
    }
    
}
