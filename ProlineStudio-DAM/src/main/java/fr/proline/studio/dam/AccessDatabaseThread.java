package fr.proline.studio.dam;

import fr.proline.studio.dam.tasks.AbstractDatabaseTask;
import fr.proline.studio.dam.tasks.PriorityChangement;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dam.taskinfo.TaskInfoManager;
import fr.proline.studio.dam.tasks.AbstractDatabaseTask.Priority;
import java.util.HashMap;
import java.util.Iterator;
import java.util.PriorityQueue;
import org.slf4j.LoggerFactory;

/**
 * Thread in charge to read asynchronously ORM objects from the database
 * - manage a queue of tasks according to their priorities
 * - manage changements of priorities
 *
 * @author JM235353
 */
public class AccessDatabaseThread extends Thread {

    private static AccessDatabaseThread instance;
    private PriorityQueue<AbstractDatabaseTask> actions;
    private HashMap<Long, AbstractDatabaseTask> actionMap;
    private HashMap<Long, PriorityChangement> priorityChangements;

    private AccessDatabaseWorkerPool m_workerPool = null;
    
    private AccessDatabaseThread() {
        actions = new PriorityQueue<>();
        actionMap = new HashMap<>();
        priorityChangements = new HashMap<>();
        
        m_workerPool = AccessDatabaseWorkerPool.getWorkerPool();

        setName("AccessDatabaseThread"); // useful for debugging
    }

    public static AccessDatabaseThread getAccessDatabaseThread() {
        if (instance == null) {
            instance = new AccessDatabaseThread();
            instance.start();
        }
        return instance;
    }

    /**
     * Main loop of the thread
     */
    @Override
    public void run() {
        try {
            while (true) {
                AbstractDatabaseTask action = null;
                synchronized (this) {

                    while (true) {

                        // Management of the changement of priorities
                        if (!priorityChangements.isEmpty()) {
                            Iterator<Long> it = priorityChangements.keySet().iterator();
                            while (it.hasNext()) {
                                Long taskId = it.next();
                                PriorityChangement priorityChangement = priorityChangements.get(taskId);
                                AbstractDatabaseTask task = priorityChangement.getTask();

                                if (actions.contains(task)) {
                                    actions.remove(task);
                                    task.applyPriorityChangement(priorityChangement);
                                    actions.offer(task);
                                }
                            }
                        }

                        // look for a task to be done
                        if (!actions.isEmpty()) {
                            action = actions.poll();
                            break;
                        }
                        wait();
                    }
                    notifyAll();
                }

                
                
                
                Object workerPoolMutex = m_workerPool.getMutex();
                synchronized (workerPoolMutex) {
                    
                    AccessDatabaseWorkerThread workerThread = null;
                    while (true) {
                        workerThread = m_workerPool.getWorkerThread(action.getCurrentPriority());
                        if (workerThread != null) {
                            break;
                        }
                        workerPoolMutex.wait();
                    }
                    workerThread.setAction(action);
                    
                    workerPoolMutex.notifyAll();
                }

                

            }


        } catch (Throwable t) {
            LoggerFactory.getLogger("ProlineStudio.DAM").debug("Unexpected exception in main loop of AccessDatabaseThread", t);
            instance = null; // reset thread
        }

    }

    public void actionDone(AbstractDatabaseTask task) {
        synchronized (this) {
            // check if subtasks need to be done
            if (task.hasSubTasksToBeDone()) {
                // put back action in the queue for subtasks
                actions.add(task);

            } else {
                // action completely finished
                actionMap.remove(task.getId());
                priorityChangements.remove(task.getId());
                
                String errorMessage = task.getErrorMessage();
                task.getTaskInfo().setFinished((errorMessage==null), errorMessage, true);
            }
            
            if (task.hasConsecutiveTask()) {
                AbstractDatabaseTask consecutiveTask = task.getConsecutiveTask();
                Priority taskPriority = task.getCurrentPriority();
                if (taskPriority.ordinal() > consecutiveTask.getCurrentPriority().ordinal()) {
                    consecutiveTask.setPriority(taskPriority);
                }
                
                addTask(consecutiveTask);
            }
            
            notifyAll();
        }
        
    }
    
    /**
     * Add a task to be done later according to its priority
     *
     * @param action
     */
    public final void addTask(AbstractDatabaseTask task) {

        
        
        // check if we need to fetch data for this action
        if (!task.needToFetch()) {
            // fetch already done : return immediately
            task.callback(true, true);
            
            if (task.hasConsecutiveTask()) {
                AbstractDatabaseTask consecutiveTask = task.getConsecutiveTask();
                Priority taskPriority = task.getCurrentPriority();
                if (taskPriority.ordinal() > consecutiveTask.getCurrentPriority().ordinal()) {
                    consecutiveTask.setPriority(taskPriority);
                }
                
                addTask(consecutiveTask);
            }
            
            return;
        }

        TaskInfoManager.getTaskInfoManager().add(task.getTaskInfo());
        
        // action is queued
        synchronized (this) {
            actions.add(task);
            actionMap.put(task.getId(), task);
            notifyAll();
        }
    }

    public final void removeTask(Long taskId) {
        synchronized (this) {
            AbstractDatabaseTask task = actionMap.get(taskId);
            if (task == null) {
                // task is already finished
                return;
            }
            boolean isActionRegistered = actions.remove(task);
            if (isActionRegistered) {
                // action was not running, remove it from map
                actionMap.remove(taskId);
                // remove iis info from TaskInfoManager
                TaskInfoManager.getTaskInfoManager().cancel(task.getTaskInfo());
            }
        }
    }
    
    
    /**
     * Add an index to be loaded in priority for a given task (index added
     * define a range of indexes to be loaded firstly)
     *
     * @param taskId
     * @param index
     */
    public void addPriorityIndex(Long taskId, int index) {
        synchronized (this) {
            AbstractDatabaseTask task = actionMap.get(taskId);
            if (task == null) {
                // task is already finished
                return;
            }
            PriorityChangement priorityChangement = priorityChangements.get(taskId);
            if (priorityChangement == null) {
                priorityChangement = new PriorityChangement(task);
                priorityChangement.setIndex(index);
                priorityChangements.put(taskId, priorityChangement);
            } else {
                priorityChangement.addIndex(index);
            }


        }
    }

    /**
     * Define an unique index of a task to be loaded in priority
     *
     * @param taskId
     * @param index
     */
    public void setPriorityIndex(Long taskId, int index) {
        synchronized (this) {
            AbstractDatabaseTask task = actionMap.get(taskId);
            if (task == null) {
                // task is already finished
                return;
            }
            PriorityChangement priorityChangement = priorityChangements.get(taskId);
            if (priorityChangement == null) {
                priorityChangement = new PriorityChangement(task);
                priorityChangements.put(taskId, priorityChangement);
            }
            priorityChangement.setIndex(index);



        }
    }

    /**
     * Give priority to a specific subtask of a task. (Index range priority is
     * less important)
     *
     * @param taskId
     * @param subTaskId
     */
    public void givePriorityToSubTask(Long taskId, int subTaskId) {
        synchronized (this) {
            AbstractDatabaseTask task = actionMap.get(taskId);
            if (task == null) {
                // task is already finished
                return;
            }
            PriorityChangement priorityChangement = priorityChangements.get(taskId);
            if (priorityChangement == null) {
                priorityChangement = new PriorityChangement(task);
                priorityChangements.put(taskId, priorityChangement);
            }

            priorityChangement.setSubTask(subTaskId);



        }
    }

    /**
     * Clear the range index priority of a task
     *
     * @param taskId
     */
    public void clearIndexPriorityTo(Long taskId) {
        synchronized (this) {

            PriorityChangement priorityChangement = priorityChangements.get(taskId);

            if (priorityChangement == null) {
                return; // nothing to do
            }

            if (priorityChangement.getSubTaskId() != -1) {
                // we clear only indexes
                priorityChangement.clearIndex();
            } else {
                priorityChangements.remove(taskId);
                AbstractDatabaseTask task = priorityChangement.getTask();
                boolean isActionRegistered = actions.remove(task);
                task.resetPriority();
                if (isActionRegistered) {
                    actions.offer(task);
                }

            }

        }
    }



}
