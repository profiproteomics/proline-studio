package fr.proline.studio.dpm.task.jms;

import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;

/**
 *
 * @author JM235353
 */
public abstract class AbstractJMSCallback {
    
    private TaskError m_taskError = null;
    private TaskInfo m_taskInfo = null;
     
     
    /**
     * Returns if the callback must be called 
     * in the Graphical Thread (AWT)
     * @return 
     */
    public abstract boolean mustBeCalledInAWT();
    
    /**
     * Method called by the AccessServiceThread when the data is fetched
     * 
     * @param success   indicates if the loading of data has been a success or not
     * @param taskId    id of the task which has been executed
     * @param slice     if subTask is not null : only a subpart of the task has been done
     */
    public abstract void run(boolean success);
    
    public TaskError getTaskError() {
        return m_taskError;
    }
    public void setTaskError(TaskError taskError) {
        m_taskError = taskError;
    }   
    
    public TaskInfo getTaskInfo() {
        return m_taskInfo;
    }
    
    public void setTaskInfo(TaskInfo taskInfo) {
        m_taskInfo = taskInfo;
    }
}
