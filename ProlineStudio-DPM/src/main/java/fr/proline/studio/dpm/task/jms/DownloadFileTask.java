package fr.proline.studio.dpm.task.jms;


import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dpm.jms.AccessJMSManagerThread;
import static fr.proline.studio.dpm.task.jms.AbstractJMSTask.m_loggerProline;
import fr.proline.studio.dpm.task.util.JMSConnectionManager;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;


/**
 * Task to download a file from the server
 * @author JM235353
 */
public class DownloadFileTask extends AbstractJMSTask {

    private String m_userFilePath;
    private String m_serverFilePath;
    private String m_serverNodeId;
    
    public DownloadFileTask(AbstractJMSCallback callback, String userFilePath, String serverFilePath, String serverNodeId) {
        super(callback, new TaskInfo("Download File " + userFilePath, true, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));
        m_userFilePath = userFilePath;
        m_serverFilePath = serverFilePath;
        m_serverNodeId = serverNodeId;
    }
    
    
    @Override
    public void taskRun() throws JMSException {
        
        
        final JSONRPC2Request jsonRequest = new JSONRPC2Request(JMSConnectionManager.PROLINE_GET_RSC_METHOD_NAME, Integer.valueOf(m_id));
        jsonRequest.setNamedParams(createParams());
           
        final TextMessage message = AccessJMSManagerThread.getAccessJMSManagerThread().getSession().createTextMessage(jsonRequest.toJSONString());

        /* ReplyTo = Temporary Destination Queue for Server -> Client response */
        message.setJMSReplyTo(m_replyQueue);
        message.setStringProperty(JMSConnectionManager.PROLINE_SERVICE_NAME_KEY, "proline/misc/ResourceService");
        message.setStringProperty(JMSConnectionManager.PROLINE_NODE_ID_KEY, m_serverNodeId);
        addSourceToMessage(message);
        
        setTaskInfoRequest(message.getText());
        
        //  Send the Message
        m_producer.send(message);
        m_loggerProline.info("Message [{}] sent", message.getJMSMessageID());
        
    }
    
    private HashMap<String, Object> createParams() {
        HashMap<String, Object> namedParams = new HashMap<String, Object>();
        namedParams.put("file_path", m_serverFilePath);
                    
        return namedParams;
    }
    
    @Override
    public void taskDone(final Message jmsMessage) throws Exception {
        
        if (jmsMessage instanceof BytesMessage) {
            /* It is a Large Message Stream */

            boolean success = false;
            BufferedOutputStream bos = null;
            String errorMsg = "";
            try {
                bos = new BufferedOutputStream(new FileOutputStream(m_userFilePath));
                m_loggerProline.debug("Saving stream to File [" + m_userFilePath + ']');

                /* Block until all BytesMessage content is streamed into File OutputStream */
                jmsMessage.setObjectProperty(JMSConnectionManager.HORNET_Q_SAVE_STREAM_KEY, bos);
                success = true;
            } catch (FileNotFoundException | JMSException ex) {
                m_loggerProline.error("Error handling JMS_HQ_SaveStream OutputStream [" + m_userFilePath + ']', ex);
                errorMsg = "Error handling JMS_HQ_SaveStream OutputStream [" + m_userFilePath + "] ("+ex.getMessage()+")" ;
            } finally {

                if (bos != null) {
                    try {
                        bos.close();

                        if (success) {
                            m_loggerProline.info("File [" + m_userFilePath + "] Saved");
                        }

                    } catch (IOException ioEx) {
                        m_loggerProline.error("Error closing OutputStream [" + m_userFilePath + ']', ioEx);
                        errorMsg = "Error closing OutputStream [" + m_userFilePath + "] ("+ioEx.getMessage()+")" ;
                    }
                }

            }
            if(!errorMsg.isEmpty())
                throw new Exception(errorMsg);

        } else if (jmsMessage instanceof TextMessage) {
		/* It is a JSON-RPC Response (Error) */
            final TextMessage textMessage = (TextMessage) jmsMessage;
            final String jsonString = textMessage.getText();
            throw new Exception(" Invalide message type to download file ! " +jsonString);

        } else {
            throw new Exception(" Invalide message type to download file ! ");           
        }
        
         m_currentState = JMSState.STATE_DONE;
    }


}
