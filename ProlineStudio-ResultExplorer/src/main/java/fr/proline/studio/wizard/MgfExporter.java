/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.wizard;

import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.SubTask;
import java.io.File;
import javax.swing.SwingUtilities;

/**
 *
 * @author AK249877
 */
public class MgfExporter implements Runnable, WorkerInterface {

    private boolean m_run = false;
    private final File m_file;
    private static int m_state = WorkerInterface.ACTIVE_STATE;
    private final StringBuilder m_logs;
    
    private final MgfExportSettings m_mgfExportSettings;

    public MgfExporter(File file, MgfExportSettings exportSettings) {
        m_file = file;
        m_mgfExportSettings= exportSettings;
        
        m_logs = new StringBuilder();
    }

    @Override
    public void run() {
        m_run = true;

        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return false;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {

                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {

                        if (success) {

                            if (m_state == WorkerInterface.ACTIVE_STATE) {
                                m_state = WorkerInterface.FINISHED_STATE;
                            }
                        } else {
                            terminate();
                        }

                    }
                });

            }
        };

        MgfExportTask task = new MgfExportTask(callback, m_file, m_logs, m_mgfExportSettings);
        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);

        m_logs.append("Exporting .mgf for " + m_file.getAbsolutePath() + " has come to its end.\n\n");
        m_run = false;
    }

    @Override
    public void terminate() {
        m_run = false;
        m_state = WorkerInterface.KILLED_STATE;
    }

    @Override
    public boolean isAlive() {
        return m_run;
    }

    @Override
    public int getState() {
        return m_state;
    }

    @Override
    public File getFile() {
        return m_file;
    }

    @Override
    public int getWorkerType() {
        return WorkerInterface.CONVERTER_TYPE;
    }

    @Override
    public StringBuilder getLogs() {
        return m_logs;
    }

}
