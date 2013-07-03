package fr.proline.studio.dam.tasks;


import fr.proline.studio.dam.taskinfo.TaskInfo;
import javax.swing.SwingUtilities;

/**
 *
 * Extend this class to write a Task which can be sliced in SubTasks
 * 
 * @author JM235353
 */
public abstract class AbstractDatabaseSlicerTask extends AbstractDatabaseTask {
    
    // Manager of the subtasks
    protected SubTaskManager subTaskManager;
    
    public AbstractDatabaseSlicerTask(AbstractDatabaseCallback callback) {
        super(callback, null);
    }
    
    public AbstractDatabaseSlicerTask(AbstractDatabaseCallback callback, int subTaskCount, TaskInfo taskInfo) {
        super(callback, taskInfo);
        subTaskManager = new SubTaskManager(subTaskCount);
    }
    public AbstractDatabaseSlicerTask(AbstractDatabaseCallback callback, int subTaskCount, Priority priority, TaskInfo taskInfo) {
        super(callback, priority, taskInfo);
        subTaskManager = new SubTaskManager(subTaskCount);
    }
    
    public void init(int subTaskCount, TaskInfo taskInfo) {
        setTaskInfo(taskInfo);
        subTaskManager = new SubTaskManager(subTaskCount);
    }
    
    /**
     * Return if there are remaining Sub Tasks to be done
     * @return 
     */
    @Override
    public boolean hasSubTasksToBeDone() {
        return !subTaskManager.isEmpty();
    }
    

    @Override
    public void applyPriorityChangement(PriorityChangement priorityChangement) {
        super.applyPriorityChangement(priorityChangement);
        
        subTaskManager.givePriorityTo(priorityChangement.getSubTaskId(), priorityChangement.getStartIndex(), priorityChangement.getStopIndex());
        
    }

    
    @Override
    public void resetPriority() {
        currentPriority = defaultPriority;
        subTaskManager.resetPriority();
    }
    
    
    /**
     * Method called after the data has been fetched
     * @param success  boolean indicating if the fetch has succeeded
     */
    @Override
    public void callback(final boolean success, final boolean finished) {
        if (callback == null) {
            return;
        }

        final SubTask taskDone = subTaskManager.getCurrentTask();
        /*if (taskDone != null) {
            taskDone.setAllSubtaskFinished(!hasSubTasksToBeDone());
        }*/
        
        if (callback.mustBeCalledInAWT()) {
            // Callback must be executed in the Graphical thread (AWT)
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    callback.run(success, id, taskDone, finished);
                }
            });
        } else {
            // Method called in the current thread
            // In this case, we assume the execution is fast.
            callback.run(success, id, taskDone, finished);
        }


    }
    
}
