package fr.proline.studio.dam.data;

import fr.proline.core.orm.uds.RawFile;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import java.util.List;

/**
 *
 * @author JM235353
 */
public class RunInfoData extends AbstractData {
    
    private String m_peakListPath = null;
    private RawFile m_rawFile = null;
    private String m_message = null;
    
    public RunInfoData() {
        
    }
    
    public void setPeakListPath(String peakListPath) {
        m_peakListPath = peakListPath;
    }
    public String getPeakListPath() {
        return m_peakListPath;
    }
    
    
    public void setMessage(String message) {
        m_message = message;
    }
    
    public void setRawFile(RawFile rawFile) {
       m_message = null; 
       m_rawFile = rawFile;
    }

    public RawFile getRawFile() {
        return m_rawFile;
    }

    @Override
    public String getName() {
        if (m_rawFile != null) {
            return "Raw File : "+m_rawFile.getRawFileName();
        }
   
        if (m_message != null) {
            return m_message;
        }

        return "Search...";
    }
    
    
    @Override
    public void load(AbstractDatabaseCallback callback, List<AbstractData> list) {
        // nothing to do
    }

}
