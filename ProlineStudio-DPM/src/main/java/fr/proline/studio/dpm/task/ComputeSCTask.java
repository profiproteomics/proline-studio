package fr.proline.studio.dpm.task;

import com.google.api.client.http.HttpResponse;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.rpc2.JsonRpcRequest;
import com.google.api.client.util.ArrayMap;
import fr.proline.core.orm.uds.Dataset;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Task to start the validation service
 * @author jm235353
 */
public class ComputeSCTask extends AbstractServiceTask {

    private Dataset m_refDataset = null;    
    List<Long> m_resultSummaryIds = null;
    
    String[] m_spCountJSONResult;
    protected static final Logger m_logger =  LoggerFactory.getLogger("ProlineStudio.DPM.Task");
    
    public ComputeSCTask(AbstractServiceCallback callback, Dataset refDataset, List<Long> resultSummaryIds,  String[] spectralCountResultList) {
        super(callback, false /*asynchronous*/, new TaskInfo("Compute Spectral Count based on "+refDataset.getName(), TASK_LIST_INFO));
        m_refDataset = refDataset;
        m_resultSummaryIds = resultSummaryIds;
        m_spCountJSONResult = spectralCountResultList;
    }
    
     
    @Override
    public boolean askService() {
        try {
            // create the request
            JsonRpcRequest request = new JsonRpcRequest();
            
            request.setId(m_id);
            request.setMethod("run_job");
            
            
            Map<String, Object> params = new HashMap<>();
	    params.put("project_id", m_refDataset.getProject().getId());  
            params.put("ref_result_summary_id", m_refDataset.getResultSummaryId() );
            params.put("compute_result_summary_ids", m_resultSummaryIds );                         
            request.setParameters(params);
            m_logger.debug("Will postRequest with params  project_id "+m_refDataset.getProject().getId()+" ; ref_result_summary_id "+m_refDataset.getResultSummaryId()+" ; compute_result_summary_ids "+m_resultSummaryIds);
            HttpResponse response = postRequest("dps.msq/compute_relative_sc/"+request.getMethod()+getIdString(), request);

            GenericJson jsonResult = response.parseAs(GenericJson.class);

            ArrayMap errorMap = (ArrayMap) jsonResult.get("error");

            if (errorMap != null) {
                String message = (String) errorMap.get("message");

                if (message != null) {
                    m_errorMessage = message;
                }
                
                String data = (String) errorMap.get("data");
                if (data != null) {
                    if (m_errorMessage == null) {
                        m_errorMessage = data;
                    } else {
                        m_errorMessage = m_errorMessage+"\n"+data;
                    }
                }
                
                if (m_errorMessage != null) {
                    m_loggerWebcore.error(getClass().getSimpleName() + " failed : "+m_errorMessage);
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
            m_errorMessage = e.getMessage();
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

            HttpResponse response = postRequest("dps.msq/compute_relative_sc/"+request.getMethod()+getIdString(), request);

            GenericJson jsonResult = response.parseAs(GenericJson.class);

            ArrayMap errorMap = (ArrayMap) jsonResult.get("error");

            if (errorMap != null) {

                m_errorMessage = (String) errorMap.get("message");
                
                String data = (String) errorMap.get("data");
                if (data != null) {
                    if (m_errorMessage == null) {
                        m_errorMessage = data;
                    } else {
                        m_errorMessage = m_errorMessage+"\n"+data;
                    }
                }
                
                if (m_errorMessage != null) {
                    m_loggerWebcore.error(getClass().getSimpleName() + " failed : " + m_errorMessage);
                }
                
                return ServiceState.STATE_FAILED; // should not happen !
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

                    BigDecimal duration = (BigDecimal) resultMap.get("duration");
                    if (duration != null) {
                        getTaskInfo().setDuration(duration.longValue());
                    }
                    
                    // retrieve resultSummary id
                    String resultSummariesSC = (String) resultMap.get("result");
                    if (resultSummariesSC == null || resultSummariesSC.isEmpty()) {
                        m_loggerProline.error(getClass().getSimpleName() + " failed : No Spectral Count returned.");
                        return ServiceState.STATE_FAILED;
                    }

                    m_spCountJSONResult[0] = resultSummariesSC;

                    return ServiceState.STATE_DONE;
                } else {
                    m_errorMessage = (String) resultMap.get("message");
                    
                    
                    if (m_errorMessage != null) {
                        m_loggerWebcore.error(getClass().getSimpleName() + " failed : " + m_errorMessage);
                    }
                
                    
                    return ServiceState.STATE_FAILED;
                }
                
            }

            

        } catch (Exception e) {
            m_errorMessage = e.getMessage();
            m_loggerProline.error(getClass().getSimpleName() + " failed", e);
            return ServiceState.STATE_FAILED; // should not happen !
        }
               
        return ServiceState.STATE_WAITING;
        
    }
}
