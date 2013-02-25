/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.proline.studio.dam.taskinfo;

/**
 *
 * @author JM235353
 */
public class TaskInfo {
    
        
    protected String taskName = null;
    protected String taskDescription = null;
    protected String idList = null;
    
    
    
    public TaskInfo(String taskName, String taskDescription, String idList) {
        this.taskName = taskName;
        this.taskDescription = taskDescription;
    }

    public String getIdList() {
        return idList;
    }
    
    public String getTaskName() {
        return taskName;
    }
    
    public String getTaskDescription() {
        return taskDescription;
    }
}
