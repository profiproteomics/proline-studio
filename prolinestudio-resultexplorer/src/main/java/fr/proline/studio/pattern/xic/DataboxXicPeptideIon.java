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

import fr.proline.core.orm.msi.Peptide;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.msi.dto.DMasterQuantPeptide;

import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.core.orm.uds.dto.DDatasetType;
import fr.proline.studio.extendedtablemodel.GlobalTabelModelProviderInterface;
import fr.proline.core.orm.msi.dto.DMasterQuantPeptideIon;
import fr.proline.core.orm.msi.dto.DPeptideInstance;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.msi.dto.MasterQuantPeptideProperties;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.dam.tasks.xic.DatabaseLoadLcMSTask;
import fr.proline.studio.dam.tasks.xic.DatabaseLoadXicMasterQuantTask;
import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.pattern.AbstractDataBox;
import fr.proline.studio.pattern.ParameterList;
import fr.proline.studio.pattern.ParameterSubtypeEnum;
import fr.proline.studio.rsmexplorer.gui.xic.QuantChannelInfo;
import fr.proline.studio.rsmexplorer.gui.xic.XicPeptideIonPanel;
import fr.proline.studio.types.XicMode;
import java.util.ArrayList;
import java.util.List;
import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;
import java.util.Map;

/**
 *
 * @author JM235353
 */
public class DataboxXicPeptideIon extends AbstractDataBox {
    
    private DDataset m_dataset;
    private DMasterQuantPeptide m_masterQuantPeptide;
    private List<DMasterQuantPeptideIon> m_masterQuantPeptideIonList;
    private QuantChannelInfo m_quantChannelInfo;

    private DDatasetType.QuantitationMethodInfo m_quantMethodInfo;
    
    public DataboxXicPeptideIon() {
        super(DataboxType.DataboxXicPeptideIon, DataboxStyle.STYLE_XIC);

        // Name of this databox
        m_typeName = "Quanti. Peptides Ions";
        m_description = "All Peptides Ions of a Quanti. or Quanti Peptide";
        m_quantMethodInfo = DDatasetType.QuantitationMethodInfo.FEATURES_EXTRACTION;

        // Register in parameters
        ParameterList inParameter = new ParameterList();
        
        inParameter.addParameter(DDataset.class, ParameterSubtypeEnum.SINGLE_DATA, false /* not compulsory */);
        inParameter.addParameter(QuantChannelInfo.class, ParameterSubtypeEnum.SINGLE_DATA, false /* not compulsory */);
        inParameter.addParameter(DMasterQuantPeptide.class);
        inParameter.addParameter(DDatasetType.QuantitationMethodInfo.class);
        
        registerInParameter(inParameter);

        // Register possible out parameters
        ParameterList outParameter = new ParameterList();

        outParameter.addParameter(ResultSummary.class);
        outParameter.addParameter(DDataset.class);
        outParameter.addParameter(DMasterQuantPeptideIon.class);
        outParameter.addParameter(QuantChannelInfo.class);
        outParameter.addParameter(DPeptideMatch.class);
        
        outParameter.addParameter(ExtendedTableModelInterface.class);
        
        registerOutParameter(outParameter);
        
    }
    
    @Override
    public void createPanel() {
        XicPeptideIonPanel p = new XicPeptideIonPanel();
        p.setIsAllPeptideIon(m_previousDataBox == null);
        p.setName(m_typeName);
        p.setDataBox(this);
        setDataBoxPanelInterface(p);
        
        getDataBoxPanelInterface().addSingleValue(m_quantMethodInfo);
    }
    
    @Override
    public void dataChanged() {
        
        final boolean allPeptideIons = m_previousDataBox == null;
        DMasterQuantPeptide oldPeptide = m_masterQuantPeptide;
        
        if (!allPeptideIons) {
            m_masterQuantPeptide = (DMasterQuantPeptide) m_previousDataBox.getData(DMasterQuantPeptide.class);
            m_dataset = (DDataset) m_previousDataBox.getData(DDataset.class);
            m_quantChannelInfo = (QuantChannelInfo) m_previousDataBox.getData(QuantChannelInfo.class);
            if (m_masterQuantPeptide == null || m_masterQuantPeptide.equals(oldPeptide)) {
                return;
            }
            m_quantMethodInfo = (DDatasetType.QuantitationMethodInfo) m_previousDataBox.getData(DDatasetType.QuantitationMethodInfo.class);
        }
        
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
                    if (allPeptideIons)
                        m_quantChannelInfo = new QuantChannelInfo(m_dataset);
                   ((XicPeptideIonPanel) getDataBoxPanelInterface()).setData(taskId, m_quantChannelInfo.getQuantChannels(), m_masterQuantPeptideIonList, m_quantMethodInfo, finished);
                } else {
                    ((XicPeptideIonPanel) getDataBoxPanelInterface()).dataUpdated(subTask, finished);
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
        m_masterQuantPeptideIonList = new ArrayList();
        DatabaseLoadXicMasterQuantTask task = new DatabaseLoadXicMasterQuantTask(callback);
        if (allPeptideIons) {
            task.initLoadPeptideIons(getProjectId(), m_dataset, m_masterQuantPeptideIonList);
        } else {
            task.initLoadPeptideIons(getProjectId(), m_dataset, m_masterQuantPeptide, m_masterQuantPeptideIonList);
        }
        Long taskId = task.getId();
        registerTask(task);
        
    }

    public void setQuantitationMethodInfo(DDatasetType.QuantitationMethodInfo quantMethodInfo) {
        m_quantMethodInfo = quantMethodInfo;
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
                if (parameterType.equals(DMasterQuantPeptideIon.class)) {
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
                    DMasterQuantPeptideIon qpi = ((XicPeptideIonPanel) getDataBoxPanelInterface()).getSelectedMasterQuantPeptideIon();
                    if (qpi == null) {
                        return null;
                    }
                    DPeptideInstance pi = qpi.getPeptideInstance();
                    if (pi == null) {
                        return null;
                    }
                    return pi.getBestPeptideMatch();
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
