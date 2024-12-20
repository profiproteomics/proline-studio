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

import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import fr.proline.studio.Exceptions;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dpm.task.util.JMSConnectionManager;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Upload Files for MaxQuant Result : Specific for MAxQuant by searching opnly
 * some files of interest oin folder and create a Zip file with them
 *
 * @author VD225637
 */
public class FileUploadTask extends AbstractJMSTask {

    private final static int UPLOAD_GENERIC_FILE = 0;
    private final static int UPLOAD_MAXQUANT_FILE = 1;
    private final static int UPLOAD_MZDB_FILE = 2;

    private int m_action = UPLOAD_MAXQUANT_FILE;

    private String m_mountLabel;
    private final String m_filePath;
    private final String[] m_remoteFilePath;

    private String m_destinationPath = null;

    public FileUploadTask(AbstractJMSCallback callback, String filePath, String[] remoteFilePath) {
        super(callback, new TaskInfo("Upload file " + filePath, true, TASK_LIST_INFO, TaskInfo.INFO_IMPORTANCE_HIGH));
        m_filePath = filePath;
        m_remoteFilePath = remoteFilePath;
    }

    public void initUploadMaxquantFile() {
        m_action = UPLOAD_MAXQUANT_FILE;
    }

    public void initUploadGenericFile(String mountLabel, String destinationPath) {
        m_action = UPLOAD_GENERIC_FILE;
        m_mountLabel = mountLabel;
        m_destinationPath = destinationPath;
    }
    
    
    public void initUploadMZDB(String mountLabel, String destinationPath) {
        m_action = UPLOAD_MZDB_FILE;
        m_mountLabel = mountLabel;
        m_destinationPath = destinationPath;
    }

    @Override
    public void taskRun() throws JMSException {

        InputStream in = null;
        try {
            final BytesMessage message = m_session.createBytesMessage();

            File uploadFile = null;

            
            
            if (m_action == UPLOAD_GENERIC_FILE) {
                m_loggerProline.debug("Prepare to upload file " + m_filePath);
                uploadFile = new File(m_filePath);
            } else if (m_action == UPLOAD_MAXQUANT_FILE) {
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
            if ((m_action == UPLOAD_MZDB_FILE) || (m_action == UPLOAD_GENERIC_FILE)) {                
                message.setStringProperty("dest_folder_path", m_mountLabel + m_destinationPath);
            }

            addSupplementaryInfo(message);

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
    public void processWithResult(JSONRPC2Response jsonResponse) throws Exception {

        final Object result = jsonResponse.getResult();
        if ((result == null) || (!String.class.isInstance(result))) {
            m_loggerProline.error(getClass().getSimpleName() + " failed : No returned values");
            throw new Exception("Invalid result " + result);
        }

        String returnedValues = (String) result;

        m_remoteFilePath[0] = returnedValues;
    }


    private static List<String> getZipFilesList(String rootFolder) {

        File rootFolderFile = new File(rootFolder);
        String subFolderString = "combined/txt";
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
                    || name.equals("summary.txt") || name.equals("allPeptides.txt") || name.equals("evidence.txt")) {
                acceptedFiles.add(subFolderString + "/" + name);
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
                ZipEntry ze = new ZipEntry(zipSource +"/" + file);
                zos.putNextEntry(ze);
                try {
                    in = new FileInputStream(sourceDir + "/" + file);
                    int len;
                    while ((len = in.read(buffer)) > 0) {
                        zos.write(buffer, 0, len);
                    }
                } finally {
                    if(in !=null)
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
