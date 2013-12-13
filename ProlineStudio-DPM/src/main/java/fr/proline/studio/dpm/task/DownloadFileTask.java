package fr.proline.studio.dpm.task;


import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;


/**
 * Task to download a file from the server
 * @author JM235353
 */
public class DownloadFileTask extends AbstractServiceTask {

    private String m_userFilePath;
    private String m_serverFilePath;
    
    public DownloadFileTask(AbstractServiceCallback callback, String userFilePath, String serverFilePath) {
        super(callback, false /** asynchronous */, new TaskInfo("Download File " + userFilePath, TASK_LIST_INFO));
        m_userFilePath = userFilePath;
        m_serverFilePath = serverFilePath;
    }
    
    
    @Override
    public boolean askService() {
        
        
        FileOutputStream outputStream = null;
        try {

            String httpRemoved = m_baseURL.substring("http://".length());
            String authority = httpRemoved.substring(0, httpRemoved.indexOf('/'));
            String path = httpRemoved.substring(httpRemoved.indexOf('/'),httpRemoved.length())+"resource_as_stream";
            String query = "file_path=" + m_serverFilePath;

            
            URI uri = new URI(
                    "http",
                    authority,
                    path,
                    query,
                    null);

            URL url = uri.toURL();


            Object obj = url.getContent();

            if (obj instanceof InputStream) {

                // read this file into InputStream
                InputStream inputStream = (InputStream) obj;

                // write the inputStream to a FileOutputStream
                outputStream = new FileOutputStream(new File(m_userFilePath));

                int read;
                byte[] bytes = new byte[1024];
                while ((read = inputStream.read(bytes)) != -1) {
                    outputStream.write(bytes, 0, read);
                }



            }


        } catch (Exception e) {
            m_taskError = new TaskError(e);
            m_loggerProline.error(getClass().getSimpleName() + " failed", e);
            return false;
        } finally {

            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                }

            }
        }

        return true;
    }

    @Override
    public ServiceState getServiceState() {

        return ServiceState.STATE_DONE;
    }
    
}
