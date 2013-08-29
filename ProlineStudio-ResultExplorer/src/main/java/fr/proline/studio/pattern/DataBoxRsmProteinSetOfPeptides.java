package fr.proline.studio.pattern;


import fr.proline.core.orm.msi.PeptideInstance;
import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.msi.dto.DProteinSet;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseProteinSetsTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.rsmexplorer.gui.RsmProteinSetPanel;
import java.util.Arrays;

/**
 * Databox for the list of Protein Sets corresponding to a Peptide Instance
 * @author JM235353
 */
public class DataBoxRsmProteinSetOfPeptides extends AbstractDataBox {
    
    private ResultSummary m_rsm = null;

    
    
    public DataBoxRsmProteinSetOfPeptides() { 

         // Name of this databox
        m_name = "Protein Set";
        m_description = "All Protein Sets coresponding to a Peptide Instance";

        // Register Possible in parameters
        // One ResultSummary

        // or one PeptideInstance
        GroupParameter inParameter = new GroupParameter();
        inParameter.addParameter(PeptideInstance.class, false);
        registerInParameter(inParameter);


        // Register possible out parameters
        // One or Multiple ProteinSet
        GroupParameter outParameter = new GroupParameter();
        outParameter.addParameter(DProteinSet.class, true);
        outParameter.addParameter(ResultSummary.class, false);
        registerOutParameter(outParameter);

    }


    @Override
    public void createPanel() {
        RsmProteinSetPanel p = new RsmProteinSetPanel(false);
        p.setName(m_name);
        p.setDataBox(this);
        m_panel = p;
    }
    
    @Override
    public void dataChanged() {

        final PeptideInstance _peptideInstance = (PeptideInstance) m_previousDataBox.getData(false, PeptideInstance.class);


        if (_peptideInstance == null) {
            ((RsmProteinSetPanel) m_panel).setData(null, null, true);
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

                if (subTask == null) {

                    DProteinSet[] proteinSetArray = _peptideInstance.getTransientData().getDProteinSetArray();
                    
                    ((RsmProteinSetPanel) m_panel).setData(taskId, proteinSetArray, finished);
                } else {
                    ((RsmProteinSetPanel) m_panel).dataUpdated(subTask, finished);
                }

                setLoaded(loadingId);
            }
        };


        // ask asynchronous loading of data
        DatabaseProteinSetsTask task = new DatabaseProteinSetsTask(callback);
        task.initLoadProteinSetForPeptideInstance(getProjectId(), _peptideInstance);
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
            if (parameterType.equals(DProteinSet.class)) {
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
