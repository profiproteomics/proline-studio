package fr.proline.studio.dam.tasks;

import fr.proline.core.orm.uds.*;

import fr.proline.core.orm.util.DataStoreConnectorFactory;
import fr.proline.repository.ProlineDatabaseType;
import fr.proline.repository.DatabaseConnectorFactory;
import fr.proline.repository.IDatabaseConnector;
import fr.proline.studio.dam.UDSDataManager;
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
 * Used to connect to a UDS DB, MSI DB or PDI DB
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
        setTaskInfo(new TaskInfo("Connection to UDS Database", TASK_LIST_INFO));
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
        setTaskInfo(new TaskInfo("Connection to MSI Database", TASK_LIST_INFO));
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
        setTaskInfo(new TaskInfo("Check Project User", TASK_LIST_INFO));
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
                    
                    IDatabaseConnector udsConn = DatabaseConnectorFactory.createDatabaseConnectorInstance(ProlineDatabaseType.UDS, m_databaseProperties);
                    DataStoreConnectorFactory.getInstance().initialize(udsConn);
                } catch (Exception e) {
                    m_logger.error(getClass().getSimpleName() + " failed", e);
                    m_taskError = new TaskError(e.getMessage());
                    DataStoreConnectorFactory.getInstance().closeAll();
                    return false;
                }
                
                // Load needed static data from UDS
                
                EntityManager entityManagerUDS = DataStoreConnectorFactory.getInstance().getUdsDbConnector().getEntityManagerFactory().createEntityManager();

                // Load all users
                try {
                    entityManagerUDS.getTransaction().begin();

                    TypedQuery<UserAccount> userQuery = entityManagerUDS.createQuery("SELECT user FROM fr.proline.core.orm.uds.UserAccount user ORDER BY user.login ASC", UserAccount.class);
                    List<UserAccount> userList = userQuery.getResultList();
                    UDSDataManager.getUDSDataManager().setProjectUsers(userList);

                    entityManagerUDS.getTransaction().commit();

                } catch (Exception e) {
                    m_taskError = new TaskError("Unable to load UserAccount from UDS");
                    m_logger.error(getClass().getSimpleName() + " failed", e);
                    entityManagerUDS.getTransaction().rollback();
                    DataStoreConnectorFactory.getInstance().closeAll();
                    return false;
                }
                

                
                // check if the projectUser asked is known
                if (!checkProjectUser()) {
                    return false;
                }
                
                // check if the projectUser asked is known
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
                UDSDataManager.getUDSDataManager().setUdsJdbcDriver(udsJdbcDriver);
                UDSDataManager.getUDSDataManager().setUdsJdbcURL(udsJdbcUrl);
                
                
            } else if (action == UDS_USER_TEST) {
                // check if the projectUser asked is known
                if (!checkProjectUser()) {
                    return false;
                }
                
                // load needed UDS Data
                EntityManager entityManagerUDS = DataStoreConnectorFactory.getInstance().getUdsDbConnector().getEntityManagerFactory().createEntityManager();
                if (!loadUDSData(entityManagerUDS)) {
                    return false;
                }
                
            } else if (action == MSI_CONNECTION) {
                try {
                    // MSI Connection
                    EntityManager entityManagerMSI = DataStoreConnectorFactory.getInstance().getMsiDbConnector(m_projectId).getEntityManagerFactory().createEntityManager();
                    entityManagerMSI.close();

                    
                    // PS Connection
                    EntityManager entityManagerPS = DataStoreConnectorFactory.getInstance().getPsDbConnector().getEntityManagerFactory().createEntityManager();
                    entityManagerPS.close();
                } catch (Exception e) {
                    m_logger.error(getClass().getSimpleName() + " failed", e);
                    m_taskError = new TaskError(e);
                    return false;
                }
            } /*else if (action == PDI_CONNECTION) {
                try {
                    // MSI Connection
                    EntityManager entityManagerPDI = DataStoreConnectorFactory.getInstance().getPdiDbConnector().getEntityManagerFactory().createEntityManager();
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
        UDSDataManager udsMgr = UDSDataManager.getUDSDataManager();
        boolean foundUser = false;
        UserAccount[] projectUsers = udsMgr.getProjectUsersArray();
        int nb = projectUsers.length;
        for (int i = 0; i < nb; i++) {
            UserAccount account = projectUsers[i];
            if (m_projectUser.compareToIgnoreCase(account.getLogin()) == 0) {
                udsMgr.setProjectUser(account);
                foundUser = true;
                break;
            }
        }
        if (!foundUser) {
            m_taskError = new TaskError("Project User " + m_projectUser + " is unknown");
            //DataStoreConnectorFactory.getInstance().closeAll();
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
            UDSDataManager.getUDSDataManager().setIntruments(instrumentList);

            entityManagerUDS.getTransaction().commit();

        } catch (Exception e) {
            m_taskError = new TaskError("Unable to load Instrument Configurations from UDS");
            m_logger.error(getClass().getSimpleName() + " failed", e);
            entityManagerUDS.getTransaction().rollback();
            DataStoreConnectorFactory.getInstance().closeAll();
            return false;
        }

        // Load All peaklist softwares
        try {
            entityManagerUDS.getTransaction().begin();

            TypedQuery<PeaklistSoftware> peaklistSoftwareQuery = entityManagerUDS.createQuery("SELECT p FROM fr.proline.core.orm.uds.PeaklistSoftware p ORDER BY p.name ASC", PeaklistSoftware.class);
            List<PeaklistSoftware> peaklistSoftwareList = peaklistSoftwareQuery.getResultList();
            UDSDataManager.getUDSDataManager().setPeaklistSofwares(peaklistSoftwareList);

            entityManagerUDS.getTransaction().commit();

        } catch (Exception e) {
            m_taskError  = new TaskError("Unable to load Peaklist Softwares from UDS");
            m_logger.error(getClass().getSimpleName() + " failed", e);
            entityManagerUDS.getTransaction().rollback();
            DataStoreConnectorFactory.getInstance().closeAll();
            return false;
        }

        // Look all Aggregation
        try {
            entityManagerUDS.getTransaction().begin();

            TypedQuery<Aggregation> aggregationQuery = entityManagerUDS.createQuery("SELECT a FROM fr.proline.core.orm.uds.Aggregation a", Aggregation.class);
            List<Aggregation> aggregationList = aggregationQuery.getResultList();
            UDSDataManager.getUDSDataManager().setAggregationList(aggregationList);

            entityManagerUDS.getTransaction().commit();

        } catch (Exception e) {
            m_taskError = new TaskError("Unable to load Datasets from UDS");
            m_logger.error(getClass().getSimpleName() + " failed", e);
            entityManagerUDS.getTransaction().rollback();
            DataStoreConnectorFactory.getInstance().closeAll();
            return false;
        }

        return true;
    }
    
    /*
    public static void testInsert(Integer projectId) {
        
        double[] doubles = { 13.25, 57145.454545 };
        
        ByteBuffer byteDBuffer = ByteBuffer.allocate(doubles.length*8);
        for (int i=0;i<doubles.length;i++) {
            byteDBuffer.putDouble(doubles[i]);
        }
        byte[] byteDArray = byteDBuffer.array();
        
        float[] floats = { 13.25f, 57145.454f };
        
        ByteBuffer byteFBuffer = ByteBuffer.allocate(floats.length*4);
        for (int i=0;i<floats.length;i++) {
            byteFBuffer.putFloat(floats[i]);
        }
        byte[] byteFArray = byteFBuffer.array();
        
        
        testInsert(projectId, byteDArray, byteFArray);
        
    }
    
    
  
    public static void testInsert(Integer projectId, byte[] testByteDouble, byte[] testByteFloat) {
        


         EntityManager entityManagerMSI = DataStoreConnectorFactory.getInstance().getMsiDbConnector(projectId).getEntityManagerFactory().createEntityManager();

        try {

            entityManagerMSI.getTransaction().begin();

            Spectrum s = new Spectrum();
            s.setTitle("Title");
            s.setMozList(testByteDouble);
            s.setIntensityList(testByteFloat);
            s.setSerializedProperties(null);
            s.setPeakCount(52);
            s.setPrecursorCharge(2);
            s.setPrecursorIntensity(5.26f);
            s.setFirstScan(10);
            s.setFirstCycle(20);
            s.setFirstTime(3566.04f);
            s.setPeaklistId(1);
            s.setInstrumentConfigId(1);
            entityManagerMSI.persist(s);

        
            entityManagerMSI.getTransaction().commit();
         
         
         
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            entityManagerMSI.close();
        }
        
    }*/
        /*


        long timeStart6 = 0;
        try {
    String DATABASE_URL = "jdbc:postgresql://gre037784:5433/MSIORM_db";
    String DATABASE_USER = "dupierris";
    String DATABASE_PASSWORD = "dupierris";
        
    BaseConnection connection = (BaseConnection) DriverManager.getConnection(DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD);
        
    timeStart6 = System.currentTimeMillis();
    StringBuilder sb = new StringBuilder();
    for (int j=0;j<10;j++) {
    for (lIndex = 0; lIndex < 1000; lIndex++) {
        sb.append("title|253|5|2|true|1|2000|4000|3521|3245|993|");
        sb.append("\\x");
        bytes2Hex(sb,testByte );
        sb.append('|');
        sb.append("\\x");
        bytes2Hex(sb,testByte );
        sb.append("|52|\\N|1|1");
        sb.append("\n");
    }
    
    StringReader sr = new StringReader(sb.toString());
    
   // System.out.println(sb.toString());
    
    //InputStream is = new ByteArrayInputStream(sr.toString().getBytes());
        
        CopyManager cm = new CopyManager(connection);
        cm.copyIn("COPY spectrum(title, precursor_moz, precursor_intensity, precursor_charge, is_summed, first_cycle, last_cycle, first_scan, last_scan, first_time, last_time, moz_list, intensity_list, peak_count, serialized_properties, peaklist_id, instrument_config_id) FROM STDIN WITH DELIMITER '|'", sr);
    sb.setLength(0);
    }
        connection.close();
        
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(lIndex);
        }
        
        long deltaTime6 = System.currentTimeMillis()-timeStart6;
        System.out.println(" Création de 10000 spectrum : "+deltaTime6);
    }
    
    private static void bytes2Hex(StringBuilder sb, byte[] byteArray) {
    
        int nb = byteArray.length;
        for (int i = 0; i < nb; i++) {
            byte b = byteArray[i];
            sb.append(HEX_CHARS[(b & 0xF0) >>> 4]);
        }


    }
  private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();*/
}
