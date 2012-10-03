package fr.proline.studio.dam.tasks;

import javax.swing.SwingUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to be extended to access to the database through the
 * AccessDatabaseThread
 *
 * @author JM235353
 */
public abstract class AbstractDatabaseTask implements Comparable<AbstractDatabaseTask> {

    public enum Priority {
        LOW   ,  // for batch actions
        NORMAL,  // all actions
        HIGH     // action needed as soon as possible 
    };
    
    protected static final Logger logger = LoggerFactory.getLogger(AbstractDatabaseTask.class);
    
    // callback is called by the AccessDatabaseThread when the data is fetched
    protected AbstractDatabaseCallback callback;

    // priority
    protected Priority priority; 
    
    // id of the action
    protected long id;
    
    private static long idIncrement = 0;
    
    /**
     * Contructor
     *
     * @param callback called by the AccessDatabaseThread when the data is
     * fetched
     */
    public AbstractDatabaseTask(AbstractDatabaseCallback callback) {
        this(callback,Priority.NORMAL);
    }
    public AbstractDatabaseTask(AbstractDatabaseCallback callback, Priority priority) {
        this.callback = callback;
        this.priority = priority; 
        id = idIncrement++;
    }

    /**
     * Method called by the AccessDatabaseThread to fetch Data from database
     * @return 
     */
    public abstract boolean fetchData();

    /**
     * Method called by the AccessDatabaseThread to check if data is or not
     * already known
     * @return 
     */
    public abstract boolean needToFetch();
    
    
    /**
     * Method called after the data has been fetched
     * @param success  boolean indicating if the fetch has succeeded
     */
    public final void callback(final boolean success) {
        if (callback == null) {
            return;
        }

        if (callback.mustBeCalledInAWT()) {
            // Callback must be executed in the Graphical thread (AWT)
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    callback.run(success);
                }
            });
        } else {
            // Method called in the current thread
            // In this case, we assume the execution is fast.
            callback.run(success);
        }


    }
    
    /**
     * Used to prioritize actions
     * @param task
     * @return 
     */
    @Override
    public int compareTo(AbstractDatabaseTask task) {
        
        // first we compare on priority
        long diff = priority.ordinal()-task.priority.ordinal();
        if (diff != 0) {
            return (diff)>0 ? 1 : -1 ;
        }
        
        // for equal priority, we compare on id
        diff = id-task.id;
        if (diff == 0) {
            return 0;
        }
        return (diff)>0 ? 1 : -1 ;
    }
}
