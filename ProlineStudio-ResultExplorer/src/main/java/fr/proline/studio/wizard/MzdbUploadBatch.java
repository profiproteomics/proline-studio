/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.wizard;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author AK249877
 */
public class MzdbUploadBatch implements Runnable {

    private final ThreadPoolExecutor m_executor;
    private final ArrayList<File> m_files;
    private final MzdbUploadSettings m_uploadSettings;
    

    public MzdbUploadBatch(ArrayList<File> files, MzdbUploadSettings uploadSettings) {
        m_files = files;
        m_uploadSettings = uploadSettings;
        m_executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(5);

    }

    public void addFile(File f) {
        if (f.getAbsolutePath().toLowerCase().endsWith(".mzdb")) {
            MzdbUploader uploader = new MzdbUploader(f, m_uploadSettings);
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
