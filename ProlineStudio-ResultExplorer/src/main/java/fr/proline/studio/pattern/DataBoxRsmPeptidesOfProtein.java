package fr.proline.studio.pattern;


import fr.proline.core.orm.msi.ResultSummary;
import fr.proline.core.orm.msi.dto.DPeptideInstance;
import fr.proline.core.orm.msi.dto.DPeptideMatch;
import fr.proline.core.orm.msi.dto.DProteinMatch;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseLoadPeptidesInstancesTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.rsmexplorer.gui.RsmPeptidesOfProteinPanel;
import java.util.ArrayList;

/**
 * Databox : Peptides of a Protein in a Rsm
 * @author JM235353
 */
public class DataBoxRsmPeptidesOfProtein extends AbstractDataBox {
    

    
    public DataBoxRsmPeptidesOfProtein() {
        super(DataboxType.DataBoxRsmPeptidesOfProtein);

        // Name of this databox
        m_name = "Peptides";
        m_description = "All Peptides of a Protein Match";
        
        // Register Possible in parameters
        // One ProteinMatch AND one ResultSummary
        GroupParameter inParameter = new GroupParameter();
        inParameter.addParameter(DProteinMatch.class, false);
        inParameter.addParameter(ResultSummary.class, false);
        registerInParameter(inParameter);
        
        // Register possible out parameters
        // One or Multiple  PeptideInstance
        GroupParameter outParameter = new GroupParameter();
        outParameter.addParameter(DPeptideInstance.class, true);
        registerOutParameter(outParameter);
        
        outParameter = new GroupParameter();
        outParameter.addParameter(DPeptideMatch.class, true);
        registerOutParameter(outParameter);
        
       
    }
    

     
     @Override
    public void createPanel() {
        RsmPeptidesOfProteinPanel p = new RsmPeptidesOfProteinPanel();
        p.setName(m_name);
        p.setDataBox(this);
        m_panel = p;
    }
    

    @Override
    public void dataChanged() {
        final DProteinMatch proteinMatch = (DProteinMatch) m_previousDataBox.getData(false, DProteinMatch.class);
        final ResultSummary rsm = (ResultSummary) m_previousDataBox.getData(false, ResultSummary.class);

        if (proteinMatch == null) {
            ((RsmPeptidesOfProteinPanel) m_panel).setData(null, null);
            return;
        }

        final int loadingId = setLoading();
        
        // prepare callback to view new data
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {

                if (success) {
                    ((RsmPeptidesOfProteinPanel) m_panel).setData(proteinMatch, rsm);
                } else {
                    ((RsmPeptidesOfProteinPanel) m_panel).setData(null, null);
                }
                
                setLoaded(loadingId);
                
                if (finished) {
                    unregisterTask(taskId);
                }
            }
        };

        // Load data if needed asynchronously
        ArrayList<ResultSummary> rsmList = new ArrayList<>(1);
        rsmList.add(rsm);
        DatabaseLoadPeptidesInstancesTask task = new DatabaseLoadPeptidesInstancesTask(callback, getProjectId(), proteinMatch, rsmList);
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
        if (parameterType!= null && (parameterType.equals(DPeptideInstance.class))) {
            return ((RsmPeptidesOfProteinPanel)m_panel).getSelectedPeptide();
        } else if (parameterType!= null && (parameterType.equals(DPeptideMatch.class))) {
            DPeptideInstance pi = ((RsmPeptidesOfProteinPanel)m_panel).getSelectedPeptide();
            if (pi != null) {
                return pi.getBestPeptideMatch();
            }
        }
        return super.getData(getArray, parameterType);
    }

}
