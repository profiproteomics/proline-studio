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
 * @author JM235353
 */
public class DataBoxRsmAllProteinSet extends AbstractDataBox {
    
    private ResultSummary m_rsm = null;

    
    public DataBoxRsmAllProteinSet() { 

         // Name of this databox
        m_name = "Protein Set";
        m_description = "All Protein Sets of an Identification Summary";

        // Register Possible in parameters
        // One ResultSummary
        GroupParameter inParameter = new GroupParameter();
        inParameter.addParameter(ResultSummary.class, false);
        registerInParameter(inParameter);


        // Register possible out parameters
        // One or Multiple ProteinSet
        GroupParameter outParameter = new GroupParameter();
        outParameter.addParameter(ProteinSet.class, true);
        outParameter.addParameter(ResultSummary.class, false);
        registerOutParameter(outParameter);

    }


    @Override
    public void createPanel() {
        RsmProteinSetPanel p = new RsmProteinSetPanel(true);
        p.setName(m_name);
        p.setDataBox(this);
        m_panel = p;
    }
    
    @Override
    public void dataChanged() {


        final ResultSummary _rsm = (m_rsm != null) ? m_rsm : (ResultSummary) m_previousDataBox.getData(false, ResultSummary.class);

        final int loadingId = setLoading();

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

                setLoaded(loadingId);
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
        dataChanged();
    }
}
