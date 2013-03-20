package fr.proline.studio.dam.taskinfo;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author JM235353
 */
public class TaskInfoThread extends Thread {
    
    private static final int UPDATE_RATE = 1000; // one second
    
    private long lastUpdate = 0;
    private boolean needUpdate = false;
    
    private ArrayList<TaskInfoListenerInterface> listeners = new ArrayList<>();
    
    private HashMap<String, ArrayList<TaskInfoGroup>> taskInfoGroupMap = new HashMap<>();
    

    private static TaskInfoThread m_singleton = null;
    
    public static synchronized TaskInfoThread getTaskInfoManager() {
        if (m_singleton == null) {
            m_singleton = new TaskInfoThread();
            m_singleton.start();
        }
        return m_singleton;
    }
    
    private TaskInfoThread() {
        
    }
    
    @Override
    public void run() {
        
        try {
        
            while (true) {
                long timeToWait = 0;
                synchronized (this) {
                    if (needUpdate) {
                        long current = System.currentTimeMillis();
                        timeToWait = current - (lastUpdate + UPDATE_RATE);
                    } else {
                        wait();
                        notifyAll();
                    }
                }
                if (timeToWait > 0) {
                    Thread.sleep(timeToWait);
                }

                needUpdate = false;
                
                synchronized (this) {
                    
                }
                
            }
        } catch (Throwable t) {
            
        }
    }

    
    public synchronized void needUpdate() {
        needUpdate = true;
        notifyAll();
    }

    public synchronized void addTaskInfo(TaskInfo taskInfo) {
        
        //taskInfo. //JPM.TODO
        
        needUpdate = true;
        notifyAll();
    }

    public synchronized void addListener(TaskInfoListenerInterface listener) {
        listeners.add(listener);
        needUpdate = true;
        notifyAll();
    }

    public synchronized void removeListener(TaskInfoListenerInterface listener) {
        listeners.remove(listener);
    }

    
    private class TaskInfoGroup {
    
        private ArrayList<TaskInfo> finishedTask;
        private ArrayList<TaskInfo> waitingTask;
        
        public TaskInfoGroup() {
            finishedTask = new ArrayList<TaskInfo>();
            waitingTask = new ArrayList<TaskInfo>();
        }
        
    }
    
}
