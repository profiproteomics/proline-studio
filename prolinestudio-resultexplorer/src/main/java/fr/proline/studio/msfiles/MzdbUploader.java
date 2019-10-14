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
package fr.proline.studio.msfiles;

import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.dpm.AccessJMSManagerThread;
import fr.proline.studio.dpm.task.jms.AbstractJMSCallback;
import fr.proline.studio.dpm.task.jms.FileUploadTask;
import java.io.File;
import java.util.ArrayList;
import java.util.logging.Level;

/**
 *
 * @author AK249877
 */
public class MzdbUploader implements Runnable {

    private final File m_file;
    private MsListener m_msListener;

    private final MzdbUploadSettings m_uploadSettings;

    public MzdbUploader(File file, MzdbUploadSettings uploadSettings) {
        m_file = file;
        m_uploadSettings = uploadSettings;
    }

    public void addMsListener(MsListener listener) {
        m_msListener = listener;
    }

    @Override
    public void run() {

        verifyFinalization();

        verifyEncoding();

    }

    public File getFile() {
        return m_file;
    }

    private void verifyIntegrityAndUpload() {
        AbstractDatabaseCallback verificationCallback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return false;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                if (success) {
                    final String[] result = new String[1];

                    AbstractJMSCallback callback = new AbstractJMSCallback() {

                        @Override
                        public boolean mustBeCalledInAWT() {
                            return true;
                        }

                        @Override
                        public void run(boolean success) {

                            if (success) {

                                if (m_msListener != null && m_file.exists()) {
                                    ArrayList<MsListenerParameter> list = new ArrayList<MsListenerParameter>();
                                    list.add(new MsListenerParameter(m_file, true));
                                    m_msListener.uploadPerformed(list);
                                }

                                if (m_uploadSettings.getDeleteMzdb()) {
                                    while (!m_file.canWrite() && !m_file.exists()) {
                                        try {
                                            Thread.sleep(1000);
                                        } catch (InterruptedException ex) {
                                            java.util.logging.Logger.getLogger(MzdbUploader.class.getName()).log(Level.SEVERE, null, ex);
                                        }
                                    }
                                    m_file.setWritable(true);
                                    try {
                                        Thread.sleep(5000);
                                    } catch (InterruptedException ex) {
                                        java.util.logging.Logger.getLogger(MzdbUploader.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                    FileUtility.deleteFile(m_file);
                                }

                            } else {
                                if (m_msListener != null) {
                                    ArrayList<MsListenerParameter> list = new ArrayList<MsListenerParameter>();
                                    list.add(new MsListenerParameter(m_file, false));
                                    m_msListener.uploadPerformed(list);
                                }
                            }
                        }
                    };

                    FileUploadTask task = new FileUploadTask(callback, m_file.getAbsolutePath(), result);

                    task.initUploadMZDB(m_uploadSettings.getMountingPointPath(), m_uploadSettings.getDestination());

                    AccessJMSManagerThread.getAccessJMSManagerThread().addTask(task);
                }
            }

        };

        MzdbIntegrityVerificationTask verificationTask = new MzdbIntegrityVerificationTask(verificationCallback, m_file);
        AccessDatabaseThread.getAccessDatabaseThread().addTask(verificationTask);
    }

    private void verifyFinalization() {
        try {
            while (!FileUtility.isCompletelyWritten(m_file)) {
                Thread.sleep(10000);
            }
        } catch (InterruptedException ex) {
            java.util.logging.Logger.getLogger(MzdbUploader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void verifyEncoding() {
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                if (success) {
                    verifyIntegrityAndUpload();
                }
            }

        };
        MzdbEncodingVerificationTask encodingVerificationTask = new MzdbEncodingVerificationTask(callback, m_file);
        AccessDatabaseThread.getAccessDatabaseThread().addTask(encodingVerificationTask);
    }

}
