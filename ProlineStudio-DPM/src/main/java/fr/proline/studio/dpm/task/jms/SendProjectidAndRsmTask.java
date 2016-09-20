package fr.proline.studio.dpm.task.jms;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Message;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Notification;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dpm.jms.AccessJMSManagerThread;
import static fr.proline.studio.dpm.task.jms.AbstractJMSTask.m_loggerProline;
import fr.proline.studio.dpm.task.util.JMSConnectionManager;
import java.util.HashMap;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

/**
 * Task to get the project id and the result syummary id 
 * 
 */
public class SendProjectidAndRsmTask extends AbstractJMSTask {

    private DDataset m_dataset;
    public SendProjectidAndRsmTask(AbstractJMSCallback callback, DDataset dataset) {
        super(callback, new TaskInfo("send ProjectId and result Summary Id" + dataset.getName(), true, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_HIGH));
        m_dataset = dataset;
    }
    @Override
    public void taskRun() throws JMSException {
    	
        final JSONRPC2Request jsonRequest = new JSONRPC2Request(JMSConnectionManager.PROLINE_PROCESS_METHOD_NAME, Integer.valueOf(m_taskInfo.getId()));
        jsonRequest.setNamedParams(createParams());
        final TextMessage message = AccessJMSManagerThread.getAccessJMSManagerThread().getSession().createTextMessage(jsonRequest.toJSONString());
        message.setJMSReplyTo(m_replyQueue);
        message.setStringProperty(JMSConnectionManager.PROLINE_SERVICE_NAME_KEY, "proline/dps/msi/GetProteinSequence");
        addSourceToMessage(message);  
        addDescriptionToMessage(message);
        setTaskInfoRequest(message.getText());
        m_producer.send(message);
        m_loggerProline.info("Message [{}] sent", message.getJMSMessageID());
        m_taskInfo.setJmsMessageID(message.getJMSMessageID());    
    }
    
    private HashMap<String, Object> createParams() {
        HashMap<String, Object> params = new HashMap<>();
        params.put("project_id", m_dataset.getProject().getId());
        params.put("result_summary_id", m_dataset.getResultSummaryId());
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
