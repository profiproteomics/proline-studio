package fr.proline.studio.dpm.task;

import com.google.api.client.http.HttpResponse;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.rpc2.JsonRpcRequest;
import com.google.api.client.util.ArrayMap;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Task to import identifications from files
 * @author jm235353
 */
public class ImportIdentificationTask extends AbstractServiceTask {

    private String parserId;
    private HashMap<String, String> parserArguments;
    private String filePath;
    private int instrumentId;
    private int peaklistSoftwareId;
    private int projectId;
    
    public ImportIdentificationTask(AbstractServiceCallback callback, String parserId, HashMap<String, String> parserArguments, String filePath, int instrumentId, int peaklistSoftwareId, int projectId) {
        super(callback, false /*asynchronous*/);
        
        this.parserId = parserId;
        this.parserArguments = parserArguments;
        this.filePath = filePath;
        this.instrumentId = instrumentId;
        this.peaklistSoftwareId = peaklistSoftwareId;
        this.projectId = projectId;
    }
    
    @Override
    public boolean askService() {
        try {
            // create the request
            JsonRpcRequest request = new JsonRpcRequest();
            
            request.setId(id);
            request.setMethod("run_job");
            
            
            Map<String, Object> params = new HashMap<String, Object>();
	    params.put("project_id", projectId);
            
            List args = new ArrayList();
            
            // add the file to parse
            Map<String, Object> resultfile = new HashMap<String, Object>();
            resultfile.put("path", filePath);  // files must be accessible from web-core by the same path
            resultfile.put("format", parserId);
            args.add(resultfile);
            params.put("result_files", args);

            
            
            params.put("instrument_config_id", instrumentId);
            params.put("peaklist_software_id", peaklistSoftwareId);

            //JPM.TODO : parserArguments not used for the moment

            
            request.setParameters(params);

            HttpResponse response = postRequest("Proline/dps.msi/import_result_files/"+request.getMethod()+getIdString(), request);

            GenericJson jsonResult = response.parseAs(GenericJson.class);

            ArrayMap errorMap = (ArrayMap) jsonResult.get("error");

            if (errorMap != null) {
                String message = (String) errorMap.get("message");

                if (message != null) {
                    errorMessage = message.toString();
                }
                return false;
            }



        } catch (Exception e) {
            errorMessage = e.getMessage();
            logger.error(getClass().getSimpleName() + " failed", e);
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
            
            Map<String, Object> params = new HashMap<String, Object>();
	    params.put("job_id", id);

            request.setParameters(params);

            HttpResponse response = postRequest("Proline/dps.msi/import_result_files/"+request.getMethod()+getIdString(), request);

            GenericJson jsonResult = response.parseAs(GenericJson.class);

            ArrayMap errorMap = (ArrayMap) jsonResult.get("error");

            if (errorMap != null) {

                errorMessage = (String) errorMap.get("message");
                if (errorMessage == null) {
                    errorMessage = "";
                }
                logger.error(getClass().getSimpleName() + " failed "+errorMessage);
                return ServiceState.STATE_FAILED; // should not happen !
            }
            
            ArrayMap resultMap = (ArrayMap) jsonResult.get("result");
        
            if (resultMap != null) {
                Boolean success = (Boolean) resultMap.get("success");
                // key not used : "duration", "progression" JPM.TODO
                
                if (success == null) {
                    return ServiceState.STATE_WAITING;
                }
                
                if (success) {
                    return ServiceState.STATE_DONE;
                } else {
                    errorMessage = (String) resultMap.get("message");
                    if (errorMessage == null) {
                        errorMessage = "";
                    }
                    logger.error(getClass().getSimpleName() + " failed "+errorMessage);
                    return ServiceState.STATE_FAILED;
                }
                
            }

            

        } catch (Exception e) {
            errorMessage = e.getMessage();
            logger.error(getClass().getSimpleName() + " failed", e);
            return ServiceState.STATE_FAILED; // should not happen !
        }
               
        return ServiceState.STATE_WAITING;
        
    }
    
}
