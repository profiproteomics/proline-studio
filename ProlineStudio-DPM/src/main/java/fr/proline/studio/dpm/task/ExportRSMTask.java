package fr.proline.studio.dpm.task;

import com.google.api.client.http.HttpResponse;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.rpc2.JsonRpcRequest;
import com.google.api.client.util.ArrayMap;
import fr.proline.core.orm.uds.dto.DDataset;
import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dpm.data.CVParam;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Task to export a RSM (Identification Summary)
 * @author JM235353
 */
public class ExportRSMTask extends AbstractServiceTask {

    private DDataset m_dataset;
    private String[] m_filePathResult;
    HashMap<String,Object> m_exportParams;
    private boolean m_exportAllPSMs;
    private boolean m_export2Pride;
    
    public ExportRSMTask(AbstractServiceCallback callback, DDataset dataset, boolean exportAllPSMs, String[] filePathInfo) {
        super(callback, false /** asynchronous */, new TaskInfo("Export Identification Summary " + dataset.getName(), true, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_HIGH));
        m_dataset = dataset;
        m_exportAllPSMs =  exportAllPSMs;
        m_filePathResult = filePathInfo;
        m_export2Pride = false;
        m_exportParams = null;
    }
    
    public ExportRSMTask(AbstractServiceCallback callback, DDataset dataset, boolean exportAllPSMs,HashMap<String,Object> exportParams, boolean prideFormat, String[] filePathInfo) {
        super(callback, false /** asynchronous */, null);
        m_dataset = dataset;
        m_exportAllPSMs =  exportAllPSMs;
        m_filePathResult = filePathInfo;
        m_export2Pride = prideFormat;
        m_exportParams = exportParams;
        if(m_export2Pride)
            super.setTaskInfo( new TaskInfo("Export Ident. Summary " + dataset.getName()+" to Pride format", true, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_HIGH));
        else
            super.setTaskInfo( new TaskInfo("Export Identification Summary " + dataset.getName(), true, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_HIGH));
    }
    
    @Override
    public boolean askService() {
        try {
            // create the request
            JsonRpcRequest request = new JsonRpcRequest();
            
            request.setId(m_id);
            request.setMethod("run_job");

        Map<String, Object> params = new HashMap<>();
        if(m_export2Pride) {
            params.put("file_format", "PRIDE"); //Ou MZIDENTML ...
            HashMap<String,Object> finalExportParams = new HashMap<>();
            finalExportParams.putAll(m_exportParams);
            if(m_exportParams.containsKey("sample_additional")){            
                finalExportParams.remove("sample_additional");
                List<CVParam> additionals =  (List<CVParam>) m_exportParams.get("sample_additional");
                List<String> additionalsXmlString = new ArrayList<>(additionals.size());
                for(CVParam nextCVParam : additionals){
                    additionalsXmlString.add(nextCVParam.toXMLString());
                }
                finalExportParams.put("sample_additional", additionalsXmlString);           
            }
            if (m_exportParams.containsKey("protocol_steps")) {
                finalExportParams.remove("protocol_steps");
                List<CVParam> additionals = (List<CVParam>) m_exportParams.get("protocol_steps");
                List<String> additionalsXmlString = new ArrayList<>(additionals.size());
                for (CVParam nextCVParam : additionals) {
                    additionalsXmlString.add(nextCVParam.toXMLString());
                }
                finalExportParams.put("protocol_steps", additionalsXmlString);

            }
            params.put("extra_params", finalExportParams); //Ou MZIDENTML ...
        } else {

            params.put("file_format", "TEMPLATED"); //Ou MZIDENTML ...
            /*
            * "ALL_PEP_MATCHES_XLSX" -> AllPSMViewSetTemplateAsXLSX,
            * "IRMA_LIKE_TSV" -> IRMaLikeViewSetTemplateAsTSV,
            * "IRMA_LIKE_XLSX" -> IRMaLikeViewSetTemplateAsXLSX,
            * "IRMA_LIKE_FULL_XLSX" -> IRMaLikeFullViewSetTemplateAsXLSX
            */

            Map<String, Object> finalExportParams = new HashMap<>();
            if(m_exportAllPSMs){
                finalExportParams.put("template_name", "IRMA_LIKE_FULL_XLSX"); //************ TODO Liste des templates possibles ?! 
            } else {
                finalExportParams.put("template_name", "IRMA_LIKE_XLSX"); //************ TODO Liste des templates possibles ?! 
            }
            params.put("extra_params", finalExportParams);

        }
        // **** Pour la version FILE :"file_name" & "file_directory" 
        params.put("output_mode", "STREAM"); // *** ou STREAM
            
        ArrayList rsmIdents = new ArrayList();
        Map<String, Object> rsmIdent = new HashMap<>();
        rsmIdent.put("project_id", m_dataset.getProject().getId()); 
        rsmIdent.put("rsm_id", m_dataset.getResultSummaryId());
        rsmIdents.add(rsmIdent);
        params.put("rsm_identifiers",rsmIdents);

        
        request.setParameters(params);

            HttpResponse response = postRequest("dps.msi/export_result_summaries/"+request.getMethod()+getIdString(), request);

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

            HttpResponse response = postRequest("dps.msi/export_result_summaries/"+request.getMethod()+getIdString(), request);

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
                Boolean success = (Boolean) resultMap.get("success");  //JPM.TODO : get ResultSummary created
                
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
