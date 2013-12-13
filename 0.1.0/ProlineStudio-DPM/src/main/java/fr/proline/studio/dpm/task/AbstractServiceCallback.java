package fr.proline.studio.dpm.task;

import fr.proline.studio.dam.taskinfo.TaskInfo;

/**
 * Superclass for all Callback which are called when a service is done
 * @author jm235353
 */
public abstract class AbstractServiceCallback {
    
    private String errorMessage = null;
    private TaskInfo taskInfo = null;
     
     
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
    
    public String getErrorMessage() {
        return errorMessage;
    }
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }   
    
    public TaskInfo getTaskInfo() {
        return taskInfo;
    }
    
    public void setTaskInfo(TaskInfo taskInfo) {
        this.taskInfo = taskInfo;
    }
}
