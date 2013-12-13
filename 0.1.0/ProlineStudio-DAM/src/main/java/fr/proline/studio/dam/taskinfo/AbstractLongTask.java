/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.dam.taskinfo;

/**
 *
 * @author JM235353
 */
public abstract class AbstractLongTask {
    
    protected TaskInfo taskInfo = null;

    public AbstractLongTask(TaskInfo taskInfo) {
        this.taskInfo = taskInfo;
    }

    public void setTaskInfo(TaskInfo taskInfo) {
        this.taskInfo = taskInfo;
    }
    
    public TaskInfo getTaskInfo() {
        return taskInfo;
    }
}
