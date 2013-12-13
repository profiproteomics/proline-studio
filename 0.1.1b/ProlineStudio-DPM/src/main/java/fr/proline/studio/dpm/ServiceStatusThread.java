package fr.proline.studio.dpm;

import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dpm.task.AbstractServiceTask;
import java.util.LinkedList;
import org.openide.awt.StatusDisplayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thread used to check the state of the running services thanks to a polling evert ten seconds
 * @author jm235353
 */
public class ServiceStatusThread extends Thread {
    
    private final static int DELAY_BETWEEN_POLLING = 10000;
    
    private TaskInfo m_currentTaskInfo = null;
    
    private static ServiceStatusThread m_instance;
    
    private LinkedList<AbstractServiceTask> m_taskList = new LinkedList<>();
    
    public static ServiceStatusThread getServiceStatusThread() {
        if (m_instance == null) {
            m_instance = new ServiceStatusThread();
            m_instance.start();
        }
        return m_instance;
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
                
                m_currentTaskInfo = null;
                updateStatusDisplay();
                
                Thread.sleep(DELAY_BETWEEN_POLLING);
                
                AbstractServiceTask task = null;
                synchronized (this) {

                    if (!m_taskList.isEmpty()) {
                        task = m_taskList.poll();
                        m_currentTaskInfo = task.getTaskInfo();
                        updateStatusDisplay();
                    } else {
                        // taskList is empty, we stop to do polling
                        m_currentTaskInfo = null;
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
            m_instance = null; // reset thread
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
            m_taskList.add(task);
            
            // wake up the thread if it is waiting
            notifyAll();
        }
        
        updateStatusDisplay();
    }
    
    public void updateStatusDisplay() {

        synchronized (this) {
            int taskListSize = m_taskList.size();
            TaskInfo taskInfoToDisplay = null;
            if (m_currentTaskInfo !=null) {
                taskListSize++;
                taskInfoToDisplay = m_currentTaskInfo;
            } else if (taskListSize>0) {
                taskInfoToDisplay = m_taskList.peek().getTaskInfo();
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
