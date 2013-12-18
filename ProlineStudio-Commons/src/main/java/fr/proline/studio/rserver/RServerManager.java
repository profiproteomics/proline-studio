package fr.proline.studio.rserver;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RFileInputStream;
import org.rosuda.REngine.Rserve.RFileOutputStream;
import org.rosuda.REngine.Rserve.RserveException;
import org.slf4j.LoggerFactory;


/**
 * RServer Manager to be able to connect, upload files, execute commands and download file results from a R Server
 * @author JM235353
 */
public class RServerManager {
    
    private static RServerManager m_rServerManager = null;

    private RConnection m_connection = null;
    
    private String m_host = null;
    private int m_port = -1;
    
    private RServerManager(String host, int port) {
        m_host = host;
        m_port = port;
    }

    
    public static RServerManager getRServerManager(String host, int port) {

        if (m_rServerManager == null) {
            m_rServerManager = new RServerManager(host, port);
        }

        return m_rServerManager;

    }

    public RConnection connect() throws RServerException {
        return connect(null, null);
    }
    public RConnection connect(String user, String password) throws RServerException {
        
        try {
            m_connection = new RConnection(m_host, m_port);

            if (user != null) {
                m_connection.login(user, password);
            }
            
            // for the moment, we need to load the library roline //JPM.TODO
            m_connection.eval("library(Roline)");

            return m_connection;

        } catch (RserveException e) {
            LoggerFactory.getLogger("ProlineStudio.Commons").error(getClass().getSimpleName() + " failed", e);
            throw new RServerException(e.getMessage());
        }
    }
    
    public RConnection getConnection() {
        return m_connection;
    }
    
    public void uploadFile(String userPathSrc, String serverFileName) throws RServerException {

        RFileOutputStream fout = null;
        FileInputStream input = null;

        try {

            fout = m_connection.createFile(serverFileName);

            input = new FileInputStream(userPathSrc);

            byte[] buffer = new byte[256];
            int bytesRead;
            while ((bytesRead = input.read(buffer)) != -1) {

                fout.write(buffer, 0, bytesRead);
            }

        } catch (Exception e) {
            LoggerFactory.getLogger("ProlineStudio.Commons").error(getClass().getSimpleName() + " failed", e);
            throw new RServerException(e.getMessage());
        } finally {
            if (fout != null) {
                try {
                    fout.close();
                } catch (IOException e) {
                }
            }
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                }
            }
        }



    }
    
    public void downloadFile(String serverFileName, String userPathDestination) throws RServerException {
        
        RFileInputStream fin = null;
        FileOutputStream output = null;

        try {
            fin = m_connection.openFile(serverFileName);

            output = new FileOutputStream(userPathDestination);

            byte[] buffer = new byte[256];
            int bytesRead;
            while ((bytesRead = fin.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
        } catch (Exception e) {
            LoggerFactory.getLogger("ProlineStudio.Commons").error(getClass().getSimpleName() + " failed", e);
            throw new RServerException(e.getMessage());
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                }
            }
            if (fin != null) {
                try {
                    fin.close();
                } catch (IOException e) {
                }
            }
        }
 
    }
    
    public void deleteFile(String fileNameDestination) throws RServerException {
        try {
            m_connection.removeFile(fileNameDestination);
        } catch (RserveException e) {
            LoggerFactory.getLogger("ProlineStudio.Commons").error(getClass().getSimpleName() + " failed", e);
            throw new RServerException(e.getMessage());
        }
    }
    
    public String getPwd() throws RServerException {
        try {
            return m_connection.eval("getwd()").asString();
        } catch  (REXPMismatchException | REngineException e) {
            LoggerFactory.getLogger("ProlineStudio.Commons").error(getClass().getSimpleName() + " failed", e);
            throw new RServerException(e.getMessage());
        }
    }
    
    public REXP eval(String code) throws RServerException {
        
        try {
            REXP result = m_connection.parseAndEval("try(" + code + ",silent=TRUE)");
            if (result.inherits("try-error")) {
                RServerException e = new RServerException(result.asString());
                LoggerFactory.getLogger("ProlineStudio.Commons").error(getClass().getSimpleName() + " failed", e);
                throw e;
            }
        
            return result;
        } catch (REXPMismatchException | REngineException e) {
            LoggerFactory.getLogger("ProlineStudio.Commons").error(getClass().getSimpleName() + " failed", e);
            throw new RServerException(e.getMessage());
        }
    }
    
    public class RServerException extends Exception {
        
        public RServerException(String message) {
            super(message);
        }
    }
    
}
