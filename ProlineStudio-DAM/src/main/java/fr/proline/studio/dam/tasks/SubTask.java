package fr.proline.studio.dam.tasks;

import java.util.List;

/**
 * SubTask of an AbstractDatabaseSlicerTask. It corresponds to a range of index
 * used in a Select Query of type subTaskId
 *
 * @author JM235353
 */
public class SubTask implements Comparable<SubTask> {

    private int subTaskId;
    private int startIndex;
    private int stopIndex;
    private boolean highPriority = false;
    //private boolean allSubtaskFinished = false;

    public SubTask(int subTaskId, int startIndex, int stopIndex) {
        this.subTaskId = subTaskId;
        this.startIndex = startIndex;
        this.stopIndex = stopIndex;
    }

    public List getSubList(List l) {
        if ((startIndex == 0) && (stopIndex == (l.size() - 1))) {
            return l;
        }
        return l.subList(startIndex, stopIndex + 1);
    }

    public int getSubTaskId() {
        return subTaskId;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public int getStopIndex() {
        return stopIndex;
    }

    public boolean hasIndex(int index) {
        return ((index >= startIndex) && (index <= stopIndex));
    }

    public boolean hasCommonIndexes(int start, int stop) {
        return (hasIndex(start) || hasIndex(stop));
    }

    public boolean isHighPriority() {
        return highPriority;
    }

    public void setHighPriority(boolean highPriority) {
        this.highPriority = highPriority;
    }

    /*public boolean isAllSubtaskFinished() {
        return allSubtaskFinished;
    }*/

    /*public void setAllSubtaskFinished(boolean allSubtaskFinished) {
        this.allSubtaskFinished = allSubtaskFinished;
    }*/
    
    @Override
    public int compareTo(SubTask o) {
        if (highPriority && !o.highPriority) {
            return -1;
        } else if (!highPriority && o.highPriority) {
            return 1;
        }

        int diffIndex = (startIndex - o.startIndex); // low index have a higher priority than big index
        if (diffIndex != 0) {
            return diffIndex;
        }

        return subTaskId - o.subTaskId; // low subTaskId have a higher priority than big subTaskId
    }
}