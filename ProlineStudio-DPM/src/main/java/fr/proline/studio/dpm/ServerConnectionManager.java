package fr.proline.studio.dpm;

import fr.proline.core.orm.uds.UserAccount;
import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.UDSDataManager;
import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseConnectionTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.dpm.task.AbstractServiceCallback;
import fr.proline.studio.dpm.task.ServerConnectionTask;
import fr.proline.studio.dpm.task.UserAccountTask;
import java.util.HashMap;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;
import org.slf4j.LoggerFactory;

/**
 * Management of the connection to the Jetty Server and to the databases
 * @author jm235353
 */
public class ServerConnectionManager {
    
    private static final String KEY_SERVER_URL = "serverURL";
    private static final String KEY_PROJECT_USER = "projectUser";
    private static final String KEY_USER_PASSWORD = "databasePassword";
    private static final String KEY_PASSWORD_NEEDED = "passwordNeeded";
    
    public static final int NOT_CONNECTED = 0;
    public static final int CONNECTION_SERVER_ASKED = 1;
    public static final int CONNECTION_SERVER_FAILED = 2;
    public static final int CONNECTION_DATABASE_ASKED = 3;
    public static final int CONNECTION_DATABASE_FAILED = 4;
    public static final int CONNECTION_DONE = 5;
    
    private int m_connectionState = NOT_CONNECTED;
    private TaskError m_connectionError = null;
    
    private String m_serverURL;
    private String m_projectUser;
    private String m_databasePassword;
    private String m_userPassword;
    private boolean m_passwordNeeded;
    
    
    private String m_previousServerURL = "";
    private String m_previousProjectUser = "";
    private String m_previousUserPassword = "";
    
    private int m_previousErrorId = -1;

    
    private static ServerConnectionManager m_connectionManager = null;

    public static synchronized ServerConnectionManager getServerConnectionManager() {
        if (m_connectionManager == null) {
            m_connectionManager = new ServerConnectionManager();
        }
        return m_connectionManager;
    }

    public ServerConnectionManager() {
        restoreParameters();

        tryServerConnection();

    }
    
   private void restoreParameters() {
        Preferences preferences = NbPreferences.root();

        m_serverURL = preferences.get(KEY_SERVER_URL, "http://");
        m_projectUser = preferences.get(KEY_PROJECT_USER, "");
        m_userPassword = preferences.get(KEY_USER_PASSWORD, "");
        
        m_passwordNeeded = preferences.getBoolean(KEY_PASSWORD_NEEDED, false);
    }
   
    public void saveParameters() {
        Preferences preferences = NbPreferences.root();
        
        preferences.put(KEY_SERVER_URL, m_serverURL);
        preferences.put(KEY_PROJECT_USER, m_projectUser);
        preferences.put(KEY_USER_PASSWORD, m_userPassword);

        preferences.putBoolean(KEY_PASSWORD_NEEDED, m_passwordNeeded);
        
        try {
            preferences.flush();
        } catch (BackingStoreException e) {
            LoggerFactory.getLogger("ProlineStudio.DPM").error("Saving Server Connection Parameters Failed", e);
        }
    }
   
   private void tryServerConnection() {
       tryServerConnection(null, m_serverURL, m_projectUser, m_userPassword, false);
   }
   public void tryServerConnection(final Runnable connectionCallback, final String serverURL, final String projectUser, String userPassword, final boolean changingUser) {

       // pre-check to avoid to try a connection when the parameters are not set
       if (serverURL.length()<="http://".length()) {
           setConnectionState(CONNECTION_SERVER_FAILED);
           return;
       }

       
       // check if the user has not already tried to connect with the same parameters
       // and the project user was unknown
       if ((m_previousServerURL.compareTo(serverURL) ==0 ) &&
           (m_previousUserPassword.compareTo(userPassword) ==0 ) &&
           (m_previousErrorId == DatabaseConnectionTask.ERROR_USER_UNKNOWN)) {
           // special case, we only check the project user
           tryProjectUser(connectionCallback, projectUser);
           return;
       }
       
       // keep settings used to try to connect
       m_previousServerURL = serverURL;
       m_previousUserPassword = userPassword;
       m_previousProjectUser = projectUser;
       m_previousErrorId = -1;
       
       m_passwordNeeded = !userPassword.isEmpty();
       
       setConnectionState(CONNECTION_SERVER_ASKED);
       
       
       // First, we check the user password
       final String[] databasePassword = new String[1];
       AbstractServiceCallback callback = new AbstractServiceCallback() {

           @Override
           public boolean mustBeCalledInAWT() {
               return true;
           }

           @Override
           public void run(boolean success) {
               if (success) {
                   // we now try to connect to the server
                   m_databasePassword = databasePassword[0];
                   tryConnectToServer(connectionCallback, projectUser, serverURL, m_databasePassword, changingUser);



                   
                   
               } else {
                   setConnectionState(CONNECTION_SERVER_FAILED);
                   m_connectionError = getTaskError();
                   if (connectionCallback != null) {
                       connectionCallback.run();
                   }
               }
           }
       };
       
       
       UserAccountTask task = new UserAccountTask(callback, serverURL, projectUser, userPassword, databasePassword);
       AccessServiceThread.getAccessServiceThread().addTask(task);
 
   }
   
   private void tryConnectToServer(final Runnable connectionCallback, final String projectUser, final String serverURL, final String databasePassword, final boolean changingUser) {
       
       if (changingUser) {
           // we now ask for the database connection
           tryDatabaseConnection(connectionCallback, null, projectUser, changingUser);
           return;
       }
       
       final HashMap<Object, Object> databaseProperties = new HashMap<>();
       
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
                   tryDatabaseConnection(connectionCallback, databaseProperties, projectUser, false);
               } else {
                   setConnectionState(CONNECTION_SERVER_FAILED);
                   m_connectionError = getTaskError();
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
                    
                    m_previousErrorId = getErrorId();
                    
                    setConnectionState(CONNECTION_DATABASE_FAILED);
                    m_connectionError = getTaskError();

                }
                
                 if (connectionCallback != null) {
                    connectionCallback.run();
                }

            }
        };

        // ask asynchronous loading of data
        DatabaseConnectionTask connectionTask = new DatabaseConnectionTask(callback);
        connectionTask.initCheckProjectUser(projectUser);

        AccessDatabaseThread.getAccessDatabaseThread().addTask(connectionTask); 
   }
   
   private void tryDatabaseConnection(final Runnable connectionCallback, HashMap<Object, Object> databaseProperties, String projectUser, boolean changingUser) {
      
       if (changingUser) {
           // save connection parameters
           saveParameters();
           setConnectionState(CONNECTION_DONE);
           
           if (connectionCallback != null) {
               connectionCallback.run();
           }

     
           return;
       }
       
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
                    
                    // ask connection to PDI
                    //connectionToPdiDB();
                } else {
                    
                    m_previousErrorId = getErrorId();
                    
                    setConnectionState(CONNECTION_DATABASE_FAILED);
                    m_connectionError = getTaskError();
                    
                    //JPM.TODO : WART if no user has been created
                    /*if ((m_connectionError!= null) && (m_connectionError.getErrorTitle().indexOf("dupierris")!=-1)) {
                        // we create the user dupierris
                        
                        //CreateProjectTask.postUserRequest();
                        
                        CreateUserTask task = new CreateUserTask(null, "dupierris");
                        AccessServiceThread.getAccessServiceThread().addTask(task);
                    }*/
                }
                
                if (connectionCallback != null) {
                    connectionCallback.run();
                }

            }
        };

        // ask asynchronous loading of data
        DatabaseConnectionTask connectionTask = new DatabaseConnectionTask(callback);
        connectionTask.initConnectionToUDS(databaseProperties, projectUser);
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
    
    public String getUserPassword() {
        return m_userPassword;
    }

    public void setServerURL(String serverURL) {
        m_serverURL = serverURL;
    }
    
    public void setProjectUser(String projectUser) {
        m_projectUser = projectUser;
    }
    
    public void setUserPassword(String userPassword) {
        m_userPassword = userPassword;
    }

    public void setPasswordNeeded(boolean passwordNeeded) {
        m_passwordNeeded = passwordNeeded;
    }
    
    public synchronized boolean isConnectionFailed() {
        return ((m_connectionState == CONNECTION_SERVER_FAILED) || (m_connectionState == CONNECTION_DATABASE_FAILED));
    }
    public synchronized boolean isConnectionAsked() {
        return ((m_connectionState == CONNECTION_SERVER_ASKED) || (m_connectionState == CONNECTION_DATABASE_ASKED));
    }
    public synchronized boolean isConnectionDone() {
        return (m_connectionState == CONNECTION_DONE);
    }
    public synchronized boolean isNotConnected() {
        return (m_connectionState == NOT_CONNECTED);
    }

    public synchronized void setConnectionState(int connectionState) {
        m_connectionState = connectionState;

    }

    public TaskError getConnectionError() {
        return m_connectionError;
    }

}
