package fr.proline.studio.dpm.task;

import com.google.api.client.http.*;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.rpc2.JsonRpcRequest;
import com.google.api.client.util.ArrayMap;
import fr.proline.repository.AbstractDatabaseConnector;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import java.util.HashMap;
import java.util.Map;



/**
 * Task to connect to the server the first time and get back the
 * parameters to connect to the UDS
 * @author jm235353
 */
public class ServerConnectionTask extends AbstractServiceTask {

    private String serverURL;
    private String password;
    private HashMap<Object, Object> databaseProperties;  // out parameter

    
    public ServerConnectionTask(AbstractServiceCallback callback, String serverURL, String password, HashMap<Object, Object> databaseProperties) {
        super(callback, true /*synchronous*/, new TaskInfo("Connection", "Connection to Server "+serverURL, TASK_LIST_INFO));
        
        this.serverURL = serverURL;
        this.password = password;
        this.databaseProperties = databaseProperties;
    }
    
    

    @Override
    public boolean askService() {

        
        

        try {
            // create the request
            JsonRpcRequest request = new JsonRpcRequest();
            request.setId(id);
            request.setMethod("template"); 
            
            Map<String, Object> params = new HashMap<String, Object>();
            //params.put("password", password);  //JPM.TODO
            request.setParameters(params); //JPM.TODO : check if we can set null parameters

            HttpResponse response = null;
            try {
                response = postRequest(serverURL, "Proline/admin/connection/"+request.getMethod()+getIdString(), request); //JPM.TODO
            } catch (Exception e) {
                errorMessage = "Server Connection Failed.";
                logger.error(getClass().getSimpleName()+" failed", errorMessage);
                logger.error(getClass().getSimpleName()+" failed", e);
                return false;
            }
            
            GenericJson jsonResult = response.parseAs(GenericJson.class);

            ArrayMap errorMap = (ArrayMap) jsonResult.get("error");
            
            if (errorMap != null) {
                String message = (String) errorMap.get("message");
                
                if (message != null) {
                    errorMessage = message.toString();
                }
                return false;
            }

            // retrieve database parameters
            ArrayMap result = (ArrayMap) jsonResult.get("result");
            if (result == null) {
                errorMessage = "Internal Error : Dabasase Parameters not returned";
                return false;
            }
            
            String databaseUser = (String) result.get("javax.persistence.jdbc.user");
            String databaseDriver = (String) result.get("javax.persistence.jdbc.driver");
            String databaseURL = (String) result.get("javax.persistence.jdbc.url");
      
            if ((databaseUser == null) || (databaseDriver == null) || (databaseURL == null)) {
                errorMessage = "Internal Error : Dabasase Parameters uncomplete";
                return false;
            }
            
            databaseProperties.put(AbstractDatabaseConnector.PERSISTENCE_JDBC_USER_KEY, databaseUser);
            databaseProperties.put(AbstractDatabaseConnector.PERSISTENCE_JDBC_PASSWORD_KEY, password);
            databaseProperties.put(AbstractDatabaseConnector.PERSISTENCE_JDBC_DRIVER_KEY, databaseDriver);
            databaseProperties.put(AbstractDatabaseConnector.PERSISTENCE_JDBC_URL_KEY, databaseURL);

   
            
        } catch (Exception e) {
            errorMessage = e.getMessage();
            if (errorMessage == null) {
                errorMessage = "Connection to Server failed";
            }
            logger.error(getClass().getSimpleName()+" failed", e);
            return false;
        }

        return true;
    }
    

    @Override
    public AbstractServiceTask.ServiceState getServiceState() {
        // always returns STATE_DONE because to retrieve UDS parameters
        // is a synchronous service
        return AbstractServiceTask.ServiceState.STATE_DONE;
    }
    
    

    
}
