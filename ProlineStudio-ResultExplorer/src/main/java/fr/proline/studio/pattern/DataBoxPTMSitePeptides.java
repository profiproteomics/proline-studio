/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.pattern;

import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.studio.comparedata.CompareDataInterface;
import fr.proline.studio.comparedata.GlobalTabelModelProviderInterface;
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
public class DataBoxPTMSitePeptides extends AbstractDataBox {

    private ResultSummary m_rsm ;
    protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");

    public DataBoxPTMSitePeptides() { 
        super(DataboxType.DataBoxPTMSitePeptides, DataboxStyle.STYLE_RSM);
        
        // Name of this databox
        m_typeName = "PTM Site's Peptides";
        m_description = "Peptides of a PTM Protein Sites ";

        // Register Possible in parameters
        // One ResultSummary
        GroupParameter inParameter = new GroupParameter();
        inParameter.addParameter(PTMSite.class, false);
        inParameter.addParameter(ResultSummary.class, false);
        registerInParameter(inParameter);
        
        GroupParameter outParameter = new GroupParameter();
        outParameter.addParameter(PTMSite.class, true);
        outParameter.addParameter(ResultSummary.class, false);
        outParameter.addParameter(Long.class, false);
        registerOutParameter(outParameter);
    }
    
    
    @Override
    public void createPanel() {
        PeptidesPTMSitePanel p = new PeptidesPTMSitePanel(false);
        p.setName(m_typeName);
        p.setDataBox(this);
        setDataBoxPanelInterface(p);        
    }

    @Override
    public void dataChanged() {

        final PTMSite ptmSite = (PTMSite) m_previousDataBox.getData(false, PTMSite.class); 
        final ResultSummary rsm = (ResultSummary) m_previousDataBox.getData(false, ResultSummary.class);
        m_rsm = rsm;

        if (ptmSite == null) {
            ((PeptidesPTMSitePanel)getDataBoxPanelInterface()).setData(null);           
            return;
        }
         
        m_logger.info("DATA Changed : Update PTMSite Peptide WINDOWS. " + ptmSite.toString()+" data loaded " + ptmSite.isAllPeptideMatchesLoaded());
        if(ptmSite.isAllPeptideMatchesLoaded()){
            m_previousTaskId = null;
            ((PeptidesPTMSitePanel) getDataBoxPanelInterface()).setData(ptmSite);           
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
                m_logger.info(" Back from PTMSite Peptide task # "+taskId);
                 if (success) {
                     ((PeptidesPTMSitePanel) getDataBoxPanelInterface()).setData(ptmSite);
                } else {
                    ((PeptidesPTMSitePanel) getDataBoxPanelInterface()).setData(null);
                }

                setLoaded(loadingId);
                
                if (finished) {
                    m_previousTaskId = null;
                    unregisterTask(taskId);
                    propagateDataChanged(CompareDataInterface.class);
                }
            }
        };
        
        DatabasePTMProteinSiteTask_V2 task = new DatabasePTMProteinSiteTask_V2(callback, getProjectId(), rsm, ptmSite);
        Long taskId = task.getId();
        m_logger.info(" Call PTMSite Peptide DatabasePTMProteinSiteTask_V2 task # "+taskId);
        if (m_previousTaskId != null) {
            // old task is suppressed if it has not been already done
            AccessDatabaseThread.getAccessDatabaseThread().abortTask(m_previousTaskId);
        }
        m_previousTaskId = taskId;
        registerTask(task);
                      
    }
    
    private Long m_previousTaskId = null;
    
    @Override
    public Object getData(boolean getArray, Class parameterType) {
        if (parameterType != null) {

            if (parameterType.equals(ResultSummary.class)) {
                if (m_rsm != null)
                    return m_rsm;
            }

            if (parameterType.equals(PTMSite.class)) {
                PTMSite ptmSite = ((PeptidesPTMSitePanel) getDataBoxPanelInterface()).getSelectedPTMSite();
                if (ptmSite != null)
                    return ptmSite;
            }
            
            if (parameterType.equals(Long.class)) {
                Long selectedPepInstanceId = ((PeptidesPTMSitePanel) getDataBoxPanelInterface()).getSelectedPepInstanceId();
                if (selectedPepInstanceId != null)
                    return selectedPepInstanceId;
            }
            
             if (parameterType.equals(CompareDataInterface.class)) {
                return ((GlobalTabelModelProviderInterface) getDataBoxPanelInterface()).getGlobalTableModelInterface();
            }
        }
        
        return super.getData(getArray, parameterType);
    }
    
}
