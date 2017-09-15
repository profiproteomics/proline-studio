package fr.proline.studio.graphics;

import fr.proline.studio.comparedata.CompareDataInterface;
import fr.proline.studio.graphics.parallelcoordinates.ParallelCoordinatesAxis;
import fr.proline.studio.parameter.ColorOrGradientParameter;
import fr.proline.studio.parameter.ParameterList;
import fr.proline.studio.utils.CyclicColorPalette;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author JM235353
 */
public class PlotParallelCoordinates extends PlotMultiDataAbstract {
    
    private ArrayList<ParallelCoordinatesAxis> m_axisList = new ArrayList<>();
    
    
    private ArrayList<ParameterList> m_parameterListArray = null;
    private final ColorOrGradientParameter m_colorParameter;
    
    private Color[] m_gradientColors;
    private float[] m_gradientFractions;
    private int m_selectedAxisIndex = 0;
    
    private boolean[] m_selectionArray = null;
    
    public PlotParallelCoordinates(BasePlotPanel plotPanel, CompareDataInterface compareDataInterface, CrossSelectionInterface crossSelectionInterface, int[] cols) {
        super(plotPanel, PlotType.PARALLEL_COORDINATES_PLOT, compareDataInterface, crossSelectionInterface);

        update(cols, null);

        
        // Color parameter
        ParameterList colorParameteList = new ParameterList("Colors");
       
        ColorOrGradient colorOrGradient = new ColorOrGradient();
        colorOrGradient.setColor(CyclicColorPalette.getColor(21, 128));
        float[] fractions = {0.0f, 1.0f};
        Color[] colors = {Color.white, Color.red};
        LinearGradientPaint gradient = ColorOrGradientChooserPanel.getGradientForPanel(colors, fractions);
        colorOrGradient.setGradient(gradient);

        m_colorParameter = new ColorOrGradientParameter("PLOT_COORD_PARALLEL_COLOR_KEY", "Color", colorOrGradient, null);
        colorParameteList.add(m_colorParameter);

        
        m_parameterListArray = new  ArrayList<>(1);
        m_parameterListArray.add(colorParameteList);
        
        
    }


    @Override
    public boolean needsXAxis() {
        return false;
    }

    @Override
    public boolean needsYAxis() {
        return false;
    }

    @Override
    public double getNearestXData(double x) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double getNearestYData(double y) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double getXMin() {
        return 0;
    }

    @Override
    public double getXMax() {
        if (m_plotPanel == null) {
            return 0;
        }
        return m_plotPanel.getWidth();
    }

    @Override
    public double getYMin() {
        return 0;
    }

    @Override
    public double getYMax() {
        if (m_plotPanel == null) {
            return 0;
        }
        return m_plotPanel.getHeight();
    }

    @Override
    public void parametersChanged() {
    }

    @Override
    public void paint(Graphics2D g) {
        
        if (m_axisList.isEmpty()) {
            return;
        }
        
        int width = m_plotPanel.getWidth();
        int height = m_plotPanel.getHeight();
        
        int nbAxis = m_axisList.size();
        int PAD_X = 30;
        int deltaAxis = (width-ParallelCoordinatesAxis.AXIS_WIDTH-PAD_X*2)/(nbAxis-1);

        int i = 0;
        for (ParallelCoordinatesAxis axis : m_axisList) {
            axis.setPosition(PAD_X+i*deltaAxis, height);
            axis.paintBackground(g);
            i++;
        }

        ColorOrGradient colorOrGradient = m_colorParameter.getColor();
        boolean useGradient = !colorOrGradient.isColorSelected();

        Color plotColor = Color.black; // default init

        if (!useGradient) {
            plotColor = colorOrGradient.getColor();
        } else {
            setGradientValues(); // set gradient values if needed
        }

        
        /*ParallelCoordinatesAxis selectedAxis = m_axisList.get(m_selectedAxisIndex);
        
        ParallelCoordinatesAxis firstAxis = m_axisList.get(0);
        int nbRows = m_compareDataInterface.getRowCount();
        for (int index=0;index<nbRows;index++) {
            
            boolean selected = m_selectionArray[index];
            
            int rowIndex = selectedAxis.getRowIndexFromIndex(index);
            
            int y1 = firstAxis.getPositionByRowIndex(rowIndex);
            int x1 = firstAxis.getX();
            Color c = (selected) ? selectColor(plotColor, useGradient, rowIndex) : Color.lightGray;
            g.setColor(c);
            for (int j = 0;j< m_axisList.size();j++) {
                ParallelCoordinatesAxis secondAxis = m_axisList.get(j);
                int y2 = secondAxis.getPositionByRowIndex(rowIndex);
                int x2 = secondAxis.getX();
                
                g.drawLine(x1, y1, x2, y2);
                x1 = x2;
                y1 = y2;
            }   
        }*/
        
        // paint non selected lines
        drawLines(g, plotColor, useGradient, false);
        
        // paint selected lines
        drawLines(g, plotColor, useGradient, true);
        
        for (ParallelCoordinatesAxis axis : m_axisList) {
            axis.paintForeground(g, m_selectedAxisIndex == axis.getId());
        }
        
    }
    
    private void drawLines(Graphics2D g, Color plotColor, boolean useGradient, boolean paintSelection) {
        
        ParallelCoordinatesAxis selectedAxis = m_axisList.get(m_selectedAxisIndex);
        ParallelCoordinatesAxis firstAxis = m_axisList.get(0);
        
        int nbRows = m_compareDataInterface.getRowCount();
        for (int index = 0; index < nbRows; index++) {

            int rowIndex = selectedAxis.getRowIndexFromIndex(index);

            if (m_selectionArray[rowIndex] ^ paintSelection) {
                continue;
            }
            
            int y1 = firstAxis.getPositionByRowIndex(rowIndex);
            int x1 = firstAxis.getX();
            Color c = (paintSelection) ? selectColor(plotColor, useGradient, rowIndex) : Color.lightGray;
            g.setColor(c);
            for (int j = 0; j < m_axisList.size(); j++) {
                ParallelCoordinatesAxis secondAxis = m_axisList.get(j);
                int y2 = secondAxis.getPositionByRowIndex(rowIndex);
                int x2 = secondAxis.getX();

                g.drawLine(x1, y1, x2, y2);
                x1 = x2;
                y1 = y2;
            }
        }
    }
    
    private void prepareSelectionHashSet() {
        if (m_axisList.isEmpty()) {
            return;
        }
        
        int nbRows = m_compareDataInterface.getRowCount();
        
        
        ParallelCoordinatesAxis selectedAxis = m_axisList.get(m_selectedAxisIndex);
        
        for (int index = 0; index < nbRows; index++) {
            boolean selected = true;
            int rowIndex = selectedAxis.getRowIndexFromIndex(index);
            for (ParallelCoordinatesAxis axis : m_axisList) {

                
                if (!axis.isRowIndexSelected(rowIndex)) {
                    selected = false;
                    break;
                }
            }
            m_selectionArray[rowIndex] = selected;
        }
    }
    
    private void setGradientValues() {
            
        ColorOrGradient colorOrGradient = m_colorParameter.getColor();
        boolean useGradient = !colorOrGradient.isColorSelected();
        if (!useGradient) {
            return;
        }
        
        m_gradientColors = colorOrGradient.getGradient().getColors();
        m_gradientFractions = colorOrGradient.getGradient().getFractions();

        /*ParallelCoordinatesAxis firstAxis = m_axisList.get(0);
        
        
        int height = firstAxis.getHeight();
        if ((m_gradientParamValues == null) || (m_gradientParamValues.length != height)) {
            m_gradientParamValues = new double[height];
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
        }*/

    }
    
      private Color selectColor(Color plotColor, boolean useGradient, int rowIndex) {
        Color c = null;
        /*if (!m_idsToGraphicDataGroup.isEmpty()) {
            GraphicDataGroup dataGroup = m_idsToGraphicDataGroup.get(m_ids[i]);
            if (dataGroup != null) {
                c = dataGroup.getColor();
            } else if (useGradient) {
                c = getColorInGradient(m_gradientParamValues[i]);
            } else {
                c = plotColor;
            }
        } else*/ if (useGradient) {
            ParallelCoordinatesAxis selectedAxis = m_axisList.get(m_selectedAxisIndex);

            int height = selectedAxis.getHeight();
            int heightInAxis = selectedAxis.getRelativePositionByRowIndex(rowIndex);
            c = getColorInGradient(heightInAxis, height);
        } else {
            c = plotColor;
        }
        return c;
    }
    
    private Color getColorInGradient(int heightInAxis, int height) {
        double f = ((double)heightInAxis)/((double)height);
        for (int i = 0; i < m_gradientFractions.length - 1; i++) {
            float f1 = m_gradientFractions[i];
            float f2 = m_gradientFractions[i + 1];
            if ((f >= f1) && (f <= f2)) {
                Color c1 = m_gradientColors[i];
                Color c2 = m_gradientColors[i + 1];
                double fInInterval = (f - f1) / (f2 - f1);
                int r = (int) Math.round(((double) (c2.getRed() - c1.getRed())) * fInInterval + (double) c1.getRed());
                int g = (int) Math.round(((double) (c2.getGreen() - c1.getGreen())) * fInInterval + (double) c1.getGreen());
                int b = (int) Math.round(((double) (c2.getBlue() - c1.getBlue())) * fInInterval + (double) c1.getBlue());
                int a = (int) Math.round(((double) (c2.getAlpha() - c1.getAlpha())) * fInInterval + (double) c1.getAlpha());
                return new Color(r, g, b, a);
            }
        }

        if (f <= m_gradientFractions[0]) {
            return m_gradientColors[0];
        }
        return m_gradientColors[m_gradientFractions.length - 1];
    }

    @Override
    public String getToolTipText(double x, double y) {
        return null;
    }

    @Override
    public void update() {

        int nbRows = m_compareDataInterface.getRowCount();
        m_selectionArray = new boolean[nbRows];
        Arrays.fill(m_selectionArray, Boolean.TRUE);
        
        m_axisList.clear();
        
        
        
        int nbCols = m_cols.length;
        for (int i=0;i<nbCols;i++) {
            int colId = m_cols[i];
            
            ParallelCoordinatesAxis axis = new ParallelCoordinatesAxis(i, m_compareDataInterface, colId, this);
            m_axisList.add(axis);
            
        }
        
        //JPM.TODO : remove it !!!!!!!!!!!!!!!!!!!!!!
        prepareSelectionHashSet();
        
        m_plotPanel.repaint();
        
        
    }

    @Override
    public boolean select(double x, double y, boolean append) {
        return false;
    }

    @Override
    public boolean select(Path2D.Double path, double minX, double maxX, double minY, double maxY, boolean append) {
        return false;
    }

    @Override
    public ArrayList<Long> getSelectedIds() {
        return null;
    }

    @Override
    public void setSelectedIds(ArrayList<Long> selection) {
        
    }

    @Override
    public ArrayList<ParameterList> getParameters() {
                
        return m_parameterListArray;
    }

    @Override
    public boolean isMouseOnPlot(double x, double y) {
        return true;
    }

    @Override
    public boolean isMouseOnSelectedPlot(double x, double y) {
        return false;
    }

    @Override
    public String getEnumValueX(int index, boolean fromData) {
        return null;
    }

    @Override
    public String getEnumValueY(int index, boolean fromData) {
        return null;
    }
    

    @Override
    public MoveableInterface getOverMovable(int x, int y) {
        MoveableInterface movable = super.getOverMovable(x, y);
        if (movable != null) {
            return movable;
        }
        for (ParallelCoordinatesAxis axis : m_axisList) {
            movable = axis.getOverMovable(x, y);
            if (movable != null) {
                return movable;
            }
        }
        return null;
    }
    
    public void selectAxis(ParallelCoordinatesAxis axis) {
        int id = axis.getId();
        if (m_selectedAxisIndex != id) {
            m_selectedAxisIndex = id;
            m_plotPanel.repaint();
        }
    }
    
    public void axisChanged() {
        prepareSelectionHashSet();
        m_plotPanel.repaintUpdateDoubleBuffer();
    }
    
}
