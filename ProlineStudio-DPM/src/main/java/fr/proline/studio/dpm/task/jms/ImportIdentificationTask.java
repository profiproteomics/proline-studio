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
import java.util.List;
import java.util.Map;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

/**
 * Task to import identifications from files
 *
 * @author VD225637
 */
public class ImportIdentificationTask extends AbstractJMSTask {

    private String m_parserId;
    private HashMap<String, String> m_parserArguments;
    private String m_filePath;
    private String m_decoyRegex;
    private long m_instrumentId;
    private long m_peaklistSoftwareId;
    private long m_projectId;
    private boolean m_saveSpectrumMatches;
    private Long[] m_resultSetId = null;

    public ImportIdentificationTask(AbstractJMSCallback callback, String parserId, HashMap<String, String> parserArguments, String filePath, String decoyRegex, long instrumentId, long peaklistSoftwareId, boolean saveSpectrumMatches, long projectId, Long[] resultSetId) {
        super(callback, new TaskInfo("Import Identification " + filePath, true, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_HIGH));

        m_parserId = parserId;
        m_parserArguments = parserArguments;
        m_filePath = filePath;
        m_decoyRegex = decoyRegex;
        m_instrumentId = instrumentId;
        m_peaklistSoftwareId = peaklistSoftwareId;
        m_saveSpectrumMatches = saveSpectrumMatches;
        m_projectId = projectId;
        m_resultSetId = resultSetId;
    }

    @Override
    public void taskRun() throws JMSException {

        final JSONRPC2Request jsonRequest = new JSONRPC2Request(JMSConnectionManager.PROLINE_PROCESS_METHOD_NAME, Integer.valueOf(m_id));
        jsonRequest.setNamedParams(createParams());

        final TextMessage message = AccessJMSManagerThread.getAccessJMSManagerThread().getSession().createTextMessage(jsonRequest.toJSONString());

        /* ReplyTo = Temporary Destination Queue for Server -> Client response */
        message.setJMSReplyTo(m_replyQueue);
	  message.setStringProperty(JMSConnectionManager.PROLINE_SERVICE_NAME_KEY, "proline/dps/msi/ImportResultFiles");
        addSourceToMessage(message);
        
        setTaskInfoRequest(message.getText());
        //  Send the Message
        m_producer.send(message);
        m_loggerProline.info("Message [{}] sent", message.getJMSMessageID());

    }

    private HashMap<String, Object> createParams() {
        HashMap<String, Object> params = new HashMap<>();
        params.put("project_id", m_projectId);

        List args = new ArrayList();

        // add the file to parse
        Map<String, Object> resultfile = new HashMap<>();
        resultfile.put("path", m_filePath);  // files must be accessible from web-core by the same path
        resultfile.put("format", m_parserId);
        if (m_decoyRegex != null) {
            resultfile.put("decoy_strategy", m_decoyRegex);
        }
        args.add(resultfile);
        params.put("result_files", args);

        params.put("instrument_config_id", m_instrumentId);
        params.put("peaklist_software_id", m_peaklistSoftwareId);

        params.put("save_spectrum_matches", m_saveSpectrumMatches);

        // parser arguments
        params.put("importer_properties", m_parserArguments);

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

            final Object result = jsonResponse.getResult();
            if ((result == null) || (!ArrayList.class.isInstance(result))) {
                m_loggerProline.error(getClass().getSimpleName() + " failed : No returned values");
                throw new Exception("Invalid result " + result);
            }

            ArrayList returnedValues = (ArrayList) result;
            HashMap returnedValuesMap = (HashMap) returnedValues.get(0);

            // retrieve resultSet id
            Long resultSetIdBD = (Long) returnedValuesMap.get("target_result_set_id");
            if (resultSetIdBD == null) {
                m_loggerProline.error(getClass().getSimpleName() + " failed : No returned ResultSet Id");
                throw new Exception("Import result error : No returned ResultSet Id");
            }

            m_resultSetId[0] = resultSetIdBD;
        }

        m_currentState = JMSState.STATE_DONE;
    }

}
