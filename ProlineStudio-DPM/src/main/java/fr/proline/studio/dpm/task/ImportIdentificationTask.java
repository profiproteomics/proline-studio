package fr.proline.studio.dpm.task;

import com.google.api.client.http.HttpResponse;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.rpc2.JsonRpcRequest;
import com.google.api.client.util.ArrayMap;
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

    private String parserId;
    private HashMap<String, String> parserArguments;
    private String canonicalFilePath;
    private String decoyRegex;
    private long instrumentId;
    private long peaklistSoftwareId;
    private long projectId;
    private Long[] resultSetId = null;
    
    public ImportIdentificationTask(AbstractServiceCallback callback, String parserId, HashMap<String, String> parserArguments, String canonicalFilePath, String decoyRegex, long instrumentId, long peaklistSoftwareId, long projectId, Long[] resultSetId) {
        super(callback, false /*asynchronous*/, new TaskInfo("Import Identification "+canonicalFilePath, TASK_LIST_INFO));
        
        this.parserId = parserId;
        this.parserArguments = parserArguments;
        this.canonicalFilePath = canonicalFilePath;
        this.decoyRegex = decoyRegex;
        this.instrumentId = instrumentId;
        this.peaklistSoftwareId = peaklistSoftwareId;
        this.projectId = projectId;
        this.resultSetId = resultSetId;
    }
    
    @Override
    public boolean askService() {
        try {
            // create the request
            JsonRpcRequest request = new JsonRpcRequest();
            
            request.setId(id);
            request.setMethod("run_job");
            
            
            Map<String, Object> params = new HashMap<>();
	    params.put("project_id", projectId);
            
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
                if (canonicalFilePath.startsWith(serverFilePath)) {
                    canonicalFilePath = canonicalFilePath.substring(serverFilePath.length());
                }
            }
            
            
            
            // add the file to parse
            Map<String, Object> resultfile = new HashMap<>();
            resultfile.put("path", canonicalFilePath);  // files must be accessible from web-core by the same path
            resultfile.put("format", parserId);
            if (decoyRegex != null) {
                resultfile.put("decoy_strategy", decoyRegex);
            }
            args.add(resultfile);
            params.put("result_files", args);

            
            
            params.put("instrument_config_id", instrumentId);
            params.put("peaklist_software_id", peaklistSoftwareId);

            // parser arguments
            params.put("importer_properties", parserArguments);
            
            request.setParameters(params);

            HttpResponse response = postRequest("dps.msi/import_result_files/"+request.getMethod()+getIdString(), request);

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

            HttpResponse response = postRequest("dps.msi/import_result_files/"+request.getMethod()+getIdString(), request);

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

                    ArrayList returnedValues = (ArrayList) resultMap.get("result");
                    if ((returnedValues == null) || (returnedValues.isEmpty()))  {
                        loggerProline.error(getClass().getSimpleName() + " failed : No returned values");
                        return ServiceState.STATE_FAILED;
                    }
                    
                    ArrayMap returnedValuesMap = (ArrayMap) returnedValues.get(0);
                    
                    // retrieve resultSet id
                    BigDecimal resultSetIdBD = (BigDecimal) returnedValuesMap.get("target_result_set_id");
                    if (resultSetIdBD == null) {
                        loggerProline.error(getClass().getSimpleName() + " failed : No returned ResultSet Id");
                        return ServiceState.STATE_FAILED;
                    }
                    
                    resultSetId[0] = new Long(resultSetIdBD.longValue());
                    
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
