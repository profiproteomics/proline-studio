package fr.proline.studio.graphics;

import fr.proline.studio.comparedata.CompareDataInterface;
import fr.proline.studio.graphics.parallelcoordinates.ParallelCoordinatesAxis;
import fr.proline.studio.parameter.ColorOrGradientParameter;
import fr.proline.studio.parameter.ColorParameter;
import fr.proline.studio.parameter.IntegerParameter;
import fr.proline.studio.parameter.ParameterList;
import fr.proline.studio.utils.CyclicColorPalette;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSpinner;

/**
 *
 * @author JM235353
 */
public class PlotParallelCoordinates extends PlotMultiDataAbstract {
    
    
    private static final Color COLOR_VALUES_NOT_SELECTED = new Color(230,230,230);
    private static final Color[] GRADIENT_COLORS_VALUES_SELECTED = { new Color(132,90,133) /* purple*/, new Color(255,200,255) /* pink */ };
    
    private static final int PAD_AXIS_X = 30;
    
    private ArrayList<ParallelCoordinatesAxis> m_axisList = new ArrayList<>();
    
    
    private ArrayList<ParameterList> m_parameterListArray = null;
    private final ColorOrGradientParameter m_colorSelectedParameter;
    private final ColorParameter m_colorParameter;
    private final IntegerParameter m_thicknessParameter;
    
    private Color[] m_gradientColors;
    private float[] m_gradientFractions;
    private int m_mainSelectedAxisIndex = 0;
    
    private boolean[] m_selectionArray = null;
    private boolean[] m_selectionArrayForExport = null;
    private long[] m_ids;
    
    public PlotParallelCoordinates(BasePlotPanel plotPanel, CompareDataInterface compareDataInterface, CrossSelectionInterface crossSelectionInterface, int[] cols) {
        super(plotPanel, PlotType.PARALLEL_COORDINATES_PLOT, compareDataInterface, crossSelectionInterface);

        update(cols, null);

        
        // Color parameter
        ParameterList colorParameteList = new ParameterList("Colors");
       
        ColorOrGradient colorOrGradient = new ColorOrGradient();
        colorOrGradient.setColor(CyclicColorPalette.getColor(21, 128));
        float[] fractions = {0.0f, 1.0f};
        
        LinearGradientPaint gradient = ColorOrGradientChooserPanel.getGradientForPanel(GRADIENT_COLORS_VALUES_SELECTED, fractions);
        colorOrGradient.setGradient(gradient);
        colorOrGradient.setGradientSelected();

        m_colorSelectedParameter = new ColorOrGradientParameter("PLOT_COORD_PARALLEL_COLOR_KEY", "Color for selected values", colorOrGradient, null);
        colorParameteList.add(m_colorSelectedParameter);

        m_colorParameter = new ColorParameter("UNSELECTED_COLOR_KEY", "Color for unselected values", COLOR_VALUES_NOT_SELECTED);
        colorParameteList.add(m_colorParameter);
        
        m_thicknessParameter = new IntegerParameter("THICKNESS_BORDER_VENNDIAGRAM", "Border Thickness", JSpinner.class, 1, 1, 5);
        colorParameteList.add(m_thicknessParameter);
        
        m_parameterListArray = new  ArrayList<>(1);
        m_parameterListArray.add(colorParameteList);
        
         // disable selection buttons
        m_plotPanel.enableButton(BasePlotPanel.PlotToolbarListener.BUTTONS.GRID, false);
        m_plotPanel.enableButton(BasePlotPanel.PlotToolbarListener.BUTTONS.IMPORT_SELECTION, false);
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
        
        int deltaAxis = getAxisDelta(width, nbAxis);

        int i = 0;
        for (ParallelCoordinatesAxis axis : m_axisList) {
            axis.setPosition(PAD_AXIS_X+i*deltaAxis, height);
            i++;
        }

        ColorOrGradient colorOrGradient = m_colorSelectedParameter.getColor();
        boolean useGradient = !colorOrGradient.isColorSelected();

        Color plotColor = Color.black; // default init

        if (!useGradient) {
            plotColor = colorOrGradient.getColor();
        } else {
            setGradientValues(); // set gradient values if needed
        }

        
        // paint non selected lines
        drawLines(g, plotColor, useGradient, false);
        
        Stroke previousStroke = g.getStroke();
        Integer thickness = (Integer) m_thicknessParameter.getObjectValue();
        if (thickness == null) {
            thickness = 1;
        }
        g.setStroke( new BasicStroke(thickness));
        
        // paint selected lines
        drawLines(g, plotColor, useGradient, true);
        
        g.setStroke(previousStroke);
        
        for (ParallelCoordinatesAxis axis : m_axisList) {
            axis.paint(g, width);
        }
        
    }
    
    private int getAxisDelta(int width, int nbAxis) {
        return (width-ParallelCoordinatesAxis.AXIS_WIDTH-PAD_AXIS_X*2)/(nbAxis-1);
    }
    
    private void drawLines(Graphics2D g, Color plotColor, boolean useGradient, boolean paintSelection) {
        
        ParallelCoordinatesAxis selectedAxis = m_axisList.get(m_mainSelectedAxisIndex);
        ParallelCoordinatesAxis firstAxis = m_axisList.get(0);
        
        int nbRows = m_compareDataInterface.getRowCount();
        for (int index = 0; index < nbRows; index++) {

            int rowIndex = selectedAxis.getRowIndexFromIndex(index);

            if (m_selectionArray[rowIndex] ^ paintSelection) {
                continue;
            }
            
            int y1 = firstAxis.getPositionByRowIndex(rowIndex);
            int x1 = firstAxis.getX();
            Color c = (paintSelection) ? selectColor(plotColor, useGradient, rowIndex) : m_colorParameter.getColor();
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
        
        
        ParallelCoordinatesAxis selectedAxis = m_axisList.get(m_mainSelectedAxisIndex);
        
        for (int index = 0; index < nbRows; index++) {
            boolean selected = true;
            int rowIndex = selectedAxis.getRowIndexFromIndex(index);
            for (ParallelCoordinatesAxis axis : m_axisList) {

                
                if (!axis.isRowIndexSelected(rowIndex, index, false)) {
                    selected = false;
                    break;
                }
            }
            m_selectionArray[rowIndex] = selected;
        }
        
        for (int index = 0; index < nbRows; index++) {
            boolean selected = true;
            int rowIndex = selectedAxis.getRowIndexFromIndex(index);
            for (ParallelCoordinatesAxis axis : m_axisList) {

                if (!axis.isRowIndexSelected(rowIndex, index, true)) {
                    selected = false;
                    break;
                }
            }
            m_selectionArrayForExport[rowIndex] = selected;
        }
        
    }
    
    private void setGradientValues() {
            
        ColorOrGradient colorOrGradient = m_colorSelectedParameter.getColor();
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
            ParallelCoordinatesAxis selectedAxis = m_axisList.get(m_mainSelectedAxisIndex);

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
        m_selectionArrayForExport = new boolean[nbRows];
        m_ids = new long[nbRows];
        Arrays.fill(m_selectionArray, Boolean.TRUE);
        Arrays.fill(m_selectionArrayForExport, Boolean.TRUE);
        
        m_axisList.clear();
        
        for (int i = 0; i < nbRows; i++) {
            m_ids[i] = m_compareDataInterface.row2UniqueId(i);
        }
        
        
        m_mainSelectedAxisIndex = 0;
        int nbCols = m_cols.length;
        for (int i=0;i<nbCols;i++) {
            int colId = m_cols[i];
            
            ParallelCoordinatesAxis axis = new ParallelCoordinatesAxis(i, m_compareDataInterface, colId, this);
            if (i == 0) {
                axis.displaySelected(true);
            }
            m_axisList.add(axis);
            
        }
        
        
        //JPM.TODO : remove it ?
        prepareSelectionHashSet();
        
        m_plotPanel.forceUpdateDoubleBuffer();
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
        
        ParallelCoordinatesAxis selectedAxis = m_axisList.get(m_mainSelectedAxisIndex);
        int nbRows = m_compareDataInterface.getRowCount();

        ArrayList<Long> selection = new ArrayList();
        for (int index = 0; index < nbRows; index++) {
            int rowIndex = selectedAxis.getRowIndexFromIndex(index);

            if (m_selectionArrayForExport[rowIndex]) {
                selection.add(m_ids[rowIndex]);
            }
        }
        return selection;
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
    
    
    @Override
    public JPopupMenu getPopupMenu(double x, double y) {
        
        MoveableInterface over = getOverMovable((int) Math.round(x), (int) Math.round(y));
        if ( (over == null) || (!(over instanceof ParallelCoordinatesAxis)) || (! ((ParallelCoordinatesAxis) over).isSelected())) {
            return null;
        }
 
        
        int nbNumberAxisSelected = 0;
        boolean stringAxisSelected = false;
        boolean canLog = true;
        for (ParallelCoordinatesAxis axis : m_axisList) {
            if (axis.isSelected() ) {
                if (axis.isNumeric()) {
                    nbNumberAxisSelected++;
                    if (!axis.canLog()) {
                        canLog = false;
                    }
                } else {
                    stringAxisSelected = true;
                }
            }
        }
        
        JMenuItem logAction = new JMenuItem("Log10");
        logAction.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (ParallelCoordinatesAxis axis : m_axisList) {
                    if (axis.isSelected() && axis.isNumeric()) {
                        axis.log();
                    }
                }
                
                // only main axis is selected after a log
                ParallelCoordinatesAxis mainAxis = m_axisList.get(m_mainSelectedAxisIndex);
                m_mainSelectedAxisIndex = -1;
                selectAxis(mainAxis, false);
                
                m_plotPanel.repaintUpdateDoubleBuffer();
                
                
            }
        });
        logAction.setEnabled((nbNumberAxisSelected>=1) && (!stringAxisSelected) && canLog);
        
        JMenuItem normalizeAction = new JMenuItem("Normalize");
        normalizeAction.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                double rangeMin = Double.POSITIVE_INFINITY;
                double rangeMax= -Double.POSITIVE_INFINITY;
                boolean hasNaN = false;
                for (ParallelCoordinatesAxis axis : m_axisList) {
                    if (!axis.isSelected()) {
                        continue;
                    }
                    hasNaN |= axis.hasNaN();
                    double min = axis.getRealMinValue();
                    if (min<rangeMin) {
                        rangeMin = min;
                    }
                    double max = axis.getRealMaxValue();
                    if (max>rangeMax) {
                        rangeMax = max;
                    }
                }
                for (ParallelCoordinatesAxis axis : m_axisList) {
                    if (!axis.isSelected()) {
                        continue;
                    }
                    axis.setRange(rangeMin, rangeMax, hasNaN);
                }
                
                // only main axis is selected after a normalize
                ParallelCoordinatesAxis mainAxis = m_axisList.get(m_mainSelectedAxisIndex);
                m_mainSelectedAxisIndex = -1;
                selectAxis(mainAxis, false);
                
                m_plotPanel.repaintUpdateDoubleBuffer();
            }
        });
        normalizeAction.setEnabled((nbNumberAxisSelected>1) && (!stringAxisSelected));

        JPopupMenu menu = new JPopupMenu();
        menu.add(logAction);
        menu.add(normalizeAction);

        return menu;
    }
        
    @Override
    public void doubleClicked(int x, int y) {
        for (ParallelCoordinatesAxis axis : m_axisList) {
             MoveableInterface movable = axis.getOverMovable(x, y);
            if (movable != null) {
                // we are over the axis
                axis.doubleClicked();
            }
        }
    }
    
    public void selectAxis(ParallelCoordinatesAxis axis, boolean isCtrlOrShiftDown) {
        int id = axis.getId();
        if (m_mainSelectedAxisIndex != id) {
            m_mainSelectedAxisIndex = id;
            
            if (!isCtrlOrShiftDown) {

                for (ParallelCoordinatesAxis axisCur : m_axisList) {
                    axisCur.displaySelected(false);
                }
                
            }
            axis.displaySelected(true);
            
            m_plotPanel.repaint();
        }
    }

    
    public void axisMoved(ParallelCoordinatesAxis axis, int deltaX) {
        
        int nbAxis = m_axisList.size();
        int width = m_plotPanel.getWidth();
        int deltaAxis = getAxisDelta(width, nbAxis);
        
        if (Math.abs(deltaX)<deltaAxis) {
            m_plotPanel.repaintUpdateDoubleBuffer();
            return;
        }
        
        int axisPosition = axis.getX()+deltaX;
        
        ArrayList<ParallelCoordinatesAxis> m_axisListNew = new ArrayList<>();
        
        boolean axisAdded = false;
        ParallelCoordinatesAxis previousAxis = null;
        for (ParallelCoordinatesAxis nextAxis : m_axisList) {
            if (nextAxis.equals(axis)) {
                previousAxis = nextAxis;
                continue;
            }
            if (previousAxis == null) {
                if (axisPosition < nextAxis.getX()) {
                    m_axisListNew.add(axis);
                    axisAdded = true;
                }

            } else if ((previousAxis.getX()<axisPosition) && (axisPosition<nextAxis.getX())) {
                m_axisListNew.add(axis);
                axisAdded = true;
            }
            m_axisListNew.add(nextAxis);
            
            previousAxis = nextAxis;
        }
        if (!axisAdded) {
            m_axisListNew.add(axis);
        }
        
        m_axisList = m_axisListNew;

        m_plotPanel.repaintUpdateDoubleBuffer();

    }
    
    public void axisChanged() {
        prepareSelectionHashSet();
        m_plotPanel.repaintUpdateDoubleBuffer();
    }
    
    public void axisIsMoving() {
        m_plotPanel.repaintUpdateDoubleBuffer();
    }
    
}
