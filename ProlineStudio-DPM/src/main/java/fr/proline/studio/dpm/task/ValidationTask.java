package fr.proline.studio.dpm.task;

import com.google.api.client.http.HttpResponse;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.rpc2.JsonRpcRequest;
import com.google.api.client.util.ArrayMap;
import fr.proline.core.orm.uds.Dataset;
import java.util.HashMap;
import java.util.Map;

/**
 * Task to start the validation service
 * @author jm235353
 */
public class ValidationTask extends AbstractServiceTask {

    private Dataset dataset = null;
    String description;
    int peptideFDR;
    int peptideMinPepSequence;
    int proteinFDR;
    int proteinMinPepSequence;
    
    public ValidationTask(AbstractServiceCallback callback, Dataset dataset, String description, int peptideFDR, int peptideMinPepSequence, int proteinFDR, int proteinMinPepSequence) {
        super(callback, false /*asynchronous*/);
        this.dataset = dataset;
        this.description = description;
        this.peptideFDR = peptideFDR;
        this.peptideMinPepSequence = peptideMinPepSequence;
        this.proteinFDR = proteinFDR;
        this.proteinMinPepSequence = proteinMinPepSequence;
    }
    
    @Override
    public boolean askService() {
        try {
            // create the request
            JsonRpcRequest request = new JsonRpcRequest();
            
            request.setId(id);
            request.setMethod("JPM.TODO");  // JPM.TODO method name
            
            
            Map<String, Object> params = new HashMap<String, Object>();
	    /*params.put("project_id", projectId);  //JPM.TODO : parameters
            
            List args = new ArrayList();
            
            // add the file to parse
            Map<String, Object> resultfile = new HashMap<String, Object>();
            resultfile.put("path", filePath);  //JPM.TODO : possible file access problem
            resultfile.put("format", "mascot.dat");  //JPM.TODO
            args.add(resultfile);
            params.put("result_files", args);

            
            
            params.put("instrument_config_id", instrumentId);
            params.put("peaklist_software_id", peaklistSoftwareId);*/


            request.setParameters(params);

            HttpResponse response = postRequest("Proline/JPM.TODO"+request.getMethod()+getIdString(), request); //JPM.TODO : URL

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
            request.setMethod("get_job_status");  //JPM.TODO : check method name
            
            Map<String, Object> params = new HashMap<String, Object>();
	    params.put("job_id", id);

            request.setParameters(params);

            HttpResponse response = postRequest("Proline/JPM.TODO"+request.getMethod()+getIdString(), request); //JPM.TODO

            GenericJson jsonResult = response.parseAs(GenericJson.class);

            ArrayMap errorMap = (ArrayMap) jsonResult.get("error");

            if (errorMap != null) {

                errorMessage = (String) errorMap.get("message");
                return ServiceState.STATE_FAILED; // should not happen !
            }
            
            ArrayMap resultMap = (ArrayMap) jsonResult.get("result");
        
            if (resultMap != null) {
                Boolean success = (Boolean) resultMap.get("success");  //JPM.TODO : get ResultSummary created
                
                if (success == null) {
                    return ServiceState.STATE_WAITING;
                }
                
                if (success) {
                    return ServiceState.STATE_DONE;
                } else {
                    errorMessage = (String) resultMap.get("message");
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
