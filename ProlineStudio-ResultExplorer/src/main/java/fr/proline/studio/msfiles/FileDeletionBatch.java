/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.msfiles;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author AK249877
 */
public class FileDeletionBatch implements Runnable {

    private final ThreadPoolExecutor m_executor;
    private ArrayList<File> m_files;
    private int m_successCounter, m_failCounter;
    private HashSet<String> m_parentDirectories;

    public FileDeletionBatch(ArrayList<File> files) {
        m_files = files;
        m_executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
    }

    private void deleteFile(File f) {
        if (f.getAbsolutePath().toLowerCase().endsWith(".mzdb") || f.getAbsolutePath().toLowerCase().endsWith(".raw") || f.getAbsolutePath().toLowerCase().endsWith(".wiff") || f.getAbsolutePath().toLowerCase().endsWith(".mgf")) {
            FileDeleter deleter = new FileDeleter(f);
            m_executor.execute(deleter);
        }
    }

    @Override
    public void run() {

        m_successCounter = 0;
        m_failCounter = 0;

        m_parentDirectories = new HashSet<String>();

        for (int i = 0; i < m_files.size(); i++) {
            m_parentDirectories.add(m_files.get(i).getParentFile().getAbsolutePath());
            deleteFile(m_files.get(i));
        }

        m_executor.shutdown();
        try {
            m_executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            ;
        }
    }

}
