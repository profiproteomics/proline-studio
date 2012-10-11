package fr.proline.studio.dam;

import fr.proline.studio.dam.tasks.AbstractDatabaseTask;
import fr.proline.repository.DatabaseConnector;
import fr.proline.studio.dam.tasks.CreateDatabaseTestTask;
import fr.proline.studio.dam.tasks.DatabaseConnectionTask;
import fr.proline.studio.dam.tasks.PriorityChangement;
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
    private static PriorityQueue<AbstractDatabaseTask> actions;
    private static HashMap<Long, AbstractDatabaseTask> actionMap;
    private static HashMap<Long, PriorityChangement> priorityChangements;

    private AccessDatabaseThread() {
        actions = new PriorityQueue<AbstractDatabaseTask>();
        actionMap = new HashMap<Long, AbstractDatabaseTask>();
        priorityChangements = new HashMap<Long, PriorityChangement>();

        //JPM.TODO : remove it code for test
        // UDS DB properties
        HashMap<String, String> databaseProperties = new HashMap<String, String>();
        databaseProperties.put(DatabaseConnector.PROPERTY_USERNAME, "dupierris");
        databaseProperties.put(DatabaseConnector.PROPERTY_PASSWORD, "dupierris");
        databaseProperties.put(DatabaseConnector.PROPERTY_DRIVERCLASSNAME, "org.postgresql.Driver");
        databaseProperties.put(DatabaseConnector.PROPERTY_URL, "jdbc:postgresql://gre037784:5433/UDS_db");

        DatabaseConnectionTask connection = new DatabaseConnectionTask(null, databaseProperties, getProjectIdTMP());
        addTask(connection);

        //CreateDatabaseTestTask createDatabase = new CreateDatabaseTestTask(null);
        //addTask(createDatabase);

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

                                actions.remove(task);
                                task.applyPriorityChangement(priorityChangement);
                                actions.offer(task);
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

                //Thread.sleep(500);

                //System.out.println("Action : "+action.getClass().toString()+" "+System.currentTimeMillis());

                // fetch data
                boolean success = action.fetchData();



                // call callback code
                action.callback(success);

                synchronized (this) {
                    // check if subtasks need to be done
                    if (action.hasSubTasksToBeDone()) {
                        // put back action in the queue for subtasks
                        actions.add(action);

                    } else {
                        // action completely finished
                        actionMap.remove(action.getId());
                        priorityChangements.remove(action.getId());
                    }
                }


            }


        } catch (Throwable t) {
            LoggerFactory.getLogger(AccessDatabaseThread.class).debug("Unexpected exception in main loop of AccessDatabaseThread", t);
            instance = null; // reset thread
        }

    }

    /**
     * Add a task to be done later according to its priority
     *
     * @param action
     */
    public final void addTask(AbstractDatabaseTask action) {

        // check if we need to fetch data for this action
        if (!action.needToFetch()) {
            // fetch already done : return immediately
            action.callback(true);
            return;
        }

        // action is queued
        synchronized (this) {
            actions.add(action);
            actionMap.put(action.getId(), action);
            notifyAll();
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
                actions.remove(task);
                task.resetPriority();
                actions.offer(task);
            }

        }
    }

    public static Integer getProjectIdTMP() {
        // JPM.TODO : remove this method
        return projectId;
    }
    private static Integer projectId = new Integer(1);
}
