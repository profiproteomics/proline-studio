/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.dpm.task.jms;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import fr.proline.studio.dpm.data.JMSNotificationMessage;
import fr.proline.studio.dpm.jms.AccessJMSManagerThread;
import fr.proline.studio.dpm.task.util.JMSConnectionManager;
import fr.proline.studio.dpm.task.util.JMSMessageUtil;
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

    private JMSNotificationMessage m_msgToRemove;
    protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.ResultExplorer");
    private AbstractJMSCallback m_callback = null;
    private JMSNotificationMessage[] m_replyVal = new JMSNotificationMessage[1];
   
    public PurgeConsumer(AbstractJMSCallback callback, JMSNotificationMessage msgToRemove, JMSNotificationMessage[] replyVal ) {
        m_callback = callback;        
        m_msgToRemove = msgToRemove;
        m_replyVal = replyVal;
    }

    public void clearMessage() {
        
        final String selectorString = "JMSMessageID = \'" + m_msgToRemove.getServerUniqueMsgId()+"\'";
        
        
        new Thread() {

            @Override
            public void run() {
                m_logger.debug("Purge Consumer selector [" + selectorString + ']');
                Session session = AccessJMSManagerThread.getAccessJMSManagerThread().getSession();
                String errorMsg = null;
                MessageConsumer consumer = null;
                MessageProducer replyProducer = null;
        
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
                            m_logger.debug("Purging JMS Message with ID "+messageId );
                            final Destination replyDestination = message.getJMSReplyTo();
                            if (replyDestination == null) {
                                m_logger.warn("Message has no JMSReplyTo Destination : Cannot send JSON Response to Client");
                            } else {
                                /* Try to send a JSON-RPC Error to client Producer */
                                final JSONRPC2Error jsonError = new JSONRPC2Error(JSONRPC2Error.INTERNAL_ERROR.getCode(), "JMS message was removed ");
                                final JSONRPC2Response jsonResponse = new JSONRPC2Response(jsonError, null);
                                // Step 7. Create a Text Message
                                final TextMessage jmsResponseMessage = session.createTextMessage();
                                jmsResponseMessage.setJMSCorrelationID(messageId);
                                jmsResponseMessage.setText(jsonResponse.toJSONString());
                                m_logger.debug("Sending JMS Response to Message [" + messageId + "] on Destination [" + replyDestination + ']');
                                replyProducer.send(replyDestination, jmsResponseMessage);
                            }
                            JMSNotificationMessage purgeMsg = JMSMessageUtil.buildJMSNotificationMessage(message, JMSNotificationMessage.MessageStatus.ABORTED);
                            m_replyVal[0] =  purgeMsg;
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
                    if (m_callback.mustBeCalledInAWT()) {
                    // Callback must be executed in the Graphical thread (AWT)
                    SwingUtilities.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            m_callback.run(success);
                        }
                        });
                    } else {
                        // Method called in the current thread
                       // In this case, we assume the execution is fast.
                        m_callback.run(success);
                    }
                } 
            } //End Run
        }.start();
        
    }
}
