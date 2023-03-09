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

import fr.proline.core.orm.msi.MasterQuantReporterIon;
import fr.proline.core.orm.msi.Peptide;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.msi.dto.DMasterQuantPeptideIon;
import fr.proline.core.orm.msi.dto.DPeptideInstance;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.core.orm.uds.dto.DDatasetType;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.dam.tasks.xic.DatabaseLoadXicMasterQuantTask;
import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;
import fr.proline.studio.extendedtablemodel.GlobalTabelModelProviderInterface;
import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.ParameterList;
import fr.proline.studio.pattern.ParameterSubtypeEnum;
import fr.proline.studio.rsmexplorer.gui.xic.QuantChannelInfo;
import fr.proline.studio.rsmexplorer.gui.xic.QuantPepMatchReporterIonPanel;
import fr.proline.studio.rsmexplorer.gui.xic.XicPeptideIonPanel;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author VD225637
 */
public class DataboxXicReporterIon extends AbstractDataBox {

    private DDataset m_dataset;
    private DMasterQuantPeptideIon m_masterQuantPeptideIon;
    private List<MasterQuantReporterIon> m_masterQuantRepIonsList;
    private QuantChannelInfo m_quantChannelInfo;

    private DDatasetType.QuantitationMethodInfo m_quantMethodInfo;

    public DataboxXicReporterIon() {
        super(DataboxType.DataboxXicReporterIon, DataboxStyle.STYLE_XIC);

        // Name of this databox
        m_typeName = "Quanti. PSMs";
        m_description = "All Quanti. PSMs of a Quanti. Peptide Ion";
        m_quantMethodInfo = DDatasetType.QuantitationMethodInfo.ISOBARIC_TAGGING;

        // Register in parameters
        ParameterList inParameter = new ParameterList();
        inParameter.addParameter(DDataset.class, ParameterSubtypeEnum.SINGLE_DATA, false /* not compulsory */);
        inParameter.addParameter(QuantChannelInfo.class, ParameterSubtypeEnum.SINGLE_DATA, false /* not compulsory */);
        inParameter.addParameter(DMasterQuantPeptideIon.class);
        registerInParameter(inParameter);

        // Register possible out parameters
        ParameterList outParameter = new ParameterList();
        outParameter.addParameter(ResultSummary.class);
        outParameter.addParameter(DDataset.class);
        outParameter.addParameter(DMasterQuantPeptideIon.class);
        outParameter.addParameter(QuantChannelInfo.class);
        outParameter.addParameter(DPeptideMatch.class);
        outParameter.addParameter(ExtendedTableModelInterface.class);
        outParameter.addParameter(DPeptideMatch.class);
        outParameter.addParameter(DDatasetType.QuantitationMethodInfo.class);
        registerOutParameter(outParameter);
        
    }
    
    @Override
    public void createPanel() {
        QuantPepMatchReporterIonPanel p = new QuantPepMatchReporterIonPanel();
        p.setName(m_typeName);
        p.setDataBox(this);
        setDataBoxPanelInterface(p);
        
        getDataBoxPanelInterface().addSingleValue(m_quantMethodInfo);
    }
    
    @Override
    public void dataChanged() {

        DMasterQuantPeptideIon oldPSMquant = m_masterQuantPeptideIon;

        m_masterQuantPeptideIon = (DMasterQuantPeptideIon) m_previousDataBox.getData(DMasterQuantPeptideIon.class);
        m_dataset = (DDataset) m_previousDataBox.getData(DDataset.class);
        m_quantChannelInfo = (QuantChannelInfo) m_previousDataBox.getData(QuantChannelInfo.class);
        if (m_masterQuantPeptideIon == null || m_masterQuantPeptideIon.equals(oldPSMquant)) {
            return;
        }
        m_quantMethodInfo = (DDatasetType.QuantitationMethodInfo) m_previousDataBox.getData(DDatasetType.QuantitationMethodInfo.class);

        final int loadingId = setLoading();

        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, final long taskId, SubTask subTask, boolean finished) {

                // register the link to the Transient Data
                if (m_dataset != null) {
                    linkCache(m_dataset.getResultSummary());
                }

                if (subTask == null) {
                        ((QuantPepMatchReporterIonPanel) getDataBoxPanelInterface()).setData(taskId, m_quantChannelInfo.getQuantChannels(), m_masterQuantRepIonsList, m_quantMethodInfo, finished);
                } else {
                    ((QuantPepMatchReporterIonPanel) getDataBoxPanelInterface()).dataUpdated(subTask, finished);
                }

                setLoaded(loadingId);

                if (finished) {
                    unregisterTask(taskId);
                    addDataChanged(ExtendedTableModelInterface.class);
                    propagateDataChanged();
                }
            }
        };

        // ask asynchronous loading of data
        m_masterQuantRepIonsList = new ArrayList();
        DatabaseLoadXicMasterQuantTask task = new DatabaseLoadXicMasterQuantTask(callback);
        task.initLoadReporterIons(getProjectId(), m_dataset, m_masterQuantPeptideIon, m_masterQuantRepIonsList);
        Long taskId = task.getId();
        registerTask(task);
        
    }

    public void setQuantitationMethodInfo(DDatasetType.QuantitationMethodInfo quantMethodInfo) {
        m_quantMethodInfo = quantMethodInfo;
        //should only br ISOBARIC !
        m_style = (m_quantMethodInfo.equals(DDatasetType.QuantitationMethodInfo.SPECTRAL_COUNTING)) ? DataboxStyle.STYLE_SC : DataboxStyle.STYLE_XIC;
        if (getDataBoxPanelInterface() != null) {
            getDataBoxPanelInterface().addSingleValue(m_quantMethodInfo);
        }
    }

    
    @Override
    public void setEntryData(Object data) {
        getDataBoxPanelInterface().addSingleValue(data);
        m_dataset = (DDataset) data;
        dataChanged();
    }
    
    @Override
    public Object getDataImpl(Class parameterType, ParameterSubtypeEnum parameterSubtype) {
        
        if (parameterType != null) {
            
            if (parameterSubtype == ParameterSubtypeEnum.SINGLE_DATA) {

                if (parameterType.equals(ResultSummary.class)) {
                    return m_dataset.getResultSummary();
                }
                if (parameterType.equals(MasterQuantReporterIon.class)) {
                    //VDS TODO : selected RepIon !
                    return ((XicPeptideIonPanel) getDataBoxPanelInterface()).getSelectedMasterQuantPeptideIon();
                }
                if (parameterType.equals(QuantChannelInfo.class)) {
                    if (m_quantChannelInfo != null) {
                        return m_quantChannelInfo;
                    }
                }
                if (parameterType.equals(DDatasetType.QuantitationMethodInfo.class)) {
                    if (m_quantMethodInfo != null) {
                        return m_quantMethodInfo;
                    }
                }
                if (parameterType.equals(DPeptideMatch.class)) {
                    //VDS TODO : selected RepIon !
                    if(m_masterQuantPeptideIon != null) {
                        DPeptideInstance pi = m_masterQuantPeptideIon.getPeptideInstance();
                        if (pi == null) {
                            return null;
                        }
                    }
                    return null;
                }
                if (parameterType.equals(DDataset.class)) {
                    return m_dataset;
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
    
    @Override
    public String getFullName() {
        if (m_dataset == null) {
            return super.getFullName();
        }
        return m_dataset.getName() + " " + getTypeName();
    }
    
    @Override
    public Class[] getDataboxNavigationOutParameterClasses() {
        Class[] classList = {DMasterQuantPeptideIon.class, DPeptideMatch.class};
        return classList;
    }
    
    @Override
    public String getDataboxNavigationDisplayValue() {
        DMasterQuantPeptideIon peptideIon = (DMasterQuantPeptideIon) getData(DMasterQuantPeptideIon.class);
        if (peptideIon != null) {
            DPeptideInstance peptideInstance = peptideIon.getPeptideInstance();
            if (peptideInstance != null) {
                Peptide peptide = peptideInstance.getPeptide();
                if (peptide != null) {
                    return peptide.getSequence();
                }
            }
        }
        return null;
    }
}
