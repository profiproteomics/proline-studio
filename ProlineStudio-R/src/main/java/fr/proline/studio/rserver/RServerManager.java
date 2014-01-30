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
    
    private int m_currentVarIndex = 0;
    
    private RServerManager() {}

    public static RServerManager getRServerManager() {

        if (m_rServerManager == null) {
            m_rServerManager = new RServerManager();
        }

        return m_rServerManager;

    }

    public RConnection connect(String host, int port) throws RServerException {
        return connect(host, port, null, null);
    }
    public RConnection connect(String host, int port, String user, String password) throws RServerException {
        
        m_host = host;
        m_port = port;

        try {
            m_connection = new RConnection(m_host, m_port);

            if (user != null) {
                m_connection.login(user, password);
            }
            
            // for the moment, we need to load the library roline //JPM.TODO
            //m_connection.eval("library(Roline)");
            //m_connection.eval("library(Cairo)");
            //m_connection.eval("library(png)");

            return m_connection;

        } catch (RserveException e) {
            LoggerFactory.getLogger("ProlineStudio.Commons").error(getClass().getSimpleName() + " failed", e);
            m_connection = null;
            throw new RServerException(e.getMessage());
        }

    }
    
    public void close() {
        if (m_connection == null) {
            return;
        }
        
        m_connection.close();
        m_connection = null;
        
    }
    
    public RConnection getConnection() {
        return m_connection;
    }
    
    public boolean isConnected() {
        return (m_connection != null);
    }
    
    public String getNewVariableName(String varBase) {
        m_currentVarIndex++;
        return varBase+String.valueOf(m_currentVarIndex);
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
    
    public REXP parseAndEval(String code) throws RServerException {
        
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
    
    /*public REXP eval(String code) throws RServerException {
        
        try {
            m_connection.eval("try(" + code + ",silent=TRUE)");
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
    }*/
    
    
    public class RServerException extends Exception {
        
        public RServerException(String message) {
            super(message);
        }
    }
    
}
