package fr.proline.studio.dpm.task.jms;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Message;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Notification;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dpm.AccessJMSManagerThread;
import fr.proline.studio.dpm.task.util.JMSConnectionManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

/**
 * Task to generate spectrum matches
 * @author VD225637
 */
public class IdentifyPtmSitesTask extends AbstractJMSTask {
    private static final String m_serviceName = "proline/dps/msi/IdentifyPtmSites";
    //private static final String m_version = "1.0";
    
    private Long m_projectId;
    private Long m_resultSummaryId;
    private Boolean m_forceAction;
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
           
        final TextMessage message = AccessJMSManagerThread.getAccessJMSManagerThread().getSession().createTextMessage(jsonRequest.toJSONString());

        /* ReplyTo = Temporary Destination Queue for Server -> Client response */
        message.setJMSReplyTo(m_replyQueue);
        message.setStringProperty(JMSConnectionManager.PROLINE_SERVICE_NAME_KEY, m_serviceName);
        if(m_version != null )
            message.setStringProperty(JMSConnectionManager.PROLINE_SERVICE_VERSION_KEY, m_version);
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
	    }
        }
          m_currentState = JMSState.STATE_DONE;
        
    }
    
}
