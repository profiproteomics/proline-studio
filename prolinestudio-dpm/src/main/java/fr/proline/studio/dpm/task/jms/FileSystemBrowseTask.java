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
import fr.proline.studio.dpm.serverfilesystem.ServerFile;
import fr.proline.studio.dpm.task.util.JMSConnectionManager;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Task to create a new Project in the UDS db
 * @author jm235353
 */
public class FileSystemBrowseTask extends AbstractJMSTask {

    private final String m_dirPath;
    private final ArrayList<ServerFile> m_files;
    
    public FileSystemBrowseTask(AbstractJMSCallback callback, String dirPath, ArrayList<ServerFile> files) {
        super(callback,  new TaskInfo("Browse Server File System "+dirPath, true, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_LOW));
        
        m_dirPath = dirPath;
        m_files = files;
    }
    
   @Override
    public void taskRun() throws JMSException {

        // create the request
        final JSONRPC2Request jsonRequest = new JSONRPC2Request("retrieve_directory_content", Integer.valueOf(m_taskInfo.getId()));
        jsonRequest.setNamedParams(createParams());

        final TextMessage message = m_session.createTextMessage(jsonRequest.toJSONString());

        /* ReplyTo = Temporary Destination Queue for Server -> Client response */
        message.setJMSReplyTo(m_replyQueue);
        message.setStringProperty(JMSConnectionManager.PROLINE_SERVICE_NAME_KEY, "proline/misc/FileSystem");
        addSupplementaryInfo(message);

        setTaskInfoRequest(message.getText());
         
        
        // Send the Message
        m_producer.send(message);
        m_loggerProline.info("Message [{}] sent", message.getJMSMessageID());
        m_taskInfo.setJmsMessageID(message.getJMSMessageID());
    }

    private HashMap<String, Object> createParams() {
        HashMap<String, Object> params = new HashMap<>();
        params.put("label_path", m_dirPath);
        params.put("include_files", Boolean.TRUE);
        params.put("include_dirs", Boolean.TRUE);
        return params;
    }

    @Override
    public void processWithResult(JSONRPC2Response jsonResponse) throws Exception {
        final Object result = jsonResponse.getResult();

        if (result == null || !ArrayList.class.isInstance(result)) {
            m_loggerProline.debug("Invalid or no result");
            throw new Exception("null or invalid result " + result);
        } else {
            ArrayList resultList = (ArrayList) result;
            for (int i = 0; i < resultList.size(); i++) {
                Map fileMap = (Map) resultList.get(i);

                ServerFile f;
                boolean isDir = (Boolean) fileMap.get("is_dir");
                if (isDir) {
                    f = new ServerFile((String) fileMap.get("path"), (String) fileMap.get("name"), true, 0, 0);
                } else {
                    Long size = (fileMap.containsKey("size") ? (Long) fileMap.get("size") : 0);
                    f = new ServerFile((String) fileMap.get("path"), (String) fileMap.get("name"), false, (Long) fileMap.get("lastmodified"), size);
                }
                m_files.add(f);
            }
        }
    }

}
