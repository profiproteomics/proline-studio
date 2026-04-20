package fr.proline.studio.dpm.task.jms;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dpm.task.util.JMSConnectionManager;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ImportDiaNNTask extends AbstractJMSTask {

  private final long m_projectId;
  private final long m_instrumentId;
  private final long m_peaklistSoftwareId;
  private final Object[] m_resultData;
  private final String m_filePath;

  public ImportDiaNNTask(AbstractJMSCallback callback,String filePath , long instrumentId, long peaklistSoftwareId, long projectId, Object[] resultData) {
    super(callback, new TaskInfo("Import DiaNN Result "+filePath,true, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_HIGH));

    m_projectId = projectId;
    m_instrumentId = instrumentId;
    m_peaklistSoftwareId = peaklistSoftwareId;
    m_resultData = resultData;
    m_filePath = filePath;
  }

  @Override
  public void taskRun() throws JMSException {

    final JSONRPC2Request jsonRequest = new JSONRPC2Request(JMSConnectionManager.PROLINE_PROCESS_METHOD_NAME, Integer.valueOf(m_taskInfo.getId()));
    jsonRequest.setNamedParams(createParams());

    final TextMessage message = m_session.createTextMessage(jsonRequest.toJSONString());

    /* ReplyTo = Temporary Destination Queue for Server -> Client response */
    message.setJMSReplyTo(m_replyQueue);
    message.setStringProperty(JMSConnectionManager.PROLINE_SERVICE_NAME_KEY, "proline/dps/msq/ImportDiaNNResults");
    addSupplementaryInfo(message, m_projectId);

    setTaskInfoRequest(message.getText());
    //  Send the Message
    m_producer.send(message);
    m_loggerProline.info("ImportDiaNNTask Message [{}] sent", message.getJMSMessageID());
    m_taskInfo.setJmsMessageID(message.getJMSMessageID());
  }

  @Override
  public void processWithResult(JSONRPC2Response jsonResponse) throws Exception {
    final Object result = jsonResponse.getResult();
    if(!(result instanceof HashMap) || ((HashMap) result).isEmpty()) {
      m_loggerProline.error(getClass().getSimpleName() + " failed : Invalid returned values");
      throw new Exception("Invalid result " + result);
    }

    HashMap<String, Object> importResult = (HashMap) result;

    HashMap<Long, Long> rsmIds = (HashMap<Long, Long>) importResult.get("rsm_ids_by_rs_id");
    Long quantDsds = (Long) importResult.get("quant_data_set_id");
    m_resultData[0] = rsmIds;
    m_resultData[1] = quantDsds;
  }

  private HashMap<String, Object> createParams() {
    HashMap<String, Object> params = new HashMap<>();
    params.put("project_id", m_projectId);
    params.put("result_files_dir",m_filePath );
    params.put("instrument_config_id", m_instrumentId);
    params.put("peaklist_software_id", m_peaklistSoftwareId);
    return params;
  }
}
