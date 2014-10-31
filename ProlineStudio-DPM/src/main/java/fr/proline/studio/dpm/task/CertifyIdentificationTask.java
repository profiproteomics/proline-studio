package fr.proline.studio.dpm.task;

import com.google.api.client.http.HttpResponse;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.rpc2.JsonRpcRequest;
import com.google.api.client.util.ArrayMap;
import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 * Task to import identifications from files
 * @author jm235353
 */
public class CertifyIdentificationTask extends AbstractServiceTask {

    private String m_parserId;
    private HashMap<String, String> m_parserArguments;
    
    private String[] m_pathArray;
    private long m_projectId;
    private String[] m_certifyErrorMessage = null;
    
    public CertifyIdentificationTask(AbstractServiceCallback callback, String parserId, HashMap<String, String> parserArguments, String[] pathArray, long projectId, String[] certifyErrorMessage) {
        super(callback, false /*asynchronous*/, new TaskInfo("Check Files to Import : "+pathArray[0]+", ...", true, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_HIGH));

        m_parserId = parserId;
        m_parserArguments = parserArguments;
        m_pathArray = pathArray;
        m_projectId = projectId;
        m_certifyErrorMessage = certifyErrorMessage;
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

            
            // Result Files Parameter
            List args = new ArrayList();

            // retrieve the canonical server file path
            /*Preferences preferences = NbPreferences.root();
            String serverFilePath = preferences.get("ServerIdentificationFilePath", null);
            if (serverFilePath != null) {
                // retrieve canonical file path when it is possible
                File serverFile = new File(serverFilePath);
                if (serverFile.exists() && serverFile.isDirectory()) {
                    try {
                        serverFilePath = serverFile.getCanonicalPath();
                    } catch (IOException ioe) {
                    }
                }

            }*/

            for (int i = 0; i < m_pathArray.length; i++) {


                // add the file to parse
                Map<String, Object> resultfile = new HashMap<>();
                resultfile.put("path", m_pathArray[i]);  // files must be accessible from web-core by the same path
                resultfile.put("format", m_parserId);

                args.add(resultfile);

            }
            
            params.put("result_files", args);
            

            // parser arguments
            params.put("importer_properties", m_parserArguments);

            
            request.setParameters(params);

            HttpResponse response = postRequest("dps.msi/certify_result_files/"+request.getMethod()+getIdString(), request);

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
                m_loggerProline.error(getClass().getSimpleName() + " failed : job id not defined");
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

            HttpResponse response = postRequest("dps.msi/certify_result_files/"+request.getMethod()+getIdString(), request);

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

                    
                    
                    String returnedResult = (String) resultMap.get("result");
                    
                    if ((returnedResult == null) || (returnedResult.isEmpty()))  {
                        m_loggerProline.error(getClass().getSimpleName() + " failed : No returned values");
                        return ServiceState.STATE_FAILED;
                    }
 
                    
                    if (returnedResult.equalsIgnoreCase("OK")) {
                        m_certifyErrorMessage[0] = null;
                    } else {
                        m_certifyErrorMessage[0] = returnedResult;
                        
                        String errorMessage = returnedResult;
                        m_taskError = new TaskError(errorMessage);

                        m_loggerProline.error(getClass().getSimpleName() + " failed : returnedResult");
                        return ServiceState.STATE_FAILED;
                    }

                    
                    return ServiceState.STATE_DONE;
                } else {
                    String errorMessage = (String) resultMap.get("message");
                    if (errorMessage == null) {
                        errorMessage = "";
                    } else {
                        m_taskError = new TaskError(errorMessage);
                    }
                    m_loggerWebcore.error(getClass().getSimpleName() + " failed "+errorMessage);
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
