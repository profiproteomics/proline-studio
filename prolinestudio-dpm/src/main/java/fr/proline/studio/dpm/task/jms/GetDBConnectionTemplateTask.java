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
package fr.proline.studio.dpm.task.jms;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import fr.proline.repository.AbstractDatabaseConnector;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dpm.task.util.JMSConnectionManager;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author VD225637
 */
public class GetDBConnectionTemplateTask extends AbstractJMSTask {

    private static final String m_serviceName = "proline/admin/GetConnectionTemplate";
    
    private final String m_password; //password to use for DB connection
    private final HashMap<Object, Object> m_databaseProperties;  // out parameter

    public GetDBConnectionTemplateTask(AbstractJMSCallback callback, String password, HashMap<Object, Object> databaseProperties) {
        super(callback, new TaskInfo("Get information from JMS Server ", false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_HIGH));
        
        m_password = password;
        m_databaseProperties = databaseProperties;
    }

    @Override
    public void taskRun() throws JMSException {
        
        final JSONRPC2Request jsonRequest = new JSONRPC2Request(JMSConnectionManager.PROLINE_PROCESS_METHOD_NAME, Integer.valueOf(m_taskInfo.getId()));

        final TextMessage message = m_session.createTextMessage(jsonRequest.toJSONString());

        /* ReplyTo = Temporary Destination Queue for Server -> Client response */
        message.setJMSReplyTo(m_replyQueue);
        message.setStringProperty(JMSConnectionManager.PROLINE_SERVICE_NAME_KEY, m_serviceName);
        //message.setStringProperty(JMSConnectionManager.PROLINE_SERVICE_VERSION_KEY, m_version);
        addSupplementaryInfo(message);

        setTaskInfoRequest(message.getText());

        //  Send the Message
        m_producer.send(message);
        m_loggerProline.info("Message [{}] sent", message.getJMSMessageID());
        m_taskInfo.setJmsMessageID(message.getJMSMessageID());
    }

    @Override
    public void processWithResult(JSONRPC2Response jsonResponse) throws Exception {

        final Object result = jsonResponse.getResult();
        if (result == null || !Map.class.isInstance(result)) {
            m_loggerProline.debug("Get DB Connection Template, invalid result. Dabasase Parameters not returned");
            throw new Exception("Get DB Connection Template. Invalid result " + result + " . Dabasase Parameters not returned");
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
            m_databaseProperties.put(AbstractDatabaseConnector.PROLINE_MAX_POOL_CONNECTIONS_KEY, 3);
        }
    }

}

