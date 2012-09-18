/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.dam.actions;

import fr.proline.core.orm.uds.Identification;
import fr.proline.core.orm.uds.Project;
import fr.proline.core.orm.uds.repository.IdentificationRepository;
import fr.proline.repository.DatabaseConnector;
import fr.proline.repository.ProlineRepository;
import fr.proline.studio.dam.DatabaseAction;
import fr.proline.studio.dam.DatabaseCallback;
import fr.proline.studio.repositorymgr.ProlineDBManagement;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;

/**
 *
 * @author JM235353
 */
public class DatabaseConnectionAction extends DatabaseAction {
    
    private Map<String, String> databaseProperties;
    private int projectId;
    
    public DatabaseConnectionAction(DatabaseCallback callback, Map<String, String> databaseProperties, int projectId) {
        super(callback);
        
        this.databaseProperties = databaseProperties;
        this.projectId = projectId;
        
    }
    
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
