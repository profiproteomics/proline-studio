/* 
 * Copyright (C) 2019 VD225637
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1 
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License 
 * along with this program; If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */
package fr.proline.studio.graphics;

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
import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;
import fr.proline.studio.graphics.core.PlotToolbarListenerInterface;

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
    private long[] m_ids;
    
    private boolean[] m_SpecificSelection = null;
    
    public PlotParallelCoordinates(BasePlotPanel plotPanel, ExtendedTableModelInterface compareDataInterface, CrossSelectionInterface crossSelectionInterface, int[] cols) {
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
        m_plotPanel.enableButton(PlotToolbarListenerInterface.BUTTONS.GRID, false);
        //m_plotPanel.enableButton(BasePlotPanel.PlotToolbarListener.BUTTONS.IMPORT_SELECTION, false);
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
    public boolean canLogXAxis() {
        return false;
    }
    
    @Override
    public boolean canLogYAxis() {
        return true;
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
    public void paint(Graphics2D g, XAxis xAxis, YAxis yAxis) {
        
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
        

        
    }
    
    @Override
    public void paintOver(Graphics2D g) {
        int width = m_plotPanel.getWidth();
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

            if (m_SpecificSelection != null) {
                if (m_SpecificSelection[rowIndex] ^ paintSelection) {
                    continue;
                }
            } else if (m_selectionArray[rowIndex] ^ paintSelection) {
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
        
        m_SpecificSelection = null;
        
        int nbRows = m_compareDataInterface.getRowCount();
        
        
        ParallelCoordinatesAxis selectedAxis = m_axisList.get(m_mainSelectedAxisIndex);
        
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
            
        ColorOrGradient colorOrGradient = m_colorSelectedParameter.getColor();
        boolean useGradient = !colorOrGradient.isColorSelected();
        if (!useGradient) {
            return;
        }
        
        m_gradientColors = colorOrGradient.getGradient().getColors();
        m_gradientFractions = colorOrGradient.getGradient().getFractions();


    }
    
      private Color selectColor(Color plotColor, boolean useGradient, int rowIndex) {
        Color c;
        if (useGradient) {
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
        m_SpecificSelection = null;
        m_ids = new long[nbRows];
        Arrays.fill(m_selectionArray, Boolean.TRUE);
        
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
        if (m_mainSelectedAxisIndex == -1) {
            return false;
        }
        ParallelCoordinatesAxis selectedAxis = m_axisList.get(m_mainSelectedAxisIndex);
        m_mainSelectedAxisIndex = -1;
        selectAxis(selectedAxis, false);
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

            
            if (m_SpecificSelection != null) {
                if (m_SpecificSelection[rowIndex]) {
                    selection.add(m_ids[rowIndex]);
                }
            } else if (m_selectionArray[rowIndex]) {
                selection.add(m_ids[rowIndex]);
            }

        }
        return selection;
    }

    @Override
    public void setSelectedIds(ArrayList<Long> selection) {

        int nbRows = m_compareDataInterface.getRowCount();
        m_SpecificSelection = new boolean[nbRows];
        Arrays.fill(m_SpecificSelection, Boolean.FALSE);
        for (Long l : selection) {
            m_SpecificSelection[l.intValue()] = true;
        }
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
    public String getEnumValueY(int index, boolean fromData, Axis axis) {
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
            
            m_SpecificSelection = null;
            
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

        ParallelCoordinatesAxis mainAxis = m_axisList.get(m_mainSelectedAxisIndex);

        
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

        // set the index of the main axis which can have been changed
        m_mainSelectedAxisIndex = 0;
        for (ParallelCoordinatesAxis axisCur : m_axisList) {
            axisCur.setId(m_mainSelectedAxisIndex);
            if (axisCur.equals(mainAxis)) {
                break;
            }
            m_mainSelectedAxisIndex++;
        }


        
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
