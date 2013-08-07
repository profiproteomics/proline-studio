package fr.proline.studio.pattern;

import fr.proline.core.orm.msi.PeptideMatch;
import fr.proline.core.orm.msi.ProteinMatch;
import fr.proline.core.orm.msi.ResultSet;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseLoadPeptideMatchTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.rsmexplorer.gui.PeptideMatchPanel;

/**
 * Databox : Peptides of a Protein in a Rset
 * @author JM235353
 */
public class DataboxRsetPeptidesOfProtein extends AbstractDataBox {

    public DataboxRsetPeptidesOfProtein() {

        // Name of this databox
        m_name = "Peptides";
        m_description = "All Peptides of a Protein Match";

        // Register Possible in parameters
        // One ProteinMatch AND one ResultSet
        GroupParameter inParameter = new GroupParameter();
        inParameter.addParameter(ProteinMatch.class, false);
        inParameter.addParameter(ResultSet.class, false);
        registerInParameter(inParameter);

        // Register possible out parameters
        // One or Multiple  PeptideInstance
        GroupParameter outParameter = new GroupParameter();
        outParameter.addParameter(PeptideMatch.class, true);
        registerOutParameter(outParameter);


    }
    
    @Override
    public void createPanel() {
        PeptideMatchPanel p = new PeptideMatchPanel(false, false);
        p.setName(m_name);
        p.setDataBox(this);
        m_panel = p;
    }

    @Override
    public void dataChanged() {
        

        final ProteinMatch proteinMatch = (ProteinMatch) m_previousDataBox.getData(false, ProteinMatch.class);
        final ResultSet rset = (ResultSet) m_previousDataBox.getData(false, ResultSet.class);

        if (proteinMatch == null) {
            ((PeptideMatchPanel) m_panel).setData(-1, null, true);
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

               if (subTask == null) {

                    PeptideMatch[] peptideMatchArray = proteinMatch.getTransientData().getPeptideMatches();
                    ((PeptideMatchPanel)m_panel).setData(taskId, peptideMatchArray, finished);
               } else {
                    ((PeptideMatchPanel)m_panel).dataUpdated(subTask, finished);
               }
                
                setLoaded(loadingId);
            }
        };

        // ask asynchronous loading of data
        DatabaseLoadPeptideMatchTask task = new DatabaseLoadPeptideMatchTask(callback, getProjectId(), rset, proteinMatch);
        Long taskId = task.getId();
        if (m_previousTaskId != null) {
            // old task is suppressed if it has not been already done
            AccessDatabaseThread.getAccessDatabaseThread().removeTask(m_previousTaskId);
        }
        m_previousTaskId = taskId;
        registerTask(task);

    }
    private Long m_previousTaskId = null;
    
}
