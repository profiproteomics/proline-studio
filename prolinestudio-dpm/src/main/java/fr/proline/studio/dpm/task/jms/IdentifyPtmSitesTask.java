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
import java.util.Map;

/**
 * Task to generate spectrum matches
 * @author VD225637
 */
public class IdentifyPtmSitesTask extends AbstractJMSTask {
    private static final String m_serviceName = "proline/dps/msi/IdentifyPtmSites";
    
    private final Long m_projectId;
    private final Long m_resultSummaryId;
    private final Boolean m_forceAction;
    private String m_version;
    private List<Long> m_ptmIds;
    private String m_clusteringMethodName;

    public IdentifyPtmSitesTask(AbstractJMSCallback callback, String datasetName, Long projectId, Long resultSummaryId, Boolean force) {
        super(callback, new TaskInfo( ((datasetName != null) ? "Identify Modification sites for "+datasetName : "Identify Modification sites"), true, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_HIGH));
        m_projectId = projectId;
        m_resultSummaryId = resultSummaryId;
        m_forceAction = force;
    }

    public IdentifyPtmSitesTask(AbstractJMSCallback callback, String datasetName, Long projectId, Long resultSummaryId, Boolean force, String version, List<Long> ptmIds, String clusteringMethodName) {
        this(callback, datasetName, projectId, resultSummaryId, force);
        this.m_version = version;
        this.m_ptmIds = ptmIds;
        this.m_clusteringMethodName = clusteringMethodName;
    }

    @Override
    public void taskRun() throws JMSException {
        final JSONRPC2Request jsonRequest = new JSONRPC2Request(JMSConnectionManager.PROLINE_PROCESS_METHOD_NAME, Integer.valueOf(m_taskInfo.getId()));
        jsonRequest.setNamedParams(createParams());
           
        final TextMessage message = m_session.createTextMessage(jsonRequest.toJSONString());

        /* ReplyTo = Temporary Destination Queue for Server -> Client response */
        message.setJMSReplyTo(m_replyQueue);
        message.setStringProperty(JMSConnectionManager.PROLINE_SERVICE_NAME_KEY, m_serviceName);
        if(m_version != null )
            message.setStringProperty(JMSConnectionManager.PROLINE_SERVICE_VERSION_KEY, m_version);
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
        params.put("result_summary_id", m_resultSummaryId);
        if (m_forceAction != null) {
            params.put("force",  m_forceAction);
        } else {
            // WARNING : for development purpose ONLY ! (allow to recompute ptm site of a single RSM multiple times
            params.put("force",  true);
        }
        if (m_version != null) {
            params.put("ptm_ids", m_ptmIds);
            Map<String, Object> clusteringConfig = new HashMap<>();
            clusteringConfig.put("method_name", m_clusteringMethodName);
            params.put("clustering_config", clusteringConfig);
        }
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
