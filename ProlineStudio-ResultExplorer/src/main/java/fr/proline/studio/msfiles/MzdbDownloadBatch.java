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
import java.util.ArrayList;
import javax.swing.tree.TreePath;

/**
 *
 * @author AK249877
 */
public class MzdbDownloadBatch implements Runnable {

    private final TreePath m_pathToExpand;
    private final String m_localURL;
    private final ArrayList<File> m_files;
    private final String m_root;
    private MsListener m_listener;
    private int m_successful, m_failed;

    public MzdbDownloadBatch(ArrayList<File> files, TreePath pathToExpand, String root) {

        m_files = files;
        m_pathToExpand = pathToExpand;
        m_root = root;

        m_successful = 0;
        m_failed = 0;

        StringBuilder temp = new StringBuilder();

        Object elements[] = pathToExpand.getPath();
        for (int i = 0, n = elements.length; i < n; i++) {
            temp.append(elements[i]).append("\\");
        }

        m_localURL = temp.toString();

    }

    public void addMsListener(MsListener listener) {
        m_listener = listener;
    }

    private void download(File remoteFile) {
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
                        m_successful++;
                    } else {
                        m_failed++;
                    }

                    if (m_successful + m_failed == m_files.size()) {
                        m_listener.downloadPerformed(m_successful > 0);
                    }
                }
            };
            
            String title = (m_pathToExpand!=null) ? m_root + m_localURL : " in a temp destination.";

            DownloadMzdbTask task = new DownloadMzdbTask(callback, title, remoteFile.getAbsolutePath());

            AccessJMSManagerThread.getAccessJMSManagerThread().addTask(task);

        }
    }

    @Override
    public void run() {
        if (m_pathToExpand != null) {
            MzdbFilesTopComponent.getExplorer().getLocalFileSystemView().expandTreePath(m_pathToExpand);
        }
        for (int i = 0; i < m_files.size(); i++) {
            download(m_files.get(i));
        }
    }

}
