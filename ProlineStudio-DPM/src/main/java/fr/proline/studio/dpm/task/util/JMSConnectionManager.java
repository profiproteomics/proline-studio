package fr.proline.studio.dpm.task.util;

import fr.proline.studio.dpm.jms.AccessJMSManagerThread;
import fr.proline.studio.parameter.ParameterList;
import fr.proline.studio.parameter.StringParameter;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.Session;
import javax.jms.Topic;
import javax.swing.JTextField;
import javax.swing.event.EventListenerList;
import org.hornetq.api.core.TransportConfiguration;
import org.hornetq.api.jms.HornetQJMSClient;
import org.hornetq.api.jms.JMSFactoryType;
import org.hornetq.core.remoting.impl.netty.NettyConnectorFactory;
import org.hornetq.core.remoting.impl.netty.TransportConstants;
import org.openide.util.NbPreferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author VD225637
 */
public class JMSConnectionManager {

    private JMSConnectionManager() {
        listenersList = new EventListenerList();
        connectionState = ConnectionListener.NOT_CONNECTED;
    }
    
    private ParameterList m_parameterList;
    private static final String JMS_SETTINGS = "JMS Settings";

    protected static final Logger m_loggerProline = LoggerFactory.getLogger("ProlineStudio.DPM.Task");

    public static final String DEFAULT_SERVICE_REQUEST_QUEUE_NAME = "ProlineServiceRequestQueue";

    public static final String SERVICE_REQUEST_QUEUE_NAME_KEY = "JMSProlineQueueName";

    public static final String SERVICE_MONITORING_NOTIFICATION_TOPIC_NAME = "ProlineServiceMonitoringNotificationTopic";

    public static final String PROLINE_NODE_ID_KEY = "Proline_NodeId";

    public static final String PROLINE_SERVICE_NAME_KEY = "Proline_ServiceName";

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

    private final EventListenerList listenersList;
    private int connectionState;

    public String m_jmsServerHost = null;/* Default = HornetQ Proline Prod Grenoble = "132.168.72.129" */

    public int m_jmsServerPort = 5445;

    private Connection m_connection = null;
    private Queue m_serviceQueue = null;
    private Topic m_notificationTopic = null;
    private Session m_topicSession = null;
    private JMSContext m_jmsContext = null;

    private QueueBrowser m_browser = null;
    private ServiceNotificationListener m_notifListener = null;
    private MessageConsumer m_topicSuscriber;
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
            /*
            try {
                preferences.put(SERVICE_REQUEST_QUEUE_NAME_KEY, m_serviceQueue.getQueueName());
                preferences.flush();
            } catch (BackingStoreException | JMSException e) {
                LoggerFactory.getLogger("ProlineStudio.DPM").error("Saving Server Connection Parameters Failed", e);
            }
            */
            m_parameterList.saveParameters(preferences, true);
        }
    }

    public boolean isJMSDefined() {
        try {
            return (getJMSConnection() != null);

        } catch (Exception e) {
            return false;
        }
    }

    public int getConnectionState() {
        return connectionState;
    }

    private void resetConnObjects() {
        m_connection = null;
        m_serviceQueue = null;
        m_notificationTopic = null;
        m_topicSession = null;
        m_jmsContext = null;
        m_browser = null;
        m_notifListener = null;
        m_topicSuscriber = null;
        connectionState = ConnectionListener.NOT_CONNECTED;
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

    private void createConnection() throws JMSException {
        try {
            if (m_jmsServerHost == null) {
                throw new RuntimeException("JMS Host not defined ! ");
            }

            // Step 1. Directly instantiate the JMS Queue object.
            //Get JMS Queue Name from preference 
            
            m_parameterList = new ParameterList(JMS_SETTINGS);
            StringParameter m_parameter = new StringParameter(JMSConnectionManager.SERVICE_REQUEST_QUEUE_NAME_KEY, "JMSProlineQueueName", JTextField.class, DEFAULT_SERVICE_REQUEST_QUEUE_NAME, 5, null);
            m_parameterList.add(m_parameter);
            m_parameterList.loadParameters(NbPreferences.root(), true);
            String queueName = m_parameter.getStringValue();

            m_loggerProline.info(" Use JMS Queure " + queueName);
            m_serviceQueue = HornetQJMSClient.createQueue(queueName);
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
            final ConnectionFactory cf = (ConnectionFactory) HornetQJMSClient
                    .createConnectionFactoryWithoutHA(JMSFactoryType.CF, transportConfiguration);

            // Create a JMS v 2.0 Context
            m_jmsContext = cf.createContext();

            // Step 4.Create a JMS Connection
            m_connection = cf.createConnection();

            // Step 5. Create a JMS Session (Session MUST be confined in current Thread)
            // Not transacted, AUTO_ACKNOWLEDGE
            m_topicSession = m_connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            // Step 6. Create the subscription and the subscriber.//TODO : create & listen only when asked !?
            m_topicSuscriber = m_topicSession.createConsumer(m_notificationTopic);
            m_notifListener = new ServiceNotificationListener();
            m_topicSuscriber.setMessageListener(m_notifListener);
            connectionState = ConnectionListener.CONNECTION_DONE;
            fireConnectionStateChanged(ConnectionListener.CONNECTION_DONE);
        } catch (JMSException je) {
            if (m_connection != null) {
                try {
                    m_connection.close();
                    m_loggerProline.info("JMS Connection closed on error " + je.getMessage());
                } catch (Exception exClose) {
                    m_loggerProline.error("Error closing JMS Connection", exClose);
                } finally {
                    resetConnObjects();
                }
            }
            connectionState = ConnectionListener.CONNECTION_FAILED;
            fireConnectionStateChanged(ConnectionListener.CONNECTION_FAILED);
            throw je;
        }
    }

    public ServiceNotificationListener getNotificationListener() {
        return m_notifListener;
    }

    public QueueBrowser getQueueBrowser() {
        if (m_browser == null) {
            JMSContext jmsCtxt = getJMSContext();
            if (jmsCtxt != null) {
                m_browser = jmsCtxt.createBrowser(m_serviceQueue);
            }
        }
        return m_browser;
    }

    public JMSContext getJMSContext() {
        if (m_connection == null) {
            try {
                createConnection();
            } catch (JMSException ex) {
                return null;
            }
        }
        return m_jmsContext;
    }

    public void closeConnection() {
        if (m_connection != null) {
            try {

                // need to cleanup jms thread
                AccessJMSManagerThread.getAccessJMSManagerThread().cleanup();
                m_topicSuscriber.close();
                m_topicSession.close();
                m_connection.close();

                m_loggerProline.info("JMS Connection closed");
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
        synchronized (listenersList) {
            listenersList.add(ConnectionListener.class, listener);
        }
    }

    public void removeConnectionListener(ConnectionListener listener) {
        synchronized (listenersList) {
            listenersList.remove(ConnectionListener.class, listener);
        }
    }

    protected void fireConnectionStateChanged(int newConnState) {
        synchronized (listenersList) {
            ConnectionListener[] allListeners = listenersList.getListeners(ConnectionListener.class);
            for (ConnectionListener nextOne : allListeners) {
                nextOne.connectionStateChanged(newConnState);
            }
        }
    }

}
