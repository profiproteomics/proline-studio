package fr.proline.studio.pattern;


import fr.proline.core.orm.msi.PeptideInstance;
import fr.proline.core.orm.msi.ProteinSet;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseProteinSetsTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.rsmexplorer.gui.RsmProteinSetPanel;

/**
 * Databox for the list of Protein Sets of a Rsm
 * OR for the list of Protein Sets corresponding to a Peptide Instance
 * @author JM235353
 */
public class DataBoxRsmProteinSet extends AbstractDataBox {
    
    private ResultSummary m_rsm = null;
    private boolean m_forRSM;
    
    
    public DataBoxRsmProteinSet(boolean forRSM) {

        m_forRSM = forRSM;
       
         // Name of this databox
        name = "Protein Set";
        
        // Register Possible in parameters
        // One ResultSummary
        DataParameter inParameter = new DataParameter();
        inParameter.addParameter(ResultSummary.class, false);
        registerInParameter(inParameter);
        
        // or one PeptideInstance
        inParameter = new DataParameter();
        inParameter.addParameter(PeptideInstance.class, false);
        registerInParameter(inParameter);
        
   
        // Register possible out parameters
        // One or Multiple ProteinSet
        DataParameter outParameter = new DataParameter();
        outParameter.addParameter(ProteinSet.class, true);
        outParameter.addParameter(ResultSummary.class, false);
        registerOutParameter(outParameter);

       
    }
    

    @Override
    public void createPanel() {
        RsmProteinSetPanel p = new RsmProteinSetPanel(m_forRSM);
        p.setName(name);
        p.setDataBox(this);
        m_panel = p;
    }
    
    @Override
    public void dataChanged(Class dataType) {
        
        if (dataType == PeptideInstance.class) {

            final PeptideInstance _peptideInstance = (PeptideInstance) previousDataBox.getData(false, PeptideInstance.class);

            
            if (_peptideInstance == null) {
                ((RsmProteinSetPanel) m_panel).setData(null, null, true);
                return;
            }
            
            
            AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

                @Override
                public boolean mustBeCalledInAWT() {
                    return true;
                }

                @Override
                public void run(boolean success, long taskId, SubTask subTask, boolean finished) {

                    if (subTask == null) {

                        ProteinSet[] proteinSetArray = _peptideInstance.getTransientData().getProteinSetArray();
                        ((RsmProteinSetPanel) m_panel).setData(taskId, proteinSetArray, finished);
                    } else {
                        ((RsmProteinSetPanel) m_panel).dataUpdated(subTask, finished);
                    }
                }
            };


            // ask asynchronous loading of data
            DatabaseProteinSetsTask task = new DatabaseProteinSetsTask(callback);
            task.initLoadProteinSetForPeptideInstance(getProjectId(), _peptideInstance);
            Long taskId = task.getId();
            if (m_previousTaskId != null) {
                // old task is suppressed if it has not been already done
                AccessDatabaseThread.getAccessDatabaseThread().removeTask(m_previousTaskId);
            }
            m_previousTaskId = taskId;
            AccessDatabaseThread.getAccessDatabaseThread().addTask(task);

        } else {
        
            final ResultSummary _rsm = (m_rsm != null) ? m_rsm : (ResultSummary) previousDataBox.getData(false, ResultSummary.class);


            AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

                @Override
                public boolean mustBeCalledInAWT() {
                    return true;
                }

                @Override
                public void run(boolean success, long taskId, SubTask subTask, boolean finished) {



                    if (subTask == null) {

                        ProteinSet[] proteinSetArray = _rsm.getTransientData().getProteinSetArray();
                        ((RsmProteinSetPanel) m_panel).setData(taskId, proteinSetArray, finished);
                    } else {
                        ((RsmProteinSetPanel) m_panel).dataUpdated(subTask, finished);
                    }
                }
            };


            // ask asynchronous loading of data
            
            DatabaseProteinSetsTask task = new DatabaseProteinSetsTask(callback);
            task.initLoadProteinSets(getProjectId(), _rsm);
            Long taskId = task.getId();
            if (m_previousTaskId != null) {
                // old task is suppressed if it has not been already done
                AccessDatabaseThread.getAccessDatabaseThread().removeTask(m_previousTaskId);
            }
            m_previousTaskId = taskId;
            AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
        }
    }
    private Long m_previousTaskId = null;
    
    
    
    @Override
    public Object getData(boolean getArray, Class parameterType) {
        if (parameterType!= null ) {
            if (parameterType.equals(ProteinSet.class)) {
                return ((RsmProteinSetPanel)m_panel).getSelectedProteinSet();
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
        m_rsm = (ResultSummary) data;
        dataChanged(ResultSummary.class);
    }
}
