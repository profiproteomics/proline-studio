/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.dam;

import fr.proline.repository.DatabaseConnector;
import fr.proline.studio.dam.actions.DatabaseConnectionAction;
import java.util.HashMap;
import java.util.LinkedList;
import javax.swing.SwingUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author JM235353
 */
public class AccessDatabaseThread extends Thread {
    
    private static AccessDatabaseThread instance;

    
    private static LinkedList<DatabaseAction> actions;
    
    private AccessDatabaseThread() {
        actions = new LinkedList<DatabaseAction>();
        
        //JPM.TODO : remove it code for test
        // UDS DB properties
        HashMap<String, String> databaseProperties = new HashMap<String, String>();
        databaseProperties.put(DatabaseConnector.PROPERTY_USERNAME, "dupierris");
        databaseProperties.put(DatabaseConnector.PROPERTY_PASSWORD, "dupierris");
        databaseProperties.put(DatabaseConnector.PROPERTY_DRIVERCLASSNAME, "org.postgresql.Driver");
        databaseProperties.put(DatabaseConnector.PROPERTY_URL, "jdbc:postgresql://gre037784:5433/UDS_db");

        DatabaseConnectionAction connection = new DatabaseConnectionAction(null, databaseProperties, getProjectIdTMP());
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
                DatabaseAction action = null;
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
                
                if (action.fetchData()) {
                    //Thread.sleep(2000);
                    if (action.callbackInAWT()) {
                        final DatabaseAction _action = action;
                        SwingUtilities.invokeLater(new Runnable() {

                            @Override
                            public void run() {
                                _action.callback();
                            }
                        });
                    } else {
                        action.callback();
                    }
                }
                
            }
            
        } catch (Throwable t) {
            LoggerFactory.getLogger(AccessDatabaseThread.class).debug("Unexpected exception in main loop of AccessDatabaseThread", t);
            instance = null; // reset thread
        }
        
    }
    
    public final synchronized void addTask(DatabaseAction action) {
        actions.add(action);
        notifyAll();
    }
    
    public static Integer getProjectIdTMP() {
        // JPM.TODO : remove this methode
        return projectId;
    }
    private static Integer projectId = new Integer(1);
    
}
