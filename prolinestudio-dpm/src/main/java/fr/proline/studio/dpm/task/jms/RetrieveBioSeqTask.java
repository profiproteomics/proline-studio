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
import javax.jms.Message;
import javax.jms.TextMessage;
import java.util.HashMap;
import java.util.List;

/**
 * Task used to retrieve BioSequence from source (fasta ....) to SeqRepository
 * and then to MSI where other calculated properties are stored (MW, coverage)
 *
 * @author VD225637
 */
public class RetrieveBioSeqTask extends AbstractJMSTask {

    private static boolean isRunning;

    private final List<Long> m_resultSummariesIds;
    private final long m_projectId;
    private final boolean m_forceUpdate;

    public RetrieveBioSeqTask(AbstractJMSCallback callback, List<Long> rsmIds, long projectId, boolean forceUpdate) {
        super(callback, new TaskInfo("Retrieve Protein's Sequences", false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_HIGH));
        m_resultSummariesIds = rsmIds;
        m_projectId = projectId;
        m_forceUpdate = forceUpdate;
    }

    @Override
    public void taskRun() throws JMSException {
        final JSONRPC2Request jsonRequest = new JSONRPC2Request(JMSConnectionManager.PROLINE_PROCESS_METHOD_NAME, Integer.valueOf(m_taskInfo.getId()));
        jsonRequest.setNamedParams(createParams());

        final TextMessage message = m_session.createTextMessage(jsonRequest.toJSONString());

        /* ReplyTo = Temporary Destination Queue for Server -> Client response */
        message.setJMSReplyTo(m_replyQueue);
        message.setStringProperty(JMSConnectionManager.PROLINE_SERVICE_NAME_KEY, "proline/seq/RetrieveBioSeqForRSMs");
        addSupplementaryInfo(message);
        setTaskInfoRequest(message.getText());

        //  Send the Message
        isRunning= true;
        m_producer.send(message);
        String m_lastMsgId = message.getJMSMessageID();
        m_loggerProline.info("Message [{}] sent", m_lastMsgId);
        m_taskInfo.setJmsMessageID(message.getJMSMessageID());
    }

    private HashMap<String, Object> createParams() {
        HashMap<String, Object> params = new HashMap<>();
        params.put("project_id", m_projectId);
        params.put("result_summaries_ids", m_resultSummariesIds);
        params.put("force_update", m_forceUpdate);
        return params;
    }

    @Override
    public void taskDone(Message jmsMessage) throws Exception {
         isRunning = false;
         super.taskDone(jmsMessage);
    }

    @Override
    public void processWithResult(JSONRPC2Response jsonResponse) throws Exception {
        final Object result = jsonResponse.getResult(); //Should be String OK...
        if (result == null) {
            m_loggerProline.error(getClass().getSimpleName() + " failed : No returned values");
            throw new Exception("Invalid result " + result);
        }
    }

    public static boolean isRetrieveRunning(){
        return isRunning;
    }
}
