package fr.proline.studio.dpm.task;

import com.google.api.client.http.*;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.rpc2.JsonRpcRequest;
import com.google.api.client.util.ArrayMap;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import java.util.HashMap;
import java.util.Map;


/**
 * Task to create a Project User in the UDS db
 * @author jm235353
 */
public class CreateUserTask extends AbstractServiceTask {

    private String name;

    
    public CreateUserTask(AbstractServiceCallback callback, String name) {
        super(callback, true /*synchronous*/, new TaskInfo("Create User", "Create User "+name, TASK_LIST_INFO));
        
        this.name = name;

    }
    
    @Override
    public boolean askService() {
        


        try {
            // create the request
            JsonRpcRequest request = new JsonRpcRequest();
            request.setId(id);
            request.setMethod("create");
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("login", name);
            request.setParameters(params);

            HttpResponse response = postRequest("admin/user_account/"+request.getMethod()+getIdString(), request);
            
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
                
                return false;
            }

            // JPM.TODO : check result
            
        } catch (Exception e) {
            errorMessage = e.getMessage();
            logger.error(getClass().getSimpleName()+" failed", e);
            return false;
        }
        
        return true;
    }
    

    @Override
    public AbstractServiceTask.ServiceState getServiceState() {
        // always returns STATE_DONE because to create a user
        // is a synchronous service
        return AbstractServiceTask.ServiceState.STATE_DONE;
    }
    
    

    
 
    
}
