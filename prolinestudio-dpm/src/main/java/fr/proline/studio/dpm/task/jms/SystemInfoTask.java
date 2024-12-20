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
public class SystemInfoTask extends AbstractJMSTask {
    
    private static final String m_serviceName = "proline/monitoring/Info";
    private static final String m_version = "1.0";
    private final String[] m_result;
        
    public SystemInfoTask(AbstractJMSCallback callback,String[] resultLog){
        super(callback, new TaskInfo( "Get Messaging System information", false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));
        m_result = resultLog;
    }
    
    @Override
    public void taskRun() throws JMSException {
         final JSONRPC2Request jsonRequest = new JSONRPC2Request(JMSConnectionManager.PROLINE_PROCESS_METHOD_NAME, Integer.valueOf(m_taskInfo.getId()));
        jsonRequest.setNamedParams(createParams());
           
        final TextMessage message = m_session.createTextMessage(jsonRequest.toJSONString());

        /* ReplyTo = Temporary Destination Queue for Server -> Client response */
        message.setJMSReplyTo(m_replyQueue);
        message.setStringProperty(JMSConnectionManager.PROLINE_SERVICE_NAME_KEY, m_serviceName);
        message.setStringProperty(JMSConnectionManager.PROLINE_SERVICE_VERSION_KEY, m_version);
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
        if (result == null || !String.class.isInstance(result)) {
            m_loggerProline.debug("Invalid result");
            throw new Exception("Invalid result " + result);
        } else {
            m_result[0] = result.toString();
        }
    }


    private Map<String, Object> createParams() {
        return new HashMap<>();
    }
    
    
    
}
