package fr.proline.studio.dam.taskinfo;

/**
 * Class contains informations about a Task (description, timestamps and status)
 * @author JM235353
 */
public class TaskInfo implements Comparable<TaskInfo> {
    
    public final static int STATE_WAITING = 0;
    public final static int STATE_RUNNING = 1;
    public final static int STATE_FINISHED = 2;
    public final static int STATE_ABORTED = 3;


    private String m_taskDescription = null;
    private String m_idList = null;
    
    private int m_state;
    private int m_updateState = -1;
    
    private long m_askTimestamp = -1;
    private long m_startTimestamp = -1;
    private long m_endTimestamp = -1;
    
    private float m_percentage = 0;
    
    
    private long m_duration = -1; // used for services where there is no start or end time
    
    private int m_id;
    
    private boolean m_success;
    private TaskError m_taskError = null;
    
    private static int INC_ID = 0;
    
    public TaskInfo(String taskDescription, String idList) {
        m_taskDescription = taskDescription;
        m_idList = idList;
        
        m_state = STATE_WAITING;
        
        m_id = INC_ID++;
        
        m_success = true;
        m_taskError = null;
        
        m_askTimestamp = System.currentTimeMillis();
    }

    public TaskInfo(TaskInfo src) {
        m_taskDescription = src.m_taskDescription;
        m_idList = src.m_idList;
        m_state = src.m_state;
        m_id = src.m_id;
        m_success = src.m_success;
        m_taskError = src.m_taskError;
        m_askTimestamp = src.m_askTimestamp;
        m_startTimestamp = src.m_startTimestamp;
        m_endTimestamp = src.m_endTimestamp;
        m_duration = src.m_duration;
        m_percentage = src.m_percentage;
    }
    
    public void setRunning(boolean saveTimestamp) {
        
        if ((m_state == STATE_RUNNING) || (m_updateState == STATE_RUNNING) || (m_updateState == STATE_FINISHED) || (m_updateState == STATE_ABORTED)) {
            // already running (or service finished, so afterwards little modification of database will not be taken in account)
            return;
        }
        
        m_updateState = STATE_RUNNING;
        if (saveTimestamp) {
            m_startTimestamp = System.currentTimeMillis();
        }
        TaskInfoManager.getTaskInfoManager().update(this);
        
    }
    
    public void setPercentage(float percentage) {
        m_percentage = percentage;
        TaskInfoManager.getTaskInfoManager().update(this, false);
    }
    
    public float getPercentage() {
        return m_percentage;
    }
    
    public void setAborted() {
        m_updateState = STATE_ABORTED;
        m_percentage = 0;
        TaskInfoManager.getTaskInfoManager().update(this);
    }
    
    public void setFinished(boolean success, TaskError taskError, boolean saveTimestamp) {
        
        if (m_updateState == STATE_ABORTED) {
            return;
        }
        
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
        m_percentage = 100;
        m_success = success;
        m_taskError = taskError;

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
    
    public boolean isAborted() {
        return m_state == STATE_ABORTED;
    }
    
    public boolean isSuccess() {
        return m_success;
    }
    
    public TaskError getTaskError() {

        return m_taskError;
    }
    
    public boolean hasTaskError() {
        return (m_taskError != null);
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
        
        // STATE_ABORTED and STATE_FINISHED are put at the same level for the sorting
        int state = m_state;
        if (state == STATE_ABORTED) {
            state = STATE_FINISHED;
        }
        
        int o_state = o.m_state;
        if (o_state == STATE_ABORTED) {
            o_state = STATE_FINISHED;
        }
        
        int cmp = state-o_state;
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
        dest.m_taskError = m_taskError;
        dest.m_askTimestamp = m_askTimestamp;
        dest.m_startTimestamp = m_startTimestamp;
        dest.m_endTimestamp = m_endTimestamp;
        dest.m_duration = m_duration;
        dest.m_percentage = m_percentage;
    }
    
    @Override
    public String toString() {
        return "";
    }
    

}
