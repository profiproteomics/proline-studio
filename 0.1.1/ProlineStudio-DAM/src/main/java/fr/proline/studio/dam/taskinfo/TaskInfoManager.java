package fr.proline.studio.dam.taskinfo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;

/**
 *
 * @author JM235353
 */
public class TaskInfoManager {
    
    private static final int MAX_SIZE = 1000;
    
    private TreeSet<TaskInfo> m_tasks;
    private ArrayList<TaskInfo> m_taskToBeUpdated;
    
    private static TaskInfoManager m_singleton = null;
    
    private int m_lastUpdate = -1;
    private int m_curUpdate = 0;
    
    public static TaskInfoManager getTaskInfoManager() {
        if (m_singleton == null) {
            m_singleton = new TaskInfoManager();
        }
        return m_singleton;
    }
    
    private TaskInfoManager() {
        m_tasks = new  TreeSet<>();
        m_taskToBeUpdated = new ArrayList<>();
    }
    
    public void add(TaskInfo taskInfo) {
        m_tasks.add(taskInfo);
        if (m_tasks.size()>MAX_SIZE) {
            m_tasks.remove(m_tasks.last());
        }
        m_curUpdate++;
    }
    
    public void cancel(TaskInfo taskInfo) {
        m_tasks.remove(taskInfo);
        m_curUpdate++;
    }
    
    public synchronized void update(TaskInfo taskInfo) {
        m_taskToBeUpdated.add(taskInfo);
        m_curUpdate++;
    }
    
    public synchronized void update(TaskInfo taskInfo, boolean changeSorting) {
        if (changeSorting) {
            update(taskInfo);
        } else {
            m_curUpdate++;
        }
    }
    
    private synchronized void updateAll() {

        int nb = m_taskToBeUpdated.size();
        for (int i=0;i<nb;i++) {
            TaskInfo info = m_taskToBeUpdated.get(i);
            m_tasks.remove(info);
            info.update();
            m_tasks.add(info);
        }
        m_taskToBeUpdated.clear();

    }
    
    public synchronized boolean isUpdateNeeded() {
        return (m_curUpdate != m_lastUpdate);
    }
    
    public synchronized boolean copyData(ArrayList<TaskInfo> destinationList) {
        
        if ((m_curUpdate == m_lastUpdate) && (!destinationList.isEmpty())) {
            return false;
        }
        m_lastUpdate = m_curUpdate;
        
        updateAll();
        
        int sizeDestination = destinationList.size();
        int index = 0;
        Iterator<TaskInfo> it = m_tasks.iterator(); 
        while (it.hasNext()) {
            TaskInfo infoCur = it.next();
            
            if (index<sizeDestination) {
                infoCur.copyData(destinationList.get(index));
            } else {
                destinationList.add(new TaskInfo(infoCur));
            }
            index++;
        }
        
        return true;
    }
    
    
}
