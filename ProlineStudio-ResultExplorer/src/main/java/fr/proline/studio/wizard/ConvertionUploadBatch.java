/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.wizard;

import fr.proline.studio.rsmexplorer.MzdbFilesTopComponent;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.swing.tree.TreePath;

/**
 *
 * @author AK249877
 */
public class ConvertionUploadBatch implements Runnable, ConversionListener {

    private ThreadPoolExecutor m_conversionExecutor, m_uploadExecutor;
    private HashMap<File, ConversionSettings> m_conversions;
    private TreePath m_pathToExpand;
    private int m_uploadCounter, m_failedConversions;
    private HashSet<String> m_parentDirectories;

    public ConvertionUploadBatch(HashMap<File, ConversionSettings> conversions) {
        m_conversions = conversions;
        m_uploadCounter = 0;
        m_failedConversions = 0;
        m_conversionExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
        m_uploadExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(5);
    }

    public ConvertionUploadBatch(HashMap<File, ConversionSettings> conversions, TreePath pathToExpand) {
        this(conversions);
        m_pathToExpand = pathToExpand;
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
        
        m_parentDirectories = new HashSet<String>();

        Iterator it = m_conversions.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            
            File f = (File) pair.getKey();
            ConversionSettings settings = (ConversionSettings) pair.getValue();

            if (m_pathToExpand == null) {

                if (!settings.getUploadSettings().getDestination().equalsIgnoreCase("")) {
                    if (settings.getUploadSettings().getDestination().startsWith(File.separator)) {
                        m_parentDirectories.add(settings.getUploadSettings().getDestination().substring(1));
                    } else {
                        m_parentDirectories.add(settings.getUploadSettings().getDestination());
                    }
                }

            }
            
            convert(f, settings);
        }

        m_conversionExecutor.shutdown();
        try {
            m_conversionExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            ;
        }

    }

    @Override
    public void ConversionPerformed(File f, ConversionSettings conversionSettings, boolean success) {

        if (success) {
            if (conversionSettings.getUploadSettings() != null) {
                upload(f, conversionSettings.getUploadSettings());
                m_uploadCounter++;
            }
        } else {
            m_failedConversions++;
        }

        if ((m_uploadCounter + m_failedConversions) == m_conversions.size()) {
            m_uploadExecutor.shutdown();
            try {
                m_uploadExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            } catch (InterruptedException e) {
                ;
            }
            if (m_pathToExpand != null) {
                MzdbFilesTopComponent.getTreeFileChooserPanel().expandTreePath(m_pathToExpand);
            }else{
                MzdbFilesTopComponent.getTreeFileChooserPanel().expandMultipleTreePath(m_parentDirectories, conversionSettings.getUploadSettings().getMountLabel());
            }
        }
    }

}
