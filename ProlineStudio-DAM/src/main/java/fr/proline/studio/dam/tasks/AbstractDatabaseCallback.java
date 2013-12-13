package fr.proline.studio.dam.tasks;

import fr.proline.studio.dam.taskinfo.TaskError;


/**
 * Class is used to execute code when an AbstractDatabaseTask is finished
 * @author JM235353
 */
public abstract class AbstractDatabaseCallback {
    
    private TaskError m_taskError = null;
    private int m_errorId = -1;
    
    /**
     * Returns if the callback must be called 
     * in the Graphical Thread (AWT)
     * @return 
     */
    public abstract boolean mustBeCalledInAWT();
    
    /**
     * Method called by the AccessDatabaseThread when the data is fetched
     * 
     * @param success   indicates if the loading of data has been a success or not
     * @param taskId    id of the task which has been executed
     * @param slice     if subTask is not null : only a subpart of the task has been done
     */
    public abstract void run(boolean success, long taskId, SubTask subTask, boolean finished);
    
    public TaskError getTaskError() {
        return m_taskError;
    }
    public int getErrorId() {
        return m_errorId;
    }
    public void setErrorMessage(TaskError taskError, int errorId) {
        m_taskError = taskError;
        m_errorId = errorId;
    }
}
