/* 
 * Copyright (C) 2019 VD225637
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
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dpm.AccessJMSManagerThread;
import static fr.proline.studio.dpm.task.jms.AbstractJMSTask.TASK_LIST_INFO;
import static fr.proline.studio.dpm.task.jms.AbstractJMSTask.m_loggerProline;
import fr.proline.studio.dpm.task.util.JMSConnectionManager;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

/**
 *
 * @author AK249877
 */
public class DownloadMzdbTask extends AbstractJMSTask {

    private final String m_remoteFileURL;
    private final File m_localFile;

    public DownloadMzdbTask(AbstractJMSCallback callback, String remoteFileURL, File localFile) {
        super(callback, new TaskInfo("Download mzDB file " + remoteFileURL, true, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));
        m_remoteFileURL = remoteFileURL;
        m_localFile = localFile;
    }

    @Override
    public void taskRun() throws JMSException {
        final JSONRPC2Request jsonRequest = new JSONRPC2Request(JMSConnectionManager.PROLINE_GET_RSC_METHOD_NAME, Integer.valueOf(m_taskInfo.getId()));
        jsonRequest.setNamedParams(createParams());

        final TextMessage message = AccessJMSManagerThread.getAccessJMSManagerThread().getSession().createTextMessage(jsonRequest.toJSONString());

        /* ReplyTo = Temporary Destination Queue for Server -> Client response */
        message.setJMSReplyTo(m_replyQueue);
        message.setStringProperty(JMSConnectionManager.PROLINE_SERVICE_NAME_KEY, "proline/misc/ProlineResourceService");
        addSourceToMessage(message);
        addDescriptionToMessage(message);

        setTaskInfoRequest(message.getText());

        //  Send the Message
        m_producer.send(message);
        m_loggerProline.info("Message [{}] sent", message.getJMSMessageID());
        m_taskInfo.setJmsMessageID(message.getJMSMessageID());
    }

    private HashMap<String, Object> createParams() {
        HashMap<String, Object> namedParams = new HashMap<String, Object>();
        namedParams.put("file_path", m_remoteFileURL);
        return namedParams;
    }

    @Override
    public void taskDone(Message jmsMessage) throws Exception {
        if (jmsMessage instanceof BytesMessage) {
            /* It is a Large Message Stream */
            

            boolean success = false;
            BufferedOutputStream bos = null;
            String errorMsg = "";
            try {
                bos = new BufferedOutputStream(new FileOutputStream(m_localFile.getAbsolutePath()));
                m_loggerProline.debug("Saving stream to File [" + m_localFile.getAbsolutePath() + ']');

                /* Block until all BytesMessage content is streamed into File OutputStream */
                jmsMessage.setObjectProperty(JMSConnectionManager.HORNET_Q_SAVE_STREAM_KEY, bos);
                success = true;
            } catch (FileNotFoundException | JMSException ex) {
                m_loggerProline.error("Error handling JMS_HQ_SaveStream OutputStream [" + m_localFile.getAbsolutePath() + ']', ex);
                errorMsg = "Error handling JMS_HQ_SaveStream OutputStream [" + m_localFile.getAbsolutePath() + "] (" + ex.getMessage() + ")";
            } finally {

                if (bos != null) {
                    try {
                        bos.close();

                        if (success) {
                            m_loggerProline.info("File [" + m_localFile.getAbsolutePath() + "] Saved");
                        }

                    } catch (IOException ioEx) {
                        m_loggerProline.error("Error closing OutputStream [" + m_localFile.getAbsolutePath() + ']', ioEx);
                        errorMsg = "Error closing OutputStream [" + m_localFile.getAbsolutePath() + "] (" + ioEx.getMessage() + ")";
                    }
                }

            }
            if (!errorMsg.isEmpty()) {
                throw new Exception(errorMsg);
            }

        } else if (jmsMessage instanceof TextMessage) {
            /* It is a JSON-RPC Response (Error) */
            final TextMessage textMessage = (TextMessage) jmsMessage;
            final String jsonString = textMessage.getText();
            throw new Exception(" Invalide message type to download file ! " + jsonString);

        } else {
            throw new Exception(" Invalide message type to download file ! ");
        }

         m_currentState = JMSState.STATE_DONE;
    }

}
