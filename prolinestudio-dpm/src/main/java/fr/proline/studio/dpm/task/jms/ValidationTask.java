/* 
 * Copyright (C) 2019 VD225637
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

import com.thetransactioncompany.jsonrpc2.*;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dpm.AccessJMSManagerThread;
import fr.proline.studio.dpm.task.util.JMSConnectionManager;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author JM235353
 */
public class ValidationTask extends AbstractJMSTask  {

    //PSM PreFilter

    public enum PSMFilter {

        RANK("PRETTY_RANK", "Pretty Rank"),
        SCORE("SCORE", "Score"),
        PEP_LENGTH("PEP_SEQ_LENGTH", "Length"),
        MASCOT_EVAL("MASCOT_EVALUE","e-Value"),
        MASCOT_ADJUSTED_EVALUE("MASCOT_ADJUSTED_EVALUE","Adjusted e-Value"),
        MASCOT_IT_SCORE("SCORE_IT_P-VALUE", "Identity p-Value"),
        MASCOT_HT_SCORE("SCORE_HT_P-VALUE", "Homology p-Value"),
        SINGLE_PSM_QUERY("SINGLE_PSM_PER_QUERY","Single PSM per MS Query"),
        SINGLE_PSM_RANK("SINGLE_PSM_PER_RANK","Single PSM per Rank"),
        ISOTOPE_OFFSET("ISOTOPE_OFFSET", "Isotope Offset"),
        BH_AJUSTED_PVALUE("BH_AJUSTED_PVALUE", "BH adjusted pValue (%)");

        public final String key;
        public final String name;

        PSMFilter(String key, String name) {
            this.key = key;
            this.name = name;
        }
    }

    public enum ValidationParameters {

        EXPECTED_FDR("expected_fdr", "FDR"),
        EXPECTED_FDR_PARAM("expected_fdr_parameter", "FDR Variable"),
        PROTEIN_EXPECTED_FDR("protein_expected_fdr", "Protein FDR");

        public final String key;
        public final String name;

        ValidationParameters(String key, String name) {
            this.key = key;
            this.name = name;
        }
    }

    private final DDataset m_dataset;
    private final String m_description;  //Not used on server side
    private final HashMap<String, String> m_argumentsMap;
    private final String m_scoringType;
    private final Integer[] m_resultSummaryId;
    private Map<Long,Long> m_rsmIdsPerRsIds = null;        
    private String m_version = null;
    
    public ValidationTask(AbstractJMSCallback callback, DDataset dataset, String description, HashMap<String, String> argumentsMap, Integer[] resultSummaryId, String scoringType) {
        super(callback, new TaskInfo("JMS Validation of Search Result " + dataset.getName(), true, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_HIGH));
        m_dataset = dataset;
        m_description = description;
        m_argumentsMap = argumentsMap;
        m_resultSummaryId = resultSummaryId;
        m_scoringType = scoringType;
    }
    
    public ValidationTask(AbstractJMSCallback callback, DDataset dataset, String description, HashMap<String, String> argumentsMap, Integer[] resultSummaryId, HashMap<Long,Long> rsmIdsPerRsIds, String scoringType) {
        super(callback, new TaskInfo("JMS Validation of Search Result " + dataset.getName(), true, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_HIGH));
        m_dataset = dataset;
        m_description = description;
        m_argumentsMap = argumentsMap;
        m_rsmIdsPerRsIds = rsmIdsPerRsIds;
        m_resultSummaryId = resultSummaryId;
        m_scoringType = scoringType;
    }
       
    @Override
    public void taskRun() throws JMSException {
            final JSONRPC2Request jsonRequest = new JSONRPC2Request(JMSConnectionManager.PROLINE_PROCESS_METHOD_NAME, Integer.valueOf(m_taskInfo.getId()));
            jsonRequest.setNamedParams(createParams());
           
            final TextMessage message = AccessJMSManagerThread.getAccessJMSManagerThread().getSession().createTextMessage(jsonRequest.toJSONString());

            /* ReplyTo = Temporary Destination Queue for Server -> Client response */
            message.setJMSReplyTo(m_replyQueue);
            message.setStringProperty(JMSConnectionManager.PROLINE_SERVICE_NAME_KEY, "proline/dps/msi/ValidateResultSet");
            if(m_version != null )
                message.setStringProperty(JMSConnectionManager.PROLINE_SERVICE_VERSION_KEY, m_version);
            addSourceToMessage(message);  
            addDescriptionToMessage(message);
        
            setTaskInfoRequest(message.getText());
            
            // Step 8. Send the Message
            m_producer.send(message);
            m_loggerProline.info("ValidationTask Message [{}] sent", message.getJMSMessageID());
            m_taskInfo.setJmsMessageID(message.getJMSMessageID());
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

	    final Object result = jsonResponse.getResult();
            if(m_version != null){
                
                if (result == null || ! Map.class.isInstance(result) ) {
                    m_loggerProline.debug("Invalid or no result");
                    throw new Exception("null or invalid result "+result);
                } else {
                    m_loggerProline.debug("Result :\n" + result);                    
                    Long rsmId = (Long) ((Map) result).get(m_dataset.getResultSetId().toString());
                    m_resultSummaryId[0] = rsmId.intValue();
                    ((Map<String,Long>) result).forEach( (String key, Long value) -> {
                        m_rsmIdsPerRsIds.put(Long.parseLong(key), value);
                    });                                                           
                }
            } else {
                if (result == null || ! Long.class.isInstance(result) ) {
                    m_loggerProline.debug("Invalid or no result");
                    throw new Exception("null or invalid result "+result);
                } else {
                    m_loggerProline.debug("Result :\n" + result);
                    m_resultSummaryId[0] = ((Long) result).intValue();
                }
            }
        }
        
        /*
         * TODO Use JSON-RPC Response
         */
        //traceJSONResponse(jsonString);
        m_currentState = JMSState.STATE_DONE;

    }
  
    
    private HashMap<String, Object> createParams() {

        HashMap<String, Object> params = new HashMap<>();
        params.put("project_id", m_dataset.getProject().getId());
        params.put("result_set_id", m_dataset.getResultSetId());
        params.put("description", m_description); //JPM.TODO : string is ""

        // PSM Pre-PSMFilter
        ArrayList psmFilters = new ArrayList();

        if (m_argumentsMap.containsKey("PSM_"+ PSMFilter.RANK.key)) {
            Map<String, Object> filterCfg = new HashMap<>();
            filterCfg.put("parameter", PSMFilter.RANK.key);
            filterCfg.put("threshold", Integer.valueOf(m_argumentsMap.get("PSM_"+ PSMFilter.RANK.key)));
            psmFilters.add(filterCfg);
        }
        if (m_argumentsMap.containsKey("PSM_"+ PSMFilter.SCORE.key)) {
            Map<String, Object> filterCfg = new HashMap<>();
            filterCfg.put("parameter", PSMFilter.SCORE.key);
            filterCfg.put("threshold", Float.valueOf(m_argumentsMap.get("PSM_"+ PSMFilter.SCORE.key)));
            psmFilters.add(filterCfg);
        }
        if (m_argumentsMap.containsKey("PSM_"+ PSMFilter.MASCOT_EVAL.key)) {
            HashMap filterCfg = new HashMap();
            filterCfg.put("parameter", PSMFilter.MASCOT_EVAL.key);
            filterCfg.put("threshold", Float.valueOf(m_argumentsMap.get("PSM_"+ PSMFilter.MASCOT_EVAL.key)));
            psmFilters.add(filterCfg);
        }
        if (m_argumentsMap.containsKey("PSM_"+ PSMFilter.MASCOT_ADJUSTED_EVALUE.key)) {
                HashMap filterCfg = new HashMap();
                filterCfg.put("parameter", PSMFilter.MASCOT_ADJUSTED_EVALUE.key);
                filterCfg.put("threshold", Float.valueOf(m_argumentsMap.get("PSM_"+ PSMFilter.MASCOT_ADJUSTED_EVALUE.key)));
                psmFilters.add(filterCfg);
        }
        if (m_argumentsMap.containsKey("PSM_"+ PSMFilter.PEP_LENGTH.key)) {
            HashMap filterCfg = new HashMap();
            filterCfg.put("parameter", PSMFilter.PEP_LENGTH.key);
            filterCfg.put("threshold", Integer.valueOf(m_argumentsMap.get("PSM_"+ PSMFilter.PEP_LENGTH.key)));
            psmFilters.add(filterCfg);
        }
        if (m_argumentsMap.containsKey("PSM_"+ PSMFilter.MASCOT_IT_SCORE.key)) {
            HashMap filterCfg = new HashMap();
            filterCfg.put("parameter", PSMFilter.MASCOT_IT_SCORE.key);
            filterCfg.put("threshold", Float.valueOf(m_argumentsMap.get("PSM_"+ PSMFilter.MASCOT_IT_SCORE.key)));
            psmFilters.add(filterCfg);
        }
        if (m_argumentsMap.containsKey("PSM_"+ PSMFilter.MASCOT_HT_SCORE.key)) {
            HashMap filterCfg = new HashMap();
            filterCfg.put("parameter", PSMFilter.MASCOT_HT_SCORE.key);
            filterCfg.put("threshold", Float.valueOf(m_argumentsMap.get("PSM_"+ PSMFilter.MASCOT_HT_SCORE.key)));
            psmFilters.add(filterCfg);
        }
        if (m_argumentsMap.containsKey("PSM_"+ PSMFilter.SINGLE_PSM_QUERY.key)) {
            HashMap filterCfg = new HashMap();
            filterCfg.put("parameter", PSMFilter.SINGLE_PSM_QUERY.key);
            filterCfg.put("threshold", 1);
            filterCfg.put("post_validation", Boolean.valueOf(m_argumentsMap.get("PSM_"+ PSMFilter.SINGLE_PSM_QUERY.key)));
            psmFilters.add(filterCfg);
        }
        if (m_argumentsMap.containsKey("PSM_"+ PSMFilter.SINGLE_PSM_RANK.key)) {
            HashMap filterCfg = new HashMap();
            filterCfg.put("parameter", PSMFilter.SINGLE_PSM_RANK.key);
            filterCfg.put("threshold", 1);
            filterCfg.put("post_validation", Boolean.valueOf(m_argumentsMap.get("PSM_"+ PSMFilter.SINGLE_PSM_RANK.key)));
            psmFilters.add(filterCfg);
        }
        if (m_argumentsMap.containsKey("PSM_"+ PSMFilter.ISOTOPE_OFFSET.key)) {
            HashMap filterCfg = new HashMap();
            filterCfg.put("parameter", PSMFilter.ISOTOPE_OFFSET.key);
            filterCfg.put("threshold", Integer.valueOf(m_argumentsMap.get("PSM_"+ PSMFilter.ISOTOPE_OFFSET.key)));
            psmFilters.add(filterCfg);
        }
        if (m_argumentsMap.containsKey("PSM_"+ PSMFilter.BH_AJUSTED_PVALUE.key)) {
            HashMap filterCfg = new HashMap();
            filterCfg.put("parameter", PSMFilter.BH_AJUSTED_PVALUE.key);
            filterCfg.put("threshold", Double.valueOf(m_argumentsMap.get("PSM_"+ PSMFilter.BH_AJUSTED_PVALUE.key))/100.0);
            psmFilters.add(filterCfg);
        }

        params.put("pep_match_filters", psmFilters);

        // PSM Validator
        if (m_argumentsMap.containsKey(ValidationParameters.EXPECTED_FDR.key)) {
            HashMap pepMatchValidator = new HashMap();
            pepMatchValidator.put("parameter", m_argumentsMap.get(ValidationParameters.EXPECTED_FDR_PARAM.key));
            pepMatchValidator.put("expected_fdr", m_argumentsMap.get(ValidationParameters.EXPECTED_FDR.key));
            params.put("pep_match_validator_config", pepMatchValidator);
        }

        if (m_argumentsMap.containsKey("td_analyzer")) {
            HashMap tdAnalyzerConfig = new HashMap();
            tdAnalyzerConfig.put("method_name", m_argumentsMap.get("td_analyzer"));
            
            if (m_argumentsMap.containsKey("db_ratio")) {
                HashMap tdAnalyzerParams = new HashMap();
                tdAnalyzerParams.put("ratio", m_argumentsMap.get("db_ratio"));            
                tdAnalyzerConfig.put("params", tdAnalyzerParams);
            }
            params.put("td_analyzer_config", tdAnalyzerConfig);
            
        }

        // Peptide Filters
        ArrayList peptideFilters = new ArrayList();
        if (m_argumentsMap.containsKey("PEPTIDE_"+ PSMFilter.BH_AJUSTED_PVALUE.key)) {
            HashMap filterCfg = new HashMap();
            filterCfg.put("parameter", PSMFilter.BH_AJUSTED_PVALUE.key);
            filterCfg.put("threshold", Double.valueOf(m_argumentsMap.get("PEPTIDE_"+ PSMFilter.BH_AJUSTED_PVALUE.key))/100.0);
            peptideFilters.add(filterCfg);
        }
        params.put("peptide_filters", peptideFilters);
        
        
        params.put("pep_set_score_type", m_scoringType);

        // Protein Pre-PSMFilter
        ArrayList proteinFilters = new ArrayList();

        for (FilterProteinSetsTask.Filter filter: FilterProteinSetsTask.Filter.values()) {
            String filterKeyOfInMap = "PROT_"+filter.key;
            if (m_argumentsMap.containsKey(filterKeyOfInMap)) {
                HashMap filterCfg = new HashMap();
                filterCfg.put("parameter", filter.key);
                if (filter == FilterProteinSetsTask.Filter.SCORE) {
                    filterCfg.put("threshold", Double.valueOf(m_argumentsMap.get(filterKeyOfInMap)));
                } else if (filter == FilterProteinSetsTask.Filter.BH_ADJUSTED_PVALUE) {
                    filterCfg.put("threshold", Double.valueOf(m_argumentsMap.get(filterKeyOfInMap))/100.0);                  
                } else {
                    filterCfg.put("threshold", Integer.valueOf(m_argumentsMap.get(filterKeyOfInMap)));
                }
                proteinFilters.add(filterCfg);
            }
        }

        params.put("prot_set_filters", proteinFilters);


        // protein parameters
        if (m_argumentsMap.containsKey(ValidationParameters.PROTEIN_EXPECTED_FDR.key)) {
            HashMap protSetValidator = new HashMap();
            protSetValidator.put("parameter", "SCORE");
            protSetValidator.put("expected_fdr", m_argumentsMap.get(ValidationParameters.PROTEIN_EXPECTED_FDR.key));
            protSetValidator.put("validation_method", "PROTEIN_SET_RULES");
            params.put("prot_set_validator_config", protSetValidator);
        }

        if(m_argumentsMap.containsKey("propagate_prot_set_filters") ){            
            params.put("propagate_prot_set_filters", Boolean.parseBoolean(m_argumentsMap.get("propagate_prot_set_filters")));
            m_version = "2.0";
        }
        
        if(m_argumentsMap.containsKey("propagate_pep_match_filters")) {
            params.put("propagate_pep_match_filters",  Boolean.parseBoolean(m_argumentsMap.get("propagate_pep_match_filters")));
            m_version = "2.0";
        }
        
        return params;
    }


    
}
