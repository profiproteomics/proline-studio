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


import fr.proline.studio.dam.tasks.AbstractDatabaseTask.Priority;

/**
 * Pool of Threads.
 * There are three different thread.
 * The High one can be only used by high priority tasks
 * The Normal one can be used by high priority tasks and Normal Tasks
 * The Low one can be used by all tasks
 * @author JM235353
 */
public class AccessDatabaseWorkerPool {
    
    private static AccessDatabaseWorkerPool m_workerPool = null;
    
    private AccessDatabaseWorkerThread m_highThread = null;
    private AccessDatabaseWorkerThread m_normalThread = null;
    private AccessDatabaseWorkerThread m_lowThread = null;
    
    public synchronized static AccessDatabaseWorkerPool getWorkerPool() {
        if (m_workerPool == null) {
            m_workerPool = new AccessDatabaseWorkerPool();
        }
        return m_workerPool;
    }
    
    private AccessDatabaseWorkerPool() {
        
    }
    
    public Object getMutex() {
        return this;
    }
    
    public synchronized void threadFinished() {
        notifyAll();
    }
    
    public synchronized AccessDatabaseWorkerThread getWorkerThread(Priority priority) {
        
        switch (priority) {
            case TOP: 
            case HIGH_3:
            case HIGH_2:
            case HIGH_1:   
                return getHighThread();
            case NORMAL_3:
            case NORMAL_2:
            case NORMAL_1:   
                return getNormalThread();
            case LOW:
            default:
                return getLowThread();
                
        }

    }
    
    private AccessDatabaseWorkerThread getHighThread() {
        if (m_highThread == null) {
            m_highThread = new AccessDatabaseWorkerThread(this);
            m_highThread.start();
            return m_highThread;
        }
        if (m_highThread.isAvailable()) {
            return m_highThread;
        }
        return getNormalThread();
    }
    
    private AccessDatabaseWorkerThread getNormalThread() {
        if (m_normalThread == null) {
            m_normalThread = new AccessDatabaseWorkerThread(this);
            m_normalThread.start();
            return m_normalThread;
        }
        if (m_normalThread.isAvailable()) {
            return m_normalThread;
        }
        return getLowThread();
    }
    
    private AccessDatabaseWorkerThread getLowThread() {
        if (m_lowThread == null) {
            m_lowThread = new AccessDatabaseWorkerThread(this);
            m_lowThread.start();
            return m_lowThread;
        }
        if (m_lowThread.isAvailable()) {
            return m_lowThread;
        }
        return null;
    }
    
}
