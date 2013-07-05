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
 * Service to Merge ResultSet or ResultSummaries
 * @author JM235353
 */
public class MergeTask extends AbstractServiceTask {

    private List<Long> m_rsetOrRsmIdList = null;
    private long m_projectId = -1;
    private Long[] m_resultSetId = null;
    private Long[] m_resultSummaryId = null;
    
    private int m_action;
    
    private static final int MERGE_RSM = 0;
    private static final int MERGE_RSET = 1;
    
    public MergeTask(AbstractServiceCallback callback, long projectId) {
        super(callback, false /*asynchronous*/, null);
        m_projectId = projectId; 
    }
    
    public void initMergeRset(List<Long> resultSetIdList, String parentName, Long[] resultSetId) {
        setTaskInfo(new TaskInfo("Merge Search Results on "+parentName, TASK_LIST_INFO));
        m_rsetOrRsmIdList = resultSetIdList;
        m_resultSetId = resultSetId;
        m_action = MERGE_RSET;
    }
    
    public void initMergeRsm(List<Long> resultSummaryIdList, String parentName, Long[] resultSetId, Long[] resultSummaryId) {
        setTaskInfo(new TaskInfo("Merge Identification Summaries on "+parentName, TASK_LIST_INFO));
        m_rsetOrRsmIdList = resultSummaryIdList;
        m_resultSetId = resultSetId;
        m_resultSummaryId = resultSummaryId;
        m_action = MERGE_RSM;
    }
    
    
    @Override
    public boolean askService() {
        
        try {
            // create the request
            JsonRpcRequest request = new JsonRpcRequest();

            request.setId(m_id);
            request.setMethod("run_job");


            Map<String, Object> params = new HashMap<>();
            params.put("project_id", m_projectId);
            if (m_action == MERGE_RSET) {
                params.put("result_set_ids", m_rsetOrRsmIdList);
            } else {
                params.put("result_summary_ids", m_rsetOrRsmIdList);
            }
            
            request.setParameters(params);
            
            HttpResponse response = (m_action == MERGE_RSET) ? postRequest("dps.msi/merge_result_sets/"+request.getMethod()+getIdString(), request) :
                                                               postRequest("dps.msi/merge_result_summaries/"+request.getMethod()+getIdString(), request);

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
                m_loggerProline.error(getClass().getSimpleName() + " failed : job id not defined");
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

            HttpResponse response = (m_action == MERGE_RSET) ? postRequest("dps.msi/merge_result_sets/"+request.getMethod()+getIdString(), request) :
                                                               postRequest("dps.msi/merge_result_summaries/"+request.getMethod()+getIdString(), request);
            
            GenericJson jsonResult = response.parseAs(GenericJson.class);

            ArrayMap errorMap = (ArrayMap) jsonResult.get("error");

            if (errorMap != null) {

                m_errorMessage = (String) errorMap.get("message");
                if (m_errorMessage == null) {
                    m_errorMessage = "";
                }
                
                
                String data = (String) errorMap.get("data");
                if (data != null) {
                    m_errorMessage = m_errorMessage+"\n"+data;
                }
                
                m_loggerWebcore.error(getClass().getSimpleName() + " failed "+m_errorMessage);
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
                    
                    if (m_action == MERGE_RSET) {
                        BigDecimal resultSetIdBD = (BigDecimal) resultMap.get("result");
                        if (resultSetIdBD == null) {
                            m_loggerProline.error(getClass().getSimpleName() + " failed : No returned ResultSet Id");
                            return ServiceState.STATE_FAILED;
                        }



                        m_resultSetId[0] = new Long(resultSetIdBD.longValue());
                    } else {
                        ArrayMap result = (ArrayMap) resultMap.get("result");
                        
                        BigDecimal resultSetIdBD = (BigDecimal) result.get("target_result_set_id");
                        if (resultSetIdBD == null) {
                            m_loggerProline.error(getClass().getSimpleName() + " failed : No returned ResultSet Id");
                            return ServiceState.STATE_FAILED;
                        }
                        m_resultSetId[0] = new Long(resultSetIdBD.longValue());
                        
                        BigDecimal resultSummaryIdBD = (BigDecimal) result.get("target_result_summary_id");
                        if (resultSummaryIdBD == null) {
                            m_loggerProline.error(getClass().getSimpleName() + " failed : No returned ResultSummary Id");
                            return ServiceState.STATE_FAILED;
                        }
                        m_resultSummaryId[0] = new Long(resultSummaryIdBD.longValue());
                    }
                    

                    
                    return ServiceState.STATE_DONE;
                } else {
                    m_errorMessage = (String) resultMap.get("message");
                    if (m_errorMessage == null) {
                        m_errorMessage = "";
                    }
                    m_loggerWebcore.error(getClass().getSimpleName() + " failed "+m_errorMessage);
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
