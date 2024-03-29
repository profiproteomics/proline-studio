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
package fr.proline.studio.rsmexplorer.gui.xic;

import fr.proline.core.orm.msi.dto.DMasterQuantProteinSet;
import fr.proline.core.orm.msi.dto.DProteinMatch;
import fr.proline.core.orm.msi.dto.DQuantProteinSet;
import fr.proline.core.orm.uds.dto.DQuantitationChannel;
import fr.proline.studio.graphics.PlotInformation;
import fr.proline.studio.extendedtablemodel.SecondAxisTableModelInterface;
import fr.proline.studio.graphics.PlotDataSpec;
import java.awt.Color;
import java.util.Map;

/**
 *
 * @author Karine XUE
 */
//VDS FIXME : Pas vraiment un modele pour les abondance de Proteines.... A Renommer ! 
public class XicAbundanceProteinTableModel extends XICComparePeptideTableModel implements SecondAxisTableModelInterface{

    private DMasterQuantProteinSet m_proteinSet = null;

    public XicAbundanceProteinTableModel() {
        super();
    }

    public void setData(DQuantitationChannel[] quantChannels, DMasterQuantProteinSet proteinSet) {
        this.m_quantChannels = quantChannels;
        this.m_proteinSet = proteinSet;
        fireTableDataChanged();
    }

   
    @Override
    public Object getValueAt(int row, int col) {
        if (m_quantChannels == null || m_proteinSet == null)
            return null;
        // Retrieve QuantChannel
        DQuantitationChannel qc = m_quantChannels[row];
        // retrieve quantPeptide for the quantChannelId
        Map<Long, DQuantProteinSet> quantProteinSetByQchIds = m_proteinSet.getQuantProteinSetByQchIds();
        DQuantProteinSet quantProteinSet = quantProteinSetByQchIds.get(qc.getId());
        switch (col) {
            case COLTYPE_QC_ID: {
                return qc.getId();
            }
            case COLTYPE_QC_NAME: {
                return qc.getName();
            }
            case COLTYPE_ABUNDANCE: {
                if (quantProteinSet == null || quantProteinSet.getAbundance() == null) {
                    return null;
                }
                return quantProteinSet.getAbundance().isNaN() ? null : quantProteinSet.getAbundance();
            }
            case COLTYPE_RAW_ABUNDANCE: {
                if (quantProteinSet == null || quantProteinSet.getRawAbundance() == null) {
                    return null;
                }
                return quantProteinSet.getRawAbundance().isNaN() ? null : quantProteinSet.getRawAbundance();
            }
            case COLTYPE_PSM: {
                if (quantProteinSet == null || quantProteinSet.getPeptideMatchesCount() == null) {
                    return null;
                }
                return quantProteinSet.getPeptideMatchesCount();
            }
        }
        return null; // should never happen
    }

    @Override
    public PlotInformation getPlotInformation() {
        PlotInformation plotInformation = new PlotInformation();

        if (m_proteinSet != null && m_proteinSet.getProteinSet() != null && m_proteinSet.getProteinSet().getTypicalProteinMatch() != null) {
            DProteinMatch proteinMatch = m_proteinSet.getProteinSet().getTypicalProteinMatch();
            StringBuilder sb = new StringBuilder(proteinMatch.getAccession()); //protein name, 
            plotInformation.setPlotTitle(sb.toString());
        }

        if (m_proteinSet.getSelectionLevel() < 2) {
            plotInformation.setPlotColor(Color.LIGHT_GRAY);
        } else {
            plotInformation.setPlotColor(Color.ORANGE);
        }
        plotInformation.setDrawPoints(true);
        plotInformation.setDrawGap(true);
        return plotInformation;
    }


//    @Override
//    public ArrayList<ExtraDataType> getExtraDataTypes() {
//        ArrayList<ExtraDataType> list = new ArrayList<>();
//        list.add(new ExtraDataType(DQuantitationChannel.class, true));
//        list.add(new ExtraDataType(DMasterQuantProteinSet.class, false));
//        registerSingleValuesAsExtraTypes(list);
//        return list;
//    }

    @Override
    public Object getValue(Class c) {
        if (c.equals(DMasterQuantProteinSet.class)) {
            return m_proteinSet;
        }
        return getSingleValue(c);
    }

    @Override
    public Object getRowValue(Class c, int row) {
        if (c.equals(DQuantitationChannel.class)) {
            return m_quantChannels[row];
        }
        return null;
    }

    //private static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer.XicAbundanceProteinTableModel");
    @Override
    public PlotDataSpec getDataSpecAt(int row) {
        // m_logger.debug("########call getDataSpecAt in Protein TableModel");
        PlotDataSpec result = new PlotDataSpec();
        DQuantitationChannel qc = m_quantChannels[row];
        // retrieve quantPeptide for the quantChannelId
        Map<Long, DQuantProteinSet> quantProteinSetByQchIds = m_proteinSet.getQuantProteinSetByQchIds();
        DQuantProteinSet quantProteinSet = quantProteinSetByQchIds.get(qc.getId());

        if (!(quantProteinSet == null || quantProteinSet.getRawAbundance() == null || quantProteinSet.getPeptideMatchesCount() == null)) {
            int matcheCount = quantProteinSet.getPeptideMatchesCount();
            //m_logger.debug(String.format("#########row %s, matchcount=%d", qc.getFullName(), matcheCount));
            if (matcheCount == 0) {
                result.setFill(PlotDataSpec.FILL.EMPTY);
            }
        }
        return result;
    }

}
