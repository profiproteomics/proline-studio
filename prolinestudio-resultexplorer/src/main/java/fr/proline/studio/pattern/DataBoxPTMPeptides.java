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
package fr.proline.studio.pattern;

import fr.proline.core.orm.msi.Peptide;
import fr.proline.core.orm.msi.dto.DMasterQuantPeptide;
import fr.proline.core.orm.msi.dto.DMasterQuantProteinSet;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.uds.dto.DDatasetType;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.dam.tasks.data.ptm.PTMCluster;
import fr.proline.studio.dam.tasks.data.ptm.PTMPeptideInstance;
import fr.proline.studio.dam.tasks.xic.DatabaseLoadLcMSTask;
import fr.proline.studio.dam.tasks.xic.DatabaseLoadXicMasterQuantTask;
import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;
import fr.proline.studio.extendedtablemodel.SecondAxisTableModelInterface;
import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.rsmexplorer.gui.PTMPeptidesTablePanel;
import fr.proline.studio.rsmexplorer.gui.xic.QuantChannelInfo;
import fr.proline.studio.rsmexplorer.gui.xic.XICComparePeptideTableModel;
import fr.proline.studio.rsmexplorer.gui.xic.SecondAxisProteinAbundanceTableModel;
import fr.proline.studio.types.XicMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * Databox used to display PTMPeptideInstance (with Peptide Match info): All
 * Peptide Matches or only best one
 *
 * @author VD225637
 */
public class DataBoxPTMPeptides extends AbstractDataBoxPTMPeptides {

    //For Quantitation data only
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
     * Create a DataBoxPTMPeptides : table view of PTMPeptideInstances. By
     * default, this databox is displayed in a quantitation context and only
     * best Peptide Match is displayed
     * Warning:  Default Constructor should be defined for Custom view & "Diaplay" navigation
     *
     */
    public DataBoxPTMPeptides() {
        this(false, false);
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
        m_isMS1LabelFreeQuantitation = xicResult;
        StringBuilder stb = (m_isMS1LabelFreeQuantitation) ? new StringBuilder("Quanti. ") : new StringBuilder();
        m_typeName = m_displayAllPepMatches ? stb.append("Site PSMs").toString() : stb.append("Site Peptides").toString();
        stb = (m_isMS1LabelFreeQuantitation) ? new StringBuilder("Quantified ") : new StringBuilder();
        m_description = m_displayAllPepMatches ? stb.append("PSMs matching a modification site or cluster").toString() : stb.append("Peptides matching of modification site or cluster").toString();

        // Register in parameters          
        super.registerParameters();
    }

    @Override
    public void createPanel() {
        PTMPeptidesTablePanel p = new PTMPeptidesTablePanel(m_displayAllPepMatches, m_isMS1LabelFreeQuantitation);
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
            m_logger.trace("DataBoxPTMPeptides : **** updateData m_ptmPepInstances "+m_ptmPepInstances.size()+"; m_ptmClusters "+m_ptmClusters +" m_isMS1LabelFreeQuantitation: "+m_isMS1LabelFreeQuantitation);
            //Get QuantInfo        
            if (m_isMS1LabelFreeQuantitation) {
                m_quantChannelInfo = (QuantChannelInfo) getData(QuantChannelInfo.class);
                if (m_quantChannelInfo != null) {
                    panel.addSingleValue(m_quantChannelInfo);
                }
                m_masterQuantProteinSet = (DMasterQuantProteinSet) getData(DMasterQuantProteinSet.class);
            }

//            final List<PTMSite> notLoadedPtmSite = getNotLoadedPTMSite();
//            if (notLoadedPtmSite.isEmpty()) {
                resetPrevPTMTaskId();
                if (m_isMS1LabelFreeQuantitation) {
                    loadXicAndPropagate();
                } else {
                    ((PTMPeptidesTablePanel) getDataBoxPanelInterface()).setData(null, m_ptmPepInstances, m_ptmClusters, null, true);
                    addDataChanged(PTMPeptideInstance.class, null);  //   //JPM.DATABOX : put null, because I don't know which subtype has been change : null means all. So it works as previously
                    addDataChanged(DPeptideMatch.class);
                    addDataChanged(ExtendedTableModelInterface.class);
                    propagateDataChanged();
                }
//            } else {
//                loadPtmSite(notLoadedPtmSite);
//            }
        }
    }

    @Override
    public Class[] getDataboxNavigationOutParameterClasses() {
        if(isMS1LabelFreeQuantitation()) {
            return new Class[]{DMasterQuantPeptide.class, PTMPeptideInstance.class};
        } else {
            return new Class[]{ PTMPeptideInstance.class};
        }
    }

    @Override
    public String getDataboxNavigationDisplayValue() {
        try {
            Peptide peptide = null;
            if (isMS1LabelFreeQuantitation()) {
                DMasterQuantPeptide qPep = ((PTMPeptidesTablePanel) getDataBoxPanelInterface()).getSelectedMasterQuantPeptide();
                if (qPep != null)
                     peptide = qPep.getPeptideInstance().getPeptide();
            } else {
                DPeptideMatch p = (DPeptideMatch) getData(DPeptideMatch.class);
                if (p != null) {
                    peptide = p.getPeptide();
                }
            }

            if (peptide != null) {
                return peptide.getSequence();
            }

            return null;
        } catch (Exception e){
            m_logger.error("Error getting (quant) peptide information ",e);
            return null;
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
                m_logger.debug("DataBoxPTMPeptides : **** loadXicAndPropagate.initLoadPeptides CALLBACK subTask"+subTask+" finosh " +finished);
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
                                    qpepByPepId.put(masterQPep.getPeptideInstanceId(), masterQPep);
                                }
                                //m_logger.debug(" DB PTM peptide : DatabaseLoadLcMSTask run m_quantChannelInfo defined CALL setData ");                                
                                ((PTMPeptidesTablePanel) getDataBoxPanelInterface()).setData(taskId, m_ptmPepInstances, m_ptmClusters, qpepByPepId, finished);
                                unregisterTask(task2Id);
                                addDataChanged(PTMPeptideInstance.class, null); //  //JPM.DATABOX : put null, because I don't know which subtype has been change : null means all. So it works as previously
                                addDataChanged(ExtendedTableModelInterface.class);
                                propagateDataChanged();
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
                    addDataChanged(PTMPeptideInstance.class, null); //  //JPM.DATABOX : put null, because I don't know which subtype has been change : null means all. So it works as previously
                    addDataChanged(ExtendedTableModelInterface.class);
                    propagateDataChanged();
                }
            }
        };

        // ask asynchronous loading of data
        m_masterQuantPeptideList = new ArrayList<>();
        if (m_previousXICTaskId != null) {
            // old task is suppressed if it has not been already done
            AccessDatabaseThread.getAccessDatabaseThread().abortTask(m_previousXICTaskId);
            m_previousXICTaskId = null;
        }

        if(m_ptmClusters != null && !m_ptmClusters.isEmpty()) {
            m_logger.debug("DataBoxPTMPeptides : **** loadXicAndPropagate m_ptmClusters "+m_ptmClusters.size());

            Map<Long, DMasterQuantPeptide> qpepByPepId = new HashMap<>();
            for(PTMCluster cluster : m_ptmClusters) {
                List<DMasterQuantPeptide> qPeps = cluster.getRepresentativeMQPepMatch() != null ? cluster.getRepresentativeMQPepMatch().getAggregatedMQPeptides() : new ArrayList<>();
                m_masterQuantPeptideList.addAll(qPeps);
                m_logger.trace("DataBoxPTMPeptides : **** loadXicAndPropagate m_masterQuantPeptideList "+m_masterQuantPeptideList.size());
                for(DMasterQuantPeptide qPep : qPeps)
                    qpepByPepId.put(qPep.getPeptideInstanceId(), qPep);
            }

            ((PTMPeptidesTablePanel) getDataBoxPanelInterface()).setData(null, m_ptmPepInstances, m_ptmClusters, qpepByPepId, true);
            setLoaded(loadingId);
            addDataChanged(PTMPeptideInstance.class, null); //  //JPM.DATABOX : put null, because I don't know which subtype has been change : null means all. So it works as previously
            addDataChanged(ExtendedTableModelInterface.class);
            propagateDataChanged();

        } else if (m_ptmPepInstances != null && !m_ptmPepInstances.isEmpty()) {
            DatabaseLoadXicMasterQuantTask task = new DatabaseLoadXicMasterQuantTask(callback);
            m_logger.debug("DataBoxPTMPeptides : **** loadXicAndPropagate Task initLoadPeptides  "+task.getId());
            List<Long> parentPepInstanceIdsL = m_ptmPepInstances.stream().map(ptmPepInst -> ptmPepInst.getPeptideInstance().getId()).toList();
            task.initLoadPeptides(getProjectId(), m_ptmDataset.getDataset(), parentPepInstanceIdsL.toArray(new Long[parentPepInstanceIdsL.size()]), m_masterQuantPeptideList, true);
            m_previousXICTaskId = task.getId();
            registerTask(task);
        }
    }

    @Override
    protected ArrayList<Integer> getSelectedIndex() {
        ArrayList<Long> selection = ((PTMPeptidesTablePanel) this.m_panel).getCrossSelectionInterface().getSelection();
        ArrayList<Integer> result = new ArrayList<>();
        for (Long l : selection) {
            result.add(l.intValue());
        }
        return result;
    }

    @Override
    public Object getDataImpl(Class parameterType, ParameterSubtypeEnum parameterSubtype) {

        DataBoxPanelInterface panel = getDataBoxPanelInterface();

        if ( (parameterType != null) &&
                (!(panel instanceof SplittedPanelContainer.ReactiveTabbedComponent)
                    || ((panel instanceof SplittedPanelContainer.ReactiveTabbedComponent)
                    && ((SplittedPanelContainer.ReactiveTabbedComponent) panel).isShowed()))  //JPM.DATABOX : this check could produce bugs
                && m_isMS1LabelFreeQuantitation) {

            // Returning single data
            if (parameterSubtype == ParameterSubtypeEnum.SINGLE_DATA) {
                if (parameterType.equals(SecondAxisTableModelInterface.class)) {
                    if (m_quantChannelInfo == null || m_masterQuantProteinSet == null) {
                        return null;
                    }
                    SecondAxisProteinAbundanceTableModel protTableModel = new SecondAxisProteinAbundanceTableModel();
                    //VDS TODO: Use QuantMethodInfo in PTMView!
                    protTableModel.setData(m_quantChannelInfo.getQuantChannels(), m_masterQuantProteinSet, DDatasetType.QuantitationMethodInfo.FEATURES_EXTRACTION );
                    protTableModel.setName("Protein");
                    return protTableModel;
                }
//                if (parameterType.equals(DPeptideInstance.class)) {
//                    return  ((PTMPeptidesTablePanel) getDataBoxPanelInterface()).getSelectedPTMPeptideInstance();
//                }

                if (parameterType.equals(DMasterQuantPeptide.class)) {
                    return  ((PTMPeptidesTablePanel) getDataBoxPanelInterface()).getSelectedMasterQuantPeptide()    ;
                }
                if (parameterType.equals(XicMode.class)) {
                    return new XicMode(m_isMS1LabelFreeQuantitation);
                }
            }
            
            // Returning a list of data
            if (parameterSubtype == ParameterSubtypeEnum.LIST_DATA) {
                if (parameterType.equals(ExtendedTableModelInterface.class)) {
                    return getTableModelInterfaceList();
                }
            }
            
            if (parameterSubtype == ParameterSubtypeEnum.PEPTIDES_SELECTION_LIST) {
                if (parameterType.equals(Integer.class)) {
                    CrossSelectionInterface crossSelectionInterface = ((PTMPeptidesTablePanel) this.m_panel).getCrossSelectionInterface();
                    ArrayList<Integer> result = new ArrayList<>();
                    if (crossSelectionInterface != null) {
                        ArrayList<Long> selection = null;
                        try {
                            selection = crossSelectionInterface.getSelection();
                        } catch (Exception e) {
                            m_logger.error("wart : selection not ready ", e);
                        }
                        if (selection != null) {
                            for (Long l : selection) {
                                result.add(l.intValue());
                            }
                        }
                    }
                    return result;
                }
            }
            
        }
        return super.getDataImpl(parameterType, parameterSubtype);
    }

    private List<XICComparePeptideTableModel> getTableModelInterfaceList() {
        List<XICComparePeptideTableModel> list = new ArrayList<>();
        if (m_quantChannelInfo == null && m_previousDataBox != null) {
            m_quantChannelInfo = (QuantChannelInfo) getData(QuantChannelInfo.class);
        }

        if (m_quantChannelInfo != null && m_masterQuantPeptideList != null) {
            for (DMasterQuantPeptide quantPeptide : m_masterQuantPeptideList) {
                XICComparePeptideTableModel peptideData = new XICComparePeptideTableModel();
                //VDS TODO: Use QuantMethodInfo in PTMView!
                peptideData.setData(m_quantChannelInfo.getQuantChannels(), quantPeptide, DDatasetType.QuantitationMethodInfo.FEATURES_EXTRACTION);
                list.add(peptideData);
            }
        }

        return list;
    }

}
