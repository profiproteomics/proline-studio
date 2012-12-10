/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.repositorymgr;

import fr.proline.core.dal.DatabaseManagement;
import fr.proline.core.orm.utils.JPAUtil;
import fr.proline.repository.DatabaseConnector;
import fr.proline.repository.ProlineRepository;
import fr.proline.repository.ProlineRepository.Databases;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.openide.util.NbBundle.Messages;
import static  fr.proline.studio.repositorymgr.Bundle.*;

/**
 *
 * @author VD225637
 */
@Messages({ "getEMDB.error=Specified Database, {0}, is not unique. You must use appropriate method specifying project.",
            "unknown.db.error=Specified Database is of unknown type.",
            "not.singleton.db.error=Specified Database is not a singleton Database.",
            "not.supported.db.error=Specified Database is not yet supportef.",
            "noneInitDBManagment.error=ProlineDbManagment instance has not been initialized yet ! Call intiProlineDbManagment(DatabaseConnector) first."
        
        })
public class ProlineDBManagement {
    protected static ProlineDBManagement instance = null;
    
    protected DatabaseManagement dbMgmt;
    protected EntityManager udsEM;
    protected EntityManager pdiEM;
    protected EntityManager psEM;
    protected Map<Integer, EntityManagerFactory> msiEMFByProject = new HashMap<Integer, EntityManagerFactory>();
    protected Map<Integer, EntityManager> msiEMByProject = new HashMap<Integer, EntityManager>();
    
    private ProlineDBManagement(DatabaseConnector udsConnector) {
        dbMgmt = new DatabaseManagement(udsConnector);        
    }
    
    public DatabaseManagement getAssociatedDBManagement(){
        return dbMgmt;
    }
    
   /**
     * initialize unique ProlineDBManagement instance. Specified UDS DatabaseConnector
     * is used to create associated DatabaseManagment. 
     * If ProlineDbManagment singleton instance has already been initialized false is returned
     * Use getProlineDBManagment() to get initialized instance. 
     * 
     * @param udsConnector DatabaseConnector to access UDSdb
     * @return true if initialization is successful false if ProlineDBManagment already initialized
     */
    public static boolean initProlineDBManagment(DatabaseConnector udsConnector) {
        if(instance != null)
            return false;
        
        instance = new ProlineDBManagement(udsConnector);                   
        return true;
    }
    
    /**
     * Get the unique ProlineDbManagment instance. If instance has not be initialized an exception
     * is thrown
     * @see #initProlineDBManagment(fr.proline.repository.DatabaseConnector)  
     * @return the ProlineDBManagement instance
     * @throws UnsupportedOperationException if instance has not been initialized yet
     */
    public static ProlineDBManagement getProlineDBManagement() {
        if(instance == null)
            throw new UnsupportedOperationException(noneInitDBManagment_error());
        return instance;
    }
    
    /**
     * return true if ProlineDBManagment have been initialized, false otherwise
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
    public DatabaseConnector getDatabaseConnector(Databases db) throws IllegalArgumentException{
        switch(db){
            case PS: 
                return dbMgmt.psDBConnector();               
            case PDI : return dbMgmt.pdiDBConnector();
            case UDS : return dbMgmt.udsDBConnector();
            case LCMS : 
            case MSI : 
                throw new IllegalArgumentException(not_singleton_db_error());
        }
        throw new IllegalArgumentException(unknown_db_error());    
    }
    
    /**
     * Return the DatabaseConnector for specified Database specific to specified project. If specified database
     * is a singleton, not project dependant, its unique DatabaseConnector will be returned
     * @param db the Database to get DatabaseConnector for 
     * @param projectId the related Project ID to get databaeConnector for
     * @return the DatabaseConnector for specified Database and project
     */
    public DatabaseConnector getProjectDatabaseConnector(Databases db, int projectId ) {
        switch(db){
            case LCMS : 
                 throw new IllegalArgumentException(not_supported_db_error());   
            case MSI :            
                return dbMgmt.getMSIDatabaseConnector(projectId, false);
            default:
                return getDatabaseConnector(db);
        }        
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
     * Get an EntityManager for the specified Database specific to specified project. 
     * If newEM is true, then a new EntityManager will be created. Otherwise, 
     * existing EntityManager will be return, if it don't exist a new one will 
     * be created and returned.
     * 
     * @param db : database to get an EntityManager for
     * @param newEM : specify of EntityManager should be created (even if one exist)
     * @param projectId the related Project ID to get databaeConnector for
     * @return a EntityManager for specified Database
     * @exception if specified database is not a singleton database.
     */    
    public EntityManager getProjectEntityManager(Databases db, boolean newEM, int projectId){
        switch(db){
            case LCMS:
                throw new IllegalArgumentException(not_supported_db_error());
            case MSI:
                if(newEM || !msiEMByProject.containsKey(projectId))
                    return createEM(db,projectId);
                else {
                    EntityManager prjEM = msiEMByProject.get(projectId);
                    if(!prjEM.isOpen())
                        return createEM(db,projectId);
                    return prjEM;
               }
            default :
                return getEntityManager(db, newEM);
        } 
    }
    
    /**
     * 
     * Get an EntityManager for the specified unique Database. Id newEM is true, then a new EntityManager will be created.
     * Otherwise, existing EntityManager will be return, if it don't exist a new one will be created and returned.
     * 
     * @param db : database to get an EntityManager for
     * @param newEM : specify of EntityManager should be created (even if one exist)
     * @return a EntityManager for specified Database
     * @exception if specified database is not a singleton database.
     */    
    public EntityManager getEntityManager(Databases db, boolean newEM){
        switch(db){
            case UDS:
                if(newEM || udsEM == null || !udsEM.isOpen())
                     udsEM = createEM(db,0);
                return udsEM;
            case LCMS:
                throw new IllegalArgumentException(getEMDB_error(ProlineRepository.Databases.LCMS.name()));
            case PDI:
                 if(newEM || pdiEM == null || !pdiEM.isOpen())
                     pdiEM = createEM(db,0);
                return pdiEM;
            case PS:
                 if(newEM || psEM == null || !psEM.isOpen())
                     psEM = createEM(db,0);
                return psEM;
            case MSI:
                throw new IllegalArgumentException(getEMDB_error(ProlineRepository.Databases.MSI.name()));
            default :
                throw new IllegalArgumentException(unknown_db_error());
        }  
    }
    
    private EntityManager createEM(Databases db, int projectID){
        switch(db){
            case UDS: return dbMgmt.udsEMF().createEntityManager();
            case PS:  return dbMgmt.psEMF().createEntityManager();
            case PDI: return dbMgmt.pdiEMF().createEntityManager();
            case LCMS:
            case MSI:               
                if(!msiEMFByProject.containsKey(projectID)){
                    final Map<String, Object> entityManagerSettings = getProjectDatabaseConnector(db, projectID).getEntityManagerSettings(); 
                    
                    //JPM.TODO : code to trace sql queries from hibernate
                    //entityManagerSettings.put("hibernate.show_sql", "true");
                    //System.out.println("XXXXXXXXXX hibernate.show_sql=true");
                    
                    msiEMFByProject.put(projectID, Persistence.createEntityManagerFactory(JPAUtil.PersistenceUnitNames.getPersistenceUnitNameForDB(db),                                                                                          entityManagerSettings ));
                }                
                EntityManagerFactory emf = msiEMFByProject.get(projectID);
                EntityManager em = emf.createEntityManager();
                msiEMByProject.put(projectID, em);
                return  em;
            default :  throw new IllegalArgumentException(unknown_db_error());
        }
    }
    
}
