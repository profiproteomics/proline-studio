/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.dbs;

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
        "newRepo.error=Only one ProlineRepository could be specified.",
        "existingRepo.error=ProlineDbManagment instance has not been initialized. Cass intiProlineDbManagment(ProlineRepository pr) first."
        })
public class ProlineDbManagment {

    protected static ProlineDbManagment instance = null;
    
    protected ProlineRepository repository;
    protected EntityManager udsEM;
    protected EntityManager pdiEM;
    protected EntityManager psEM;
    
    private ProlineDbManagment() {
    }

    /**
     * initialize unique ProlineDbManagment instance. At first call, create ProlineDbManagment 
     * with specified ProlineRepository.
     * If ProlineDbManagment singleton instance has already been initialized with a different 
     * ProlineRepository , an exception will be thrown. 
     * Use getProlineDbManagment() to get initialized instance. 
     * 
     * @param prRepo associated ProlineRepository
     * @return 
     */
    public static boolean intiProlineDbManagment(ProlineRepository prRepo) {
        if (instance == null) {
            instance = new ProlineDbManagment();
            instance.setRepository(prRepo);
        } else {
            if(!prRepo.equals(instance.repository))
                throw new IllegalArgumentException(newRepo_error());                
        }    
        return true;
    }
    
    /**
     * Get the unique ProlineDbManagment instance. If instance has not be initialized an exception
     * is thrown
     * @see #intiProlineDbManagment(fr.proline.repository.ProlineRepository)  
     * @return the ProlineDbManagment instance
     * @throws UnsupportedOperationException if instance has not been initialized yet
     */
    public static ProlineDbManagment getProlineDbManagment() {
        if(instance == null)
            throw new UnsupportedOperationException(existingRepo_error());
        return instance;
    }
    
    private void setRepository(ProlineRepository repo) {
        repository = repo;
    }
    
    /**
     * Return the DatabaseConnector for specified Database
     * @param db the Database  to get DatabaseConnector for 
     * @return the DatabaseConnector for specified Database
     */
    public DatabaseConnector getDatabaseConnector(ProlineRepository.Databases db){
        return repository.getConnector(db);
    }
    
    /**
     * Close all connections to databases associated to this manager
     * 
     * @throws Exception 
     */
    public void closeAll() throws Exception{
        repository.closeAll();
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
        }
        if(!udsEM.isOpen()){
            DatabaseConnector dc = repository.getConnector(ProlineRepository.Databases.UDS);   
            udsEM = createEM(ProlineRepository.Databases.UDS);
        }
        return udsEM;
    }
    
    private EntityManager createEM(ProlineRepository.Databases db){
        
        final Map<String, Object> entityManagerSettings = repository.getConnector(db).getEntityManagerSettings();
        String persistenceName = JPAUtil.PersistenceUnitNames.getPersistenceUnitNameForDB(db);
        EntityManagerFactory emf = Persistence.createEntityManagerFactory(persistenceName, entityManagerSettings);
        return  emf.createEntityManager();
    }
}
