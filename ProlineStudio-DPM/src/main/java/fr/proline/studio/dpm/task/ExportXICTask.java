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
 * Task to export a XIC Dataset
 * @author JM235353
 */
public class ExportXICTask extends AbstractServiceTask {

    private DDataset m_dataset;
    private String[] m_filePathResult;
    private String m_exportType;
    private String m_templateName;
    
    public ExportXICTask(AbstractServiceCallback callback, DDataset dataset, String exportTypeParam, String[] filePathInfo, String templateName) {
        super(callback, false /** asynchronous */, new TaskInfo("Export XIC " + dataset.getName(), true, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_HIGH));
        m_dataset = dataset;
        m_exportType = exportTypeParam;
        m_filePathResult = filePathInfo;
        m_templateName = templateName;
    }
    
    
    @Override
    public boolean askService() {
        try {
            // create the request
            JsonRpcRequest request = new JsonRpcRequest();

            request.setId(m_id);
            request.setMethod("run_job");

            setWsVersion("0.2");
            Map<String, Object> params = new HashMap<>();
            params.put("output_mode", "STREAM"); // use of a stream, (do not export to file)
            params.put("project_id", m_dataset.getProject().getId());
            params.put("data_set_id", m_dataset.getId());
            params.put("export_type", m_exportType);
            
            Map<String, Object> extraParams = new HashMap<>();
            extraParams.put("template_name", m_templateName); 
            params.put("extra_params", extraParams);
            
            request.setParameters(params);

            HttpResponse response = postRequest("dps.msq/export_quant_dataset/" + request.getMethod() + getIdString()+getWSVersionString(), request);

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

            HttpResponse response = postRequest("dps.msq/export_quant_dataset/" + request.getMethod() + getIdString(), request);

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
                Boolean success = (Boolean) resultMap.get("success");  //JPM.TODO : get ResultSummary created

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

                    // retrieve file path
                    ArrayList exportedFilePathList = (ArrayList) resultMap.get("result");
                    if (exportedFilePathList == null ) {
                        m_loggerProline.error(getClass().getSimpleName() + " failed : No file path returned.");
                        return ServiceState.STATE_FAILED;
                    }

                    m_filePathResult[0] = (String) exportedFilePathList.get(0);


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
