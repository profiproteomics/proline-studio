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
package fr.proline.studio.graphics.measurement;

import fr.proline.studio.graphics.BasePlotPanel;
import fr.proline.studio.graphics.PlotBaseAbstract;
import fr.proline.studio.graphics.PlotLinear;
import fr.proline.studio.graphics.XAxis;
import fr.proline.studio.graphics.YAxis;
import fr.proline.studio.graphics.cursor.VerticalCursor;
import fr.proline.studio.graphics.marker.LabelMarker;
import fr.proline.studio.graphics.marker.WaveformAreaMarker;
import fr.proline.studio.graphics.marker.XDeltaMarker;
import fr.proline.studio.graphics.marker.coordinates.DataCoordinates;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Base measurement class for measurements based on x1 / x2 interval selection
 * @author JM235353
 */
public class DeltaXMeasurement extends AbstractMeasurement {
    
    private VerticalCursor m_verticalCursor1;
    private VerticalCursor m_verticalCursor2;
    private XDeltaMarker m_horizontalDeltaMarker;
    private LabelMarker m_resultLabelMarker;
    protected WaveformAreaMarker m_waveformAreaMarker = null;
    
    protected PlotBaseAbstract m_plot;
    private AlgorithmMeasurement m_algorithm;
    
    
    public DeltaXMeasurement(PlotBaseAbstract plot) {
        m_plot = plot;
    }
    
    public final void setAlgorithm(AlgorithmMeasurement algorithm) {
        m_algorithm = algorithm;
    }
    
    @Override
    public void applyMeasurement(int x, int y) {
        BasePlotPanel basePlotPanel = m_plot.getBasePlotPanel();

        XAxis xAxis = basePlotPanel.getXAxis();
        double minX = xAxis.getMinValue();
        double maxX = xAxis.getMaxValue();
        double xCenter = xAxis.pixelToValue(x);
        
        double x1 = xCenter-(maxX-minX)/10;
        double x2 = xCenter+(maxX-minX)/10;
        if (x1<minX) {
            x2 += minX-x1;
            x1 = minX;
        }
        if (x2>maxX) {
            x1 -= x2-maxX;
            x2 = maxX;
        }

        YAxis yAxis = basePlotPanel.getYAxis();
        double minY = yAxis.getMinValue();
        double maxY = yAxis.getMaxValue();

        String res = m_algorithm.calculate(m_plot, x1, x2);
        
        m_verticalCursor1 = new VerticalCursor(basePlotPanel, x1);

        m_verticalCursor2 = new VerticalCursor(basePlotPanel, x2);

        m_horizontalDeltaMarker = new XDeltaMarker(basePlotPanel, x1, x2, (maxY - minY) / 2);
        
        if (m_plot instanceof PlotLinear) {
            m_waveformAreaMarker = new WaveformAreaMarker(basePlotPanel, ((PlotLinear) m_plot), new Color(196,196,196,128),  x1, x2);
        }
        m_resultLabelMarker = new LabelMarker(basePlotPanel, new DataCoordinates((x1 + x2) / 2, (maxY - minY) / 2), res, LabelMarker.ORIENTATION_XY_MIDDLE, LabelMarker.ORIENTATION_Y_TOP);

        m_plot.addCursor(m_verticalCursor1);
        m_plot.addCursor(m_verticalCursor2);
        if (m_waveformAreaMarker != null) {
            m_plot.addMarker(m_waveformAreaMarker);
        }
        m_plot.addMarker(m_horizontalDeltaMarker);
        m_plot.addMarker(m_resultLabelMarker);

        ActionListener widthModificationListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                double x1 = m_verticalCursor1.getValue();
                double x2 = m_verticalCursor2.getValue();
                if (x1 > x2) {
                    double tmp = x2;
                    x2 = x1;
                    x1 = tmp;
                }
                String res = m_algorithm.calculate(m_plot, x1, x2);
                m_resultLabelMarker.setLabel(res);
                DataCoordinates coordinates = (DataCoordinates) m_resultLabelMarker.getCoordinates();
                coordinates.setDataCoordinates((x1 + x2) / 2, (maxY - minY) / 2);

                m_horizontalDeltaMarker.set(x1, x2);
                if (m_waveformAreaMarker != null) {
                    m_waveformAreaMarker.set(x1, x2);
                }
            }

        };

        m_verticalCursor1.addActionListener(widthModificationListener);
        m_verticalCursor2.addActionListener(widthModificationListener);

        
    }
    
    @Override
    public String getName() {
        return m_algorithm.getName();
    }
    
    @Override
    public MeasurementType getMeasurementType() {
        return MeasurementType.X_AXIS_POPUP;
    }
    
    @Override
    public boolean canApply() {
        XAxis axisX = m_plot.getBasePlotPanel().getXAxis();
        
        if (axisX == null) {
            return false;
        }
        if (axisX.isEnum() || axisX.isLog()) {
            return false;
        }
        
        return true;
    }
    
    public static abstract class AlgorithmMeasurement {

        public abstract String getName();
        public abstract String calculate(PlotBaseAbstract plot, double x1, double x2);
    }
}
