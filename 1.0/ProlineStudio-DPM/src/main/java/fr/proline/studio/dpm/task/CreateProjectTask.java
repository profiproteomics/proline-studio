package fr.proline.studio.dpm.task;

import com.google.api.client.http.*;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.http.json.JsonHttpContent;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.json.rpc2.JsonRpcRequest;
import com.google.api.client.util.ArrayMap;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import fr.proline.studio.dam.data.ProjectData;
import fr.proline.core.orm.uds.Project;
import fr.proline.core.orm.util.DataStoreConnectorFactory;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import java.io.IOException;
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
        super(callback, true /*synchronous*/, new TaskInfo("Add Project", "Add Project named "+name, TASK_LIST_INFO));
        
        this.name = name;
        this.description = description;
        this.ownerId = ownerId;
        this.projectData = projectData;
    }
    
    @Override
    public boolean askService() {
        
        BigDecimal idProject = null;
        
        ////////////////////////////////////////////////
        ///////////////////////////////////////////////
        //postUserRequest();  //JPM.TODO : REMOVE !!!!!!!!

        try {
            // create the request
            JsonRpcRequest request = new JsonRpcRequest();
            request.setId(id);
            request.setMethod("create");
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("name", name);
            params.put("description", description);
            params.put("owner_id", ownerId);
            request.setParameters(params);

            HttpResponse response = postRequest("Proline/admin/project/"+request.getMethod()+getIdString(), request);
            
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
                logger.error(getClass().getSimpleName()+errorMessage);
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
            logger.error(getClass().getSimpleName()+" failed", e);
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
            
            projectData.setProject(p);
            
            entityManagerUDS.getTransaction().commit();

        } catch (Exception e) {
            logger.error(getClass().getSimpleName() + " failed", e);
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
    
    
    //////////////////////
    // JPM.TODO : remove two next methods
    
  /*  private static JsonRpcRequest createUserRequest() {

        JsonRpcRequest request = new JsonRpcRequest();
        request.setId(12356);
        //request.setMethod("create");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("login", "dupierris");
        request.setParameters(params);

        return request;
    }
    
    
    public static void postUserRequest() {
                HttpTransport transport = new ApacheHttpTransport();
        HttpRequestFactory factory = transport.createRequestFactory();
        try {

            JsonHttpContent content = new JsonHttpContent(new JacksonFactory(), createUserRequest().getParameters());
            HttpRequest request = factory.buildPostRequest(new GenericUrl("http://localhost:8080/Proline/admin/user_account/create"), content);

            //JsonHttpContent content = new JsonHttpContent(new JacksonFactory(), createProjectRequest().getParameters());
            //HttpRequest request = factory.buildPostRequest(new GenericUrl("http://localhost:8080/Proline/admin/project/create"), content);


            System.out.println(content.getData().toString());
            HttpResponse response = request.execute();
            System.out.println(response.parseAsString());
            
            

            
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }*/
    
}
