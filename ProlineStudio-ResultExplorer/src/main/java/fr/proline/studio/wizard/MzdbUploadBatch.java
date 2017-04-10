/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.wizard;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author AK249877
 */
public class MzdbUploadBatch implements Runnable {

    private final ThreadPoolExecutor m_executor;
    private final HashMap<File, MzdbUploadSettings> m_uploads;

    public MzdbUploadBatch(HashMap<File, MzdbUploadSettings> uploads) {
        m_uploads = uploads;
        m_executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(5);

    }

    public void upload(File f, MzdbUploadSettings uploadSettings) {
        if (f.getAbsolutePath().toLowerCase().endsWith(".mzdb")) {
            MzdbUploader uploader = new MzdbUploader(f, uploadSettings);
            m_executor.execute(uploader);
        }
    }

    @Override
    public void run() {

        Iterator it = m_uploads.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();       
            upload((File)pair.getKey(), (MzdbUploadSettings)pair.getValue());          
        }

        m_executor.shutdown();
        try {
            m_executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            ;
        }
    }

}
