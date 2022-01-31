/*
 * Copyright (C) 2019
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
package fr.proline.studio.dpm.task.util;

import fr.proline.studio.NbPreferences;
import fr.proline.studio.dpm.AccessJMSManagerThread;
import fr.proline.studio.parameter.ParameterList;
import fr.proline.studio.parameter.StringParameter;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;
import javax.jms.*;
import javax.swing.JTextField;
import javax.swing.event.EventListenerList;
import org.hornetq.api.core.TransportConfiguration;
import org.hornetq.api.jms.HornetQJMSClient;
import org.hornetq.api.jms.JMSFactoryType;
import org.hornetq.core.remoting.impl.netty.NettyConnectorFactory;
import org.hornetq.core.remoting.impl.netty.TransportConstants;
import org.hornetq.jms.client.HornetQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author VD225637
 */
public class JMSConnectionManager {

    private JMSConnectionManager() {
        m_connectionListenersList = new EventListenerList();
        m_connectionState = ConnectionListener.NOT_CONNECTED;
    }
    
    // paramaters management
    private ParameterList m_jmsSettingsPList;
    public static final String JMS_SETTINGS_PARAMETER_LIST_KEY = "JMS";

    protected static final Logger m_loggerProline = LoggerFactory.getLogger("ProlineStudio.DPM.Task");

    //JMS Constants
    public static final String DEFAULT_SERVICE_REQUEST_QUEUE_NAME = "ProlineServiceRequestQueue";

    public static final String SERVICE_REQUEST_QUEUE_NAME_KEY = "JMSProlineQueueName";

    public static final String SERVICE_MONITORING_NOTIFICATION_TOPIC_NAME = "ProlineServiceMonitoringNotificationTopic";

    public static final String PROLINE_NODE_ID_KEY = "Proline_NodeId";

    public static final String PROLINE_SERVICE_NAME_KEY = "Proline_ServiceName";

    public static final String PROLINE_SERVICE_PROJECT_ID_KEY = "Proline_Service_ProjectId";

    public static final String PROLINE_SERVICE_VERSION_KEY = "Proline_ServiceVersion";

    public static final String PROLINE_SERVICE_SOURCE_KEY = "Proline_ServiceSource";

    public static final String PROLINE_SERVICE_DESCR_KEY = "Proline_ServiceDescription";

    public static final String HORNET_Q_SAVE_STREAM_KEY = "JMS_HQ_SaveStream";

    public static final String HORNET_Q_INPUT_STREAM_KEY = "JMS_HQ_InputStream";

    public static final String PROLINE_PROCESS_METHOD_NAME = "process";

    public static final String PROLINE_USER_AUTHENTICATE_METHOD_NAME = "authenticate";

    public static final String PROLINE_GET_RSC_METHOD_NAME = "get_resource_as_stream";

    public static final String JMS_SERVER_HOST_PARAM_KEY = "jms.server.host";

    public static final String JMS_SERVER_PORT_PARAM_KEY = "jms.server.port";

    public static final int JMS_CANCELLED_TASK_ERROR_CODE = -32004;

    public static final int JMS_EXPIRED_MSG_ERROR_CODE = -32003;
    
    private final EventListenerList m_connectionListenersList;//ConnectionListeners list
    private int m_connectionState;//Connection current state

    public String m_jmsServerHost = null;
    public static int m_jmsServerPort = 5445;

    //Queue objects
    private Connection m_connection = null;
    private Queue m_serviceQueue = null;

    private Session m_mainSession = null;

    //Topic objects
    private TopicConnection m_topicConnection = null;
    private TopicSession m_topicSession = null;
    private Topic m_notificationTopic = null;
    private ServiceNotificationListener m_notifListener = null;
    private MessageConsumer m_topicSuscriber;
    private QueueBrowser m_browser = null;

    private static JMSConnectionManager m_jmsConnectionManager = null;

    public static synchronized JMSConnectionManager getJMSConnectionManager() {
        if (m_jmsConnectionManager == null) {
            m_jmsConnectionManager = new JMSConnectionManager();
        }
        return m_jmsConnectionManager;
    }

    public void saveParameters() {
        if (m_serviceQueue != null) {
            Preferences preferences = NbPreferences.root();
            m_jmsSettingsPList.saveParameters(preferences);
        }
    }


    public int getConnectionState() {
        return m_connectionState;
    }

    private void resetConnObjects() {
        m_connection = null;
        m_topicConnection = null;
        m_serviceQueue = null;
        m_notificationTopic = null;
        m_mainSession = null;
        m_topicSession = null;
        m_browser = null;
        m_notifListener = null;
        m_topicSuscriber = null;
        m_connectionState = ConnectionListener.NOT_CONNECTED;
        fireConnectionStateChanged(ConnectionListener.NOT_CONNECTED);
    }

    /**
     * Set the JMS Server Host. Connection and session will be reseted
     *
     * @param jmsHost
     */
    public void setJMSServerHost(String jmsHost) {
        m_jmsServerHost = jmsHost;
        resetConnObjects();
    }

    /**
     * Set the JMS Server Port. Connection and session will be reseted
     *
     * @param jmsPort
     */
    public void setJMSServerPort(int jmsPort) {
        m_jmsServerPort = jmsPort;
        resetConnObjects();
    }

    /**
     * Get Studio JMS Connection. Create one if necessary
     * @return
     * @throws Exception 
     */
    public Connection getJMSConnection() throws Exception {

        if (m_connection == null) {
            try {
                createConnection();
            } catch (Exception e) {
                throw e;
            }
        }
        return m_connection;
    }

    /**
     * Get Proline Server servuce Queue, create JMS connection if necessary
     * @return
     * @throws Exception 
     */
    public Queue getServiceQueue() throws Exception {
        if (m_serviceQueue == null) {
            try {
                createConnection();
            } catch (Exception e) {
                throw e;
            }
        }
        return m_serviceQueue;
    }

    public  void  startJMSConnections() throws Exception {
        getJMSConnection().start();
        m_topicConnection.start();
    }

    private void createConnection() throws JMSException {
        try {
            if (m_jmsServerHost == null) {
                throw new RuntimeException("JMS Host not defined ! ");
            }

            // Step 1. Directly instantiate the JMS Queue object.
            //Get JMS Queue Name from preference 
            
            m_jmsSettingsPList = new ParameterList(JMS_SETTINGS_PARAMETER_LIST_KEY);
            StringParameter m_parameter = new StringParameter(JMSConnectionManager.SERVICE_REQUEST_QUEUE_NAME_KEY, "JMSProlineQueueName", JTextField.class, DEFAULT_SERVICE_REQUEST_QUEUE_NAME, 5, null);
            m_jmsSettingsPList.add(m_parameter);
            m_jmsSettingsPList.loadParameters(NbPreferences.root());
            String queueName = m_parameter.getStringValue();

            m_loggerProline.info(" Use JMS Queue " + queueName+" and JMS topic "+SERVICE_MONITORING_NOTIFICATION_TOPIC_NAME);
            //Queue Data
            m_serviceQueue = HornetQJMSClient.createQueue(queueName);

            //Topic data
            m_notificationTopic = HornetQJMSClient.createTopic(SERVICE_MONITORING_NOTIFICATION_TOPIC_NAME);

            // Step 2. Instantiate the TransportConfiguration object which contains the knowledge of what
            // transport to use, the server port etc.
            final Map<String, Object> connectionParams = new HashMap<>();
            /* JMS Server hostname or IP */
            connectionParams.put(TransportConstants.HOST_PROP_NAME, m_jmsServerHost);
            /* JMS port */
            connectionParams.put(TransportConstants.PORT_PROP_NAME, Integer.valueOf(m_jmsServerPort));

            final TransportConfiguration transportConfiguration = new TransportConfiguration(
                    NettyConnectorFactory.class.getName(), connectionParams);

            // Step 3 Directly instantiate the JMS ConnectionFactory object using that TransportConfiguration
            final HornetQConnectionFactory cf = (HornetQConnectionFactory) HornetQJMSClient
                    .createConnectionFactoryWithoutHA(JMSFactoryType.CF, transportConfiguration);
            cf.setReconnectAttempts(10);
            
            // Step 4.Create a JMS Connection
            m_connection = cf.createConnection();
            m_topicConnection = cf.createTopicConnection();

            // Step 5. Create a JMS Session (Session MUST be confined in current Thread)
            // Not transacted, AUTO_ACKNOWLEDGE
            m_mainSession = m_connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            m_topicSession = m_topicConnection.createTopicSession(false,Session.AUTO_ACKNOWLEDGE);

            // Step 6. Create the subscription and the subscriber.//TODO : create & listen only when asked !?
            m_topicSuscriber = m_topicSession.createConsumer(m_notificationTopic);
            m_notifListener = new ServiceNotificationListener();
            m_topicSuscriber.setMessageListener(m_notifListener);
            m_connectionState = ConnectionListener.CONNECTION_DONE;
            fireConnectionStateChanged(ConnectionListener.CONNECTION_DONE);
        } catch (RuntimeException | JMSException je) {
            if (m_connection != null || m_topicConnection != null) {
                try {
                    if(m_connection != null)
                        m_connection.close();
                    m_loggerProline.info("JMS Connection closed on error " + je.getMessage());
                    if(m_topicConnection != null)
                        m_topicConnection.close();
                    m_loggerProline.info("JMS Topic Connection closed on error " + je.getMessage());
                } catch (Exception exClose) {
                    m_loggerProline.error("Error closing JMS [Topic] Connection", exClose);
                } finally {
                    resetConnObjects();
                }
            }
            m_connectionState = ConnectionListener.CONNECTION_FAILED;
            fireConnectionStateChanged(ConnectionListener.CONNECTION_FAILED);
            throw je;
        }
    }

    public ServiceNotificationListener getNotificationListener() {
        return m_notifListener;
    }

    /**
     * Get Proline Server service Queue QueueBrowser, create JMS connection if necessary
     * @return
     * @throws Exception 
     */
    public QueueBrowser getQueueBrowser() {
        if (m_browser == null) {
            if(m_mainSession == null){
                try {
                    createConnection();
                } catch (JMSException ex) {
                    return null;
                }
            }

            try {
                m_browser = m_mainSession.createBrowser(m_serviceQueue);
            } catch (JMSException ex) {
                ex.printStackTrace();

                m_browser = null;
            }
        }
        return m_browser;
    }

    public void closeConnection() {
        if (m_connection != null) {
            try {

                // need to cleanup jms thread
                AccessJMSManagerThread.getAccessJMSManagerThread().cleanup();
                m_mainSession.close();
                m_connection.close();

                m_loggerProline.info("JMS Connection closed");
                if(m_topicConnection != null) {
                    m_topicSuscriber.close();
                    m_topicSession.close();
                    m_topicConnection.close();
                }

            } catch (Exception exClose) {
                m_loggerProline.error("Error closing JMS Connection", exClose);
            } finally {
                m_connection = null;
            }
        }
        resetConnObjects();
        m_jmsServerHost = null;
    }

    //Listener Methods
    public void addConnectionListener(ConnectionListener listener) {
        synchronized (m_connectionListenersList) {
            m_connectionListenersList.add(ConnectionListener.class, listener);
        }
    }

    public void removeConnectionListener(ConnectionListener listener) {
        synchronized (m_connectionListenersList) {
            m_connectionListenersList.remove(ConnectionListener.class, listener);
        }
    }

    protected void fireConnectionStateChanged(int newConnState) {
        synchronized (m_connectionListenersList) {
            ConnectionListener[] allListeners = m_connectionListenersList.getListeners(ConnectionListener.class);
            for (ConnectionListener nextOne : allListeners) {
                nextOne.connectionStateChanged(newConnState);
            }
        }
    }

}
