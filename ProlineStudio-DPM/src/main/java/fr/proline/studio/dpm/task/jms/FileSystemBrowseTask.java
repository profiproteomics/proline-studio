package fr.proline.studio.dpm.task.jms;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Message;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Notification;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;

import java.util.HashMap;
import java.util.Map;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dpm.jms.AccessJMSManagerThread;
import fr.proline.studio.dpm.serverfilesystem.ServerFile;
import static fr.proline.studio.dpm.task.jms.AbstractJMSTask.m_loggerProline;
import fr.proline.studio.dpm.task.util.JMSConnectionManager;
import java.util.ArrayList;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

/**
 * Task to create a new Project in the UDS db
 * @author jm235353
 */
public class FileSystemBrowseTask extends AbstractJMSTask {

    private String m_dirPath;
    private ArrayList<ServerFile> m_files;
    
    public FileSystemBrowseTask(AbstractJMSCallback callback, String dirPath, ArrayList<ServerFile> files) {
        super(callback,  new TaskInfo("Browse Server File System "+dirPath, true, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_LOW));
        
        m_dirPath = dirPath;
        m_files = files;
    }
    
   @Override
    public void taskRun() throws JMSException {

        // create the request
        final JSONRPC2Request jsonRequest = new JSONRPC2Request("retrieve_directory_content", Integer.valueOf(m_taskInfo.getId()));
        jsonRequest.setNamedParams(createParams());

        final TextMessage message = AccessJMSManagerThread.getAccessJMSManagerThread().getSession().createTextMessage(jsonRequest.toJSONString());

        /* ReplyTo = Temporary Destination Queue for Server -> Client response */
        message.setJMSReplyTo(m_replyQueue);
        message.setStringProperty(JMSConnectionManager.PROLINE_SERVICE_NAME_KEY, "proline/misc/FileSystem");
        addSourceToMessage(message);   
        addDescriptionToMessage(message);
        
        setTaskInfoRequest(message.getText());
         
        
        // Send the Message
        m_producer.send(message);
        m_loggerProline.info("Message [{}] sent", message.getJMSMessageID());
        m_taskInfo.setJmsMessageID(message.getJMSMessageID());
    }

    private HashMap<String, Object> createParams() {
        HashMap<String, Object> params = new HashMap<>();
        params.put("label_path", m_dirPath);
        params.put("include_files", Boolean.TRUE);
        params.put("include_dirs", Boolean.TRUE);
        return params;
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
		m_loggerProline.debug("Invalid or no result");
                throw new Exception("null or invalid result "+result);
	    } else {
                ArrayList resultList = (ArrayList) result;
		for (int i = 0; i < resultList.size(); i++) {
                    Map fileMap = (Map) resultList.get(i);

                    ServerFile f;
                    boolean isDir = (Boolean) fileMap.get("is_dir");
                    if (isDir) {
                        f = new ServerFile((String) fileMap.get("path"), (String) fileMap.get("name"), true, 0, 0);
                    } else {
                        Long size = (fileMap.containsKey("size") ?(Long) fileMap.get("size") : 0 );
                        f = new ServerFile((String) fileMap.get("path"), (String) fileMap.get("name"), false, (Long) fileMap.get("lastmodified"), size);
                    }
                    m_files.add(f);
                }
	    }
        }
        
        // always returns STATE_DONE because to browse files on server
        // is a synchronous service
        m_currentState = JMSState.STATE_DONE;
    }
    
    
    
}
