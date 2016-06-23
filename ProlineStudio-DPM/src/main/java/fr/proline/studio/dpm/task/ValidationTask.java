package fr.proline.studio.dpm.task;

import com.google.api.client.http.HttpResponse;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.rpc2.JsonRpcRequest;
import com.google.api.client.util.ArrayMap;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import static fr.proline.studio.dpm.task.FilterRSMProtSetsTask.FILTER_KEYS;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Task to start the validation service
 * @author jm235353
 */
public class ValidationTask extends AbstractServiceTask {

    private DDataset m_dataset = null;
    private String m_description;
    private HashMap<String, String> m_argumentsMap;
    private Integer[] m_resultSummaryId = null;
    private String m_scoringType = null;
    
        //PSM PreFilter
    public static String RANK_FILTER_KEY = "RANK";
    public static String RANK_FILTER_NAME = "Rank"; // It is a pretty rank in fact
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
        
    public ValidationTask(AbstractServiceCallback callback, DDataset dataset, String description, HashMap<String, String> argumentsMap, Integer[] resultSummaryId, String scoringType) {
        super(callback, false /*asynchronous*/, new TaskInfo("Validation of Search Result "+dataset.getName(), true, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_HIGH));
        m_dataset = dataset;
        m_description = description;
        m_argumentsMap = argumentsMap;
        m_resultSummaryId = resultSummaryId;
        m_scoringType = scoringType;
    }
    
    @Override
    public boolean askService() {
        try {
            // create the request
            JsonRpcRequest request = new JsonRpcRequest();
            
            request.setId(m_id);
            request.setMethod("run_job");
            
            
            Map<String, Object> params = new HashMap<>();
	    params.put("project_id", m_dataset.getProject().getId());  
            params.put("result_set_id", m_dataset.getResultSetId() );
            params.put("description", m_description ); //JPM.TODO : string is ""
             
            // Peptide Pre-Filters
            ArrayList pepFilters = new ArrayList();
            
            if (m_argumentsMap.containsKey(RANK_FILTER_KEY)) {
                HashMap filterCfg = new HashMap();
                filterCfg.put("parameter", RANK_FILTER_KEY);
                filterCfg.put("threshold", Integer.valueOf(m_argumentsMap.get(RANK_FILTER_KEY)) );
                pepFilters.add(filterCfg);
            }
            if (m_argumentsMap.containsKey(SCORE_FILTER_KEY)) {
                HashMap filterCfg = new HashMap();
                filterCfg.put("parameter", SCORE_FILTER_KEY);
                filterCfg.put("threshold", Float.valueOf(m_argumentsMap.get(SCORE_FILTER_KEY)));
                pepFilters.add(filterCfg);
            }
            if (m_argumentsMap.containsKey(MASCOT_EVAL_FILTER_KEY)) {
                HashMap filterCfg = new HashMap();
                filterCfg.put("parameter", MASCOT_EVAL_FILTER_KEY);
                filterCfg.put("threshold", Float.valueOf(m_argumentsMap.get(MASCOT_EVAL_FILTER_KEY)));
                pepFilters.add(filterCfg);
            }
            if (m_argumentsMap.containsKey(MASCOT_ADJUSTED_EVAL_FILTER_KEY)) {
                HashMap filterCfg = new HashMap();
                filterCfg.put("parameter", MASCOT_ADJUSTED_EVAL_FILTER_KEY);
                filterCfg.put("threshold", Float.valueOf(m_argumentsMap.get(MASCOT_ADJUSTED_EVAL_FILTER_KEY)));
                pepFilters.add(filterCfg);
            }
            if (m_argumentsMap.containsKey(PEP_LENGTH_FILTER_KEY)) {
                HashMap filterCfg = new HashMap();
                filterCfg.put("parameter", PEP_LENGTH_FILTER_KEY);
                filterCfg.put("threshold", Integer.valueOf(m_argumentsMap.get(PEP_LENGTH_FILTER_KEY)));
                pepFilters.add(filterCfg);
            }
            if (m_argumentsMap.containsKey(MASCOT_IT_SCORE_FILTER_KEY)) {
                HashMap filterCfg = new HashMap();
                filterCfg.put("parameter", MASCOT_IT_SCORE_FILTER_KEY);
                filterCfg.put("threshold", Float.valueOf(m_argumentsMap.get(MASCOT_IT_SCORE_FILTER_KEY)));
                pepFilters.add(filterCfg);
            }
            if (m_argumentsMap.containsKey(MASCOT_HT_SCORE_FILTER_KEY)) {
                HashMap filterCfg = new HashMap();
                filterCfg.put("parameter", MASCOT_HT_SCORE_FILTER_KEY);
                filterCfg.put("threshold", Float.valueOf(m_argumentsMap.get(MASCOT_HT_SCORE_FILTER_KEY)));
                pepFilters.add(filterCfg);
            }
            if (m_argumentsMap.containsKey(SINGLE_PSM_QUERY_FILTER_KEY)) {
                HashMap filterCfg = new HashMap();
                filterCfg.put("parameter", SINGLE_PSM_QUERY_FILTER_KEY);
                filterCfg.put("threshold", 1);
                filterCfg.put("post_validation", Boolean.valueOf(m_argumentsMap.get(SINGLE_PSM_QUERY_FILTER_KEY)));
                pepFilters.add(filterCfg);
            }
            if (m_argumentsMap.containsKey(SINGLE_PSM_RANK_FILTER_KEY)) {
                HashMap filterCfg = new HashMap();
                filterCfg.put("parameter", SINGLE_PSM_RANK_FILTER_KEY);
                filterCfg.put("threshold", 1);
//                filterCfg.put("post_validation", Boolean.valueOf(m_argumentsMap.get(SINGLE_PSM_RANK_FILTER_KEY)));
                pepFilters.add(filterCfg);
            }
            params.put("pep_match_filters",pepFilters);
            
            // Peptide Validator
            if (m_argumentsMap.containsKey("expected_fdr")) {
                HashMap pepMatchValidator = new HashMap();
                pepMatchValidator.put("parameter", m_argumentsMap.get("expected_fdr_parameter"));
                pepMatchValidator.put("expected_fdr", m_argumentsMap.get("expected_fdr"));
                params.put("pep_match_validator_config", pepMatchValidator);
            }

            if (m_argumentsMap.containsKey("use_td_competition")) {
                params.put("use_td_competition", Boolean.parseBoolean(m_argumentsMap.get("use_td_competition")) );
            }
                
            params.put("pep_set_score_type", m_scoringType);
                
            // Protein Pre-Filters
            ArrayList proteinFilters = new ArrayList();
            for (String filterKey : FILTER_KEYS) {
                if (m_argumentsMap.containsKey(filterKey)) {
                    HashMap filterCfg = new HashMap();
                    filterCfg.put("parameter", filterKey);
                    filterCfg.put("threshold", Integer.valueOf(m_argumentsMap.get(filterKey)));
                    proteinFilters.add(filterCfg);
                }
            }
            
            params.put("prot_set_filters", proteinFilters);
            
            
            // protein parameters
            if (m_argumentsMap.containsKey("protein_expected_fdr")) {
                HashMap protSetValidator = new HashMap();
                protSetValidator.put("parameter", "SCORE");
                protSetValidator.put("expected_fdr", m_argumentsMap.get("protein_expected_fdr"));
                protSetValidator.put("validation_method","PROTEIN_SET_RULES");
                params.put("prot_set_validator_config", protSetValidator);
            }
            

            request.setParameters(params);

            HttpResponse response = postRequest("dps.msi/validate_result_set/"+request.getMethod()+getIdString(), request);

            GenericJson jsonResult = response.parseAs(GenericJson.class);

            ArrayMap errorMap = (ArrayMap) jsonResult.get("error");

            if (errorMap != null) {
                String message = (String) errorMap.get("message");

                if (message != null) {
                    m_taskError = new TaskError(message);
                }
                
                String data = (String) errorMap.get("data");
                if (data != null) {
                    if (m_taskError == null) {
                        m_taskError = new TaskError(data);
                    } else {
                        m_taskError.setErrorText(data);
                    }
                }
                
                if (m_taskError != null) {
                    m_loggerWebcore.error(getClass().getSimpleName() + " failed : "+m_taskError.toString());
                }
                
                return false;
            }
            
            
            BigDecimal jobId = (BigDecimal) jsonResult.get("result");
            if (jobId != null) {
                m_id = jobId.intValue();
            } else {
                m_loggerProline.error(getClass().getSimpleName() + " failed : id not defined");
            }



        } catch (Exception e) {
            m_taskError = new TaskError(e);
            m_loggerProline.error(getClass().getSimpleName() + " failed", e);
            return false;
        }

        return true;
    }


    @Override
    public ServiceState getServiceState() {
        
        try {
                
            // create the request
            JsonRpcRequest request = new JsonRpcRequest();
            
            request.setId(m_idIncrement++);
            request.setMethod("get_job_status");
            
            Map<String, Object> params = new HashMap<>();
	    params.put("job_id", m_id);

            request.setParameters(params);

            HttpResponse response = postRequest("dps.msi/validate_result_set/"+request.getMethod()+getIdString(), request);

            GenericJson jsonResult = response.parseAs(GenericJson.class);

            ArrayMap errorMap = (ArrayMap) jsonResult.get("error");

            if (errorMap != null) {

                String message = (String) errorMap.get("message");

                if (message != null) {
                    m_taskError = new TaskError(message);
                }
                
                String data = (String) errorMap.get("data");
                if (data != null) {
                    if (m_taskError == null) {
                        m_taskError = new TaskError(data);
                    } else {
                        m_taskError.setErrorText(data);
                    }
                }
                
                if (m_taskError != null) {
                    m_loggerWebcore.error(getClass().getSimpleName() + " failed : "+m_taskError.toString());
                }
                
                return ServiceState.STATE_FAILED; // should not happen !
            }
            
            ArrayMap resultMap = (ArrayMap) jsonResult.get("result");
        
            if (resultMap != null) {
                Boolean success = (Boolean) resultMap.get("success");  //JPM.TODO : get ResultSummary created
                
                if (success == null) {
                    
                    String message = (String) resultMap.get("message");
                    if ((message!=null) && message.startsWith("Running")) {
                        getTaskInfo().setRunning(false);
                    }
                    
                    return ServiceState.STATE_WAITING;
                }
                
                if (success) {

                    BigDecimal duration = (BigDecimal) resultMap.get("duration");
                    if (duration != null) {
                        getTaskInfo().setDuration(duration.longValue());
                    }
                    
                    // retrieve resultSummary id
                    BigDecimal resultSummaryIdBD = (BigDecimal) resultMap.get("result");
                    if (resultSummaryIdBD == null) {
                        m_loggerProline.error(getClass().getSimpleName() + " failed : No returned ResultSummary Id");
                        return ServiceState.STATE_FAILED;
                    }

                    m_resultSummaryId[0] = new Integer(resultSummaryIdBD.intValue());

                    return ServiceState.STATE_DONE;
                } else {
                    String errorMessage = (String) resultMap.get("message");
                    
                    
                    if (errorMessage != null) {
                        m_taskError = new TaskError(errorMessage);
                        m_loggerWebcore.error(getClass().getSimpleName() + " failed : " + errorMessage);
                    }
                
                    
                    return ServiceState.STATE_FAILED;
                }
                
            }

            

        } catch (Exception e) {
            m_taskError = new TaskError(e);
            m_loggerProline.error(getClass().getSimpleName() + " failed", e);
            return ServiceState.STATE_FAILED; // should not happen !
        }
               
        return ServiceState.STATE_WAITING;
        
    }
    
}
