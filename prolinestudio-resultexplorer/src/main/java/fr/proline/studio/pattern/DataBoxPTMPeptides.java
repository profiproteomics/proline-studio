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

import fr.proline.core.orm.msi.dto.DMasterQuantPeptide;
import fr.proline.core.orm.msi.dto.DMasterQuantProteinSet;
import fr.proline.core.orm.msi.dto.DPeptideInstance;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.dam.tasks.data.ptm.PTMPeptideInstance;
import fr.proline.studio.dam.tasks.data.ptm.PTMSite;
import fr.proline.studio.dam.tasks.xic.DatabaseLoadLcMSTask;
import fr.proline.studio.dam.tasks.xic.DatabaseLoadXicMasterQuantTask;
import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;
import fr.proline.studio.extendedtablemodel.SecondAxisTableModelInterface;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.rsmexplorer.gui.PTMPeptidesTablePanel;
import fr.proline.studio.rsmexplorer.gui.xic.QuantChannelInfo;
import fr.proline.studio.rsmexplorer.gui.xic.XICComparePeptideTableModel;
import fr.proline.studio.rsmexplorer.gui.xic.XicAbundanceProteinTableModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * Databox used to display PTMPeptideInstance (with Peptide Match info): All
 * Peptide Matches or only best one
 *
 * @author VD225637
 */
public class DataBoxPTMPeptides extends AbstractDataBoxPTMPeptides {

    //For XIC Data only
    private QuantChannelInfo m_quantChannelInfo;
    private List<DMasterQuantPeptide> m_masterQuantPeptideList;
    private DMasterQuantProteinSet m_masterQuantProteinSet;

    
    private static DataboxType getDataboxType(boolean xicResult, boolean showAllPepMatches) {
        if (xicResult) {
            if (showAllPepMatches) {
                return DataboxType.DataBoxXicPTMPeptidesMatches;
            } else {
                return DataboxType.DataBoxXicPTMPeptides;
            }
        } else {
            if (showAllPepMatches) {
                return DataboxType.DataBoxPTMPeptidesMatches;
            } else {
                return DataboxType.DataBoxPTMPeptides;
            }
        }
    }

    /**
     * Create a DataBoxPTMPeptides : table view of PTMPeptideInstances. Specify
     * if this databox is displayed in a quantitation context and if all peptide
     * matches should be displayed or only best one
     *
     * @param xicResult
     */
    public DataBoxPTMPeptides(boolean xicResult, boolean showAllPepMatches) {
        super(getDataboxType(xicResult, showAllPepMatches), xicResult ? DataboxStyle.STYLE_XIC : DataboxStyle.STYLE_RSM);
        m_displayAllPepMatches = showAllPepMatches;
        m_isXICResult = xicResult;
        m_typeName = m_displayAllPepMatches ? "PSMs" : "Peptides";
        m_description = m_displayAllPepMatches ? "PSMs matching a modification site or cluster" : "Peptides matching of modification site or cluster";
        m_logger.debug(" ----> Created DataBoxPTMPeptides " + m_typeName);

        // Register Possible in parameters          
        super.registerParameters();
    }

    @Override
    public void createPanel() {
        PTMPeptidesTablePanel p = new PTMPeptidesTablePanel(m_displayAllPepMatches, m_isXICResult);
        p.setName(m_typeName);
        p.setDataBox(this);
        setDataBoxPanelInterface(p);
    }

    @Override
    protected void setSelectedPTMPeptide(PTMPeptideInstance pepInstance) {
        ((PTMPeptidesTablePanel) getDataBoxPanelInterface()).setSelectedPeptide(pepInstance);
    }

    @Override
    protected DPeptideMatch getSelectedPeptideMatch() {
        return ((PTMPeptidesTablePanel) getDataBoxPanelInterface()).getSelectedPeptideMatch();
    }

    @Override
    protected PTMPeptideInstance getSelectedPTMPeptide() {
        return ((PTMPeptidesTablePanel) getDataBoxPanelInterface()).getSelectedPTMPeptideInstance();
    }

    @Override
    public void updateData() {
        //LOG.debug("updateData called for "+ (m_displayAllPepMatches?" leaf ": " parent"));
        PTMPeptidesTablePanel panel = (PTMPeptidesTablePanel) getDataBoxPanelInterface();

        if (panel.isShowed()) {
            if (m_ptmPepInstances == null || m_ptmPepInstances.isEmpty()) {
                panel.setData(null, null, null, null, false);
                return;
            }

            //Get QuantInfo        
            if (m_isXICResult) {
                m_quantChannelInfo = (QuantChannelInfo) m_previousDataBox.getData(QuantChannelInfo.class);
                if (m_quantChannelInfo != null) {
                    panel.addSingleValue(m_quantChannelInfo);
                }
                m_masterQuantProteinSet = (DMasterQuantProteinSet) m_previousDataBox.getData(DMasterQuantProteinSet.class);
            }

            final List<PTMSite> notLoadedPtmSite = getNotLoadedPTMSite();

            if (notLoadedPtmSite.isEmpty()) {
                resetPrevPTMTaskId();
                if (m_isXICResult) {
                    loadXicAndPropagate();
                } else {
                    ((PTMPeptidesTablePanel) getDataBoxPanelInterface()).setData(null, m_ptmPepInstances, m_ptmClusters, null, true);
                    propagateDataChanged(PTMPeptideInstance.class);
                    propagateDataChanged(DPeptideMatch.class);
                    propagateDataChanged(ExtendedTableModelInterface.class);
                }
            } else {
                loadPtmSite(notLoadedPtmSite);
            }
        }
    }

    private Long m_previousXICTaskId = null;

    @Override
    protected void loadXicAndPropagate() {

        final int loadingId = setLoading();

        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, final long taskId, SubTask subTask, boolean finished) {
                if (subTask == null) {
                    if (m_quantChannelInfo != null) {
                        Map<Long, DMasterQuantPeptide> qpepByPepId = new HashMap<>();
                        for (DMasterQuantPeptide masterQPep : m_masterQuantPeptideList) {
                            qpepByPepId.put(masterQPep.getPeptideInstanceId(), masterQPep);
                        }
                        ((PTMPeptidesTablePanel) getDataBoxPanelInterface()).setData(taskId, m_ptmPepInstances, m_ptmClusters, qpepByPepId, finished);

                    } else {
                        AbstractDatabaseCallback mapCallback = new AbstractDatabaseCallback() {

                            @Override
                            public boolean mustBeCalledInAWT() {
                                return true;
                            }

                            @Override
                            public void run(boolean success, long task2Id, SubTask subTask, boolean finished) {
                                // m_logger.debug(" DB PTM peptide : DatabaseLoadLcMSTask run "+subTask+ " id "+task2Id+" subtask "+subTask+" finished "+finished);
                                m_quantChannelInfo = new QuantChannelInfo(m_ptmDataset.getDataset());
                                getDataBoxPanelInterface().addSingleValue(m_quantChannelInfo);

                                Map<Long, DMasterQuantPeptide> qpepByPepId = new HashMap<>();
                                for (DMasterQuantPeptide masterQPep : m_masterQuantPeptideList) {
                                    masterQPep.getPeptideInstanceId();
                                    qpepByPepId.put(masterQPep.getPeptideInstanceId(), masterQPep);
                                }
                                //m_logger.debug(" DB PTM peptide : DatabaseLoadLcMSTask run m_quantChannelInfo defined CALL setData ");                                
                                ((PTMPeptidesTablePanel) getDataBoxPanelInterface()).setData(taskId, m_ptmPepInstances, m_ptmClusters, qpepByPepId, finished);
                                unregisterTask(task2Id);
                                propagateDataChanged(PTMPeptideInstance.class);
                                propagateDataChanged(ExtendedTableModelInterface.class);
                            }
                        };
                        // ask asynchronous loading of data
                        DatabaseLoadLcMSTask maptask = new DatabaseLoadLcMSTask(mapCallback);
                        maptask.initLoadAlignmentForXic(getProjectId(), m_ptmDataset.getDataset());
                        registerTask(maptask);
                        if (finished) {
                            m_previousXICTaskId = null;
                            setLoaded(loadingId);
                            unregisterTask(taskId);
                        }
                        return;
                    }
                } else {
                    ((PTMPeptidesTablePanel) getDataBoxPanelInterface()).dataUpdated(subTask, finished); //subtask : some part data obtain: update
                }

                if (finished) {
                    m_previousXICTaskId = null;
                    setLoaded(loadingId);
                    unregisterTask(taskId);
                    propagateDataChanged(PTMPeptideInstance.class);
                    propagateDataChanged(ExtendedTableModelInterface.class);
                }
            }
        };

        // ask asynchronous loading of data
        m_masterQuantPeptideList = new ArrayList();
        if (m_previousXICTaskId != null) {
            // old task is suppressed if it has not been already done
            AccessDatabaseThread.getAccessDatabaseThread().abortTask(m_previousXICTaskId);
            m_previousXICTaskId = null;
        }
        if (m_ptmPepInstances != null && !m_ptmPepInstances.isEmpty()) {
            DatabaseLoadXicMasterQuantTask task = new DatabaseLoadXicMasterQuantTask(callback);
            List<Long> parentPepInstanceIdsL = m_ptmPepInstances.stream().map(ptmPepInst -> ptmPepInst.getPeptideInstance().getId()).collect(Collectors.toList());
            task.initLoadPeptides(getProjectId(), m_ptmDataset.getDataset(), parentPepInstanceIdsL.toArray(new Long[parentPepInstanceIdsL.size()]), m_masterQuantPeptideList, true);
            m_previousXICTaskId = task.getId();
            registerTask(task);
        }
    }

    @Override
    protected ArrayList<Integer> getSelectedIndex() {
        ArrayList<Long> selection = ((PTMPeptidesTablePanel) this.m_panel).getCrossSelectionInterface().getSelection();
        ArrayList<Integer> result = new ArrayList();
        for (Long l : selection) {
            result.add(l.intValue());
        }
        return result;
    }

    @Override
    public Object getData(Class parameterType, ParameterSubtypeEnum parameterSubtype) {

        DataBoxPanelInterface panel = getDataBoxPanelInterface();

        if ( (parameterType != null) &&
                (!(panel instanceof SplittedPanelContainer.ReactiveTabbedComponent)
                    || ((panel instanceof SplittedPanelContainer.ReactiveTabbedComponent)
                    && ((SplittedPanelContainer.ReactiveTabbedComponent) panel).isShowed()))  //JPM.DATABOX : this check could produce bugs
                && m_isXICResult) {

            // Returning single data
            if (parameterSubtype == ParameterSubtypeEnum.SINGLE_DATA) {
                if (parameterType.equals(SecondAxisTableModelInterface.class)) {
                    if (m_quantChannelInfo == null || m_masterQuantProteinSet == null) {
                        return null;
                    }
                    XicAbundanceProteinTableModel protTableModel = new XicAbundanceProteinTableModel();
                    protTableModel.setData(m_quantChannelInfo.getQuantChannels(), m_masterQuantProteinSet);
                    protTableModel.setName("Protein");
                    return protTableModel;
                }
                if (parameterType.equals(DPeptideInstance.class)) {
                    ((PTMPeptidesTablePanel) getDataBoxPanelInterface()).getSelectedPTMPeptideInstance();
                }
            }
            
            // Returning a list of data
            if (parameterSubtype == ParameterSubtypeEnum.LIST_DATA) {
                if (parameterType.equals(ExtendedTableModelInterface.class)) {
                    return getTableModelInterfaceList();
                }
            }
            
        }
        return super.getData(parameterType, parameterSubtype);
    }

    private List<XICComparePeptideTableModel> getTableModelInterfaceList() {
        List<XICComparePeptideTableModel> list = new ArrayList();
        if (m_quantChannelInfo == null && m_previousDataBox != null) {
            m_quantChannelInfo = (QuantChannelInfo) m_previousDataBox.getData(QuantChannelInfo.class);
        }

        if (m_quantChannelInfo != null && m_masterQuantPeptideList != null) {
            for (DMasterQuantPeptide quantPeptide : m_masterQuantPeptideList) {
                XICComparePeptideTableModel peptideData = new XICComparePeptideTableModel();
                peptideData.setData(m_quantChannelInfo.getQuantChannels(), quantPeptide, true);
                list.add(peptideData);
            }
        }

        return list;
    }

}
