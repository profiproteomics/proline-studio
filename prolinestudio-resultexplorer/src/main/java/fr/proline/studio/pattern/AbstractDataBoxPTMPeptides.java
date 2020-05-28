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
import fr.proline.core.orm.msi.dto.DMasterQuantProteinSet;
import fr.proline.core.orm.msi.dto.DPeptideInstance;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabasePTMSitesTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.dam.tasks.data.ptm.PTMCluster;
import fr.proline.studio.dam.tasks.data.ptm.PTMDataset;
import fr.proline.studio.dam.tasks.data.ptm.PTMPeptideInstance;
import fr.proline.studio.dam.tasks.data.ptm.PTMSite;
import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;
import fr.proline.studio.extendedtablemodel.GlobalTabelModelProviderInterface;
import fr.proline.studio.gui.SplittedPanelContainer;
import fr.proline.studio.rsmexplorer.gui.PTMPeptidesTablePanel;
import fr.proline.studio.rsmexplorer.gui.xic.QuantChannelInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author VD225637
 */
public abstract class AbstractDataBoxPTMPeptides extends AbstractDataBox {

    protected ResultSummary m_rsm;

    protected PTMDataset m_ptmDataset;
    protected List<PTMPeptideInstance> m_ptmPepInstances;
    protected List<PTMCluster> m_ptmClusters;

    protected boolean m_isXICResult = true;
    protected boolean m_displayAllPepMatches = false;
    protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer.ptm");

    private Long m_previousPTMTaskId = null;

    public AbstractDataBoxPTMPeptides(DataboxType type, DataboxStyle style) {
        super(type, style);
    }

    protected void registerParameters() {

        // Register in parameters          
        // One ResultSummary
        GroupParameter inParameter = new GroupParameter();
        inParameter.addParameter(PTMPeptideInstance.class, m_displayAllPepMatches ? ParameterSubtypeEnum.LEAF_PTMPeptideInstance : ParameterSubtypeEnum.PARENT_PTMPeptideInstance);
        inParameter.addParameter(PTMPeptideInstance.class);
        
        inParameter.addParameter(PTMDataset.class);
        if (m_isXICResult) {
            inParameter.addParameter(QuantChannelInfo.class);
            inParameter.addParameter(DMasterQuantProteinSet.class);
        }
        inParameter.addParameter(PTMCluster.class, ParameterSubtypeEnum.LIST_DATA);
        registerInParameter(inParameter);

        
        GroupParameter outParameter = new GroupParameter();
        outParameter.addParameter(PTMPeptideInstance.class);
        outParameter.addParameter(PTMPeptideInstance.class, ParameterSubtypeEnum.PARENT_PTMPeptideInstance);
        outParameter.addParameter(PTMPeptideInstance.class, ParameterSubtypeEnum.LEAF_PTMPeptideInstance);
        outParameter.addParameter(DPeptideMatch.class);
        outParameter.addParameter(DPeptideInstance.class);
        outParameter.addParameter(DPeptideInstance.class, ParameterSubtypeEnum.LIST_DATA);
        outParameter.addParameter(ResultSummary.class);
        outParameter.addParameter(PTMDataset.class);
        if (m_isXICResult) {
            outParameter.addParameter(ExtendedTableModelInterface.class);
        }
        outParameter.addParameter(MsQueryInfoRsm.class);
        registerOutParameter(outParameter);
    }

    protected boolean isAllPSMsDisplayed() {
        return m_displayAllPepMatches;
    }

    protected boolean isQuantiResult() {
        return m_isXICResult;
    }

    @Override
    public Object getDataImpl(Class parameterType, ParameterSubtypeEnum parameterSubtype) {
        DataBoxPanelInterface panel = getDataBoxPanelInterface();

        if (parameterType != null && (!(panel instanceof SplittedPanelContainer.ReactiveTabbedComponent)
                || (panel instanceof SplittedPanelContainer.ReactiveTabbedComponent && ((SplittedPanelContainer.ReactiveTabbedComponent) panel).isShowed()))) {
            
            if (parameterSubtype == ParameterSubtypeEnum.SINGLE_DATA) {

                if (parameterType.equals(Integer.class)) {
                    return getSelectedIndex();
                }
                if (parameterType.equals(ResultSummary.class)) {
                    if (m_rsm != null) {
                        return m_rsm;
                    }
                }

                if (parameterType.equals(PTMDataset.class)) {
                    return m_ptmDataset;
                }

                if (parameterType.equals(DPeptideInstance.class)) {
                    PTMPeptideInstance selectedParentPepInstance = getSelectedPTMPeptide();
                    if (selectedParentPepInstance != null) {
                        return selectedParentPepInstance.getPeptideInstance();
                    }
                }

                if (parameterType.equals(DPeptideMatch.class)) {
                    PTMPeptideInstance selectedParentPepInstance = getSelectedPTMPeptide();
                    if (selectedParentPepInstance != null) {
                    return selectedParentPepInstance.getRepresentativePepMatch(m_ptmClusters);
                    }
                }

                if (parameterType.equals(ExtendedTableModelInterface.class)) {
                    return ((GlobalTabelModelProviderInterface) getDataBoxPanelInterface()).getGlobalTableModelInterface();
                }

                if (parameterType.equals(MsQueryInfoRsm.class)) {
                    PTMPeptideInstance selectedParentPepInstance = getSelectedPTMPeptide();
                    if (selectedParentPepInstance != null) {
                        DPeptideMatch match = getSelectedPeptideMatch();
                        if (match != null) {
                            return new MsQueryInfoRsm(match.getMsQuery(), selectedParentPepInstance.getPeptideInstance().getResultSummary());
                            //return new MsQueryInfoRsm(match.getMsQuery(), m_ptmDataset.getDataset().getResultSummary());
                        }
                    }
                }
                
                if (parameterType.equals(PTMPeptideInstance.class)) {

                    PTMPeptideInstance selectedParentPepInstance = getSelectedPTMPeptide();
                    if (selectedParentPepInstance != null) {
                        return selectedParentPepInstance;
                    }
                }
            }
            
            if (parameterSubtype == ParameterSubtypeEnum.LIST_DATA) {
                if (parameterType.equals(DPeptideInstance.class)) {
                    if (m_ptmPepInstances == null) {
                        return new ArrayList<DPeptideInstance>();
                    }
                    return m_ptmPepInstances.stream().map(ptpPepInst -> ptpPepInst.getPeptideInstance()).collect(Collectors.toList());

                }
            }
   
            if (parameterType.equals(PTMPeptideInstance.class)) {


                //FIXME TODO  !!! VDS BIG WART !!! TO BE REMOVED WITH Propagate refactoring
                // Use "getArray" to specify parent (==> display best PepMatch) (if false) or leaf (==> display all peptife matches) PTMPeptideInstance... if true
                // if !getArray Return same data only if PARENT
                if ((parameterSubtype.equals(ParameterSubtypeEnum.PARENT_PTMPeptideInstance) && !m_displayAllPepMatches) || (parameterSubtype.equals(ParameterSubtypeEnum.LEAF_PTMPeptideInstance) && m_displayAllPepMatches)) {
                    if (m_ptmPepInstances == null) {
                        return new ArrayList<PTMPeptideInstance>();
                    }
                    List<PTMPeptideInstance> ptmPepInstances = new ArrayList(m_ptmPepInstances);
                    return ptmPepInstances;
                }

                
            }

        }

        return super.getDataImpl(parameterType, parameterSubtype);
    }



    @Override
    public void dataChanged() {

        // register the link to the Transient Data
        linkCache( m_rsm);

        //LOG.debug("dataChanged called for "+ (m_displayAllPepMatches?" leaf ": " parent"));
        //Get information from previous box:
        // -- PTM Dataset & RSM the ptm peptides belong to
        // -- List of PTM Peptides to display
        PTMDataset newPtmDataset = (PTMDataset) m_previousDataBox.getData(PTMDataset.class);

        //FIXME TODO  !!! VDS BIG WART !!! TO BE REMOVED WITH Propagate refactoring
        // Use "getArray" to specify parent (false) or leaf (true) PTMPeptideInstance... corresponding to view only best vs view all peptide matches
        ParameterSubtypeEnum parameterSubtype = m_displayAllPepMatches ? ParameterSubtypeEnum.LEAF_PTMPeptideInstance : ParameterSubtypeEnum.PARENT_PTMPeptideInstance;
        List<PTMPeptideInstance> newPtmPepInstances = (List<PTMPeptideInstance>) m_previousDataBox.getData(PTMPeptideInstance.class, parameterSubtype);

        boolean valueUnchanged = Objects.equals(newPtmDataset, m_ptmDataset) && Objects.equals(newPtmPepInstances, m_ptmPepInstances);
        if (valueUnchanged) {
            //selection may have changed 
            PTMPeptideInstance selectedPep = (PTMPeptideInstance) m_previousDataBox.getData(PTMPeptideInstance.class);
            setSelectedPTMPeptide(selectedPep);
            return;
        }

        m_ptmDataset = newPtmDataset;
        m_ptmPepInstances = newPtmPepInstances;
        m_ptmClusters = (List<PTMCluster>) m_previousDataBox.getData(PTMCluster.class, ParameterSubtypeEnum.LIST_DATA);
        m_rsm = m_ptmDataset.getDataset().getResultSummary();
        updateData();
    }

    protected List<PTMSite> getNotLoadedPTMSite() {

        final List<PTMSite> notLoadedPtmSite = new ArrayList<>();
        for (PTMPeptideInstance ptmPepInst : m_ptmPepInstances) {
            ptmPepInst.getPTMSites().stream().forEach(ptmSite -> {
                if (!ptmSite.isLoaded()) {
                    notLoadedPtmSite.add(ptmSite);
                }
            });
        }
        return notLoadedPtmSite;
    }

    protected void resetPrevPTMTaskId() {
        m_previousPTMTaskId = null;
    }

    protected void loadPtmSite(List<PTMSite> notLoadedPtmSite) {

        //Load PTMSite not yet loaded !
        final int loadingId = setLoading();

        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                setLoaded(loadingId);
                if (!success) {
                    ((PTMPeptidesTablePanel) getDataBoxPanelInterface()).setData(null, null, null, null, finished);
                } else {
                    m_previousPTMTaskId = null;
                    unregisterTask(taskId);
                    if (m_isXICResult) {
                        loadXicAndPropagate();
                    } else {
                        ((PTMPeptidesTablePanel) getDataBoxPanelInterface()).setData(null, m_ptmPepInstances, m_ptmClusters, null, finished);
                        addDataChanged(PTMPeptideInstance.class, null); //JPM.DATABOX : put null, because I don't know which subtype has been change : null means all. So it works as previously
                        addDataChanged(ExtendedTableModelInterface.class);
                        propagateDataChanged();
                    }
                }
            }
        };

        DatabasePTMSitesTask task = new DatabasePTMSitesTask(callback);
        task.initFillPTMSites(getProjectId(), m_rsm, notLoadedPtmSite);
        Long taskId = task.getId();
        if (m_previousPTMTaskId != null) {
            // old task is suppressed if it has not been already done
            AccessDatabaseThread.getAccessDatabaseThread().abortTask(m_previousPTMTaskId);
        }
        m_previousPTMTaskId = taskId;
        registerTask(task);
    }

    abstract protected DPeptideMatch getSelectedPeptideMatch();

    abstract protected PTMPeptideInstance getSelectedPTMPeptide();

    abstract protected void setSelectedPTMPeptide(PTMPeptideInstance pepInstance);

    abstract public void updateData();

    abstract protected void loadXicAndPropagate();
    
    abstract protected ArrayList<Integer> getSelectedIndex();
}
