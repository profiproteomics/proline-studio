package fr.proline.studio.dpm.task;

import com.google.api.client.http.HttpResponse;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.rpc2.JsonRpcRequest;
import com.google.api.client.util.ArrayMap;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
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
    String m_description;
    HashMap<String, String> m_argumentsMap;
    Integer[] m_resultSummaryId = null;
    
    public ValidationTask(AbstractServiceCallback callback, DDataset dataset, String description, HashMap<String, String> argumentsMap, Integer[] resultSummaryId) {
        super(callback, false /*asynchronous*/, new TaskInfo("Validation of Search Result "+dataset.getName(), true, TASK_LIST_INFO));
        m_dataset = dataset;
        m_description = description;
        m_argumentsMap = argumentsMap;
        m_resultSummaryId = resultSummaryId;
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
            
            if (m_argumentsMap.containsKey("RANK")) {
                HashMap filterCfg = new HashMap();
                filterCfg.put("parameter", "RANK");
                filterCfg.put("threshold", Integer.valueOf(m_argumentsMap.get("RANK")) );
                pepFilters.add(filterCfg);
            }
            if (m_argumentsMap.containsKey("SCORE")) {
                HashMap filterCfg = new HashMap();
                filterCfg.put("parameter", "SCORE");
                filterCfg.put("threshold", Float.valueOf(m_argumentsMap.get("SCORE")));
                pepFilters.add(filterCfg);
            }
            if (m_argumentsMap.containsKey("MASCOT_EVALUE")) {
                HashMap filterCfg = new HashMap();
                filterCfg.put("parameter", "MASCOT_EVALUE");
                filterCfg.put("threshold", Float.valueOf(m_argumentsMap.get("MASCOT_EVALUE")));
                pepFilters.add(filterCfg);
            }
            if (m_argumentsMap.containsKey("PEP_SEQ_LENGTH")) {
                HashMap filterCfg = new HashMap();
                filterCfg.put("parameter", "PEP_SEQ_LENGTH");
                filterCfg.put("threshold", Integer.valueOf(m_argumentsMap.get("PEP_SEQ_LENGTH")));
                pepFilters.add(filterCfg);
            }
            if (m_argumentsMap.containsKey("SCORE_IT_P-VALUE")) {
                HashMap filterCfg = new HashMap();
                filterCfg.put("parameter", "SCORE_IT_P-VALUE");
                filterCfg.put("threshold", Float.valueOf(m_argumentsMap.get("SCORE_IT_P-VALUE")));
                pepFilters.add(filterCfg);
            }
            if (m_argumentsMap.containsKey("SCORE_HT_P-VALUE")) {
                HashMap filterCfg = new HashMap();
                filterCfg.put("parameter", "SCORE_HT_P-VALUE");
                filterCfg.put("threshold", Float.valueOf(m_argumentsMap.get("SCORE_HT_P-VALUE")));
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
                
                
            // Protein Pre-Filters
            ArrayList proteinFilters = new ArrayList();
            
            if (m_argumentsMap.containsKey("SPECIFIC_PEP")) {
                HashMap filterCfg = new HashMap();
                filterCfg.put("parameter", "SPECIFIC_PEP");
                filterCfg.put("threshold", Integer.valueOf(m_argumentsMap.get("SPECIFIC_PEP")) );
                proteinFilters.add(filterCfg);
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
