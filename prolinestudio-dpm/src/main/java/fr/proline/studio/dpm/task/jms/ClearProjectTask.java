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
import java.util.List;

/**
 * JMS Task to clear orphan data into a specified project: clear rsm and rs in msi
 * @author MB243701
 */
public class ClearProjectTask extends AbstractJMSTask {
    
    private static final String m_serviceName = "proline/dps/msi/DeleteOrphanData";
    //private static final String m_version = "2.0";    
    
    private final Long m_projectId;
    private final List<Long> m_resultSummaryIds;
    private final List<Long> m_resultSetIds;
    
    public ClearProjectTask(AbstractJMSCallback callback,  Long projectId,  List<Long> resultSummaryIds, List<Long> resultSetIds) {
        super(callback, new TaskInfo("Clean up data for projectId "+projectId, true, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_HIGH));
        m_projectId = projectId;   
        m_resultSummaryIds = resultSummaryIds;
        m_resultSetIds = resultSetIds;
    }
    
    @Override
    public void taskRun() throws JMSException {
        final JSONRPC2Request jsonRequest = new JSONRPC2Request(JMSConnectionManager.PROLINE_PROCESS_METHOD_NAME, Integer.valueOf(m_taskInfo.getId()));
        jsonRequest.setNamedParams(createParams());
           
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
        m_taskInfo.setJmsMessageID(message.getJMSMessageID());
    }

    private HashMap<String, Object> createParams() {
        HashMap<String, Object> params = new HashMap<>();
        //-- Global PARAMS
        params.put("project_id", m_projectId);
        params.put("result_summary_ids", m_resultSummaryIds);
        params.put("result_set_ids", m_resultSetIds);
        return params;
    }

    @Override
    public void processWithResult(JSONRPC2Response jsonResponse) throws Exception {

        final Object result = jsonResponse.getResult();
        if (result == null || !Boolean.class.isInstance(result)) {
            m_loggerProline.debug("Invalid result");
            throw new Exception("Invalid result " + result);
        } else {
            m_loggerProline.debug("Result :\n" + result);
        }
    }
    
}
