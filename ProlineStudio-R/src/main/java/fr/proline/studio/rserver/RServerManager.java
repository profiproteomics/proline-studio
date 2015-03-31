package fr.proline.studio.rserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;
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
    
    private Process m_RProcess = null;
    
    private RServerManager() {}

    public static RServerManager getRServerManager() {

        if (m_rServerManager == null) {
            m_rServerManager = new RServerManager();
        }

        return m_rServerManager;

    }

    /**
     * Démarrage de R
     * @throws Exception 
     */
    public boolean startRProcess() throws Exception {

        if (m_RProcess != null) {
            return true;
        }
        
        // process can have been created outside
        if (isConnected()) {
            return true;
        }
        
        // try to connect without starting the process
        try {
            connect(false);
        } catch (Exception e) {
            
        }
        if (isConnected()) {
            return true;
        }
        
         
        // Look for R.exe in ProlineStudio Path or inf path specified in properties file
        File f = new File(".");
        String pathToExe = f.getCanonicalPath()+File.separatorChar+"R"+File.separatorChar+"bin"+File.separatorChar+"R.exe";
        f = new File(pathToExe);
        if (!f.exists()) {
            String defaultExePath = pathToExe;
            // R.exe not found, try to read it from Preferences
            Preferences preferences = NbPreferences.root();
            pathToExe = preferences.get("RServerExePath", null);
            if (pathToExe == null) {
                LoggerFactory.getLogger("ProlineStudio.R").error("R.exe not found: "+defaultExePath);
                LoggerFactory.getLogger("ProlineStudio.R").error("RServerExePath in Preferences file not found : "+pathToExe);
                return false;
            } else {
                f = new File(pathToExe);
                if (!f.exists()) {
                    LoggerFactory.getLogger("ProlineStudio.R").error("R.exe not found: "+defaultExePath);
                    LoggerFactory.getLogger("ProlineStudio.R").error("R.exe not found: "+pathToExe);
                    return false;
                }
            }
        }


        // Start R Process
        String operatingSystem = System.getProperty("os.name");
        if (operatingSystem != null && operatingSystem.startsWith("Windows")) {

            String[] cmds = {pathToExe, "-e", "\"library(Rserve);Rserve(TRUE,args='--no-save --slave')\"", "--no-save", "--slave"};
            m_RProcess = Runtime.getRuntime().exec(cmds);

            // error stream
            InOutThread errorStreamThread = new InOutThread(m_RProcess.getErrorStream(), null);

            // output stream
            InOutThread outputStreamThread = new InOutThread(m_RProcess.getInputStream(), null);

            errorStreamThread.start();
            outputStreamThread.start();
            
            return true;

        } else {
            // linux and MacIntosh ?

            LoggerFactory.getLogger("ProlineStudio.R").error("R Server not available for Linux or MacIntosh");
            return false;
            // linux
            //command = "echo \"" + todo + "\"|" + cmd + " " + rargs;
            //System.out.println("e=" + e);
            //Pocess p = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", command});
        }


    }
    
    public void stopRProcess() {
        
        // try to close the process by quitting
        try {
            parseAndEval("quit()");
        } catch (Exception e) {
            // quit() launches an exception but server is really quitted !
        }
        
        // close connection if needed
        close();
        
        if (m_RProcess != null) {
            try {
                m_RProcess.destroy(); // does not work with java 7.0 : m_RProcess.destroyForcibly();
            } catch (Exception e) {
                
            } finally {
                m_RProcess = null;
            }
        }
    }
    
    public void connect() throws RServerManager.RServerException {
        connect(true);
    }
    public void connect(boolean log) throws RServerManager.RServerException {
        String serverURL = "localhost";
        Preferences preferences = NbPreferences.root();
        int port = preferences.getInt("RServerPort", 6311);

        String user = null;
        String password = null;

        connect(serverURL, port, user, password, log);

    }
    public RConnection connect(String host, int port) throws RServerException {
        return connect(host, port, null, null, true);
    }
    public RConnection connect(String host, int port, String user, String password, boolean log) throws RServerException {
        
        if (m_connection != null) {
            return m_connection;
        }
        
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
            if (log) {
                LoggerFactory.getLogger("ProlineStudio.Commons").error(getClass().getSimpleName() + " failed", e);
            }
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
    
    private static class InOutThread extends Thread {

        private InputStream m_is;
        private OutputStream m_os;

        InOutThread(InputStream is) {
            this(is, null);
        }

        InOutThread(InputStream is, OutputStream redirect) {
            m_is = is;
            m_os = redirect;
        }

        @Override
        public void run() {
            try {
                PrintWriter pw = null;
                if (m_os != null) {
                    pw = new PrintWriter(m_os);
                }

                InputStreamReader isr = new InputStreamReader(m_is);
                BufferedReader br = new BufferedReader(isr);
                String line;
                while ((line = br.readLine()) != null) {
                    if (pw != null) {
                        pw.println(line);
                    }
                }
                if (pw != null) {
                    pw.flush();
                }
            } catch (IOException ioe) {
                //ioe.printStackTrace();
            }
        }
    }
    
}
