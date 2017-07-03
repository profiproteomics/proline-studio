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

/**
 *
 * @author AK249877
 */
public class FileDeleter implements Runnable {

    private File m_file;

    public FileDeleter(File file) {
        m_file = file;
    }

    @Override
    public void run() {
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

        FileDeletionTask task = new FileDeletionTask(callback, m_file);
        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
    }


}
