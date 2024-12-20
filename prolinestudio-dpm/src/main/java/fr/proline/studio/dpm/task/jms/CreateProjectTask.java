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

import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import fr.proline.core.orm.uds.Project;
import fr.proline.core.orm.uds.ProjectUserAccountMap;
import fr.proline.core.orm.util.DStoreCustomPoolConnectorFactory;
import fr.proline.studio.dam.data.ProjectIdentificationData;
import fr.proline.studio.dam.data.ProjectQuantitationData;
import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dpm.task.util.JMSConnectionManager;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import javax.persistence.EntityManager;
import java.util.HashMap;
import java.util.Set;

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

        final TextMessage message = m_session.createTextMessage(jsonRequest.toJSONString());

        /* ReplyTo = Temporary Destination Queue for Server -> Client response */
        message.setJMSReplyTo(m_replyQueue);
        message.setStringProperty(JMSConnectionManager.PROLINE_SERVICE_NAME_KEY, m_serviceName);
        //message.setStringProperty(JMSConnectionManager.PROLINE_SERVICE_VERSION_KEY, m_version);
        addSupplementaryInfo(message);

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
    public void processWithResult(JSONRPC2Response jsonResponse) throws Exception {

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
}
