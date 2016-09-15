/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.dpm.task.util;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Notification;
import com.thetransactioncompany.jsonrpc2.JSONRPC2ParseException;
import fr.proline.studio.dpm.data.JMSNotificationMessage;
import fr.proline.studio.dpm.task.jms.AbstractJMSCallback;
import java.util.Map;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.swing.SwingUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author VD225637
 */
public class ServiceNotificationListener implements MessageListener {

    public final static String NOTIFICATION_SERVICE_NAME_KEY="service_name";
    public final static String NOTIFICATION_SERVICE_VERSION_KEY="service_version";
    public final static String NOTIFICATION_SERVICE_SOURCE_KEY="service_source";
    public final static String NOTIFICATION_SERVICE_MORE_INFO_KEY="complementary_info";
    public final static String NOTIFICATION_TIMESTAMP_KEY= "event_timestamp";
    public final static String NOTIFICATION_JMS_ID_KEY= "request_jms_message_id";
    public final static String NOTIFICATION_JSON_REQ_ID_KEY= "json_rpc_request_id";
    public final static String NOTIFICATION_SERVICE_STATUS_KEY= "event_type";   
            
    protected static final Logger m_loggerProline = LoggerFactory.getLogger("ProlineStudio.DPM.Task");
    private AbstractJMSCallback m_callback = null;
    private JMSNotificationMessage[] m_replyVal = new JMSNotificationMessage[1];
    
    public ServiceNotificationListener() {
    }

    public void setServiceNotifierCallback(AbstractJMSCallback callback, JMSNotificationMessage[] replyVal ) {        
        m_callback = callback;
        m_replyVal = replyVal;
    }

    @Override
    public void onMessage(Message jmsMessage) {
        if (m_callback == null) {
            m_loggerProline.debug("SKIP Notification message  : " + JMSMessageUtil.formatMessage(jmsMessage));
        } else {
            m_loggerProline.debug("Notification Listener Receiving message  : " + JMSMessageUtil.formatMessage(jmsMessage));
            if (jmsMessage instanceof TextMessage) {
                final TextMessage textMessage = (TextMessage) jmsMessage;

                try {
                    final String jsonString = textMessage.getText();
                    final JSONRPC2Notification  jsonNotif = JSONRPC2Notification.parse(jsonString);
                    Map<String,Object> params = jsonNotif.getNamedParams();                
                    JMSNotificationMessage msg = new JMSNotificationMessage(params.getOrDefault(NOTIFICATION_SERVICE_NAME_KEY, "Undefined").toString(),params.getOrDefault(NOTIFICATION_SERVICE_VERSION_KEY, "default").toString(),
                            params.getOrDefault(NOTIFICATION_SERVICE_SOURCE_KEY, "Unknown").toString(), params.getOrDefault(NOTIFICATION_SERVICE_MORE_INFO_KEY, "Unknown").toString(), (Long) params.getOrDefault(NOTIFICATION_TIMESTAMP_KEY,0l),
                            params.getOrDefault(NOTIFICATION_JMS_ID_KEY, "Unknown").toString(), params.getOrDefault(NOTIFICATION_JSON_REQ_ID_KEY,"Unknown").toString(), 
                            JMSNotificationMessage.MessageStatus.parseString(params.get(NOTIFICATION_SERVICE_STATUS_KEY).toString()) );
                    m_replyVal[0] =  msg;
                    if (m_callback.mustBeCalledInAWT()) {
                        // Callback must be executed in the Graphical thread (AWT)
                        SwingUtilities.invokeLater(new Runnable() {

                            @Override
                            public void run() {
                                m_callback.run(true);
                            }
                        });
                    } else {
                        // Method called in the current thread
                        // In this case, we assume the execution is fast.
                        m_callback.run(true);                        
                    }
                } catch (JMSException | JSONRPC2ParseException ex) {
                    m_loggerProline.error("Error handling JMS Message", ex);
                }

            } else {
                m_loggerProline.warn("Invalid JMS Message type");
            }
        }
    }

}
