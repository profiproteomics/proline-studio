/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.dpm.task.jms;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import fr.proline.studio.dpm.data.JMSNotificationMessage;
import fr.proline.studio.dpm.AccessJMSManagerThread;
import fr.proline.studio.dpm.task.util.JMSConnectionManager;
import fr.proline.studio.dpm.task.util.JMSMessageUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.swing.SwingUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author VD225637
 */
public class PurgeConsumer {


    protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");
    private List<AbstractJMSCallback> m_callbacks = null;
    private List<JMSNotificationMessage[]> m_replyVals = null;
   
    private static PurgeConsumer m_singleton;
    
    public static PurgeConsumer getPurgeConsumer(){
        if(m_singleton==null)
            m_singleton = new PurgeConsumer();
        return m_singleton;
    }
    
    private PurgeConsumer() {
        m_callbacks = new  ArrayList<>();
        m_replyVals = new  ArrayList<>();
    }
    
    public void addCallback( AbstractJMSCallback callback,JMSNotificationMessage[] replyVal ){
        if(callback == null || replyVal == null || replyVal.length != 1)
            throw new RuntimeException("Must specify callback and reply value pair to register for PurgeConsumer ");
        m_callbacks.add(callback);        
        m_replyVals.add(replyVal);
    }
    
    /**
     * Remove the specified callback with its associated reply value
     */
    public void removeCallback(AbstractJMSCallback callback){
        int index = m_callbacks.indexOf(callback);
        if(index<0)
            return;
        m_callbacks.remove(index);
        m_replyVals.remove(index);
    }
    
    public void clearMessage(String msgIdToRemove) {
        
        final String selectorString = "JMSMessageID = \'" +msgIdToRemove+"\'";        
                
        new Thread() {

            @Override
            public void run() {
                m_logger.debug("Purge Consumer selector [" + selectorString + ']');
                Session session = AccessJMSManagerThread.getAccessJMSManagerThread().getSession();
                String errorMsg = null;
                MessageConsumer consumer = null;
                MessageProducer replyProducer = null;
                JMSNotificationMessage resultMsg = null;
                
                try {
                    consumer = session.createConsumer(JMSConnectionManager.getJMSConnectionManager().getServiceQueue(), selectorString);                
                    //ReplyProducer to send JMS Response Message to Client (Producer MUST be confined in current thread)
                    replyProducer = session.createProducer(null);
                    
                    // Block max. 5 seconds
                    final Message message = consumer.receive(TimeUnit.SECONDS.toMillis(5));                
                    if (message == null) {
                        errorMsg = "No message to consume. ";
                    } else {
                        String messageId = message.getJMSMessageID();
                        m_logger.debug("Purging JMS Message with ID " + messageId);
                        final Destination replyDestination = message.getJMSReplyTo();
                        if (replyDestination == null) {
                            m_logger.warn("Message has no JMSReplyTo Destination : Cannot send JSON Response to Client");
                        } else {
                            /* Try to send a JSON-RPC Error to client Producer */
                            final JSONRPC2Error jsonError = new JSONRPC2Error(JMSConnectionManager.JMS_CANCELLED_TASK_ERROR_CODE, "JMS message was cancelled ");
                            final JSONRPC2Response jsonResponse = new JSONRPC2Response(jsonError, null);
                            // Step 7. Create a Text Message
                            final TextMessage jmsResponseMessage = session.createTextMessage();
                            jmsResponseMessage.setJMSCorrelationID(messageId);
                            jmsResponseMessage.setText(jsonResponse.toJSONString());
                            m_logger.debug("Sending JMS Response to Message [" + messageId + "] on Destination [" + replyDestination + ']');
                            replyProducer.send(replyDestination, jmsResponseMessage);
                        }
                        resultMsg = JMSMessageUtil.buildJMSNotificationMessage(message, JMSNotificationMessage.MessageStatus.ABORTED);                            
                    }
                                 
                } catch (Exception ex) {
                     errorMsg = "Error purging JMS Message";
                    m_logger.error("Unable to create Consumer message ! ");
                } finally {
                    if (consumer != null) {
                        try {
                            consumer.close();                        
                        } catch (Exception exClose) {
                            m_logger.error("Error closing Purge consumer ", exClose);
                        }
                    }

                    if (replyProducer != null) {
                        try {
                            replyProducer.close();                            
                        } catch (Exception exClose) {
                            m_logger.error("Error closing replyProducer in Purge Consumer", exClose);
                        }
                    }
                    
                    boolean success = errorMsg != null;
                    int index;
                    for(index =0; index < m_callbacks.size(); index++ ){
                        AbstractJMSCallback callback = m_callbacks.get(index);
                        JMSNotificationMessage[] replyVal = m_replyVals.get(index);
                        replyVal[0] = resultMsg;
                        if (callback.mustBeCalledInAWT()) {
                            // Callback must be executed in the Graphical thread (AWT)
                            SwingUtilities.invokeLater(new Runnable() {

                                @Override
                                public void run() {
                                    callback.run(success);
                                }
                            });
                        } else {
                            // Method called in the current thread
                            // In this case, we assume the execution is fast.
                            callback.run(success);
                        }
                    }
                }
            } //End Run
        }.start();
        
    }
}
