package fr.proline.studio.dpm.task;

import com.google.api.client.http.*;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.rpc2.JsonRpcRequest;
import com.google.api.client.util.ArrayMap;
import fr.proline.core.orm.uds.RawFile;
import fr.proline.core.orm.uds.Run;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import fr.proline.core.orm.util.DataStoreConnectorFactory;
import fr.proline.studio.dam.data.RunInfoData;
import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import javax.persistence.EntityManager;

/**
 * Task to create a new Project in the UDS db
 * @author jm235353
 */
public class RegisterRawFileTask extends AbstractServiceTask {

    private String m_raw_file_path;
    private String m_raw_file_name;
    private long m_instrumentId;
    private long m_ownerId;
    private RunInfoData m_runInfoData;
            
    public RegisterRawFileTask(AbstractServiceCallback callback, long instrumentId, long ownerId, RunInfoData runInfo) {
        super(callback, true /*synchronous*/, new TaskInfo("Register raw file "+runInfo.getRawFileSouce().getRawFileOnDisk(), true, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));
        
        m_raw_file_path = runInfo.getRawFileSouce().getRawFileOnDisk().getPath();
        m_raw_file_name = runInfo.getRawFileSouce().getRawFileOnDisk().getName();
        m_ownerId = ownerId;
        m_instrumentId = instrumentId;
        m_runInfoData = runInfo;
    }
    
    @Override
    public boolean askService() {
        
        // first we check if the Raw File exists already or not 
        EntityManager entityManagerUDS = DataStoreConnectorFactory.getInstance().getUdsDbConnector().getEntityManagerFactory().createEntityManager();
        try {
            entityManagerUDS.getTransaction().begin();
        
            RawFile rawFile = entityManagerUDS.find(RawFile.class, m_raw_file_name);

            if (rawFile != null) {
                Run r = rawFile.getRuns().get(0);
                m_runInfoData.setRun(r);
                return true;
            }

            
            
            entityManagerUDS.getTransaction().commit();

        } catch (Exception e) {
            m_loggerProline.error(getClass().getSimpleName() + " failed", e);
            entityManagerUDS.getTransaction().rollback();
            return false;
        } finally {
            entityManagerUDS.close();
        }
        
        
        
        BigDecimal idRun;
        
        try {
            // create the request
            JsonRpcRequest request = new JsonRpcRequest();
            request.setId(m_id);
            request.setMethod("register");
            Map<String, Object> params = new HashMap<>();
            Map<String, String> propertiesMap = new HashMap<>();
            propertiesMap.put("mzdb_file_path", m_raw_file_path);
            params.put("raw_file_path", m_raw_file_path);
            params.put("properties", propertiesMap);
            params.put("instrument_id", m_instrumentId);
            params.put("owner_id", m_ownerId);
            request.setParameters(params);

            HttpResponse response = postRequest("das.uds/raw_file/"+request.getMethod()+getIdString(), request);
            
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

            idRun = (BigDecimal) jsonResult.get("result");
            if (idRun == null) {
                if (m_taskError == null) { //JPM.WART
                    m_taskError = new TaskError("Internal Error : Run Id not found");
                }
                return false;
            }
            
            
            
        } catch (Exception e) {
            m_taskError = new TaskError(e);
            m_loggerProline.error(getClass().getSimpleName()+" failed", e);
            return false;
        }
        
        entityManagerUDS = DataStoreConnectorFactory.getInstance().getUdsDbConnector().getEntityManagerFactory().createEntityManager();
        try {
            entityManagerUDS.getTransaction().begin();
        
            Run r = entityManagerUDS.find(Run.class, Long.valueOf(idRun.longValue()));

            if (r == null) {
                m_taskError = new TaskError("Internal Error : Project not Found");
                return false;
            }
            
            m_runInfoData.setRun(r);
            //m_runInfoData.setRunInfoInDatabase(true); //JPM.RUNINFODATA
            
            entityManagerUDS.getTransaction().commit();

        } catch (Exception e) {
            m_loggerProline.error(getClass().getSimpleName() + " failed", e);
            entityManagerUDS.getTransaction().rollback();
            return false;
        } finally {
            entityManagerUDS.close();
        }
        
        return true;
    }
    

    @Override
    public ServiceState getServiceState() {
        // always returns STATE_DONE because to create a project
        // is a synchronous service
        return ServiceState.STATE_DONE;
    }
    
    
    
}
