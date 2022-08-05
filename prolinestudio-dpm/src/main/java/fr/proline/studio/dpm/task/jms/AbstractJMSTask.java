/* 
 * Copyright (C) 2019
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1 
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License 
 * along with this program; If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */
package fr.proline.studio.dpm.task.jms;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Message;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Notification;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import fr.proline.core.orm.uds.Project;
import fr.proline.core.orm.uds.UserAccount;
import fr.proline.studio.WindowManager;
import fr.proline.studio.dam.DatabaseDataManager;
import fr.proline.studio.dam.taskinfo.AbstractLongTask;
import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dock.gui.InfoLabel;
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

import static fr.proline.studio.dpm.task.util.JMSConnectionManager.JMS_EXPIRED_MSG_ERROR_CODE;
import static fr.proline.studio.dpm.task.util.JMSConnectionManager.PROLINE_SERVICE_NAME_KEY;

/**
 *
 * @author JM235353
 */
public abstract class AbstractJMSTask extends AbstractLongTask implements MessageListener {

    public enum JMSState {
        STATE_FAILED,
        STATE_WAITING,
        STATE_DONE
    }

    // callback is called by the AccessServiceThread when the service is done
    protected AbstractJMSCallback m_callback;

    protected MessageProducer m_producer = null;
    protected MessageConsumer m_responseConsumer = null;
    protected TemporaryQueue m_replyQueue = null;

    protected JMSState m_currentState = null;

    protected TaskError m_taskError = null;

    protected boolean m_synchronous;
    private int responseTimeout = 10000;

    protected static final Logger m_loggerProline = LoggerFactory.getLogger("ProlineStudio.DPM.Task");

    public static final String TASK_LIST_INFO = "Services JMS";

    protected Session m_session;

    /* To count received messages */
    public final AtomicInteger MESSAGE_COUNT_SEQUENCE = new AtomicInteger(0);

    public AbstractJMSTask(AbstractJMSCallback callback, TaskInfo taskInfo) {
        super(taskInfo);

        m_callback = callback;
        m_synchronous = false;
    }

    public AbstractJMSTask(AbstractJMSCallback callback, boolean synchronous, TaskInfo taskInfo) {
        super(taskInfo);
        m_taskInfo = taskInfo;
        m_callback = callback;
        m_synchronous = synchronous;

    }

    /**
     * Specify the timeout value (in milliseconds) to wait for message. This is
     * only used for synchronuous tasks. Default value is 10000 ms.
     *
     * @param timeout
     */
    protected void setResponseTimeout(int timeout) {
        responseTimeout = timeout;
    }

    /**
     * Method called by the AccessJMSManagerThread to ask for the service to be
     * done
     *
     * @throws javax.jms.JMSException
     */
    public void askJMS() throws JMSException {
        try {

            /*
             * Thread specific : Session, Producer, Consumer ...
             */
            // Get JMS Session (Session MUST be confined in current Thread)
            m_session = AccessJMSManagerThread.getAccessJMSManagerThread().getSession();

            // Step 6. Create a JMS Message Producer (Producer MUST be confined in current Thread)
            m_producer = m_session.createProducer(JMSConnectionManager.getJMSConnectionManager().getServiceQueue());

            m_replyQueue = m_session.createTemporaryQueue();
            m_responseConsumer = m_session.createConsumer(m_replyQueue);
            if (!m_synchronous) {
                m_responseConsumer.setMessageListener(this);
            }

            m_currentState = JMSState.STATE_WAITING;
            taskRun();
            if (m_synchronous) {
                Message responseMsg = m_responseConsumer.receive(responseTimeout);
                onMessage(responseMsg);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
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

    public void setTaskInfoRequest(String content) throws JMSException {
        m_taskInfo.setRequestURL(m_producer.getDestination().toString());
        m_taskInfo.setRequestContent(content);
    }

    protected void  addSupplementaryInfo(Message message, Long projectId )throws JMSException {
        addSourceToMessage(message);
        addDescriptionToMessage(message);
        addProjectIdToMessage(message, projectId);
    }

    protected void  addSupplementaryInfo(Message message)throws JMSException {
        addSourceToMessage(message);
        addDescriptionToMessage(message);
        addProjectIdToMessage(message, null);
    }

    protected void addSourceToMessage(Message message) throws JMSException {

        StringBuilder userLoginSB = new StringBuilder();
        UserAccount user = DatabaseDataManager.getDatabaseDataManager().getLoggedUser();
        if (user != null) {
            userLoginSB.append(user.getLogin());
        } else {
            userLoginSB.append("Unknown user");
        }
        String hostIP;
        try {
            hostIP = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException uhe) {
            hostIP = "Unknown";
        }
        userLoginSB.append(" (host  ").append(hostIP).append(")");
        message.setStringProperty(JMSConnectionManager.PROLINE_SERVICE_SOURCE_KEY, userLoginSB.toString());

    }

    protected void addDescriptionToMessage(Message message) throws JMSException {
        message.setStringProperty(JMSConnectionManager.PROLINE_SERVICE_DESCR_KEY, m_taskInfo.getTaskDescription());
    }

    protected void addProjectIdToMessage(Message message, Long projectId) throws JMSException {
        String pId = null;
        if(projectId == null) {
            Project p = DatabaseDataManager.getDatabaseDataManager().getCurrentProject();
            if(p!=null)
                projectId = p.getId();
        }

        if(projectId != null)
            pId = String.valueOf(projectId);

        message.setStringProperty(JMSConnectionManager.PROLINE_SERVICE_PROJECT_ID_KEY, pId);
    }

    /**
     * Called when the task is done and reply is receive
     * Default implementation will
     *  - verify message is a JSONRPC2Response (throw exception if not),
     *  - check eventual JSONRPC2Error (throw exception if exist)
     * and call processWithResult(JSONRPC2Response jsonResponse)
     * and set current state to done  (??)
     * If other treatment must be done before processWithResult, overwrite this method.
     *
     * @param jmsMessage reply message from JMS server
     * @throws Exception
     */
    public void taskDone(final Message jmsMessage) throws Exception {
        final TextMessage textMessage = (TextMessage) jmsMessage;
        final String jsonString = textMessage.getText();

        final JSONRPC2Message jsonMessage = JSONRPC2Message.parse(jsonString);
        if (jsonMessage instanceof JSONRPC2Notification) {
            m_loggerProline.warn("JSON Notification method: " + ((JSONRPC2Notification) jsonMessage).getMethod() + " instead of JSON Response");
            throw new Exception("Invalid JSONRPC2Message type");

        } else if (jsonMessage instanceof JSONRPC2Response) {

            final JSONRPC2Response jsonResponse = JSONRPC2Response.parse(jsonString, false, false, true);
            m_loggerProline.trace("JSON Response Id: " + jsonResponse.getID());

            JSONRPC2Error jsonError = jsonResponse.getError();
            if (jsonError != null) {
                int jsonErrCode = jsonError.getCode();
                if (m_synchronous && jsonErrCode == JMS_EXPIRED_MSG_ERROR_CODE && m_taskInfo.getDuration() <= responseTimeout) {
                    jsonError = jsonError.appendMessage("\n Your clock should not be synchronized with Proline Server's one !! ");
                }
                String serviceErr = (String) jsonResponse.getNonStdAttribute(PROLINE_SERVICE_NAME_KEY);
                m_loggerProline.error("JSON Error code {}, on serive {},  message : \"{}\"", jsonError.getCode(), serviceErr, jsonError.getMessage());
                throw jsonError;
            }

            processWithResult(jsonResponse);
        }
    }

    public abstract void processWithResult(JSONRPC2Response jsonResponse) throws Exception;

    /**
     * Method called by the ServiceStatusThread to check if the service is done
     *
     * @return current JMS Task State (one of AbstractJMSTask.JMSState)
     */
    public AbstractJMSTask.JMSState getJMSState() {
        return m_currentState;
    }

    /**
     * task listen received message, finished
     *
     * @param jmsMessage :received Message
     */
    @Override
    public final void onMessage(final Message jmsMessage) {

        long endRun = System.currentTimeMillis();
        this.m_taskInfo.setDuration(endRun - m_taskInfo.getStartTimestamp());

        if (jmsMessage != null) {
            m_loggerProline.info("Receiving message n° " + MESSAGE_COUNT_SEQUENCE.incrementAndGet() + " : " + JMSMessageUtil.formatMessage(jmsMessage));

            try {
                taskDone(jmsMessage);
                if(m_taskError == null)
                    m_currentState = JMSState.STATE_DONE;
            } catch (JSONRPC2Error jsonErr) {

//                jsonErr.printStackTrace();
                m_currentState = JMSState.STATE_FAILED;
                m_loggerProline.error("Error handling JMS Message", jsonErr);
                if (jsonErr.getCode() == JMSConnectionManager.JMS_CANCELLED_TASK_ERROR_CODE) {
                    m_taskInfo.setAborted();
                }
                m_taskError = new TaskError(jsonErr);
            } catch (Exception e) {

//                e.printStackTrace();
                m_currentState = JMSState.STATE_FAILED;
                m_loggerProline.error("Error handling JMS Message", e);
                m_taskError = new TaskError(e);
            }
        } else {
            String msg = "Error receiving message n° " + MESSAGE_COUNT_SEQUENCE.incrementAndGet() + ": timeout should have occured ";
            m_loggerProline.info(msg);
            m_currentState = JMSState.STATE_FAILED;
            m_taskError = new TaskError(new RuntimeException(msg));
        }


        try {
            if (jmsMessage != null) {
                jmsMessage.acknowledge();
            }
        } catch (JMSException ex) {
            m_loggerProline.error("Error running JMS Message acknowledge", ex);
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
                    showError(m_taskError);
                    m_callback.run(success);

                    getTaskInfo().setFinished(success, m_taskError, true);
                }
            });
        } else {
            showError(m_taskError);
            // Method called in the current thread
            // In this case, we assume the execution is fast.
            m_callback.run(success);
            getTaskInfo().setFinished(success, m_taskError, true);
        }
    }

    protected void showError(TaskError taskErr) {
        if (taskErr != null) {
            WindowManager.getDefault().getMainWindow().alert(InfoLabel.INFO_LEVEL.ERROR, new Exception(taskErr.getErrorTitle() + "\n" + taskErr.getErrorText()));
        }
    }

}
