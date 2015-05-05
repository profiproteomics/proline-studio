package fr.proline.studio.dpm.task.jms;

import com.google.api.client.util.ArrayMap;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Message;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Notification;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;

import java.util.HashMap;
import java.util.Map;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dpm.jms.AccessJMSManagerThread;
import fr.proline.studio.dpm.serverfilesystem.RootInfo;
import fr.proline.studio.dpm.serverfilesystem.ServerFile;
import static fr.proline.studio.dpm.task.jms.AbstractJMSTask.m_loggerProline;
import fr.proline.studio.dpm.task.util.JMSConnectionManager;
import java.math.BigDecimal;
import java.util.ArrayList;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

/**
 * Task to get the Roots path for the Virtual File System on the server
 * @author jm235353
 */
public class FileSystemRootsTask extends AbstractJMSTask {

    ArrayList<RootInfo> m_roots;
    
    public FileSystemRootsTask(AbstractJMSCallback callback, ArrayList<RootInfo> roots) {
        super(callback,  new TaskInfo("Get Server File System Root Paths", true, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));
        
        m_roots = roots;
    }
    
    @Override
    public void taskRun() throws JMSException {

        // create the request
        final JSONRPC2Request jsonRequest = new JSONRPC2Request("retrieve_all_mount_points", Integer.valueOf(m_id));
        Map<String, Object> params = new HashMap<>(); // no parameter
        jsonRequest.setNamedParams(params);

        final TextMessage message = AccessJMSManagerThread.getAccessJMSManagerThread().getSession().createTextMessage(jsonRequest.toJSONString());

        /* ReplyTo = Temporary Destination Queue for Server -> Client response */
        message.setJMSReplyTo(m_replyQueue);
        message.setStringProperty(JMSConnectionManager.PROLINE_SERVICE_NAME_KEY, "proline/misc/FileSystem");

        setTaskInfoRequest(message.getText());

        // Send the Message
        m_producer.send(message);
        m_loggerProline.info("Message [{}] sent", message.getJMSMessageID());
    }


    @Override
    public void taskDone(final Message jmsMessage) throws Exception {
        
        final TextMessage textMessage = (TextMessage) jmsMessage;
        final String jsonString = textMessage.getText();

        final JSONRPC2Message jsonMessage = JSONRPC2Message.parse(jsonString);
        if(jsonMessage instanceof JSONRPC2Notification) {
            m_loggerProline.warn("JSON Notification method: " + ((JSONRPC2Notification) jsonMessage).getMethod()+" instead of JSON Response");
            throw new Exception("Invalid JSONRPC2Message type");
        } else if (jsonMessage instanceof JSONRPC2Response)  {
            
            final JSONRPC2Response jsonResponse = (JSONRPC2Response) jsonMessage;
	    m_loggerProline.debug("JSON Response Id: " + jsonResponse.getID());

	    final JSONRPC2Error jsonError = jsonResponse.getError();

	    if (jsonError != null) {
		m_loggerProline.error("JSON Error code {}, message : \"{}\"", jsonError.getCode(), jsonError.getMessage());
		m_loggerProline.error("JSON Throwable", jsonError);
                throw jsonError;
	    }

	    final Object result = jsonResponse.getResult();

	    if (result == null || ! ArrayList.class.isInstance(result) ) {
                String msg = "Server has returned no Root Path. There is a problem with the server installation, please contact your administrator.";
		m_loggerProline.debug(msg);
                throw new Exception("null or invalid result : "+msg);
	    } else {
                ArrayList resultList = (ArrayList) result;
		for (int i = 0; i < resultList.size(); i++) {
                    Map fileMap = (Map) resultList.get(i);
                    String label = (String) fileMap.get("label");
                    String directoryType = (String) fileMap.get("directory_type");  // use it !
                    m_roots.add(new RootInfo(label, directoryType));
                                       
                }
	    }
        }
        
        // always returns STATE_DONE because to get roots path from the server
        // is a synchronous service
        m_currentState = JMSState.STATE_DONE;
    }
    
    
    
}
