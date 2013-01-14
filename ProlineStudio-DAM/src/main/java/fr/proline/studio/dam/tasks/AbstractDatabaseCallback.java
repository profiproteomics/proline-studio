package fr.proline.studio.dam.tasks;


/**
 * Class is used to execute code when an AbstractDatabaseTask is finished
 * @author JM235353
 */
public abstract class AbstractDatabaseCallback {
    
    private String errorMessage = null;
    
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
    public abstract void run(boolean success, long taskId, SubTask subTask);
    
    public String getErrorMessage() {
        return errorMessage;
    }
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
