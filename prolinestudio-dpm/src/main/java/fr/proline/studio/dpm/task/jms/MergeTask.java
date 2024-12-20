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
 * JMS Task Merge specified result sets (or result summaries) into one new
 * result set (or new result summary).
 *
 * @author VD225637
 */
public class MergeTask extends AbstractJMSTask {


    public enum Config { 
        AGGREGATION("aggregation"), UNION("union");
        
        final String value;
        
        Config(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
    }
    
    private List<Long> m_rsetOrRsmIdList = null;
    private final long m_projectId;
    private final Config m_configuration;
    private Long[] m_resultSetId = null;
    private Long[] m_resultSummaryId = null;

    private int m_action;

    private static final int MERGE_RSM = 0;
    private static final int MERGE_RSET = 1;

    public MergeTask(AbstractJMSCallback callback, long projectId) {
        this(callback, projectId, Config.AGGREGATION);
    }
    
    public MergeTask(AbstractJMSCallback callback, long projectId, Config configuration) {
        super(callback, null);
        m_projectId = projectId;
        m_configuration = configuration;
    }

    public void initMergeRset(List<Long> resultSetIdList, String parentName, Long[] resultSetId) {
        setTaskInfo(new TaskInfo("Merge Search Results on " + parentName, true, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_HIGH));
        m_rsetOrRsmIdList = resultSetIdList;
        m_resultSetId = resultSetId;
        m_action = MERGE_RSET;
    }

    public void initMergeRsm(List<Long> resultSummaryIdList, String parentName, Long[] resultSetId, Long[] resultSummaryId) {
        setTaskInfo(new TaskInfo("Merge Identification Summaries on " + parentName, true, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_HIGH));
        m_rsetOrRsmIdList = resultSummaryIdList;
        m_resultSetId = resultSetId;
        m_resultSummaryId = resultSummaryId;
        m_action = MERGE_RSM;
    }

    @Override
    public void taskRun() throws JMSException {
        JSONRPC2Request jsonRequest;
        if (m_action == MERGE_RSET) {
            jsonRequest = new JSONRPC2Request("merge_result_sets", m_taskInfo.getId());
        } else {
            jsonRequest = new JSONRPC2Request("merge_result_summaries", m_taskInfo.getId());
        }
        jsonRequest.setNamedParams(createParams());

        final TextMessage message = m_session.createTextMessage(jsonRequest.toJSONString());

        /* ReplyTo = Temporary Destination Queue for Server -> Client response */
        message.setJMSReplyTo(m_replyQueue);
        message.setStringProperty(JMSConnectionManager.PROLINE_SERVICE_NAME_KEY, "proline/dps/msi/MergeResults");
        message.setStringProperty(JMSConnectionManager.PROLINE_SERVICE_VERSION_KEY, "2.0");
        addSupplementaryInfo(message);

        setTaskInfoRequest(message.getText());

        //  Send the Message
        m_producer.send(message);
        m_loggerProline.info("Message [{}] sent", message.getJMSMessageID());
        m_taskInfo.setJmsMessageID(message.getJMSMessageID());
    }

    private HashMap<String, Object> createParams() {
        HashMap<String, Object> params = new HashMap<>();
        params.put("project_id", m_projectId);
        params.put("aggregation_mode", m_configuration.value);
        if (m_action == MERGE_RSET) {
            params.put("result_set_ids", m_rsetOrRsmIdList);
        } else {
            params.put("result_summary_ids", m_rsetOrRsmIdList);
        }
        return params;
    }

    @Override
    public void processWithResult(JSONRPC2Response jsonResponse) throws Exception {

        final Object result = jsonResponse.getResult();
        if (result == null) {
            m_loggerProline.debug("No returned ResultSet Id");
            throw new Exception("JMS Result : No returned ResultSet Id");
        }

        if (m_action == MERGE_RSET) {

            Long resultSetId = (Long) result;
            m_resultSetId[0] = resultSetId;

        } else {
//                if (!ArrayMap.class.isInstance(result)) {
//                    m_loggerProline.debug("Invalid JMS result type");
//                    throw new Exception("Invalid JMS result type " + result.getClass());
//                }

            HashMap<String,Object> resultIds = (HashMap<String,Object>) result;
            Long resultSetId = (Long) resultIds.get("target_result_set_id");
            if (resultSetId == null) {
                m_loggerProline.error(getClass().getSimpleName() + " failed : No returned ResultSet Id");
                throw new Exception("No returned ResultSet Id");
            }
            m_resultSetId[0] = resultSetId;

            Long resultSummaryId = (Long) resultIds.get("target_result_summary_id");
            if (resultSummaryId == null) {
                m_loggerProline.error(getClass().getSimpleName() + " failed : No returned ResultSummary Id");
                throw new Exception("No returned ResultSummary Id");
            }
            m_resultSummaryId[0] = resultSummaryId;
        }

    }

}
