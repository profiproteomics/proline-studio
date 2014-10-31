/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.dpm.task;

import com.google.api.client.http.HttpResponse;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.rpc2.JsonRpcRequest;
import com.google.api.client.util.ArrayMap;
import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Computes quantitative profiles of peptides and protein sets
 * @author MB243701
 */
public class ComputeQuantProfileTask extends AbstractServiceTask {

    private Long m_pId;
    
    private Long m_masterQuantChannelId;
    
    private Map<String,Object> m_quantProfileParams;
    
    public ComputeQuantProfileTask(AbstractServiceCallback callback, Long projectId,  Long masterQuantChannelId,  Map<String,Object> quantProfileParams, String xicName) {
        super(callback, false /** asynchronous */, new   TaskInfo("Compute Quantitation Profile for "+xicName, true, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_HIGH ));
        m_pId= projectId;
        m_masterQuantChannelId = masterQuantChannelId ;
        m_quantProfileParams = quantProfileParams ;
        
    }
    
    
    @Override
    public boolean askService() {
         //Create JSON for XIC Quanttitation service         
        try {
            
            // create the request
            JsonRpcRequest request = new JsonRpcRequest();

            request.setId(m_id);
            request.setMethod("run_job");

            //Create JSON parameters Strings 

            Map<String, Object> params = new HashMap<>();
            
            //-- Global PARAMS
            params.put("project_id", m_pId);
            params.put("master_quant_channel_id", m_masterQuantChannelId);
            
            // quantitation profile params
            params.put("config", m_quantProfileParams);
            
            
            request.setParameters(params);
            HttpResponse response = postRequest("dps.msq/compute_quant_profiles/" + request.getMethod() + getIdString(), request);

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
                    m_loggerWebcore.error(getClass().getSimpleName() + " failed : " + m_taskError.toString());
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
            m_taskError = new TaskError(e.getMessage());
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

            HttpResponse response = postRequest("dps.msq/compute_quant_profiles/"+request.getMethod()+getIdString(), request);

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
                    return ServiceState.STATE_DONE;
                } else {
                    m_taskError = new TaskError((String) resultMap.get("message"));
                    
                    
                    if (m_taskError != null) {
                        m_loggerWebcore.error(getClass().getSimpleName() + " failed : " + m_taskError.toString());
                    }
                
                    
                    return ServiceState.STATE_FAILED;
                }
                
            }

            

        } catch (Exception e) {
            m_taskError = new TaskError(e.getMessage());
            m_loggerProline.error(getClass().getSimpleName() + " failed", e);
            return ServiceState.STATE_FAILED; // should not happen !
        }
               
        return ServiceState.STATE_WAITING;
    }
    
}
