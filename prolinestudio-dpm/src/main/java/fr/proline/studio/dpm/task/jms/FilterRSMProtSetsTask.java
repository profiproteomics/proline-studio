/* 
 * Copyright (C) 2019 VD225637
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
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.memory.TransientMemoryCacheManager;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dpm.AccessJMSManagerThread;
import static fr.proline.studio.dpm.task.jms.AbstractJMSTask.m_loggerProline;
import fr.proline.studio.dpm.task.util.JMSConnectionManager;
import java.util.ArrayList;
import java.util.HashMap;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

/**
 * Task to generate spectrum matches
 * @author VD225637
 */
public class FilterRSMProtSetsTask extends AbstractJMSTask {
    private static final String m_serviceName = "proline/dps/msi/FilterRSMProteinSets";
    //private static final String m_version = "2.0";
    
    private DDataset m_dataset = null;
    private HashMap<String, String> m_argumentsMap;
    
    //Protein PreFilter
    public static String[] FILTER_KEYS = {"SPECIFIC_PEP","PEP_COUNT", "PEP_SEQ_COUNT", "SCORE", "BH_ADJUSTED_PVALUE"};//TODO USE ENUM
    public static String[] FILTER_NAME = {"Specific Peptides","Peptides count", "Peptide sequence count","Protein Set Score", "BH adjusted pValue (%)"};
    
    public FilterRSMProtSetsTask(AbstractJMSCallback callback,  DDataset dataset,HashMap<String, String> argumentsMap) {
        super(callback, new TaskInfo( "Filter Protein Sets of Identification Summary "+dataset.getName(), true, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_HIGH));
        m_argumentsMap = argumentsMap;
        m_dataset = dataset; 
    }
    
    
    @Override
    public void taskRun() throws JMSException {
        final JSONRPC2Request jsonRequest = new JSONRPC2Request(JMSConnectionManager.PROLINE_PROCESS_METHOD_NAME, Integer.valueOf(m_taskInfo.getId()));
        jsonRequest.setNamedParams(createParams());
           
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
    
    private HashMap<String, Object> createParams() {
        HashMap<String, Object> params = new HashMap<>();
        params.put("project_id", m_dataset.getProject().getId());
        params.put("result_summary_id", m_dataset.getResultSummaryId());

        // Protein Pre-Filters
        ArrayList proteinFilters = new ArrayList();
        for (String filterKey : FILTER_KEYS) {
            if (m_argumentsMap.containsKey(filterKey)) {
                HashMap filterCfg = new HashMap();
                filterCfg.put("parameter", filterKey);
                if(filterKey.equals("SCORE")) { //TODO USE ENUM
                    filterCfg.put("threshold", Double.valueOf(m_argumentsMap.get(filterKey)));
                } if (filterKey.equals("BH_ADJUSTED_PVALUE")) {
                    filterCfg.put("threshold", Double.valueOf(m_argumentsMap.get(filterKey))/100.0);
                }else {
                    filterCfg.put("threshold", Integer.valueOf(m_argumentsMap.get(filterKey)));
                }
                proteinFilters.add(filterCfg);
            }
        }
        params.put("prot_set_filters", proteinFilters);

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
            if (result == null || ! Boolean.class.isInstance(result) ) {
		m_loggerProline.debug("Invalid result");
                throw new Exception("Invalid result "+result);
	    } else {
		m_loggerProline.debug("Result :\n" + result); 
                if(m_dataset.getResultSummary() != null){
                    m_dataset.getResultSummary().getTransientData(TransientMemoryCacheManager.getSingleton()).setProteinSetArray(null);
                }
	    }
        }
          m_currentState = JMSState.STATE_DONE;
        
    }
    
}
