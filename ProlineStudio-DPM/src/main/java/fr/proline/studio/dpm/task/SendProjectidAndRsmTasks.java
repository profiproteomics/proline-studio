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
import java.util.List;
import java.util.Map;

public class SendProjectidAndRsmTasks extends AbstractServiceTask {

    private DDataset m_dataset;
    private HashMap<String,String> m_argumentsMap;
    private Long m_resultSummaryId = null;
    
    // Filter
    
    public static String RANK_FILTER_KEY = "RANK";
    public static String RANK_FILTER_NAME = "Rank";
    public static String SCORE_FILTER_KEY = "SCORE";
    public static String SCORE_FILTER_NAME = "Score";
    public static String PEP_LENGTH_FILTER_KEY = "PEP_SEQ_LENGTH";
    public static String PEP_LENGTH_FILTER_NAME = "Length";
       
    public SendProjectidAndRsmTasks(AbstractServiceCallback callback, DDataset dataset,HashMap<String, String> argumentsMap,Long rsm) {
        super(callback, false /** asynchronous */, new TaskInfo("Compute and get Protein Sequence for " + dataset.getName(), true, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_HIGH));
        m_dataset = dataset;
        m_argumentsMap = argumentsMap;
        m_resultSummaryId=rsm;
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
            params.put("result_summary_id",m_resultSummaryId);
            
            if (m_argumentsMap.containsKey(RANK_FILTER_KEY)) {
            	
                params.put("rank_filter_key", Integer.valueOf(m_argumentsMap.get(RANK_FILTER_KEY)) );
            }
            if (m_argumentsMap.containsKey(SCORE_FILTER_KEY)) {
            	
            	params.put("score_fliter_key", Float.valueOf(m_argumentsMap.get(SCORE_FILTER_KEY))); 
            }
            if (m_argumentsMap.containsKey(PEP_LENGTH_FILTER_KEY)) { 
            	
            	params.put("pep_length_filter_key", Integer.valueOf(m_argumentsMap.get(PEP_LENGTH_FILTER_KEY)));  
            }
            request.setParameters(params);
            
            HttpResponse response = postRequest("dps.msi/get_protein_sequence/"+request.getMethod()+getIdString(), request);

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

            HttpResponse response = postRequest("dps.msi/get_protein_sequence/"+request.getMethod()+getIdString(), request);

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
                
                return ServiceState.STATE_FAILED; 
            }
            
            ArrayMap resultMap = (ArrayMap) jsonResult.get("result");
        
            if (resultMap != null) {
                Boolean success = (Boolean) resultMap.get("success");  
                
                if (success == null) {
                    
                    String message = (String) resultMap.get("message");
                    if ((message!=null) && message.startsWith("Running")) {
                        getTaskInfo().setRunning(false);
                    }
                    
                    return ServiceState.STATE_WAITING;
                }
                
                if (success) {

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
            return ServiceState.STATE_FAILED; 
        }      
        return ServiceState.STATE_WAITING;
    }
    
}
