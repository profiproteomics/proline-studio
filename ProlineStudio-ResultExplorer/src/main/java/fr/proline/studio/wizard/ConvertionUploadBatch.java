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
public class ConvertionUploadBatch implements Runnable, ConversionListener {

    private ThreadPoolExecutor m_conversionExecutor, m_uploadExecutor;
    private ArrayList<File> m_rawFiles, m_mzdbFiles;
    private ConversionSettings m_conversionSettings;

    public ConvertionUploadBatch(ArrayList<File> rawFiles, ConversionSettings conversionSettings) {
        m_rawFiles = rawFiles;
        m_conversionSettings = conversionSettings;
        
        m_conversionExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);

        if (m_conversionSettings.getUploadAfterConversion()) {
            m_uploadExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(5);
            m_mzdbFiles = new ArrayList<File>();          
        }
    }

    private void addFile(File f) {
        if (f.getAbsolutePath().toLowerCase().endsWith(".raw")) {
            RawConverter converter = new RawConverter(f, m_conversionSettings);
            if (m_conversionSettings.getUploadAfterConversion()) {
                converter.addConversionListener(this);
            }
            m_conversionExecutor.execute(converter);
        } else if (f.getAbsolutePath().toLowerCase().endsWith(".mzdb")) {
            if(m_conversionSettings.getUploadSettings().getMountLabel()==null){
                return;
            }
            
            
            
            MzdbUploader uploader = new MzdbUploader(f, m_conversionSettings.getUploadSettings());
            m_uploadExecutor.execute(uploader);
        }
    }

    @Override
    public void run() {
        for (int i = 0; i < m_rawFiles.size(); i++) {
            addFile(m_rawFiles.get(i));
        }
        m_conversionExecutor.shutdown();
        try {
            m_conversionExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            ;
        }
    }

    @Override
    public void ConversionPerformed(File f) {
        addFile(f);
    }

}