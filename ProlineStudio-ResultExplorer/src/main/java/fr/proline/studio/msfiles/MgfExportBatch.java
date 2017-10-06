/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.msfiles;

import fr.proline.studio.rsmexplorer.MzdbFilesTopComponent;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.swing.JOptionPane;

/**
 *
 * @author AK249877
 */
public class MgfExportBatch implements Runnable, MsListener {

    private final ThreadPoolExecutor m_executor;
    private final HashMap<File, MgfExportSettings> m_exports;
    private int m_successCounter, m_failCounter;
    private HashSet<String> m_parentDirectories;

    public MgfExportBatch(HashMap<File, MgfExportSettings> exports) {
        m_exports = exports;
        m_executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
    }

    @Override
    public void run() {

        m_successCounter = 0;
        m_failCounter = 0;

        m_parentDirectories = new HashSet<String>();

        Iterator it = m_exports.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();

            File f = (File) pair.getKey();
            MgfExportSettings settings = (MgfExportSettings) pair.getValue();

            File outputDirectory = new File(settings.getDestinationDirectory());

            while (outputDirectory.getParentFile() != null) {
                m_parentDirectories.add(outputDirectory.getAbsolutePath());
                outputDirectory = outputDirectory.getParentFile();
            }

            export(f, settings);
        }

    }

    private void export(File f, MgfExportSettings settings) {
        if (f.getAbsolutePath().toLowerCase().endsWith(".mzdb")) {
            MgfExporter exporter = new MgfExporter(f, settings);
            exporter.addMsListener(this);
            m_executor.execute(exporter);
        }
    }

    @Override
    public void conversionPerformed(File f, ConversionSettings settings, boolean success) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void uploadPerformed(File f, boolean success) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void downloadPerformed(boolean success) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void exportPerformed(File f, boolean success) {
        if (success) {
            m_successCounter++;

        } else {
            m_failCounter++;
        }

        if ((m_successCounter + m_failCounter) == m_exports.size()) {

            if (m_failCounter > 0) {
                JOptionPane.showMessageDialog(null, "One or more files were found to be corrupted and were thus not converted.");
            }

            m_executor.shutdown();
            try {
                m_executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            } catch (InterruptedException e) {
                ;
            }

            MzdbFilesTopComponent.getExplorer().getLocalFileSystemView().expandMultipleTreePath(m_parentDirectories);
            MzdbFilesTopComponent.getExplorer().getLocalFileSystemView().updateTree();

        }
    }

}
