package fr.proline.studio.dpm.task.jms;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Message;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Notification;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import fr.proline.core.orm.uds.dto.DDataset;
import java.util.List;
import javax.jms.JMSException;
import javax.jms.Message;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dpm.jms.AccessJMSManagerThread;
import static fr.proline.studio.dpm.task.jms.AbstractJMSTask.m_loggerProline;
import fr.proline.studio.dpm.task.util.JMSConnectionManager;
import java.util.HashMap;
import java.util.Map;
import javax.jms.TextMessage;

/**
 * task to retrieve the information for export configuration
 * @author MB243701
 */
public class GetExportInformationTask extends AbstractJMSTask {
    private static String m_request = "proline/dps/uds/GetExportInformation";
    private DDataset m_dataset;
    private String m_mode;

    // contains the json string representing the configuration
    private List<String> m_customizableExport;
    
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
        final JSONRPC2Request jsonRequest = new JSONRPC2Request(JMSConnectionManager.PROLINE_PROCESS_METHOD_NAME, Integer.valueOf(m_id));
        jsonRequest.setNamedParams(createParams());
           
        final TextMessage message = AccessJMSManagerThread.getAccessJMSManagerThread().getSession().createTextMessage(jsonRequest.toJSONString());

        /* ReplyTo = Temporary Destination Queue for Server -> Client response */
        message.setJMSReplyTo(m_replyQueue);
        message.setStringProperty(JMSConnectionManager.PROLINE_SERVICE_NAME_KEY, m_request);
        
        setTaskInfoRequest(message.getText());
	
        //  Send the Message
        m_producer.send(message);
        m_loggerProline.info("Message [{}] sent", message.getJMSMessageID());
    }

    @Override
    public void taskDone(Message jmsMessage) throws Exception {
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
            
            // FOR STREAM ONLY !
            final Object result = jsonResponse.getResult();
            if ((result == null) || (! String.class.isInstance(result)))  {
                m_loggerProline.error(getClass().getSimpleName() + " failed : No valid configuration file / information returned :"+result);
                throw new Exception("No valid configuration returned : "+result);
            }
                  
            // retrieve configuration 
            String configStr = (String) result;
            if (configStr.isEmpty()) {
                m_loggerProline.error(getClass().getSimpleName() + " failed : No configuration returned.");
               throw new Exception("No configuration returned. "+result);
            }
            m_customizableExport.add(configStr);
        }
               
        m_currentState = JMSState.STATE_DONE;
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
