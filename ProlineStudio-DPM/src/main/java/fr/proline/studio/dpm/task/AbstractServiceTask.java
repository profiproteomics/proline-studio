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
    protected AbstractServiceCallback m_callback;
    
    
    protected int m_id;
    protected boolean m_synchronous;
    protected String m_errorMessage = null;
    
    protected static int m_idIncrement = 0;
    
    protected static String m_baseURL = "";
    
    protected static final Logger m_loggerProline = LoggerFactory.getLogger("ProlineStudio.DPM.Task");
    protected static final Logger m_loggerWebcore = LoggerFactory.getLogger("ProlineWebCore");
    
    public static final String TASK_LIST_INFO = "Services";
    
    public AbstractServiceTask(AbstractServiceCallback callback, boolean synchronous, TaskInfo taskInfo) {
        super(taskInfo);
        
        m_callback = callback;
        m_synchronous = synchronous;
        
        m_id = m_idIncrement++;
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
        return m_synchronous;
    }
    
    protected HttpResponse postRequest(String serviceURL, JsonRpcRequest rpcRequest) throws IOException {
        return postRequest(m_baseURL, serviceURL, rpcRequest);
    }
    protected HttpResponse postRequest(String serverURL, String serviceURL, JsonRpcRequest rpcRequest) throws IOException {
       
        m_baseURL = serverURL;
        
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
        if (m_callback == null) {
            
            getTaskInfo().setFinished(success, m_errorMessage, false);
            
            return;
        }

        m_callback.setTaskInfo(m_taskInfo);
        m_callback.setErrorMessage(m_errorMessage);
        
        if (m_callback.mustBeCalledInAWT()) {
            // Callback must be executed in the Graphical thread (AWT)
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    m_callback.run(success);
                    getTaskInfo().setFinished(success, m_errorMessage, false);
                }
            });
        } else {
            // Method called in the current thread
            // In this case, we assume the execution is fast.
            m_callback.run(success);
            getTaskInfo().setFinished(success, m_errorMessage, false);
        }


    }
    
    protected String getIdString() {
        return "?request_id="+m_id;
    }
    
}
