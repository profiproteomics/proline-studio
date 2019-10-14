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
import fr.proline.studio.dpm.task.jms.DownloadMzdbTask;
import fr.proline.studio.rsmexplorer.MzdbFilesTopComponent;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import javax.swing.tree.TreePath;

/**
 *
 * @author AK249877
 */
public class MzdbDownloadBatch implements Runnable {

    private TreePath m_pathToExpand;
    private final String m_localURL;
    private ArrayList<File> m_files;
    private ArrayList<WorkingSetEntry> m_entries;
    private final String m_root;
    private MsListener m_listener;
    private ArrayList<MsListenerDownloadParameter> m_downloadList;
    private File m_localFile;

    public MzdbDownloadBatch(ArrayList<File> files, TreePath pathToExpand, String root) {
        m_files = files;
        m_pathToExpand = pathToExpand;
        m_root = root;

        m_downloadList = new ArrayList<MsListenerDownloadParameter>();

        StringBuilder temp = new StringBuilder();

        Object elements[] = pathToExpand.getPath();
        for (int i = 0, n = elements.length; i < n; i++) {
            temp.append(elements[i]).append("\\");
        }

        m_localURL = temp.toString();
    }

    public MzdbDownloadBatch(String localURL, String root, ArrayList<WorkingSetEntry> entries) {
        m_entries = entries;
        m_root = root;
        m_localURL = localURL;
        m_downloadList = new ArrayList<MsListenerDownloadParameter>();
    }

    public void addMsListener(MsListener listener) {
        m_listener = listener;
    }

    private void downloadFile(File remoteFile) {
        if (remoteFile.getAbsolutePath().toLowerCase().endsWith(".mzdb")) {
            AbstractJMSCallback callback = new AbstractJMSCallback() {
                @Override
                public boolean mustBeCalledInAWT() {
                    return true;
                }

                @Override
                public void run(boolean success) {

                    if (success) {
                        if (m_pathToExpand != null) {
                            MzdbFilesTopComponent.getExplorer().getLocalFileSystemView().updateTree();
                        }
                        m_downloadList.add(new MsListenerDownloadParameter(m_localFile, remoteFile,true));
                    } else {
                        m_downloadList.add(new MsListenerDownloadParameter(m_localFile, remoteFile, false));
                    }

                    if (m_listener != null) {
                        if (m_downloadList.size() == m_files.size()) {
                            m_listener.downloadPerformed(m_downloadList);
                        }
                    }
                }
            };
            
            String destinationDirectory = m_root + m_localURL;
            
            Path path = Paths.get(m_localURL);       
            String filename = path.getFileName().toString();                  
            String url = destinationDirectory+File.separatorChar+filename;
            
            m_localFile = new File(url);            
            while(m_localFile.exists()){
                url = url.substring(0, url.lastIndexOf("."))+" - Copie.mzdb";
                m_localFile = new File(url);
            }
            
            DownloadMzdbTask task = new DownloadMzdbTask(callback, remoteFile.getAbsolutePath(), m_localFile);
            AccessJMSManagerThread.getAccessJMSManagerThread().addTask(task);
        }
    }

    private void downloadEntry(WorkingSetEntry entry) {
        if (entry.getFile().getAbsolutePath().toLowerCase().endsWith(".mzdb")) {
            AbstractJMSCallback callback = new AbstractJMSCallback() {
                @Override
                public boolean mustBeCalledInAWT() {
                    return true;
                }

                @Override
                public void run(boolean success) {

                    if (success) {
                        if (m_pathToExpand != null) {
                            MzdbFilesTopComponent.getExplorer().getLocalFileSystemView().updateTree();
                        }
                        m_downloadList.add(new MsListenerDownloadParameter(m_localFile, entry.getFile(), true));
                    } else {
                        m_downloadList.add(new MsListenerDownloadParameter(m_localFile, entry.getFile(), false));
                    }
                    
                    if (m_listener != null) {  
                        
                        ArrayList<MsListenerEntryUpdateParameter> stateList = new ArrayList<MsListenerEntryUpdateParameter>();
                        stateList.add(new MsListenerEntryUpdateParameter(entry.getFile(), MsListenerEntryUpdateParameter.State.DOWNLOAD_COMPLETE));
                        
                        m_listener.entryStateUpdated(stateList);
                        
                        if (m_downloadList.size() == m_entries.size()) {
                            m_listener.downloadPerformed(m_downloadList);
                        }
                    }
                }
            };
            
            String destinationDirectory = m_root + m_localURL;
                
            String filename = entry.getFilename();                  
            String url = destinationDirectory+File.separatorChar+filename;
            
            m_localFile = new File(url);            
            while(m_localFile.exists()){
                url = url.substring(0, url.lastIndexOf("."))+" - Copie.mzdb";
                m_localFile = new File(url);
            }
            
            DownloadMzdbTask task = new DownloadMzdbTask(callback, entry.getFile().getAbsolutePath(), m_localFile);

            if (m_listener != null) {      
                ArrayList<MsListenerEntryUpdateParameter> list = new ArrayList<MsListenerEntryUpdateParameter>();
                list.add(new MsListenerEntryUpdateParameter(entry.getFile(), MsListenerEntryUpdateParameter.State.START_DOWNLOADING));              
                m_listener.entryStateUpdated(list);
            }

            AccessJMSManagerThread.getAccessJMSManagerThread().addTask(task);
        }
    }

    @Override
    public void run() {
        if (m_pathToExpand != null) {
            MzdbFilesTopComponent.getExplorer().getLocalFileSystemView().expandTreePath(m_pathToExpand);
        }
        if (m_files != null) {
            for (int i = 0; i < m_files.size(); i++) {
                downloadFile(m_files.get(i));
            }
        } else if (m_entries != null) {
            for (int i = 0; i < m_entries.size(); i++) {
                downloadEntry(m_entries.get(i));
            }
        }
    }

}
