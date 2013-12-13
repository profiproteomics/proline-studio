package fr.proline.studio.dam.taskinfo;

/**
 * Base class for Tasks and Services
 * @author JM235353
 */
public abstract class AbstractLongTask {
    
    protected TaskInfo m_taskInfo = null;

    public AbstractLongTask(TaskInfo taskInfo) {
        m_taskInfo = taskInfo;
    }

    public void setTaskInfo(TaskInfo taskInfo) {
        m_taskInfo = taskInfo;
    }
    
    public TaskInfo getTaskInfo() {
        return m_taskInfo;
    }
}
