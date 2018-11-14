/*
 * @cea 
 * http://www.profiproteomics.fr
 * create date: 12 nov. 2018
 */
package fr.proline.studio.rsmexplorer.gui.xic.alignment;

import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;
import fr.proline.studio.graphics.BasePlotPanel;
import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.graphics.PlotScatter;
import fr.proline.studio.graphics.PlotType;
import fr.proline.studio.graphics.XAxis;
import fr.proline.studio.graphics.YAxis;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 *
 * @author Karine XUE
 */
public class PlotScatterXicCloud extends PlotScatter {

    private int transparence = 64;

    private Color m_color;
    private StringBuilder m_sBuffer;

    public PlotScatterXicCloud(BasePlotPanel plotPanel, ExtendedTableModelInterface compareDataInterface, CrossSelectionInterface crossSelectionInterface, int colX, int colY) {
        super(plotPanel, compareDataInterface, crossSelectionInterface, colX, colY);
        m_sBuffer = null;
        m_color = Color.ORANGE;
        m_plotType = PlotType.SCATTER_PLOT_XIC_CLOUD;
    }

    public void setColor(Color c) {
        this.m_color = new Color(c.getRed(), c.getGreen(), c.getBlue(), transparence);

    }

    /**
     * limite the initial axis Y bound
     *
     * @param m_yMin
     */
    public void setYMin(double m_yMin) {
        this.m_yMin = m_yMin;
    }

    /**
     * limite the initial axis Y bound
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

        if (m_sBuffer == null) {
            m_sBuffer = new StringBuilder();
        }

        String infoValue = ((RTCompareTableModel) m_compareDataInterface).getToolTipInfo(indexFound);

        m_sBuffer.append(infoValue);
        m_sBuffer.append("<BR>");
        m_sBuffer.append(m_plotPanel.getXAxis().getTitle());
        m_sBuffer.append(" : ");

        m_sBuffer.append(m_plotPanel.getXAxis().getExternalDecimalFormat().format(m_dataX[indexFound]));

        m_sBuffer.append(" <BR>");
        m_sBuffer.append(m_plotPanel.getYAxis().getTitle());

        if (m_dataY[indexFound] > 0) {
            m_sBuffer.append("+");
        }
        m_sBuffer.append(m_plotPanel.getYAxis().getExternalDecimalFormat().format(m_dataY[indexFound]));

        String tooltip = m_sBuffer.toString();
        m_sBuffer.setLength(0);
        return tooltip;

    }

    @Override
    public void paint(Graphics2D g) {

        XAxis xAxis = m_plotPanel.getXAxis();
        YAxis yAxis = m_plotPanel.getYAxis();

        // set clipping area
        int clipX = xAxis.valueToPixel(xAxis.getMinValue());
        int clipWidth = xAxis.valueToPixel(xAxis.getMaxValue()) - clipX;
        int clipY = yAxis.valueToPixel(yAxis.getMaxValue());
        int clipHeight = yAxis.valueToPixel(yAxis.getMinValue()) - clipY;
        g.setClip(clipX, clipY, clipWidth, clipHeight);

        // first plot non selected
        int size = (m_dataX != null) ? m_dataX.length : 0;
        for (int i = 0; i < size; i++) {
            if (m_selected[i]) {
                continue;
            }
            if (m_dataY[i] == Float.NaN || m_dataY[i] == Double.NaN) {
                continue;
            }
            int x = xAxis.valueToPixel(m_dataX[i]) + ((m_jitterX != null) ? m_jitterX[i] : 0);
            int y = yAxis.valueToPixel(m_dataY[i]) + ((m_jitterY != null) ? m_jitterY[i] : 0);

            g.setColor(m_color);

            g.fillOval(x - 3, y - 3, 6, 6);

        }

        // plot selected
        for (int i = 0; i < size; i++) {
            if (!m_selected[i]) {
                continue;
            }
            if (m_dataY[i] == Float.NaN || m_dataY[i] == Double.NaN) {
                continue;
            }
            int x = xAxis.valueToPixel(m_dataX[i]) + ((m_jitterX != null) ? m_jitterX[i] : 0);
            int y = yAxis.valueToPixel(m_dataY[i]) + ((m_jitterY != null) ? m_jitterY[i] : 0);

            g.setColor(m_color);

            g.fillOval(x - 3, y - 3, 6, 6);

            g.setColor(Color.black);
            g.drawOval(x - 3, y - 3, 6, 6);

        }

    }

}
