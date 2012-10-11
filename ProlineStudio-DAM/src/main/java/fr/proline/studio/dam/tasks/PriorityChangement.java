/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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

    private AbstractDatabaseTask task;
    private int startIndex = -1;
    private int stopIndex = -1;
    private int subTaskId = -1;

    public PriorityChangement(AbstractDatabaseTask task) {
        this.task = task;
    }

    public void setIndex(int index) {
        this.startIndex = index;
        this.stopIndex = index;
    }

    public void setSubTask(int subTaskId) {
        this.subTaskId = subTaskId;
    }

    public AbstractDatabaseTask getTask() {
        return task;
    }

    public void addIndex(int index) {
        if (startIndex > index) {
            startIndex = index;
        } else if (stopIndex < index) {
            stopIndex = index;
        }

    }

    public void clearIndex() {
        startIndex = -1;
        stopIndex  = -1;
    }
    public int getStartIndex() {
        return startIndex;
    }

    public int getStopIndex() {
        return stopIndex;
    }
    
    public int getSubTaskId() {
        return subTaskId;
    }
}