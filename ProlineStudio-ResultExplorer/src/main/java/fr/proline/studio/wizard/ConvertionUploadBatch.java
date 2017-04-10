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
public class ConvertionUploadBatch implements Runnable, ConversionListener {

    private ThreadPoolExecutor m_conversionExecutor, m_uploadExecutor;
    private HashMap<File, ConversionSettings> m_conversions;

    public ConvertionUploadBatch(HashMap<File, ConversionSettings> conversions) {
        m_conversions = conversions;

        m_conversionExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
        m_uploadExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(5);
        
    }

    private void upload(File f, MzdbUploadSettings uploadSettings) {
        if (f.getAbsolutePath().toLowerCase().endsWith(".mzdb")) {
            if (uploadSettings.getMountLabel() == null) {
                return;
            }
            MzdbUploader uploader = new MzdbUploader(f, uploadSettings);
            m_uploadExecutor.execute(uploader);
        }
    }

    private void convert(File f, ConversionSettings conversionSettings) {
        if (f.getAbsolutePath().toLowerCase().endsWith(".raw")) {
            RawConverter converter = new RawConverter(f, conversionSettings);
            if (conversionSettings.getUploadAfterConversion()) {
                converter.addConversionListener(this);
            }
            m_conversionExecutor.execute(converter);
        }
    }

    @Override
    public void run() {

        Iterator it = m_conversions.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            convert((File) pair.getKey(), (ConversionSettings) pair.getValue());
        }

        m_conversionExecutor.shutdown();
        try {
            m_conversionExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            ;
        }
    }

    @Override
    public void ConversionPerformed(File f, ConversionSettings conversionSettings) {
        if (conversionSettings.getUploadSettings() != null) {
            upload(f, conversionSettings.getUploadSettings());
        }
    }

}
