/* 
 * Copyright (C) 2019
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1 
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License 
 * along with this program; If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */
package fr.proline.studio.dpm.task.jms;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dpm.task.util.JMSConnectionManager;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import java.util.*;

/**
 * compute Spectral Count Quantitation via JMS
 *
 * @author MB243701
 */
public class SpectralCountTask extends AbstractJMSTask {

    private static final String m_serviceName = "proline/dps/msq/QuantifySC";
    private static final String m_version_2_0 = "2.0";

    private final DDataset m_refDataset;
    private final List<DDataset> m_rsmDataset;
    private final List<DDataset> m_rsmWeightDataset;
    private final Long[] m_quantiDatasetId;
    private String m_dsName;
    private String m_dsDescr;

    public SpectralCountTask(AbstractJMSCallback callback, DDataset refDataset, List<DDataset> rsmDataset, List<DDataset> rsmWeightDataset, String dsName, String dsDescr, Long[] quantiDatasetId) {
        super(callback, new TaskInfo("Spectral Count on " + refDataset.getName(), true, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_HIGH));
        m_refDataset = refDataset;
        m_rsmDataset = rsmDataset;
        m_rsmWeightDataset = rsmWeightDataset;
        m_quantiDatasetId = quantiDatasetId;
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
        final JSONRPC2Request jsonRequest = new JSONRPC2Request(JMSConnectionManager.PROLINE_PROCESS_METHOD_NAME, Integer.valueOf(m_taskInfo.getId()));
        jsonRequest.setNamedParams(createParams());

        final TextMessage message = m_session.createTextMessage(jsonRequest.toJSONString());

        /* ReplyTo = Temporary Destination Queue for Server -> Client response */
        message.setJMSReplyTo(m_replyQueue);
        message.setStringProperty(JMSConnectionManager.PROLINE_SERVICE_NAME_KEY, m_serviceName);
        message.setStringProperty(JMSConnectionManager.PROLINE_SERVICE_VERSION_KEY, m_version_2_0);

        addSupplementaryInfo(message);

        setTaskInfoRequest(message.getText());

        //  Send the Message
        m_producer.send(message);
        m_loggerProline.info("Message [{}] sent", message.getJMSMessageID());
        m_taskInfo.setJmsMessageID(message.getJMSMessageID());
    }

    private HashMap<String, Object> createParams() {
        HashMap<String, Object> params = new HashMap<>();
        params.put("name", m_dsName);
        params.put("description", m_dsDescr);
        params.put("project_id", m_refDataset.getProject().getId());

        List<Long> weightRefRSMIds = new ArrayList<>();
        for (DDataset ddset : m_rsmWeightDataset) {
            weightRefRSMIds.add(ddset.getResultSummaryId());
        }
        params.put("peptide_ref_rsm_ids", weightRefRSMIds);

        // experimental_design
        Map<String, Object> experimentalDesignParams = new HashMap<>();

        List<Integer> sampleNumbers = new ArrayList<>();
        List<Map<String, Object>> biologicalSampleList = new ArrayList<>();
        List<Map<String, Object>> quantChanneList = new ArrayList<>();
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
            quantChannelParams.put("name", name);
            quantChannelParams.put("ident_result_summary_id", d.getResultSummaryId());

            quantChanneList.add(quantChannelParams);

            sampleNumbers.add(Integer.valueOf(number));

            number++;
        }
        experimentalDesignParams.put("biological_samples", biologicalSampleList);

        List<Map<String, Object>> biologicalGroupList = new ArrayList<>();
        Map<String, Object> biologicalGroupParams = new HashMap<>();
        biologicalGroupParams.put("number", Integer.valueOf(0));
        biologicalGroupParams.put("name", m_refDataset.getName());
        biologicalGroupParams.put("sample_numbers", sampleNumbers);
        biologicalGroupList.add(biologicalGroupParams);
        experimentalDesignParams.put("biological_groups", biologicalGroupList);

        List<Map<String, Object>> masterQuantChannelsList = new ArrayList<>();
        Map<String, Object> masterQuantChannelParams = new HashMap<>();
        masterQuantChannelParams.put("number", 0);
        masterQuantChannelParams.put("name", m_refDataset.getName() + " Spectral Count");
        masterQuantChannelParams.put("ident_result_summary_id", m_refDataset.getResultSummaryId());
        masterQuantChannelParams.put("ident_dataset_id", m_refDataset.getId());
        masterQuantChannelParams.put("quant_channels", quantChanneList);
        masterQuantChannelsList.add(masterQuantChannelParams);
        experimentalDesignParams.put("master_quant_channels", masterQuantChannelsList);

        params.put("experimental_design", experimentalDesignParams);

        return params;
    }

    @Override
    public void processWithResult(JSONRPC2Response jsonResponse) throws Exception {
        final Object result = jsonResponse.getResult();
        if (result == null || !Long.class.isInstance(result)) {
            m_loggerProline.debug("Invalid result");
            throw new Exception("Invalid result " + result);
        } else {
            m_loggerProline.debug("Result :\n" + result);
            // retrieve Quanti Dataset ID
            m_quantiDatasetId[0] = (Long) result;
        }

    }
}
