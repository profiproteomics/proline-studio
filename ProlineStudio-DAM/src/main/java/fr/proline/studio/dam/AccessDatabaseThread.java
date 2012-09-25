package fr.proline.studio.dam;

import fr.proline.studio.dam.tasks.AbstractDatabaseTask;
import fr.proline.repository.DatabaseConnector;
import fr.proline.studio.dam.tasks.DatabaseConnectionTask;
import java.util.HashMap;
import java.util.LinkedList;
import javax.swing.SwingUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thread in charge to read asynchronously ORM objects from the database
 * 
 * @author JM235353
 */
public class AccessDatabaseThread extends Thread {
    
    private static AccessDatabaseThread instance;

    
    private static LinkedList<AbstractDatabaseTask> actions;
    
    private AccessDatabaseThread() {
        actions = new LinkedList<AbstractDatabaseTask>();
        
        //JPM.TODO : remove it code for test
        // UDS DB properties
        HashMap<String, String> databaseProperties = new HashMap<String, String>();
        databaseProperties.put(DatabaseConnector.PROPERTY_USERNAME, "dupierris");
        databaseProperties.put(DatabaseConnector.PROPERTY_PASSWORD, "dupierris");
        databaseProperties.put(DatabaseConnector.PROPERTY_DRIVERCLASSNAME, "org.postgresql.Driver");
        databaseProperties.put(DatabaseConnector.PROPERTY_URL, "jdbc:postgresql://gre037784:5433/UDS_db");

        DatabaseConnectionTask connection = new DatabaseConnectionTask(null, databaseProperties, getProjectIdTMP());
        addTask(connection);
    }
    
    public static AccessDatabaseThread getAccessDatabaseThread() {
        if (instance == null) {
            instance = new AccessDatabaseThread();
            instance.start();
        }
        return instance;
    }
    
    @Override
    public void run() {
        try {
            while (true) {
                AbstractDatabaseTask action = null;
                synchronized(this) {
                    
                    while (true) {
                        if (!actions.isEmpty()) {
                            action = actions.removeFirst();
                            break;
                        }
                        wait();
                    }
                    notifyAll();
                }
                
                // fetch data
                boolean success = action.fetchData();
                
                // call callback code
                action.callback(success);
                
                
 
            }

            
        } catch (Throwable t) {
            LoggerFactory.getLogger(AccessDatabaseThread.class).debug("Unexpected exception in main loop of AccessDatabaseThread", t);
            instance = null; // reset thread
        }
        
    }
    
    public final void addTask(AbstractDatabaseTask action) {
        if (!action.needToFetch()) {
            action.callback(true);
            return;
        }
        synchronized(this) {
            actions.add(action);
            notifyAll();
        }
    }
    
    public static Integer getProjectIdTMP() {
        // JPM.TODO : remove this method
        return projectId;
    }
    private static Integer projectId = new Integer(1);
    
}
