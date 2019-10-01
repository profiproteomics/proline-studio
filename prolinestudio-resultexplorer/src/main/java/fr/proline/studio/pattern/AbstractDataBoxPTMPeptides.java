/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
    protected boolean m_isXICResult = true;
    protected boolean m_displayAllPepMatches = false;
    protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer.ptm");

    private Long m_previousPTMTaskId = null;

    public AbstractDataBoxPTMPeptides(DataboxType type, DataboxStyle style) {
        super(type, style);
    }

    protected void registerParameters() {

        // Register Possible in parameters          
        // One ResultSummary
        GroupParameter inParameter = new GroupParameter();
        inParameter.addParameter(PTMPeptideInstance.class, true, true);
        inParameter.addParameter(PTMDataset.class, false, true);
        if (m_isXICResult) {
            inParameter.addParameter(QuantChannelInfo.class, false);
            inParameter.addParameter(DMasterQuantProteinSet.class, false, true);
        }
        registerInParameter(inParameter);

        GroupParameter outParameter = new GroupParameter();
        outParameter.addParameter(PTMPeptideInstance.class, true);
        outParameter.addParameter(DPeptideMatch.class, false);
        outParameter.addParameter(DPeptideInstance.class, true);
        outParameter.addParameter(ResultSummary.class, false);
        outParameter.addParameter(PTMDataset.class, false);
        if (m_isXICResult) {
            outParameter.addParameter(ExtendedTableModelInterface.class, false);
        }
        if (m_displayAllPepMatches) {
            outParameter.addParameter(MsQueryInfoRset.class, false);
        }
        registerOutParameter(outParameter);
    }

    protected boolean isAllPSMsDisplayed() {
        return m_displayAllPepMatches;
    }

    protected boolean isQuantiResult() {
        return m_isXICResult;
    }

    @Override
    public Object getData(boolean getArray, Class parameterType) {
        DataBoxPanelInterface panel = getDataBoxPanelInterface();

        if (parameterType != null && (!(panel instanceof SplittedPanelContainer.ReactiveTabbedComponent)
                || (panel instanceof SplittedPanelContainer.ReactiveTabbedComponent && ((SplittedPanelContainer.ReactiveTabbedComponent) panel).isShowed()))) {

            if (parameterType.equals(ResultSummary.class)) {
                if (m_rsm != null) {
                    return m_rsm;
                }
            }

            if (parameterType.equals(PTMDataset.class)) {
                return m_ptmDataset;
            }

            if (parameterType.equals(PTMPeptideInstance.class)) {
                PTMPeptideInstance selectedParentPepInstance = getSelectedPTMPeptide();
                if (selectedParentPepInstance != null) {
                    return selectedParentPepInstance;
                }
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
                    return selectedParentPepInstance.getBestPepMatch();
                }
            }

            if (parameterType.equals(ExtendedTableModelInterface.class)) {
                return ((GlobalTabelModelProviderInterface) getDataBoxPanelInterface()).getGlobalTableModelInterface();
            }
        }

        return super.getData(getArray, parameterType);
    }

    @Override
    public Object getData(boolean getArray, Class parameterType, boolean isList) {
        //LOG.debug("getData called for "+ (m_displayAllPepMatches?" leaf ": " parent")+" with var array/list :  " +getArray+" - "+isList);        
        DataBoxPanelInterface panel = getDataBoxPanelInterface();

        if (parameterType != null && isList && (!(panel instanceof SplittedPanelContainer.ReactiveTabbedComponent)
                || ((panel instanceof SplittedPanelContainer.ReactiveTabbedComponent) && ((SplittedPanelContainer.ReactiveTabbedComponent) panel).isShowed()))) {

            //FIXME TODO  !!! VDS BIG WART !!! TO BE REMOVED WITH Propagate refactoring
            // Use "getArray" to specify parent (==> display best PepMatch) or leaf (==> display all peptife matches) PTMPeptideInstance... 
            // if !getArrayReturn same data only if PARENT
            if (parameterType.equals(PTMPeptideInstance.class) && !getArray && !m_displayAllPepMatches
                    || (parameterType.equals(PTMPeptideInstance.class) && getArray && m_displayAllPepMatches)) {
                if (m_ptmPepInstances == null) {
                    return new ArrayList<DPeptideInstance>();
                }
                List<PTMPeptideInstance> ptmPepInstances = new ArrayList(m_ptmPepInstances);
                return ptmPepInstances;
            }
            if (parameterType.equals(DPeptideInstance.class)) {
                if (m_ptmPepInstances == null) {
                    return new ArrayList<DPeptideInstance>();
                }
                return m_ptmPepInstances.stream().map(ptpPepInst -> ptpPepInst.getPeptideInstance()).collect(Collectors.toList());
            }
        }
        return super.getData(getArray, parameterType, isList);
    }

    @Override
    public void dataChanged() {
        //LOG.debug("dataChanged called for "+ (m_displayAllPepMatches?" leaf ": " parent"));
        //Get information from previous box:
        // -- PTM Dataset & RSM the ptm peptides belong to
        // -- List of PTM Peptides to display
        PTMDataset newPtmDataset = (PTMDataset) m_previousDataBox.getData(false, PTMDataset.class);

        //FIXME TODO  !!! VDS BIG WART !!! TO BE REMOVED WITH Propagate refactoring
        // Use "getArray" to specify parent or leaf PTMPeptideInstance... corresponding to view only best vs view all peptide matches
        List<PTMPeptideInstance> newPtmPepInstances = (List<PTMPeptideInstance>) m_previousDataBox.getData(m_displayAllPepMatches, PTMPeptideInstance.class, true);

        boolean valueUnchanged = Objects.equals(newPtmDataset, m_ptmDataset) && Objects.equals(newPtmPepInstances, m_ptmPepInstances);
        if (valueUnchanged) {
            //selection may have changed 
            PTMPeptideInstance selectedPep = (PTMPeptideInstance) m_previousDataBox.getData(false, PTMPeptideInstance.class);
            setSelectedPTMPeptide(selectedPep);
            return;
        }

        m_ptmDataset = newPtmDataset;
        m_ptmPepInstances = newPtmPepInstances;
        m_rsm = m_ptmDataset.getDataset().getResultSummary();
        updateData();
    }

    protected List<PTMSite> getNotLoadedPTMSite() {

        final List<PTMSite> notLoadedPtmSite = new ArrayList<>();
        for (PTMPeptideInstance ptmPepInst : m_ptmPepInstances) {
            ptmPepInst.getSites().stream().forEach(ptmSite -> {
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
                    ((PTMPeptidesTablePanel) getDataBoxPanelInterface()).setData(null, null, null, finished);
                } else {
                    m_previousPTMTaskId = null;
                    unregisterTask(taskId);
                    if (m_isXICResult) {
                        loadXicAndPropagate();
                    } else {
                        ((PTMPeptidesTablePanel) getDataBoxPanelInterface()).setData(null, m_ptmPepInstances, null, finished);
                        propagateDataChanged(PTMPeptideInstance.class);
                        propagateDataChanged(ExtendedTableModelInterface.class);
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

    abstract protected PTMPeptideInstance getSelectedPTMPeptide();

    abstract protected void setSelectedPTMPeptide(PTMPeptideInstance pepInstance);

    abstract public void updateData();

    abstract protected void loadXicAndPropagate();
}
