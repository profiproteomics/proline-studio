package fr.proline.studio.dpm.task;

import com.google.api.client.http.*;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.rpc2.JsonRpcRequest;
import com.google.api.client.util.ArrayMap;
import fr.proline.repository.AbstractDatabaseConnector;
import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dpm.task.util.JMSConnectionManager;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

 

/**
 * Task to connect to the server the first time and get back the
 * parameters to connect to the UDS
 * @author jm235353
 */
public class ServerConnectionTask extends AbstractServiceTask {

    private String m_serverURL;
    private String m_password;
    private HashMap<Object, Object> m_databaseProperties;  // out parameter

    
    public ServerConnectionTask(AbstractServiceCallback callback, String serverURL, String password, HashMap<Object, Object> databaseProperties) {
        super(callback, true /*synchronous*/, new TaskInfo("Connection to Server "+serverURL, false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_HIGH));
        
        m_serverURL = serverURL;
        m_password = password;
        m_databaseProperties = databaseProperties;
    }
    
    

    @Override
    public boolean askService() {


        try {
            // create the request
            JsonRpcRequest request = new JsonRpcRequest();
            request.setId(m_id);
            request.setMethod("template"); 
            
            Map<String, Object> params = new HashMap<>();
            //params.put("password", password);  //JPM.TODO
            request.setParameters(params); //JPM.TODO : check if we can set null parameters

            HttpResponse response;
            try {
                response = postRequest(m_serverURL, "admin/connection/"+request.getMethod()+getIdString(), request); //JPM.TODO
            } catch (Exception e) {
                m_taskError = new TaskError("Server Connection Failed.");
                m_loggerProline.error(getClass().getSimpleName()+" failed", m_taskError.getErrorTitle());
                m_loggerProline.error(getClass().getSimpleName()+" failed", e);
                return false;
            }
            
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
                
                return false;
            }

            // retrieve database parameters
            ArrayMap result = (ArrayMap) jsonResult.get("result");
            if (result == null) {
                m_taskError = new TaskError("Internal Error : Dabasase Parameters not returned");
                return false;
            }
            
            String databaseUser = (String) result.get("javax.persistence.jdbc.user");
            String databaseDriver = (String) result.get("javax.persistence.jdbc.driver");
            String databaseURL = (String) result.get("javax.persistence.jdbc.url");
            String jmsHost =  (String) result.get(JMSConnectionManager.JMS_SERVER_HOST_PARAM_KEY);
            BigDecimal jmsPort = (BigDecimal) result.get(JMSConnectionManager.JMS_SERVER_PORT_PARAM_KEY);
             
            if ((databaseUser == null) || (databaseDriver == null) || (databaseURL == null)) {
                 m_taskError = new TaskError("Internal Error : Dabasase Parameters uncomplete");
                return false;
            }
            
            m_databaseProperties.put(AbstractDatabaseConnector.PERSISTENCE_JDBC_USER_KEY, databaseUser);
            m_databaseProperties.put(AbstractDatabaseConnector.PERSISTENCE_JDBC_PASSWORD_KEY, m_password);
            m_databaseProperties.put(AbstractDatabaseConnector.PERSISTENCE_JDBC_DRIVER_KEY, databaseDriver);
            m_databaseProperties.put(AbstractDatabaseConnector.PERSISTENCE_JDBC_URL_KEY, databaseURL);
            m_databaseProperties.put(AbstractDatabaseConnector.PERSISTENCE_JDBC_URL_KEY, databaseURL);
            if(jmsHost != null)
                m_databaseProperties.put(JMSConnectionManager.JMS_SERVER_HOST_PARAM_KEY, jmsHost);
            if(jmsPort != null)
                m_databaseProperties.put(JMSConnectionManager.JMS_SERVER_PORT_PARAM_KEY, jmsPort.intValue());

   
            
        } catch (Exception e) {
            m_taskError = new TaskError(e);

            m_loggerProline.error(getClass().getSimpleName()+" failed", e);
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
