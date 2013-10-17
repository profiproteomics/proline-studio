package fr.proline.studio.dpm.task;

import com.google.api.client.http.HttpResponse;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.rpc2.JsonRpcRequest;
import com.google.api.client.util.ArrayMap;
import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dam.taskinfo.TaskInfoManager;
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
public class ImportIdentificationTask extends AbstractServiceTask {

    private String m_parserId;
    private HashMap<String, String> m_parserArguments;
    private String m_canonicalFilePath;
    private String m_decoyRegex;
    private long m_instrumentId;
    private long m_peaklistSoftwareId;
    private long m_projectId;
    private boolean m_saveSpectrumMatches;
    private Long[] m_resultSetId = null;
    
    public ImportIdentificationTask(AbstractServiceCallback callback, String parserId, HashMap<String, String> parserArguments, String canonicalFilePath, String decoyRegex, long instrumentId, long peaklistSoftwareId, boolean saveSpectrumMatches, long projectId, Long[] resultSetId) {
        super(callback, false /*asynchronous*/, new TaskInfo("Import Identification "+canonicalFilePath, TASK_LIST_INFO));
        
        m_parserId = parserId;
        m_parserArguments = parserArguments;
        m_canonicalFilePath = canonicalFilePath;
        m_decoyRegex = decoyRegex;
        m_instrumentId = instrumentId;
        m_peaklistSoftwareId = peaklistSoftwareId;
        m_saveSpectrumMatches = saveSpectrumMatches;
        m_projectId = projectId;
        m_resultSetId = resultSetId;
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
            
            List args = new ArrayList();
            
            // retrieve the canonical server file path
            Preferences preferences = NbPreferences.root();
            String serverFilePath = preferences.get("ServerIdentificationFilePath", null);
            if (serverFilePath !=null) {
                // retrieve canonical file path when it is possible
                File serverFile = new File(serverFilePath);
                if (serverFile.exists() && serverFile.isDirectory()) {
                    try {
                        serverFilePath = serverFile.getCanonicalPath();
                    } catch (IOException ioe) {
                        
                    }
                }
                
                // if canonicalFilePath="D:\\dir1\dir2\foo.dat" and serverFilePath="D:\\dir1\";
                // then canonicalFilePath="dir2\foo.dat"
                if (m_canonicalFilePath.startsWith(serverFilePath)) {
                    m_canonicalFilePath = m_canonicalFilePath.substring(serverFilePath.length());
                }
            }
            
            
            
            // add the file to parse
            Map<String, Object> resultfile = new HashMap<>();
            resultfile.put("path", m_canonicalFilePath);  // files must be accessible from web-core by the same path
            resultfile.put("format", m_parserId);
            if (m_decoyRegex != null) {
                resultfile.put("decoy_strategy", m_decoyRegex);
            }
            args.add(resultfile);
            params.put("result_files", args);

            
            
            params.put("instrument_config_id", m_instrumentId);
            params.put("peaklist_software_id", m_peaklistSoftwareId);

            params.put("save_spectrum_matches", m_saveSpectrumMatches);
            
            // parser arguments
            params.put("importer_properties", m_parserArguments);
            
            request.setParameters(params);

            HttpResponse response = postRequest("dps.msi/import_result_files/"+request.getMethod()+getIdString(), request);

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

            HttpResponse response = postRequest("dps.msi/import_result_files/"+request.getMethod()+getIdString(), request);

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

                    ArrayList returnedValues = (ArrayList) resultMap.get("result");
                    if ((returnedValues == null) || (returnedValues.isEmpty()))  {
                        m_loggerProline.error(getClass().getSimpleName() + " failed : No returned values");
                        return ServiceState.STATE_FAILED;
                    }
                    
                    ArrayMap returnedValuesMap = (ArrayMap) returnedValues.get(0);
                    
                    // retrieve resultSet id
                    BigDecimal resultSetIdBD = (BigDecimal) returnedValuesMap.get("target_result_set_id");
                    if (resultSetIdBD == null) {
                        m_loggerProline.error(getClass().getSimpleName() + " failed : No returned ResultSet Id");
                        return ServiceState.STATE_FAILED;
                    }
                    
                    m_resultSetId[0] = new Long(resultSetIdBD.longValue());
                    
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
