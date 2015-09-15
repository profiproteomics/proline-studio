/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.dpm.task.jms;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Message;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Notification;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dpm.jms.AccessJMSManagerThread;
import static fr.proline.studio.dpm.task.jms.AbstractJMSTask.TASK_LIST_INFO;
import static fr.proline.studio.dpm.task.jms.AbstractJMSTask.m_loggerProline;
import fr.proline.studio.dpm.task.util.JMSConnectionManager;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

/**
 * compute Spectral Count Quantitation via JMS
 *
 * @author MB243701
 */
public class SpectralCountTask extends AbstractJMSTask {

    private static final String m_serviceName = "proline/dps/msq/QuantifySC";
    //private static final String m_version = "2.0";

    private DDataset m_refDataset = null;
    private List<DDataset> m_rsmDataset = null;
    private List<DDataset> m_rsmWeightDataset = null;
    private Long[] m_quantiDatasetId = null;
    private String[] m_spCountJSONResult = null;
    private String m_dsName = null;
    private String m_dsDescr = null;

    public SpectralCountTask(AbstractJMSCallback callback, DDataset refDataset, List<DDataset> rsmDataset, List<DDataset> rsmWeightDataset, String dsName, String dsDescr, Long[] quantiDatasetId, String[] spectralCountResultList) {
        super(callback, new TaskInfo("Spectral Count on " + refDataset.getName(), true, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_HIGH));
        m_refDataset = refDataset;
        m_rsmDataset = rsmDataset;
        m_rsmWeightDataset = rsmWeightDataset;
        m_quantiDatasetId = quantiDatasetId;
        m_spCountJSONResult = spectralCountResultList;
        m_dsName = dsName;
        m_dsDescr = dsDescr;
        if (m_dsName == null || m_dsName.isEmpty()) {
            m_dsName = m_refDataset.getName() + " Spectral Count";
        }

        if (m_dsDescr == null || m_dsDescr.isEmpty()) {
            m_dsDescr = m_dsName;
        }
    }

    @Override
    public void taskRun() throws JMSException {
        final JSONRPC2Request jsonRequest = new JSONRPC2Request(JMSConnectionManager.PROLINE_PROCESS_METHOD_NAME, Integer.valueOf(m_id));
        jsonRequest.setNamedParams(createParams());

        final TextMessage message = AccessJMSManagerThread.getAccessJMSManagerThread().getSession().createTextMessage(jsonRequest.toJSONString());

        /* ReplyTo = Temporary Destination Queue for Server -> Client response */
        message.setJMSReplyTo(m_replyQueue);
        message.setStringProperty(JMSConnectionManager.PROLINE_SERVICE_NAME_KEY, m_serviceName);
        //message.setStringProperty(JMSConnectionManager.PROLINE_SERVICE_VERSION_KEY, m_version);

        setTaskInfoRequest(message.getText());

        //  Send the Message
        m_producer.send(message);
        m_loggerProline.info("Message [{}] sent", message.getJMSMessageID());

    }

    private HashMap<String, Object> createParams() {
        HashMap<String, Object> params = new HashMap<>();
        params.put("name", m_dsName);
        params.put("description", m_dsDescr);
        params.put("project_id", m_refDataset.getProject().getId());
        params.put("ref_rsm_id", m_refDataset.getResultSummaryId());
        params.put("ref_ds_id", m_refDataset.getId());
        List<Long> weightRefRSMIds = new ArrayList<>();
        for (DDataset ddset : m_rsmWeightDataset) {
            weightRefRSMIds.add(ddset.getResultSummaryId());
        }
        params.put("peptide_ref_rsm_ids", weightRefRSMIds);

        // experimental_design
        Map<String, Object> experimentalDesignParams = new HashMap<>();

        List sampleNumbers = new ArrayList();
        List biologicalSampleList = new ArrayList();
        List quantChanneList = new ArrayList();
        int number = 1;
        Iterator<DDataset> itDataset = m_rsmDataset.iterator();
        while (itDataset.hasNext()) {
            DDataset d = itDataset.next();
            String name = d.getName();

            Map<String, Object> biologicalSampleParams = new HashMap<>();
            biologicalSampleParams.put("number", Integer.valueOf(number));
            biologicalSampleParams.put("name", name);

            biologicalSampleList.add(biologicalSampleParams);

            Map<String, Object> quantChannelParams = new HashMap<>();
            quantChannelParams.put("number", Integer.valueOf(number));
            quantChannelParams.put("sample_number", Integer.valueOf(number));
            quantChannelParams.put("ident_result_summary_id", d.getResultSummaryId());

            quantChanneList.add(quantChannelParams);

            sampleNumbers.add(Integer.valueOf(number));

            number++;
        }
        experimentalDesignParams.put("biological_samples", biologicalSampleList);

        List biologicalGroupList = new ArrayList();
        Map<String, Object> biologicalGroupParams = new HashMap<>();
        biologicalGroupParams.put("number", Integer.valueOf(0));
        biologicalGroupParams.put("name", m_refDataset.getName());
        biologicalGroupParams.put("sample_numbers", sampleNumbers);
        biologicalGroupList.add(biologicalGroupParams);
        experimentalDesignParams.put("biological_groups", biologicalGroupList);

        List masterQuantChannelsList = new ArrayList();
        Map<String, Object> masterQuantChannelParams = new HashMap<>();
        masterQuantChannelParams.put("number", 0);
        masterQuantChannelParams.put("name", m_refDataset.getName() + " Spectral Count");
        masterQuantChannelParams.put("quant_channels", quantChanneList);
        masterQuantChannelsList.add(masterQuantChannelParams);
        experimentalDesignParams.put("master_quant_channels", masterQuantChannelsList);

        params.put("experimental_design", experimentalDesignParams);

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
            if (result == null || !Map.class.isInstance(result)) {
                m_loggerProline.debug("Invalid result");
                throw new Exception("Invalid result " + result);
            } else {
                m_loggerProline.debug("Result :\n" + result);
                Map returnedValues = (Map) result;
                if (returnedValues.isEmpty()) {
                    m_loggerProline.error(getClass().getSimpleName() + " failed : No returned values");
                    m_currentState = JMSState.STATE_FAILED;
                    throw new Exception("No returned values " + result);
                }

                // retrieve Quanti Dataset ID
                Long quantiDatasetIdBD = (Long) returnedValues.get("quant_dataset_id");
                if (quantiDatasetIdBD == null) {
                    m_loggerProline.error(getClass().getSimpleName() + " failed : No returned Quanti Dataset Id");
                    m_currentState = JMSState.STATE_FAILED;
                    throw new Exception("No returned Quanti Dataset Id ");
                }
                m_quantiDatasetId[0] = quantiDatasetIdBD;

                //retrieve SC Values as JSON String 
                String scValues = (String) returnedValues.get("spectral_count_result");
                if (scValues == null) {
                    m_loggerProline.error(getClass().getSimpleName() + " failed : No Spectral Count returned.");
                    m_currentState = JMSState.STATE_FAILED;
                    throw new Exception("No Spectral Count returned.");
                }
                m_spCountJSONResult[0] = scValues;
            }
        }
        m_currentState = JMSState.STATE_DONE;

    }
}
