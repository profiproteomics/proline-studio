package fr.proline.studio.dpm.task.jms;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Message;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Notification;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dpm.data.CVParam;
import fr.proline.studio.dpm.jms.AccessJMSManagerThread;
import static fr.proline.studio.dpm.task.jms.AbstractJMSTask.m_loggerProline;
import fr.proline.studio.dpm.task.util.JMSConstants;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

/**
 * Task to export RSM data into "template" files
 * @author VD225637
 */
public class ExportRSM2PrideTask extends AbstractJMSTask {
    
    private DDataset m_dataset;
    HashMap<String,Object> m_exportParams;
    private String[] m_filePathResult;
    private String[] m_JMSNodeID;

    
    public ExportRSM2PrideTask(AbstractJMSCallback callback, DDataset dataset, HashMap<String,Object> exportParams, String[] filePathInfo, String[] nodeIDInfo) {
        super(callback,  new TaskInfo("Pride Export: Identification Summary "+dataset.getName(), true, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_HIGH));
        
        m_dataset = dataset;        
        m_filePathResult = filePathInfo;
        m_JMSNodeID = nodeIDInfo;
        m_exportParams = exportParams;
    }
    
    @Override
    public void taskRun() throws JMSException {
        
        final JSONRPC2Request jsonRequest = new JSONRPC2Request(JMSConstants.PROLINE_PROCESS_METHOD_NAME, Integer.valueOf(m_id));
        jsonRequest.setNamedParams(createParams());
           
        final TextMessage message = AccessJMSManagerThread.getAccessJMSManagerThread().getSession().createTextMessage(jsonRequest.toJSONString());

        /* ReplyTo = Temporary Destination Queue for Server -> Client response */
        message.setJMSReplyTo(m_replyQueue);
        message.setStringProperty(JMSConstants.PROLINE_SERVICE_NAME_KEY, "proline/dps/msi/ExportResultSummary");
	
        super.setTaskInfoRequest(message.getText());
        
        //  Send the Message
        m_producer.send(message);
        m_loggerProline.info("Message [{}] sent", message.getJMSMessageID());
        
    }
    
    private HashMap<String, Object> createParams() {
        
        HashMap<String, Object> params = new HashMap<>();
        params.put("file_format", "PRIDE"); 
        // **** Pour la version FILE :"file_name" & "file_directory" 
        params.put("output_mode", "STREAM"); // *** ou STREAM
            
        Map<String, Object> rsmIdent = new HashMap<>();
        rsmIdent.put("project_id", m_dataset.getProject().getId()); 
        rsmIdent.put("rsm_id", m_dataset.getResultSummaryId());
        params.put("rsm_identifier",rsmIdent);

        HashMap<String,Object> finalExportParams = new HashMap<>();
        finalExportParams.putAll(m_exportParams);
        if(m_exportParams.containsKey("sample_additional")){            
            finalExportParams.remove("sample_additional");
            List<CVParam> additionals =  (List<CVParam>) m_exportParams.get("sample_additional");
            List<String> additionalsXmlString = new ArrayList<>(additionals.size());
            for(CVParam nextCVParam : additionals){
                additionalsXmlString.add(nextCVParam.toXMLString());
            }
            finalExportParams.put("sample_additional", additionalsXmlString);
            
        }
       
        params.put("extra_params", finalExportParams);

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
            
            // FOR STREAM ONLY !
            final Object result = jsonResponse.getResult();
            if ((result == null) || (! HashMap.class.isInstance(result)))  {
                m_loggerProline.error(getClass().getSimpleName() + " failed : No valid file path / inforamtion returned :"+result);
                throw new Exception("No  file path returned : "+result);
            }
            
            HashMap returnedValues = (HashMap) result;                    
            m_filePathResult[0] = (String) ((ArrayList)returnedValues.get("file_paths")).get(0);
            m_JMSNodeID[0] = (String) returnedValues.get(JMSConstants.PROLINE_NODE_ID_KEY);
        }
               
        m_currentState = JMSState.STATE_DONE;
    }
    
}
