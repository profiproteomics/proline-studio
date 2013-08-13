package fr.proline.studio.dam.tasks;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.PriorityQueue;

/**
 *
 * Manage the SubTasks of an AbstractDatabaseSlicerTask
 *
 * @author JM235353
 */
public class SubTaskManager {

    // Priority Queue for Sub Tasks
    private PriorityQueue<SubTask> m_subTasks = new PriorityQueue<>();
    // We keep a count of remaining SubTasks of each type
    private int[] m_subTaskCount;
    // Current Task
    private SubTask m_currentTask = null;

    private int m_maxSlicesNumber;
    
    public SubTaskManager(int nbDifferentSubTaskTypes) {
        m_subTaskCount = new int[nbDifferentSubTaskTypes];
    }

    public synchronized void deleteThis() {
        m_subTasks.clear();
    }
    
    public synchronized boolean isEmpty() {
        return m_subTasks.isEmpty();
    }

    public synchronized SubTask getCurrentTask() {
        return m_currentTask;
    }

    public synchronized void setCurrentTask(SubTask currentTask) {
        m_currentTask = currentTask;
    }

    public synchronized SubTask sliceATaskAndGetFirst(int subTaskId, int arrayLength, int range) {

        SubTask firstTask = null;

        int i = 0;
        while (i < arrayLength) {
            int indexStart = i;
            int indexEnd = i + range - 1;
            if (indexEnd >= arrayLength) {
                indexEnd = arrayLength - 1;
            }

            if (i == 0) {
                firstTask = new SubTask(subTaskId, indexStart, indexEnd);
            } else {
                m_subTasks.offer(new SubTask(subTaskId, indexStart, indexEnd));
                m_subTaskCount[subTaskId]++;
            }

            i = indexEnd + 1;
        }

        m_maxSlicesNumber = m_subTasks.size();
        
        return firstTask;
    }

    public synchronized float getAccomplishedPercentage() {
        int nbTask = m_subTasks.size();
        return 100.0f-100.0f*(((float)nbTask)/m_maxSlicesNumber);
    }
    
    /**
     * Get The next SubTask to be done
     *
     * @return
     */
    public synchronized SubTask getNextSubTask() {
        if (m_subTasks.size() == 0) {
            m_currentTask = null;
        } else {
            m_currentTask = m_subTasks.poll();
            m_subTaskCount[m_currentTask.getSubTaskId()]--;
        }
        return m_currentTask;

    }

    /**
     * Give priority to a type of SubTask or a range of indexes if the subTaskId
     * is given, it is considered as to be done first
     *
     * @param subTaskId
     * @param indexStart
     * @param indexEnd
     */
    public synchronized void givePriorityTo(int subTaskId, int indexStart, int indexEnd) {

        if ((subTaskId != -1) && m_subTaskCount[subTaskId] > 0) {
            // priority is given first to a subTask type
            Iterator<SubTask> it = m_subTasks.iterator();
            while (it.hasNext()) {
                SubTask st = it.next();
                boolean highPriority = (st.getSubTaskId() == subTaskId);
                if ((highPriority && !st.isHighPriority()) || (!highPriority && st.isHighPriority())) {
                    subTaskTmp.add(st);
                }
            }
        } else {
            // priority is given to the range of indexes
            Iterator<SubTask> it = m_subTasks.iterator();
            while (it.hasNext()) {
                SubTask st = it.next();
                boolean highPriority = st.hasCommonIndexes(indexStart, indexEnd);
                if ((highPriority && !st.isHighPriority()) || (!highPriority && st.isHighPriority())) {
                    subTaskTmp.add(st);
                }
            }
        }



        int nb = subTaskTmp.size();
        if (nb > 0) {
            for (int i = 0; i < nb; i++) {
                m_subTasks.remove(subTaskTmp.get(i));
            }
            for (int i = 0; i < nb; i++) {
                SubTask task = subTaskTmp.get(i);
                task.setHighPriority(!task.isHighPriority());
                m_subTasks.offer(task);
            }


            subTaskTmp.clear();
        }

    }

    /**
     * Reset Priority of all remaining SubTasks
     */
    public synchronized void resetPriority() {

        Iterator<SubTask> it = m_subTasks.iterator();
        while (it.hasNext()) {
            SubTask sb = it.next();
            if (sb.isHighPriority()) {
                subTaskTmp.add(sb);
            }
        }

        int nb = subTaskTmp.size();
        if (nb > 0) {
            for (int i = 0; i < nb; i++) {
                m_subTasks.remove(subTaskTmp.get(i));
            }
            for (int i = 0; i < nb; i++) {
                SubTask task = subTaskTmp.get(i);
                task.setHighPriority(false);
                m_subTasks.offer(task);
            }

            subTaskTmp.clear();
        }
    }
    private ArrayList<SubTask> subTaskTmp = new ArrayList<>();

}
