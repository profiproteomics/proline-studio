/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.dpm.task.jms;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Message;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Notification;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import fr.proline.core.orm.uds.Project;
import fr.proline.core.orm.uds.ProjectUserAccountMap;
import fr.proline.core.orm.util.DStoreCustomPoolConnectorFactory;
import fr.proline.studio.dam.data.ProjectIdentificationData;
import fr.proline.studio.dam.data.ProjectQuantitationData;
import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dpm.AccessJMSManagerThread;
import static fr.proline.studio.dpm.task.jms.AbstractJMSTask.TASK_LIST_INFO;
import static fr.proline.studio.dpm.task.jms.AbstractJMSTask.m_loggerProline;
import fr.proline.studio.dpm.task.util.JMSConnectionManager;
import java.util.HashMap;
import java.util.Set;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.persistence.EntityManager;

/**
 * Create Project Task through JMS
 *
 * @author MB243701
 */
public class CreateProjectTask extends AbstractJMSTask {

    private static final String m_serviceName = "proline/admin/CreateProject";
    //private static final String m_version = "2.0";

    private final String m_name;
    private final String m_description;
    private final long m_ownerId;
    private final ProjectIdentificationData m_projectIdentificationData;
    private final ProjectQuantitationData m_projectQuantificationData;

    public CreateProjectTask(AbstractJMSCallback callback, String name, String description, long ownerId, ProjectIdentificationData projectIdentificationData, ProjectQuantitationData projectQuantificationData) {
        super(callback, new TaskInfo("Add Project named " + name, false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_HIGH));
        m_name = name;
        m_description = description;
        m_ownerId = ownerId;
        m_projectIdentificationData = projectIdentificationData;
        m_projectQuantificationData = projectQuantificationData;

    }

    @Override
    public void taskRun() throws JMSException {
        final JSONRPC2Request jsonRequest = new JSONRPC2Request(JMSConnectionManager.PROLINE_PROCESS_METHOD_NAME, Integer.valueOf(m_taskInfo.getId()));
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
        params.put("name", m_name);
        params.put("description", m_description);
        params.put("owner_id", m_ownerId);
        return params;
    }

    @Override
    public void taskDone(final Message jmsMessage) throws Exception {

        final TextMessage textMessage = (TextMessage) jmsMessage;
        final String jsonString = textMessage.getText();

        final JSONRPC2Message jsonMessage = JSONRPC2Message.parse(jsonString);
        if (jsonMessage instanceof JSONRPC2Notification) {
            m_loggerProline.warn("JSON Notification method: " + ((JSONRPC2Notification) jsonMessage).getMethod() + " instead of JSON Response");
            throw new Exception("Invalid JSONRPC2Message type");

        } else if (jsonMessage instanceof JSONRPC2Response) {

            final JSONRPC2Response jsonResponse = (JSONRPC2Response) jsonMessage;
            m_loggerProline.debug("JSON Response Id: " + jsonResponse.getID());

            final JSONRPC2Error jsonError = jsonResponse.getError();

            if (jsonError != null) {
                m_loggerProline.error("JSON Error code {}, message : \"{}\"", jsonError.getCode(), jsonError.getMessage());
                m_loggerProline.error("JSON Throwable", jsonError);
                throw jsonError;
            }

            final Object result = jsonResponse.getResult();
            if (result == null || !Long.class.isInstance(result)) {
                m_loggerProline.debug("Invalid result: No projectId returned");
                throw new Exception("Invalid result " + result);
            } else {
                m_loggerProline.debug("Result :\n" + result);
            }
            Long projectId = (Long) result;
            EntityManager entityManagerUDS = DStoreCustomPoolConnectorFactory.getInstance().getUdsDbConnector().createEntityManager();
            try {
                entityManagerUDS.getTransaction().begin();

                Project p = entityManagerUDS.find(Project.class, Long.valueOf(projectId.longValue()));

                if (p == null) {
                    m_taskError = new TaskError("Internal Error : Project not Found");
                    return;
                }

                // avoid lazy initialization problem
                Set<ProjectUserAccountMap> members = p.getProjectUserAccountMap();
                for (ProjectUserAccountMap projectUserAccount : members) {
                    projectUserAccount.getUserAccount();
                }

                m_projectIdentificationData.setProject(p);
                m_projectQuantificationData.setProject(p);

                entityManagerUDS.getTransaction().commit();

            } catch (Exception e) {
                m_loggerProline.error(getClass().getSimpleName() + " failed", e);
                try {
                    entityManagerUDS.getTransaction().rollback();
                } catch (Exception rollbackException) {
                    m_loggerProline.error(getClass().getSimpleName() + " failed : potential network problem", rollbackException);
                }
            } finally {
                entityManagerUDS.close();
            }

        }
        m_currentState = JMSState.STATE_DONE;

    }
}
