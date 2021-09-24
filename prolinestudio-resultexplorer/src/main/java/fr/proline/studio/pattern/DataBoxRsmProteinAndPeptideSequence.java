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

import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.msi.dto.DInfoPTM;
import fr.proline.core.orm.msi.dto.DPeptideInstance;
import fr.proline.core.orm.msi.dto.DPeptideSet;
import fr.proline.core.orm.msi.dto.DProteinMatch;
import fr.proline.studio.dam.tasks.DatabasePTMsTask;
import fr.proline.studio.rsmexplorer.gui.RsmProteinAndPeptideSequencePanel;

/**
 * Databox : Protein Sequence + inner peptide sequence when peptide are given by
 * a previous databox.
 *
 * @author JM235353
 */
public class DataBoxRsmProteinAndPeptideSequence extends AbstractDataBox {

    public DataBoxRsmProteinAndPeptideSequence() {
        super(DataboxType.DataBoxRsmProteinAndPeptideSequence, DataboxStyle.STYLE_RSM);

        // Name of this databox
        m_typeName = "Protein Sequence";
        m_description = "Protein Sequence and its Peptides Sequences";

        // Register in parameters
        ParameterList inParameter = new ParameterList();
        inParameter.addParameter(DProteinMatch.class);
        inParameter.addParameter(DPeptideInstance.class, ParameterSubtypeEnum.SINGLE_DATA, false /* parameter is not compulsory */);
        inParameter.addParameter(ResultSummary.class); // needs a ProteinMatch and a ResultSummary (PeptideInstance is optionnal)
        registerInParameter(inParameter);

        // Register possible out parameters
        // none
    }

    @Override
    public void createPanel() {
        RsmProteinAndPeptideSequencePanel p = new RsmProteinAndPeptideSequencePanel();
        p.setName(m_typeName);
        p.setDataBox(this);
        setDataBoxPanelInterface(p);
    }

    @Override
    public void dataChanged() {
        DProteinMatch proteinMatch = (DProteinMatch) getData(DProteinMatch.class);
        DPeptideInstance selectedPeptide = (DPeptideInstance) getData(DPeptideInstance.class);
        ResultSummary resultSummary = (ResultSummary) getData(ResultSummary.class);

        if ((proteinMatch == null) || (resultSummary == null)) {
            ((RsmProteinAndPeptideSequencePanel) getDataBoxPanelInterface()).setData(null, null, null, null);
            return;
        }
        
        // register the link to the Transient Data
        linkCache(resultSummary);

        DPeptideSet peptideSet = proteinMatch.getPeptideSet(resultSummary.getId());
        if (peptideSet == null) {
            ((RsmProteinAndPeptideSequencePanel) getDataBoxPanelInterface()).setData(null, null, null, null);
            return;
        }

        DPeptideInstance[] peptideInstances = peptideSet.getPeptideInstances();

        // check that the selectedPeptide found in previous databox is really a peptide of the proteinMatch (they
        // can come from different databoxes)
        if (peptideInstances == null) {
            selectedPeptide = null;
        } else if (selectedPeptide != null) {

            boolean foundPeptide = false;
            for (DPeptideInstance peptideInstance : peptideInstances) {
                if (peptideInstance.getId() == selectedPeptide.getId()) {
                    foundPeptide = true;
                    break;
                }
            }
            if (!foundPeptide) {
                selectedPeptide = null;
            }
        }
        Long projectId = getProjectId();
        //retrive all DPTMInfo map
        if (DInfoPTM.getInfoPTMMap().isEmpty()) {
            DatabasePTMsTask ptmTask = new DatabasePTMsTask(null);
            ptmTask.initFillPTMInfo(projectId);
            ptmTask.fetchData();
        }
        ((RsmProteinAndPeptideSequencePanel) getDataBoxPanelInterface()).setData(projectId, proteinMatch, selectedPeptide, peptideInstances);
    }

}
