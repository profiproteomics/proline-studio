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
 * Task to export RSM data into "template" files
 *
 * @author VD225637
 */
public class ExportRSMTask extends AbstractJMSTask {

    public enum ExporterFormat {

        PRIDE,
        TEMPLATED,
        SPECTRA_LIST
    };

    private DDataset m_dataset;
    private String[] m_filePathResult;
    private String[] m_JMSNodeID;
    private boolean m_exportAllPSMs;
    private ExporterFormat m_exportFormat;
    HashMap<String, Object> m_exportParams;

    public ExportRSMTask(AbstractJMSCallback callback, DDataset dataset, boolean exportAllPSMs, String[] filePathInfo, String[] nodeIDInfo) {
        super(callback, new TaskInfo("Export Identification Summary " + dataset.getName(), true, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_HIGH));

        m_dataset = dataset;
        m_exportAllPSMs = exportAllPSMs;
        m_filePathResult = filePathInfo;
        m_JMSNodeID = nodeIDInfo;
        m_exportFormat = ExporterFormat.TEMPLATED;
        m_exportParams = null;
    }

    public ExportRSMTask(AbstractJMSCallback callback, DDataset dataset, boolean exportAllPSMs, String[] filePathInfo, String[] nodeIDInfo, ExporterFormat exportFormat, HashMap<String, Object> exportParams ) {
        super(callback, null);

        m_dataset = dataset;
        m_exportAllPSMs = exportAllPSMs;
        m_filePathResult = filePathInfo;
        m_JMSNodeID = nodeIDInfo;
        m_exportFormat = exportFormat;
        m_exportParams = exportParams;
        StringBuilder sb = new StringBuilder("Export Identification Summary ");
        sb.append(dataset.getName()).append("(");
        sb.append(m_exportFormat.toString()).append(" format)");
        super.setTaskInfo(new TaskInfo(sb.toString(), true, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_HIGH));
    }

    @Override
    public void taskRun() throws JMSException {

        final JSONRPC2Request jsonRequest = new JSONRPC2Request(JMSConnectionManager.PROLINE_PROCESS_METHOD_NAME, Integer.valueOf(m_id));
        jsonRequest.setNamedParams(createParams());

        final TextMessage message = AccessJMSManagerThread.getAccessJMSManagerThread().getSession().createTextMessage(jsonRequest.toJSONString());

        /* ReplyTo = Temporary Destination Queue for Server -> Client response */
        message.setJMSReplyTo(m_replyQueue);
        message.setStringProperty(JMSConnectionManager.PROLINE_SERVICE_NAME_KEY, "proline/dps/msi/ExportResultSummary");

        setTaskInfoRequest(message.getText());

        //  Send the Message
        m_producer.send(message);
        m_loggerProline.info("Message [{}] sent", message.getJMSMessageID());

    }

    private HashMap<String, Object> createParams() {

        HashMap<String, Object> params = new HashMap<>();
        params.put("file_format", m_exportFormat.toString());

        // **** Pour la version FILE :"file_name" & "file_directory" 
        params.put("output_mode", "STREAM"); // *** ou STREAM

        Map<String, Object> rsmIdent = new HashMap<>();
        rsmIdent.put("project_id", m_dataset.getProject().getId());
        rsmIdent.put("rsm_id", m_dataset.getResultSummaryId());
        params.put("rsm_identifier", rsmIdent);

        Map<String, Object> extraParams = new HashMap<>();
        switch (m_exportFormat) {
            
            case TEMPLATED: {
                /*
                 * "ALL_PEP_MATCHES_XLSX" -> AllPSMViewSetTemplateAsXLSX,
                 * "IRMA_LIKE_TSV" -> IRMaLikeViewSetTemplateAsTSV,
                 * "IRMA_LIKE_XLSX" -> IRMaLikeViewSetTemplateAsXLSX,
                 * "IRMA_LIKE_FULL_XLSX" -> IRMaLikeFullViewSetTemplateAsXLSX
                 */
                if (m_exportAllPSMs) {
                    extraParams.put("template_name", "IRMA_LIKE_FULL_XLSX"); //************ TODO Liste des templates possibles ?! 
                } else {
                    extraParams.put("template_name", "IRMA_LIKE_XLSX"); //************ TODO Liste des templates possibles ?! 
                }
                break;
            }

            case PRIDE: {
                extraParams.putAll(m_exportParams);
                // Transform sampleAdditional CVParam List to XMLString List
                if (m_exportParams.containsKey("sample_additional")) {
                    extraParams.remove("sample_additional");
                    List<CVParam> additionals = (List<CVParam>) m_exportParams.get("sample_additional");
                    List<String> additionalsXmlString = new ArrayList<>(additionals.size());
                    for (CVParam nextCVParam : additionals) {
                        additionalsXmlString.add(nextCVParam.toXMLString());
                    }
                    extraParams.put("sample_additional", additionalsXmlString);
                }
                
                // Transform protocol CVParam List to XMLString List
                if (m_exportParams.containsKey("protocol_steps")) {
                    extraParams.remove("protocol_steps");
                    List<CVParam> additionals = (List<CVParam>) m_exportParams.get("protocol_steps");
                    List<String> additionalsXmlString = new ArrayList<>(additionals.size());
                    additionals.stream().forEach((nextCVParam) -> {
                        additionalsXmlString.add(nextCVParam.toXMLString());
                    });
                    extraParams.put("protocol_steps", additionalsXmlString);
                }
                break;
            }
            
            case SPECTRA_LIST:{
                extraParams.putAll(m_exportParams);
                break;
            }
        }

        params.put("extra_params", extraParams);
        return params;
    }

    @Override
    public void taskDone(final Message jmsMessage) throws Exception {

        final TextMessage textMessage = (TextMessage) jmsMessage;
        final String jsonString = textMessage.getText();

        final JSONRPC2Message jsonMessage = JSONRPC2Message.parse(jsonString);
        if (jsonMessage instanceof JSONRPC2Notification) {
            m_loggerProline.warn("JSON Notification method: " + ((JSONRPC2Notification) jsonMessage).getMethod() + " instead of JSON Response");
            throw new Exception("Invalid JSONRPC2Message type");

        } else if (jsonMessage instanceof JSONRPC2Response) {
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
            if ((result == null) || (!HashMap.class.isInstance(result))) {
                m_loggerProline.error(getClass().getSimpleName() + " failed : No valid file path / information returned :" + result);
                throw new Exception("No  file path returned : " + result);
            }

            HashMap returnedValues = (HashMap) result;
            m_filePathResult[0] = (String) ((ArrayList) returnedValues.get("file_paths")).get(0);
            m_JMSNodeID[0] = (String) returnedValues.get(JMSConnectionManager.PROLINE_NODE_ID_KEY);
        }

        m_currentState = JMSState.STATE_DONE;
    }

}
