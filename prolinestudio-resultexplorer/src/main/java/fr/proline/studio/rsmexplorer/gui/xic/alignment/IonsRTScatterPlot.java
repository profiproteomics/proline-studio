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
package fr.proline.studio.rsmexplorer.gui.xic.alignment;

import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;
import fr.proline.studio.graphics.*;
import fr.proline.studio.utils.CyclicColorPalette;

import java.awt.*;
import java.awt.geom.AffineTransform;

/**
 *
 * @author Karine XUE
 */
public class IonsRTScatterPlot extends PlotScatter {

    private static final int TRANSPARENCY = 70;
    private static final BasicStroke DEFAULT_STROKE = new BasicStroke(1.2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);

    private Color m_color;
    private Color m_highlightColor = new Color(CyclicColorPalette.GRAY_DARK.getRed(), CyclicColorPalette.GRAY_DARK.getGreen(), CyclicColorPalette.GRAY_DARK.getBlue(), TRANSPARENCY+15);

    private boolean m_showCrossAssignedIons = true;

    private StringBuilder m_sBuilder;
    private int m_colY; //index of column Y
    private double m_featureAlignmentTimeTolerance;

    public IonsRTScatterPlot(BasePlotPanel plotPanel, ExtendedTableModelInterface compareDataInterface, CrossSelectionInterface crossSelectionInterface, int colX, int colY) {
        super(plotPanel, compareDataInterface, crossSelectionInterface, colX, colY);
        m_sBuilder = new StringBuilder();
        m_color = CyclicColorPalette.getColor(2, TRANSPARENCY);
        m_colY = colY;
    }

    public void setColor(Color c) {
        this.m_color = new Color(c.getRed(), c.getGreen(), c.getBlue(), TRANSPARENCY);
    }

    public void setFeatureAlignmentTimeTolerance(double tolerance) {
        this.m_featureAlignmentTimeTolerance = tolerance;
    }

    public void showCrossAssignedIons(boolean showCrossAssignedIons) {
        this.m_showCrossAssignedIons = showCrossAssignedIons;
    }

    /**
     * limit the initial axis Y bound
     *
     * @param m_yMin
     */
    public void setYMin(double m_yMin) {
        this.m_yMin = m_yMin;
    }

    /**
     * limit the initial axis Y bound
     *
     * @param m_yMax
     */
    public void setYMax(double m_yMax) {
        this.m_yMax = m_yMax;
    }

    @Override
    public String getToolTipText(double x, double y) {
        int indexFound = findPoint(x, y);
        if (indexFound == -1) {
            return null;
        }

        if (((IonsRTTableModel) this.m_compareDataInterface).isCrossAssigned(indexFound, m_colY) && !m_showCrossAssignedIons) {
            return null;
        }

        String infoValue = ((IonsRTTableModel) m_compareDataInterface).getToolTipInfo(indexFound);

        m_sBuilder.append(infoValue);
        m_sBuilder.append("<BR>");
        m_sBuilder.append(m_plotPanel.getXAxis().getTitle());
        m_sBuilder.append(" : ");

        m_sBuilder.append(m_plotPanel.getXAxis().getExternalDecimalFormat().format(m_dataX[indexFound]));

        m_sBuilder.append(" <BR>");
        m_sBuilder.append(m_plotPanel.getYAxis().getTitle());

        if (m_dataY[indexFound] > 0) {
            m_sBuilder.append("+");
        }
        m_sBuilder.append(m_plotPanel.getYAxis().getExternalDecimalFormat().format(m_dataY[indexFound]));

        String tooltip = m_sBuilder.toString();
        m_sBuilder.setLength(0);
        return tooltip;

    }

    @Override
    public void paint(Graphics2D g, XAxis xAxis, YAxis yAxis) {

        // set clipping area
        int clipX = xAxis.valueToPixel(xAxis.getMinValue());
        int clipWidth = xAxis.valueToPixel(xAxis.getMaxValue()) - clipX;
        int clipY = yAxis.valueToPixel(yAxis.getMaxValue());
        int clipHeight = yAxis.valueToPixel(yAxis.getMinValue()) - clipY;
        g.setClip(clipX, clipY, clipWidth, clipHeight);

        g.setStroke(DEFAULT_STROKE);

        // first plot non selected
        int size = (m_dataX != null) ? m_dataX.length : 0;
        for (int i = 0; i < size; i++) {
            if (m_selected[i]) {
                continue;
            }
            if (Double.isNaN(m_dataY[i])) {
                continue;
            }
            int x = xAxis.valueToPixel(m_dataX[i]) + ((m_jitterX != null) ? m_jitterX[i] : 0);
            int y = yAxis.valueToPixel(m_dataY[i]) + ((m_jitterY != null) ? m_jitterY[i] : 0);

            if (((IonsRTTableModel) this.m_compareDataInterface).isCrossAssigned(i, m_colY)) {
                if (m_showCrossAssignedIons) {
                    g.setColor(m_highlightColor);
                    g.drawOval(x - 3, y - 3, 6, 6);
                }
            } else if (Math.abs(m_dataY[i]) > m_featureAlignmentTimeTolerance) {
                AffineTransform savedTransform = g.getTransform();
                AffineTransform transform = g.getTransform();
                transform.translate(x, y);
                transform.rotate(Math.PI / 4.0);
                g.setTransform(transform);
                g.drawRect(- 2, - 2, 4, 4);
                g.setTransform(savedTransform);
            } else {
                g.setColor(m_color);
                g.fillOval(x - 3, y - 3, 6, 6);
            }
        }

        // plot selected
        for (int i = 0; i < size; i++) {
            if (!m_selected[i]) {
                continue;
            }
            if (Double.isNaN(m_dataY[i])) {
                continue;
            }
            int x = xAxis.valueToPixel(m_dataX[i]) + ((m_jitterX != null) ? m_jitterX[i] : 0);
            int y = yAxis.valueToPixel(m_dataY[i]) + ((m_jitterY != null) ? m_jitterY[i] : 0);

            g.setColor(m_color);

            if (!((IonsRTTableModel) this.m_compareDataInterface).isCrossAssigned(i, m_colY)) {
                g.fillOval(x - 3, y - 3, 6, 6);
                g.setColor(Color.black);
                g.drawOval(x - 3, y - 3, 6, 6);
            } else if (m_showCrossAssignedIons) {
                g.setColor(Color.black);
                g.drawOval(x - 3, y - 3, 6, 6);
            }

        }

    }

}
