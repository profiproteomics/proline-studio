package fr.proline.studio.dpm.task;

import com.google.api.client.http.*;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.rpc2.JsonRpcRequest;
import com.google.api.client.util.ArrayMap;
import fr.proline.repository.AbstractDatabaseConnector;
import java.util.HashMap;
import java.util.Map;

import java.io.IOException;


/**
 * Task to connect to the server the first time and get back the
 * parameters to connect to the UDS
 * @author jm235353
 */
public class ServerConnectionTask extends AbstractServiceTask {

    private String serverURL;
    private String projectUser;
    private String password;
    private HashMap<Object, Object> databaseProperties;  // out parameter

    
    public ServerConnectionTask(AbstractServiceCallback callback, String serverURL, String projectUser, String password, HashMap<Object, Object> databaseProperties) {
        super(callback, true /*synchronous*/);
        
        this.serverURL = serverURL;
        this.projectUser = projectUser;
        this.password = password;
        this.databaseProperties = databaseProperties;
    }
    
    //@Override
    public boolean askService() {
        
        //JPM.TODO : look to the methode bellow
        databaseProperties.put(AbstractDatabaseConnector.PERSISTENCE_JDBC_USER_KEY, "dupierris");
        databaseProperties.put(AbstractDatabaseConnector.PERSISTENCE_JDBC_PASSWORD_KEY, "dupierris");
        databaseProperties.put(AbstractDatabaseConnector.PERSISTENCE_JDBC_DRIVER_KEY, "org.postgresql.Driver");
        

        databaseProperties.put(AbstractDatabaseConnector.PERSISTENCE_JDBC_URL_KEY, "jdbc:postgresql://localhost:5432/UDS_db");
        
        return true;
    }
    public boolean TODOaskService() {  //JPM.TODO

        
        

        try {
            // create the request
            JsonRpcRequest request = new JsonRpcRequest();
            request.setId(id);
            request.setMethod("JPM.TODO");  // JPM.TODO
            
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("user", projectUser);  //JPM.TODO
            params.put("password", password);  //JPM.TODO
            request.setParameters(params);

            HttpResponse response = null;
            try {
                response = postRequest(serverURL, "Proline/JPM.TODO"+request.getMethod()+getIdString(), request); //JPM.TODO
            } catch (IOException ioe) {
                errorMessage = "Impossible to connect to the server.";
                logger.error(getClass().getSimpleName()+" failed", errorMessage);
                logger.error(getClass().getSimpleName()+" failed", ioe);
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

            //HPM.TODO : get back database parameters
            /*idProject = jsonResult.get("result");
            if (idProject == null) {
                errorMessage = "Internal Error : Project Id not found";
                return false;
            }*/
            
            
            
        } catch (Exception e) {
            errorMessage = e.getMessage();
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
