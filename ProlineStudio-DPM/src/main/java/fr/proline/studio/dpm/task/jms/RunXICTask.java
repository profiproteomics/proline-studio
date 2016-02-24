package fr.proline.studio.dpm.task.jms;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Message;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Notification;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dpm.jms.AccessJMSManagerThread;
import static fr.proline.studio.dpm.task.jms.AbstractJMSTask.m_loggerProline;
import fr.proline.studio.dpm.task.util.JMSConnectionManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

/**
 * XIC Quantitation Task JMS
 */
public class RunXICTask extends AbstractJMSTask {
    private static final String m_serviceName = "proline/dps/msq/Quantify";
    private static final String m_existingRSM_version = "2.0";
    
    private Long[] m_xicQuantiResult = null;
    private HashMap<String, ArrayList<String>> m_samplesByGroup;
    private HashMap<String, ArrayList<String>> m_samplesAnalysisBySample;
    private HashMap<String, Long> m_rsmIdBySampleAnalysis;
    private String m_quantiDSName;
    private Long m_pId;
    private Map<String,Object> m_quantParams;
    private Map<String,Object> m_expDesignParams;
    private boolean m_useExistingRSM =false;
    
    public RunXICTask(AbstractJMSCallback callback, boolean mergedRSMSpecified, Long projectId,  String quantDSName,  Map<String,Object> quantParams, Map<String,Object> expDesignParams, Long[] retValue) {
        super(callback, new TaskInfo("Run XIC Quantitation for "+quantDSName, true, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_HIGH));
        m_xicQuantiResult = retValue;     
        m_expDesignParams = expDesignParams;
        m_quantiDSName = quantDSName;
        m_pId= projectId;
        m_quantParams = quantParams;
        m_useExistingRSM = mergedRSMSpecified;
    }
    
    
    @Override
    public void taskRun() throws JMSException {
        final JSONRPC2Request jsonRequest = new JSONRPC2Request(JMSConnectionManager.PROLINE_PROCESS_METHOD_NAME, Integer.valueOf(m_id));
        jsonRequest.setNamedParams(createParams());
           
        final TextMessage message = AccessJMSManagerThread.getAccessJMSManagerThread().getSession().createTextMessage(jsonRequest.toJSONString());

        /* ReplyTo = Temporary Destination Queue for Server -> Client response */
        message.setJMSReplyTo(m_replyQueue);
        message.setStringProperty(JMSConnectionManager.PROLINE_SERVICE_NAME_KEY, m_serviceName);
        if(m_useExistingRSM)
            message.setStringProperty(JMSConnectionManager.PROLINE_SERVICE_VERSION_KEY, m_existingRSM_version);
                
        setTaskInfoRequest(message.getText());
	
        //  Send the Message
        m_producer.send(message);
        m_loggerProline.info("Message [{}] sent", message.getJMSMessageID());
                    
    }
    
    private HashMap<String, Object> createParams() {
        HashMap<String, Object> params = new HashMap<>();
        //-- Global PARAMS
        params.put("name", m_quantiDSName);
        params.put("description", m_quantiDSName);
        params.put("project_id", m_pId);
        params.put("method_id", 1); //TODO Attention en dure !!! A lire la methode type = "label_free" & abundance_unit = "feature_intensity"
        params.put("experimental_design", m_expDesignParams);
        
        //extraire ref rsm/ds id from quant config
         if (m_useExistingRSM && !(m_quantParams.containsKey("ref_rsm_id") && m_quantParams.containsKey("ref_ds_id") )) 
             throw new RuntimeException(" Identification Summary and dataset references not specified");
         else {
            Object refRsm = m_quantParams.get("ref_rsm_id");
            Object refDs = m_quantParams.get("ref_ds_id");
            
            m_quantParams.remove("ref_rsm_id");
            m_quantParams.remove("ref_ds_id");
            params.put("ref_rsm_id", refRsm);
            params.put("ref_ds_id", refDs);
        }
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
