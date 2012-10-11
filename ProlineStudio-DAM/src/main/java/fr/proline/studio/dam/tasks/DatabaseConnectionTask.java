package fr.proline.studio.dam.tasks;

import fr.proline.repository.DatabaseConnector;
import fr.proline.repository.ProlineRepository;
import fr.proline.studio.repositorymgr.ProlineDBManagement;
import java.util.Map;
import javax.persistence.EntityManager;

/**
 * Used to connect to a UDS DB or MSI DB
 *
 * @author JM235353
 */
public class DatabaseConnectionTask extends AbstractDatabaseTask {

    // used for UDS Connection
    private Map<String, String> databaseProperties;
    // used for MSI Connection
    private int projectId;

    /**
     * Constructor used for UDS database
     *
     * @param callback
     * @param databaseProperties
     * @param projectId
     */
    public DatabaseConnectionTask(AbstractDatabaseCallback callback, Map<String, String> databaseProperties, int projectId) {
        super(callback, Priority.TOP);

        this.databaseProperties = databaseProperties;
        this.projectId = projectId;


    }

    /**
     * Constructor used for MSI database
     *
     * @param callback
     * @param projectId
     */
    public DatabaseConnectionTask(AbstractDatabaseCallback callback, int projectId) {
        super(callback, Priority.TOP);

        databaseProperties = null;
        this.projectId = projectId;


    }

    @Override
    public boolean needToFetch() {
        return true; // anyway this task is used only one time for each node

    }

    @Override
    public boolean fetchData() {
        try {
            if (databaseProperties != null) {
                // UDS Connection
                DatabaseConnector udsConn = new DatabaseConnector(databaseProperties);
                ProlineDBManagement.initProlineDBManagment(udsConn);
            } else {
                // MSI Connection
                EntityManager entityManagerMSI = ProlineDBManagement.getProlineDBManagement().getProjectEntityManager(ProlineRepository.Databases.MSI, true, projectId);
                entityManagerMSI.close();
            }
        } catch (Exception e) {
            logger.error("DatabaseConnectionAction failed", e);
            return false;
        }
        return true;
    }
}
