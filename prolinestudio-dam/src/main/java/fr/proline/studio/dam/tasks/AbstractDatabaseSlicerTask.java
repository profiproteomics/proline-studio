/* 
 * Copyright (C) 2019 VD225637
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1 
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License 
 * along with this program; If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */
package fr.proline.studio.dam.tasks;


import fr.proline.studio.dam.taskinfo.TaskInfo;
import javax.swing.SwingUtilities;

/**
 *
 * Extend this class to write a Task which can be sliced in SubTasks
 * 
 * @author JM235353
 */
public abstract class AbstractDatabaseSlicerTask extends AbstractDatabaseTask {
    
    // Manager of the subtasks
    protected SubTaskManager m_subTaskManager;
    
    public AbstractDatabaseSlicerTask(AbstractDatabaseCallback callback) {
        super(callback, null);
    }
    
    public AbstractDatabaseSlicerTask(AbstractDatabaseCallback callback, int subTaskCount, TaskInfo taskInfo) {
        super(callback, taskInfo);
        m_subTaskManager = new SubTaskManager(subTaskCount);
    }
    public AbstractDatabaseSlicerTask(AbstractDatabaseCallback callback, int subTaskCount, Priority priority, TaskInfo taskInfo) {
        super(callback, priority, taskInfo);
        m_subTaskManager = new SubTaskManager(subTaskCount);
    }
    
    public void init(int subTaskCount, TaskInfo taskInfo) {
        setTaskInfo(taskInfo);
        m_subTaskManager = new SubTaskManager(subTaskCount);
    }
    
    @Override
    public void updatePercentage() {
        float percentage = m_subTaskManager.getAccomplishedPercentage();
        m_taskInfo.setPercentage(percentage);
    }
    
    
    @Override
    public void deleteThis() {
        super.deleteThis();
        m_subTaskManager.deleteThis();
    }
    
    /**
     * Return if there are remaining Sub Tasks to be done
     * @return 
     */
    @Override
    public boolean hasSubTasksToBeDone() {
        return !m_subTaskManager.isEmpty();
    }
    

    @Override
    public void applyPriorityChangement(PriorityChangement priorityChangement) {
        super.applyPriorityChangement(priorityChangement);
        
        m_subTaskManager.givePriorityTo(priorityChangement.getSubTaskId(), priorityChangement.getStartIndex(), priorityChangement.getStopIndex());
        
    }

    
    @Override
    public void resetPriority() {
        m_currentPriority = m_defaultPriority;
        m_subTaskManager.resetPriority();
    }
    
    
    /**
     * Method called after the data has been fetched
     * @param success  boolean indicating if the fetch has succeeded
     */
    @Override
    public void callback(final boolean success, final boolean finished) {
        if (m_callback == null) {
            return;
        }

        final SubTask taskDone = m_subTaskManager.getCurrentTask();
        /*if (taskDone != null) {
            taskDone.setAllSubtaskFinished(!hasSubTasksToBeDone());
        }*/
        
        if (m_callback.mustBeCalledInAWT()) {
            // Callback must be executed in the Graphical thread (AWT)
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    if (m_callback != null) {
                        m_callback.run(success, m_id, taskDone, finished);
                    }
                }
            });
        } else {
            // Method called in the current thread
            // In this case, we assume the execution is fast.
            m_callback.run(success, m_id, taskDone, finished);
        }


    }
    
}
