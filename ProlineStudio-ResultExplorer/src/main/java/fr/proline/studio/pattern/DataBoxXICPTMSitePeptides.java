/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.pattern;

import fr.proline.core.orm.lcms.MapAlignment;
import fr.proline.core.orm.lcms.ProcessedMap;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.msi.dto.DMasterQuantPeptide;
import fr.proline.core.orm.msi.dto.DMasterQuantProteinSet;
import fr.proline.core.orm.msi.dto.DPeptideInstance;
import fr.proline.core.orm.msi.dto.DProteinMatch;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.core.orm.uds.dto.DMasterQuantitationChannel;
import fr.proline.core.orm.uds.dto.DQuantitationChannel;
import fr.proline.studio.comparedata.CompareDataInterface;
import fr.proline.studio.comparedata.GlobalTabelModelProviderInterface;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabasePTMProteinSiteTask_V2;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.dam.tasks.data.PTMSite;
import fr.proline.studio.dam.tasks.xic.DatabaseLoadLcMSTask;
import fr.proline.studio.dam.tasks.xic.DatabaseLoadXicMasterQuantTask;
import static fr.proline.studio.pattern.DataBoxPTMSitePeptides.m_logger;
import fr.proline.studio.rsmexplorer.gui.xic.QuantChannelInfo;
import fr.proline.studio.rsmexplorer.gui.xic.XicPeptidePanel;
import fr.proline.studio.rsmexplorer.gui.xic.XicPeptidesPTMSitePanel;
import fr.proline.studio.types.XicMode;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author VD225637
 */
public class DataBoxXICPTMSitePeptides extends AbstractDataBox {

    private ResultSummary m_rsm ;
    private DDataset m_dataset;
    
    public DataBoxXICPTMSitePeptides(){
        super(DataboxType.DataBoxXICPTMSitePeptides, DataboxStyle.STYLE_XIC);

        // Name of this databox
        m_typeName = "Quantification : PTM Site's Peptides";
        m_description = "Quantification of all Peptides of a PTM Protein Sites";

        // Register Possible in parameters
        // One ResultSummary
        GroupParameter inParameter = new GroupParameter();
        inParameter.addParameter(PTMSite.class, false);
        inParameter.addParameter(ResultSummary.class, false);
        registerInParameter(inParameter);
        
        GroupParameter outParameter = new GroupParameter();
        outParameter.addParameter(PTMSite.class, true);
        outParameter.addParameter(ResultSummary.class, false);
        outParameter.addParameter(DPeptideInstance.class, false);
        registerOutParameter(outParameter);
    } 
    
     @Override
    public void createPanel() {
        XicPeptidesPTMSitePanel p = new XicPeptidesPTMSitePanel();
        p.setName(m_typeName);
        p.setDataBox(this);
        setDataBoxPanelInterface(p);        
    }
    
    private Long m_previousTaskId = null;
    @Override
    public void dataChanged() {
        
        final PTMSite ptmSite = (PTMSite) m_previousDataBox.getData(false, PTMSite.class); 
        m_dataset  = (DDataset) m_previousDataBox.getData(false, DDataset.class);
        m_rsm = m_dataset.getResultSummary();

        if (ptmSite == null) {
            ((XicPeptidesPTMSitePanel)getDataBoxPanelInterface()).setData(null);           
            return;
        }
        ptmSiteChanged(ptmSite);
    }
        
    private void ptmSiteChanged(PTMSite ptmSite){
         
        m_logger.info("DATA Changed : Update XIC PTMSite Peptide WINDOWS. " + ptmSite.toString()+" data loaded " + ptmSite.isAllPeptideMatchesLoaded());
        if(ptmSite.isAllPeptideMatchesLoaded()){
            m_previousTaskId = null;
            xicDataChanged(ptmSite,-1);
//            setPanelData(ptmSite);
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
                xicDataChanged(ptmSite,loadingId);
             }
        };
        
        DatabasePTMProteinSiteTask_V2 task = new DatabasePTMProteinSiteTask_V2(callback, getProjectId(), m_rsm, ptmSite);
        Long taskId = task.getId();
        m_logger.info(" Call PTMSite Peptide DatabasePTMProteinSiteTask_V2 task # "+taskId);
        if (m_previousTaskId != null) {
            // old task is suppressed if it has not been already done
            AccessDatabaseThread.getAccessDatabaseThread().abortTask(m_previousTaskId);
        }
        m_previousTaskId = taskId;
        registerTask(task);
    }
    
        //Load XIC Information    
    private DQuantitationChannel[] m_quantitationChannelArray = null;    
    private QuantChannelInfo m_quantChannelInfo;
    private List<DMasterQuantPeptide> m_masterQuantPeptideList;
    
    private void xicDataChanged(PTMSite ptmSite,int startedLoadingId){
        
        DMasterQuantProteinSet m_masterQuantProteinSet = (DMasterQuantProteinSet) m_previousDataBox.getData(false, DMasterQuantProteinSet.class);
        m_dataset = (DDataset) m_previousDataBox.getData(false, DDataset.class);
        m_quantChannelInfo = (QuantChannelInfo) m_previousDataBox.getData(false, QuantChannelInfo.class);       

        
        final int loadingId = (startedLoadingId <= 0) ? setLoading() : startedLoadingId ;

        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, final long taskId, SubTask subTask, boolean finished) {

                if (subTask == null) {
                    m_quantitationChannelArray = m_quantChannelInfo.getQuantChannels();
                    ((XicPeptidesPTMSitePanel) getDataBoxPanelInterface()).setData(taskId, m_quantitationChannelArray, m_masterQuantPeptideList,  ptmSite, finished);                    
                } else {
                    ((XicPeptidesPTMSitePanel) getDataBoxPanelInterface()).dataUpdated(subTask, finished);
                }
                setLoaded(loadingId);

                if (finished) {
                    m_previousTaskId = null;
                    unregisterTask(taskId);
                    propagateDataChanged(CompareDataInterface.class);
                }
            }
        };

        // ask asynchronous loading of data
        m_masterQuantPeptideList = new ArrayList();
        DatabaseLoadXicMasterQuantTask task = new DatabaseLoadXicMasterQuantTask(callback);
        task.initLoadPeptides(getProjectId(), m_dataset,  m_masterQuantProteinSet, m_masterQuantPeptideList, true);
        
        registerTask(task);

    }

     @Override
    public Object getData(boolean getArray, Class parameterType) {
        if (parameterType != null) {

            if (parameterType.equals(ResultSummary.class)) {
                if (m_rsm != null)
                    return m_rsm;
            }

//            if (parameterType.equals(PTMSite.class)) {
//                PTMSite ptmSite = ((XicPeptidesPTMSitePanel) getDataBoxPanelInterface()).getSelectedPTMSite();
//                if (ptmSite != null)
//                    return ptmSite;
//            }
            
//            if (parameterType.equals(DPeptideInstance.class)) {
//                DPeptideInstance selectedParentPepInstance = ((PeptidesPTMSitePanel) getDataBoxPanelInterface()).getSelectedPeptideInstance();
//                if (selectedParentPepInstance != null)
//                    return selectedParentPepInstance;
//            }
            
             if (parameterType.equals(CompareDataInterface.class)) {
                return ((GlobalTabelModelProviderInterface) getDataBoxPanelInterface()).getGlobalTableModelInterface();
            }
        }
        
        return super.getData(getArray, parameterType);
    }
}
