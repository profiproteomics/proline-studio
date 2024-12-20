/* 
 * Copyright (C) 2019
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
package fr.proline.studio.graphics.parallelcoordinates;

import fr.proline.studio.graphics.MoveableInterface;
import fr.proline.studio.graphics.PlotParallelCoordinates;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;



/**  
 * Axis used for parallel coordinates.
 * An Axis can be moved to modify the order of the axis. It has a scrollbar with two handles to select min and max.
 * It is possible to move this scrollbar to change accordingly min and max.
 * This axis support String and Number (integer or double).
 * It handles NaN values by putting them separately at the bottom.
 * 
 * @author JM235353
 */
public class ParallelCoordinatesAxis implements MoveableInterface {
    
    public static final int AXIS_WIDTH = 10;
    public static final int PAD_Y_UP = 40;
    public static final int PAD_Y_DOWN = 20;
    public static final int PAD_HANDLE = 2;
    public static final int PAD_NAN = 20;
    
    private static final Color COLOR_TRANSPARENT_GRAY = new Color(164,164,164,128);
    private static final Color COLOR_SELECTED = new Color(164,80,40,128);
    

    protected Font m_valuesFont = null;
    protected FontMetrics m_valuesFontMetrics = null;
    
    private ArrayList<AbstractValue> m_values = null;
    private HashMap<Integer, AbstractValue> m_rowIndexToValueMap = null;
    private boolean m_hasNan = false;
    private boolean m_log = false;
    private int m_firstNonNanValueIndex = -1;
    
    private String m_columnName = null;
    
    private int m_x;
    private int m_y = PAD_Y_UP;
    private int m_heightTotal;
    
    private boolean m_numericAxis;
    private boolean m_discreteAxis;
    
    private int m_id;
    private final PlotParallelCoordinates m_plot;
    
    private double m_selectionMinPercentage = 0;
    private double m_selectionMaxPercentage = 1;
    
    private int m_movingX = 0;
    
    private boolean m_displaySelected = false;
    private Double m_rangeMin = null;
    private Double m_rangeMax = null;
    private Double m_rangeMinWithNaN = null;
    private boolean m_forceNaN = false;

    
    private enum OverSubObject {
      HANDLE_UP,
      HANDLE_BOTTOM,
      SCROLLBAR,
      NONE
    };
    
    private OverSubObject m_overSubObject = OverSubObject.NONE;
    
    
    public ParallelCoordinatesAxis(int id, ExtendedTableModelInterface compareDataInterface, int colId, PlotParallelCoordinates plot) {
        Class dataClass = compareDataInterface.getDataColumnClass(colId);

        m_id = id;
        m_plot = plot;

        
        m_values = new ArrayList<>(compareDataInterface.getRowCount());
        m_rowIndexToValueMap = new HashMap<>();
        
        
        if (dataClass.equals(String.class)) {
            prepareStringData(compareDataInterface, colId);
            m_numericAxis = false;
            m_discreteAxis = true;
            Collections.sort(m_values);
            m_firstNonNanValueIndex = m_values.size()-1;
        } else if ((dataClass.equals(Long.class)) || (dataClass.equals(Integer.class))) {
            prepareNumberData(compareDataInterface, colId);
            m_numericAxis = true;
            m_discreteAxis = true;
            Collections.sort(m_values, Collections.reverseOrder());
            m_firstNonNanValueIndex = m_values.size()-1;
            m_rangeMin = ((NumberValue)m_values.get(m_firstNonNanValueIndex)).doubleValue();
            m_rangeMax = ((NumberValue)m_values.get(0)).doubleValue();
        } else { // Float or Double
            prepareNumberData(compareDataInterface, colId);
            m_numericAxis = true;
            m_discreteAxis = false;            
            Collections.sort(m_values, Collections.reverseOrder());
            
            searchFirstNonNaNValue();
            m_rangeMin = ((NumberValue)m_values.get(m_firstNonNanValueIndex)).doubleValue();
            m_rangeMax = ((NumberValue)m_values.get(0)).doubleValue();
        }
        m_rangeMinWithNaN = m_rangeMin;

        
        m_columnName = compareDataInterface.getDataColumnIdentifier(colId);

        if (dataClass.equals(String.class)) {
            int iDiff = 0;
            String prev = null;
            for (int i = 0; i < m_values.size(); i++) {
                String v = ((StringValue) m_values.get(i)).toString();
                if ((prev!=null) && (! prev.equals(v))) {
                    iDiff++;
                }
                prev = v;
                m_values.get(i).setIndex(iDiff);
            }
        } else {
            for (int i=0;i<m_values.size();i++) {
                m_values.get(i).setIndex(i);
            }
        }


    }
    
    public int getId() {
        return m_id;
    }
    public void setId(int id) {
        m_id = id;
    }
    
    public double getRealMaxValue() {
        return ((NumberValue) m_values.get(0)).doubleValue();
    }

    public double getRealMinValue() {
        return ((NumberValue) m_values.get(m_firstNonNanValueIndex)).doubleValue();
    }
    
    public void setRange(Double rangeMin, Double rangeMax, boolean forceNaN) {
        
        // to be able to position correctly the selection zone
        double vMin = (m_rangeMax-m_rangeMinWithNaN)*(1-m_selectionMinPercentage)+m_rangeMinWithNaN;
        double vMax = (m_rangeMax-m_rangeMinWithNaN)*(1-m_selectionMaxPercentage)+m_rangeMinWithNaN;
        
        m_rangeMin = rangeMin;
        m_rangeMinWithNaN = rangeMin;
        m_rangeMax = rangeMax;
        m_forceNaN = forceNaN;
        
        // set position of selected zone correctly
        m_selectionMinPercentage = 1-(vMin-m_rangeMin)/(m_rangeMax-m_rangeMin);
        m_selectionMaxPercentage = 1-(vMax-m_rangeMin)/(m_rangeMax-m_rangeMin);
        
    }
    
    public void paint(Graphics2D g, int plotWidth) {
        
        if (m_valuesFont == null) {
            m_valuesFont = g.getFont().deriveFont(Font.PLAIN, 10);
            m_valuesFontMetrics = g.getFontMetrics(m_valuesFont);
        }

        g.setColor(Color.black);
        int x = getX();
        g.drawLine(x, m_y, x, m_y + m_heightTotal);
        
        
        int y1 = (int) Math.round(m_y+m_heightTotal*m_selectionMinPercentage);
        int y2 =(int) Math.round(m_y+m_heightTotal*m_selectionMaxPercentage);
        int height = (int) Math.round(m_heightTotal*(m_selectionMaxPercentage-m_selectionMinPercentage));
        
        g.setColor(m_displaySelected ? COLOR_SELECTED : COLOR_TRANSPARENT_GRAY);
        g.fillRect(m_x, y1, AXIS_WIDTH, height);
        
        g.setColor(Color.white);
        g.drawRect(m_x, y1, AXIS_WIDTH, height);
        int handleSize = AXIS_WIDTH-PAD_HANDLE*2;
        g.drawRect(m_x+PAD_HANDLE,y1+PAD_HANDLE,handleSize,handleSize);
        g.drawRect(m_x+PAD_HANDLE,y1+height-PAD_HANDLE-handleSize,handleSize,handleSize);
        
        if (Math.abs(m_movingX)>10) {
            g.setColor(Color.gray);
            g.drawLine(x+m_movingX, m_y, x+m_movingX, m_y + m_heightTotal);
            
            g.setColor( COLOR_TRANSPARENT_GRAY);
            g.fillRect(m_x+m_movingX, y1, AXIS_WIDTH, height);
            
            g.setColor( Color.lightGray);
            g.drawRect(m_x+m_movingX, y1, AXIS_WIDTH, height);
            
        }
        

        g.setFont(m_valuesFont);
        int fontHeight = m_valuesFontMetrics.getHeight();

        // Display Column Name
        int textX = xForLabel(m_columnName, plotWidth);
        int textY = fontHeight+4;
        if (m_displaySelected) {
            Rectangle2D r = m_valuesFontMetrics.getStringBounds(m_columnName, g);
            g.setColor(Color.white);
            g.fillRect(((int) Math.round(r.getX()))+textX-2, ((int) Math.round(r.getY()))+textY, ((int) Math.round(r.getWidth()))+4, ((int) Math.round(r.getHeight())));
        }
        g.setColor(Color.black);
        g.drawString(m_columnName, textX, textY);

        // Display Selected values
        if (m_numericAxis) {
            double vMin = m_rangeMinWithNaN;
            double vMax = ((NumberValue) m_values.get(0)).doubleValue();

            // min
            String min;
            if (m_discreteAxis) {
                min = (m_firstNonNanValueIndex == -1) || (vMin < m_rangeMin) ? "NaN" : Integer.toString((int) Math.round(Math.floor(m_rangeMin)));
            } else {
                min = String.format("%6.5e", m_rangeMin);
            }


            textX = xForLabel(min, plotWidth);
            textY = m_y+m_heightTotal+fontHeight+4;
            if (m_displaySelected) {
                Rectangle2D r = m_valuesFontMetrics.getStringBounds(min, g);
                g.setColor(Color.white);
                g.fillRect(((int) Math.round(r.getX()))+textX-2, ((int) Math.round(r.getY()))+textY, ((int) Math.round(r.getWidth()))+4, ((int) Math.round(r.getHeight())));
            }
            g.setColor(Color.black);
            g.drawString(min, textX, textY);
            
            // max
            String max;
            if (m_discreteAxis) {
                max = (m_firstNonNanValueIndex == -1) || (vMax < m_rangeMin) ? "NaN" : Integer.toString((int) Math.round(Math.floor(m_rangeMax)));
            } else {
                max = String.format("%6.5e", m_rangeMax);
            }


            textX = xForLabel(max, plotWidth);
            textY = PAD_Y_UP/2+fontHeight+4;
            if (m_displaySelected) {
                Rectangle2D r = m_valuesFontMetrics.getStringBounds(max, g);
                g.setColor(Color.white);
                g.fillRect(((int) Math.round(r.getX()))+textX-2, ((int) Math.round(r.getY()))+textY, ((int) Math.round(r.getWidth()))+4, ((int) Math.round(r.getHeight())));
            }
            g.setColor(Color.black);
            g.drawString(max, textX, textY);
            
            // Min Value Selected
            if (y1>m_y) {
                double v = (m_rangeMax-m_rangeMinWithNaN)*(1-m_selectionMinPercentage)+m_rangeMinWithNaN;

                String minValueSelected;
                if (m_discreteAxis) {
                    minValueSelected = (m_firstNonNanValueIndex == -1) || (v < m_rangeMin) ? "NaN" : Integer.toString((int) Math.round(Math.floor(v)));
                } else {
                    minValueSelected = (m_firstNonNanValueIndex==-1) || ( v < m_rangeMin )  ? "NaN" : String.format("%6.5e", v);
                }
                
                Rectangle2D r = m_valuesFontMetrics.getStringBounds(minValueSelected, g);
                textX = xForLabel(minValueSelected, plotWidth);
                textY = y1-fontHeight;
                g.setColor(Color.white);
                g.fillRect(((int) Math.round(r.getX()))+textX-2, ((int) Math.round(r.getY()))+textY, ((int) Math.round(r.getWidth()))+4, ((int) Math.round(r.getHeight())));
                
                g.setColor(Color.black);
                g.drawString(minValueSelected, textX, textY);
            }
            
            // Max Value Selected
             if (y2 < m_y+m_heightTotal) {
                double v = (m_rangeMax-m_rangeMinWithNaN)*(1-m_selectionMaxPercentage)+m_rangeMinWithNaN;
                
                String maxValueSelected;
                if (m_discreteAxis) {
                    maxValueSelected = (m_firstNonNanValueIndex == -1) || (v < m_rangeMin) ? "NaN" : Integer.toString((int) Math.round(Math.ceil(v)));
                } else {
                    maxValueSelected = (m_firstNonNanValueIndex==-1) || ( v < m_rangeMin )  ? "NaN" : String.format("%6.5e", v);
                }
                
                
                Rectangle2D r = m_valuesFontMetrics.getStringBounds(maxValueSelected, g);
                textX = xForLabel(maxValueSelected, plotWidth);
                textY = y2 + fontHeight + 4;
                g.setColor(Color.white);
                g.fillRect(((int) Math.round(r.getX()))+textX-2, ((int) Math.round(r.getY()))+textY, ((int) Math.round(r.getWidth()))+4, ((int) Math.round(r.getHeight())));
                
                g.setColor(Color.black);
                g.drawString(maxValueSelected, textX, textY);
            }
            
        } else {

            
            // Display Min Value
            String valueMin = m_values.get(0).toString();


            textX = xForLabel(valueMin, plotWidth);
            textY = PAD_Y_UP/2+fontHeight+4;
            if (m_displaySelected) {
                Rectangle2D r = m_valuesFontMetrics.getStringBounds(valueMin, g);
                g.setColor(Color.white);
                g.fillRect(((int) Math.round(r.getX()))+textX-2, ((int) Math.round(r.getY()))+textY, ((int) Math.round(r.getWidth()))+4, ((int) Math.round(r.getHeight())));
            }

            g.setColor(Color.black);
            g.drawString(valueMin, textX, textY);

            // Display Max Value
            String valueMax = m_values.get(m_values.size() - 1).toString();

            textX = xForLabel(valueMax, plotWidth);
            textY = m_y + m_heightTotal + fontHeight + 4;
            if (m_displaySelected) {
                Rectangle2D r = m_valuesFontMetrics.getStringBounds(valueMax, g);
                g.setColor(Color.white);
                g.fillRect(((int) Math.round(r.getX()))+textX-2, ((int) Math.round(r.getY()))+textY, ((int) Math.round(r.getWidth()))+4, ((int) Math.round(r.getHeight())));
            }
            g.setColor(Color.black);

            g.drawString(valueMax, textX, textY);

            // Min Value Selected
            if (y1>m_y) {
                int index = (int) Math.round((m_values.size()-1)*m_selectionMinPercentage+0.5d);
                if (index>m_values.size()-1) {
                    index = m_values.size()-1;
                } else if (index<0){
                    index = 0;
                }
                valueMin = m_values.get(index).toString();
                
                Rectangle2D r = m_valuesFontMetrics.getStringBounds(valueMin, g);
                textX = xForLabel(valueMin, plotWidth);
                textY =  y1-fontHeight;
                g.setColor(Color.white);
                g.fillRect(((int) Math.round(r.getX()))+textX-2, ((int) Math.round(r.getY()))+textY, ((int) Math.round(r.getWidth()))+4, ((int) Math.round(r.getHeight())));
                
                g.setColor(Color.black);
                g.drawString(valueMin, textX, textY);
            }

            // Max Value Selected
             if (y2 < m_y+m_heightTotal) {
                int index = (int) Math.round((m_values.size()-1)*m_selectionMaxPercentage-0.5d);
                if (index>m_values.size()-1) {
                    index = m_values.size()-1;
                } else if (index<0){
                    index = 0;
                }
                valueMax = m_values.get(index).toString();
                
                Rectangle2D r = m_valuesFontMetrics.getStringBounds(valueMax, g);
                textX =  xForLabel(valueMax, plotWidth);
                textY =  y2 + fontHeight + 4;
                g.setColor(Color.white);
                g.fillRect(((int) Math.round(r.getX()))+textX-2, ((int) Math.round(r.getY()))+textY, ((int) Math.round(r.getWidth()))+4, ((int) Math.round(r.getHeight())));
                
                g.setColor(Color.black);
                g.drawString(valueMax, textX, textY);
            }
        }

    }
    
    private int xForLabel(String label, int plotWidth) {
        int stringWidth = m_valuesFontMetrics.stringWidth(label);
        int x = getX()-stringWidth/2;
        if (x<0) {
            x = 0;
        } else if (x+stringWidth>=plotWidth) {
            x = plotWidth-stringWidth-1;
        }
        return x;
    }
    
    public void setPosition(int x, int height) {
        m_x = x;
        m_heightTotal = height-PAD_Y_UP-PAD_Y_DOWN;
        
        manageNaN();

    }
    
    public boolean hasNaN() {
        return m_hasNan;
    }
    
    private void manageNaN() {
        if (m_hasNan || m_forceNaN) {

            // replace NaN Values by a small value which will be put at PAD_NAN
            double naNValue;
            if (m_firstNonNanValueIndex == -1) {
                // only  NaN values
                naNValue = 0d;
            } else {
                double min = ((NumberValue) m_values.get(m_firstNonNanValueIndex)).doubleValue();
                double max = ((NumberValue) m_values.get(0)).doubleValue();
                naNValue = (max == min) ? min - 1 : max - ((double) m_heightTotal) * ((max - min) / ((double) (m_heightTotal - PAD_NAN)));
                m_rangeMinWithNaN = naNValue;
                
            }
            for (int i = m_values.size() - 1; i > m_firstNonNanValueIndex; i--) {
                ((NumberValue) m_values.get(i)).setValue(naNValue);
                ((NumberValue) m_values.get(i)).setError(true);
            }
            for (int i = m_firstNonNanValueIndex; i >=0; i--) {
                ((NumberValue) m_values.get(i)).setError(false);
            }
        }
    }
    private void putBackNaN() {
        // put back NaN value
        for (int i = m_values.size() - 1; i > m_firstNonNanValueIndex; i--) {
            NumberValue v = (NumberValue) m_values.get(i);
            v.setValue(Double.NaN);
        }

    }
    
    private void setNegativeValuesAsNaN() {
        int indexFound = -1;
        for (int i = m_firstNonNanValueIndex;i>=0;i--) {
            NumberValue v = (NumberValue) m_values.get(i);
            if (v.doubleValue()<1e-14) {
                v.setValue(Double.NaN);
                indexFound = i;
            } else {
                break;
            }
            
        }
        if (indexFound!=-1) {
            m_firstNonNanValueIndex = indexFound - 1;
        }
    }
    
    private void searchFirstNonNaNValue() {
        m_firstNonNanValueIndex = m_values.size() - 1;
        if (m_values.get(m_values.size() - 1).isNan()) {
            m_hasNan = true;
            m_firstNonNanValueIndex = -1;
            for (int i = m_values.size() - 1; i >= 0; i--) {
                if (!m_values.get(i).isNan()) {
                    m_firstNonNanValueIndex = i;
                    break;
                }
            }

        }
    }
    
    public boolean isRowIndexSelected(int rowIndex) {
        
        if ((m_selectionMinPercentage<=1e-10) && (m_selectionMaxPercentage-1>=-1e-10)) {
            return true;
        }

        AbstractValue v = m_rowIndexToValueMap.get(rowIndex);
        if (m_numericAxis) {
            double value = ((NumberValue) v).doubleValue();
            double vMin = m_rangeMinWithNaN;
            double vMax = m_rangeMax;
            double maxSelected = (vMax-vMin)*(1-m_selectionMinPercentage)+vMin;
            if (value>maxSelected) {
                return false;
            }
            double minSelected = (vMax - vMin) * (1-m_selectionMaxPercentage) +vMin;
            if (value<minSelected) {
                return false;
            }
            
        } else { // String
            
            int srcIndex = v.getIndex();
            
            int indexMin = (int) Math.round((m_values.size()-1)*m_selectionMinPercentage+0.5d-1e-10);
            int indexMax = (int) Math.round((m_values.size()-1)*m_selectionMaxPercentage-0.5d+1e-10);

            return ((indexMax>=srcIndex) && (indexMin<=srcIndex));


        }
        
        return true;
    }

    
    public int getPositionByRowIndex(int rowIndex) {
        return getRelativePositionByRowIndex(rowIndex)+m_y;
    }
    
    public int getRelativePositionByRowIndex(int rowIndex) {
        AbstractValue v = m_rowIndexToValueMap.get(rowIndex);

        if (m_numericAxis) {
            if (!m_hasNan && !m_forceNaN) {
                double min = m_rangeMin;
                double max = m_rangeMax;
                double value = ((NumberValue) v).doubleValue();

                return (int) ((1d - ((value - min) / (max - min))) * m_heightTotal);
            } else {
                double min = m_rangeMinWithNaN;
                double max = m_rangeMax;
                double value = ((NumberValue) v).doubleValue();

                return (int) ((1d - ((value - min) / (max - min))) * m_heightTotal);
            }
        } else {
            int maxIndex = m_values.get(m_values.size()-1).getIndex();
            if (maxIndex == 0) {
                maxIndex = 1;
            }
           return (int) Math.round(   ((double)v.getIndex())/((double)maxIndex) *m_heightTotal);
        }

    }
    
    public boolean isOnError(int rowIndex) {
        AbstractValue v = m_rowIndexToValueMap.get(rowIndex);
        return v.error();
    }
    
    public int getX() {
        return m_x + AXIS_WIDTH/2;
    }

    public int getHeight() {
        return m_heightTotal;
    }
    
    public int getRowIndexFromIndex(int index) {
        return m_values.get(index).getRowIndex();
    }
    
    private void prepareStringData(ExtendedTableModelInterface compareDataInterface, int colId) {

        int nbRows = compareDataInterface.getRowCount();
        for (int i = 0; i < nbRows; i++) {
            String value = (String) compareDataInterface.getDataValueAt(i, colId);
            if (value == null) {
                value = ""; // null values are considered as empty string
            }
            StringValue nValue = new StringValue(value, i);
            m_values.add(nValue);
            m_rowIndexToValueMap.put(i, nValue);
        }
        
        

    }
    
    private void prepareNumberData(ExtendedTableModelInterface compareDataInterface, int colId) {

        int nbRows = compareDataInterface.getRowCount();
        for (int i = 0; i < nbRows; i++) {
            Number v = (Number) compareDataInterface.getDataValueAt(i, colId);
            NumberValue nValue;
            if (v != null) {
                nValue = new NumberValue(v, i);
            } else {
                nValue = new NumberValue(Double.NaN, i); // Null values as considered as NaN
            }
            m_values.add(nValue);
            m_rowIndexToValueMap.put(i, nValue);
        }

    }
    
    public boolean isLog() {
        return m_log;
    }
    
    public boolean canLog() {
        if (!m_numericAxis || m_log) {
            return false;
        };

        return true;
    }
    
    public void log() {

        // calculate logged vmin and vmax
        double vMin = (m_rangeMax-m_rangeMinWithNaN)*(1-m_selectionMinPercentage)+m_rangeMinWithNaN;
        double vMax = (m_rangeMax-m_rangeMinWithNaN)*(1-m_selectionMaxPercentage)+m_rangeMinWithNaN;
        vMin = Math.log10(vMin);
        vMax = Math.log10(vMax);

        
        m_log = true;
        
        // put back NaN value
       putBackNaN();
       
       // treat values <10e-14 as NaN values
       setNegativeValuesAsNaN();
        
        // log other values
        for (int i = m_firstNonNanValueIndex; i >= 0; i--) {
            NumberValue v = (NumberValue) m_values.get(i);
            ((NumberValue) v).log();
        }
        
        m_hasNan = m_firstNonNanValueIndex != -1;

        // replace NaN values for display
        manageNaN();
        
        // update range
        m_rangeMin = ((NumberValue)m_values.get(m_firstNonNanValueIndex)).doubleValue();
        m_rangeMinWithNaN = m_rangeMin;
        m_rangeMax = ((NumberValue)m_values.get(0)).doubleValue();
        
        
        // set position of selected zone correctly
        m_selectionMinPercentage = 1-(vMin-m_rangeMin)/(m_rangeMax-m_rangeMin);
        m_selectionMaxPercentage = 1-(vMax-m_rangeMin)/(m_rangeMax-m_rangeMin);
        
        // modify column name
        m_columnName = "log10("+m_columnName+")";
    }
    
    public void doubleClicked() {
        if ((m_selectionMinPercentage>0) || (m_selectionMaxPercentage<1)) {
            m_selectionMinPercentage = 0;
            m_selectionMaxPercentage = 1;
            setSelected(true, false);
            m_plot.axisChanged();
        }
    }

    public MoveableInterface getOverMovable(int x, int y) {

        if ((x>=m_x) && (x<=m_x+AXIS_WIDTH) && (y>=m_y) && (y<=m_y+m_heightTotal)) {
            
            int y1 = (int) Math.round(m_y + m_heightTotal * m_selectionMinPercentage);
            int y2 = (int) Math.round(m_y + m_heightTotal * m_selectionMaxPercentage);

            if ((y>=y1) && (y<=y1+AXIS_WIDTH)) {
                m_overSubObject = OverSubObject.HANDLE_UP;
            } else if ((y <= y2) && (y >= y2 - AXIS_WIDTH)) {
                m_overSubObject = OverSubObject.HANDLE_BOTTOM;
            } else if ((y>=y1) && (y<=y2)) {
                m_overSubObject = OverSubObject.SCROLLBAR;
            } else {
                m_overSubObject = OverSubObject.NONE; // should not happen !
            }
            
            m_movingX = 0;
            
            return this;
        }
        m_overSubObject = OverSubObject.NONE;
        return null;
    }

    @Override
    public boolean insideXY(int x, int y) {
        return true;
    }

    @Override
    public void moveDXY(int deltaX, int deltaY) {

        if (deltaY != 0) {
            double deltaMinHandle = ((double) AXIS_WIDTH + PAD_HANDLE) / m_heightTotal;
            double percentageMove = ((double) deltaY) / ((double) m_heightTotal);
            switch (m_overSubObject) {
                case HANDLE_UP:
                    m_selectionMinPercentage += percentageMove;
                    if (m_selectionMinPercentage <= 0) {
                        m_selectionMinPercentage = 0;
                    } else if (m_selectionMinPercentage + deltaMinHandle >= m_selectionMaxPercentage) {
                        m_selectionMinPercentage = m_selectionMaxPercentage - deltaMinHandle;
                    }
                    m_plot.axisChanged();
                    break;
                case HANDLE_BOTTOM:
                    m_selectionMaxPercentage += percentageMove;
                    if (m_selectionMaxPercentage >= 1) {
                        m_selectionMaxPercentage = 1;
                    } else if (m_selectionMinPercentage + deltaMinHandle >= m_selectionMaxPercentage) {
                        m_selectionMaxPercentage = m_selectionMinPercentage + deltaMinHandle;
                    }
                    m_plot.axisChanged();
                    break;
                case SCROLLBAR:
                    if (m_selectionMinPercentage + percentageMove < 0) {
                       percentageMove = -m_selectionMinPercentage;
                    } else if (m_selectionMaxPercentage + percentageMove > 1) {
                        percentageMove = 1-m_selectionMaxPercentage;
                    }
                    if (percentageMove != 0) {
                        m_selectionMinPercentage += percentageMove;
                        m_selectionMaxPercentage += percentageMove;
                        m_plot.axisChanged();
                    }
                    break;
                case NONE:
                    // should not happen
                    break;
            }
        }
        if ((deltaX != 0) && (! m_overSubObject.equals(OverSubObject.HANDLE_BOTTOM)) && (!m_overSubObject.equals(OverSubObject.HANDLE_UP)))  {
            boolean plottingMovableAxisPrevious = Math.abs(m_movingX) > 10;
            m_movingX += deltaX;
            boolean plottingMovableAxis = Math.abs(m_movingX) > 10;
            
            if (plottingMovableAxisPrevious ^ plottingMovableAxis) {
                m_plot.axisIsMoving();
            }
        }
    }
    


    @Override
    public boolean isMoveable() {
        return true;
    }

    @Override
    public void snapToData(boolean isCtrlOrShiftDown) {
        m_plot.selectAxis(this, isCtrlOrShiftDown);
        if (Math.abs(m_movingX) > 5) {
            m_plot.axisMoved(this, m_movingX);
        }
        m_movingX = 0;
    }

    @Override
    public void setSelected(boolean s, boolean isCtrlOrShiftDown) {
        m_plot.selectAxis(this, isCtrlOrShiftDown);
    }
    
    public void displaySelected(boolean selected) {
        m_displaySelected = selected;
    }
    
    public boolean isSelected() {
        return m_displaySelected;
    }
    
    public boolean isNumeric() {
        return m_numericAxis;
    }

}
