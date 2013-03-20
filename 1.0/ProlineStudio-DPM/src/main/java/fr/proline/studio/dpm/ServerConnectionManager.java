package fr.proline.studio.dpm;

import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseConnectionTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.dpm.task.AbstractServiceCallback;
import fr.proline.studio.dpm.task.CreateProjectTask;
import fr.proline.studio.dpm.task.CreateUserTask;
import fr.proline.studio.dpm.task.ServerConnectionTask;
import java.util.HashMap;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jm235353
 */
public class ServerConnectionManager {
    
    private static final String KEY_SERVER_URL = "serverURL";
    private static final String KEY_PROJECT_USER = "projectUser";
    private static final String KEY_DB_PASSWORD = "databasePassword";
    
    public static final int NOT_CONNECTED = 0;
    public static final int CONNECTION_SERVER_ASKED = 1;
    public static final int CONNECTION_SERVER_FAILED = 2;
    public static final int CONNECTION_DATABASE_ASKED = 3;
    public static final int CONNECTION_DATABASE_FAILED = 4;
    public static final int CONNECTION_DONE = 5;
    
    private int connectionState = NOT_CONNECTED;
    private String connectionError = null;
    
    private String m_serverURL;
    private String m_projectUser;
    private String m_databasePassword;
    
    
    private String previousServerURL = "";
    private String previousProjectUser = "";
    private String previousDatabsePassword = "";
    private int previousErrorId = -1;
    
    
    private static ServerConnectionManager connectionManager = null;

    public static synchronized ServerConnectionManager getServerConnectionManager() {
        if (connectionManager == null) {
            connectionManager = new ServerConnectionManager();
        }
        return connectionManager;
    }

    public ServerConnectionManager() {
        restoreParameters();
        tryServerConnection();
    }
    
   private void restoreParameters() {
        Preferences preferences = NbPreferences.root();

        m_serverURL = preferences.get(KEY_SERVER_URL, "http://");
        m_projectUser = preferences.get(KEY_PROJECT_USER, "");
        m_databasePassword = preferences.get(KEY_DB_PASSWORD, "");
    }
   
       public void saveParameters() {
        Preferences preferences = NbPreferences.root();
        
        preferences.put(KEY_SERVER_URL, m_serverURL);
        preferences.put(KEY_PROJECT_USER, m_projectUser);
        preferences.put(KEY_DB_PASSWORD, m_databasePassword);

        
        try {
            preferences.flush();
        } catch (BackingStoreException e) {
            LoggerFactory.getLogger(ServerConnectionManager.class).error("Saving Server Connection Parameters Failed", e);
        }
    }
   
   private void tryServerConnection() {
       tryServerConnection(null, m_serverURL, m_projectUser, m_databasePassword);
   }
   public void tryServerConnection(final Runnable connectionCallback, String serverURL, final String projectUser, String databasePassword) {

       // pre-check to avoid to try a connection when the parameters are not set
       if (serverURL.length()<="http://".length()) {
           setConnectionState(CONNECTION_SERVER_FAILED);
           return;
       }

       
       // check if the user has not already tried to connect with the same parameters
       // and the project user was unknown
       if ((previousServerURL.compareTo(serverURL) ==0 ) &&
           (previousDatabsePassword.compareTo(databasePassword) ==0 ) &&
           (previousErrorId == DatabaseConnectionTask.ERROR_USER_UNKNOWN)) {
           // special case, we only check the project user
           tryProjectUser(connectionCallback, projectUser);
           return;
       }
       
       // keep settings used to try to connect
       previousServerURL = serverURL;
       previousDatabsePassword = databasePassword;
       previousProjectUser = projectUser;
       previousErrorId = -1;
       
     
       
       setConnectionState(CONNECTION_SERVER_ASKED);
       
       final HashMap<Object, Object> databaseProperties = new HashMap<Object, Object>();
       
       // First, we try to connect to the service 
       AbstractServiceCallback callback = new AbstractServiceCallback() {

           @Override
           public boolean mustBeCalledInAWT() {
               return true;
           }

           @Override
           public void run(boolean success) {
               if (success) {
                   // we now ask for the database connection
                   tryDatabaseConnection(connectionCallback, databaseProperties, projectUser);
               } else {
                   setConnectionState(CONNECTION_SERVER_FAILED);
                   connectionError = getErrorMessage();
                   if (connectionCallback != null) {
                       connectionCallback.run();
                   }
               }
           }
       };


       ServerConnectionTask task = new ServerConnectionTask(callback, serverURL, databasePassword, databaseProperties);
       AccessServiceThread.getAccessServiceThread().addTask(task);
       
   }
   
   private void tryProjectUser(final Runnable connectionCallback, String projectUser) {
       setConnectionState(CONNECTION_DATABASE_ASKED);
       
       
        // ask for the connection
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {

                if (success) {
                    // save connection parameters
                    saveParameters();
                    setConnectionState(CONNECTION_DONE);
                } else {
                    
                    previousErrorId = getErrorId();
                    
                    setConnectionState(CONNECTION_DATABASE_FAILED);
                    connectionError = getErrorMessage();

                }
                
                 if (connectionCallback != null) {
                    connectionCallback.run();
                }

            }
        };

        // ask asynchronous loading of data
        DatabaseConnectionTask connectionTask = new DatabaseConnectionTask(callback, projectUser);

        AccessDatabaseThread.getAccessDatabaseThread().addTask(connectionTask); 
   }
   
   private void tryDatabaseConnection(final Runnable connectionCallback, HashMap<Object, Object> databaseProperties, String projectUser) {
      
       setConnectionState(CONNECTION_DATABASE_ASKED);
       
        // ask for the connection
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {

                if (success) {
                    // save connection parameters
                    saveParameters();
                    setConnectionState(CONNECTION_DONE);
                } else {
                    
                    previousErrorId = getErrorId();
                    
                    setConnectionState(CONNECTION_DATABASE_FAILED);
                    connectionError = getErrorMessage();
                    
                    //JPM.TODO : WART if no user has been created
                    if ((connectionError!= null) && (connectionError.indexOf("dupierris")!=-1)) {
                        // we create the user dupierris
                        
                        //CreateProjectTask.postUserRequest();
                        
                        CreateUserTask task = new CreateUserTask(null, "dupierris");
                        AccessServiceThread.getAccessServiceThread().addTask(task);
                    }
                }
                
                if (connectionCallback != null) {
                    connectionCallback.run();
                }

            }
        };

        // ask asynchronous loading of data
        DatabaseConnectionTask connectionTask = new DatabaseConnectionTask(callback, databaseProperties, projectUser);

        AccessDatabaseThread.getAccessDatabaseThread().addTask(connectionTask); 
   }
   
   
   public String getServerURL() {
       return m_serverURL;
   }
   
   public String getProjectUser() {
       return m_projectUser;
   }
   
    public String getDatabasePassword() {
        return m_databasePassword;
    }

    public void setServerURL(String serverURL) {
        m_serverURL = serverURL;
    }
    
    public void setProjectUser(String projectUser) {
        m_projectUser = projectUser;
    }
    
    public void setDatabasePassword(String databasePassword) {
        m_databasePassword = databasePassword;
    }

    
    
    public synchronized boolean isConnectionFailed() {
        return ((connectionState == CONNECTION_SERVER_FAILED) || (connectionState == CONNECTION_DATABASE_FAILED));
    }
    public synchronized boolean isConnectionAsked() {
        return ((connectionState == CONNECTION_SERVER_ASKED) || (connectionState == CONNECTION_DATABASE_ASKED));
    }
    public synchronized boolean isConnectionDone() {
        return (connectionState == CONNECTION_DONE);
    }
    public synchronized boolean isNotConnected() {
        return (connectionState == NOT_CONNECTED);
    }

    public synchronized void setConnectionState(int connectionState) {
        this.connectionState = connectionState;
    }

    public String getConnectionError() {
        return connectionError;
    }
}
