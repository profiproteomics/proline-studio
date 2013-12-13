package fr.proline.studio.dam.tasks;

import java.util.List;

/**
 * SubTask of an AbstractDatabaseSlicerTask. It corresponds to a range of index
 * used in a Select Query of type subTaskId
 *
 * @author JM235353
 */
public class SubTask implements Comparable<SubTask> {

    private int m_subTaskId;
    private int m_startIndex;
    private int m_stopIndex;
    private boolean m_highPriority = false;


    public SubTask(int subTaskId, int startIndex, int stopIndex) {
        m_subTaskId = subTaskId;
        m_startIndex = startIndex;
        m_stopIndex = stopIndex;
    }

    public List getSubList(List l) {
        if ((m_startIndex == 0) && (m_stopIndex == (l.size() - 1))) {
            return l;
        }
        return l.subList(m_startIndex, m_stopIndex + 1);
    }

    public int getSubTaskId() {
        return m_subTaskId;
    }

    public int getStartIndex() {
        return m_startIndex;
    }

    public int getStopIndex() {
        return m_stopIndex;
    }

    public boolean hasIndex(int index) {
        return ((index >= m_startIndex) && (index <= m_stopIndex));
    }

    public boolean hasCommonIndexes(int start, int stop) {
        return (hasIndex(start) || hasIndex(stop));
    }

    public boolean isHighPriority() {
        return m_highPriority;
    }

    public void setHighPriority(boolean highPriority) {
        m_highPriority = highPriority;
    }

    /*public boolean isAllSubtaskFinished() {
        return allSubtaskFinished;
    }*/

    /*public void setAllSubtaskFinished(boolean allSubtaskFinished) {
        allSubtaskFinished = allSubtaskFinished;
    }*/
    
    @Override
    public int compareTo(SubTask o) {
        if (m_highPriority && !o.m_highPriority) {
            return -1;
        } else if (!m_highPriority && o.m_highPriority) {
            return 1;
        }

        int diffIndex = (m_startIndex - o.m_startIndex); // low index have a higher priority than big index
        if (diffIndex != 0) {
            return diffIndex;
        }

        return m_subTaskId - o.m_subTaskId; // low subTaskId have a higher priority than big subTaskId
    }
}