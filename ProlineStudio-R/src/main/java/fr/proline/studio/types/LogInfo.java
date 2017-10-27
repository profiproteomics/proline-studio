package fr.proline.studio.types;

/**
 *
 * Class used to add information to a Table Model in the Data Analyzer
 * This class is used to know if a columns has been logged.
 * 
 * @author JM235353
 */
public class LogInfo {
    public enum LogState {
        LOG2,
        LOG10,
        NO_LOG
    }
    
    private final LogState m_state;
    
    public LogInfo(LogState state) {
        m_state = state;
    }
    
    public boolean noLog() {
        return m_state == LogState.NO_LOG;
    } 
    
    public boolean isLog() {
        return m_state != LogState.NO_LOG;
    } 
    
    public boolean isLog2() {
        return m_state == LogState.LOG2;
    } 
    
    public boolean isLog10() {
        return m_state == LogState.LOG10;
    } 
    
}
