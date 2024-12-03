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
 *
 * @author VD225637
 */
public class ImportMaxQuantTask extends AbstractJMSTask {

    public static int RESULT_SET_IDS_LIST = 0;
    public static int WARNING_MESSAGES = 1;
    public static int RESULT_SUMMARY_IDS_MAP = 2;
    public static int QUANTITATION_DATASET_ID = 3;
    
    public static int RESULTS_SIZE = 4;
    
    private final String m_filePath;
    private final long m_instrumentId;
    private long m_peaklistSoftwareId;
    private final long m_projectId;
    private final String m_accessionRegexp;
    private final Boolean m_importQuantResult;
    
    private Object[] m_taskResult = null;
 

    public ImportMaxQuantTask(AbstractJMSCallback callback, String filePath, long instrumentId, long peaklistSoftwareId, long projectId, Object[] resultsFromTask) {
        super(callback, new TaskInfo("Import MaxQuant file " + filePath, true, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_HIGH));
        m_filePath = filePath;
        m_instrumentId = instrumentId;
        m_peaklistSoftwareId = peaklistSoftwareId;
        m_projectId = projectId;
        m_importQuantResult = false; 
        m_accessionRegexp = "*";
        m_taskResult = resultsFromTask;
    }

    public ImportMaxQuantTask(AbstractJMSCallback callback, String filePath, long instrumentId, String accessionRegexp, Boolean importQuantitation, long projectId, Object[] resultsFromTask) {
        super(callback, new TaskInfo("Import MaxQuant file " + filePath, true, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_HIGH));
        m_filePath = filePath;
        m_instrumentId = instrumentId;
        m_accessionRegexp = accessionRegexp;
        m_importQuantResult = importQuantitation;
        m_projectId = projectId;
        m_taskResult = resultsFromTask;
    }

    
    @Override
    public void taskRun() throws JMSException {
        final JSONRPC2Request jsonRequest = new JSONRPC2Request(JMSConnectionManager.PROLINE_PROCESS_METHOD_NAME, Integer.valueOf(m_taskInfo.getId()));
        jsonRequest.setNamedParams(createParams());
           
        final TextMessage message = m_session.createTextMessage(jsonRequest.toJSONString());

        /* ReplyTo = Temporary Destination Queue for Server -> Client response */
        message.setJMSReplyTo(m_replyQueue);
        message.setStringProperty(JMSConnectionManager.PROLINE_SERVICE_NAME_KEY, "proline/dps/msi/ImportMaxQuantResults");
        message.setStringProperty(JMSConnectionManager.PROLINE_SERVICE_VERSION_KEY, "2.0");

        addSupplementaryInfo(message, m_projectId);

        setTaskInfoRequest(message.getText());
        //  Send the Message
        m_producer.send(message);
        m_loggerProline.info("Message [{}] sent", message.getJMSMessageID());        
        m_taskInfo.setJmsMessageID(message.getJMSMessageID());
    }

    @Override
    public void processWithResult(JSONRPC2Response jsonResponse) throws Exception {

        final Object result = jsonResponse.getResult();
        if ((result == null) || (!Map.class.isInstance(result))) {
            m_loggerProline.error(getClass().getSimpleName() + " failed : No returned values");
            throw new Exception("Invalid result " + result);
        }


        Map returnedValues = (Map) result;
        List returnedRsIds = (List) returnedValues.get("result_set_Ids");
        if (returnedRsIds == null || returnedRsIds.isEmpty()) {
            m_loggerProline.error(getClass().getSimpleName() + " failed : No returned ResultSet Ids");
            throw new Exception("Import result error : No returned ResultSet Ids");
        }


        Map resultSummaries = (Map) returnedValues.get("result_summary_ids_by_result_set_id");
        if (resultSummaries != null && !resultSummaries.isEmpty()) {
            m_taskResult[RESULT_SUMMARY_IDS_MAP] = resultSummaries;
        }

        Long datasetId = (Long) returnedValues.get("quantitation_dataset_id");
        if (datasetId != null) {
            m_taskResult[QUANTITATION_DATASET_ID] = datasetId;
        }

        m_taskResult[RESULT_SET_IDS_LIST] = returnedRsIds;
        m_taskResult[WARNING_MESSAGES] = (String) returnedValues.get("warning_msg");
            
    }

    private HashMap<String, Object> createParams() {
                
        HashMap<String, Object> params = new HashMap<>();
        params.put("project_id", m_projectId);
        params.put("result_files_dir",m_filePath );
        params.put("instrument_config_id", m_instrumentId);
        if ((m_accessionRegexp != null) && !m_accessionRegexp.isEmpty())
            params.put("accession_regexp", m_accessionRegexp);
        params.put("import_quant_result", m_importQuantResult);
        return params;
    }
    
    
    
}
