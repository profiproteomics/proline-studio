package fr.proline.studio.dpm.task;

import com.google.api.client.http.HttpResponse;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.rpc2.JsonRpcRequest;
import com.google.api.client.util.ArrayMap;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dpm.data.CVParam;
import static fr.proline.studio.dpm.task.AbstractServiceTask.TASK_LIST_INFO;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Task to export a dataset (it could be identification, XIC or SC...) It uses a
 * configuration file to specify the export. It will replace ExportXICTask and
 * ExportRSMTask
 *
 * @author MB243701
 */
public class ExportDatasetTask extends AbstractServiceTask {

    private static final String m_request = "dps.msi/export_result_summaries/";
    private static final String m_version = "0.3";
    private List<DDataset> m_datasetList;
    private List<String> m_filePathResult;
    private HashMap<String, Object> m_exportParams;
    private boolean m_export2Pride;
    private String m_configStr;

    /* export dataset constructor*/
    public ExportDatasetTask(AbstractServiceCallback callback, List<DDataset> listDataset, String configStr, List<String> filePathInfo) {
        super(callback, false /**
                 * asynchronous
                 */
                , new TaskInfo("Export Dataset for " + listDataset.size() + " datasets", true, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_HIGH));
        m_datasetList = listDataset;
        m_filePathResult = filePathInfo;
        m_export2Pride = false;
        m_exportParams = null;
        m_configStr = configStr;
    }

    @Override
    public boolean askService() {
        try {
            // create the request
            JsonRpcRequest request = new JsonRpcRequest();

            request.setId(m_id);
            request.setMethod("run_job");
            setWsVersion(m_version);

            Map<String, Object> params = new HashMap<>();
            if (m_export2Pride) {
                params.put("file_format", "PRIDE"); //Ou MZIDENTML ...
                HashMap<String, Object> finalExportParams = new HashMap<>();
                finalExportParams.putAll(m_exportParams);
                if (m_exportParams.containsKey("sample_additional")) {
                    finalExportParams.remove("sample_additional");
                    List<CVParam> additionals = (List<CVParam>) m_exportParams.get("sample_additional");
                    List<String> additionalsXmlString = new ArrayList<>(additionals.size());
                    for (CVParam nextCVParam : additionals) {
                        additionalsXmlString.add(nextCVParam.toXMLString());
                    }
                    finalExportParams.put("sample_additional", additionalsXmlString);
                }
                params.put("extra_params", finalExportParams); //Ou MZIDENTML ...
            } else {
                params.put("file_format", "TEMPLATED"); //Ou MZIDENTML ...
            }
            // **** Pour la version FILE :"file_name" & "file_directory" 
            params.put("output_mode", "STREAM"); // *** ou STREAM

            ArrayList rsmIdents = new ArrayList();
            for (DDataset dataset : m_datasetList) {
                Map<String, Object> rsmIdent = new HashMap<>();
                rsmIdent.put("project_id", dataset.getProject().getId());
                rsmIdent.put("ds_id", dataset.getId());
                Long rsmId = dataset.getResultSummaryId();
                if (dataset.getMasterQuantitationChannels() != null && !dataset.getMasterQuantitationChannels().isEmpty()) {
                    rsmId = dataset.getMasterQuantitationChannels().get(0).getQuantResultSummaryId();
                }
                rsmIdent.put("rsm_id", rsmId);
                rsmIdents.add(rsmIdent);
            }
            params.put("rsm_identifiers", rsmIdents);

            Map<String, Object> extraParams = new HashMap<>();

            extraParams.put("config", m_configStr);
            params.put("extra_params", extraParams);

            request.setParameters(params);

            HttpResponse response = postRequest(m_request + request.getMethod() + getIdString() + getWSVersionString(), request);

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
    public AbstractServiceTask.ServiceState getServiceState() {

        try {

            // create the request
            JsonRpcRequest request = new JsonRpcRequest();

            request.setId(m_idIncrement++);
            request.setMethod("get_job_status");

            Map<String, Object> params = new HashMap<>();
            params.put("job_id", m_id);

            request.setParameters(params);

            HttpResponse response = postRequest(m_request + request.getMethod() + getIdString(), request);

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

                return AbstractServiceTask.ServiceState.STATE_FAILED; // should not happen !
            }

            ArrayMap resultMap = (ArrayMap) jsonResult.get("result");

            if (resultMap != null) {
                Boolean success = (Boolean) resultMap.get("success");  //JPM.TODO : get ResultSummary created

                if (success == null) {

                    String message = (String) resultMap.get("message");
                    if ((message != null) && message.startsWith("Running")) {
                        getTaskInfo().setRunning(false);
                    }

                    return AbstractServiceTask.ServiceState.STATE_WAITING;
                }

                if (success) {

                    BigDecimal duration = (BigDecimal) resultMap.get("duration");
                    if (duration != null) {
                        getTaskInfo().setDuration(duration.longValue());
                    }

                    // retrieve file path
                    ArrayList exportedFilePathList = (ArrayList) resultMap.get("result");
                    if (exportedFilePathList == null || exportedFilePathList.isEmpty()) {
                        m_loggerProline.error(getClass().getSimpleName() + " failed : No file path returned.");
                        return AbstractServiceTask.ServiceState.STATE_FAILED;
                    }
                    // in case of TSV format, we have different files to download
                    for (Object exportedFilePathList1 : exportedFilePathList) {
                        m_filePathResult.add((String) exportedFilePathList1);
                    }
                    //m_filePathResult[0] = (String) exportedFilePathList.get(0);

                    return AbstractServiceTask.ServiceState.STATE_DONE;
                } else {
                    String errorMessage = (String) resultMap.get("message");

                    if (errorMessage != null) {
                        m_taskError = new TaskError(errorMessage);
                        m_loggerWebcore.error(getClass().getSimpleName() + " failed : " + errorMessage);
                    }

                    return AbstractServiceTask.ServiceState.STATE_FAILED;
                }

            }

        } catch (Exception e) {
            m_taskError = new TaskError(e);
            m_loggerProline.error(getClass().getSimpleName() + " failed", e);
            return AbstractServiceTask.ServiceState.STATE_FAILED; // should not happen !
        }

        return AbstractServiceTask.ServiceState.STATE_WAITING;

    }
}
