/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.dpm.task.jms;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Message;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Notification;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import fr.proline.repository.AbstractDatabaseConnector;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dpm.jms.AccessJMSManagerThread;
import static fr.proline.studio.dpm.task.jms.AbstractJMSTask.m_loggerProline;
import fr.proline.studio.dpm.task.util.JMSConnectionManager;
import java.util.HashMap;
import java.util.Map;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

/**
 *
 * @author VD225637
 */
public class GetDBConnectionTemplateTask extends AbstractJMSTask {

    private static final String m_serviceName = "proline/admin/GetConnectionTemplate";
    
    private String m_password; //password to use for DB connection
    private HashMap<Object, Object> m_databaseProperties;  // out parameter

    public GetDBConnectionTemplateTask(AbstractJMSCallback callback, String password, HashMap<Object, Object> databaseProperties) {
        super(callback, new TaskInfo("Get information from JMS Server ", false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_HIGH));
        
        m_password = password;
        m_databaseProperties = databaseProperties;
    }

    @Override
    public void taskRun() throws JMSException {
        
        final JSONRPC2Request jsonRequest = new JSONRPC2Request(JMSConnectionManager.PROLINE_PROCESS_METHOD_NAME, Integer.valueOf(m_taskInfo.getId()));

        final TextMessage message = AccessJMSManagerThread.getAccessJMSManagerThread().getSession().createTextMessage(jsonRequest.toJSONString());

        /* ReplyTo = Temporary Destination Queue for Server -> Client response */
        message.setJMSReplyTo(m_replyQueue);
        message.setStringProperty(JMSConnectionManager.PROLINE_SERVICE_NAME_KEY, m_serviceName);
        //message.setStringProperty(JMSConnectionManager.PROLINE_SERVICE_VERSION_KEY, m_version);
        addSourceToMessage(message);   
        addDescriptionToMessage(message);
        
        setTaskInfoRequest(message.getText());

        //  Send the Message
        m_producer.send(message);
        m_loggerProline.info("Message [{}] sent", message.getJMSMessageID());
        m_taskInfo.setJmsMessageID(message.getJMSMessageID());
    }

    @Override
    public void taskDone(Message jmsMessage) throws Exception {
        final TextMessage textMessage = (TextMessage) jmsMessage;
        final String jsonString = textMessage.getText();

        final JSONRPC2Message jsonMessage = JSONRPC2Message.parse(jsonString);
        if (jsonMessage instanceof JSONRPC2Notification) {
            m_loggerProline.warn("JSON Notification method: " + ((JSONRPC2Notification) jsonMessage).getMethod() + " instead of JSON Response");
            throw new Exception("Invalid JSONRPC2Message type");

        } else if (jsonMessage instanceof JSONRPC2Response) {

            final JSONRPC2Response jsonResponse = (JSONRPC2Response) jsonMessage;
            m_loggerProline.debug("JSON Response Id: " + jsonResponse.getID());

            final JSONRPC2Error jsonError = jsonResponse.getError();

            if (jsonError != null) {
                m_loggerProline.error("Get DB Connection Template. JSON Error code {}, message : \"{}\"", jsonError.getCode(), jsonError.getMessage());
                m_loggerProline.error("JSON Throwable", jsonError);
                throw jsonError;
            }

            final Object result = jsonResponse.getResult();
            if (result == null || !Map.class.isInstance(result)) {
                m_loggerProline.debug("Get DB Connection Template, invalid result. Dabasase Parameters not returned");
                throw new Exception("Get DB Connection Template. Invalid result " + result+" . Dabasase Parameters not returned");
            } else {
                m_loggerProline.debug("Result :\n" + result);
                Map returnedValues = (Map) result;
                if (returnedValues.isEmpty()) {
                    m_loggerProline.error(getClass().getSimpleName() + " failed : No returned values");
                    throw new Exception("No returned values " + result);
                }

                String databaseUser = (String) returnedValues.get("javax.persistence.jdbc.user");
                String databaseDriver = (String) returnedValues.get("javax.persistence.jdbc.driver");
                String databaseURL = (String) returnedValues.get("javax.persistence.jdbc.url");
            
                if ((databaseUser == null) || (databaseDriver == null) || (databaseURL == null)) {
                    m_loggerProline.error(getClass().getSimpleName() + " failed : Dabasase Parameters uncomplete");
                    throw new Exception("Dabasase Parameters uncomplete " + result);                    
                }
                            
                m_databaseProperties.put(AbstractDatabaseConnector.PERSISTENCE_JDBC_USER_KEY, databaseUser);
                m_databaseProperties.put(AbstractDatabaseConnector.PERSISTENCE_JDBC_PASSWORD_KEY, m_password);
                m_databaseProperties.put(AbstractDatabaseConnector.PERSISTENCE_JDBC_DRIVER_KEY, databaseDriver);
                m_databaseProperties.put(AbstractDatabaseConnector.PERSISTENCE_JDBC_URL_KEY, databaseURL);
                m_databaseProperties.put(AbstractDatabaseConnector.PERSISTENCE_JDBC_URL_KEY, databaseURL);
                m_databaseProperties.put(AbstractDatabaseConnector.PROLINE_MAX_POOL_CONNECTIONS_KEY, 3); 
            }
        }
        m_currentState = JMSState.STATE_DONE;
    }

}

