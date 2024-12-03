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
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dpm.serverfilesystem.RootInfo;
import fr.proline.studio.dpm.task.util.JMSConnectionManager;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Task to get the Roots path for the Virtual File System on the server
 * @author jm235353
 */
public class FileSystemRootsTask extends AbstractJMSTask {

    ArrayList<RootInfo> m_roots;

    public FileSystemRootsTask(AbstractJMSCallback callback, ArrayList<RootInfo> roots) {
        super(callback, new TaskInfo("Get Server File System Root Paths", true, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_MEDIUM));

        m_roots = roots;
    }

    @Override
    public void taskRun() throws JMSException {

        // create the request
        final JSONRPC2Request jsonRequest = new JSONRPC2Request("retrieve_all_mount_points", Integer.valueOf(m_taskInfo.getId()));
        Map<String, Object> params = new HashMap<>(); // no parameter
        jsonRequest.setNamedParams(params);

        final TextMessage message = m_session.createTextMessage(jsonRequest.toJSONString());

        /* ReplyTo = Temporary Destination Queue for Server -> Client response */
        message.setJMSReplyTo(m_replyQueue);
        message.setStringProperty(JMSConnectionManager.PROLINE_SERVICE_NAME_KEY, "proline/misc/FileSystem");
        addSupplementaryInfo(message);

        setTaskInfoRequest(message.getText());
        m_taskInfo.setJmsMessageID(message.getJMSMessageID());
        // Send the Message
        m_producer.send(message);
        m_loggerProline.info("Message [{}] sent", message.getJMSMessageID());

    }

    @Override
    public void processWithResult(JSONRPC2Response jsonResponse) throws Exception {

        final Object result = jsonResponse.getResult();

        if (result == null || !ArrayList.class.isInstance(result)) {
            String msg = "Server has returned no Root Path. There is a problem with the server installation, please contact your administrator.";
            m_loggerProline.debug(msg);
            throw new Exception("null or invalid result : " + msg);
        } else {
            ArrayList resultList = (ArrayList) result;
            for (int i = 0; i < resultList.size(); i++) {
                Map fileMap = (Map) resultList.get(i);
                String label = (String) fileMap.get("label");
                String directoryType = (String) fileMap.get("directory_type");  // use it !
                m_roots.add(new RootInfo(label, directoryType));

            }
        }

    }

}
