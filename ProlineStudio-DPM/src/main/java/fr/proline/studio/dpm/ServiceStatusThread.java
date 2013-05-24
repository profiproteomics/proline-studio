package fr.proline.studio.dpm;

import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dpm.task.AbstractServiceTask;
import java.util.LinkedList;
import org.openide.awt.StatusDisplayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jm235353
 */
public class ServiceStatusThread extends Thread {
    
    private final static int DELAY_BETWEEN_POLLING = 10000;
    
    private TaskInfo currentTaskInfo = null;
    
    private static ServiceStatusThread instance;
    
    private LinkedList<AbstractServiceTask> taskList = new LinkedList<AbstractServiceTask>();
    
    public static ServiceStatusThread getServiceStatusThread() {
        if (instance == null) {
            instance = new ServiceStatusThread();
            instance.start();
        }
        return instance;
    }
    
    private static Logger logger = LoggerFactory.getLogger("ProlineStudio.DPM");
    
    private ServiceStatusThread() {
        
    }
    
    /**
     * Main loop of the thread
     */
    @Override
    public void run() {
        try {
            while (true) {
                
                currentTaskInfo = null;
                updateStatusDisplay();
                
                Thread.sleep(DELAY_BETWEEN_POLLING);
                
                AbstractServiceTask task = null;
                synchronized (this) {

                    if (!taskList.isEmpty()) {
                        task = taskList.poll();
                        currentTaskInfo = task.getTaskInfo();
                        updateStatusDisplay();
                    } else {
                        // taskList is empty, we stop to do polling
                        currentTaskInfo = null;
                        updateStatusDisplay();
                        wait();
                    }
                    notifyAll();
                }
                
                if (task == null) {
                    continue;
                }
                
                AbstractServiceTask.ServiceState serviceState = task.getServiceState();
                switch(serviceState) {
                    case STATE_FAILED:
                        task.callback(false);
                        break;
                    case STATE_WAITING:
                        // put back the task
                        addTask(task);
                        break;
                    case STATE_DONE:
                        task.callback(true);
                        break;
                }

            }


        } catch (Throwable t) {
            logger.debug("Unexpected exception in main loop of AccessServiceThread", t);
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
            
            // wake up the thread if it is waiting
            notifyAll();
        }
        
        updateStatusDisplay();
    }
    
    public void updateStatusDisplay() {

        synchronized (this) {
            int taskListSize = taskList.size();
            TaskInfo taskInfoToDisplay = null;
            if (currentTaskInfo !=null) {
                taskListSize++;
                taskInfoToDisplay = currentTaskInfo;
            } else if (taskListSize>0) {
                taskInfoToDisplay = taskList.peek().getTaskInfo();
            }
            String status;
            if (taskInfoToDisplay != null) {
                status = taskListSize+" service(s) running. Awaiting "+taskInfoToDisplay.getTaskDescription()+".";
            } else {
                status = "";
            }
            StatusDisplayer.getDefault().setStatusText(status, StatusDisplayer.IMPORTANCE_ANNOTATION);
        }
    }
    
}
