/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.msfiles;

import fr.proline.studio.dam.AccessDatabaseThread;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.SubTask;
import fr.proline.studio.rsmexplorer.MzdbFilesTopComponent;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author AK249877
 */
public class MgfExportBatch implements Runnable {

    private final HashMap<File, MgfExportSettings> m_exports;
    private HashSet<String> m_parentDirectories;

    public MgfExportBatch(HashMap<File, MgfExportSettings> exports) {
        m_exports = exports;
    }

    @Override
    public void run() {

        m_parentDirectories = new HashSet<String>();

        Iterator itDir = m_exports.entrySet().iterator();
        while (itDir.hasNext()) {
            Map.Entry pair = (Map.Entry) itDir.next();

            File f = (File) pair.getKey();
            MgfExportSettings settings = (MgfExportSettings) pair.getValue();

            File outputDirectory = new File(settings.getDestinationDirectory());

            while (outputDirectory.getParentFile() != null) {
                m_parentDirectories.add(outputDirectory.getAbsolutePath());
                outputDirectory = outputDirectory.getParentFile();
            }

            MzdbFilesTopComponent.getExplorer().getLocalFileSystemView().expandMultipleTreePath(m_parentDirectories);
            MzdbFilesTopComponent.getExplorer().getLocalFileSystemView().updateTree();

        }

        Iterator itFiles = m_exports.entrySet().iterator();
        while (itFiles.hasNext()) {
            Map.Entry pair = (Map.Entry) itFiles.next();
            File f = (File) pair.getKey();
            MgfExportSettings settings = (MgfExportSettings) pair.getValue();
            export(f, settings);
        }

    }

    private void export(File f, MgfExportSettings settings) {
        if (f.getAbsolutePath().toLowerCase().endsWith(".mzdb")) {

            AbstractDatabaseCallback encodingCallback = new AbstractDatabaseCallback() {

                @Override
                public boolean mustBeCalledInAWT() {
                    return true;
                }

                @Override
                public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                    if (success) {
                        AbstractDatabaseCallback integrityCallback = new AbstractDatabaseCallback() {

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
                                                MzdbFilesTopComponent.getExplorer().getLocalFileSystemView().updateTree();
                                            }
                                        }

                                    };

                                    MgfExportTask exportMgfTask = new MgfExportTask(exportMgfCallback, f, settings);
                                    AccessDatabaseThread.getAccessDatabaseThread().addTask(exportMgfTask);
                                }
                            }

                        };

                        MzdbIntegrityVerificationTask verificationTask = new MzdbIntegrityVerificationTask(integrityCallback, f);
                        AccessDatabaseThread.getAccessDatabaseThread().addTask(verificationTask);
                    }
                }

            };
            MzdbEncodingVerificationTask encodingVerificationTask = new MzdbEncodingVerificationTask(encodingCallback, f);
            AccessDatabaseThread.getAccessDatabaseThread().addTask(encodingVerificationTask);

        }
    }

}
