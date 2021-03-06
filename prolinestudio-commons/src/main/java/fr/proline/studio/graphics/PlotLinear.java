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

import fr.proline.studio.graphics.marker.coordinates.DataCoordinates;
import fr.proline.studio.graphics.marker.LabelMarker;
import fr.proline.studio.graphics.marker.LineMarker;
import fr.proline.studio.parameter.ColorOrGradientParameter;
import fr.proline.studio.parameter.ParameterList;
import fr.proline.studio.utils.CyclicColorPalette;
import fr.proline.studio.utils.DataFormat;
import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.LinearGradientPaint;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;
import fr.proline.studio.extendedtablemodel.LogAdapterModel;
import static fr.proline.studio.graphics.PlotBaseAbstract.COL_X_ID;
import static fr.proline.studio.graphics.PlotBaseAbstract.COL_Y_ID;
import fr.proline.studio.graphics.core.PlotToolbarListenerInterface;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Scatter Plot or linear
 *
 * @author JM235353
 */
public class PlotLinear extends PlotXYAbstract {

    protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");
    private static final int DEFAULT_POINT_RADIUS = 3;
    private double m_xMin;
    private double m_xMax;
    private double m_yMin;
    private double m_yMax;

    private double[] m_dataX;
    private double[] m_dataY;
    private PlotDataSpec[] m_dataSpec;

    private double m_gradientParamValuesMin;
    private double m_gradientParamValuesMax;
    private double[] m_gradientParamValues;
    private int m_gradientParamCol = -1;
    private Color[] m_gradientColors;
    private float[] m_gradientFractions;

    private boolean[] m_selected;

    // could be an imposed color to display data, a title...
    private PlotInformation m_plotInformation = null;

    private static final int SELECT_SENSIBILITY = 2;
    private static final int SELECT_SENSIBILITY_POW = SELECT_SENSIBILITY * SELECT_SENSIBILITY;

    private static final String PLOT_SCATTER_COLOR_KEY = "PLOT_SCATTER_COLOR";

    private ColorOrGradientParameter m_colorParameter;

    // for linear plots, draw (or not) the points
    private boolean m_isDrawPoints = false;

    // draw a line between points, even if there is a missing value
    private boolean m_isDrawGap = true;

    private static final BasicStroke STROKE_1 = new BasicStroke(1.2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
    private static final BasicStroke STROKE_2 = new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
    private boolean strokeFixed = false;

    private static final float dash[] = {2.0f, 2.0f};
    private static final BasicStroke STROKE_1_DASHED = new BasicStroke(1.2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f);


    private BasicStroke m_strokeLine = STROKE_1;
    private BasicStroke m_userStrock = null;
    private static final BasicStroke EDGE_STROKE = new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
    private static final BasicStroke SELECTED_STROCK = new BasicStroke(4f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
    private static final Color SELECTED_COLOR = new Color(0, 120, 215); //= JTable default getSelectionColor, blue

    private ArrayList<ParameterList> m_parameterListArray = null;
    private boolean m_hasSharedParameters = false; // shared parameter list if for multiple PlotLinear with same color for instance

    private boolean displayAntiAliasing = true;
    private double m_tolerance;

    private Double m_limitMinY = null;  // minY must be <= limitMinY
    
    public PlotLinear(BasePlotPanel plotPanel, ExtendedTableModelInterface compareDataInterface, CrossSelectionInterface crossSelectionInterface, int colX, int colY) {
        this(plotPanel, compareDataInterface, crossSelectionInterface, colX, colY, null, null);
    }
    public PlotLinear(BasePlotPanel plotPanel, ExtendedTableModelInterface compareDataInterface, CrossSelectionInterface crossSelectionInterface, int colX, int colY, Double limitMinY, ArrayList<ParameterList> sharedParameterListArray) {
        super(plotPanel, PlotType.SCATTER_PLOT, compareDataInterface, crossSelectionInterface);
        
        if (sharedParameterListArray != null) {
            m_hasSharedParameters = true;
            m_parameterListArray = sharedParameterListArray;
        }
        
        m_limitMinY = limitMinY;
        
        int[] cols = new int[2]; //JPM.TODO enhance
        cols[COL_X_ID] = colX;
        cols[COL_Y_ID] = colY;
        update(cols, null);

        if (!m_hasSharedParameters) {
            // Color parameter
            ParameterList colorParameterList = new ParameterList("Colors");

            ColorOrGradient colorOrGradient = new ColorOrGradient();
            colorOrGradient.setColor(CyclicColorPalette.getColor(21, 128));//purple bordeaux brightness modified
            float[] fractions = {0.0f, 1.0f};
            Color[] colors = {Color.white, Color.red};
            LinearGradientPaint gradient = ColorOrGradientChooserPanel.getGradientForPanel(colors, fractions);
            colorOrGradient.setGradient(gradient);

            m_colorParameter = new ColorOrGradientParameter(PLOT_SCATTER_COLOR_KEY, "Scatter Plot Color", colorOrGradient, null);
            colorParameterList.add(m_colorParameter);

            m_parameterListArray = new ArrayList<>(1);
            m_parameterListArray.add(colorParameterList);
        
        } else {
            ParameterList colorParameterList = m_parameterListArray.get(0);
            m_colorParameter = (ColorOrGradientParameter) colorParameterList.getParameter(PLOT_SCATTER_COLOR_KEY);
            
        }
        m_tolerance = 0;
        
        m_plotPanel.enableButton(PlotToolbarListenerInterface.BUTTONS.VIEW_ALL_MAP, true);
    }

    /**
     * if the tolerance is not 0, there will be a tolerance edge abover and
     * under the main curve.
     *
     * @param tolerance
     */
    public void setTolerance(double tolerance) {
        m_tolerance = tolerance;
    }

    @Override
    public ArrayList<ParameterList> getParameters() {

        if ((m_plotInformation != null) && (m_plotInformation.getPlotColor() != null)) {
            // color can not be changed
            return null;
        }
        
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
        if (m_plotInformation != null && m_plotInformation.getPlotColor() != null) {
            String rsmHtmlColor = CyclicColorPalette.getHTMLColor(m_plotInformation.getPlotColor());
            m_sb.append("<font color='").append(rsmHtmlColor).append("'>&#x25A0;&nbsp;</font>");
        }
        if (m_plotInformation != null && m_plotInformation.getPlotTitle() != null && !m_plotInformation.getPlotTitle().isEmpty()) {
            m_sb.append(m_plotInformation.getPlotTitle());
        } else {
            m_sb.append(m_compareDataInterface.getDataValueAt(indexFound, m_compareDataInterface.getInfoColumn()).toString());
        }
        if (m_plotInformation != null && m_plotInformation.getPlotInfo() != null) {
            for (Map.Entry<String, String> entrySet : m_plotInformation.getPlotInfo().entrySet()) {
                Object key = entrySet.getKey();
                Object value = entrySet.getValue();
                m_sb.append(key);
                m_sb.append(": ");
                m_sb.append(value);
                m_sb.append("<br>");
            }
            m_sb.append("<br>");
        }
        String labelX;
        if (m_plotPanel.getXAxis().isEnum()) {
            labelX = m_plotPanel.getEnumValueX(indexFound, true);
        } else {
            labelX = DataFormat.format(m_dataX[indexFound], 4);
        }
        String labelY;
        if (m_plotPanel.getYAxis().isEnum()) {
            labelY = m_plotPanel.getEnumValueY(indexFound, true, m_plotPanel.getYAxis());
        } else {
            labelY = DataFormat.format(m_dataY[indexFound], 4);
        }

        m_sb.append("<BR>");
        m_sb.append(m_plotPanel.getXAxis().getTitle());
        m_sb.append(" : ");
        m_sb.append(labelX);
        m_sb.append("<BR>");
        m_sb.append(m_plotPanel.getYAxis().getTitle());
        m_sb.append(" : ");
        m_sb.append(labelY);
        String tooltip = m_sb.toString();
        m_sb.setLength(0);
        return tooltip;

    }
    private StringBuilder m_sb = null;

    @Override
    public boolean select(double x, double y, boolean append) {

        int indexFound = findPoint(x, y);

        int size = m_dataX == null ? 0 : m_dataX.length;
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
        int size = m_dataX == null ? 0 : m_dataX.length;
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
        /*ArrayList<Integer> selection = new ArrayList();
        for (int i = 0; i < m_selected.length; i++) {
            if (m_selected[i]) {
                selection.add(i);
            }
        }
        return selection;*/
        return null;  // not used for the moment
    }

    @Override
    public void setSelectedIds(ArrayList<Long> selection) {
        /*for (int i = 0; i < m_selected.length; i++) {
            m_selected[i] = false;
        }
        for (int i = 0; i < selection.size(); i++) {
            m_selected[selection.get(i)] = true;
        }*/
        // not used for the moment
    }

    /**
     * return the distance between two points
     *
     * @param p1,p2 the two points
     * @return dist the distance
     */
    private static double distance(Point p1, Point p2) {
        int d2 = (p2.x - p1.x) * (p2.x - p1.x) + (p2.y - p1.y) * (p2.y - p1.y);
        //return Math.sqrt(d2);
        return d2;
    }

    public double[] getDataX() {
        return m_dataX;
    }

    public double[] getDataY() {
        return m_dataY;
    }

    /**
     * Return the distance from a point to a segment
     *
     * @param ps,pe the start/end of the segment
     * @param p the given point
     * @return the distance from the given point to the segment
     */
    private static double distanceToSegment(Point ps, Point pe, Point p) {

        if (ps.x == pe.x && ps.y == pe.y) {
            return distance(ps, p);
        }

        int sx = pe.x - ps.x;
        int sy = pe.y - ps.y;

        int ux = p.x - ps.x;
        int uy = p.y - ps.y;

        int dp = sx * ux + sy * uy;
        if (dp < 0) {
            return distance(ps, p);
        }

        int sn2 = sx * sx + sy * sy;
        if (dp > sn2) {
            return distance(pe, p);
        }

        double ah2 = dp * dp / sn2;
        int un2 = ux * ux + uy * uy;
        //return Math.sqrt(un2 - ah2);
        return un2 - ah2;
    }

    /**
     * return the nearest point index for a given point (x,y), depending of the
     * distance to the segments
     *
     * @param x
     * @param y
     * @return
     */
    private int findPoint(double x, double y) {

        double rangeX = m_xMax - m_xMin;
        double rangeY = m_yMax - m_yMin;

        double distanceMin = Double.MAX_VALUE;
        int nearestDataIndex = -1;
        int size = m_dataX == null ? 0 : m_dataX.length;
        for (int i = size - 1; i >= 0; i--) { // reverse loop to select first the data in foreground
            double dataX = m_dataX[i];
            double dataY = m_dataY[i];

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

        XAxis xAxis = m_plotPanel.getXAxis();
        YAxis yAxis = m_plotPanel.getYAxis();

        if (nearestDataIndex != -1) {
            Point p = new Point(xAxis.valueToPixel(x), m_plotPanel.getYAxis().valueToPixel(y));
            Point ps = null;
            Point pe = null;
            Point pnd = new Point(xAxis.valueToPixel(m_dataX[nearestDataIndex]), yAxis.valueToPixel(m_dataY[nearestDataIndex]));
            if (nearestDataIndex > 0) {
                ps = new Point(xAxis.valueToPixel(m_dataX[nearestDataIndex - 1]), yAxis.valueToPixel(m_dataY[nearestDataIndex - 1]));
            }
            if (nearestDataIndex < size - 1) {
                pe = new Point(xAxis.valueToPixel(m_dataX[nearestDataIndex + 1]), yAxis.valueToPixel(m_dataY[nearestDataIndex + 1]));
            }
            if (ps != null) {
                double d0 = distanceToSegment(ps, pnd, p);
                if (Math.abs(d0) <= SELECT_SENSIBILITY_POW) {
                    return nearestDataIndex;
                }
            }
            if (pe != null) {
                double d1 = distanceToSegment(pnd, pe, p);
                if (Math.abs(d1) <= SELECT_SENSIBILITY_POW) {
                    return nearestDataIndex;
                }
            }
        }

        return -1;
    }

    @Override
    public double getNearestXData(double x) {
        return x; //JPM.TODO
    }

    @Override
    public double getNearestYData(double y) {
        return y; //JPM.TODO
    }

    /**
     * from m_col, X, Y, create one m_dataX list, one m_dataY list. These 2
     * lists will be used when Paint
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
            
            if (!(m_compareDataInterface instanceof LogAdapterModel)) {
                m_compareDataInterface = new LogAdapterModel(m_compareDataInterface);
            }

            double replaceValueD = 0.1d;
            ((LogAdapterModel) m_compareDataInterface).update(LogAdapterModel.POLICY.REPLACE_BY_VALUE, -1, logY ? m_cols[COL_Y_ID] : -1, replaceValueD);
            
        } else if (m_compareDataInterface instanceof LogAdapterModel) {
            m_compareDataInterface = ((LogAdapterModel) m_compareDataInterface).getInnerModel();
        }
        size = m_compareDataInterface.getRowCount();
        
        
        
        m_dataX = new double[size];
        m_dataY = new double[size];
        m_dataSpec = new PlotDataSpec[size];
        m_selected = new boolean[size];

        boolean xAsEnum = m_plotPanel.getXAxis().isEnum();
        boolean yAsEnum = m_plotPanel.getYAxis().isEnum();

        for (int i = 0; i < size; i++) {
            if (xAsEnum) {
                m_dataX[i] = i;
            } else {
                Object value = m_compareDataInterface.getDataValueAt(i, m_cols[COL_X_ID]);
                m_dataX[i] = (value == null || !Number.class.isAssignableFrom(value.getClass())) ? Double.NaN : ((Number) value).doubleValue(); //CBy TODO : ne pas avoir a tester le type Number
            }

            if (yAsEnum) {
                m_dataY[i] = i;
            } else {
                Object value = m_compareDataInterface.getDataValueAt(i, m_cols[COL_Y_ID]);
                m_dataY[i] = (value == null || value.equals(Float.NaN) || !Number.class.isAssignableFrom(value.getClass())) ? Double.NaN : ((Number) value).doubleValue(); //CBy TODO : ne pas avoir a tester le type Number
            }
            m_selected[i] = false;
            m_dataSpec[i] = m_compareDataInterface.getDataSpecAt(i);
        }

        // min and max values
        double minX = m_dataX[0];
        double maxX = minX;
        double minY = m_dataY[0];
        double maxY = minY;

        for (int i = 0; i < size; i++) {
            double x = m_dataX[i];
            double y = m_dataY[i];
            if (!Double.valueOf(x).isNaN()) {
                if (Double.valueOf(minX).isNaN()) {
                    minX = x;
                }
                if (Double.valueOf(maxX).isNaN()) {
                    maxX = x;
                }
                minX = Math.min(minX, x);
                maxX = Math.max(maxX, x);
            }
            if (!Double.valueOf(y).isNaN()) {
                if (Double.valueOf(minY).isNaN()) {
                    minY = y;
                }
                if (Double.valueOf(maxY).isNaN()) {
                    maxY = y;
                }
                minY = Math.min(minY, y);
                maxY = Math.max(maxY, y);
            }
        }

        if (Double.valueOf(minX).isNaN() || Double.valueOf(minY).isNaN()) {
            m_dataX = new double[0];
            m_dataY = new double[0];
            m_selected = new boolean[0];
            m_dataSpec = new PlotDataSpec[0];
            return;
        }

        m_xMin = minX;
        m_xMax = maxX;
        
        if (m_limitMinY != null) {
            m_yMin = Math.min(minY, m_limitMinY);
        } else {
            m_yMin = minY;
        }
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
                    // m_xMin = m_xMin - deltaX * 0.01; // no need to have a margin on xMin
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
                    //m_yMin = m_yMin - deltaY * 0.01; // no need to have a margin on yMin
                    m_yMax = m_yMax + deltaY * 0.01;
                }
            }
        }

        //data must be sorted to be drawn correctly in case of linear plot
        sortData();

        double yLabel = m_yMax;
        if (m_compareDataInterface.getExternalData() != null) {
             // we let place at the top to be able to put information
            if (logY) {
                m_yMax *= 10;
            } else {
                m_yMax *= 1.2;
            }
        }
        DecimalFormat df = new DecimalFormat("#.00");

        // add marker if needed
        if (m_compareDataInterface.getExternalData() != null) {
            if (m_compareDataInterface.getExternalData().containsKey(ExtendedTableModelInterface.EXTERNAL_DATA_VERTICAL_MARKER_VALUE)) {
                Object o = m_compareDataInterface.getExternalData().get(ExtendedTableModelInterface.EXTERNAL_DATA_VERTICAL_MARKER_VALUE);
                Object o2 = m_compareDataInterface.getExternalData().get(ExtendedTableModelInterface.EXTERNAL_DATA_VERTICAL_MARKER_TEXT);
                List<Color> colors = null;
                if (m_compareDataInterface.getExternalData().containsKey(ExtendedTableModelInterface.EXTERNAL_DATA_VERTICAL_MARKER_COLOR)) {
                    colors = (List<Color>) m_compareDataInterface.getExternalData().get(ExtendedTableModelInterface.EXTERNAL_DATA_VERTICAL_MARKER_COLOR);
                }
                try {
                    List<Double> values = (List<Double>) o;
                    List<String> texts = new ArrayList();
                    if (o2 != null) {
                        texts = (List<String>) o2;
                    }
                    int j = 0;
                    double minForMarker = m_xMin;
                    double maxForMarker = m_xMax;
                    double delta = 5 * (m_xMax - m_xMin) / 100;
                    double yL = yLabel;
                    for (Double d : values) {
                        addMarker(new LineMarker(m_plotPanel, d, LineMarker.ORIENTATION_VERTICAL));
                        minForMarker = Math.min(minForMarker, d - delta);
                        maxForMarker = Math.max(maxForMarker, d + delta);
                        if (texts.size() >= j) {
                            int orientation = LabelMarker.ORIENTATION_X_RIGHT;
                            if (j > 0) {
                                orientation = LabelMarker.ORIENTATION_X_LEFT;
                            }

                            LabelMarker labelMarker = new LabelMarker(m_plotPanel, new DataCoordinates(d, yL), texts.get(j) + df.format(d), orientation, LabelMarker.ORIENTATION_Y_TOP);
                            if (colors != null && colors.size() >= j) {
                                labelMarker = new LabelMarker(m_plotPanel, new DataCoordinates(d, yL), texts.get(j) + df.format(d), orientation, LabelMarker.ORIENTATION_Y_TOP, colors.get(j));
                            }
                            addMarker(labelMarker);
                        }
                        j++;
                        yL = yLabel * 1.1;
                        // update xMin and xMax if needed
                        m_xMin = Math.min(m_xMin, minForMarker);
                        m_xMax = Math.max(m_xMax, maxForMarker);
                    }
                } catch (Exception e) {
                    // not expected format for the markers
                }
            }
        }

        m_plotPanel.updateAxis(this);
        m_plotPanel.setXAxisTitle(m_compareDataInterface.getDataColumnIdentifier(m_cols[COL_X_ID]));
        m_plotPanel.setYAxisTitle(m_compareDataInterface.getDataColumnIdentifier(m_cols[COL_Y_ID]));
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

    private Color getColorInGradient(LinearGradientPaint gradient, double d) {
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
        int clipHeight = yAxis.valueToPixel(yAxis.getMinValue()) - clipY + 1;
        g.setClip(clipX, clipY, clipWidth, clipHeight);

        boolean isLog = xAxis.isLog() || yAxis.isLog();
        
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

        if (this.m_plotInformation != null && m_plotInformation.getPlotColor() != null) {
            plotColor = this.m_plotInformation.getPlotColor();
        }
        if (displayAntiAliasing) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }

        int size = m_dataX == null ? 0 : m_dataX.length;
        if (size > 0) {
            if ((m_plotInformation!=null) && (m_plotInformation.isSelected())) {
                paintExtendedLinear(g, SELECTED_COLOR, SELECTED_STROCK, 0);
            }
            int x0 = xAxis.valueToPixel(m_dataX[0]);
            int y0 = yAxis.valueToPixel(m_dataY[0]);
            boolean isDef0 = !Double.valueOf(m_dataX[0]).isNaN() && !Double.valueOf(m_dataY[0]).isNaN();

            for (int i = 0; i < size; i++) {
                if (useGradient && (this.m_plotInformation == null || this.m_plotInformation.getPlotColor() == null)) {
                    plotColor = getColorInGradient(gradientPaint, m_gradientParamValues[i]);
                }
                int x = xAxis.valueToPixel(m_dataX[i]);
                int y = yAxis.valueToPixel(m_dataY[i]);
                boolean isDef = !Double.valueOf(m_dataX[i]).isNaN() && !Double.valueOf(m_dataY[i]).isNaN();                
                g.setColor(plotColor);
                if (m_userStrock != null) {
                    g.setStroke(m_userStrock);
                } else {
                    if ((m_plotInformation != null) && (m_plotInformation.isDashed())) {
                        g.setStroke(STROKE_1_DASHED);
                    } else {
                        g.setStroke(m_strokeLine);
                    }
                }
                
                boolean errorOnLog = (isLog && ((LogAdapterModel) m_compareDataInterface).isOnError(i));
                if (errorOnLog || !isDef) {
                    g.setColor(Color.red);
                }
                if (m_isDrawPoints && isDef) {
                    int radius = DEFAULT_POINT_RADIUS;
                    if ((m_dataSpec[i]!=null) && (m_dataSpec[i].getFill().equals(PlotDataSpec.FILL.EMPTY))) {
                        g.drawOval(x - radius, y - radius, radius * 2, radius * 2);
                    } else {
                        g.fillOval(x - radius, y - radius, radius * 2, radius * 2);
                    }
                } else if (errorOnLog || !isDef) {
                    int radius = DEFAULT_POINT_RADIUS;
                    g.fillOval(x - radius, y - radius, radius * 2, radius * 2);
                }
                
                if (m_isDrawGap || (!m_isDrawGap && isDef && isDef0)) {
                    g.setColor(plotColor);
                    if (!isDef || !isDef0) { // there is an error, plot the line as an error
                        if (x0 != x) { // avoid first point
                            prepareGradientErrorColors(plotColor);
                            if (y0 < y) {
                                m_gradientStart.setLocation(x0, y0);
                                m_gradientEnd.setLocation(x, y);
                            } else {
                                m_gradientStart.setLocation(x, y);
                                m_gradientEnd.setLocation(x0, y0);
                            }
                            LinearGradientPaint gradient = new LinearGradientPaint(m_gradientStart, m_gradientEnd, ERROR_GRADIENT_STEPS, ERROR_GRADIENT_COLORS);
                            g.setPaint(gradient);
                        }

                    }
                    g.drawLine(x0, y0, x, y);
                }
                x0 = x;
                y0 = y;
                isDef0 = isDef;
            }

            if (m_tolerance != 0) {
                paintExtendedLinear(g, plotColor, EDGE_STROKE, m_tolerance);
                paintExtendedLinear(g, plotColor, EDGE_STROKE, -m_tolerance);
            }
        }
    }
    private void prepareGradientErrorColors(Color plotColor) {
        ERROR_GRADIENT_COLORS[0] = plotColor;
        ERROR_GRADIENT_COLORS[1] = new Color(plotColor.getRed(), plotColor.getGreen(), plotColor.getBlue(), 64);
        ERROR_GRADIENT_COLORS[2] = new Color(plotColor.getRed(), plotColor.getGreen(), plotColor.getBlue(), 0);
                        
    }
    private static final Point2D m_gradientStart = new Point2D.Double();
    private static final Point2D m_gradientEnd = new Point2D.Double();
    private static final float[] ERROR_GRADIENT_STEPS = {0.0f, 0.3f, 1.0f};
    private static final Color[] ERROR_GRADIENT_COLORS = new Color[3];


    /**
     * if tolerance is not 0, draw a line parallel with the main curve
     *
     * @param g
     * @param color
     * @param tolerance, the distance to de main curve
     */
    private void paintExtendedLinear(Graphics2D g, Color color, Stroke strock, double tolerance) {
        int x0, y0;
        int x, y;
        XAxis xAxis = m_plotPanel.getXAxis();
        YAxis yAxis = m_plotPanel.getYAxis();
        int size = m_dataX == null ? 0 : m_dataX.length;
        x0 = xAxis.valueToPixel(m_dataX[0]);
        y0 = yAxis.valueToPixel(m_dataY[0] + tolerance);
        boolean isDef0 = !Double.valueOf(m_dataX[0]).isNaN() && !Double.valueOf(m_dataY[0]).isNaN();
        int radius = (m_isDrawPoints) ? DEFAULT_POINT_RADIUS:0;
        for (int i = 0; i < size; i++) {
            boolean isDef = !Double.valueOf(m_dataX[i]).isNaN() && !Double.valueOf(m_dataY[i]).isNaN();
            x = xAxis.valueToPixel(m_dataX[i]);
            y = yAxis.valueToPixel(m_dataY[i] + tolerance);
            g.setStroke(strock);
            g.setColor(color);
            if (m_isDrawGap || (!m_isDrawGap && isDef && isDef0)) {
                g.drawLine(x0 + radius, y0, x, y);
            }
            if (m_isDrawPoints && isDef) {
                g.setColor(m_plotPanel.getBackground());
                g.fillOval(x - radius, y - radius, radius * 2, radius * 2);//reserve an empty zone
            }
            x0 = x;
            y0 = y;
            isDef0 = isDef;
        }
    }

    @Override
    public boolean getDoubleBufferingPolicy() {
        return (m_dataX != null && m_dataX.length > 2000);
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

    
    private void sortData() {
        int dataSize = m_dataX == null ? 0 : m_dataX.length;
        int j;
        if (dataSize > 1) {
            for (int i = 1; i < dataSize; i++) {
                double el = m_dataX[i];
                double elY = m_dataY[i];
                PlotDataSpec spec = m_dataSpec[i];
                for (j = i; j > 0 && m_dataX[j - 1] > el; j--) {
                    m_dataX[j] = m_dataX[j - 1];
                    m_dataY[j] = m_dataY[j - 1];
                    m_dataSpec[j] = m_dataSpec[j - 1];
                }
                m_dataX[j] = el;
                m_dataY[j] = elY;
                m_dataSpec[j] = spec;
            }
        }
    }

    public void setPlotInformation(PlotInformation plotInformation) {
        this.m_plotInformation = plotInformation;
        if (plotInformation != null) {
            setDrawPoints(plotInformation.isDrawPoints());
            setDrawGap(plotInformation.isDrawGap());
            
            Color c = plotInformation.getPlotColor();
            if (c != null) {
                // JPM : these lines have been done for #22157, but it leads to a big bug : #22453
                // for the moment, I remove them and I reopen the ticket 22157
                //m_colorParameter.getColor().setColor(c);
                //m_colorParameter.getColor().setColorSelected();
            }
        }
    }

    public PlotInformation getPlotInformation() {
        if (m_plotInformation.getPlotColor() != null) {
            m_plotInformation.setPlotColor(m_colorParameter.getColor().getColor());
        }
        return this.m_plotInformation;
    }

    /**
     * Change stroke. when isPaintMarker, the strock is more wide. exemple:
     * called by mouseMoved, if Plotis Selected, then this linear plot paint a
     * wider stroke
     *
     * @param isPaintMarker
     * @return
     */
    @Override
    public boolean setIsPaintMarker(boolean isPaintMarker) {
        if (strokeFixed) {
            m_isPaintMarker = true;
            return false;
        }
        boolean modified = isPaintMarker ^ m_isPaintMarker;

        if (modified) {
            m_isPaintMarker = isPaintMarker;
            if (isPaintMarker) {
                m_strokeLine = STROKE_2;
            } else if (m_userStrock != null) {
                m_strokeLine = m_userStrock;
            } else {
                m_strokeLine = STROKE_1;
            }
        }

        return modified;
    }

    @Override
    public boolean isMouseOnPlot(double x, double y) {
        return findPoint(x, y) != -1;
    }

    @Override
    public boolean isMouseOnSelectedPlot(double x, double y) {
        return false; // JPM : not supported for the moment
    }

    public void setDrawPoints(boolean drawPoints) {
        this.m_isDrawPoints = drawPoints;
    }

    public void setDrawGap(boolean drawGap) {
        this.m_isDrawGap = drawGap;
    }

    @Override
    public void parametersChanged() {
        // nothing to do
    }

    @Override
    public String getEnumValueX(int index, boolean fromData) {
        if ((index < 0) || (index >= m_dataX.length)) {
            return "";
        }

        return m_compareDataInterface.getDataValueAt(index, m_cols[COL_X_ID]).toString();

    }

    @Override
    public String getEnumValueY(int index, boolean fromData, Axis axis) {
        if ((index < 0) || (index >= m_dataY.length)) {
            return "";
        }

        return m_compareDataInterface.getDataValueAt(index, m_cols[COL_Y_ID]).toString();
    }

    public void setStrokeFixed(boolean b) {
        this.strokeFixed = b;
        m_strokeLine = STROKE_1;
    }

    public void setStroke(float witdh) {
        m_userStrock = new BasicStroke(witdh, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
    }

    public void setAntiAliasing(boolean displayAntiAliasing) {
        this.displayAntiAliasing = displayAntiAliasing;
    }

}
