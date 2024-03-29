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
package fr.proline.studio.extendedtablemodel;

import fr.proline.studio.graphics.PlotDataSpec;
import fr.proline.studio.graphics.PlotInformation;
import java.util.Map;

/**
 * Extendsion of the table model, to define methods needed for plotting, calculations and export
 * @author JM235353
 */
public interface ExtendedTableModelInterface extends ExtraDataForTableModelInterface {
    
    public final static String EXTERNAL_DATA_VERTICAL_MARKER_VALUE = "VERTICAL_MARKER_VALUE"; // List<Double> as Object expected
    public final static String EXTERNAL_DATA_VERTICAL_MARKER_TEXT = "VERTICAL_MARKER_TEXT"; // List<String> as Object expected, corresponding to the values
    public final static String EXTERNAL_DATA_VERTICAL_MARKER_COLOR = "VERTICAL_MARKER_COLOR"; // List<Color> as Object expected, corresponding to the color to display on the label
    
    
    // same methods that in table model. I was obliged to put them here, if we don't know that
    // we are in fact manipulating a table model
    public int getRowCount();
    public int getColumnCount();
    
    // Columns used when we want to access to data as instances of basic classes (like String, Double, Long)
    // instead of classes like Peptide. It is used for export, calculations in Data Analyser(Join for instance)
    // and graphics
    public String getDataColumnIdentifier(int columnIndex);
    public Class getDataColumnClass(int columnIndex);
    public Object getDataValueAt(int rowIndex, int columnIndex);
    
    // return default keys columns (used for join)
    public int[] getKeysColumn();
    
    // complementary info column used for graphics.
    public int getInfoColumn();
    
    
    // used to set/get name of a model.
    // This name can be used for the display
    public void setName(String name);
    public String getName();
    
// return a list of external data to add on the graph, like lineMarker
    public Map<String, Object> getExternalData();
    
    // return the color if we want to impose a color for the graph, the title, ...
    public PlotInformation getPlotInformation();
    
    // each model must return an unique id
    // it is important to be able to track rows after filtering
    public long row2UniqueId(int rowIndex);
    public int uniqueId2Row(long id);

    public PlotDataSpec getDataSpecAt(int i);

}
