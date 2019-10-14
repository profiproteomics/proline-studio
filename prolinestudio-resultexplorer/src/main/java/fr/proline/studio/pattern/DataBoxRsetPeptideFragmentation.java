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
package fr.proline.studio.pattern;

import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.studio.rsmexplorer.gui.spectrum.RsetPeptideFragmentationTablePanel;
import fr.proline.studio.rsmexplorer.gui.spectrum.PeptideFragmentationData;

/**
 *
 * @author AW
 */
public class DataBoxRsetPeptideFragmentation extends AbstractDataBox {

    private DPeptideMatch m_previousPeptideMatch = null;

    public DataBoxRsetPeptideFragmentation() {
        super(DataboxType.DataBoxRsetPeptideFragmentation, DataboxStyle.STYLE_RSET);

        // Name of this databox
        m_typeName = "Fragmentation Table";
        m_description = "Fragmentation table of a Peptide";

        // Register in parameters
        GroupParameter inParameter = new GroupParameter();
        inParameter.addParameter(DPeptideMatch.class, false);
        inParameter.addParameter(PeptideFragmentationData.class, false);
        registerInParameter(inParameter);

    }

    @Override
    public void createPanel() {
        RsetPeptideFragmentationTablePanel p = new RsetPeptideFragmentationTablePanel();
        p.setName(m_typeName);
        p.setDataBox(this);
        setDataBoxPanelInterface(p);
    }

    @Override
    public void dataChanged() {
        final PeptideFragmentationData fragmentationData = (PeptideFragmentationData) m_previousDataBox.getData(false, PeptideFragmentationData.class);
        final DPeptideMatch peptideMatch = (fragmentationData != null) ? fragmentationData.getPeptideMatch() : null;

        if ((m_previousPeptideMatch == peptideMatch) && (fragmentationData == null)) {
            return;
        }
        m_previousPeptideMatch = peptideMatch;

        if (peptideMatch == null) {
            ((RsetPeptideFragmentationTablePanel) getDataBoxPanelInterface()).setData(null, null);
        } else {
            ((RsetPeptideFragmentationTablePanel) getDataBoxPanelInterface()).setData(peptideMatch, fragmentationData);
        }

    }
}
