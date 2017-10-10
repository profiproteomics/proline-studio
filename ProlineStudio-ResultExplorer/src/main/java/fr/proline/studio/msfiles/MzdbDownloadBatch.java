/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.msfiles;

import fr.proline.studio.dpm.jms.AccessJMSManagerThread;
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

    public MzdbDownloadBatch(ArrayList<File> files, TreePath pathToExpand, String root) {

        m_files = files;
        m_pathToExpand = pathToExpand;
        m_root = root;

        StringBuilder temp = new StringBuilder();

        Object elements[] = pathToExpand.getPath();
        for (int i = 0, n = elements.length; i < n; i++) {
            temp.append(elements[i]).append("\\");
        }

        m_localURL = temp.toString();

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
                        MzdbFilesTopComponent.getExplorer().getLocalFileSystemView().expandTreePath(m_pathToExpand);
                        MzdbFilesTopComponent.getExplorer().getLocalFileSystemView().updateTree();
                    }
                }
            };

            DownloadMzdbTask task = new DownloadMzdbTask(callback, m_root + m_localURL, remoteFile.getAbsolutePath());

            AccessJMSManagerThread.getAccessJMSManagerThread().addTask(task);

        }
    }

    @Override
    public void run() {
        for (int i = 0; i < m_files.size(); i++) {
            download(m_files.get(i));
        }
    }

}
