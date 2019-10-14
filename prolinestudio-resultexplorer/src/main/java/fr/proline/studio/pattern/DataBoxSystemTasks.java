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

import fr.proline.studio.rsmexplorer.gui.SystemTasksPanel;

/**
 *
 * @author VD225637
 */
public class DataBoxSystemTasks extends AbstractDataBox  {
    
      
    public DataBoxSystemTasks() {
        super(DataboxType.DataBoxSystemTasks, DataboxStyle.STYLE_UNKNOWN);
        // Name of this databox
        m_typeName = "System Tasks List";
    }
    
    @Override
    public void createPanel() {
        SystemTasksPanel p = new SystemTasksPanel();
        p.setName(m_typeName);
        p.setDataBox(this);
        setDataBoxPanelInterface(p);               
    }

    @Override
    public void dataChanged() {
        //
    }
    
}
