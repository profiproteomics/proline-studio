package fr.proline.studio.pattern;

import fr.proline.core.orm.msi.dto.DPeptideInstance;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabasePTMsTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.dam.tasks.data.ptm.PTMSite;
import fr.proline.studio.rsmexplorer.gui.PeptidesPTMSiteTablePanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import fr.proline.studio.extendedtablemodel.ExtendedTableModelInterface;

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
        PeptidesPTMSiteTablePanel p = new PeptidesPTMSiteTablePanel(true);
        p.setName(m_typeName);
        p.setDataBox(this);
        setDataBoxPanelInterface(p);        
    }

    @Override
    public void dataChanged() {

        final PTMSite ptmSite = (PTMSite) m_previousDataBox.getData(false, PTMSite.class); 
        final DPeptideInstance pepInstance = (DPeptideInstance) m_previousDataBox.getData(false, DPeptideInstance.class); 
       
        m_parentPeptideInstance = pepInstance;
        
        if ((ptmSite == null) || (pepInstance == null)) {
            ((PeptidesPTMSiteTablePanel)getDataBoxPanelInterface()).setData(null, null);           
            return;
        }
        
        //Data already loaded. Just update panel.
         if(ptmSite.isLoaded()){
            m_previousTaskId = null;
            ((PeptidesPTMSiteTablePanel) getDataBoxPanelInterface()).setData(ptmSite, m_parentPeptideInstance);           
            propagateDataChanged(ExtendedTableModelInterface.class);
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
                if (success) {
                    ((PeptidesPTMSiteTablePanel) getDataBoxPanelInterface()).setData(ptmSite, pepInstance);
                } else {
                    ((PeptidesPTMSiteTablePanel) getDataBoxPanelInterface()).setData(null, null);
                }

                setLoaded(loadingId);
                
                if (finished) {
                    unregisterTask(taskId);
                    propagateDataChanged(ExtendedTableModelInterface.class);
                }
            }
        };
        
         if (m_previousTaskId != null) {
            // old task is suppressed if it has not been already done
            AccessDatabaseThread.getAccessDatabaseThread().abortTask(m_previousTaskId);
        }
         
        DatabasePTMsTask task = new DatabasePTMsTask(callback);
        task.initFillPTMSite(getProjectId(), ptmSite.getPTMdataset().getDataset().getResultSummary(), ptmSite);
        Long taskId = task.getId();
        m_previousTaskId = taskId;
        registerTask(task);            
                             
    }
    
    private Long m_previousTaskId = null;
    
    @Override
    public Object getData(boolean getArray, Class parameterType) {
        if (parameterType != null) {

            if (parameterType.equals(PTMSite.class)) {
                PTMSite ptmSite = ((PeptidesPTMSiteTablePanel) getDataBoxPanelInterface()).getSelectedPTMSite();
                if (ptmSite != null)
                    return ptmSite;
            }
                        
            if (parameterType.equals(DPeptideMatch.class)) {
                DPeptideMatch selectedPeptideMatch = ((PeptidesPTMSiteTablePanel) getDataBoxPanelInterface()).getSelectedPeptideMatchSite();
                if (selectedPeptideMatch != null)
                    return selectedPeptideMatch;
            }
            
            if (parameterType.equals(MsQueryInfoRset.class)) {
                DPeptideMatch pepMatch = ((PeptidesPTMSiteTablePanel) getDataBoxPanelInterface()).getSelectedPeptideMatchSite();
                DPeptideInstance pepInstance = ((PeptidesPTMSiteTablePanel) getDataBoxPanelInterface()).getSelectedPeptideInstance();
                if (pepMatch != null && pepInstance != null) {
                    return new MsQueryInfoRset(pepMatch.getMsQuery(), pepInstance.getResultSummary().getResultSet());
                }
            }
        }
        
        return super.getData(getArray, parameterType);
    }
    
}
