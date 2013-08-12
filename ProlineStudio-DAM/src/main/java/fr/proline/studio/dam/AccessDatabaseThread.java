package fr.proline.studio.dam;

import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dam.tasks.AbstractDatabaseTask;
import fr.proline.studio.dam.tasks.PriorityChangement;
import fr.proline.studio.dam.taskinfo.TaskInfoManager;
import fr.proline.studio.dam.tasks.AbstractDatabaseTask.Priority;
import java.util.ArrayList;
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

    private static AccessDatabaseThread m_instance;
    private PriorityQueue<AbstractDatabaseTask> m_actions;
    private HashMap<Long, AbstractDatabaseTask> m_actionMap;
    private HashMap<Long, PriorityChangement> m_priorityChangements;
    private ArrayList<Long> m_abortedActionIdList;

    private AccessDatabaseWorkerPool m_workerPool = null;
    
    private AccessDatabaseThread() {
        m_actions = new PriorityQueue<>();
        m_actionMap = new HashMap<>();
        m_priorityChangements = new HashMap<>();
        m_abortedActionIdList = new ArrayList<>();
        
        m_workerPool = AccessDatabaseWorkerPool.getWorkerPool();

        setName("AccessDatabaseThread"); // useful for debugging
    }

    public static AccessDatabaseThread getAccessDatabaseThread() {
        if (m_instance == null) {
            m_instance = new AccessDatabaseThread();
            m_instance.start();
        }
        return m_instance;
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

                        // Management of aborted task
                        if (!m_abortedActionIdList.isEmpty()) {
                            int nbAbortedTask = m_abortedActionIdList.size();
                            for (int i=0;i<nbAbortedTask;i++) {
                                Long taskId = m_abortedActionIdList.get(i);
                                AbstractDatabaseTask taskToStop = m_actionMap.remove(taskId);
                                if (taskToStop == null) {
                                    continue;
                                }
                                if (m_actions.contains(taskToStop)) {
                                    m_actions.remove(taskToStop);
                                    
                                    //TaskInfoManager.getTaskInfoManager().cancel(abortedTask.getTaskInfo());
                                    
                                    TaskInfo info = taskToStop.getTaskInfo();
                                    if (info.isWaiting()) {
                                        // task has not already started, cancel it
                                        TaskInfoManager.getTaskInfoManager().cancel(info);
                                    } else {
                                        taskToStop.abortTask();
                                    }

                                    taskToStop.deleteThis();
                                }
                            }
                            m_abortedActionIdList.clear();
                        }
                        
                        // Management of the changement of priorities
                        if (!m_priorityChangements.isEmpty()) {
                            Iterator<Long> it = m_priorityChangements.keySet().iterator();
                            while (it.hasNext()) {
                                Long taskId = it.next();
                                PriorityChangement priorityChangement = m_priorityChangements.get(taskId);
                                AbstractDatabaseTask task = priorityChangement.getTask();

                                if (m_actions.contains(task)) {
                                    m_actions.remove(task);
                                    task.applyPriorityChangement(priorityChangement);
                                    m_actions.offer(task);
                                }
                            }
                        }

                        // look for a task to be done
                        if (!m_actions.isEmpty()) {
                            action = m_actions.poll();
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
            m_instance = null; // reset thread
        }

    }

    public void actionDone(AbstractDatabaseTask task) {
        synchronized (this) {
            // check if subtasks need to be done
            if (task.hasSubTasksToBeDone()) {
                // put back action in the queue for subtasks
                m_actions.add(task);

            } else {
                // action completely finished
                m_actionMap.remove(task.getId());
                m_priorityChangements.remove(task.getId());
                
                TaskError taskError = task.getTaskError();
                task.getTaskInfo().setFinished((taskError==null), taskError, true);
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
            m_actions.add(task);
            m_actionMap.put(task.getId(), task);
            notifyAll();
        }
    }

    public final void abortTask(Long taskId) {
        synchronized (this) {
            abortTaskImpl(taskId);
        }
    }

    public final void abortTasks(ArrayList<Long> taskIds) {
        synchronized (this) {
            int nb = taskIds.size();
            for (int i = 0; i < nb; i++) {
                abortTask(taskIds.get(i));
            }
        }
    }
    
    private void abortTaskImpl(Long taskId) {
        AbstractDatabaseTask task = m_actionMap.get(taskId);
        if (task == null) {
            // task is already finished
            return;
        }
        m_abortedActionIdList.add(taskId);
        
        /*
        boolean isActionRegistered = m_actions.remove(task);
        if (isActionRegistered) {
            // action was not running, remove it from map
            m_actionMap.remove(taskId);
            // remove iis info from TaskInfoManager
            TaskInfoManager.getTaskInfoManager().cancel(task.getTaskInfo());
        }
        if ((!task.getTaskInfo().isFinished()) && (!task.getTaskInfo().isAborted())) {
            task.abortTask();
        }
        
        task.deleteThis();*/
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
            AbstractDatabaseTask task = m_actionMap.get(taskId);
            if (task == null) {
                // task is already finished
                return;
            }
            PriorityChangement priorityChangement = m_priorityChangements.get(taskId);
            if (priorityChangement == null) {
                priorityChangement = new PriorityChangement(task);
                priorityChangement.setIndex(index);
                m_priorityChangements.put(taskId, priorityChangement);
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
            AbstractDatabaseTask task = m_actionMap.get(taskId);
            if (task == null) {
                // task is already finished
                return;
            }
            PriorityChangement priorityChangement = m_priorityChangements.get(taskId);
            if (priorityChangement == null) {
                priorityChangement = new PriorityChangement(task);
                m_priorityChangements.put(taskId, priorityChangement);
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
            AbstractDatabaseTask task = m_actionMap.get(taskId);
            if (task == null) {
                // task is already finished
                return;
            }
            PriorityChangement priorityChangement = m_priorityChangements.get(taskId);
            if (priorityChangement == null) {
                priorityChangement = new PriorityChangement(task);
                m_priorityChangements.put(taskId, priorityChangement);
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

            PriorityChangement priorityChangement = m_priorityChangements.get(taskId);

            if (priorityChangement == null) {
                return; // nothing to do
            }

            if (priorityChangement.getSubTaskId() != -1) {
                // we clear only indexes
                priorityChangement.clearIndex();
            } else {
                m_priorityChangements.remove(taskId);
                AbstractDatabaseTask task = priorityChangement.getTask();
                boolean isActionRegistered = m_actions.remove(task);
                task.resetPriority();
                if (isActionRegistered) {
                    m_actions.offer(task);
                }

            }

        }
    }



}
