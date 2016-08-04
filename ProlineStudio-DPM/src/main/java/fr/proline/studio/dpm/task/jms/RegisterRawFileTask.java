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
import fr.proline.core.orm.uds.RawFile;
import fr.proline.core.orm.uds.Run;
import fr.proline.core.orm.util.DataStoreConnectorFactory;
import fr.proline.studio.dam.data.RunInfoData;
import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dpm.jms.AccessJMSManagerThread;
import static fr.proline.studio.dpm.task.jms.AbstractJMSTask.m_loggerProline;
import fr.proline.studio.dpm.task.util.JMSConnectionManager;
import java.io.File;
import java.util.HashMap;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.persistence.EntityManager;

/**
 * register raw file Task through JMS
 * @author MB243701
 */
public class RegisterRawFileTask extends AbstractJMSTask {
    private static final String m_serviceName = "proline/dps/uds/RegisterRawFile";
    //private static final String m_version = "2.0";
    
    private String m_raw_file_path;
    private String m_raw_file_name;
    private long m_instrumentId;
    private long m_ownerId;
    private RunInfoData m_runInfoData;
    
    private EntityManager entityManagerUDS;
    
    public RegisterRawFileTask(AbstractJMSCallback callback, long instrumentId, long ownerId, RunInfoData runInfo) {
        super(callback,  new TaskInfo("Register raw file "+runInfo.getRawFileSouce().getRawFileOnDisk(), true, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));
        
        //Change separator to be complient with both '/' !
        String Sep = File.separator;
        String normalizedPath = runInfo.getRawFileSouce().getRawFileOnDisk().getPath();
        if(Sep.equals("\\") ){
            normalizedPath = normalizedPath.replaceAll("\\\\","/");
        }

        m_raw_file_path = normalizedPath;
        
        m_raw_file_name = runInfo.getRawFileSouce().getRawFileOnDisk().getName();
        m_ownerId = ownerId;
        m_instrumentId = instrumentId;
        m_runInfoData = runInfo;
        
        // first we check if the Raw File exists already or not 
        entityManagerUDS = DataStoreConnectorFactory.getInstance().getUdsDbConnector().createEntityManager();
    }
    
    
    @Override
    public void taskRun() throws JMSException {
        // first we check if the Raw File exists already or not 
        try {
            entityManagerUDS.getTransaction().begin();
        
            RawFile rawFile = entityManagerUDS.find(RawFile.class, m_raw_file_name);

            if (rawFile != null) {
                Run r = rawFile.getRuns().get(0);
                m_runInfoData.setRun(r);
                return ;
            }

            
            
            entityManagerUDS.getTransaction().commit();

        } catch (Exception e) {
            m_loggerProline.error(getClass().getSimpleName() + " failed", e);
            try {
                entityManagerUDS.getTransaction().rollback();
            } catch (Exception rollbackException) {
                m_loggerProline.error(getClass().getSimpleName() + " failed : potential network problem", rollbackException);
            }
            return ;
        } finally {
            entityManagerUDS.close();
        }
        
        
        
        // task Run
        final JSONRPC2Request jsonRequest = new JSONRPC2Request(JMSConnectionManager.PROLINE_PROCESS_METHOD_NAME, Integer.valueOf(m_id));
        jsonRequest.setNamedParams(createParams());

        final TextMessage message = AccessJMSManagerThread.getAccessJMSManagerThread().getSession().createTextMessage(jsonRequest.toJSONString());

        /* ReplyTo = Temporary Destination Queue for Server -> Client response */
        message.setJMSReplyTo(m_replyQueue);
        message.setStringProperty(JMSConnectionManager.PROLINE_SERVICE_NAME_KEY, m_serviceName);
        //message.setStringProperty(JMSConnectionManager.PROLINE_SERVICE_VERSION_KEY, m_version);
        addSourceToMessage(message);
        
        setTaskInfoRequest(message.getText());

        //  Send the Message
        m_producer.send(message);
        m_loggerProline.info("Message [{}] sent", message.getJMSMessageID());

    }
    
    private HashMap<String, Object> createParams() {
        HashMap<String, Object> params = new HashMap<>();
        String rwFilePath = m_raw_file_path;
        int id = rwFilePath.lastIndexOf(".");
        if (id != -1) {
            rwFilePath = rwFilePath.substring(0, id);
        }
        String identifier = m_raw_file_name;
        int id2 = m_raw_file_name.lastIndexOf(".");
        if (id2 != -1){
            identifier = m_raw_file_name.substring(0, id2);
        }
        params.put("mzdb_file_path", m_raw_file_path);
        params.put("raw_file_identifier", identifier);
        params.put("raw_file_path",rwFilePath);
        params.put("instrument_id", m_instrumentId);
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
                m_loggerProline.debug("Invalid result: No runId returned");
                throw new Exception("Invalid result " + result);
            } else {
                m_loggerProline.debug("Result :\n" + result);
            }
            Long runId = (Long) result;
            
            entityManagerUDS = DataStoreConnectorFactory.getInstance().getUdsDbConnector().createEntityManager();
            try {
                entityManagerUDS.getTransaction().begin();

                Run r = entityManagerUDS.find(Run.class, Long.valueOf(runId.longValue()));

                if (r == null) {
                    m_taskError = new TaskError("Internal Error : Project not Found");
                    return ;
                }

                m_runInfoData.setRun(r);
                //m_runInfoData.setRunInfoInDatabase(true); //JPM.RUNINFODATA

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
