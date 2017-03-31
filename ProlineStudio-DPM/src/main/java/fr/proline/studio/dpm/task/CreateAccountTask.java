package fr.proline.studio.dpm.task;

import com.google.api.client.http.HttpResponse;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.rpc2.JsonRpcRequest;
import com.google.api.client.util.ArrayMap;
import fr.profi.util.security.EncryptionManager;
import fr.profi.util.security.SecurityUtils;
import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import static fr.proline.studio.dpm.task.AbstractServiceTask.TASK_LIST_INFO;
import static fr.proline.studio.dpm.task.AbstractServiceTask.m_loggerProline;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/** REMOVE REMOVE REMOVE REMOVE
 *
 * @author JM235353
 */
public class CreateAccountTask extends AbstractServiceTask {

    private String m_userName;
    private String m_password;
    
    
    public CreateAccountTask(AbstractServiceCallback callback, String userName, String newPassword) {
        super(callback, true /** synchronous */, new TaskInfo("Create User Account for " + userName, false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_HIGH));

        m_userName = userName;
        m_password = newPassword;    
    }
    
    @Override
    public boolean askService() {

        try {
            // create the request
            JsonRpcRequest request = new JsonRpcRequest();
            request.setId(m_id);
            request.setMethod("create"); 
            
            Map<String, Object> params = new HashMap<>();
            params.put("login", m_userName);
            params.put("password_hash", SecurityUtils.sha256Hex(m_password));
            
            request.setParameters(params);

            HttpResponse response;
            try {
                response = postRequest( "admin/user_account/"+request.getMethod()+getIdString(), request); //JPM.TODO
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
                m_taskError = new TaskError("Internal Error : No Result received");
                return false;
            }
            
            String errorMessage = (String) result.get("error_message");
            if (errorMessage != null) {
                m_taskError = new TaskError(errorMessage);
                return false;
            }
            
            BigDecimal status = (BigDecimal) result.get("status");
            if (status == null) {
                m_taskError = new TaskError("Internal Error : No Status received");
                return false;
            }
            
            if (status.intValue() == -1) {
                m_taskError = new TaskError("Create User Account failed");
                return false;
            }
            
            
        } catch (Exception e) {
            m_taskError = new TaskError(e);

            m_loggerProline.error(getClass().getSimpleName()+" failed", e);
            return false;
        }

        return true;
    }

    @Override
    public ServiceState getServiceState() {
        // always returns STATE_DONE because it
        // is a synchronous service
        return AbstractServiceTask.ServiceState.STATE_DONE;
    }
    
}
