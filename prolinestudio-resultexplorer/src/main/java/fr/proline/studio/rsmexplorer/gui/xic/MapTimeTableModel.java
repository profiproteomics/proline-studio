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
package fr.proline.studio.rsmexplorer.gui.xic;

import fr.proline.core.orm.lcms.MapTime;
import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;
import fr.proline.studio.extendedtablemodel.ExtraDataType;
import fr.proline.studio.graphics.PlotDataSpec;
import fr.proline.studio.graphics.PlotInformation;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * map RT alignment on delta of value table model
 *
 * @author CB205360
 */
public class MapTimeTableModel implements ExtendedTableModelInterface {


    protected List<MapTime> m_mapValues = null;
    public static final int COLTYPE_VALUE = 0;
    public static final int COLTYPE_DELTA_VALUE = 1;

    protected String[] m_columnNames = {"Value in Map", "Delta value"};

    protected Color m_color;
    protected String m_title;

    private boolean m_inverseDeltaSign = false;//VDS WART: To have delta ppm from moz calibration and peptide delta moz with same "sign"

    /**
     * Create a MapTableModel for a single map.
     * This is called when delta value to display are moz
     *
     * @param mapTimes : value to represent as MapTime list
     * @param color : color to use for plot representation
     * @param title : plot title
     * @param mapName : name of represented map
     */
    public MapTimeTableModel(List<MapTime> mapTimes, Color color, String title, String mapName) {
        m_mapValues = mapTimes;
        m_color = color;
        m_title = title;
        m_inverseDeltaSign = true;
        m_columnNames[0] = "Time in Map " + mapName + " (min)";
        m_columnNames[1] = "Delta moz in Map " + mapName;
    }

    /**
     * Create a MapTableModel with 2 different maps
     * This is called when delta value to display are time
     *
     * @param mapTimes : value to represent as MapTime list
     * @param color : color to use for plot representation
     * @param title : plot title
     * @param fromMapName : first map name
     * @param toMapName : compared map name
     */
    public MapTimeTableModel(List<MapTime> mapTimes, Color color, String title, String fromMapName, String toMapName) {
        m_mapValues = mapTimes;
        m_color = color;
        m_title = title;

        m_columnNames[0] = "Time in Map " + fromMapName + " (min)";
        m_columnNames[1] = "Delta time in Map " + toMapName + " (s)";
    }


    @Override
    public int getRowCount() {
        return m_mapValues == null ? 0 : m_mapValues.size();
    }

    @Override
    public int getColumnCount() {
        return m_columnNames.length;
    }


    @Override
    public String getDataColumnIdentifier(int columnIndex) {
        return m_columnNames[columnIndex];
    }

    @Override
    public Class getDataColumnClass(int columnIndex) {
        switch (columnIndex) {
            case COLTYPE_VALUE:
            case COLTYPE_DELTA_VALUE:
                return Double.class;
        }
        return null; // should not happen
    }

    @Override
    public Object getDataValueAt(int rowIndex, int columnIndex) {
        // Retrieve MapTime
        MapTime mapTime = m_mapValues.get(rowIndex);

        switch (columnIndex) {
            case COLTYPE_VALUE: {
                return mapTime.getTime() / 60;

            }
            case COLTYPE_DELTA_VALUE: {
                if(m_inverseDeltaSign)
                    return -mapTime.getDeltaValue();
                return mapTime.getDeltaValue();

            }
        }
        return null; // should never happen
    }

    @Override
    public Object getRowValue(Class c, int row) {
        if (c.equals(MapTime.class)) {
            return m_mapValues.get(row);
        }
        return null;
    }

    @Override
    public int[] getKeysColumn() {
        return new int[]{ COLTYPE_VALUE };
    }

    @Override
    public int getInfoColumn() {
        return COLTYPE_VALUE;
    }

    @Override
    public void setName(String name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getName() {
        return "Signal";
    }

    @Override
    public Map<String, Object> getExternalData() {
        return null;
    }


    @Override
    public PlotInformation getPlotInformation() {
        PlotInformation plotInformation = new PlotInformation();
        plotInformation.setPlotColor(m_color);
        plotInformation.setPlotTitle(m_title);
        plotInformation.setDrawPoints(false);
        plotInformation.setDrawGap(true);
        return plotInformation;
    }
    @Override
    public ArrayList<ExtraDataType> getExtraDataTypes() {
        return null;
    }

    @Override
    public Object getValue(Class c) {
        return getSingleValue(c);
    }

    @Override
    public Object getColValue(Class c, int col) {
        return null;
    }

    @Override
    public void addSingleValue(Object v) {

    }

    @Override
    public Object getSingleValue(Class c) {
        return null;
    }

    @Override
    public long row2UniqueId(int rowIndex) {
        return rowIndex;
    }

    @Override
    public int uniqueId2Row(long id) {
        return (int) id;
    }

    @Override
    public PlotDataSpec getDataSpecAt(int i) {
        return null;
    }


}
