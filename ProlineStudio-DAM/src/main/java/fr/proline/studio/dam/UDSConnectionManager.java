package fr.proline.studio.dam;

import fr.proline.repository.AbstractDatabaseConnector;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.AbstractDatabaseTask;
import fr.proline.studio.dam.tasks.DatabaseConnectionTask;
import fr.proline.studio.dam.tasks.SubTask;
import java.util.HashMap;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;
import org.slf4j.LoggerFactory;

/**
 *
 * @author JM235353
 */
public class UDSConnectionManager {

    private static final String KEY_DRIVER_NAME = "driverNameDBServer";
    private static final String KEY_JDBC_URL = "jdbcUrlDBServer";
    private static final String KEY_HOST = "hostDBServer";
    private static final String KEY_PORT = "portDBServer";
    private static final String KEY_DB_NAME = "databaseName";
    private static final String KEY_DB_USER = "databaseUserName";
    private static final String KEY_DB_PASSWORD = "databasePassword";
    private static final String[] PREDEFINED_DRIVERS_NAMES = {"PostgreSQL"};
    private static final String[] PREDEFINED_DRIVERS_CLASSES = {"org.postgresql.Driver"};
    private static final String[] PREDEFINED_JDBC_DRIVERS = {"jdbc:postgresql:"};
    public static final int NOT_CONNECTED = 0;
    public static final int CONNECTION_ASKED = 1;
    public static final int CONNECTION_FAILED = 2;
    public static final int CONNECTION_DONE = 3;
    private int connectionStep = NOT_CONNECTED;
    private String connectionError = null;
    private String m_driverName;
    private String m_driverClass;
    private String m_jdbcUrl;
    private String m_host;
    private String m_port;
    private String m_dbName;
    private String m_userName;
    private String m_password;
    private static UDSConnectionManager databaseConnectionManager = null;

    public static synchronized UDSConnectionManager getUDSConnectionManager() {
        if (databaseConnectionManager == null) {
            databaseConnectionManager = new UDSConnectionManager();
        }
        return databaseConnectionManager;
    }

    public UDSConnectionManager() {
        restoreParameters();
        tryToConnect();
    }

    public synchronized int getConnectionStep() {
        return connectionStep;
    }

    public synchronized void setConnectionStep(int connectionStep) {
        this.connectionStep = connectionStep;
    }

    public String getConnectionError() {
        return connectionError;
    }
    
    public String getDriverName() {
        return m_driverName;
    }

    public String getDriverClass() {
        return m_driverClass;
    }

    public String getJdbcUrl() {
        return m_jdbcUrl;
    }

    public String getHost() {
        return m_host;
    }

    public String getPort() {
        return m_port;
    }

    public String getDBName() {
        return m_dbName;
    }

    public String getUserName() {
        return m_userName;
    }

    public String getPassword() {
        return m_password;
    }


    public void setDriverName(String driverName) {
        m_driverName = driverName;
    }

    public void setDriverClass(String driverClass) {
        m_driverClass = driverClass;
    }

    public void setJdbcUrl(String jdbcUrl) {
        m_jdbcUrl = jdbcUrl;
    }

    public void setHost(String host) {
        m_host = host;
    }

    public void setPort(String port) {
        m_port = port;
    }

    public void setDBName(String dbName) {
        m_dbName = dbName;
    }

    public void setUserName(String userName) {
        m_userName = userName;
    }

    public void setPassword(String password) {
        m_password = password;
    }
    
    private void restoreParameters() {
        Preferences preferences = NbPreferences.forModule(UDSConnectionManager.class);

        m_driverName = preferences.get(KEY_DRIVER_NAME, "");
        int driverIndex = -1;
        for (int i = 0; i < PREDEFINED_DRIVERS_NAMES.length; i++) {
            if (PREDEFINED_DRIVERS_NAMES[i].compareTo(m_driverName) == 0) {
                driverIndex = i;
            }
        }
        if (driverIndex > -1) {
            m_driverClass = PREDEFINED_DRIVERS_CLASSES[driverIndex];
        } else {
            m_driverClass = "";
        }


        m_jdbcUrl = preferences.get(KEY_JDBC_URL, "");
        if (m_jdbcUrl.isEmpty() && (driverIndex > -1)) {
            m_jdbcUrl = PREDEFINED_JDBC_DRIVERS[driverIndex];
        }


        m_host = preferences.get(KEY_HOST, "");
        m_port = preferences.get(KEY_PORT, "");
        m_dbName = preferences.get(KEY_DB_NAME, "");
        m_userName = preferences.get(KEY_DB_USER, "");
        m_password = preferences.get(KEY_DB_PASSWORD, "");


    }

    public void saveParameters() {
        Preferences preferences = NbPreferences.forModule(UDSConnectionManager.class);
        
        preferences.put(KEY_DRIVER_NAME, m_driverName);
        preferences.put(KEY_JDBC_URL, m_jdbcUrl);
        preferences.put(KEY_HOST, m_host);
        preferences.put(KEY_PORT, m_port);
        
        preferences.put(KEY_DB_NAME, m_dbName);
        preferences.put(KEY_DB_USER, m_userName);
        
        preferences.put(KEY_DB_PASSWORD, m_password);
        
        try {
            preferences.flush();
        } catch (BackingStoreException e) {
            LoggerFactory.getLogger(UDSConnectionManager.class).error("Saving UDS Connection Parameters Failed", e);
        }
    }
    
    private void tryToConnect() {
        tryToConnect(null, m_jdbcUrl, m_driverClass, m_dbName, m_host, m_port, m_userName, m_password);
    }
    public void tryToConnect(final Runnable connectionCallback, String jdbcUrl, String driverClass, String dbName, String host, String port, String userName, String password) {
        setConnectionStep(CONNECTION_ASKED);

        // first check on parameters
        if (driverClass.isEmpty()) {
            connectionError = "Driver Class is not set";
            connectFailed(connectionCallback);
            return;
        }

        if (host.isEmpty()) {
            connectionError = "Host is not set";
            connectFailed(connectionCallback);
            return;
        }
        if (port.isEmpty()) {
            connectionError = "Port is not set";
            connectFailed(connectionCallback);
            return;
        }
        if (jdbcUrl.isEmpty()) {
            connectionError = "JDBC URL is not set";
            connectFailed(connectionCallback);
            return;
        }
        if (dbName.isEmpty()) {
            connectionError = "Database Name is not set";
            connectFailed(connectionCallback);
            return;
        }

        // Prepare Connection Parameters
        HashMap<Object, Object> databaseProperties = new HashMap<Object, Object>();

        databaseProperties.put(AbstractDatabaseConnector.PERSISTENCE_JDBC_USER_KEY, userName);
        databaseProperties.put(AbstractDatabaseConnector.PERSISTENCE_JDBC_PASSWORD_KEY, password);
        databaseProperties.put(AbstractDatabaseConnector.PERSISTENCE_JDBC_DRIVER_KEY, driverClass);

        String jdbcURL = jdbcUrl + "//" + host + ":" + port + "/" + dbName;
        databaseProperties.put(AbstractDatabaseConnector.PERSISTENCE_JDBC_URL_KEY, jdbcURL);

        // ask for the connection
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask) {

                if (!success) {
                    //JPM.TODO
                    setConnectionStep(CONNECTION_FAILED);
                    connectionError = getErrorMessage();
                } else {
                    // save connection parameters
                    saveParameters();
                    setConnectionStep(CONNECTION_DONE);
                }
                
                if (connectionCallback != null) {
                    connectionCallback.run();
                }

            }
        };

        // ask asynchronous loading of data
        DatabaseConnectionTask connectionTask = new DatabaseConnectionTask(callback, databaseProperties);

        AccessDatabaseThread.getAccessDatabaseThread().addTask(connectionTask);

    }
    private void connectFailed(Runnable connectionCallback) {
        setConnectionStep(CONNECTION_FAILED);
        if (connectionCallback != null) {
            connectionCallback.run();
        }
    }
}
