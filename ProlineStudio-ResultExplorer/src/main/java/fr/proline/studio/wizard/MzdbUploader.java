/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose CommunicationTools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.wizard;

import fr.proline.studio.dpm.jms.AccessJMSManagerThread;
import fr.proline.studio.dpm.task.jms.AbstractJMSCallback;
import fr.proline.studio.dpm.task.jms.FileUploadTask;
import java.io.File;
import java.util.logging.Level;

/**
 *
 * @author AK249877
 */
public class MzdbUploader implements Runnable, WorkerInterface {

    private boolean m_run = false;
    private final File m_file;
    private static int m_state = WorkerInterface.ACTIVE_STATE;
    private final StringBuilder m_logs;
    private final boolean m_deleteMzdb;
    private final String m_destinationLabel;

    public MzdbUploader(File file, boolean deleteMzdb, String destinationLabel) {
        m_file = file;
        m_deleteMzdb = deleteMzdb;
        m_destinationLabel = destinationLabel;
        m_logs = new StringBuilder();
    }

    @Override
    public void terminate() {
        this.m_run = false;
        this.m_state = WorkerInterface.KILLED_STATE;
    }

    @Override
    public boolean isAlive() {
        return this.m_run;
    }

    @Override
    public void run() {
        this.m_run = true;

        this.m_state = WorkerInterface.ACTIVE_STATE;

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
                    if (m_state == WorkerInterface.ACTIVE_STATE) { 
                        m_state = WorkerInterface.FINISHED_STATE;

                        if (m_deleteMzdb) {
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

                    }
                }else{
                    terminate();
                }
            }
        };

        FileUploadTask task = new FileUploadTask(callback, m_file.getAbsolutePath(), result);
        task.initUploadMZDB(m_destinationLabel);
        AccessJMSManagerThread.getAccessJMSManagerThread().addTask(task);

        this.m_run = false;

    }

    public File getFile() {
        return this.m_file;
    }

    @Override
    public int getState() {
        return this.m_state;
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

    @Override
    public int getWorkerType() {
        return WorkerInterface.UPLOADER_TYPE;
    }

    @Override
    public StringBuilder getLogs() {
        return m_logs;
    }

}
