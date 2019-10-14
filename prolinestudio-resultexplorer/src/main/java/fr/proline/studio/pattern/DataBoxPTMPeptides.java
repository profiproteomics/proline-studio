/* * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
 * Databox used to display PTMPeptideInstance (with Peptide Match info): 
 * All Peptide Matches or only best one
 *
 * @author VD225637
 */
public class DataBoxPTMPeptides extends AbstractDataBoxPTMPeptides {

    //For XIC Data only
    private QuantChannelInfo m_quantChannelInfo;
    private List<DMasterQuantPeptide> m_masterQuantPeptideList;
    private DMasterQuantProteinSet m_masterQuantProteinSet;
 
    /**
     * Create a DataBoxPTMPeptides : table view of PTMPeptideInstances. 
     * By default, this databox is displayed in a quantitation context and only 
     * best Peptide Match is displayed
     *
     */
    public DataBoxPTMPeptides() {
        this(true, false);
    }
    
    private static DataboxType getDataboxType(boolean xicResult, boolean showAllPepMatches){
        if(xicResult) {
            if(showAllPepMatches)
                return DataboxType.DataBoxXicPTMPeptidesMatches;
            else    
                return DataboxType.DataBoxXicPTMPeptides;
        } else {
            if(showAllPepMatches)
                return DataboxType.DataBoxPTMPeptidesMatches;
            else    
                return DataboxType.DataBoxPTMPeptides;            
        }
    }

    
    /**
     * Create a DataBoxPTMPeptides : table view of PTMPeptideInstances. 
     * Specify if this databox is displayed in a quantitation context and
     * if all peptide matches should be displayed or only best one
     * 
     * @param xicResult 
     */    
    public DataBoxPTMPeptides( boolean xicResult, boolean showAllPepMatches) {
        super(getDataboxType(xicResult,showAllPepMatches), DataboxStyle.STYLE_RSM);
        m_displayAllPepMatches = showAllPepMatches;
        m_isXICResult = xicResult;        
        
        // Name of this databox
        if(m_isXICResult) {
            m_typeName = m_displayAllPepMatches ? "Quantified PSM PTMs info" : "Quantified Peptides PTMs info";
            m_description = m_displayAllPepMatches ? "PTMs information of quantified PSMs of a [Group] Modification Site" :  "PTMs information of quantified Peptides of a [Group] Modification Site";
 
        } else {
            m_typeName = m_displayAllPepMatches ? "PSM PTMs info" : "Peptides PTMs info";
            m_description = m_displayAllPepMatches ? "PTMs information of PSM of a [Group] Modification Site" :  "PTMs information of Peptides of a [Group] Modification Site";        
        }
        m_logger.debug(" ----> Created DataBoxPTMPeptides "+m_typeName);

        
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
     protected void setSelectedPTMPeptide(PTMPeptideInstance pepInstance){
        ((PTMPeptidesTablePanel) getDataBoxPanelInterface()).setSelectedPeptide(pepInstance);
     }
     
    @Override
    protected PTMPeptideInstance getSelectedPTMPeptide(){
       return ((PTMPeptidesTablePanel) getDataBoxPanelInterface()).getSelectedPTMPeptideInstance();
     }
    
    @Override
    public void updateData(){
        //LOG.debug("updateData called for "+ (m_displayAllPepMatches?" leaf ": " parent"));
        PTMPeptidesTablePanel panel = (PTMPeptidesTablePanel) getDataBoxPanelInterface();        

        if(panel.isShowed()) {
            if (m_ptmPepInstances == null || m_ptmPepInstances.isEmpty()) {
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

            final List<PTMSite> notLoadedPtmSite = getNotLoadedPTMSite();
                    
            if (notLoadedPtmSite.isEmpty()) {
                resetPrevPTMTaskId();
                if(m_isXICResult) {
                    loadXicAndPropagate();
                } else{
                    ((PTMPeptidesTablePanel) getDataBoxPanelInterface()).setData(null, m_ptmPepInstances, null, true);                
                    propagateDataChanged(PTMPeptideInstance.class);
                    propagateDataChanged(DPeptideMatch.class);                    
                    propagateDataChanged(ExtendedTableModelInterface.class);                
                }
            } else
                loadPtmSite(notLoadedPtmSite);
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
                        for(DMasterQuantPeptide masterQPep : m_masterQuantPeptideList){
                            qpepByPepId.put(masterQPep.getPeptideInstanceId(), masterQPep);
                        }
                        ((PTMPeptidesTablePanel) getDataBoxPanelInterface()).setData(taskId, m_ptmPepInstances, qpepByPepId,finished);

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
                                ((PTMPeptidesTablePanel) getDataBoxPanelInterface()).setData(taskId, m_ptmPepInstances, qpepByPepId,finished);
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
        if(m_previousXICTaskId != null){
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
    public Object getData(boolean getArray, Class parameterType, boolean isList) {

        DataBoxPanelInterface panel = getDataBoxPanelInterface();
    
        if (parameterType != null && isList &&  ( !(panel instanceof SplittedPanelContainer.ReactiveTabbedComponent) ||
                             ( (panel instanceof SplittedPanelContainer.ReactiveTabbedComponent) && ((SplittedPanelContainer.ReactiveTabbedComponent) panel).isShowed()) )
                && m_isXICResult) {
                   
            if (parameterType.equals(ExtendedTableModelInterface.class)) {
                return getTableModelInterfaceList();
            }
            if (parameterType.equals(SecondAxisTableModelInterface.class) ) {
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
        return super.getData(getArray, parameterType, isList);
    }
    
    private List<ExtendedTableModelInterface> getTableModelInterfaceList() {
        List<ExtendedTableModelInterface> list = new ArrayList();
        if (m_quantChannelInfo == null && m_previousDataBox != null) {
            m_quantChannelInfo = (QuantChannelInfo) m_previousDataBox.getData(false, QuantChannelInfo.class);
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
