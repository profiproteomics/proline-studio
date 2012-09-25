package fr.proline.studio.dam.tasks;


import fr.proline.repository.DatabaseConnector;
import fr.proline.studio.repositorymgr.ProlineDBManagement;
import java.util.Map;


/**
 * Used to connect to a UDS DB
 * @author JM235353
 */
public class DatabaseConnectionTask extends AbstractDatabaseTask {
    
    private Map<String, String> databaseProperties;
    private int projectId;
    
    public DatabaseConnectionTask(AbstractDatabaseCallback callback, Map<String, String> databaseProperties, int projectId) {
        super(callback);
        
        this.databaseProperties = databaseProperties;
        this.projectId = projectId;
        
    }
    
    @Override
    public boolean needToFetch() {
        return true; // anyway this task is used only one time for each node
            
    }

    @Override
    public boolean fetchData() {
        try {
            // Init UDS DB connection
            DatabaseConnector udsConn = new DatabaseConnector(databaseProperties);
            ProlineDBManagement.initProlineDBManagment(udsConn);

        } catch (Exception e) {
            logger.error("DatabaseConnectionAction failed", e);
            return false;
        }
        return true;
    }
    
}
