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
import java.util.ArrayList;

/**
 *
 * @author AK249877
 */
public class FileDeletionBatch implements Runnable {

    private final ArrayList<File> m_files;

    public FileDeletionBatch(ArrayList<File> files) {
        m_files = files;
    }

    private void deleteFile(File f) {
        if (f.getAbsolutePath().toLowerCase().endsWith(".mzdb") || f.getAbsolutePath().toLowerCase().endsWith(".raw") || f.getAbsolutePath().toLowerCase().endsWith(".wiff") || f.getAbsolutePath().toLowerCase().endsWith(".mgf")) {
            
            AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

                @Override
                public boolean mustBeCalledInAWT() {
                    return false;
                }

                @Override
                public void run(boolean success, long taskId, SubTask subTask, boolean finished) {

                    MzdbFilesTopComponent.getExplorer().getLocalFileSystemView().updateTree();

                }
            };

            FileDeletionTask task = new FileDeletionTask(callback, f);
            AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
        }
    }

    @Override
    public void run() {

        for (int i = 0; i < m_files.size(); i++) {
            deleteFile(m_files.get(i));
        }

    }

}
