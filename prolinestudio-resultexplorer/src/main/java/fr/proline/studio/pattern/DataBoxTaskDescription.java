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
package fr.proline.studio.pattern;

import fr.proline.studio.dam.taskinfo.TaskInfo;
import fr.proline.studio.rsmexplorer.gui.tasklog.TaskDescriptionPanel;

/**
 * Management of one TaskInfo to display
 * @author JM235353
 */
public class DataBoxTaskDescription extends AbstractDataBox {

    public DataBoxTaskDescription() {

        super(DataboxType.DataBoxTaskDescription, DataboxStyle.STYLE_UNKNOWN);
        
        // Name of this databox
        m_typeName = "Task";
        m_description = "Task Description";
        
        // Register in parameters
        GroupParameter inParameter = new GroupParameter();
        inParameter.addParameter(TaskInfo.class);
        registerInParameter(inParameter);
    }

    @Override
    public void createPanel() {
        TaskDescriptionPanel p = new TaskDescriptionPanel();
        p.setName(m_typeName);
        p.setDataBox(this);
        setDataBoxPanelInterface(p);
    }

    @Override
    public void dataChanged() {
        TaskInfo taskInfo = (TaskInfo) getData(TaskInfo.class);
        ((TaskDescriptionPanel)getDataBoxPanelInterface()).setTaskInfo(taskInfo);
    }
    
        
    
}
