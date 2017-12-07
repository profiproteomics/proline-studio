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
public class MzdbUploadBatch implements Runnable, MsListener {

    private final ThreadPoolExecutor m_executor;
    private final HashMap<File, MzdbUploadSettings> m_uploads;
    private TreePath m_pathToExpand;

    public MzdbUploadBatch(HashMap<File, MzdbUploadSettings> uploads) {
        m_uploads = uploads;
        m_executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(5);
    }

    public MzdbUploadBatch(HashMap<File, MzdbUploadSettings> uploads, TreePath pathToExpand) {
        this(uploads);
        m_pathToExpand = pathToExpand;
    }

    public void upload(File f, MzdbUploadSettings uploadSettings) {
        if (f.getAbsolutePath().toLowerCase().endsWith(".mzdb")) {
            MzdbUploader uploader = new MzdbUploader(f, uploadSettings);
            uploader.addMsListener(this);
            m_executor.execute(uploader);
        }
    }

    @Override
    public void run() {

        if (m_pathToExpand == null) {
            HashSet<String> m_directories = new HashSet<String>();
            Iterator it = m_uploads.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                MzdbUploadSettings settings = (MzdbUploadSettings) pair.getValue();

                if (!settings.getDestination().equalsIgnoreCase("")) {
                    if (settings.getDestination().startsWith(File.separator)) {
                        m_directories.add(settings.getDestination().substring(1));
                    } else {
                        m_directories.add(settings.getDestination());
                    }
                }
            }
            MzdbFilesTopComponent.getExplorer().getTreeFileChooserPanel().expandMultipleTreePath(m_directories, m_uploads.entrySet().iterator().next().getValue().getMountingPointPath());
        } else {
            MzdbFilesTopComponent.getExplorer().getTreeFileChooserPanel().expandTreePath(m_pathToExpand);
        }

        Iterator it = m_uploads.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            upload((File) pair.getKey(), (MzdbUploadSettings) pair.getValue());
        }

    }

    @Override
    public void conversionPerformed(ArrayList<MsListenerConverterParameter> list) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void uploadPerformed(ArrayList<MsListenerParameter> list) {
        if (list!=null && !list.isEmpty()) {
            
            boolean success = false;
            
            for(int i=0; i<list.size(); i++){
                if(list.get(i).wasSuccessful()){
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

    @Override
    public void msStateChanged() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
