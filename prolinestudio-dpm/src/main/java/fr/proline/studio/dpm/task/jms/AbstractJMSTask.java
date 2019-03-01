package fr.proline.studio.dpm.task.jms;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import fr.proline.core.orm.uds.UserAccount;
import fr.proline.studio.dam.DatabaseDataManager;
import fr.proline.studio.dam.taskinfo.AbstractLongTask;
import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dpm.AccessJMSManagerThread;
import fr.proline.studio.dpm.task.util.JMSConnectionManager;
import fr.proline.studio.dpm.task.util.JMSMessageUtil;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicInteger;
import javax.jms.*;
import javax.swing.SwingUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author JM235353
 */
public abstract class AbstractJMSTask  extends AbstractLongTask implements MessageListener {
    
    public enum JMSState {
        STATE_FAILED,
        STATE_WAITING,
        STATE_DONE
    };
    
    // callback is called by the AccessServiceThread when the service is done
    protected AbstractJMSCallback m_callback;
    
    protected MessageProducer m_producer = null;
    protected MessageConsumer m_responseConsumer = null;
    protected TemporaryQueue m_replyQueue = null;
        
    protected JMSState m_currentState = null;
    
    protected TaskError m_taskError = null;
    
    protected boolean m_synchronous;
    private int responseTimeout = 10000;
        
    
    protected static int m_idIncrement = 0;

    protected static final Logger m_loggerProline = LoggerFactory.getLogger("ProlineStudio.DPM.Task");
    
    public static final String TASK_LIST_INFO = "Services JMS";
    

    /* To count received messages */
    public final AtomicInteger MESSAGE_COUNT_SEQUENCE = new AtomicInteger(0);

    public AbstractJMSTask(AbstractJMSCallback callback, TaskInfo taskInfo) {
        super(taskInfo);
        
        m_callback = callback;
        m_synchronous = false;
    }
       
    public AbstractJMSTask(AbstractJMSCallback callback,  boolean synchronous, TaskInfo taskInfo) {
        super(taskInfo);
        
        m_callback = callback;
        m_synchronous = synchronous;
    }
    
    /**
     * Specify the timeout value (in milliseconds) to wait for message. This is only used for synchronuous tasks.
     * Default value is 10000 ms.
     * @param timeout 
     */
    protected void setResponseTimeout(int timeout){
        responseTimeout = timeout;
    }
    
    /**
     * Method called by the AccessJMSManagerThread to ask for the service to be done     
     * @throws javax.jms.JMSException
     */
    public void askJMS() throws JMSException {
        try {

            /*
             * Thread specific : Session, Producer, Consumer ...
             */
            // Get JMS Session (Session MUST be confined in current Thread)            
            Session m_session  = AccessJMSManagerThread.getAccessJMSManagerThread().getSession();

            // Step 6. Create a JMS Message Producer (Producer MUST be confined in current Thread)
            m_producer = m_session.createProducer(JMSConnectionManager.getJMSConnectionManager().getServiceQueue());
            
            m_replyQueue = m_session.createTemporaryQueue();
            m_responseConsumer = m_session.createConsumer(m_replyQueue);
            if(!m_synchronous)
                m_responseConsumer.setMessageListener(this);

            m_currentState = JMSState.STATE_WAITING;
            taskRun();
            if(m_synchronous){
              Message responseMsg  =  m_responseConsumer.receive(responseTimeout);
              onMessage(responseMsg);
            }
        } catch (Exception ex) {
            m_loggerProline.error("Error sending JMS Message", ex);
            m_currentState = JMSState.STATE_FAILED;
            m_taskError = new TaskError(ex);
            callback(false);
        }
    }

    /**
     * Called when the task must be started. The implementation should call
     * setTaskInfoRequest to register request informations
     * 
     * @throws JMSException 
     */
    public abstract void taskRun() throws JMSException;
    
    public void setTaskInfoRequest(String content) throws JMSException{
        m_taskInfo.setRequestURL(m_producer.getDestination().toString());
        m_taskInfo.setRequestContent(content);
    }
    
    protected void addSourceToMessage(Message message) throws JMSException  {
        
        StringBuilder userLoginSB = new StringBuilder();
        UserAccount user = DatabaseDataManager.getDatabaseDataManager().getLoggedUser();
        if(user!=null)
            userLoginSB.append(user.getLogin());
        else
            userLoginSB.append("Unknonw user");
        String hostIP;
        try { 
            hostIP = InetAddress.getLocalHost().getHostAddress();
        } catch(UnknownHostException uhe){
           hostIP = "Unknown";
        }
        userLoginSB.append(" (host  ").append(hostIP).append(")");
        message.setStringProperty(JMSConnectionManager.PROLINE_SERVICE_SOURCE_KEY, userLoginSB.toString());

    }
    
    protected void addDescriptionToMessage(Message message) throws JMSException {    
        message.setStringProperty(JMSConnectionManager.PROLINE_SERVICE_DESCR_KEY, m_taskInfo.getTaskDescription());
    }
    
    /**
     * Called when the task is done
     * @param jmsMessage
     * @throws Exception 
     */
    public abstract void taskDone(final Message jmsMessage) throws Exception;
    

    /**
     * Method called by the ServiceStatusThread
     * to check if the service is done
     * @return current JMS Task State (one of AbstractJMSTask.JMSState)
     */
    public AbstractJMSTask.JMSState getJMSState() {
        return m_currentState;
    }

    
    @Override
    public final void onMessage(final Message jmsMessage) {       
        
        long endRun = System.currentTimeMillis();
//        this.m_taskInfo.setDuration(endRun-m_startRun);
        this.m_taskInfo.setDuration(endRun-m_taskInfo.getStartTimestamp());
        
        if(jmsMessage != null) {
            m_loggerProline.info("Receiving message n° " + MESSAGE_COUNT_SEQUENCE.incrementAndGet() + " : " + JMSMessageUtil.formatMessage(jmsMessage));
        
            try {                    
                taskDone(jmsMessage);
            } catch (JSONRPC2Error jsonErr) {
                m_currentState = JMSState.STATE_FAILED;
                m_loggerProline.error("Error handling JMS Message", jsonErr);
                if(jsonErr.getCode() == JMSConnectionManager.JMS_CANCELLED_TASK_ERROR_CODE){
                    m_taskInfo.setAborted();               
                }
                m_taskError = new TaskError(jsonErr);                    
            } catch (Exception e) {
                m_currentState = JMSState.STATE_FAILED;
                m_loggerProline.error("Error handling JMS Message", e);
                m_taskError = new TaskError(e);
            }
        } else {
            String msg = "Error receiving message n° " + MESSAGE_COUNT_SEQUENCE.incrementAndGet() + ": timeout should have occured " ;
            m_loggerProline.info(msg);
            m_currentState = JMSState.STATE_FAILED;
            m_taskError = new TaskError(new RuntimeException(msg));
        }
        
        if (m_currentState == JMSState.STATE_FAILED) {
            callback(false);
        } else if (m_currentState == JMSState.STATE_DONE) {
            callback(true);
        } else {
            // should never happen : state not set
             m_taskError = new TaskError("Task with State not set");
             m_currentState = JMSState.STATE_FAILED;
             callback(false);
        }
        
                        
        try {
            if(jmsMessage != null)
                jmsMessage.acknowledge();
        } catch (JMSException ex) {
            m_loggerProline.error("Error running JMS Message acknowledge", ex);
        }
        
    }
    

        
    /**
     * Method called after the service has been done
     *
     * @param success boolean indicating if the fetch has succeeded
     */
    protected void callback(final boolean success) {
        if (m_callback == null) {
            
            getTaskInfo().setFinished(success, m_taskError, true);
            
            return;
        }

        m_callback.setTaskInfo(m_taskInfo);
        m_callback.setTaskError(m_taskError);
        
        if (m_callback.mustBeCalledInAWT()) {
            // Callback must be executed in the Graphical thread (AWT)
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    m_callback.run(success);
                    getTaskInfo().setFinished(success, m_taskError, true);
                }
            });
        } else {
            // Method called in the current thread
            // In this case, we assume the execution is fast.
            m_callback.run(success);
            getTaskInfo().setFinished(success, m_taskError, true);
        }
    }
    
                 
}
