package fr.proline.studio.pattern;


import fr.proline.core.orm.msi.ResultSet;
import fr.proline.core.orm.uds.Project;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseRsetTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.rsmexplorer.gui.RsetAllPanel;
import java.util.ArrayList;

/**
 *
 * @author JM235353
 */
public class DataBoxRsetAll extends AbstractDataBox {
    
    private Project m_project = null;
    
    public DataBoxRsetAll() {

        // Name of this databox
        name = "Result Sets";
        
        // Register Possible in parameters
        // One ResultSummary
        DataParameter inParameter = new DataParameter();
        inParameter.addParameter(Project.class, false);
        registerInParameter(inParameter);
        
        // Register possible out parameters
        // One or Multiple PeptideMatch
        DataParameter outParameter = new DataParameter();
        outParameter.addParameter(ResultSet.class, true);
        registerOutParameter(outParameter);

    }

    @Override
    public void createPanel() {
        RsetAllPanel p = new RsetAllPanel();
        p.setName(name);
        p.setDataBox(this);
        panel = p;
    }

    @Override
    public void dataChanged(Class dataType) {
        if (dataType == Project.class) {
            Project p = (m_project != null) ? m_project : (Project) previousDataBox.getData(false, Project.class);

            final ArrayList<ResultSet> resultSetArrayList = new ArrayList<>();
            
             AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

                @Override
                public boolean mustBeCalledInAWT() {
                    return true;
                }

                @Override
                public void run(boolean success, long taskId, SubTask subTask, boolean finished) {

                    ((RsetAllPanel) panel).setData(taskId, resultSetArrayList);

                }
            };


            // ask asynchronous loading of data
            
            DatabaseRsetTask task = new DatabaseRsetTask(callback, p.getId(), resultSetArrayList);
            AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
            
        }
    }
    
        @Override
    public Object getData(boolean getArray, Class parameterType) {
        if (parameterType!= null ) {
            if (parameterType.equals(ResultSet.class)) {
                return ((RsetAllPanel)panel).getSelectedResultSet();
            }

        }
        return super.getData(getArray, parameterType);
    }
    
    @Override
    public void setEntryData(Object data) {
        m_project = (Project) data;
        dataChanged(Project.class);
    }
}
