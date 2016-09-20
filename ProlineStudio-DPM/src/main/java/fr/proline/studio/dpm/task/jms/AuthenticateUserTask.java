package fr.proline.studio.dpm.task.jms;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Message;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Notification;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import fr.profi.util.security.EncryptionManager;
import fr.profi.util.security.SecurityUtils;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dpm.jms.AccessJMSManagerThread;
import static fr.proline.studio.dpm.task.jms.AbstractJMSTask.m_loggerProline;
import fr.proline.studio.dpm.task.util.JMSConnectionManager;
import java.util.HashMap;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

/**
 * Task for User authentication
 *
 * @author VD225637
 */
public class AuthenticateUserTask extends AbstractJMSTask {

    private String m_userName;
    private String m_password;
    private String[] m_databasePassword;
    private static int TASK_TIMEOUT_MS = 20000;
            
    public AuthenticateUserTask(AbstractJMSCallback callback, String m_userName, String m_password, String[] m_databasePassword) {
        super(callback, true,new TaskInfo("Check User " + m_userName, false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_HIGH));
        this.m_userName = m_userName;
        this.m_password = m_password;
        this.m_databasePassword = m_databasePassword;
        super.setResponseTimeout(TASK_TIMEOUT_MS);
    }
    
    @Override
    public void taskRun() throws JMSException {

        final JSONRPC2Request jsonRequest = new JSONRPC2Request(JMSConnectionManager.PROLINE_USER_AUTHENTICATE_METHOD_NAME, Integer.valueOf(m_taskInfo.getId()));
        jsonRequest.setNamedParams(createParams());

        final TextMessage message = AccessJMSManagerThread.getAccessJMSManagerThread().getSession().createTextMessage(jsonRequest.toJSONString());

        /* ReplyTo = Temporary Destination Queue for Server -> Client response */
        message.setJMSReplyTo(m_replyQueue);
        message.setStringProperty(JMSConnectionManager.PROLINE_SERVICE_NAME_KEY, "proline/admin/UserAccount");
        addSourceToMessage(message);     
        addDescriptionToMessage(message);
        
        setTaskInfoRequest(message.getText());
        //  Send the Message
        m_producer.send(message,Message.DEFAULT_DELIVERY_MODE,8,TASK_TIMEOUT_MS+5000);
        m_loggerProline.debug("Message AuthenticateUserTask [{}] sent", message.getJMSMessageID());
        m_taskInfo.setJmsMessageID(message.getJMSMessageID());
    }

    private HashMap<String, Object> createParams() {
        HashMap<String, Object> params = new HashMap<>();
        params.put("login", m_userName);
        params.put("password_hash", SecurityUtils.sha256Hex(m_password));
        params.put("return_db_password", Boolean.TRUE);
        EncryptionManager encryptionMgr = EncryptionManager.getEncryptionManager();
        params.put("public_key", encryptionMgr.getPublicKeyAsString());
        
        return params;
    }

    @Override
    public void taskDone(Message jmsMessage) throws Exception {
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
            if (result == null  ) {
		m_loggerProline.debug("Internal Error : No DB password received");
                throw new Exception("Internal Error : No DB password received ");
	    } else {
                String dbPwd = (String) result.toString();
                m_databasePassword[0] = EncryptionManager.getEncryptionManager().decrypt(dbPwd);
		m_loggerProline.debug("Result :\n" +  m_databasePassword[0]);                
	    }
        }        
        m_currentState = JMSState.STATE_DONE;

    }

}
