package fr.proline.studio.dam.tasks;

import fr.proline.core.orm.uds.*;

import fr.proline.core.orm.util.DStoreCustomPoolConnectorFactory;
import fr.proline.repository.DatabaseConnectorFactory;
import fr.proline.repository.ProlineDatabaseType;
import fr.proline.repository.IDatabaseConnector;
import fr.proline.studio.dam.DatabaseDataManager;
import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverManager;


/**
 * Used to connect to a UDS DB, MSI DB
 *
 * @author JM235353
 */
public class DatabaseConnectionTask extends AbstractDatabaseTask {

    // used for UDS Connection
    private Map<Object, Object> m_databaseProperties;
    private String m_projectUser;
    
    // used for MSI and PS Connection
    private int m_projectId;

    public static int ERROR_USER_UNKNOWN = 1;

   
    
    private int action;
    
    private static int UDS_CONNECTION = 0;
    private static int MSI_CONNECTION = 1;
    //private static int PDI_CONNECTION = 3;
    private static int UDS_USER_TEST  = 4;
          
    
    /**
     * Constructor 
     *
     * @param callback
     * @param databaseProperties
     * @param projectId
     */
    public DatabaseConnectionTask(AbstractDatabaseCallback callback) {
        super(callback, null);
    }
    
    /**
     * Init connection to UDS
     * @param databaseProperties
     * @param projectUser 
     */
    public void initConnectionToUDS(Map<Object, Object> databaseProperties, String projectUser) {
        setTaskInfo(new TaskInfo("Connection to UDS Database", false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_HIGH));
        setPriority(Priority.TOP);
        
        m_databaseProperties = databaseProperties;
//        m_databaseProperties.put("hibernate.show_sql", Boolean.TRUE);
//        m_databaseProperties.put("hibernate.format_sql", Boolean.TRUE);
        
        m_projectUser = projectUser;
        
        action = UDS_CONNECTION;
    }
    
    /**
     * Connection to MSI
     * @param projectId 
     */
    public void initConnectionToMSI(int projectId) {
        setTaskInfo(new TaskInfo("Connection to MSI Database", false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_HIGH));
        setPriority(Priority.TOP);
        
        m_projectId = projectId;
        
        action = MSI_CONNECTION;
    }
    
    /**
     * Connection to PDI
     * @param projectId 
     */
    /*public void initConnectionToPDI() {
        setTaskInfo(new TaskInfo("Connection to PDI Database", TASK_LIST_INFO));
        setPriority(Priority.TOP);

        
        action = PDI_CONNECTION;
    }*/
    
    /**
     * to check if the project user is known in the database
     * @param databaseProperties
     * @param projectUser 
     */
    public void initCheckProjectUser(String projectUser) {
        setTaskInfo(new TaskInfo("Check Project User", false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));
        setPriority(Priority.TOP);

        m_projectUser = projectUser;
        
        action = UDS_USER_TEST;
    }
    
    
    @Override
    public boolean needToFetch() {
        return true; // anyway this task is used only one time for each node

    }

    
    @Override
    public boolean fetchData() {
        
            if (action == UDS_CONNECTION) {

                // UDS Connection
                try {
                    
                    IDatabaseConnector udsConn = DatabaseConnectorFactory.createDatabaseConnectorInstance(ProlineDatabaseType.UDS, m_databaseProperties); //VD #16961,IDatabaseConnector.ConnectionPoolType.SIMPLE_POOL_MANAGEMENT);
                    DStoreCustomPoolConnectorFactory.getInstance().initialize(udsConn, "Proline STUDIO"); //VD #16961,IDatabaseConnector.ConnectionPoolType.SIMPLE_POOL_MANAGEMENT);
                } catch (Exception e) {
                    m_logger.error(getClass().getSimpleName() + " failed", e);
                    m_taskError = new TaskError(e.getMessage());
                    DStoreCustomPoolConnectorFactory.getInstance().closeAll();
                    return false;
                }
                
                // Load needed static data from UDS
                
                EntityManager entityManagerUDS = DStoreCustomPoolConnectorFactory.getInstance().getUdsDbConnector().createEntityManager();

                // Load all users
                try {
                    entityManagerUDS.getTransaction().begin();

                    TypedQuery<UserAccount> userQuery = entityManagerUDS.createQuery("SELECT user FROM fr.proline.core.orm.uds.UserAccount user ORDER BY user.login ASC", UserAccount.class);
                    List<UserAccount> userList = userQuery.getResultList();
                    DatabaseDataManager.getDatabaseDataManager().setProjectUsers(userList);

                    entityManagerUDS.getTransaction().commit();

                } catch (Exception e) {
                    m_taskError = new TaskError("Unable to load UserAccount from UDS");
                    m_logger.error(getClass().getSimpleName() + " failed", e);
                    entityManagerUDS.getTransaction().rollback();
                    DStoreCustomPoolConnectorFactory.getInstance().closeAll();
                    return false;
                }
                

                
                // check if the projectUser asked is known
                if (!checkProjectUser()) {
                    return false;
                }
                
                // load data from UDS : instruments, peaklists...
                if (!loadUDSData(entityManagerUDS)) {
                    return false;
                }
                
                
                entityManagerUDS.close();
                
               
                // add the UDS connection to the Netbeans Service
                String udsJdbcDriver = (String) m_databaseProperties.get("javax.persistence.jdbc.driver");
                String udsJdbcUrl = (String) m_databaseProperties.get("javax.persistence.jdbc.url");
                try {
                    ConnectionManager cm = ConnectionManager.getDefault();

                    JDBCDriver driver = JDBCDriverManager.getDefault().getDrivers(udsJdbcDriver)[0];

                    DatabaseConnection dbconn = DatabaseConnection.create(driver, udsJdbcUrl, (String) m_databaseProperties.get("javax.persistence.jdbc.user"), "public", (String) m_databaseProperties.get("javax.persistence.jdbc.password"), true);
                 
                    cm.addConnection(dbconn);
                } catch (Exception e) {
                    
                    String message = e.getMessage();
                    if ((message == null) || (message.indexOf("connection already exists") == -1)) { //JPM.WART : avoid error because the connection already exist
                        m_logger.error(getClass().getSimpleName() + " failed to add UDS connection to Services ", e);
                    }
                }
                
                // keep some information to be able to add MSI connection to Netbeans Service
                DatabaseDataManager.getDatabaseDataManager().setUdsJdbcDriver(udsJdbcDriver);
                DatabaseDataManager.getDatabaseDataManager().setUdsJdbcURL(udsJdbcUrl);
                
                
            } else if (action == UDS_USER_TEST) {
                // check if the projectUser asked is known
                if (!checkProjectUser()) {
                    return false;
                }
                
                // load needed UDS Data
                EntityManager entityManagerUDS = DStoreCustomPoolConnectorFactory.getInstance().getUdsDbConnector().createEntityManager();
                if (!loadUDSData(entityManagerUDS)) {
                    return false;
                }
                
            } else if (action == MSI_CONNECTION) {
                try {
                    // MSI Connection
                    EntityManager entityManagerMSI = DStoreCustomPoolConnectorFactory.getInstance().getMsiDbConnector(m_projectId).createEntityManager();
                    entityManagerMSI.close();

                    
                } catch (Exception e) {
                    m_logger.error(getClass().getSimpleName() + " failed", e);
                    m_taskError = new TaskError(e);
                    return false;
                }
            } /*else if (action == PDI_CONNECTION) {
                try {
                    // MSI Connection
                    EntityManager entityManagerPDI = DStoreCustomPoolConnectorFactory.getInstance().getPdiDbConnector().createEntityManager();
                    entityManagerPDI.close();

                } catch (Exception e) {
                    m_logger.error(getClass().getSimpleName() + " failed", e);
                    m_taskError = new TaskError(e);
                    return false;
                }
            }*/
        
        return true;
    }

    private boolean checkProjectUser() {
        // check if the projectUser asked is known
        DatabaseDataManager udsMgr = DatabaseDataManager.getDatabaseDataManager();
        boolean foundUser = false;
        UserAccount[] projectUsers = udsMgr.getProjectUsersArray();
        int nb = projectUsers.length;
        for (int i = 0; i < nb; i++) {
            UserAccount account = projectUsers[i];
            if (m_projectUser.compareToIgnoreCase(account.getLogin()) == 0) {
                udsMgr.setLoggedUser(account);
                foundUser = true;
                break;
            }
        }
        if (!foundUser) {
            m_taskError = new TaskError("Project User " + m_projectUser + " is unknown");
            //DStoreCustomPoolConnectorFactory.getInstance().closeAll();
            m_errorId = ERROR_USER_UNKNOWN;
            return false;
        }

        return true;
    }

    private boolean loadUDSData(EntityManager entityManagerUDS) {


        // Load All instruments
        try {
            entityManagerUDS.getTransaction().begin();

            TypedQuery<InstrumentConfiguration> instrumentQuery = entityManagerUDS.createQuery("SELECT i FROM fr.proline.core.orm.uds.InstrumentConfiguration i ORDER BY i.name ASC", InstrumentConfiguration.class);
            List<InstrumentConfiguration> instrumentList = instrumentQuery.getResultList();
            DatabaseDataManager.getDatabaseDataManager().setIntruments(instrumentList);

            entityManagerUDS.getTransaction().commit();

        } catch (Exception e) {
            m_taskError = new TaskError("Unable to load Instrument Configurations from UDS");
            m_logger.error(getClass().getSimpleName() + " failed", e);
            entityManagerUDS.getTransaction().rollback();
            DStoreCustomPoolConnectorFactory.getInstance().closeAll();
            return false;
        }
                
        // Load All fragmentation rules
        try {
            entityManagerUDS.getTransaction().begin();

            TypedQuery<FragmentationRule> fragRulesQuery = entityManagerUDS.createQuery("SELECT fr FROM fr.proline.core.orm.uds.FragmentationRule fr ORDER BY fr.id ASC", FragmentationRule.class);
            List<FragmentationRule> frList = fragRulesQuery.getResultList();
            DatabaseDataManager.getDatabaseDataManager().setFragmentationRules(frList);

            entityManagerUDS.getTransaction().commit();

        } catch (Exception e) {
            m_taskError = new TaskError("Unable to load FragmentationRule Configurations from UDS");
            m_logger.error(getClass().getSimpleName() + " failed", e);
            entityManagerUDS.getTransaction().rollback();
            DStoreCustomPoolConnectorFactory.getInstance().closeAll();
            return false;
        }
        
        // Load All fragmentation rule sets
        try {
            entityManagerUDS.getTransaction().begin();

            TypedQuery<FragmentationRuleSet> fragRuleSetsQuery = entityManagerUDS.createQuery("SELECT frs FROM fr.proline.core.orm.uds.FragmentationRuleSet frs ORDER BY frs.name ASC", FragmentationRuleSet.class);
            List<FragmentationRuleSet> frsList = fragRuleSetsQuery.getResultList();
            frsList.stream().forEach(frs -> { 
                frs.getFragmentationRules().stream().forEach(fr -> fr.getDescription());
            });
            DatabaseDataManager.getDatabaseDataManager().setFragmentationRuleSets(frsList);

            entityManagerUDS.getTransaction().commit();

        } catch (Exception e) {
            m_taskError = new TaskError("Unable to load FragmentationRuleSet Configurations from UDS");
            m_logger.error(getClass().getSimpleName() + " failed", e);
            entityManagerUDS.getTransaction().rollback();
            DStoreCustomPoolConnectorFactory.getInstance().closeAll();
            return false;
        }

        // Load All peaklist softwares
        try {
            entityManagerUDS.getTransaction().begin();

            TypedQuery<PeaklistSoftware> peaklistSoftwareQuery = entityManagerUDS.createQuery("SELECT p FROM fr.proline.core.orm.uds.PeaklistSoftware p ORDER BY p.name ASC", PeaklistSoftware.class);
            List<PeaklistSoftware> peaklistSoftwareList = peaklistSoftwareQuery.getResultList();
            DatabaseDataManager.getDatabaseDataManager().setPeaklistSofwares(peaklistSoftwareList);

            entityManagerUDS.getTransaction().commit();

        } catch (Exception e) {
            m_taskError  = new TaskError("Unable to load Peaklist Softwares from UDS");
            m_logger.error(getClass().getSimpleName() + " failed", e);
            entityManagerUDS.getTransaction().rollback();
            DStoreCustomPoolConnectorFactory.getInstance().closeAll();
            return false;
        }

        // Look all Aggregation
        try {
            entityManagerUDS.getTransaction().begin();

            TypedQuery<Aggregation> aggregationQuery = entityManagerUDS.createQuery("SELECT a FROM fr.proline.core.orm.uds.Aggregation a", Aggregation.class);
            List<Aggregation> aggregationList = aggregationQuery.getResultList();
            DatabaseDataManager.getDatabaseDataManager().setAggregationList(aggregationList);

            entityManagerUDS.getTransaction().commit();

        } catch (Exception e) {
            m_taskError = new TaskError("Unable to load Datasets from UDS");
            m_logger.error(getClass().getSimpleName() + " failed", e);
            entityManagerUDS.getTransaction().rollback();
            DStoreCustomPoolConnectorFactory.getInstance().closeAll();
            return false;
        }

        return true;
    }
    
}
