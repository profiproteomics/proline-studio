/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.dpm.task.jms;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Message;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Notification;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dpm.jms.AccessJMSManagerThread;
import static fr.proline.studio.dpm.task.jms.AbstractJMSTask.m_loggerProline;
import fr.proline.studio.dpm.task.util.JMSConnectionManager;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import org.openide.util.Exceptions;

/**
 * Upload Files for MaxQuant Result : Specific for MAxQuant by searching opnly
 * some files of interest oin folder and create a Zip file with them
 *
 * @author VD225637
 */
public class FileUploadTask extends AbstractJMSTask {

    private final static int UPLOAD_GENERIC_FILE = 0;
    private final static int UPLOAD_MZDB_FILE = 1;

    private int m_action = UPLOAD_GENERIC_FILE;

    private String m_mountLabel;
    private String m_filePath;
    private String[] m_remoteFilePath = null;
    private boolean m_createParentDirectory;

    private String m_destinationPath = null;

    public FileUploadTask(AbstractJMSCallback callback, String filePath, String[] remoteFilePath) {
        super(callback, new TaskInfo("Upload file " + filePath, true, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_HIGH));
        m_filePath = filePath;
        m_remoteFilePath = remoteFilePath;
    }

    public void initUploadGenericFile() {
        m_action = UPLOAD_GENERIC_FILE;
    }

    public void initUploadMZDB(String mountLabel, boolean createParentDirectory) {
        m_action = UPLOAD_MZDB_FILE;
        m_mountLabel = mountLabel;
        m_createParentDirectory = createParentDirectory;
    }

    public void initUploadMZDB(String mountLabel, String destinationPath) {
        m_action = UPLOAD_MZDB_FILE;
        m_mountLabel = mountLabel;
        m_destinationPath = destinationPath;
        m_createParentDirectory = false;
    }

    @Override
    public void taskRun() throws JMSException {

        InputStream in = null;
        try {
            final BytesMessage message = AccessJMSManagerThread.getAccessJMSManagerThread().getSession().createBytesMessage();

            File uploadFile = null;

            if (m_action == UPLOAD_GENERIC_FILE) {
                m_loggerProline.debug("Prepare MaxQuant Import First Step : Upload Files from " + m_filePath);
                //Create ZIP file to upload
                List<String> files2Zip = getZipFilesList(m_filePath);
                uploadFile = zipIt(m_filePath, files2Zip);
            } else if (m_action == UPLOAD_MZDB_FILE) {
                m_loggerProline.debug("Prepare to upload mzDB file " + m_filePath);
                uploadFile = new File(m_filePath);
            }

            // Upload File on server side
            message.setJMSReplyTo(m_replyQueue);
            message.setStringProperty(JMSConnectionManager.PROLINE_SERVICE_NAME_KEY, "proline/misc/FileUpload");

            message.setStringProperty("dest_file_name", uploadFile.getName());

            //this needs checking!
            if (m_action == UPLOAD_MZDB_FILE) {

                String pathExtention;
                if (m_destinationPath == null) {
                    pathExtention = (m_createParentDirectory) ? File.separator + uploadFile.getParentFile().getName() : "";
                } else {
                    pathExtention = m_destinationPath;
                }
                message.setStringProperty("dest_folder_path", m_mountLabel + pathExtention);

            }

            addSourceToMessage(message);
            addDescriptionToMessage(message);

            in = new FileInputStream(uploadFile);
            BufferedInputStream inBuf = new BufferedInputStream(in);
            message.setObjectProperty(JMSConnectionManager.HORNET_Q_INPUT_STREAM_KEY, inBuf);
            setTaskInfoRequest("Call Service proline/misc/FileUpload with dest_file_name " + uploadFile.getName());
            //  Send the Message
            m_producer.send(message);
            m_loggerProline.info("Message [{}] sent", message.getJMSMessageID());
            m_taskInfo.setJmsMessageID(message.getJMSMessageID());
        } catch (FileNotFoundException ex) {
            throw new JMSException(ex.getMessage());
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    @Override
    public void taskDone(Message jmsMessage) throws Exception {
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
            if ((result == null) || (!String.class.isInstance(result))) {
                m_loggerProline.error(getClass().getSimpleName() + " failed : No returned values");
                throw new Exception("Invalid result " + result);
            }

            String returnedValues = (String) result;

            m_remoteFilePath[0] = returnedValues;
        }

        m_currentState = JMSState.STATE_DONE;

    }

    private static List<String> getZipFilesList(String rootFolder) {

        File rootFolderFile = new File(rootFolder);
        String subFolderString = "combined" + File.separator + "txt";
        File subTxtFolder = new File(rootFolderFile, subFolderString);

        if (!subTxtFolder.exists() && !subTxtFolder.isDirectory()) {
            throw new RuntimeException(" Uncomplete source folder ");
        }

        List<String> acceptedFiles = new ArrayList<>();
        String[] filesNames = rootFolderFile.list();
        for (String f : filesNames) {
            if (f.equals("mqpar.xml")) {
                acceptedFiles.add(f);
            }
        }

        String[] dataFilesNames = subTxtFolder.list();
        for (String name : dataFilesNames) {
            if (name.equals("msms.txt") || name.equals("msmsScans.txt") || name.equals("parameters.txt") || name.equals("proteinGroups.txt")
                    || name.equals("summary.txt")) {
                acceptedFiles.add(subFolderString + File.separator + name);
            }
        }
        return acceptedFiles;
    }

    private static File zipIt(String sourceDir, List<String> files2Zip) {
        byte[] buffer = new byte[1024];
        ZipOutputStream zos = null;
        try {
            File sourceDirFile = new File(sourceDir);
            String zipSource = sourceDirFile.getName();

            File zipFile = new File(sourceDir, zipSource + ".zip");
            FileOutputStream fos = new FileOutputStream(zipFile);
            zos = new ZipOutputStream(fos);

            FileInputStream in = null;
            for (String file : files2Zip) {
                m_loggerProline.debug("-- Add file: " + file);
                ZipEntry ze = new ZipEntry(zipSource + File.separator + file);
                zos.putNextEntry(ze);
                try {
                    in = new FileInputStream(sourceDir + File.separator + file);
                    int len;
                    while ((len = in.read(buffer)) > 0) {
                        zos.write(buffer, 0, len);
                    }
                } finally {
                    in.close();
                }
            }

            zos.closeEntry();
            m_loggerProline.debug("data successfully compressed");
            return zipFile;
        } catch (IOException ex) {
            String msg = "Error while creating Zip " + sourceDir;
            m_loggerProline.error(msg);
            throw new RuntimeException(msg, ex);
        } finally {
            try {
                if (zos != null) {
                    zos.close();
                }
            } catch (IOException e) {
                m_loggerProline.error("Error while closing Zip OutputStream");
            }
        }
    }

}
