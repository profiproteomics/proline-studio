package fr.proline.studio.dam.tasks;


/**
 * Class is used to execute code when an AbstractDatabaseTask is finished
 * @author JM235353
 */
public abstract class AbstractDatabaseCallback {
    
    //protected HashMap<Class,Object> results = null;
    
    /*public void addResult(Class key, Object result) {
        if (results == null) {
            results = new HashMap<Class, Object>();
        }
        results.put(key, result);
    }*/
    
    /**
     * Returns if the callback must be called 
     * in the Graphical Thread (AWT)
     * @return 
     */
    public abstract boolean mustBeCalledInAWT();
    
    /**
     * Method called by the AccessDatabaseThread when the data is fetched
     * @param success   indicates if the loading of data has been a success or not
     */
    public abstract void run(boolean success);
    
}
