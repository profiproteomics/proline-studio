/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.msfiles;

import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.SubTask;
import java.io.File;

/**
 *
 * @author AK249877
 */
public class MgfExporter implements Runnable {

    private final File m_file;
    private MsListener m_msListener;

    private final MgfExportSettings m_mgfExportSettings;

    public MgfExporter(File file, MgfExportSettings exportSettings) {
        m_file = file;
        m_mgfExportSettings = exportSettings;
    }

    public void addMsListener(MsListener listener) {
        m_msListener = listener;
    }

    @Override
    public void run() {

        AbstractDatabaseCallback verificationCallback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return false;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                if (success) {
                    AbstractDatabaseCallback exportMgfCallback = new AbstractDatabaseCallback() {

                        @Override
                        public boolean mustBeCalledInAWT() {
                            return false;
                        }

                        @Override
                        public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                            if (success) {
                                m_msListener.exportPerformed(m_file, true);
                            } else {
                                m_msListener.exportPerformed(m_file, false);
                            }
                        }

                    };

                    MgfExportTask exportMgfTask = new MgfExportTask(exportMgfCallback, m_file, m_mgfExportSettings);
                    AccessDatabaseThread.getAccessDatabaseThread().addTask(exportMgfTask);
                }
            }

        };

        MzdbVerificationTask verificationTask = new MzdbVerificationTask(verificationCallback, m_file);
        AccessDatabaseThread.getAccessDatabaseThread().addTask(verificationTask);

    }

}
