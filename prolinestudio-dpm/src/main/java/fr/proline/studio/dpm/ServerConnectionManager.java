/* 
 * Copyright (C) 2019 VD225637
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1 
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License 
 * along with this program; If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */
package fr.proline.studio.dpm;

import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.DatabaseDataManager;
import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.DatabaseConnectionTask;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.dpm.task.jms.AbstractJMSCallback;
import fr.proline.studio.dpm.task.jms.AuthenticateUserTask;
import fr.proline.studio.dpm.task.jms.GetDBConnectionTemplateTask;
import fr.proline.studio.dpm.task.util.JMSConnectionManager;
import java.util.HashMap;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Management of the connection to the Jetty Server and to the databases
 *
 * @author jm235353
 */
public class ServerConnectionManager {

    private static final String KEY_SERVER_URL = "serverURL";
    private static final String KEY_PROJECT_USER = "projectUser";
    private static final String KEY_USER_PASSWORD = "databasePassword";
    private static final String KEY_PASSWORD_NEEDED = "passwordNeeded";

    public static final int NOT_CONNECTED = 0;
    public static final int CONNECTION_ASKED = 1;
    public static final int CONNECTION_FAILED = 2;
    public static final int CONNECTION_DONE = 3;

    protected static final Logger m_loggerProline = LoggerFactory.getLogger("ProlineStudio.DPM.Task");
    
    private int m_connectionState = NOT_CONNECTED;

    private TaskError m_connectionError = null;

    private String m_serverURL;
    private String m_projectUser;
    private String m_databasePassword;
    private String m_userPassword;
    private boolean m_passwordNeeded;

    private HashMap<Object, Object> m_serverConnectionParam;

    private static ServerConnectionManager m_connectionManager = null;

    public static synchronized ServerConnectionManager getServerConnectionManager() {
        if (m_connectionManager == null) {
            m_connectionManager = new ServerConnectionManager();
        }
        return m_connectionManager;
    }

    public ServerConnectionManager() {
        restoreParameters();        
    }

    private void restoreParameters() {
        Preferences preferences = NbPreferences.root();

        m_serverURL = preferences.get(KEY_SERVER_URL, "");
        m_projectUser = preferences.get(KEY_PROJECT_USER, "");
        m_userPassword = preferences.get(KEY_USER_PASSWORD, "");

        m_passwordNeeded = preferences.getBoolean(KEY_PASSWORD_NEEDED, false);
        //m_jmsServer = preferences.getBoolean(KEY_IS_JMSSERVER, false);
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
        JMSConnectionManager.getJMSConnectionManager().saveParameters();
    }


    public void tryServerConnection(final Runnable connectionCallback, final String serverURL, final String projectUser, String userPassword, final boolean changingUser) {

        
        m_passwordNeeded = !userPassword.isEmpty();

        // pre-check to avoid to try a connection when the parameters are not set
        if (serverURL.isEmpty()) {
            setConnectionState(CONNECTION_FAILED);
            m_connectionError = new TaskError("JMS Server Connection", "Empty Server Host");
            return;
        }

        // reconnect even if changing user
        JMSConnectionManager.getJMSConnectionManager().closeConnection();

        setConnectionState(CONNECTION_ASKED);

        //Configure JMSConnectionManager and try to connect to server
        try {
            String[] jmsHostAndPort = parseJMSServerURL(serverURL);
            JMSConnectionManager.getJMSConnectionManager().setJMSServerHost(jmsHostAndPort[0]);
            if (jmsHostAndPort[1] != null) {
                JMSConnectionManager.getJMSConnectionManager().setJMSServerPort(Integer.parseInt(jmsHostAndPort[1]));
            }
            //Try to connect to server
            JMSConnectionManager.getJMSConnectionManager().getJMSConnection();
        } catch (Exception e) {
            setConnectionState(CONNECTION_FAILED);
            JMSConnectionManager.getJMSConnectionManager().closeConnection();
            m_connectionError = new TaskError(e.getMessage());

            if (connectionCallback != null) {
                connectionCallback.run();
            }

            return;
            //throw new RuntimeException("Error creating connection to JMS Server "+e.getMessage());
        }


        userAuthenticateJMS(connectionCallback, serverURL, projectUser, userPassword, changingUser);
    }

    private String[] parseJMSServerURL(String serverURL) {
        String[] hostAndPort = new String[2];

        String parsedURL = serverURL;
        int portSep = parsedURL.indexOf(":");
        if (portSep > 0) {
            hostAndPort[0] = parsedURL.substring(0, portSep);
            hostAndPort[1] = parsedURL.substring(portSep + 1);
        } else {
            hostAndPort[0] = parsedURL;
        }

        return hostAndPort;
    }

    private void userAuthenticateJMS(final Runnable connectionCallback, final String serverURL, final String projectUser, String userPassword, final boolean changingUser) {
        final String[] databasePassword = new String[1];
        AbstractJMSCallback callback = new AbstractJMSCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success) {
                if (success) {
                    m_serverURL = serverURL;
                    if (changingUser) { // No need to get DBConnection (use previous one). skip further steps
                        // save connection parameters
                        saveParameters();
                        setConnectionState(CONNECTION_DONE);

                        m_serverURL = serverURL;
                        
                        if (connectionCallback != null) {
                            connectionCallback.run();
                        }
                    } else {
                        // we now try to connect to the server
                        m_databasePassword = databasePassword[0];
                        getDBConnectionTemplate(connectionCallback, projectUser, m_databasePassword);
                    }
                } else {
                    setConnectionState(CONNECTION_FAILED);
                    m_connectionError = getTaskError();
                    JMSConnectionManager.getJMSConnectionManager().closeConnection();
                    if (connectionCallback != null) {
                        connectionCallback.run();
                    }
                }
            }
        };
        m_loggerProline.debug(" ---- WILL RUN AuthenticateUserTask ");
        AuthenticateUserTask task = new AuthenticateUserTask(callback, projectUser, userPassword, databasePassword);
        AccessJMSManagerThread.getAccessJMSManagerThread().addTask(task);

    }


    private void getDBConnectionTemplate(final Runnable connectionCallback, final String projectUser, final String databasePassword) {

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
                    tryDatabaseConnection(connectionCallback, databaseProperties, projectUser);
                } else {
                    setConnectionState(CONNECTION_FAILED);
                    m_connectionError = getTaskError();
                    if (connectionCallback != null) {
                        connectionCallback.run();
                    }
                }
            }
        };
        m_loggerProline.debug(" ---- WILL RUN GetDBConnectionTemplateTask with  "+databasePassword);
        GetDBConnectionTemplateTask task = new GetDBConnectionTemplateTask(callback, databasePassword, databaseProperties);
        AccessJMSManagerThread.getAccessJMSManagerThread().addTask(task);
    }

    private void tryDatabaseConnection(final Runnable connectionCallback, HashMap<Object, Object> databaseProperties, String projectUser) {

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
                    m_serverConnectionParam = databaseProperties;
                    DatabaseDataManager.getDatabaseDataManager().setServerConnectionProperties(m_serverConnectionParam);
                    saveParameters();
                    setConnectionState(CONNECTION_DONE);

                } else {

                    setConnectionState(CONNECTION_FAILED);
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

    public HashMap<Object, Object> getConnectionParams() {
        return m_serverConnectionParam;
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
        return (m_connectionState == CONNECTION_FAILED);
    }

    public synchronized boolean isConnectionAsked() {
        return (m_connectionState == CONNECTION_ASKED);
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
