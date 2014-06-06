package fr.proline.studio.dpm.serverfilesystem;

/**
 *
 * @author JM235353
 */
public class RootInfo {
    
    public static final String TYPE_RESULT_FILES = "result_files";
    public static final String TYPE_RAW_FILES = "raw_files";
    public static final String TYPE_MZDB_FILES = "mzdb_files";

            
    private String m_label;
    private String m_type;
    
    public RootInfo(String label, String type) {
        m_label = label;
        m_type = type;
    }
    
    public String getLabel() {
        return m_label;
    }
    
    public String getType() {
        return m_type;
    }
    
}
