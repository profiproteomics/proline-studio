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
package fr.proline.studio.rsmexplorer.gui.spectrum;

import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.msi.dto.DSpectrum;
import fr.proline.studio.utils.DataFormat;
import org.jfree.chart.annotations.XYLineAnnotation;
import org.jfree.chart.annotations.XYPointerAnnotation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.Range;
import org.jfree.ui.TextAnchor;

import java.awt.*;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.ValueMarker;

/**
 * Panel used to display a Spectrum of a PeptideMatch
 *
 * @author CBy
 */
public class RsetPeptideSpectrumErrorPanel extends AbstractPeptideSpectrumPanel {

    private boolean m_isDisplayed = true;

    /**
     * Creates new form RsetPeptideSpectrumErrorPanel
     */
    public RsetPeptideSpectrumErrorPanel() {
        super("Delta Mass Error");
    }

    public void updateFragmentationPlot(DPeptideMatch peptideMatch, PeptideFragmentationData peptideFragmentationData) {
        if (m_isDisplayed) {
            m_chart.getPlot().removeChangeListener(this);
            constructSpectrumChart(peptideMatch);
            double maxY = Math.max(Math.abs(m_chart.getXYPlot().getRangeAxis().getLowerBound()), Math.abs(m_chart.getXYPlot().getRangeAxis().getUpperBound()));

            m_chart.getXYPlot().getRangeAxis().setUpperMargin(0.5);
            m_chart.getXYPlot().getRangeAxis().setLowerMargin(0.5);

            // set default auto bounds since this values will be used by restoreAutoBounds int he abstract super class
            m_chart.getXYPlot().getRangeAxis().setDefaultAutoRange(new Range(-maxY, maxY));
            m_chart.getXYPlot().getDomainAxis().setDefaultAutoRange(new Range(m_chart.getXYPlot().getDomainAxis().getLowerBound(), m_chart.getXYPlot().getDomainAxis().getUpperBound()));

            m_chart.getXYPlot().getRangeAxis().setRange(new Range(-maxY, maxY));

            annotateSpectrum();
            // revalidate();
            m_chart.getPlot().addChangeListener(this);
        }
    }

    @Override
    public double[][] getMassIntensityValues(DSpectrum spectrum) {

        double[][] data = new double[2][m_peptideFragmentationData.getFragmentMatches().length+1];
        int count = 0;
        for (PeptideFragmentationData.FragmentMatch match : m_peptideFragmentationData.getFragmentMatches()) {
            data[DSpectrum.MASSES_INDEX][count] = match.moz;
            data[DSpectrum.INTENSITIES_INDEX][count] = match.calculated_moz - match.moz;
            count++;
        }
        data[DSpectrum.MASSES_INDEX][count] = spectrum.getPrecursorMoz();
        data[DSpectrum.INTENSITIES_INDEX][count] = 0.0;

        return data;
    }


    @Override
    public void addCustomAnnotations() {

        PeptideFragmentationData.FragmentMatch[] fragmentationMatches = m_peptideFragmentationData.getFragmentMatches();
        XYPlot plot = (XYPlot) m_chart.getPlot();

        double spacerValue = pixelToValueRange(3);

        Marker baseline = new ValueMarker(0.0);
        baseline.setPaint(Color.BLACK);
        plot.addRangeMarker(baseline);

        for (PeptideFragmentationData.FragmentMatch match : fragmentationMatches) {

            double error = match.calculated_moz - match.moz;
            String label = (match.neutral_loss_mass != null && match.neutral_loss_mass > 0) ? match.label + "-" + Math.round(match.neutral_loss_mass) : match.label ;

            final XYPointerAnnotation pointer = new XYPointerAnnotation(label, match.moz, error + Math.signum(error)*spacerValue ,Math.signum(error) * 6.0 * Math.PI / 4.0);
            pointer.setBaseRadius(5.0);
            pointer.setTipRadius(0.0);
            pointer.setArrowWidth(2);
            pointer.setFont(new Font("SansSerif", Font.PLAIN, 9));

            Color color = match.isABCSerie() ? AbstractPeptideSpectrumPanel.ABC_SERIE_COLOR : AbstractPeptideSpectrumPanel.XYZ_SERIE_COLOR;
            pointer.setArrowPaint(color);
            pointer.setPaint(color);
            pointer.setTextAnchor((error > 0) ? TextAnchor.BOTTOM_CENTER : TextAnchor.TOP_CENTER);

            StringBuilder builder = new StringBuilder("<html>");
            builder.append("m/z: ").append(match.moz).append("<br>");
            builder.append("error: ").append(DataFormat.format(error, 6)).append("<br>");
            builder.append("ppm error: ").append(DataFormat.format(1e6*error/match.moz, 2)).append("ppm<br>");
            if (match.neutral_loss_mass != null && match.neutral_loss_mass > 0) {
                builder.append("<br>").append("Neutral loss: ").append(match.neutral_loss_mass).append("<br>");
            }
            builder.append("</html>");
            pointer.setToolTipText(builder.toString());
            plot.addAnnotation(pointer);

            BasicStroke stk = new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
            XYLineAnnotation line = new XYLineAnnotation(match.calculated_moz,0, match.calculated_moz, error, stk, color);
            plot.addAnnotation(line);
        }
    }

    @Override
    public void setShowed(boolean showed) {
        if (showed == m_isDisplayed) {
            return;
        }
        if (showed && !m_isDisplayed) {
            m_isDisplayed = true;
            if (m_peptideMatch != null) {
                updateFragmentationPlot(m_peptideMatch, m_peptideFragmentationData);
            }
        }
    }

    @Override
    public boolean isShowed() {
        return m_isDisplayed;
    }
}
