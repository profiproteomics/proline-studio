package fr.proline.studio.dpm.serverfilesystem;

import fr.proline.studio.dpm.AccessServiceThread;
import fr.proline.studio.dpm.task.AbstractServiceCallback;
import fr.proline.studio.dpm.task.FileSystemBrowseTask;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.Icon;
import fr.proline.studio.utils.IconManager;
import javax.swing.filechooser.FileSystemView;

/**
 *
 * @author JM235353
 */
public class ServerFileSystemView extends FileSystemView {

    private static ServerFileSystemView m_singleton;
    private static File[] m_roots = null;
    
    private ServerFileSystemView() {
        
    }
    
    @Override
    public File[] getRoots() {
        if (m_roots == null) {
            m_roots = new File[1];
            m_roots[0] = new ServerFile();
        }


        return m_roots;
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

                boolean[] fileLoaded = new boolean[1];
                fileLoaded[0] = false;




                AbstractServiceCallback callback = new AbstractServiceCallback() {

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


                FileSystemBrowseTask task = new FileSystemBrowseTask(callback, dir.getPath(), files);
                AccessServiceThread.getAccessServiceThread().addTask(task);

                // wait untill the files are loaded
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
