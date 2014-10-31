package fr.proline.studio.dpm.task;

import com.google.api.client.http.HttpResponse;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.rpc2.JsonRpcRequest;
import com.google.api.client.util.ArrayMap;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import java.math.BigDecimal;
import java.util.*;

/**
 *
 * @author JM235353
 */
public class RetrieveSpectralCountTask extends AbstractServiceTask {

    private DDataset m_refQuantDataset = null;
    private Long[] m_refRSMId = null;
    private Long[] m_refDSId = null;
    private String[] m_spCountJSONResult = null;

    public RetrieveSpectralCountTask(AbstractServiceCallback callback, DDataset refDataset, Long[] refRSMId, Long[] refDSId, String[] spectralCountResultList) {
        super(callback, false /** asynchronous*/, new TaskInfo("Retrieve Spectral Count on " + refDataset.getName(), true, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_HIGH));
        m_refQuantDataset = refDataset;
        m_refRSMId = refRSMId;
        m_refDSId = refDSId;
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
            params.put("project_id", m_refQuantDataset.getProject().getId());
            params.put("dataset_quanti_id", m_refQuantDataset.getId());

            request.setParameters(params);           
            
            HttpResponse response = postRequest("dps.msq/retrieve_wsc/" + request.getMethod() + getIdString(), request);

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

            HttpResponse response = postRequest("dps.msq/retrieve_wsc/" + request.getMethod() + getIdString(), request);

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

                return ServiceState.STATE_FAILED; // should not happen !
            }

            ArrayMap resultMap = (ArrayMap) jsonResult.get("result");

            if (resultMap != null) {
                Boolean success = (Boolean) resultMap.get("success");
                // key not used : "duration", "progression" JPM.TODO

                if (success == null) {

                    String message = (String) resultMap.get("message");
                    if ((message != null) && message.startsWith("Running")) {
                        getTaskInfo().setRunning(false);
                    }

                    return ServiceState.STATE_WAITING;
                }

                if (success) {

                    BigDecimal duration = (BigDecimal) resultMap.get("duration");
                    if (duration != null) {
                        getTaskInfo().setDuration(duration.longValue());
                    }

                    Map returnedValues = (Map) resultMap.get("result");
                    if ((returnedValues == null) || (returnedValues.isEmpty())) {
                        m_loggerProline.error(getClass().getSimpleName() + " failed : No returned values");
                        return ServiceState.STATE_FAILED;
                    }

//                    ArrayMap returnedValuesMap = (ArrayMap) returnedValues.get(0);

                    // retrieve identification Reference RSM ID
                    BigDecimal rsmIDFdBD = (BigDecimal) returnedValues.get("ref_rsm_id");
                    if (rsmIDFdBD == null) {
                        m_loggerProline.warn(getClass().getSimpleName() + " failed : No returned Ref RSM Id");
                    }
                    m_refRSMId[0] = new Long(rsmIDFdBD.longValue());

                     // retrieve identification Reference dataset ID
                    BigDecimal dsIDFdBD = (BigDecimal) returnedValues.get("ref_ds_id");
                    if (dsIDFdBD == null) {
                        m_loggerProline.warn(getClass().getSimpleName() + " failed : No returned Ref Dataset Id");
                        return ServiceState.STATE_FAILED;
                    }
                    m_refDSId[0] = new Long(dsIDFdBD.longValue());
                    
                    //retrieve SC Values as JSON String 
                    String scValues = (String) returnedValues.get("spectral_count_result");
                    if (scValues == null) {
                        m_loggerProline.error(getClass().getSimpleName() + " failed : No Spectral Count returned.");
                        return ServiceState.STATE_FAILED;
                    }
                    m_spCountJSONResult[0] = scValues;

                    return ServiceState.STATE_DONE;
                } else {
                    String errorMessage = (String) resultMap.get("message");
                    if (errorMessage == null) {
                        errorMessage = "";
                    } else {
                        m_taskError = new TaskError(errorMessage);
                    }
                    m_loggerWebcore.error(getClass().getSimpleName() + " failed " + errorMessage);
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
