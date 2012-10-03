package fr.proline.studio.dam.data;

import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * Must be extended by classes used as User Data in Result Explorer Nodes
 * @author JM235353
 */
public abstract class AbstractData {

    public enum DataTypes {
        MAIN,
        PROJECT,
        IDENTIFICATION,
        IDENTIFICATION_FRACTION,
        RESULT_SET,
        RESULT_SUMMARY}
    
    protected String name;
    protected DataTypes dataType;
    
    // by default, any data can have children when corresponding node is expanded
    protected boolean hasChildren = true; 

    /**
     * Returns if the corresponding Node will have Children when it will be expanded
     * @return 
     */
    public boolean hasChildren() {
        return hasChildren;
    }
    

    /**
     * This method is called from a thread which is not the AWT and can wait
     * for the answer. So we use a Semaphore to synchronize and wait the result
     * from the AccessDatabaseThread
     * @param list 
     */
    public final void load(List<AbstractData> list) {

        // Semaphore used to wait the result
        final Semaphore waitDataSemaphore = new Semaphore(0, true);


        // Callback used only for the synchronization with the AccessDatabaseThread
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return false;
            }
            
            @Override
            public void run(boolean success) {
                // data is ready
                waitDataSemaphore.release();
            }
        };

        // Call the loading of each subclass
        loadImpl(callback, list);


        // Wait for the end of task
        try {
            waitDataSemaphore.acquire();
        } catch (InterruptedException e) {
            // should not happen
            //JPM.TODO
        }
        // now data is loaded
    }

    /**
     * The subclass must override this method to create the Action to load its data.
     * @param callback
     * @param list 
     */
    protected abstract void loadImpl(AbstractDatabaseCallback callback, List<AbstractData> list);

    /**
     * Subclasses must overriden this method to give the name of the data
     * @return 
     */
    public abstract String getName();

    /*public void setName(String name) {
        //JPM.TODO
    }*/

    public DataTypes getDataType() {
        return dataType;
    }
}
