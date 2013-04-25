package fr.proline.studio.dpm;

import fr.proline.studio.dpm.task.AbstractServiceTask;
import java.util.LinkedList;
import org.slf4j.LoggerFactory;

/**
 * Thread in charge to ask asynchronously the execution of services to Web-Core
 * @author jm235353
 */
public class AccessServiceThread extends Thread {
    private static AccessServiceThread instance;
    
    private LinkedList<AbstractServiceTask> taskList = new LinkedList<AbstractServiceTask>();
    
    public static AccessServiceThread getAccessServiceThread() {
        if (instance == null) {
            instance = new AccessServiceThread();
            instance.start();
        }
        return instance;
    }
    
    private AccessServiceThread() {
        
    }
    
        /**
     * Main loop of the thread
     */
    @Override
    public void run() {
        try {
            while (true) {
                AbstractServiceTask task = null;
                synchronized (this) {

                    while (true) {

                        // look for a task to be done
                        if (!taskList.isEmpty()) {
                            task = taskList.poll();
                            break;
                        }
                        wait();
                    }
                    notifyAll();
                }

                //Thread.sleep(500);

                //System.out.println("Action : "+action.getClass().toString()+" "+System.currentTimeMillis());

                // fetch data
                boolean success = task.askService();

                if (success) {
                    // we check that the service is done
                    if (task.isSynchronous()) {
                        // call callback code at once
                        task.callback(success);
                    } else {
                        // for an asynchronous task, we will do a polling
                        // to check when it is finished
                        ServiceStatusThread.getServiceStatusThread().addTask(task);
                    }
                    
                } else {
                    // call callback code
                    task.callback(success);
                }


            }


        } catch (Throwable t) {
            LoggerFactory.getLogger("ProlineStudio.DPM").debug("Unexpected exception in main loop of AccessServiceThread", t);
            instance = null; // reset thread
        }

    }
    
     /**
     * Add a task to be done
     *
     * @param task
     */
    public final void addTask(AbstractServiceTask task) {

        // task is queued
        synchronized (this) {
            taskList.add(task);
            notifyAll();
        }
    }
}
