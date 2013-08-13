package fr.proline.studio.dam.tasks;

import fr.proline.studio.dam.taskinfo.AbstractLongTask;
import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import javax.swing.SwingUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to be extended to access to the database through the
 * AccessDatabaseThread
 *
 * @author JM235353
 */
public abstract class AbstractDatabaseTask extends AbstractLongTask implements Comparable<AbstractDatabaseTask> {

    // Different possible priorities of a Task
    public enum Priority {

        LOW, // for batch actions
        NORMAL_1, // most of actions
        NORMAL_2, // most of actions, but priority higher
        NORMAL_3, // most of actions, but priority higher
        HIGH_1, // actions to be done fast
        HIGH_2, // actions to be done fast, but priority higher
        HIGH_3, // actions to be done fast, but priority higher
        TOP        // action which needs to be done first
    };
    protected static final Logger m_logger = LoggerFactory.getLogger("ProlineStudio.DAM.Task");
    // callback is called by the AccessDatabaseThread when the data is fetched
    protected AbstractDatabaseCallback m_callback;
    // default priority of the task
    protected Priority m_defaultPriority;
    // current priority of the task
    // can be higer than defaultPriority if a task
    // must be done fast because the user waits for it
    protected Priority m_currentPriority;
    // id of the action
    protected Long m_id;
    private static long m_idIncrement = 0;

    protected TaskError m_taskError = null;
    protected int m_errorId = -1;
    
    protected AbstractDatabaseTask m_consecutiveTask = null;
    
    public final static String TASK_LIST_INFO = "Database Access";
    
    /**
     * Contructor
     *
     * @param callback called by the AccessDatabaseThread when the data is
     * fetched
     */
    public AbstractDatabaseTask(AbstractDatabaseCallback callback, TaskInfo taskInfo) {
        this(callback, Priority.NORMAL_1, taskInfo);
    }

    public AbstractDatabaseTask(AbstractDatabaseCallback callback, Priority priority, TaskInfo taskInfo) {
        super(taskInfo);
        m_callback = callback;
        m_defaultPriority = priority;
        m_currentPriority = priority;

        m_idIncrement++;
        if (m_idIncrement == Long.MAX_VALUE) {
            m_idIncrement = 0;
        }
        m_id = m_idIncrement;

    }

    public void updatePercentage() {
        // nothing to do
    }
    
    /**
     * called when a task is aborted.
     * It is important in the case of tasks with subtask
     * to clean the data loaded (and not let partially loaded data)
     */
    public void abortTask() {
        getTaskInfo().setAborted();
    }
    
    
    public void deleteThis() {
        m_callback = null;
        if (m_consecutiveTask != null) {
            m_consecutiveTask.deleteThis();
            m_consecutiveTask = null;
        }
    }
    
    public void setConsecutiveTask(AbstractDatabaseTask task) {
        m_consecutiveTask = task;
    }
    
    public AbstractDatabaseTask getConsecutiveTask() {
        return m_consecutiveTask;
    }
    
    public boolean hasConsecutiveTask() {
        return (m_consecutiveTask != null);
    }
    
    /**
     * Return the id of the Task
     *
     * @return
     */
    public Long getId() {
        return m_id;
    }

    /**
     * Return the current priority of the Task
     *
     * @return
     */
    public Priority getCurrentPriority() {
        return m_currentPriority;
    }

    public void setPriority(Priority priority) {
        m_defaultPriority = priority;
        m_currentPriority = priority;
    }
    
    
    public void applyPriorityChangement(PriorityChangement priorityChangement) {
        incrementPriority();
    }

    public void resetPriority() {
        m_currentPriority = m_defaultPriority;
    }

    /**
     * increment slightly the priority
     */
    public void incrementPriority() {

        switch (m_defaultPriority) {
            case LOW:
                m_currentPriority = Priority.NORMAL_1;
                break;
            case NORMAL_1:
                m_currentPriority = Priority.NORMAL_2;
                break;
            case NORMAL_2:
                m_currentPriority = Priority.NORMAL_3;
                break;
            case NORMAL_3:
                m_currentPriority = Priority.HIGH_1;
                break;
            case HIGH_1:
                m_currentPriority = Priority.HIGH_2;
                break;
            case HIGH_2:
                m_currentPriority = Priority.HIGH_3;
                break;
            case HIGH_3:
                m_currentPriority = Priority.TOP;
                break;
        }
    }

    /**
     * speed up priority to a higher level
     */
    public void speedUpPriority() {

        switch (m_defaultPriority) {
            case LOW:
                m_currentPriority = Priority.NORMAL_3;
                break;
            case NORMAL_1:
                m_currentPriority = Priority.HIGH_1;
                break;
            case NORMAL_2:
                m_currentPriority = Priority.HIGH_2;
                break;
            case NORMAL_3:
                m_currentPriority = Priority.HIGH_3;
                break;
            case HIGH_1:
            case HIGH_2:
            case HIGH_3:
                m_currentPriority = Priority.TOP;
                break;
        }
    }

    /**
     * Method called by the AccessDatabaseThread to fetch Data from database
     *
     * @return
     */
    public abstract boolean fetchData();

    /**
     * Method called by the AccessDatabaseThread to check if data is or not
     * already known
     *
     * @return
     */
    public abstract boolean needToFetch();

    /**
     * Return if there are sub tasks which remain to be done later
     *
     * @return
     */
    public boolean hasSubTasksToBeDone() {
        return false;
    }

    /**
     * Method called after the data has been fetched
     *
     * @param success boolean indicating if the fetch has succeeded
     */
    public void callback(final boolean success, final boolean finished) {
        if (m_callback == null) {
            
            getTaskInfo().setFinished(success, m_taskError, true);
            
            return;
        }

        m_callback.setErrorMessage(m_taskError, m_errorId);
        
        if (m_callback.mustBeCalledInAWT()) {
            // Callback must be executed in the Graphical thread (AWT)
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    m_callback.run(success, m_id, null, finished);
                    getTaskInfo().setFinished(success, getTaskError(), true);
                }
            });
        } else {
            // Method called in the current thread
            // In this case, we assume the execution is fast.
            m_callback.run(success, m_id, null, finished);
            getTaskInfo().setFinished(success, getTaskError(), true);
        }


    }

    /**
     * Used to prioritize actions
     *
     * @param task
     * @return
     */
    @Override
    public int compareTo(AbstractDatabaseTask task) {

        // first we compare on priority
        long diff = task.m_currentPriority.ordinal() - m_currentPriority.ordinal();
        if (diff != 0) {
            return (diff) > 0 ? 1 : -1;
        }

        // for equal priority, we compare on id : priority is given to older id == smaller
        diff = m_id - task.m_id;
        if (diff == 0) {
            return 0;
        }
        return (diff) > 0 ? 1 : -1;
    }
    
    public TaskError getTaskError() {
        return m_taskError;
    }
}
