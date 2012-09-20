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
public abstract class AbstractDatabaseTask {

    protected static final Logger logger = LoggerFactory.getLogger(AbstractDatabaseTask.class);
    // callback is called by the AccessDatabaseThread when the data is fetched
    protected AbstractDatabaseCallback callback;

    /**
     * Contructor
     *
     * @param callback called by the AccessDatabaseThread when the data is
     * fetched
     */
    public AbstractDatabaseTask(AbstractDatabaseCallback callback) {
        this.callback = callback;
    }

    /**
     * Method called by the AccessDatabaseThread to fetch Data from database
     * @return 
     */
    public abstract boolean fetchData();

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
}
