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
 * map alignment table model
 *
 * @author CB205360
 */
public class MapTimeTableModel implements ExtendedTableModelInterface {

    public static final int COLTYPE_TIME = 0;
    public static final int COLTYPE_DELTA_TIME = 1;

    private String[] m_columnNames = {"Time in Reference Map (min)", "Delta time (s)"};;
    private static final String[] m_toolTipColumns = {"Time (min)", "Delta time (s)"};

    private List<MapTime> m_mapTimes = null;
    private Color m_color = null;
    private String m_title = null;

    public MapTimeTableModel(List<MapTime> mapTimes, Color color, String title, String fromMapName, String toMapName) {
        m_mapTimes = mapTimes;
        m_color = color;
        m_title = title;
        m_columnNames[0] = "Time in Map " + fromMapName + " (min)";
        m_columnNames[1] = "Delta time in Map " + toMapName + " (s)";
    }

    @Override
    public int getRowCount() {
        return m_mapTimes == null ? 0 : m_mapTimes.size();
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
            case COLTYPE_TIME:
            case COLTYPE_DELTA_TIME:
                return Double.class;
        }
        return null; // should not happen
    }

    @Override
    public Object getDataValueAt(int rowIndex, int columnIndex) {
        // Retrieve MapTime
        MapTime mapTime = m_mapTimes.get(rowIndex);

        switch (columnIndex) {
            case COLTYPE_TIME: {
                return mapTime.getTime() / 60;

            }
            case COLTYPE_DELTA_TIME: {
                return mapTime.getDeltaTime();

            }
        }
        return null; // should never happen
    }

    @Override
    public int[] getKeysColumn() {
        int[] keys = {COLTYPE_TIME};
        return keys;
    }

    @Override
    public int getInfoColumn() {
        return COLTYPE_TIME;
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
    public Object getRowValue(Class c, int row) {
        if (c.equals(MapTime.class)) {
            return m_mapTimes.get(row);
        }
        return null;
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
