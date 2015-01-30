package fr.proline.studio.graphics;

import fr.proline.studio.comparedata.CompareDataInterface;
import fr.proline.studio.parameter.ColorOrGradientParameter;
import fr.proline.studio.parameter.IntegerParameter;
import fr.proline.studio.parameter.ParameterList;
import fr.proline.studio.utils.CyclicColorPalette;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.LinearGradientPaint;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import javax.swing.JSlider;

/**
 * Scatter Plot
 * @author JM235353
 */
public class PlotScatter extends PlotAbstract implements Axis.EnumXInterface, Axis.EnumYInterface {

    private double m_xMin;
    private double m_xMax;
    private double m_yMin;
    private double m_yMax;
    
    private double[] m_dataX;
    private double[] m_dataY;
    private int[] m_jitterX;
    private int[] m_jitterY;
    
    private double m_gradientParamValuesMin;
    private double m_gradientParamValuesMax;
    private double[] m_gradientParamValues;
    private int m_gradientParamCol = -1;
    private Color[] m_gradientColors;
    private float[] m_gradientFractions;
    
    
    private boolean[] m_selected;

    private static final int SELECT_SENSIBILITY = 8;
    
    private static final String PLOT_SCATTER_COLOR_KEY = "PLOT_SCATTER_COLOR";
    private static final String PLOT_SCATTER_X_JITTER_KEY = "PLOT_SCATTER_X_JITTER";
    private static final String PLOT_SCATTER_Y_JITTER_KEY = "PLOT_SCATTER_Y_JITTER";
    
    private ColorOrGradientParameter m_colorParameter;
    private IntegerParameter m_jitterXParameter;
    private IntegerParameter m_jitterYParameter;
    
    private ArrayList<ParameterList> m_parameterListArray = null;
    
    public PlotScatter(PlotPanel plotPanel, CompareDataInterface compareDataInterface, CrossSelectionInterface crossSelectionInterface, int colX, int colY) {
        super(plotPanel, PlotType.SCATTER_PLOT, compareDataInterface, crossSelectionInterface);
        update(colX, colY, null); 
        
        // Color parameter
        ParameterList colorParameteList = new ParameterList("Colors");
       
        ColorOrGradient colorOrGradient = new ColorOrGradient();
        colorOrGradient.setColor(CyclicColorPalette.getColor(21, 128));
        float[] fractions = {0.0f, 1.0f};
        Color[] colors = {Color.white, Color.red};
        LinearGradientPaint gradient = ColorOrGradientChooserPanel.getGradientForPanel(colors, fractions);
        colorOrGradient.setGradient(gradient);

        m_colorParameter = new ColorOrGradientParameter(PLOT_SCATTER_COLOR_KEY, "Scatter Plot Color", colorOrGradient, null);
        colorParameteList.add(m_colorParameter);
        
        // Jitter parameter
        ParameterList settingsParameterList = new ParameterList("Settings");
        
        m_jitterXParameter = new IntegerParameter(PLOT_SCATTER_X_JITTER_KEY, "X Jitter", JSlider.class, 0, 0, 20);
        m_jitterYParameter = new IntegerParameter(PLOT_SCATTER_Y_JITTER_KEY, "Y Jitter", JSlider.class, 0, 0, 20);
        settingsParameterList.add(m_jitterXParameter);
        settingsParameterList.add(m_jitterYParameter);
        
        
        m_parameterListArray = new  ArrayList<>(2);
        m_parameterListArray.add(colorParameteList);
        m_parameterListArray.add(settingsParameterList);
    }

    
    @Override
    public ArrayList<ParameterList> getParameters() {

        // update parameters
        ArrayList<ReferenceIdName> m_potentialGradientParamArray = new ArrayList<>();
        HashSet<Class> acceptedValues = m_plotType.getAcceptedValuesAsParam();
        int nbCol = m_compareDataInterface.getColumnCount();
        for (int i = 0; i < nbCol; i++) {
            Class c = m_compareDataInterface.getDataColumnClass(i);
            if (acceptedValues.contains(c)) {
                ReferenceIdName ref = new ReferenceIdName(m_compareDataInterface.getDataColumnIdentifier(i), i);
                m_potentialGradientParamArray.add(ref);
            }
        }

        m_colorParameter.setGradientParam(m_potentialGradientParamArray);


        
        return m_parameterListArray;
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

        m_sb.append(m_compareDataInterface.getDataValueAt(indexFound, m_compareDataInterface.getInfoColumn()).toString());
        m_sb.append("<BR>");
        m_sb.append(m_plotPanel.getXAxis().getTitle());
        m_sb.append(" : ");
        boolean xAsEnum = m_plotPanel.getXAxis().isEnum();
        if (xAsEnum) {
            m_sb.append(getEnumValueX(indexFound));
        } else {
            m_sb.append(m_plotPanel.getXAxis().getExternalDecimalFormat().format(m_dataX[indexFound]));
        }
        m_sb.append("<BR>");
        m_sb.append(m_plotPanel.getYAxis().getTitle());
        m_sb.append(" : ");
        boolean yAsEnum = m_plotPanel.getXAxis().isEnum();
        if (yAsEnum) {
            m_sb.append(getEnumValueY(indexFound));
        } else {
            m_sb.append(m_plotPanel.getYAxis().getExternalDecimalFormat().format(m_dataY[indexFound]));
        }

        String tooltip = m_sb.toString();
        m_sb.setLength(0);
        return tooltip;
 
    }
    private StringBuilder m_sb = null;
    
    @Override
    public boolean select(double x, double y, boolean append) {
        
        int indexFound = findPoint(x, y);

        int size = m_dataX.length;
        if (!append) {
            for (int i = 0; i < size; i++) {
                m_selected[i] = false;
            }
        }

        if (indexFound != -1) {

            m_selected[indexFound] = true;
            
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
    

    private int findPoint(double x, double y) {

        double rangeX = m_xMax - m_xMin;
        double rangeY = m_yMax - m_yMin;

        double distanceMin = Double.MAX_VALUE;
        int nearestDataIndex = -1;
        int size = m_dataX.length;
        for (int i = size - 1; i >= 0; i--) { // reverse loop to select first the data in foreground
            double dataX = m_dataX[i];
            double dataY = m_dataY[i];
        
            
            // take in account jitter
            if (m_jitterX != null) {
                XAxis xAxis = m_plotPanel.getXAxis();
                int xWithJitter = xAxis.valueToPixel(dataX) + m_jitterX[i];
                dataX = xAxis.pixelToValue(xWithJitter);
            }
            if (m_jitterY != null) {
                YAxis yAxis = m_plotPanel.getYAxis();
                int yWithJitter = yAxis.valueToPixel(dataY) + m_jitterY[i];
                dataY = yAxis.pixelToValue(yWithJitter);
            }

            
            double normalizedDistanceX = (rangeX<=10e-10) ? 0 : (x-dataX)/rangeX;
            if (normalizedDistanceX<0) {
                normalizedDistanceX = -normalizedDistanceX;
            }

            double normalizedDistanceY = (rangeY <= 10e-10) ? 0 : (y - dataY) / rangeY;
            if (normalizedDistanceY < 0) {
                normalizedDistanceY = -normalizedDistanceY;
            }

            double squaredDistance = normalizedDistanceX * normalizedDistanceX + normalizedDistanceY * normalizedDistanceY;
            if (distanceMin > squaredDistance) {
                distanceMin = squaredDistance;
                nearestDataIndex = i;
            }

        }

        if (nearestDataIndex != -1) {

            int jitterX = (m_jitterX == null) ? 0 : m_jitterX[nearestDataIndex];
            if (Math.abs(m_plotPanel.getXAxis().valueToPixel(x)-m_plotPanel.getXAxis().valueToPixel( m_dataX[nearestDataIndex])-jitterX)>SELECT_SENSIBILITY) {
                return -1;
            }
            int jitterY = (m_jitterY == null) ? 0 : m_jitterY[nearestDataIndex];
            if (Math.abs(m_plotPanel.getYAxis().valueToPixel(y)-m_plotPanel.getYAxis().valueToPixel( m_dataY[nearestDataIndex])-jitterY)>SELECT_SENSIBILITY) {
                return -1;
            }

        }

        return nearestDataIndex;
    }

    @Override
    public void parametersChanged() {
        updateJitter();
    }
    
    private void updateJitter() {
        
        int size = m_compareDataInterface.getRowCount();
        if (size == 0) {

            return;
        }
        
        if ((m_jitterXParameter == null) || (m_jitterYParameter == null) || (m_jitterXParameter.getObjectValue() == null) || (m_jitterYParameter.getObjectValue() == null)) {
            m_jitterX = null;
            m_jitterY = null;
            return;
        }
        
        int jitterX = ((Integer) m_jitterXParameter.getObjectValue());
        int jitterY = ((Integer) m_jitterYParameter.getObjectValue());

        if (jitterX > 0) {
            m_jitterX = new int[size];
            Random r = new Random(System.currentTimeMillis());

            int modulo = jitterX * 2 + 1;
            for (int i = 0; i < size; i++) {
                m_jitterX[i] = (r.nextInt(modulo)) - jitterX;
            }
        } else {
            m_jitterX = null;
        }
        if (jitterY > 0) {
            m_jitterY = new int[size];
            Random r = new Random(System.currentTimeMillis());

            int modulo = jitterY * 2 + 1;
            for (int i = 0; i < size; i++) {
                m_jitterY[i] = (r.nextInt(modulo)) - jitterY;
            }
        } else {
            m_jitterY = null;
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

        // set jitter values
        updateJitter();
        
        boolean xAsEnum = m_plotPanel.getXAxis().isEnum();
        boolean yAsEnum = m_plotPanel.getYAxis().isEnum();
        for (int i = 0; i < size; i++) {
            
            if (xAsEnum) {
                m_dataX[i] = i;
            } else {
                Object value = m_compareDataInterface.getDataValueAt(i, m_colX);
                m_dataX[i] = (value == null || !Number.class.isAssignableFrom(value.getClass())) ? Double.NaN : ((Number) value).doubleValue(); //CBy TODO : ne pas avoir a tester le type Number
            }
            
            if (yAsEnum) {
                 m_dataY[i] = i;
            } else {
                Object value = m_compareDataInterface.getDataValueAt(i, m_colY);
                m_dataY[i] = (value == null || !Number.class.isAssignableFrom(value.getClass())) ? Double.NaN : ((Number) value).doubleValue(); //CBy TODO : ne pas avoir a tester le type Number
            }
            m_selected[i] = false;
        }

        // min and max values
        double minX = m_dataX[0];
        double maxX = minX;
        double minY = m_dataY[0];
        double maxY = minY;
        int i = 1;
        while (minX != minX || (minY!=minY)) { // NaN values
            if (i>=size) {
                break;
            }
            minX = m_dataX[i];
            maxX = minX;
            minY = m_dataY[i];
            maxY = minY;
            i++;
        }
        for (; i < size; i++) {
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
        if (!xAsEnum) {
            double deltaX = (m_xMax - m_xMin);
            if (deltaX <= 10e-10) {
                // no real delta
                m_xMin = m_xMin - 1;  //JPM.TODO : enhance this
                m_xMax = m_xMax + 1;
            } else {
                m_xMin = m_xMin - deltaX * 0.01;
                m_xMax = m_xMax + deltaX * 0.01;
            }
        }

        if (!yAsEnum) {
            double deltaY = (m_yMax - m_yMin);
            if (deltaY <= 10e-10) {
                // no real delta
                m_yMin = m_yMin - 1;  //JPM.TODO : enhance this
                m_yMax = m_yMax + 1;
            } else {
                m_yMin = m_yMin - deltaY * 0.01;
                m_yMax = m_yMax + deltaY * 0.01;
            }
        }

        m_plotPanel.updateAxis(this);
        m_plotPanel.setXAxisTitle(m_compareDataInterface.getDataColumnIdentifier(m_colX));
        m_plotPanel.setYAxisTitle(m_compareDataInterface.getDataColumnIdentifier(m_colY));


        m_plotPanel.repaint();
    }
    
    private void setGradientValues() {
        ColorOrGradient colorOrGradient = m_colorParameter.getColor();
        boolean useGradient = !colorOrGradient.isColorSelected();
        if (!useGradient) {
            return;
        }
        
        m_gradientColors = colorOrGradient.getGradient().getColors();
        m_gradientFractions = colorOrGradient.getGradient().getFractions();
        
        int gradientParamCol = m_colorParameter.getSelectedReferenceIdName().getColumnIndex();
        if (gradientParamCol == m_gradientParamCol) {
            return;
        }
        m_gradientParamCol = gradientParamCol;
        
        int size = m_compareDataInterface.getRowCount();
        if ((m_gradientParamValues == null) || (m_gradientParamValues.length != size)) {
            m_gradientParamValues = new double[size];
        }
        if (size == 0) {
            return;
        }
        
        Object value = m_compareDataInterface.getDataValueAt(0, m_gradientParamCol);
        double d = (value == null || ! Number.class.isAssignableFrom(value.getClass())) ? Double.NaN : ((Number)value).doubleValue();
        m_gradientParamValues[0] = d;

        m_gradientParamValuesMin = d;
        m_gradientParamValuesMax = d;

        for (int i = 1; i < size; i++) {
            value = m_compareDataInterface.getDataValueAt(i, m_gradientParamCol);
            d = (value == null || ! Number.class.isAssignableFrom(value.getClass())) ? Double.NaN : ((Number)value).doubleValue();
            m_gradientParamValues[i] = d;
            if (d<m_gradientParamValuesMin) {
                m_gradientParamValuesMin = d;
            } else if (d>m_gradientParamValuesMax) {
                m_gradientParamValuesMax = d;
            }
        }
        

        
        
    }

    private Color getColorInGradient(LinearGradientPaint gradient, double d) {
        double f = (d-m_gradientParamValuesMin)/(m_gradientParamValuesMax-m_gradientParamValuesMin);
        for (int i=0;i<m_gradientFractions.length-1;i++) {
            float f1 = m_gradientFractions[i];
            float f2 = m_gradientFractions[i+1];
            if ((f>=f1) && (f<=f2)) {
                Color c1 = m_gradientColors[i];
                Color c2 = m_gradientColors[i+1];
                double fInInterval = (f-f1)/(f2-f1);
                int r = (int) Math.round(  ((double) (c2.getRed()-c1.getRed()))  *fInInterval+(double) c1.getRed() );
                int g = (int) Math.round(  ((double) (c2.getGreen()-c1.getGreen()))  *fInInterval+(double) c1.getGreen() );
                int b = (int) Math.round(  ((double) (c2.getBlue()-c1.getBlue()))  *fInInterval+(double) c1.getBlue() );
                int a = (int) Math.round(  ((double) (c2.getAlpha()-c1.getAlpha()))  *fInInterval+(double) c1.getAlpha() );
                return new Color(r, g, b, a);
            }
        }
        
        if (f<= m_gradientFractions[0]) {
            return m_gradientColors[0];
        }
        return m_gradientColors[m_gradientFractions.length-1];
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

        ColorOrGradient colorOrGradient = m_colorParameter.getColor();
        boolean useGradient = !colorOrGradient.isColorSelected();
        
        Color plotColor = Color.black; // default init
        LinearGradientPaint gradientPaint = null;

        if (!useGradient) {
            plotColor = colorOrGradient.getColor();
        } else {
            setGradientValues(); // set gradient values if needed
            gradientPaint = colorOrGradient.getGradient();
        }
 
        // first plot non selected
        g.setColor(plotColor);
        int size = m_dataX.length;
        for (int i=0;i<size;i++) {
            if (m_selected[i]) {
                continue;
            }
            int x = xAxis.valueToPixel( m_dataX[i]) + ((m_jitterX != null) ? m_jitterX[i] : 0);
            int y = yAxis.valueToPixel( m_dataY[i]) + ((m_jitterY != null) ? m_jitterY[i] : 0);

            if (useGradient) {
               plotColor = getColorInGradient(gradientPaint,  m_gradientParamValues[i]);
               g.setColor(plotColor);
            }
            
            g.fillOval(x-3, y-3, 6, 6);

        }
        
        // plot selected
        for (int i=0;i<size;i++) {
            if (!m_selected[i]) {
                continue;
            }
            int x = xAxis.valueToPixel( m_dataX[i]) + ((m_jitterX != null) ? m_jitterX[i] : 0);
            int y = yAxis.valueToPixel( m_dataY[i]) + ((m_jitterY != null) ? m_jitterY[i] : 0);

            if (useGradient) {
               plotColor = getColorInGradient(gradientPaint,  m_gradientParamValues[i]);
            }
            
            g.setColor(plotColor);
            g.fillOval(x-3, y-3, 6, 6);
            
            g.setColor(Color.black);
            g.drawOval(x-3, y-3, 6, 6);


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
    
    @Override
    public boolean isMouseOnPlot(double x, double y) {
        return findPoint(x, y) != -1;
    }
    
    @Override
    public String getEnumValueX(int index) {
        if ((index <0) || (index>=m_dataX.length)) {
            return "";
        }
        
        return m_compareDataInterface.getDataValueAt(index, m_colX).toString();
        
    }
    
    @Override
    public String getEnumValueY(int index) {
        if ((index < 0) || (index >= m_dataY.length)) {
            return "";
        }

        return m_compareDataInterface.getDataValueAt(index, m_colY).toString();
    }
}
