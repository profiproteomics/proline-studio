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
package fr.proline.mzscope.ui.model;


import fr.proline.studio.extendedtablemodel.ExtraDataType;
import fr.proline.studio.graphics.PlotInformation;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Map;
import javax.swing.table.AbstractTableModel;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;
import fr.proline.studio.graphics.PlotDataSpec;

/**
 *
 * @author CB205360
 */
public class StatisticsTableModel extends AbstractTableModel implements ExtendedTableModelInterface {

    public static final int COLTYPE_TIME = 0;
    public static final int COLTYPE_METRIC = 1;
    
    private DescriptiveStatistics m_statistics;
    private String[] m_columnNames;
    private double[] m_time;
    private String m_modelName;
    private Color m_color;
    
    public StatisticsTableModel(String metricName, double[] time, DescriptiveStatistics statistics) {
        this.m_time = time;
        this.m_columnNames = new String[] {"time", metricName};
        this.m_statistics = statistics;
    }
    
    @Override
    public int getRowCount() {
        return m_time.length;
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public String getColumnName(int col) {
        return m_columnNames[col];
    }

   @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
       
        switch (columnIndex) {
            case COLTYPE_TIME: {
                return m_time[rowIndex];
            }
            case COLTYPE_METRIC:{
                return m_statistics.getElement(rowIndex);
            }
        }
        return null; // should never happen
    }


    @Override
    public String getDataColumnIdentifier(int columnIndex) {
        return getColumnName(columnIndex);
    }

    @Override
    public Class getDataColumnClass(int columnIndex) {
        return Double.class;
    }

    @Override
    public Object getDataValueAt(int rowIndex, int columnIndex) {
        return getValueAt(rowIndex, columnIndex);
    }
    
    @Override
    public PlotDataSpec getDataSpecAt(int i) {
            return null;
    }
   @Override
    public int[] getKeysColumn() {
        int[] keys = { COLTYPE_TIME };
        return keys;
    }

    @Override
    public int getInfoColumn() {
        return COLTYPE_METRIC;
    }

    @Override
    public void setName(String name) {
        m_modelName = name;
    }

    @Override
    public String getName() {
        return m_modelName;
    }

    @Override
    public Map<String, Object> getExternalData() {
        return null;
    }

    @Override
    public PlotInformation getPlotInformation() {
       PlotInformation plotInformation = new PlotInformation();
       plotInformation.setPlotColor(m_color);
       return plotInformation;
    }
    
    public void setColor(Color c){
        this.m_color = c;
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
    public ArrayList<ExtraDataType> getExtraDataTypes() {
        return null;
    }

    @Override
    public Object getValue(Class c) {
        return null;
    }

    @Override
    public Object getRowValue(Class c, int row) {
        return null;
    }

    @Override
    public Object getColValue(Class c, int col) {
        return null;
    }

    @Override
    public void addSingleValue(Object v) {
        return;
    }

    @Override
    public Object getSingleValue(Class c) {
        return null;
    }
    
    
}
