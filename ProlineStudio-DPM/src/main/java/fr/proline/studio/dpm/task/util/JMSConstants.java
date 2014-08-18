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
 * TO TRANSFORM TO JMSMANAGER ... 
 * @author VD225637
 */
public class JMSConstants {
 
    private JMSConstants() {
    }
    protected static final Logger m_loggerProline = LoggerFactory.getLogger("ProlineStudio.DPM.Task");

    /* HornetQ Proline Prod Grenoble */
    public static final String JMS_SERVER_HOST = "132.168.72.129";

    public static final int JMS_SERVER_PORT = 5445;

    public static final String SERVICE_REQUEST_QUEUE_NAME = "ProlineServiceRequestQueueVDS";

    public static final String SERVICE_MONITORING_NOTIFICATION_TOPIC_NAME = "ProlineServiceMonitoringNotificationTopic";

    public static final String PROLINE_NODE_ID_KEY = "Proline_NodeId";

    public static final String PROLINE_SERVICE_NAME_KEY = "Proline_ServiceName";

    public static final String PROLINE_SERVICE_VERSION_KEY = "Proline_ServiceVersion";
    
    private static Connection connection = null;
    private static Queue serviceQueue = null;
    
    public static Connection getJMSConnection(){
        if(connection == null){
            createConnection();
        }
        
        return connection;            
    }
    
    public static Queue getServiceQueue(){
        if(serviceQueue == null){
            createConnection();
        }
        return serviceQueue;
    }
    
    private static void createConnection(){
                    try {
            // Step 1. Directly instantiate the JMS Queue object.
	    serviceQueue = HornetQJMSClient.createQueue(SERVICE_REQUEST_QUEUE_NAME);

	    // Step 2. Instantiate the TransportConfiguration object which contains the knowledge of what
	    // transport to use, the server port etc.
	    final Map<String, Object> connectionParams = new HashMap<>();
	    /* JMS Server hostname or IP */
	    connectionParams.put(TransportConstants.HOST_PROP_NAME, JMS_SERVER_HOST);
	    /* JMS port */
	    connectionParams.put(TransportConstants.PORT_PROP_NAME, Integer.valueOf(JMS_SERVER_PORT));

	    final TransportConfiguration transportConfiguration = new TransportConfiguration(
		    NettyConnectorFactory.class.getName(), connectionParams);

	    // Step 3 Directly instantiate the JMS ConnectionFactory object using that TransportConfiguration
	    final ConnectionFactory cf = (ConnectionFactory) HornetQJMSClient
		    .createConnectionFactoryWithoutHA(JMSFactoryType.CF, transportConfiguration);   
            
	    // Step 4.Create a JMS Connection
	    connection = cf.createConnection();
            }catch(JMSException je){
                
                if (connection != null) {
                    try {
                       connection.close();
                        m_loggerProline.info("JMS Connection closed on error "+je.getMessage());
                    } catch (Exception exClose) {
                        m_loggerProline.error("Error closing JMS Connection", exClose);
                    }
                }
            }
    }
    
    public static void closeConnection(){


        if (connection != null) {           
            try {
                connection.close();
                m_loggerProline.info("JMS Connection closed");
            } catch (Exception exClose) {
                m_loggerProline.error("Error closing JMS Connection", exClose);
            }
        }
	
    }


}
