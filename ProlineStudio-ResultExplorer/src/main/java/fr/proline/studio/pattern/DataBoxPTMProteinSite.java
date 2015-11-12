package fr.proline.studio.pattern;

import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.msi.dto.DProteinMatch;
import fr.proline.core.orm.msi.dto.DProteinPTMSite;
import fr.proline.studio.comparedata.CompareDataInterface;
import fr.proline.studio.comparedata.GlobalTabelModelProviderInterface;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabasePTMProteinSiteTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.graphics.CrossSelectionInterface;
import fr.proline.studio.rsmexplorer.gui.PTMProteinSitePanel;
import fr.proline.studio.table.GlobalTableModelInterface;
import java.util.ArrayList;

/**
 *
 * @author JM235353
 */
public class DataBoxPTMProteinSite extends AbstractDataBox {
    
    private ResultSummary m_rsm = null;

    
    public DataBoxPTMProteinSite() { 
        super(DataboxType.DataBoxPTMProteinSite);
        
        // Name of this databox
        m_typeName = "PTM Protein Sites";
        m_description = "PTM Protein Sites of an Identification Summary";

        // Register Possible in parameters
        // One ResultSummary
        GroupParameter inParameter = new GroupParameter();
        inParameter.addParameter(ResultSummary.class, false);
        registerInParameter(inParameter);


        // Register possible out parameters
        // One or Multiple ProteinSet
        /*GroupParameter outParameter = new GroupParameter();
        outParameter.addParameter(DProteinSet.class, true);
        outParameter.addParameter(ResultSummary.class, false);
        registerOutParameter(outParameter);*/ //JPM.TODO

        GroupParameter outParameter = new GroupParameter();
        outParameter.addParameter(DProteinPTMSite.class, true);
        outParameter.addParameter(ResultSummary.class, false);
        registerOutParameter(outParameter);
        
        outParameter = new GroupParameter();
        outParameter.addParameter(DProteinMatch.class, true); 
        outParameter.addParameter(ResultSummary.class, false);
        registerOutParameter(outParameter);
        
        outParameter = new GroupParameter();
        outParameter.addParameter(DPeptideMatch.class, true);
        outParameter.addParameter(ResultSummary.class, false);
        registerOutParameter(outParameter);
        
        outParameter = new GroupParameter();
        outParameter.addParameter(CompareDataInterface.class, true);
        registerOutParameter(outParameter);

        
    }


    @Override
    public void createPanel() {
        PTMProteinSitePanel p = new PTMProteinSitePanel();
        p.setName(m_typeName);
        p.setDataBox(this);
        m_panel = p;
    }
    
    @Override
    public void dataChanged() {

        final int loadingId = setLoading();

        final ArrayList<DProteinPTMSite> proteinPTMSiteArray = new ArrayList<>();
        
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {

                ((PTMProteinSitePanel) m_panel).setData(taskId, proteinPTMSiteArray, finished);
                
                //PTMProteinSitePanel //JPM.TODO
                
                /*if (subTask == null) {
                    DProteinSet[] proteinSetArray = m_rsm.getTransientData().getProteinSetArray();
                    ((RsmProteinSetPanel) m_panel).setData(taskId, proteinSetArray, finished);
                    
                    if (m_dataToBeSelected != null) {
                        ((RsmProteinSetPanel) m_panel).selectData(m_dataToBeSelected);
                        m_dataToBeSelected = null;
                    }
                } else {
                    ((RsmProteinSetPanel) m_panel).dataUpdated(subTask, finished);
                    
                }*/
                
                

                setLoaded(loadingId);
                
                if (finished) {
                    unregisterTask(taskId);
                    propagateDataChanged(CompareDataInterface.class);
                }
            }
        };


        // ask asynchronous loading of data

        DatabasePTMProteinSiteTask task = new DatabasePTMProteinSiteTask(callback, getProjectId(), m_rsm, proteinPTMSiteArray);
        Long taskId = task.getId();
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
        if (parameterType!= null ) {
            /*if (parameterType.equals(DProteinSet.class)) {
                return ((RsmProteinSetPanel)m_panel).getSelectedProteinSet();
            }*/ //JPM.TODO
            if (parameterType.equals(ResultSummary.class)) {
                if (m_rsm != null) {
                    return m_rsm;
                }
            }
            
            
            if (parameterType.equals(DProteinMatch.class)) {
                DProteinPTMSite proteinPtmSite = ((PTMProteinSitePanel)m_panel).getSelectedProteinPTMSite();
                if (proteinPtmSite != null) {
                    return proteinPtmSite.getPoteinMatch();
                }
            }
            if (parameterType.equals(DPeptideMatch.class)) {
                DProteinPTMSite proteinPtmSite = ((PTMProteinSitePanel)m_panel).getSelectedProteinPTMSite();
                if (proteinPtmSite != null) {
                    return proteinPtmSite.getPeptideMatch();
                }
            }
            if (parameterType.equals(DProteinPTMSite.class)) {
                return ((PTMProteinSitePanel)m_panel).getSelectedProteinPTMSite();
            }
 
            if (parameterType.equals(CompareDataInterface.class)) {
                return ((GlobalTabelModelProviderInterface) m_panel).getGlobalTableModelInterface();
            }
            if (parameterType.equals(CrossSelectionInterface.class)) {
                return ((GlobalTabelModelProviderInterface)m_panel).getCrossSelectionInterface();
            }
        }
        return super.getData(getArray, parameterType);
    }
 
    @Override
    public void setEntryData(Object data) {
        
        m_panel.addSingleValue(data);
        
        m_rsm = (ResultSummary) data;
        dataChanged();
    }
    
   


}
