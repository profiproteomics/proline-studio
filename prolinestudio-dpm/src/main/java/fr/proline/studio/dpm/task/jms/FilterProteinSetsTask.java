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
import fr.proline.studio.dam.memory.TransientMemoryCacheManager;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dpm.task.util.JMSConnectionManager;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Task to filter RSM proteinSets
 *
 * @author VD225637
 */
public class FilterProteinSetsTask extends AbstractJMSTask {

    private static final String m_serviceName = "proline/dps/msi/FilterRSMProteinSets";
    //private static final String m_version = "2.0";
    
    private final DDataset m_dataset;
    private final HashMap<String, String> m_argumentsMap;

    public enum Filter {

        SPECIFIC_PEP("SPECIFIC_PEP", "Specific Peptides"),
        PEP_COUNT("PEP_COUNT", "Peptides count"),
        PEP_SEQ_COUNT("PEP_SEQ_COUNT", "Peptide sequence count"),
        SCORE("SCORE", "Protein Set Score");

        public final String key;
        public final String name;

        Filter(String key, String name) {
            this.key = key;
            this.name = name;
        }
    }

    public FilterProteinSetsTask(AbstractJMSCallback callback, DDataset dataset, HashMap<String, String> argumentsMap) {
        super(callback, new TaskInfo( "PSMFilter Protein Sets of Identification Summary "+dataset.getName(), true, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_HIGH));
        m_argumentsMap = argumentsMap;
        m_dataset = dataset; 
    }
    
    
    @Override
    public void taskRun() throws JMSException {
        final JSONRPC2Request jsonRequest = new JSONRPC2Request(JMSConnectionManager.PROLINE_PROCESS_METHOD_NAME, Integer.valueOf(m_taskInfo.getId()));
        jsonRequest.setNamedParams(createParams());
           
        final TextMessage message = m_session.createTextMessage(jsonRequest.toJSONString());

        /* ReplyTo = Temporary Destination Queue for Server -> Client response */
        message.setJMSReplyTo(m_replyQueue);
        message.setStringProperty(JMSConnectionManager.PROLINE_SERVICE_NAME_KEY, m_serviceName);
        //message.setStringProperty(JMSConnectionManager.PROLINE_SERVICE_VERSION_KEY, m_version);
        addSupplementaryInfo(message);

        setTaskInfoRequest(message.getText());
	
        //  Send the Message
        m_producer.send(message);
        m_loggerProline.info("Message [{}] sent", message.getJMSMessageID());
        m_taskInfo.setJmsMessageID(message.getJMSMessageID());
    }
    
    private Map<String, Object> createParams() {
        Map<String, Object> params = new HashMap<>();

        params.put("project_id", m_dataset.getProject().getId());
        params.put("result_summary_id", m_dataset.getResultSummaryId());

        // Protein Pre-PSMFilter
        List<Map<String, Object>> proteinFilters = new ArrayList<>();
        for (Filter filter : Filter.values()) {
            if (m_argumentsMap.containsKey(filter.key)) {
                Map<String, Object> filterCfg = new HashMap<>();
                filterCfg.put("parameter", filter.key);
                if(filter == Filter.SCORE) {
                    filterCfg.put("threshold", Double.valueOf(m_argumentsMap.get(filter.key)));
                }  else {
                    filterCfg.put("threshold", Integer.valueOf(m_argumentsMap.get(filter.key)));
                }
                proteinFilters.add(filterCfg);
            }
        }
        params.put("prot_set_filters", proteinFilters);

        return params;
    }

    @Override
    public void processWithResult(JSONRPC2Response jsonResponse) throws Exception {

        final Object result = jsonResponse.getResult();
        if (result == null || !Boolean.class.isInstance(result)) {
            m_loggerProline.debug("Invalid result");
            throw new Exception("Invalid result " + result);
        } else {
            m_loggerProline.debug("Result :\n" + result);
            if (m_dataset.getResultSummary() != null) {
                m_dataset.getResultSummary().getTransientData(TransientMemoryCacheManager.getSingleton()).setProteinSetArray(null);
            }
        }
    }
    
}
