package fr.proline.studio.pattern;

import fr.proline.core.orm.msi.ProteinMatch;
import fr.proline.core.orm.msi.ResultSet;
import fr.proline.studio.rsmexplorer.gui.RsetProteinsPanel;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseProteinMatchesTask;
import fr.proline.studio.dam.tasks.SubTask;

/**
 * All Protein Matches of a Search Result
 * @author JM235353
 */
public class DataBoxRsetAllProteinMatch extends AbstractDataBox {

    private ResultSet m_rset;
    
    public DataBoxRsetAllProteinMatch() {
        
        // Name of this databox
        name = "Protein";
        description = "All Proteins of a Search Result";
        
        // Register Possible in parameters
        // One PeptideMatch
        GroupParameter inParameter = new GroupParameter();
        inParameter.addParameter(ResultSet.class, false);
        registerInParameter(inParameter);
        
        // Register possible out parameters
        // One or Multiple ProteinMatch
        GroupParameter outParameter = new GroupParameter();
        outParameter.addParameter(ProteinMatch.class, true);
        registerOutParameter(outParameter);
    }
    
    @Override
    public void createPanel() {
        RsetProteinsPanel p = new RsetProteinsPanel();
        p.setName(name);
        p.setDataBox(this);
        m_panel = p;
    }

    @Override
    public void dataChanged() {

        final ResultSet _rset = (m_rset != null) ? m_rset : (ResultSet) previousDataBox.getData(false, ResultSet.class);

        final int loadingId = setLoading();

        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {

                if (subTask == null) {

                    ProteinMatch[] proteinMatchArray = _rset.getTransientData().getProteinMatches();
                    ((RsetProteinsPanel) m_panel).setDataProteinMatchArray(proteinMatchArray);
                    //((RsetProteinsPanel) m_panel).setData(taskId, proteinMatchArray, finished);
                } else {
                    ((RsetProteinsPanel) m_panel).dataUpdated(subTask, finished);
                }

                setLoaded(loadingId);
            }
        };


        // ask asynchronous loading of data

        DatabaseProteinMatchesTask task = new DatabaseProteinMatchesTask(callback,getProjectId(), _rset);
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
            if (parameterType.equals(ProteinMatch.class)) {
                return ((RsetProteinsPanel)m_panel).getSelectedProteinMatch();
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
        m_rset = (ResultSet) data;
        dataChanged();
    }
    
}
