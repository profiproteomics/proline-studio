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
import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dpm.task.util.JMSConnectionManager;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Task to verify result files integrity for importation.
 * @author vd225637
 */
public class CertifyIdentificationTask extends AbstractJMSTask {

    private final String m_parserId;
    private final HashMap<String, String> m_parserArguments;
    
    private final String[] m_pathArray;
    private final long m_projectId;
    private String[] m_certifyErrorMessage;

    
    public CertifyIdentificationTask(AbstractJMSCallback callback, String parserId, HashMap<String, String> parserArguments, String[] pathArray, long projectId, String[] certifyErrorMessage) {
        super(callback, new TaskInfo("JMS Check Files to Import : "+pathArray[0]+", ...", true, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_HIGH));

        m_parserId = parserId;
        m_parserArguments = parserArguments;
        m_pathArray = pathArray;
        m_projectId = projectId;
        m_certifyErrorMessage = certifyErrorMessage;
    }

    @Override
    public void taskRun() throws JMSException  {

        // create the request
        final JSONRPC2Request jsonRequest = new JSONRPC2Request(JMSConnectionManager.PROLINE_PROCESS_METHOD_NAME, Integer.valueOf(m_taskInfo.getId()));
        //Add Service parameters
        jsonRequest.setNamedParams(createParams());

        final TextMessage message = m_session.createTextMessage(jsonRequest.toJSONString());
        /* ReplyTo = Temporary Destination Queue for Server -> Client response */
        message.setJMSReplyTo(m_replyQueue);
        message.setStringProperty(JMSConnectionManager.PROLINE_SERVICE_NAME_KEY, "proline/dps/msi/CertifyResultFiles");
        addSupplementaryInfo(message);

        setTaskInfoRequest(message.getText());
        
        // Send the Message
        m_producer.send(message);
        m_loggerProline.info("Message [{}] sent", message.getJMSMessageID());
        m_taskInfo.setJmsMessageID(message.getJMSMessageID());
    }
    
    private HashMap<String, Object> createParams() {

        HashMap<String, Object> params = new HashMap<>();
        params.put("project_id", m_projectId);

        // Result Files Parameter
        List<Map<String, Object>> args = new ArrayList<>();
        for (String m_pathArray1 : m_pathArray) {
            // add the file to parse
            Map<String, Object> resultfile = new HashMap<>();
            resultfile.put("path", m_pathArray1); // files must be accessible from web-core by the same path
            resultfile.put("format", m_parserId);
            args.add(resultfile);
        }

        params.put("result_files", args);

        // parser arguments
        params.put("importer_properties", m_parserArguments);

        return params;
    }


    @Override
    public void processWithResult(JSONRPC2Response jsonResponse) throws Exception {

        final String returnedResult = (String) jsonResponse.getResult();

	    if (returnedResult == null || (returnedResult.isEmpty())) {
		    m_loggerProline.debug("Invalid result");
            throw new Exception("null or empty result "+returnedResult);
	    } else {
		    m_loggerProline.debug("Result :\n" + returnedResult);
            if (returnedResult.equalsIgnoreCase("OK")) {
                m_certifyErrorMessage[0] = null;
            } else {
                m_certifyErrorMessage[0] = returnedResult;

                String errorMessage = returnedResult;
                m_taskError = new TaskError(errorMessage);

                m_loggerProline.error(getClass().getSimpleName() + " failed : returnedResult");
                throw new Exception(errorMessage);
            }
	    }
    }
    
}
