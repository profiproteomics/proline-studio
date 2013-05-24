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
import fr.proline.studio.dam.data.ProjectData;
import fr.proline.core.orm.uds.Project;
import fr.proline.core.orm.util.DataStoreConnectorFactory;
import fr.proline.studio.dam.UDSDataManager;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import javax.persistence.EntityManager;

/**
 * Task to create a new Project in the UDS db
 * @author jm235353
 */
public class CreateProjectTask extends AbstractServiceTask {

    private String name;
    private String description;
    private int ownerId;
    private ProjectData projectData;
    
    public CreateProjectTask(AbstractServiceCallback callback, String name, String description, int ownerId, ProjectData projectData) {
        super(callback, true /*synchronous*/, new TaskInfo("Add Project named "+name, TASK_LIST_INFO));
        
        this.name = name;
        this.description = description;
        this.ownerId = ownerId;
        this.projectData = projectData;
    }
    
    @Override
    public boolean askService() {
        
        BigDecimal idProject;
        
        try {
            // create the request
            JsonRpcRequest request = new JsonRpcRequest();
            request.setId(id);
            request.setMethod("create");
            Map<String, Object> params = new HashMap<>();
            params.put("name", name);
            params.put("description", description);
            params.put("owner_id", ownerId);
            request.setParameters(params);

            HttpResponse response = postRequest("admin/project/"+request.getMethod()+getIdString(), request);
            
            GenericJson jsonResult = response.parseAs(GenericJson.class);

            ArrayMap errorMap = (ArrayMap) jsonResult.get("error");
            
            errorMessage = null;
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
                
                
                 //JPM.WART : Web core returns an error and the project id !!!
                loggerWebcore.error(getClass().getSimpleName()+errorMessage);
               // return false; 
            }

            idProject = (BigDecimal) jsonResult.get("result");
            if (idProject == null) {
                if (errorMessage == null) { //JPM.WART
                    errorMessage = "Internal Error : Project Id not found";
                }
                return false;
            }
            
            
            
        } catch (Exception e) {
            errorMessage = e.getMessage();
            loggerProline.error(getClass().getSimpleName()+" failed", e);
            return false;
        }
        
        EntityManager entityManagerUDS = DataStoreConnectorFactory.getInstance().getUdsDbConnector().getEntityManagerFactory().createEntityManager();
        try {
            entityManagerUDS.getTransaction().begin();
        
            Project p = entityManagerUDS.find(Project.class, new Integer(idProject.intValue()));

            if (p == null) {
                errorMessage = "Internal Error : ";
                return false;
            }

            // create the trash
            Dataset trashDataset = new Dataset(p);
            trashDataset.setType(Dataset.DatasetType.TRASH);

            Aggregation aggregation = UDSDataManager.getUDSDataManager().getAggregation(Aggregation.ChildNature.OTHER);
            Aggregation mergedAggregation = entityManagerUDS.merge(aggregation);
            trashDataset.setAggregation(mergedAggregation);

            trashDataset.setName("Trash");
            trashDataset.setChildrenCount(0); // trash is empty

            trashDataset.setNumber(0); //JPM.TODO ?

            p.getTransientData().setChildrenNumber(1);

            entityManagerUDS.persist(trashDataset);
            
            projectData.setProject(p);
            
            entityManagerUDS.getTransaction().commit();

        } catch (Exception e) {
            loggerProline.error(getClass().getSimpleName() + " failed", e);
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
