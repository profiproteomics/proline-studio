package fr.proline.studio.dpm.task.jms;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Message;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Notification;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dpm.AccessJMSManagerThread;
import static fr.proline.studio.dpm.task.jms.AbstractJMSTask.m_loggerProline;
import static fr.proline.studio.dpm.task.jms.FilterRSMProtSetsTask.FILTER_KEYS;
import fr.proline.studio.dpm.task.util.JMSConnectionManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;


/**
 *
 * @author JM235353
 */
public class ValidationTask extends AbstractJMSTask  {

    //PSM PreFilter
    public static String RANK_FILTER_KEY = "PRETTY_RANK";
    public static String RANK_FILTER_NAME = "Pretty Rank";
    public static String SCORE_FILTER_KEY = "SCORE";
    public static String SCORE_FILTER_NAME = "Score";
    public static String PEP_LENGTH_FILTER_KEY = "PEP_SEQ_LENGTH";
    public static String PEP_LENGTH_FILTER_NAME = "Length";
    public static String MASCOT_EVAL_FILTER_KEY = "MASCOT_EVALUE";
    public static String MASCOT_EVAL_FILTER_NAME = "e-Value";
    public static String MASCOT_ADJUSTED_EVAL_FILTER_KEY = "MASCOT_ADJUSTED_EVALUE";
    public static String MASCOT_ADJUSTED_EVAL_FILTER_NAME = "Adjusted e-Value";
    public static String MASCOT_IT_SCORE_FILTER_KEY = "SCORE_IT_P-VALUE";
    public static String MASCOT_IT_SCORE_FILTER_NAME = "Identity p-Value";
    public static String MASCOT_HT_SCORE_FILTER_KEY = "SCORE_HT_P-VALUE";
    public static String MASCOT_HT_SCORE_FILTER_NAME = "Homology p-Value";
    public static String SINGLE_PSM_QUERY_FILTER_KEY = "SINGLE_PSM_PER_QUERY";
    public static String SINGLE_PSM_QUERY_FILTER_NAME = "Single PSM per MS Query";               
    public static String SINGLE_PSM_RANK_FILTER_KEY = "SINGLE_PSM_PER_RANK";
    public static String SINGLE_PSM_RANK_FILTER_NAME = "Single PSM per Rank";                                 
    public static String ISOTOPE_OFFSET_FILTER_KEY = "ISOTOPE_OFFSET";
    public static String ISOTOPE_OFFSET_FILTER_NAME = "Isotope Offset";               

    private DDataset m_dataset = null;
    private String m_description;  //Not used on server side
    private HashMap<String, String> m_argumentsMap;
    private String m_scoringType = null;
    private Integer[] m_resultSummaryId = null;    
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

        // Peptide Pre-Filters
        ArrayList pepFilters = new ArrayList();

        if (m_argumentsMap.containsKey("PSM_"+RANK_FILTER_KEY)) {
            HashMap filterCfg = new HashMap();
            filterCfg.put("parameter", RANK_FILTER_KEY);
            filterCfg.put("threshold", Integer.valueOf(m_argumentsMap.get("PSM_"+RANK_FILTER_KEY)));
            pepFilters.add(filterCfg);
        }
        if (m_argumentsMap.containsKey("PSM_"+SCORE_FILTER_KEY)) {
            HashMap filterCfg = new HashMap();
            filterCfg.put("parameter", SCORE_FILTER_KEY);
            filterCfg.put("threshold", Float.valueOf(m_argumentsMap.get("PSM_"+SCORE_FILTER_KEY)));
            pepFilters.add(filterCfg);
        }
        if (m_argumentsMap.containsKey("PSM_"+MASCOT_EVAL_FILTER_KEY)) {
            HashMap filterCfg = new HashMap();
            filterCfg.put("parameter", MASCOT_EVAL_FILTER_KEY);
            filterCfg.put("threshold", Float.valueOf(m_argumentsMap.get("PSM_"+MASCOT_EVAL_FILTER_KEY)));
            pepFilters.add(filterCfg);
        }
        if (m_argumentsMap.containsKey("PSM_"+MASCOT_ADJUSTED_EVAL_FILTER_KEY)) {
                HashMap filterCfg = new HashMap();
                filterCfg.put("parameter", MASCOT_ADJUSTED_EVAL_FILTER_KEY);
                filterCfg.put("threshold", Float.valueOf(m_argumentsMap.get("PSM_"+MASCOT_ADJUSTED_EVAL_FILTER_KEY)));
                pepFilters.add(filterCfg);
        }
        if (m_argumentsMap.containsKey("PSM_"+PEP_LENGTH_FILTER_KEY)) {
            HashMap filterCfg = new HashMap();
            filterCfg.put("parameter", PEP_LENGTH_FILTER_KEY);
            filterCfg.put("threshold", Integer.valueOf(m_argumentsMap.get("PSM_"+PEP_LENGTH_FILTER_KEY)));
            pepFilters.add(filterCfg);
        }
        if (m_argumentsMap.containsKey("PSM_"+MASCOT_IT_SCORE_FILTER_KEY)) {
            HashMap filterCfg = new HashMap();
            filterCfg.put("parameter", MASCOT_IT_SCORE_FILTER_KEY);
            filterCfg.put("threshold", Float.valueOf(m_argumentsMap.get("PSM_"+MASCOT_IT_SCORE_FILTER_KEY)));
            pepFilters.add(filterCfg);
        }
        if (m_argumentsMap.containsKey("PSM_"+MASCOT_HT_SCORE_FILTER_KEY)) {
            HashMap filterCfg = new HashMap();
            filterCfg.put("parameter", MASCOT_HT_SCORE_FILTER_KEY);
            filterCfg.put("threshold", Float.valueOf(m_argumentsMap.get("PSM_"+MASCOT_HT_SCORE_FILTER_KEY)));
            pepFilters.add(filterCfg);
        }
        if (m_argumentsMap.containsKey("PSM_"+SINGLE_PSM_QUERY_FILTER_KEY)) {
            HashMap filterCfg = new HashMap();
            filterCfg.put("parameter", SINGLE_PSM_QUERY_FILTER_KEY);
            filterCfg.put("threshold", 1);
            filterCfg.put("post_validation", Boolean.valueOf(m_argumentsMap.get("PSM_"+SINGLE_PSM_QUERY_FILTER_KEY)));
            pepFilters.add(filterCfg);
        }
        if (m_argumentsMap.containsKey("PSM_"+SINGLE_PSM_RANK_FILTER_KEY)) {
            HashMap filterCfg = new HashMap();
            filterCfg.put("parameter", SINGLE_PSM_RANK_FILTER_KEY);
            filterCfg.put("threshold", 1);
            filterCfg.put("post_validation", Boolean.valueOf(m_argumentsMap.get("PSM_"+SINGLE_PSM_RANK_FILTER_KEY)));
            pepFilters.add(filterCfg);
        }
        if (m_argumentsMap.containsKey("PSM_"+ISOTOPE_OFFSET_FILTER_KEY)) {
            HashMap filterCfg = new HashMap();
            filterCfg.put("parameter", ISOTOPE_OFFSET_FILTER_KEY);
            filterCfg.put("threshold", Integer.valueOf(m_argumentsMap.get("PSM_"+ISOTOPE_OFFSET_FILTER_KEY)));
            pepFilters.add(filterCfg);
        }
        params.put("pep_match_filters", pepFilters);

        // Peptide Validator
        if (m_argumentsMap.containsKey("expected_fdr")) {
            HashMap pepMatchValidator = new HashMap();
            pepMatchValidator.put("parameter", m_argumentsMap.get("expected_fdr_parameter"));
            pepMatchValidator.put("expected_fdr", m_argumentsMap.get("expected_fdr"));
            params.put("pep_match_validator_config", pepMatchValidator);
        }


            //DEPRECATED On server Side
        if (m_argumentsMap.containsKey("use_td_competition")) {
            params.put("use_td_competition", Boolean.parseBoolean(m_argumentsMap.get("use_td_competition")));
        }

        params.put("pep_set_score_type", m_scoringType);

        // Protein Pre-Filters
        ArrayList proteinFilters = new ArrayList();

        for (String filterKey : FILTER_KEYS) {   
            String filterKeyOfInMap = "PROT_"+filterKey;
            if (m_argumentsMap.containsKey(filterKeyOfInMap)) {
                HashMap filterCfg = new HashMap();
                filterCfg.put("parameter", filterKey);
                if (filterKey.equals("SCORE")) //TODO USE ENUM
                    filterCfg.put("threshold", Double.valueOf(m_argumentsMap.get(filterKeyOfInMap)));
                else
                    filterCfg.put("threshold", Integer.valueOf(m_argumentsMap.get(filterKeyOfInMap)));                
                proteinFilters.add(filterCfg);
            }
        }

        params.put("prot_set_filters", proteinFilters);


        // protein parameters
        if (m_argumentsMap.containsKey("protein_expected_fdr")) {
            HashMap protSetValidator = new HashMap();
            protSetValidator.put("parameter", "SCORE");
            protSetValidator.put("expected_fdr", m_argumentsMap.get("protein_expected_fdr"));
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
