package fr.proline.studio.dpm.task;

import com.google.api.client.http.HttpResponse;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.rpc2.JsonRpcRequest;
import com.google.api.client.util.ArrayMap;
import fr.proline.core.orm.uds.Dataset;
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

    private Dataset dataset = null;
    String description;
    HashMap<String, String> argumentsMap;
    Integer[] resultSummaryId = null;
    
    public ValidationTask(AbstractServiceCallback callback, Dataset dataset, String description, HashMap<String, String> argumentsMap, Integer[] resultSummaryId) {
        super(callback, false /*asynchronous*/, new TaskInfo("Validation", "Validation of "+dataset.getName()+" Result Set", TASK_LIST_INFO));
        this.dataset = dataset;
        this.description = description;
        this.argumentsMap = argumentsMap;
        this.resultSummaryId = resultSummaryId;
    }
    
    @Override
    public boolean askService() {
        try {
            // create the request
            JsonRpcRequest request = new JsonRpcRequest();
            
            request.setId(id);
            request.setMethod("run_job");
            
            
            Map<String, Object> params = new HashMap<>();
	    params.put("project_id", dataset.getProject().getId());  
            params.put("result_set_id", dataset.getResultSetId() );
            params.put("description", description ); //JPM.TODO : string is ""
             
            // Peptide Pre-Filters
            ArrayList pepFilters = new ArrayList();
            
            if (argumentsMap.containsKey("RANK")) {
                HashMap filterCfg = new HashMap();
                filterCfg.put("parameter", "RANK");
                filterCfg.put("threshold", Integer.valueOf(argumentsMap.get("RANK")) );
                pepFilters.add(filterCfg);
            }
            if (argumentsMap.containsKey("SCORE")) {
                HashMap filterCfg = new HashMap();
                filterCfg.put("parameter", "SCORE");
                filterCfg.put("threshold", Float.valueOf(argumentsMap.get("SCORE")));
                pepFilters.add(filterCfg);
            }
            if (argumentsMap.containsKey("MASCOT_EVALUE")) {
                HashMap filterCfg = new HashMap();
                filterCfg.put("parameter", "MASCOT_EVALUE");
                filterCfg.put("threshold", Float.valueOf(argumentsMap.get("MASCOT_EVALUE")));
                pepFilters.add(filterCfg);
            }
            if (argumentsMap.containsKey("PEP_SEQ_LENGTH")) {
                HashMap filterCfg = new HashMap();
                filterCfg.put("parameter", "PEP_SEQ_LENGTH");
                filterCfg.put("threshold", Integer.valueOf(argumentsMap.get("PEP_SEQ_LENGTH")));
                pepFilters.add(filterCfg);
            }
            if (argumentsMap.containsKey("SCORE_IT_P-VALUE")) {
                HashMap filterCfg = new HashMap();
                filterCfg.put("parameter", "SCORE_IT_P-VALUE");
                filterCfg.put("threshold", Float.valueOf(argumentsMap.get("SCORE_IT_P-VALUE")));
                pepFilters.add(filterCfg);
            }
            if (argumentsMap.containsKey("SCORE_HT_P-VALUE")) {
                HashMap filterCfg = new HashMap();
                filterCfg.put("parameter", "SCORE_HT_P-VALUE");
                filterCfg.put("threshold", Float.valueOf(argumentsMap.get("SCORE_HT_P-VALUE")));
                pepFilters.add(filterCfg);
            }
            params.put("pep_match_filters",pepFilters);
            
            // Peptide Validator
            if (argumentsMap.containsKey("expected_fdr")) {
                HashMap pepMatchValidator = new HashMap();
                pepMatchValidator.put("parameter", argumentsMap.get("expected_fdr_parameter"));
                pepMatchValidator.put("expected_fdr", argumentsMap.get("expected_fdr"));
                params.put("pep_match_validator_config", pepMatchValidator);
            }

            
            
            if (argumentsMap.containsKey("use_td_competition")) {
                params.put("use_td_competition", Boolean.parseBoolean(argumentsMap.get("use_td_competition")) );
            }
                
                
            // Protein Pre-Filters
            ArrayList proteinFilters = new ArrayList();
            
            if (argumentsMap.containsKey("PROTEOTYPIQUE_PEP")) {
                HashMap filterCfg = new HashMap();
                filterCfg.put("parameter", "PROTEOTYPIQUE_PEP");
                filterCfg.put("threshold", Integer.valueOf(argumentsMap.get("PROTEOTYPIQUE_PEP")) );
                proteinFilters.add(filterCfg);
            }
            
            params.put("prot_set_filters", proteinFilters);
            
            
            // protein parameters
            if (argumentsMap.containsKey("protein_expected_fdr")) {
                HashMap protSetValidator = new HashMap();
                protSetValidator.put("parameter", "SCORE");
                protSetValidator.put("expected_fdr", argumentsMap.get("protein_expected_fdr"));
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
                    errorMessage = message;
                }
                
                String data = (String) errorMap.get("data");
                if (data != null) {
                    if (errorMessage == null) {
                        errorMessage = data;
                    } else {
                        errorMessage = errorMessage+"\n"+data;
                    }
                }
                
                if (errorMessage != null) {
                    logger.error(getClass().getSimpleName() + " failed : "+errorMessage);
                }
                
                return false;
            }
            
            
            BigDecimal jobId = (BigDecimal) jsonResult.get("result");
            if (jobId != null) {
                id = jobId.intValue();
            } else {
                logger.error(getClass().getSimpleName() + " failed : id not defined");
            }



        } catch (Exception e) {
            errorMessage = e.getMessage();
            logger.error(getClass().getSimpleName() + " failed", e);
            return false;
        }

        return true;
    }


    @Override
    public ServiceState getServiceState() {
        
        try {
                
            // create the request
            JsonRpcRequest request = new JsonRpcRequest();
            
            request.setId(idIncrement++);
            request.setMethod("get_job_status");
            
            Map<String, Object> params = new HashMap<String, Object>();
	    params.put("job_id", id);

            request.setParameters(params);

            HttpResponse response = postRequest("dps.msi/validate_result_set/"+request.getMethod()+getIdString(), request); //JPM.TODO

            GenericJson jsonResult = response.parseAs(GenericJson.class);

            ArrayMap errorMap = (ArrayMap) jsonResult.get("error");

            if (errorMap != null) {

                errorMessage = (String) errorMap.get("message");
                
                String data = (String) errorMap.get("data");
                if (data != null) {
                    if (errorMessage == null) {
                        errorMessage = data;
                    } else {
                        errorMessage = errorMessage+"\n"+data;
                    }
                }
                
                if (errorMessage != null) {
                    logger.error(getClass().getSimpleName() + " failed : " + errorMessage);
                }
                
                return ServiceState.STATE_FAILED; // should not happen !
            }
            
            ArrayMap resultMap = (ArrayMap) jsonResult.get("result");
        
            if (resultMap != null) {
                Boolean success = (Boolean) resultMap.get("success");  //JPM.TODO : get ResultSummary created
                
                if (success == null) {
                    return ServiceState.STATE_WAITING;
                }
                
                if (success) {

                    // retrieve resultSummary id
                    BigDecimal resultSummaryIdBD = (BigDecimal) resultMap.get("result");
                    if (resultSummaryIdBD == null) {
                        logger.error(getClass().getSimpleName() + " failed : No returned ResultSummary Id");
                        return ServiceState.STATE_FAILED;
                    }

                    resultSummaryId[0] = new Integer(resultSummaryIdBD.intValue());

                    return ServiceState.STATE_DONE;
                } else {
                    errorMessage = (String) resultMap.get("message");
                    
                    
                    if (errorMessage != null) {
                        logger.error(getClass().getSimpleName() + " failed : " + errorMessage);
                    }
                
                    
                    return ServiceState.STATE_FAILED;
                }
                
            }

            

        } catch (Exception e) {
            errorMessage = e.getMessage();
            logger.error(getClass().getSimpleName() + " failed", e);
            return ServiceState.STATE_FAILED; // should not happen !
        }
               
        return ServiceState.STATE_WAITING;
        
    }
    
}
