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
import fr.proline.core.orm.uds.RawFile;
import fr.proline.core.orm.uds.Run;
import fr.proline.core.orm.util.DStoreCustomPoolConnectorFactory;
import fr.proline.studio.dam.data.RunInfoData;
import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dpm.task.util.JMSConnectionManager;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import javax.persistence.EntityManager;
import java.io.File;
import java.util.HashMap;

/**
 * register raw file Task through JMS
 * @author MB243701
 */
public class RegisterRawFileTask extends AbstractJMSTask {
    private static final String m_serviceName = "proline/dps/uds/RegisterRawFile";
    //private static final String m_version = "2.0";
    
    private final String m_raw_file_path;
    private final String m_raw_file_name;
    private final long m_instrumentId;
    private final long m_ownerId;
    private final RunInfoData m_runInfoData;
    
    private EntityManager entityManagerUDS;
    
    public RegisterRawFileTask(AbstractJMSCallback callback, long instrumentId, long ownerId, RunInfoData runInfo) {
        super(callback,  new TaskInfo("Register raw file "+runInfo.getRawFileOnDisk(), true, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));
        
        //Change separator to be complient with both '/' !
        String Sep = File.separator;
        String normalizedPath = runInfo.getRawFileOnDisk().getPath();
        if(Sep.equals("\\") ){
            normalizedPath = normalizedPath.replaceAll("\\\\","/");
        }

        m_raw_file_path = normalizedPath;
        
        m_raw_file_name = runInfo.getRawFileOnDisk().getName();
        m_ownerId = ownerId;
        m_instrumentId = instrumentId;
        m_runInfoData = runInfo;
        
        // first we check if the Raw File exists already or not 
        entityManagerUDS = DStoreCustomPoolConnectorFactory.getInstance().getUdsDbConnector().createEntityManager();
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
    public void processWithResult(JSONRPC2Response jsonResponse) throws Exception {

        final Object result = jsonResponse.getResult();
        if (result == null || !Long.class.isInstance(result)) {
            m_loggerProline.debug("Invalid result: No runId returned");
            throw new Exception("Invalid result " + result);
        } else {
            m_loggerProline.debug("Result :\n" + result);
        }
        Long runId = (Long) result;

        entityManagerUDS = DStoreCustomPoolConnectorFactory.getInstance().getUdsDbConnector().createEntityManager();
        try {
            entityManagerUDS.getTransaction().begin();

            Run r = entityManagerUDS.find(Run.class, Long.valueOf(runId.longValue()));

            if (r == null) {
                m_taskError = new TaskError("Internal Error : Project not Found");
                return;
            }

            m_runInfoData.setRun(r);
            m_runInfoData.setLinkedRawFile(r.getRawFile());

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

}
