/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.dpm.task;

import com.google.api.client.http.HttpResponse;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.rpc2.JsonRpcRequest;
import com.google.api.client.util.ArrayMap;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author JM235353
 */
public class MergeTask extends AbstractServiceTask {

    private List<Long> m_resultSetIdList = null;
    private long m_projectId = -1;
    private Long[] m_resultSetId = null;
    
    public MergeTask(AbstractServiceCallback callback, long projectId, List<Long> resultSetIdList, String parentName, Long[] resultSetId) {
        super(callback, false /*asynchronous*/, new TaskInfo("Merge on "+parentName, TASK_LIST_INFO));
        m_resultSetIdList = resultSetIdList;
        m_projectId = projectId;
        m_resultSetId = resultSetId;
    }
    
    @Override
    public boolean askService() {
        
        try {
            // create the request
            JsonRpcRequest request = new JsonRpcRequest();

            request.setId(id);
            request.setMethod("run_job");


            Map<String, Object> params = new HashMap<>();
            params.put("project_id", m_projectId);
            params.put("result_set_ids",m_resultSetIdList);
            
            request.setParameters(params);
            
            HttpResponse response = postRequest("dps.msi/merge_result_sets/"+request.getMethod()+getIdString(), request);

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
                    loggerWebcore.error(getClass().getSimpleName() + " failed : "+errorMessage);
                }
                
                return false;
            }
            
            BigDecimal jobId = (BigDecimal) jsonResult.get("result");
            if (jobId != null) {
                id = jobId.intValue();
            } else {
                loggerProline.error(getClass().getSimpleName() + " failed : job id not defined");
            }

        } catch (Exception e) {
            errorMessage = e.getMessage();
            loggerProline.error(getClass().getSimpleName() + " failed", e);
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

            Map<String, Object> params = new HashMap<>();
            params.put("job_id", id);

            request.setParameters(params);

            HttpResponse response = postRequest("dps.msi/merge_result_sets/" + request.getMethod() + getIdString(), request);

            
            GenericJson jsonResult = response.parseAs(GenericJson.class);

            ArrayMap errorMap = (ArrayMap) jsonResult.get("error");

            if (errorMap != null) {

                errorMessage = (String) errorMap.get("message");
                if (errorMessage == null) {
                    errorMessage = "";
                }
                
                
                String data = (String) errorMap.get("data");
                if (data != null) {
                    errorMessage = errorMessage+"\n"+data;
                }
                
                loggerWebcore.error(getClass().getSimpleName() + " failed "+errorMessage);
                return ServiceState.STATE_FAILED; // should not happen !
            }
            
            ArrayMap resultMap = (ArrayMap) jsonResult.get("result");
        
            if (resultMap != null) {
                Boolean success = (Boolean) resultMap.get("success");
                // key not used : "duration", "progression" JPM.TODO
                
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
                    
                    // retrieve resultSet id
                    BigDecimal resultSetIdBD = (BigDecimal) resultMap.get("result");
                    if (resultSetIdBD == null) {
                        loggerProline.error(getClass().getSimpleName() + " failed : No returned ResultSet Id");
                        return ServiceState.STATE_FAILED;
                    }
                    
                    m_resultSetId[0] = new Long(resultSetIdBD.longValue());
                    
                    return ServiceState.STATE_DONE;
                } else {
                    errorMessage = (String) resultMap.get("message");
                    if (errorMessage == null) {
                        errorMessage = "";
                    }
                    loggerWebcore.error(getClass().getSimpleName() + " failed "+errorMessage);
                    return ServiceState.STATE_FAILED;
                }
                
            }
            
        } catch (Exception e) {
            errorMessage = e.getMessage();
            loggerProline.error(getClass().getSimpleName() + " failed", e);
            return ServiceState.STATE_FAILED; // should not happen !
        }

        return ServiceState.STATE_WAITING;
    }
    
}
