package fr.proline.studio.pattern;

import fr.proline.core.orm.msi.PeptideMatch;
import fr.proline.core.orm.msi.ResultSet;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseLoadPeptideMatchTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.rsmexplorer.gui.PeptideMatchPanel;

/**
 * Databox for all PSM of a ResultSet (Search Result)
 * @author JM235353
 */
public class DataBoxRsetPeptide extends AbstractDataBox {

    
    private ResultSet m_rset = null;
    
    public DataBoxRsetPeptide() {

        // Name of this databox
        m_name = "PSM";
        m_description = "All PSM of a Search Result";
        
        // Register Possible in parameters
        // One ResultSummary
        GroupParameter inParameter = new GroupParameter();
        inParameter.addParameter(ResultSet.class, false);
        registerInParameter(inParameter);
        
        // Register possible out parameters
        // One or Multiple PeptideMatch
        GroupParameter outParameter = new GroupParameter();
        outParameter.addParameter(PeptideMatch.class, true);
        registerOutParameter(outParameter);

       
    }
    

    @Override
    public void createPanel() {
        PeptideMatchPanel p = new PeptideMatchPanel(false);
        p.setName(m_name);
        p.setDataBox(this);
        m_panel = p;
    }
    
    @Override
    public void dataChanged() {
        
        final ResultSet _rset = (m_rset!=null) ? m_rset : (ResultSet) m_previousDataBox.getData(false, ResultSet.class);

        final int loadingId = setLoading();
        
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
            
            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                
               if (subTask == null) {

                    PeptideMatch[] peptideMatchArray = _rset.getTransientData().getPeptideMatches();
                    ((PeptideMatchPanel)m_panel).setData(taskId, peptideMatchArray, finished);
               } else {
                    ((PeptideMatchPanel)m_panel).dataUpdated(subTask, finished);
               }
               
               setLoaded(loadingId);
            }
        };
        

        // ask asynchronous loading of data
        DatabaseLoadPeptideMatchTask task = new DatabaseLoadPeptideMatchTask(callback, getProjectId(), _rset);
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
            if (parameterType.equals(PeptideMatch.class)) {
                return ((PeptideMatchPanel)m_panel).getSelectedPeptideMatch();
            }
            if (parameterType.equals(ResultSet.class)) {
                if (m_rset != null) {
                    return m_rset;
                }
            }
        }
        return super.getData(getArray, parameterType);
    }
 
    @Override
    public void setEntryData(Object data) {
        if (data instanceof ResultSet) {
            m_rset = (ResultSet) data;
            dataChanged();
        } else if (data instanceof ResultSummary) {
            m_rset = ((ResultSummary) data).getResultSet();
        }
    }
}
