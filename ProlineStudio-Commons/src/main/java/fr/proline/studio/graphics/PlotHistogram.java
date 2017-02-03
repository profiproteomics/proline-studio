package fr.proline.studio.graphics;

import fr.proline.studio.comparedata.CompareDataInterface;
import fr.proline.studio.comparedata.StatsModel;
import fr.proline.studio.graphics.marker.AbstractMarker;
import fr.proline.studio.graphics.marker.coordinates.DataCoordinates;
import fr.proline.studio.graphics.marker.LabelMarker;
import fr.proline.studio.graphics.marker.LineMarker;
import fr.proline.studio.graphics.marker.coordinates.PercentageCoordinates;
import fr.proline.studio.graphics.marker.XDeltaMarker;
import fr.proline.studio.parameter.ColorParameter;
import fr.proline.studio.parameter.DefaultParameterDialog;
import fr.proline.studio.parameter.IntegerParameter;
import fr.proline.studio.parameter.ParameterList;
import fr.proline.studio.parameter.StringParameter;
import fr.proline.studio.utils.CyclicColorPalette;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.HashMap;


import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import org.openide.windows.WindowManager;

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
    private double[] m_selected;
    private long[] m_ids;
    private HashMap<Long, Integer> m_idToIndex;

    private StatsModel m_values;
    private int m_bins;
    
    private final ColorParameter m_colorParameter;
    private final IntegerParameter m_binsParameter;
    private ArrayList<ParameterList> m_parameterListArray = null;
    
    private final LinkedList<GraphicDataGroup> m_dataGroup = new LinkedList<>();
    private final HashMap<GraphicDataGroup, LinkedHashMap<Integer, Double>> m_graphicDataGroupToIndex = new HashMap<>();
    
    private static final String PLOT_HISTOGRAM_COLOR_KEY = "PLOT_HISTOGRAM_COLOR";
    private static final String PLOT_HISTOGRAM_BIN_KEY = "PLOT_HISTOGRAM_BIN";
    
    public static String HISTOGRAM_COUNT = "Count";
    public static String HISTOGRAM_PERCENTAGE = "Percentage %";
    
    private boolean m_asPercentage = false;
    
    public PlotHistogram(BasePlotPanel plotPanel, CompareDataInterface compareDataInterface, CrossSelectionInterface crossSelectionInterface, int colX, String paramZ) {
        super(plotPanel, PlotType.HISTOGRAM_PLOT, compareDataInterface, crossSelectionInterface);
        update(colX, -1, paramZ); 
        
        
        ParameterList parameterColorList = new ParameterList("Colors");
        Color histogramColor = CyclicColorPalette.getColor(21);
        m_colorParameter = new ColorParameter(PLOT_HISTOGRAM_COLOR_KEY, "Histogram Plot Color", histogramColor, ColorButtonAndPalettePanel.class);
        parameterColorList.add(m_colorParameter);
        
        ParameterList parameterSettingsList = new ParameterList("Settings");
        m_binsParameter = new IntegerParameter(PLOT_HISTOGRAM_BIN_KEY, "Bins", JTextField.class, m_bins, 5, 1000);
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
                m_selected[i] = 1;
            } else {
                if (!append) {
                    m_selected[i] = 0;
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
                m_selected[i] = 1;
            } else {
                if (!append) {
                    m_selected[i] = 0;
                }
            }            
        }
        
        return false;
    }
    
    private int findPoint(double x, double y) {
        double y2 = 0;
        int size = m_dataX == null ? 0 : m_dataX.length;
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
    public double getNearestXData(double x) {
        return x; //JPM.TODO
    }
    
    @Override
    public double getNearestYData(double y) {
        return y; //JPM.TODO
    }
    
    @Override
    public ArrayList<Long> getSelectedIds() {
        
        ArrayList<Long> selectedIds = new ArrayList();
        
        int size = m_values.getRowCount();
        for (int i=0;i<size;i++) {
            double v = m_values.getValue(i);
            int index = (int) (((v - m_xMin) / (m_xMax-m_xMin)) * (m_bins));
            if (index >= m_bins) {
                index = m_bins - 1;
            }
            if (m_selected[index] > 0) {
                selectedIds.add(m_ids[i]);
            }
        }

        return selectedIds;
    }
    
    @Override
    public void setSelectedIds(ArrayList<Long> selectedIds) {
        

        int[] m_selectCount = new int[m_bins + 1];
        
        for (int i = 0; i < selectedIds.size(); i++) {
            Integer index = m_idToIndex.get(selectedIds.get(i));
            if (index == null) {
                continue;
            }
            
            double v = m_values.getValue(index);
            int binIndex = (int) (((v - m_xMin) / (m_xMax - m_xMin)) * (m_bins));
            if (binIndex >= m_bins) {
                binIndex = m_bins - 1;
            }
            m_selectCount[binIndex]++;
        }
        
        for (int i = 0; i < m_selected.length; i++) {
            m_selected[i] = ((double)m_selectCount[i])/((double)m_dataCountY[i]);
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

        String tooltip = m_sb.toString();
        m_sb.setLength(0);
        return tooltip;
 
    }
    private StringBuilder m_sb = null;
    
    @Override
    public void parametersChanged() {
        
        // parametersChanged() can be call soon, and so parameters could be not initialized
        if (m_binsParameter == null) {
            return;
        }
        
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
        
        
        m_plotPanel.setXAxisTitle(m_compareDataInterface.getDataColumnIdentifier(m_colX));
        m_plotPanel.setYAxisTitle(m_asPercentage ? HISTOGRAM_PERCENTAGE : HISTOGRAM_COUNT);
        
        // bins
        double std = m_values.standardDeviationNaN();
        m_bins = (int) Math.round((max-min)/(3.5*std*Math.pow(size, -1/3.0)));
        if (m_bins<10) {
            m_bins = 10;
        }

        

        updataDataAccordingToBins();
        
        double yStdevLabel = m_yMax*0.1;
        double yMeanLabel = m_yMax*1.1;
        m_yMax *= 1.2; // we let place at the top to be able to put information

                

        m_plotPanel.updateAxis(this);
        
        // add Stdev value
        double mean = m_values.meanNaN();
        addMarker(new XDeltaMarker(m_plotPanel, mean, mean+std, yStdevLabel));
        addMarker(new LineMarker(m_plotPanel, mean+std, LineMarker.ORIENTATION_VERTICAL));
        
        addMarker(new XDeltaMarker(m_plotPanel, mean, mean-std, yStdevLabel));
        addMarker(new LineMarker(m_plotPanel, mean-std, LineMarker.ORIENTATION_VERTICAL));
        
        addMarker(new LabelMarker(m_plotPanel, new DataCoordinates(mean+std/2, yStdevLabel), "Stdev : "+std, LabelMarker.ORIENTATION_X_RIGHT, LabelMarker.ORIENTATION_Y_TOP));
        
        
        // add Mean value
        addMarker(new LineMarker(m_plotPanel, mean, LineMarker.ORIENTATION_VERTICAL));
        addMarker(new LabelMarker(m_plotPanel, new DataCoordinates(mean, yMeanLabel), "Mean : "+mean, LabelMarker.ORIENTATION_X_RIGHT, LabelMarker.ORIENTATION_Y_BOTTOM));
        
        // add Title
        LabelMarker titleMarker = new LabelMarker(m_plotPanel, new PercentageCoordinates(0.2, 0.95), m_values.getDataColumnIdentifier(0) +" Histogram");
        titleMarker.setFont(LabelMarker.TITLE_FONT);
        titleMarker.setDrawFrame(false);
        addMarker(titleMarker);
        
        m_plotPanel.repaint();
    }
    
    private void updataDataAccordingToBins() {
        
        int size = m_values.getRowCount();
        
        double[] data = new double[size];
        m_ids = new long[size];
        m_idToIndex = new HashMap(size);
        for (int i=0;i<data.length;i++) {
            data[i] = m_values.getValue(i);
            m_ids[i] = m_values.row2UniqueId(i);
            m_idToIndex.put(m_ids[i], i);
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
        m_selected = new double[m_bins];
        double binDelta = delta / m_bins;
        for (int i = 0; i < m_bins; i++) {
            m_dataX[i] = m_xMin + i * binDelta;
            m_dataY[i] = histogram[i];
            m_selected[i] = 0;
        }
        m_dataX[m_bins] = m_dataX[m_bins - 1] + binDelta;
        m_dataY[m_bins] = m_dataY[m_bins - 1];

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
        int clipX = xAxis.valueToPixel(xAxis.getMinValue());
        int clipWidth = xAxis.valueToPixel(xAxis.getMaxValue())-clipX;
        int clipY = yAxis.valueToPixel(yAxis.getMaxValue());
        int clipHeight = yAxis.valueToPixel(yAxis.getMinValue())-clipY;
        g.setClip(clipX, clipY, clipWidth, clipHeight);
        
        int y2 = yAxis.valueToPixel(0);
        int size = m_dataX == null ? 0 :m_dataX.length;
        
        // draw normal bars
        for (int i=0;i<size-1;i++) {
            int x1 = xAxis.valueToPixel( m_dataX[i]);
            int x2 = xAxis.valueToPixel( m_dataX[i+1]);
            int y1 = yAxis.valueToPixel( m_asPercentage ? m_dataY[i] : m_dataCountY[i]);
            

            g.setColor(m_colorParameter.getColor());
            g.fillRect(x1, y1 , x2-x1, y2-y1);
                        
            g.setColor(Color.black);
            g.drawRect(x1, y1 , x2-x1, y2-y1);
            
        }
        
        // draw groups
        int nbDataGroups = m_dataGroup.size();
        for (int i=nbDataGroups-1;i>=0;i--) {
            GraphicDataGroup group = m_dataGroup.get(i);
            LinkedHashMap<Integer, Double> indexMap = m_graphicDataGroupToIndex.get(group);
            Set<Integer> idSet = indexMap.keySet();
            for (Integer index : idSet) {
                int x1 = xAxis.valueToPixel(m_dataX[index]);
                int x2 = xAxis.valueToPixel(m_dataX[index + 1]);
                int y1 = yAxis.valueToPixel(m_asPercentage ? m_dataY[index] : m_dataCountY[index]);
                double percentage = indexMap.get(index);
                g.setColor(group.getColor());
                int height = (int) Math.round((1-percentage)* (y2 - y1));
                g.fillRect(x1, y1 + height, x2 - x1, y2 - y1 - height);

                g.setColor(Color.black);
                //g.drawLine(x1, y1 + height, x2, y1 + height);
                g.drawRect(x1, y1 + height, x2 - x1, y2 - y1 - height);
            }

        }

        // draw selection
        for (int i=0;i<size-1;i++) {
            int x1 = xAxis.valueToPixel( m_dataX[i]);
            int x2 = xAxis.valueToPixel( m_dataX[i+1]);
            int y1 = yAxis.valueToPixel( m_asPercentage ? m_dataY[i] : m_dataCountY[i]);
            if (m_selected[i]>0) {
                g.setColor(CyclicColorPalette.getColor(5));
                int height = (int) Math.round((1-m_selected[i])*(y2-y1));
                g.fillRect(x1, y1+height , x2-x1, y2-y1-height);
                
                g.setColor(Color.black);
                //g.drawLine(x1, y1+height , x2, y1+height);
                g.drawRect(x1, y1+height , x2-x1, y2-y1-height);
            }
        }
        
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
    public boolean isMouseOnSelectedPlot(double x, double y) {
        int index = findPoint(x, y);
        if (index == -1) {
            return false;
        }
        return (m_selected[index]>0);
    }
    
    
    @Override
    public JPopupMenu getPopupMenu(double x, double y) {

        boolean onPoint = false;
        boolean onSelection = false;
        GraphicDataGroup onGroup = null;
        int index = findPoint(x, y);
        if (index != -1) {
            onPoint = true;
            double yData1 = m_asPercentage ? m_dataY[index] : m_dataCountY[index];
            if (m_selected[index] > 0) {
                
                double ySelection = m_selected[index]*yData1;
                onSelection = y<ySelection;
            } else {
                onSelection = false;
            }

            // search for group
            for (GraphicDataGroup group : m_dataGroup) {
                LinkedHashMap<Integer, Double> percentageMap = m_graphicDataGroupToIndex.get(group);
                double percentage = percentageMap.get(index);
                if (y<percentage*yData1) {
                    onGroup = group;
                    break;
                }
            }
        }

        AbstractAction addGroupAction = new AbstractAction("Add Group") {

            @Override
            public void actionPerformed(ActionEvent e) {

                ArrayList<ParameterList> parameterListArray = new ArrayList<>(1);
                // Color parameter
                ParameterList groupParameterList = new ParameterList("Define Group");
                parameterListArray.add(groupParameterList);

                StringParameter groupNameParameter = new StringParameter("GroupName", "Group Name", JTextField.class, "", 0, 32);
                groupParameterList.add(groupNameParameter);
                
                Color defaultColor = CyclicColorPalette.getColor(18);
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
                   LinkedHashMap<Integer, Double> selectedIndexes = new LinkedHashMap<>();
                   for (int i = 0; i < m_selected.length; i++) {
                       if (m_selected[i]>0) {
                           selectedIndexes.put(i, m_selected[i]);   
                       }
                   }
                   m_graphicDataGroupToIndex.put(dataGroup, selectedIndexes);
                   
                   String name = dataGroup.getName();
                    if ((name != null) && (!name.isEmpty())) {
                        // calculation of percentageY is a wart, could be done in a better way
                        double percentageY = 1-(0.1*m_dataGroup.size());
                        if (percentageY<0.1) {
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
            
        };
        
        addGroupAction.setEnabled(onSelection);

        final GraphicDataGroup _onGroup = onGroup;
        AbstractAction selectGroupAction = new AbstractAction("Select Group") {

            @Override
            public void actionPerformed(ActionEvent e) {

                LinkedHashMap<Integer, Double> percentageMap = m_graphicDataGroupToIndex.get(_onGroup);
                int size = m_selected.length;
                for (int i = 0; i < size; i++) {
                    Double percentage = percentageMap.get(i);
                    m_selected[i] = (percentage == null) ?0 : percentage;
                }


                // repaint
                m_plotPanel.repaintUpdateDoubleBuffer();

            }
        };
        selectGroupAction.setEnabled(_onGroup != null);
        
        
        AbstractAction deleteGroupAction = new AbstractAction("Delete Group") {

            @Override
            public void actionPerformed(ActionEvent e) {

                m_graphicDataGroupToIndex.remove(_onGroup);
                m_dataGroup.remove(_onGroup);
                AbstractMarker marker = _onGroup.getAssociatedMarker();
                if (marker != null) {
                    removeMarker(marker);
                }
                
                // repaint
                   m_plotPanel.repaintUpdateDoubleBuffer();
            }
        };
        deleteGroupAction.setEnabled(_onGroup != null);
        
        JPopupMenu menu = new JPopupMenu();
        menu.add(addGroupAction);
        menu.add(selectGroupAction);
        menu.add(deleteGroupAction);
        
        return menu;
    }
    

    @Override
    public String getEnumValueX(int index, boolean fromData ) {
        return null; // should not be called
    }

    @Override
    public String getEnumValueY(int index, boolean fromData) {
        return null; // should not be called
    }
    
}
