package fr.proline.studio.dpm.task.jms;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import fr.proline.core.orm.uds.dto.DDataset;
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
  private final DDataset m_dataset;
  private final Object[] m_resultData;
  private final String m_filePath;
  private final String m_filterMode;

  /**
   * Call Import DiaNN Server Service using specified parameter.
   * Returned value from service will be parsed and stores in resultData which should contains at least 3 entries.
   * - Map of identification RS/RSM
   * - Parent Ident dataset id
   * - quant dataset id
   * @param callback Method to be called back once service has returned
   * @param filePath path, on server side, to the diaNN file to import
   * @param instrumentId InstrumentConfiguration Id associated to resultset to create
   * @param peaklistSoftwareId Peaklist software Id used to create peaklist (TODO : not for DiaNN !)
   * @param projectId Project  to import data to
   * @param resultData Object array to store result of server service call
   */
  public ImportDiaNNTask(AbstractJMSCallback callback, String filePath , long instrumentId, long peaklistSoftwareId, long projectId, DDataset parentDS, String filterMode, Object[] resultData) {
    super(callback, new TaskInfo("Import DiaNN Result "+filePath,true, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_HIGH));
    if(resultData.length<3)
      throw new IllegalArgumentException(" Result data array should contains 3 entries ");
    m_projectId = projectId;
    m_instrumentId = instrumentId;
    m_peaklistSoftwareId = peaklistSoftwareId;
    m_resultData = resultData;
    m_filePath = filePath;
    m_dataset= parentDS;
    m_filterMode = (filterMode == null) ? "NONE" : filterMode;
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
    if(!(result instanceof HashMap) || ((HashMap<?, ?>) result).isEmpty()) {
      m_loggerProline.error(getClass().getSimpleName() + " failed : Invalid returned values");
      throw new Exception("Invalid result " + result);
    }

    HashMap<String, Object> importResult = (HashMap) result;

    HashMap<Long,Long> rsmIds = (HashMap) importResult.get("rsm_ids_by_rs_id");
    Long quantDsid = (Long) importResult.get("quant_data_set_id");
    Long identDsid = (Long) importResult.get("ident_data_set_id");
    m_resultData[0] = rsmIds;
    m_resultData[1] = identDsid;
    m_resultData[2] = quantDsid;
  }

  private HashMap<String, Object> createParams() {
    HashMap<String, Object> params = new HashMap<>();
    params.put("project_id", m_projectId);
    params.put("result_files_dir",m_filePath );
    params.put("instrument_config_id", m_instrumentId);
    params.put("peaklist_software_id", m_peaklistSoftwareId);
    params.put("filter_mode", m_filterMode);
    if(m_dataset == null)
      params.put("parent_dataset_id", -1L);
    else
      params.put("parent_dataset_id", m_dataset.getId());
    return params;
  }
}
