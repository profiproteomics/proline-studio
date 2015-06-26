package fr.proline.studio.dam.data;

import fr.proline.core.orm.uds.RawFile;
import fr.proline.core.orm.uds.Run;
import fr.proline.studio.dam.tasks.AbstractDatabaseCallback;
import fr.proline.studio.dam.tasks.AbstractDatabaseTask;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JM235353
 */
public class RunInfoData extends AbstractData {
    
    
    private RawFileSource m_rawFileSource = new RawFileSource();
    private String m_peakListPath = null;
    
    //private String m_rawFilePath = null;
    
    private Run m_run = null;
    private String m_message = null;
    
    
    private ArrayList<RawFile> m_potentialRawFileList = null;
    
    private boolean m_runInfoInDatabase = false;
    
    public RunInfoData() {
        
    }
 
    public void setPotentialRawFiles(ArrayList<RawFile> potentialRawFileList) {
        m_potentialRawFileList = potentialRawFileList;
    }
    
    public boolean hasPotentialRawFiles() {
        return m_potentialRawFileList!=null;
    }
    
    public ArrayList<RawFile> getPotentialRawFiles() {
        return m_potentialRawFileList;
    }
    

    /*
    public void setRawFilePath(String rawFilePath) {
        m_rawFilePath = rawFilePath;
    }
    
    public String getRawFilePath() {
        return m_rawFilePath;
    }*/
    
    public void setPeakListPath(String peakListPath) {
        m_peakListPath = peakListPath;
    }
    public String getPeakListPath() {
        return m_peakListPath;
    }
    
    
    public void setMessage(String message) {
        m_message = message;
    }
    
    public void setRawFileSource(RawFileSource rawFileSource) {
       m_message = null;
       m_rawFileSource = rawFileSource;
    }

    public RawFileSource getRawFileSouce() {
        return m_rawFileSource;
    }
    
    public boolean hasRawFile() {
        return m_rawFileSource.hasRawFile();
    }
    
    public boolean isRunInfoInDatabase() {
        if (m_rawFileSource.getLinkedRawFile() != null) {
            return true;
        } 
        RawFile selectedRawFile = m_rawFileSource.getSelectedRawFile();
        if (selectedRawFile!=null && selectedRawFile.getOwnerId()!=0) {
            return true;
        }
        
        return false;
    }

    @Override
    public String getName() {
        
        String name = m_rawFileSource.getName();
        if (name != null) {
            return "Raw File : "+name;
        }
   
        if (m_message != null) {
            return m_message;
        }

        return "Search...";
    }
    
    
    @Override
    public void load(AbstractDatabaseCallback callback, List<AbstractData> list, AbstractDatabaseTask.Priority priority, boolean identificationDataset) {
        // nothing to do
    }

    /**
     * @return the m_run
     */
    public Run getRun() {
        return m_run;
    }

    /**
     * @param m_run the m_run to set
     */
    public void setRun(Run run) {
        this.m_run = run;
    }

    
    public static class RawFileSource {
        
        private File m_rawFileOnDisk = null;
        private RawFile m_linkedRawFile = null;
        private RawFile m_selectedRawFile = null;
        
        public RawFileSource() {
            
        }
        
        
        
        public boolean hasRawFile() {
            return ((m_linkedRawFile!=null) || (m_selectedRawFile!=null) || (m_rawFileOnDisk!=null));
        }
        
         public String getName() {
             if (m_linkedRawFile != null) {
                 return m_linkedRawFile.getMzDbFileName();
             }
             if (m_selectedRawFile != null) {
                 return m_selectedRawFile.getMzDbFileName();
             }
             if (m_rawFileOnDisk != null) {
                 return m_rawFileOnDisk.getName();
             }
             return null;
         }
        
        public void setRawFileOnDisk(File rawFileOnDisk) {
            m_rawFileOnDisk = rawFileOnDisk;
            m_selectedRawFile = null;
            m_linkedRawFile = null;
        }

        public File getRawFileOnDisk() {
            return m_rawFileOnDisk;
        }
        
        public void setLinkedRawFile(RawFile rawFile) {
            m_linkedRawFile = rawFile;
            m_selectedRawFile = null;
            m_rawFileOnDisk = null;
        }

        
        public RawFile getLinkedRawFile() {
            return m_linkedRawFile;
        }
        
        public RawFile getSelectedRawFile() {
            if ((m_selectedRawFile == null) && (m_rawFileOnDisk != null)) {
                RawFile rawFile = new RawFile();
                rawFile.setRawFileDirectory(m_rawFileOnDisk.getPath());
                rawFile.setMzDbFileDirectory(m_rawFileOnDisk.getPath());
                int id= m_rawFileOnDisk.getName().indexOf(".");
                if (id == -1){
                    id = m_rawFileOnDisk.getName().length();
                }
                String identifier = m_rawFileOnDisk.getName().substring(0, id);
                rawFile.setRawFileName(identifier+".raw");
                rawFile.setIdentifier(identifier);
                rawFile.setMzDbFileName(m_rawFileOnDisk.getName());
                m_selectedRawFile = rawFile;
            }
            return m_selectedRawFile;
        }
       
        public void setSelectedRawFile(RawFile rawFile) {
            m_selectedRawFile = rawFile;
            m_rawFileOnDisk = null;
            m_linkedRawFile = null;
        }

        
    }
    
}
