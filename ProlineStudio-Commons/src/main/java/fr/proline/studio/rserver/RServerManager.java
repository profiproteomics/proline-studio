package fr.proline.studio.rserver;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RFileInputStream;
import org.rosuda.REngine.Rserve.RFileOutputStream;
import org.rosuda.REngine.Rserve.RserveException;


/**
 *
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

    public RConnection connect() throws RserveException {
        m_connection = new RConnection(m_host, m_port);
        
        // for the moment, we need to load the library roline //JPM.TODO
        m_connection.eval("library(Roline)");
        
        return m_connection;
    }
    
    public RConnection getConnection() {
        return m_connection;
    }
    
    public void uploadFile(String userPathSrc, String serverFileName) throws Exception {

        //String pwd = m_connection.eval("getwd()").asString();

        //System.out.println("Pwd : " + pwd);

        RFileOutputStream fout = m_connection.createFile(/*pwd + "/" +*/ serverFileName);

        FileInputStream input = new FileInputStream(userPathSrc);

        //System.out.println("upload to " + pwd + "/" + fileNameDestination);

        byte[] buffer = new byte[256];
        int bytesRead;
        while ((bytesRead = input.read(buffer)) != -1) {

            fout.write(buffer, 0, bytesRead);
        }

        fout.close();
        input.close();

        System.out.println("Upload Done");
    }
    
    public void downloadFile(String serverFileName, String userPathDestination) throws IOException {
        RFileInputStream fin = m_connection.openFile(serverFileName);
        
        FileOutputStream output = new FileOutputStream(userPathDestination);
        
        byte[] buffer = new byte[256];
        int bytesRead;
        while ((bytesRead = fin.read(buffer)) != -1) {

            output.write(buffer, 0, bytesRead);
        }

        output.close();
        fin.close();
    }
    
    public void deleteFile(String fileNameDestination) throws RserveException {
        m_connection.removeFile(fileNameDestination);
    }
    
}
