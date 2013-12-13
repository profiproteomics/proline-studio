/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.dam.taskinfo;

/**
 *
 * @author JM235353
 */
public class TaskInfo implements Comparable<TaskInfo> {
    
    public final static int STATE_WAITING = 0;
    public final static int STATE_RUNNING = 1;
    public final static int STATE_FINISHED = 2;


    private String m_taskDescription = null;
    private String m_idList = null;
    
    private int m_state;
    private int m_updateState = -1;
    
    private long m_askTimestamp = -1;
    private long m_startTimestamp = -1;
    private long m_endTimestamp = -1;
    
    private long m_duration = -1; // used for services where there is no start or end time
    
    private int m_id;
    
    private boolean m_success;
    private String m_errorMessage = null;
    
    private static int INC_ID = 0;
    
    public TaskInfo(String taskDescription, String idList) {
        m_taskDescription = taskDescription;
        m_idList = idList;
        
        m_state = STATE_WAITING;
        
        m_id = INC_ID++;
        
        m_success = true;
        m_errorMessage = null;
        
        m_askTimestamp = System.currentTimeMillis();
    }

    public TaskInfo(TaskInfo src) {
        m_taskDescription = src.m_taskDescription;
        m_idList = src.m_idList;
        m_state = src.m_state;
        m_id = src.m_id;
        m_success = src.m_success;
        m_errorMessage = src.m_errorMessage;
        m_askTimestamp = src.m_askTimestamp;
        m_startTimestamp = src.m_startTimestamp;
        m_endTimestamp = src.m_endTimestamp;
        m_duration = src.m_duration;
    }
    
    public void setRunning(boolean saveTimestamp) {
        
        if ((m_state == STATE_RUNNING) || (m_updateState == STATE_RUNNING) || (m_updateState == STATE_FINISHED)) {
            // already running (or service finished, so afterwards little modification of database will not be taken in account)
            return;
        }
        
        m_updateState = STATE_RUNNING;
        if (saveTimestamp) {
            m_startTimestamp = System.currentTimeMillis();
        }
        TaskInfoManager.getTaskInfoManager().update(this);
        
    }
    
    public void setFinished(boolean success, String errorMessage, boolean saveTimestamp) {
        if (m_updateState == STATE_FINISHED) {
            // already finished : so afterwards little modification of database will not be taken in account
            saveTimestamp = false;
        }
        m_updateState = STATE_FINISHED;
        if (saveTimestamp) {
            if (m_startTimestamp == -1) {
                // in fact nothing has been done : data was already loaded
                m_endTimestamp = -1;
            } else {
                m_endTimestamp = System.currentTimeMillis();
            }
        }
        m_success = success;
        m_errorMessage = errorMessage;

        TaskInfoManager.getTaskInfoManager().update(this);
    }
    
    public void setDuration(long duration) {
        m_duration = duration;
        TaskInfoManager.getTaskInfoManager().update(this, false);
    }
    
    public void update() {
        if (m_updateState != -1) {
            m_state = m_updateState;
            m_updateState = -1;
        }
    }
    
    public boolean isWaiting() {
        return m_state == STATE_WAITING;
    }
    
    public boolean isRunning() {
        return m_state == STATE_RUNNING;
    }
    
    public boolean isFinished() {
        return m_state == STATE_FINISHED;
    }
    
    public boolean isSuccess() {
        return m_success;
    }
    
    public String getErrorMessage() {
        if (m_errorMessage == null) {
            return "";
        }
        return m_errorMessage;
    }
    
    public boolean hasErrorMessage() {
        return (m_errorMessage != null);
    }
    
    public int getId() {
        return m_id;
    }
    
    public String getIdList() {
        return m_idList;
    }

    
    public String getTaskDescription() {
        return m_taskDescription;
    }
    
    public long getAskTimestamp() {
        return m_askTimestamp;
    }
    public long getStartTimestamp() {
        return m_startTimestamp;
    }
    public long getEndTimestamp() {
        return m_endTimestamp;
    }

    public long getDuration() {
        if (m_duration != -1) {
            return m_duration;
        }
        if ((m_endTimestamp!=-1) && (m_startTimestamp!=-1)) {
            return m_endTimestamp-m_startTimestamp;
        }
        return -1;
    }
    
    public long getDelay() {
        if ((m_startTimestamp!=-1) && (m_askTimestamp!=-1)) {
            return m_startTimestamp-m_askTimestamp;
        }
        return -1;
    }
    


    @Override
    public int compareTo(TaskInfo o) {
        int cmp = m_state-o.m_state;
        if (cmp != 0) {
           return cmp; 
        }
        return o.m_id-m_id;
        
    }
    
    public void copyData(TaskInfo dest) {
        dest.m_taskDescription = m_taskDescription;
        dest.m_idList = m_idList;
        dest.m_state = m_state;
        dest.m_id = m_id;
        dest.m_success = m_success;
        dest.m_errorMessage = m_errorMessage;
        dest.m_askTimestamp = m_askTimestamp;
        dest.m_startTimestamp = m_startTimestamp;
        dest.m_endTimestamp = m_endTimestamp;
        dest.m_duration = m_duration;
    }
    
    @Override
    public String toString() {
        return "";
    }
    

}
