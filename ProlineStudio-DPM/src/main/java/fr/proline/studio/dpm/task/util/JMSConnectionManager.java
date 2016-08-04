package fr.proline.studio.dpm.task.util;

import fr.proline.studio.dpm.jms.AccessJMSManagerThread;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.BackingStoreException;
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
    }
    
    protected static final Logger m_loggerProline = LoggerFactory.getLogger("ProlineStudio.DPM.Task");

    public static final String DEFAULT_SERVICE_REQUEST_QUEUE_NAME = "ProlineServiceRequestQueue";
    public static final String SERVICE_REQUEST_QUEUE_NAME_KEY = "JMSProlineQueueName";
    
    public static final String SERVICE_MONITORING_NOTIFICATION_TOPIC_NAME = "ProlineServiceMonitoringNotificationTopic";

    public static final String PROLINE_NODE_ID_KEY = "Proline_NodeId";

    public static final String PROLINE_SERVICE_NAME_KEY = "Proline_ServiceName";

    public static final String PROLINE_SERVICE_VERSION_KEY = "Proline_ServiceVersion";
    
    public static final String PROLINE_SERVICE_SOURCE_KEY = "Proline_ServiceSource";
    
    public static final String HORNET_Q_SAVE_STREAM_KEY = "JMS_HQ_SaveStream";
    
    public static final String HORNET_Q_INPUT_STREAM_KEY = "JMS_HQ_InputStream";
    
    
    public static final String PROLINE_PROCESS_METHOD_NAME = "process";
        
    public static final String PROLINE_USER_AUTHENTICATE_METHOD_NAME = "authenticate";
    
    public static final String PROLINE_GET_RSC_METHOD_NAME = "get_resource_as_stream";
    
    public static final String JMS_SERVER_HOST_PARAM_KEY = "jms.server.host";
    
    public static final String JMS_SERVER_PORT_PARAM_KEY = "jms.server.port";
    
        
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
        if(m_jmsConnectionManager == null){
            m_jmsConnectionManager = new JMSConnectionManager();
        }
        return m_jmsConnectionManager;
    }
    

    public void saveParameters() {
        if(m_serviceQueue != null) {
            Preferences preferences = NbPreferences.root();
            try {
                preferences.put(SERVICE_REQUEST_QUEUE_NAME_KEY, m_serviceQueue.getQueueName());
                preferences.flush();
            } catch (BackingStoreException | JMSException e) {
                LoggerFactory.getLogger("ProlineStudio.DPM").error("Saving Server Connection Parameters Failed", e);
            }
        }
    }
    
    public boolean isJMSDefined(){
        try {
            return (getJMSConnection() != null);
            
        } catch(Exception e){
            return false;
        }
    }
    
    /**
     * Set the JMS Server Host.
     * Connection and session will be reseted
     * @param jmsHost 
     */
    public void setJMSServerHost(String jmsHost ){
        m_jmsServerHost = jmsHost;
        m_connection = null;
        m_serviceQueue = null;        
    }
    
    /**
     * Set the JMS Server Port.
     * Connection and session will be reseted
     * @param jmsPort 
     */
    public void setJMSServerPort(int jmsPort){
        m_jmsServerPort = jmsPort;
        m_connection = null;
        m_serviceQueue = null;     
    }
    
    public Connection getJMSConnection() throws Exception {
            
        if(m_connection == null){
            try{
                createConnection();    
            }catch(Exception e){
                m_connection = null;
                m_serviceQueue = null;
                throw e;
            }            
        }        
        return m_connection;            
    }
    
    public Queue getServiceQueue() throws Exception {
        if(m_serviceQueue == null){
            try{
                createConnection();    
            }catch(Exception e){
                m_connection = null;
                m_serviceQueue = null;
                throw e;
            }
        }
        return m_serviceQueue;
    }
    
    private void createConnection() throws JMSException{
        try {
            if(m_jmsServerHost == null)
                throw new RuntimeException("JMS Host not defined ! ");
            
            // Step 1. Directly instantiate the JMS Queue object.
            //Get JMS Queue Name from preference 
            Preferences preferences = NbPreferences.root();
            String queueName =  preferences.get(SERVICE_REQUEST_QUEUE_NAME_KEY, DEFAULT_SERVICE_REQUEST_QUEUE_NAME);
            m_loggerProline.info(" Use JMS Queure "+queueName);
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
            // Step 6. Create the subscription and the subscriber.
	    m_topicSuscriber = m_topicSession.createConsumer(m_notificationTopic);
            m_notifListener = new ServiceNotificationListener();	 
            m_topicSuscriber.setMessageListener(m_notifListener); //TODO : listen only when asked !?
        }catch(JMSException je){
                
            if (m_connection != null) {
                try {
                    m_connection.close();
                    m_loggerProline.info("JMS Connection closed on error "+je.getMessage());
                } catch (Exception exClose) {
                    m_loggerProline.error("Error closing JMS Connection", exClose);
                }
            }            
            throw je;
        }
    }
       
    public ServiceNotificationListener getNotificationListener(){
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
    
    public JMSContext getJMSContext(){
        if(m_connection == null){
            try {
                createConnection();
            } catch (JMSException ex) {
               return null;
            }
        }
        return m_jmsContext;
    }
    
    public void closeConnection(){


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
        
        m_jmsServerHost = null;
        m_serviceQueue = null;

    }


}
