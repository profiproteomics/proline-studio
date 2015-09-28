package fr.proline.studio.dpm.task.util;

import java.util.HashMap;
import java.util.Map;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Queue;
import org.hornetq.api.core.TransportConfiguration;
import org.hornetq.api.jms.HornetQJMSClient;
import org.hornetq.api.jms.JMSFactoryType;
import org.hornetq.core.remoting.impl.netty.NettyConnectorFactory;
import org.hornetq.core.remoting.impl.netty.TransportConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 
 * @author VD225637
 */
public class JMSConnectionManager {
 
    private JMSConnectionManager() {
    }
    
    protected static final Logger m_loggerProline = LoggerFactory.getLogger("ProlineStudio.DPM.Task");

    public static final String SERVICE_REQUEST_QUEUE_NAME = "ProlineServiceRequestQueue";
  
    public static final String SERVICE_MONITORING_NOTIFICATION_TOPIC_NAME = "ProlineServiceMonitoringNotificationTopic";

    public static final String PROLINE_NODE_ID_KEY = "Proline_NodeId";

    public static final String PROLINE_SERVICE_NAME_KEY = "Proline_ServiceName";

    public static final String PROLINE_SERVICE_VERSION_KEY = "Proline_ServiceVersion";
    
    public static final String HORNET_Q_SAVE_STREAM_KEY = "JMS_HQ_SaveStream";
    
    public static final String PROLINE_PROCESS_METHOD_NAME = "process";
        
    public static final String PROLINE_USER_AUTHENTICATE_METHOD_NAME = "authenticate";
    
    public static final String PROLINE_GET_RSC_METHOD_NAME = "get_resource_as_stream";
    
    public static final String JMS_SERVER_HOST_PARAM_KEY = "jms.server.host";
    
    public static final String JMS_SERVER_PORT_PARAM_KEY = "jms.server.port";
    
        
    public String m_jmsServerHost = null;/* Default = HornetQ Proline Prod Grenoble = "132.168.72.129" */
    public int m_jmsServerPort = 5445;
    
    private Connection m_connection = null;
    private Queue m_serviceQueue = null;
    
    private static JMSConnectionManager m_jmsConnectionManager = null;
    
    public static synchronized JMSConnectionManager getJMSConnectionManager() {
        if(m_jmsConnectionManager == null){
            m_jmsConnectionManager = new JMSConnectionManager();
        }
        return m_jmsConnectionManager;
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
    
    public Connection getJMSConnection() {
            
        if(m_connection == null){
            try{
                createConnection();    
            }catch(Exception e){
                 m_connection = null;
            }
            
        }
        
        return m_connection;            
    }
    
    public Queue getServiceQueue(){
        if(m_serviceQueue == null){
            try{
                createConnection();    
            }catch(Exception e){
                m_connection = null;
                m_serviceQueue = null;
            }
        }
        return m_serviceQueue;
    }
    
    private void createConnection(){
        try {
            if(m_jmsServerHost == null)
                throw new RuntimeException("JMS Host not defined ! ");
            
            // Step 1. Directly instantiate the JMS Queue object.
	    m_serviceQueue = HornetQJMSClient.createQueue(SERVICE_REQUEST_QUEUE_NAME);

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
            
	    // Step 4.Create a JMS Connection
	    m_connection = cf.createConnection();
        }catch(JMSException je){
                
            if (m_connection != null) {
                try {
                    m_connection.close();
                    m_loggerProline.info("JMS Connection closed on error "+je.getMessage());
                } catch (Exception exClose) {
                    m_loggerProline.error("Error closing JMS Connection", exClose);
                }
            }
        }
    }
    
    public void closeConnection(){


        if (m_connection != null) {           
            try {
                m_connection.close();
                m_loggerProline.info("JMS Connection closed");
            } catch (Exception exClose) {
                m_loggerProline.error("Error closing JMS Connection", exClose);
            }
        }
	
    }


}
