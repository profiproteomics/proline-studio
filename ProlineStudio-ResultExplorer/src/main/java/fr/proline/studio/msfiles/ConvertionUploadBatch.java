/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.msfiles;

import fr.proline.studio.rsmexplorer.MzdbFilesTopComponent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import javax.swing.tree.TreePath;

/**
 *
 * @author AK249877
 */
public class ConvertionUploadBatch implements Runnable, MsListener {

    private ThreadPoolExecutor m_conversionExecutor, m_uploadExecutor;
    private HashMap<File, ConversionSettings> m_conversions;
    private TreePath m_pathToExpand;

    public ConvertionUploadBatch(HashMap<File, ConversionSettings> conversions) {
        m_conversions = conversions;
        m_conversionExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
        m_uploadExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(5);
    }

    public ConvertionUploadBatch(HashMap<File, ConversionSettings> conversions, TreePath pathToExpand) {
        this(conversions);
        m_pathToExpand = pathToExpand;
    }

    private void upload(File f, MzdbUploadSettings uploadSettings) {
        if (f.getAbsolutePath().toLowerCase().endsWith(".mzdb")) {
            if (uploadSettings.getMountingPointPath() == null) {
                return;
            }
            MzdbUploader uploader = new MzdbUploader(f, uploadSettings);
            uploader.addMsListener(this);
            m_uploadExecutor.execute(uploader);
        }
    }

    private void convert(File f, ConversionSettings conversionSettings) {
        if (f.getAbsolutePath().toLowerCase().endsWith(".raw") || f.getAbsolutePath().toLowerCase().endsWith(".wiff")) {
            RawConverter converter = new RawConverter(f, conversionSettings);
            converter.addMsListener(this);
            m_conversionExecutor.execute(converter);
        }
    }

    @Override
    public void run() {

        HashSet<String> m_parentDirectories = new HashSet<String>();

        if (m_pathToExpand == null) {
            Iterator it = m_conversions.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                ConversionSettings settings = (ConversionSettings) pair.getValue();

                if (settings.getUploadSettings() != null && !settings.getUploadSettings().getDestination().equalsIgnoreCase("")) {
                    if (settings.getUploadSettings().getDestination().startsWith(File.separator)) {
                        m_parentDirectories.add(settings.getUploadSettings().getDestination().substring(1));
                    } else {
                        m_parentDirectories.add(settings.getUploadSettings().getDestination());
                    }
                }
            }
            MzdbFilesTopComponent.getExplorer().getTreeFileChooserPanel().expandMultipleTreePath(m_parentDirectories, m_conversions.values().iterator().next().getUploadSettings().getMountingPointPath());
        } else {
            MzdbFilesTopComponent.getExplorer().getTreeFileChooserPanel().expandTreePath(m_pathToExpand);
        }

        MzdbFilesTopComponent.getExplorer().getTreeFileChooserPanel().updateTree();

        Iterator it = m_conversions.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            convert((File) pair.getKey(), (ConversionSettings) pair.getValue());
        }

    }

    @Override
    public void conversionPerformed(ArrayList<MsListenerConverterParameter> list) {

        if (list != null && !list.isEmpty()) {

            for (int i = 0; i < list.size(); i++) {

                MsListenerConverterParameter p = list.get(i);

                if (p.wasSuccessful()) {
                    if (p.getConversionSettings() != null && p.getConversionSettings().getUploadSettings() != null) {
                        MzdbFilesTopComponent.getExplorer().getLocalFileSystemView().updateTree();
                        upload(p.getFile(), p.getConversionSettings().getUploadSettings());
                    }
                }

            }
        }
    }

    @Override
    public void uploadPerformed(ArrayList<MsListenerParameter> list) {

        if (list != null && !list.isEmpty()) {

            boolean success = false;

            for (MsListenerParameter parameter : list) {
                if (parameter.wasSuccessful()) {
                    success = true;
                    break;
                }
            }

            if (success) {
                MzdbFilesTopComponent.getExplorer().getTreeFileChooserPanel().updateTree();
            }
        }
    }

    @Override
    public void downloadPerformed(ArrayList<MsListenerParameter> list) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void exportPerformed(ArrayList<MsListenerParameter> list) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void verificationPerformed(ArrayList<MsListenerParameter> list) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
