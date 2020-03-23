/* 
 * Copyright (C) 2019 VD225637
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1 
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License 
 * along with this program; If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */
package fr.proline.studio.msfiles;

import fr.proline.studio.dpm.AccessJMSManagerThread;
import fr.proline.studio.dpm.task.jms.AbstractJMSCallback;
import fr.proline.studio.dpm.task.jms.FileUploadTask;
import fr.proline.studio.rsmexplorer.MzdbFilesTopComponent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Level;
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

    public void upload(final File f, MzdbUploadSettings uploadSettings) {
        if (f.getAbsolutePath().toLowerCase().endsWith(".mzdb")) {
            MzdbUploader uploader = new MzdbUploader(f, uploadSettings);
            uploader.addMsListener(this);
            m_executor.execute(uploader);
        } else {
            final String[] result = new String[1];

            AbstractJMSCallback callback = new AbstractJMSCallback() {

                @Override
                public boolean mustBeCalledInAWT() {
                    return true;
                }

                @Override
                public void run(boolean success) {

                    if (success) {

                        if (f.exists()) {
                            ArrayList<MsListenerParameter> list = new ArrayList<MsListenerParameter>();
                            list.add(new MsListenerParameter(f, true));
                            uploadPerformed(list);
                        }

                    } else {
                        ArrayList<MsListenerParameter> list = new ArrayList<>();
                        list.add(new MsListenerParameter(f, false));
                        uploadPerformed(list);
                    }
                }
            };

            FileUploadTask task = new FileUploadTask(callback, f.getAbsolutePath(), result);

            task.initUploadGenericFile(uploadSettings.getMountingPointPath(), uploadSettings.getDestination());

            AccessJMSManagerThread.getAccessJMSManagerThread().addTask(task);
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
    public void downloadPerformed(ArrayList<MsListenerDownloadParameter> list) {
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
    public void entryStateUpdated(ArrayList<MsListenerEntryUpdateParameter> list) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
