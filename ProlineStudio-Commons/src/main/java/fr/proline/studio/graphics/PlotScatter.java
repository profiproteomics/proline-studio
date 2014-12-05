package fr.proline.studio.graphics;

import fr.proline.studio.comparedata.CompareDataInterface;
import fr.proline.studio.utils.CyclicColorPalette;
import java.awt.Graphics2D;
import java.util.HashSet;

/**
 * Histogram Plot
 * @author JM235353
 */
public class PlotScatter extends PlotAbstract {

    private double m_xMin;
    private double m_xMax;
    private double m_yMin;
    private double m_yMax;
    
    private double[] m_dataX;
    private double[] m_dataY;

    
    public PlotScatter(PlotPanel plotPanel, CompareDataInterface compareDataInterface, int colX, int colY) {
        super(plotPanel);
        update(compareDataInterface, colX, colY); 
    }

    public static HashSet<Class> getAcceptedXValues() {
        HashSet<Class> acceptedValues = new HashSet(2);
        acceptedValues.add(Double.class);
        acceptedValues.add(Float.class);
        acceptedValues.add(Integer.class);
        return acceptedValues;
    }

    public static HashSet<Class> getAcceptedYValues() {
        HashSet<Class> acceptedValues = new HashSet();
        return acceptedValues;
    }
    
    @Override
    public final void update(CompareDataInterface compareDataInterface, int colX, int colY) {

        int size = compareDataInterface.getRowCount();
        if (size == 0) {

            return;
        }

        m_dataX = new double[size];
        m_dataY = new double[size];
        
        for (int i = 0; i < size; i++) {
            Object value = compareDataInterface.getDataValueAt(i, colX);
            m_dataX[i] = (value == null || ! Number.class.isAssignableFrom(value.getClass())) ? Double.NaN : ((Number)value).doubleValue(); //CBy TODO : ne pas avoir a tester le type Number
            value = compareDataInterface.getDataValueAt(i, colY);
            m_dataY[i] = (value == null || ! Number.class.isAssignableFrom(value.getClass())) ? Double.NaN : ((Number)value).doubleValue(); //CBy TODO : ne pas avoir a tester le type Number
        }
        
        // min and max values
        double minX = m_dataX[0];
        double maxX = minX;
        double minY = m_dataY[0];
        double maxY = minY;
        for (int i = 1; i < size; i++) {
            double x = m_dataX[i];
            if (x < minX) {
                minX = x;
            } else if (x > maxX) {
                maxX = x;
            }
            double y = m_dataY[i];
            if (y < minY) {
                minY = y;
            } else if (y > maxY) {
                maxY = y;
            }
        }
        m_xMin = minX;
        m_xMax = maxX;
        m_yMin = minY;
        m_yMax = maxY;

        // we let margins
        
        double deltaX = (m_xMax-m_xMin);
        if (deltaX<=10e-10) {
            // no real delta
            m_xMin = m_xMin-1;  //JPM.TODO : enhance this
            m_xMax = m_xMax+1;
        } else {
            m_xMin =  m_xMin-deltaX*0.01;
            m_xMax = m_xMax+deltaX*0.01;
        }
        
        double deltaY = (m_yMax-m_yMin);
        if (deltaY<=10e-10) {
            // no real delta
            m_yMin = m_yMin-1;  //JPM.TODO : enhance this
            m_yMax = m_yMax+1;
        } else {
            m_yMin = m_yMin-deltaY*0.01;
            m_yMax = m_yMax+deltaY*0.01;
        }

        m_plotPanel.updateAxis(this);
        m_plotPanel.setXAxisTitle(compareDataInterface.getDataColumnIdentifier(colX));
        m_plotPanel.setYAxisTitle(compareDataInterface.getDataColumnIdentifier(colY));
        
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
        return m_yMin;
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

        int size = m_dataX.length;
        for (int i=0;i<size-1;i++) {
            int x = xAxis.valueToPixel( m_dataX[i]);
            int y = yAxis.valueToPixel( m_dataY[i]);

            g.setColor(CyclicColorPalette.getColor(21, 128));
            g.fillOval(x-3, y-3, 6, 6);
            
            

            
        }

        
        paintMarkers(g);
    }

    
    @Override
    public boolean needsDoubleBuffering() {
        return m_dataX.length>2000;
    }

    @Override
    public boolean needsXAxis() {
        return true;
    }

    @Override
    public boolean needsYAxis() {
        return true;
    }
    
}
