package fr.proline.studio.dpm.task;

import com.google.api.client.http.*;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.rpc2.JsonRpcRequest;
import com.google.api.client.util.ArrayMap;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dpm.serverfilesystem.ServerFile;
import java.util.ArrayList;

/**
 * Task to create a new Project in the UDS db
 * @author jm235353
 */
public class FileSystemBrowseTask extends AbstractServiceTask {

    private String m_dirPath;
    private ArrayList<ServerFile> m_files;
    
    public FileSystemBrowseTask(AbstractServiceCallback callback, String dirPath, ArrayList<ServerFile> files) {
        super(callback, true /*synchronous*/, new TaskInfo("Browse Server File System "+dirPath, true, TASK_LIST_INFO));
        
        m_dirPath = dirPath;
        m_files = files;
    }
    
    @Override
    public boolean askService() {
        
        BigDecimal idProject;
        
        try {
            // create the request
            JsonRpcRequest request = new JsonRpcRequest();
            request.setId(m_id);
            request.setMethod("get_directory_content");
            Map<String, Object> params = new HashMap<>();
            params.put("dir_path", m_dirPath);
            params.put("include_files", Boolean.TRUE);
            params.put("include_dirs", Boolean.TRUE);
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
            if (result != null) { // result is null when directory is empty
                for (int i = 0; i < result.size(); i++) {
                    ArrayMap fileMap = (ArrayMap) result.get(i);

                    ServerFile f;
                    boolean isDir = (Boolean) fileMap.get("is_dir");
                    if (isDir) {
                        f = new ServerFile((String) fileMap.get("path"), (String) fileMap.get("name"), true, 0, 0);
                    } else {
                        f = new ServerFile((String) fileMap.get("path"), (String) fileMap.get("name"), false, ((BigDecimal) fileMap.get("lastmodified")).longValue(), ((BigDecimal) fileMap.get("size")).longValue());
                    }
                    m_files.add(f);
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
    public ServiceState getServiceState() {
        // always returns STATE_DONE because to browse files on server
        // is a synchronous service
        return ServiceState.STATE_DONE;
    }
    
    
    
}
