/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.pattern;

import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.studio.comparedata.CompareDataInterface;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabasePTMProteinSiteTask_V2;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.dam.tasks.data.PTMSite;
import fr.proline.studio.rsmexplorer.gui.PeptidesPTMSitePanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author VD225637
 */
public class DataBoxPTMSitePepMatches extends AbstractDataBox {

    private ResultSummary m_rsm ;
    protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");

            
    public DataBoxPTMSitePepMatches() { 
            super(DataboxType.DataBoxPTMSitePepMatches, DataboxStyle.STYLE_RSM);
         
        // Name of this databox
        m_typeName = "PTM Site's Peptide Matches";
        m_description = "Peptide Matches of a PTM Protein Sites ";

        // Register Possible in parameters
        // One ResultSummary
        GroupParameter inParameter = new GroupParameter();
        inParameter.addParameter(PTMSite.class, false);
        inParameter.addParameter(ResultSummary.class, false);
        registerInParameter(inParameter);
        
        inParameter = new GroupParameter();
        inParameter.addParameter(PTMSite.class, false);
        inParameter.addParameter(ResultSummary.class, false);
        inParameter.addParameter(Long.class, false);
        registerInParameter(inParameter);
        
        GroupParameter outParameter = new GroupParameter();
        outParameter.addParameter(PTMSite.class, true);
        outParameter.addParameter(ResultSummary.class, false);
        outParameter.addParameter(DPeptideMatch.class, false);
        registerOutParameter(outParameter);
    }
    
    
    @Override
    public void createPanel() {
        PeptidesPTMSitePanel p = new PeptidesPTMSitePanel(true);
        p.setName(m_typeName);
        p.setDataBox(this);
        setDataBoxPanelInterface(p);        
    }

    @Override
    public void dataChanged() {

        final PTMSite ptmSite = (PTMSite) m_previousDataBox.getData(false, PTMSite.class); 
        final Long pepInstanceId = (Long) m_previousDataBox.getData(false, Long.class); 
        
       
        if (ptmSite == null) {
            ((PeptidesPTMSitePanel)getDataBoxPanelInterface()).setData(null);           
            return;
        }
        
        //Data already loaded. Just update panel.
        m_logger.info("DATA Changed : Update PepMatch WINDOWS. " + ptmSite.toString()+" data loaded " + ptmSite.isAllPeptideMatchesLoaded());
        if(ptmSite.isAllPeptideMatchesLoaded()){
            m_previousTaskId = null;
            ((PeptidesPTMSitePanel) getDataBoxPanelInterface()).setData(ptmSite, pepInstanceId);           
            propagateDataChanged(CompareDataInterface.class);
            return;
        }
        
        final int loadingId = setLoading(); 
        
        final ResultSummary rsm = (ResultSummary) m_previousDataBox.getData(false, ResultSummary.class);
        m_rsm = rsm;
        
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                m_logger.info(" Back from PepMatch task # "+taskId);
                if (success) {
                    ((PeptidesPTMSitePanel) getDataBoxPanelInterface()).setData(ptmSite,pepInstanceId);
                } else {
                    ((PeptidesPTMSitePanel) getDataBoxPanelInterface()).setData(null);
                }

                setLoaded(loadingId);
                
                if (finished) {
                    unregisterTask(taskId);
                    propagateDataChanged(CompareDataInterface.class);
                }
            }
        };
        
         if (m_previousTaskId != null) {
            // old task is suppressed if it has not been already done
            AccessDatabaseThread.getAccessDatabaseThread().abortTask(m_previousTaskId);
        }
         
        DatabasePTMProteinSiteTask_V2 task = new DatabasePTMProteinSiteTask_V2(callback, getProjectId(), rsm, ptmSite);
        Long taskId = task.getId();
        m_logger.info(" Call pepMatch DatabasePTMProteinSiteTask_V2 task # "+taskId);
        m_previousTaskId = taskId;
        registerTask(task);            
                             
    }
    
    private Long m_previousTaskId = null;
    
    @Override
    public Object getData(boolean getArray, Class parameterType) {
        if (parameterType != null) {

            if (parameterType.equals(ResultSummary.class)) {
                if (m_rsm != null) {
                    return m_rsm;
                }
            }

            if (parameterType.equals(PTMSite.class)) {
                return ((PeptidesPTMSitePanel) getDataBoxPanelInterface()).getSelectedPTMSite();
            }
            
            if (parameterType.equals(Long.class)) {
                return ((PeptidesPTMSitePanel) getDataBoxPanelInterface()).getSelectedPTMSite();
            }
            
            if (parameterType.equals(DPeptideMatch.class)) {
                return ((PeptidesPTMSitePanel) getDataBoxPanelInterface()).getSelectedPeptideMatchSite();
            }
            
        }
        
        return super.getData(getArray, parameterType);
    }
    
}
