/* 
 * Copyright (C) 2019 VD225637
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1 
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License 
 * along with this program; If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */
package fr.proline.studio.dam;

import fr.proline.studio.dam.tasks.AbstractDatabaseTask;
import org.slf4j.LoggerFactory;

/**
 * Thread which executes a task
 * @author JM235353
 */
public class AccessDatabaseWorkerThread extends Thread {
    
    private AbstractDatabaseTask m_action = null;
    
    private static int m_threadCounter = 0;
    
    private AccessDatabaseWorkerPool m_workerPool = null;
    
    public AccessDatabaseWorkerThread(AccessDatabaseWorkerPool workerPool) {
        super("AccessDatabaseWorkerThread"+m_threadCounter);
        m_threadCounter++;
        
        m_workerPool = workerPool;

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
