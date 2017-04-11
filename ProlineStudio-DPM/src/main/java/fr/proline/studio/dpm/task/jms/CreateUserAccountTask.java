package fr.proline.studio.dpm.task.jms;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Message;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Notification;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import fr.profi.util.security.SecurityUtils;
import fr.proline.core.orm.uds.UserAccount;
import fr.proline.core.orm.util.DataStoreConnectorFactory;
import fr.proline.studio.dam.DatabaseDataManager;
import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dpm.jms.AccessJMSManagerThread;
import static fr.proline.studio.dpm.task.jms.AbstractJMSTask.TASK_LIST_INFO;
import static fr.proline.studio.dpm.task.jms.AbstractJMSTask.m_loggerProline;
import fr.proline.studio.dpm.task.util.JMSConnectionManager;
import java.util.HashMap;
import java.util.List;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

/**
 *
 * @author JM235353
 */
public class CreateUserAccountTask  extends AbstractJMSTask {
    private static final String m_serviceName = "proline/admin/UserAccount";
    private static final String m_methodName = "create";
    //private static final String m_version = "2.0";
    
    private String m_userName;
    private String m_password;
    private boolean m_isAdmin;
    
    public CreateUserAccountTask(AbstractJMSCallback callback,  String userName, String newPassword, boolean isAdmin) {
        super(callback,  new TaskInfo("Create user account for " + userName, false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_HIGH));
        m_userName = userName;
        m_password = newPassword;   
        m_isAdmin = isAdmin;
    }
    
    @Override
    public void taskRun() throws JMSException {
        final JSONRPC2Request jsonRequest = new JSONRPC2Request(m_methodName, Integer.valueOf(m_taskInfo.getId()));
        jsonRequest.setNamedParams(createParams());
           
        final TextMessage message = AccessJMSManagerThread.getAccessJMSManagerThread().getSession().createTextMessage(jsonRequest.toJSONString());

        /* ReplyTo = Temporary Destination Queue for Server -> Client response */
        message.setJMSReplyTo(m_replyQueue);
        message.setStringProperty(JMSConnectionManager.PROLINE_SERVICE_NAME_KEY, m_serviceName);
        //message.setStringProperty(JMSConnectionManager.PROLINE_SERVICE_VERSION_KEY, m_version);
        addSourceToMessage(message);
        addDescriptionToMessage(message);
        
        setTaskInfoRequest(message.getText());
	
        //  Send the Message
        m_producer.send(message);
        m_loggerProline.info("Message [{}] sent", message.getJMSMessageID());
        m_taskInfo.setJmsMessageID(message.getJMSMessageID());
                    
    }
    
    private HashMap<String, Object> createParams() {
        HashMap<String, Object> params = new HashMap<>();
        //-- Global PARAMS
        params.put("login", m_userName);
        params.put("password_hash", SecurityUtils.sha256Hex(m_password));
        params.put("is_user_group", Boolean.valueOf(!m_isAdmin));
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
             
            final Object rs = jsonResponse.getResult();
            if (rs == null || ! Long.class.isInstance(rs) ) {
		m_loggerProline.debug("Invalid result: No id returned");
                throw new Exception("Invalid result "+rs);
	    } else {
		m_loggerProline.debug("Result :\n" + rs); 
	    }
            Long userId = (Long) rs;
            
            if (userId == null) {
                m_taskError = new TaskError("Create user account failed");
                return ;
            }
            
            
            // Reload users
            EntityManager entityManagerUDS = DataStoreConnectorFactory.getInstance().getUdsDbConnector().createEntityManager();

            // Load all users
            try {
                entityManagerUDS.getTransaction().begin();

                TypedQuery<UserAccount> userQuery = entityManagerUDS.createQuery("SELECT user FROM fr.proline.core.orm.uds.UserAccount user ORDER BY user.login ASC", UserAccount.class);
                List<UserAccount> userList = userQuery.getResultList();
                DatabaseDataManager.getDatabaseDataManager().setProjectUsers(userList);

                entityManagerUDS.getTransaction().commit();

            } catch (Exception e) {
                m_taskError = new TaskError("Unable to load UserAccounts from UDS");
                entityManagerUDS.getTransaction().rollback();
                DataStoreConnectorFactory.getInstance().closeAll();
                return;
            }
            
        }
          m_currentState = JMSState.STATE_DONE;
        
    }
}
