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

import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Message;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Notification;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dpm.AccessJMSManagerThread;
import static fr.proline.studio.dpm.task.jms.AbstractJMSTask.m_loggerProline;
import fr.proline.studio.dpm.task.util.JMSConnectionManager;
import java.util.HashMap;
import java.util.Map;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

/**
 * XIC Quantitation Task JMS
 */
public class RunQuantitationTask extends AbstractJMSTask {
    private static final String m_serviceName = "proline/dps/msq/Quantify";
    private String m_version = "3.0";
    
    private Long[] m_xicQuantiResult = null;
    private final String m_quantiDSName;
    private final Long m_pId;
    private final Long m_quantMethodId;
    private final Map<String,Object> m_quantParams;
    private final Map<String,Object> m_expDesignParams;
    
    public RunQuantitationTask(AbstractJMSCallback callback, Long projectId,  String quantDSName,  Map<String,Object> quantParams, Map<String,Object> expDesignParams, Long quantMethodId, Long[] retValue) {
        super(callback, new TaskInfo("Run Quantitation for "+quantDSName, true, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_HIGH));
        m_xicQuantiResult = retValue;     
        m_expDesignParams = expDesignParams;
        m_quantiDSName = quantDSName;
        m_pId= projectId;
        m_quantParams = quantParams;  
        m_quantMethodId = quantMethodId;
    }
    
    
    @Override
    public void taskRun() throws JMSException {
        final JSONRPC2Request jsonRequest = new JSONRPC2Request(JMSConnectionManager.PROLINE_PROCESS_METHOD_NAME, Integer.valueOf(m_taskInfo.getId()));
        jsonRequest.setNamedParams(createParams());
           
        final TextMessage message = m_session.createTextMessage(jsonRequest.toJSONString());

        /* ReplyTo = Temporary Destination Queue for Server -> Client response */
        message.setJMSReplyTo(m_replyQueue);
        message.setStringProperty(JMSConnectionManager.PROLINE_SERVICE_NAME_KEY, m_serviceName);
        addSupplementaryInfo(message);

        message.setStringProperty(JMSConnectionManager.PROLINE_SERVICE_VERSION_KEY, m_version);
                
        setTaskInfoRequest(message.getText());
	
        //  Send the Message
        m_producer.send(message);
        m_loggerProline.info("Message [{}] sent", message.getJMSMessageID());
        m_taskInfo.setJmsMessageID(message.getJMSMessageID());
    }
    
    private HashMap<String, Object> createParams() {
        HashMap<String, Object> params = new HashMap<>();
        //-- Global PARAMS
        params.put("name", m_quantiDSName);
        params.put("description", m_quantiDSName);
        params.put("project_id", m_pId);
        params.put("method_id", m_quantMethodId);
        params.put("experimental_design", m_expDesignParams);        
        params.put("quantitation_config", m_quantParams);
        return params;
    }

    @Override
    public void taskDone(final Message jmsMessage) throws Exception {
        
        final TextMessage textMessage = (TextMessage) jmsMessage;
        final String jsonString = textMessage.getText();

        final JSONRPC2Message jsonMessage = JSONRPC2Message.parse(jsonString);
        if(jsonMessage instanceof JSONRPC2Notification) {
            m_loggerProline.warn("JSON Notification method: " + ((JSONRPC2Notification) jsonMessage).getMethod()+" instead of JSON Response");
            throw new Exception("Invalid JSONRPC2Message type");
            
        } else if (jsonMessage instanceof JSONRPC2Response)  {
            
            final JSONRPC2Response jsonResponse = (JSONRPC2Response) jsonMessage;
	    m_loggerProline.debug("JSON Response Id: " + jsonResponse.getID());
            
            final JSONRPC2Error jsonError = jsonResponse.getError();

	    if (jsonError != null) {
		m_loggerProline.error("JSON Error code {}, message : \"{}\"", jsonError.getCode(), jsonError.getMessage());
		m_loggerProline.error("JSON Throwable", jsonError);
                throw jsonError;
	    }
             
            final Object result = jsonResponse.getResult();
            if (result == null || ! Long.class.isInstance(result) ) {
		m_loggerProline.debug("Invalid result: No returned Quantitation dataset Id");
                throw new Exception("Invalid result "+result);
	    } else {
		m_loggerProline.debug("Result :\n" + result); 
                // retrieve resultSummary id
                 m_xicQuantiResult[0] = (Long) result;
	    }
        }
          m_currentState = JMSState.STATE_DONE;
        
    }
    
}
