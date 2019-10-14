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
