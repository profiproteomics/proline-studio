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

import fr.proline.studio.extendedtablemodel.LockedDataModel;
import fr.proline.studio.graphics.cursor.AbstractCursor;
import fr.proline.studio.graphics.marker.AbstractMarker;
import fr.proline.studio.parameter.ParameterList;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import javax.swing.JPopupMenu;
import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;
import fr.proline.studio.graphics.measurement.AbstractMeasurement;

/**
 *
 * @author JM235353
 */
public abstract class PlotBaseAbstract implements Axis.EnumXInterface, Axis.EnumYInterface {

    public static final int COL_X_ID = 0;
    public static final int COL_Y_ID = 1;

    protected PlotType m_plotType;
    /**
     * data to paint
     */
    protected ExtendedTableModelInterface m_compareDataInterface = null;
    protected CrossSelectionInterface m_crossSelectionInterface = null;
    /**
     * m_cols[0]=X axis, m_cols[1]=Y axis
     */
    protected int[] m_cols;
    protected String m_parameterZ;

    protected BasePlotPanel m_plotPanel;

    protected ArrayList<AbstractMarker> m_markersList = null;

    protected ArrayList<AbstractCursor> m_cursorList = null;
    protected AbstractCursor m_selectedCursor = null;

    protected boolean m_isPaintMarker = true;

    protected ArrayList<AbstractMeasurement> m_measurements = null;

    protected boolean m_locked = false;

    public abstract boolean needsXAxis();

    public abstract boolean needsYAxis();
    
    public abstract boolean canLogXAxis();

    public abstract boolean canLogYAxis();


    public abstract double getNearestXData(double x);

    public abstract double getNearestYData(double y);

    public abstract double getXMin();

    public abstract double getXMax();

    public abstract double getYMin();

    public abstract double getYMax();

    //public abstract boolean inside(int x, int y);

    public abstract void parametersChanged();

    public boolean parametersCanceled() {
        return false;
    }

    public abstract void paint(Graphics2D g, XAxis xAxis, YAxis yAxis);

    /**
     * usualy used to show tooltips information
     *
     * @param g
     */
    public void paintOver(Graphics2D g) {

    }

    public enum DoubleBufferingPolicyEnum {
        DOUBLE_BUFFERING
    }

    public PlotBaseAbstract(BasePlotPanel plotPanel, PlotType plotType, ExtendedTableModelInterface compareDataInterface, CrossSelectionInterface crossSelectionInterface) {
        m_plotPanel = plotPanel;
        m_plotType = plotType;
        m_compareDataInterface = (m_locked) ? new LockedDataModel(compareDataInterface) : compareDataInterface;
        m_crossSelectionInterface = crossSelectionInterface;
    }

    public BasePlotPanel getBasePlotPanel() {
        return m_plotPanel;
    }

    public abstract String getToolTipText(double x, double y);

    /**
     *
     * @param cols Axis X, Y
     * @param parameterZ, Axis Z
     */
    public void update(int[] cols, String parameterZ) {

        if (cols == null) {
            updateAxisSpecificities();
            return;
        }

        if ((m_cols == null) || (m_cols.length != cols.length)) {
            m_cols = new int[cols.length];
        }

        for (int i = 0; i < cols.length; i++) {
            m_cols[i] = cols[i];
        }

        m_parameterZ = parameterZ;
        updateAxisSpecificities();
        update();
    }

    /**
     * update the value of isInteger, isEnum, isPixel in XAxis and YAxis
     */
    public void updateAxisSpecificities() {
        if (!needsXAxis()) {
            m_plotPanel.getXAxis().setSpecificities(false, false, true);
        } else if (m_cols[COL_X_ID] != -1) {
            Class xClass = m_compareDataInterface.getDataColumnClass(m_cols[COL_X_ID]);
            boolean isIntegerX = ((xClass.equals(Integer.class)) || (xClass.equals(Long.class)) || (xClass.equals(String.class)));
            boolean isEnumX = (xClass.equals(String.class));
            m_plotPanel.setAxisXSpecificities(isIntegerX, isEnumX, false);
        }

        if (!needsYAxis()) {
            m_plotPanel.getYAxis().setSpecificities(false, false, true);
        } else if ((needsYAxis()) && (m_cols[COL_Y_ID] != -1)) {
            Class yClass = m_compareDataInterface.getDataColumnClass(m_cols[COL_Y_ID]);
            boolean isIntegerY = ((yClass.equals(Integer.class)) || (yClass.equals(Long.class)) || (yClass.equals(String.class)));
            boolean isEnumY = (yClass.equals(String.class));
            m_plotPanel.setAxisYSpecificities(isIntegerY, isEnumY, false, this);
        }
    }

    public abstract void update();

    public String getXAxisTitle() {
        return m_compareDataInterface.getDataColumnIdentifier(m_cols[COL_X_ID]);
    }

    public String getYAxisTitle() {
        return m_compareDataInterface.getDataColumnIdentifier(m_cols[COL_Y_ID]);
    }

    public abstract boolean select(double x, double y, boolean append);

    public abstract boolean select(Path2D.Double path, double minX, double maxX, double minY, double maxY, boolean append);

    public abstract ArrayList<Long> getSelectedIds();

    public abstract void setSelectedIds(ArrayList<Long> selection);

    public abstract ArrayList<ParameterList> getParameters();

    public void addMarker(AbstractMarker m) {
        if (m_markersList == null) {
            m_markersList = new ArrayList<>();
        }
        m_markersList.add(m);
    }

    public void selectCursor(AbstractCursor c) {
        if (m_selectedCursor != null) {
            m_selectedCursor.setSelected(false, false);
        }
        m_selectedCursor = c;
        c.setSelected(true, false);
    }

    public void addCursor(AbstractCursor c) {
        if (m_cursorList == null) {
            m_cursorList = new ArrayList<>();
        }
        m_cursorList.add(c);
        selectCursor(c);
    }

    public boolean removeCursor(AbstractCursor c) {
        if (m_cursorList != null) {
            m_cursorList.remove(c);
        }
        if (c == m_selectedCursor) {
            if (!m_cursorList.isEmpty()) {
                selectCursor(m_cursorList.get(m_cursorList.size() - 1));
            } else {
                m_selectedCursor = null;
            }

        }
        return false;
    }

    public void removeAllCursors() {
        if (m_cursorList != null) {
            m_cursorList.clear();
            m_selectedCursor = null;
        }
    }

    public MoveableInterface getOverMovable(int x, int y) {
        if (m_markersList != null) {
            int nb = m_markersList.size();
            for (int i = 0; i < nb; i++) {
                AbstractMarker m = m_markersList.get(i);
                if (m instanceof MoveableInterface) {
                    MoveableInterface movable = (MoveableInterface) m;
                    if (movable.isMoveable() && movable.insideXY(x, y)) {
                        return movable;
                    }
                }
            }
        }

        return getOverCursor(x, y);
    }

    public AbstractCursor getOverCursor(int x, int y) {
        if (m_cursorList != null) {
            int nb = m_cursorList.size();
            for (int i = nb - 1; i >= 0; i--) { // Last cursor in the list is at the front, so we check it first
                AbstractCursor c = m_cursorList.get(i);
                if (c.isMoveable() && c.insideXY(x, y)) {
                    return c;
                }
            }
        }

        return null;
    }

    /**
     * remove the specified marker, returns true if the marker was in the list,
     * false otherwise
     *
     * @param m
     * @return
     */
    public boolean removeMarker(AbstractMarker m) {
        if (m_markersList != null) {
            return m_markersList.remove(m);
        }
        return false;
    }

    public void clearMarkers() {
        if (m_markersList == null) {
            return;
        }
        m_markersList.clear();
    }

    public final void addMeasurement(AbstractMeasurement measurement) {
        if (m_measurements == null) {
            m_measurements = new ArrayList<>();
        }
        m_measurements.add(measurement);
    }

    public ArrayList<AbstractMeasurement> getMeasurements(ArrayList<AbstractMeasurement> measurements, AbstractMeasurement.MeasurementType type) {

        if (m_measurements == null) {
            return measurements;
        }
        for (AbstractMeasurement measurement : m_measurements) {
            if (measurement.getMeasurementType() == type) {
                measurements.add(measurement);

            }
        }
        return measurements;
    }

    public void paintMarkers(Graphics2D g) {
        if (!m_isPaintMarker) {
            return;
        }

        if (m_markersList == null) {
            return;
        }

        int nb = m_markersList.size();
        for (int i = 0; i < nb; i++) {
            m_markersList.get(i).paint(g);
        }
    }

    public void paintCursors(Graphics2D g) {

        if (m_cursorList == null) {
            return;
        }

        int nb = m_cursorList.size();
        for (int i = 0; i < nb; i++) {
            AbstractCursor cursor = m_cursorList.get(i);
            cursor.paint(g);
        }

    }

    public boolean getDoubleBufferingPolicy() {
        return false;
    }

    /**
     * Return true when the Plot Area needs to be repainted
     *
     * @param isPaintMarker
     * @return
     */
    public boolean setIsPaintMarker(boolean isPaintMarker) {
        return false;
    }

    public abstract boolean isMouseOnPlot(double x, double y);

    public abstract boolean isMouseOnSelectedPlot(double x, double y);

    public boolean isMouseWheelSupported() {
        return true;
    }

    public JPopupMenu getPopupMenu(double x, double y) {
        return null;
    }

    public void doubleClicked(int x, int y) {

    }
}
