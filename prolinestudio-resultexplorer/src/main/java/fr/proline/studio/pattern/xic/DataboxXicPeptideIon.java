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
package fr.proline.studio.pattern.xic;

import fr.proline.core.orm.msi.Peptide;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.msi.dto.DMasterQuantPeptide;

import fr.proline.core.orm.uds.dto.DDataset;
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
import fr.proline.studio.pattern.GroupParameter;
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
    
    private boolean m_isXICMode = true;
    
    public DataboxXicPeptideIon() {
        super(DataboxType.DataboxXicPeptideIon, DataboxStyle.STYLE_XIC);

        // Name of this databox
        m_typeName = "Quanti. Peptides Ions";
        m_description = "All Peptides Ions of a Quanti. Peptide";

        // Register Possible in parameters
        // One Dataset and list of Peptide
        GroupParameter inParameter = new GroupParameter();
        inParameter.addParameter(DDataset.class);
        registerInParameter(inParameter);
        
        inParameter = new GroupParameter();
        inParameter.addParameter(DMasterQuantPeptide.class);
        registerInParameter(inParameter);

        // Register possible out parameters
        GroupParameter outParameter = new GroupParameter();
        outParameter.addParameter(DMasterQuantPeptideIon.class);
        registerOutParameter(outParameter);
        
        outParameter = new GroupParameter();
        outParameter.addParameter(QuantChannelInfo.class);
        registerOutParameter(outParameter);
        
        outParameter = new GroupParameter();
        outParameter.addParameter(DPeptideMatch.class);
        registerOutParameter(outParameter);
        
        outParameter = new GroupParameter();
        outParameter.addParameter(DDataset.class);
        outParameter.addParameter(ResultSummary.class);
        registerOutParameter(outParameter);
        
        outParameter = new GroupParameter();
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
        
        getDataBoxPanelInterface().addSingleValue(new XicMode((m_isXICMode)));
    }
    
    @Override
    public void dataChanged() {
        
        final boolean allPeptides = m_previousDataBox == null;
        DMasterQuantPeptide oldPeptide = m_masterQuantPeptide;
        
        if (!allPeptides) {
            m_masterQuantPeptide = (DMasterQuantPeptide) m_previousDataBox.getData(DMasterQuantPeptide.class);
            m_dataset = (DDataset) m_previousDataBox.getData(DDataset.class);
            m_quantChannelInfo = (QuantChannelInfo) m_previousDataBox.getData(QuantChannelInfo.class);
            if (m_masterQuantPeptide == null || m_masterQuantPeptide.equals(oldPeptide)) {
                return;
            }
            m_isXICMode = ((XicMode) m_previousDataBox.getData(XicMode.class)).isXicMode();
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
                    if (!allPeptides) {
                        MasterQuantPeptideProperties properties = m_masterQuantPeptide.getMasterQuantPeptideProperties();
                        ((XicPeptideIonPanel) getDataBoxPanelInterface()).setData(taskId, m_quantChannelInfo.getQuantChannels(), m_masterQuantPeptideIonList, m_isXICMode, finished);
                    } else {
                        AbstractDatabaseCallback mapCallback = new AbstractDatabaseCallback() {
                            
                            @Override
                            public boolean mustBeCalledInAWT() {
                                return true;
                            }
                            
                            @Override
                            public void run(boolean success, long task2Id, SubTask subTask, boolean finished) {
                                m_quantChannelInfo = new QuantChannelInfo(m_dataset);
                                ((XicPeptideIonPanel) getDataBoxPanelInterface()).setData(taskId, m_quantChannelInfo.getQuantChannels(), m_masterQuantPeptideIonList, m_isXICMode, finished);
                                
                                if (finished) {
                                    unregisterTask(task2Id);
                                }
                            }
                        };
                        // ask asynchronous loading of data
                        DatabaseLoadLcMSTask taskMap = new DatabaseLoadLcMSTask(mapCallback);
                        taskMap.initLoadAlignmentForXic(getProjectId(), m_dataset);
                        registerTask(taskMap);
                    }
                    
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
        if (allPeptides) {
            task.initLoadPeptideIons(getProjectId(), m_dataset, m_masterQuantPeptideIonList);
        } else {
            task.initLoadPeptideIons(getProjectId(), m_dataset, m_masterQuantPeptide, m_masterQuantPeptideIonList);
        }
        Long taskId = task.getId();
        registerTask(task);
        
    }
    
    public boolean isXICMode() {
        return m_isXICMode;
    }
    
    public void setXICMode(boolean isXICMode) {
        m_isXICMode = isXICMode;
        m_style = (m_isXICMode) ? DataboxStyle.STYLE_XIC : DataboxStyle.STYLE_SC;
        if (getDataBoxPanelInterface() != null) {
            getDataBoxPanelInterface().addSingleValue(new XicMode((isXICMode)));
        }
    }
    
    @Override
    public void setEntryData(Object data) {
        getDataBoxPanelInterface().addSingleValue(data);
        m_dataset = (DDataset) data;
        dataChanged();
    }
    
    @Override
    public Object getData(Class parameterType, ParameterSubtypeEnum parameterSubtype) {
        
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
                if (parameterType.equals(QuantChannelInfo.class)) {
                    return m_quantChannelInfo;
                }
            }
        }
        return super.getData(parameterType, parameterSubtype);
    }
    
    @Override
    public String getFullName() {
        return m_dataset.getName() + " " + getTypeName();
    }
    
    @Override
    public Class[] getImportantInParameterClass() {
        Class[] classList = {DMasterQuantPeptideIon.class, DPeptideMatch.class};
        return classList;
    }
    
    @Override
    public String getImportantOutParameterValue() {
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
