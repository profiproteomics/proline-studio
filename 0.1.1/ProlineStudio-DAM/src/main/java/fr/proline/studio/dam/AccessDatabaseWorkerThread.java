/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.dam;

import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.dam.taskinfo.TaskInfoManager;
import fr.proline.studio.dam.tasks.AbstractDatabaseTask;
import org.slf4j.LoggerFactory;

/**
 *
 * @author JM235353
 */
public class AccessDatabaseWorkerThread extends Thread {
    
    private AbstractDatabaseTask m_action = null;
    
    private AccessDatabaseWorkerPool m_workerPool = null;
    
    public AccessDatabaseWorkerThread(AccessDatabaseWorkerPool workerPool) {
        m_workerPool = workerPool;
        
        setName("AccessDatabaseWorkerThread"); // useful for debugging
    }
    
    public synchronized boolean isAvailable() {
        return (m_action == null);
    }
    
    public synchronized void setAction(AbstractDatabaseTask action) {

        m_action = action;
        notifyAll();

    }
    
    @Override
    public void run() {
        try {
            while (true) {
                
                AbstractDatabaseTask action = null;
                
                synchronized (this) {

                    while (true) {

                        if (m_action != null) {
                            action = m_action;
                            break;
                        }
                        wait();
                    }
                    notifyAll();
                }

                action.getTaskInfo().setRunning(true);

                
                // fetch data
                boolean success = action.fetchData();

                // call callback code (if there is not a consecutive task)
                action.callback(success, !action.hasSubTasksToBeDone());

                
                AccessDatabaseThread.getAccessDatabaseThread().actionDone(action);

                synchronized(this) {
                    m_action = null;
                }

                m_workerPool.threadFinished();
            }


        } catch (Throwable t) {
            LoggerFactory.getLogger("ProlineStudio.DAM").debug("Unexpected exception in main loop of AccessDatabaseWorkerThread", t);
        }

    }
    
}
