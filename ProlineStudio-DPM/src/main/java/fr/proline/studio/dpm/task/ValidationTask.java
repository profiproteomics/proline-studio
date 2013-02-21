package fr.proline.studio.dpm.task;

import com.google.api.client.http.HttpResponse;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.rpc2.JsonRpcRequest;
import com.google.api.client.util.ArrayMap;
import fr.proline.core.orm.uds.Dataset;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import java.util.HashMap;
import java.util.Map;

/**
 * Task to start the validation service
 * @author jm235353
 */
public class ValidationTask extends AbstractServiceTask {

    private Dataset dataset = null;
    String description;
    HashMap<String, String> parserArguments;
    
    public ValidationTask(AbstractServiceCallback callback, Dataset dataset, String description, HashMap<String, String> parserArguments) {
        super(callback, false /*asynchronous*/, new TaskInfo("Validation", "Validation of "+dataset.getName()+" Result Set", TASK_LIST_INFO));
        this.dataset = dataset;
        this.description = description;
        this.parserArguments = parserArguments;
    }
    
    @Override
    public boolean askService() {
        try {
            // create the request
            JsonRpcRequest request = new JsonRpcRequest();
            
            request.setId(id);
            request.setMethod("run_job");
            
            
            Map<String, Object> params = new HashMap<String, Object>();
	    params.put("project_id", dataset.getProject().getId());  
            params.put("result_set_id", dataset.getResultSetId() );
            params.put("description", description ); //JPM.TODO : string is ""
            params.put("mode", "" );  //JPM.TODO : checked : 14/02/2013 not used in WebCore ValidateResultSet.scala
            
            // parserArguments : not used //JPM.TODO
            
            //params.put("pep_match_params", "" );  //JPM.TODO //JPM.TODO : parameters
            //params.put("prot_set_params", "" );  //JPM.TODO
            
            /*
            List args = new ArrayList();
            
            // add the file to parse
            Map<String, Object> resultfile = new HashMap<String, Object>();
            resultfile.put("path", filePath);  //JPM.TODO : possible file access problem
            resultfile.put("format", "mascot.dat");  //JPM.TODO
            args.add(resultfile);
            params.put("result_files", args);
*/


            request.setParameters(params);

            HttpResponse response = postRequest("Proline/dps.msi/validate_result_set/"+request.getMethod()+getIdString(), request);

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

            HttpResponse response = postRequest("Proline/dps.msi/validate_result_set/"+request.getMethod()+getIdString(), request); //JPM.TODO

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
                    
                    //JPM.TODO : get ResultRsmId
                    
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
