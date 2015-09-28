package fr.proline.studio.dpm;

import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseConnectionTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.dpm.jms.AccessJMSManagerThread;
import fr.proline.studio.dpm.task.AbstractServiceCallback;
import fr.proline.studio.dpm.task.ServerConnectionTask;
import fr.proline.studio.dpm.task.UserAccountTask;
import fr.proline.studio.dpm.task.jms.AbstractJMSCallback;
import fr.proline.studio.dpm.task.jms.AuthenticateUserTask;
import fr.proline.studio.dpm.task.jms.GetDBConnectionTemplateTask;
import fr.proline.studio.dpm.task.util.JMSConnectionManager;
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
    private static final String KEY_IS_JMSSERVER = "isJMSServer";
        
    public static final int NOT_CONNECTED = 0;
    public static final int CONNECTION_SERVER_ASKED = 1;
    public static final int CONNECTION_SERVER_FAILED = 2;
    public static final int CONNECTION_DATABASE_ASKED = 3;
    public static final int CONNECTION_DATABASE_FAILED = 4;
    public static final int CONNECTION_DONE = 5;
    
    private static final String HTTP_URL_PREFFIX = "http://";
    
    private int m_connectionState = NOT_CONNECTED;    
    private TaskError m_connectionError = null;
    
    private String m_serverURL;
    private String m_projectUser;
    private String m_databasePassword;
    private String m_userPassword;
    private boolean m_passwordNeeded;

    private boolean m_jmsServer;
    
    
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

        m_serverURL = preferences.get(KEY_SERVER_URL, HTTP_URL_PREFFIX);
        m_projectUser = preferences.get(KEY_PROJECT_USER, "");
        m_userPassword = preferences.get(KEY_USER_PASSWORD, "");
        
        m_passwordNeeded = preferences.getBoolean(KEY_PASSWORD_NEEDED, false);
        m_jmsServer = preferences.getBoolean(KEY_IS_JMSSERVER, false);
    }
   
    public void saveParameters() {
        Preferences preferences = NbPreferences.root();
        
        preferences.put(KEY_SERVER_URL, m_serverURL);
        preferences.put(KEY_PROJECT_USER, m_projectUser);
        preferences.put(KEY_USER_PASSWORD, m_userPassword);

        preferences.putBoolean(KEY_PASSWORD_NEEDED, m_passwordNeeded);
        preferences.putBoolean(KEY_IS_JMSSERVER, m_jmsServer);
        
        try {
            preferences.flush();
        } catch (BackingStoreException e) {
            LoggerFactory.getLogger("ProlineStudio.DPM").error("Saving Server Connection Parameters Failed", e);
        }
    }
   
   private void tryServerConnection() {
       tryServerConnection(null, m_serverURL, m_projectUser, m_userPassword, false, m_jmsServer);
   }
   public void tryServerConnection(final Runnable connectionCallback, final String serverURL, final String projectUser, String userPassword, final boolean changingUser, final boolean isJMSServer) {

       // pre-check to avoid to try a connection when the parameters are not set
       if (!isJMSServer && serverURL.length()<=HTTP_URL_PREFFIX.length()) {
           setConnectionState(CONNECTION_SERVER_FAILED);
           return;
       }
       if (isJMSServer && serverURL.isEmpty()) {
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
       m_jmsServer = isJMSServer;
       setConnectionState(CONNECTION_SERVER_ASKED);
       
       
       // First, we check the user password      
       if (isJMSServer) {
           //Configure JMSConnectionManager
           try {
               String[] jmsHostAndPort = parseJMSServerURL(serverURL);
               JMSConnectionManager.getJMSConnectionManager().setJMSServerHost(jmsHostAndPort[0]);
               if (jmsHostAndPort[1] != null) {
                   JMSConnectionManager.getJMSConnectionManager().setJMSServerPort(Integer.parseInt(jmsHostAndPort[1]));
               }
           }catch(Exception e){
               throw new RuntimeException("Error creating connection to JMS Server "+e.getMessage());
           }
           
           userAuthenticateJMS(connectionCallback, projectUser, userPassword, changingUser);
       } else {
           userAuthenticateWC(connectionCallback, serverURL, projectUser, userPassword, changingUser);
       } 
 
   }
   
   private String[] parseJMSServerURL(String serverURL) {
       String[] hostAndPort = new String[2];
       
       String parsedURL =  (serverURL.startsWith(HTTP_URL_PREFFIX)) ? serverURL.substring(HTTP_URL_PREFFIX.length()): serverURL;
       
       int portSep = parsedURL.indexOf(":");
       if(portSep > 0){
           hostAndPort[0] = parsedURL.substring(0,portSep);           
           hostAndPort[1] = parsedURL.substring(portSep+1);           
       }else{
           hostAndPort[0] = parsedURL;
       }
       
       return hostAndPort;
   }
   
    private void userAuthenticateJMS(final Runnable connectionCallback, final String projectUser, String userPassword, final boolean changingUser) {
        final String[] databasePassword = new String[1];
        AbstractJMSCallback callback = new AbstractJMSCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success) {
                if (success) {
                    // we now try to connect to the server
                    m_databasePassword = databasePassword[0];
                    tryConnectToJMSServer(connectionCallback, projectUser,  m_databasePassword, changingUser);

                } else {
                    setConnectionState(CONNECTION_SERVER_FAILED);
                    m_connectionError = getTaskError();
                    if (connectionCallback != null) {
                        connectionCallback.run();
                    }
                }
            }
        };
        AuthenticateUserTask task = new AuthenticateUserTask(callback, projectUser, userPassword, databasePassword);
        AccessJMSManagerThread.getAccessJMSManagerThread().addTask(task);

    }
   
   private void userAuthenticateWC(final Runnable connectionCallback, final String serverURL, final String projectUser, String userPassword, final boolean changingUser){
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
                   tryConnectToWCServer(connectionCallback, projectUser, serverURL, m_databasePassword, changingUser);
                   
                   
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
   
   
   private void tryConnectToWCServer(final Runnable connectionCallback, final String projectUser, final String serverURL, final String databasePassword, final boolean changingUser) {
       
       if (changingUser) {
           // we now ask for the database connection
           tryDatabaseConnection(connectionCallback, null, projectUser, changingUser);
           return;
       }
       
       final HashMap<Object, Object> databaseProperties = new HashMap<>();
       
       //  we try to connect to the service 
       AbstractServiceCallback callback = new AbstractServiceCallback() {

           @Override
           public boolean mustBeCalledInAWT() {
               return true;
           }

           @Override
           public void run(boolean success) {
               if (success) {
//                    JMSConnectionManager.getJMSConnectionManager().setJMSServerHost((String)databaseProperties.get(JMSConnectionManager.JMS_SERVER_HOST_PARAM_KEY));
//                    if(databaseProperties.containsKey(JMSConnectionManager.JMS_SERVER_PORT_PARAM_KEY))
//                        JMSConnectionManager.getJMSConnectionManager().setJMSServerPort((Integer)databaseProperties.get(JMSConnectionManager.JMS_SERVER_PORT_PARAM_KEY));
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
   
      private void tryConnectToJMSServer(final Runnable connectionCallback, final String projectUser,  final String databasePassword, final boolean changingUser) {
       
       if (changingUser) {
           // we now ask for the database connection
           tryDatabaseConnection(connectionCallback, null, projectUser, changingUser);
           return;
       }
       
       final HashMap<Object, Object> databaseProperties = new HashMap<>();
       
       //  we try to connect to the service 
       AbstractJMSCallback callback = new AbstractJMSCallback() {

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


       GetDBConnectionTemplateTask task = new GetDBConnectionTemplateTask(callback, databasePassword, databaseProperties);
       AccessJMSManagerThread.getAccessJMSManagerThread().addTask(task);
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
    
    public Boolean isJMSServer(){
        return m_jmsServer;
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
    
    public void setIsJMSServer(boolean isJMSServer){
        m_jmsServer = isJMSServer;
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
