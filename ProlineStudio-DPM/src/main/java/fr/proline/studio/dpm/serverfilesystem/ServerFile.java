package fr.proline.studio.dpm.serverfilesystem;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;

/**
 *
 * @author JM235353
 */
public class ServerFile extends File {
    
    private char SEPARATOR = '/'; // default separator
    
    private final boolean m_isDirectory;
    private final long m_lastModified;
    private final long m_length;
    
    private final String m_serverFilePath;
    private final String m_serverFileName;
    
    
   /* public ServerFile() {
        super(".");
        m_serverFilePath = ".";
        m_serverFileName = ".";
        m_isDirectory = true;
        m_lastModified = 0;
        m_length = 0;
    }*/
    
    public ServerFile(String path, String name, boolean isDirectory, long lastModified, long length) {
        super(path);
        
        m_serverFilePath = path;
        m_serverFileName = name;
        
        // check the separator
        if (m_serverFilePath.length()>m_serverFileName.length()) {
            char separator = m_serverFilePath.charAt(m_serverFilePath.length()-m_serverFileName.length()-1);
            if ((separator== '/') || (separator== '\\')) {
                SEPARATOR = separator;
            }
        }
        
        m_isDirectory = isDirectory;
        m_lastModified = lastModified;
        m_length = length;
    }
    

    
    @Override
    public String getPath() {
        return m_serverFilePath;
    }
    
    @Override
    public String getName() {
        return m_serverFileName;
    }
    
    @Override
    public String getParent() {

        if (m_serverFilePath.length() <= m_serverFileName.length()) {
            return null;
        }

        String parentPath = m_serverFilePath.substring(0, m_serverFilePath.length()-m_serverFileName.length()-1); 
        return parentPath;
    }
    
    @Override
    public File getParentFile() {
        String p = this.getParent();
        if (p == null) {
            return null;
        }
        
        int lastIndex = p.lastIndexOf(SEPARATOR);
        if (lastIndex == -1) {
            return new ServerFile(p, p, true, 0, 0);
        } else {
            return new ServerFile(p, p.substring(lastIndex+1, p.length()), true, 0, 0);
        }
        
    }
    
    @Override
    public boolean isAbsolute() {
        return true;
    }
    
    @Override
    public String getAbsolutePath() {
        return m_serverFilePath;
    }
    
    @Override
    public File getAbsoluteFile() {
        return this;
    }
    
    @Override
    public String getCanonicalPath() throws IOException {
        return m_serverFilePath;
    }
    
    @Override
    public File getCanonicalFile() throws IOException {
        return this;
    }
    
    @Override
    public boolean canRead() {
        return true;
    }
    
    @Override
    public boolean canWrite() {
        return false;
    }
    
    @Override
    public boolean exists() {
        return true;
    }
    
    @Override
    public boolean isDirectory() {
        return m_isDirectory;
    }
    
    @Override
    public boolean isFile() {
        return !m_isDirectory;
    }
    
    @Override
    public boolean isHidden() {
        return false;
    }
    
    @Override
    public long lastModified() {
        return m_lastModified;
    }
    
    @Override
    public long length() {
        return m_length;
    }
    
    @Override
    public boolean createNewFile() throws IOException {
        throw new SecurityException("createNewFile not allowed");
    }
    
    @Override
    public boolean delete() {
        throw new SecurityException("delete not allowed");
    }
    
    @Override
    public void deleteOnExit() {
        throw new SecurityException("deleteOnExit not allowed");
    }
    
    @Override
    public String[] list() {
        if (m_isDirectory) {
            File[] files = ServerFileSystemView.getServerFileSystemView().getFiles(this, false);
            String[] filesArray = new String[files.length];
            for (int i=0;i<filesArray.length;i++) {
                filesArray[i] = files[i].getPath();
            }
            return filesArray;
        }
        
        return null;
    }
    
    @Override
    public File[] listFiles() {

        if (m_isDirectory) {
            return ServerFileSystemView.getServerFileSystemView().getFiles(this, false);
            
        }
        
        return null;
    }
    
    @Override
    public File[] listFiles(FilenameFilter filter) {
        throw new UnsupportedOperationException("listFiles Not supported.");
    }
    
    @Override
    public File[] listFiles(FileFilter filter) {
        throw new UnsupportedOperationException("listFiles Not supported.");
    }
    
    @Override
    public boolean mkdir() {
        throw new SecurityException("mkdir not allowed");
    }
    @Override
    public boolean mkdirs() {
        throw new SecurityException("mkdir not allowed");
    }
    @Override
    public boolean renameTo(File dest) {
        throw new SecurityException("renameTo not allowed");
    }
    @Override
    public boolean setLastModified(long time) {
        throw new SecurityException("setLastModified not allowed");
    }
    
    @Override
    public boolean setReadOnly() {
        throw new SecurityException("setReadOnly not allowed");
    }
    
    @Override
    public boolean setWritable(boolean writable, boolean ownerOnly) {
        throw new SecurityException("setWritable not allowed");
    }
    
    @Override
    public boolean setReadable(boolean readable, boolean ownerOnly) {
        throw new SecurityException("setReadable not allowed");
    }
    
    @Override
    public boolean setExecutable(boolean executable, boolean ownerOnly) {
        throw new SecurityException("setReadable not allowed");
    }
    
    @Override
    public boolean canExecute() {
        return false;
    }

    @Override
    public long getTotalSpace() {
        throw new UnsupportedOperationException("getTotalSpace Not supported.");
    }
    
    @Override
    public long getFreeSpace() {
        throw new UnsupportedOperationException("getTotalSpace Not supported.");
    }
    
    @Override
    public long getUsableSpace() {
        throw new SecurityException("getUsableSpace not allowed");
    }
    

    
}
