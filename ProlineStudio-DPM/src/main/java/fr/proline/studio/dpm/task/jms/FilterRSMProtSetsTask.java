package fr.proline.studio.dpm.task.jms;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Message;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Notification;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dpm.jms.AccessJMSManagerThread;
import static fr.proline.studio.dpm.task.FilterRSMProtSetsTask.FILTER_KEYS;
import static fr.proline.studio.dpm.task.jms.AbstractJMSTask.m_loggerProline;
import fr.proline.studio.dpm.task.util.JMSConnectionManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    
    public FilterRSMProtSetsTask(AbstractJMSCallback callback,  DDataset dataset,HashMap<String, String> argumentsMap) {
        super(callback, new TaskInfo( "Filter Protein Sets of Identifcation Summary "+dataset.getName(), true, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_HIGH));
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
                filterCfg.put("threshold", Integer.valueOf(m_argumentsMap.get(filterKey)));
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
                    m_dataset.getResultSummary().getTransientData().setProteinSetArray(null);
                }
	    }
        }
          m_currentState = JMSState.STATE_DONE;
        
    }
    
}
