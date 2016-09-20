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
import fr.proline.studio.dpm.task.util.JMSConnectionManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

/**
 *Task to export a dataset (it could be identification, XIC or SC...) It uses a
 * configuration file to specify the export. It will replace ExportXICTask and
 * ExportRSMTask
 * @author MB243701
 */
public class ExportDatasetTask extends AbstractJMSTask {

    public enum ExporterFormat {
        PRIDE,
        TEMPLATED,
        SPECTRA_LIST
    };
    private static String m_serviceName = "proline/dps/msi/ExportResultSummary";
    private static final String m_version = "2.0";
    
    private List<DDataset> m_datasetList;
    private List<String> m_filePathResult;
    private HashMap<String, Object> m_exportParams;
    private ExporterFormat m_exportFormat;
    private String m_configStr;
    private List<String> m_JMSNodeID;

    public ExportDatasetTask(AbstractJMSCallback callback, List<DDataset> listDataset, String configStr, List<String> filePathInfo, List<String> jmsNodeID) {
        super(callback, new TaskInfo("Export Dataset for " + listDataset.size() + " datasets", true, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_HIGH));
        
        m_datasetList = listDataset;
        m_filePathResult = filePathInfo;
        m_JMSNodeID = jmsNodeID;
        m_exportFormat = ExporterFormat.TEMPLATED;
        m_exportParams = null;
        m_configStr = configStr;
    }
    
    public ExportDatasetTask(AbstractJMSCallback callback, List<DDataset> listDataset, String configStr, List<String> filePathInfo, List<String> jmsNodeID, ExporterFormat exportFormat, HashMap<String, Object> exportParams) {
        super(callback, new TaskInfo("Export Dataset for " + listDataset.size() + " datasets", true, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_HIGH));
        
        m_datasetList = listDataset;
        m_filePathResult = filePathInfo;
        m_JMSNodeID = jmsNodeID;
        m_exportFormat = exportFormat;
        m_exportParams = exportParams;
        m_configStr = configStr;
    }
    
    @Override
    public void taskRun() throws JMSException {
        final JSONRPC2Request jsonRequest = new JSONRPC2Request(JMSConnectionManager.PROLINE_PROCESS_METHOD_NAME, Integer.valueOf(m_taskInfo.getId()));
        jsonRequest.setNamedParams(createParams());
           
        final TextMessage message = AccessJMSManagerThread.getAccessJMSManagerThread().getSession().createTextMessage(jsonRequest.toJSONString());

        /* ReplyTo = Temporary Destination Queue for Server -> Client response */
        message.setJMSReplyTo(m_replyQueue);
        message.setStringProperty(JMSConnectionManager.PROLINE_SERVICE_NAME_KEY, m_serviceName);
        message.setStringProperty(JMSConnectionManager.PROLINE_SERVICE_VERSION_KEY, m_version);
        addSourceToMessage(message);   
        addDescriptionToMessage(message);
        
        setTaskInfoRequest(message.getText());
	
        //  Send the Message
        m_producer.send(message);
        m_loggerProline.info("Message [{}] sent", message.getJMSMessageID());
        m_taskInfo.setJmsMessageID(message.getJMSMessageID());
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
            if ((result == null) || (! HashMap.class.isInstance(result)))  {
                m_loggerProline.error(getClass().getSimpleName() + " failed : No valid file path / information returned :"+result);
                throw new Exception("No  file path returned : "+result);
            }
            
            HashMap returnedValues = (HashMap) result;  
            ArrayList exportedFilePathList = (ArrayList)returnedValues.get("file_paths");
            if (exportedFilePathList == null || exportedFilePathList.isEmpty()) {
                m_loggerProline.error(getClass().getSimpleName() + " failed : No file path returned :"+result);
                throw new Exception("No  file path returned : "+result);
            }
            // in case of TSV format, we have different files to download
            for (Object filePath : exportedFilePathList) {
                m_filePathResult.add((String) filePath);
                m_JMSNodeID.add((String) returnedValues.get(JMSConnectionManager.PROLINE_NODE_ID_KEY));
            } 
        }
               
        m_currentState = JMSState.STATE_DONE;
    }
    
    private HashMap<String, Object> createParams() {
        HashMap<String, Object> params = new HashMap<>();
        params.put("file_format", m_exportFormat.toString());
        // **** Pour la version FILE :"file_name" & "file_directory" 
        params.put("output_mode", "STREAM"); // *** ou STREAM

        Map<String, Object> extraParams = new HashMap<>();

        switch (m_exportFormat) {
            case TEMPLATED: {
                extraParams.put("config", m_configStr);
                break;
            }
            case PRIDE: {
               
                HashMap<String, Object> finalExportParams = new HashMap<>();
                finalExportParams.putAll(m_exportParams);
                if (m_exportParams.containsKey("sample_additional")) {
                    finalExportParams.remove("sample_additional");
                    List<CVParam> additionals = (List<CVParam>) m_exportParams.get("sample_additional");
                    List<String> additionalsXmlString = new ArrayList<>(additionals.size());
                    for (CVParam nextCVParam : additionals) {
                        additionalsXmlString.add(nextCVParam.toXMLString());
                    }
                    finalExportParams.put("sample_additional", additionalsXmlString);
                }
                if (m_exportParams.containsKey("protocol_steps")) {
                    finalExportParams.remove("protocol_steps");
                    List<CVParam> additionals = (List<CVParam>) m_exportParams.get("protocol_steps");
                    List<String> additionalsXmlString = new ArrayList<>(additionals.size());
                    for (CVParam nextCVParam : additionals) {
                        additionalsXmlString.add(nextCVParam.toXMLString());
                    }
                    finalExportParams.put("protocol_steps", additionalsXmlString);
                }
                extraParams = finalExportParams;                
                 break;
            }
            
            case SPECTRA_LIST:{
                if(m_exportParams != null)
                    extraParams.putAll(m_exportParams);
                break;
            }
        }


        List<Map<String, Object>> rsmIdents = new ArrayList();
        for (DDataset dataset : m_datasetList) {
            Map<String, Object> rsmIdent = new HashMap<>();
            rsmIdent.put("project_id", dataset.getProject().getId());
            rsmIdent.put("ds_id", dataset.getId());
            Long rsmId = dataset.getResultSummaryId();
            if (dataset.getMasterQuantitationChannels() != null && !dataset.getMasterQuantitationChannels().isEmpty()) {
                rsmId = dataset.getMasterQuantitationChannels().get(0).getQuantResultSummaryId();
            }
            rsmIdent.put("rsm_id", rsmId);
            rsmIdents.add(rsmIdent);
        }
        params.put("rsm_identifiers", rsmIdents);
       
        params.put("extra_params", extraParams);
        return params;
    }
    
}
