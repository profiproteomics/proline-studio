package fr.proline.studio.dpm.task;

import com.google.api.client.http.HttpResponse;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.rpc2.JsonRpcRequest;
import com.google.api.client.util.ArrayMap;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import static fr.proline.studio.dpm.task.AbstractServiceTask.m_loggerWebcore;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author VD225637
 */
public class FilterRSMProtSetsTask extends AbstractServiceTask {
    
    private DDataset m_dataset = null;
    private HashMap<String, String> m_argumentsMap;

    //Protein PreFilter
    public static String[] FILTER_KEYS = {"SPECIFIC_PEP","PEP_COUNT", "PEP_SEQ_COUNT"};
    public static String[] FILTER_NAME = {"Specific Peptides","Peptides count", "Peptide sequence count"};

    public FilterRSMProtSetsTask(AbstractServiceCallback callback,   DDataset dataset,HashMap<String, String> argumentsMap) {        
        super(callback, false /*asynchronous*/, new TaskInfo("Filter Protein Sets of Identifcation Summary "+dataset.getName(), true, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_HIGH));
        m_argumentsMap = argumentsMap;
        m_dataset = dataset;        
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
            params.put("result_summary_id", m_dataset.getResultSummaryId());
             
                
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
            
            
  
            request.setParameters(params);

            HttpResponse response = postRequest("dps.msi/filter_proteinsets_rsm/"+request.getMethod()+getIdString(), request);

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



        } catch (NumberFormatException | IOException e) {
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

            HttpResponse response = postRequest("dps.msi/filter_proteinsets_rsm/"+request.getMethod()+getIdString(), request);

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

                    Boolean resultStatus = (Boolean) resultMap.get("result");
                    if (!resultStatus) {
                        m_loggerProline.error(getClass().getSimpleName() + " failed ");
                        return ServiceState.STATE_FAILED;
                    }
                                        
                    if(m_dataset.getResultSummary() != null){
                        m_dataset.getResultSummary().getTransientData().setProteinSetArray(null);
                    }
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
