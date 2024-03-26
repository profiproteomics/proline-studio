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
import fr.profi.util.security.EncryptionManager;
import fr.profi.util.security.SecurityUtils;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dpm.task.util.JMSConnectionManager;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import java.util.HashMap;

/**
 * Task for User authentication
 *
 * @author VD225637
 */
public class AuthenticateUserTask extends AbstractJMSTask {

    private final String m_userName;
    private final String m_password;
    private final String[] m_databasePassword;
    private static final int TASK_TIMEOUT_MS = 20000;


    public AuthenticateUserTask(AbstractJMSCallback callback, String m_userName, String m_password, String[] m_databasePassword) {
        super(callback, true, new TaskInfo("Check User " + m_userName, false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_HIGH));
        this.m_userName = m_userName;
        this.m_password = m_password;
        this.m_databasePassword = m_databasePassword;
        super.setResponseTimeout(TASK_TIMEOUT_MS);

    }

    @Override
    public void taskRun() throws JMSException {

        final JSONRPC2Request jsonRequest = new JSONRPC2Request(JMSConnectionManager.PROLINE_USER_AUTHENTICATE_METHOD_NAME, Integer.valueOf(m_taskInfo.getId()));
        jsonRequest.setNamedParams(createParams());

        final TextMessage message = m_session.createTextMessage(jsonRequest.toJSONString());

        /* ReplyTo = Temporary Destination Queue for Server -> Client response */
        message.setJMSReplyTo(m_replyQueue);
        message.setStringProperty(JMSConnectionManager.PROLINE_SERVICE_NAME_KEY, "proline/admin/UserAccount");
        addSupplementaryInfo(message);

        setTaskInfoRequest(message.getText());
        //  Send the Message
        m_producer.send(message);
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
    public void processWithResult(JSONRPC2Response jsonResponse) throws Exception {
        final Object result = jsonResponse.getResult();
        if (result == null) {
            m_loggerProline.trace("Internal Error : No DB password received");
            throw new Exception("Internal Error : No DB password received ");
        } else {
            String dbPwd = result.toString();
            m_databasePassword[0] = EncryptionManager.getEncryptionManager().decrypt(dbPwd);
            m_loggerProline.trace("Result :\n" + m_databasePassword[0]);
        }
    }

}
