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

import java.util.ArrayList;
import java.util.HashSet;

/**
 * @Karine XUE enum constant of each type of plot
 * @author JM235353
 */
public enum PlotType {

    HISTOGRAM_PLOT("Histogram", false, "on", null, "as"),
    SCATTER_PLOT("Scatter Plot", false, "X Axis:", "Y Axis:", null),
    VENN_DIAGRAM_PLOT("Venn Diagram", true, "Data:", null, null),
    PARALLEL_COORDINATES_PLOT("Parallel Coordinates", true, "Data:", null, null),
    LINEAR_PLOT("Linear Plot", false, "X Axis:", "Y Axis:", null);

    public static PlotType[] ALL_PLOTS = {HISTOGRAM_PLOT, SCATTER_PLOT, VENN_DIAGRAM_PLOT, PARALLEL_COORDINATES_PLOT};
    public static PlotType[] LINEAR_PLOTS = {LINEAR_PLOT};

    PlotType(String name, boolean illimitedAxis, String xLabel, String yLabel, String zLabel) {
        m_name = name;
        m_illimitedAxis = illimitedAxis;
        m_xLabel = xLabel;
        m_yLabel = yLabel;
        m_zLabel = zLabel;
    }

    private final boolean m_illimitedAxis;
    private final String m_name;
    private final String m_xLabel;
    private final String m_yLabel;
    private final String m_zLabel;

    public boolean needsMultiData() {
        return m_illimitedAxis;
    }

    public boolean needsX() {
        return (m_xLabel != null) && (!m_illimitedAxis);
    }

    public boolean needsY() {
        return (m_yLabel != null) && (!m_illimitedAxis);
    }

    public boolean needsZ() {
        return (m_zLabel != null) && (!m_illimitedAxis);
    }

    public String getXLabel() {
        return m_xLabel;
    }

    public String getYLabel() {
        return m_yLabel;
    }

    public String getZLabel() {
        return m_zLabel;
    }

    @Override
    public String toString() {
        return m_name;
    }

    public HashSet<Class> getAcceptedXValues() {

        HashSet<Class> acceptedValues = new HashSet<>(3);

        switch (this) {
            case VENN_DIAGRAM_PLOT:
            case HISTOGRAM_PLOT:
                acceptedValues.add(Double.class);
                acceptedValues.add(Float.class);
                acceptedValues.add(Integer.class);
                acceptedValues.add(Long.class);
                break;
            case SCATTER_PLOT:
            case LINEAR_PLOT:
            case PARALLEL_COORDINATES_PLOT:
                acceptedValues.add(Double.class);
                acceptedValues.add(Float.class);
                acceptedValues.add(Integer.class);
                acceptedValues.add(String.class);
                acceptedValues.add(Long.class);
                break;
        }
        return acceptedValues;
    }

    public HashSet<Class> getAcceptedValuesAsParam() {

        HashSet<Class> acceptedValues = new HashSet<>(2);

        switch (this) {
            case VENN_DIAGRAM_PLOT:
            case HISTOGRAM_PLOT:
            case PARALLEL_COORDINATES_PLOT:
                break;
            case SCATTER_PLOT:
            case LINEAR_PLOT:
                acceptedValues.add(Double.class);
                acceptedValues.add(Float.class);
                acceptedValues.add(Integer.class);
                break;
        }
        return acceptedValues;
    }

    public ArrayList<String> getZValues() {
        switch (this) {
            case HISTOGRAM_PLOT: {
                ArrayList<String> zParams = new ArrayList<>();
                zParams.add(PlotHistogram.HISTOGRAM_COUNT);
                zParams.add(PlotHistogram.HISTOGRAM_PERCENTAGE);
                return zParams;
            }
            case SCATTER_PLOT:
            case LINEAR_PLOT:
            case VENN_DIAGRAM_PLOT:
            case PARALLEL_COORDINATES_PLOT:
            default:
                return null;
        }
    }

}
