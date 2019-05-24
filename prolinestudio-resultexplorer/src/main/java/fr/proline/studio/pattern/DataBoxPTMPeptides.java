/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.pattern;

import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.msi.dto.DMasterQuantPeptide;
import fr.proline.core.orm.msi.dto.DMasterQuantProteinSet;
import fr.proline.core.orm.msi.dto.DPeptideInstance;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabasePTMSitesTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.dam.tasks.data.ptm.PTMDataset;
import fr.proline.studio.dam.tasks.data.ptm.PTMPeptideInstance;
import fr.proline.studio.dam.tasks.data.ptm.PTMSite;
import fr.proline.studio.dam.tasks.xic.DatabaseLoadLcMSTask;
import fr.proline.studio.dam.tasks.xic.DatabaseLoadXicMasterQuantTask;
import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;
import fr.proline.studio.extendedtablemodel.GlobalTabelModelProviderInterface;
import fr.proline.studio.extendedtablemodel.SecondAxisTableModelInterface;
import fr.proline.studio.rsmexplorer.gui.PTMPeptidesTablePanel;
import fr.proline.studio.rsmexplorer.gui.xic.PeptideTableModel;
import fr.proline.studio.rsmexplorer.gui.xic.QuantChannelInfo;
import fr.proline.studio.rsmexplorer.gui.xic.XicAbundanceProteinTableModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author VD225637
 */
public class DataBoxPTMPeptides extends AbstractDataBox {

    private ResultSummary m_rsm;
    protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer.ptm");     
    protected PTMDataset m_ptmDataset;
    protected List<PTMPeptideInstance> m_ptmPepInstancesPtmSite;
    private boolean m_isXICResult = true;
    //For XIC Data only
    private QuantChannelInfo m_quantChannelInfo;
    private List<DMasterQuantPeptide> m_masterQuantPeptideList;
    private DMasterQuantProteinSet m_masterQuantProteinSet;
 
    /**
     * Create a DataBoxPTMPeptides : table view of PTMPeptideInstances. 
     * By default, this databox is displayed in a quantitation context. 
     * 
     *
     */
    public DataBoxPTMPeptides() {
        this(true);
    }
    
    /**
     * Create a DataBoxPTMPeptides : table view of PTMPeptideInstances. 
     * Specify if this databox is displayed in a quantitation context. 
     * 
     * @param xicResult 
     */    
    public DataBoxPTMPeptides( boolean xicResult) {
        super(DataboxType.DataBoxPTMPeptides, DataboxStyle.STYLE_RSM);
        
        // Name of this databox
        m_typeName = "PTM Peptides";
        m_description = "Peptides of a [Group] PTM Sites";        
        m_isXICResult = xicResult;
        
        // Register Possible in parameters          
        // One ResultSummary
        GroupParameter inParameter = new GroupParameter();
        inParameter.addParameter(PTMPeptideInstance.class, true);
        inParameter.addParameter(PTMDataset.class, false);
        if(m_isXICResult)
            inParameter.addParameter(QuantChannelInfo.class, false);
        registerInParameter(inParameter);

        GroupParameter outParameter = new GroupParameter();
        outParameter.addParameter(PTMPeptideInstance.class, true);
        outParameter.addParameter(DPeptideInstance.class, true);
        outParameter.addParameter(ResultSummary.class, false);
        outParameter.addParameter(PTMDataset.class, false);
        registerOutParameter(outParameter);
    }

    @Override
    public void createPanel() {
            PTMPeptidesTablePanel p = new PTMPeptidesTablePanel(false, m_isXICResult);
            p.setName(m_typeName);
            p.setDataBox(this);
            setDataBoxPanelInterface(p);
    }

    @Override
    public void dataChanged() {
        
        PTMPeptidesTablePanel panel = (PTMPeptidesTablePanel) getDataBoxPanelInterface();
        
        //Get information from prevous box:     
        // -- PTM Dataset & RSM the ptm peptides belong to
        // -- List of PTM Peptides to display
        PTMDataset newPtmDataset = (PTMDataset) m_previousDataBox.getData(false, PTMDataset.class);        
        List<PTMPeptideInstance> newPtmPepInstancesPtmSite = (List<PTMPeptideInstance> ) m_previousDataBox.getData(false, PTMPeptideInstance.class, true);

//        // Load data if not yet loaded
//        if(newPtmPepInstancesPtmSite == null || newPtmPepInstancesPtmSite.isEmpty()){
//            
//        }
        
        
        
        boolean valueUnchanged  = Objects.equals(newPtmDataset, m_ptmDataset) && Objects.equals(newPtmPepInstancesPtmSite,m_ptmPepInstancesPtmSite);
        
        if(valueUnchanged){
            //selection may have changed 
            PTMPeptideInstance selectedPep = (PTMPeptideInstance)  m_previousDataBox.getData(false, PTMPeptideInstance.class);
            panel.setSelectedPeptide(selectedPep);
            return;
        }

        m_ptmDataset = newPtmDataset;
        m_ptmPepInstancesPtmSite = newPtmPepInstancesPtmSite;
        m_rsm = m_ptmDataset.getDataset().getResultSummary();

        // -- Selected PTM Peptide
        //DPeptideInstance selectedInsts = (DPeptideInstance) m_previousDataBox.getData(false, DPeptideInstance.class);
        //m_logger.debug("selected peptide Match, ptm {}", m_selecedDPeptideMatch.getPeptide().getTransientData().getPeptideReadablePtmString().getReadablePtmString());
        
        if (m_ptmPepInstancesPtmSite == null || m_ptmPepInstancesPtmSite.isEmpty()) {
            panel.setData(null, null, null, false);
            return;
        }

        //Get QuantInfo        
        if(m_isXICResult){
            m_quantChannelInfo = (QuantChannelInfo) m_previousDataBox.getData(false, QuantChannelInfo.class);
            if(m_quantChannelInfo != null)
                panel.addSingleValue(m_quantChannelInfo);
            m_masterQuantProteinSet = (DMasterQuantProteinSet) m_previousDataBox.getData(false, DMasterQuantProteinSet.class);       
        }

        final List<PTMSite> notLoadedPtmSite = new ArrayList<>();
        for(PTMPeptideInstance ptmPepInst : m_ptmPepInstancesPtmSite){
            ptmPepInst.getSites().stream().forEach(ptmSite -> {
                if(!ptmSite.isLoaded())
                   notLoadedPtmSite.add(ptmSite); 
            });               
        }
        
        if (notLoadedPtmSite.isEmpty()) {
            m_previousPTMTaskId = null;
            if(m_isXICResult) {
                loadXicAndPropagate();
            } else{
                ((PTMPeptidesTablePanel) getDataBoxPanelInterface()).setData(null, m_ptmPepInstancesPtmSite, null, true);                
                propagateDataChanged(PTMPeptideInstance.class);
                propagateDataChanged(ExtendedTableModelInterface.class);                
            }
        } else
            loadPtmSite(notLoadedPtmSite);
 
    }

    private Long m_previousPTMTaskId = null;
    
    protected void resetPrevPTMTaskId(){
        m_previousPTMTaskId = null;
    }
    
    protected void loadPtmSite(List<PTMSite> notLoadedPtmSite){
        
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
                    ((PTMPeptidesTablePanel) getDataBoxPanelInterface()).setData(null,null,null, finished);                
                } else {
                    m_previousPTMTaskId = null;
                    unregisterTask(taskId);
                    if(m_isXICResult) {
                        loadXicAndPropagate();
                    } else{
                        ((PTMPeptidesTablePanel) getDataBoxPanelInterface()).setData(null, m_ptmPepInstancesPtmSite, null, finished);                
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
    
    private Long m_previousXICTaskId = null;
    private void loadXicAndPropagate() {

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
                        for(DMasterQuantPeptide masterQPep : m_masterQuantPeptideList){
                            masterQPep.getPeptideInstanceId();
                            qpepByPepId.put(masterQPep.getPeptideInstanceId(), masterQPep);
                        }
                        ((PTMPeptidesTablePanel) getDataBoxPanelInterface()).setData(taskId, m_ptmPepInstancesPtmSite, qpepByPepId,finished);

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
                                for(DMasterQuantPeptide masterQPep : m_masterQuantPeptideList){
                                    masterQPep.getPeptideInstanceId();
                                    qpepByPepId.put(masterQPep.getPeptideInstanceId(), masterQPep);
                                }
                                //m_logger.debug(" DB PTM peptide : DatabaseLoadLcMSTask run m_quantChannelInfo defined CALL setData ");                                
                                ((PTMPeptidesTablePanel) getDataBoxPanelInterface()).setData(taskId, m_ptmPepInstancesPtmSite, qpepByPepId,finished);
                                unregisterTask(task2Id);
                                propagateDataChanged(PTMPeptideInstance.class);
                                propagateDataChanged(ExtendedTableModelInterface.class);
                            }
                        };
                        // ask asynchronous loading of data
                        DatabaseLoadLcMSTask maptask = new DatabaseLoadLcMSTask(mapCallback);
                        maptask.initLoadAlignmentForXic(getProjectId(), m_ptmDataset.getDataset());
                        registerTask(maptask);
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
        DatabaseLoadXicMasterQuantTask task = new DatabaseLoadXicMasterQuantTask(callback);
        if (m_ptmPepInstancesPtmSite != null && !m_ptmPepInstancesPtmSite.isEmpty()) {
            List<Long> parentPepInstanceIdsL = m_ptmPepInstancesPtmSite.stream().map(pi -> pi.getPeptideInstance().getId()).collect(Collectors.toList());
            task.initLoadPeptides(getProjectId(), m_ptmDataset.getDataset(), parentPepInstanceIdsL.toArray(new Long[parentPepInstanceIdsL.size()]), m_masterQuantPeptideList, true);
        }
        if(m_previousXICTaskId != null){
            // old task is suppressed if it has not been already done
            AccessDatabaseThread.getAccessDatabaseThread().abortTask(m_previousXICTaskId);
        }
        m_previousXICTaskId = task.getId();
        registerTask(task);
    }
    
    
    @Override
    public Object getData(boolean getArray, Class parameterType) {
        if (parameterType != null) {

            if (parameterType.equals(ResultSummary.class)) {
                if (m_rsm != null) {
                    return m_rsm;
                }
            } 

            if(parameterType.equals(PTMDataset.class)){
                return m_ptmDataset;
            }
            
            if (parameterType.equals(PTMPeptideInstance.class)) {
                PTMPeptideInstance selectedParentPepInstance = ((PTMPeptidesTablePanel) getDataBoxPanelInterface()).getSelectedPTMPeptideInstance();
                if (selectedParentPepInstance != null) {
                    return selectedParentPepInstance;
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
        if (parameterType != null && isList) {
            if (parameterType.equals(PTMPeptideInstance.class)) {
                List<PTMPeptideInstance> ptmPepInstances = new ArrayList(m_ptmPepInstancesPtmSite);
                return ptmPepInstances;
            } 
            if(parameterType.equals(DPeptideInstance.class)) {
                if(m_ptmPepInstancesPtmSite == null)
                    return new ArrayList<DPeptideInstance>();
                return  m_ptmPepInstancesPtmSite.stream().map(ptpPepInst ->ptpPepInst.getPeptideInstance()).collect(Collectors.toList());
            }                         
            if (parameterType.equals(ExtendedTableModelInterface.class) && m_isXICResult) {
                return getTableModelInterfaceList();
            }
            if (parameterType.equals(SecondAxisTableModelInterface.class) && m_isXICResult) {
                if (m_quantChannelInfo == null || m_masterQuantProteinSet == null) {
                    return null;
                }
                XicAbundanceProteinTableModel protTableModel = new XicAbundanceProteinTableModel();
                protTableModel.setData(m_quantChannelInfo.getQuantChannels(), m_masterQuantProteinSet);
                protTableModel.setName("Protein");
                return protTableModel;
            }            
        }
        return super.getData(getArray, parameterType, isList);
    }
    
    private List<ExtendedTableModelInterface> getTableModelInterfaceList() {
        List<ExtendedTableModelInterface> list = new ArrayList();
        if (m_quantChannelInfo == null && m_previousDataBox != null) {
            m_quantChannelInfo = (QuantChannelInfo) m_previousDataBox.getData(false, QuantChannelInfo.class);
        }

        if (m_quantChannelInfo != null && m_masterQuantPeptideList != null) {
            for (DMasterQuantPeptide quantPeptide : m_masterQuantPeptideList) {
                PeptideTableModel peptideTableModel = new PeptideTableModel(null);
                peptideTableModel.setData(m_quantChannelInfo.getQuantChannels(), quantPeptide, true);
                list.add(peptideTableModel);
            }
        }

        return list;
    } 

}
