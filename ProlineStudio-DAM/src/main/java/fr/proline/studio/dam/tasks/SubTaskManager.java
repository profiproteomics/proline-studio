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
    private PriorityQueue<SubTask> subTasks = new PriorityQueue<>();
    // We keep a count of remaining SubTasks of each type
    private int[] subTaskCount;
    // Current Task
    private SubTask currentTask = null;

    public SubTaskManager(int nbDifferentSubTaskTypes) {
        subTaskCount = new int[nbDifferentSubTaskTypes];
    }

    public synchronized boolean isEmpty() {
        return subTasks.isEmpty();
    }

    public synchronized SubTask getCurrentTask() {
        return currentTask;
    }

    public synchronized void setCurrentTask(SubTask currentTask) {
        this.currentTask = currentTask;
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
                subTasks.offer(new SubTask(subTaskId, indexStart, indexEnd));
                subTaskCount[subTaskId]++;
            }

            i = indexEnd + 1;
        }

        return firstTask;
    }

    /**
     * Get The next SubTask to be done
     *
     * @return
     */
    public synchronized SubTask getNextSubTask() {
        if (subTasks.size() == 0) {
            currentTask = null;
        } else {
            currentTask = subTasks.poll();
            subTaskCount[currentTask.getSubTaskId()]--;
        }
        return currentTask;

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

        if ((subTaskId != -1) && subTaskCount[subTaskId] > 0) {
            // priority is given first to a subTask type
            Iterator<SubTask> it = subTasks.iterator();
            while (it.hasNext()) {
                SubTask st = it.next();
                boolean highPriority = (st.getSubTaskId() == subTaskId);
                if ((highPriority && !st.isHighPriority()) || (!highPriority && st.isHighPriority())) {
                    subTaskTmp.add(st);
                }
            }
        } else {
            // priority is given to the range of indexes
            Iterator<SubTask> it = subTasks.iterator();
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
                subTasks.remove(subTaskTmp.get(i));
            }
            for (int i = 0; i < nb; i++) {
                SubTask task = subTaskTmp.get(i);
                task.setHighPriority(!task.isHighPriority());
                subTasks.offer(task);
            }


            subTaskTmp.clear();
        }

    }

    /**
     * Reset Priority of all remaining SubTasks
     */
    public synchronized void resetPriority() {

        Iterator<SubTask> it = subTasks.iterator();
        while (it.hasNext()) {
            SubTask sb = it.next();
            if (sb.isHighPriority()) {
                subTaskTmp.add(sb);
            }
        }

        int nb = subTaskTmp.size();
        if (nb > 0) {
            for (int i = 0; i < nb; i++) {
                subTasks.remove(subTaskTmp.get(i));
            }
            for (int i = 0; i < nb; i++) {
                SubTask task = subTaskTmp.get(i);
                task.setHighPriority(false);
                subTasks.offer(task);
            }

            subTaskTmp.clear();
        }
    }
    private ArrayList<SubTask> subTaskTmp = new ArrayList<>();

}
