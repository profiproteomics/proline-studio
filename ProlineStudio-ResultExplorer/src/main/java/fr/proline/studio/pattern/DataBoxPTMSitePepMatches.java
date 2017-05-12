/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.pattern;

import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.msi.dto.DPeptideInstance;
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

    protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");
    private DPeptideInstance m_parentPeptideInstance;
            
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
        inParameter.addParameter(DPeptideInstance.class, false);
        registerInParameter(inParameter);
        
        GroupParameter outParameter = new GroupParameter();
        outParameter.addParameter(PTMSite.class, true);
        outParameter.addParameter(DPeptideMatch.class, false);
        outParameter.addParameter(MsQueryInfoRset.class, false);
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
        final ResultSummary rsm = (ResultSummary) m_previousDataBox.getData(false, ResultSummary.class);         
        final DPeptideInstance pepInstance = (DPeptideInstance) m_previousDataBox.getData(false, DPeptideInstance.class); 
       
        m_parentPeptideInstance = pepInstance;
        
        if (ptmSite == null) {
            ((PeptidesPTMSitePanel)getDataBoxPanelInterface()).setData(null);           
            return;
        }
        
        //Data already loaded. Just update panel.
        m_logger.info("DATA Changed : Update PepMatch WINDOWS. " + ptmSite.toString()+" data loaded " + ptmSite.isAllPeptideMatchesLoaded());
        if(ptmSite.isAllPeptideMatchesLoaded()){
            m_previousTaskId = null;
            ((PeptidesPTMSitePanel) getDataBoxPanelInterface()).setData(ptmSite, m_parentPeptideInstance);           
            propagateDataChanged(CompareDataInterface.class);
            return;
        }
        
        final int loadingId = setLoading(); 
                
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                m_logger.info(" Back from PepMatch task # "+taskId);
                if (success) {
                    ((PeptidesPTMSitePanel) getDataBoxPanelInterface()).setData(ptmSite,m_parentPeptideInstance);
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

            if (parameterType.equals(PTMSite.class)) {
                PTMSite ptmSite = ((PeptidesPTMSitePanel) getDataBoxPanelInterface()).getSelectedPTMSite();
                if (ptmSite != null)
                    return ptmSite;
            }
                        
            if (parameterType.equals(DPeptideMatch.class)) {
                DPeptideMatch selectedPeptideMatch = ((PeptidesPTMSitePanel) getDataBoxPanelInterface()).getSelectedPeptideMatchSite();
                if (selectedPeptideMatch != null)
                    return selectedPeptideMatch;
            }
            
            if (parameterType.equals(MsQueryInfoRset.class)) {
                DPeptideMatch pepMatch = ((PeptidesPTMSitePanel) getDataBoxPanelInterface()).getSelectedPeptideMatchSite();
                DPeptideInstance pepInstance = ((PeptidesPTMSitePanel) getDataBoxPanelInterface()).getSelectedPeptideInstance();
                if (pepMatch != null && pepInstance != null) {
                    return new MsQueryInfoRset(pepMatch.getMsQuery(), pepInstance.getResultSummary().getResultSet());
                }
            }
        }
        
        return super.getData(getArray, parameterType);
    }
    
}
