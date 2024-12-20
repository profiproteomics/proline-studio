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
package fr.proline.studio.graphics;

import fr.proline.studio.NbPreferences;
import fr.proline.studio.WindowManager;
import fr.proline.studio.graphics.cursor.AbstractCursor;
import fr.proline.studio.graphics.cursor.CursorInfo;
import fr.proline.studio.graphics.cursor.CursorInfoList;
import fr.proline.studio.graphics.cursor.HorizontalCursor;
import fr.proline.studio.graphics.cursor.VerticalCursor;
import fr.proline.studio.graphics.marker.AbstractMarker;
import fr.proline.studio.graphics.marker.LabelMarker;
import fr.proline.studio.graphics.marker.coordinates.PercentageCoordinates;
import fr.proline.studio.parameter.ColorOrGradientParameter;
import fr.proline.studio.parameter.ColorParameter;
import fr.proline.studio.parameter.DefaultParameterDialog;
import fr.proline.studio.parameter.IntegerParameter;
import fr.proline.studio.parameter.ParameterList;
import fr.proline.studio.parameter.StringParameter;
import fr.proline.studio.utils.CyclicColorPalette;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.LinearGradientPaint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import javax.swing.JSlider;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;
import fr.proline.studio.extendedtablemodel.ExtraDataForTableModelInterface;
import fr.proline.studio.extendedtablemodel.LogAdapterModel;
import fr.proline.studio.graphics.core.PlotToolbarListenerInterface;
import fr.proline.studio.graphics.measurement.WidthMeasurement;
import fr.proline.studio.parameter.AbstractLinkedParameters;
import fr.proline.studio.parameter.DoubleParameter;
import fr.proline.studio.parameter.ObjectParameter;
import javax.swing.JComboBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Scatter Plot
 *
 * @author JM235353
 */
public class PlotScatter extends PlotXYAbstract implements Axis.EnumXInterface, Axis.EnumYInterface {

    protected static final Logger logger = LoggerFactory.getLogger("ProlineStudio.Common");
    protected double m_xMin;
    protected double m_xMax;
    protected double m_yMin;
    protected double m_yMax;

    protected double[] m_dataX;
    protected double[] m_dataY;
    protected int[] m_jitterX;
    protected int[] m_jitterY;
    private String[] m_enumX;
    private String[] m_enumY;

    // next 6 variable are for color
    private double m_gradientParamValuesMin;
    private double m_gradientParamValuesMax;
    private double[] m_gradientParamValues;
    private int m_gradientParamCol = -1;
    private Color[] m_gradientColors;
    private float[] m_gradientFractions;

    protected boolean[] m_selected;
    private long[] m_ids;
    private HashMap<Long, Integer> m_idToIndex;

    
    public static final String PLOT_PARAMETER_LIST_KEY = "Plots";
    
    public static final int LOG_SUPPRESS_VALUES = 0;
    public static final int LOG_REPLACE_VALUES = 1;
    public static final int DEFAULT_LOG_ALGO = 0;
    
    public static final String LOG_ALGO_KEY = "Log_Algo";
    public static final String LOG_ALGO_NAME = "Log Axis Algorithm";
    public static final String LOG_ALGO_OPTION1 = "Suppress Negative and Null values";
    public static final String LOG_ALGO_OPTION2 = "Replace Negative and Null Values";
    
    public static final String DEFAULT_LOG_REPLACE_VALUE_KEY = "Replacement_Value";
    public static final String DEFAULT_LOG_REPLACE_VALUE_NAME = "Replacement Value";
    
    private static final int SELECT_SENSIBILITY = 8;

    private static final String PLOT_SCATTER_COLOR_KEY = "PLOT_SCATTER_COLOR";
    private static final String PLOT_SCATTER_X_JITTER_KEY = "PLOT_SCATTER_X_JITTER";
    private static final String PLOT_SCATTER_Y_JITTER_KEY = "PLOT_SCATTER_Y_JITTER";

    private final ColorOrGradientParameter m_colorParameter;
    private final IntegerParameter m_jitterXParameter;
    private final IntegerParameter m_jitterYParameter;
    
    private final ObjectParameter m_logAlgoParameter;
    private final DoubleParameter m_replaceValue;

    private ArrayList<ParameterList> m_parameterListArray = null;

    private final LinkedList<GraphicDataGroup> m_dataGroup = new LinkedList<>();
    private final HashMap<Long, GraphicDataGroup> m_idsToGraphicDataGroup = new HashMap<>();
    private final HashMap<GraphicDataGroup, LinkedHashSet<Long>> m_graphicDataGroupToId = new HashMap<>();
    private StringBuilder m_sb = null;

    public PlotScatter(BasePlotPanel plotPanel, ExtendedTableModelInterface compareDataInterface, CrossSelectionInterface crossSelectionInterface, int colX, int colY) {
        super(plotPanel, PlotType.SCATTER_PLOT, compareDataInterface, crossSelectionInterface);
        int[] cols = new int[2]; //JPM.TODO enhance
        cols[COL_X_ID] = colX;
        cols[COL_Y_ID] = colY;

        //update(cols, null); //by call this methode, min max X, min max Y are calculated

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
        
        // Log parameter
        ParameterList plotParameterList = new ParameterList(PlotScatter.PLOT_PARAMETER_LIST_KEY);

        Object[] logOptions = {PlotScatter.LOG_ALGO_OPTION1, PlotScatter.LOG_ALGO_OPTION2};
        JComboBox comboBox = new JComboBox(logOptions);
        
        Object[] objectTable = { PlotScatter.LOG_SUPPRESS_VALUES, PlotScatter.LOG_REPLACE_VALUES };
        m_logAlgoParameter = new ObjectParameter(PlotScatter.LOG_ALGO_KEY, PlotScatter.LOG_ALGO_NAME, comboBox, logOptions, objectTable, PlotScatter.DEFAULT_LOG_ALGO, null);
        plotParameterList.add(m_logAlgoParameter);

        m_replaceValue = new DoubleParameter(PlotScatter.DEFAULT_LOG_REPLACE_VALUE_KEY, PlotScatter.DEFAULT_LOG_REPLACE_VALUE_NAME, JTextField.class, Double.valueOf(1), Double.valueOf(10e-14), Double.valueOf(10e14));
        plotParameterList.add(m_replaceValue);

        plotParameterList.loadParameters(NbPreferences.root());

        AbstractLinkedParameters linkedParameters = new AbstractLinkedParameters(plotParameterList) {

            @Override
            public void valueChanged(String value, Object associatedValue) {
                double valueDouble = Double.parseDouble(m_replaceValue.getStringValue());
                int selection = Integer.parseInt(m_logAlgoParameter.getStringValue());
                showParameter(m_replaceValue, selection == PlotScatter.LOG_REPLACE_VALUES, valueDouble);
                updateParameterListPanel();
            }

        };

        m_logAlgoParameter.addLinkedParameters(linkedParameters);

        update(cols, null); //by call this methode, min max X, min max Y are calculated

        int selection = Integer.parseInt(m_logAlgoParameter.getStringValue());
        linkedParameters.valueChanged((String) logOptions[selection], objectTable[selection]);

        m_parameterListArray = new ArrayList<>(2);
        m_parameterListArray.add(colorParameteList);
        m_parameterListArray.add(settingsParameterList);
        m_parameterListArray.add(plotParameterList);

        addMeasurement(new WidthMeasurement(this));
        
        m_plotPanel.enableButton(PlotToolbarListenerInterface.BUTTONS.VIEW_ALL_MAP, true);
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

        String infoValue;
        int infoColumn = m_compareDataInterface.getInfoColumn();
        if (infoColumn == m_cols[COL_X_ID]) {
            boolean xAsEnum = m_plotPanel.getXAxis().isEnum();
            if (xAsEnum) {
                infoValue = getEnumValueX(indexFound, true);
            } else {
                infoValue = m_compareDataInterface.getDataValueAt(indexFound, m_compareDataInterface.getInfoColumn()).toString();
            }
        } else if (infoColumn == m_cols[COL_Y_ID]) {
            boolean yAsEnum = m_plotPanel.getYAxis().isEnum();
            if (yAsEnum) {
                infoValue = getEnumValueY(indexFound, true, m_plotPanel.getYAxis());
            } else {
                infoValue = m_compareDataInterface.getDataValueAt(indexFound, m_compareDataInterface.getInfoColumn()).toString();
            }
        } else {
            infoValue = m_compareDataInterface.getDataValueAt(indexFound, m_compareDataInterface.getInfoColumn()).toString();

        }

        m_sb.append(infoValue);
        m_sb.append("<BR>");
        m_sb.append(m_plotPanel.getXAxis().getTitle());
        m_sb.append(" : ");
        boolean xAsEnum = m_plotPanel.getXAxis().isEnum();
        if (xAsEnum) {
            m_sb.append(getEnumValueX(indexFound, true));
        } else {
            m_sb.append(m_plotPanel.getXAxis().getExternalDecimalFormat().format(m_dataX[indexFound]));
        }
        m_sb.append("<BR>");
        m_sb.append(m_plotPanel.getYAxis().getTitle());
        m_sb.append(" : ");
        boolean yAsEnum = m_plotPanel.getYAxis().isEnum();
        if (yAsEnum) {
            m_sb.append(getEnumValueY(indexFound, true, m_plotPanel.getYAxis()));
        } else {
            m_sb.append(m_plotPanel.getYAxis().getExternalDecimalFormat().format(m_dataY[indexFound]));
        }

        String tooltip = m_sb.toString();
        m_sb.setLength(0);
        return tooltip;

    }

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
            } else if (!append) {
                m_selected[i] = false;
            }

        }

        return aSelection;
    }

    @Override
    public ArrayList<Long> getSelectedIds() {
        ArrayList<Long> selection = new ArrayList();
        for (int i = 0; i < m_selected.length; i++) {
            if (m_selected[i]) {
                selection.add(m_ids[i]);
            }
        }
        return selection;
    }

    @Override
    public void setSelectedIds(ArrayList<Long> selectedIds) {
        for (int i = 0; i < m_selected.length; i++) {
            m_selected[i] = false;
        }
        for (int i = 0; i < selectedIds.size(); i++) {
            Integer index = m_idToIndex.get(selectedIds.get(i));
            if (index == null) {
                continue;
            }
            m_selected[index] = true;
        }
    }

    /**
     * return nearestDataIndex
     *
     * @param x, pixel x
     * @param y, pixel y
     * @return
     */
    protected int findPoint(double x, double y) {

        double rangeX = m_xMax - m_xMin;
        double rangeY = m_yMax - m_yMin;

        double distanceMin = Double.MAX_VALUE;
        int nearestDataIndex = -1;
        int size = m_dataX == null ? 0 : m_dataX.length;
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

            double normalizedDistanceX = (rangeX <= 10e-10) ? 0 : (x - dataX) / rangeX;
            if (normalizedDistanceX < 0) {
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
            if (Math.abs(m_plotPanel.getXAxis().valueToPixel(x) - m_plotPanel.getXAxis().valueToPixel(m_dataX[nearestDataIndex]) - jitterX) > SELECT_SENSIBILITY) {
                return -1;
            }
            int jitterY = (m_jitterY == null) ? 0 : m_jitterY[nearestDataIndex];
            if (Math.abs(m_plotPanel.getYAxis().valueToPixel(y) - m_plotPanel.getYAxis().valueToPixel(m_dataY[nearestDataIndex]) - jitterY) > SELECT_SENSIBILITY) {
                return -1;
            }

        }

        return nearestDataIndex;
    }

    @Override
    public double getNearestXData(double x) {

        double distanceMin = Double.MAX_VALUE;
        int nearestDataIndex = -1;
        int size = m_dataX == null ? 0 : m_dataX.length;
        for (int i = size - 1; i >= 0; i--) { // reverse loop to select first the data in foreground
            double dataX = m_dataX[i];

            // take in account jitter
            /*if (m_jitterX != null) {
                XAxis xAxis = m_plotPanel.getXAxis();
                int xWithJitter = xAxis.valueToPixel(dataX) + m_jitterX[i];
                dataX = xAxis.pixelToValue(xWithJitter);
            }*/ //JPM.TODO
            double distanceX = (x - dataX);
            if (distanceX < 0) {
                distanceX = -distanceX;
            }

            if (distanceMin > distanceX) {
                distanceMin = distanceX;
                nearestDataIndex = i;
            }

        }

        if (nearestDataIndex != -1) {
            return m_dataX[nearestDataIndex];
        }

        return x;
    }

    @Override
    public double getNearestYData(double y) {

        double distanceMin = Double.MAX_VALUE;
        int nearestDataIndex = -1;
        int size = m_dataY == null ? 0 : m_dataY.length;
        for (int i = size - 1; i >= 0; i--) { // reverse loop to select first the data in foreground
            double dataY = m_dataY[i];

            double distanceY = (y - dataY);
            if (distanceY < 0) {
                distanceY = -distanceY;
            }

            if (distanceMin > distanceY) {
                distanceMin = distanceY;
                nearestDataIndex = i;
            }

        }

        if (nearestDataIndex != -1) {
            return m_dataY[nearestDataIndex];
        }

        return y;
    }

    @Override
    public void parametersChanged() {
        updateJitter();
        boolean logX = m_plotPanel.getXAxis().isLog();
        boolean logY = m_plotPanel.getYAxis().isLog();
        boolean log = logX || logY;
        if (log) {
            update();
        }
    }

    private void updateJitter() {

        // parametersChanged() can be call soon, and so parameters could be not initialized
        if (m_compareDataInterface == null) {
            return;
        }

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

    /**
     * here we get min max X,min max Y
     */
    @Override
    public final void update() {

        int size = m_compareDataInterface.getRowCount();
        if (size == 0) {
            return;
        }
        

 
        
        boolean logX = m_plotPanel.getXAxis().isLog();
        boolean logY = m_plotPanel.getYAxis().isLog();
        boolean log = logX || logY;
        if (log) {
            
            String algo = m_logAlgoParameter.getStringValue();
            boolean algoSupressValues = ((algo == null) || (Integer.parseInt(algo) == PlotScatter.LOG_SUPPRESS_VALUES));

            if (! (m_compareDataInterface instanceof LogAdapterModel)) {
                m_compareDataInterface = new LogAdapterModel(m_compareDataInterface);
                
            }
            if (algoSupressValues) {
                ((LogAdapterModel) m_compareDataInterface).update(LogAdapterModel.POLICY.REMOVE_INCORRECT_VALUES, logX ? m_cols[COL_X_ID] : -1, logY ? m_cols[COL_Y_ID] : -1, null);
            } else {
                String value = m_replaceValue.getStringValue();
                double replaceValueD = (value == null) ? 1d : Double.parseDouble(value);
                ((LogAdapterModel) m_compareDataInterface).update(LogAdapterModel.POLICY.REPLACE_BY_VALUE, logX ? m_cols[COL_X_ID] : -1, logY ? m_cols[COL_Y_ID] : -1, replaceValueD);
            }
        } else if (m_compareDataInterface instanceof LogAdapterModel) {
            m_compareDataInterface = ((LogAdapterModel) m_compareDataInterface).getInnerModel();
        }
        size = m_compareDataInterface.getRowCount();
        

        m_dataX = new double[size];
        m_dataY = new double[size];
        m_selected = new boolean[size];
        m_ids = new long[size];
        m_idToIndex = new HashMap(size);

        // set jitter values
        updateJitter();

        boolean xAsEnum = m_plotPanel.getXAxis().isEnum();
        boolean yAsEnum = m_plotPanel.getYAxis().isEnum();

        if (xAsEnum) {
            HashMap<String, Integer> stringToEnumMap = new HashMap<>();
            HashMap<Integer, String> enumToStringMap = new HashMap<>();

            int enumIndex = 0;
            for (int i = 0; i < size; i++) {

                String stringValue = m_compareDataInterface.getDataValueAt(i, m_cols[COL_X_ID]).toString();

                Integer enumIndexCur = stringToEnumMap.get(stringValue);
                if (enumIndexCur == null) {
                    enumIndexCur = enumIndex;
                    stringToEnumMap.put(stringValue, enumIndexCur);
                    enumToStringMap.put(enumIndexCur, stringValue);
                    enumIndex++;
                }
                m_dataX[i] = enumIndexCur;
                m_selected[i] = false;
                m_ids[i] = m_compareDataInterface.row2UniqueId(i);
                m_idToIndex.put(m_ids[i], i);
            }

            m_enumX = new String[enumToStringMap.size()];
            Iterator<Integer> itEnum = enumToStringMap.keySet().iterator();
            while (itEnum.hasNext()) {
                Integer enumI = itEnum.next();
                m_enumX[enumI] = enumToStringMap.get(enumI);
            }

        } else {
            for (int i = 0; i < size; i++) {
                Object value = m_compareDataInterface.getDataValueAt(i, m_cols[COL_X_ID]);
                m_dataX[i] = (value == null || !Number.class.isAssignableFrom(value.getClass())) ? Double.NaN : ((Number) value).doubleValue(); //CBy TODO : ne pas avoir a tester le type Number
                m_selected[i] = false;
                m_ids[i] = m_compareDataInterface.row2UniqueId(i);
                m_idToIndex.put(m_ids[i], i);
            }
        }

        if (yAsEnum) {
            HashMap<String, Integer> stringToEnumMap = new HashMap<>();
            HashMap<Integer, String> enumToStringMap = new HashMap<>();

            int enumIndex = 0;
            for (int i = 0; i < size; i++) {

                String stringValue = m_compareDataInterface.getDataValueAt(i, m_cols[COL_Y_ID]).toString();

                Integer enumIndexCur = stringToEnumMap.get(stringValue);
                if (enumIndexCur == null) {
                    enumIndexCur = enumIndex;
                    stringToEnumMap.put(stringValue, enumIndexCur);
                    enumToStringMap.put(enumIndexCur, stringValue);
                    enumIndex++;
                }
                m_dataY[i] = enumIndexCur;
            }

            m_enumY = new String[enumToStringMap.size()];
            Iterator<Integer> itEnum = enumToStringMap.keySet().iterator();
            while (itEnum.hasNext()) {
                Integer enumI = itEnum.next();
                m_enumY[enumI] = enumToStringMap.get(enumI);
            }
        } else {
            for (int i = 0; i < size; i++) {
                Object value = m_compareDataInterface.getDataValueAt(i, m_cols[COL_Y_ID]);
                m_dataY[i] = (value == null || !Number.class.isAssignableFrom(value.getClass())) ? Double.NaN : ((Number) value).doubleValue(); //CBy TODO : ne pas avoir a tester le type Number
            }
        }

        // min and max values
        double minX = m_dataX[0];
        double maxX = minX;
        double minY = m_dataY[0];
        double maxY = minY;
        int i = 1;
        while (minX != minX || (minY != minY)) { // NaN values
            if (i >= size) {
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

            if (logX) {
                m_xMax *= 2;
                m_xMin /= 2;
            } else {
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
        }

        if (!yAsEnum) {

            if (logY) {
                m_yMax *= 2;
                m_yMin /= 2;
            } else {
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
        }

        m_plotPanel.updateAxis(this);
        m_plotPanel.setXAxisTitle(m_compareDataInterface.getDataColumnIdentifier(m_cols[COL_X_ID]));
        m_plotPanel.setYAxisTitle(m_compareDataInterface.getDataColumnIdentifier(m_cols[COL_Y_ID]));

        removeAllCursors();

        if (m_compareDataInterface instanceof ExtraDataForTableModelInterface) {
            CursorInfoList cursorInfoList = (CursorInfoList) ((ExtraDataForTableModelInterface) m_compareDataInterface).getColValue(CursorInfoList.class, m_cols[COL_X_ID]);
            if (cursorInfoList != null) {
                for (CursorInfo info : cursorInfoList.getCursorInfoList()) {
                    VerticalCursor cursor = new VerticalCursor(m_plotPanel, info.getValue());
                    info.applyParametersToCursor(cursor);
                    addCursor(cursor);
                }
            }

            cursorInfoList = (CursorInfoList) ((ExtraDataForTableModelInterface) m_compareDataInterface).getColValue(CursorInfoList.class, m_cols[COL_Y_ID]);
            if (cursorInfoList != null) {
                for (CursorInfo info : cursorInfoList.getCursorInfoList()) {
                    HorizontalCursor cursor = new HorizontalCursor(m_plotPanel, info.getValue());
                    info.applyParametersToCursor(cursor);
                    addCursor(cursor);
                }
            }
        }

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
        double d = (value == null || !Number.class.isAssignableFrom(value.getClass())) ? Double.NaN : ((Number) value).doubleValue();
        m_gradientParamValues[0] = d;

        m_gradientParamValuesMin = d;
        m_gradientParamValuesMax = d;

        for (int i = 1; i < size; i++) {
            value = m_compareDataInterface.getDataValueAt(i, m_gradientParamCol);
            d = (value == null || !Number.class.isAssignableFrom(value.getClass())) ? Double.NaN : ((Number) value).doubleValue();
            m_gradientParamValues[i] = d;
            if (d < m_gradientParamValuesMin) {
                m_gradientParamValuesMin = d;
            } else if (d > m_gradientParamValuesMax) {
                m_gradientParamValuesMax = d;
            }
        }

    }

    private Color getColorInGradient(double d) {
        double f = (d - m_gradientParamValuesMin) / (m_gradientParamValuesMax - m_gradientParamValuesMin);
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
    public void paint(Graphics2D g, XAxis xAxis, YAxis yAxis) {      
        // set clipping area
        int clipX = xAxis.valueToPixel(xAxis.getMinValue());
        int clipWidth = xAxis.valueToPixel(xAxis.getMaxValue()) - clipX;
        int clipY = yAxis.valueToPixel(yAxis.getMaxValue());
        int clipHeight = yAxis.valueToPixel(yAxis.getMinValue()) - clipY;
        g.setClip(clipX, clipY, clipWidth, clipHeight);

        boolean isLog = xAxis.isLog() || yAxis.isLog();
        
        ColorOrGradient colorOrGradient = m_colorParameter.getColor();
        boolean useGradient = !colorOrGradient.isColorSelected();

        Color plotColor = Color.black; // default init

        if (!useGradient) {
            plotColor = colorOrGradient.getColor();
        } else {
            setGradientValues(); // set gradient values if needed
        }

        // first plot non selected
        int size = (m_dataX != null) ? m_dataX.length : 0;
        for (int i = 0; i < size; i++) {
            if (m_selected[i]) {
                continue;
            }

            int x = xAxis.valueToPixel(m_dataX[i]) + ((m_jitterX != null) ? m_jitterX[i] : 0);
            int y = yAxis.valueToPixel(m_dataY[i]) + ((m_jitterY != null) ? m_jitterY[i] : 0);

            Color c = null;
            if (isLog && ((LogAdapterModel) m_compareDataInterface).isOnError(i)) {
                c = Color.red;
            } else {
                c = selectColor(plotColor, useGradient, i);
            }
            g.setColor(c);

            g.fillOval(x - 3, y - 3, 6, 6);

        }

        // plot selected
        for (int i = 0; i < size; i++) {
            if (!m_selected[i]) {
                continue;
            }
            int x = xAxis.valueToPixel(m_dataX[i]) + ((m_jitterX != null) ? m_jitterX[i] : 0);
            int y = yAxis.valueToPixel(m_dataY[i]) + ((m_jitterY != null) ? m_jitterY[i] : 0);

            Color c = selectColor(plotColor, useGradient, i);
            g.setColor(c);

            g.fillOval(x - 3, y - 3, 6, 6);

            g.setColor(Color.black);
            g.drawOval(x - 3, y - 3, 6, 6);

        }

    }


    private Color selectColor(Color plotColor, boolean useGradient, int i) {
        Color c = null;
        if (!m_idsToGraphicDataGroup.isEmpty()) {
            GraphicDataGroup dataGroup = m_idsToGraphicDataGroup.get(m_ids[i]);
            if (dataGroup != null) {
                c = dataGroup.getColor();
            } else if (useGradient) {
                c = getColorInGradient(m_gradientParamValues[i]);
            } else {
                c = plotColor;
            }
        } else if (useGradient) {
            c = getColorInGradient(m_gradientParamValues[i]);
        } else {
            c = plotColor;
        }
        return c;
    }

    @Override
    public boolean getDoubleBufferingPolicy() {
        return m_dataX != null && m_dataX.length > 2000;
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
    public boolean canLogXAxis() {
        return true;
    }

    @Override
    public boolean canLogYAxis() {
        return true;
    }

    @Override
    public boolean isMouseOnPlot(double x, double y) {
        return findPoint(x, y) != -1;
    }

    @Override
    public boolean isMouseOnSelectedPlot(double x, double y) {
        int index = findPoint(x, y);
        if (index == -1) {
            return false;
        }
        return m_selected[index];
    }

    @Override
    public JPopupMenu getPopupMenu(double x, double y) {

        boolean onSelection = false;
        int index = findPoint(x, y);
        if (index != -1) {
            onSelection = m_selected[index];
        }
        long id = (index == -1) ? -1 : m_ids[index];

        // search for group
        final GraphicDataGroup group = (id == -1) ? null : m_idsToGraphicDataGroup.get(id);

        JMenuItem addGroupAction = new JMenuItem("Add Group");
        addGroupAction.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                ArrayList<ParameterList> parameterListArray = new ArrayList<>(1);
                // Color parameter
                ParameterList groupParameterList = new ParameterList("Define Group");
                parameterListArray.add(groupParameterList);

                StringParameter groupNameParameter = new StringParameter("GroupName", "Group Name", JTextField.class, "", 0, 32);
                groupParameterList.add(groupNameParameter);

                Color defaultColor = CyclicColorPalette.getColor(21, 128);
                ColorParameter colorParameter = new ColorParameter("GroupColorParameter", "Color", defaultColor, ColorButtonAndPalettePanel.class);
                groupParameterList.add(colorParameter);

                DefaultParameterDialog parameterDialog = new DefaultParameterDialog(WindowManager.getDefault().getMainWindow(), "Plot Parameters", parameterListArray);
                parameterDialog.setLocationRelativeTo(m_plotPanel);
                parameterDialog.setVisible(true);

                if (parameterDialog.getButtonClicked() == DefaultParameterDialog.BUTTON_OK) {
                    String groupName = groupNameParameter.getStringValue();
                    Color c = colorParameter.getColor();
                    GraphicDataGroup dataGroup = new GraphicDataGroup(groupName, c);

                    m_dataGroup.addFirst(dataGroup);
                    LinkedHashSet<Long> selectedIds = new LinkedHashSet<>();
                    for (int i = 0; i < m_selected.length; i++) {
                        if (m_selected[i]) {

                            long id = m_ids[i];
                            GraphicDataGroup oldGroup = m_idsToGraphicDataGroup.get(id);
                            if (oldGroup != null) {
                                m_graphicDataGroupToId.get(oldGroup).remove(id);
                            }

                            m_idsToGraphicDataGroup.put(id, dataGroup);
                            selectedIds.add(id);

                        }
                    }
                    m_graphicDataGroupToId.put(dataGroup, selectedIds);

                    String name = dataGroup.getName();
                    if ((name != null) && (!name.isEmpty())) {
                        // calculation of percentageY is a wart, could be done in a better way
                        double percentageY = 1 - (0.1 * m_dataGroup.size());
                        if (percentageY < 0.1) {
                            percentageY = 0.1;
                        }
                        LabelMarker marker = new LabelMarker(m_plotPanel, new PercentageCoordinates(0.9, percentageY), dataGroup.getName(), LabelMarker.ORIENTATION_XY_MIDDLE, LabelMarker.ORIENTATION_XY_MIDDLE, dataGroup.getColor());
                        dataGroup.setAssociatedMarker(marker);
                        addMarker(marker);
                    }

                    // repaint
                    m_plotPanel.repaintUpdateDoubleBuffer();

                }
            }
        });

        addGroupAction.setEnabled(onSelection);

        JMenuItem selectGroupAction = new JMenuItem("Select Group");
        selectGroupAction.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                int size = m_selected.length;
                for (int i = 0; i < size; i++) {
                    m_selected[i] = false;
                }

                LinkedHashSet<Long> ids = m_graphicDataGroupToId.get(group);
                for (Long id : ids) {

                    int index = m_idToIndex.get(id);
                    m_selected[index] = true;

                }

                // repaint
                m_plotPanel.repaintUpdateDoubleBuffer();

            }
        });
        selectGroupAction.setEnabled(group != null);

        JMenuItem deleteGroupAction = new JMenuItem("Delete Group");
        deleteGroupAction.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                LinkedHashSet<Long> ids = m_graphicDataGroupToId.get(group);
                for (Long id : ids) {
                    m_idsToGraphicDataGroup.remove(id);
                }
                m_graphicDataGroupToId.remove(group);
                m_dataGroup.remove(group);
                AbstractMarker marker = group.getAssociatedMarker();
                if (marker != null) {
                    removeMarker(marker);
                }

                // repaint
                m_plotPanel.repaintUpdateDoubleBuffer();
            }
        });

        deleteGroupAction.setEnabled(group != null);

        AbstractCursor cursor = getOverCursor(m_plotPanel.getXAxis().valueToPixel(x), m_plotPanel.getYAxis().valueToPixel(y));
        boolean isOverCursor = (cursor != null);

        if (isOverCursor) {
            // cursor must be selected
            selectCursor(cursor);
        }

        JMenuItem addVerticalCursorAction = new JMenuItem("Add Vertical Cursor");
        addVerticalCursorAction.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                VerticalCursor c = new VerticalCursor(m_plotPanel, x);
                addCursor(c);
                m_plotPanel.repaint();
            }
        });

        JMenuItem addHorizontalCursorAction = new JMenuItem("Add Horizontal Cursor");
        addHorizontalCursorAction.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                HorizontalCursor c = new HorizontalCursor(m_plotPanel, y);
                addCursor(c);
                m_plotPanel.repaint();
            }
        });

        addVerticalCursorAction.setEnabled(!isOverCursor);
        addHorizontalCursorAction.setEnabled(!isOverCursor);

        JMenuItem deleteCursorAction = new JMenuItem("Delete Cursor");
        deleteCursorAction.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeCursor(cursor);
                m_plotPanel.repaint();
            }
        });
        deleteCursorAction.setEnabled(isOverCursor);

        JCheckBoxMenuItem snapToDataMenuItem = null;
        if (isOverCursor) {
            snapToDataMenuItem = new JCheckBoxMenuItem("Snap to Data");
            snapToDataMenuItem.setSelected(cursor.isSnapToData());
            snapToDataMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    cursor.toggleSnapToData();
                }
            });
        }

        JPopupMenu menu = new JPopupMenu();
        menu.add(addGroupAction);
        menu.add(selectGroupAction);
        menu.add(deleteGroupAction);
        menu.addSeparator();
        menu.add(addVerticalCursorAction);
        menu.add(addHorizontalCursorAction);
        menu.add(deleteCursorAction);
        if (snapToDataMenuItem != null) {
            menu.add(snapToDataMenuItem);
        }

        return menu;
    }

    @Override
    public String getEnumValueX(int index, boolean fromData) {

        if (fromData) {
            if ((index < 0) || (index >= m_dataX.length)) {
                return "";
            }

            return m_enumX[(int) Math.round(m_dataX[index])];
        } else {
            if ((index < 0) || (index >= m_enumX.length)) {
                return "";
            }

            return m_enumX[index];
        }

    }

    @Override
    public String getEnumValueY(int index, boolean fromData, Axis axis) {

        if (fromData) {
            if ((index < 0) || (index >= m_dataY.length)) {
                return "";
            }

            return m_enumY[(int) Math.round(m_dataY[index])];
        } else {
            if ((index < 0) || (index >= m_enumY.length)) {
                return "";
            }

            return m_enumY[index];
        }
    }
}
