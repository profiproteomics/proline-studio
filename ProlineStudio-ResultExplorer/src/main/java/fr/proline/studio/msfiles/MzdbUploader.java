/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose CommunicationTools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.msfiles;

import fr.proline.studio.dpm.jms.AccessJMSManagerThread;
import fr.proline.studio.dpm.task.jms.AbstractJMSCallback;
import fr.proline.studio.dpm.task.jms.FileUploadTask;
import java.io.File;
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

        this.checkFileFinalization();

        final String[] result = new String[1];

        AbstractJMSCallback callback = new AbstractJMSCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success) {

                if (success) {
                        
                        if(m_msListener!=null && m_file.exists()){
                            m_msListener.uploadPerformed(m_file, true);
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
                        m_msListener.uploadPerformed(m_file, false);
                    }
                }
            }
        };

        FileUploadTask task = new FileUploadTask(callback, m_file.getAbsolutePath(), result);

        task.initUploadMZDB(m_uploadSettings.getMountLabel(), m_uploadSettings.getDestination());

        AccessJMSManagerThread.getAccessJMSManagerThread().addTask(task);

    }

    public File getFile() {
        return m_file;
    }

    private void checkFileFinalization() {
        try {
            while (!FileUtility.isCompletelyWritten(m_file)) {
                Thread.sleep(10000);
            }
        } catch (InterruptedException ex) {
            java.util.logging.Logger.getLogger(MzdbUploader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
