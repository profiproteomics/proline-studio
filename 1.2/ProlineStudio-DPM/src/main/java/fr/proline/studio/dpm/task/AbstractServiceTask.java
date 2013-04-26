package fr.proline.studio.dpm.task;

import com.google.api.client.http.*;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.http.json.JsonHttpContent;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.json.rpc2.JsonRpcRequest;
import com.google.api.client.json.JsonObjectParser;
import java.io.IOException;
import javax.swing.SwingUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import fr.proline.studio.dam.taskinfo.AbstractLongTask;
import fr.proline.studio.dam.taskinfo.TaskInfo;

/**
 * Superclass for all Task which wants to access to a web-core service and looks for
 * the result.
 * @author jm235353
 */
public abstract class AbstractServiceTask extends AbstractLongTask {
    
    public enum ServiceState {
        STATE_FAILED,
        STATE_WAITING,
        STATE_DONE
    };
    
    // callback is called by the AccessServiceThread when the service is done
    protected AbstractServiceCallback callback;
    
    
    protected int id;
    protected boolean synchronous;
    protected String errorMessage = null;
    
    protected static int idIncrement = 0;
    
    protected static String baseURL = "";
    
    protected static final Logger loggerProline = LoggerFactory.getLogger("ProlineStudio.DPM.Task");
    protected static final Logger loggerWebcore = LoggerFactory.getLogger("ProlineWebCore");
    
    public static final String TASK_LIST_INFO = "Services";
    
    public AbstractServiceTask(AbstractServiceCallback callback, boolean synchronous, TaskInfo taskInfo) {
        super(taskInfo);
        
        this.callback = callback;
        this.synchronous = synchronous;
        
        id = idIncrement++;
    }
    
    /**
     * Method called by the AccessServiceThread to ask for the service to be done
     */
    public abstract boolean askService();
    
    /**
     * Method called by the ServiceStatusThread
     * to check if the service is done
     */
    public abstract ServiceState getServiceState();


    
    /**
     * Method called by the AccessServiceThread to know if this service is asynchronous
     */
    public boolean isSynchronous() {
        return synchronous;
    }
    
    protected HttpResponse postRequest(String serviceURL, JsonRpcRequest rpcRequest) throws IOException {
        return postRequest(baseURL, serviceURL, rpcRequest);
    }
    protected HttpResponse postRequest(String serverURL, String serviceURL, JsonRpcRequest rpcRequest) throws IOException {
       
        baseURL = serverURL;
        
        //JPM.TODO : create some of the following classes only the first time
        // -> transport, factory, JacksonFactory, JsonObjectParser
        HttpTransport transport = new ApacheHttpTransport();
        HttpRequestFactory factory = transport.createRequestFactory();


        JsonHttpContent content = new JsonHttpContent(new JacksonFactory(), rpcRequest.getParameters());
        HttpRequest httpRequest = factory.buildPostRequest(new GenericUrl(serverURL + serviceURL), content);

        httpRequest.setConnectTimeout(0);
        httpRequest.setReadTimeout(0);
        
        JsonObjectParser parser = new JsonObjectParser(new GsonFactory());
        httpRequest.setParser(parser);
        

        //System.out.println(content.getData().toString());
        HttpResponse response = httpRequest.execute();
        //System.out.println(response.parseAsString());

        
        return response;
    }
    

    /**
     * Method called after the service has been done
     *
     * @param success boolean indicating if the fetch has succeeded
     */
    public void callback(final boolean success) {
        if (callback == null) {
            return;
        }

        callback.setTaskInfo(taskInfo);
        callback.setErrorMessage(errorMessage);
        
        if (callback.mustBeCalledInAWT()) {
            // Callback must be executed in the Graphical thread (AWT)
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    callback.run(success);
                }
            });
        } else {
            // Method called in the current thread
            // In this case, we assume the execution is fast.
            callback.run(success);
        }


    }
    
    protected String getIdString() {
        return "?request_id="+id;
    }
    
}
