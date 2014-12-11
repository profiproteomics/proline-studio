package fr.proline.studio.graphics;

import fr.proline.studio.comparedata.CompareDataInterface;
import fr.proline.studio.utils.CyclicColorPalette;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.geom.Path2D;
import java.util.ArrayList;
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
    private boolean[] m_selected;

    private static final int SELECT_SENSIBILITY = 8;
    
    public PlotScatter(PlotPanel plotPanel, CompareDataInterface compareDataInterface, CrossSelectionInterface crossSelectionInterface, int colX, int colY) {
        super(plotPanel, compareDataInterface, crossSelectionInterface);
        update(colX, colY); 
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
    public boolean select(double x, double y, boolean append) {
        
        double rangeX = m_xMax-m_xMin;
        double rangeY = m_yMax-m_yMin;
        
        double distanceMin = Double.MAX_VALUE;
        int nearestDataIndex = -1;
        int size = m_dataX.length;
        for (int i = size-1; i >=0 ; i--) { // reverse loop to select first the data in foreground
            double dataX = m_dataX[i];
            double dataY = m_dataY[i];
        
            double normalizedDistanceX = (rangeX<=10e-10) ? 0 : (x-dataX)/rangeX;
            if (normalizedDistanceX<0) {
                normalizedDistanceX = -normalizedDistanceX;
            }
            
            double normalizedDistanceY = (rangeY<=10e-10) ? 0 : (y-dataY)/rangeY;
            if (normalizedDistanceY<0) {
                normalizedDistanceY = -normalizedDistanceY;
            }
            
            double squaredDistance = normalizedDistanceX*normalizedDistanceX+normalizedDistanceY*normalizedDistanceY;
            if (distanceMin>squaredDistance) {
                distanceMin = squaredDistance;
                nearestDataIndex = i;
            }
            
            if (!append) {
                m_selected[i] = false;
            }
        }

        
        
        if (nearestDataIndex != -1) {
            

            if (Math.abs(m_plotPanel.getXAxis().valueToPixel(x)-m_plotPanel.getXAxis().valueToPixel( m_dataX[nearestDataIndex]))>SELECT_SENSIBILITY) {
                return false;
            }
            if (Math.abs(m_plotPanel.getYAxis().valueToPixel(y)-m_plotPanel.getYAxis().valueToPixel( m_dataY[nearestDataIndex]))>SELECT_SENSIBILITY) {
                return false;
            }
            
            m_selected[nearestDataIndex] = true;
            
            return true;
        }

        return false;
    }
    
    @Override
    public boolean select(Path2D.Double path, double minX, double maxX, double minY, double maxY, boolean append) {

        boolean aSelection = false;
        int size = m_dataX.length;
        for (int i = 0; i < size; i++) {

            double dataX = m_dataX[i];
            double dataY = m_dataY[i];
            if ((dataX < minX) || (dataX > maxX) || (dataY < minY) || (dataY > maxY)) {
                if (!append) {
                    m_selected[i] = false;
                }
            } else if (path.contains(dataX, dataY)) {
                m_selected[i] = true;
                aSelection = true;
            } else {
                if (!append) {
                    m_selected[i] = false;
                }
            }

        }
        
        return aSelection;
    }
    
    @Override
    public ArrayList<Integer> getSelection() {
        ArrayList<Integer> selection = new ArrayList();
        for (int i = 0; i < m_selected.length; i++) {
            if (m_selected[i]) {
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
             m_selected[selection.get(i)] = true; 
        }
    }
    

    
    @Override
    public final void update() {

        int size = m_compareDataInterface.getRowCount();
        if (size == 0) {

            return;
        }

        m_dataX = new double[size];
        m_dataY = new double[size];
        m_selected = new boolean[size];
        
        for (int i = 0; i < size; i++) {
            Object value = m_compareDataInterface.getDataValueAt(i, m_colX);
            m_dataX[i] = (value == null || ! Number.class.isAssignableFrom(value.getClass())) ? Double.NaN : ((Number)value).doubleValue(); //CBy TODO : ne pas avoir a tester le type Number
            value = m_compareDataInterface.getDataValueAt(i, m_colY);
            m_dataY[i] = (value == null || ! Number.class.isAssignableFrom(value.getClass())) ? Double.NaN : ((Number)value).doubleValue(); //CBy TODO : ne pas avoir a tester le type Number
            m_selected[i] = false;
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
        m_plotPanel.setXAxisTitle(m_compareDataInterface.getDataColumnIdentifier(m_colX));
        m_plotPanel.setYAxisTitle(m_compareDataInterface.getDataColumnIdentifier(m_colY));
        
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

        // first plot non selected
        int size = m_dataX.length;
        for (int i=0;i<size-1;i++) {
            if (m_selected[i]) {
                continue;
            }
            int x = xAxis.valueToPixel( m_dataX[i]);
            int y = yAxis.valueToPixel( m_dataY[i]);

            g.setColor(CyclicColorPalette.getColor(21, 128));
            g.fillOval(x-3, y-3, 6, 6);

        }
        
        // plot selected
        for (int i=0;i<size-1;i++) {
            if (!m_selected[i]) {
                continue;
            }
            int x = xAxis.valueToPixel( m_dataX[i]);
            int y = yAxis.valueToPixel( m_dataY[i]);

            g.setColor(Color.red);
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
