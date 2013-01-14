package fr.proline.studio.dam.tasks;



import fr.proline.core.orm.util.DatabaseManager;
import fr.proline.repository.Database;
import fr.proline.repository.DatabaseConnectorFactory;
import fr.proline.repository.IDatabaseConnector;
import java.util.Map;
import javax.persistence.EntityManager;

/**
 * Used to connect to a UDS DB or MSI DB
 *
 * @author JM235353
 */
public class DatabaseConnectionTask extends AbstractDatabaseTask {

    // used for UDS Connection
    private Map<Object, Object> databaseProperties;
    // used for MSI and PS Connection
    private int projectId;


    /**
     * Constructor used for UDS database
     *
     * @param callback
     * @param databaseProperties
     * @param projectId
     */
    public DatabaseConnectionTask(AbstractDatabaseCallback callback, Map<Object, Object> databaseProperties) {
        super(callback, Priority.TOP);

        this.databaseProperties = databaseProperties;


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
        
            if (databaseProperties != null) {
                
                try {
                    // UDS Connection
                    IDatabaseConnector udsConn = DatabaseConnectorFactory.createDatabaseConnectorInstance(Database.UDS, databaseProperties);
                    DatabaseManager.getInstance().initialize(udsConn);
                } catch (Exception e) {
                    logger.error(getClass().getSimpleName() + " failed", e);
                    errorMessage = e.getMessage();
                    DatabaseManager.getInstance().closeAll();
                    return false;
                }
            } else {
                try {
                    // MSI Connection
                    EntityManager entityManagerMSI = DatabaseManager.getInstance().getMsiDbConnector(projectId).getEntityManagerFactory().createEntityManager();
                    entityManagerMSI.close();

                    // PS Connection
                    EntityManager entityManagerPS = DatabaseManager.getInstance().getPsDbConnector().getEntityManagerFactory().createEntityManager();
                    entityManagerPS.close();
                } catch (Exception e) {
                    logger.error(getClass().getSimpleName() + " failed", e);
                    errorMessage = e.getMessage();
                    return false;
                }
            }
        
        return true;
    }

    
    /*
    public static void testInsert(byte[] testByteDouble, byte[] testByteFloat) {
        
        int SQL_LOOP = 10000;
        
        int lIndex = 0;
        
        long timeStart5 = System.currentTimeMillis();
         EntityManager entityManagerMSI = ProlineDBManagement.getProlineDBManagement().getProjectEntityManager(Database.MSI, true, AccessDatabaseThread.getProjectIdTMP());  //JPM.TODO : project id
        try {

            entityManagerMSI.getTransaction().begin();
        
        for (lIndex = 0; lIndex < SQL_LOOP; lIndex++) {
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
        }
        
        
        
         entityManagerMSI.getTransaction().commit();
         
         
         
        } catch (RuntimeException e) {
            e.printStackTrace();
        } finally {
            entityManagerMSI.close();
        }
        
        long deltaTime5 = System.currentTimeMillis()-timeStart5;
        System.out.println(" Création de 10000 spectrum : "+deltaTime5);*/
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
        System.out.println(" Création de 10000 spectrum : "+deltaTime6);*/
    /*}*/
    /*
    private static void bytes2Hex(StringBuilder sb, byte[] byteArray) {
    
        int nb = byteArray.length;
        for (int i = 0; i < nb; i++) {
            byte b = byteArray[i];
            sb.append(HEX_CHARS[(b & 0xF0) >>> 4]);
        }


    }
  private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();*/
}
