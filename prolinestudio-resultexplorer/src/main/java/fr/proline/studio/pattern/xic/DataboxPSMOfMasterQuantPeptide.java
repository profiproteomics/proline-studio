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
package fr.proline.studio.pattern.xic;

import fr.proline.studio.pattern.*;
import fr.proline.core.orm.msi.dto.DMasterQuantPeptide;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.core.orm.uds.dto.DMasterQuantitationChannel;
import fr.proline.core.orm.uds.dto.DQuantitationChannel;
import fr.proline.studio.extendedtablemodel.GlobalTabelModelProviderInterface;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.dam.tasks.xic.DatabaseLoadXicMasterQuantTask;
import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.rsmexplorer.gui.xic.QuantChannelInfo;
import fr.proline.studio.rsmexplorer.gui.xic.QuantChannelPeptideMatchPanel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;

/**
 * Load the PSM of a Peptide
 *
 * @author JM235353
 */
public class DataboxPSMOfMasterQuantPeptide extends AbstractDataBox {

    private DDataset m_dataset;
    private DMasterQuantPeptide m_masterQuantPeptide;
    private DQuantitationChannel[] quantitationChannelArray = null;
    private Map<Long, List<Long>> m_peptideMatchIdListPerQC;
    private List<DPeptideMatch> m_peptideMatchList;
    private QuantChannelInfo m_quantChannelInfo;

    public DataboxPSMOfMasterQuantPeptide() {
        super(DataboxType.DataboxPSMOfMasterQuantPeptide, DataboxStyle.STYLE_XIC);

        // Name of this databox
        m_typeName = "PSM of Quanti. Peptide";
        m_description = "All PSM of a Quanti. Peptide";

        // Register in parameters
        ParameterList inParameter = new ParameterList();
        inParameter.addParameter(DMasterQuantPeptide.class);
        registerInParameter(inParameter);

        // Register possible out parameters
        ParameterList outParameter = new ParameterList();

        outParameter.addParameter(DPeptideMatch.class);
        outParameter.addParameter(ExtendedTableModelInterface.class);

        registerOutParameter(outParameter);

    }

    @Override
    public void createPanel() {
        QuantChannelPeptideMatchPanel p = new QuantChannelPeptideMatchPanel();
        p.setName(m_typeName);
        p.setDataBox(this);
        setDataBoxPanelInterface(p);
    }

    @Override
    public void dataChanged() {

        m_masterQuantPeptide = (DMasterQuantPeptide) getData(DMasterQuantPeptide.class);
        m_dataset = (DDataset) getData(DDataset.class);
        m_quantChannelInfo = (QuantChannelInfo) getData(QuantChannelInfo.class);

        if (m_masterQuantPeptide == null) {
            ((QuantChannelPeptideMatchPanel) getDataBoxPanelInterface()).setData((long)-1, null, m_quantChannelInfo, null, null, true);
            return;
        }
        final int loadingId = setLoading();

        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {

                // list quant Channels
                List<DQuantitationChannel> listQuantChannel = new ArrayList();
                if (m_dataset.getMasterQuantitationChannels() != null && !m_dataset.getMasterQuantitationChannels().isEmpty()) {
                    DMasterQuantitationChannel masterChannel = m_dataset.getMasterQuantitationChannels().get(0);
                    listQuantChannel = masterChannel.getQuantitationChannels();
                }
                quantitationChannelArray = new DQuantitationChannel[listQuantChannel.size()];
                listQuantChannel.toArray(quantitationChannelArray);

                if (subTask == null) {
                    ((QuantChannelPeptideMatchPanel) getDataBoxPanelInterface()).setData(taskId, quantitationChannelArray, m_quantChannelInfo, m_peptideMatchList, m_peptideMatchIdListPerQC, finished);
                } else {
                    ((QuantChannelPeptideMatchPanel) getDataBoxPanelInterface()).dataUpdated(subTask, finished);
                }

                setLoaded(loadingId);

                if (finished) {
                    unregisterTask(taskId);
                    addDataChanged(ExtendedTableModelInterface.class);
                    propagateDataChanged();
                }
            }
        };
        m_peptideMatchList = new ArrayList();
        m_peptideMatchIdListPerQC = new HashMap<>();
        // ask asynchronous loading of data
        DatabaseLoadXicMasterQuantTask task = new DatabaseLoadXicMasterQuantTask(callback);
        task.initLoadPSMForPeptide(getProjectId(), m_dataset, m_masterQuantPeptide, m_peptideMatchList, m_peptideMatchIdListPerQC);

        registerTask(task);

    }

    @Override
    public Object getDataImpl(Class parameterType, ParameterSubtypeEnum parameterSubtype) {
        
        if (parameterType != null) {
            if (parameterSubtype == ParameterSubtypeEnum.SINGLE_DATA) {
                if (parameterType.equals(DPeptideMatch.class)) {
                    return ((QuantChannelPeptideMatchPanel) getDataBoxPanelInterface()).getSelectedPSM();
                }
                if (parameterType.equals(ExtendedTableModelInterface.class)) {
                    return ((GlobalTabelModelProviderInterface) getDataBoxPanelInterface()).getGlobalTableModelInterface();
                }
                if (parameterType.equals(CrossSelectionInterface.class)) {
                    return ((GlobalTabelModelProviderInterface) getDataBoxPanelInterface()).getCrossSelectionInterface();
                }
            }
        }
        return super.getDataImpl(parameterType, parameterSubtype);
    }

}
