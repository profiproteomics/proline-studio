package fr.proline.studio.pattern;



import fr.proline.core.orm.msi.PeptideMatch;
import fr.proline.core.orm.msi.PeptideInstance;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseLoadPeptidesInstancesTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.rsmexplorer.gui.RsmPeptidesPanel;



/**
 * 
 * @author JM235353
 */
public class DataBoxRsmPeptideInstances extends AbstractDataBox {

    
    ResultSummary m_rsm = null;
    
    public DataBoxRsmPeptideInstances() {

        // Name of this databox
        name = "Peptides";
        
        // Register Possible in parameters
        // One ResultSummary
        DataParameter inParameter = new DataParameter();
        inParameter.addParameter(ResultSummary.class, false);
        registerInParameter(inParameter);
        
        // Register possible out parameters
        // One or Multiple PeptideMatch
        DataParameter outParameter = new DataParameter();
        outParameter.addParameter(PeptideMatch.class, true);
        registerInParameter(outParameter);
        
        outParameter = new DataParameter();
        outParameter.addParameter(PeptideInstance.class, true);
        registerInParameter(outParameter);
       
    }
    

    @Override
    public void createPanel() {
        RsmPeptidesPanel p = new RsmPeptidesPanel();
        p.setName(name);
        p.setDataBox(this);
        m_panel = p;
    }
    
    @Override
    public void dataChanged(Class dataType) {
        
        final ResultSummary _rsm = (m_rsm!=null) ? m_rsm : (ResultSummary) previousDataBox.getData(false, ResultSummary.class);

        final int loadingId = setLoading();
        
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
            
            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                
               if (subTask == null) {

                    PeptideInstance[] peptideinstanceArray = _rsm.getTransientData().getPeptideInstanceArray();
                    ((RsmPeptidesPanel)m_panel).setData(taskId, peptideinstanceArray, finished);
               } else {
                    ((RsmPeptidesPanel)m_panel).dataUpdated(subTask, finished);
               }
               
               setLoaded(loadingId);
            }
        };
        

        // ask asynchronous loading of data
        DatabaseLoadPeptidesInstancesTask task = new DatabaseLoadPeptidesInstancesTask(callback, getProjectId(), _rsm);
        Long taskId = task.getId();
        if (m_previousTaskId != null) {
            // old task is suppressed if it has not been already done
            AccessDatabaseThread.getAccessDatabaseThread().removeTask(m_previousTaskId);
        }
        m_previousTaskId = taskId;
        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);

       
        
    }
    private Long m_previousTaskId = null;
    
    @Override
    public Object getData(boolean getArray, Class parameterType) {
        if (parameterType!= null ) {
            if (parameterType.equals(PeptideInstance.class)) {
                return ((RsmPeptidesPanel)m_panel).getSelectedPeptideInstance();
            }
            if (parameterType.equals(PeptideMatch.class)) {
                PeptideInstance pi = ((RsmPeptidesPanel)m_panel).getSelectedPeptideInstance();
                if (pi == null) {
                    return null;
                }
                return pi.getTransientData().getBestPeptideMatch();
            }
            if (parameterType.equals(ResultSummary.class)) {
                if (m_rsm != null) {
                    return m_rsm;
                }
            }
        }
        return super.getData(getArray, parameterType);
    }
 
    @Override
    public void setEntryData(Object data) {
        if (data instanceof ResultSummary) {
            m_rsm = (ResultSummary) data;
            dataChanged(ResultSummary.class);
        }
    }
}
