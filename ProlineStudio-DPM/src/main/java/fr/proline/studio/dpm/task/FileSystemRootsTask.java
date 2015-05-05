package fr.proline.studio.dpm.task;

import com.google.api.client.http.*;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.rpc2.JsonRpcRequest;
import com.google.api.client.util.ArrayMap;

import java.util.HashMap;
import java.util.Map;
import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dpm.serverfilesystem.RootInfo;
import java.util.ArrayList;

/**
 * Task to get the Roots path for the Virtual File System on the server
 * @author jm235353
 */
public class FileSystemRootsTask extends AbstractServiceTask {

    ArrayList<RootInfo> m_roots;
    
    public FileSystemRootsTask(AbstractServiceCallback callback, ArrayList<RootInfo> roots) {
        super(callback, true /*synchronous*/, new TaskInfo("Get Server File System Root Paths", true, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));
        
        m_roots = roots;
    }
    
    @Override
    public boolean askService() {

        try {
            // create the request
            JsonRpcRequest request = new JsonRpcRequest();
            request.setId(m_id);
            request.setMethod("retrieve_all_mount_points");
            Map<String, Object> params = new HashMap<>(); // no parameter
            request.setParameters(params);

            HttpResponse response = postRequest("admin/fs/"+request.getMethod()+getIdString(), request);
            
            GenericJson jsonResult = response.parseAs(GenericJson.class);

            ArrayMap errorMap = (ArrayMap) jsonResult.get("error");
            
            m_taskError = null;
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
                
                
                 //JPM.WART : Web core returns an error and the project id !!!
                m_loggerWebcore.error(getClass().getSimpleName()+m_taskError.toString());
               // return false; 
            }

            ArrayList result = (ArrayList) jsonResult.get("result");
            if ((result == null) || (result.isEmpty())) {
                 m_taskError = new TaskError("No Root Path","Server has returned no Root Path. There is a problem with the server installation, please contact your administrator.");
                 m_loggerProline. error("Server has returned no Root Path. There is a problem with the server installation, please contact your administrator.");
                 return false;
            } else  {
                for (int i = 0; i < result.size(); i++) {
                    ArrayMap fileMap = (ArrayMap) result.get(i);
                    String label = (String) fileMap.get("label");
                    String directoryType = (String) fileMap.get("directory_type");  // use it !
                    
                    m_roots.add(new RootInfo(label, directoryType));
                }
            }
            
        } catch (Exception e) {
            m_taskError = new TaskError(e);
            m_loggerProline.error(getClass().getSimpleName()+" failed", e);
            return false;
        }

        
        return true;
    }
    

    @Override
    public AbstractServiceTask.ServiceState getServiceState() {
        // always returns STATE_DONE because to get roots path from the server
        // is a synchronous service
        return AbstractServiceTask.ServiceState.STATE_DONE;
    }
    
    
    
}
