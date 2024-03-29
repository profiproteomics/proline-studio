/* 
 * Copyright (C) 2019
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
package fr.proline.studio.dpm.serverfilesystem;

import fr.proline.studio.dpm.AccessJMSManagerThread;
import fr.proline.studio.dpm.task.jms.AbstractJMSCallback;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.Icon;
import fr.proline.studio.utils.IconManager;
import java.util.HashMap;
import javax.swing.filechooser.FileSystemView;

/**
 *
 * @author JM235353
 */
public class ServerFileSystemView extends FileSystemView {

    private static ServerFileSystemView m_singleton;
    private static File[] m_roots = null;
    private static HashMap<String, ArrayList<String>> m_rootsInfo = null;
    
    private ServerFileSystemView() {
    }
    
    public void setRoots(File[] roots){
        m_roots = roots;
    }
    
    @Override
    public File[] getRoots() {
        if (m_roots == null || m_roots.length==0) {
            //m_roots = new File[1];
            //m_roots[0] = new ServerFile();
            
            final Object mutexRootsLoaded = new Object();

            ArrayList<RootInfo> rootInfoArray = new ArrayList<>();

            try {
                synchronized (mutexRootsLoaded) {

//                    boolean[] fileLoaded = new boolean[1];
//                    fileLoaded[0] = false;
//                    
                    AbstractJMSCallback callback = new AbstractJMSCallback() {

                       @Override
                       public boolean mustBeCalledInAWT() {
                           return false;
                       }

                       @Override
                       public void run(boolean success) {

                           synchronized (mutexRootsLoaded) {
                               mutexRootsLoaded.notifyAll();
                           }
                       }
                   };

                   fr.proline.studio.dpm.task.jms.FileSystemRootsTask task = new fr.proline.studio.dpm.task.jms.FileSystemRootsTask(callback, rootInfoArray);
                   AccessJMSManagerThread.getAccessJMSManagerThread().addTask(task);
                        
                    // wait untill the files are loaded
                    mutexRootsLoaded.wait();
                }

            } catch (InterruptedException ie) {
                // should not happen
            }

            m_roots = new File[rootInfoArray.size()];
            m_rootsInfo = new HashMap<>();
            for (int i = 0; i < rootInfoArray.size(); i++) {
                String label = rootInfoArray.get(i).getLabel();
                String type = rootInfoArray.get(i).getType();
                m_roots[i] = new ServerFile(label, label, true, 0, 0);
                
                ArrayList<String> labels = m_rootsInfo.get(type);
                if (labels == null) {
                    labels = new ArrayList<>();
                    m_rootsInfo.put(type, labels);
                }
                labels.add(label);
                
            }

 
        }


        return m_roots;
    }
    
    public ArrayList<String> getLabels(String type) {
        // load data if needed 
        getRoots();
        
        return m_rootsInfo.get(type);
    }
    
    public static ServerFileSystemView getServerFileSystemView() {
        if (m_singleton == null) {
            m_singleton = new ServerFileSystemView();
        }
        return m_singleton;
    }

    
    @Override
    public Icon getSystemIcon(File f) {
        if (f == null) {
            return null;
        }

        return f.isDirectory() ? IconManager.getIcon(IconManager.IconType.FOLDER) : IconManager.getIcon(IconManager.IconType.FILE);

    }
    
    @Override
    public File getParentDirectory(File dir) {
        if (dir == null) {
            return null;
        }

        return dir.getParentFile();

    }
    
    @Override
    public String getSystemDisplayName(File f) {
        return f.getName();
    }
    
    @Override
    public File createNewFolder(File containingDir) throws IOException {
        throw new IOException("It is not allowed to create a directory.");
    }

    @Override
    public File[] getFiles(File dir, boolean useFileHiding) {

        final Object mutexFileLoaded = new Object();

        ArrayList<ServerFile> files = new ArrayList<>();
                
        try {
            synchronized (mutexFileLoaded) {

//                boolean[] fileLoaded = new boolean[1];
//                fileLoaded[0] = false;

                AbstractJMSCallback callback = new AbstractJMSCallback() {

                       @Override
                       public boolean mustBeCalledInAWT() {
                           return false;
                       }

                       @Override
                       public void run(boolean success) {

                           synchronized (mutexFileLoaded) {
                               mutexFileLoaded.notifyAll();
                           }
                       }
                };

                fr.proline.studio.dpm.task.jms.FileSystemBrowseTask task = new fr.proline.studio.dpm.task.jms.FileSystemBrowseTask(callback, dir.getPath(), files);
                AccessJMSManagerThread.getAccessJMSManagerThread().addTask(task);
                
                mutexFileLoaded.wait();
            }
        
        } catch (InterruptedException ie) {
            // should not happen
        }
        
        File[] fileArray = new File[files.size()];
        for (int i=0;i<files.size();i++) {
            fileArray[i] = files.get(i);
        }
        
        
        
        return fileArray;
    }
}
