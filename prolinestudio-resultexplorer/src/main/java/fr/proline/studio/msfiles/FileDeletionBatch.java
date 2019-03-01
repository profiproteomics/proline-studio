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
