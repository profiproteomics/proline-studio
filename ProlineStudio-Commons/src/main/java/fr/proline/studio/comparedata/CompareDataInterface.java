package fr.proline.studio.comparedata;

import fr.proline.studio.graphics.PlotInformation;
import java.awt.Color;
import java.util.Map;

/**
 *
 * @author JM235353
 */
public interface CompareDataInterface {
    
    public static String EXTERNAL_DATA_VERTICAL_MARKER_VALUE = "VERTICAL_MARKER_VALUE"; // List<Double> as Object expected
    public static String EXTERNAL_DATA_VERTICAL_MARKER_TEXT = "VERTICAL_MARKER_TEXT"; // List<String> as Object expected, corresponding to the values
    public static String EXTERNAL_DATA_VERTICAL_MARKER_COLOR = "VERTICAL_MARKER_COLOR"; // List<Color> as Object expected, corresponding to the color to display on the label
    
    public int getRowCount();
    public int getColumnCount();
    public String getDataColumnIdentifier(int columnIndex);
    public Class getDataColumnClass(int columnIndex);
    public Object getDataValueAt(int rowIndex, int columnIndex);
    public int[] getKeysColumn();
    public int getInfoColumn();
    public void setName(String name);
    public String getName();
    // return a list of external data to add on the graph, like lineMarker
    public Map<String, Object> getExternalData();
    // return the color if we want to impose a color for the graph, the title, ...
    public PlotInformation getPlotInformation();
    
    // each model must return an unique id
    public long row2UniqueId(int rowIndex);
    public int uniqueId2Row(long id);

}
