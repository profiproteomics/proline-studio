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
package fr.proline.studio.dpm.task.jms;

import fr.proline.studio.dam.taskinfo.TaskError;
import fr.proline.studio.dam.taskinfo.TaskInfo;

/**
 *
 * @author JM235353
 */
public abstract class AbstractJMSCallback {
    
    private TaskError m_taskError = null;
    private TaskInfo m_taskInfo = null;
     
     
    /**
     * Returns if the callback must be called 
     * in the Graphical Thread (AWT)
     * @return 
     */
    public abstract boolean mustBeCalledInAWT();
    
    /**
     * Method called by the AccessServiceThread when the data is fetched
     * 
     * @param success   indicates if the loading of data has been a success or not
     */
    public abstract void run(boolean success);
    
    public TaskError getTaskError() {
        return m_taskError;
    }
    public void setTaskError(TaskError taskError) {
        m_taskError = taskError;
    }   
    
    public TaskInfo getTaskInfo() {
        return m_taskInfo;
    }
    
    public void setTaskInfo(TaskInfo taskInfo) {
        m_taskInfo = taskInfo;
    }
}
