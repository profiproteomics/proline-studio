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
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import fr.profi.util.security.EncryptionManager;
import fr.profi.util.security.SecurityUtils;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dpm.AccessJMSManagerThread;
import static fr.proline.studio.dpm.task.util.JMSConnectionManager.JMS_EXPIRED_MSG_ERROR_CODE;
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

    public static int count = 0;
            
    public AuthenticateUserTask(AbstractJMSCallback callback, String m_userName, String m_password, String[] m_databasePassword) {
        super(callback, true,new TaskInfo("Check User " + m_userName, false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_HIGH));
        this.m_userName = m_userName;
        this.m_password = m_password;
        this.m_databasePassword = m_databasePassword;
        super.setResponseTimeout(TASK_TIMEOUT_MS);

        count++;
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
    public void taskDone(Message jmsMessage) throws Exception {
        final TextMessage textMessage = (TextMessage) jmsMessage;
        final String jsonString = textMessage.getText();

        final JSONRPC2Message jsonMessage = JSONRPC2Message.parse(jsonString);
        if(jsonMessage instanceof JSONRPC2Notification) {
            System.out.println(count);

            m_loggerProline.warn("JSON Notification method: " + ((JSONRPC2Notification) jsonMessage).getMethod()+" instead of JSON Response");
            throw new Exception("Invalid JSONRPC2Message type");
            
        } else if (jsonMessage instanceof JSONRPC2Response)  {
            
            final JSONRPC2Response jsonResponse = (JSONRPC2Response) jsonMessage;
	    m_loggerProline.debug("JSON Response Id: " + jsonResponse.getID());
            
            JSONRPC2Error jsonError = jsonResponse.getError();
	    if (jsonError != null) {
                int jsonErrCode = jsonError.getCode();
                if(jsonErrCode == JMS_EXPIRED_MSG_ERROR_CODE && m_taskInfo.getDuration() <= TASK_TIMEOUT_MS) {
                    jsonError = jsonError.appendMessage("\n Your clock should not be synchronized with Proline Server's one !! ");
                }
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
