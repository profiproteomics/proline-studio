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
import fr.proline.core.orm.uds.UserAccount;
import fr.proline.core.orm.util.DStoreCustomPoolConnectorFactory;
import fr.proline.studio.dam.DatabaseDataManager;
import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dpm.task.util.JMSConnectionManager;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author JM235353
 */
public class ResetPasswordTask extends AbstractJMSTask {
    private static final String m_serviceName = "proline/admin/UserAccount";
    private static final String m_methodName = "reset_password";
    //private static final String m_version = "2.0";

    private final Long m_userId;
    private final String m_password;
    
    public ResetPasswordTask(AbstractJMSCallback callback,  String userName, Long userId, String password) {
        super(callback,  new TaskInfo("Change password for " + userName, false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_HIGH));

        m_userId = userId;
        m_password = password;
    }
    
    @Override
    public void taskRun() throws JMSException {
        final JSONRPC2Request jsonRequest = new JSONRPC2Request(m_methodName, Integer.valueOf(m_taskInfo.getId()));
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
        //-- Global PARAMS
        params.put("login_id", m_userId);
        params.put("new_password_hash", m_password);
        return params;
    }

    @Override
    public void processWithResult(JSONRPC2Response jsonResponse) throws Exception {

        final Object rs = jsonResponse.getResult();
        if (rs == null || !Boolean.class.isInstance(rs)) {
            m_loggerProline.debug("Invalid result: Boolean expected");
            throw new Exception("Invalid result " + rs);
        } else {
            m_loggerProline.debug("Result :\n" + rs);
        }
        Boolean rsBoolean = (Boolean) rs;

        if ( !rsBoolean) {
            m_taskError = new TaskError("Change user group failed");
            return;
        }

        // Reload users
        EntityManager entityManagerUDS = DStoreCustomPoolConnectorFactory.getInstance().getUdsDbConnector().createEntityManager();

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
            DStoreCustomPoolConnectorFactory.getInstance().closeAll();
            return;
        }

    }
}
