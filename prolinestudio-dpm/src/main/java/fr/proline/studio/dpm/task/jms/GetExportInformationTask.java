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
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dpm.task.util.JMSConnectionManager;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * task to retrieve the information for export configuration
 * @author MB243701
 */
public class GetExportInformationTask extends AbstractJMSTask {
    private static final String m_request = "proline/dps/uds/GetExportInformation";
    private DDataset m_dataset;
    private String m_mode;

    // contains the json string representing the configuration
    private final List<String> m_customizableExport;
    
    public GetExportInformationTask(AbstractJMSCallback callback, DDataset dataset, List<String> config) {
        super(callback,  new TaskInfo("Get Export Information " + (dataset == null ? "null" : dataset.getName()), true, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));
        m_dataset = dataset;
        m_customizableExport = config;
    }
    
    public GetExportInformationTask(AbstractJMSCallback callback, String mode, List<String> config) {
        super(callback,  new TaskInfo("Get Export Information " + mode, true, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));
        m_mode = mode;
        m_customizableExport = config;
    }
    
    
    @Override
    public void taskRun() throws JMSException {
        final JSONRPC2Request jsonRequest = new JSONRPC2Request(JMSConnectionManager.PROLINE_PROCESS_METHOD_NAME, Integer.valueOf(m_taskInfo.getId()));
        jsonRequest.setNamedParams(createParams());
           
        final TextMessage message = m_session.createTextMessage(jsonRequest.toJSONString());

        /* ReplyTo = Temporary Destination Queue for Server -> Client response */
        message.setJMSReplyTo(m_replyQueue);
        message.setStringProperty(JMSConnectionManager.PROLINE_SERVICE_NAME_KEY, m_request);
        addSupplementaryInfo(message);

        setTaskInfoRequest(message.getText());
	
        //  Send the Message
        m_producer.send(message);
        m_loggerProline.info("Message [{}] sent", message.getJMSMessageID());
        m_taskInfo.setJmsMessageID(message.getJMSMessageID());
    }

    @Override
    public void processWithResult(JSONRPC2Response jsonResponse) throws Exception {

        // FOR STREAM ONLY !
        final Object result = jsonResponse.getResult();
        if ((result == null) || (!String.class.isInstance(result))) {
            m_loggerProline.error(getClass().getSimpleName() + " failed : No valid configuration file / information returned :" + result);
            throw new Exception("No valid configuration returned : " + result);
        }

        // retrieve configuration
        String configStr = (String) result;
        if (configStr.isEmpty()) {
            m_loggerProline.error(getClass().getSimpleName() + " failed : No configuration returned.");
            throw new Exception("No configuration returned. " + result);
        }
        m_customizableExport.add(configStr);
    }

    
    private HashMap<String, Object> createParams() {
        HashMap<String, Object> params = new HashMap<>();
        Map<String, Object> extraParams = new HashMap<>();
        if (m_dataset != null) {
            params.put("project_id", m_dataset.getProject().getId());
            params.put("dataset_id", m_dataset.getId());
        } else {
            extraParams.put("export_mode", m_mode);
        }
        params.put("extra_params", extraParams);
        return params;
    }
    
}
