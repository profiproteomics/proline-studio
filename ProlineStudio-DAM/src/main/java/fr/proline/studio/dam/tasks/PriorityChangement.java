package fr.proline.studio.dam.tasks;

/**
 * 
 * Manage priority changement, by collecting an index range which
 * needs to be done in priority, and/or a SubTask type which needs 
 * to be done in priority.
 * A SubTask priority is treated before an index range priority.
 * 
 * @author JM235353
 */

public class PriorityChangement {

    private AbstractDatabaseTask m_task;
    private int m_startIndex = -1;
    private int m_stopIndex = -1;
    private int m_subTaskId = -1;

    public PriorityChangement(AbstractDatabaseTask task) {
        m_task = task;
    }

    public void setIndex(int index) {
        m_startIndex = index;
        m_stopIndex = index;
    }

    public void setSubTask(int subTaskId) {
        m_subTaskId = subTaskId;
    }

    public AbstractDatabaseTask getTask() {
        return m_task;
    }

    public void addIndex(int index) {
        if (m_startIndex > index) {
            m_startIndex = index;
        } else if (m_stopIndex < index) {
            m_stopIndex = index;
        }

    }

    public void clearIndex() {
        m_startIndex = -1;
        m_stopIndex  = -1;
    }
    public int getStartIndex() {
        return m_startIndex;
    }

    public int getStopIndex() {
        return m_stopIndex;
    }
    
    public int getSubTaskId() {
        return m_subTaskId;
    }
}