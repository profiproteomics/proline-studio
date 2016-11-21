/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.wizard;

import fr.proline.studio.dpm.serverfilesystem.RootInfo;
import fr.proline.studio.dpm.serverfilesystem.ServerFileSystemView;
import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author AK249877
 */
public class UploadBatch implements Runnable {

    private final ThreadPoolExecutor m_executor;
    private final ArrayList<File> m_files;
    private final boolean m_deleteMzdb;
    private String m_destinationLabel;

    public UploadBatch(ArrayList<File> files, boolean deleteMzdb, String destinationLabel) {
        m_files = files;
        m_deleteMzdb = deleteMzdb;
        m_destinationLabel = destinationLabel;
        m_executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(5);

    }

    public void addFile(File f) {
        if (f.getAbsolutePath().toLowerCase().endsWith(".mzdb")) {
            MzdbUploader uploader = new MzdbUploader(f, m_deleteMzdb, m_destinationLabel);
            m_executor.execute(uploader);
        }
    }

    @Override
    public void run() {
        for (int i = 0; i < m_files.size(); i++) {
            addFile(m_files.get(i));
        }
        m_executor.shutdown();
        try {
            m_executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            ;
        }
    }

}
