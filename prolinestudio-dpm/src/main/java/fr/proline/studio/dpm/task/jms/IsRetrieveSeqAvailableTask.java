package fr.proline.studio.dpm.task.jms;

import com.thetransactioncompany.jsonrpc2.*;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dpm.task.util.JMSConnectionManager;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

public class IsRetrieveSeqAvailableTask extends AbstractJMSTask {

    private static final String m_serviceName = "proline/seq/isServiceAlive";
    private static final String m_version = "1.0";
    private Boolean[] m_result;

    public IsRetrieveSeqAvailableTask(AbstractJMSCallback callback, Boolean[] m_result) {
        super(callback, true, new TaskInfo( "Verify if Retrieve Sequence is available", false, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));
        this.m_result = m_result;
    }

    @Override
    public void taskRun() throws JMSException {
        final JSONRPC2Request jsonRequest = new JSONRPC2Request(JMSConnectionManager.PROLINE_PROCESS_METHOD_NAME, Integer.valueOf(m_taskInfo.getId()));
        final TextMessage message = m_session.createTextMessage(jsonRequest.toJSONString());

        /* ReplyTo = Temporary Destination Queue for Server -> Client response */
        message.setJMSReplyTo(m_replyQueue);
        message.setStringProperty(JMSConnectionManager.PROLINE_SERVICE_NAME_KEY, m_serviceName);
        message.setStringProperty(JMSConnectionManager.PROLINE_SERVICE_VERSION_KEY, m_version);
        addSupplementaryInfo(message);

        setTaskInfoRequest(message.getText());

        //  Send the Message
        m_producer.send(message, Message.DEFAULT_DELIVERY_MODE, Message.DEFAULT_PRIORITY, 10000);
        m_loggerProline.info("Message [{}] sent", message.getJMSMessageID());
        m_taskInfo.setJmsMessageID(message.getJMSMessageID());
    }

    @Override
    public void taskDone(Message jmsMessage) throws Exception {
        final TextMessage textMessage = (TextMessage) jmsMessage;
        final String jsonString = textMessage.getText();

        final JSONRPC2Message jsonMessage = JSONRPC2Message.parse(jsonString);
        if(jsonMessage instanceof JSONRPC2Notification) {
            m_loggerProline.warn("JSON Notification method: " + ((JSONRPC2Notification) jsonMessage).getMethod()+" instead of JSON Response");
            throw new Exception("Invalid JSONRPC2Message type");

        } else if (jsonMessage instanceof JSONRPC2Response)  {

            final JSONRPC2Response jsonResponse = (JSONRPC2Response) jsonMessage;
            m_loggerProline.debug("JSON Response Id: " + jsonResponse.getID());

            final JSONRPC2Error jsonError = jsonResponse.getError();

            if (jsonError != null) {
                m_loggerProline.error("JSON Error code {}, message : \"{}\"", jsonError.getCode(), jsonError.getMessage());
                m_loggerProline.error("JSON Throwable", jsonError);
                if(jsonError.getCode() == JMSConnectionManager.JMS_EXPIRED_MSG_ERROR_CODE) {
                    m_result[0] = false;
                } else {
                    throw jsonError;
                }
            } else {

                final Object result = jsonResponse.getResult();
                if (result == null || !Boolean.class.isInstance(result)) {
                    m_loggerProline.debug("Invalid result");
                    throw new Exception("Invalid result " + result);
                } else {
                    m_result[0] = (Boolean) result;
                }
            }
        }
        m_currentState = JMSState.STATE_DONE;
    }
}
