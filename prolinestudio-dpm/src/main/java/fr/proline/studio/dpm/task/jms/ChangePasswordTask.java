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
import fr.profi.util.security.SecurityUtils;
import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dpm.task.util.JMSConnectionManager;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import java.util.HashMap;

/**
 * change password task through JMS
 * @author MB243701
 */
public class ChangePasswordTask  extends AbstractJMSTask {
    private static final String m_serviceName = "proline/admin/UserAccount";
    private static final String m_methodName = "change_password";
    //private static final String m_version = "2.0";
    
    private final String m_userName;
    private final String m_password;
    private final String m_oldPassword;
    
    public ChangePasswordTask(AbstractJMSCallback callback,  String userName, String oldPassword, String newPassword) {
        super(callback,  new TaskInfo("Change password for " + userName, false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_HIGH));
        m_userName = userName;
        m_password = newPassword;
        m_oldPassword = oldPassword;       
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
        params.put("login", m_userName);
        params.put("old_password_hash", SecurityUtils.sha256Hex(m_oldPassword));
        params.put("new_password_hash", SecurityUtils.sha256Hex(m_password));
        return params;
    }

    @Override
    public void processWithResult(JSONRPC2Response jsonResponse) throws Exception {

        final Object rs = jsonResponse.getResult();
        if (rs == null || !Boolean.class.isInstance(rs)) {
            m_loggerProline.debug("Invalid result: No status returned");
            throw new Exception("Invalid result " + rs);
        } else {
            m_loggerProline.debug("Result :\n" + rs);
        }
        Boolean status = (Boolean) rs;

        if (!status) {
            m_taskError = new TaskError("Change password failed");
            return;
        }
    }

}
