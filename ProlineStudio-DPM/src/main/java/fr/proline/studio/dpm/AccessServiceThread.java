package fr.proline.studio.dpm;

import fr.proline.studio.dam.taskinfo.TaskInfoManager;
import fr.proline.studio.dpm.task.AbstractServiceTask;
import java.util.LinkedList;
import org.slf4j.LoggerFactory;

/**
 * Thread in charge to ask asynchronously the execution of services to Web-Core
 * @author jm235353
 */
public class AccessServiceThread extends Thread {
    private static AccessServiceThread m_instance;
    
    private LinkedList<AbstractServiceTask> m_taskList = new LinkedList<>();
    
    public static AccessServiceThread getAccessServiceThread() {
        if (m_instance == null) {
            m_instance = new AccessServiceThread();
            m_instance.start();
        }
        return m_instance;
    }
    
    private AccessServiceThread() {
        super("AccessServiceThread"); // useful for debugging
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
                        if (!m_taskList.isEmpty()) {
                            task = m_taskList.poll();
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
            m_instance = null; // reset thread
        }

    }
    
     /**
     * Add a task to be done
     *
     * @param task
     */
    public final void addTask(AbstractServiceTask task) {

        TaskInfoManager.getTaskInfoManager().add(task.getTaskInfo());
        
        // task is queued
        synchronized (this) {
            m_taskList.add(task);
            notifyAll();
        }
    }
}
