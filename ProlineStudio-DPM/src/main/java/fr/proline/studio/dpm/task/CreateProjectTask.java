package fr.proline.studio.dpm.task;

import com.google.api.client.http.*;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.rpc2.JsonRpcRequest;
import com.google.api.client.util.ArrayMap;
import fr.proline.core.orm.uds.Aggregation;
import fr.proline.core.orm.uds.Dataset;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import fr.proline.studio.dam.data.ProjectIdentificationData;
import fr.proline.core.orm.uds.Project;
import fr.proline.core.orm.util.DataStoreConnectorFactory;
import fr.proline.studio.dam.DatabaseDataManager;
import fr.proline.studio.dam.data.ProjectQuantitationData;
import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import javax.persistence.EntityManager;

/**
 * Task to create a new Project in the UDS db
 * @author jm235353
 */
public class CreateProjectTask extends AbstractServiceTask {

    private String m_name;
    private String m_description;
    private long m_ownerId;
    private ProjectIdentificationData m_projectIdentificationData;
    private ProjectQuantitationData m_projectQuantificationData;
    
    public CreateProjectTask(AbstractServiceCallback callback, String name, String description, long ownerId, ProjectIdentificationData projectIdentificationData, ProjectQuantitationData projectQuantificationData) {
        super(callback, true /*synchronous*/, new TaskInfo("Add Project named "+name, true, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_HIGH));
        
        m_name = name;
        m_description = description;
        m_ownerId = ownerId;
        m_projectIdentificationData = projectIdentificationData;
        m_projectQuantificationData = projectQuantificationData;
    }
    
    @Override
    public boolean askService() {
        
        BigDecimal idProject;
        
        try {
            // create the request
            JsonRpcRequest request = new JsonRpcRequest();
            request.setId(m_id);
            request.setMethod("create");
            Map<String, Object> params = new HashMap<>();
            params.put("name", m_name);
            params.put("description", m_description);
            params.put("owner_id", m_ownerId);
            request.setParameters(params);

            HttpResponse response = postRequest("admin/project/"+request.getMethod()+getIdString(), request);
            
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

            idProject = (BigDecimal) jsonResult.get("result");
            if (idProject == null) {
                if (m_taskError == null) { //JPM.WART
                    m_taskError = new TaskError("Internal Error : Project Id not found");
                }
                return false;
            }
            
            
            
        } catch (Exception e) {
            m_taskError = new TaskError(e);
            m_loggerProline.error(getClass().getSimpleName()+" failed", e);
            return false;
        }
        
        EntityManager entityManagerUDS = DataStoreConnectorFactory.getInstance().getUdsDbConnector().getEntityManagerFactory().createEntityManager();
        try {
            entityManagerUDS.getTransaction().begin();
        
            Project p = entityManagerUDS.find(Project.class, Long.valueOf(idProject.longValue()));

            if (p == null) {
                m_taskError = new TaskError("Internal Error : Project not Found");
                return false;
            }

            // create the trash
            // ----- No longer needed, done in create project -------
            
            //Dataset trashDataset = new Dataset(p);
            //trashDataset.setType(Dataset.DatasetType.TRASH);

            //Aggregation aggregation = DatabaseDataManager.getUDSDataManager().getAggregation(Aggregation.ChildNature.OTHER);
            //Aggregation mergedAggregation = entityManagerUDS.merge(aggregation);
            //trashDataset.setAggregation(mergedAggregation);

            //trashDataset.setName("Trash");
            //trashDataset.setChildrenCount(0); // trash is empty

            //trashDataset.setNumber(0); //JPM.TODO ?

            //p.getTransientData().setChildrenNumber(1);

            //entityManagerUDS.persist(trashDataset);
            
            m_projectIdentificationData.setProject(p);
            m_projectQuantificationData.setProject(p);
            
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
