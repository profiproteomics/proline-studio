/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.dbs;

import fr.proline.core.dal.DatabaseManagment;
import fr.proline.core.orm.utils.JPAUtil;
import fr.proline.repository.DatabaseConnector;
import fr.proline.repository.ProlineRepository;
import static fr.proline.studio.dbs.Bundle.*;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author VD225637
 */
@Messages({"getEMDB.error=Specified Database, {0}, is not unique. You must use appropriate method specifying project.",
            "unknown.db.error=Specified Database is of unknown type.",
            "not.singleton.db.error=Specified Database is not a singleton Database.",
            "noneInitDBManagment.error=ProlineDbManagment instance has not been initialized yet ! Call intiProlineDbManagment(DatabaseConnector) first."
        })
public class ProlineDbManagment {

    protected static ProlineDbManagment instance = null;
    
    protected DatabaseManagment dbMgmt;
    protected EntityManager udsEM;
    protected EntityManager pdiEM;
    protected EntityManager psEM;
    
    private ProlineDbManagment(DatabaseConnector udsConnector) {
        dbMgmt = new DatabaseManagment(udsConnector);        
    }

    /**
     * initialize unique ProlineDbManagment instance. Specified UDS DatabaseConnector
     * is used to create associated DatabaseManagment. 
     * If ProlineDbManagment singleton instance has already been initialized false is returned
     * Use getProlineDbManagment() to get initialized instance. 
     * 
     * @param udsConnector DatabaseConnector to access UDSdb
     * @return true if initialization is successful false if ProlineDbManagment already initialized
     */
    public static boolean initProlineDbManagment(DatabaseConnector udsConnector) {
        if(instance != null)
            return false;
        
        instance = new ProlineDbManagment(udsConnector);                   
        return true;
    }
    
    /**
     * Get the unique ProlineDbManagment instance. If instance has not be initialized an exception
     * is thrown
     * @see #initProlineDbManagment(fr.proline.repository.DatabaseConnector)  
     * @return the ProlineDbManagment instance
     * @throws UnsupportedOperationException if instance has not been initialized yet
     */
    public static ProlineDbManagment getProlineDbManagment() {
        if(instance == null)
            throw new UnsupportedOperationException(noneInitDBManagment_error());
        return instance;
    }
    
    /**
     * return true if ProlineDbManagment have been initialized, false otherwise
     * @return 
     */
    public static boolean isInitilized(){ 
        return instance != null;
    }     
    
    /**
     * Return the DatabaseConnector for specified unique Database. If specified database
     * is project dependant, an error is thrown
     * @param db the singleton Database to get DatabaseConnector for 
     * @return the DatabaseConnector for specified Database
     * @exception if specified database is not a singleton database.
     */
    public DatabaseConnector getDatabaseConnector(ProlineRepository.Databases db) throws IllegalArgumentException{
        switch(db){
            case PS: return dbMgmt.psDBConnector();
            case PDI : return dbMgmt.pdiDBConnector();
            case UDS : return dbMgmt.udsDBConnector();
            case LCMS : 
            case MSI : 
                throw new IllegalArgumentException(not_singleton_db_error());
        }
        throw new IllegalArgumentException(unknown_db_error());    
    }
    
    /**
     * Close all connections to databases associated to this manager
     * 
     * @throws Exception 
     */
    public void closeAll() throws Exception{
       dbMgmt.closeAll();
    }
    
    
    /**
     * 
     * Get an EntityManager for the specified Database. Id newEM is true, then a new EntityManager will be created.
     * Otherwise, existing EntityManager will be return, if it don't exist a new one will be created and returned.
     * 
     * @param db : database to get an EntityManager for
     * @param newEM : specify of EntityManager should be created (even if one exist)
     * @return a EntityManager for specified Database
     */    
    public EntityManager getEntityManager(ProlineRepository.Databases db, boolean newEM){
        switch(db){
            case UDS:
                if(newEM || udsEM == null || !udsEM.isOpen())
                     udsEM = createEM(db);
                return udsEM;
            case LCMS:
                throw new IllegalArgumentException(getEMDB_error(ProlineRepository.Databases.LCMS.name()));
            case PDI:
                 if(newEM || pdiEM == null || !pdiEM.isOpen())
                     pdiEM = createEM(db);
                return pdiEM;
            case PS:
                 if(newEM || psEM == null || !psEM.isOpen())
                     psEM = createEM(db);
                return psEM;
            case MSI:
                throw new IllegalArgumentException(getEMDB_error(ProlineRepository.Databases.MSI.name()));
            default :
                throw new IllegalArgumentException(unknown_db_error());
        }  
    }
    
    private EntityManager createEM(ProlineRepository.Databases db){
        
        final Map<String, Object> entityManagerSettings = getDatabaseConnector(db).getEntityManagerSettings();
        String persistenceName = JPAUtil.PersistenceUnitNames.getPersistenceUnitNameForDB(db);
        EntityManagerFactory emf = Persistence.createEntityManagerFactory(persistenceName, entityManagerSettings);
        return  emf.createEntityManager();
    }
}
