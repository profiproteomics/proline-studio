/*
 * Copyright (C) 2024
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

package fr.proline.mzscope.ui.peakels;

import fr.proline.mzscope.model.IPeakel;
import fr.proline.mzscope.model.PairedPeakel;
import fr.proline.studio.extendedtablemodel.ExtraDataType;
import fr.proline.studio.extendedtablemodel.GlobalTableModelInterface;
import fr.proline.studio.table.AbstractNonLazyTableModel;
import fr.proline.studio.table.Column;
import fr.proline.studio.table.LazyData;
import fr.proline.studio.table.TableDefaultRendererManager;
import fr.proline.studio.table.renderer.BigFloatOrDoubleRenderer;
import fr.proline.studio.table.renderer.DefaultAlignRenderer;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.util.ArrayList;

/**
 * Model to compare two peakel lists which should be very similar. A status shows the potential errors.
 */
public class PeakelsCompareTableModel  extends AbstractNonLazyTableModel implements GlobalTableModelInterface {

    private static TableCellRenderer BIG_FLOAT = new BigFloatOrDoubleRenderer( new DefaultAlignRenderer(TableDefaultRendererManager.getDefaultRenderer(String.class), JLabel.RIGHT), 0 );

    public static final Column COLTYPE_STATUS = new Column("Status", "Pairing Status", String.class,0, Column.STRING_LEFT);
    public static final Column COLTYPE_FEATURE_MZCOL = new Column("m/z", "m/z", Double.class,1, Column.DOUBLE_5DIGITS_RIGHT);
    public static final Column COLTYPE_FEATURE_ET_COL = new Column("Elution", "Elution Time in minutes", Float.class, 2, Column.DOUBLE_2DIGITS_RIGHT);
    public static final Column COLTYPE_FEATURE_DURATION_COL = new Column("Duration", "Duration in minutes", Float.class, 3, Column.DOUBLE_2DIGITS_RIGHT);
    public static final Column COLTYPE_FEATURE_APEX_INT_COL = new Column("Apex Int.", "Apex intensity", Float.class, 4, BIG_FLOAT);
    public static final Column COLTYPE_FEATURE_AREA_COL = new Column("Area", "Area", Double.class,5, BIG_FLOAT);
    public static final Column COLTYPE_FEATURE_SCAN_COUNT_COL = new Column("MS Count", "MS Count", Integer.class, 6);
    public static final Column COLTYPE_FEATURE_RAWFILE = new Column("Raw file", "Raw File Name", String.class,7, Column.STRING_LEFT);
    //public static final Column COLTYPE_FEATURE_MZ_STDEV = new Column("mz Stdev", "m/z standard deviation", Double.class, X, Column.DOUBLE_5DIGITS_RIGHT);

    public static final Column COLTYPE_FEATURE_MZCOL2 = new Column("m/z 2", "m/z", Double.class,8, Column.DOUBLE_5DIGITS_RIGHT);
    public static final Column COLTYPE_FEATURE_ET_COL2 = new Column("Elution 2", "Elution Time in minutes", Float.class, 9, Column.DOUBLE_2DIGITS_RIGHT);
    public static final Column COLTYPE_FEATURE_DURATION_COL2 = new Column("Duration 2", "Duration in minutes", Float.class, 10, Column.DOUBLE_2DIGITS_RIGHT);
    public static final Column COLTYPE_FEATURE_APEX_INT_COL2 = new Column("Apex Int. 2", "Apex intensity", Float.class, 11, BIG_FLOAT);
    public static final Column COLTYPE_FEATURE_AREA_COL2 = new Column("Area 2", "Area", Double.class,12, BIG_FLOAT);
    public static final Column COLTYPE_FEATURE_SCAN_COUNT_COL2 = new Column("MS Count 2", "MS Count", Integer.class, 13);
    public static final Column COLTYPE_FEATURE_RAWFILE2 = new Column("Raw file 2", "Raw File Name", String.class,14, Column.STRING_LEFT);
    //public static final Column COLTYPE_FEATURE_MZ_STDEV2 = new Column("mz Stdev 2", "m/z standard deviation", Double.class,7, Column.DOUBLE_5DIGITS_RIGHT);



    protected ArrayList<PairedPeakel> m_pairedPeakelArrayList = new ArrayList<>();

    //protected Map<Integer, Double> m_statistics = new HashMap<>();

    @Override
    public int[] getKeysColumn() {
        int[] keys = { COLTYPE_FEATURE_MZCOL.getIndex(), COLTYPE_FEATURE_ET_COL.getIndex(), COLTYPE_FEATURE_RAWFILE.getIndex() };
        return keys;
    }

    @Override
    public int getInfoColumn() {
        return COLTYPE_FEATURE_MZCOL.getIndex();
    }

    @Override
    public ArrayList<ExtraDataType> getExtraDataTypes() {
        return null;
    }

    @Override
    public String getTootlTipValue(int row, int col) {
        return null;
    }

    @Override
    public int getRowCount() {
        return m_pairedPeakelArrayList.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {

        PairedPeakel pairedPeakel = m_pairedPeakelArrayList.get(rowIndex);


        if (columnIndex == COLTYPE_STATUS.getIndex()) {
            return pairedPeakel.getStatus().toString();
        }

        IPeakel p;
        if (columnIndex<COLTYPE_FEATURE_MZCOL2.getIndex()) {
            p = pairedPeakel.getPeakel1();
            if (p == null) {
                return null;
            }

            if (columnIndex == COLTYPE_FEATURE_MZCOL.getIndex()) {
                return p.getMz();
            } else if (columnIndex == COLTYPE_FEATURE_ET_COL.getIndex()) {
                return p.getElutionTime() / 60.0;
            } else if (columnIndex == COLTYPE_FEATURE_DURATION_COL.getIndex()) {
                return p.getDuration() / 60.0;
            } else if (columnIndex == COLTYPE_FEATURE_APEX_INT_COL.getIndex()) {
                return p.getApexIntensity();
            } else if (columnIndex == COLTYPE_FEATURE_AREA_COL.getIndex()) {
                return p.getArea() / 60.0;
            } else if (columnIndex == COLTYPE_FEATURE_SCAN_COUNT_COL.getIndex()) {
                return p.getScanCount();
            } else if (columnIndex == COLTYPE_FEATURE_RAWFILE.getIndex()) {
                return p.getRawFile().getName();
            }
        } else {
            p = pairedPeakel.getPeakel2();
            if (p == null) {
                return null;
            }

            if (columnIndex == COLTYPE_FEATURE_MZCOL2.getIndex()) {
                return p.getMz();
            } else if (columnIndex == COLTYPE_FEATURE_ET_COL2.getIndex()) {
                return p.getElutionTime() / 60.0;
            } else if (columnIndex == COLTYPE_FEATURE_DURATION_COL2.getIndex()) {
                return p.getDuration() / 60.0;
            } else if (columnIndex == COLTYPE_FEATURE_APEX_INT_COL2.getIndex()) {
                return p.getApexIntensity();
            } else if (columnIndex == COLTYPE_FEATURE_AREA_COL2.getIndex()) {
                return p.getArea() / 60.0;
            } else if (columnIndex == COLTYPE_FEATURE_SCAN_COUNT_COL2.getIndex()) {
                return p.getScanCount();
            } else if (columnIndex == COLTYPE_FEATURE_RAWFILE2.getIndex()) {
                return p.getRawFile().getName();
            }
        }


        return null; // should not happen
    }

    /*private Double getMzStats(int row, IPeakel iPeakel) {
        if (!m_statistics.containsKey(row)) {
            if (iPeakel.getPeakel() != null) {
                double[] mzValues = iPeakel.getPeakel().mzValues();
                double standardDeviation = new DescriptiveStatistics(mzValues).getStandardDeviation();
                m_statistics.put(row, 1e6 * standardDeviation / iPeakel.getMz());
            } else {
                return 0.0;
            }
        }
        return m_statistics.get(row);
    }*/

    @Override
    public Object getValue(Class c) {
        return getSingleValue(c);
    }

    @Override
    public Object getRowValue(Class c, int row) {
        if (IPeakel.class.isAssignableFrom(c)) {
            PairedPeakel pairedPeakel = m_pairedPeakelArrayList.get(row);
            return pairedPeakel.getPeakel1();
        }
        return null;
    }

    @Override
    public Object getColValue(Class c, int col) {
        return null;
    }

    public void setPeakels(ArrayList<PairedPeakel> pairedPeakelArrayList) {

        m_pairedPeakelArrayList = pairedPeakelArrayList;

        fireTableDataChanged();
    }


    @Override
    public GlobalTableModelInterface getFrozzenModel() {
        return this;
    }

    @Override
    public boolean isLoaded() {
        return true;
    }

    @Override
    public int getLoadingPercentage() {
        return 100;
    }

    @Override
    public Long getTaskId() {
        return -1L;
    }

    @Override
    public LazyData getLazyData(int row, int col) {
        return null;
    }

    @Override
    public void givePriorityTo(Long taskId, int row, int col) {

    }

    @Override
    public void sortingChanged(int col) {

    }

    @Override
    public int getSubTaskId(int col) {
        return 0;
    }
}
