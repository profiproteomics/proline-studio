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
import java.util.ArrayList;

/**
 *
 * @author AK249877
 */
public class MzdbEncodingVerificationBatch implements Runnable {

    private final ArrayList<File> m_files;

    public MzdbEncodingVerificationBatch(ArrayList<File> files) {
        m_files = files;
    }

    @Override
    public void run() {
        for (int i = 0; i < m_files.size(); i++) {
            verifyEncoding(m_files.get(i));
        }
    }

    private void verifyEncoding(File f) {
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success, long taskId, SubTask subTask, boolean finished) {
                ;
            }

        };
        MzdbEncodingVerificationTask encodingVerificationTask = new MzdbEncodingVerificationTask(callback, f);
        AccessDatabaseThread.getAccessDatabaseThread().addTask(encodingVerificationTask);
    }

}
