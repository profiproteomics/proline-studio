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
package fr.proline.studio.rserver;

import fr.proline.studio.NbPreferences;
import fr.proline.studio.WindowManager;
import fr.proline.studio.dock.gui.InfoLabel;
import fr.proline.studio.python.math.StatsUtil;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.prefs.Preferences;
import javax.swing.SwingUtilities;
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

    public synchronized static RServerManager getRServerManager() {

        if (m_rServerManager == null) {
            m_rServerManager = new RServerManager();
        }

        return m_rServerManager;

    }
    
    
    public synchronized boolean startRProcessWithRetry() throws Exception {
        
        LoggerFactory.getLogger("ProlineStudio.R").info("startRProcessWithRetry()");

        boolean RStarted = false;
        
        try {
            RStarted = startRProcess();
        } catch (Exception e) {
            LoggerFactory.getLogger("ProlineStudio.R").error("Unexpected Exception while Starting R Server ", e);
        }
        
        if (!RStarted) {
            LoggerFactory.getLogger("ProlineStudio.R").info("startRProcessWithRetry() : Retry");
            RStarted = startRProcess();
        }
        
        if (RStarted) {
            try {
                LoggerFactory.getLogger("ProlineStudio.R").info("startRProcessWithRetry() : Connect");
                connect(false);
            } catch (Exception e) {
                LoggerFactory.getLogger("ProlineStudio.R").error("Unexpected Exception while connecting to R Server ", e);
            }
        }
        
        if (RStarted && !isConnected()) {
            // R has started, but we can not connect correctly
            LoggerFactory.getLogger("ProlineStudio.R").info("startRProcessWithRetry() : R has started, but we can not connect correctly");

            stopRProcess();
            Thread.sleep(5000);
            RStarted = startRProcess();
             Thread.sleep(2000);
            connect(true);
        } 
        
        return RStarted;
    }

    /**
     * Démarrage de R
     * @throws Exception 
     */
    private boolean startRProcess() throws Exception {

        if (m_RProcess != null) {
            LoggerFactory.getLogger("ProlineStudio.R").info("startRProcess() : Process already exists");

            return true;
        }
        
        // process can have been created outside
        if (isConnected()) {
            LoggerFactory.getLogger("ProlineStudio.R").info("startRProcess() : Process created outside");

            return true;
        }
        
        // try to connect without starting the process
        try {
            LoggerFactory.getLogger("ProlineStudio.R").info("startRProcess() : try to connect");
            connect(false);
        } catch (Exception e) {
            LoggerFactory.getLogger("ProlineStudio.R").error("Unexpected Exception 2 while connecting to R Server ", e);
        }
        if (isConnected()) {
            LoggerFactory.getLogger("ProlineStudio.R").info("startRProcess() : Process Connected");
            return true;
        }

        
        File f = null;
        
        LoggerFactory.getLogger("ProlineStudio.R").info("startRProcess() : Try to find R.exe if the path is defined in the RServerExePath key of the preference file");

        // try to read R.exe path potentially defined in Properties file (saved in key "RServerExePath" )
        Preferences preferences = NbPreferences.root();
        String pathToExe = preferences.get("RServerExePath", null);
        if (pathToExe != null) {
            f = new File(pathToExe);
            if (!f.exists()) {
                LoggerFactory.getLogger("ProlineStudio.R").error("R.exe is defined in Preferences file but has not been found: " + pathToExe);
                f = null;
                // we will try to read default R.exe
            }
        } else {
            // just an information in log file
            LoggerFactory.getLogger("ProlineStudio.R").info("R.exe not defined in Preferences file. Key is : RServerExePath");
        }

        // if R.exe path is not defined in Preferences file, we read it in default application path
        if (f == null) {
            LoggerFactory.getLogger("ProlineStudio.R").info("startRProcess() : Try to find R.exe in the path");

            f = new File(".");
            pathToExe = f.getCanonicalPath() + File.separatorChar + "R" + File.separatorChar + "bin" + File.separatorChar + "R.exe";
            f = new File(pathToExe);
            if (!f.exists()) {
                LoggerFactory.getLogger("ProlineStudio.R").error("R.exe not found: " + pathToExe);
                WindowManager.getDefault().getMainWindow().alert(InfoLabel.INFO_LEVEL.ERROR, "R.exe not found", new FileNotFoundException(pathToExe));
                return false;
            }
        }



        // Start R Process
        String operatingSystem = System.getProperty("os.name");
        if (operatingSystem != null && operatingSystem.startsWith("Windows")) {

            LoggerFactory.getLogger("ProlineStudio.R").info("startRProcess() : Windows system, start R.exe");


            String[] cmds = {pathToExe, "-e", "\"library(Rserve);Rserve(TRUE,args='--no-save')\"", "--no-save"};
            //String[] cmds = {pathToExe, "-e", "\"library(Rserve);Rserve(TRUE,args='--no-save --slave')\"", "--no-save", "--slave"};
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
            WindowManager.getDefault().getMainWindow().alert(InfoLabel.INFO_LEVEL.ERROR, "R Server not available for Linux or MacIntosh", new FileNotFoundException());

            return false;
            // linux
            //command = "echo \"" + todo + "\"|" + cmd + " " + rargs;
            //System.out.println("e=" + e);
            //Pocess p = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", command});
        }


    }
    
    public void stopRProcess() {
        
        // try to close the process by quitting
        if (m_connection != null) {
            try {
                LoggerFactory.getLogger("ProlineStudio.R").info("stopRProcess() : Quit R");

                parseAndEval("quit()");
            } catch (Exception e) {
                // quit() launches an exception but server is really quitted !
                LoggerFactory.getLogger("ProlineStudio.R").error("Unexpected Exception while quitting R Server Process ", e);
            }
        }
        
        // close connection if needed
        LoggerFactory.getLogger("ProlineStudio.R").info("stopRProcess() : Close R Connection");
        close();
        
        if (m_RProcess != null) {
            try {
                LoggerFactory.getLogger("ProlineStudio.R").info("stopRProcess() : Destroy R Process");
                m_RProcess.destroy(); // does not work with java 7.0 : m_RProcess.destroyForcibly();
            } catch (Exception e) {
                LoggerFactory.getLogger("ProlineStudio.R").error("Unexpected Exception while destroying R Server Process ", e);
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

        LoggerFactory.getLogger("ProlineStudio.R").info("connect() : Connect to R Process");
        connect(serverURL, port, user, password, log);
        
        if (isConnected()) {
             LoggerFactory.getLogger("ProlineStudio.R").info("connect() : Connection done, loading ProlineStudioInit.R");
           try {
                File f = new File(".");
                String pathToInitRFile = f.getCanonicalPath() + File.separatorChar + "R" + File.separatorChar + "ProlineStudioInit.R";
                f = new File(pathToInitRFile);
                if (f.exists()) {
                     m_connection.voidEval("source('"+ StatsUtil.getPath(f)+"')");
                }
            } catch  (Exception e) {
                LoggerFactory.getLogger("ProlineStudio.R").error("Loading ProlineStudioInit.R failed", e);
                throw new RServerException(e.getMessage());
            }
        }
    }
    
    public RConnection connect(String host, int port) throws RServerException {
        return connect(host, port, null, null, true);
    }
    public RConnection connect(String host, int port, String user, String password, boolean log) throws RServerException {
        
        if (m_connection != null) {
            LoggerFactory.getLogger("ProlineStudio.R").info("connect() : Already Connected");
            return m_connection;
        }
        
        m_host = host;
        m_port = port;

        try {
            m_connection = new RConnection(m_host, m_port);

            if (user != null) {
                m_connection.login(user, password);
            }

            return m_connection;

        } catch (RserveException e) {
            if (log) {
                LoggerFactory.getLogger("ProlineStudio.Commons").error(getClass().getSimpleName() + " failed", e);
                WindowManager.getDefault().getMainWindow().alert(InfoLabel.INFO_LEVEL.ERROR, e);

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
//                LoggerFactory.getLogger("ProlineStudio.Commons").error(getClass().getSimpleName() + " failed", e);
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
            }
        }
    }
    
    public boolean isRStarted() {
       return (m_RProcess != null); 
    }

    public void checkRAvailability(final RAvailableInterface client) {
        
        Runnable r = new Runnable() {
            @Override
            public void run() {

                boolean success = false;
                try {
                    success = startRProcessWithRetry();
                } catch (Exception e) {
                     LoggerFactory.getLogger("ProlineStudio.R").error("Unexpected Exception while starting R Process with retry ", e);
                }
                final boolean _success = success;
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {

                        client.available(_success);
                    }
                    
                });
                
            }
            
        };
        
        Thread t = new Thread(r);
        t.start();
    }
    
    
    public interface RAvailableInterface {
        void available(boolean b);
    }
    
}
