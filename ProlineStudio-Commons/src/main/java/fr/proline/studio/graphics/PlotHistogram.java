package fr.proline.studio.graphics;

import fr.proline.studio.comparedata.CompareDataInterface;
import fr.proline.studio.comparedata.StatsModel;
import fr.proline.studio.graphics.marker.LabelMarker;
import fr.proline.studio.graphics.marker.LineMarker;
import fr.proline.studio.graphics.marker.TextMarker;
import fr.proline.studio.graphics.marker.XDeltaMarker;
import fr.proline.studio.parameter.ColorParameter;
import fr.proline.studio.parameter.IntegerParameter;
import fr.proline.studio.parameter.ParameterList;
import fr.proline.studio.utils.CyclicColorPalette;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.util.ArrayList;


import java.util.HashSet;
import javax.swing.JSlider;

/**
 * Histogram Plot
 * @author JM235353
 */
public class PlotHistogram extends PlotAbstract {

    private double m_xMin;
    private double m_xMax;
    private double m_yMax;
    
    private double[] m_dataX;
    private double[] m_dataY;
    private int[] m_dataCountY;
    private boolean[] m_selected;

    private StatsModel m_values;
    private int m_bins;
    
    private final ColorParameter m_colorParameter;
    private final IntegerParameter m_binsParameter;
    private ArrayList<ParameterList> m_parameterListArray = null;
    
    private static final String PLOT_HISTOGRAM_COLOR_KEY = "PLOT_HISTOGRAM_COLOR";
    private static final String PLOT_HISTOGRAM_BIN_KEY = "PLOT_HISTOGRAM_BIN";
    
    public static String HISTOGRAM_COUNT = "Count";
    public static String HISTOGRAM_PERCENTAGE = "Percentage %";
    
    private boolean m_asPercentage = false;
    
    public PlotHistogram(PlotPanel plotPanel, CompareDataInterface compareDataInterface, CrossSelectionInterface crossSelectionInterface, int colX, String paramZ) {
        super(plotPanel, PlotType.HISTOGRAM_PLOT, compareDataInterface, crossSelectionInterface);
        update(colX, -1, paramZ); 
        
        
        ParameterList parameterColorList = new ParameterList("Colors");
        Color histogramColor = CyclicColorPalette.getColor(21);
        m_colorParameter = new ColorParameter(PLOT_HISTOGRAM_COLOR_KEY, "Histogram Plot Color", histogramColor);
        parameterColorList.add(m_colorParameter);
        
        ParameterList parameterSettingsList = new ParameterList("Settings");
        m_binsParameter = new IntegerParameter(PLOT_HISTOGRAM_BIN_KEY, "Bins", JSlider.class, m_bins, 5, 100);
        parameterSettingsList.add(m_binsParameter);
        
        m_parameterListArray = new ArrayList<>(2);
        m_parameterListArray.add(parameterColorList);
        m_parameterListArray.add(parameterSettingsList);
        
    }



    public static HashSet<Class> getAcceptedYValues() {
        HashSet<Class> acceptedValues = new HashSet();
        return acceptedValues;
    }
    
    @Override
    public ArrayList<ParameterList> getParameters() {
        return m_parameterListArray;
    }
    
    @Override
    public boolean select(double x, double y, boolean append) {

        double y2 = 0;
        int size = m_dataX.length;
        for (int i=0;i<size-1;i++) {
            double x1 = m_dataX[i];
            double x2 = m_dataX[i+1];
            double y1 = m_asPercentage ? m_dataY[i] : m_dataCountY[i];
            
            if ((x>=x1) && (x<=x2) && (y>=y2) && (y<=y1)) {
                m_selected[i] = true;
            } else {
                if (!append) {
                    m_selected[i] = false;
                }
            }            
        }
        
        return true;
    }
    
    @Override
    public boolean select(Path2D.Double path, double minX, double maxX, double minY, double maxY, boolean append) {

        double y2 = 0;
        int size = m_dataX.length;
        for (int i=0;i<size-1;i++) {
            double x1 = m_dataX[i];
            double x2 = m_dataX[i+1];
            double y1 = m_asPercentage ? m_dataY[i] : m_dataCountY[i];

            if (path.intersects(x1, y2, x2-x1, y1-y2)) {
                m_selected[i] = true;
            } else {
                if (!append) {
                    m_selected[i] = false;
                }
            }            
        }
        
        return false;
    }
    
    private int findPoint(double x, double y) {
        double y2 = 0;
        int size = m_dataX.length;
        for (int i=0;i<size-1;i++) {
            double x1 = m_dataX[i];
            double x2 = m_dataX[i+1];
            double y1 = m_asPercentage ? m_dataY[i] : m_dataCountY[i];
            
            if ((x>=x1) && (x<=x2) && (y>=y2) && (y<=y1)) {
                return i;
            }           
        }
        
        return -1;
    }
    
    @Override
    public ArrayList<Integer> getSelection() {
        
        ArrayList<Integer> selection = new ArrayList();
        
        int size = m_values.getRowCount();
        for (int i=0;i<size;i++) {
            double v = m_values.getValue(i);
            int index = (int) (((v - m_xMin) / (m_xMax-m_xMin)) * (m_bins));
            if (index >= m_bins) {
                index = m_bins - 1;
            }
            if (m_selected[index]) {
                selection.add(i);
            }
        }

        return selection;
    }
    
    @Override
    public void setSelection(ArrayList<Integer> selection) {
        for (int i = 0; i < m_selected.length; i++) {
            m_selected[i] = false;
        }

        for (int i = 0; i < selection.size(); i++) {
            double v = m_values.getValue(selection.get(i));
            int index = (int) (((v - m_xMin) / (m_xMax - m_xMin)) * (m_bins));
            if (index >= m_bins) {
                index = m_bins - 1;
            }
            m_selected[index] = true;
        }
    }
    
        @Override
    public String getToolTipText(double x, double y) {
        int indexFound = findPoint(x, y);
        if (indexFound == -1) {
            return null;
        }
        if (m_sb == null) {
            m_sb = new StringBuilder();
        }
        m_sb.append("<HTML>");
        m_sb.append(m_plotPanel.getXAxis().getTitle());
        m_sb.append(" : ");
        m_sb.append(m_plotPanel.getXAxis().getExternalDecimalFormat().format(m_dataX[indexFound]));
        m_sb.append(" to ");
        m_sb.append(m_plotPanel.getXAxis().getExternalDecimalFormat().format(m_dataX[indexFound+1]));
        m_sb.append("<BR>");
        m_sb.append("Percentage : ");
        m_sb.append(m_plotPanel.getYAxis().getExternalDecimalFormat().format(m_dataY[indexFound]));
        m_sb.append(" % ");
        m_sb.append("<BR>");
        m_sb.append("Count : ");
        m_sb.append(m_dataCountY[indexFound]);
        m_sb.append("</HTML>");
        String tooltip = m_sb.toString();
        m_sb.setLength(0);
        return tooltip;
 
    }
    private StringBuilder m_sb = null;
    
    @Override
    public void parametersChanged() {
        m_bins = ((Integer)m_binsParameter.getObjectValue());
        updataDataAccordingToBins();
        m_plotPanel.updateAxis(this);
    }
    
    @Override
    public final void update() {
         
        m_values = new StatsModel(m_compareDataInterface, m_colX);
        
        m_asPercentage = (m_parameterZ.compareTo(HISTOGRAM_PERCENTAGE) == 0);
        
        clearMarkers();
        
        // number of bins
        int size = m_values.getRowCount();
        if (size == 0) {

            return;
        }

        // min and max values
        double min = m_values.getValue(0);
        int i = 1;
        while (min != min) { // NaN values
            if (i>=size) {
                break;
            }
            min = m_values.getValue(i);
            i++;
        }
        double max = min;
        for (; i < size; i++) {
            double v = m_values.getValue(i);
            if (v < min) {
                min = v;
            } else if (v > max) {
                max = v;
            }
        }
        m_xMin = min;
        m_xMax = max;
        
        double yStdevLabel = m_yMax*0.1;
        double yMeanLabel = m_yMax*1.1;
        m_yMax *= 1.2; // we let place at the top to be able to put information
        
        m_plotPanel.updateAxis(this);
        m_plotPanel.setXAxisTitle(m_compareDataInterface.getDataColumnIdentifier(m_colX));
        m_plotPanel.setYAxisTitle(m_asPercentage ? HISTOGRAM_PERCENTAGE : HISTOGRAM_COUNT);
        
        // bins
        double std = m_values.standardDeviationNaN();
        m_bins = (int) Math.round((max-min)/(3.5*std*Math.pow(size, -1/3.0)));
        if (m_bins<10) {
            m_bins = 10;
        }

        
        updataDataAccordingToBins();



        // add Stdev value
        double mean = m_values.meanNaN();
        addMarker(new XDeltaMarker(m_plotPanel, mean, mean+std, yStdevLabel));
        addMarker(new LineMarker(m_plotPanel, mean+std, LineMarker.ORIENTATION_VERTICAL));
        
        addMarker(new XDeltaMarker(m_plotPanel, mean, mean-std, yStdevLabel));
        addMarker(new LineMarker(m_plotPanel, mean-std, LineMarker.ORIENTATION_VERTICAL));
        
        addMarker(new LabelMarker(m_plotPanel, mean+std/2, yStdevLabel, "Stdev : "+std, LabelMarker.ORIENTATION_X_RIGHT, LabelMarker.ORIENTATION_Y_TOP));
        
        
        // add Mean value
        addMarker(new LineMarker(m_plotPanel, mean, LineMarker.ORIENTATION_VERTICAL));
        addMarker(new LabelMarker(m_plotPanel, mean, yMeanLabel, "Mean : "+mean, LabelMarker.ORIENTATION_X_RIGHT, LabelMarker.ORIENTATION_Y_BOTTOM));
        
        // add Title
        addMarker(new TextMarker(m_plotPanel, 0.05, 0.95, m_values.getDataColumnIdentifier(0) +" Histogram"));
        
        m_plotPanel.repaint();
    }
    
    private void updataDataAccordingToBins() {
        
        int size = m_values.getRowCount();
        
        double[] data = new double[m_values.getRowCount()];
        for (int i=0;i<data.length;i++) {
            data[i] = m_values.getValue(i);
        }
        
        double delta = m_xMax-m_xMin;
        m_dataCountY = new int[m_bins];
        double[] histogram = new double[m_bins];
        for (int i = 0; i < size; i++) {
            double v = m_values.getValue(i);
            if (v != v) {
                continue; // remove NaN values
            }
            int index = (int) (((v - m_xMin) / delta) * (m_bins));
            if (index >= m_bins) {
                index = m_bins - 1;
            }

            m_dataCountY[index]++;
        }
        
        m_yMax = 0;
        for (int i = 0; i < m_bins; i++) {
            double percentage = ((double)m_dataCountY[i]) / size * 100;
            histogram[i] = percentage;
            double y = (m_asPercentage) ? percentage : m_dataCountY[i];
            
            if (y > m_yMax) {
                m_yMax = y;
            }
        }
        
        
        m_dataX = new double[m_bins + 1];
        m_dataY = new double[m_bins + 1];
        m_selected = new boolean[m_bins + 1];
        double binDelta = delta / m_bins;
        for (int i = 0; i < m_bins; i++) {
            m_dataX[i] = m_xMin + i * binDelta;
            m_dataY[i] = histogram[i];
            m_selected[i] = false;
        }
        m_dataX[m_bins] = m_dataX[m_bins - 1] + binDelta;
        m_dataY[m_bins] = m_dataY[m_bins - 1];
        m_selected[m_bins] = false;

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
        
        // set clipping area
        int clipX = xAxis.valueToPixel(xAxis.getMinTick());
        int clipWidth = xAxis.valueToPixel(xAxis.getMaxTick())-clipX;
        int clipY = yAxis.valueToPixel(yAxis.getMaxTick());
        int clipHeight = yAxis.valueToPixel(yAxis.getMinTick())-clipY;
        g.setClip(clipX, clipY, clipWidth, clipHeight);
        
        int y2 = yAxis.valueToPixel(0);
        int size = m_dataX.length;
        for (int i=0;i<size-1;i++) {
            int x1 = xAxis.valueToPixel( m_dataX[i]);
            int x2 = xAxis.valueToPixel( m_dataX[i+1]);
            int y1 = yAxis.valueToPixel( m_asPercentage ? m_dataY[i] : m_dataCountY[i]);
            
            if (m_selected[i]) {
                g.setColor(CyclicColorPalette.getColor(5));
            } else {
                g.setColor(m_colorParameter.getColor());
            }
            g.fillRect(x1, y1 , x2-x1, y2-y1);
            
            g.setColor(Color.black);
            g.drawRect(x1, y1 , x2-x1, y2-y1);
            
        }
        
        paintMarkers(g);
    }


    @Override
    public boolean needsXAxis() {
        return true;
    }

    @Override
    public boolean needsYAxis() {
        return true;
    }
    
    @Override
    public boolean isMouseOnPlot(double x, double y) {
        return findPoint(x, y) != -1;
    }
    
}
