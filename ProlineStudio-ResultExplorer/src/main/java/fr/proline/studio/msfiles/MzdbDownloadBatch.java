/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
    private ArrayList<MsListenerDownloadParameter> m_list;
    private File m_localFile;

    public MzdbDownloadBatch(ArrayList<File> files, TreePath pathToExpand, String root) {
        m_files = files;
        m_pathToExpand = pathToExpand;
        m_root = root;

        m_list = new ArrayList<MsListenerDownloadParameter>();

        StringBuilder temp = new StringBuilder();

        Object elements[] = pathToExpand.getPath();
        for (int i = 0, n = elements.length; i < n; i++) {
            temp.append(elements[i]).append("\\");
        }

        m_localURL = temp.toString();
    }

    public MzdbDownloadBatch(ArrayList<File> files, String localURL, String root) {
        m_files = files;
        m_root = root;
        m_localURL = localURL;
        m_list = new ArrayList<MsListenerDownloadParameter>();
    }

    public MzdbDownloadBatch(String localURL, String root, ArrayList<WorkingSetEntry> entries) {
        m_entries = entries;
        m_root = root;
        m_localURL = localURL;
        m_list = new ArrayList<MsListenerDownloadParameter>();
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
                        m_list.add(new MsListenerDownloadParameter(m_localFile, remoteFile,true));
                    } else {
                        m_list.add(new MsListenerDownloadParameter(m_localFile, remoteFile, false));
                    }

                    if (m_listener != null) {
                        if (m_list.size() == m_files.size()) {
                            m_listener.downloadPerformed(m_list);
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
                        m_list.add(new MsListenerDownloadParameter(m_localFile, entry.getFile(), true));
                    } else {
                        m_list.add(new MsListenerDownloadParameter(m_localFile, entry.getFile(), false));
                    }

                    entry.setDownloading(false);

                    if (m_listener != null) {                        
                        m_listener.msStateChanged();
                        if (m_list.size() == m_entries.size()) {
                            m_listener.downloadPerformed(m_list);
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

            entry.setDownloading(true);

            if (m_listener != null) {
                m_listener.msStateChanged();
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
