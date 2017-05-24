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
public class MgfExportBatch implements Runnable {

    private final ThreadPoolExecutor m_executor;
    private final HashMap<File, MgfExportSettings> m_exports;

    public MgfExportBatch(HashMap<File, MgfExportSettings> exports) {
        m_exports = exports;
        m_executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
    }

    @Override
    public void run() {

        Iterator it = m_exports.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();

            File f = (File) pair.getKey();
            MgfExportSettings settings = (MgfExportSettings) pair.getValue();

            export(f, settings);
        }

        m_executor.shutdown();
        try {
            m_executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            ;
        }
        
    }

    private void export(File f, MgfExportSettings settings) {
        if (f.getAbsolutePath().toLowerCase().endsWith(".mzdb")) {
            MgfExporter exporter = new MgfExporter(f, settings);
            m_executor.execute(exporter);
        }
    }

}
